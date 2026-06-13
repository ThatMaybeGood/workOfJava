# 门诊收入分析接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：门诊收入分析

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-revenue"
    },
    "body": {
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
| timeRange | string | 是 | 统计时间范围：lastMonth、lastWeek、yesterday、today |
| startDate | string | 是 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 是 | 结束日期，格式 yyyy-MM-dd |
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
        "sub_msg": "门诊收入分析查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "outpatientRevenue": 52612536.3,
            "serviceRevenue": 7353266.8
        },
        "deptTable": {
            "list": [
                {
                    "deptName": "心血管内科门诊",
                    "outpatientRevenue": "500.0",
                    "serviceRevenue": "400.0"
                }
            ],
            "total": 10,
            "page": 1,
            "pageSize": 10
        },
        "doctorTable": {
            "list": [
                {
                    "doctorName": "张三",
                    "deptName": "心血管内科门诊",
                    "doctorBenefit": "500.0",
                    "serviceRevenue": "400.0"
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
| body.overview | object | 收入概览数据 |
| body.overview.outpatientRevenue | number | 门诊收入 |
| body.overview.serviceRevenue | number | 服务性收入 |
| body.deptTable | object | 各科室收入统计表格 |
| body.deptTable.list | array | 科室收入列表 |
| body.doctorTable | object | 各医生收入统计表格 |
| body.doctorTable.list | array | 医生收入列表 |
| body.*.total | number | 总记录数 |
| body.*.page | number | 当前页码 |
| body.*.pageSize | number | 每页条数 |
