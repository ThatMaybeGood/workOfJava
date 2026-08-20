# ETL 前端 UI/UX 改进进度文档

> 创建时间：2026-08-19
> 目标：解决流水线任务列表、来源库、数据源、调度历史、向导选择等界面的布局、分页、分类、单步调试问题。
> 更新：2026-08-20 全部需求已完成并通过浏览器端到端验证，另修复 3 处阻断性缺陷；阶段四 3 个加分项（下次执行时间/复制错误/全局执行参数）亦已完成。

## 需求完成状态

| # | 需求 | 状态 | 验证 |
|---|------|------|------|
| 1 | 全局布局横向拉满，移除 `max-width: 1080px`，优化折叠交互 | ✅ 完成 | 浏览器 1280 宽度正常 |
| 2 | 任务列表：左侧树状分类 + 右侧分页 | ✅ 完成 | `#/tasks` 树（全部/抽取类型/目标源/来源）+ 表格 + 分页 |
| 3 | 来源库、数据源列表分页 + 搜索 + 类型/用途过滤 | ✅ 完成 | `#/sources`、`#/datasources` 分页与过滤正常 |
| 4 | 调度历史增加任务筛选下拉框 | ✅ 完成 | `#/history` 任务下拉 + 分页 + 步骤日志 |
| 5 | 向导第一步来源选择改为可搜索、可分页下拉组件 | ✅ 完成 | `#/wizard` 搜索 + 分页 + 键盘选择 |
| 6 | 向导三步（来源→转换→映射）步骤说明 + 单步调试（仅调试不保存） | ✅ 完成 | 三步 Debug 按钮均可预览，创建后未落库调试数据 |
| 7 | 任务列表横排表格（9 列）+ 任务步骤单步调试 + 编辑入口 | ✅ 完成 | `#/tasks` 横排表格 + 步骤弹窗三步调试 + 编辑跳转向导 |
| 8 | 定时 cron 转为中文可读（每5分钟 / 每天 08:30） | ✅ 完成 | 任务列表「定时」列 + 向导 cron 实时中文预览 |
| 9 | 向导二/三步清晰化：明确「来源库=抽取来源」「数据源=目的写入」 | ✅ 完成 | 步骤条改名 + 第三步流程横幅 + 目标数据源标注（目的写入） |
| 10 | 日志保留参数独立为「系统设置」树状菜单页（可扩展） | ✅ 完成 | `#/settings` 树状菜单：日志保留策略 / 全局执行参数（预留）/ 关于 |
| 11 | 任务列表左侧树菜单 CSS 化优化（去除内联样式） | ✅ 完成 | `#/tasks` 树分组图标 + 悬停/选中态 + 折叠 chevron |
| 12 | 调度历史全宽平铺 + 分等级展示：简洁行 + 展开步骤明细 + 失败定位 | ✅ 完成 | `#/history` 每行成功/失败徽标 + 点击展开抽取/转换/写入分步 |
| 13 | 任务列表「定时」列显示下次执行时间（前端 cron 本地推算） | ✅ 完成 | `nextCronRun` 修复 3 个 bug（日扫描未重置时分秒 / `N/step` 只展开单值 / 名称数组端点）后，`.tt-nextrun` 渲染「下次 MM-dd HH:mm」 |
| 14 | 历史失败行错误原因条加「复制错误」按钮 | ✅ 完成 | `.etl-copy-err` 按钮 + `ScheduleApp.copyError`（clipboard + textarea 兜底），点击 toast「错误信息已复制」 |
| 15 | 系统设置 → 全局执行参数（存储 + 面板 + 接线引擎） | ✅ 完成 | `sys_config` 表 + `GET/PUT /api/etl/global/config` + settings-app 表单；`defaultBatchSize`/`defaultMaxRows` 接入引擎 fallback，`maxRetryCount`/`timeoutSeconds` 存库待接线 |

## 模块分工

