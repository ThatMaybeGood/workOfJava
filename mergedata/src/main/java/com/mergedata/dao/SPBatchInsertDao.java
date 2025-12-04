package com.mergedata.dao;

import oracle.jdbc.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// ❗ 标记为 Spring Bean，以便 @Autowired 生效
@Repository
public class SPBatchInsertDao {

    // ... (现有代码保持不变) ...

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    // 注入 JdbcTemplate 和 DataSource
    @Autowired
    public SPBatchInsertDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    // --- 新增：通用批量写入方法 ---

    /**
     * 【通用方法】调用接收 Oracle 集合类型参数的批量插入存储过程。
     * 该方法使用函数式接口进行转换，无需实体类实现特定接口。
     *
     * @param <T> 任意实体类型
     * @param procedureName 存储过程名称 (如: "P_BATCH_INSERT_EMP")
     * @param objTypeName Oracle 对象类型名 (如: "T_EMPLOYEE_OBJ")
     * @param tabTypeName Oracle 集合类型名 (如: "T_EMPLOYEE_TAB")
     * @param items 待插入的实体列表
     * @param converter 核心！用于将实体 T 转换为 Object[] 数组的 Lambda 表达式 (与 Oracle Object 属性顺序匹配)。
     * @return 包含 OUT 参数（状态码 P_OUT_CODE 和消息 P_OUT_MSG）的 Map
     */
    /**
     * 【最终修复版】通用方法：调用接收 Oracle 集合类型参数的批量插入存储过程。
     * 使用 CallableStatementCallback 避免 Java 8/Spring API 冲突。
     */
//    @SuppressWarnings("deprecation")  //来抑制ArrayDescriptor警告
//    public <T> Map<String, Object> executeBatchInsert(
//            final String procedureName,
//            final String objTypeName,
//            final String tabTypeName,
//            final List<T> items,
//            final Function<T, Object[]> converter) {
//
//        // 存储过程调用格式：{call PROCEDURE_NAME(IN_ARRAY, OUT_CODE, OUT_MSG)}
//        final String procedureCall = "{call " + procedureName + "(?, ?, ?)}";
//
//        // 1. 【Java 8 兼容】无需声明参数列表，所有操作在 Callback 中完成。
//
//        // 2. 使用 JdbcTemplate.execute 执行 CallableStatementCallback
//        // 签名: <T> T execute(String callString, CallableStatementCallback<T> action)
//        return jdbcTemplate.execute(procedureCall, (CallableStatement cs) -> {
//
//            // --- A. 获取底层 OracleConnection ---
//            OracleConnection oracleConn;
//            Connection con = cs.getConnection(); // 通过 CallableStatement 获取连接
//            try {
//                if (con.isWrapperFor(OracleConnection.class)) {
//                    oracleConn = con.unwrap(OracleConnection.class);
//                } else if (con instanceof OracleConnection) {
//                    oracleConn = (OracleConnection) con;
//                } else {
//                    throw new SQLException("Connection is not an OracleConnection.");
//                }
//            } catch (SQLException e) {
//                // 抛出运行时异常，交给 Spring 事务管理
//                throw new DataAccessException("Failed to unwrap connection to OracleConnection: " + e.getMessage()) {
//                };
//            }
//
//            // --- B. 构造数据数组 ---
//            Object[][] data = new Object[items.size()][];
//            for (int i = 0; i < items.size(); i++) {
//                data[i] = converter.apply(items.get(i));
//            }
//
//            // --- C. 创建 Oracle ARRAY 对象 ---
//            ArrayDescriptor objDescriptor = ArrayDescriptor.createDescriptor(objTypeName, oracleConn);
//            ARRAY oracleArray = new ARRAY(objDescriptor, oracleConn, data);
//
//            // --- D. 绑定参数和注册 OUT 参数 ---
//            // 绑定输入参数 1: 集合 ARRAY
//            cs.setArray(1, oracleArray);
//
//            // 注册输出参数 2: 状态码 (P_OUT_CODE)
//            cs.registerOutParameter(2, Types.INTEGER);
//
//            // 注册输出参数 3: 消息 (P_OUT_MSG)
//            cs.registerOutParameter(3, Types.VARCHAR);
//
//            // --- E. 执行存储过程 ---
//            cs.execute();
//
//            // --- F. 解析并返回结果 Map ---
//            Map<String, Object> outMap = new HashMap<>();
//            outMap.put("P_OUT_CODE", cs.getInt(2));
//            outMap.put("P_OUT_MSG", cs.getString(3));
//
//            return outMap;
//        });
//    }

