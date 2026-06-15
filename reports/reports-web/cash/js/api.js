/**
 * 现金报表 API 接口层
 * 基于统一 api-config.js 配置，支持 Mock / 真实接口切换
 * 后端统一 POST 接口规范：请求格式 {head: {method: 'xxx'}, body: {...}}
 */

const ReportAPI = {
    /**
     * 获取出院结算概览数据
     * method: reports.cash.cash-discharge-settlement
     */
    getDischargeSettlementOverview(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'overview', params);
    },

    /**
     * 获取出院结算图表分析数据
     * method: reports.cash.cash-discharge-settlement
     */
    getDischargeSettlementCharts(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'charts', params);
    },

    /**
     * 获取出院结算表格数据
     * method: reports.cash.cash-discharge-settlement
     */
    getDischargeSettlementTable(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'table', params);
    },

    /**
     * 导出出院结算 Excel
     * method: reports.cash.cash-discharge-settlement
     */
    exportDischargeSettlement(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'export', params);
    },

    /**
     * 获取收费员结账统计概览数据
     * method: reports.cash.cash-cashier-settlement
     */
    getCashierSettlementOverview(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'overview', params);
    },

    /**
     * 获取收费员结账统计表格数据
     * method: reports.cash.cash-cashier-settlement
     */
    getCashierSettlementTable(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'table', params);
    },

    /**
     * 获取收费员结账统计图表数据
     * method: reports.cash.cash-cashier-settlement
     */
    getCashierSettlementChart(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'chart', params);
    },

    /**
     * 导出收费员结账统计 Excel
     * method: reports.cash.cash-cashier-settlement
     */
    exportCashierSettlement(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'export', params);
    },

    /**
     * 获取住院预交金统计概览数据
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentOverview(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'overview', params);
    },

    /**
     * 获取住院预交金汇总表格数据
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentSummaryTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'summaryTable', params);
    },

    /**
     * 获取住院预交金进项表格数据
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentIncomeTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'incomeTable', params);
    },

    /**
     * 获取住院预交金退项表格数据
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentRefundTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'refundTable', params);
    },

    /**
     * 获取住院预交金趋势图表数据
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentTrendChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'trendChart', params);
    },

    /**
     * 获取住院预交金渠道图表数据（进项）
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentChannelChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'channelChart', params);
    },

    /**
     * 获取住院预交金支付方式图表数据（退项）
     * method: reports.cash.cash-inpatient-prepayment
     */
    getInpatientPrepaymentPayTypeChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'payTypeChart', params);
    },

    /**
     * 导出住院预交金统计 Excel
     * method: reports.cash.cash-inpatient-prepayment
     */
    exportInpatientPrepayment(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'export', params);
    }
};
