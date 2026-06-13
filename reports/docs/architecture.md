# Reports 报表网关项目架构设计文档

## 一、项目概述

Reports 报表网关是一个基于 Spring Boot 的统一报表查询服务平台，采用 RESTful 风格接口设计，对外暴露单一网关入口，通过请求报文中的 `method` 字段实现路由分发，支持多数据源动态切换、链路追踪日志、统一分页和异常处理。

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| JDK | 1.8 | Java 版本 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| Druid | 1.2.18 | 连接池 |
| Oracle JDBC | 21.8.0.0 | 数据库驱动 |
| Lombok | 1.18.36 | 代码简化 |
| Spring AOP | - | 日志切面 |

---

## 二、项目目录结构

```
reports/
├── pom.xml                                    # Maven 构建配置
├── docs/
│   └── architecture.md                        # 架构设计文档（本文档）
└── src/main/java/com/reports/
    ├── ReportsApplication.java                # 启动类
    ├── config/                               # 配置层
    │   ├── DataSourceConfig.java             # 多数据源配置
    │   ├── DynamicDataSource.java            # 动态数据源路由
    │   ├── DynamicDataSourceContextHolder.java # 数据源上下文
    │   ├── MybatisPlusConfig.java            # MyBatis-Plus 配置
    │   ├── TraceIdConfig.java                # 追踪号配置
    │   └── ReportDataConfig.java             # 数据模式配置（mock/jdbc/mybatis-plus）
    ├── controller/                           # 控制层
    │   └── GatewayController.java            # 统一网关入口
    ├── service/                              # 业务层
    │   ├── GatewayService.java               # 网关服务接口
    │   ├── OutpatientOperationService.java   # 门诊报表服务
    │   ├── handler/                          # 处理器
    │   │   ├── ReportHandler.java            # 处理器接口
    │   │   ├── ReportHandlerFactory.java     # 处理器工厂
    │   │   └── impl/                         # 处理器实现
    │   │       └── OutpatientOperationHandler.java
    │   └── impl/                             # 服务实现
    │       ├── GatewayServiceImpl.java
    │       └── OutpatientOperationServiceImpl.java
    ├── dto/                                  # 数据传输对象
    │   ├── common/                           # 通用 DTO
    │   │   ├── ApiRequest.java               # 统一请求包装
    │   │   ├── ApiResponse.java              # 统一响应包装
    │   │   ├── RequestHead.java              # 请求头
    │   │   ├── BaseRequestBody.java          # 请求体基类
    │   │   ├── Result.java                  # 响应结果
    │   │   ├── PageParam.java               # 分页参数
    │   │   └── PageResult.java              # 分页结果
    │   ├── request/                          # 请求 DTO
    │   │   └── OutpatientOperationRequest.java
    │   └── response/                         # 响应 DTO
    │       ├── OutpatientOperationResponse.java
    │       ├── OverviewData.java
    │       ├── TableItem.java
    │       ├── VisitCountDetail.java
    │       ├── UnitDetail.java
    │       └── UnitDetailItem.java
    ├── entity/                               # 实体类
    ├── enums/                                # 枚举
    │   └── ResultCode.java                   # 结果码枚举
    ├── exception/                            # 异常处理
    │   ├── BusinessException.java            # 业务异常
    │   ├── DataSourceException.java          # 数据源异常
    │   └── GlobalExceptionHandler.java       # 全局异常处理器
    ├── aspect/                               # AOP 切面
    │   └── LogAspect.java                    # 日志切面
    ├── util/                                 # 工具类
    │   ├── TraceIdGenerator.java            # 追踪号生成器
    │   └── MdcUtil.java                     # MDC 工具
    └── mapper/                               # Mapper 接口
        └── OutpatientOperationMapper.java
```

---

## 三、统一网关设计

### 3.1 接口地址

```
POST http://localhost:18089/reports/gateway
Content-Type: application/json
```

