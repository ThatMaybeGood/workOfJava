# ETL 数据集成模块

基于向导式流水线的 ETL 数据抽取与同步系统，支持存储过程（PROC）和 WebService（WS）两种来源类型。

---

## 目录结构

```
reports/
├── src/main/java/com/reports/etl/
│   ├── config/
│   │   ├── EtlMetaDataSourceConfig.java    # 独立 H2 元数据库配置
│   │   └── EtlWebStaticConfig.java         # 前端静态资源暴露
│   ├── controller/
│   │   ├── EtlDatasourceController.java    # 数据源 CRUD + 连接测试 + 表/列浏览
│   │   ├── EtlSourceController.java        # 来源管理（新建/编辑/调试/结构分析）
│   │   ├── EtlTaskController.java          # 任务 CRUD + 手动触发
│   │   ├── EtlMappingController.java       # 字段映射批量写入
│   │   ├── EtlDebugController.java         # 分步调试（抽取→转换→写入）
│   │   └── EtlLogController.java           # 执行历史 + 步骤日志 + 保留策略
│   ├── entity/
│   │   ├── EtlDatasource.java              # 数据源实体
│   │   ├── EtlSource.java                  # 抽取来源（可被多个任务复用）
│   │   ├── EtlTask.java                    # 流水线任务
│   │   ├── EtlMapping.java                 # 字段映射
│   │   ├── EtlTaskLog.java                 # 执行日志
│   │   ├── EtlStepLog.java                 # 步骤日志
│   │   └── EtlWsConfig.java / EtlProcConfig.java  # WS/PROC 配置（旧子表，兼容迁移）
│   ├── service/
│   │   ├── core/
│   │   │   ├── EtlMetaDao.java             # 元数据 DAO（JdbcTemplate）
│   │   │   ├── EtlStructureService.java    # 来源结构树构建
│   │   │   └── EtlEngine.java              # 完整链路引擎
│   │   ├── extractor/
│   │   │   ├── SourceExtractorFacade.java  # 来源抽取门面
│   │   │   ├── WebServiceExtractor.java    # REST/SOAP 抽取
│   │   │   └── ProcedureExtractor.java     # 存储过程抽取
│   │   ├── registry/EtlDataSourceRegistry.java  # 数据源连接池管理
│   │   ├── transformer/EtlTransformer.java      # 转换映射
│   │   ├── writer/EtlWriter.java                # 写入引擎
│   │   └── scheduler/EtlScheduler.java          # Quartz 定时调度
│   ├── util/
│   │   ├── DsEncryptUtil.java               # 密码 AES 加密
│   │   └── DialectResolver.java             # JDBC 方言解析
│   └── SeedData.java                        # 测试种子数据初始化
│
└── reports-web/etl/
    ├── index.html                           # ETL 页面入口（hash 路由）
    ├── css/etl.css                          # 设计系统（蓝图仪器风）
    └── js/
        ├── api.js                           # HTTP 客户端封装
        ├── store.js                         # 向导跨步状态（sessionStorage）
        ├── components.js                    # 公共组件（步骤条/树/调试面板）
        ├── wizard-app.js                    # 三步向导（选择来源→转换提取→映射匹配）
        ├── source-app.js                    # 来源库管理（列表/新建/调试全屏面板）
        ├── datasource-app.js                # 数据源管理（列表/新建弹窗/表列浏览）
        ├── schedule-app.js                  # 调度历史（执行时间线/日志配置）
        └── task-app.js                      # 流水线任务管理（备用页）
```

---

## API 接口

### 数据源 `/api/etl/datasource`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 数据源列表 |
| GET | `/{id}` | 数据源详情 |
| POST | `/` | 新建数据源 |
| PUT | `/{id}` | 更新数据源 |
| DELETE | `/{id}` | 删除数据源 |
| POST | `/{id}/test` | 测试连接 |
| GET | `/{id}/tables` | 获取表列表 |
| GET | `/{id}/columns/{tableName}` | 获取列信息 |

### 来源 `/api/etl/source`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 来源列表 |
| GET | `/{id}` | 来源详情 |
| POST | `/` | 新建来源 |
| PUT | `/{id}` | 更新来源 |
| DELETE | `/{id}` | 删除来源（检查引用） |
| POST | `/preview-debug` | 预览调试（不落库） |
| GET | `/structure/{id}` | 来源结构树分析 |

