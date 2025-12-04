package com.mergedata.server.impl;

import com.mergedata.constants.ReqConstant;
import com.mergedata.mapper.YQOperatorMapper;
import com.mergedata.model.YQOperator;
import com.mergedata.server.HisDataService;
import com.mergedata.server.YQOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class YQOperatorServiceImpl implements YQOperatorService {


    @Autowired
    YQOperatorMapper yqOperatorMapper;

    @Autowired
    HisDataService hisdata;

    @Override
    public List<YQOperator> findAll() {
        try {
            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            List<YQOperator> rawRecords = yqOperatorMapper.getNoParams();
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
     * @param
     * @return
     */
    @Override
    @Transactional
    public Boolean insert(YQOperator operator) {
        if (operator == null) {
            // 如果列表为空，直接返回
            return false;
        }

        // 1. 调用 DAO 方法获取存储过程返回的结果列表
        Boolean b = yqOperatorMapper.insertMultParams(buildParams(operator, ReqConstant.SP_TYPE_INSERT));

        // ❗ 如果存储过程返回失败（b=false），应该抛出运行时异常
        // 这样 Spring 才能捕获到错误并触发 ROLLBACK
        if (!b) {
            throw new RuntimeException("存储过程调用失败，数据同步中断。");
        }
        return true;
    }

    /**
     * @Transactional 开启事务。
     * 事务的开始：方法调用前。
     * 事务的结束：方法成功执行后自动 COMMIT；如果抛出运行时异常，自动 ROLLBACK。
     */
    @Transactional
    @Override
    public Boolean batchInsert(List<YQOperator> list) {
        if (list == null || list.isEmpty()) {
            // 如果列表为空，直接返回
            return false;
        }


        for (YQOperator dto : list) {
            // 1. 调用 DAO 方法获取存储过程返回的结果列表
            Boolean b = insert(dto);
            // ❗ 如果存储过程返回失败（b=false），应该抛出运行时异常
            // 这样 Spring 才能捕获到错误并触发 ROLLBACK
            if (!b) {
                throw new RuntimeException("存储过程调用失败，数据同步中断。");
            }
        }
        return true;

    }

    @Override
    @Transactional
    public Boolean update(YQOperator operator) {
        if (operator == null) {
            // 如果列表为空，直接返回
            return false;
        }

        // 1. 调用 DAO 方法获取存储过程返回的结果列表
        Boolean b = yqOperatorMapper.insertMultParams(buildParams(operator, ReqConstant.SP_TYPE_UPDATE));

        // ❗ 如果存储过程返回失败（b=false），应该抛出运行时异常
        // 这样 Spring 才能捕获到错误并触发 ROLLBACK
        if (!b) {
            throw new RuntimeException("存储过程调用失败，数据同步中断。");
        }
        return true;
    }


    /**
     * 封装：构建存储过程所需的参数 Map
     *
     * @param dto  当前 DTO 对象 (作废时可传入 null)
     * @param type 操作类型 (INSERT/UPDATE/INVALIDATE)
     * @return 封装好的 Map
     */
    private Map<String, Object> buildParams(YQOperator dto,
                                            String type) {
        Map<String, Object> maps = new HashMap<>();

        // ❗ 优化：只有 INSERT/UPDATE 时才传入日期数据，作废时传入 null 减少冗余
        maps.put("A_OPERATOR_NO", dto.getOperatorNo());
        maps.put("A_OPERATOR_NAME", dto.getOperatorName());
        // 填充操作类型
        maps.put("A_TYPE", type);

        return maps;
    }

    /**
     * 封装：执行作废操作
     */
    private void executeInvalidate() {
        // ❗ 优化：调用封装方法，只传入作废必需的类型和序列号
        Map<String, Object> invalidateMap = buildParams(null, ReqConstant.SP_TYPE_UPDATE);
        // 假设您的存储过程能识别 A_TYPE=UPDATE 时执行全局作废
        yqOperatorMapper.insertMultParams(invalidateMap);
    }


}


