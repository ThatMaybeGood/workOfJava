# HIS 库中文乱码处理

## 背景

HIS 业务库（`netcen_yb`）字符集是 **US7ASCII**，但库里实际存的是 GBK 字节。
Oracle thin 驱动按服务器字符集解码返回值，`0x7F` 以上的字节在**驱动内部**就被替换掉了，
Java 侧再怎么做编码转换都救不回来。

## 症状 → 做法

直查 HIS 库的中文列显示成 `?` 或乱码时：SQL 里用 `RAWTOHEX` 包住该列，
Java 侧把 hex 还原成字节再按 GBK 解码。

参考实现：`mapper/cash/DischSettleCntMapper.xml` + `DischSettleCntServiceImpl.hexGbk()`

> 原理：`RAWTOHEX` 在服务端就把原始字节转成纯 ASCII 的十六进制串，
> 绕过了驱动那次有损解码。这是它唯一的作用，不是"多此一举的格式化"。

## 不要这么做（已验证无效）

| 尝试 | 结果 |
|---|---|
| `CONVERT(col,'ZHS16GBK','US7ASCII')` | 源字符集是 US7ASCII 时字节直通，输出仍是 GBK 字节，驱动照样按 ASCII 解，无效 |
| 只靠 `NLS_LANG` | 209 部署环境实测 thin 驱动设 `NLS_LANG=SIMPLIFIED CHINESE_CHINA.ZHS16GBK` 后直读正常，但依赖启动脚本环境变量，换环境/换驱动版本不一定生效，故只作双保险 |
| SQL 里写中文字面量 | 驱动编码时高字节变问号，条件永远匹配不上 |
| 直接查字段不加 `RAWTOHEX` | **不报错**，静默返回乱码/问号 |

## 适用范围

目前只有**出院结算人次统计**直查 HIS 且涉及中文列（`@DataSource("his")` 全项目仅此一处）。
其余报表走本地库，不受影响。

## 代码里的三个陷阱

1. `PersonCountItem.feeType` 在归并之前装的是 **hex 串不是中文**，
   `DischSettleCntServiceImpl` 归并时才解码。中途打断点看到 `E8B4B9E588AB` 属正常。
2. `NVL(u.USER_NAME, a.OPERATOR_NO)` 只有姓名字段包了 `RAWTOHEX`：
   姓名可能含中文，工号是纯 ASCII 不需要，**不是漏写**。
3. `hexGbk()` 异常时 `return hex` 是静默降级：
   哪天 SQL 漏包 `RAWTOHEX` 不会报错，只会显示乱码。

## 什么时候该换方案

直查 HIS 且涉及中文列的报表超过 **3~5 个**时，可以把 `NLS_LANG=SIMPLIFIED CHINESE_CHINA.ZHS16GBK`
写进部署环境的启动脚本（209 已实测 thin 驱动下直读中文正常），Java 侧零改动。
在此之前，逐列 `RAWTOHEX` 的维护成本更低——代价是啰嗦，收益是**不依赖环境变量和驱动行为**，
换部署机器、换 ojdbc 版本都不会失效。

当前策略是两者并用：`RAWTOHEX` 兜底，`NLS_LANG` 作双保险。