    // 在 SPQueryDao.java 中
    @SuppressWarnings("deprecation")
    public <T> Map<String, Object> executeBatchInsert(
            final String procedureName,
            final String objTypeName,
            final String tabTypeName,
            final List<T> items,
            final Function<T, Object[]> converter) {

        final String procedureCall = "{call " + procedureName + "(?, ?, ?)}";

        // 尽量避免使用复杂的 finally 块，让 Spring 自动管理资源
        return jdbcTemplate.execute(procedureCall, (CallableStatement cs) -> {

            // 1. 获取原生连接
            OracleConnection oracleConn = getNativeOracleConnection(cs.getConnection());

            // 2. 构造数据数组
            Object[][] data = new Object[items.size()][];
            for (int i = 0; i < items.size(); i++) {
                data[i] = converter.apply(items.get(i));
            }

            // 3. 创建 Oracle ARRAY 对象 (不使用 try-finally 块管理)
            ArrayDescriptor objDescriptor = ArrayDescriptor.createDescriptor(objTypeName, oracleConn);
            ARRAY oracleArray = new ARRAY(objDescriptor, oracleConn, data);

            // 4. 绑定参数和注册 OUT 参数
            cs.setArray(1, oracleArray);
            cs.registerOutParameter(2, Types.INTEGER); // P_OUT_CODE
            cs.registerOutParameter(3, Types.VARCHAR); // P_OUT_MSG

            // 5. 执行存储过程
            cs.execute();

            // 6. 解析并返回结果 Map
            Map<String, Object> outMap = new HashMap<>();
            outMap.put("P_OUT_CODE", cs.getInt(2));
            outMap.put("P_OUT_MSG", cs.getString(3));

            return outMap;
        });
    }

    // 辅助方法：确保连接获取失败时抛出 RuntimeException
    private OracleConnection getNativeOracleConnection(Connection con) throws SQLException {
        OracleConnection oracleConn = null;
        if (con.isWrapperFor(OracleConnection.class)) {
            oracleConn = con.unwrap(OracleConnection.class);
        } else if (con instanceof OracleConnection) {
            oracleConn = (OracleConnection) con;
        }
        if (oracleConn == null) {
            // 转换为 RuntimeException，让 Spring 捕获
            throw new DataAccessException("Failed to obtain native OracleConnection.") {
            };
        }
        return oracleConn;
    }

//
//    /**
//     * 【测试方法】直接调用批量插入，用于连接性验证和调试。
//     */
//    public Map<String, Object> testBatchInsertMppOperConnection() {
//
//        System.out.println("--- Starting Batch Insert Test ---");
//
//        // 1. 准备测试数据 (使用 Java 8 兼容方式)
//        List<YQOperator> testList = new ArrayList<>();
//        testList.add(YQOperator.builder().
//                operatorNo("TEST_001").operatorName("Test Operator").
//                build());
//        testList.add(YQOperator.builder().
//                operatorNo("TEST_002").operatorName("Test2 Operator").
//                build());
//
//        // 2. Oracle 元数据 (请确保这些名称和大小写是正确的)
//        final String procedureName = "P_BATCH_CASH_REG_OPER";
//        final String objTypeName = "T_CASH_REG_OPERATOR_OBJ";
//        final String tabTypeName = "T_CASH_REG_OPERATOR_TAB";
//
//        // 3. 核心 Lambda 转换函数 (顺序必须与 Oracle 对象类型一致)
//        Function<YQOperator, Object[]> converter = (YQOperator oper) -> new Object[]{
//                oper.getOperatorNo(),
//                oper.getOperatorName(),
//                oper.getDepartmentId(),
//                oper.getIsValid(),
//                oper.getCreator(),
//                oper.getCreateTime(),
//                oper.getUpdateTime()
//        };
//
//        Map<String, Object> result;
//        try {
//            // 4. 调用通用的批量插入方法
//            result = this.executeBatchInsert(
//                    procedureName,
//                    objTypeName,
//                    tabTypeName,
//                    testList,
//                    converter
//            );
//
//            // 5. 打印结果
//            Integer code = (Integer) result.get("A_RETCODE");
//            String message = (String) result.get("A_ERRMSG");
//
//            System.out.println("✅ Batch Test Complete.");
//            System.out.println("   Code: " + code);
//            System.out.println("   Message: " + message);
//
//            return result;
//
//        } catch (Exception e) {
//            // 6. 打印任何捕获到的 Spring 或底层异常
//            System.err.println("❌ Batch Test Failed Due to Exception:");
//            e.printStackTrace(System.err);
//
//            Map<String, Object> errorMap = new HashMap<>();
//            errorMap.put("P_OUT_CODE", -999);
//            errorMap.put("P_OUT_MSG", "System Error: " + e.getMessage());
//            return errorMap;
//        }
//    }
}