package com.mergedata.mapper;

import com.mergedata.model.YQOperatorDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
@Component
public class YQOperatorMapper implements RowMapper<YQOperatorDTO> {
    @Override
    public YQOperatorDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        YQOperatorDTO dto = new YQOperatorDTO();
        dto.setOperatorNo(rs.getString("OPERATOR_NO"));
        dto.setOperatorName(rs.getString("OPERATOR_NAME"));
        dto.setDepartmentId(rs.getString("DEPARTMENT_ID"));
        dto.setValid(rs.getBoolean("VALID"));
        dto.setCreator(rs.getString("CREATOR"));
        dto.setCreateTime(rs.getDate("CREATE_TIME").toLocalDate());
        dto.setUpdater(rs.getString("UPDATER"));

        return dto;
    }
}
