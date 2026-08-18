# ETL 向导式流水线重构 - 进度记录

## 项目

reports 项目 ETL 模块前端从五个平级 CRUD tab 重构为三步向导式流水线（来源 → 转换 → 映射），后端新增可复用来源实体（etl_source 表）。

## 完成状态

- 阶段 0 契约冻结：✅ 完成
- 阶段 1 后端来源实体：✅ 完成
- 阶段 2 设计系统 + 公共层：✅ 完成
- 阶段 3 向导三步页：✅ 完成
- 阶段 4 辅助页：✅ 完成
- 联调验证：进行中

## 已完成清单

### 后端（src/main/java/com/reports/etl/）

- `entity/EtlSource.java`：来源实体（id/name/type/sourceDsId/configJson/createTime/updateTime）
- `controller/EtlSourceController.java`：来源 CRUD + preview-debug + structure
- `service/extractor/SourceExtractorFacade.java`：来源抽取门面，委托 WebServiceExtractor / ProcedureExtractor
- `service/core/EtlStructureService.java`：JSON/XML 层级树构建（数组合并单层 + sampleCount）
- `config/EtlWebStaticConfig.java`：暴露 reports-web 静态资源

### 前端（reports-web/etl/）

- `css/etl.css`：设计系统变量 + 组件类（步骤条/来源卡片/层级树/映射行/调试面板/时间线）
- `js/store.js`：向导跨步状态（sessionStorage）
- `js/components.js`：步骤条 / 结构树 / 调试面板渲染函数
- `js/wizard-app.js`：三步向导（来源建/选 → 结构 + 转换 → 映射 + 任务设置）
- `js/source-app.js`：来源库列表/编辑/调试
- 已删除旧文件：`js/task-app.js`、`js/mapping-app.js`、`js/debug-app.js`

## 已修复的关键 Bug

1. **API 协议 unwrap**：后端统一返回 `{result, body}` 包装，前端 api.js 增加解包逻辑，避免各页面重复判空。
2. **structure 路由冲突**：`/api/etl/source/{id}/structure` 与 `/api/etl/source/{id}` 在某些场景产生路径匹配歧义，调整为 `/api/etl/source/structure/{id}`。
3. **preview-debug 兼容 configJson 包装**：来源配置既支持平铺字段也支持 `configJson` 字符串包装，反序列化时双兼容。
4. **EtlWebStaticConfig 静态资源暴露**：补充 ResourceHandler 注册，使 `/etl/**` 直接映射到 reports-web/etl，无需手工拷贝。
5. **task add 返回 taskId**：`POST /api/etl/task` 新增返回体中带 `taskId`，供向导第三步保存映射后立即跳转调试。
6. **REST 抽取误用 POST**：`WebServiceExtractor.fetchPageData` 对 REST 恒发 POST（空 `{}` body），查询型 GET 接口会被误创建资源（jsonplaceholder 返回假 id=11）。修复：REST 且无请求体模板时改发 GET，SOAP/带模板仍 POST。修复后 10 列 10 行正常。
7. **vendor 静态资源 404**：index.html 以 `../vendor` 引用 bootstrap/icons，但只暴露了 `/etl/**`。EtlWebStaticConfig 增加 `/vendor/**` → `file:reports-web/vendor/`。
8. **mapping/batch 的 isUpdateCol 类型**：后端为 Integer，wizard-app.js 原传 boolean 会反序列化失败，改为 0/1。
9. **任务 id=1 迁移遗留**：来源 1 URL 指向不存在的 mock 端点、responsePath 误配 `body.records`，已改为 jsonplaceholder 顶层数组；手动触发 SUCCESS（10 行）。

## 端到端 API 验证结果（2026-08-18）

- preview-debug：15 列（含嵌套拍平 address.street 等）、行数据正常 ✅
- structure/34：完整嵌套树（address 子节点、sampleCount=10）✅
- 建任务(33) → mapping/batch → 手动触发：EXTRACT/TRANSFORM/LOAD 全 SUCCESS，10 行写入 ✅（测试任务已删除）
- 任务 1 回归：SUCCESS，extractedRows=10 / writtenRows=10 ✅
- 静态资源：index/css/js/vendor 全部 200 ✅

## 遗留事项

- 浏览器端到端走查（建来源 → 调试 → 结构 → 映射 → 触发 → 调度历史）尚未完成全链路回归（Chrome 扩展未连接，可人工走查）

## 验证入口

http://localhost:18089/etl/index.html
