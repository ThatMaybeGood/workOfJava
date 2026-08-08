的# ETL Nexus — 数据流转平台

轻量级、可扩展的 ETL 数据抽取工具。支持多数据源、多抽取方式、可视化字段映射与定时调度。前端采用科幻主题 React SPA，可独立部署。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 2.7.18 + Java 8 |
| ORM | MyBatis-Plus 3.5.7 |
| 连接池 | Druid 1.2.18 |
| 调度 | Quartz 2.3.x |
| 加密 | Jasypt 3.0.5 |
| 前端 | **React 18 + Vite 5 + React Router 6** |
| 数据库 | 元数据 H2（开发）/ Oracle（生产）；源端支持 Oracle / MySQL / PostgreSQL / SQL Server |

---

## 快速开始（零配置启动）

```bash
# 1. 克隆项目
cd etl-platform

# 2. 启动后端（H2 内存数据库，开箱即用）
mvn spring-boot:run

# 3. 打开浏览器
# http://localhost:18880
```

登录账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 操作员 | `operator` | `operator123` |
| 观察者 | `viewer` | `viewer123` |

> 首次启动后 H2 中无任何数据，进入系统后从「数据节点管理」开始配置你的第一个数据源。

---

## 前端独立部署

前端项目位于 `frontend/`，可完全脱离后端独立开发和部署。

```bash
cd frontend

# 安装依赖（仅首次）
npm install

# 开发模式 —— 热更新，API 自动代理到 localhost:18880
npm run dev
# → http://localhost:3000

# 生产构建 —— 输出到 ../src/main/resources/static/
npm run build
```

目录结构：

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx              # 入口
    ├── App.jsx               # 路由定义
    ├── api/etl.js            # API 客户端
    ├── assets/sci-fi.css     # 科幻主题样式
    ├── context/AuthContext.jsx  # 登录状态管理
    ├── components/
    │   ├── Layout.jsx        # 侧边栏布局
    │   └── useToast.jsx      # Toast 通知 Hook
    └── pages/
        ├── Login.jsx         # 登录页
        ├── Dashboard.jsx     # 仪表盘
        ├── Datasource.jsx    # 数据源管理
        ├── Task.jsx          # 任务管理
        ├── Mapping.jsx       # 字段映射
        └── Log.jsx           # 执行日志
```

---

## 数据库初始化

### 开发环境（默认）

无需手动操作。`application-dev.yml` 已配置 H2 内存数据库，`h2_init.sql` 会自动建表。

H2 控制台：http://localhost:18880/h2-console  
JDBC URL: `jdbc:h2:mem:etl_platform`，用户名 `sa`，密码为空。

### 生产环境（Oracle）

1. 执行建表脚本：
```bash
sqlplus your_user/your_password@your_host:1521/orcl @src/main/resources/db/migration/V1.0__init_schema.sql
```

2. 编辑 `application-prod.yml`，填写数据库连接信息。

3. 启动时指定 profile：
```bash
java -jar etl-platform-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 配置说明

### 主配置 `application.yml`

```yaml
server:
  port: 18880

spring:
  profiles:
    active: dev        # dev | prod

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true

jasypt:
  encryptor:
    password: ETL_PLATFORM_SECRET_KEY
```

### 开发配置 `application-dev.yml`

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:etl_platform;MODE=Oracle;DB_CLOSE_DELAY=-1
  h2:
    console:
      enabled: true

springdoc:
  swagger-ui:
    enabled: true

logging:
  level:
    com.etl: DEBUG
```

### 生产配置 `application-prod.yml`

```yaml
spring:
  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: jdbc:oracle:thin:@${DB_HOST:localhost}:${DB_PORT:1521}/${DB_SID:orcl}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

springdoc:
  swagger-ui:
    enabled: false       # 生产环境关闭 Swagger
```

---

## API 接口

Base URL: `/api/etl/`

### 数据源管理

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/datasource` | 获取所有数据源 |
| GET | `/datasource/enabled` | 获取已启用的数据源 |
| GET | `/datasource/{id}` | 获取数据源详情 |
| GET | `/datasource/{id}/test` | 测试连接 |
| POST | `/datasource` | 新增数据源 |
| PUT | `/datasource/{id}` | 更新数据源 |
| DELETE | `/datasource/{id}` | 删除数据源 |

### 任务管理

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/task` | 获取所有任务 |
| GET | `/task/{id}` | 获取任务详情 |
| POST | `/task` | 新增任务 |
| PUT | `/task/{id}` | 更新任务 |
| DELETE | `/task/{id}` | 删除任务 |
| POST | `/task/{taskCode}/execute` | 手动执行 |
| POST | `/task/{taskCode}/schedule` | 启动定时调度 |
| POST | `/task/{taskCode}/pause` | 暂停调度 |
| POST | `/task/{taskCode}/resume` | 恢复调度 |
| POST | `/task/reload-schedules` | 重载所有调度 |

### 字段映射管理

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/mapping/task/{taskCode}` | 获取任务字段映射 |
| GET | `/mapping/{id}` | 获取映射详情 |
| POST | `/mapping` | 新增映射 |
| PUT | `/mapping/{id}` | 更新映射 |
| DELETE | `/mapping/{id}` | 删除映射 |

