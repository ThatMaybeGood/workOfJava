/**
 * 出院结算报表页面主逻辑
 */

/**
 * 默认统计区间：今天往前 30 天。
 * 原先写死成 2025-09-22 ~ 2025-10-22，是过期的固定日期，
 * 页面一打开就查不到数据（测试库和真实库都一样）。
 *
 * 必须用 yyyy-MM-dd 短横线：后端请求 DTO 上是
 * @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")，
 * 传斜杠会反序列化失败，接口直接返回"请求参数错误"。
 * 日期选择框显示用的斜杠由 flatpickr 的 dateFormat 单独控制，两者互不影响。
 */
function defaultDateRange() {
    const fmt = (d) => d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0');
    const end = new Date();
    const start = new Date(end.getTime() - 30 * 24 * 60 * 60 * 1000);
    return { startDate: fmt(start), endDate: fmt(end) };
}

class DischargeSettlementController {
    constructor() {
        this.filter = Object.assign({
            dimension: 'day'
        }, defaultDateRange());
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
        this.drillChart = echarts.init(document.getElementById('drillChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
            this.drillChart && this.drillChart.resize();
        });

        // 紧凑布局（iframe 宽度 <=1300px，即 1440 屏）切换时按已加载数据重绘图表
        this.compactMq = window.matchMedia('(max-width: 1300px)');
        this.compactMq.addEventListener('change', () => {
            if (this.chartData) {
                this.renderCharts(this.chartData);
            }
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

        // 钻取弹窗关闭：按钮、遮罩点击、Esc
        document.getElementById('drillModalClose').addEventListener('click', () => this.closeDrillModal());
        document.getElementById('drillModal').addEventListener('click', (e) => {
            if (e.target === e.currentTarget) this.closeDrillModal();
        });
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') this.closeDrillModal();
        });
    }

    initDateRangePicker() {
        this.resetDatePicker();
    }

    /** 按天/按月切换时重建日期控件：按月用月份范围选择器(只选月),按天用flatpickr */
    resetDatePicker() {
        const dateRangeInput = document.getElementById('dateRange');
        const monthWrap = document.getElementById('monthRangePicker');
        if (!dateRangeInput || !monthWrap) return;

        const monthMode = this.filter.dimension === 'month';
        const formatDate = (date) => {
            const y = date.getFullYear();
            const m = String(date.getMonth() + 1).padStart(2, '0');
            const d = String(date.getDate()).padStart(2, '0');
            return `${y}-${m}-${d}`;
        };

        if (this.datePicker) {
            this.datePicker.destroy();
            this.datePicker = null;
        }
        if (this.monthPicker) {
            this.monthPicker.destroy();
            this.monthPicker = null;
        }

        if (monthMode) {
            dateRangeInput.parentElement.style.display = 'none';
            monthWrap.style.display = '';
            // 已选范围对齐到粒度边界:起始月1号 ~ 结束月最后一天
            const s = new Date(this.filter.startDate + 'T00:00:00');
            const e = new Date(this.filter.endDate + 'T00:00:00');
            this.filter.startDate = formatDate(new Date(s.getFullYear(), s.getMonth(), 1));
            this.filter.endDate = formatDate(new Date(e.getFullYear(), e.getMonth() + 1, 0));
            this.monthPicker = new MonthRangePicker(monthWrap, {
                start: this.filter.startDate.slice(0, 7),
                end: this.filter.endDate.slice(0, 7),
                onConfirm: (start, end) => {
                    const [sy, sm] = start.split('-').map(Number);
                    const [ey, em] = end.split('-').map(Number);
                    this.filter.startDate = formatDate(new Date(sy, sm - 1, 1));
                    this.filter.endDate = formatDate(new Date(ey, em, 0));
                    this.tableState.currentPage = 1;
                    this.loadData();
                }
            });
            return;
        }

        monthWrap.style.display = 'none';
        dateRangeInput.parentElement.style.display = '';
        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: [this.filter.startDate.replace(/-/g, '/'), this.filter.endDate.replace(/-/g, '/')],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
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
        this.resetDatePicker();
        // 表格首列表头跟随统计粒度：按月显示「月份」
        document.getElementById('itemDateHeader').textContent = this.filter.dimension === 'month' ? '月份' : '日期';
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
        this.chartData = data;
        this.renderPieChart(this.charts.channel, data.channelAnalysis, '结算渠道分析', 'CHANNEL');
        this.renderPieChart(this.charts.patientType, data.patientTypeAnalysis, '结算费别人次分析', null);
        this.renderPieChart(this.charts.amountType, data.amountTypeAnalysis, '结算金额支付方式分析', 'PAY_TYPE');
    }

