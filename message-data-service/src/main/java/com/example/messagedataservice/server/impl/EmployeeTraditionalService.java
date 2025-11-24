package com.example.messagedataservice.server;

import com.example.messagedataservice.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// 根据您的数据库选择导入（Oracle 或 PostgreSQL）
import java.sql.Types;
// 如果是 Oracle 数据库
// import oracle.jdbc.OracleTypes;

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

    // 方法1：获取员工数量 - 修复版本
    public Integer getEmployeeCountTraditional(Integer deptId) {
        return jdbcTemplate.execute((Connection connection) -> {
            CallableStatement callableStatement = connection.prepareCall("{call GET_EMPLOYEE_COUNT(?, ?)}");
            callableStatement.setInt(1, deptId);
            callableStatement.registerOutParameter(2, Types.NUMERIC);
            return callableStatement;
        }, (CallableStatement callableStatement) -> {
            callableStatement.execute();
            return callableStatement.getInt(2);
        });
    }

    // 方法2：获取部门员工列表 - 修复版本
    public List<Employee> getEmployeesByDeptTraditional(Integer deptId) {
        return jdbcTemplate.execute((Connection connection) -> {
            CallableStatement callableStatement = connection.prepareCall("{call GET_EMPLOYEES_BY_DEPT(?, ?)}");
            callableStatement.setInt(1, deptId);

            // 根据数据库类型选择
            // 如果是 Oracle 数据库，使用：
            // callableStatement.registerOutParameter(2, OracleTypes.CURSOR);

            // 如果是 PostgreSQL 或其他数据库，使用：
            callableStatement.registerOutParameter(2, Types.REF_CURSOR);

            return callableStatement;
        }, (CallableStatement callableStatement) -> {
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
            rs.close();
            return employees;
        });
    }
}