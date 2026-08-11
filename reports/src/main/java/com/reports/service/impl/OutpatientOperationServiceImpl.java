package com.reports.service.impl;

import com.reports.config.PageConfig;
import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientOperationRequest;
import com.reports.dto.response.outpatient.operation.OverviewData;
import com.reports.dto.response.outpatient.operation.TableItem;
import com.reports.entity.OutpatientOperationEntity;
import com.reports.entity.OutpatientOpDtlEntity;
import com.reports.mapper.OutpatientOperationMapper;
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

        String sql = "SELECT " +
                "SUM(NVL(total_visits, 0)) AS totalVisits, " +
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
                "SUM(NVL(unit_ordinary_total, 0)) AS unitOrdinaryTotal, " +
                "SUM(NVL(appointment_total, 0)) AS appointmentTotal, " +
                "SUM(NVL(appointment_count, 0)) AS appointmentCount, " +
                "SUM(NVL(return_visits, 0)) AS returnVisits, " +
                "SUM(NVL(treat_count, 0)) AS treatCount " +
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
                "SUM(NVL(unit_ordinary_total, 0)) AS unitOrdinaryTotal, " +
                "SUM(NVL(appointment_total, 0)) AS appointmentTotal, " +
                "SUM(NVL(appointment_count, 0)) AS appointmentCount, " +
                "SUM(NVL(return_visits, 0)) AS returnVisits, " +
                "SUM(NVL(treat_count, 0)) AS treatCount " +
                "FROM TR_OUTP_OP " +
                "WHERE stat_date BETWEEN ? AND ? ";

        List<Object> params = new ArrayList<>();
        params.add(request.getStartDate());
        params.add(request.getEndDate());
        if (request.getDeptCode() != null && !request.getDeptCode().isEmpty()) {
            baseSql += "AND dept_code = ? ";
            params.add(request.getDeptCode());
        }
        if (request.getDeptName() != null && !request.getDeptName().isEmpty()) {
            baseSql += "AND dept_name LIKE '%' || ? || '%' ";
            params.add(request.getDeptName());
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
        try {
            OutpatientOperationEntity entity = operationMapper.querySummaryByDateAndDept(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDeptCode());
            return buildOverviewData(entity);
        } catch (Exception e) {
            log.warn("查询概览数据失败", e);
            return new OverviewData();
        }
    }

    private PageResult<TableItem> queryTableByMybatisPlus(OutpatientOperationRequest request, Integer page, Integer pageSize) {
        try {
            List<OutpatientOpDtlEntity> rows = operationMapper.queryDeptDetail(
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getDeptCode(),
                    request.getDeptName());

            List<TableItem> allItems = new ArrayList<>();
            for (OutpatientOpDtlEntity row : rows) {
                allItems.add(buildTableItemFromDtl(row));
            }

            int total = allItems.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            List<TableItem> pageList = start < total ? allItems.subList(start, end) : new ArrayList<>();

            return PageResult.of(pageList, (long) total, page, pageSize);
        } catch (Exception e) {
            log.warn("查询表格数据失败", e);
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
    }

    // ==================== 工具方法 ====================

    /** 从 Map（JDBC 模式）构建 OverviewData */
    private OverviewData buildOverviewData(Map<String, Object> map) {
        if (map == null) {
            map = new java.util.HashMap<>();
        }
        int totalVisits = getInt(map, "totalVisits");
        int famousExpert = getInt(map, "famousExpert");
        int specialExpert = getInt(map, "specialExpert");
        int knownExpert = getInt(map, "knownExpert");
        int expertA = getInt(map, "expertA");
        int expertB = getInt(map, "expertB");
        int ordinary = getInt(map, "ordinary");
        int appointmentTotal = getInt(map, "appointmentTotal");
        int appointmentCount = getInt(map, "appointmentCount");
        int treatCount = getInt(map, "treatCount");

        // 出诊人次 = 所有专家类人次之和
        int visitCount = famousExpert + specialExpert + knownExpert + expertA + expertB + ordinary;

        // 有效出诊单元 = 所有 unit_??_effective 之和
        int effectiveUnits = getInt(map, "unitFamousEffective")
                + getInt(map, "unitSpecialEffective")
                + getInt(map, "unitKnownEffective")
                + getInt(map, "unitAEffective")
                + getInt(map, "unitBEffective")
                + getInt(map, "unitOrdinaryEffective");
        // 出诊单元总数 = 所有 unit_??_total 之和
        int totalUnits = getInt(map, "unitFamousTotal")
                + getInt(map, "unitSpecialTotal")
                + getInt(map, "unitKnownTotal")
                + getInt(map, "unitATotal")
                + getInt(map, "unitBTotal")
                + getInt(map, "unitOrdinaryTotal");

        OverviewData overview = new OverviewData();
        overview.setTotalVisits(totalVisits);
        overview.setAppointmentRate(calcRate(appointmentCount, appointmentTotal));
        overview.setVisitCount(visitCount);
        overview.setExamRate(calcRate(treatCount, visitCount));
        overview.setEfficiency(calcEfficiency(effectiveUnits, totalUnits));
        overview.setEffectiveUnits(effectiveUnits);
        overview.setTotalUnits(totalUnits);
        overview.setFamousExpert(famousExpert);
        overview.setSpecialExpert(specialExpert);
        overview.setKnownExpert(knownExpert);
        overview.setExpertA(expertA);
        overview.setExpertB(expertB);
        overview.setOrdinary(ordinary);
        overview.setUnitFamousEffective(getInt(map, "unitFamousEffective"));
        overview.setUnitFamousTotal(getInt(map, "unitFamousTotal"));
        overview.setUnitSpecialEffective(getInt(map, "unitSpecialEffective"));
        overview.setUnitSpecialTotal(getInt(map, "unitSpecialTotal"));
        overview.setUnitKnownEffective(getInt(map, "unitKnownEffective"));
        overview.setUnitKnownTotal(getInt(map, "unitKnownTotal"));
        overview.setUnitAEffective(getInt(map, "unitAEffective"));
        overview.setUnitATotal(getInt(map, "unitATotal"));
        overview.setUnitBEffective(getInt(map, "unitBEffective"));
        overview.setUnitBTotal(getInt(map, "unitBTotal"));
        overview.setUnitOrdinaryEffective(getInt(map, "unitOrdinaryEffective"));
        overview.setUnitOrdinaryTotal(getInt(map, "unitOrdinaryTotal"));
        return overview;
    }

    /** 从 Entity（MyBatis 模式）构建 OverviewData */
    private OverviewData buildOverviewData(OutpatientOperationEntity entity) {
        if (entity == null) {
            return new OverviewData();
        }
        int totalVisits = entity.getTotalVisits() != null ? entity.getTotalVisits() : 0;
        int famousExpert = entity.getFamousExpert() != null ? entity.getFamousExpert() : 0;
        int specialExpert = entity.getSpecialExpert() != null ? entity.getSpecialExpert() : 0;
        int knownExpert = entity.getKnownExpert() != null ? entity.getKnownExpert() : 0;
        int expertA = entity.getExpertA() != null ? entity.getExpertA() : 0;
        int expertB = entity.getExpertB() != null ? entity.getExpertB() : 0;
        int ordinary = entity.getOrdinary() != null ? entity.getOrdinary() : 0;
        int appointmentTotal = entity.getAppointmentTotal() != null ? entity.getAppointmentTotal() : 0;
        int returnVisits = entity.getReturnVisits() != null ? entity.getReturnVisits() : 0;
        int appointmentCount = entity.getAppointmentCount() != null ? entity.getAppointmentCount() : 0;
        int treatCount = entity.getTreatCount() != null ? entity.getTreatCount() : 0;

        int visitCount = famousExpert + specialExpert + knownExpert + expertA + expertB + ordinary;
        int effectiveUnits = sumNonNull(
                entity.getUnitFamousEffective(), entity.getUnitSpecialEffective(),
                entity.getUnitKnownEffective(), entity.getUnitAEffective(),
                entity.getUnitBEffective(), entity.getUnitOrdinaryEffective());
        int totalUnits = sumNonNull(
                entity.getUnitFamousTotal(), entity.getUnitSpecialTotal(),
                entity.getUnitKnownTotal(), entity.getUnitATotal(),
                entity.getUnitBTotal(), entity.getUnitOrdinaryTotal());

        OverviewData overview = new OverviewData();
        overview.setTotalVisits(totalVisits);
        overview.setAppointmentRate(calcRate(appointmentCount, appointmentTotal-returnVisits));
        overview.setVisitCount(visitCount);
        overview.setExamRate(calcRate(appointmentCount + treatCount, visitCount+appointmentCount+treatCount));
        overview.setEfficiency(calcEfficiency(effectiveUnits, totalUnits));
        overview.setEffectiveUnits(effectiveUnits);
        overview.setTotalUnits(totalUnits);
        overview.setFamousExpert(famousExpert);
        overview.setSpecialExpert(specialExpert);
        overview.setKnownExpert(knownExpert);
        overview.setExpertA(expertA);
        overview.setExpertB(expertB);
        overview.setOrdinary(ordinary);
        overview.setUnitFamousEffective(entity.getUnitFamousEffective() != null ? entity.getUnitFamousEffective() : 0);
        overview.setUnitFamousTotal(entity.getUnitFamousTotal() != null ? entity.getUnitFamousTotal() : 0);
        overview.setUnitSpecialEffective(entity.getUnitSpecialEffective() != null ? entity.getUnitSpecialEffective() : 0);
        overview.setUnitSpecialTotal(entity.getUnitSpecialTotal() != null ? entity.getUnitSpecialTotal() : 0);
        overview.setUnitKnownEffective(entity.getUnitKnownEffective() != null ? entity.getUnitKnownEffective() : 0);
        overview.setUnitKnownTotal(entity.getUnitKnownTotal() != null ? entity.getUnitKnownTotal() : 0);
        overview.setUnitAEffective(entity.getUnitAEffective() != null ? entity.getUnitAEffective() : 0);
        overview.setUnitATotal(entity.getUnitATotal() != null ? entity.getUnitATotal() : 0);
        overview.setUnitBEffective(entity.getUnitBEffective() != null ? entity.getUnitBEffective() : 0);
        overview.setUnitBTotal(entity.getUnitBTotal() != null ? entity.getUnitBTotal() : 0);
        overview.setUnitOrdinaryEffective(entity.getUnitOrdinaryEffective() != null ? entity.getUnitOrdinaryEffective() : 0);
        overview.setUnitOrdinaryTotal(entity.getUnitOrdinaryTotal() != null ? entity.getUnitOrdinaryTotal() : 0);
        return overview;
    }

    private TableItem buildTableItem(OutpatientOperationEntity entity) {
        if (entity == null) {
            return new TableItem();
        }
        int famousExpert = entity.getFamousExpert() != null ? entity.getFamousExpert() : 0;
        int specialExpert = entity.getSpecialExpert() != null ? entity.getSpecialExpert() : 0;
        int knownExpert = entity.getKnownExpert() != null ? entity.getKnownExpert() : 0;
        int expertA = entity.getExpertA() != null ? entity.getExpertA() : 0;
        int expertB = entity.getExpertB() != null ? entity.getExpertB() : 0;
        int ordinary = entity.getOrdinary() != null ? entity.getOrdinary() : 0;
        int appointmentTotal = entity.getAppointmentTotal() != null ? entity.getAppointmentTotal() : 0;
        int appointmentCount = entity.getAppointmentCount() != null ? entity.getAppointmentCount() : 0;
        int treatCount = entity.getTreatCount() != null ? entity.getTreatCount() : 0;

        int visitCount = famousExpert + specialExpert + knownExpert + expertA + expertB + ordinary;
        int effectiveUnits = sumNonNull(
                entity.getUnitFamousEffective(), entity.getUnitSpecialEffective(),
                entity.getUnitKnownEffective(), entity.getUnitAEffective(),
                entity.getUnitBEffective(), entity.getUnitOrdinaryEffective());
        int totalUnits = sumNonNull(
                entity.getUnitFamousTotal(), entity.getUnitSpecialTotal(),
                entity.getUnitKnownTotal(), entity.getUnitATotal(),
                entity.getUnitBTotal(), entity.getUnitOrdinaryTotal());

        TableItem item = new TableItem();
        item.setDeptCode(entity.getDeptCode());
        item.setDeptName(entity.getDeptName());
        item.setTotalVisits(entity.getTotalVisits() != null ? entity.getTotalVisits() : 0);
        item.setAppointmentRate(calcRate(appointmentCount, appointmentTotal));
        item.setVisitCount(visitCount);
        item.setExamRate(calcRate(treatCount, visitCount));
        item.setEfficiency(calcEfficiency(effectiveUnits, totalUnits));
        item.setEffectiveUnits(effectiveUnits);
        item.setTotalUnits(totalUnits);
        item.setFamousExpert(famousExpert);
        item.setSpecialExpert(specialExpert);
        item.setKnownExpert(knownExpert);
        item.setExpertA(expertA);
        item.setExpertB(expertB);
        item.setOrdinary(ordinary);
        item.setUnitFamousEffective(entity.getUnitFamousEffective() != null ? entity.getUnitFamousEffective() : 0);
        item.setUnitFamousTotal(entity.getUnitFamousTotal() != null ? entity.getUnitFamousTotal() : 0);
        item.setUnitSpecialEffective(entity.getUnitSpecialEffective() != null ? entity.getUnitSpecialEffective() : 0);
        item.setUnitSpecialTotal(entity.getUnitSpecialTotal() != null ? entity.getUnitSpecialTotal() : 0);
        item.setUnitKnownEffective(entity.getUnitKnownEffective() != null ? entity.getUnitKnownEffective() : 0);
        item.setUnitKnownTotal(entity.getUnitKnownTotal() != null ? entity.getUnitKnownTotal() : 0);
        item.setUnitAEffective(entity.getUnitAEffective() != null ? entity.getUnitAEffective() : 0);
        item.setUnitATotal(entity.getUnitATotal() != null ? entity.getUnitATotal() : 0);
        item.setUnitBEffective(entity.getUnitBEffective() != null ? entity.getUnitBEffective() : 0);
        item.setUnitBTotal(entity.getUnitBTotal() != null ? entity.getUnitBTotal() : 0);
        item.setUnitOrdinaryEffective(entity.getUnitOrdinaryEffective() != null ? entity.getUnitOrdinaryEffective() : 0);
        item.setUnitOrdinaryTotal(entity.getUnitOrdinaryTotal() != null ? entity.getUnitOrdinaryTotal() : 0);
        return item;
    }

    private TableItem buildTableItemFromDtl(OutpatientOpDtlEntity row) {
        if (row == null) {
            return new TableItem();
        }
        TableItem item = new TableItem();
        item.setDeptName(row.getDeptName());
        item.setTotalVisits(row.getVisits());
        item.setAppointmentRate(row.getAppointmentRate());
        item.setExamRate(row.getExamRate());
        item.setEfficiency(row.getEfficiency() != null ? row.getEfficiency().doubleValue() : 0.0);
        item.setVisitCount(row.getVisitCount());
        item.setFamousExpert(row.getFamousExpert() != null ? row.getFamousExpert() : 0);
        item.setSpecialExpert(row.getSpecialExpert() != null ? row.getSpecialExpert() : 0);
        item.setKnownExpert(row.getKnownExpert() != null ? row.getKnownExpert() : 0);
        item.setExpertA(row.getExpertA() != null ? row.getExpertA() : 0);
        item.setExpertB(row.getExpertB() != null ? row.getExpertB() : 0);
        item.setOrdinary(row.getOrdinary() != null ? row.getOrdinary() : 0);
        item.setEffectiveUnits(row.getEffectiveTotal() != null ? row.getEffectiveTotal() : 0);
        item.setTotalUnits(row.getTotalDetail() != null ? row.getTotalDetail() : 0);
        return item;
    }

    private TableItem mapResultSetToTableItem(ResultSet rs) throws SQLException {
        int famousExpert = rs.getInt("FAMOUSEXPERT");
        int specialExpert = rs.getInt("SPECIALEXPERT");
        int knownExpert = rs.getInt("KNOWNEXPERT");
        int expertA = rs.getInt("EXPERTA");
        int expertB = rs.getInt("EXPERTB");
        int ordinary = rs.getInt("ORDINARY");
        int appointmentTotal = rs.getInt("APPOINTMENTTOTAL");
        int appointmentCount = rs.getInt("APPOINTMENTCOUNT");
        int treatCount = rs.getInt("TREATCOUNT");

        int visitCount = famousExpert + specialExpert + knownExpert + expertA + expertB + ordinary;
        int effectiveUnits = rs.getInt("UNITFAMOUSEFFECTIVE")
                + rs.getInt("UNITSPECIALEFFECTIVE")
                + rs.getInt("UNITKNOWNEFFECTIVE")
                + rs.getInt("UNITAEFFECTIVE")
                + rs.getInt("UNITBEFFECTIVE")
                + rs.getInt("UNITORDINARYEFFECTIVE");
        int totalUnits = rs.getInt("UNITFAMOUSTOTAL")
                + rs.getInt("UNITSPECIALTOTAL")
                + rs.getInt("UNITKNOWNTOTAL")
                + rs.getInt("UNITATOTAL")
                + rs.getInt("UNITBTOTAL")
                + rs.getInt("UNITORDINARYTOTAL");

        TableItem item = new TableItem();
        item.setDeptCode(rs.getString("DEPTCODE"));
        item.setDeptName(rs.getString("DEPTNAME"));
        item.setTotalVisits(rs.getInt("TOTALVISITS"));
        item.setAppointmentRate(calcRate(appointmentCount, appointmentTotal));
        item.setVisitCount(visitCount);
        item.setExamRate(calcRate(treatCount, visitCount));
        item.setEfficiency(calcEfficiency(effectiveUnits, totalUnits));
        item.setEffectiveUnits(effectiveUnits);
        item.setTotalUnits(totalUnits);
        item.setFamousExpert(famousExpert);
        item.setSpecialExpert(specialExpert);
        item.setKnownExpert(knownExpert);
        item.setExpertA(expertA);
        item.setExpertB(expertB);
        item.setOrdinary(ordinary);
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

    /** 计算百分比字符串，如 "83.10%" */
    private String calcRate(int numerator, int denominator) {
        if (denominator == 0) return "0.00%";
        return String.format("%.2f%%", (numerator * 100.0) / denominator);
    }

    /** 计算效率 = 有效出诊单元 / 出诊单元总数（保留1位小数） */
    private Double calcEfficiency(int effectiveUnits, int totalUnits) {
        if (totalUnits == 0) return 0.0;
        return Math.round((effectiveUnits * 100.0 / totalUnits) * 10.0) / 10.0;
    }

    private int sumNonNull(Integer... values) {
        int sum = 0;
        for (Integer v : values) {
            if (v != null) sum += v;
        }
        return sum;
    }

    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) val = map.get(key.toUpperCase());
        if (val == null) val = map.get(key.toLowerCase());
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
