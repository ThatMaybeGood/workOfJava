package com.mergedata.mapper;


import com.mergedata.model.YQCashRegRecordDTO;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class YQCashRegRecordMapper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 定义 YQCashRegRecordDTO 的 RowMapper (用于映射游标结果)
    // 建议将这个 RowMapper 独立定义为一个公共类，以便重用
    private final RowMapper<YQCashRegRecordDTO> cashRegRowMapper = new RowMapper<YQCashRegRecordDTO>() {
        @Override
        public YQCashRegRecordDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            YQCashRegRecordDTO dto = new YQCashRegRecordDTO();
            // 确保这里的字段名与存储过程返回的游标字段名一致！
            dto.setApplyDate(rs.getString("APPLY_DATE"));
            dto.setCreateTime(rs.getString("USER_NAME"));
            dto.setOperatorNo(rs.getString("USER_EMAIL"));
            dto.setOperatType(rs.getString("USER_EMAIL"));

            dto.setWindowNo(rs.getString("USER_EMAIL"));
            dto.setSechduling(rs.getString("USER_EMAIL"));
            dto.setSaveDate(rs.getString("USER_EMAIL"));
            dto.setRetainedCash(rs.getBigDecimal("USER_EMAIL"));
            dto.setOperatorNo(rs.getString("USER_EMAIL"));

            // ... 设置其他字段 ...
            return dto;
        }
    };


    /**
     * 【具体业务方法】调用存储过程 GET_USER_RECORDS
     * 该过程接收一个 String 参数，返回一个 YQCashRegRecordDTO 游标。
     *
     * @param inputDate 传入存储过程的查询日期参数
     * @return YQCashRegRecordDTO 的列表
     */
    public List<YQCashRegRecordDTO> getCashRegRecordsByDate(String inputDate) {

        // 1. 存储过程调用语句：包含一个输入参数 (?) 和一个输出游标参数 (?)
        final String procedureCall = "{call GET_USER_RECORDS(?, ?)}";

        // 2. 使用 execute 方法执行 CallableStatement 原生调用
        return jdbcTemplate.execute(procedureCall, (CallableStatement cs) -> {

            // 3. 设置输入参数
            cs.setString(1, inputDate); // 绑定第一个参数：输入日期

            // 4. 注册 OUT 参数 (游标)
            // 绑定第二个参数：输出游标
            cs.registerOutParameter(2, OracleTypes.CURSOR);

            // 5. 执行存储过程
            cs.execute();

            // 6. 获取游标结果集 (游标在第二个位置)
            ResultSet rs = (ResultSet) cs.getObject(2);

            // 7. 使用 RowMapper 将 ResultSet 映射为 List
            List<YQCashRegRecordDTO> resultList = new ArrayList<>();

            if (rs != null) {
                try {
                    int rowNum = 0;
                    while (rs.next()) {
                        // 使用我们定义的 RowMapper 进行映射
                        resultList.add(cashRegRowMapper.mapRow(rs, rowNum++));
                    }
                } finally {
                    // 确保 ResultSet 被关闭
                    rs.close();
                }
            }
            return resultList;
        });
    }

    // 您可以根据需要，为其他存储过程编写类似的、独立的 DAO 方法。
    // 例如：public List<Order> getOrdersByStatus(String status) { ... }
}

// -------------------------------------------------------------
// 注意：以下是假定的 DTO 类结构，您需要确保它在您的项目中存在
// -------------------------------------------------------------
/*
public class YQCashRegRecordDTO {
    private String applyDate;
    private String name;
    private String email;
    // Getters and Setters
    public void setApplyDate(String applyDate) { this.applyDate = applyDate; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    // ...
}
*/