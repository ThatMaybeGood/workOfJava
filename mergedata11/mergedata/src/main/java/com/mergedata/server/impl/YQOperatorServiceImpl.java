package com.mergedata.server.impl;

import com.mergedata.dao.SPBatchInsertDao;
import com.mergedata.dao.SPQueryDao;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.YQOperator;
import com.mergedata.server.YQOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        try {

            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            List<YQOperator> rawRecords = yqOperatorMapper.getYQOperators();

            // 2. Service 层业务逻辑处理
            if (rawRecords.isEmpty()) {
                log.info("查询日期无记录返回。");
                return rawRecords;
            }
            log.info("成功获取 " + rawRecords.size() + " 条记录。");
            // 3. 返回最终处理结果
            return rawRecords;


        } catch (Exception e) {
            log.error("获取YQ数据异常", e);
            return new ArrayList<>();
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