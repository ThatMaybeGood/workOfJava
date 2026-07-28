package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.QualityControlOvEntity;
import com.reports.entity.QualityControlDtlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊质量控制 Mapper
 */
@Mapper
public interface QualityControlMapper extends BaseMapper<QualityControlOvEntity> {

    /**
     * 查询门诊质量控制概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 质量控制概览数据
     */
    QualityControlOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询门诊质量控制月度明细
     *
     * @param startMonth 开始月份
     * @param endMonth   结束月份
     * @return 月度明细数据列表
     */
    List<QualityControlDtlEntity> queryMonthlyDetail(@Param("startMonth") String startMonth,
                                                      @Param("endMonth") String endMonth);
}
