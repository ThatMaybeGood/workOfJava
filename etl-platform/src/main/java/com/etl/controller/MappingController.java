package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.EtlColumnMapping;
import com.etl.service.admin.EtlColumnMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/etl/mapping")
@Tag(name = "字段映射管理", description = "ETL字段映射配置管理")
public class MappingController {

    @Autowired
    private EtlColumnMappingService columnMappingService;

    @PostMapping
    @Operation(summary = "新增字段映射")
    public ApiResponse<EtlColumnMapping> add(@RequestBody EtlColumnMapping mapping) {
        columnMappingService.save(mapping);
        return ApiResponse.success(mapping, "字段映射添加成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新字段映射")
    public ApiResponse<EtlColumnMapping> update(@PathVariable Long id, @RequestBody EtlColumnMapping mapping) {
        mapping.setId(id);
        columnMappingService.updateById(mapping);
        return ApiResponse.success(mapping, "字段映射更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除字段映射")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        columnMappingService.removeById(id);
        return ApiResponse.success("字段映射删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取字段映射详情")
    public ApiResponse<EtlColumnMapping> getById(@PathVariable Long id) {
        return ApiResponse.success(columnMappingService.getById(id));
    }

    @GetMapping("/task/{taskCode}")
    @Operation(summary = "获取任务的所有字段映射")
    public ApiResponse<List<EtlColumnMapping>> listByTaskCode(@PathVariable String taskCode) {
        return ApiResponse.success(columnMappingService.listByTaskCode(taskCode));
    }
}