    /**
     * 环形饼图 + 右侧自定义图例（参考门诊财务风格）：
     * 平时图上不出标签，悬停扇区才显示 名称/数值/百分比；可钻取的图例项蓝名带▸
     * @param drillType CHANNEL/PAY_TYPE 可点击钻取，null 不可点
     */
    renderPieChart(chart, data, title, drillType) {
        const colors = ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#f5222d', '#722ed1'];
        const compact = window.matchMedia('(max-width: 1300px)').matches;
        const option = {
            // 标题只在 HTML 卡片上有一份，不画进 canvas，避免重复
            tooltip: {
                trigger: 'item',
                formatter: (params) => {
                    const item = data[params.dataIndex];
                    const compareText = item.compare >= 0 ? `+${item.compare}%` : `${item.compare}%`;
                    return `${params.name}: ${params.value} (${params.percent}%)<br/>同比${compareText}`;
                }
            },
            legend: { show: false },
            color: colors,
            series: [
                {
                    type: 'pie',
                    radius: ['48%', '72%'],
                    center: ['50%', '50%'],
                    avoidLabelOverlap: true,
                    label: { show: false },
                    labelLine: { show: false },
                    emphasis: {
                        label: {
                            show: true,
                            fontSize: 13,
                            fontWeight: 'bold',
                            formatter: '{b}\n{c} ({d}%)'
                        },
                        itemStyle: { shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.15)' }
                    },
                    data: data
                }
            ]
        };
        if (compact) {
            // 紧凑布局：饼图缩小，图例列表在下方不受影响
            option.series[0].radius = ['40%', '60%'];
            option.series[0].center = ['50%', '48%'];
        }
        chart.setOption(option, true);

        // 钻取：点扇区或点图例都可弹明细
        chart.off('click');
        if (drillType) {
            chart.on('click', (params) => {
                if (params.componentType === 'series') {
                    this.openDrillModal(drillType, params.name);
                }
            });
        }
        this.renderChartLegend(chart, data, colors, drillType);
    }

    /**
     * 饼图右侧图例列表：色块/名称/数值/占比/同比；drillType 非空时项可点钻取
     */
    renderChartLegend(chart, data, colors, drillType) {
        const dom = chart.getDom();
        const wrapper = dom.parentNode;
        let listEl = wrapper.querySelector('.chart-legend-list');
        if (!listEl) {
            listEl = document.createElement('div');
            listEl.className = 'chart-legend-list';
            wrapper.appendChild(listEl);
        }
        const total = data.reduce((sum, d) => sum + (parseFloat(d.value) || 0), 0);
        listEl.innerHTML = data.map((item, idx) => {
            const val = parseFloat(item.value) || 0;
            const pct = total > 0 ? (val / total * 100).toFixed(1) + '%' : '';
            const perText = (item.compare >= 0 ? '+' : '') + item.compare + '%';
            const perCls = item.compare >= 0 ? 'up' : 'down';
            const drillable = drillType ? ' drillable' : '';
            const drillAttr = drillType ? ` data-drill="${item.name}" title="点击查看明细"` : '';
            return `<div class="chart-legend-item${drillable}"${drillAttr}>
                <span class="chart-legend-dot" style="background:${colors[idx % colors.length]}"></span>
                <span class="chart-legend-name">${item.name}</span>
                <span class="chart-legend-val">${val}</span>
                <span class="chart-legend-pct">${pct}</span>
                <span class="chart-legend-per ${perCls}">${perText}</span>
            </div>`;
        }).join('');

        if (drillType) {
            listEl.querySelectorAll('.chart-legend-item.drillable').forEach(el => {
                el.addEventListener('click', () => this.openDrillModal(drillType, el.getAttribute('data-drill')));
            });
        }
    }

