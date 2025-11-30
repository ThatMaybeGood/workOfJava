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
import java.util.List;

@Component
@Slf4j
public class YQOperatorMapper extends AbstractSPQueryMapper implements RowMapper<YQOperator> {

    @Value("${sp.name.operator}")
    public String SP_NAME ;

    @Autowired
    SPQueryDao queryDao;




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
    protected String getSPName() {
        return SP_NAME;
    }

    @Override
    protected RowMapper getRowMapper() {
        return this;
    }

    // 【重要】：无需覆盖 getInParams()！它继承了基类的默认行为 (返回空 Map)

    /**
     * Service 层的无参接口。
     */
    public List<YQOperator> getYQOperators() {
        log.info("调用存储过程 {}：无输入参数。", getSPName());
        // 直接调用基类方法，参数自动解析为 Collections.emptyMap()
         return executeSPQuery();
    }
}