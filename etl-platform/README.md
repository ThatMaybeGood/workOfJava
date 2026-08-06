# ETL通用数据抽取平台

## 项目简介

ETL通用数据抽取平台是一个轻量级、可配置、可扩展的数据抽取工具，支持多种数据源和写入模式，通过可视化Web界面管理数据源、任务和字段映射。

## 技术栈

- Java 8
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.7
- Druid 1.2.18
- Oracle (ojdbc8 21.8.0.0)
- Quartz 2.3.x
- Jasypt 3.0.5
- Apache HttpClient 5.x
- SpringDoc OpenAPI 1.6.15

## 功能特性

### 数据源管理
- 支持多种数据库类型：Oracle、MySQL、PostgreSQL、SQLServer
- 连接测试功能
- 密码加密存储（Jasypt）

### 任务配置
- 多种抽取方式：存储过程、SQL、视图、表、HTTP接口、文件
- INSERT/MERGE两种写入模式
- Cron表达式定时调度
- 字段映射配置

### 执行引擎
- 存储过程→游标读取→批量写入
- 流式处理，内存友好
- 执行日志记录
- 进度跟踪

## 快速开始

### 1. 数据库准备

执行 `src/main/resources/db/migration/V1.0__init_schema.sql` 脚本创建表结构。

### 2. 配置修改

编辑 `application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@your_host:1521/orcl
    username: your_username
    password: your_password
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

访问 Swagger UI: http://localhost:18082/swagger-ui.html

### 4. 数据源配置

通过API或数据库直接插入数据源配置。

### 5. 任务配置

创建ETL任务，配置源/目标数据源、抽取方式、写入模式、定时表达式等。

### 6. 字段映射

配置源字段与目标字段的映射关系。

### 7. 执行任务

通过API手动触发任务或等待Quartz定时调度自动执行。

## API接口

### 数据源管理
- `POST /api/etl/datasource` - 新增数据源
- `PUT /api/etl/datasource/{id}` - 更新数据源
- `DELETE /api/etl/datasource/{id}` - 删除数据源
- `GET /api/etl/datasource/{id}` - 获取数据源详情
- `GET /api/etl/datasource` - 获取所有数据源
- `GET /api/etl/datasource/{id}/test` - 测试数据源连接

### 任务管理
- `POST /api/etl/task` - 新增任务
- `PUT /api/etl/task/{id}` - 更新任务
- `DELETE /api/etl/task/{id}` - 删除任务
- `GET /api/etl/task/{id}` - 获取任务详情
- `POST /api/etl/task/{taskCode}/execute` - 手动执行任务
- `POST /api/etl/task/{taskCode}/schedule` - 启动定时调度
- `POST /api/etl/task/{taskCode}/pause` - 暂停定时任务
- `POST /api/etl/task/{taskCode}/resume` - 恢复定时任务

### 字段映射管理
- `POST /api/etl/mapping` - 新增字段映射
- `PUT /api/etl/mapping/{id}` - 更新字段映射
- `DELETE /api/etl/mapping/{id}` - 删除字段映射
- `GET /api/etl/mapping/task/{taskCode}` - 获取任务的字段映射

### 监控
- `GET /api/etl/monitor/logs/task/{taskCode}` - 获取任务执行日志
- `GET /api/etl/monitor/dashboard` - 获取仪表盘数据

## 部署模式

### 数据库模式（默认）
配置存储在数据库表中，支持Web界面实时修改。

### 无数据库模式
通过YAML/JSON文件配置，适合开发测试环境。
在 `application.yml` 中设置：
```yaml
etl:
  deployment-mode: local
```

## 目录结构

```
etl-platform/
├── src/main/java/com/etl/
│   ├── EtlApplication.java          # 启动类
│   ├── config/                       # 配置类
│   ├── controller/                   # Web API
│   ├── service/
│   │   ├── core/                     # 核心引擎
│   │   ├── reader/                   # 数据读取器
│   │   ├── writer/                   # 数据写入器
│   │   └── admin/                    # 管理服务
│   ├── mapper/                       # MyBatis Mapper
│   ├── entity/                       # 实体类
│   ├── enums/                        # 枚举类
│   ├── dto/                          # 数据传输对象
│   ├── exception/                    # 异常处理
│   └── util/                         # 工具类
├── src/main/resources/
│   ├── application.yml               # 主配置
│   ├── application-dev.yml           # 开发配置
│   ├── application-prod.yml          # 生产配置
│   └── db/migration/               # 数据库脚本
└── pom.xml
```
