/**
 * 统一 API 配置与路由中心
 * 集中管理所有报表接口地址、mock 开关、请求封装
 */

const API_CONFIG = {
    // ==================== 全局开关 ====================
    // 是否使用 Mock 数据（true = 走 MockService，false = 请求真实接口）
    useMock: true,

    // 真实接口基础地址
    baseUrl: 'http://localhost:18089/reports/gateway',

    // ==================== 接口方法名映射 ====================
    // 每个 key 对应 API 文档中的 method 字段
    methods: {
        // 门诊运行数据统计（门诊量统计）
        'reports.outp.outpatient-operation': {
            overview: '/api/outpatient/overview',
            departmentStats: '/api/outpatient/department-stats',
            export: '/api/outpatient/export'
        },
        // 门诊收入分析
        'reports.outp.outpatient-revenue': {
            endpoint: '/api/outpatient/revenue-overview',
            deptRevenue: '/api/outpatient/dept-revenue',
            doctorRevenue: '/api/outpatient/doctor-revenue'
        },
        // 患者画像
        'reports.outp.outpatient-patient-portrait': {
            endpoint: '/api/outpatient/patient-portrait'
        },
        // 人工窗口统计
        'reports.outp.outpatient-window-stats': {
            endpoint: '/api/outpatient/window-stats'
        },
        // 检验统计
        'reports.outp.outpatient-lab-stats': {
            endpoint: '/api/outpatient/lab-stats'
        },
        // 医技统计
        'reports.outp.outpatient-med-tech': {
            endpoint: '/api/outpatient/med-tech-stats'
        },
        // 爽约退号分析
        'reports.outp.outpatient-no-show': {
            endpoint: '/api/outpatient/no-show-stats'
        },
        // 门诊预警统计
        'reports.outp.outpatient-alert': {
            endpoint: '/api/outpatient/alert-stats'
        },
        // 诊室使用率分析
        'reports.outp.outpatient-room-usage': {
            endpoint: '/api/outpatient/room-usage-stats'
        },
        // 专科治疗量统计
        'reports.outp.outpatient-specialty-treatment': {
            endpoint: '/api/outpatient/specialty-treatment-stats'
        },
        // 治疗统计报表（treatment-stats）
        'reports.outp.outpatient-treatment-stats': {
            endpoint: '/api/outpatient/treatment-stats'
        },
        // 预测门诊量报表
        'reports.outp.outpatient-forecast': {
            endpoint: '/api/outpatient/forecast-stats'
        },
        // 门诊服务质量分析
        'reports.outp.outpatient-service-quality': {
            endpoint: '/api/outpatient/service-quality-stats'
        },
        // 门诊管理质量控制
        'reports.outp.outpatient-quality-control': {
            endpoint: '/api/outpatient/quality-control-stats'
        },
        // 互医质控运营月报
        'reports.outp.outpatient-internet-hospital': {
            endpoint: '/api/outpatient/internet-hospital-stats'
        },
        // 出院结算报表
        'reports.cash.cash-discharge-settlement': {
            overview: '/api/cash/discharge-settlement/overview',
            charts: '/api/cash/discharge-settlement/charts',
            table: '/api/cash/discharge-settlement/table',
            export: '/api/cash/discharge-settlement/export'
        },
        // 收费员结账统计
        'reports.cash.cash-cashier-settlement': {
            overview: '/api/cash/cashier-settlement/overview',
            table: '/api/cash/cashier-settlement/table',
            chart: '/api/cash/cashier-settlement/chart',
            export: '/api/cash/cashier-settlement/export'
        },
        // 住院预交金统计
        'reports.cash.cash-inpatient-prepayment': {
            overview: '/api/cash/inpatient-prepayment/overview',
            summaryTable: '/api/cash/inpatient-prepayment/summary-table',
            incomeTable: '/api/cash/inpatient-prepayment/income-table',
            refundTable: '/api/cash/inpatient-prepayment/refund-table',
            trendChart: '/api/cash/inpatient-prepayment/trend-chart',
            channelChart: '/api/cash/inpatient-prepayment/channel-chart',
            payTypeChart: '/api/cash/inpatient-prepayment/pay-type-chart',
            export: '/api/cash/inpatient-prepayment/export'
        }
    }
};

/**
 * 统一请求封装
 * 根据 useMock 开关决定走 Mock 还是真实接口
 * 后端统一 POST 接口规范：请求格式 {head: {method: 'xxx'}, body: {...}}，响应格式 {result: {...}, body: {...}}
 * @param {string} methodKey - API_CONFIG.methods 中的 key
 * @param {string} endpointKey - 接口子路径 key（Mock 路由用）
 * @param {Object} requestBody - 业务请求体（body 部分）
 * @returns {Promise}
 */
async function apiRequest(methodKey, endpointKey, requestBody = null) {
    // 如果开启 Mock，直接返回 MockService 数据
    if (API_CONFIG.useMock) {
        const response = await callMockService(methodKey, endpointKey, requestBody);
        return response.data || response;
    }

    // 真实接口请求：统一 POST 到 baseUrl，构建 head + body 格式
    const payload = {
        head: {
            charset: 'utf-8',
            encrypt_type: 'AES',
            language: 'zh_CN',
            method: methodKey
        },
        body: requestBody || {}
    };

    const options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    };

    const response = await fetchData(API_CONFIG.baseUrl, options);

    // 解析后端响应格式 {result: {...}, body: {...}}
    if (response.result && response.result.code === '10000' && response.result.success) {
        return response.body;
    }

    // 业务错误
    const errorMsg = response.result ? (response.result.sub_msg || response.result.msg || '请求失败') : '未知错误';
    throw new Error(errorMsg);
}

