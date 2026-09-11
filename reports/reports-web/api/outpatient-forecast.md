# 预测门诊量报表接口文档

## 接口基本信息

- **接口地址**：`http://localhost:18089/reports/gateway`
- **请求方式**：POST
- **Content-Type**：`application/json`
- **接口名称**：预测门诊量报表

## 请求报文

```json
{
    "head": {
        "charset": "utf-8",
        "encrypt_type": "AES",
        "language": "zh_CN",
        "method": "reports.outp.outpatient-forecast"
    },
    "body": {
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
        "sub_msg": "预测门诊量报表查询成功！",
        "success": true
    },
    "body": {
        "overview": {
            "tomorrow": 460,
            "nextWeek": 3220,
            "nextMonth": 12880,
            "nextYear": 157200
        },
        "monthForecast": {
            "dates": ["2026-09-12", "2026-09-13", "2026-09-14"],
            "data": [460, 368, 460]
        },
        "yearForecast": {
            "months": ["2026-01", "2026-02", "2026-03"],
            "data": [12000, 12400, 12600]
        }
    }
}
```

### 响应参数说明

| 参数名 | 类型 | 说明 |
|--------|------|------|
| body.overview | object | 门诊量预测概览 |
| body.overview.tomorrow | number | 预测明日门诊量 |
| body.overview.nextWeek | number | 预测未来一周门诊量(明日起7天合计) |
| body.overview.nextMonth | number | 预测未来一个月门诊量(明日起30天合计) |
| body.overview.nextYear | number | 预测未来一年门诊量(当年12个月合计) |
| body.monthForecast | object | 未来30天门诊量预测(明日起) |
| body.monthForecast.dates | array | 日期(yyyy-MM-dd) |
| body.monthForecast.data | array | 预测数据 |
| body.yearForecast | object | 当年12个月门诊量预测 |
| body.yearForecast.months | array | 月份标签(yyyy-MM) |
| body.yearForecast.data | array | 预测数据 |

### 计算口径

- 每日预测 = CLAMP(基础量 × 就诊系数, 基础量×90%, 基础量×110%)
- 基础量 = 实时预约量(tr_fc_appoint,无则近30天预约日均) + 近30天挂号量日均(TR_OUTPATIENT_STATS_DAY_RESULT)
- 就诊系数 = (1 − 近30天爽约退号率) × 天气出勤系数(tr_fc_weather,空按1) × 节假日系数(tr_fc_holiday,非法定节假日按1)
- 月度预测:1月 = 去年1月 × (1 + 去年比前年门诊量增率);2-12月 = 预测全年量 × 去年同月占比;历史不足按近90天日均兜底
