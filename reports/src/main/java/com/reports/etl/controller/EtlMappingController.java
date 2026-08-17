package com.reports.etl.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reports.dto.common.ApiResponse;
import com.reports.etl.entity.EtlMapping;
import com.reports.etl.mapper.EtlMappingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/etl/mapping")
public class EtlMappingController {

    private final EtlMappingMapper mappingMapper;

    public EtlMappingController(EtlMappingMapper mappingMapper) {
        this.mappingMapper = mappingMapper;
    }

    @GetMapping("/list/{taskId}")
    public ApiResponse<List<EtlMapping>> list(@PathVariable Long taskId) {
        List<EtlMapping> mappings = mappingMapper.selectList(
                new LambdaQueryWrapper<EtlMapping>()
                        .eq(EtlMapping::getTaskId, taskId)
                        .orderByAsc(EtlMapping::getSortOrder)
        );
        return ApiResponse.success(mappings);
    }

    @PostMapping
    public ApiResponse<String> add(@RequestBody EtlMapping mapping) {
        mappingMapper.insert(mapping);
        return ApiResponse.success("映射添加成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<String> update(@PathVariable Long id, @RequestBody EtlMapping mapping) {
        mapping.setId(id);
        mappingMapper.updateById(mapping);
        return ApiResponse.success("映射更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        mappingMapper.deleteById(id);
        return ApiResponse.success("映射删除成功");
    }

    @DeleteMapping("/task/{taskId}")
    public ApiResponse<String> deleteByTask(@PathVariable Long taskId) {
        mappingMapper.delete(new LambdaQueryWrapper<EtlMapping>().eq(EtlMapping::getTaskId, taskId));
        return ApiResponse.success("映射清空成功");
    }
}