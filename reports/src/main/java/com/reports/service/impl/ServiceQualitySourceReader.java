package com.reports.service.impl;

import com.reports.annotation.DataSource;
import com.reports.dto.source.QuestionAnswerRow;
import com.reports.dto.source.SourceFeedback;
import com.reports.mapper.ServiceQualitySourceMapper;
import com.reports.util.QuestionnaireAnswerParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 门诊服务质量分析-源库读取
 * <p>
 * 单独一个 bean 挂 {@code @DataSource("yq_powersfp")}：切库是方法级 AOP，
 * 和读 master 库人工补充字段的逻辑必须分在两个 bean 里，否则切不过来。
 */
@Slf4j
@Service
@DataSource("yq_powersfp")
public class ServiceQualitySourceReader {

    private final ServiceQualitySourceMapper sourceMapper;

    @Autowired
    public ServiceQualitySourceReader(ServiceQualitySourceMapper sourceMapper) {
        this.sourceMapper = sourceMapper;
    }

    /**
     * 取区间内的投诉/表扬记录。医院安全隐患和分类缺失的直接丢掉。
     *
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     * @return 解析后的记录，按时间倒序
     */
    public List<SourceFeedback> queryFeedback(Date startDate, Date endDate) {
        List<SourceFeedback> result = new ArrayList<>();
        List<QuestionAnswerRow> rows;
        try {
            rows = sourceMapper.queryAnswers(startDate, endDate);
        } catch (Exception e) {
            log.warn("读取投诉表扬源库失败", e);
            return result;
        }
        for (QuestionAnswerRow row : rows) {
            SourceFeedback feedback = toFeedback(row);
            if (feedback != null) {
                result.add(feedback);
            }
        }
        return result;
    }

    /** 解析一行答卷；分类为空的（安全隐患/没填类型）返回 null */
    private static SourceFeedback toFeedback(QuestionAnswerRow row) {
        Map<String, String> answers = QuestionnaireAnswerParser.parseAnswers(row.getAnswerInfo());
        String kind = QuestionnaireAnswerParser.classify(answers);
        if (kind == null) {
            return null;
        }
        SourceFeedback feedback = new SourceFeedback();
        feedback.setSourceId(row.getRecordId());
        feedback.setTime(row.getCreateTime());
        feedback.setKind(kind);
        feedback.setDeptName(QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.DEPT_TOPIC));
        feedback.setPersonName(QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.PERSON_TOPIC));
        feedback.setAppeal(QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.APPEAL_TOPIC));
        // 投诉分支的正文在"您投诉的内容"，其余分支在"您反馈的内容"，两者只会有一个
        String content = QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.COMPLAINT_CONTENT_TOPIC);
        if (content == null) {
            content = QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.FEEDBACK_CONTENT_TOPIC);
        }
        feedback.setContent(content);
        feedback.setResult(row.getDisposeResult());
        return feedback;
    }
}