| 模块 | 关键文件 | 状态 |
|------|----------|------|
| 后端接口扩展 | `EtlMetaDao.java`, `EtlTaskController.java`, `EtlSourceController.java`, `EtlDatasourceController.java`, `EtlLogController.java`, `EtlEngine.java`, `EtlTransformer.java` | ✅ 完成 |
| 全局布局与公共组件 | `reports-web/etl/css/etl.css`, `reports-web/etl/js/components.js` | ✅ 完成 |
| 任务列表树状分类 + 分页 + 历史筛选 | `reports-web/etl/js/task-app.js`, `reports-web/etl/js/schedule-app.js` | ✅ 完成 |
| 来源库与数据源分页 | `reports-web/etl/js/source-app.js`, `reports-web/etl/js/datasource-app.js` | ✅ 完成 |
| 向导来源下拉搜索 + 三步单步调试 | `reports-web/etl/js/wizard-app.js` | ✅ 完成 |
| 定时中文转换（fmtCron） | `reports-web/etl/js/components.js` | ✅ 完成 |
| 系统设置树状菜单页（日志保留迁移） | `reports-web/etl/js/settings-app.js`（新建） | ✅ 完成 |
| 调度历史全宽分层可展开 | `reports-web/etl/js/schedule-app.js`（重构） | ✅ 完成 |
| 任务树菜单 CSS 化 + 向导横幅/设置/历史样式 | `reports-web/etl/css/etl.css` | ✅ 完成 |
| 路由接入（#/settings）+ 缓存版本 | `reports-web/etl/js/main.js`, `reports-web/etl/index.html` | ✅ 完成 |
| 集成验证 | 启动后端 + 预览代理 + 浏览器端到端 | ✅ 完成 |

## 后端接口约定

### 分页通用返回

```json
{
  "records": [],
  "total": 100,
  "page": 1,
  "size": 10
}
```

### 分页/筛选接口

- `GET /api/etl/task/list?page=1&size=10&keyword=xxx` — 任务分页 + 名称搜索
- `GET /api/etl/source/list?page=1&size=10&keyword=xxx&type=PROC` — 来源分页 + 搜索 + 类型过滤
- `GET /api/etl/datasource/list?page=1&size=10&keyword=xxx&role=SOURCE` — 数据源分页 + 搜索 + 用途过滤
- `GET /api/etl/log/history?taskId=&page=1&size=10` — 历史日志分页
- `GET /api/etl/task/simple-list` — 任务简易下拉列表（id/name）供历史筛选使用

### 单步调试接口（仅调试，不保存）

- `POST /api/etl/task/{id}/debug-extract` — 抽取预览
- `POST /api/etl/task/{id}/debug-transform` — 转换预览（抽取后传入 rows）
- `POST /api/etl/task/{id}/debug-load-preview` — 映射后目标数据预览（不落库）
- `POST /api/etl/source/{id}/preview-debug` — 来源直接调试

### 全局执行参数接口（sys_config，阶段四）

- `GET /api/etl/global/config` — 全量配置 `Map<config_key, config_value>`
- `PUT /api/etl/global/config` — body `Map<String,Object>` 逐条 `MERGE INTO sys_config` upsert
- 默认 key：`defaultBatchSize=100`（已接入引擎：任务 `batch_size` 为空/≤0 时回退）、`defaultMaxRows=10000`（已接入抽取器：来源 `max_rows` 为空时回退）、`maxRetryCount=0`、`timeoutSeconds=0`（后两个存库待后续接线）

## 集成验证期间修复的阻断性缺陷

