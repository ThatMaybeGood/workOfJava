/**
 * 出院结算报表页面主逻辑
 */
class DischargeSettlementController {
    constructor() {
        this.filter = {
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
        this.charts = {};

        this.init();
    }

    init() {
        this.initCharts();
        this.bindEvents();
        this.initDateRangePicker();
        this.loadData();
    }

    initCharts() {
        this.charts.channel = echarts.init(document.getElementById('channelChart'));
        this.charts.patientType = echarts.init(document.getElementById('patientTypeChart'));
        this.charts.amountType = echarts.init(document.getElementById('amountTypeChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleDimensionChange(e));
        });

        document.getElementById('settlementPageSizeSelect').addEventListener('change', (e) => {
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
            this.loadCharts(),
            this.loadTableData()
        ]);
    }

    async loadOverview() {
        try {
            const body = await ReportAPI.getDischargeSettlementOverview(this.filter);
            if (body && body.totalDischargeCount !== undefined) {
                this.renderOverview(body);
            }
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        document.getElementById('totalDischargeCount').textContent = data.totalDischargeCount.toLocaleString('zh-CN');
        document.getElementById('totalDischargeCompare').textContent = `同比${data.totalDischargeCompare >= 0 ? '+' : ''}${data.totalDischargeCompare}%`;
        document.getElementById('totalDischargeCompare').className = `stat-compare ${data.totalDischargeCompare >= 0 ? 'text-up' : 'text-down'}`;

        document.getElementById('dischargedCount').textContent = data.dischargedCount.toLocaleString('zh-CN');
        document.getElementById('dischargedCompare').textContent = `同比${data.dischargedCompare >= 0 ? '+' : ''}${data.dischargedCompare}%`;
        document.getElementById('dischargedCompare').className = `stat-compare ${data.dischargedCompare >= 0 ? 'text-up' : 'text-down'}`;

        document.getElementById('notDischargedCount').textContent = data.notDischargedCount.toLocaleString('zh-CN');
        document.getElementById('notDischargedCompare').textContent = `同比${data.notDischargedCompare >= 0 ? '+' : ''}${data.notDischargedCompare}%`;
        document.getElementById('notDischargedCompare').className = `stat-compare ${data.notDischargedCompare >= 0 ? 'text-up' : 'text-down'}`;

        document.getElementById('settlementAmount').textContent = data.settlementAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById('settlementAmountCompare').textContent = `同比${data.settlementAmountCompare >= 0 ? '+' : ''}${data.settlementAmountCompare}%`;
        document.getElementById('settlementAmountCompare').className = `stat-compare ${data.settlementAmountCompare >= 0 ? 'text-up' : 'text-down'}`;
    }

    async loadCharts() {
        try {
            const body = await ReportAPI.getDischargeSettlementCharts(this.filter);
            if (body && body.channelAnalysis) {
                this.renderCharts(body);
            }
        } catch (error) {
            console.error('Load charts failed:', error);
        }
    }

    renderCharts(data) {
        this.renderPieChart(this.charts.channel, data.channelAnalysis, '结算渠道分析');
        this.renderPieChart(this.charts.patientType, data.patientTypeAnalysis, '结算费别人次分析');
        this.renderPieChart(this.charts.amountType, data.amountTypeAnalysis, '结算费别金额分析');
    }

    renderPieChart(chart, data, title) {
        const colors = ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#f5222d', '#722ed1'];
        const option = {
            title: {
                text: title,
                left: 'left',
                top: 0,
                textStyle: {
                    fontSize: 14,
                    fontWeight: 600,
                    color: '#262626'
                }
            },
            tooltip: {
                trigger: 'item',
                formatter: (params) => {
                    const item = data[params.dataIndex];
                    const compareText = item.compare >= 0 ? `+${item.compare}%` : `${item.compare}%`;
                    return `${params.name}: ${params.value} (${params.percent}%)\n同比${compareText}`;
                }
            },
            legend: {
                orient: 'vertical',
                right: 10,
                top: 'center',
                itemWidth: 10,
                itemHeight: 10,
                textStyle: { color: '#595959', fontSize: 12 },
                formatter: (name) => {
                    const item = data.find(d => d.name === name);
                    const compareText = item.compare >= 0 ? `+${item.compare}%` : `${item.compare}%`;
                    const compareColor = item.compare >= 0 ? '#f5222d' : '#52c41a';
                    return `{name|${name}} {compare|（同比${compareText}）}`;
                },
                textStyle: {
                    rich: {
                        name: { color: '#595959', fontSize: 12 },
                        compare: { color: '#595959', fontSize: 12 }
                    }
                }
            },
            color: colors,
            series: [
                {
                    type: 'pie',
                    radius: ['45%', '70%'],
                    center: ['32%', '55%'],
                    avoidLabelOverlap: true,
                    label: {
                        show: true,
                        formatter: '{c}\n({d}%)',
                        fontSize: 11,
                        color: '#595959'
                    },
                    labelLine: {
                        show: true,
                        length: 10,
                        length2: 10
                    },
                    data: data
                }
            ]
        };
        chart.setOption(option, true);
    }

    async loadTableData() {
        try {
            const body = await ReportAPI.getDischargeSettlementTable({
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
        const tbody = document.getElementById('settlementTableBody');
        let html = '';
        this.tableState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.date}</td>
                    <td>${row.totalLast}</td>
                    <td>${row.totalCurrent}</td>
                    <td class="${row.totalCompare >= 0 ? 'text-up' : 'text-down'}">${row.totalCompare >= 0 ? '+' : ''}${row.totalCompare}%</td>
                    <td>${row.dischargedLast.toLocaleString('zh-CN')}</td>
                    <td>${row.dischargedCurrent.toLocaleString('zh-CN')}</td>
                    <td class="${row.dischargedCompare >= 0 ? 'text-up' : 'text-down'}">${row.dischargedCompare >= 0 ? '+' : ''}${row.dischargedCompare}%</td>
                    <td>${row.notDischargedLast.toLocaleString('zh-CN')}</td>
                    <td>${row.notDischargedCurrent.toLocaleString('zh-CN')}</td>
                    <td class="${row.notDischargedCompare >= 0 ? 'text-up' : 'text-down'}">${row.notDischargedCompare >= 0 ? '+' : ''}${row.notDischargedCompare}%</td>
                    <td>${row.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td>${row.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td class="${row.amountCompare >= 0 ? 'text-up' : 'text-down'}">${row.amountCompare >= 0 ? '+' : ''}${row.amountCompare}%</td>
                </tr>
            `;
        });
        tbody.innerHTML = html;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.tableState.total / this.tableState.pageSize);
        const current = this.tableState.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="dischargeSettlementController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="dischargeSettlementController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="dischargeSettlementController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="dischargeSettlementController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="dischargeSettlementController.goToPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('settlementPagination').innerHTML = html;
    }

    updatePageInfo() {
        document.getElementById('settlementPageInfo').textContent = `${this.tableState.pageSize}条/页 共${this.tableState.total}条`;
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.tableState.total / this.tableState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.tableState.currentPage = page;
        this.loadTableData();
    }

    jumpToPage() {
        const input = document.getElementById('settlementJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToPage(page);
            input.value = '';
        }
    }
}

function jumpToSettlementPage() {
    dischargeSettlementController.jumpToPage();
}

function exportReport() {
    const data = dischargeSettlementController.tableState.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = ['日期', '总出院人数-去年同期', '总出院人数-当前日期', '总出院人数-同比',
        '已出院人数-去年同期', '已出院人数-当前日期', '已出院人数-同比',
        '未出院人数-去年同期', '未出院人数-当前日期', '未出院人数-同比',
        '结算金额-去年同期', '结算金额-当前日期', '结算金额-同比'];
    const rows = data.map(row => [
        row.date, row.totalLast, row.totalCurrent, `${row.totalCompare}%`,
        row.dischargedLast, row.dischargedCurrent, `${row.dischargedCompare}%`,
        row.notDischargedLast, row.notDischargedCurrent, `${row.notDischargedCompare}%`,
        row.amountLast, row.amountCurrent, `${row.amountCompare}%`
    ]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [
        { wch: 14 }, { wch: 12 }, { wch: 12 }, { wch: 10 },
        { wch: 14 }, { wch: 14 }, { wch: 10 },
        { wch: 14 }, { wch: 14 }, { wch: 10 },
        { wch: 16 }, { wch: 16 }, { wch: 10 }
    ];

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

    XLSX.utils.book_append_sheet(wb, ws, '出院结算报表');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `出院结算报表_${dateStr}.xlsx`);
}

const dischargeSettlementController = new DischargeSettlementController();
