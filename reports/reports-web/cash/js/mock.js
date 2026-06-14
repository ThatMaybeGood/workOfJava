/**
 * Mock 数据服务
 * 出院结算报表模拟数据
 */
const MockService = {
    /**
     * 获取出院结算概览数据
     */
    getDischargeSettlementOverview() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        totalDischargeCount: 10726,
                        totalDischargeCompare: -8,
                        dischargedCount: 10726,
                        dischargedCompare: -8,
                        notDischargedCount: 10726,
                        notDischargedCompare: -8,
                        settlementAmount: 10726.00,
                        settlementAmountCompare: 5
                    }
                });
            }, 200);
        });
    },

    /**
     * 获取出院结算图表分析数据
     */
    getDischargeSettlementCharts() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        channelAnalysis: [
                            { name: '窗口', value: 31, compare: -8 },
                            { name: '自助机', value: 31, compare: 5 },
                            { name: '掌上医院', value: 31, compare: 1 }
                        ],
                        patientTypeAnalysis: [
                            { name: '微信', value: 31, compare: -8 },
                            { name: '支付宝', value: 31, compare: 5 },
                            { name: '银行卡', value: 31, compare: 1 },
                            { name: '现金', value: 31, compare: -14 }
                        ],
                        amountTypeAnalysis: [
                            { name: '微信', value: 31, compare: -8 },
                            { name: '支付宝', value: 31, compare: 5 },
                            { name: '银行卡', value: 31, compare: 1 },
                            { name: '现金', value: 31, compare: -14 }
                        ]
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取出院结算表格数据
     * @param {Object} params - 查询参数 { page, pageSize, dimension, startDate, endDate }
     */
    getDischargeSettlementTable(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateSettlementTableData(params);
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = allData.length;
                const startIndex = (page - 1) * pageSize;
                const list = allData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成出院结算表格数据
     */
    generateSettlementTableData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            data.push({
                date: dateStr,
                totalLast: 100,
                totalCurrent: 120,
                totalCompare: 20,
                dischargedLast: 1000,
                dischargedCurrent: 1100,
                dischargedCompare: 10,
                notDischargedLast: 2000,
                notDischargedCurrent: 3000,
                notDischargedCompare: 50,
                amountLast: 1000000.00,
                amountCurrent: 500000.00,
                amountCompare: -50
            });
        }

        return data;
    },

    /**
     * 获取收费员结账统计概览数据
     */
    getCashierSettlementOverview() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        appointmentRegister: 10726,
                        appointmentRegisterCompare: -8,
                        appointmentFetch: 10726,
                        appointmentFetchCompare: 5,
                        todayRegister: 10726,
                        todayRegisterCompare: 5,
                        refund: 10726,
                        refundCompare: 5,
                        outpatientCharge: 10726,
                        outpatientChargeCompare: -8,
                        outpatientRefund: 10726,
                        outpatientRefundCompare: -8,
                        prepayment: 10726,
                        prepaymentCompare: 5,
                        hospitalRefund: 10726,
                        hospitalRefundCompare: 5,
                        dischargeSettlement: 10726,
                        dischargeSettlementCompare: 5
                    }
                });
            }, 200);
        });
    },

    /**
     * 获取收费员结账统计表格数据
     * @param {Object} params - 查询参数 { tab, page, pageSize, dimension, startDate, endDate }
     */
    getCashierSettlementTable(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const tab = params.tab || 'cashier';
                const allData = tab === 'cashier'
                    ? this.generateCashierByCashierData(params)
                    : tab === 'source'
                        ? this.generateCashierBySourceData(params)
                        : this.generateCashierWorkloadData(params);

                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = allData.length;
                const startIndex = (page - 1) * pageSize;
                const list = allData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 按收费员统计表格数据
     */
    generateCashierByCashierData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');
        const cashiers = ['收费员1', '收费员2', '收费员3', '收费员4', '收费员5', '收费员6', '收费员7', '收费员8'];

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            const row = { date: dateStr };
            let total = 0;
            cashiers.forEach((name, index) => {
                const value = index < 4
                    ? (index % 2 === 0 ? 1000 : 1100)
                    : (index === 4 || index === 5 ? 500000.00 : (index === 6 ? 1000 : 1100));
                row[name] = value;
                total += value;
            });
            row['汇总'] = total;
            data.push(row);
        }

        return data;
    },

    /**
     * 按来源方式统计表格数据
     */
    generateCashierBySourceData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            const row = { date: dateStr };
            let total = 0;
            ['预约挂号量', '预约取号量', '当日挂号量', '退号量', '门诊收费量', '门诊退费量', '收预交金量', '退院量', '出院结算量'].forEach((name, index) => {
                const value = index < 4 ? 1100 : (index === 4 || index === 5 ? 500000.00 : (index === 6 ? 1000000.00 : 1100));
                row[name] = value;
                total += value;
            });
            row['汇总'] = total;
            data.push(row);
        }

        return data;
    },

    /**
     * 工作量报表表格数据
     */
    generateCashierWorkloadData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');
        const cashiers = ['收费员1', '收费员2', '收费员3', '收费员4', '收费员5', '收费员6', '收费员7', '收费员8', '收费员9', '收费员10'];

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            cashiers.forEach(name => {
                data.push({
                    dateRange: `${dateStr}~${dateStr}`,
                    cashier: name,
                    todayRegister: 1000,
                    effectiveRegister: 1000,
                    appointmentRegister: 1000,
                    outpatientCharge: 1000,
                    outpatientRefund: 1000
                });
            });
        }

        return data;
    },

    /**
     * 获取住院预交金统计概览数据
     */
    getInpatientPrepaymentOverview() {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        prepaymentCount: 10726,
                        prepaymentCountCompare: -8,
                        prepaymentAmount: 10726.00,
                        prepaymentAmountCompare: 5
                    }
                });
            }, 200);
        });
    },

    /**
     * 获取住院预交金汇总表格数据
     */
    getInpatientPrepaymentSummaryTable(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateInpatientPrepaymentSummaryData(params);
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = allData.length;
                const startIndex = (page - 1) * pageSize;
                const list = allData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成住院预交金汇总数据
     */
    generateInpatientPrepaymentSummaryData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            data.push({
                date: dateStr,
                countLast: 1000,
                countCurrent: 1100,
                countCompare: 10,
                amountLast: 1000000.00,
                amountCurrent: 500000.00,
                amountCompare: -50
            });
        }

        return data;
    },

    /**
     * 获取住院预交金进项表格数据
     */
    getInpatientPrepaymentIncomeTable(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateInpatientPrepaymentIncomeData(params);
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = allData.length;
                const startIndex = (page - 1) * pageSize;
                const list = allData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成住院预交金进项数据
     */
    generateInpatientPrepaymentIncomeData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            data.push({
                date: dateStr,
                countLast: 800,
                countCurrent: 900,
                countCompare: 12,
                amountLast: 800000.00,
                amountCurrent: 400000.00,
                amountCompare: -50
            });
        }

        return data;
    },

    /**
     * 获取住院预交金退项表格数据
     */
    getInpatientPrepaymentRefundTable(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const allData = this.generateInpatientPrepaymentRefundData(params);
                const page = params.page || 1;
                const pageSize = params.pageSize || 10;
                const total = allData.length;
                const startIndex = (page - 1) * pageSize;
                const list = allData.slice(startIndex, startIndex + pageSize);

                resolve({
                    code: 200,
                    data: { list, total, page, pageSize }
                });
            }, 300);
        });
    },

    /**
     * 生成住院预交金退项数据
     */
    generateInpatientPrepaymentRefundData(params = {}) {
        const dimension = params.dimension || 'day';
        const count = dimension === 'month' ? 12 : 55;
        const data = [];
        const baseDate = new Date('2025-01-12');

        for (let i = 0; i < count; i++) {
            let dateStr;
            if (dimension === 'month') {
                const d = new Date(baseDate);
                d.setMonth(d.getMonth() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
            } else {
                const d = new Date(baseDate);
                d.setDate(d.getDate() - i);
                dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
            }

            data.push({
                date: dateStr,
                countLast: 200,
                countCurrent: 200,
                countCompare: 0,
                amountLast: 200000.00,
                amountCurrent: 100000.00,
                amountCompare: -50
            });
        }

        return data;
    },

    /**
     * 获取住院预交金趋势图表数据
     */
    getInpatientPrepaymentTrendChart(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const type = params.type || 'income_count';
                const isRefund = type.startsWith('refund');
                const isAmount = type.endsWith('amount');
                const categories = [];
                const currentData = [];
                const lastData = [];
                for (let i = 13; i >= 0; i--) {
                    const d = new Date('2025-01-12');
                    d.setDate(d.getDate() - i);
                    categories.push(`${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`);
                    currentData.push(Math.floor(Math.random() * 60) + 80);
                    lastData.push(Math.floor(Math.random() * 60) + 50);
                }

                let title;
                if (isRefund) {
                    title = isAmount ? '退院金额分析' : '退院人次分析';
                } else {
                    title = isAmount ? '缴费金额分析' : '缴费人次分析';
                }

                resolve({
                    code: 200,
                    data: {
                        title,
                        legend: ['当前日期', '上年同期'],
                        categories,
                        currentData,
                        lastData
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取住院预交金渠道图表数据（进项）
     */
    getInpatientPrepaymentChannelChart(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const type = params.type || 'income_count';
                const isAmount = type.endsWith('amount');
                const titlePrefix = isAmount ? '缴费金额' : '缴费人次';

                resolve({
                    code: 200,
                    data: {
                        type,
                        channelAnalysis: [
                            { name: '窗口', value: 31, compare: -8 },
                            { name: '自助机', value: 31, compare: 5 },
                            { name: '掌上医院', value: 31, compare: 1 }
                        ],
                        payTypeAnalysis: [
                            { name: '微信', value: 31, compare: -8 },
                            { name: '支付宝', value: 31, compare: 5 },
                            { name: '银行卡', value: 31, compare: 1 },
                            { name: '现金', value: 31, compare: -14 }
                        ],
                        channelPayTypeAnalysis: {
                            categories: ['微信', '支付宝', '银行卡', '现金'],
                            series: [
                                { name: '窗口', data: [45, 30, 20, 25] },
                                { name: '自助机', data: [40, 35, 25, 10] },
                                { name: '掌上医院', data: [50, 20, 10, 0] }
                            ]
                        }
                    }
                });
            }, 300);
        });
    },

    /**
     * 获取住院预交金支付方式图表数据（退项）
     */
    getInpatientPrepaymentPayTypeChart(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const type = params.type || 'refund_count';
                const isAmount = type.endsWith('amount');
                const titlePrefix = isAmount ? '退院金额' : '退院人次';

                resolve({
                    code: 200,
                    data: {
                        type,
                        payTypeAnalysis: [
                            { name: '微信', value: 31, compare: -8 },
                            { name: '支付宝', value: 31, compare: 5 },
                            { name: '银行卡', value: 31, compare: 1 },
                            { name: '现金', value: 31, compare: -14 }
                        ]
                    }
                });
            }, 300);
        });
    },

    /**
     * 导出出院结算 Excel（模拟）
     */
    exportDischargeSettlement(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ code: 200, data: { message: '导出成功' } });
            }, 300);
        });
    },

    /**
     * 导出收费员结账统计 Excel（模拟）
     */
    exportCashierSettlement(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ code: 200, data: { message: '导出成功' } });
            }, 300);
        });
    },

    /**
     * 导出住院预交金统计 Excel（模拟）
     */
    exportInpatientPrepayment(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ code: 200, data: { message: '导出成功' } });
            }, 300);
        });
    },

    /**
     * 获取收费员结账统计图表数据
     */
    getCashierSettlementChart(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                const tab = params.tab || 'cashier';
                const categories = [];
                const seriesData = [];
                for (let i = 13; i >= 0; i--) {
                    const d = new Date('2025-01-12');
                    d.setDate(d.getDate() - i);
                    categories.push(`${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`);
                    seriesData.push(Math.floor(Math.random() * 60) + 80);
                }
                resolve({
                    code: 200,
                    data: {
                        title: tab === 'cashier' ? '收费员业务工作量分析' : (tab === 'source' ? '来源方式工作量分析' : '工作量分析'),
                        dateRange: '2025-01-12~2025-01-20',
                        subTitle: '收费员1',
                        categories,
                        series: [{ name: '业务量', data: seriesData }]
                    }
                });
            }, 300);
        });
    }
};
