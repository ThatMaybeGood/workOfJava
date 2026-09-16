# 出院结算人次统计

- **method**: `reports.cash.disch-settle-cnt`
- **入口**: 住院报表菜单「出院结算人次统计」
- **数据源**: HIS 业务库（`spring.datasource.his`，service 上 `@DataSource("his")` 切换），直查 `inp_settle_master`
- **口径**:
  - 费别：`CHARGE_TYPE = '军队医改'` → 军队医改，其余 → 普通患者
  - 结算类别：`OPERATOR_NO IN ('E700','9111')` → 自助结算，其余 → 窗口结算
  - 按支付类别维度仅统计普通患者（`CHARGE_TYPE != '军队医改'`），支付类别取原始 `CHARGE_TYPE`
  - 操作员姓名：`LEFT JOIN users u ON u.USER_ID = OPERATOR_NO` 取 `USER_NAME`，无匹配显示工号

## 请求体（body）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| dimension | string | 统计维度：`summary`（按费别）/ `operator`（按操作员）/ `payType`（按支付类别） |
| timeDimension | string | 时间粒度：`day`（按天，默认）/ `month`（按月） |
| startDate | string | 开始日期 yyyy-MM-dd |
| endDate | string | 结束日期 yyyy-MM-dd（含当天，SQL 内 `&lt; endDate + 1`） |

## 响应体（body）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| list | array | 统计行，字段见下 |
| total | number | 总行数 |

list 元素（按维度取对应字段，其余为空）：

| 字段 | 说明 |
| --- | --- |
| itemDate | 统计日期（按天 yyyy-MM-dd / 按月 yyyy-MM） |
| feeType | 费别（军队医改/普通患者） |
| settleChannel | 结算类别（自助结算/窗口结算） |
| operatorNo | 操作员工号（按操作员维度） |
| operatorName | 操作员姓名（按操作员维度） |
| payType | 支付类别（按支付类别维度） |
| cnt | 人次 |
