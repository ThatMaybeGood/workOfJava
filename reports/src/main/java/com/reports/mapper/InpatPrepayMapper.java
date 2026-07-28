package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.InpatPrepayOvEntity;
import com.reports.entity.InpatPrepayDtlEntity;
import com.reports.entity.InpatPrepayChtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 住院预交金统计 Mapper
 */
@Mapper
public interface InpatPrepayMapper extends BaseMapper<InpatPrepayOvEntity> {

    /**
     * 查询住院预交金概览
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 概览数据
     */
    InpatPrepayOvEntity queryOverview(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 查询住院预交金日明细
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param dataType  数据类型
     * @return 日明细数据
     */
    List<InpatPrepayDtlEntity> queryDetail(@Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate,
                                            @Param("dataType") String dataType);

    /**
     * 查询住院预交金图表数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param chartType 图表类型
     * @return 图表数据
     */
    List<InpatPrepayChtEntity> queryChart(@Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("chartType") String chartType);
}