### 3.2 请求报文格式

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-operation"
    },
    "body": {
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
        "deptCode": "232323",
        "extend_params1": null,
        "extend_params2": null,
        "extend_params3": null
    }
}
```

### 3.3 响应报文格式

```json
{
    "result": {
        "sign_type": "md5",
        "code": "10000",
        "msg": "接口调用成功，并且业务系统也处理成功",
        "sub_code": "success",
        "sub_msg": "门诊运行数据统计查询成功！",
        "success": true
    },
    "body": {
        // 业务数据
    }
}
```

### 3.4 路由分发机制

采用 **策略模式** + **工厂模式** 实现 Method 路由：

1. `GatewayController` 接收统一入口请求
2. `GatewayService` 提取 `method` 字段
3. `ReportHandlerFactory` 根据 `method` 查找对应的 `ReportHandler`
4. 调用 `ReportHandler.handle()` 执行具体业务逻辑

```
Request -> GatewayController -> GatewayService -> ReportHandlerFactory -> ReportHandler
```

### 3.5 新增报表步骤

新增报表只需以下步骤，无需修改网关入口：

1. **创建请求 DTO**：继承 `BaseRequestBody`
2. **创建响应 DTO**：定义业务数据结构
3. **创建 Service 接口和实现**：编写业务逻辑和 SQL
4. **创建 Handler 实现 `ReportHandler`**：注册 `method` 名称
5. **创建 Mapper**：编写个性化 SQL（可选）

---

## 四、链路追踪日志设计

### 4.1 追踪号规则

追踪号格式支持配置化，通过 `application.yml` 配置：

```yaml
reports:
  trace:
    prefix: "YQ"          # 前缀
    number-length: 9      # 数字长度
    include-brackets: true # 是否包含方括号
```

示例输出：`[YQ123232322]`

### 4.2 日志级别

| 级别 | 用途 |
|------|------|
| INFO | 请求进入、响应返回 |
| TRACE | 详细参数、SQL 执行 |
| ERROR | 异常捕获 |

### 4.3 MDC 链路追踪

- `TraceIdGenerator`：生成追踪号
- `LogAspect`：AOP 切面拦截 `GatewayController`
- `MdcUtil`：MDC 工具类管理 TraceId
- 全局异常处理器中使用 MDC 获取 TraceId

---

## 五、多数据源动态切换

### 5.1 设计思路

采用 `AbstractRoutingDataSource` 实现动态数据源切换：

- `DynamicDataSource`：继承 `AbstractRoutingDataSource`，实现 `determineCurrentLookupKey()`
- `DynamicDataSourceContextHolder`：ThreadLocal 保存当前数据源标识
- `DataSourceConfig`：配置多个数据源，注册到 `DynamicDataSource`

### 5.2 切换方式

```java
// 在 Service 中切换数据源
DynamicDataSourceContextHolder.set("slave");
try {
    // 执行查询
} finally {
    DynamicDataSourceContextHolder.clear();
}
```

### 5.3 配置示例

```yaml
spring:
  datasource:
    master:
      url: jdbc:oracle:thin:@//host1:1521/ORCL
    slave:
      url: jdbc:oracle:thin:@//host2:1522/ORCL2
```

---

## 六、统一分页策略

### 6.1 分页参数

```java
public class PageParam {
    private Integer page = 1;      // 当前页码
    private Integer pageSize = 10; // 每页条数
}
```

### 6.2 分页结果

```java
public class PageResult<T> {
    private List<T> list;      // 数据列表
    private Long total;        // 总记录数
    private Integer page;      // 当前页码
    private Integer pageSize;  // 每页条数
}
```

### 6.3 使用方式

```java
PageResult<TableItem> result = PageResult.of(list, total, page, pageSize);
```

---

## 七、异常处理机制

### 7.1 异常体系

```
Exception
├── BusinessException     # 业务异常
├── DataSourceException   # 数据源异常
└── ...                   # 其他自定义异常
```

### 7.2 全局异常处理器

`GlobalExceptionHandler` 统一捕获所有异常，返回标准化响应：

- `BusinessException` -> 业务错误码
- `BindException` -> 参数绑定错误
- `IllegalArgumentException` -> 非法参数
- `DataSourceException` -> 数据源错误
- `Exception` -> 系统内部错误

---

## 八、结果码枚举

| 码 | 说明 |
|-----|------|
| 10000 | 成功 |
| 20001 | 参数错误 |
| 20002 | 参数缺失 |
| 20003 | 参数格式错误 |
| 30001 | 方法不存在 |
| 30002 | 方法未实现 |
| 40001 | 数据不存在 |
| 50001 | 数据库异常 |
| 50002 | 数据源异常 |
| 50003 | SQL 执行异常 |
| 99999 | 系统内部错误 |

---

## 九、扩展指南

### 新增报表步骤

以新增 `reports.inp.inpatient-operation`（住院运行统计）为例：

1. **创建请求 DTO**

```java
package com.reports.dto.request;

