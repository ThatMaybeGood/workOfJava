package com.mergedata.dao;

import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// ❗ 标记为 Spring Bean，以便 @Autowired 生效
@Repository
public class YQStoredProcedureDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    // 注入 JdbcTemplate 和 DataSource
    @Autowired
    public YQStoredProcedureDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }
    /**
     * 【通用方法】调用接收一个 String 输入参数并返回单个游标的存储过程。
     *
     * @param <T> 目标实体类型
     * @param procedureName 存储过程的完整名称 (如: "GET_USER_RECORDS")
     * @param inputStringParam 传入存储过程的 String 类型参数
     * @param rowMapper 目标实体对应的 RowMapper 实例
     * @return 映射后的实体列表
     */
    public <T> List<T> executeQuerySingleStringParam(
            String procedureName,
            String inputStringParam,
            RowMapper<T> rowMapper) {

        // 1. 存储过程调用语句：包含一个输入参数 (?) 和一个输出游标参数 (?)
        // 动态构建调用字符串
        final String procedureCall = "{call " + procedureName + "(?, ?)}";

        // 2. 使用 execute 方法执行 CallableStatement 原生调用
        return jdbcTemplate.execute(procedureCall, (CallableStatement cs) -> {

            // 3. 设置输入参数
            cs.setString(1, inputStringParam); // 绑定第一个参数：String

            // 4. 注册 OUT 参数 (游标)
            cs.registerOutParameter(2, OracleTypes.CURSOR); // 绑定第二个参数：游标

            // 5. 执行存储过程
            cs.execute();

            // 6. 获取游标结果集 (游标在第二个位置)
            ResultSet rs = (ResultSet) cs.getObject(2);

            // 7. 使用 RowMapper 将 ResultSet 映射为 List
            List<T> resultList = new ArrayList<>();

            if (rs != null) {
                try {
                    int rowNum = 0;
                    while (rs.next()) {
                        // 使用传入的 RowMapper 进行映射
                        resultList.add(rowMapper.mapRow(rs, rowNum++));
                    }
                } finally {
                    // 确保 ResultSet 被关闭
                    rs.close();
                }
            }
            return resultList;
        });
    }




    /**
     * 【通用方法】调用不接收输入参数，但返回单个游标的存储过程。
     *
     * @param <T> 目标实体类型
     * @param procedureName 存储过程的完整名称 (如: "GET_ALL_PRODUCTS")
     * @param rowMapper 目标实体对应的 RowMapper 实例
     * @return 映射后的实体列表
     */
    public <T> List<T> executeQueryNoParam(
            String procedureName,
            RowMapper<T> rowMapper) {

        // 1. 存储过程调用语句：只包含一个输出游标参数 (?)
        final String procedureCall = "{call " + procedureName + "(?)}";

        // 2. 使用 execute 方法执行 CallableStatement 原生调用
        // 返回类型为 List<T>
        return jdbcTemplate.execute(procedureCall, (CallableStatement cs) -> {

            // 3. 注册 OUT 参数 (游标)
            // 游标是唯一的参数，索引为 1
            cs.registerOutParameter(1, OracleTypes.CURSOR);

            // 4. 执行存储过程
            cs.execute();

            // 5. 获取游标结果集 (游标在第一个位置)
            ResultSet rs = (ResultSet) cs.getObject(1);

            // 6. 使用 RowMapper 将 ResultSet 映射为 List
            if (rs != null) {
                try {
                    List<T> resultList = new ArrayList<>();
                    int rowNum = 0;
                    while (rs.next()) {
                        // 使用传入的 RowMapper 进行映射
                        resultList.add(rowMapper.mapRow(rs, rowNum++));
                    }
                    return resultList;
                } finally {
                    // 确保 ResultSet 被关闭
                    rs.close();
                }
            }
            // 如果 rs 为 null (极少见) 或无数据，返回空列表
            return Collections.emptyList();
        });
    }


    /**
     * 【通用方法】调用具有不固定数量输入参数和单个游标返回的存储过程。
     * * @param <T> 目标实体类型
     * @param procedureName 存储过程的名称 (如: "GET_RECORDS_FLEXIBLE")
     * @param rowMapper 目标实体对应的 RowMapper 实例
     * @param inParams 包含所有输入参数的 Map<参数名, 参数值>
     * @param cursorName 存储过程中 REF_CURSOR OUT 参数的名称 (如: "P_CURSOR")
     * @return 映射后的实体列表
     */
    public <T> List<T> executeQueryFlexibleParams(
            String procedureName,
            RowMapper<T> rowMapper,
            Map<String, Object> inParams,
            String cursorName) {

        // 1. 初始化 SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName(procedureName)
                // 必须声明 REF_CURSOR 返回参数
                // registerOutParameter(参数名, JDBC类型, RowMapper)
                .returningResultSet(cursorName, rowMapper);

        // 2. 执行调用
        // execute(inParams) 接收 Map，将 Map 键值对自动绑定为输入参数
        Map<String, Object> out = jdbcCall.execute(inParams);

        // 3. 从结果 Map 中获取游标对应的列表
        // SimpleJdbcCall 会自动将 RowMapper 作用于游标结果，并放入 Map 中
        // 键名就是我们注册的 cursorName
        List<T> resultList = (List<T>) out.get(cursorName);

        // 4. 返回结果
        return resultList != null ? resultList : Collections.emptyList();
    }






