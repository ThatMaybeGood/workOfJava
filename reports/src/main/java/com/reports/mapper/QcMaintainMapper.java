package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.QcMaintainEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门诊质控指标人工维护 Mapper
 */
@Mapper
public interface QcMaintainMapper extends BaseMapper<QcMaintainEntity> {

    /**
     * 按月份查询维护记录
     *
     * @param statMonth 统计月份(YYYY-MM)
     * @return 维护记录列表
     */
    List<QcMaintainEntity> queryByMonth(@Param("statMonth") String statMonth);

    /**
     * 按月份区间查询维护记录（报表覆盖用）
     *
     * @param startMonth 开始月份(YYYY-MM)
     * @param endMonth   结束月份(YYYY-MM)
     * @return 维护记录列表
     */
    List<QcMaintainEntity> queryByRange(@Param("startMonth") String startMonth,
                                        @Param("endMonth") String endMonth);
}
