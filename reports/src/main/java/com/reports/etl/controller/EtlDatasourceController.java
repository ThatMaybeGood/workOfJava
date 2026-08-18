package com.reports.etl.controller;

import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlDatasource;
import com.reports.etl.service.core.EtlMetaDao;
import com.reports.etl.service.registry.EtlDataSourceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/datasource")
public class EtlDatasourceController {

    private final EtlMetaDao metaDao;
    private final EtlDataSourceRegistry registry;

    public EtlDatasourceController(EtlMetaDao metaDao, EtlDataSourceRegistry registry) {
        this.metaDao = metaDao;
        this.registry = registry;
    }

    @GetMapping("/list")
    public ApiResponse<?> list() {
        List<EtlDatasource> all = metaDao.listDatasources();
        Map<String, Object> wrap = new java.util.HashMap<>();
        wrap.put("records", all);
        wrap.put("total", all.size());
        return ApiResponse.success(wrap);
    }

    @GetMapping("/{id}")
    public ApiResponse<EtlDatasource> get(@PathVariable Long id) {
        return ApiResponse.success(metaDao.getDatasource(id));
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody EtlDatasource ds) {
        if (ds.getPassword() != null && !ds.getPassword().isEmpty()) {
            ds.setPassword(com.reports.etl.util.DsEncryptUtil.encrypt(ds.getPassword()));
        }
        Long id = metaDao.insertDatasource(ds);
        ds.setId(id);
        if (ds.getEnabled() == null || ds.getEnabled() == 1) {
            registry.refresh(id);
        }
        return ApiResponse.success("数据源创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlDatasource ds) {
        ds.setId(id);
        // 密码留空则保留原密码
        if (ds.getPassword() == null || ds.getPassword().isEmpty()) {
            EtlDatasource old = metaDao.getDatasource(id);
            ds.setPassword(old != null ? old.getPassword() : ds.getPassword());
        } else if (!ds.getPassword().equalsIgnoreCase("******")) {
            ds.setPassword(com.reports.etl.util.DsEncryptUtil.encrypt(ds.getPassword()));
        }
        metaDao.updateDatasource(ds);
        registry.refresh(id);
        return ApiResponse.success("数据源更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        metaDao.deleteDatasource(id);
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