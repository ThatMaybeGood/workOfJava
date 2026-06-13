# 门诊量统计 - 接口报文出参格式文档

---

## 1. 获取统计概览数据

### 接口说明
获取顶部统计卡片的汇总数据。

| 项目 | 内容 |
|------|------|
| 请求方式 | GET |
| 接口地址 | /api/outpatient/overview |
| Content-Type | application/json |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| timeRange | string | 否 | 时间范围：lastMonth/lastWeek/yesterday/today |
| startDate | string | 否 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 否 | 结束日期，格式 yyyy-MM-dd |
| deptName | string | 否 | 科室名称筛选 |

### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalVisits": 12536,
    "appointmentRate": "83.1%",
    "visitCount": 112,
    "visitCountDetail": {
      "famousExpert": 112,
      "specialExpert": 112,
      "knownExpert": 112,
      "expertA": 112,
      "expertB": 112,
      "ordinary": 112
    },
    "examRate": "56.5%",
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
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| totalVisits | int | 门诊量 |
| appointmentRate | string | 预约挂号率 |
| visitCount | int | 出诊人次 |
| visitCountDetail | object | 出诊人次按专家类型细分 |
| examRate | string | 预约诊察率 |
| efficiency | float | 接诊效率 |
| effectiveUnits | int | 有效出诊单元数 |
| totalUnits | int | 出诊单元总数 |
| unitDetail | object | 有效出诊单元/出诊单元按专家类型细分 |

---

## 2. 获取科室门诊量统计数据

### 接口说明
获取各科室门诊量统计表格数据，支持分页、排序、筛选。

| 项目 | 内容 |
|------|------|
| 请求方式 | GET |
| 接口地址 | /api/outpatient/department-stats |
| Content-Type | application/json |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | 否 | 当前页码，默认 1 |
| pageSize | int | 否 | 每页条数，默认 10 |
| startDate | string | 否 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 否 | 结束日期，格式 yyyy-MM-dd |
| deptName | string | 否 | 科室名称筛选 |
| sortColumn | string | 否 | 排序字段 |
| sortDirection | string | 否 | 排序方向：asc/desc |

### 响应参数

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "deptName": "心血管内科门诊",
        "visits": 357,
        "appointmentRate": "62.2%",
        "examRate": "73.6%",
        "efficiency": "27.5",
        "visitCount": 31,
        "famousExpert": 3,
        "specialExpert": 6,
        "knownExpert": 4,
        "expertA": 11,
        "expertB": 9,
        "ordinary": 4,
        "effectiveUnitsTotal": {
          "effective": 47,
          "total": 55
        },
        "unitDetail": {
          "famousExpert": { "effective": 3, "total": 4 },
          "specialExpert": { "effective": 3, "total": 4 },
          "knownExpert": { "effective": 3, "total": 4 },
          "expertA": { "effective": 3, "total": 4 },
          "expertB": { "effective": 3, "total": 4 },
          "ordinary": { "effective": 3, "total": 4 }
        }
      }
    ],
    "total": 55,
    "page": 1,
    "pageSize": 10
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| deptName | string | 科室名称 |
| visits | int | 门诊量 |
| appointmentRate | string | 预约挂号率 |
| examRate | string | 预约诊察率 |
| efficiency | string | 接诊效率 |
| visitCount | int | 出诊人次 |
| famousExpert | int | 名医专家出诊人次 |
| specialExpert | int | 特需专家出诊人次 |
| knownExpert | int | 知名专家出诊人次 |
| expertA | int | 专家A类出诊人次 |
| expertB | int | 专家B类出诊人次 |
| ordinary | int | 普通门诊出诊人次 |
| effectiveUnitsTotal | object | 有效出诊单元/出诊单元总计 |
| unitDetail | object | 各专家类型有效出诊单元/出诊单元明细 |
| total | int | 总记录数 |
| page | int | 当前页码 |
| pageSize | int | 每页条数 |

---

## 3. 导出 Excel

### 接口说明
导出当前筛选条件下的科室门诊量统计数据为 Excel 文件。

| 项目 | 内容 |
|------|------|
| 请求方式 | POST |
| 接口地址 | /api/outpatient/export |
| Content-Type | application/json |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | string | 否 | 开始日期，格式 yyyy-MM-dd |
| endDate | string | 否 | 结束日期，格式 yyyy-MM-dd |
| deptName | string | 否 | 科室名称筛选 |
| timeRange | string | 否 | 时间范围标识 |

### 请求示例

```json
{
  "startDate": "2025-09-22",
  "endDate": "2025-10-22",
  "deptName": "心血管内科",
  "timeRange": "today"
}
```

### 响应参数

#### 成功响应

```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "downloadUrl": "/api/outpatient/export/download?fileId=xxx",
    "fileName": "门诊量统计_20251022.xlsx"
  }
}
```

#### 错误响应

```json
{
  "code": 500,
  "message": "导出失败：数据量过大",
  "data": null
}
```

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null
}
```

### 响应状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |
