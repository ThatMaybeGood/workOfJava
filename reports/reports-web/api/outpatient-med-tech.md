# 医技统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：医技统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-med-tech"
    },
    "body": {
        "timeRange": "today",
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
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
| page | number | 否 | 当前页码，默认 1 |
| pageSize | number | 否 | 每页条数，默认 10 |
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
        "sub_msg": "医技统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "checkCount": 12536,
            "onTimeRate": "73.2%",
            "waitTime": "26.6分",
            "avgWaitLate": "216.7分",
            "avgReportTime": "216.7分"
        },
        "table": {
            "list": [
                {
                    "deptName": "CT",
                    "checkCount": 357,
                    "onTimeRate": "62.2%",
                    "waitTime": 15.8,
                    "avgWaitLate": 56.6,
                    "avgReportTime": 56.6
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
| body.overview | object | 医技概览数据 |
| body.overview.checkCount | number | 检查量 |
| body.overview.onTimeRate | string | 按时检查率 |
| body.overview.waitTime | string | 预约等候时间 |
| body.overview.avgWaitLate | string | 平均等候时长（未按时） |
| body.overview.avgReportTime | string | 平均出报告时间 |
| body.table | object | 各检查项目统计表格 |
| body.table.list | array | 检查项目列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
