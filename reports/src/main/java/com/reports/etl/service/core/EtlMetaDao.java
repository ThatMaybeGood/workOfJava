package com.reports.etl.service.core;

import com.reports.etl.entity.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ETL 元数据仓储层（基于独立 H2 库 JdbcTemplate）
 * 统一封装任务/数据源/映射/来源/WS配置/存储过程配置/日志的增删改查，
 * 替代 MyBatis-Plus Mapper，彻底避开与现有 Oracle 报表 Mapper 的冲突。
 */
@lombok.extern.slf4j.Slf4j
@Repository
public class EtlMetaDao {

    private final JdbcTemplate jdbc;

    public EtlMetaDao(@Qualifier("etlMetaJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 存量迁移（幂等）：把任务下挂的旧 ws/proc 子配置迁移为独立 etl_source，
     * 并回填 etl_task.source_id。source_id 已非空的任务直接跳过，天然幂等。
     * 旧表 etl_task_ws_config / etl_task_proc_config 保留只读不删。
     */
    @PostConstruct
    public void migrateLegacyConfigs() {
        try {
            List<EtlTask> tasks = jdbc.query("SELECT * FROM etl_task WHERE source_id IS NULL",
                    (rs, i) -> mapTask(rs));
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            int migrated = 0;
            for (EtlTask t : tasks) {
                EtlSource s = null;
                if ("WEBSERVICE".equals(t.getExtractType())) {
                    EtlWsConfig ws = getWsConfig(t.getId());
                    if (ws != null) {
                        s = new EtlSource();
                        s.setType("WS");
                        s.setConfigJson(configToJson(om, ws));
                    }
                } else if ("PROCEDURE".equals(t.getExtractType())) {
                    EtlProcConfig proc = getProcConfig(t.getId());
                    if (proc != null) {
                        s = new EtlSource();
                        s.setType("PROC");
                        s.setConfigJson(configToJson(om, proc));
                    }
                }
                if (s != null) {
                    s.setName("迁移来源-任务" + t.getId());
                    s.setSourceDsId(t.getSourceDsId());
                    Long sid = insertSource(s);
                    jdbc.update("UPDATE etl_task SET source_id=? WHERE id=?", sid, t.getId());
                    migrated++;
                }
            }
            if (migrated > 0) {
                log.info("ETL 旧子配置迁移完成，共迁移 {} 个任务的抽取来源", migrated);
            }
        } catch (Exception e) {
            // 迁移失败不阻断启动：旧任务仍走 source_id 为空的兼容路径
            log.error("ETL 旧子配置迁移失败: {}", e.getMessage(), e);
        }
    }

    private String configToJson(com.fasterxml.jackson.databind.ObjectMapper om, Object config) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = om.convertValue(config, Map.class);
            map.remove("id");
            map.remove("taskId");
            return om.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("旧子配置序列化失败: " + e.getMessage(), e);
        }
    }

    // ==================== ETL 任务 ====================

    public List<EtlTask> listTasks() {
        return jdbc.query("SELECT * FROM etl_task ORDER BY create_time DESC",
                (rs, i) -> mapTask(rs));
    }

