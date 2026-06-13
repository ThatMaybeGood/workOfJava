# 互医质控运营月报接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：互医质控运营月报

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-internet-hospital"
    },
    "body": {
        "month": "2025-12",
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
| month | string | 是 | 统计月份，格式 yyyy-MM |
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
        "sub_msg": "互医质控运营月报查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "outpatientVolume": 4652,
            "doctorRatio": "73.5%",
            "receptionRate": "92.6%",
            "prescriptionRate": "83.1%",
            "recordRate": "95.7%",
            "reviewRate": "68.7%",
            "executionRate": "71.9%"
        },
        "operationTable": {
            "list": [
                {
                    "name": "诊察号量（含退号）",
                    "current": 120,
                    "last": 100,
                    "growth": "+10%"
                }
            ]
        },
        "businessChart": {
            "categories": ["在线诊疗", "便民咨询", "护理咨询"],
            "current": [5000, 6000, 7000],
            "last": [5000, 6000, 7000]
        },
        "deptRanking": {
            "list": [
                {
                    "rank": 1,
                    "deptName": "皮肤_风湿免疫科门诊",
                    "currentMonth": 120,
                    "lastMonth": 100,
                    "growth": "+10%"
                }
            ],
            "total": 10,
            "page": 1,
            "pageSize": 10
        },
        "doctorRanking": {
            "list": [
                {
                    "rank": 1,
                    "doctorName": "张一",
                    "deptName": "皮肤_风湿免疫科门诊",
                    "title": "主治医师",
                    "currentMonth": 100
                }
            ],
            "total": 10,
            "page": 1,
            "pageSize": 10
        },
        "growthChart": {
            "categories": ["皮肤_风湿免疫科门诊", "心血管内科门诊"],
            "data": [50, 60]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 互联网医院运营概览 |
| body.overview.outpatientVolume | number | 互联网医院门诊量 |
| body.overview.doctorRatio | string | 互联网医师占比 |
| body.overview.receptionRate | string | 互联网医院接诊率 |
| body.overview.prescriptionRate | string | 互联网医院处方开具率 |
| body.overview.recordRate | string | 互联网医院病历书写率 |
| body.overview.reviewRate | string | 互联网医院处方点评率 |
| body.overview.executionRate | string | 互联网医院药品处方执行率 |
| body.operationTable | object | 互联网医院运行情况表 |
| body.businessChart | object | 互联网医院业务分析图表 |
| body.deptRanking | object | 互联网医院临床科室按接诊量排行 |
| body.doctorRanking | object | 互联网医院医生个人接诊量排行 |
| body.growthChart | object | 互联网医院科室增长趋势图表 |
| body.*Ranking.total | number | 总记录数 |
| body.*Ranking.page | number | 当前页码 |
| body.*Ranking.pageSize | number | 每页条数 |
