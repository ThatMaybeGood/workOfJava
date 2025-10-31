# 系统架构说明

## 后端分层
- **Controller**：对外暴露 REST API，进行入参校验与结果包装。
- **Service**：业务逻辑处理，聚合/编排领域模型与仓储操作。
- **Service Impl**：`service/impl` 中实现接口，支持 AOP/事务扩展。
- **Repository/Mapper**：
  - `repository/`：面向领域的仓储接口。
  - `mapper/` + `mapper/*.xml`：MyBatis 映射，用于数据库访问。
- **Model/DTO**：
  - `model/` 用于数据库实体或领域对象。
  - `dto/` 用于跨层传输的数据结构。

## 前端资产
```
assets/
├── css/
│   ├── index.css         # 页面入口样式
│   └── modules/*.css     # 分模块样式（按职能拆分）
├── js/
│   ├── core/             # 公共基座逻辑（init、state、工具）
│   ├── features/         # 功能模块脚本（表格/参数/节假日）
│   ├── shared/           # 复用型逻辑（导出等）
│   └── main.js           # 页面入口脚本
└── templates/            # HTML 片段，通过 fetch 动态加载
```

### 模块依赖关系
- `main.js`：启动入口 → 调用 `initializeApp()`。
- `app-init.js`：加载模板、绑定事件、初始化状态。
- `view-switcher.js`：切换视图并调度 `features` 模块初始化。
- `features/*.js`：具体页面功能，依赖 `core` & `shared` 输出。

## 命名约定
- Java 类使用 PascalCase，接口以 `Service`/`Repository` 结尾。
- DTO/VO 以用途命名：`CashStatisticsDTO`、`CashStatisticsTableDTO`。
- Java 包全小写，使用单一职责名称：`controller`、`service`、`mapper`。
- JS 模块文件采用 kebab-case（如 `table-view.js`），与所处目录对应。
- CSS 模块建议继续按功能划分文件，并在 `index.css` 中统一引入或复制内容。

## 构建流水线建议
1. `mvn clean verify`：执行单元/集成测试。
2. `mvn spring-boot:repackage`：生成可执行 JAR。
3. 使用 CI/CD 时建议缓存 `~/.m2`，并在 `target/` 中提取产物。

## 测试约定
- `src/test/java` 继续沿用包结构，测试类与被测类同名后缀 `Test` 或 `IT`。
- 可引入 `@SpringBootTest` + MockMvc 或 WebTestClient 进行 API 测试。

## 术语对照
- **Accounting**：会计室数据块。
- **Appointment**：预约中心数据块。
- **Merge Config**：前端表格合并单元格配置。

---
> 如需扩展，可在 `docs/` 下补充数据模型设计、API 文档与上游系统对接说明。

