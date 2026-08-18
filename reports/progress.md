# ETL 向导式流水线重构 - 进度记录

## 项目

reports 项目 ETL 模块前端从五个平级 CRUD tab 重构为三步向导式流水线（来源 → 转换 → 映射），后端新增可复用来源实体（etl_source 表）。

## 完成状态

- 阶段 0 契约冻结：✅ 完成
- 阶段 1 后端来源实体：✅ 完成
- 阶段 2 设计系统 + 公共层：✅ 完成
- 阶段 3 向导三步页：✅ 完成
- 阶段 4 辅助页：✅ 完成
- 联调验证：✅ 完成
- 数据修复与界面优化：✅ 完成（2026-08-19）

## 本次更新内容（2026-08-19）

### 界面修复
1. **来源库调试面板**：从行内展开改为独立全屏 overlay，宽度铺满、高度撑到视窗底部，表头 sticky 固定
2. **新建数据源弹窗**：宽度从 1060px 增至 1400px，充分利用大屏空间
3. **向导第一步来源选择**：改为长条列表；单个来源自动选中并启用「下一步」；多个来源时可点击选择
4. **调度历史页面**：移除「流水线任务」表格区块，只保留执行历史时间线 + 日志配置

### 测试数据
- 新增 `SeedData.java`：一键创建 SOURCE H2 测试库（50员工+30订单）、TARGET H2 库、写入完整 ETL 元数据
- 2 数据源（SOURCE/TARGET H2）、2 来源（PROC+WS）、3 任务、11 映射、6 执行日志、10 步骤日志

### 文档
- 新增 `docs/etl-module.md`：完整 API 文档 + 目录结构 + 设计系统说明

## 已完成清单

### 后端（src/main/java/com/reports/etl/）

- `entity/EtlSource.java`：来源实体（id/name/type/sourceDsId/configJson/createTime/updateTime）
- `controller/EtlSourceController.java`：来源 CRUD + preview-debug + structure
- `service/extractor/SourceExtractorFacade.java`：来源抽取门面，委托 WebServiceExtractor / ProcedureExtractor
- `service/core/EtlStructureService.java`：JSON/XML 层级树构建（数组合并单层 + sampleCount）
- `config/EtlWebStaticConfig.java`：暴露 reports-web 静态资源
- `SeedData.java`：测试种子数据初始化

### 前端（reports-web/etl/）

- `css/etl.css`：设计系统变量 + 组件类（步骤条/来源卡片/层级树/映射行/调试面板/时间线/fullscreen debug overlay）
- `js/store.js`：向导跨步状态（sessionStorage）
- `js/components.js`：步骤条 / 结构树 / 调试面板渲染函数
- `js/wizard-app.js`：三步向导（来源选择→结构+转换→映射+任务设置）
- `js/source-app.js`：来源库列表/编辑/全屏调试面板
- `js/datasource-app.js`：数据源管理（列表/新建弹窗/表列浏览）
- `js/schedule-app.js`：调度历史（执行时间线/日志配置，已移除任务管理）

## 已修复的关键 Bug

1. **API 协议 unwrap**：后端统一返回 `{result, body}` 包装，前端 api.js 增加解包逻辑。
2. **structure 路由冲突**：调整为 `/api/etl/source/structure/{id}`。
3. **preview-debug 兼容 configJson 包装**。
4. **EtlWebStaticConfig 静态资源暴露**。
5. **task add 返回 taskId**。
6. **REST 抽取误用 POST**：REST 且无请求体模板时改发 GET。
7. **vendor 静态资源 404**：增加 `/vendor/**` → `file:reports-web/vendor/`。
8. **mapping/batch 的 isUpdateCol 类型**：改为 0/1。
9. **任务 id=1 迁移遗留**：URL 改为 jsonplaceholder，手动触发 SUCCESS。
10. **来源调试面板宽度受限**：改为全屏 overlay。
11. **新建数据源弹窗过窄**：max-width 从 1060px 增至 1400px。
12. **向导第一步按钮始终灰色**：单来源自动选中，多来源列表可选。
13. **调度历史冗余任务管理**：移除流水线任务表格。

## 端到端验证结果（2026-08-19）

- 数据来源源管理：数据源列表 ✅、连接测试 ✅、表/列浏览 ✅
- 来源库：来源列表 ✅、调试全屏面板 ✅、结构树 ✅
- 向导第一步：单来源自动选中+按钮可用 ✅
- 调度历史：执行历史时间线 ✅、步骤展开 ✅、日志配置 ✅

## 验证入口

http://localhost:18089/etl/index.html#/wizard
