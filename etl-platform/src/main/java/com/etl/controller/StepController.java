package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.EtlPipelineStep;
import com.etl.entity.EtlStepColumnMapping;
import com.etl.service.admin.EdgeService;
import com.etl.service.admin.StepColumnMappingService;
import com.etl.service.admin.StepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/etl/step")
@Tag(name = "ETL步骤管理", description = "管线步骤的独立 CRUD")
public class StepController {

    @Autowired
    private StepService stepService;

    @Autowired
    private StepColumnMappingService stepColumnMappingService;

    @Autowired
    private EdgeService edgeService;

    @PostMapping
    @Operation(summary = "新增步骤")
    public ApiResponse<EtlPipelineStep> add(@RequestBody EtlPipelineStep step) {
        stepService.save(step);
        return ApiResponse.success(step, "步骤创建成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新步骤")
    public ApiResponse<EtlPipelineStep> update(@PathVariable Long id, @RequestBody EtlPipelineStep step) {
        step.setId(id);
        stepService.updateById(step);
        return ApiResponse.success(step, "步骤更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除步骤")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        EtlPipelineStep step = stepService.getById(id);
        if (step == null) {
            return ApiResponse.error("步骤不存在");
        }
        edgeService.deleteByStepId(id);
        stepColumnMappingService.deleteByStepId(id);
        stepService.removeById(id);
        return ApiResponse.success("步骤删除成功");
    }

    @GetMapping("/pipeline/{pipelineId}")
    @Operation(summary = "按管线查询步骤列表")
    public ApiResponse<List<EtlPipelineStep>> listByPipeline(@PathVariable Long pipelineId) {
        return ApiResponse.success(stepService.listByPipelineId(pipelineId));
    }

    @GetMapping("/{id}/mappings")
    @Operation(summary = "获取步骤的字段映射")
    public ApiResponse<List<EtlStepColumnMapping>> getMappings(@PathVariable Long id) {
        return ApiResponse.success(stepColumnMappingService.listByStepId(id));
    }

    @PostMapping("/{stepId}/mapping")
    @Operation(summary = "为步骤添加字段映射")
    public ApiResponse<EtlStepColumnMapping> addMapping(@PathVariable Long stepId,
                                                         @RequestBody EtlStepColumnMapping mapping) {
        mapping.setStepId(stepId);
        stepColumnMappingService.save(mapping);
        return ApiResponse.success(mapping, "映射创建成功");
    }

    @PutMapping("/{stepId}/mapping/{mappingId}")
    @Operation(summary = "更新字段映射")
    public ApiResponse<EtlStepColumnMapping> updateMapping(@PathVariable Long stepId,
                                                            @PathVariable Long mappingId,
                                                            @RequestBody EtlStepColumnMapping mapping) {
        mapping.setId(mappingId);
        mapping.setStepId(stepId);
        stepColumnMappingService.updateById(mapping);
        return ApiResponse.success(mapping, "映射更新成功");
    }

    @DeleteMapping("/{stepId}/mapping/{mappingId}")
    @Operation(summary = "删除字段映射")
    public ApiResponse<Void> deleteMapping(@PathVariable Long stepId, @PathVariable Long mappingId) {
        stepColumnMappingService.removeById(mappingId);
        return ApiResponse.success("映射删除成功");
    }
}
