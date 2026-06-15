package com.reports.service.impl;

import com.reports.config.PageConfig;
import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.*;
import com.reports.service.OutpatientOperationService;
import com.reports.util.OraclePageUtil;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 门诊运行数据统计服务实现
 *
 * 支持多种数据获取方式:
 * 1. Mock 模式: 返回模拟数据（默认）
 * 2. JdbcTemplate 模式: 在 Service 中直接写 SQL 执行
 * 3. MyBatis-Plus 模式: 通过实体类 + QueryWrapper 操作
 *
 * 切换方式: application.yml 中配置 reports.data.mode
 *   - mock:  Mock 数据
 *   - jdbc:  JdbcTemplate 直连 SQL
 *   - mybatis-plus: MyBatis-Plus 实体操作
 */
@Slf4j
@Service
public class OutpatientOperationServiceImpl implements OutpatientOperationService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

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

        VisitCountDetail visitCountDetail = new VisitCountDetail();
        visitCountDetail.setFamousExpert(112);
        visitCountDetail.setSpecialExpert(112);
        visitCountDetail.setKnownExpert(112);
        visitCountDetail.setExpertA(112);
        visitCountDetail.setExpertB(112);
        visitCountDetail.setOrdinary(112);
        overview.setVisitCountDetail(visitCountDetail);

        overview.setExamRate("56.50%");
        overview.setEfficiency(27.5);
        overview.setEffectiveUnits(112);
        overview.setTotalUnits(251);

        UnitDetail unitDetail = new UnitDetail();
        unitDetail.setFamousExpert(newUnitDetailItem(52, 112));
        unitDetail.setSpecialExpert(newUnitDetailItem(52, 112));
        unitDetail.setKnownExpert(newUnitDetailItem(52, 112));
        unitDetail.setExpertA(newUnitDetailItem(52, 112));
        unitDetail.setExpertB(newUnitDetailItem(52, 112));
        unitDetail.setOrdinary(newUnitDetailItem(52, 112));
        overview.setUnitDetail(unitDetail);

        return overview;
    }

    private PageResult<TableItem> queryTableMock(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        log.info("使用 Mock 数据返回表格");

        List<TableItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            TableItem item = new TableItem();
            item.setDeptName("心血管内科门诊" + (i + 1));
            item.setVisits(350 + i * 10);
            item.setAppointmentRate("70.00%");
            item.setExamRate("75.00%");
            item.setEfficiency(25.00 + i);
            item.setVisitCount(30 + i);
            item.setFamousExpert(2 + i);
            item.setSpecialExpert(3 + i);
            item.setKnownExpert(2 + i);
            item.setExpertA(5 + i);
            item.setExpertB(4 + i);
            item.setOrdinary(2 + i);

            UnitDetailItem effectiveUnitsTotal = new UnitDetailItem();
            effectiveUnitsTotal.setEffective(45 + i);
            effectiveUnitsTotal.setTotal(50 + i);
            item.setEffectiveUnitsTotal(effectiveUnitsTotal);

            UnitDetail unitDetail = new UnitDetail();
            unitDetail.setFamousExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setSpecialExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setKnownExpert(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setExpertA(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setExpertB(newUnitDetailItem(2 + i, 4 + i));
            unitDetail.setOrdinary(newUnitDetailItem(2 + i, 4 + i));
            item.setUnitDetail(unitDetail);

            list.add(item);
        }

        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientOperationRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");

        // 示例：在 Service 中直接写 SQL
        String sql = "SELECT COUNT(*) AS total_visits, " +
                "ROUND(COUNT(CASE WHEN appointment_flag = '1' THEN 1 END) * 100.0 / COUNT(*), 2) || '%' AS appointment_rate " +
                "FROM outpatient_record " +
                "WHERE visit_date BETWEEN ? AND ?";

        try {
            // 如需切换数据源
            // DynamicDataSourceContextHolder.set("slave");
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, request.getStartDate(), request.getEndDate());
            // DynamicDataSourceContextHolder.clear();

            OverviewData overview = new OverviewData();
            overview.setTotalVisits(result.get("TOTAL_VISITS") != null ? Integer.parseInt(result.get("TOTAL_VISITS").toString()) : 0);
            overview.setAppointmentRate(result.get("APPOINTMENT_RATE") != null ? result.get("APPOINTMENT_RATE").toString() : "0%");
            // ... 其他字段类似

            return overview;
        } catch (Exception e) {
            log.warn("JdbcTemplate 查询失败，回退到 Mock 数据", e);
            return queryOverviewMock(request);
        }
    }

    private PageResult<TableItem> queryTableByJdbc(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        log.info("使用 JdbcTemplate 查询表格数据");

        // 原始 SQL（不含分页）
        String baseSql = "SELECT DEPT_NAME, COUNT(*) AS VISITS, " +
                "ROUND(COUNT(CASE WHEN appointment_flag = '1' THEN 1 END) * 100.0 / COUNT(*), 2) || '%' AS appointment_rate " +
                "FROM outpatient_record " +
                "WHERE visit_date BETWEEN ? AND ? " +
                "GROUP BY DEPT_NAME " +
                "ORDER BY COUNT(*) DESC";

        // 包装分页 SQL（Oracle 12c+ 用 OFFSET FETCH，11g 用 ROWNUM）
        String pageSql = OraclePageUtil.wrapOffsetFetchPage(baseSql, page, pageSize);
        // 如果是 Oracle 11g: String pageSql = OraclePageUtil.wrapRowNumPage(baseSql, page, pageSize);

        try {
            List<TableItem> list = jdbcTemplate.query(pageSql, new RowMapper<TableItem>() {
                @Override
                public TableItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                    TableItem item = new TableItem();
                    item.setDeptName(rs.getString("DEPT_NAME"));
                    item.setVisits(rs.getInt("VISITS"));
                    item.setAppointmentRate(rs.getString("APPOINTMENT_RATE"));
                    // ... 其他字段
                    return item;
                }
            }, request.getStartDate(), request.getEndDate());

            // 查询总数
            String countSql = "SELECT COUNT(DISTINCT DEPT_NAME) FROM outpatient_record WHERE visit_date BETWEEN ? AND ?";
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, request.getStartDate(), request.getEndDate());

            return PageResult.of(list, total != null ? total : 0L, page, pageSize);
        } catch (Exception e) {
            log.warn("JdbcTemplate 查询失败，回退到 Mock 数据", e);
            return queryTableMock(request, page, pageSize);
        }
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientOperationRequest request) {
        log.info("使用 MyBatis-Plus 查询概览数据");

        // 示例：使用 QueryWrapper 进行条件查询
        // QueryWrapper<OutpatientRecordEntity> wrapper = new QueryWrapper<>();
        // wrapper.between("visit_date", request.getStartDate(), request.getEndDate());
        // List<OutpatientRecordEntity> records = outpatientRecordMapper.selectList(wrapper);
        // ... 进行统计计算

        // 当前回退到 Mock
        return queryOverviewMock(request);
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        log.info("使用 MyBatis-Plus 查询表格数据");

        // 示例：使用 QueryWrapper + 分页插件
        // Page<OutpatientRecordEntity> mpPage = new Page<>(page, pageSize);
        // QueryWrapper<OutpatientRecordEntity> wrapper = new QueryWrapper<>();
        // wrapper.between("visit_date", request.getStartDate(), request.getEndDate());
        // wrapper.groupBy("dept_name");
        // Page<OutpatientRecordEntity> result = outpatientRecordMapper.selectPage(mpPage, wrapper);

        // 当前回退到 Mock
        return queryTableMock(request, page, pageSize);
    }

    // ==================== 工具方法 ====================

    private UnitDetailItem newUnitDetailItem(int effective, int total) {
        UnitDetailItem item = new UnitDetailItem();
        item.setEffective(effective);
        item.setTotal(total);
        return item;
    }

}
