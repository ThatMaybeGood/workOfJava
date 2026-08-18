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
| 联调验证 | 端到端浏览器走查 + 存量数据回归（任务 id=1） | 主线程 | 🟡 进行中 |

## 关键约束（沿用原计划）

- Spring Boot 2.7.18 + Java 8（禁 Map.of / List.of / var / text block）
- 不动 com.reports 主代码与 reports-web 主界面
- 元数据独立 H2 ./data/etl_meta，迁移幂等
- 存量兼容：数据源 id=33、任务 id=1
- 端口 18089

## 下一步

1. 浏览器端到端走查（建来源 → 调试出参 → 结构树 → 映射 → 任务生成 → 手动触发 → 调度历史 SUCCESS）
2. 处理任务 id=1 mock URL 失效问题（独立排查）
3. 收尾文档与提交
