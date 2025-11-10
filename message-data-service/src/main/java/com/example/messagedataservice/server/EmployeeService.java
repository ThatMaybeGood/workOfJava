package com.example.messagedataservice.server;

import com.example.messagedataservice.entity.BusinessException;
import com.example.messagedataservice.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:37
 */
@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer getEmployeeCount(Integer deptId) {
        // 参数验证
        if (deptId == null || deptId <= 0) {
            throw new BusinessException("VALID_001", "部门ID必须大于0");
        }

        try {
            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("GET_EMPLOYEE_COUNT")
                    .declareParameters(
                            new SqlParameter("p_dept_id", Types.NUMERIC),
                            new SqlOutParameter("p_count", Types.NUMERIC)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("p_dept_id", deptId);

            Map<String, Object> result = simpleJdbcCall.execute(inParams);

            Number count = (Number) result.get("p_count");
            if (count == null) {
                throw new BusinessException("DB_002", "存储过程返回结果为空");
            }

            return count.intValue();

        } catch (DataAccessException ex) {
            logger.error("调用存储过程失败，部门ID: {}", deptId, ex);
            throw new BusinessException("DB_001", "获取员工数量失败", ex);
        }
    }

    public void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new BusinessException("VALID_002", "员工信息不能为空");
        }

        if (employee.getEmployeeName() == null || employee.getEmployeeName().trim().isEmpty()) {
            throw new BusinessException("VALID_003", "员工姓名不能为空");
        }

        if (employee.getSalary() == null || employee.getSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("VALID_004", "员工薪资必须大于等于0");
        }
    }
}