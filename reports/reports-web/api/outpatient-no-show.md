# 爽约退号分析接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：爽约退号分析

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-no-show"
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
        "sub_msg": "爽约退号分析查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "refundCount": 12536,
            "refundRate": "14.6%",
            "noShowCount": 12536,
            "noShowRate": "14.6%"
        },
        "refundOrigin": [
            { "name": "重庆", "value": 31 },
            { "name": "四川", "value": 31 },
            { "name": "贵州", "value": 31 },
            { "name": "云南", "value": 31 },
            { "name": "其他", "value": 31 }
        ],
        "refundChannel": [
            { "name": "窗口", "value": 31 },
            { "name": "小程序", "value": 31 }
        ],
        "ageAnalysis": {
            "categories": ["0-14", "15-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90+"],
            "data": [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
        },
        "table": {
            "list": [
                {
                    "deptName": "心血管内科门诊",
                    "refundCount": 357,
                    "refundRate": "62.2%",
                    "refundOrigin": { "chongqing": "357 (30%)", "sichuan": "357 (30%)", "guizhou": "357 (30%)", "yunnan": "357 (30%)", "other": "357 (30%)" },
                    "refundChannel": { "window": 357, "miniprogram": 357 },
                    "noShowCount": 357,
                    "noShowRate": "62.5%",
                    "noShowOrigin": { "chongqing": "357 (30%)", "sichuan": "357 (30%)", "guizhou": "357 (30%)", "yunnan": "357 (30%)", "other": "357 (30%)" }
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
| body.overview | object | 爽约退号概览 |
| body.refundOrigin | array | 退号患者归属地分析 |
| body.refundChannel | array | 退号渠道分析 |
| body.ageAnalysis | object | 年龄分析 |
| body.table | object | 各科室退号爽约量统计表格 |
| body.table.list | array | 科室退号爽约数据列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
