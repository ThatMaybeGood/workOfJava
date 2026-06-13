# 出院结算报表接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：出院结算报表

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.cash.cash-discharge-settlement"
    },
    "body": {
        "dimension": "day",
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
| dimension | string | 是 | 统计维度：month（按月统计）、day（按天统计） |
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
        "sub_msg": "出院结算报表查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "totalDischargeCount": 10726,
            "totalDischargeCompare": -8,
            "dischargedCount": 10726,
            "dischargedCompare": -8,
            "notDischargedCount": 10726,
            "notDischargedCompare": -8,
            "settlementAmount": 10726.00,
            "settlementAmountCompare": 5
        },
        "charts": {
            "channelAnalysis": [
                { "name": "窗口", "value": 31, "compare": -8 },
                { "name": "自助机", "value": 31, "compare": 5 },
                { "name": "掌上医院", "value": 31, "compare": 1 }
            ],
            "patientTypeAnalysis": [
                { "name": "微信", "value": 31, "compare": -8 },
                { "name": "支付宝", "value": 31, "compare": 5 },
                { "name": "银行卡", "value": 31, "compare": 1 },
                { "name": "现金", "value": 31, "compare": -14 }
            ],
            "amountTypeAnalysis": [
                { "name": "微信", "value": 31, "compare": -8 },
                { "name": "支付宝", "value": 31, "compare": 5 },
                { "name": "银行卡", "value": 31, "compare": 1 },
                { "name": "现金", "value": 31, "compare": -14 }
            ]
        },
        "table": {
            "list": [
                {
                    "date": "2025-01-12",
                    "totalLast": 100,
                    "totalCurrent": 120,
                    "totalCompare": 20,
                    "dischargedLast": 1000,
                    "dischargedCurrent": 1100,
                    "dischargedCompare": 10,
                    "notDischargedLast": 2000,
                    "notDischargedCurrent": 3000,
                    "notDischargedCompare": 50,
                    "amountLast": 1000000.00,
                    "amountCurrent": 500000.00,
                    "amountCompare": -50
                }
            ],
            "total": 55,
            "page": 1,
            "pageSize": 10
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 出院结算概览数据 |
| body.overview.totalDischargeCount | number | 总出院人数 |
| body.overview.totalDischargeCompare | number | 总出院人数同比 |
| body.overview.dischargedCount | number | 已出院人数 |
| body.overview.dischargedCompare | number | 已出院人数同比 |
| body.overview.notDischargedCount | number | 未出院人数 |
| body.overview.notDischargedCompare | number | 未出院人数同比 |
| body.overview.settlementAmount | number | 结算金额 |
| body.overview.settlementAmountCompare | number | 结算金额同比 |
| body.charts | object | 图表分析数据 |
| body.charts.channelAnalysis | array | 结算渠道分析 |
| body.charts.patientTypeAnalysis | array | 结算费别人次分析 |
| body.charts.amountTypeAnalysis | array | 结算费别金额分析 |
| body.table | object | 结算明细表格 |
| body.table.list | array | 日期维度结算数据列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