### 监控

| 方法 | 端点 | 说明 |
|------|------|------|
| GET | `/monitor/logs/task/{taskCode}` | 获取任务执行日志 |
| GET | `/monitor/logs/execution/{executionId}` | 获取单次执行日志 |
| GET | `/monitor/logs/running` | 获取运行中的任务 |
| GET | `/monitor/dashboard` | 获取仪表盘摘要 |

---

## 使用流程

### 第一步：添加数据源

进入「数据节点管理」页面，点击「新增节点」：

- 选择数据库类型（Oracle / MySQL / PostgreSQL / SQL Server）
- 填写 JDBC URL、用户名、密码
- 可选调整连接池参数
- 保存后点击「测试连接」确认可达

### 第二步：创建采集任务

进入「任务管理中心」，点击「新建任务」：

- **基础信息**：任务编码、名称、源/目标数据源、抽取类型、写入模式、目标表
- **源配置**：根据抽取类型填写对应内容（SQL 语句、存储过程名、表名、视图名等）
- **HTTP/文件**：如抽取类型为 HTTP 或文件，配置 URL、请求方法、文件路径等
- 可选 Cron 表达式实现定时调度

### 第三步：配置字段映射

进入「字段映射」页面，选择目标任务：

- 添加源字段 → 目标字段的映射关系
- 指定数据类型、转换表达式
- 标记主键字段（MERGE 模式必须）
- 调整映射顺序

### 第四步：执行与监控

- 在「任务管理中心」点击 ▶ 按钮手动执行
- 在「执行日志」查看运行状态、耗时、影响行数
- 失败时展开日志详情查看错误信息

---

## 抽取类型说明

| 类型 | 说明 |
|------|------|
| `PROCEDURE` | Oracle 存储过程，通过游标输出参数返回数据 |
| `SQL` | 自定义 SQL 查询语句 |
| `VIEW` | 直接读取数据库视图 |
| `TABLE` | 全表读取 |
| `HTTP` | 通过 HTTP 接口获取 JSON/XML 数据 |
| `FILE` | 读取 CSV/JSON/Excel 文件 |

## 写入模式说明

| 模式 | 说明 |
|------|------|
| `INSERT` | 批量插入，执行前可选 TRUNCATE 清空目标表 |
| `MERGE` | 基于主键的 UPSERT 逻辑（存在则更新，不存在则插入） |

---

## 项目结构

```
etl-platform/
├── frontend/                          # React 前端（可独立部署）
│   ├── src/
│   │   ├── api/etl.js                 # API 客户端
│   │   ├── assets/sci-fi.css          # 科幻主题样式
│   │   ├── components/                # 通用组件
│   │   ├── context/AuthContext.jsx    # 认证上下文
│   │   └── pages/                     # 页面组件
│   └── vite.config.js
├── src/main/java/com/etl/
│   ├── EtlApplication.java            # 启动类
│   ├── config/                        # 配置类
│   │   ├── WebConfig.java             # CORS + SPA 路由回退
│   │   └── ...
│   ├── controller/                    # REST API 控制器
│   │   ├── DatasourceController.java
│   │   ├── TaskController.java
│   │   ├── MappingController.java
│   │   └── MonitorController.java
│   ├── service/
│   │   ├── core/                      # 核心引擎
│   │   │   ├── EtlEngine.java
│   │   │   ├── DataSourceManager.java
│   │   │   └── TaskScheduler.java
│   │   ├── reader/                    # 数据读取器（PROCEDURE/SQL/VIEW/TABLE/HTTP/FILE）
│   │   ├── writer/                    # 数据写入器（INSERT/MERGE）
│   │   └── admin/                     # 管理服务
│   ├── entity/                        # 数据库实体
│   ├── enums/                         # 枚举类
│   ├── mapper/                        # MyBatis Mapper
│   └── util/                          # 工具类
├── src/main/resources/
│   ├── application.yml                # 主配置
│   ├── application-dev.yml            # 开发配置
│   ├── application-prod.yml           # 生产配置
│   ├── db/
│   │   ├── h2_init.sql                # H2 建表脚本
│   │   └── migration/V1.0__init_schema.sql  # Oracle 建表脚本
│   └── static/                        # 前端构建产物
└── pom.xml
```

---

## 常见问题

**Q: 前端开发时 API 请求 404？**  
确保后端在 `localhost:18880` 启动。Vite 开发服务器会自动将 `/api` 请求代理到后端。

**Q: 连接 Oracle 报错？**  
Oracle JDBC 驱动受 Maven 中央仓库限制。确保 `ojdbc8` 依赖已正确下载。如使用 Oracle 19c+，可能需要更新驱动版本。

**Q: 前端构建后页面空白？**  
检查 `vite.config.js` 中 `build.outDir` 是否正确指向 `../src/main/resources/static`。重新执行 `npm run build` 后重启后端。

**Q: 修改密码？**  
密码通过 Jasypt 加密存储。生产环境部署前务必修改 `jasypt.encryptor.password` 密钥。

---

## License

MIT
