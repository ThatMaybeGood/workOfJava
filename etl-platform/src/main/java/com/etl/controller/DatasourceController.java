package com.etl.controller;

import com.etl.dto.ApiResponse;
import com.etl.entity.DatasourceConfig;
import com.etl.service.admin.DatasourceConfigService;
import com.etl.service.core.DataSourceManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Slf4j
@RestController
@RequestMapping("/api/etl/datasource")
@Tag(name = "数据源管理", description = "ETL数据源配置管理")
public class DatasourceController {

    @Autowired
    private DatasourceConfigService datasourceConfigService;

    @Autowired
    private DataSourceManager dataSourceManager;

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
        boolean updated = datasourceConfigService.updateById(config);
        if (!updated) {
            return ApiResponse.error("数据源不存在: " + id);
        }
        return ApiResponse.success(config, "数据源更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据源")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boolean removed = datasourceConfigService.removeById(id);
        if (!removed) {
            return ApiResponse.error("数据源不存在: " + id);
        }
        return ApiResponse.success("数据源删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据源详情")
    public ApiResponse<DatasourceConfig> getById(@PathVariable Long id) {
        DatasourceConfig config = datasourceConfigService.getById(id);
        if (config == null) {
            return ApiResponse.error("数据源不存在: " + id);
        }
        return ApiResponse.success(config);
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

    @GetMapping("/role/{role}")
    @Operation(summary = "按用途角色获取启用的数据源（SOURCE / TARGET / BOTH）")
    public ApiResponse<List<DatasourceConfig>> listByRole(@PathVariable String role) {
        return ApiResponse.success(datasourceConfigService.listEnabledByRole(role));
    }

    @GetMapping("/{id}/test")
    @Operation(summary = "测试数据源连接")
    public ApiResponse<Boolean> testConnection(@PathVariable Long id) {
        boolean result = datasourceConfigService.testConnection(id);
        return ApiResponse.success(result, result ? "连接成功" : "连接失败");
    }

    @GetMapping("/{dsName}/tables")
    @Operation(summary = "获取 JDBC 数据源下的所有表/视图")
    public ApiResponse<List<String>> listTables(@PathVariable String dsName) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSourceManager.getDataSource(dsName).getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE", "VIEW"})) {
                TreeSet<String> names = new TreeSet<>();
                while (rs.next()) {
                    // 过滤数据库系统 schema（H2 的 INFORMATION_SCHEMA、Oracle 的 SYS 等）
                    String schemaName = rs.getString("TABLE_SCHEM");
                    if (schemaName != null && isSystemSchema(schemaName)) continue;
                    String name = rs.getString("TABLE_NAME");
                    if (name != null) names.add(name);
                }
                tables.addAll(names);
            }
        } catch (Exception e) {
            log.error("获取数据源 [{}] 表列表失败", dsName, e);
            return ApiResponse.error("获取表列表失败: " + e.getMessage());
        }
        return ApiResponse.success(tables);
    }

    @GetMapping("/{dsName}/table/{tableName}/columns")
    @Operation(summary = "获取指定表的字段列表")
    public ApiResponse<List<Map<String, Object>>> listColumns(@PathVariable String dsName,
                                                               @PathVariable String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();
        String schema = null;
        String table = tableName;
        if (tableName.contains(".")) {
            int idx = tableName.indexOf('.');
            schema = tableName.substring(0, idx);
            table = tableName.substring(idx + 1);
        }
        try (Connection conn = dataSourceManager.getDataSource(dsName).getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            columns = queryColumns(meta, conn.getCatalog(), schema, table);
            // 大小写回退：H2/Oracle 未加引号标识符存储为大写，用户可能输入小写
            if (columns.isEmpty() && !table.equals(table.toUpperCase())) {
                columns = queryColumns(meta, conn.getCatalog(), schema, table.toUpperCase());
            }
            if (columns.isEmpty() && !table.equals(table.toLowerCase())) {
                columns = queryColumns(meta, conn.getCatalog(), schema, table.toLowerCase());
            }
        } catch (Exception e) {
            log.error("获取数据源 [{}] 表 [{}] 字段失败", dsName, tableName, e);
            return ApiResponse.error("获取表字段失败: " + e.getMessage());
        }
        return ApiResponse.success(columns);
    }

    /** 是否数据库系统 schema（H2/Oracle 等元数据 schema），列出表时过滤掉 */
    private boolean isSystemSchema(String schema) {
        String s = schema.toUpperCase();
        return s.startsWith("INFORMATION_SCHEMA")
                || "SYSTEM".equals(s) || "SYS".equals(s) || "PG_CATALOG".equals(s)
                || "MYSQL".equals(s) || "SYSTEM_SCHEMA".equals(s);
    }

    private List<Map<String, Object>> queryColumns(DatabaseMetaData meta, String catalog,
                                                   String schema, String table) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        try (ResultSet rs = meta.getColumns(catalog, schema, table, "%")) {
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("columnName", rs.getString("COLUMN_NAME"));
                col.put("dataTypeName", rs.getString("TYPE_NAME"));
                col.put("dataType", rs.getInt("DATA_TYPE"));
                col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.put("size", rs.getInt("COLUMN_SIZE"));
                result.add(col);
            }
        }
        return result;
    }
}
