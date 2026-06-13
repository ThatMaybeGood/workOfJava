# 患者画像接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：患者画像

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-patient-portrait"
    },
    "body": {
        "patientType": "outpatient",
        "timeRange": "today",
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
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
| patientType | string | 是 | 患者类型：outpatient（门诊患者）、inpatient（住院患者） |
| timeRange | string | 是 | 统计时间范围 |
| startDate | string | 是 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 是 | 结束日期，格式 yyyy-MM-dd |
| deptName | string | 否 | 科室名称 |
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
        "sub_msg": "患者画像查询成功！",
        "success": true
    },
    "body": {
        "ageAnalysis": {
            "categories": ["0-5", "6-10", "11-15", "16-20", "21-25", "26-30", "31-35", "36-40", "41-45", "80+"],
            "archiveData": [8500, 6500, 5000, 3800, 3500, 2800, 2200, 1800, 1500, 1200],
            "outpatientData": [4500, 5800, 7200, 6800, 7000, 7500, 8000, 4200, 2800, 1500]
        },
        "insuranceAnalysis": [
            { "name": "本地职工医保", "value": 31 },
            { "name": "本地居民医保", "value": 31 },
            { "name": "本地自费", "value": 31 },
            { "name": "本地其他", "value": 31 },
            { "name": "异地职工医保", "value": 31 },
            { "name": "异地居民医保", "value": 31 },
            { "name": "异地自费", "value": 31 },
            { "name": "异地其他", "value": 31 }
        ],
        "identityAnalysis": [
            { "name": "一般人员", "value": 31 },
            { "name": "军属", "value": 31 },
            { "name": "离休干部", "value": 31 },
            { "name": "其他（地/退军人）", "value": 31 }
        ],
        "registerOriginAnalysis": [
            { "name": "重庆", "value": 31 },
            { "name": "四川", "value": 31 },
            { "name": "贵州", "value": 31 },
            { "name": "云南", "value": 31 },
            { "name": "其他", "value": 31 }
        ],
        "archiveOriginAnalysis": [
            { "name": "重庆", "value": 31 },
            { "name": "四川", "value": 31 },
            { "name": "贵州", "value": 31 },
            { "name": "云南", "value": 31 },
            { "name": "其他", "value": 31 }
        ]
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.ageAnalysis | object | 患者年龄区间分析 |
| body.insuranceAnalysis | array | 患者医保身份构成分析 |
| body.identityAnalysis | array | 患者身份类别构成分析 |
| body.registerOriginAnalysis | array | 挂号患者归属地分析 |
| body.archiveOriginAnalysis | array | 建档患者归属地分析 |