    /**
     * 打开钻取明细弹窗：渠道→费别人次明细，支付方式→收/退占比
     */
    async openDrillModal(drillType, itemName) {
        const isChannel = drillType === 'CHANNEL';
        document.getElementById('drillModalTitle').textContent =
            isChannel ? `${itemName} · 费别人次明细` : `${itemName} · 收退占比`;
        document.getElementById('drillModal').classList.add('show');
        // 弹窗初始为隐藏，echarts 初始化时是 0 尺寸，显示后必须手动 resize 否则饼图极小
        this.drillChart.resize();
        this.drillChart.showLoading({ text: '加载中...', color: '#0b5e7e', maskColor: 'rgba(255,255,255,0.6)' });

        try {
            const list = await ReportAPI.getDischargeSettlementChartDetail(Object.assign({}, this.filter, {
                drillType, itemName
            }));
            const data = (list || []).filter(d => d && d.name != null);
            if (!data.length) {
                this.drillChart.hideLoading();
                this.drillChart.clear();
                this.drillChart.setOption({
                    title: { text: '暂无明细数据', left: 'center', top: 'center', textStyle: { fontSize: 14, color: '#8c9aa5', fontWeight: 500 } }
                });
                return;
            }
            this.renderDrillPie(data, isChannel);
        } catch (error) {
            // 全局错误弹窗已提示，这里仅兜底关闭图表loading
            this.drillChart.hideLoading();
            console.error('Load drill detail failed:', error);
        }
    }

    renderDrillPie(data, isChannel) {
        this.drillChart.hideLoading();
        // 收=绿 退=橙；费别人次用常规色板
        const colors = isChannel
            ? ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#722ed1', '#f5222d']
            : ['#52c41a', '#fa8c16'];
        this.drillChart.setOption({
            tooltip: {
                trigger: 'item',
                formatter: (params) => `${params.name}: ${params.value} (${params.percent}%)`
            },
            legend: { show: false },
            color: colors,
            series: [
                {
                    type: 'pie',
                    radius: ['42%', '68%'],
                    center: ['50%', '50%'],
                    label: { show: false },
                    labelLine: { show: false },
                    emphasis: {
                        label: {
                            show: true,
                            fontSize: 15,
                            fontWeight: 'bold',
                            formatter: '{b}\n{c} ({d}%)'
                        },
                        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.18)' }
                    },
                    data: data
                }
            ]
        }, true);
        this.drillChart.resize();
        // 弹窗图例与主页饼图同款布局：横条列表在饼下方，超 5 条分页
        this.drillLegendState = { page: 1, data: data, colors: colors };
        this.renderDrillLegend();
    }

    /** 钻取弹窗图例：色块/名称/数值/占比，超过 5 条分页（参考门诊财务） */
    renderDrillLegend() {
        const PAGE_SIZE = 5;
        const state = this.drillLegendState;
        const listEl = document.getElementById('drillLegend');
        if (!listEl || !state) return;
        const data = state.data;
        const total = data.reduce((sum, d) => sum + (parseFloat(d.value) || 0), 0);
        const totalPages = Math.max(1, Math.ceil(data.length / PAGE_SIZE));
        if (state.page > totalPages) state.page = totalPages;
        const start = (state.page - 1) * PAGE_SIZE;
        const pageData = data.slice(start, start + PAGE_SIZE);

        let html = pageData.map((item, idx) => {
            const val = parseFloat(item.value) || 0;
            const pct = total > 0 ? (val / total * 100).toFixed(1) + '%' : '';
            return `<div class="chart-legend-item">
                <span class="chart-legend-dot" style="background:${state.colors[(start + idx) % state.colors.length]}"></span>
                <span class="chart-legend-name">${item.name}</span>
                <span class="chart-legend-val">${val}</span>
                <span class="chart-legend-pct">${pct}</span>
            </div>`;
        }).join('');

        if (totalPages > 1) {
            html += `<div class="chart-legend-pagination">
                <button type="button" ${state.page <= 1 ? 'disabled' : ''} data-page="${state.page - 1}">上一页</button>
                <span class="page-info">${state.page}/${totalPages} 页</span>
                <button type="button" ${state.page >= totalPages ? 'disabled' : ''} data-page="${state.page + 1}">下一页</button>
            </div>`;
        }
        listEl.innerHTML = html;
        listEl.querySelectorAll('button[data-page]').forEach(btn => {
            btn.addEventListener('click', () => {
                state.page = parseInt(btn.getAttribute('data-page'));
                this.renderDrillLegend();
            });
        });
    }

    closeDrillModal() {
        document.getElementById('drillModal').classList.remove('show');
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

    const dateHeader = dischargeSettlementController.filter.dimension === 'month' ? '月份' : '日期';
    const headers = [dateHeader, '总出院人数-去年同期', '总出院人数-当前日期', '总出院人数-同比',
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
