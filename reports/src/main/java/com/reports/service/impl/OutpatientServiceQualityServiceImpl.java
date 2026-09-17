package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientServiceQualityRequest;
import com.reports.dto.request.ServiceQualityMaintainRequest;
import com.reports.dto.response.outpatient.service.quality.*;
import com.reports.dto.source.SourceFeedback;
import com.reports.entity.ServiceQualityCmplEntity;
import com.reports.entity.ServiceQualityPrzEntity;
import com.reports.mapper.ServiceQualityMapper;
import com.reports.service.OutpatientServiceQualityService;
import com.reports.util.QuestionnaireAnswerParser;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 门诊服务质量分析服务实现
 * <p>
 * 明细是两个来源拼起来的，界面上用"来源"列区分：
 * <ul>
 *   <li><b>院领导信箱</b>：直连源库 yq_powersfp 查出来的答卷，只读，不落库；</li>
 *   <li><b>人工登记</b>：master 库 TR_SVC_QUALITY_CMPL / TR_SVC_QUALITY_PRZ，
 *       由「数据维护」弹窗增删改。</li>
 * </ul>
 * 两边互不关联，各列各的行，两边都展示。
 */
@Slf4j
@Service
public class OutpatientServiceQualityServiceImpl implements OutpatientServiceQualityService {

    /** 来源：源库答卷 */
    private static final String SOURCE_MAILBOX = "院领导信箱";
    /** 来源：人工登记表 */
    private static final String SOURCE_MANUAL = "人工登记";

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;
    private final ServiceQualitySourceReader sourceReader;
    private final ServiceQualityMapper serviceQualityMapper;

    /** 同一次请求内复用源库结果，见 {@link #cachedSource} */
    private final ThreadLocal<CachedSource> cachedSources = new ThreadLocal<>();

