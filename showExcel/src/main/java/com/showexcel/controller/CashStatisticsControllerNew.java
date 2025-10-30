//package com.showexcel.controller;
//
//import com.showexcel.dao.CashStatisticsTableDTO;
//import com.showexcel.model.CashStatistics;
//import com.showexcel.dao.CashStatisticsRow;
//import com.showexcel.model.CellMergeConfig;
//import com.showexcel.server.CashStatisticsServiceNew;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author Mine
// * @version 1.0
// * 描述:
// * @date 2025/10/30 16:33
// */
//@RestController
//public class CashStatisticsControllerNew {
//
//    @Autowired
//    private CashStatisticsServiceNew cashStatisticsServiceNew;
//
//    @GetMapping("/api/cash-statistics-table")
//    public CashStatisticsTableDTO getCashStatisticsTable() {
//        // 从数据库查询数据
//        List<CashStatistics> dbData = cashStatisticsServiceNew.findAll();
//
//        // 构建表格数据
//        CashStatisticsTableDTO table = new CashStatisticsTableDTO();
//
//        // 设置表头（硬编码方式）
//        String[] headerNames = {
//                "序号", "名称", "预交金收入", "医疗收入", "挂号收入",
//                "应交报表数", "前日暂收款", "实交报表数",
//                "当日暂收款", "实收现金数", "留存数差额",
//                "留存现金数", "备用金"
//        };
//
//        for (String header : headerNames) {
//            table.addHeader(header);
//        }
//
//        // 处理数据行
//        List<CashStatisticsRow> row1List = new ArrayList<>();
//        List<CashStatisticsRow> row2List = new ArrayList<>();
//
//        for (CashStatistics data : dbData) {
//            // 根据业务逻辑确定rowType，这里需要您根据实际业务实现
//            Integer rowType = determineRowType(data);
//            CashStatisticsRow row = new CashStatisticsRow(rowType, data);
//
//            if (rowType == 1) {
//                row1List.add(row);
//            } else if (rowType == 2) {
//                row2List.add(row);
//            }
//        }
//
//        // 设置行索引并添加到表格
//        int currentRowIndex = 0; // 数据行从0开始计数（前端会加上标题行和表头行）
//
//        // 添加row1数据
//        for (CashStatisticsRow row : row1List) {
//            row.setRowIndex(currentRowIndex++);
//            table.addRow(row);
//        }
//
//        // 添加中间合并行 "名字第二段"
//        int middleRowIndex = currentRowIndex;
//        table.addMergeConfig(new CellMergeConfig(
//                middleRowIndex, 0, 1, table.getHeaders().size(), "名字第二段"
//        ));
//        currentRowIndex++; // 中间行占一行
//
//        // 添加row2数据
//        for (CashStatisticsRow row : row2List) {
//            row.setRowIndex(currentRowIndex++);
//            table.addRow(row);
//        }
//
//        // 添加自定义合并配置
//        // (13列, 15行) 合并3行2列 - 注意：这里的行索引是数据行索引
//        table.addMergeConfig(new CellMergeConfig(15, 13, 3, 2, "合并内容1"));
//        // (13列, 21行) 合并3行5列
//        table.addMergeConfig(new CellMergeConfig(21, 13, 3, 5, "合并内容2"));
//
//        return table;
//    }
//
//    private Integer determineRowType(CashStatistics data) {
//        // 根据您的业务逻辑确定行类型
//        // 示例逻辑，您需要根据实际需求修改
//        if (data.getName() != null && data.getName().contains("特定条件")) {
//            return 2;
//        }
//        return 1;
//    }
//}
