package com.reports.etl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlMapping;
import com.reports.etl.service.core.EtlMetaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/etl/mapping")
public class EtlMappingController {

    private final EtlMetaDao metaDao;

    public EtlMappingController(EtlMetaDao metaDao) {
        this.metaDao = metaDao;
    }

    @GetMapping("/list/{taskId}")
    public ApiResponse<List<EtlMapping>> list(@PathVariable Long taskId) {
        return ApiResponse.success(metaDao.listMappings(taskId));
    }

    /**
     * 批量保存映射（替换式）
     * 请求体: { taskId: 1, mappings: [ {srcField, tgtField, defaultValue, isUpdateCol, srcType, tgtType, sortOrder} ] }
     */
    @PostMapping("/batch")
    public ApiResponse<String> batch(@RequestBody Map<String, Object> payload) {
        Long taskId = Long.valueOf(payload.get("taskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mappings = (List<Map<String, Object>>) payload.getOrDefault("mappings", java.util.Collections.emptyList());
        ObjectMapper om = new ObjectMapper();
        metaDao.deleteMappingsByTask(taskId);
        int sort = 0;
        for (Map<String, Object> m : mappings) {
            EtlMapping mapping = om.convertValue(m, EtlMapping.class);
            mapping.setTaskId(taskId);
            if (mapping.getSortOrder() == null) mapping.setSortOrder(sort);
            metaDao.insertMapping(mapping);
            sort++;
        }
        return ApiResponse.success("映射保存成功");
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody EtlMapping mapping) {
        metaDao.insertMapping(mapping);
        return ApiResponse.success("映射添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlMapping mapping) {
        mapping.setId(id);
        metaDao.updateMapping(mapping);
        return ApiResponse.success("映射更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        metaDao.deleteMapping(id);
        return ApiResponse.success("映射删除成功");
    }

    @DeleteMapping("/task/{taskId}")
    public ApiResponse<String> deleteByTask(@PathVariable Long taskId) {
        metaDao.deleteMappingsByTask(taskId);
        return ApiResponse.success("映射清空成功");
    }
}