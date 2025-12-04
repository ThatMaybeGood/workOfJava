package com.mergedata.mapper;


import com.mergedata.dao.AbstractSPQueryMapper;
import com.mergedata.model.YQCashRegRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Repository
@Component
@Slf4j
public class YQCashMapper extends AbstractSPQueryMapper implements RowMapper<YQCashRegRecord> {

    @Value("${sp.name.query.cash}")
    private String SP_Query_Name;


    private final ThreadLocal<Map<String, Object>> threadLocalInParams = new ThreadLocal<>();


    // 定义 YQCashRegRecordDTO 的 RowMapper (用于映射游标结果)
    // 建议将这个 RowMapper 独立定义为一个公共类，以便重用
    @Override
    public YQCashRegRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        YQCashRegRecord dto = new YQCashRegRecord();
        // 确保这里的字段名与存储过程返回的游标字段名一致！
        dto.setApplyDate(rs.getString("APPLY_DATE"));
        dto.setCreateTime(rs.getString("CREATE_TIME"));
        dto.setOperator(rs.getString("OPERATOR"));
        dto.setRetainedCash(rs.getBigDecimal("AMOUNT"));
        dto.setOperatorNo(rs.getString("OPERATOR_NO"));
        dto.setSaveDate(rs.getString("SAVE_DATE"));

        // ... 设置其他字段 ...
        return dto;
    }

    @Override
    protected String getSPQueryName() {
        return this.SP_Query_Name;
    }

    @Override
    protected String getSPInsertName() {
        return null;
    }

    @Override
    protected RowMapper getRowMapper() {
        return this;
    }



    // 【重要】：必须覆盖 getInParams() 来获取 ThreadLocal 中的参数
    @Override
    protected Map<String, Object> getInParams() {
        Map<String, Object> params = threadLocalInParams.get();
        return params != null ? params : Collections.emptyMap();
    }

    /**
     * Service 层的带参接口。
     */
    public List<YQCashRegRecord> getMultParams(Map<String, Object> inParams) {

        // 1. 设置 ThreadLocal
        threadLocalInParams.set(inParams);

        try {
            // 2. 调用基类方法
            return executeSPQuery();
        } finally {
            // 3. 清理 ThreadLocal
            threadLocalInParams.remove();
        }
    }

}
