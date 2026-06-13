# 门诊服务质量分析接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：门诊服务质量分析

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-service-quality"
    },
    "body": {
        "tab": "complaint",
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
| tab | string | 是 | 明细类型：complaint（投诉明细）、praise（表扬明细） |
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
        "sub_msg": "门诊服务质量分析查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "complaintCount": 46,
            "praiseCount": 46
        },
        "complaint": {
            "list": [
                {
                    "time": "2025-11-25 13:50",
                    "dept": "心血管内科门诊",
                    "person": "张三",
                    "position": "医师",
                    "category": "病历问题",
                    "result": "有效投诉",
                    "remark": ""
                }
            ],
            "total": 10,
            "page": 1,
            "pageSize": 10
        },
        "praise": {
            "list": [
                {
                    "time": "2025-11-25 13:50",
                    "dept": "心血管内科门诊",
                    "person": "张三",
                    "position": "医师",
                    "method": "锦旗",
                    "feedback": "已反馈",
                    "remark": ""
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
| body.overview | object | 服务质量概览 |
| body.overview.complaintCount | number | 投诉量 |
| body.overview.praiseCount | number | 表扬量 |
| body.complaint | object | 投诉明细数据 |
| body.praise | object | 表扬明细数据 |
| body.*.list | array | 明细列表 |
| body.*.total | number | 总记录数 |
| body.*.page | number | 当前页码 |
| body.*.pageSize | number | 每页条数 |
