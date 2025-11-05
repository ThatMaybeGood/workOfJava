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



```json
{
  "title": "现金统计表（2025-11-02）",
  "metadata": {
    "totalRows": 19,
    "totalCols": 14,
    "generatedAt": "2025-11-02T10:00:00Z"
  },
  "headers": [
    "序号", "名称", "预交金收入", "医疗收入", "挂号收入", 
    "应交报表数（1）", "前日暂收款（2）", "实交报表数（3）=（1）-（2）", 
    "当日暂收款（4）", "实收现金数（5）=(3)+（4）", "留存数差额（6）=（7）-（3）-（8）", 
    "留存现金数（7）", "备用金（8）", "备注"
  ],
  "sections": [
    {
      "name": "会计室",
      "type": "accounting",
      "rows": [
	  {
            "hisAdvancePayment": 20.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 22.0,
            "reportAmount": 0.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": -22.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": -22.0,
            "retainedDifference": 22.0,
            "retainedCash": 0.0,
            "pettyCash": 0.0
          }
        ,
        {
            "hisAdvancePayment": 20.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 22.0,
            "reportAmount": 0.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": -22.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": -22.0,
            "retainedDifference": 22.0,
            "retainedCash": 0.0,
            "pettyCash": 0.0
        }
		]
    },
    {
      "name": "预约中心",
      "type": "reservation",
      "rows": [
        {
            "hisAdvancePayment": 0.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 12.0,
            "reportAmount": 10.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": -2.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": -2.0,
            "retainedDifference": 25.0,
            "retainedCash": 23.0,
        },
        {
            "hisAdvancePayment": 20.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 0.0,
            "reportAmount": 0.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": 0.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": 0.0,
            "retainedDifference": 0.0,
            "retainedCash": 0.0,
            "pettyCash": 0.0
        },
        {
            "hisAdvancePayment": 20.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 12.0,
            "reportAmount": 10.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": -2.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": -2.0,
            "retainedDifference": 25.0,
            "retainedCash": 23.0,
            "pettyCash": 0.0
        }
      ]
    },
    {
      "name": "总计",
      "type": "grand_total",
      "rows": [
        {
            "hisAdvancePayment": 40.0,
            "hisMedicalIncome": 0.0,
            "hisRegistrationIncome": 34.0,
            "reportAmount": 10.0,
            "previousTemporaryReceipt": 0.0,
            "actualReportAmount": -24.0,
            "currentTemporaryReceipt": 0.0,
            "actualCashAmount": -24.0,
            "retainedDifference": 47.0,
            "retainedCash": 23.0,
            "pettyCash": 0.0
        }
      ]
    }
  ],
  "layout": {
    "sectionHeaders": [
      {
        "row": 2,
        "col": 0,
        "colSpan": 14,
        "content": "预约中心",
        "style": "section_header"
      }
    ],
    "summaryRows": [
      {
        "row": 1,
        "col": 0,
        "colSpan": 2,
        "content": "会计室计表",
        "style": "summary_label"
      },
      {
        "row": 5,
        "col": 0,
        "colSpan": 2,
        "content": "预约统计表",
        "style": "summary_label"
      },
      {
        "row": 6,
        "col": 0,
        "colSpan": 2,
        "content": "合计统计表",
        "style": "summary_label"
      }
    ],
    "specialCells": [
      {
        "row": 7,
        "col": 0,
        "colSpan": 2,
        "content": "当日暂收款",
        "style": "special_label"
      },
      {
        "row": 18,
        "col": 0,
        "colSpan": 2,
        "content": "审核",
        "style": "signature"
      },
      {
        "row": 18,
        "col": 9,
        "content": "出纳",
        "style": "signature"
      }
    ]
  }
}