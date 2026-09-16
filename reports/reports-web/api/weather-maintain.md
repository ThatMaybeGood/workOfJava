# 天气数据维护

- **method**: `reports.common.weather-maintain`
- **入口**: 预测门诊量报表页面「天气维护」按钮弹窗
- **数据表**: `TR_FC_WEATHER`（每日天气源表，与预测门诊量共用）

## 请求体（body）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| action | string | 操作类型：`query`（查询，默认）/ `save`（保存）/ `delete`（删除） |
| startDate | string | 开始日期 yyyy-MM-dd（query） |
| endDate | string | 结束日期 yyyy-MM-dd（query） |
| page | number | 页码，默认 1（query） |
| pageSize | number | 每页条数，默认 10（query） |
| weatherDate | string | 天气日期 yyyy-MM-dd（delete，按日期删） |
| list | array | 保存的天气列表（save），元素见下 |

list 元素：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| weatherDate | string | 天气日期 yyyy-MM-dd（唯一，存在即覆盖更新） |
| weatherType | string | 天气类型（取自通用字典 `weather_type`） |

> 出勤系数 **不手填**：save 时后端自动计算 `1 - 近30天同天气日期的爽约退号率`
>（爽约+退号)/挂号量，统计窗口为登记日前 30 天；无数据存空，预测时按 1 处理。

## 响应体（body）

### query

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| list | array | 天气明细，元素含 weatherDate/weatherType/weatherCoef/weatherSource |
| total | number | 总条数 |
| page | number | 当前页码 |
| pageSize | number | 每页条数 |

### save / delete

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| affected | number | 影响行数 |

## 来源（weather_source）

界面登记/修改的记录统一记为 **人工登记**；ETL 灌数模拟的为 **接口同步**。
