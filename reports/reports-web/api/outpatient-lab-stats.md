# 检验统计接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：检验统计

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-lab-stats"
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
        "sub_msg": "检验统计查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "bloodCollection": 12536,
            "bloodEfficiency": "37.5分",
            "labEfficiency": "92.6%"
        },
        "timeAnalysis": {
            "categories": ["08:00~09:00", "09:00~10:00", "10:00~11:00", "11:00~12:00", "12:00~13:00", "13:00~14:00", "14:00~15:00", "15:00~16:00", "16:00~17:00"],
            "data": [2800, 5500, 4800, 6200, 5200, 7500, 8500, 7200, 4500]
        },
        "reportRank": {
            "categories": ["项目1", "项目2", "项目3", "项目4", "项目5", "项目6", "项目7", "项目8", "项目9", "项目10"],
            "data": [130, 100, 75, 60, 55, 45, 35, 30, 25, 20]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 检验概览数据 |
| body.overview.bloodCollection | number | 采血量 |
| body.overview.bloodEfficiency | string | 采血效率 |
| body.overview.labEfficiency | string | 检验效率 |
| body.timeAnalysis | object | 分时段采血分析 |
| body.reportRank | object | 检验项目排名 |
