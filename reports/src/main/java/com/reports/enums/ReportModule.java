package com.reports.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 报表模块枚举
 * 统一管理所有报表界面的中文名称、方法名和对应的服务接口
 */
@Getter
@AllArgsConstructor
public enum ReportModule {

    // ==================== 门诊类报表 ====================

    /**
     * 门诊运行数据统计
     */
    OUTPATIENT_OPERATION("门诊运行数据统计",
            "com.reports.service.OutpatientOperationService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            )),

    /**
     * 门诊预警统计
     */
    OUTPATIENT_ALERT("门诊预警统计",
            "com.reports.service.OutpatientAlertService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryDeptTable", "查询科室预警表格"),
                    new ReportMethod("queryDoctorTable", "查询医生预警表格")
            )),

    /**
     * 预测门诊量报表
     */
    OUTPATIENT_FORECAST("预测门诊量报表",
            "com.reports.service.OutpatientForecastService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryMonthForecast", "查询未来30天预测"),
                    new ReportMethod("queryYearForecast", "查询未来12个月预测")
            )),

    /**
     * 互医质控运营月报
     */
    OUTPATIENT_INTERNET_HOSPITAL("互医质控运营月报",
            "com.reports.service.OutpatientInternetHospitalService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryOperationTable", "查询运行情况表"),
                    new ReportMethod("queryBusinessChart", "查询业务分析图表"),
                    new ReportMethod("queryDeptRanking", "查询科室排行"),
                    new ReportMethod("queryDoctorRanking", "查询医生排行"),
                    new ReportMethod("queryGrowthChart", "查询增长趋势图表")
            )),

    /**
     * 检验统计
     */
    OUTPATIENT_LAB_STATS("检验统计",
            "com.reports.service.OutpatientLabStatsService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据")
            )),

    /**
     * 医技统计
     */
    OUTPATIENT_MED_TECH("医技统计",
            "com.reports.service.OutpatientMedTechService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            )),

    /**
     * 爽约退号分析
     */
    OUTPATIENT_NO_SHOW("爽约退号分析",
            "com.reports.service.OutpatientNoShowService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryRefundOrigin", "查询退号来源分析"),
                    new ReportMethod("queryRefundChannel", "查询退号渠道分析"),
                    new ReportMethod("queryAgeAnalysis", "查询年龄分析数据"),
                    new ReportMethod("queryTable", "查询明细表格")
            )),

    /**
     * 患者画像
     */
    OUTPATIENT_PATIENT_PORTRAIT("患者画像",
            "com.reports.service.OutpatientPatientPortraitService",
            Arrays.asList(
                    new ReportMethod("queryAgeAnalysis", "查询年龄分析"),
                    new ReportMethod("queryInsuranceAnalysis", "查询医保分析"),
                    new ReportMethod("queryIdentityAnalysis", "查询身份分析"),
                    new ReportMethod("queryRegisterOriginAnalysis", "查询挂号来源分析"),
                    new ReportMethod("queryArchiveOriginAnalysis", "查询建档来源分析")
            )),

    /**
     * 门诊管理质量控制
     */
    OUTPATIENT_QUALITY_CONTROL("门诊管理质量控制",
            "com.reports.service.OutpatientQualityControlService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            )),

    /**
     * 门诊收入分析
     */
    OUTPATIENT_REVENUE("门诊收入分析",
            "com.reports.service.OutpatientRevenueService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryDeptTable", "查询科室收入表格"),
                    new ReportMethod("queryDoctorTable", "查询医生收入表格")
            )),

    /**
     * 诊室使用率分析
     */
    OUTPATIENT_ROOM_USAGE("诊室使用率分析",
            "com.reports.service.OutpatientRoomUsageService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            )),

    /**
     * 门诊服务质量分析
     */
    OUTPATIENT_SERVICE_QUALITY("门诊服务质量分析",
            "com.reports.service.OutpatientServiceQualityService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryComplaintList", "查询投诉明细"),
                    new ReportMethod("queryPraiseList", "查询表扬明细")
            )),

    /**
     * 专科治疗量统计
     */
    OUTPATIENT_SPECIALTY_TREATMENT("专科治疗量统计",
            "com.reports.service.OutpatientSpecialtyTreatmentService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            )),

    /**
     * 人工窗口统计
     */
    OUTPATIENT_WINDOW_STATS("人工窗口统计",
            "com.reports.service.OutpatientWindowStatsService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryAgeAnalysis", "查询年龄分析"),
                    new ReportMethod("queryTimeAnalysis", "查询时段分析"),
                    new ReportMethod("querySourceAnalysis", "查询来源分析"),
                    new ReportMethod("queryWorkloadTable", "查询工作量表格")
            )),

    // ==================== 收费类报表 ====================

    /**
     * 收费员结账统计
     */
    CASH_CASHIER_SETTLEMENT("收费员结账统计",
            "com.reports.service.CashCashierSettlementService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryTable", "查询表格数据"),
                    new ReportMethod("queryChart", "查询图表数据")
            )),

    /**
     * 出院结算报表
     */
    CASH_DISCHARGE_SETTLEMENT("出院结算报表",
            "com.reports.service.CashDischargeSettlementService",
            Arrays.asList(
                    new ReportMethod("queryOverview", "查询概览数据"),
                    new ReportMethod("queryCharts", "查询图表分析数据"),
                    new ReportMethod("queryTable", "查询表格数据")
            ));

    /**
     * 报表中文名称
     */
    private final String chineseName;

    /**
     * 对应服务接口全限定名
     */
    private final String serviceClassName;

    /**
     * 该报表包含的方法列表
     */
    private final List<ReportMethod> methods;

    /**
     * 根据中文名称查找报表模块
     */
    public static ReportModule getByChineseName(String chineseName) {
        for (ReportModule module : values()) {
            if (module.getChineseName().equals(chineseName)) {
                return module;
            }
        }
        return null;
    }

    /**
     * 根据服务类名查找报表模块
     */
    public static ReportModule getByServiceClassName(String serviceClassName) {
        for (ReportModule module : values()) {
            if (module.getServiceClassName().equals(serviceClassName)) {
                return module;
            }
        }
        return null;
    }

    /**
     * 报表方法信息
     */
    @Getter
    @AllArgsConstructor
    public static class ReportMethod {
        /**
         * 方法名
         */
        private final String methodName;

        /**
         * 方法中文描述
         */
        private final String methodDesc;
    }

}
