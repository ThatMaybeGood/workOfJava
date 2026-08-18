# ETL 向导式流水线重构 - 任务计划

原计划文件：`C:\Users\86156\.claude\plans\reports-radiant-flask.md`

## 阶段概览与当前状态

| 阶段 | 内容 | 负责 | 状态 |
|------|------|------|------|
| 阶段 0 | 契约冻结（API + CSS 变量） | 主线程 | ✅ 完成 |
| 阶段 1 | 后端来源实体（EtlSource / Controller / Facade / StructureService） | Agent A | ✅ 完成 |
| 阶段 2 | 设计系统 + 公共层（etl.css / api.js / store.js / components.js / index.html） | Agent B | ✅ 完成 |
| 阶段 3 | 向导三步页（wizard-app.js：来源 → 转换 → 映射） | Agent C | ✅ 完成 |
| 阶段 4 | 辅助页（source-app / datasource-app / schedule-app） | Agent D | ✅ 完成 |
| 联调验证 | 端到端浏览器走查 + 存量数据回归（任务 id=1） | 主线程 | ✅ 完成 |
| 界面优化 | 调试全屏面板 / 弹窗加宽 / 向导单源自动选中 / 调度历史精简 | 主线程 | ✅ 完成 |
| 测试数据 | SeedData 种子脚本 + 文档 | 主线程 | ✅ 完成 |

## 关键约束（沿用原计划）

- Spring Boot 2.7.18 + Java 8（禁 Map.of / List.of / var / text block）
- 不动 com.reports 主代码与 reports-web 主界面
- 元数据独立 H2 ./data/etl_meta，迁移幂等
- 存量兼容：数据源 id=33、任务 id=1
- 端口 18089

## 已交付

- 完整 ETL 向导式流水线（来源→转换→映射）
- 来源库管理（可命名复用、全屏调试）
- 数据源管理（连接测试、表/列浏览）
- 调度历史（执行时间线、步骤日志、日志保留策略）
- SeedData 测试数据初始化
- docs/etl-module.md 模块文档

## 遗留事项

无
