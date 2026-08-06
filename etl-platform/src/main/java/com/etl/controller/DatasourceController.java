package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.DatasourceConfig;
import com.etl.service.admin.DatasourceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/etl/datasource")
@Tag(name = "数据源管理", description = "ETL数据源配置管理")
public class DatasourceController {

    @Autowired
    private DatasourceConfigService datasourceConfigService;

    @PostMapping
    @Operation(summary = "新增数据源")
    public ApiResponse<DatasourceConfig> add(@RequestBody DatasourceConfig config) {
        datasourceConfigService.save(config);
        return ApiResponse.success(config, "数据源添加成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据源")
    public ApiResponse<DatasourceConfig> update(@PathVariable Long id, @RequestBody DatasourceConfig config) {
        config.setId(id);
        datasourceConfigService.updateById(config);
        return ApiResponse.success(config, "数据源更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据源")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        datasourceConfigService.removeById(id);
        return ApiResponse.success("数据源删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据源详情")
    public ApiResponse<DatasourceConfig> getById(@PathVariable Long id) {
        return ApiResponse.success(datasourceConfigService.getById(id));
    }

    @GetMapping
    @Operation(summary = "获取所有数据源")
    public ApiResponse<List<DatasourceConfig>> list() {
        return ApiResponse.success(datasourceConfigService.list());
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取启用的数据源")
    public ApiResponse<List<DatasourceConfig>> listEnabled() {
        return ApiResponse.success(datasourceConfigService.listEnabled());
    }

    @GetMapping("/{id}/test")
    @Operation(summary = "测试数据源连接")
    public ApiResponse<Boolean> testConnection(@PathVariable Long id) {
        boolean result = datasourceConfigService.testConnection(id);
        return ApiResponse.success(result, result ? "连接成功" : "连接失败");
    }
}
