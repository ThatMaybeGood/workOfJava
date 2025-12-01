package com.mergedata.mapper;

import com.mergedata.dao.AbstractSPQueryMapper;
import com.mergedata.dao.SPQueryDao;
import com.mergedata.model.YQOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class YQOperatorMapper extends AbstractSPQueryMapper implements RowMapper<YQOperator> {

    @Value("${sp.name.query.operator}")

    public String SP_Query_Name ;
    @Value("${sp.name.insert.operator}")
    public String SP_Insert_Name ;

    @Autowired
    SPQueryDao queryDao;

    // 【核心】：需要 ThreadLocal 来安全地传递参数
    private final ThreadLocal<Map<String, Object>> threadLocalInParams = new ThreadLocal<>();



    @Override
    public YQOperator mapRow(ResultSet rs, int rowNum) throws SQLException {
        YQOperator dto = new YQOperator();

        // OPERATORNO (匹配)
        dto.setOperatorNo(rs.getString("OPERATOR_NO"));

        // OPERATORNAME (匹配)
        dto.setOperatorName(rs.getString("OPERATOR_NAME"));

        // 修正 1：列名由 VALID 更改为 ISVALID
        dto.setIsValid(rs.getBoolean("ISVALID"));

        // CREATOR (匹配)
        dto.setCreator(rs.getString("CREATOR"));

        // 修正 2：列名由 CREATE_TIME 更改为 CREATETIME
        // 推荐使用 rs.getTimestamp() 并转换为 LocalDateTime，以避免时区问题，
        // 但如果您的 DTO 字段是 LocalDate，我们继续使用 getDate。
        if (rs.getDate("CREATE_TIME") != null) {
            dto.setCreateTime(rs.getDate("CREATE_TIME").toLocalDate());
        } else {
            dto.setCreateTime(null);
        }
        // 修正 3：UPDATER (匹配)
        dto.setUpdater(rs.getString("UPDATOR"));

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

    // 【重要】：无需覆盖 getInParams()！它继承了基类的默认行为 (返回空 Map)

    /**
     * Service 层的无参接口。
     */
    public List<YQOperator> getNoParams() {
        log.info("调用存储过程 {}：无输入参数。", getSPQueryName());
        // 直接调用基类方法，参数自动解析为 Collections.emptyMap()
         return executeSPQuery();
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