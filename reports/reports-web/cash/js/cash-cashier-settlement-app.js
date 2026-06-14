/**
 * 收费员结账统计页面主逻辑
 */
class CashierSettlementController {
    constructor() {
        this.filter = {
            tab: 'cashier',
            dimension: 'day',
            startDate: '2025-09-22',
            endDate: '2025-10-22'
        };
        this.tableState = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: []
        };
        this.chart = null;

        this.init();
    }

    init() {
        this.initChart();
        this.bindEvents();
        this.initDateRangePicker();
        this.loadData();
    }

    initChart() {
        const chartDom = document.getElementById('cashierChart');
        if (chartDom) {
            this.chart = echarts.init(chartDom);
            window.addEventListener('resize', () => {
                if (this.chart) this.chart.resize();
            });
        }
    }

    bindEvents() {
        document.querySelectorAll('#tabFilter .service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleTabChange(e));
        });

        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleDimensionChange(e));
        });

        document.getElementById('cashierPageSizeSelect').addEventListener('change', (e) => {
            this.tableState.pageSize = parseInt(e.target.value);
            this.tableState.currentPage = 1;
            this.loadTableData();
        });
    }

    initDateRangePicker() {
        const dateRangeInput = document.getElementById('dateRange');
        if (!dateRangeInput) return;

        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: ['2025/09/22', '2025/10/22'],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    const formatDate = (date) => {
                        const y = date.getFullYear();
                        const m = String(date.getMonth() + 1).padStart(2, '0');
                        const d = String(date.getDate()).padStart(2, '0');
                        return `${y}-${m}-${d}`;
                    };
                    this.filter.startDate = formatDate(selectedDates[0]);
                    this.filter.endDate = formatDate(selectedDates[1]);
                    this.tableState.currentPage = 1;
                    this.loadData();
                }
            }
        });
    }

    handleTabChange(e) {
        const tab = e.target;
        document.querySelectorAll('#tabFilter .service-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.filter.tab = tab.dataset.tab;
        this.tableState.currentPage = 1;
        this.loadData();
    }

    handleDimensionChange(e) {
        const btn = e.target;
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.filter.dimension = btn.dataset.value;
        this.tableState.currentPage = 1;
        this.loadData();
    }

    async loadData() {
        await Promise.all([
            this.loadOverview(),
            this.loadTableData(),
            this.loadChart()
        ]);
    }

    async loadOverview() {
        if (this.filter.tab === 'workload') {
            document.getElementById('overviewSection').innerHTML = '';
            return;
        }

        try {
            const body = await ReportAPI.getCashierSettlementOverview(this.filter);
            if (body && body.appointmentRegister !== undefined) {
                this.renderOverview(body);
            }
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        const itemMap = {
            appointmentRegister: { label: '预约挂号量', icon: 'blue' },
            appointmentFetch: { label: '预约取号量', icon: 'cyan' },
            todayRegister: { label: '当日挂号量', icon: 'purple' },
            refund: { label: '退号量', icon: 'green' },
            outpatientCharge: { label: '门诊收费量', icon: 'orange' },
            outpatientRefund: { label: '门诊退费量', icon: 'red' },
            prepayment: { label: '收预交金量', icon: 'blue' },
            hospitalRefund: { label: '退院量', icon: 'cyan' },
            dischargeSettlement: { label: '出院结算量', icon: 'purple' }
        };

        const layout = [
            [5, ['appointmentRegister', 'appointmentFetch', 'todayRegister', 'refund', 'outpatientCharge']],
            [5, ['outpatientRefund', 'prepayment', 'hospitalRefund', 'dischargeSettlement', null]]
        ];

        let html = '';
        layout.forEach(([count, keys]) => {
            html += `<div class="d-flex gap-3 mb-3">`;
            keys.forEach(key => {
                if (!key) {
                    html += `
                        <div class="cashier-overview-placeholder" style="flex: 0 0 calc(20% - 9.6px); max-width: calc(20% - 9.6px); visibility: hidden;">
                            <div class="card stat-card cashier-overview-card">
                                <div class="card-body d-flex align-items-center">
                                    <div style="height: 100%;">&nbsp;</div>
                                </div>
                            </div>
                        </div>
                    `;
                    return;
                }
                const item = itemMap[key];
                const value = data[key];
                const compare = data[key + 'Compare'];
                const compareClass = compare >= 0 ? 'text-up' : 'text-down';
                const compareText = `${compare >= 0 ? '+' : ''}${compare}%`;

                html += `
                    <div class="flex-fill" style="flex: 0 0 calc(20% - 9.6px); max-width: calc(20% - 9.6px);">
                        <div class="card stat-card cashier-overview-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon ${item.icon}"><i class="bi bi-bar-chart-fill"></i></div>
                                <div class="ms-3">
                                    <div class="stat-value text-${item.icon}">${value.toLocaleString('zh-CN')}</div>
                                    <div class="stat-label">${item.label} <i class="bi bi-info-circle info-icon"></i></div>
                                    <div class="stat-compare ${compareClass}">同比${compareText}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            });
            html += `</div>`;
        });

        document.getElementById('overviewSection').innerHTML = html;
    }

    async loadTableData() {
        try {
            const body = await ReportAPI.getCashierSettlementTable({
                tab: this.filter.tab,
                page: this.tableState.currentPage,
                pageSize: this.tableState.pageSize,
                dimension: this.filter.dimension,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate
            });
            if (body && body.list) {
                this.tableState.data = body.list;
                this.tableState.total = body.total;
                this.renderTable();
                this.renderPagination();
                this.updatePageInfo();
            }
        } catch (error) {
            console.error('Load table data failed:', error);
        }
    }

    renderTable() {
        const thead = document.getElementById('cashierTableHead');
        const tbody = document.getElementById('cashierTableBody');

        if (this.filter.tab === 'cashier') {
            const cashiers = ['收费员1', '收费员2', '收费员3', '收费员4', '收费员5', '收费员6', '收费员7', '收费员8'];
            let headHtml = `<tr><th>日期</th>`;
            cashiers.forEach(name => {
                headHtml += `<th class="text-center">${name}</th>`;
            });
            headHtml += `<th class="text-center">汇总</th></tr>`;
            thead.innerHTML = headHtml;

            let bodyHtml = '';
            this.tableState.data.forEach(row => {
                bodyHtml += `<tr><td>${row.date}</td>`;
                cashiers.forEach(name => {
                    bodyHtml += `<td class="text-center">${this.formatNumber(row[name])}</td>`;
                });
                bodyHtml += `<td class="text-center"><strong>${this.formatNumber(row['汇总'])}</strong></td></tr>`;
            });

            // 汇总行
            if (this.tableState.data.length > 0) {
                const summary = {};
                cashiers.forEach(name => summary[name] = 0);
                summary['汇总'] = 0;
                this.tableState.data.forEach(row => {
                    cashiers.forEach(name => summary[name] += parseFloat(row[name]));
                    summary['汇总'] += parseFloat(row['汇总']);
                });
                bodyHtml += `<tr class="table-active"><td><strong>汇总</strong></td>`;
                cashiers.forEach(name => {
                    bodyHtml += `<td class="text-center"><strong>${this.formatNumber(summary[name])}</strong></td>`;
                });
                bodyHtml += `<td class="text-center"><strong>${this.formatNumber(summary['汇总'])}</strong></td></tr>`;
            }
            tbody.innerHTML = bodyHtml;

        } else if (this.filter.tab === 'source') {
            const columns = ['预约挂号量', '预约取号量', '当日挂号量', '退号量', '门诊收费量', '门诊退费量', '收预交金量', '退院量', '出院结算量'];
            let headHtml = `<tr><th>日期</th>`;
            columns.forEach(name => {
                headHtml += `<th class="text-center">${name}</th>`;
            });
            headHtml += `<th class="text-center">汇总</th></tr>`;
            thead.innerHTML = headHtml;

            let bodyHtml = '';
            this.tableState.data.forEach(row => {
                bodyHtml += `<tr><td>${row.date}</td>`;
                columns.forEach(name => {
                    bodyHtml += `<td class="text-center">${this.formatNumber(row[name])}</td>`;
                });
                bodyHtml += `<td class="text-center"><strong>${this.formatNumber(row['汇总'])}</strong></td></tr>`;
            });

            if (this.tableState.data.length > 0) {
                const summary = {};
                columns.forEach(name => summary[name] = 0);
                summary['汇总'] = 0;
                this.tableState.data.forEach(row => {
                    columns.forEach(name => summary[name] += parseFloat(row[name]));
                    summary['汇总'] += parseFloat(row['汇总']);
                });
                bodyHtml += `<tr class="table-active"><td><strong>汇总</strong></td>`;
                columns.forEach(name => {
                    bodyHtml += `<td class="text-center"><strong>${this.formatNumber(summary[name])}</strong></td>`;
                });
                bodyHtml += `<td class="text-center"><strong>${this.formatNumber(summary['汇总'])}</strong></td></tr>`;
            }
            tbody.innerHTML = bodyHtml;

        } else {
            thead.innerHTML = `
                <tr>
                    <th>日期</th>
                    <th>收费员</th>
                    <th class="text-center">当日挂号</th>
                    <th class="text-center">有效挂号数</th>
                    <th class="text-center">预约挂号</th>
                    <th class="text-center">门诊收费</th>
                    <th class="text-center">门诊退费</th>
                </tr>
            `;

            let bodyHtml = '';
            this.tableState.data.forEach(row => {
                bodyHtml += `
                    <tr>
                        <td>${row.dateRange}</td>
                        <td>${row.cashier}</td>
                        <td class="text-center">${row.todayRegister.toLocaleString('zh-CN')}</td>
                        <td class="text-center">${row.effectiveRegister.toLocaleString('zh-CN')}</td>
                        <td class="text-center">${row.appointmentRegister.toLocaleString('zh-CN')}</td>
                        <td class="text-center">${row.outpatientCharge.toLocaleString('zh-CN')}</td>
                        <td class="text-center">${row.outpatientRefund.toLocaleString('zh-CN')}</td>
                    </tr>
                `;
            });
            tbody.innerHTML = bodyHtml;
        }
    }

    formatNumber(value) {
        if (value === undefined || value === null) return '-';
        const num = parseFloat(value);
        if (num % 1 === 0) return num.toLocaleString('zh-CN');
        return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    renderPagination() {
        const totalPages = Math.ceil(this.tableState.total / this.tableState.pageSize);
        const current = this.tableState.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="cashierSettlementController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="cashierSettlementController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="cashierSettlementController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="cashierSettlementController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="cashierSettlementController.goToPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('cashierPagination').innerHTML = html;
    }

    updatePageInfo() {
        document.getElementById('cashierPageInfo').textContent = `${this.tableState.pageSize}条/页 共${this.tableState.total}条`;
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.tableState.total / this.tableState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.tableState.currentPage = page;
        this.loadTableData();
    }

    jumpToPage() {
        const input = document.getElementById('cashierJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToPage(page);
            input.value = '';
        }
    }

    async loadChart() {
        if (this.filter.tab === 'workload') {
            document.getElementById('chartSection').style.display = 'none';
            return;
        }
        document.getElementById('chartSection').style.display = 'flex';

        try {
            const body = await ReportAPI.getCashierSettlementChart({
                tab: this.filter.tab,
                dimension: this.filter.dimension,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate
            });
            if (body && body.categories) {
                this.renderChart(body);
            }
        } catch (error) {
            console.error('Load chart failed:', error);
        }
    }

    renderChart(data) {
        document.getElementById('chartTitle').textContent = data.title;
        document.getElementById('chartDateRange').textContent = data.dateRange;
        document.getElementById('chartSubTitle').textContent = data.subTitle;

        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            grid: {
                left: 40,
                right: 20,
                bottom: 40,
                top: 30,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: data.categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#595959', fontSize: 12 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            series: [{
                type: 'bar',
                barWidth: '40%',
                itemStyle: { color: '#1890ff' },
                data: data.data,
                label: {
                    show: false
                }
            }]
        };

        this.chart.setOption(option, true);
    }
}

function jumpToCashierPage() {
    cashierSettlementController.jumpToPage();
}

function exportCashierReport() {
    const controller = cashierSettlementController;
    const data = controller.tableState.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    let headers, rows;
    const tab = controller.filter.tab;

    if (tab === 'cashier') {
        const cashiers = ['收费员1', '收费员2', '收费员3', '收费员4', '收费员5', '收费员6', '收费员7', '收费员8'];
        headers = ['日期', ...cashiers, '汇总'];
        rows = data.map(row => [row.date, ...cashiers.map(name => row[name]), row['汇总']]);
    } else if (tab === 'source') {
        const columns = ['预约挂号量', '预约取号量', '当日挂号量', '退号量', '门诊收费量', '门诊退费量', '收预交金量', '退院量', '出院结算量'];
        headers = ['日期', ...columns, '汇总'];
        rows = data.map(row => [row.date, ...columns.map(name => row[name]), row['汇总']]);
    } else {
        headers = ['日期', '收费员', '当日挂号', '有效挂号数', '预约挂号', '门诊收费', '门诊退费'];
        rows = data.map(row => [row.dateRange, row.cashier, row.todayRegister, row.effectiveRegister, row.appointmentRegister, row.outpatientCharge, row.outpatientRefund]);
    }

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = headers.map(() => ({ wch: 14 }));

    const range = XLSX.utils.decode_range(ws['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: 0, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        ws[cellAddress].s = {
            font: { bold: true, sz: 11 },
            fill: { fgColor: { rgb: 'E6F7FF' } },
            alignment: { horizontal: 'center', vertical: 'center' },
            border: {
                top: { style: 'thin', color: { rgb: 'D9D9D9' } },
                bottom: { style: 'thin', color: { rgb: 'D9D9D9' } },
                left: { style: 'thin', color: { rgb: 'D9D9D9' } },
                right: { style: 'thin', color: { rgb: 'D9D9D9' } }
            }
        };
    }
    for (let R = 1; R <= range.e.r; ++R) {
        for (let C = range.s.c; C <= range.e.c; ++C) {
            const cellAddress = XLSX.utils.encode_cell({ r: R, c: C });
            if (!ws[cellAddress]) ws[cellAddress] = {};
            if (!ws[cellAddress].s) ws[cellAddress].s = {};
            ws[cellAddress].s.border = {
                top: { style: 'thin', color: { rgb: 'D9D9D9' } },
                bottom: { style: 'thin', color: { rgb: 'D9D9D9' } },
                left: { style: 'thin', color: { rgb: 'D9D9D9' } },
                right: { style: 'thin', color: { rgb: 'D9D9D9' } }
            };
            ws[cellAddress].s.alignment = { horizontal: 'center', vertical: 'center' };
        }
    }

    const sheetName = tab === 'cashier' ? '按收费员统计' : tab === 'source' ? '按来源方式统计' : '工作量报表';
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `收费员结账统计_${dateStr}.xlsx`);
}

const cashierSettlementController = new CashierSettlementController();
