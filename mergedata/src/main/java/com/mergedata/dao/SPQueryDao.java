package com.mergedata.dao;

import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
public class SPQueryDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    // 注入 JdbcTemplate 和 DataSource
    @Autowired
    public SPQueryDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * 【通用方法】调用接收一个 String 输入参数并返回单个游标的存储过程。
     *
     * @param <T>              目标实体类型
     * @param procedureName    存储过程的完整名称 (如: "GET_USER_RECORDS")
     * @param inputStringParam 传入存储过程的 String 类型参数
     * @param rowMapper        目标实体对应的 RowMapper 实例
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
     * @param <T>           目标实体类型
     * @param procedureName 存储过程的完整名称 (如: "GET_ALL_PRODUCTS")
     * @param rowMapper     目标实体对应的 RowMapper 实例
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
            return Collections. <T>emptyList();
        });
    }


    /**
     * 【通用方法】调用具有不固定数量输入参数和单个游标返回的存储过程。
     * * @param <T> 目标实体类型
     *
     * @param procedureName 存储过程的名称 (如: "GET_RECORDS_FLEXIBLE")
     * @param rowMapper     目标实体对应的 RowMapper 实例
     * @param inParams      包含所有输入参数的 Map<参数名, 参数值>
     * @param cursorName    存储过程中 REF_CURSOR OUT 参数的名称 (如: "P_CURSOR")
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
        return resultList != null ? resultList : Collections. <T>emptyList();
    }


    // 假设这是在 YQStoredProcedureDao.java 中添加的新方法

    /**
     * 【通用方法】调用具有多输入参数和多 OUT 参数的存储过程。
     *
     * @param <T> 目标实体类型
     * @param procedureName 存储过程的名称
     * @param rowMapper 游标对应的 RowMapper 实例
     * @param inParams 包含所有 IN 参数的 Map<参数名, 参数值>
     * @param outParamNames 包含所有 OUT 参数信息的 Map<参数名, SQL类型>
     * @param cursorName 存储过程中 REF_CURSOR OUT 参数的名称 (如: "P_CURSOR")
     * @return 包含所有 OUT 参数和游标结果列表的 Map<String, Object>
     */
    public <T> Map<String, Object> executeQueryMultipleOutParams(
            String procedureName,
            RowMapper<T> rowMapper,
            Map<String, Object> inParams,
            Map<String, Integer> outParamNames, // Map<参数名, SQL类型(如Types.INTEGER, Types.VARCHAR)>
            String cursorName) {

        // 1. 初始化 SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName(procedureName);

        // 2. 注册 REF_CURSOR OUT 参数 (游标)
        jdbcCall.returningResultSet(cursorName, rowMapper);

        // 3. 注册所有其他 OUT 参数
        if (outParamNames != null) {
            // outParamNames 格式: Map<"P_INT_OUT", Types.INTEGER>, Map<"P_VARCHAR_OUT", Types.VARCHAR>
            for (Map.Entry<String, Integer> entry : outParamNames.entrySet()) {
                jdbcCall.declareParameters(
                        new SqlOutParameter(entry.getKey(), entry.getValue())
                );
            }
        }

        // 4. 执行调用。out 包含了所有 OUT 参数和游标结果列表
        Map<String, Object> out = jdbcCall.execute(inParams);

        // 5. 返回所有结果
        return out;
    }


    /**
     * 调用具有多输入参数、多个 OUT 参数和多个游标的存储过程。
     *
     * @param procedureName 存储过程的名称 (如: "PKG_REPORT.GET_REPORT_DATA")
     * @param inParams 包含所有 IN 参数的 Map<参数名, 参数值>
     * @param outParamNames 包含所有非游标 OUT 参数信息的 Map<参数名, SQL类型> (例如: Types.INTEGER, Types.VARCHAR)
     * @param cursorMappers 包含所有游标 OUT 参数信息的 Map<游标名, RowMapper实例>
     * @return 包含所有 OUT 参数（包括游标结果列表）的 Map<String, Object>
     */
    public Map<String, Object> executeQueryMultipleCursors(
            String procedureName,
            Map<String, Object> inParams,
            Map<String, Integer> outParamNames, // Map<参数名, SQL类型(如Types.INTEGER, Types.VARCHAR)>
            Map<String, RowMapper<?>> cursorMappers) { // Map<游标名, 对应的 RowMapper>

        // 1. 初始化 SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName(procedureName);

        // 2. 注册所有 REF_CURSOR OUT 参数 (游标)
        if (cursorMappers != null) {
            for (Map.Entry<String, RowMapper<?>> entry : cursorMappers.entrySet()) {
                // 关键点：为每个游标 OUT 参数注册其名称和对应的 RowMapper

                //cursorMappers 里的每个游标名，都通过 returningResultSet 隐式 自动完成了**“它是一个游标出参”
                jdbcCall.returningResultSet(entry.getKey(), entry.getValue());
            }
        }

        // 3. 注册所有其他 OUT 参数 (如 CODE, MESSAGE, 计数等)
        if (outParamNames != null) {
            for (Map.Entry<String, Integer> entry : outParamNames.entrySet()) {
                // 注册非游标 OUT 参数的名称和 SQL 类型  显示声明出参
                jdbcCall.declareParameters(
                        new SqlOutParameter(entry.getKey(), entry.getValue())
                );
            }
        }

        // 4. 将 IN 参数转换为 SimpleJdbcCall 可接受的 MapSqlParameterSource
        MapSqlParameterSource in = new MapSqlParameterSource(inParams);

        // 5. 执行调用
        // out 包含了所有 OUT 参数、所有游标的结果列表（以游标名为 Key）
        Map<String, Object> out = jdbcCall.execute(in);

        // 6. 返回所有结果
        return out;
    }



    /**
     * 【通用方法】调用具有多输入参数和多 OUT 参数的存储过程。
     *
     * @param <T> 目标实体类型
     * @param procedureName 存储过程的名称
     * @param inParams 包含所有 IN 参数的 Map<参数名, 参数值>
     * @param outParamNames 包含所有 OUT 参数信息的 Map<参数名, SQL类型>
     * @return 包含所有 OUT 参数和游标结果列表的 Map<String, Object>
     */
    public <T> Map<String, Object> executeInsertMultipleOutParams(
            String procedureName,
            Map<String, Object> inParams,
            Map<String, Integer> outParamNames // Map<参数名, SQL类型(如Types.INTEGER, Types.VARCHAR)>
            ) {

        // 1. 初始化 SimpleJdbcCall
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName(procedureName);

        // 2. 注册 REF_CURSOR OUT 参数 (游标)
//        jdbcCall.returningResultSet(cursorName, rowMapper);

        // 3. 注册所有其他 OUT 参数
        if (outParamNames != null) {
            // outParamNames 格式: Map<"P_INT_OUT", Types.INTEGER>, Map<"P_VARCHAR_OUT", Types.VARCHAR>
            for (Map.Entry<String, Integer> entry : outParamNames.entrySet()) {
                jdbcCall.declareParameters(
                        new SqlOutParameter(entry.getKey(), entry.getValue())
                );
            }
        }

        // 4. 执行调用。out 包含了所有 OUT 参数和游标结果列表
        Map<String, Object> out = jdbcCall.execute(inParams);

        // 5. 返回所有结果
        return out;
    }


    /*
     * 示例用法：
     * 假设存储过程返回 P_CODE, P_MSG, P_CURSOR_A, P_CURSOR_B
     */
    /*
    public void exampleUsage() {
        // 1. 定义游标映射器：为每个游标指定不同的 RowMapper
        Map<String, RowMapper<?>> cursorMap = new HashMap<>();
        // 游标 P_CURSOR_A 映射到 UserEntity.class
        cursorMap.put("P_CURSOR_USER", new BeanPropertyRowMapper<>(UserEntity.class));
        // 游标 P_CURSOR_B 映射到 LogEntity.class
        cursorMap.put("P_CURSOR_LOG", new BeanPropertyRowMapper<>(LogEntity.class));

        // 2. 定义非游标 OUT 参数
        Map<String, Integer> outMap = new HashMap<>();
        outMap.put("P_CODE", java.sql.Types.INTEGER);
        outMap.put("P_MSG", java.sql.Types.VARCHAR);

        // 3. 定义 IN 参数
        Map<String, Object> inMap = new HashMap<>();
        inMap.put("P_USER_ID", 123);

        // 4. 执行调用
        Map<String, Object> results = executeQueryMultipleCursors(
                "PKG_DATA.GET_USER_AND_LOGS",
                inMap,
                outMap,
                cursorMap
        );

        // 5. 获取结果
        Integer code = (Integer) results.get("P_CODE");
        String message = (String) results.get("P_MSG");
        List<UserEntity> users = (List<UserEntity>) results.get("P_CURSOR_USER");
        List<LogEntity> logs = (List<LogEntity>) results.get("P_CURSOR_LOG");

        // ... 后续逻辑处理
    }
    */

}

