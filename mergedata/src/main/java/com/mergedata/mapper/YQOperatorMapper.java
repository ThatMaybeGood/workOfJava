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

        // --- 修正后的映射 ---

        // OPERATORNO (匹配)
        dto.setOperatorNo(rs.getString("OPERATORNO"));

        // OPERATORNAME (匹配)
        dto.setOperatorName(rs.getString("OPERATORNAME"));

        // 修正 1：列名由 VALID 更改为 ISVALID
        dto.setIsValid(rs.getBoolean("ISVALID"));

        // CREATOR (匹配)
        dto.setCreator(rs.getString("CREATOR"));

        // 修正 2：列名由 CREATE_TIME 更改为 CREATETIME
        // 推荐使用 rs.getTimestamp() 并转换为 LocalDateTime，以避免时区问题，
        // 但如果您的 DTO 字段是 LocalDate，我们继续使用 getDate。
        if (rs.getDate("CREATETIME") != null) {
            dto.setCreateTime(rs.getDate("CREATETIME").toLocalDate());
        } else {
            dto.setCreateTime(null);
        }

        // 修正 3：UPDATER (匹配)
        dto.setUpdater(rs.getString("UPDATER"));

        // 修正 4：新增 UPDATETIME 映射（如果 DTO 中有该字段）
        // 假设 DTO 中有 setUpdateTime(LocalDate) 方法，如果您的 DTO 中没有此字段，请删除此段
        /*
        if (rs.getDate("UPDATETIME") != null) {
            dto.setUpdateTime(rs.getDate("UPDATETIME").toLocalDate());
        } else {
            dto.setUpdateTime(null);
        }
        */

        return dto;
    }
}