/**
 * 门诊报表 API 接口层
 * 基于统一 api-config.js 配置，支持 Mock / 真实接口切换
 * 后端统一 POST 接口规范：请求格式 {head: {method: 'xxx'}, body: {...}}
 */

const ReportAPI = {
    /**
     * 获取门诊运行数据统计（概览 + 表格，单次请求）
     * method: reports.outp.outpatient-operation
     */
    getOperationStats(params) {
        return apiRequest('reports.outp.outpatient-operation', 'operationStats', params);
    },

    /**
     * 导出 Excel
     * method: reports.outp.outpatient-operation
     */
    exportExcel(params) {
        return apiRequest('reports.outp.outpatient-operation', 'export', params);
    },

    /**
     * 获取门诊收入分析（概览 + 科室表 + 医生表，单次请求）
     * method: reports.outp.outpatient-revenue
     */
    getRevenueStats(params) {
        return apiRequest('reports.outp.outpatient-revenue', 'endpoint', params);
    },

    /**
     * 获取患者画像数据
     * method: reports.outp.outpatient-patient-portrait
     */
    getPatientPortrait(params) {
        return apiRequest('reports.outp.outpatient-patient-portrait', 'endpoint', params);
    },

    /**
     * 获取人工窗口统计数据
     * method: reports.outp.outpatient-window-stats
     */
    getWindowStats(params) {
        return apiRequest('reports.outp.outpatient-window-stats', 'endpoint', params);
    },

    /**
     * 获取检验统计数据
     * method: reports.outp.outpatient-lab-stats
     */
    getLabStats(params) {
        return apiRequest('reports.outp.outpatient-lab-stats', 'endpoint', params);
    },

    /**
     * 获取医技统计数据
     * method: reports.outp.outpatient-med-tech
     */
    getMedTechStats(params) {
        return apiRequest('reports.outp.outpatient-med-tech', 'endpoint', params);
    },

    /**
     * 获取爽约退号统计数据
     * method: reports.outp.outpatient-no-show
     */
    getNoShowStats(params) {
        return apiRequest('reports.outp.outpatient-no-show', 'endpoint', params);
    },

    /**
     * 获取门诊预警统计数据
     * method: reports.outp.outpatient-alert
     */
    getAlertStats(params) {
        return apiRequest('reports.outp.outpatient-alert', 'endpoint', params);
    },

    /**
     * 获取诊室使用率统计数据
     * method: reports.outp.outpatient-room-usage
     */
    getRoomUsageStats(params) {
        return apiRequest('reports.outp.outpatient-room-usage', 'endpoint', params);
    },

    /**
     * 获取专科治疗量统计数据
     * method: reports.outp.outpatient-specialty-treatment
     */
    getSpecialtyTreatmentStats(params) {
        return apiRequest('reports.outp.outpatient-specialty-treatment', 'endpoint', params);
    },

    /**
     * 获取治疗统计报表数据
     * method: reports.outp.outpatient-treatment-stats
     */
    getTreatmentStats(params) {
        return apiRequest('reports.outp.outpatient-treatment-stats', 'endpoint', params);
    },

    /**
     * 获取预测门诊量统计数据
     * method: reports.outp.outpatient-forecast
     */
    getForecastStats(params) {
        return apiRequest('reports.outp.outpatient-forecast', 'endpoint', params);
    },

    /**
     * 获取门诊服务质量统计数据
     * method: reports.outp.outpatient-service-quality
     */
    getServiceQualityStats(params) {
        return apiRequest('reports.outp.outpatient-service-quality', 'endpoint', params);
    },

    /**
     * 获取门诊管理质量控制统计数据
     * method: reports.outp.outpatient-quality-control
     */
    getQualityControlStats(params) {
        return apiRequest('reports.outp.outpatient-quality-control', 'endpoint', params);
    },

    /**
     * 获取互医质控运营月报数据
     * method: reports.outp.outpatient-internet-hospital
     */
    getInternetHospitalStats(params) {
        return apiRequest('reports.outp.outpatient-internet-hospital', 'endpoint', params);
    },

    /**
     * 获取科室字典
     * method: reports.common.dept-dict
     */
    getDeptDict(params) {
        return apiRequest('reports.common.dept-dict', 'endpoint', params);
    },

    /**
     * 通用字典（query/add/delete）
     * method: reports.common.data-dict
     */
    getDataDict(params) {
        return apiRequest('reports.common.data-dict', 'endpoint', params);
    },

    /**
     * 人员字典
     * method: reports.common.staff-dict
     */
    getStaffDict(params) {
        return apiRequest('reports.common.staff-dict', 'endpoint', params);
    },

    /**
     * 门诊服务质量数据维护（query/save/delete）
     * method: reports.outp.service-quality-maintain
     */
    maintainServiceQuality(params) {
        return apiRequest('reports.outp.service-quality-maintain', 'endpoint', params);
    }
};
