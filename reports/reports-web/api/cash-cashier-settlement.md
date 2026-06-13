# 收费员结账统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：收费员结账统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.cash.cash-cashier-settlement"
    },
    "body": {
        "tab": "cashier",
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
| tab | string | 是 | 统计页签：cashier（按收费员统计）、source（按来源方式统计）、workload（工作量报表） |
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
        "sub_msg": "收费员结账统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "appointmentRegister": 10726,
            "appointmentRegisterCompare": -8,
            "appointmentFetch": 10726,
            "appointmentFetchCompare": 5,
            "todayRegister": 10726,
            "todayRegisterCompare": 5,
            "refund": 10726,
            "refundCompare": 5,
            "outpatientCharge": 10726,
            "outpatientChargeCompare": -8,
            "outpatientRefund": 10726,
            "outpatientRefundCompare": -8,
            "prepayment": 10726,
            "prepaymentCompare": 5,
            "hospitalRefund": 10726,
            "hospitalRefundCompare": 5,
            "dischargeSettlement": 10726,
            "dischargeSettlementCompare": 5
        },
        "table": {
            "list": [
                {
                    "date": "2025-01-12",
                    "收费员1": 1000,
                    "收费员2": 1100,
                    "收费员3": 1100,
                    "收费员4": 1000000.00,
                    "收费员5": 500000.00,
                    "收费员6": 500000.00,
                    "收费员7": 1000,
                    "收费员8": 1100,
                    "汇总": 1000
                }
            ],
            "total": 55,
            "page": 1,
            "pageSize": 10
        },
        "chart": {
            "title": "收费员业务工作量分析",
            "subTitle": "收费员1",
            "dateRange": "2025-01-12~2025-01-20",
            "categories": ["预约挂号", "预约取号", "当日挂号", "退号", "门诊收费", "门诊退费", "收预交金", "退院", "出院结算"],
            "data": [120, 80, 110, 60, 130, 90, 100, 70, 85]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 收费员结账概览数据 |
| body.overview.appointmentRegister | number | 预约挂号量 |
| body.overview.appointmentRegisterCompare | number | 预约挂号量同比 |
| body.overview.appointmentFetch | number | 预约取号量 |
| body.overview.appointmentFetchCompare | number | 预约取号量同比 |
| body.overview.todayRegister | number | 当日挂号量 |
| body.overview.todayRegisterCompare | number | 当日挂号量同比 |
| body.overview.refund | number | 退号量 |
| body.overview.refundCompare | number | 退号量同比 |
| body.overview.outpatientCharge | number | 门诊收费量 |
| body.overview.outpatientChargeCompare | number | 门诊收费量同比 |
| body.overview.outpatientRefund | number | 门诊退费量 |
| body.overview.outpatientRefundCompare | number | 门诊退费量同比 |
| body.overview.prepayment | number | 收预交金量 |
| body.overview.prepaymentCompare | number | 收预交金量同比 |
| body.overview.hospitalRefund | number | 退院量 |
| body.overview.hospitalRefundCompare | number | 退院量同比 |
| body.overview.dischargeSettlement | number | 出院结算量 |
| body.overview.dischargeSettlementCompare | number | 出院结算量同比 |
| body.table | object | 收费员结账统计表格 |
| body.table.list | array | 数据列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
| body.chart | object | 工作量分析图表数据 |
| body.chart.title | string | 图表标题 |
| body.chart.subTitle | string | 图表子标题 |
| body.chart.dateRange | string | 图表日期范围 |
| body.chart.categories | array | 图表分类 |
| body.chart.data | array | 图表数据 |

### tab 不同取值说明

| tab 取值 | 表格说明 | 图表说明 |
|----------|----------|----------|
| cashier | 每行一个日期，列为各收费员及汇总 | 收费员业务工作量分析 |
| source | 每行一个日期，列为各业务来源及汇总 | 来源方式工作量分析 |
| workload | 每行一个收费员的工作量明细 | 不返回图表 |
