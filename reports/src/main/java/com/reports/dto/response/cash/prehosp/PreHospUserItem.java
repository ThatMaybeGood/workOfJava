package com.reports.dto.response.cash.prehosp;

import lombok.Data;

/**
 * HIS users行
 */
@Data
public class PreHospUserItem {

    /** 操作员=USER_ID(统计查询按此过滤) */
    private String operNo;

    /** 登录ID=DB_USER(维护页显示) */
    private String operDbuser;

    /** 操作员姓名=RAWTOHEX(USER_NAME), 待Java按GBK解码 */
    private String operNameHex;
}
