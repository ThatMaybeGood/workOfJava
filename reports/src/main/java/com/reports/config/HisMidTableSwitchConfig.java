package com.reports.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 直查HIS报表的中间表开关
 * false(默认)=直查HIS, 同现有逻辑; true=查报表库中间表(需先在HIS建SP按日抽数)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "reports.his-mid-table")
public class HisMidTableSwitchConfig {

    /**
     * 出院结算人次统计: true查报表库中间表tr_disch_settle_cnt_d
     */
    private boolean dischSettleCnt = false;

    /**
     * 院前转住院统计: true查tr_prehosp_trans_d
     */
    private boolean preHospTrans = false;
}
