/**
 * 现金报表 API 接口层
 * 基于统一 api-config.js 配置，支持 Mock / 真实接口切换
 */

const ReportAPI = {
    /**
     * 获取出院结算概览数据
     * GET /api/cash/discharge-settlement/overview
     */
    getDischargeSettlementOverview() {
        return apiRequest('reports.cash.cash-discharge-settlement', 'overview');
    },

    /**
     * 获取出院结算图表分析数据
     * GET /api/cash/discharge-settlement/charts
     */
    getDischargeSettlementCharts() {
        return apiRequest('reports.cash.cash-discharge-settlement', 'charts');
    },

    /**
     * 获取出院结算表格数据
     * GET /api/cash/discharge-settlement/table
     */
    getDischargeSettlementTable(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'table', params);
    },

    /**
     * 导出出院结算 Excel
     * POST /api/cash/discharge-settlement/export
     */
    exportDischargeSettlement(params) {
        return apiRequest('reports.cash.cash-discharge-settlement', 'export', params);
    },

    /**
     * 获取收费员结账统计概览数据
     * GET /api/cash/cashier-settlement/overview
     */
    getCashierSettlementOverview() {
        return apiRequest('reports.cash.cash-cashier-settlement', 'overview');
    },

    /**
     * 获取收费员结账统计表格数据
     * GET /api/cash/cashier-settlement/table
     */
    getCashierSettlementTable(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'table', params);
    },

    /**
     * 获取收费员结账统计图表数据
     * GET /api/cash/cashier-settlement/chart
     */
    getCashierSettlementChart(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'chart', params);
    },

    /**
     * 导出收费员结账统计 Excel
     * POST /api/cash/cashier-settlement/export
     */
    exportCashierSettlement(params) {
        return apiRequest('reports.cash.cash-cashier-settlement', 'export', params);
    },

    /**
     * 获取住院预交金统计概览数据
     * GET /api/cash/inpatient-prepayment/overview
     */
    getInpatientPrepaymentOverview() {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'overview');
    },

    /**
     * 获取住院预交金汇总表格数据
     * GET /api/cash/inpatient-prepayment/summary-table
     */
    getInpatientPrepaymentSummaryTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'summaryTable', params);
    },

    /**
     * 获取住院预交金进项表格数据
     * GET /api/cash/inpatient-prepayment/income-table
     */
    getInpatientPrepaymentIncomeTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'incomeTable', params);
    },

    /**
     * 获取住院预交金退项表格数据
     * GET /api/cash/inpatient-prepayment/refund-table
     */
    getInpatientPrepaymentRefundTable(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'refundTable', params);
    },

    /**
     * 获取住院预交金趋势图表数据
     * GET /api/cash/inpatient-prepayment/trend-chart
     */
    getInpatientPrepaymentTrendChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'trendChart', params);
    },

    /**
     * 获取住院预交金渠道图表数据（进项）
     * GET /api/cash/inpatient-prepayment/channel-chart
     */
    getInpatientPrepaymentChannelChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'channelChart', params);
    },

    /**
     * 获取住院预交金支付方式图表数据（退项）
     * GET /api/cash/inpatient-prepayment/pay-type-chart
     */
    getInpatientPrepaymentPayTypeChart(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'payTypeChart', params);
    },

    /**
     * 导出住院预交金统计 Excel
     * POST /api/cash/inpatient-prepayment/export
     */
    exportInpatientPrepayment(params) {
        return apiRequest('reports.cash.cash-inpatient-prepayment', 'export', params);
    }
};
