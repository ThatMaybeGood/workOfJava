# 预测门诊量报表接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：预测门诊量报表

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-forecast"
    },
    "body": {
        "deptName": "",
        "extend_params1": null,
        "extend_params2": null,
        "extend_params3": null
    }
}
```

### 请求参数说明

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptName | string | 否 | 科室名称，空字符串表示全部 |
| extend_params1 | any | 否 | 扩展参数1 |
| extend_params2 | any | 否 | 扩展参数2 |
| extend_params3 | any | 否 | 扩展参数3 |

## 响应报文

```json
{
    "result": {
        "sign_type": "md5",
        "code": "10000",
        "msg": "接口调用成功，并且业务系统也处理成功",
        "sub_code": "success",
        "sub_msg": "预测门诊量报表查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "tomorrow": 462,
            "nextWeek": 462,
            "nextMonth": 462,
            "nextYear": 462
        },
        "monthForecast": {
            "dates": ["01\n日", "02\n一", "03\n二"],
            "data": [30, 45, 60]
        },
        "yearForecast": {
            "months": ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"],
            "data": [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 门诊量预测概览 |
| body.overview.tomorrow | number | 预测明日门诊量 |
| body.overview.nextWeek | number | 预测未来一周门诊量 |
| body.overview.nextMonth | number | 预测未来一个月门诊量 |
| body.overview.nextYear | number | 预测未来一年门诊量 |
| body.monthForecast | object | 未来30天门诊量预测 |
| body.monthForecast.dates | array | 日期标签 |
| body.monthForecast.data | array | 预测数据 |
| body.yearForecast | object | 未来12个月门诊量预测 |
| body.yearForecast.months | array | 月份标签 |
| body.yearForecast.data | array | 预测数据 |
