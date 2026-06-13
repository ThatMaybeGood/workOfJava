# 门诊管理质量控制接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：门诊管理质量控制

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-quality-control"
    },
    "body": {
        "startMonth": "2025-01",
        "endMonth": "2025-12",
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
| startMonth | string | 是 | 开始月份，格式 yyyy-MM |
| endMonth | string | 是 | 结束月份，格式 yyyy-MM |
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
        "sub_msg": "门诊管理质量控制查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "emrUsageRate": "46.2%",
            "standardDiagnosisRate": "46.2%",
            "onTimeRate": "46.2%",
            "stopRate": "46.2%",
            "chemoRecordRate": "46.2%",
            "chemoAdverseRate": "46.2%",
            "chemoInfusionRate": "46.2%",
            "criticalValueRate": "46.2%",
            "bloodDrawErrorRate": "46.2%",
            "surgeryComplicationRate": "46.2%",
            "adverseEventRate": "46.2%"
        },
        "table": {
            "list": [
                {
                    "month": "2025-12",
                    "emrUsageRate": "62.2%",
                    "standardDiagnosisRate": "62.2%",
                    "onTimeRate": "62.2%",
                    "stopRate": "62.2%",
                    "chemoRecordRate": "62.2%",
                    "chemoAdverseRate": "62.2%",
                    "chemoInfusionRate": "62.2%",
                    "criticalValueRate": "62.2%",
                    "bloodDrawErrorRate": "62.2%",
                    "surgeryComplicationRate": "62.2%",
                    "adverseEventRate": "62.2%"
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
| body.overview | object | 质量控制指标概览 |
| body.overview.emrUsageRate | string | 门诊电子病历使用率 |
| body.overview.standardDiagnosisRate | string | 门诊标准诊断使用率 |
| body.overview.onTimeRate | string | 门诊准时出诊率 |
| body.overview.stopRate | string | 门诊停诊率 |
| body.overview.chemoRecordRate | string | 门诊化疗病历记录完整率 |
| body.overview.chemoAdverseRate | string | 门诊化疗严重不良反应发生率 |
| body.overview.chemoInfusionRate | string | 门诊化疗患者静脉治疗相关不良事件发生率 |
| body.overview.criticalValueRate | string | 门诊危急值30分钟内通报完成率 |
| body.overview.bloodDrawErrorRate | string | 门诊静脉采血相关差错发生率 |
| body.overview.surgeryComplicationRate | string | 门诊手术并发症发生率 |
| body.overview.adverseEventRate | string | 每千门诊诊疗人次不良事件发生率 |
| body.table | object | 门诊管理治疗质量控制指标表格 |
| body.table.list | array | 月度质控数据列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
