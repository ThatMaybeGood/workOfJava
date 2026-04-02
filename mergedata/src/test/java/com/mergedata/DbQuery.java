package com.mergedata;

import com.mergedata.constants.Constant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DbQuery {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Test
    void oper(){
        String sql = "SELECT NVL(MAX(row_num), 0) + 1 FROM mpp_cash_reg_operator WHERE category = ?";

        Integer nextValue = jdbcTemplate.queryForObject(sql, Integer.class, Constant.TYPE_OUTP);
        System.out.printf(nextValue.toString());
    }
}