@Data
public class InpatientOperationRequest extends BaseRequestBody {
    private String startDate;
    private String endDate;
}
```

2. **创建响应 DTO**

```java
package com.reports.dto.response;

@Data
public class InpatientOperationResponse {
    private OverviewData overview;
    private PageResult<TableItem> table;
}
```

3. **创建 Service**

```java
package com.reports.service;

public interface InpatientOperationService {
    InpatientOperationResponse query(InpatientOperationRequest request);
}
```

4. **创建 Handler**（注意：`handle` 方法接收 `ApiRequest<Object>`，需要用 `ObjectMapper` 转换 body）

```java
package com.reports.service.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class InpatientOperationHandler implements ReportHandler<InpatientOperationRequest, InpatientOperationResponse> {
    
    public static final String METHOD = "reports.inp.inpatient-operation";
    private final ObjectMapper objectMapper;
    
    public InpatientOperationHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public String getMethod() { return METHOD; }
    
    @Override
    public ApiResponse<InpatientOperationResponse> handle(ApiRequest<Object> request) {
        // 将 LinkedHashMap 转换为具体类型
        InpatientOperationRequest body;
        if (request.getBody() instanceof InpatientOperationRequest) {
            body = (InpatientOperationRequest) request.getBody();
        } else {
            body = objectMapper.convertValue(request.getBody(), InpatientOperationRequest.class);
        }
        
        // 业务逻辑...
        return ApiResponse.success(response, "住院运行数据统计查询成功！");
    }
}
```

5. **（可选）创建 Mapper 写 SQL**

```java
@Mapper
public interface InpatientOperationMapper {
    @Select("SELECT * FROM INPATIENT_STAT WHERE ...")
    List<Map<String, Object>> query(Map<String, Object> params);
}
```

---

## 十、数据模式切换

支持三种数据获取模式，通过 `application.yml` 配置：

```yaml
reports:
  data:
    mode: mock    # 可选值: mock / jdbc / mybatis-plus
```

| 模式 | 说明 |
|------|------|
| `mock` | 返回模拟数据（默认） |
| `jdbc` | 在 Service 中直接写 SQL（JdbcTemplate） |
| `mybatis-plus` | 通过实体 + QueryWrapper 操作 |

### JdbcTemplate 示例

```java
@Autowired
private JdbcTemplate jdbcTemplate;

// 切换数据源
DynamicDataSourceContextHolder.set("slave");
try {
    String sql = "SELECT * FROM outpatient_record WHERE visit_date BETWEEN ? AND ?";
    List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, startDate, endDate);
} finally {
    DynamicDataSourceContextHolder.clear();
}
```

---

## 十、启动与测试

### 启动项目

```bash
mvn spring-boot:run
```

### 测试接口

```bash
curl -X POST http://localhost:18089/reports/gateway \
  -H "Content-Type: application/json" \
  -d '{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-operation"
    },
    "body": {
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
        "deptCode": "232323"
    }
  }'
```

---

## 十一、后续优化建议

1. **接口鉴权**：在 GatewayController 中添加 Token 验证
2. **请求限流**：集成 RateLimiter 防止接口被刷
3. **缓存优化**：对热点报表数据添加 Redis 缓存
4. **SQL 监控**：集成 Druid 监控面板，实时查看 SQL 执行情况
5. **数据字典**：建立通用数据字典，统一管理科室、医生等基础数据
6. **报表导出**：支持 Excel、PDF 等格式导出
