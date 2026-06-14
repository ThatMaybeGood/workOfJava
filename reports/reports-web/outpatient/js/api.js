/**
 * 门诊报表 API 接口层
 * 基于统一 api-config.js 配置，支持 Mock / 真实接口切换
 */

const ReportAPI = {
    /**
     * 获取统计概览数据
     * GET /api/outpatient/overview
     */
    getOverview() {
        return apiRequest('reports.outp.outpatient-operation', 'overview');
    },

    /**
     * 获取科室门诊量统计数据
     * GET /api/outpatient/department-stats
     */
    getDepartmentStats(params) {
        return apiRequest('reports.outp.outpatient-operation', 'departmentStats', null, params);
    },

    /**
     * 导出 Excel
     * POST /api/outpatient/export
     */
    exportExcel(params) {
        return apiRequest('reports.outp.outpatient-operation', 'export', params);
    },

    /**
     * 获取门诊收入概览数据
     * GET /api/outpatient/revenue-overview
     */
    getRevenueOverview() {
        return apiRequest('reports.outp.outpatient-revenue', 'endpoint');
    },

    /**
     * 获取科室收入统计数据
     * GET /api/outpatient/dept-revenue
     */
    getDeptRevenueStats(params) {
        return apiRequest('reports.outp.outpatient-revenue', 'endpoint', { ...params, _type: 'dept' });
    },

    /**
     * 获取医生收入统计数据
     * GET /api/outpatient/doctor-revenue
     */
    getDoctorRevenueStats(params) {
        return apiRequest('reports.outp.outpatient-revenue', 'endpoint', { ...params, _type: 'doctor' });
    },

    /**
     * 获取患者画像数据
     * GET /api/outpatient/patient-portrait
     */
    getPatientPortrait(params) {
        return apiRequest('reports.outp.outpatient-patient-portrait', 'endpoint', params);
    },

    /**
     * 获取人工窗口统计数据
     * GET /api/outpatient/window-stats
     */
    getWindowStats(params) {
        return apiRequest('reports.outp.outpatient-window-stats', 'endpoint', params);
    },

    /**
     * 获取检验统计数据
     * GET /api/outpatient/lab-stats
     */
    getLabStats(params) {
        return apiRequest('reports.outp.outpatient-lab-stats', 'endpoint', params);
    },

    /**
     * 获取医技统计数据
     * GET /api/outpatient/med-tech-stats
     */
    getMedTechStats(params) {
        return apiRequest('reports.outp.outpatient-med-tech', 'endpoint', params);
    },

    /**
     * 获取爽约退号统计数据
     * GET /api/outpatient/no-show-stats
     */
    getNoShowStats(params) {
        return apiRequest('reports.outp.outpatient-no-show', 'endpoint', params);
    },

    /**
     * 获取门诊预警统计数据
     * GET /api/outpatient/alert-stats
     */
    getAlertStats(params) {
        return apiRequest('reports.outp.outpatient-alert', 'endpoint', params);
    },

    /**
     * 获取诊室使用率统计数据
     * GET /api/outpatient/room-usage-stats
     */
    getRoomUsageStats(params) {
        return apiRequest('reports.outp.outpatient-room-usage', 'endpoint', params);
    },

    /**
     * 获取专科治疗量统计数据
     * GET /api/outpatient/specialty-treatment-stats
     */
    getSpecialtyTreatmentStats(params) {
        return apiRequest('reports.outp.outpatient-specialty-treatment', 'endpoint', params);
    },

    /**
     * 获取治疗统计报表数据
     * GET /api/outpatient/treatment-stats
     */
    getTreatmentStats(params) {
        return apiRequest('reports.outp.outpatient-treatment-stats', 'endpoint', params);
    },

    /**
     * 获取预测门诊量统计数据
     * GET /api/outpatient/forecast-stats
     */
    getForecastStats(params) {
        return apiRequest('reports.outp.outpatient-forecast', 'endpoint', params);
    },

    /**
     * 获取门诊服务质量统计数据
     * GET /api/outpatient/service-quality-stats
     */
    getServiceQualityStats(params) {
        return apiRequest('reports.outp.outpatient-service-quality', 'endpoint', params);
    },

    /**
     * 获取门诊管理质量控制统计数据
     * GET /api/outpatient/quality-control-stats
     */
    getQualityControlStats(params) {
        return apiRequest('reports.outp.outpatient-quality-control', 'endpoint', params);
    },

    /**
     * 获取互医质控运营月报数据
     * GET /api/outpatient/internet-hospital-stats
     */
    getInternetHospitalStats(params) {
        return apiRequest('reports.outp.outpatient-internet-hospital', 'endpoint', params);
    }
};
