package com.example.messagedataservice.server;

import com.example.messagedataservice.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 18:01
 */
@Service
public class EmployeeTraditionalService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 使用 CallableStatementCreator
    public Integer getEmployeeCountTraditional(Integer deptId) {
        return jdbcTemplate.execute(connection -> {
            CallableStatement callableStatement = connection.prepareCall("{call GET_EMPLOYEE_COUNT(?, ?)}");
            callableStatement.setInt(1, deptId);
            callableStatement.registerOutParameter(2, Types.NUMERIC);
            return callableStatement;
        }, callableStatement -> {
            callableStatement.execute();
            return callableStatement.getInt(2);
        });
    }

    // 调用返回游标的存储过程
    public List<Employee> getEmployeesByDeptTraditional(Integer deptId) {
        return jdbcTemplate.execute(connection -> {
            CallableStatement callableStatement = connection.prepareCall("{call GET_EMPLOYEES_BY_DEPT(?, ?)}");
            callableStatement.setInt(1, deptId);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            return callableStatement;
        }, callableStatement -> {
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(2);
            List<Employee> employees = new ArrayList<>();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getLong("employee_id"));
                employee.setEmployeeName(rs.getString("employee_name"));
                employee.setSalary(rs.getBigDecimal("salary"));
                employee.setDepartmentId(rs.getInt("department_id"));
                employees.add(employee);
            }
            return employees;
        });
    }
}