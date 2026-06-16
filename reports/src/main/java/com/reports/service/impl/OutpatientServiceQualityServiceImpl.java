package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.common.PageResult;
import com.reports.dto.request.OutpatientServiceQualityRequest;
import com.reports.dto.response.outpatient.service.quality.*;
import com.reports.service.OutpatientServiceQualityService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 门诊服务质量分析服务实现
 */
@Slf4j
@Service
public class OutpatientServiceQualityServiceImpl implements OutpatientServiceQualityService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientServiceQualityServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OverviewData queryOverview(OutpatientServiceQualityRequest request) {
        log.info("查询门诊服务质量概览数据，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryOverviewMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryOverviewByJdbc(request);
        } else {
            return queryOverviewByMybatisPlus(request);
        }
    }

    @Override
    public PageResult<ComplaintItem> queryComplaintList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        log.info("查询投诉明细列表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryComplaintListMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryComplaintListByJdbc(request, page, pageSize);
        } else {
            return queryComplaintListByMybatisPlus(request, page, pageSize);
        }
    }

    @Override
    public PageResult<PraiseItem> queryPraiseList(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        log.info("查询表扬明细列表，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryPraiseListMock(request, page, pageSize);
        } else if (dataConfig.isJdbc()) {
            return queryPraiseListByJdbc(request, page, pageSize);
        } else {
            return queryPraiseListByMybatisPlus(request, page, pageSize);
        }
    }

    // ==================== Mock 模式 ====================

    private OverviewData queryOverviewMock(OutpatientServiceQualityRequest request) {
        SeqUtil.next();
        OverviewData overview = new OverviewData();
        overview.setComplaintCount(12);
        overview.setPraiseCount(56);
        return overview;
    }

    private PageResult<ComplaintItem> queryComplaintListMock(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<ComplaintItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            ComplaintItem item = new ComplaintItem();
            item.setTime("2024-01-15 10:00");
            item.setDept("心血管内科" + (i + 1));
            item.setPerson("张医生" + (i + 1));
            item.setPosition("主治医师");
            item.setCategory("服务态度");
            item.setResult("已处理");
            item.setRemark("等待时间过长");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    private PageResult<PraiseItem> queryPraiseListMock(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        SeqUtil.next();
        List<PraiseItem> list = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            PraiseItem item = new PraiseItem();
            item.setTime("2024-01-15 11:00");
            item.setDept("心血管内科" + (i + 1));
            item.setPerson("张医生" + (i + 1));
            item.setPosition("主治医师");
            item.setMethod("书面表扬");
            item.setFeedback("医生非常耐心细致");
            item.setRemark("");
            list.add(item);
        }
        return PageResult.of(list, 55L, page, pageSize);
    }

    // ==================== JdbcTemplate 模式 ====================

    private OverviewData queryOverviewByJdbc(OutpatientServiceQualityRequest request) {
        log.info("使用 JdbcTemplate 查询概览数据");
        return queryOverviewMock(request);
    }

    private PageResult<ComplaintItem> queryComplaintListByJdbc(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        return queryComplaintListMock(request, page, pageSize);
    }

    private PageResult<PraiseItem> queryPraiseListByJdbc(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        return queryPraiseListMock(request, page, pageSize);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private OverviewData queryOverviewByMybatisPlus(OutpatientServiceQualityRequest request) {
        return queryOverviewMock(request);
    }

    private PageResult<ComplaintItem> queryComplaintListByMybatisPlus(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        return queryComplaintListMock(request, page, pageSize);
    }

    private PageResult<PraiseItem> queryPraiseListByMybatisPlus(OutpatientServiceQualityRequest request, Integer page, Integer pageSize) {
        return queryPraiseListMock(request, page, pageSize);
    }

}
