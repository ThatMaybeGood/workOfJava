package com.reports.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 门诊运行数据 Mapper
 *
 * 注：当前使用 Mock 数据，后续可在此编写个性化 SQL
 */
@Mapper
public interface OutpatientOperationMapper {

    /**
     * 示例：查询门诊统计数据（Oracle 分页）
     * 可根据实际需求编写个性化 SQL
     */
    @Select("SELECT * FROM ( SELECT ROWNUM AS rn, t.* FROM ( SELECT DEPT_NAME, COUNT(*) AS VISITS FROM OUTPATIENT_RECORD WHERE VISIT_DATE BETWEEN #{startDate} AND #{endDate} GROUP BY DEPT_NAME ORDER BY COUNT(*) DESC ) t WHERE ROWNUM <= #{offset} + #{pageSize} ) WHERE rn > #{offset}")
    List<Map<String, Object>> queryOutpatientStats(Map<String, Object> params);

    /**
     * 示例：查询总记录数
     */
    @Select("SELECT COUNT(DISTINCT DEPT_NAME) FROM OUTPATIENT_RECORD WHERE VISIT_DATE BETWEEN #{startDate} AND #{endDate}")
    Long queryOutpatientStatsCount(Map<String, Object> params);

}
