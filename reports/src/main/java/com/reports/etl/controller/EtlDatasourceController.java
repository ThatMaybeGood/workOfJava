package com.reports.etl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.mapper.EtlDatasourceMapper;
import com.reports.etl.service.registry.EtlDataSourceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/datasource")
public class EtlDatasourceController {

    private final EtlDatasourceMapper datasourceMapper;
    private final EtlDataSourceRegistry registry;

    public EtlDatasourceController(EtlDatasourceMapper datasourceMapper, EtlDataSourceRegistry registry) {
        this.datasourceMapper = datasourceMapper;
        this.registry = registry;
    }

    @GetMapping("/list")
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        Page<EtlDatasource> pageResult = datasourceMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<EtlDatasource>().orderByDesc(EtlDatasource::getCreateTime)
        );
        Map<String, Object> wrap = new HashMap<>();
        wrap.put("records", pageResult.getRecords());
        wrap.put("total", pageResult.getTotal());
        ApiResponse<Map<String, Object>> resp = ApiResponse.success(wrap);
        return resp;
    }

    @GetMapping("/{id}")
    public ApiResponse<EtlDatasource> get(@PathVariable Long id) {
        EtlDatasource ds = datasourceMapper.selectById(id);
        return ApiResponse.success(ds);
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody EtlDatasource ds) {
        if (ds.getPassword() != null && ds.getPassword().startsWith("ENC(")) {
            ds.setPassword(ds.getPassword().substring(4, ds.getPassword().length() - 1));
        }
        datasourceMapper.insert(ds);
        registry.refresh(ds.getId());
        return ApiResponse.success("数据源创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlDatasource ds) {
        ds.setId(id);
        datasourceMapper.updateById(ds);
        registry.refresh(id);
        return ApiResponse.success("数据源更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        datasourceMapper.deleteById(id);
        registry.remove(id);
        return ApiResponse.success("数据源删除成功");
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> test(@PathVariable Long id) {
        return ApiResponse.success(registry.testConnection(id));
    }

    @GetMapping("/{id}/tables")
    public ApiResponse<List<Map<String, String>>> getTables(@PathVariable Long id) {
        return ApiResponse.success(registry.getTables(id));
    }

    @GetMapping("/{id}/columns/{tableName}")
    public ApiResponse<List<Map<String, Object>>> getColumns(@PathVariable Long id,
                                                              @PathVariable String tableName) {
        return ApiResponse.success(registry.getColumns(id, tableName));
    }
}