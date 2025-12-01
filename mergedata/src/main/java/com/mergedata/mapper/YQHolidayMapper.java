package com.mergedata.mapper;

import com.mergedata.dao.AbstractSPQueryMapper;
import com.mergedata.model.YQHolidayCalendarDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class YQHolidayMapper extends AbstractSPQueryMapper implements RowMapper<YQHolidayCalendarDTO> {

    @Value("${sp.name.query.holiday}")
    public String SP_Query_Name ;

    @Value("${sp.name.insert.holiday}")
    public String SP_Insert_Name ;

    // 【核心】：需要 ThreadLocal 来安全地传递参数
    private final ThreadLocal<Map<String, Object>> threadLocalInParams = new ThreadLocal<>();

    @Override
    public YQHolidayCalendarDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        YQHolidayCalendarDTO dto = new YQHolidayCalendarDTO();

        // 确保这里的字段名与存储过程返回的游标字段名一致！
        dto.setSerialNo(rs.getString("SERIAL_NO"));

        // 1. 使用 rs.getDate()：JDBC会忽略时间部分
        java.sql.Date sqlDate = rs.getDate("HOLIDAY_DATE");
        if (sqlDate != null) {
            dto.setHolidayDate(sqlDate.toLocalDate());
        } else {
            dto.setHolidayDate(null);
        }

        // 推荐使用 rs.getTimestamp()，这是最安全且不依赖格式字符串的方式
        if (rs.getTimestamp("CREATE_TIME") != null) {
            dto.setCreatedTime(rs.getTimestamp("CREATE_TIME").toLocalDateTime());
        } else {
            dto.setCreatedTime(null);
        }

        // ... 设置其他字段 ...
        return dto;
    }

    @Override
    protected String getSPQueryName() {
        return SP_Query_Name;
    }

    @Override
    protected String getSPInsertName() {
        return SP_Insert_Name;
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
    public List<YQHolidayCalendarDTO> getMultParams(Map<String, Object> inParams) {

        // 1. 设置 ThreadLocal
        threadLocalInParams.set(inParams);
        log.info("调用存储过程 {}：设置 {} 个输入参数。", getSPQueryName(), inParams.size());

        try {
            // 2. 调用基类方法
            return executeSPQuery();
        } finally {
            // 3. 清理 ThreadLocal
            threadLocalInParams.remove();
        }
    }
    public Boolean insertMultParams(Map<String, Object> inParams) {

        // 1. 设置 ThreadLocal
        threadLocalInParams.set(inParams);

        try {
            // 2. 调用基类方法
            return executeSPInsert();
        } finally {
            // 3. 清理 ThreadLocal
            threadLocalInParams.remove();
        }
    }
}
