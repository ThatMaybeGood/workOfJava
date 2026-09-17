package com.reports.mapper;

import com.reports.dto.source.QuestionAnswerRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊服务质量分析-源库(投诉表扬库) Mapper
 * <p>
 * 由 {@link com.reports.service.impl.ServiceQualitySourceReader} 经
 * {@code @DataSource("yq_powersfp")} 切库后调用，方法上不要再加注解。
 */
@Mapper
public interface ServiceQualitySourceMapper {

    /**
     * 按答卷创建时间捞一段区间的答卷
     *
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     * @return 答卷原始行
     */
    List<QuestionAnswerRow> queryAnswers(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
