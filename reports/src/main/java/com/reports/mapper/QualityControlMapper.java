package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.dto.response.outpatient.quality.control.TableItem;
import com.reports.entity.QualityControlDtlEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 门诊质量控制 Mapper
 */
@Mapper
public interface QualityControlMapper extends BaseMapper<QualityControlDtlEntity> {

    /**
     * 按月份区间查询报表数据（一月一行，比率由SQL按分子分母现算）
     *
     * @param startMonth 开始月份
     * @param endMonth   结束月份
     * @return 月度数据列表
     */
    List<TableItem> queryMonthly(@Param("startMonth") String startMonth,
                                 @Param("endMonth") String endMonth);

    /**
     * 按月份查询单行（数据维护弹窗预填用）
     *
     * @param statMonth 统计月份
     * @return 该月数据，没有返回 null
     */
    QualityControlDtlEntity queryByMonth(@Param("statMonth") String statMonth);

    /**
     * 人工维护：该月有行就补写填了的分子分母，没有就新建一行
     *
     * @param entity 待写入的分子分母（未填的字段为 null，保持原值）
     * @return 影响行数
     */
    int mergeMaintain(QualityControlDtlEntity entity);
}
