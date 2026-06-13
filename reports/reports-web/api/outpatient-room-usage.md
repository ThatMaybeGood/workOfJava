# 诊室使用率分析接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：诊室使用率分析

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-room-usage"
    },
    "body": {
        "timeRange": "today",
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
        "deptName": "",
        "page": 1,
        "pageSize": 10,
        "extend_params1": null,
        "extend_params2": null,
        "extend_params3": null
    }
}
```

### 请求参数说明

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| timeRange | string | 是 | 统计时间范围 |
| startDate | string | 是 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 是 | 结束日期，格式 yyyy-MM-dd |
| deptName | string | 否 | 科室名称 |
| page | number | 否 | 当前页码 |
| pageSize | number | 否 | 每页条数 |
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
        "sub_msg": "诊室使用率分析查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "avgUsage": "66.9%",
            "amUsage": "66.9%",
            "pmUsage": "66.9%",
            "holidayUsage": "66.9%"
        },
        "table": {
            "list": [
                {
                    "deptName": "心血管内科门诊",
                    "avgUsage": "62.2%",
                    "amUsage": "62.2%",
                    "pmUsage": "62.2%",
                    "holidayUsage": "62.2%"
                }
            ],
            "total": 10,
            "page": 1,
            "pageSize": 10
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 诊室使用率概览 |
| body.overview.avgUsage | string | 平均诊室使用率 |
| body.overview.amUsage | string | 上午诊室使用率 |
| body.overview.pmUsage | string | 下午诊室使用率 |
| body.overview.holidayUsage | string | 节假日诊室使用率 |
| body.table | object | 各科室诊室使用率统计表格 |
| body.table.list | array | 科室使用率列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
