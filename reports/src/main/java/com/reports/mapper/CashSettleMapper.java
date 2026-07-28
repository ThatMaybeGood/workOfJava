package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.CashSettleOvEntity;
import com.reports.entity.CashSettleDtlEntity;
import com.reports.entity.CashSettleChtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 收费员结账统计 Mapper
 */
@Mapper
public interface CashSettleMapper extends BaseMapper<CashSettleOvEntity> {

    /**
     * 查询收费员结账概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    CashSettleOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询收费员结账日明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param itemType  项目类型
     * @return 日明细数据
     */
    List<CashSettleDtlEntity> queryDetail(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("itemType") String itemType);

    /**
     * 查询收费员结账图表数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 图表数据
     */
    List<CashSettleChtEntity> queryChart(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
