# 发现与决策

## 需求
- 在 reports 报表项目下新增 ETL 定时抽取功能
- 两种抽取源：WebService 接口出参、存储过程出参（游标）
- 前端：配置数据库连接、调试出参、字段映射、创建抽取任务
- 全流程：抽取 → 转换映射 → 写入/更新
- 多数据库：数据来源库 / 目标数据源库，界面可配置
- **约束**：不改动现有报表数据库配置；目前目标库 = 报表库

## 研究发现（reports 项目现状）

### 后端技术栈
| 项 | 值 |
|----|----|
| Spring Boot | 2.7.18 |
| Java | 1.8 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | Oracle（ojdbc8 21.8，orai18n） |
| 连接池 | Druid 1.2.18 |
| HTTP 客户端 | OkHttp3 4.12.0（可用于 WebService 调用） |
| XML | jackson-dataformat-xml（可用于 SOAP/XML 解析） |
| JSON | jackson-databind 2.16.0 |
| 其它 | lombok、validation、aop |

### 现有数据源配置
- 配置文件：`src/main/resources/application.yml` + `application-dev.yml`
- 主数据源 master：Oracle `jdbc:oracle:thin:@//168.168.235.105:1521/yuanqi`（powerihsp/yuanqi）——**这就是报表库，也是当前目标数据源库，不可改动**
- slave 未启用（注释掉的示例）
- 动态数据源机制已存在：
  - `config/DataSourceConfig.java`：从 `spring.datasource.master/slave` 静态创建 DruidDataSource
  - `config/DynamicDataSource.java`：继承 AbstractRoutingDataSource
  - `config/DynamicDataSourceContextHolder.java`：ThreadLocal 切换，默认 master
- 局限：现有动态数据源绑定的是**配置文件静态项**，ETL 需要**运行时注册**的多数据源（界面配置的动态连接），需独立设计

### 后端代码组织
- controller：`GatewayController`（统一网关入口）
- service：`GatewayService` + 各业务 ServiceImpl
- service/handler：`ReportHandler` 策略模式 + `ReportHandlerFactory`（按方法名路由）
- mapper：MyBatis-Plus Mapper（各报表模块）
- entity：报表实体（Revenue、NoShow、WindowStats 等）
- dto：request / response
- config：DataSource / MybatisPlus / Cors / Page 等

### 前端组织（原生 JS，无框架）
- `reports-web/`：index.html + index.css + main-app.js + api-config.js
- 模块目录：`cash/`、`outpatient/`（各含 html + css/js）
- 每个模块 js 下有 `api.js`（接口封装）+ `*-app.js`（页面逻辑）+ `mock.js`
- api-config.js 应是接口地址/基础配置

### 其它
- 项目根有历史文件：`ETL平台冒烟测试与修复报告-2026-08-13.md`、`reports-web.zip`、`src.zip`（疑似旧打包，待确认是否需要清理）
- docs/ 目录存在

## 技术决策（用户已确认）
| 决策 | 理由 |
|------|------|
| ETL 独立成新模块（后端 com.reports.etl.*，前端 reports-web/etl/） | 不侵入现有报表代码 |
| ETL 元数据存独立 H2 文件库（jdbc:h2:file:./data/etl_meta） | 用户确认；彻底隔离 Oracle 报表库 |
| WebService 同时支持 SOAP+XML 与 REST+JSON，出参层级展开 | 用户确认（前端可直接映射层级） |
| 存储过程用 Oracle SYS_REFCURSOR 出参 | 用户确认 |
| 写入模式：INSERT/UPDATE/UPSERT(存在更新否则插入) + 查询索引（Kettle 表输出语义） | 用户确认 |
| 调度：@Scheduled cron + 手动触发 | 用户确认（先调试后定时） |
| 日志：独立目录 + 元数据表保留策略 + 界面维护与查询 | 用户确认（出问题可追溯） |
| **逐环节调试**：抽取/转换/写入三环节独立调试 + 全链路一键；写入默认 dry-run | 用户补充确认（界面排查哪一环出错） |
| 复用 okhttp + jackson-xml/json | 已有依赖，WebService 出参解析基础具备 |
| 数据源管理用运行时动态注册（自建 EtlDataSourceRegistry），不扩展 application.yml、不碰现有 DynamicDataSource | 界面要能配置多个数据库连接，静态配置无法满足 |

## 遇到的问题
| 问题 | 解决方案 |
|------|---------|
| 现有动态数据源是静态配置驱动，无法满足界面动态配置多库 | 新建独立的运行时数据源注册中心 EtlDataSourceRegistry（自建，不碰现有 DynamicDataSource） |
| 项目是 Java 8 + Spring Boot 2.7，无自带调度持久化 | 用 Spring TaskScheduler + 元数据表存 cron，支持热更新 |
| 目标库=现有 Oracle 报表库（168.168.235.105:1521/yuanqi） | 可把它配置为「目标数据源」，或提供「复用现有 master 连接池」选项，不重复建池 |
| 密码存储 | 复用项目加解密工具或自建对称加密，存密文 |
| WebService 出参是嵌套对象/数组 | 后端递归展开为 dot-path 层级树 + 路径→值映射，供前端树形映射 |
| Oracle MERGE（存在则更新否则插入） | 用 Oracle 原生 `MERGE INTO ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT` |

## 资源
- 配置文件：`src/main/resources/application-dev.yml`
- 数据源代码：`src/main/java/com/reports/config/DataSourceConfig.java`
- 前端入口：`reports-web/index.html`、`reports-web/api-config.js`

## 视觉/浏览器发现
- 未浏览浏览器页面（本阶段仅静态梳理）
