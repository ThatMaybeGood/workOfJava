package com.showexcel.repository;

import com.showexcel.model.CashStatistics;
import com.showexcel.response.RowData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

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
                "FROM cash_statistics where cash_date = '2025-11-02'";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setTableType(rs.getObject("tab_type", Integer.class));
            item.setName(rs.getString("name"));
            item.setHisAdvancePayment(rs.getObject("hisAdvancePayment", BigDecimal.class));
            item.setHisMedicalIncome(rs.getObject("hisMedicalIncome", BigDecimal.class));
            item.setHisRegistrationIncome(rs.getObject("hisRegistrationIncome", BigDecimal.class));
            item.setReportAmount(rs.getObject("reportAmount", BigDecimal.class));
            item.setPreviousTemporaryReceipt(rs.getObject("previousTemporaryReceipt", BigDecimal.class));
            item.setCurrentTemporaryReceipt(rs.getObject("currentTemporaryReceipt", BigDecimal.class));
            item.setRetainedCash(rs.getObject("retainedCash", BigDecimal.class));
            item.setPettyCash(rs.getObject("pettyCash", BigDecimal.class));
            return item;
        });
    }

    // 方案1：高效按类型查询（推荐数据量大时使用）
    public List<CashStatistics> findByTableDate(String date) {
        String sql = "SELECT id, tab_type as tableType, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics WHERE cash_date = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setTableType(rs.getObject("tableType", Integer.class));
            item.setName(rs.getString("name"));
            item.setHisAdvancePayment(rs.getObject("hisAdvancePayment", BigDecimal.class));
            item.setHisMedicalIncome(rs.getObject("hisMedicalIncome", BigDecimal.class));
            item.setHisRegistrationIncome(rs.getObject("hisRegistrationIncome", BigDecimal.class));
            item.setReportAmount(rs.getObject("reportAmount", BigDecimal.class));
            item.setPreviousTemporaryReceipt(rs.getObject("previousTemporaryReceipt", BigDecimal.class));
            item.setCurrentTemporaryReceipt(rs.getObject("currentTemporaryReceipt", BigDecimal.class));
            item.setRetainedCash(rs.getObject("retainedCash", BigDecimal.class));
            item.setPettyCash(rs.getObject("pettyCash", BigDecimal.class));
            return item;
        }, date);
    }

    public List<RowData> findByTableDateNew(String date) {
        String sql = "SELECT id, tab_type as tableType, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics WHERE cash_date = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RowData item = new RowData();
            item.setTableType(rs.getObject("tableType", Integer.class));
            item.setName(rs.getString("name"));

            item.setHisAdvancePayment(rs.getObject("hisAdvancePayment", BigDecimal.class));
            item.setHisMedicalIncome(rs.getObject("hisMedicalIncome", BigDecimal.class));
            item.setHisRegistrationIncome(rs.getObject("hisRegistrationIncome", BigDecimal.class));
            item.setReportAmount(rs.getObject("reportAmount", BigDecimal.class));
            item.setPreviousTemporaryReceipt(rs.getObject("previousTemporaryReceipt", BigDecimal.class));
            item.setCurrentTemporaryReceipt(rs.getObject("currentTemporaryReceipt", BigDecimal.class));
            item.setRetainedCash(rs.getObject("retainedCash", BigDecimal.class));
            item.setPettyCash(rs.getObject("pettyCash", BigDecimal.class));

            // 计算字段
            item.setActualReportAmount(item.getReportAmount().subtract(item.getHisRegistrationIncome()));
            item.setActualCashAmount(item.getActualReportAmount().add(item.getCurrentTemporaryReceipt()));
            item.setRetainedDifference(item.getRetainedCash().subtract(item.getPettyCash()).subtract(item.getActualReportAmount()));
            return item;
        }, date);
    }
}