| 缺陷 | 根因 | 修复 |
|------|------|------|
| `http://localhost:18089/etl/` 404 | `file:` 静态资源处理器对目录请求不回退 index.html | 新增 `EtlIndexController` 显式 forward 到 `/etl/index.html` |
| 向导创建流水线报 Jackson 反序列化失败 | `EtlTask.enabled/incremental` 为 `Integer`，前端发布尔值 | `wizard-app.js` `onCreate` 中 `enabled ? 1 : 0` |
| 向导创建任务主键冲突（PRIMARY KEY ON ETL_TASK） | 种子数据显式插入 `id=1,2,3` 不推进 H2 `AUTO_INCREMENT`，新任务自增 id 与之冲突 | `EtlPostInitSeed` 启动时按 `MAX(id)+1` 重启各表自增序列 |

## 端到端验证结果（2026-08-20）

- 后端 18089 启动正常，种子数据幂等初始化。
- 5 个页面（向导/任务/来源/数据源/历史）均正常渲染。
- 向导全流程：选来源 → 转换 → 选目标表 → 自动映射 → 创建 → 跳转历史，成功创建任务（含 sourceId、映射）。
- 新建任务手动执行：`extracted_rows=50, written_rows=50, status=SUCCESS`。
- 三个单步调试（抽取 20 行 / 转换预览 / 目标表预览）均正常。

### 第二批验证结果（cron / 向导 / 设置 / 历史）

- `fmtCron` 用例：`0 0/5 * * * ?`→每5分钟、`0 0 * * * ?`→每小时、`0 30 8 * * ?`→每天 08:30、`0 30 8 * * MON-FRI`→工作日 08:30、`0 0 0/2 * * ?`→每2小时、`0 * * * * ?`→每分钟。
- 任务列表左侧树 CSS 化后正常分组/折叠/选中；「定时」列中文频次正常（悬停显示原始 cron）。
- 向导编辑模式第三步：流程横幅（来源库→转换→数据源）、数据源字段标注（目的写入）正常；cron 输入实时预览「每5分钟」。
- `#/settings` 系统设置：树状菜单切换正常；日志保留策略可保存（45 保存→30 恢复），数据来自 `GET/PUT /log/config`。
- `#/history` 执行历史：全宽简洁行（成功/手动/任务名/时间/读100写100/耗时）→ 点击展开 抽取/转换/写入 分步（各含行数+耗时）；失败路径渲染（失败原因条 + 失败步骤红色高亮 + 报错 detail）经合成数据验证。
- 六个路由（向导/任务/来源/数据源/历史/设置）无 console 报错。

## 变更记录

- 2026-08-19：创建进度文档，启动 5 个子 agent 并行修改。
- 2026-08-20：全部需求完成；集成验证；修复 `/etl/` 404、布尔/整型反序列化、H2 自增序列主键冲突 3 处缺陷。
- 2026-08-20：任务列表横排改造 + 任务步骤单步调试 + 编辑入口。
  - `etl.css`：新增 `.etl-table` 横排表格（`width:100%`、表头吸底、悬停、`code` 样式）与 `.etl-step-card` 步骤卡片样式。
  - `task-app.js`：任务列表改为 9 列横排（任务名称/来源/目标表/抽取类型/定时/写入/最近执行/状态/操作）；新增「步骤」弹窗，展示 抽取来源→转换提取→映射匹配 三步并可逐步骤单步调试（抽取/转换/写入预览均走后端，不落库）；新增「编辑」按钮跳转向导编辑模式。
  - `wizard-app.js`：新增编辑模式（`#/wizard?edit=任务ID`）——加载任务详情回显来源/目标表/映射/任务配置，保存走 `PUT /task/{id}` + `mapping/batch`；目标表与目标列选择改为大小写不敏感匹配（种子库小写表名/列名可正确回显）。
  - `index.html`：脚本/样式加 `?v=` 版本号防浏览器缓存旧 JS。
