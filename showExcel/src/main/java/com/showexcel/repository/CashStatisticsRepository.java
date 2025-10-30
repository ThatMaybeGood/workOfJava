package com.showexcel.repository;

import com.showexcel.model.CashStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CashStatisticsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CashStatistics> findAll() {
        String sql = "SELECT id, tab_type as tab_type, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setTableType(rs.getObject("tab_type", Integer.class));
            item.setName(rs.getString("name"));
            item.setHisAdvancePayment(rs.getObject("hisAdvancePayment", Double.class));
            item.setHisMedicalIncome(rs.getObject("hisMedicalIncome", Double.class));
            item.setHisRegistrationIncome(rs.getObject("hisRegistrationIncome", Double.class));
            item.setReportAmount(rs.getObject("reportAmount", Double.class));
            item.setPreviousTemporaryReceipt(rs.getObject("previousTemporaryReceipt", Double.class));
            item.setCurrentTemporaryReceipt(rs.getObject("currentTemporaryReceipt", Double.class));
            item.setRetainedCash(rs.getObject("retainedCash", Double.class));
            item.setPettyCash(rs.getObject("pettyCash", Double.class));
            return item;
        });
    }

    // 方案1：高效按类型查询（推荐数据量大时使用）
    public List<CashStatistics> findByTableType(Integer tableType) {
        String sql = "SELECT id, tab_type as tableType, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics WHERE tab_type = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setTableType(rs.getObject("tableType", Integer.class));
            item.setName(rs.getString("name"));
            item.setHisAdvancePayment(rs.getObject("hisAdvancePayment", Double.class));
            item.setHisMedicalIncome(rs.getObject("hisMedicalIncome", Double.class));
            item.setHisRegistrationIncome(rs.getObject("hisRegistrationIncome", Double.class));
            item.setReportAmount(rs.getObject("reportAmount", Double.class));
            item.setPreviousTemporaryReceipt(rs.getObject("previousTemporaryReceipt", Double.class));
            item.setCurrentTemporaryReceipt(rs.getObject("currentTemporaryReceipt", Double.class));
            item.setRetainedCash(rs.getObject("retainedCash", Double.class));
            item.setPettyCash(rs.getObject("pettyCash", Double.class));
            return item;
        }, tableType);
    }

    // 方案2：批量查询所有数据后过滤（推荐数据量小时使用）
    public Map<Integer, List<CashStatistics>> findAllGroupByTableType() {
        List<CashStatistics> allData = findAll();
        return allData.stream()
                .collect(Collectors.groupingBy(CashStatistics::getTableType));
    }
}