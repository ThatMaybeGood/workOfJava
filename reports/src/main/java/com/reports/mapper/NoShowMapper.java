package com.reports.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.reports.entity.NoShowOvEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 爽约退号分析 Mapper
 */
@Mapper
public interface NoShowMapper extends BaseMapper<NoShowOvEntity> {

    /**
     * 爽约退号全度量: 就诊日×科室出全部35项度量, 源表直查(口径同原宽表), 服务层10min缓存
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return (statDay, deptCode, totalGuahao, tuiHaoShu, shuangYueShu, 属地/渠道/年龄...) 行
     */
    List<Map<String, Object>> queryMeasureRange(@Param("startDate") java.sql.Date startDate,
                                                @Param("endDate") java.sql.Date endDate);

    /** 科室字典: dept_code→dept_name(科室表INNER JOIN语义, 无字典的科室不展示) */
    List<Map<String, Object>> queryDeptDict();
}