### 任务 `/api/etl/task`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 任务列表 |
| GET | `/{id}` | 任务详情 |
| GET | `/{id}/detail` | 任务详情（含来源+映射） |
| POST | `/` | 创建任务 |
| PUT | `/{id}` | 更新任务 |
| DELETE | `/{id}` | 删除任务 |
| POST | `/{id}/run` | 手动触发执行 |
| GET | `/{id}/logs` | 执行日志 |

### 映射 `/api/etl/mapping`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/batch` | 批量写入映射 |

### 调试 `/api/etl/debug`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/extract` | 单独跑抽取环节 |
| POST | `/transform` | 单独跑转换环节 |
| POST | `/load` | 单独跑写入环节 |
| POST | `/run-all` | 全链路调试 |

### 日志 `/api/etl/log`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/config` | 日志保留策略 |
| PUT | `/config` | 保存日志保留策略 |
| GET | `/history` | 执行历史（最近 N 条） |
| GET | `/steps/{logId}` | 指定日志的步骤详情 |

---

## 前端路由

| 路由 | 页面 | 功能 |
|------|------|------|
| `#/wizard` | 新建流水线向导 | 三步向导：选择来源 → 转换提取 → 映射匹配 |
| `#/sources` | 来源库 | 来源列表、新建/编辑/调试 |
| `#/datasources` | 数据源 | 数据源列表、新建/编辑/表列浏览 |
| `#/tasks` | 流水线任务 | 任务列表（备用页，主要用向导创建） |
| `#/history` | 调度历史 | 执行历史时间线 + 日志保留配置 |

---

## 设计系统

CSS 变量定义在 `etl.css` 的 `:root`，主题色为深青墨 `#0C6875`，整体风格为蓝图仪器风：

- **主色**：`--etl-color-primary: #0C6875`
- **背景**：`--etl-color-bg: #F4F6F8`
- **表面**：`--etl-color-surface: #FFFFFF`
- **字体**：JetBrains Mono（代码/数值）+ 系统无衬线（正文）

关键组件类：
- `.etl-card` — 卡片容器
- `.etl-source-row` — 来源行（长条列表，带左侧色条区分类型）
- `.etl-source-card` — 来源卡片（向导第一步选择用）
- `.etl-stepper` — 向导步骤条
- `.etl-debug-panel` — 深色调试面板
- `.etl-timeline` — 执行历史时间线
- `.etl-modal` / `.etl-modal-overlay` — 弹窗
- `.etl-debug-overlay` — 全屏调试面板（来源调试用）

---

## 数据模型

### 数据库

- **元数据库**：`./data/etl_meta.mv.db`（H2，独立于 Oracle 报表库）
- **测试库**：`./data/etl_source_test.mv.db`（SOURCE 测试数据）
- **测试库**：`./data/etl_target_test.mv.db`（TARGET 测试数据）

### 核心表

```sql
etl_datasource    -- 数据源（SOURCE/TARGET 角色）
etl_source        -- 抽取来源（WS/PROC 类型，可被多任务复用）
etl_task          -- 流水线任务
etl_mapping       -- 字段映射
etl_task_log      -- 执行历史
etl_step_log      -- 步骤日志（EXTRACT/TRANSFORM/LOAD）
etl_log_config    -- 日志保留策略
```

---

## 测试数据

使用 `SeedData` 初始化测试数据：

```bash
# 编译
cd reports && mvn compile -q

# 运行种子数据（自动创建 SOURCE/TARGET H2 库 + 写入元数据）
java -cp target/classes:$(mvn dependency:build-classpath -q -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout) \
  com.reports.etl.SeedData
```

生成的测试数据：
- **2 个数据源**：测试来源H2（SOURCE）、测试目标H2（TARGET）
- **2 个来源**：员工表抽取(PROC) — 连 SOURCE H2；Posts API(WS) — jsonplaceholder.typicode.com
- **3 个任务**：员工数据同步、订单数据同步、Posts API同步
- **执行历史**：6 条日志 + 10 条步骤日志（含 1 次失败记录）

---

## 快速启动

```bash
cd reports
mvn spring-boot:run

# 访问
# 主界面：http://localhost:18089
# ETL 数据集成：http://localhost:18089/etl/index.html#/wizard
```