    @Autowired
    public OutpatientServiceQualityServiceImpl(ReportDataConfig dataConfig,
                                               JdbcTemplate jdbcTemplate,
                                               ServiceQualitySourceReader sourceReader,
                                               ServiceQualityMapper serviceQualityMapper) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
        this.sourceReader = sourceReader;
        this.serviceQualityMapper = serviceQualityMapper;
    }

    @Override
    public OverviewData queryOverview(OutpatientServiceQualityRequest request) {
        log.info("查询门诊服务质量概览数据，mode={}", dataConfig.getMode());
        OverviewData overview = new OverviewData();
        if (dataConfig.isMock()) {
            overview.setComplaintCount(12);
            overview.setPraiseCount(56);
            return overview;
        }
        overview.setComplaintCount(complaintItems(request).size());
        overview.setPraiseCount(praiseItems(request).size());
        return overview;
    }

    @Override
    public PageResult<ComplaintItem> queryComplaintList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        log.info("查询投诉明细列表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryComplaintListMock(page, pageSize);
        }
        List<ComplaintItem> all = complaintItems(request);
        return PageResult.of(pageOf(all, page, pageSize), (long) all.size(), page, pageSize);
    }

    @Override
    public PageResult<PraiseItem> queryPraiseList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        log.info("查询表扬明细列表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryPraiseListMock(page, pageSize);
        }
        List<PraiseItem> all = praiseItems(request);
        return PageResult.of(pageOf(all, page, pageSize), (long) all.size(), page, pageSize);
    }

    // ==================== 数据维护（只管人工登记表） ====================

    @Override
    public PageResult<MaintainItem> queryMaintainList(ServiceQualityMaintainRequest request, Integer page, Integer pageSize) {
        log.info("查询门诊服务质量人工登记，type={}，mode={}", request.getType(), dataConfig.getMode());
        if (dataConfig.isMock()) {
            return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
        }
        List<MaintainItem> all = new ArrayList<>();
        try {
            if (isPraise(request.getType())) {
                for (ServiceQualityPrzEntity row
                        : serviceQualityMapper.queryPraiseByDate(request.getStartDate(), request.getEndDate())) {
                    all.add(buildMaintainItem(row));
                }
            } else {
                for (ServiceQualityCmplEntity row
                        : serviceQualityMapper.queryComplaintByDate(request.getStartDate(), request.getEndDate())) {
                    all.add(buildMaintainItem(row));
                }
            }
        } catch (Exception e) {
            log.warn("查询门诊服务质量人工登记失败", e);
        }
        return PageResult.of(pageOf(all, page, pageSize), (long) all.size(), page, pageSize);
    }

    @Override
    public int saveMaintain(ServiceQualityMaintainRequest request) {
        log.info("保存门诊服务质量人工登记，type={}，mode={}", request.getType(), dataConfig.getMode());
        if (request.getList() == null || request.getList().isEmpty()) {
            return 0;
        }
        boolean praise = isPraise(request.getType());
        int affected = 0;
        for (MaintainItem item : request.getList()) {
            if (praise) {
                ServiceQualityPrzEntity entity = new ServiceQualityPrzEntity();
                entity.setId(item.getId());
                entity.setPraiseTime(item.getTime());
                entity.setDeptCode(item.getDeptCode());
                entity.setDeptName(item.getDeptName());
                entity.setPersonName(item.getPersonName());
                entity.setPosition(item.getPosition());
                entity.setMethod(item.getMethod());
                entity.setFeedback(item.getFeedback());
                entity.setRemark(item.getRemark());
                affected += (item.getId() != null)
                        ? serviceQualityMapper.updatePraise(entity)
                        : serviceQualityMapper.insertPraise(entity);
            } else {
                ServiceQualityCmplEntity entity = new ServiceQualityCmplEntity();
                entity.setId(item.getId());
                entity.setComplaintTime(item.getTime());
                entity.setDeptCode(item.getDeptCode());
                entity.setDeptName(item.getDeptName());
                entity.setPersonName(item.getPersonName());
                entity.setPosition(item.getPosition());
                entity.setCategory(item.getCategory());
                entity.setResult(item.getResult());
                entity.setRemark(item.getRemark());
                affected += (item.getId() != null)
                        ? serviceQualityMapper.updateComplaint(entity)
                        : serviceQualityMapper.insertComplaint(entity);
            }
        }
        return affected;
    }

    @Override
    public int deleteMaintain(ServiceQualityMaintainRequest request) {
        log.info("删除门诊服务质量人工登记，type={}，id={}，mode={}", request.getType(), request.getId(), dataConfig.getMode());
        if (request.getId() == null) {
            return 0;
        }
        return isPraise(request.getType())
                ? serviceQualityMapper.deletePraiseById(request.getId())
                : serviceQualityMapper.deleteComplaintById(request.getId());
    }

    // ==================== 两个来源拼接 ====================

    /** 投诉明细 = 院领导信箱（源库） + 人工登记，按时间倒序 */
    private List<ComplaintItem> complaintItems(OutpatientServiceQualityRequest request) {
        List<ComplaintItem> all = new ArrayList<>();
        for (SourceFeedback row : sourceRows(request, QuestionnaireAnswerParser.KIND_COMPLAINT)) {
            ComplaintItem item = new ComplaintItem();
            item.setSource(SOURCE_MAILBOX);
            item.setTime(formatTime(row.getTime()));
            item.setDept(row.getDeptName());
            item.setPerson(row.getPersonName());
            item.setResult(row.getResult());
            item.setContent(row.getContent());
            item.setAppeal(row.getAppeal());
            all.add(item);
        }
        try {
            for (ServiceQualityCmplEntity row
                    : serviceQualityMapper.listComplaint(request.getStartDate(), request.getEndDate())) {
                ComplaintItem item = new ComplaintItem();
                item.setSource(SOURCE_MANUAL);
                item.setTime(formatTime(row.getComplaintTime()));
                item.setDept(row.getDeptName());
                item.setPerson(row.getPersonName());
                item.setPosition(row.getPosition());
                item.setCategory(row.getCategory());
                item.setResult(row.getResult());
                item.setRemark(row.getRemark());
                all.add(item);
            }
        } catch (Exception e) {
            log.warn("查询人工登记的投诉记录失败", e);
        }
        all.sort(Comparator.comparing(ComplaintItem::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return all;
    }

    /** 表扬明细 = 院领导信箱（源库） + 人工登记，按时间倒序 */
    private List<PraiseItem> praiseItems(OutpatientServiceQualityRequest request) {
        List<PraiseItem> all = new ArrayList<>();
        for (SourceFeedback row : sourceRows(request, QuestionnaireAnswerParser.KIND_PRAISE)) {
            PraiseItem item = new PraiseItem();
            item.setSource(SOURCE_MAILBOX);
            item.setTime(formatTime(row.getTime()));
            item.setDept(row.getDeptName());
            item.setPerson(row.getPersonName());
            item.setContent(row.getContent());
            all.add(item);
        }
        try {
            for (ServiceQualityPrzEntity row
                    : serviceQualityMapper.listPraise(request.getStartDate(), request.getEndDate())) {
                PraiseItem item = new PraiseItem();
                item.setSource(SOURCE_MANUAL);
                item.setTime(formatTime(row.getPraiseTime()));
                item.setDept(row.getDeptName());
                item.setPerson(row.getPersonName());
                item.setPosition(row.getPosition());
                item.setMethod(row.getMethod());
                item.setFeedback(row.getFeedback());
                item.setRemark(row.getRemark());
                all.add(item);
            }
        } catch (Exception e) {
            log.warn("查询人工登记的表扬记录失败", e);
        }
        all.sort(Comparator.comparing(PraiseItem::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return all;
    }

    /** 源库记录，按科室过滤（科室是源里的自由文本，按请求里选的字典名匹配，对不上就查不到） */
    private List<SourceFeedback> sourceRows(OutpatientServiceQualityRequest request, String kind) {
        List<SourceFeedback> result = new ArrayList<>();
        if (dataConfig.isMock()) {
            return result;
        }
        String deptName = request.getDeptName();
        for (SourceFeedback feedback : cachedSource(request)) {
            if (!kind.equals(feedback.getKind())) {
                continue;
            }
            if (deptName == null || deptName.isEmpty()
                    || (feedback.getDeptName() != null && feedback.getDeptName().contains(deptName))) {
                result.add(feedback);
            }
        }
        return result;
    }

    /**
     * 一次请求里概览、投诉明细、表扬明细都要这份源数据，按区间缓存住，别把源库查三遍。
     * key 变了就重查，所以不会拿到别的区间的旧数据。
     */
    private List<SourceFeedback> cachedSource(OutpatientServiceQualityRequest request) {
        String key = request.getStartDate() + "~" + request.getEndDate();
        CachedSource cached = cachedSources.get();
        if (cached == null || !key.equals(cached.key)) {
            cached = new CachedSource(key, sourceReader.queryFeedback(request.getStartDate(), request.getEndDate()));
            cachedSources.set(cached);
        }
        return cached.rows;
    }

    private static final class CachedSource {
        private final String key;
        private final List<SourceFeedback> rows;

        private CachedSource(String key, List<SourceFeedback> rows) {
            this.key = key;
            this.rows = rows;
        }
    }

    // ==================== entity -> DTO 转换方法 ====================

    private static MaintainItem buildMaintainItem(ServiceQualityCmplEntity entity) {
        MaintainItem item = new MaintainItem();
        item.setId(entity.getId());
        item.setTime(entity.getComplaintTime());
        item.setDeptCode(entity.getDeptCode());
        item.setDeptName(entity.getDeptName());
        item.setPersonName(entity.getPersonName());
        item.setPosition(entity.getPosition());
        item.setCategory(entity.getCategory());
        item.setResult(entity.getResult());
        item.setRemark(entity.getRemark());
        return item;
    }

    private static MaintainItem buildMaintainItem(ServiceQualityPrzEntity entity) {
        MaintainItem item = new MaintainItem();
        item.setId(entity.getId());
        item.setTime(entity.getPraiseTime());
        item.setDeptCode(entity.getDeptCode());
        item.setDeptName(entity.getDeptName());
        item.setPersonName(entity.getPersonName());
        item.setPosition(entity.getPosition());
        item.setMethod(entity.getMethod());
        item.setFeedback(entity.getFeedback());
        item.setRemark(entity.getRemark());
        return item;
    }

    static <T> List<T> pageOf(List<T> all, Integer page, Integer pageSize) {
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        return start < all.size() ? all.subList(start, end) : new ArrayList<>();
    }

    private static String formatTime(Date time) {
        return time == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
    }

    private static boolean isPraise(String type) {
        return "praise".equalsIgnoreCase(type);
    }

    // ==================== Mock 模式 ====================

    private PageResult<ComplaintItem> queryComplaintListMock(Integer page, Integer pageSize) {
        SeqUtil.next();
        List<ComplaintItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            ComplaintItem item = new ComplaintItem();
            item.setSource(i % 2 == 0 ? SOURCE_MAILBOX : SOURCE_MANUAL);
            item.setTime("2024-01-15 10:00");
            item.setDept("心血管内科" + (i + 1));
            item.setPerson("张医生" + (i + 1));
            item.setPosition("主治医师");
            item.setCategory("服务态度");
            item.setResult("已处理");
            item.setContent("等待时间过长");
            item.setRemark("等待时间过长");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private PageResult<PraiseItem> queryPraiseListMock(Integer page, Integer pageSize) {
        SeqUtil.next();
        List<PraiseItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            PraiseItem item = new PraiseItem();
            item.setSource(i % 2 == 0 ? SOURCE_MAILBOX : SOURCE_MANUAL);
            item.setTime("2024-01-15 11:00");
            item.setDept("心血管内科" + (i + 1));
            item.setPerson("张医生" + (i + 1));
            item.setPosition("主治医师");
            item.setMethod("书面表扬");
            item.setFeedback("已反馈");
            item.setContent("医生非常耐心细致");
            item.setRemark("");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }
}
