package com.example.messagedataservice.controller;

import com.example.messagedataservice.entity.Employee;
import com.example.messagedataservice.server.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/10 17:53
 */
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Validated  // 启用方法级别参数验证
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 路径参数验证
     */
    @GetMapping("/count/{deptId}")
    public ResponseEntity<Integer> getEmployeeCount(
            @PathVariable @Min(value = 1, message = "部门ID必须大于0") Integer deptId) {
        Integer count = employeeService.getEmployeeCount(deptId);
        return ResponseEntity.ok(count);
    }

    /**
     * 查询参数验证
     */
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(
            @RequestParam @Min(1) Integer deptId,
            @RequestParam(required = false) String name) {
        // 业务逻辑
        return ResponseEntity.ok(new ArrayList<>());
    }

    /**
     * 请求体验证
     */
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        // 如果employee参数验证失败，会自动抛出MethodArgumentNotValidException
        Employee savedEmployee = employeeService.saveEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
    }
}