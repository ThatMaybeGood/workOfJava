/**
 * 接口封装层
 * 统一处理网络请求
 */

/**
 * 使用 fetch 获取数据
 * @param {string} url - 接口地址
 * @param {Object} options - 请求配置
 * @returns {Promise} 返回 Promise 对象
 */
async function fetchData(url, options = {}) {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

/**
 * 报表页面 API 接口
 */
const ReportAPI = {
    /**
     * 获取统计概览数据
     * GET /api/outpatient/overview
     */
    getOverview() {
        // 生产环境调用真实接口
        // return fetchData('/api/outpatient/overview');
        return MockService.getOverviewData();
    },

    /**
     * 获取科室门诊量统计数据
     * GET /api/outpatient/department-stats
     * @param {Object} params - 查询参数
     */
    getDepartmentStats(params) {
        // 生产环境调用真实接口
        // const queryString = new URLSearchParams(params).toString();
        // return fetchData(`/api/outpatient/department-stats?${queryString}`);
        return MockService.getTableData(params);
    },

    /**
     * 导出 Excel
     * POST /api/outpatient/export
     * @param {Object} params - 导出参数
     */
    exportExcel(params) {
        // 生产环境调用真实接口
        // return fetch('/api/outpatient/export', {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(params)
        // });
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log('导出参数：', params);
                resolve({ code: 200, message: '导出成功' });
            }, 500);
        });
    },

    /**
     * 获取门诊收入概览数据
     * GET /api/outpatient/revenue-overview
     */
    getRevenueOverview() {
        return MockService.getRevenueOverviewData();
    },

    /**
     * 获取科室收入统计数据
     * GET /api/outpatient/dept-revenue
     */
    getDeptRevenueStats(params) {
        return MockService.getDeptRevenueData(params);
    },

    /**
     * 获取医生收入统计数据
     * GET /api/outpatient/doctor-revenue
     */
    getDoctorRevenueStats(params) {
        return MockService.getDoctorRevenueData(params);
    },

    /**
     * 获取患者画像数据
     * GET /api/outpatient/patient-portrait
     */
    getPatientPortrait(params) {
        return MockService.getPatientPortraitData(params);
    },

    /**
     * 获取人工窗口统计数据
     * GET /api/outpatient/window-stats
     */
    getWindowStats(params) {
        return MockService.getWindowStatsData(params);
    },

    /**
     * 获取检验统计数据
     * GET /api/outpatient/lab-stats
     */
    getLabStats(params) {
        return MockService.getLabStatsData(params);
    },

    /**
     * 获取医技统计数据
     * GET /api/outpatient/med-tech-stats
     */
    getMedTechStats(params) {
        return MockService.getMedTechStatsData(params);
    },

    /**
     * 获取爽约退号统计数据
     * GET /api/outpatient/no-show-stats
     */
    getNoShowStats(params) {
        return MockService.getNoShowStatsData(params);
    },

    /**
     * 获取门诊预警统计数据
     * GET /api/outpatient/alert-stats
     */
    getAlertStats(params) {
        return MockService.getAlertStatsData(params);
    },

    getRoomUsageStats(params) {
        return MockService.getRoomUsageStatsData(params);
    },

    getSpecialtyTreatmentStats(params) {
        return MockService.getSpecialtyTreatmentStatsData(params);
    },

    getTreatmentStats(params) {
        return MockService.getTreatmentStatsData(params);
    },

    getForecastStats(params) {
        return MockService.getForecastStatsData(params);
    },

    getServiceQualityStats(params) {
        return MockService.getServiceQualityStatsData(params);
    },

    getQualityControlStats(params) {
        return MockService.getQualityControlStatsData(params);
    },

    getInternetHospitalStats(params) {
        return MockService.getInternetHospitalStatsData(params);
    }
};
