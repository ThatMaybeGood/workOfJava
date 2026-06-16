package com.reports.service.impl;

import com.reports.config.ReportDataConfig;
import com.reports.dto.request.OutpatientPatientPortraitRequest;
import com.reports.dto.response.outpatient.patient.portrait.*;
import com.reports.service.OutpatientPatientPortraitService;
import com.reports.util.SeqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 患者画像服务实现
 */
@Slf4j
@Service
public class OutpatientPatientPortraitServiceImpl implements OutpatientPatientPortraitService {

    private final ReportDataConfig dataConfig;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OutpatientPatientPortraitServiceImpl(ReportDataConfig dataConfig, JdbcTemplate jdbcTemplate) {
        this.dataConfig = dataConfig;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AgeAnalysis queryAgeAnalysis(OutpatientPatientPortraitRequest request) {
        log.info("查询患者画像年龄分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryAgeAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryAgeAnalysisByJdbc(request);
        } else {
            return queryAgeAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryInsuranceAnalysis(OutpatientPatientPortraitRequest request) {
        log.info("查询患者画像医保分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryInsuranceAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryInsuranceAnalysisByJdbc(request);
        } else {
            return queryInsuranceAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryIdentityAnalysis(OutpatientPatientPortraitRequest request) {
        log.info("查询患者画像身份分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryIdentityAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryIdentityAnalysisByJdbc(request);
        } else {
            return queryIdentityAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryRegisterOriginAnalysis(OutpatientPatientPortraitRequest request) {
        log.info("查询患者画像挂号来源分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryRegisterOriginAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryRegisterOriginAnalysisByJdbc(request);
        } else {
            return queryRegisterOriginAnalysisByMybatisPlus(request);
        }
    }

    @Override
    public List<AnalysisItem> queryArchiveOriginAnalysis(OutpatientPatientPortraitRequest request) {
        log.info("查询患者画像建档来源分析，mode={}", dataConfig.getMode());
        if (dataConfig.isMock()) {
            return queryArchiveOriginAnalysisMock(request);
        } else if (dataConfig.isJdbc()) {
            return queryArchiveOriginAnalysisByJdbc(request);
        } else {
            return queryArchiveOriginAnalysisByMybatisPlus(request);
        }
    }

    // ==================== Mock 模式 ====================

    private AgeAnalysis queryAgeAnalysisMock(OutpatientPatientPortraitRequest request) {
        SeqUtil.next();
        AgeAnalysis analysis = new AgeAnalysis();
        List<String> categories = new ArrayList<>();
        categories.add("0-18岁");
        categories.add("19-35岁");
        categories.add("36-50岁");
        categories.add("51-65岁");
        categories.add("65岁以上");
        analysis.setCategories(categories);
        List<Integer> archiveData = new ArrayList<>();
        archiveData.add(200);
        archiveData.add(800);
        archiveData.add(700);
        archiveData.add(500);
        archiveData.add(360);
        analysis.setArchiveData(archiveData);
        List<Integer> outpatientData = new ArrayList<>();
        outpatientData.add(150);
        outpatientData.add(600);
        outpatientData.add(550);
        outpatientData.add(400);
        outpatientData.add(280);
        analysis.setOutpatientData(outpatientData);
        return analysis;
    }

    private List<AnalysisItem> queryInsuranceAnalysisMock(OutpatientPatientPortraitRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("医保", 3200));
        list.add(newAnalysisItem("自费", 1200));
        list.add(newAnalysisItem("商保", 80));
        return list;
    }

    private List<AnalysisItem> queryIdentityAnalysisMock(OutpatientPatientPortraitRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("本地", 2800));
        list.add(newAnalysisItem("外地", 1500));
        list.add(newAnalysisItem("外籍", 20));
        return list;
    }

    private List<AnalysisItem> queryRegisterOriginAnalysisMock(OutpatientPatientPortraitRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("微信", 2500));
        list.add(newAnalysisItem("APP", 800));
        list.add(newAnalysisItem("电话", 500));
        list.add(newAnalysisItem("现场", 520));
        return list;
    }

    private List<AnalysisItem> queryArchiveOriginAnalysisMock(OutpatientPatientPortraitRequest request) {
        SeqUtil.next();
        List<AnalysisItem> list = new ArrayList<>();
        list.add(newAnalysisItem("微信", 2000));
        list.add(newAnalysisItem("APP", 600));
        list.add(newAnalysisItem("窗口", 800));
        list.add(newAnalysisItem("自助机", 500));
        return list;
    }

    // ==================== JdbcTemplate 模式 ====================

    private AgeAnalysis queryAgeAnalysisByJdbc(OutpatientPatientPortraitRequest request) {
        log.info("使用 JdbcTemplate 查询年龄分析");
        return queryAgeAnalysisMock(request);
    }

    private List<AnalysisItem> queryInsuranceAnalysisByJdbc(OutpatientPatientPortraitRequest request) {
        return queryInsuranceAnalysisMock(request);
    }

    private List<AnalysisItem> queryIdentityAnalysisByJdbc(OutpatientPatientPortraitRequest request) {
        return queryIdentityAnalysisMock(request);
    }

    private List<AnalysisItem> queryRegisterOriginAnalysisByJdbc(OutpatientPatientPortraitRequest request) {
        return queryRegisterOriginAnalysisMock(request);
    }

    private List<AnalysisItem> queryArchiveOriginAnalysisByJdbc(OutpatientPatientPortraitRequest request) {
        return queryArchiveOriginAnalysisMock(request);
    }

    // ==================== MyBatis-Plus 模式 ====================

    private AgeAnalysis queryAgeAnalysisByMybatisPlus(OutpatientPatientPortraitRequest request) {
        return queryAgeAnalysisMock(request);
    }

    private List<AnalysisItem> queryInsuranceAnalysisByMybatisPlus(OutpatientPatientPortraitRequest request) {
        return queryInsuranceAnalysisMock(request);
    }

    private List<AnalysisItem> queryIdentityAnalysisByMybatisPlus(OutpatientPatientPortraitRequest request) {
        return queryIdentityAnalysisMock(request);
    }

    private List<AnalysisItem> queryRegisterOriginAnalysisByMybatisPlus(OutpatientPatientPortraitRequest request) {
        return queryRegisterOriginAnalysisMock(request);
    }

    private List<AnalysisItem> queryArchiveOriginAnalysisByMybatisPlus(OutpatientPatientPortraitRequest request) {
        return queryArchiveOriginAnalysisMock(request);
    }

    // ==================== 工具方法 ====================

    private AnalysisItem newAnalysisItem(String name, int value) {
        AnalysisItem item = new AnalysisItem();
        item.setName(name);
        item.setValue(value);
        return item;
    }

}
