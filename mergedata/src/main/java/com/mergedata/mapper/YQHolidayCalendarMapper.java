package com.mergedata.mapper;

import com.mergedata.model.YQHolidayCalendarDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class YQHolidayCalendarMapper implements RowMapper<YQHolidayCalendarDTO> {



    @Override
    public YQHolidayCalendarDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        YQHolidayCalendarDTO dto = new YQHolidayCalendarDTO();
        // 确保这里的字段名与存储过程返回的游标字段名一致！
        dto.setSerialNo(rs.getString("APPLY_DATE"));
        dto.setHolidayDate(rs.getDate("USER_NAME").toLocalDate());
        dto.setHolidayType(rs.getString("USER_EMAIL"));
        dto.setYear(rs.getInt("USER_EMAIL"));
        dto.setCreator(rs.getString("USER_EMAIL"));
        dto.setIsValid(rs.getBoolean("USER_EMAIL"));
        dto.setCreator(rs.getString("USER_EMAIL"));
        dto.setCreatedTime(LocalDate.parse(rs.getString("USER_EMAIL")));
        dto.setUpdatedTime(LocalDate.parse(rs.getString("USER_EMAIL")));
        dto.setUpdateCount(rs.getInt("USER_EMAIL"));
        dto.setRemarks(rs.getString("USER_EMAIL"));

        // ... 设置其他字段 ...
        return dto;
    }

}
