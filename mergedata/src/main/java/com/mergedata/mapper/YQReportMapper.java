package com.mergedata.mapper;

import com.mergedata.model.ReportDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class YQReportMapper implements RowMapper<ReportDTO> {
    @Override
    public ReportDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReportDTO dto = new ReportDTO();
        dto.setSerialNo(rs.getString("SERIAL_NO"));
        dto.setOperatorNo(rs.getString("OPERATOR_NO"));
        //..................................未添加完





        return dto;
    }
}
