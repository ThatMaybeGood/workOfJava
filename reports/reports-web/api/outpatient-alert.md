# 门诊预警统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：门诊预警统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-alert"
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
        "sub_msg": "门诊预警统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "remainAlert": 462,
            "appointmentAlert": 462,
            "earlyLeave": 462
        },
        "deptTable": {
            "list": [
                {
                    "deptName": "心血管内科门诊",
                    "remainAlert": 357,
                    "appointmentAlert": 357,
                    "earlyLeave": 357
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
                    "remainAlert": 357,
                    "appointmentAlert": 357,
                    "earlyLeave": 357
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
| body.overview | object | 预警概览数据 |
| body.overview.remainAlert | number | 当日余号预警频次 |
| body.overview.appointmentAlert | number | 号源预约预警频次 |
| body.overview.earlyLeave | number | 早退统计 |
| body.deptTable | object | 各科室门诊预警统计表格 |
| body.doctorTable | object | 各科室医生门诊预警统计表格 |
| body.*.list | array | 数据列表 |
| body.*.total | number | 总记录数 |
| body.*.page | number | 当前页码 |
| body.*.pageSize | number | 每页条数 |
