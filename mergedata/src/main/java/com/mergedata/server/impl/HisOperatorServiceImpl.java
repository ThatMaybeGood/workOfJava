package com.mergedata.server.impl;

import com.mergedata.dao.YQStoredProcedureDao;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.YQOperatorDTO;
import com.mergedata.server.HisOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException; // 导入 BadSqlGrammarException 以进行详细异常处理
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.*;

@Slf4j
@Service
public class HisOperatorServiceImpl implements HisOperatorService {

    @Autowired
    YQStoredProcedureDao yqStoredProcedureDao;

    @Autowired
    YQOperatorMapper yqOperatorMapper;


    @Override
    public List<YQOperatorDTO> findData() {
        // 默认为空列表，用于失败或游标结果为空时返回
        List<YQOperatorDTO> resultList = new ArrayList<>();

        try {
            // --- 1. 定义 OUT 参数和游标名（必须与存储过程的定义完全匹配） ---
            Map<String, Integer> outParamNames = new HashMap<>();
            outParamNames.put("A_CODE", Types.INTEGER);
            outParamNames.put("A_MESSAGE", Types.VARCHAR);
            String cursorName = "A_CURSOR";

            // --- 2. 调用通用 DAO 方法 (传入空 IN 参数 Map) ---
            Map<String, Object> results = yqStoredProcedureDao.executeQueryMultipleOutParams(
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
                resultList = (List<YQOperatorDTO>) cursorResult;
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
}