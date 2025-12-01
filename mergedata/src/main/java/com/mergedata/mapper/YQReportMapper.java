package com.mergedata.mapper;

import com.mergedata.dao.AbstractSPQueryMapper;
import com.mergedata.model.ReportDTO;
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
public class YQReportMapper extends AbstractSPQueryMapper implements RowMapper<ReportDTO> {

    @Value("${sp.name.query.report}")
    private String SP_Query_Name;

    @Value("${sp.name.insert.report}")
    private String SP_Insert_Name;

    // 【核心】：需要 ThreadLocal 来安全地传递参数
    private final ThreadLocal<Map<String, Object>> threadLocalInParams = new ThreadLocal<>();


    @Override
    public ReportDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReportDTO dto = new ReportDTO();
        dto.setSerialNo(rs.getString("SERIAL_NO"));
        dto.setOperatorNo(rs.getString("EMP_ID"));
        dto.setReportDate(rs.getString("REPORT_DATE"));
        dto.setHisAdvancePayment(rs.getBigDecimal("HISADVANCEPAYMENT"));
        dto.setHisMedicalIncome(rs.getBigDecimal("HISMEDICALINCOME"));
        dto.setHisRegistrationIncome(rs.getBigDecimal("HISREGISTRATIONINCOME"));
        //..................................未添加完
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
    public List<ReportDTO> getReportList(Map<String, Object> inParams) {

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
