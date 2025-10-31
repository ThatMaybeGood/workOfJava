# ShowExcel 门诊现金统计系统

## 概览
- **技术栈**：Spring Boot 3.x、MyBatis-Plus、Spring MVC、Lombok。
- **前端资源**：同步由 Spring Boot 提供静态资源（Vanilla JS + CSS）。
- **功能定位**：门诊现金统计展示、节假日维护、参数配置。

## 快速开始
1. **环境准备**
   - JDK 17+
   - Maven 3.8+
2. **启动应用**
   ```bash
   mvn clean spring-boot:run
   ```
3. **访问地址**
   ```text
   http://localhost:8080
   ```

## 目录结构（主要模块）
```
showExcel/
├── README.md
├── docs/
│   ├── legacy/              # 早期 UI 备份
│   └── notes/               # 学习笔记 & 模块说明
├── src/
│   ├── main/
│   │   ├── java/com/showexcel
│   │   │   ├── config/      # 框架/三方配置
│   │   │   ├── controller/  # REST 控制器
│   │   │   ├── dto/         # 传输对象
│   │   │   ├── mapper/      # MyBatis 映射接口
│   │   │   ├── model/       # 领域模型/实体
│   │   │   ├── repository/  # 仓储层（数据库访问抽象）
│   │   │   └── service/
│   │   │       ├── impl/    # 业务实现
│   │   │       └── *.java   # 业务接口
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── mapper/      # XML 映射
│   │       └── static/
│   │           ├── assets/
│   │           │   ├── css/
│   │           │   │   ├── index.css
│   │           │   │   └── modules/*.css
│   │           │   ├── js/
│   │           │   │   ├── core/      # 应用基座模块
│   │           │   │   ├── features/  # 功能视图脚本
│   │           │   │   ├── shared/    # 复用逻辑
│   │           │   │   └── main.js    # 入口脚本
│   │           │   └── templates/     # HTML 片段模板
│   │           └── index.html         # 页面入口
│   └── test/java/com/showexcel        # 集成 & 单元测试
└── pom.xml
```

## 命名与代码规范
- **包结构**：`com.showexcel.<layer>`，按职责划分 controller/service/repository 等。
- **Service/Impl**：接口放在 `service/`，实现放在 `service/impl/`，命名 `XxxService` / `XxxServiceImpl`。
- **DTO/VO**：跨层传输对象放在 `dto/`，命名清晰表示用途，如 `CashStatisticsTableDTO`。
- **前端模块**：
  - `assets/js/core`：应用启动、状态管理、工具逻辑。
  - `assets/js/features`：视图级功能模块。
  - `assets/js/shared`：跨视图复用的工具。
  - `assets/templates`：通过 `fetch` 动态装载的片段。

## 运行与构建
```bash
mvn clean package      # 构建可执行 jar
java -jar target/showExcel-*.jar
```

## 更多文档
- `docs/ARCHITECTURE.md`：系统架构、模块职责、数据流说明。
- `docs/notes/`：学习笔记、旧版页面结构。
- `docs/legacy/static-ui-backup/`：迁移前的 UI 备份，便于对照。

---
> 本项目目录结构参考大型企业常用的 Spring Boot 分层模式，保持了清晰的应用分层与前端资产分类，便于持续迭代与团队协作。