    public EtlTask getTask(Long id) {
        List<EtlTask> list = jdbc.query("SELECT * FROM etl_task WHERE id=?", (rs, i) -> mapTask(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insertTask(EtlTask t) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_task(name, extract_type, source_id, source_ds_id, target_ds_id, target_table, write_mode, query_index_cols, update_cols, cron, enabled, max_rows, batch_size, incremental, inc_field, inc_placeholder, retry_count, alert_config_json) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    new String[]{"ID"});
            ps.setString(1, t.getName());
            ps.setString(2, t.getExtractType());
            ps.setObject(3, t.getSourceId());
            ps.setObject(4, t.getSourceDsId());
            ps.setObject(5, t.getTargetDsId());
            ps.setString(6, t.getTargetTable());
            ps.setString(7, t.getWriteMode() != null ? t.getWriteMode() : "INSERT");
            ps.setString(8, t.getQueryIndexCols());
            ps.setString(9, t.getUpdateCols());
            ps.setString(10, t.getCron());
            ps.setInt(11, t.getEnabled() != null ? t.getEnabled() : 0);
            ps.setInt(12, t.getMaxRows() != null ? t.getMaxRows() : 10000);
            ps.setInt(13, t.getBatchSize() != null ? t.getBatchSize() : 500);
            ps.setInt(14, t.getIncremental() != null ? t.getIncremental() : 0);
            ps.setString(15, t.getIncField());
            ps.setString(16, t.getIncPlaceholder());
            ps.setInt(17, t.getRetryCount() != null ? t.getRetryCount() : 0);
            ps.setString(18, t.getAlertConfigJson());
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public void updateTask(EtlTask t) {
        jdbc.update("UPDATE etl_task SET name=?, extract_type=?, source_id=?, source_ds_id=?, target_ds_id=?, target_table=?, write_mode=?, " +
                        "query_index_cols=?, update_cols=?, cron=?, enabled=?, max_rows=?, batch_size=?, incremental=?, inc_field=?, " +
                        "inc_placeholder=?, retry_count=?, alert_config_json=? WHERE id=?",
                t.getName(), t.getExtractType(), t.getSourceId(), t.getSourceDsId(), t.getTargetDsId(), t.getTargetTable(),
                t.getWriteMode(), t.getQueryIndexCols(), t.getUpdateCols(), t.getCron(), t.getEnabled(),
                t.getMaxRows(), t.getBatchSize(), t.getIncremental(), t.getIncField(), t.getIncPlaceholder(),
                t.getRetryCount(), t.getAlertConfigJson(), t.getId());
    }

    public void deleteTask(Long id) {
        jdbc.update("DELETE FROM etl_task WHERE id=?", id);
    }

    // ==================== 抽取来源 ====================

    public List<EtlSource> listSources() {
        return jdbc.query("SELECT * FROM etl_source ORDER BY id DESC", (rs, i) -> mapSource(rs));
    }

    public EtlSource getSource(Long id) {
        List<EtlSource> list = jdbc.query("SELECT * FROM etl_source WHERE id=?",
                (rs, i) -> mapSource(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insertSource(EtlSource s) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_source(name, type, source_ds_id, config_json) VALUES(?,?,?,?)",
                    new String[]{"ID"});
            ps.setString(1, s.getName());
            ps.setString(2, s.getType());
            ps.setObject(3, s.getSourceDsId());
            ps.setString(4, s.getConfigJson());
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public void updateSource(EtlSource s) {
        jdbc.update("UPDATE etl_source SET name=?, type=?, source_ds_id=?, config_json=?, update_time=CURRENT_TIMESTAMP WHERE id=?",
                s.getName(), s.getType(), s.getSourceDsId(), s.getConfigJson(), s.getId());
    }

    public void deleteSource(Long id) {
        jdbc.update("DELETE FROM etl_source WHERE id=?", id);
    }

    public int countTasksBySourceId(Long sourceId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM etl_task WHERE source_id=?",
                Integer.class, sourceId);
        return n != null ? n : 0;
    }

    // ==================== 数据源 ====================

    public List<EtlDatasource> listDatasources() {
        return jdbc.query("SELECT * FROM etl_datasource ORDER BY id DESC", (rs, i) -> mapDs(rs));
    }

    public EtlDatasource getDatasource(Long id) {
        List<EtlDatasource> list = jdbc.query("SELECT * FROM etl_datasource WHERE id=?",
                (rs, i) -> mapDs(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insertDatasource(EtlDatasource d) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_datasource(name, db_type, driver_class, url, username, password, role, pool_initial_size, pool_max_active, enabled) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?)",
                    new String[]{"ID"});
            ps.setString(1, d.getName());
            ps.setString(2, d.getDbType());
            ps.setString(3, d.getDriverClass());
            ps.setString(4, d.getUrl());
            ps.setString(5, d.getUsername());
            ps.setString(6, d.getPassword());
            ps.setString(7, d.getRole() != null ? d.getRole() : "SOURCE");
            ps.setInt(8, d.getPoolInitialSize() != null ? d.getPoolInitialSize() : 2);
            ps.setInt(9, d.getPoolMaxActive() != null ? d.getPoolMaxActive() : 10);
            ps.setInt(10, d.getEnabled() != null ? d.getEnabled() : 1);
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public void updateDatasource(EtlDatasource d) {
        jdbc.update("UPDATE etl_datasource SET name=?, db_type=?, driver_class=?, url=?, username=?, password=?, role=?, " +
                        "pool_initial_size=?, pool_max_active=?, enabled=? WHERE id=?",
                d.getName(), d.getDbType(), d.getDriverClass(), d.getUrl(), d.getUsername(), d.getPassword(),
                d.getRole(), d.getPoolInitialSize(), d.getPoolMaxActive(), d.getEnabled(), d.getId());
    }

    public void deleteDatasource(Long id) {
        jdbc.update("DELETE FROM etl_datasource WHERE id=?", id);
    }

    // ==================== WebService 配置 ====================

    public EtlWsConfig getWsConfig(Long taskId) {
        List<EtlWsConfig> list = jdbc.query("SELECT * FROM etl_task_ws_config WHERE task_id=?",
                (rs, i) -> mapWsConfig(rs), taskId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void insertWsConfig(EtlWsConfig c) {
        jdbc.update("INSERT INTO etl_task_ws_config(id, task_id, ws_type, url, soap_action, request_body_template, response_path, headers_json, extract_params_json, max_pages, max_rows, batch_size) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                c.getId(), c.getTaskId(), c.getWsType(), c.getUrl(), c.getSoapAction(), c.getRequestBodyTemplate(),
                c.getResponsePath(), c.getHeadersJson(), c.getExtractParamsJson(), c.getMaxPages(), c.getMaxRows(), c.getBatchSize());
    }

    public void updateWsConfig(EtlWsConfig c) {
        jdbc.update("UPDATE etl_task_ws_config SET ws_type=?, url=?, soap_action=?, request_body_template=?, response_path=?, headers_json=?, extract_params_json=?, max_pages=?, max_rows=?, batch_size=? WHERE task_id=?",
                c.getWsType(), c.getUrl(), c.getSoapAction(), c.getRequestBodyTemplate(), c.getResponsePath(),
                c.getHeadersJson(), c.getExtractParamsJson(), c.getMaxPages(), c.getMaxRows(), c.getBatchSize(), c.getTaskId());
    }

    public void deleteWsConfigByTask(Long taskId) {
        jdbc.update("DELETE FROM etl_task_ws_config WHERE task_id=?", taskId);
    }

    // ==================== 存储过程配置 ====================

    public EtlProcConfig getProcConfig(Long taskId) {
        List<EtlProcConfig> list = jdbc.query("SELECT * FROM etl_task_proc_config WHERE task_id=?",
                (rs, i) -> mapProcConfig(rs), taskId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void insertProcConfig(EtlProcConfig c) {
        jdbc.update("INSERT INTO etl_task_proc_config(id, task_id, proc_name, call_template, cursor_param_name, cursor_param_idx, in_params_json, max_pages, max_rows, batch_size) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?)",
                c.getId(), c.getTaskId(), c.getProcName(), c.getCallTemplate(), c.getCursorParamName(),
                c.getCursorParamIdx(), c.getInParamsJson(), c.getMaxPages(), c.getMaxRows(), c.getBatchSize());
    }

    public void updateProcConfig(EtlProcConfig c) {
        jdbc.update("UPDATE etl_task_proc_config SET proc_name=?, call_template=?, cursor_param_name=?, cursor_param_idx=?, in_params_json=?, max_pages=?, max_rows=?, batch_size=? WHERE task_id=?",
                c.getProcName(), c.getCallTemplate(), c.getCursorParamName(), c.getCursorParamIdx(),
                c.getInParamsJson(), c.getMaxPages(), c.getMaxRows(), c.getBatchSize(), c.getTaskId());
    }

    public void deleteProcConfigByTask(Long taskId) {
        jdbc.update("DELETE FROM etl_task_proc_config WHERE task_id=?", taskId);
    }

    // ==================== 字段映射 ====================

    public List<EtlMapping> listMappings(Long taskId) {
        return jdbc.query("SELECT * FROM etl_mapping WHERE task_id=? ORDER BY sort_order ASC, id ASC",
                (rs, i) -> mapMapping(rs), taskId);
    }

    public Long insertMapping(EtlMapping m) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_mapping(task_id, src_field, tgt_field, default_value, is_update_col, src_type, tgt_type, sort_order) " +
                            "VALUES(?,?,?,?,?,?,?,?)",
                    new String[]{"ID"});
            ps.setLong(1, m.getTaskId());
            ps.setString(2, m.getSrcField());
            ps.setString(3, m.getTgtField());
            ps.setString(4, m.getDefaultValue());
            ps.setInt(5, m.getIsUpdateCol() != null ? m.getIsUpdateCol() : 0);
            ps.setString(6, m.getSrcType());
            ps.setString(7, m.getTgtType());
            ps.setInt(8, m.getSortOrder() != null ? m.getSortOrder() : 0);
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public void updateMapping(EtlMapping m) {
        jdbc.update("UPDATE etl_mapping SET src_field=?, tgt_field=?, default_value=?, is_update_col=?, src_type=?, tgt_type=?, sort_order=? WHERE id=?",
                m.getSrcField(), m.getTgtField(), m.getDefaultValue(), m.getIsUpdateCol(), m.getSrcType(),
                m.getTgtType(), m.getSortOrder(), m.getId());
    }

    public void deleteMapping(Long id) {
        jdbc.update("DELETE FROM etl_mapping WHERE id=?", id);
    }

    public void deleteMappingsByTask(Long taskId) {
        jdbc.update("DELETE FROM etl_mapping WHERE task_id=?", taskId);
    }

    // ==================== 任务日志 & 步骤日志 ====================

    public Long insertTaskLog(EtlTaskLog log) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_task_log(task_id, task_name, start_time, status, trigger_type) " +
                            "VALUES(?,?,?,?,?)",
                    new String[]{"ID"});
            ps.setLong(1, log.getTaskId());
            ps.setString(2, log.getTaskName());
            ps.setTimestamp(3, new Timestamp(log.getStartTime() != null ? log.getStartTime().getTime() : System.currentTimeMillis()));
            ps.setString(4, log.getStatus() != null ? log.getStatus() : "RUNNING");
            ps.setString(5, log.getTriggerType() != null ? log.getTriggerType() : "MANUAL");
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public void updateTaskLog(EtlTaskLog log) {
        jdbc.update("UPDATE etl_task_log SET end_time=?, status=?, extracted_rows=?, written_rows=?, error_msg=? WHERE id=?",
                log.getEndTime() != null ? new Timestamp(log.getEndTime().getTime()) : null,
                log.getStatus(), log.getExtractedRows(), log.getWrittenRows(), log.getErrorMsg(), log.getId());
    }

    public List<EtlTaskLog> listTaskLogs(Long taskId, int limit) {
        if (taskId == null) {
            return jdbc.query("SELECT * FROM etl_task_log ORDER BY id DESC LIMIT ?",
                    (rs, i) -> mapTaskLog(rs), limit);
        }
        return jdbc.query("SELECT * FROM etl_task_log WHERE task_id=? ORDER BY id DESC LIMIT ?",
                (rs, i) -> mapTaskLog(rs), taskId, limit);
    }

    public Long insertStepLog(EtlStepLog s) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO etl_step_log(log_id, step_name, status, start_time, end_time, duration_ms, rows_count, detail) " +
                            "VALUES(?,?,?,?,?,?,?,?)",
                    new String[]{"ID"});
            ps.setLong(1, s.getLogId());
            ps.setString(2, s.getStepName());
            ps.setString(3, s.getStatus());
            ps.setTimestamp(4, s.getStartTime() != null ? new Timestamp(s.getStartTime().getTime()) : null);
            ps.setTimestamp(5, s.getEndTime() != null ? new Timestamp(s.getEndTime().getTime()) : null);
            ps.setLong(6, s.getDurationMs() != null ? s.getDurationMs() : 0L);
            ps.setInt(7, s.getRowsCount() != null ? s.getRowsCount() : 0);
            ps.setString(8, s.getDetail());
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key != null ? key.longValue() : null;
    }

    public List<EtlStepLog> listStepLogs(Long logId) {
        return jdbc.query("SELECT * FROM etl_step_log WHERE log_id=? ORDER BY id ASC",
                (rs, i) -> mapStepLog(rs), logId);
    }

    // ==================== 日志保留策略 ====================

    public EtlLogConfig getLogConfig() {
        List<EtlLogConfig> list = jdbc.query("SELECT * FROM etl_log_config WHERE id=1",
                (rs, i) -> mapLogConfig(rs));
        return list.isEmpty() ? null : list.get(0);
    }

    public void saveLogConfig(EtlLogConfig c) {
        jdbc.update("MERGE INTO etl_log_config (id, save_days, auto_clean) KEY(id) VALUES(1, ?, ?)",
                c.getSaveDays() != null ? c.getSaveDays() : 30,
                c.getAutoClean() != null ? c.getAutoClean() : 1);
    }

    // ==================== RowMapper 映射 ====================

    private EtlTask mapTask(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlTask t = new EtlTask();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setExtractType(rs.getString("extract_type"));
        Object sourceId = rs.getObject("source_id");
        t.setSourceId(sourceId != null ? rs.getLong("source_id") : null);
        t.setSourceDsId(rs.getLong("source_ds_id"));
        t.setTargetDsId(rs.getLong("target_ds_id"));
        t.setTargetTable(rs.getString("target_table"));
        t.setWriteMode(rs.getString("write_mode"));
        t.setQueryIndexCols(rs.getString("query_index_cols"));
        t.setUpdateCols(rs.getString("update_cols"));
        t.setCron(rs.getString("cron"));
        t.setEnabled(rs.getInt("enabled"));
        t.setMaxRows(rs.getInt("max_rows"));
        t.setBatchSize(rs.getInt("batch_size"));
        t.setIncremental(rs.getInt("incremental"));
        t.setIncField(rs.getString("inc_field"));
        t.setIncPlaceholder(rs.getString("inc_placeholder"));
        t.setRetryCount(rs.getInt("retry_count"));
        t.setAlertConfigJson(rs.getString("alert_config_json"));
        t.setCreateTime(rs.getTimestamp("create_time"));
        t.setUpdateTime(rs.getTimestamp("update_time"));
        return t;
    }

    private EtlDatasource mapDs(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlDatasource d = new EtlDatasource();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setDbType(rs.getString("db_type"));
        d.setDriverClass(rs.getString("driver_class"));
        d.setUrl(rs.getString("url"));
        d.setUsername(rs.getString("username"));
        d.setPassword(rs.getString("password"));
        d.setRole(rs.getString("role"));
        d.setPoolInitialSize(rs.getInt("pool_initial_size"));
        d.setPoolMaxActive(rs.getInt("pool_max_active"));
        d.setEnabled(rs.getInt("enabled"));
        d.setCreateTime(rs.getTimestamp("create_time"));
        d.setUpdateTime(rs.getTimestamp("update_time"));
        return d;
    }

    private EtlWsConfig mapWsConfig(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlWsConfig c = new EtlWsConfig();
        c.setId(rs.getLong("id"));
        c.setTaskId(rs.getLong("task_id"));
        c.setWsType(rs.getString("ws_type"));
        c.setUrl(rs.getString("url"));
        c.setSoapAction(rs.getString("soap_action"));
        c.setRequestBodyTemplate(rs.getString("request_body_template"));
        c.setResponsePath(rs.getString("response_path"));
        c.setHeadersJson(rs.getString("headers_json"));
        c.setExtractParamsJson(rs.getString("extract_params_json"));
        c.setMaxPages(rs.getInt("max_pages"));
        c.setMaxRows(rs.getInt("max_rows"));
        c.setBatchSize(rs.getInt("batch_size"));
        return c;
    }

    private EtlProcConfig mapProcConfig(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlProcConfig c = new EtlProcConfig();
        c.setId(rs.getLong("id"));
        c.setTaskId(rs.getLong("task_id"));
        c.setProcName(rs.getString("proc_name"));
        c.setCallTemplate(rs.getString("call_template"));
        c.setCursorParamName(rs.getString("cursor_param_name"));
        c.setCursorParamIdx(rs.getInt("cursor_param_idx"));
        c.setInParamsJson(rs.getString("in_params_json"));
        c.setMaxPages(rs.getInt("max_pages"));
        c.setMaxRows(rs.getInt("max_rows"));
        c.setBatchSize(rs.getInt("batch_size"));
        return c;
    }

    private EtlMapping mapMapping(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlMapping m = new EtlMapping();
        m.setId(rs.getLong("id"));
        m.setTaskId(rs.getLong("task_id"));
        m.setSrcField(rs.getString("src_field"));
        m.setTgtField(rs.getString("tgt_field"));
        m.setDefaultValue(rs.getString("default_value"));
        m.setIsUpdateCol(rs.getInt("is_update_col"));
        m.setSrcType(rs.getString("src_type"));
        m.setTgtType(rs.getString("tgt_type"));
        m.setSortOrder(rs.getInt("sort_order"));
        m.setCreateTime(rs.getTimestamp("create_time"));
        return m;
    }

    private EtlTaskLog mapTaskLog(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlTaskLog l = new EtlTaskLog();
        l.setId(rs.getLong("id"));
        l.setTaskId(rs.getLong("task_id"));
        l.setTaskName(rs.getString("task_name"));
        l.setStartTime(rs.getTimestamp("start_time"));
        l.setEndTime(rs.getTimestamp("end_time"));
        l.setStatus(rs.getString("status"));
        l.setExtractedRows(rs.getInt("extracted_rows"));
        l.setWrittenRows(rs.getInt("written_rows"));
        l.setErrorMsg(rs.getString("error_msg"));
        l.setTriggerType(rs.getString("trigger_type"));
        l.setCreateTime(rs.getTimestamp("create_time"));
        return l;
    }

    private EtlStepLog mapStepLog(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlStepLog s = new EtlStepLog();
        s.setId(rs.getLong("id"));
        s.setLogId(rs.getLong("log_id"));
        s.setStepName(rs.getString("step_name"));
        s.setStatus(rs.getString("status"));
        s.setStartTime(rs.getTimestamp("start_time"));
        s.setEndTime(rs.getTimestamp("end_time"));
        s.setDurationMs(rs.getLong("duration_ms"));
        s.setRowsCount(rs.getInt("rows_count"));
        s.setDetail(rs.getString("detail"));
        s.setCreateTime(rs.getTimestamp("create_time"));
        return s;
    }

    private EtlSource mapSource(java.sql.ResultSet rs) throws java.sql.SQLException {
        EtlSource s = new EtlSource();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setType(rs.getString("type"));
        Object dsId = rs.getObject("source_ds_id");
        s.setSourceDsId(dsId != null ? rs.getLong("source_ds_id") : null);
        s.setConfigJson(rs.getString("config_json"));
        s.setCreateTime(rs.getTimestamp("create_time"));
        s.setUpdateTime(rs.getTimestamp("update_time"));
        return s;
    }

    private EtlLogConfig mapLogConfig(java.sql.ResultSet rs) throws java.sql.SQLException {        EtlLogConfig c = new EtlLogConfig();
        c.setId(rs.getLong("id"));
        c.setSaveDays(rs.getInt("save_days"));
        c.setAutoClean(rs.getInt("auto_clean"));
        c.setUpdateTime(rs.getTimestamp("update_time"));
        return c;
    }
}