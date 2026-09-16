package com.reports.mapper;

import com.reports.dto.response.cash.discharge.settlement.PersonCountItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 出院结算人次统计 Mapper（直查 HIS 库 inp_settle_master）
 */
@Mapper
public interface DischSettleCntMapper {

    /**
     * 按费别汇总：日期、费别、结算类别、人次
     *
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param timeDimension 时间粒度(day/month)
     * @return 人次统计行
     */
    List<PersonCountItem> queryByFeeChannel(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            @Param("timeDimension") String timeDimension);

    /**
     * 按操作员：日期、费别、结算类别、操作员工号/姓名、人次
     *
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param timeDimension 时间粒度(day/month)
     * @return 人次统计行
     */
    List<PersonCountItem> queryByOperator(@Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate,
                                          @Param("timeDimension") String timeDimension);

    /**
     * 按支付类别（仅普通患者）：日期、结算类别、支付类别、人次
     *
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param timeDimension 时间粒度(day/month)
     * @return 人次统计行
     */
    List<PersonCountItem> queryByPayType(@Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate,
                                         @Param("timeDimension") String timeDimension);
}
