package com.reports.service.impl;

import com.reports.config.PageConfig;
import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.outpatient.operation.OverviewData;
import com.reports.dto.response.outpatient.operation.TableItem;
import com.reports.entity.OutpatientOperationEntity;
import com.reports.mapper.OutpatientOperationMapper;
import com.reports.service.OutpatientOperationService;
import com.reports.util.OraclePageUtil;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 门诊运行数据统计服务实现
 */
@Slf4j
@Service
public class OutpatientOperationServiceImpl implements OutpatientOperationService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    OutpatientOperationMapper operationMapper;

    @Autowired
    public OutpatientOperationServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientOperationRequest request) {
        log.info("查询门诊运行概览数据，mode={}, startDate={}, endDate={}",
                dataConfig.getMode(), request.getStartDate(), request.getEndDate());

        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<TableItem> queryTable(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊运行表格数据，mode={}, page={}, pageSize={}",
                dataConfig.getMode(), page, pageSize);

        if (dataConfig.isMock()) {
            return queryTableMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryTableByJdbc(request, page, pageSize);
        } else {
            return queryTableByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientOperationRequest request) {
        SeqUtil.next();
        log.info("使用 Mock 数据返回概览");

        OverviewData overview = new OverviewData();
        overview.setTotalVisits(12536);
        overview.setAppointmentRate("83.10%");
        overview.setVisitCount(112);
        overview.setExamRate("56.50%");
        overview.setEfficiency(27.5);
        overview.setEffectiveUnits(112);
        overview.setTotalUnits(251);
        overview.setFamousExpert(112);
        overview.setSpecialExpert(112);
        overview.setKnownExpert(112);
        overview.setExpertA(112);
        overview.setExpertB(112);
        overview.setOrdinary(112);
        overview.setUnitFamousEffective(52);
        overview.setUnitFamousTotal(112);
        overview.setUnitSpecialEffective(52);
        overview.setUnitSpecialTotal(112);
        overview.setUnitKnownEffective(52);
        overview.setUnitKnownTotal(112);
        overview.setUnitAEffective(52);
        overview.setUnitATotal(112);
        overview.setUnitBEffective(52);
        overview.setUnitBTotal(112);
        overview.setUnitOrdinaryEffective(52);
        overview.setUnitOrdinaryTotal(112);

        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        log.info("使用 Mock 数据返回表格");

        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科门诊" + (i + 1));
            item.setTotalVisits(350 + i * 10);
            item.setAppointmentRate("70.00%");
            item.setVisitCount(30 + i);
            item.setExamRate("75.00%");
            item.setEfficiency(25.00 + i);
            item.setEffectiveUnits(45 + i);
            item.setTotalUnits(50 + i);
            item.setFamousExpert(2 + i);
            item.setSpecialExpert(3 + i);
            item.setKnownExpert(2 + i);
            item.setExpertA(5 + i);
            item.setExpertB(4 + i);
            item.setOrdinary(2 + i);
            item.setUnitFamousEffective(2 + i);
            item.setUnitFamousTotal(4 + i);
            item.setUnitSpecialEffective(2 + i);
            item.setUnitSpecialTotal(4 + i);
            item.setUnitKnownEffective(2 + i);
            item.setUnitKnownTotal(4 + i);
            item.setUnitAEffective(2 + i);
            item.setUnitATotal(4 + i);
            item.setUnitBEffective(2 + i);
            item.setUnitBTotal(4 + i);
            item.setUnitOrdinaryEffective(2 + i);
            item.setUnitOrdinaryTotal(4 + i);
            list.add(item);
        }

        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientOperationRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");

        String sql = "SELECT SUM(NVL(total_visits, 0)) AS totalVisits, " +
                "ROUND(AVG(NVL(appointment_rate, 0)), 2) || '%' AS appointmentRate, " +
                "SUM(NVL(visit_count, 0)) AS visitCount, " +
                "ROUND(AVG(NVL(exam_rate, 0)), 2) || '%' AS examRate, " +
                "ROUND(AVG(NVL(efficiency, 0)), 2) AS efficiency, " +
                "SUM(NVL(effective_units, 0)) AS effectiveUnits, " +
                "SUM(NVL(total_units, 0)) AS totalUnits, " +
                "SUM(NVL(famous_expert, 0)) AS famousExpert, " +
                "SUM(NVL(special_expert, 0)) AS specialExpert, " +
                "SUM(NVL(known_expert, 0)) AS knownExpert, " +
                "SUM(NVL(expert_a, 0)) AS expertA, " +
                "SUM(NVL(expert_b, 0)) AS expertB, " +
                "SUM(NVL(ordinary, 0)) AS ordinary, " +
                "SUM(NVL(unit_famous_effective, 0)) AS unitFamousEffective, " +
                "SUM(NVL(unit_famous_total, 0)) AS unitFamousTotal, " +
                "SUM(NVL(unit_special_effective, 0)) AS unitSpecialEffective, " +
                "SUM(NVL(unit_special_total, 0)) AS unitSpecialTotal, " +
                "SUM(NVL(unit_known_effective, 0)) AS unitKnownEffective, " +
                "SUM(NVL(unit_known_total, 0)) AS unitKnownTotal, " +
                "SUM(NVL(unit_a_effective, 0)) AS unitAEffective, " +
                "SUM(NVL(unit_a_total, 0)) AS unitATotal, " +
                "SUM(NVL(unit_b_effective, 0)) AS unitBEffective, " +
                "SUM(NVL(unit_b_total, 0)) AS unitBTotal, " +
                "SUM(NVL(unit_ordinary_effective, 0)) AS unitOrdinaryEffective, " +
                "SUM(NVL(unit_ordinary_total, 0)) AS unitOrdinaryTotal " +
                "FROM TR_OUTP_OP " +
                "WHERE stat_date BETWEEN ? AND ? ";

        List<Object> params = new ArrayList<>();
        params.add(request.getStartDate());
        params.add(request.getEndDate());
        if (request.getDeptCode() != null && !request.getDeptCode().isEmpty()) {
            sql += "AND dept_code = ? ";
            params.add(request.getDeptCode());
        }

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, params.toArray());
            return buildOverviewData(result);
        } catch (Exception e) {
            log.warn("JdbcTemplate 查询失败，回退到 Mock 数据", e);
            return queryOverviewMock(request);
        }
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        log.info("使用 JdbcTemplate 查询表格数据");

        String baseSql = "SELECT dept_code AS deptCode, dept_name AS deptName, " +
                "SUM(NVL(total_visits, 0)) AS totalVisits, " +
                "ROUND(AVG(NVL(appointment_rate, 0)), 2) || '%' AS appointmentRate, " +
                "SUM(NVL(visit_count, 0)) AS visitCount, " +
                "ROUND(AVG(NVL(exam_rate, 0)), 2) || '%' AS examRate, " +
                "ROUND(AVG(NVL(efficiency, 0)), 2) AS efficiency, " +
                "SUM(NVL(effective_units, 0)) AS effectiveUnits, " +
                "SUM(NVL(total_units, 0)) AS totalUnits, " +
                "SUM(NVL(famous_expert, 0)) AS famousExpert, " +
                "SUM(NVL(special_expert, 0)) AS specialExpert, " +
                "SUM(NVL(known_expert, 0)) AS knownExpert, " +
                "SUM(NVL(expert_a, 0)) AS expertA, " +
                "SUM(NVL(expert_b, 0)) AS expertB, " +
                "SUM(NVL(ordinary, 0)) AS ordinary, " +
                "SUM(NVL(unit_famous_effective, 0)) AS unitFamousEffective, " +
                "SUM(NVL(unit_famous_total, 0)) AS unitFamousTotal, " +
                "SUM(NVL(unit_special_effective, 0)) AS unitSpecialEffective, " +
                "SUM(NVL(unit_special_total, 0)) AS unitSpecialTotal, " +
                "SUM(NVL(unit_known_effective, 0)) AS unitKnownEffective, " +
                "SUM(NVL(unit_known_total, 0)) AS unitKnownTotal, " +
                "SUM(NVL(unit_a_effective, 0)) AS unitAEffective, " +
                "SUM(NVL(unit_a_total, 0)) AS unitATotal, " +
                "SUM(NVL(unit_b_effective, 0)) AS unitBEffective, " +
                "SUM(NVL(unit_b_total, 0)) AS unitBTotal, " +
                "SUM(NVL(unit_ordinary_effective, 0)) AS unitOrdinaryEffective, " +
                "SUM(NVL(unit_ordinary_total, 0)) AS unitOrdinaryTotal " +
                "FROM TR_OUTP_OP " +
                "WHERE stat_date BETWEEN ? AND ? ";

        List<Object> params = new ArrayList<>();
        params.add(request.getStartDate());
        params.add(request.getEndDate());
        if (request.getDeptCode() != null && !request.getDeptCode().isEmpty()) {
            baseSql += "AND dept_code = ? ";
            params.add(request.getDeptCode());
        }
        baseSql += "GROUP BY dept_code, dept_name ORDER BY totalVisits DESC";

        String pageSql = OraclePageUtil.wrapOffsetFetchPage(baseSql, page, pageSize);

        try {
            List<TableItem> list = jdbcTemplate.query(pageSql, new RowMapper<TableItem>() {
                @Override
                public TableItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return mapResultSetToTableItem(rs);
                }
            }, params.toArray());

            String countSql = "SELECT COUNT(DISTINCT dept_code) FROM TR_OUTP_OP WHERE stat_date BETWEEN ? AND ? ";
            List<Object> countParams = new ArrayList<>();
            countParams.add(request.getStartDate());
            countParams.add(request.getEndDate());
            if (request.getDeptCode() != null && !request.getDeptCode().isEmpty()) {
                countSql += "AND dept_code = ? ";
                countParams.add(request.getDeptCode());
            }
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());

            return PageResult.of(list, total != null ? total : 0L, page, pageSize);
        } catch (Exception e) {
            log.warn("查询失败", e);
            return queryTableMock(request, page, pageSize);
        }
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientOperationRequest request) {
//        log.info("使用 MyBatis-Plus 查询概览数据");

        try {
            OutpatientOperationEntity entity = operationMapper.querySummaryByDateAndDept(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDeptCode());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询概览数据失败", e);
//            return queryOverviewMock(request);
            return null;
         }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientOperationRequest request, Integer page, Integer pageSize) {
//        log.info("使用 MyBatis-Plus 查询表格数据");

        try {
            List<OutpatientOperationEntity> rows = operationMapper.queryGroupByDept(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDeptCode());

            List<TableItem> allItems = new ArrayList<>();
            for (OutpatientOperationEntity row : rows) {
                allItems.add(buildTableItem(row));
            }

            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();

            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询表格数据失败", e);
//            return queryTableMock(request, page, pageSize);
             return null;

        }

    }

    // ==================== 工具方法 ====================

    private OverviewData buildOverviewData(Map<String, Object> map) {
        if (map == null) {
            map = new java.util.HashMap<>();
        }
        OverviewData overview = new OverviewData();
        overview.setTotalVisits(getInt(map, "TOTALVISITS"));
        overview.setAppointmentRate(getString(map, "APPOINTMENTRATE"));
        overview.setVisitCount(getInt(map, "VISITCOUNT"));
        overview.setExamRate(getString(map, "EXAMRATE"));
        overview.setEfficiency(getDouble(map, "EFFICIENCY"));
        overview.setEffectiveUnits(getInt(map, "EFFECTIVEUNITS"));
        overview.setTotalUnits(getInt(map, "TOTALUNITS"));
        overview.setFamousExpert(getInt(map, "FAMOUSEXPERT"));
        overview.setSpecialExpert(getInt(map, "SPECIALEXPERT"));
        overview.setKnownExpert(getInt(map, "KNOWNEXPERT"));
        overview.setExpertA(getInt(map, "EXPERTA"));
        overview.setExpertB(getInt(map, "EXPERTB"));
        overview.setOrdinary(getInt(map, "ORDINARY"));
        overview.setUnitFamousEffective(getInt(map, "UNITFAMOUSEFFECTIVE"));
        overview.setUnitFamousTotal(getInt(map, "UNITFAMOUSTOTAL"));
        overview.setUnitSpecialEffective(getInt(map, "UNITSPECIALEFFECTIVE"));
        overview.setUnitSpecialTotal(getInt(map, "UNITSPECIALTOTAL"));
        overview.setUnitKnownEffective(getInt(map, "UNITKNOWNEFFECTIVE"));
        overview.setUnitKnownTotal(getInt(map, "UNITKNOWNTOTAL"));
        overview.setUnitAEffective(getInt(map, "UNITAEFFECTIVE"));
        overview.setUnitATotal(getInt(map, "UNITATOTAL"));
        overview.setUnitBEffective(getInt(map, "UNITBEFFECTIVE"));
        overview.setUnitBTotal(getInt(map, "UNITBTOTAL"));
        overview.setUnitOrdinaryEffective(getInt(map, "UNITORDINARYEFFECTIVE"));
        overview.setUnitOrdinaryTotal(getInt(map, "UNITORDINARYTOTAL"));
        return overview;
    }

    private OverviewData buildOverviewData(OutpatientOperationEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        OverviewData overview = new OverviewData();
        overview.setTotalVisits(entity.getTotalVisits());
        overview.setAppointmentRate(entity.getAppointmentRate());
        overview.setVisitCount(entity.getVisitCount());
        overview.setExamRate(entity.getExamRate());
        overview.setEfficiency(entity.getEfficiency() != null ? entity.getEfficiency().doubleValue() : 0.0);
        overview.setEffectiveUnits(entity.getEffectiveUnits());
        overview.setTotalUnits(entity.getTotalUnits());
        overview.setFamousExpert(entity.getFamousExpert());
        overview.setSpecialExpert(entity.getSpecialExpert());
        overview.setKnownExpert(entity.getKnownExpert());
        overview.setExpertA(entity.getExpertA());
        overview.setExpertB(entity.getExpertB());
        overview.setOrdinary(entity.getOrdinary());
        overview.setUnitFamousEffective(entity.getUnitFamousEffective());
        overview.setUnitFamousTotal(entity.getUnitFamousTotal());
        overview.setUnitSpecialEffective(entity.getUnitSpecialEffective());
        overview.setUnitSpecialTotal(entity.getUnitSpecialTotal());
        overview.setUnitKnownEffective(entity.getUnitKnownEffective());
        overview.setUnitKnownTotal(entity.getUnitKnownTotal());
        overview.setUnitAEffective(entity.getUnitAEffective());
        overview.setUnitATotal(entity.getUnitATotal());
        overview.setUnitBEffective(entity.getUnitBEffective());
        overview.setUnitBTotal(entity.getUnitBTotal());
        overview.setUnitOrdinaryEffective(entity.getUnitOrdinaryEffective());
        overview.setUnitOrdinaryTotal(entity.getUnitOrdinaryTotal());
        return overview;
    }

    private TableItem buildTableItem(OutpatientOperationEntity entity) {
        if (entity == null) {
            return new TableItem();
        }
        TableItem item = new TableItem();
        item.setDeptCode(entity.getDeptCode());
        item.setDeptName(entity.getDeptName());
        item.setTotalVisits(entity.getTotalVisits());
        item.setAppointmentRate(entity.getAppointmentRate());
        item.setVisitCount(entity.getVisitCount());
        item.setExamRate(entity.getExamRate());
        item.setEfficiency(entity.getEfficiency() != null ? entity.getEfficiency().doubleValue() : 0.0);
        item.setEffectiveUnits(entity.getEffectiveUnits());
        item.setTotalUnits(entity.getTotalUnits());
        item.setFamousExpert(entity.getFamousExpert());
        item.setSpecialExpert(entity.getSpecialExpert());
        item.setKnownExpert(entity.getKnownExpert());
        item.setExpertA(entity.getExpertA());
        item.setExpertB(entity.getExpertB());
        item.setOrdinary(entity.getOrdinary());
        item.setUnitFamousEffective(entity.getUnitFamousEffective());
        item.setUnitFamousTotal(entity.getUnitFamousTotal());
        item.setUnitSpecialEffective(entity.getUnitSpecialEffective());
        item.setUnitSpecialTotal(entity.getUnitSpecialTotal());
        item.setUnitKnownEffective(entity.getUnitKnownEffective());
        item.setUnitKnownTotal(entity.getUnitKnownTotal());
        item.setUnitAEffective(entity.getUnitAEffective());
        item.setUnitATotal(entity.getUnitATotal());
        item.setUnitBEffective(entity.getUnitBEffective());
        item.setUnitBTotal(entity.getUnitBTotal());
        item.setUnitOrdinaryEffective(entity.getUnitOrdinaryEffective());
        item.setUnitOrdinaryTotal(entity.getUnitOrdinaryTotal());
        return item;
    }

    private TableItem mapResultSetToTableItem(ResultSet rs) throws SQLException {
        TableItem item = new TableItem();
        item.setDeptCode(rs.getString("DEPTCODE"));
        item.setDeptName(rs.getString("DEPTNAME"));
        item.setTotalVisits(rs.getInt("TOTALVISITS"));
        item.setAppointmentRate(rs.getString("APPOINTMENTRATE"));
        item.setVisitCount(rs.getInt("VISITCOUNT"));
        item.setExamRate(rs.getString("EXAMRATE"));
        item.setEfficiency(rs.getDouble("EFFICIENCY"));
        item.setEffectiveUnits(rs.getInt("EFFECTIVEUNITS"));
        item.setTotalUnits(rs.getInt("TOTALUNITS"));
        item.setFamousExpert(rs.getInt("FAMOUSEXPERT"));
        item.setSpecialExpert(rs.getInt("SPECIALEXPERT"));
        item.setKnownExpert(rs.getInt("KNOWNEXPERT"));
        item.setExpertA(rs.getInt("EXPERTA"));
        item.setExpertB(rs.getInt("EXPERTB"));
        item.setOrdinary(rs.getInt("ORDINARY"));
        item.setUnitFamousEffective(rs.getInt("UNITFAMOUSEFFECTIVE"));
        item.setUnitFamousTotal(rs.getInt("UNITFAMOUSTOTAL"));
        item.setUnitSpecialEffective(rs.getInt("UNITSPECIALEFFECTIVE"));
        item.setUnitSpecialTotal(rs.getInt("UNITSPECIALTOTAL"));
        item.setUnitKnownEffective(rs.getInt("UNITKNOWNEFFECTIVE"));
        item.setUnitKnownTotal(rs.getInt("UNITKNOWNTOTAL"));
        item.setUnitAEffective(rs.getInt("UNITAEFFECTIVE"));
        item.setUnitATotal(rs.getInt("UNITATOTAL"));
        item.setUnitBEffective(rs.getInt("UNITBEFFECTIVE"));
        item.setUnitBTotal(rs.getInt("UNITBTOTAL"));
        item.setUnitOrdinaryEffective(rs.getInt("UNITORDINARYEFFECTIVE"));
        item.setUnitOrdinaryTotal(rs.getInt("UNITORDINARYTOTAL"));
        return item;
    }

    private Object getMapValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        if (val != null) return val;
        val = map.get(key.toUpperCase());
        if (val != null) return val;
        val = map.get(key.toLowerCase());
        if (val != null) return val;
        // MyBatis 返回的 HashMap 可能使用驼峰别名作为 key
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = getMapValue(map, key);
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = getMapValue(map, key);
        return val != null ? val.toString() : "";
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = getMapValue(map, key);
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof BigDecimal) return ((BigDecimal) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

}
