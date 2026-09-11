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
| deptName | string | 否 | 科室名称（模糊匹配） |
| deptCode | string | 否 | 科室编码（优先于 deptName） |
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
| body.overview | object | 服务质量概览（随科室过滤条件联动，按明细表实时统计） |
| body.overview.complaintCount | number | 投诉量 |
| body.overview.praiseCount | number | 表扬量 |
| body.complaint | object | 投诉明细数据 |
| body.praise | object | 表扬明细数据 |
| body.*.list | array | 明细列表 |
| body.*.total | number | 总记录数 |
| body.*.page | number | 当前页码 |
| body.*.pageSize | number | 每页条数 |

## 关联接口

### 通用字典 `reports.common.data-dict`

字典管理弹窗用，支持岗位类别（position）、投诉分类（complaintCategory）、投诉处理结果（complaintResult）、表扬方式（praiseMethod）、是否反馈科室（feedback）五类。

请求 body：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | string | 是 | query（查询）、add（新增）、delete（删除） |
| dictType | string | 是 | 字典类型 |
| dictName | string | add 必填 | 字典名称 |
| id | number | delete 必填 | 主键ID |

- query 响应：`body.list`（id/dictType/dictCode/dictName/sortNo/status）、`body.total`
- add/delete 响应：`body.affected`

### 人员字典 `reports.common.staff-dict`

数据维护弹窗人员下拉用。

请求 body：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptCode | string | 否 | 按科室过滤 |
| staffName | string | 否 | 姓名模糊匹配 |

响应：`body.list`（id/staffCode/staffName/deptCode/deptName/position）、`body.total`

### 数据维护 `reports.outp.service-quality-maintain`

数据维护弹窗用，投诉/表扬明细的查询、保存、删除。

请求 body：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | string | 是 | query（查询）、save（保存）、delete（删除） |
| type | string | 是 | complaint（投诉）、praise（表扬） |
| startDate | string | query 必填 | 开始日期 yyyy-MM-dd |
| endDate | string | query 必填 | 结束日期 yyyy-MM-dd |
| page | number | 否 | 当前页码 |
| pageSize | number | 否 | 每页条数 |
| id | number | delete 必填 | 主键ID |
| list | array | save 必填 | 明细列表，id 为空表示新增 |

list 项字段：id、time（yyyy-MM-dd HH:mm）、deptCode、deptName、personName、position、category、result（投诉）/ method、feedback（表扬）、remark。

- query 响应：`body.list`、`body.total`、`body.page`、`body.pageSize`
- save/delete 响应：`body.affected`

## 依赖表结构

见 `docs/report_tables.sql`：

- `tr_svc_quality_cmpl` / `tr_svc_quality_prz`：投诉/表扬明细表，需 `dept_code` 列关联 `TR_DEPT_DICT`
- `tr_common_dict`：通用字典表
- `tr_staff_dict`：人员字典表
