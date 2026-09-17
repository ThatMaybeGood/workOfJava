package com.reports.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 院领导信箱答卷解析：answer_info / topic_attrs 存的都是 JSON。
 */
public final class QuestionnaireAnswerParser {

    /** 题干"您反馈的类型（单选）"的题目ID */
    public static final String FEEDBACK_TYPE_TOPIC = "173823541780282343918";
    /** 题干"被投诉科室"（只有投诉分支有这道题） */
    public static final String DEPT_TOPIC = "281494551780282498823";
    /** 题干"被投诉人姓名"（只有投诉分支有这道题） */
    public static final String PERSON_TOPIC = "402381741780282510335";
    /** 题干"您投诉的内容"（投诉分支） */
    public static final String COMPLAINT_CONTENT_TOPIC = "766190761780282522503";
    /** 题干"您反馈的内容"（意见或建议/表扬或感谢/安全隐患分支） */
    public static final String FEEDBACK_CONTENT_TOPIC = "929872951780282402479";
    /** 题干"诉求"（投诉分支） */
    public static final String APPEAL_TOPIC = "628402491780282620624";

    /** 反馈类型取值：意见或建议 */
    public static final String TYPE_ADVICE = "46013065dea1";
    /** 反馈类型取值：表扬或感谢 */
    public static final String TYPE_PRAISE = "42f8e1a02260";
    /** 反馈类型取值：投诉 */
    public static final String TYPE_COMPLAINT = "8578dd11dc7f";
    /** 反馈类型取值：医院安全隐患（报表不展示） */
    public static final String TYPE_RISK = "1b4a6b3f9e94";

    /** 分类结果：投诉明细 */
    public static final String KIND_COMPLAINT = "complaint";
    /** 分类结果：表扬明细（表扬或感谢 + 意见或建议） */
    public static final String KIND_PRAISE = "praise";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private QuestionnaireAnswerParser() {
    }

    /**
     * answer_info 是 [{topic_id, answer}, ...]，转成 topic_id -> answer。
     * 报文为空或格式不对时返回空表，不抛异常。
     */
    public static Map<String, String> parseAnswers(String answerInfo) {
        Map<String, String> answers = new LinkedHashMap<>();
        if (answerInfo == null || answerInfo.trim().isEmpty()) {
            return answers;
        }
        try {
            JsonNode root = MAPPER.readTree(answerInfo);
            if (!root.isArray()) {
                return answers;
            }
            for (JsonNode node : root) {
                String topicId = node.path("topic_id").asText(null);
                if (topicId != null && !topicId.isEmpty()) {
                    answers.put(topicId, node.path("answer").asText(""));
                }
            }
        } catch (Exception e) {
            return answers;
        }
        return answers;
    }

    /**
     * topic_attrs 里的 answer 数组是 [{label, value, sort}, ...]，转成 value -> label。
     * 用于把选题的编码翻译成中文，报文异常时返回空表。
     */
    public static Map<String, String> parseOptions(String topicAttrs) {
        Map<String, String> options = new LinkedHashMap<>();
        if (topicAttrs == null || topicAttrs.trim().isEmpty()) {
            return options;
        }
        try {
            JsonNode answer = MAPPER.readTree(topicAttrs).path("answer");
            if (!answer.isArray()) {
                return options;
            }
            for (JsonNode node : answer) {
                String value = node.path("value").asText(null);
                if (value != null && !value.isEmpty()) {
                    options.put(value, node.path("label").asText(""));
                }
            }
        } catch (Exception e) {
            return options;
        }
        return options;
    }

    /** 取某个题目的作答；没答返回 null */
    public static String answerOf(Map<String, String> answers, String topicId) {
        String answer = answers.get(topicId);
        return answer == null || answer.isEmpty() ? null : answer;
    }

    /**
     * 按"您反馈的类型"分到投诉/表扬两类。
     * 医院安全隐患和没填的返回 null，调用方直接丢掉。
     */
    public static String classify(Map<String, String> answers) {
        String type = answerOf(answers, FEEDBACK_TYPE_TOPIC);
        if (TYPE_COMPLAINT.equals(type)) {
            return KIND_COMPLAINT;
        }
        if (TYPE_PRAISE.equals(type) || TYPE_ADVICE.equals(type)) {
            return KIND_PRAISE;
        }
        return null;
    }
}
