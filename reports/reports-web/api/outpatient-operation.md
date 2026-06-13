# 门诊运行数据统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：门诊运行数据统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-operation"
    },
    "body": {
        "startDate": "2025-09-22",
        "endDate": "2025-10-22",
        "deptCode": "232323",
        "extend_params1": null,
        "extend_params2": null,
        "extend_params3": null
    }
}
```

### 请求参数说明

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| timeRange | string | 是 | 统计时间范围：lastMonth（近一月）、lastWeek（近一周）、yesterday（昨日）、today（今日） |
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
        "sub_msg": "门诊运行数据统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "totalVisits": 12536,
            "appointmentRate": "83.10%",
            "visitCount": 112,
            "visitCountDetail": {
                "famousExpert": 112,
                "specialExpert": 112,
                "knownExpert": 112,
                "expertA": 112,
                "expertB": 112,
                "ordinary": 112
            },
            "examRate": "56.50%",
            "efficiency": 27.5,
            "effectiveUnits": 112,
            "totalUnits": 251,
            "unitDetail": {
                "famousExpert": { "effective": 52, "total": 112 },
                "specialExpert": { "effective": 52, "total": 112 },
                "knownExpert": { "effective": 52, "total": 112 },
                "expertA": { "effective": 52, "total": 112 },
                "expertB": { "effective": 52, "total": 112 },
                "ordinary": { "effective": 52, "total": 112 }
            }
        },
        "table": {
            "list": [
                {
                    "deptName": "心血管内科门诊",
                    "visits": 350,
                    "appointmentRate": "70.00%",
                    "examRate": "75.00%",
                    "efficiency": 25.00,
                    "visitCount": 30,
                    "famousExpert": 2,
                    "specialExpert": 3,
                    "knownExpert": 2,
                    "expertA": 5,
                    "expertB": 4,
                    "ordinary": 2,
                    "effectiveUnitsTotal": { "effective": 45, "total": 50 },
                    "unitDetail": {
                        "famousExpert": { "effective": 2, "total": 4 },
                        "specialExpert": { "effective": 2, "total": 4 },
                        "knownExpert": { "effective": 2, "total": 4 },
                        "expertA": { "effective": 2, "total": 4 },
                        "expertB": { "effective": 2, "total": 4 },
                        "ordinary": { "effective": 2, "total": 4 }
                    }
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
| result | object | 公共响应结果 |
| result.code | string | 响应码，10000 表示成功 |
| result.msg | string | 响应消息 |
| result.sub_code | string | 业务响应码 |
| result.sub_msg | string | 业务响应消息 |
| result.success | boolean | 是否成功 |
| body | object | 业务数据 |
| body.overview | object | 统计概览数据 |
| body.table | object | 表格分页数据 |
| body.table.list | array | 科室门诊量统计列表 |
| body.table.total | number | 总记录数 |
| body.table.page | number | 当前页码 |
| body.table.pageSize | number | 每页条数 |
