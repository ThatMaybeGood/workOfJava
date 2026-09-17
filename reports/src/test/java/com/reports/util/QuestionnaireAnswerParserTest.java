package com.reports.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用院领导信箱的真实答卷样例验分类和取值。
 */
class QuestionnaireAnswerParserTest {

    /** 投诉样例：反馈类型=投诉，多了科室/被投诉人/投诉内容/诉求几道题 */
    private static final String COMPLAINT = "[{"
            + "\"topic_id\": \"631475191780282225112\", \"answer\": \"3090a031c554\"}, {"
            + "\"topic_id\": \"379350901780282273703\", \"answer\": \"ae732ebad3cb\"}, {"
            + "\"topic_id\": \"173823541780282343918\", \"answer\": \"8578dd11dc7f\"}, {"
            + "\"topic_id\": \"633117601780282478502\", \"answer\": \"保密\"}, {"
            + "\"topic_id\": \"516458421780282490182\", \"answer\": \"保密\"}, {"
            + "\"topic_id\": \"281494551780282498823\", \"answer\": \"免疫门诊肿瘤科\"}, {"
            + "\"topic_id\": \"402381741780282510335\", \"answer\": \"朱波\"}, {"
            + "\"topic_id\": \"766190761780282522503\", \"answer\": \"接诊态度傲慢\"}, {"
            + "\"topic_id\": \"293493861780282586806\", \"answer\": \"[]\"}, {"
            + "\"topic_id\": \"628402491780282620624\", \"answer\": \"要求批评教育\"}, {"
            + "\"topic_id\": \"92928151780282447118\", \"answer\": \"5e6fa2663f18\"}]";

    /** 表扬样例：反馈类型=表扬或感谢，没有科室/被投诉人这两道题 */
    private static final String PRAISE = "[{"
            + "\"topic_id\": \"631475191780282225112\", \"answer\": \"3090a031c554\"}, {"
            + "\"topic_id\": \"379350901780282273703\", \"answer\": \"ae732ebad3cb\"}, {"
            + "\"topic_id\": \"173823541780282343918\", \"answer\": \"42f8e1a02260\"}, {"
            + "\"topic_id\": \"929872951780282402479\", \"answer\": \"我要表扬心内科刘曦医生\"}, {"
            + "\"topic_id\": \"293493861780282586806\", \"answer\": \"[]\"}, {"
            + "\"topic_id\": \"92928151780282447118\", \"answer\": \"5e6fa2663f18\"}]";

    @Test
    void complaintIsClassifiedAsComplaint() {
        Map<String, String> answers = QuestionnaireAnswerParser.parseAnswers(COMPLAINT);

        assertEquals(QuestionnaireAnswerParser.KIND_COMPLAINT, QuestionnaireAnswerParser.classify(answers));
        assertEquals("免疫门诊肿瘤科", QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.DEPT_TOPIC));
        assertEquals("朱波", QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.PERSON_TOPIC));
        assertEquals("接诊态度傲慢",
                QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.COMPLAINT_CONTENT_TOPIC));
        assertEquals("要求批评教育", QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.APPEAL_TOPIC));
    }

    @Test
    void praiseIsClassifiedAsPraise() {
        Map<String, String> answers = QuestionnaireAnswerParser.parseAnswers(PRAISE);

        assertEquals(QuestionnaireAnswerParser.KIND_PRAISE, QuestionnaireAnswerParser.classify(answers));
        assertEquals("我要表扬心内科刘曦医生",
                QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.FEEDBACK_CONTENT_TOPIC));
        // 表扬分支表单里就没有这两道题，取不到是正常的
        assertNull(QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.DEPT_TOPIC));
        assertNull(QuestionnaireAnswerParser.answerOf(answers, QuestionnaireAnswerParser.PERSON_TOPIC));
    }

    /** 意见或建议也归到表扬明细 */
    @Test
    void adviceIsClassifiedAsPraise() {
        String advice = "[{\"topic_id\": \"173823541780282343918\", \"answer\": \"46013065dea1\"}]";
        assertEquals(QuestionnaireAnswerParser.KIND_PRAISE,
                QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers(advice)));
    }

    /** 医院安全隐患不展示 */
    @Test
    void safetyRiskIsDropped() {
        String risk = "[{\"topic_id\": \"173823541780282343918\", \"answer\": \"1b4a6b3f9e94\"}]";
        assertNull(QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers(risk)));
    }

    @Test
    void unknownOrMissingTypeIsDropped() {
        String unknown = "[{\"topic_id\": \"173823541780282343918\", \"answer\": \"whatever\"}]";
        assertNull(QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers(unknown)));
        assertNull(QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers("[]")));
        assertNull(QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers(null)));
    }

    /** 报文坏了不能把整张报表带崩 */
    @Test
    void brokenJsonYieldsEmptyAnswers() {
        assertTrue(QuestionnaireAnswerParser.parseAnswers("{不是数组").isEmpty());
        assertTrue(QuestionnaireAnswerParser.parseAnswers("").isEmpty());
        assertNull(QuestionnaireAnswerParser.classify(QuestionnaireAnswerParser.parseAnswers("null")));
    }

    /** 选题的编码 -> 中文，取自 topic_attrs */
    @Test
    void optionsMapValueToLabel() {
        String attrs = "{\"required\":\"false\",\"answer\":["
                + "{\"label\":\"意见或建议\",\"value\":\"46013065dea1\",\"sort\":1},"
                + "{\"label\":\"表扬或感谢\",\"value\":\"42f8e1a02260\",\"sort\":2},"
                + "{\"label\":\"投诉\",\"value\":\"8578dd11dc7f\",\"sort\":3},"
                + "{\"label\":\"医院安全隐患\",\"value\":\"1b4a6b3f9e94\",\"sort\":4}]}";

        Map<String, String> options = QuestionnaireAnswerParser.parseOptions(attrs);

        assertEquals(4, options.size());
        assertEquals("投诉", options.get(QuestionnaireAnswerParser.TYPE_COMPLAINT));
        assertEquals("表扬或感谢", options.get(QuestionnaireAnswerParser.TYPE_PRAISE));
        assertTrue(QuestionnaireAnswerParser.parseOptions("坏报文").isEmpty());
    }
}
