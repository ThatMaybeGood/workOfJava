package com.mergedata.dao;

import com.mergedata.constants.ReqConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象基类：用于封装通用的“调用带游标和出参的存储过程”的逻辑。
 */
@Slf4j
@Component
public abstract class AbstractSPQueryMapper<T> {

    @Autowired
    protected SPQueryDao queryDao; // 注入通用 DAO

    /**
     * 抽象方法：由子类实现，提供要调用的存储过程名称。
     */
    protected abstract String getSPName();

    /**
     * 抽象方法：由子类实现，提供数据结果集的映射器。
     */
    protected abstract RowMapper<T> getRowMapper();

    /**
     * 抽象方法：由子类实现，提供存储过程的输入参数。
     * 默认返回空Map，如果存储过程需要输入参数，子类需覆盖此方法。
     */
    protected Map<String, Object> getInParams() {
        return Collections.emptyMap();
    }


    /**
     * 公共方法：执行存储过程，获取数据列表。
     * @return 存储过程返回的实体列表，失败时返回空列表。
     */
    public List<T> executeSPQuery() {
        List<T> resultList = new ArrayList<>();
        String spName = getSPName(); // 获取子类定义的SP名称

        try {
            // 1. 定义 OUT 参数和游标名 (这些通常是常量，所以可以固定定义)
            Map<String, Integer> outParamNames = new HashMap<>();
            outParamNames.put(ReqConstant.SP_OUT_CODE, Types.INTEGER);
            outParamNames.put(ReqConstant.SP_OUT_MESSAGE, Types.VARCHAR);
            String cursorName = ReqConstant.SP_OUT_CURSOR;

            // 2. 调用通用 DAO 方法
            Map<String, Object> results = queryDao.executeQueryMultipleOutParams(
                    spName,
                    getRowMapper(), // 使用子类提供的 RowMapper
                    getInParams(),  // 使用子类提供的 IN 参数
                    outParamNames,
                    cursorName);

            // 3. 提取所有出参并进行处理

            // 3.1 提取游标结果 (A_CURSOR)
            Object cursorResult = results.get(cursorName);
            if (cursorResult instanceof List) {
                resultList = (List<T>) cursorResult;
                log.info("存储过程 {} 成功获取数据，共 {} 条记录.", spName, resultList.size());
            } else {
                // 处理游标为 null 或类型不匹配的情况
                log.warn("存储过程 {} 返回的游标 [{}] 结果无效 (null 或非 List).", spName, cursorName);
            }

            // 3.2 提取和记录其他 OUT 参数
            Integer intResult = (Integer) results.get(ReqConstant.SP_OUT_CODE);
            String varcharResult = (String) results.get(ReqConstant.SP_OUT_MESSAGE);
            log.info("{}存储过程 OUT 参数 [{}]: {}, [{}]: {}", spName,
                    ReqConstant.SP_OUT_CODE, intResult,
                    ReqConstant.SP_OUT_MESSAGE, varcharResult);

            return resultList;

        } catch (BadSqlGrammarException e) {
            log.error("执行存储过程 {} 异常：数据映射或SQL语法错误。请检查 RowMapper/SP 中的列名！", spName, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("执行存储过程 {} 发生未预期的运行时异常", spName, e);
            return Collections.emptyList();
        }
    }
}