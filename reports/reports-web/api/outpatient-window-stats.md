# 人工窗口统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：人工窗口统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-window-stats"
    },
    "body": {
        "timeRange": "today",
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
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
        "sub_msg": "人工窗口统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "registerCount": 12536,
            "paymentCount": 12536,
            "refundCount": 12536
        },
        "originAnalysis": [
            { "name": "重庆", "value": 31 },
            { "name": "四川", "value": 31 },
            { "name": "贵州", "value": 31 },
            { "name": "云南", "value": 31 },
            { "name": "其他", "value": 31 }
        ],
        "ageAnalysis": {
            "categories": ["0-14", "15-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90+"],
            "data": [1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000]
        },
        "timeAnalysis": {
            "categories": ["08:00~09:00", "09:00~10:00", "10:00~11:00", "11:00~12:00", "12:00~13:00", "13:00~14:00", "14:00~15:00", "15:00~16:00", "16:00~17:00"],
            "data": [3500, 5200, 3800, 5500, 4800, 6200, 8500, 9000, 7800]
        },
        "workloadTable": {
            "headers": ["08:00~09:00", "09:00~10:00", "10:00~11:00", "11:00~12:00", "12:00~13:00", "13:00~14:00", "14:00~15:00", "15:00~16:00", "16:00~17:00"],
            "rows": [
                { "business": "挂号", "data": [357, 357, 357, 357, 357, 357, 357, 357, 357] },
                { "business": "缴费", "data": [357, 357, 357, 357, 357, 357, 357, 357, 357] },
                { "business": "退费", "data": [357, 357, 357, 357, 357, 357, 357, 357, 357] }
            ]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 窗口业务概览 |
| body.originAnalysis | array | 患者归属地分析 |
| body.ageAnalysis | object | 患者年龄分析 |
| body.timeAnalysis | object | 分时段业务量分析 |
| body.workloadTable | object | 各窗口业务分时段工作量统计 |
