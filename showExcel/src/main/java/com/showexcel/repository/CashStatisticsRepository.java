package com.showexcel.repository;

import com.showexcel.model.CashStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CashStatisticsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CashStatistics> findAll() {
        String sql = "SELECT id, TYPE as type, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setType(rs.getObject("type", Integer.class));
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

    public List<CashStatistics> findByType(Integer type) {
        String sql = "SELECT id, TYPE as type, name, his_advance_payment as hisAdvancePayment, " +
                "his_medical_income as hisMedicalIncome, his_registration_income as hisRegistrationIncome, " +
                "report_amount as reportAmount, previous_temporary_receipt as previousTemporaryReceipt, " +
                "current_temporary_receipt as currentTemporaryReceipt, retained_cash as retainedCash, " +
                "petty_cash as pettyCash " +
                "FROM cash_statistics WHERE type = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CashStatistics item = new CashStatistics();
            item.setId(rs.getObject("id", Integer.class));
            item.setType(rs.getObject("type", Integer.class));
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
        }, type);
    }
}