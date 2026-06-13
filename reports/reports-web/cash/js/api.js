/**
 * 接口封装层
 * 出院结算报表统一接口
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

const ReportAPI = {
    /**
     * 获取出院结算概览数据
     * GET /api/cash/discharge-settlement/overview
     */
    getDischargeSettlementOverview() {
        return MockService.getDischargeSettlementOverview();
    },

    /**
     * 获取出院结算图表分析数据
     * GET /api/cash/discharge-settlement/charts
     */
    getDischargeSettlementCharts() {
        return MockService.getDischargeSettlementCharts();
    },

    /**
     * 获取出院结算表格数据
     * GET /api/cash/discharge-settlement/table
     */
    getDischargeSettlementTable(params) {
        return MockService.getDischargeSettlementTable(params);
    },

    /**
     * 导出 Excel
     * POST /api/cash/discharge-settlement/export
     */
    exportDischargeSettlement(params) {
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log('导出参数：', params);
                resolve({ code: 200, message: '导出成功' });
            }, 500);
        });
    },

    /**
     * 获取收费员结账统计概览数据
     * GET /api/cash/cashier-settlement/overview
     */
    getCashierSettlementOverview() {
        return MockService.getCashierSettlementOverview();
    },

    /**
     * 获取收费员结账统计表格数据
     * GET /api/cash/cashier-settlement/table
     */
    getCashierSettlementTable(params) {
        return MockService.getCashierSettlementTable(params);
    },

    /**
     * 获取收费员结账统计图表数据
     * GET /api/cash/cashier-settlement/chart
     */
    getCashierSettlementChart(params) {
        return MockService.getCashierSettlementChart(params);
    },

    /**
     * 导出收费员结账统计 Excel
     * POST /api/cash/cashier-settlement/export
     */
    exportCashierSettlement(params) {
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log('导出参数：', params);
                resolve({ code: 200, message: '导出成功' });
            }, 500);
        });
    },

    /**
     * 获取住院预交金统计概览数据
     * GET /api/cash/inpatient-prepayment/overview
     */
    getInpatientPrepaymentOverview() {
        return MockService.getInpatientPrepaymentOverview();
    },

    /**
     * 获取住院预交金汇总表格数据
     * GET /api/cash/inpatient-prepayment/summary-table
     */
    getInpatientPrepaymentSummaryTable(params) {
        return MockService.getInpatientPrepaymentSummaryTable(params);
    },

    /**
     * 获取住院预交金进项表格数据
     * GET /api/cash/inpatient-prepayment/income-table
     */
    getInpatientPrepaymentIncomeTable(params) {
        return MockService.getInpatientPrepaymentIncomeTable(params);
    },

    /**
     * 获取住院预交金退项表格数据
     * GET /api/cash/inpatient-prepayment/refund-table
     */
    getInpatientPrepaymentRefundTable(params) {
        return MockService.getInpatientPrepaymentRefundTable(params);
    },

    /**
     * 获取住院预交金趋势图表数据
     * GET /api/cash/inpatient-prepayment/trend-chart
     */
    getInpatientPrepaymentTrendChart(params) {
        return MockService.getInpatientPrepaymentTrendChart(params);
    },

    /**
     * 获取住院预交金渠道图表数据
     * GET /api/cash/inpatient-prepayment/channel-chart
     */
    getInpatientPrepaymentChannelChart(params) {
        return MockService.getInpatientPrepaymentChannelChart(params);
    },

    /**
     * 获取住院预交金支付方式图表数据（退项）
     * GET /api/cash/inpatient-prepayment/pay-type-chart
     */
    getInpatientPrepaymentPayTypeChart(params) {
        return MockService.getInpatientPrepaymentPayTypeChart(params);
    },

    /**
     * 导出住院预交金统计 Excel
     * POST /api/cash/inpatient-prepayment/export
     */
    exportInpatientPrepayment(params) {
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log('导出参数：', params);
                resolve({ code: 200, message: '导出成功' });
            }, 500);
        });
    }
};