//
//package com.mergedata.service;
//
//// ... (导入 ProductDTO, ProductRowMapper, ProcedureCaller)
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//    @Service
//    public class ProductService {
//
//        @Autowired
//        private ProcedureCaller procedureCaller;
//
//        @Autowired
//        private ProductRowMapper productRowMapper; // 假设映射 ProductDTO
//
//        public List<ProductDTO> getProductsFlexible(String date, Integer status) {
//
//            // 1. 构建输入参数 Map
//            Map<String, Object> params = new HashMap<>();
//
//            // 动态添加参数：Map的键必须与存储过程的参数名完全一致
//            if (date != null) {
//                params.put("P_DATE", date);
//            }
//            if (status != null) {
//                params.put("P_STATUS", status);
//            }
//            // 如果存储过程接收了参数但未在 Map 中提供，JDBC驱动会报错，
//            // 除非该参数在存储过程中定义了默认值。
//
//            // 2. 调用通用方法
//            return procedureCaller.executeQueryFlexibleParams(
//                    "GET_RECORDS_FLEXIBLE", // 存储过程名
//                    productRowMapper,       // 映射器
//                    params,                 // 输入参数 Map
//                    "P_CURSOR"              // 游标 OUT 参数名
//            );
//        }
//    }









}

//
//// 假设这是您的 Service 层代码
//
//// ... (导入和 @Autowired YQStoredProcedureDao)
//
//        // 在 Service 方法中：
//        public List<SomeOtherDTO> getProducts(String categoryId) {
//            // 假设您有一个名为 "GET_PRODUCTS_BY_CATEGORY" 的过程，接收一个 String
//            // 并且您已经定义了 SomeOtherDTORowMapper
//
//            RowMapper<SomeOtherDTO> productMapper = new SomeOtherDTORowMapper();
//
//            return yqStoredProcedureDao.executeQuerySingleStringParam(
//                    "GET_PRODUCTS_BY_CATEGORY", // 过程名
//                    categoryId,                 // 输入参数
//                    productMapper               // 映射器
//            );
//        }
//
//        public List<YQCashRegRecordDTO> getRecords(String date) {
//            // 假设您有一个名为 "GET_RECORDS_BY_DATE" 的过程，接收一个 String
//            RowMapper<YQCashRegRecordDTO> recordMapper = new YQCashRegRecordRowMapper();
//
//            return yqStoredProcedureDao.executeQuerySingleStringParam(
//                    "GET_RECORDS_BY_DATE",
//                    date,
//                    recordMapper
//            );
//        }