/**
 * 通用 fetch 封装
 */
async function fetchData(url, options = {}) {
    try {
        const response = await fetch(url, {
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
 * 根据 methodKey 和 endpointKey 路由到对应的 MockService 方法
 */
function callMockService(methodKey, endpointKey, params) {
    const mockRouter = {
        // 门诊运行数据统计
        'reports.outp.outpatient-operation': {
            operationStats: (p) => MockService.getOperationStats(p),
            export: (p) => MockService.exportExcel(p)
        },
        // 门诊收入分析
        'reports.outp.outpatient-revenue': {
            endpoint: (p) => {
                if (p && p.page && p.deptName !== undefined) {
                    if (p._type === 'doctor') return MockService.getDoctorRevenueData(p);
                    return MockService.getDeptRevenueData(p);
                }
                return MockService.getRevenueOverviewData();
            }
        },
        // 患者画像
        'reports.outp.outpatient-patient-portrait': {
            endpoint: (p) => MockService.getPatientPortraitData(p)
        },
        // 人工窗口统计
        'reports.outp.outpatient-window-stats': {
            endpoint: (p) => MockService.getWindowStatsData(p)
        },
        // 检验统计
        'reports.outp.outpatient-lab-stats': {
            endpoint: (p) => MockService.getLabStatsData(p)
        },
        // 医技统计
        'reports.outp.outpatient-med-tech': {
            endpoint: (p) => MockService.getMedTechStatsData(p)
        },
        // 爽约退号分析
        'reports.outp.outpatient-no-show': {
            endpoint: (p) => MockService.getNoShowStatsData(p)
        },
        // 门诊预警统计
        'reports.outp.outpatient-alert': {
            endpoint: (p) => MockService.getAlertStatsData(p)
        },
        // 诊室使用率分析
        'reports.outp.outpatient-room-usage': {
            endpoint: (p) => MockService.getRoomUsageStatsData(p)
        },
        // 专科治疗量统计
        'reports.outp.outpatient-specialty-treatment': {
            endpoint: (p) => MockService.getSpecialtyTreatmentStatsData(p)
        },
        // 治疗统计报表
        'reports.outp.outpatient-treatment-stats': {
            endpoint: (p) => MockService.getTreatmentStatsData(p)
        },
        // 预测门诊量报表
        'reports.outp.outpatient-forecast': {
            endpoint: (p) => MockService.getForecastStatsData(p)
        },
        // 门诊服务质量分析
        'reports.outp.outpatient-service-quality': {
            endpoint: (p) => MockService.getServiceQualityStatsData(p)
        },
        // 门诊管理质量控制
        'reports.outp.outpatient-quality-control': {
            endpoint: (p) => MockService.getQualityControlStatsData(p)
        },
        // 互医质控运营月报
        'reports.outp.outpatient-internet-hospital': {
            endpoint: (p) => MockService.getInternetHospitalStatsData(p)
        },
        // 出院结算报表
        'reports.cash.cash-discharge-settlement': {
            overview: () => MockService.getDischargeSettlementOverview(),
            charts: () => MockService.getDischargeSettlementCharts(),
            table: (p) => MockService.getDischargeSettlementTable(p),
            export: (p) => MockService.exportDischargeSettlement(p)
        },
        // 收费员结账统计
        'reports.cash.cash-cashier-settlement': {
            overview: () => MockService.getCashierSettlementOverview(),
            table: (p) => MockService.getCashierSettlementTable(p),
            chart: (p) => MockService.getCashierSettlementChart(p),
            export: (p) => MockService.exportCashierSettlement(p)
        },
        // 住院预交金统计
        'reports.cash.cash-inpatient-prepayment': {
            overview: () => MockService.getInpatientPrepaymentOverview(),
            summaryTable: (p) => MockService.getInpatientPrepaymentSummaryTable(p),
            incomeTable: (p) => MockService.getInpatientPrepaymentIncomeTable(p),
            refundTable: (p) => MockService.getInpatientPrepaymentRefundTable(p),
            trendChart: (p) => MockService.getInpatientPrepaymentTrendChart(p),
            channelChart: (p) => MockService.getInpatientPrepaymentChannelChart(p),
            payTypeChart: (p) => MockService.getInpatientPrepaymentPayTypeChart(p),
            export: (p) => MockService.exportInpatientPrepayment(p)
        }
    };

    const methodRouter = mockRouter[methodKey];
    if (!methodRouter) {
        throw new Error(`No mock router for method: ${methodKey}`);
    }

    const handler = methodRouter[endpointKey] || methodRouter.endpoint;
    if (!handler) {
        throw new Error(`No mock handler for endpoint: ${endpointKey} in method: ${methodKey}`);
    }

    return handler(params);
}

/**
 * 切换 Mock / 真实接口模式
 * @param {boolean} enabled - true 使用 Mock，false 使用真实接口
 */
function setMockMode(enabled) {
    API_CONFIG.useMock = enabled;
    console.log(`[API] Mock mode ${enabled ? 'enabled' : 'disabled'}`);
    // 保存到 localStorage 以便页面刷新后保持设置
    localStorage.setItem('reports_use_mock', enabled ? '1' : '0');
}

/**
 * 初始化 Mock 模式（从 localStorage 读取）
 */
function initMockMode() {
    const saved = localStorage.getItem('reports_use_mock');
    if (saved !== null) {
        API_CONFIG.useMock = saved === '1';
    }
}

// 页面加载时初始化
initMockMode();
