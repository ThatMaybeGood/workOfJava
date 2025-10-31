//package com.showexcel.server;
//
//import com.showexcel.model.CashStatistics;
//import com.showexcel.repository.CashStatisticsRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Mine
// * @version 1.0
// * 描述:
// * @date 2025/10/30 16:33
// */
//
//@Service
//public class CashStatisticsServiceNew {
//
//    @Autowired
//    private CashStatisticsRepository repository;
//
//
//    public List<CashStatistics> findAll() {
//        CashStatisticsRepository cashStatisticsRepository = null;
//
//        // 这里使用您自己的数据库查询逻辑
//        List<CashStatistics> allData = cashStatisticsRepository.findAll();
//
////        List<Object[]> results = repository.findAll();
//        List<CashStatistics> statisticsList = new ArrayList<>();
//
//        for (CashStatistics result : results) {
//            CashStatistics stats = new CashStatistics();
//            // 根据数据库查询结果设置字段
//            // 这里需要根据您的实际数据库结构进行映射
//            if (result.name > 2) stats.setName((String) result[2]);
//            if (result.name > 3) stats.setHisAdvancePayment(convertToDouble(result[3]));
//            if (result.name > 4) stats.setHisMedicalIncome(convertToDouble(result[4]));
//            if (result.name > 5) stats.setHisRegistrationIncome(convertToDouble(result[5]));
//            if (result.name > 6) stats.setReportAmount(convertToDouble(result[6]));
//            if (result.name > 7) stats.setPreviousTemporaryReceipt(convertToDouble(result[7]));
//            if (result.name > 8) stats.setCurrentTemporaryReceipt(convertToDouble(result[8]));
//            if (result.name > 9) stats.setRetainedCash(convertToDouble(result[9]));
//            if (result.name > 10) stats.setPettyCash(convertToDouble(result[10]));
//
//            statisticsList.add(stats);
//        }
//
//        return statisticsList;
//    }
//
//    private Double convertToDouble(Object value) {
//        if (value == null) return null;
//        if (value instanceof Double) return (Double) value;
//        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
//        if (value instanceof Integer) return ((Integer) value).doubleValue();
//        try {
//            return Double.parseDouble(value.toString());
//        } catch (NumberFormatException e) {
//            return null;
//        }
//    }
//
//}