package com.reports.mapper;

import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出院结算人次统计中间表 Mapper（报表库 tr_disch_settle_cnt_d / tr_disch_acct_inv_d）
 * 口径同 DischSettleCntMapper(HIS直查), 方法签名一致
 */
@Mapper
public interface DischSettleCntMidMapper {

    /** 按费别汇总：日期、费别、结算类别、收据量、人次 */
    List<PersonCountItem> queryByFeeChannel(@Param("startDate") java.sql.Date startDate,
                                            @Param("endDate") java.sql.Date endDate,
                                            @Param("timeDimension") String timeDimension);

    /** 按操作员：日期、费别、结算类别、操作员工号/姓名、收据量、人次 */
    List<PersonCountItem> queryByOperator(@Param("startDate") java.sql.Date startDate,
                                          @Param("endDate") java.sql.Date endDate,
                                          @Param("timeDimension") String timeDimension);

    /** 按支付类别（仅普通患者）：日期、结算类别、支付类别、收据量、人次 */
    List<PersonCountItem> queryByPayType(@Param("startDate") java.sql.Date startDate,
                                         @Param("endDate") java.sql.Date endDate,
                                         @Param("timeDimension") String timeDimension);
}
