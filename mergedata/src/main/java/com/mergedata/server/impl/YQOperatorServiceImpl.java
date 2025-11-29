package com.mergedata.server.impl;

import com.mergedata.dao.SPBatchInsertDao;
import com.mergedata.dao.SPQueryDao;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.YQOperator;
import com.mergedata.server.YQOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException; // 导入 BadSqlGrammarException 以进行详细异常处理
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.*;

@Slf4j
@Service
public class YQOperatorServiceImpl implements YQOperatorService {

    @Autowired
    SPQueryDao queryDao;

    @Autowired
    SPBatchInsertDao batchInsertDao;

    @Autowired
    YQOperatorMapper yqOperatorMapper;


    @Override
    public List<YQOperator> findData() {
        // 默认为空列表，用于失败或游标结果为空时返回
        List<YQOperator> resultList = new ArrayList<>();

        try {
            // --- 1. 定义 OUT 参数和游标名（必须与存储过程的定义完全匹配） ---
            Map<String, Integer> outParamNames = new HashMap<>();
            outParamNames.put("A_CODE", Types.INTEGER);
            outParamNames.put("A_MESSAGE", Types.VARCHAR);
            String cursorName = "A_CURSOR";

            // --- 2. 调用通用 DAO 方法 (传入空 IN 参数 Map) ---
            Map<String, Object> results = queryDao.executeQueryMultipleOutParams(
                    "SP_OPERATOR",
                    yqOperatorMapper,
                    Collections.emptyMap(),
                    outParamNames,
                    cursorName);

            // --- 3. 提取所有出参并进行空值检查 ---

            // 3.1 提取游标结果 (A_CURSOR)
            Object cursorResult = results.get(cursorName);

            if (cursorResult == null) {
                // 游标结果为 null，记录警告
                log.warn("存储过程 SP_OPERATOR 返回的游标 [{}] 结果为 null.", cursorName);
            } else if (cursorResult instanceof List) {
                // 游标结果非 null 且类型正确，则进行安全转换
                resultList = (List<YQOperator>) cursorResult;
                log.info("成功获取操作员数据，共 {} 条记录.", resultList.size());
            } else {
                // 类型不匹配，记录错误
                log.error("存储过程 SP_OPERATOR 返回的游标 [{}] 类型错误，期望 List，实际为 {}.",
                        cursorName, cursorResult.getClass().getName());
            }


            // 3.2 提取 Integer (OUT 参数 A_CODE)
            Integer intResult = (Integer) results.get("A_CODE");
            log.info("存储过程 OUT 参数 [A_CODE]: {}", intResult);

            // 3.3 提取 String (OUT 参数 A_MESSAGE)
            String varcharResult = (String) results.get("A_MESSAGE");
            log.info("存储过程 OUT 参数 [A_MESSAGE]: {}", varcharResult);

            // --- 4. 返回游标结果 List ---
            return resultList;

        } catch (BadSqlGrammarException e) {
            // 特别捕获 BadSqlGrammarException，通常指示列名映射或 SQL 语法错误
            log.error("获取操作员数据异常：数据映射或SQL语法错误。请检查 YQOperatorMapper.java 中的列名映射是否与数据库返回的列名完全一致！", e);
            return Collections.emptyList();
        } catch (Exception e) {
            // 捕获所有未预期的运行时异常
            log.error("获取操作员数据发生未预期的运行时异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量插入操作员数据
     *
     * @param yqOperatorDTOS
     * @return
     */
    @Override
    public Boolean batchInsert(List<YQOperator> yqOperatorList) {


        // ... (省略常量定义) ...
        // 定义存储过程名称和类型名
        final String procedureName = "P_BATCH_CASH_REG_OPER";
        // 定义 Oracle 对象类型和表类型的名称
        final String objTypeName = "T_CASH_REG_OPERATOR_OBJ";
        // 定义 Oracle 表类型的名称
        final String tabTypeName = "T_CASH_REG_OPERATOR_TAB";

        Map<String, Object> daoResult;
        try {
            // 1. 调用 DAO 执行批量操作
            daoResult = batchInsertDao.executeBatchInsert(
                    procedureName,
                    objTypeName,
                    tabTypeName,
                    yqOperatorList,
                    // Lambda 转换函数
                    (YQOperator yqOperator) -> new Object[]
                            {
                                    yqOperator.getOperatorNo(),
                                    yqOperator.getOperatorName(),
                                    yqOperator.getDepartmentId(),
                                    yqOperator.getIsValid(),
                                    yqOperator.getCreator(),
                                    yqOperator.getCreateTime(),
                                    yqOperator.getUpdater(),
                                    yqOperator.getUpdateTime()
                            }
            );

        } catch (Exception e) {
            // 2. 捕获任何底层技术异常（如连接失败、SQL语法错误等）
            log.info("Database batch execution failed: " + e.getMessage());
            // 返回一个表示系统错误的 ServiceResult
            return false;

        }

        // 3. 从 DAO 返回的 Map 中提取状态码和消息
        Integer code = (Integer) daoResult.get("P_OUT_CODE");
        String message = (String) daoResult.get("P_OUT_MSG");

        // 4. 核心业务判断：Code == 1 视为成功
        if (code != null && code.intValue() == 1) {
             log.info("Batch insert successful. Message: {}", message);
            return true;
        } else {
            // 5. 存储过程返回非 1 错误码，记录业务错误，返回失败
            System.err.println("❌ 存储过程返回业务失败。Code: " + code + ", Message: " + message);
             log.warn("Batch insert failed by SP logic. Code: {}, Message: {}", code, message);
            return false;

        }
    }
}