- 2026-08-20（第二批）：cron 中文展示 + 向导清晰化 + 系统设置页 + 历史分层展示。
  - `components.js`：新增 `fmtCron(cron)` 将 Quartz cron 转为中文（`0 0/5 * * * ?`→每5分钟、`0 30 8 * * MON-FRI`→工作日 08:30、`0 0 0/2 * * ?`→每2小时）。
  - `task-app.js`：任务列表「定时」列展示中文频次（悬停显示原始 cron）；左侧分类树去除内联样式，改用 `.tt-*` CSS 类（分组图标、悬停/选中态、折叠 chevron）。
  - `wizard-app.js`：步骤名改为「抽取来源 / 转换提取 / 映射写入」；步骤说明明确「来源库=数据从哪里抽取、数据源=目的数据写入的库」；第三步新增流程横幅（来源库→转换→数据源）+ 数据源字段标注「（目的写入）」+ cron 实时中文预览。
  - `settings-app.js`（新建）：系统设置树状菜单页，左侧分组树（日志管理→日志保留策略、任务管理→全局执行参数预留、系统→关于），右侧按菜单渲染表单；日志保留策略从「调度历史」迁移至此，`GET/PUT /log/config`。
  - `schedule-app.js`：移除日志保留卡片；执行历史改为全宽平铺——每条日志为一行（成功/失败徽标 + 触发方式 + 任务名 + 时间范围 + 读写行数 + 耗时），点击行展开「分层明细」：失败原因条 + 逐步日志（抽取/转换/写入，失败步骤红色高亮并展示报错 detail）。
  - `main.js` / `index.html`：新增 `#/settings` 路由 + 导航项「系统设置」，接入 `settings-app.js`，缓存版本升至 `20260820c`。
  - `etl.css`：新增任务树 `.tt-*`、向导横幅 `.etl-flow-banner`、cron 预览 `.wiz-cron-preview`、系统设置 `.etl-settings-*`、历史分层 `.etl-log-*` 样式。
- 2026-08-20（阶段四，3 个加分项收尾）：
  - `components.js`：修复 `nextCronRun` 3 个 bug——①日扫描未重置时分秒：`findTimeOnDay` 新增 `fromNow` 参数，首日从当前时刻起（严格晚于当前）、后续天从当天 00:00 起，修复「下午求值 `0 30 8 * * ?` 400 天内返回 null」；②`N/step` 只展开单值：step 分支 else 改 `lo = toNum(rp); hi = max;`，修复 `0 0/5 * * * ?` 只在整点匹配；③名称数组端点：新增 `toScalar` helper，修复 `MON-FRI` 因 `DOW_NAMES` 映射为数组只生成 `{1}`。新增 `nextCronRun(cron)` 前端推算下次执行时间，任务列表 `.tt-nextrun` 渲染「下次 MM-dd HH:mm」。
  - `schedule-app.js`：历史失败行错误原因条新增「复制错误」按钮（`ScheduleApp.copyError`，`navigator.clipboard` + textarea 兜底）。
  - `etl.css`：新增 `.etl-log-errbar .etl-copy-err`（右对齐 + `flex-shrink:0`）。
  - 后端：新建 `sys_config` 表（幂等 DDL + 4 个默认 key，`etl_meta_init.sql`）+ `EtlGlobalConfigController`（`GET/PUT /api/etl/global/config`）+ `EtlMetaDao` 三个方法（`getGlobalConfigs`/`getGlobalInt`/`saveGlobalConfigs`）。
  - 接线引擎：`EtlEngine` 抽取批大小回退改 `metaDao.getGlobalInt("defaultBatchSize", 100)`（任务 `batch_size` 为 null 或 ≤0 时生效，`mapTask` 会把 SQL NULL 读成 0 故判 `> 0`）；`WebServiceExtractor`/`ProcedureExtractor` 最大行数回退改 `metaDao.getGlobalInt("defaultMaxRows", 10000)`。
  - `settings-app.js`：`paintTaskGlobal` 从占位改为表单——四个数字输入（批大小/最大行数/最大重试/超时）+ 保存按钮 + `loadGlobalConfig`/`saveGlobalConfig`（回填、正整数校验、toast）。
  - `index.html`：缓存版本 `20260820c` → `20260820d`。
