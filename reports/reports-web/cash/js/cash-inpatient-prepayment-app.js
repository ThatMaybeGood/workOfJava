/**
 * 住院预交金统计页面主逻辑
 */
class InpatientPrepaymentController {
    constructor() {
        this.filter = {
            tab: 'summary',
            dimension: 'day',
            startDate: '2025-09-22',
            endDate: '2025-10-22'
        };
        this.summaryTableState = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: []
        };
        this.incomeTableState = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: []
        };
        this.refundTableState = {
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
        this.charts.summaryTrend = echarts.init(document.getElementById('summaryTrendChart'));
        this.charts.summaryChannelPie = echarts.init(document.getElementById('summaryChannelPieChart'));
        this.charts.summaryPayTypePie = echarts.init(document.getElementById('summaryPayTypePieChart'));
        this.charts.summaryChannelBar = echarts.init(document.getElementById('summaryChannelBarChart'));
        this.charts.incomeTrend = echarts.init(document.getElementById('incomeTrendChart'));
        this.charts.incomeChannelPie = echarts.init(document.getElementById('incomeChannelPieChart'));
        this.charts.incomePayTypePie = echarts.init(document.getElementById('incomePayTypePieChart'));
        this.charts.incomeChannelBar = echarts.init(document.getElementById('incomeChannelBarChart'));
        this.charts.refundTrend = echarts.init(document.getElementById('refundTrendChart'));
        this.charts.refundPayTypePie = echarts.init(document.getElementById('refundPayTypePieChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart && chart.resize());
        });
    }

    bindEvents() {
        document.querySelectorAll('#tabFilter .service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleTabChange(e));
        });

        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleDimensionChange(e));
        });

        document.querySelectorAll('#incomeTabFilter .service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleIncomeTypeChange(e));
        });

        document.querySelectorAll('#summaryChartTabFilter .service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleSummaryChartTypeChange(e));
        });

        document.querySelectorAll('#refundTabFilter .service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleRefundTypeChange(e));
        });

        document.getElementById('summaryPageSizeSelect').addEventListener('change', (e) => {
            this.summaryTableState.pageSize = parseInt(e.target.value);
            this.summaryTableState.currentPage = 1;
            this.loadSummaryTable();
        });

        document.getElementById('incomePageSizeSelect').addEventListener('change', (e) => {
            this.incomeTableState.pageSize = parseInt(e.target.value);
            this.incomeTableState.currentPage = 1;
            this.loadIncomeTable();
        });

        document.getElementById('refundPageSizeSelect').addEventListener('change', (e) => {
            this.refundTableState.pageSize = parseInt(e.target.value);
            this.refundTableState.currentPage = 1;
            this.loadRefundTable();
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
                    this.summaryTableState.currentPage = 1;
                    this.incomeTableState.currentPage = 1;
                    this.refundTableState.currentPage = 1;
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
        this.loadData();
    }

    handleDimensionChange(e) {
        const btn = e.target;
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.filter.dimension = btn.dataset.value;
        this.summaryTableState.currentPage = 1;
        this.incomeTableState.currentPage = 1;
        this.refundTableState.currentPage = 1;
        this.loadData();
    }

    handleSummaryChartTypeChange(e) {
        const tab = e.target;
        document.querySelectorAll('#summaryChartTabFilter .service-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.loadSummaryChart(tab.dataset.tab);
    }

    handleIncomeTypeChange(e) {
        const tab = e.target;
        document.querySelectorAll('#incomeTabFilter .service-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.loadIncomeChart(tab.dataset.tab);
    }

    handleRefundTypeChange(e) {
        const tab = e.target;
        document.querySelectorAll('#refundTabFilter .service-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.loadRefundChart(tab.dataset.tab);
    }

    async loadData() {
        this.switchSection();

        if (this.filter.tab === 'summary') {
            const activeType = document.querySelector('#summaryChartTabFilter .service-tab.active').dataset.tab;
            await Promise.all([
                this.loadSummaryTable(),
                this.loadSummaryChart(activeType)
            ]);
        } else if (this.filter.tab === 'income') {
            const activeType = document.querySelector('#incomeTabFilter .service-tab.active').dataset.tab;
            await Promise.all([
                this.loadIncomeOverview(),
                this.loadIncomeTable(),
                this.loadIncomeChart(activeType)
            ]);
        } else if (this.filter.tab === 'refund') {
            const activeType = document.querySelector('#refundTabFilter .service-tab.active').dataset.tab;
            await Promise.all([
                this.loadRefundOverview(),
                this.loadRefundTable(),
                this.loadRefundChart(activeType)
            ]);
        }
    }

    switchSection() {
        document.getElementById('summarySection').style.display = this.filter.tab === 'summary' ? 'block' : 'none';
        document.getElementById('incomeSection').style.display = this.filter.tab === 'income' ? 'block' : 'none';
        document.getElementById('refundSection').style.display = this.filter.tab === 'refund' ? 'block' : 'none';

        // 切换tab后resize图表
        setTimeout(() => {
            Object.values(this.charts).forEach(chart => chart && chart.resize());
        }, 100);
    }

    async loadIncomeOverview() {
        try {
            const body = await ReportAPI.getInpatientPrepaymentOverview(this.filter);
            if (body && body.prepaymentCount !== undefined) {
                this.renderIncomeOverview(body);
            }
        } catch (error) {
            console.error('Load income overview failed:', error);
        }
    }

    renderIncomeOverview(data) {
        document.getElementById('incomePrepaymentCount').textContent = data.prepaymentCount.toLocaleString('zh-CN');
        document.getElementById('incomePrepaymentCountCompare').textContent = `同比${data.prepaymentCountCompare >= 0 ? '+' : ''}${data.prepaymentCountCompare}%`;
        document.getElementById('incomePrepaymentCountCompare').className = `stat-compare ${data.prepaymentCountCompare >= 0 ? 'text-up' : 'text-down'}`;

        document.getElementById('incomePrepaymentAmount').textContent = data.prepaymentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById('incomePrepaymentAmountCompare').textContent = `同比${data.prepaymentAmountCompare >= 0 ? '+' : ''}${data.prepaymentAmountCompare}%`;
        document.getElementById('incomePrepaymentAmountCompare').className = `stat-compare ${data.prepaymentAmountCompare >= 0 ? 'text-up' : 'text-down'}`;
    }

    async loadRefundOverview() {
        try {
            const body = await ReportAPI.getInpatientPrepaymentOverview(this.filter);
            if (body && body.prepaymentCount !== undefined) {
                this.renderRefundOverview(body);
            }
        } catch (error) {
            console.error('Load refund overview failed:', error);
        }
    }

    renderRefundOverview(data) {
        document.getElementById('refundCount').textContent = data.prepaymentCount.toLocaleString('zh-CN');
        document.getElementById('refundCountCompare').textContent = `同比${data.prepaymentCountCompare >= 0 ? '+' : ''}${data.prepaymentCountCompare}%`;
        document.getElementById('refundCountCompare').className = `stat-compare ${data.prepaymentCountCompare >= 0 ? 'text-up' : 'text-down'}`;

        document.getElementById('refundAmount').textContent = data.prepaymentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById('refundAmountCompare').textContent = `同比${data.prepaymentAmountCompare >= 0 ? '+' : ''}${data.prepaymentAmountCompare}%`;
        document.getElementById('refundAmountCompare').className = `stat-compare ${data.prepaymentAmountCompare >= 0 ? 'text-up' : 'text-down'}`;
    }

    async loadSummaryTable() {
        try {
            const body = await ReportAPI.getInpatientPrepaymentSummaryTable({
                dimension: this.filter.dimension,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate,
                page: this.summaryTableState.currentPage,
                pageSize: this.summaryTableState.pageSize
            });
            if (body && body.list) {
                this.summaryTableState.data = body.list;
                this.summaryTableState.total = body.total;
                this.renderSummaryTable();
                this.renderSummaryPagination();
                this.updateSummaryPageInfo();
            }
        } catch (error) {
            console.error('Load summary table failed:', error);
        }
    }

    renderSummaryTable() {
        const tbody = document.getElementById('summaryTableBody');
        let html = '';

        this.summaryTableState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.date}</td>
                    <td>${row.countLast.toLocaleString('zh-CN')}</td>
                    <td>${row.countCurrent.toLocaleString('zh-CN')}</td>
                    <td class="${row.countCompare >= 0 ? 'text-up' : 'text-down'}">${row.countCompare >= 0 ? '+' : ''}${row.countCompare}%</td>
                    <td>${row.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td>${row.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td class="${row.amountCompare >= 0 ? 'text-up' : 'text-down'}">${row.amountCompare >= 0 ? '+' : ''}${row.amountCompare}%</td>
                </tr>
            `;
        });

        if (this.summaryTableState.data.length > 0) {
            const summary = { countLast: 0, countCurrent: 0, amountLast: 0, amountCurrent: 0 };
            this.summaryTableState.data.forEach(row => {
                summary.countLast += row.countLast;
                summary.countCurrent += row.countCurrent;
                summary.amountLast += row.amountLast;
                summary.amountCurrent += row.amountCurrent;
            });
            const countCompare = summary.countLast === 0 ? 0 : Math.round((summary.countCurrent - summary.countLast) / summary.countLast * 100);
            const amountCompare = summary.amountLast === 0 ? 0 : Math.round((summary.amountCurrent - summary.amountLast) / summary.amountLast * 100);

            html += `
                <tr class="table-active">
                    <td><strong>汇总</strong></td>
                    <td><strong>${summary.countLast.toLocaleString('zh-CN')}</strong></td>
                    <td><strong>${summary.countCurrent.toLocaleString('zh-CN')}</strong></td>
                    <td class="${countCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${countCompare >= 0 ? '+' : ''}${countCompare}%</strong></td>
                    <td><strong>${summary.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td><strong>${summary.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td class="${amountCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${amountCompare >= 0 ? '+' : ''}${amountCompare}%</strong></td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    renderSummaryPagination() {
        const totalPages = Math.ceil(this.summaryTableState.total / this.summaryTableState.pageSize);
        const current = this.summaryTableState.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToSummaryPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToSummaryPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToSummaryPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToSummaryPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToSummaryPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('summaryPagination').innerHTML = html;
    }

    updateSummaryPageInfo() {
        document.getElementById('summaryPageInfo').textContent = `${this.summaryTableState.pageSize}条/页 共${this.summaryTableState.total}条`;
    }

    goToSummaryPage(page) {
        const totalPages = Math.ceil(this.summaryTableState.total / this.summaryTableState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.summaryTableState.currentPage = page;
        this.loadSummaryTable();
    }

    jumpToSummaryPage() {
        const input = document.getElementById('summaryJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToSummaryPage(page);
            input.value = '';
        }
    }

    async loadIncomeTable() {
        try {
            const body = await ReportAPI.getInpatientPrepaymentIncomeTable({
                dimension: this.filter.dimension,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate,
                page: this.incomeTableState.currentPage,
                pageSize: this.incomeTableState.pageSize
            });
            if (body && body.list) {
                this.incomeTableState.data = body.list;
                this.incomeTableState.total = body.total;
                this.renderIncomeTable();
                this.renderIncomePagination();
                this.updateIncomePageInfo();
            }
        } catch (error) {
            console.error('Load income table failed:', error);
        }
    }

    renderIncomeTable() {
        const tbody = document.getElementById('incomeTableBody');
        let html = '';

        this.incomeTableState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.date}</td>
                    <td>${row.countLast.toLocaleString('zh-CN')}</td>
                    <td>${row.countCurrent.toLocaleString('zh-CN')}</td>
                    <td class="${row.countCompare >= 0 ? 'text-up' : 'text-down'}">${row.countCompare >= 0 ? '+' : ''}${row.countCompare}%</td>
                    <td>${row.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td>${row.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td class="${row.amountCompare >= 0 ? 'text-up' : 'text-down'}">${row.amountCompare >= 0 ? '+' : ''}${row.amountCompare}%</td>
                </tr>
            `;
        });

        if (this.incomeTableState.data.length > 0) {
            const summary = { countLast: 0, countCurrent: 0, amountLast: 0, amountCurrent: 0 };
            this.incomeTableState.data.forEach(row => {
                summary.countLast += row.countLast;
                summary.countCurrent += row.countCurrent;
                summary.amountLast += row.amountLast;
                summary.amountCurrent += row.amountCurrent;
            });
            const countCompare = summary.countLast === 0 ? 0 : Math.round((summary.countCurrent - summary.countLast) / summary.countLast * 100);
            const amountCompare = summary.amountLast === 0 ? 0 : Math.round((summary.amountCurrent - summary.amountLast) / summary.amountLast * 100);

            html += `
                <tr class="table-active">
                    <td><strong>汇总</strong></td>
                    <td><strong>${summary.countLast.toLocaleString('zh-CN')}</strong></td>
                    <td><strong>${summary.countCurrent.toLocaleString('zh-CN')}</strong></td>
                    <td class="${countCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${countCompare >= 0 ? '+' : ''}${countCompare}%</strong></td>
                    <td><strong>${summary.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td><strong>${summary.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td class="${amountCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${amountCompare >= 0 ? '+' : ''}${amountCompare}%</strong></td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    renderIncomePagination() {
        const totalPages = Math.ceil(this.incomeTableState.total / this.incomeTableState.pageSize);
        const current = this.incomeTableState.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToIncomePage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToIncomePage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToIncomePage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToIncomePage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToIncomePage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('incomePagination').innerHTML = html;
    }

    updateIncomePageInfo() {
        document.getElementById('incomePageInfo').textContent = `${this.incomeTableState.pageSize}条/页 共${this.incomeTableState.total}条`;
    }

    goToIncomePage(page) {
        const totalPages = Math.ceil(this.incomeTableState.total / this.incomeTableState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.incomeTableState.currentPage = page;
        this.loadIncomeTable();
    }

    jumpToIncomePage() {
        const input = document.getElementById('incomeJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToIncomePage(page);
            input.value = '';
        }
    }

    async loadRefundTable() {
        try {
            const body = await ReportAPI.getInpatientPrepaymentRefundTable({
                dimension: this.filter.dimension,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate,
                page: this.refundTableState.currentPage,
                pageSize: this.refundTableState.pageSize
            });
            if (body && body.list) {
                this.refundTableState.data = body.list;
                this.refundTableState.total = body.total;
                this.renderRefundTable();
                this.renderRefundPagination();
                this.updateRefundPageInfo();
            }
        } catch (error) {
            console.error('Load refund table failed:', error);
        }
    }

    renderRefundTable() {
        const tbody = document.getElementById('refundTableBody');
        let html = '';

        this.refundTableState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.date}</td>
                    <td>${row.countLast.toLocaleString('zh-CN')}</td>
                    <td>${row.countCurrent.toLocaleString('zh-CN')}</td>
                    <td class="${row.countCompare >= 0 ? 'text-up' : 'text-down'}">${row.countCompare >= 0 ? '+' : ''}${row.countCompare}%</td>
                    <td>${row.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td>${row.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td class="${row.amountCompare >= 0 ? 'text-up' : 'text-down'}">${row.amountCompare >= 0 ? '+' : ''}${row.amountCompare}%</td>
                </tr>
            `;
        });

        if (this.refundTableState.data.length > 0) {
            const summary = { countLast: 0, countCurrent: 0, amountLast: 0, amountCurrent: 0 };
            this.refundTableState.data.forEach(row => {
                summary.countLast += row.countLast;
                summary.countCurrent += row.countCurrent;
                summary.amountLast += row.amountLast;
                summary.amountCurrent += row.amountCurrent;
            });
            const countCompare = summary.countLast === 0 ? 0 : Math.round((summary.countCurrent - summary.countLast) / summary.countLast * 100);
            const amountCompare = summary.amountLast === 0 ? 0 : Math.round((summary.amountCurrent - summary.amountLast) / summary.amountLast * 100);

            html += `
                <tr class="table-active">
                    <td><strong>汇总</strong></td>
                    <td><strong>${summary.countLast.toLocaleString('zh-CN')}</strong></td>
                    <td><strong>${summary.countCurrent.toLocaleString('zh-CN')}</strong></td>
                    <td class="${countCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${countCompare >= 0 ? '+' : ''}${countCompare}%</strong></td>
                    <td><strong>${summary.amountLast.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td><strong>${summary.amountCurrent.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong></td>
                    <td class="${amountCompare >= 0 ? 'text-up' : 'text-down'}"><strong>${amountCompare >= 0 ? '+' : ''}${amountCompare}%</strong></td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    renderRefundPagination() {
        const totalPages = Math.ceil(this.refundTableState.total / this.refundTableState.pageSize);
        const current = this.refundTableState.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToRefundPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToRefundPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToRefundPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="inpatientPrepaymentController.goToRefundPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="inpatientPrepaymentController.goToRefundPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('refundPagination').innerHTML = html;
    }

    updateRefundPageInfo() {
        document.getElementById('refundPageInfo').textContent = `${this.refundTableState.pageSize}条/页 共${this.refundTableState.total}条`;
    }

    goToRefundPage(page) {
        const totalPages = Math.ceil(this.refundTableState.total / this.refundTableState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.refundTableState.currentPage = page;
        this.loadRefundTable();
    }

    jumpToRefundPage() {
        const input = document.getElementById('refundJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToRefundPage(page);
            input.value = '';
        }
    }

    async loadSummaryChart(type) {
        try {
            const [trendBody, channelBody] = await Promise.all([
                ReportAPI.getInpatientPrepaymentTrendChart({
                    type: 'summary_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                }),
                ReportAPI.getInpatientPrepaymentChannelChart({
                    type: 'summary_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                })
            ]);
            if (trendBody && trendBody.categories) {
                this.renderTrendChart(this.charts.summaryTrend, trendBody, 'summary');
            }
            if (channelBody && channelBody.channelAnalysis) {
                this.renderSummaryChannelChart(channelBody);
            }
        } catch (error) {
            console.error('Load summary chart failed:', error);
        }
    }

    renderSummaryChannelChart(data) {
        const isAmount = data.type === 'summary_amount';
        const titlePrefix = isAmount ? '缴费金额' : '缴费人次';

        document.getElementById('summaryChannelPieTitle').textContent = `${titlePrefix}渠道分析`;
        document.getElementById('summaryPayTypePieTitle').textContent = `${titlePrefix}支付方式分析`;
        document.getElementById('summaryChannelBarTitle').textContent = `${titlePrefix}渠道支付方式分析`;

        this.renderPieChart(this.charts.summaryChannelPie, data.channelAnalysis, `${titlePrefix}渠道分析`);
        this.renderPieChart(this.charts.summaryPayTypePie, data.payTypeAnalysis, `${titlePrefix}支付方式分析`);
        this.renderStackBarChart(this.charts.summaryChannelBar, data.channelPayTypeAnalysis, `${titlePrefix}渠道支付方式分析`);
    }

    async loadIncomeChart(type) {
        try {
            const [trendBody, channelBody] = await Promise.all([
                ReportAPI.getInpatientPrepaymentTrendChart({
                    type: 'income_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                }),
                ReportAPI.getInpatientPrepaymentChannelChart({
                    type: 'income_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                })
            ]);
            if (trendBody && trendBody.categories) {
                this.renderTrendChart(this.charts.incomeTrend, trendBody, 'income');
            }
            if (channelBody && channelBody.channelAnalysis) {
                this.renderIncomeChannelChart(channelBody);
            }
        } catch (error) {
            console.error('Load income chart failed:', error);
        }
    }

    async loadRefundChart(type) {
        try {
            const [trendBody, payTypeBody] = await Promise.all([
                ReportAPI.getInpatientPrepaymentTrendChart({
                    type: 'refund_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                }),
                ReportAPI.getInpatientPrepaymentPayTypeChart({
                    type: 'refund_' + type,
                    dimension: this.filter.dimension,
                    startDate: this.filter.startDate,
                    endDate: this.filter.endDate
                })
            ]);
            if (trendBody && trendBody.categories) {
                this.renderTrendChart(this.charts.refundTrend, trendBody, 'refund');
            }
            if (payTypeBody && payTypeBody.payTypeAnalysis) {
                this.renderRefundPayTypeChart(payTypeBody);
            }
        } catch (error) {
            console.error('Load refund chart failed:', error);
        }
    }

    renderTrendChart(chart, data, prefix) {
        document.getElementById(prefix + 'TrendChartTitle').textContent = data.title;

        const option = {
            tooltip: {
                trigger: 'axis'
            },
            legend: {
                data: data.legend,
                right: 10,
                top: 0
            },
            grid: {
                left: 60,
                right: 40,
                bottom: 40,
                top: 40,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: data.categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c' }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            series: [
                {
                    name: data.legend[0],
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: { color: '#1890ff' },
                    data: data.currentData
                },
                {
                    name: data.legend[1],
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: { color: '#52c41a' },
                    data: data.lastData
                }
            ]
        };

        chart.setOption(option, true);
    }

    renderIncomeChannelChart(data) {
        const isAmount = data.type === 'income_amount';
        const titlePrefix = isAmount ? '缴费金额' : '缴费人次';

        document.getElementById('incomeChannelPieTitle').textContent = `${titlePrefix}渠道分析`;
        document.getElementById('incomePayTypePieTitle').textContent = `${titlePrefix}支付方式分析`;
        document.getElementById('incomeChannelBarTitle').textContent = `${titlePrefix}渠道支付方式分析`;

        this.renderPieChart(this.charts.incomeChannelPie, data.channelAnalysis, `${titlePrefix}渠道分析`);
        this.renderPieChart(this.charts.incomePayTypePie, data.payTypeAnalysis, `${titlePrefix}支付方式分析`);
        this.renderStackBarChart(this.charts.incomeChannelBar, data.channelPayTypeAnalysis, `${titlePrefix}渠道支付方式分析`);
    }

    renderRefundPayTypeChart(data) {
        const isAmount = data.type === 'refund_amount';
        const titlePrefix = isAmount ? '退院金额' : '退院人次';

        document.getElementById('refundPayTypePieTitle').textContent = `${titlePrefix}支付方式分析`;
        this.renderPieChart(this.charts.refundPayTypePie, data.payTypeAnalysis, `${titlePrefix}支付方式分析`);
    }

    renderPieChart(chart, data, title) {
        const colors = ['#1890ff', '#52c41a', '#13c2c2', '#faad14'];
        const option = {
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
                textStyle: { color: '#595959', fontSize: 12 }
            },
            color: colors,
            series: [{
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
                labelLine: { show: true, length: 10, length2: 10 },
                data: data
            }]
        };
        chart.setOption(option, true);
    }

    renderStackBarChart(chart, data, title) {
        const colors = ['#fa8c16', '#52c41a', '#1890ff'];
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            legend: {
                data: data.series.map(s => s.name),
                right: 10,
                top: 0
            },
            color: colors,
            grid: {
                left: 50,
                right: 20,
                bottom: 30,
                top: 50,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: data.categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c' }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            series: data.series.map((s, index) => ({
                name: s.name,
                type: 'bar',
                stack: 'total',
                barWidth: '40%',
                itemStyle: { color: colors[index % colors.length] },
                data: s.data
            }))
        };
        chart.setOption(option, true);
    }
}

function jumpToSummaryPage() {
    inpatientPrepaymentController.jumpToSummaryPage();
}

function jumpToIncomePage() {
    inpatientPrepaymentController.jumpToIncomePage();
}

function jumpToRefundPage() {
    inpatientPrepaymentController.jumpToRefundPage();
}

function exportPrepaymentReport() {
    const controller = inpatientPrepaymentController;
    let data, headers, rows, sheetName;

    if (controller.filter.tab === 'summary') {
        data = controller.summaryTableState.data;
        if (data.length === 0) {
            alert('暂无数据可导出');
            return;
        }
        sheetName = '汇总统计';
        headers = ['日期', '缴费人次-去年同期', '缴费人次-当前日期', '缴费人次-同比', '缴费金额-去年同期', '缴费金额-当前日期', '缴费金额-同比'];
        rows = data.map(row => [
            row.date, row.countLast, row.countCurrent, `${row.countCompare}%`,
            row.amountLast, row.amountCurrent, `${row.amountCompare}%`
        ]);
    } else if (controller.filter.tab === 'income') {
        data = controller.incomeTableState.data;
        if (data.length === 0) {
            alert('暂无数据可导出');
            return;
        }
        sheetName = '进项统计';
        headers = ['日期', '缴费人次-去年同期', '缴费人次-当前日期', '缴费人次-同比', '缴费金额-去年同期', '缴费金额-当前日期', '缴费金额-同比'];
        rows = data.map(row => [
            row.date, row.countLast, row.countCurrent, `${row.countCompare}%`,
            row.amountLast, row.amountCurrent, `${row.amountCompare}%`
        ]);
    } else {
        data = controller.refundTableState.data;
        if (data.length === 0) {
            alert('暂无数据可导出');
            return;
        }
        sheetName = '退项统计';
        headers = ['日期', '退院人次-去年同期', '退院人次-当前日期', '退院人次-同比', '退院金额-去年同期', '退院金额-当前日期', '退院金额-同比'];
        rows = data.map(row => [
            row.date, row.countLast, row.countCurrent, `${row.countCompare}%`,
            row.amountLast, row.amountCurrent, `${row.amountCompare}%`
        ]);
    }

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 10 }, { wch: 16 }, { wch: 16 }, { wch: 10 }];

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

    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `住院预交金统计_${dateStr}.xlsx`);
}

const inpatientPrepaymentController = new InpatientPrepaymentController();
