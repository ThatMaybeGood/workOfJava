src/main/java/com/showexcel/
├── controller/
│   └── CashStatisticsController.java
├── service/
│   ├── CashStatisticsService.java          # 接口
│   └── impl/
│       └── CashStatisticsServiceImpl.java  # 实现类
├── repository/
│   └── CashStatisticsRepository.java       # 数据访问层
├── model/
│   └── CashStatistics.java                 # 数据模型
└── dto/
    └── CashStatisticsDTO.java              # 数据传输对象



门诊现金统计系统/
├── index.html              # 主入口文件
├── styles.css              # 主样式文件
├── script.js               # 主JavaScript逻辑
├── views/                  # 视图目录
│   ├── main-content.html   # 主内容区域（包含所有视图）
│   ├── table-view.html     # 表格视图（可选拆分）
│   ├── param-view.html     # 参数维护视图（可选拆分）
│   └── holiday-view.html   # 节假日维护视图（可选拆分）
└── assets/                 # 资源目录（可选）
    ├── images/
    └── data/


在java目录下的项目包名中：
• controller：此目录主要是存放Controller的 ,比如：UserController.java。
• service：这里分接口和实现类，接口在service目录下，接口实现类在service/impl目录下。
• dao：持久层，目前比较流行的Mybatis或者jpa之类的。
• entity：就是数据库表的实体对象。
• param：放的是请求参数和相应参数UserQueryRequest、BaseResponse等
• util：通常是一些工具类，比如说：DateUtil.java、自定义的StringUtil.java
• interrupt：项目统一拦截处理，比如：登录信息，统一异常处理
• exception：自定义异常，异常错误码
• config：配置读取相关，比如RedisConfig.java