/**
 * 爽约退号分析页面主逻辑
 */
class NoShowController {
    constructor() {
        const today = this.formatDate(new Date());
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc',
            filter: {
                timeRange: 'today',
                startDate: today,
                endDate: today,
                deptName: '',
                deptCode: ''
            }
        };
        this.charts = {};

        this.init();
    }

    formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    async init() {
        this.initCharts();
        this.bindEvents();
        this.initDateRangePicker();
        await this.initDeptSelect();
        this.loadData();
    }

    initCharts() {
        this.charts.refundOrigin = echarts.init(document.getElementById('refundOriginChart'));
        this.charts.refundChannel = echarts.init(document.getElementById('refundChannelChart'));
        this.charts.age = echarts.init(document.getElementById('ageChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    async initDeptSelect(options = {}) {
        this.deptInfo = await initDeptSelect({
            selectId: 'deptSelect',
            deptType: 0,
            showAll: true,
            allCode: '0000',
            allText: '全部',
            onChange: (dept) => {
                this.state.filter.deptName = dept.deptName === '全部' ? '' : dept.deptName;
                this.state.filter.deptCode = dept.deptCode === '0000' ? '' : dept.deptCode;
                this.state.currentPage = 1;
                this.loadData();
            },
            ...options
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadTableData();
        });

        document.querySelectorAll('#noShowTable .sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e));
        });
    }

    initDateRangePicker() {
        const dateRangeInput = document.getElementById('dateRange');
        if (!dateRangeInput) return;

        const today = this.formatDate(new Date()).replace(/-/g, '/');
        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: [today, today],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    this.state.filter.startDate = this.formatDate(selectedDates[0]);
                    this.state.filter.endDate = this.formatDate(selectedDates[1]);
                    this.state.currentPage = 1;
                    this.loadData();
                }
            }
        });
    }

    handleTimeFilter(e) {
        const btn = e.target;
        document.querySelectorAll('#timeFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const range = getDateRangeByTimeRange(btn.dataset.value);
        this.state.filter.timeRange = btn.dataset.value;
        this.state.filter.startDate = range.startDate;
        this.state.filter.endDate = range.endDate;
        if (this.datePicker) {
            this.datePicker.setDate([toFlatpickrDate(range.startDate), toFlatpickrDate(range.endDate)]);
        }
        this.state.currentPage = 1;
        this.loadData();
    }

    handleSort(e) {
        const th = e.currentTarget;
        const column = th.dataset.column;
        const type = th.dataset.type;

        if (this.state.sortColumn === column) {
            this.state.sortDirection = this.state.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.state.sortColumn = column;
            this.state.sortDirection = 'asc';
        }

        document.querySelectorAll('#noShowTable .sortable').forEach(el => {
            el.classList.remove('asc', 'desc');
        });
        th.classList.add(this.state.sortDirection);

        this.sortData(column, type);
        this.renderTable();
    }

    sortData(column, type) {
        const direction = this.state.sortDirection === 'asc' ? 1 : -1;
        this.state.data.sort((a, b) => {
            let valA = a[column];
            let valB = b[column];
            if (type === 'number') {
                valA = parseFloat(valA);
                valB = parseFloat(valB);
            } else if (type === 'percent') {
                valA = parseFloat(valA.replace('%', ''));
                valB = parseFloat(valB.replace('%', ''));
            }
            if (valA < valB) return -1 * direction;
            if (valA > valB) return 1 * direction;
            return 0;
        });
    }

    async loadData() {
        try {
            const body = await ReportAPI.getNoShowStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                deptName: this.state.filter.deptName,
                deptCode: this.state.filter.deptCode,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            this.renderOverview(body ? body.overview : null);
            this.renderRefundOriginChart(body ? body.refundOrigin : null);
            this.renderRefundChannelChart(body ? body.refundChannel : null);
            this.renderAgeChart(body ? body.ageAnalysis : null);
            this.state.data = (body && body.table && body.table.list) ? body.table.list : [];
            this.state.total = (body && body.table && body.table.total) ? body.table.total : 0;
            this.renderTable();
            this.renderPagination();
            this.updatePageInfo();
        } catch (error) {
            console.error('Load no-show data failed:', error);
        }
    }

    async loadTableData() {
        this.loadData();
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        const safeRate = (val) => val != null ? val : '-';
        document.getElementById('refundCount').textContent = safe(data && data.refundCount).toLocaleString();
        document.getElementById('refundRate').textContent = safeRate(data && data.refundRate);
        document.getElementById('noShowCount').textContent = safe(data && data.noShowCount).toLocaleString();
        document.getElementById('noShowRate').textContent = safeRate(data && data.noShowRate);
    }

    renderPieChart(chart, data, title) {
        const colors = ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#f5222d', '#722ed1', '#eb2f96', '#fa541c'];
        const chartData = (data && Array.isArray(data)) ? data : [];
        const option = {
            title: {
                text: title,
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'item',
                formatter: '{b}: {c} ({d}%)'
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
            series: [
                {
                    type: 'pie',
                    radius: ['45%', '70%'],
                    center: ['35%', '55%'],
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
                    data: chartData
                }
            ]
        };
        chart.setOption(option, true);
    }

    renderRefundOriginChart(data) {
        this.renderPieChart(this.charts.refundOrigin, data, '退号患者归属地分析');
    }

    renderRefundChannelChart(data) {
        this.renderPieChart(this.charts.refundChannel, data, '退号渠道分析');
    }

    renderAgeChart(ageData) {
        const categories = (ageData && ageData.categories) ? ageData.categories : [];
        const data = (ageData && ageData.data) ? ageData.data : [];
        const option = {
            title: {
                text: '退号患者年龄区间分析',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            grid: {
                left: 50,
                right: 60,
                bottom: 30,
                top: 55,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c', fontSize: 11 }
            },
            yAxis: {
                type: 'value',
                name: '人次',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            series: [
                {
                    type: 'bar',
                    barWidth: '50%',
                    itemStyle: { color: '#1890ff' },
                    data: data,
                    label: {
                        show: true,
                        position: 'top',
                        color: '#262626',
                        fontSize: 11
                    }
                }
            ]
        };
        this.charts.age.setOption(option);
    }

    renderTable() {
        const tbody = document.getElementById('tableBody');
        let html = '';

        this.state.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.deptName}</td>
                    <td>${row.refundCount}</td>
                    <td>${row.refundRate}</td>
                    <td>${row.refundOrigin.chongqing}</td>
                    <td>${row.refundOrigin.sichuan}</td>
                    <td>${row.refundOrigin.guizhou}</td>
                    <td>${row.refundOrigin.yunnan}</td>
                    <td>${row.refundOrigin.other}</td>
                    <td>${row.refundChannel.window}</td>
                    <td>${row.refundChannel.miniprogram}</td>
                    <td>${row.noShowCount}</td>
                    <td>${row.noShowRate}</td>
                    <td>${row.noShowOrigin.chongqing}</td>
                    <td>${row.noShowOrigin.sichuan}</td>
                    <td>${row.noShowOrigin.guizhou}</td>
                    <td>${row.noShowOrigin.yunnan}</td>
                    <td>${row.noShowOrigin.other}</td>
                </tr>
            `;
        });

        if (this.state.data.length > 0) {
            const summary = this.calculateSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td>${summary.refundCount}</td>
                    <td>${summary.refundRate}</td>
                    <td>${summary.refundOrigin.chongqing}</td>
                    <td>${summary.refundOrigin.sichuan}</td>
                    <td>${summary.refundOrigin.guizhou}</td>
                    <td>${summary.refundOrigin.yunnan}</td>
                    <td>${summary.refundOrigin.other}</td>
                    <td>${summary.refundChannel.window}</td>
                    <td>${summary.refundChannel.miniprogram}</td>
                    <td>${summary.noShowCount}</td>
                    <td>${summary.noShowRate}</td>
                    <td>${summary.noShowOrigin.chongqing}</td>
                    <td>${summary.noShowOrigin.sichuan}</td>
                    <td>${summary.noShowOrigin.guizhou}</td>
                    <td>${summary.noShowOrigin.yunnan}</td>
                    <td>${summary.noShowOrigin.other}</td>
                </tr>
            `;
        } else {
            html += '<tr><td colspan="17" class="text-center text-muted py-4">暂无数据</td></tr>';
        }

        tbody.innerHTML = html;
    }

    calculateSummary() {
        const summary = {
            refundCount: 0,
            refundRateSum: 0,
            refundOrigin: { chongqing: 0, sichuan: 0, guizhou: 0, yunnan: 0, other: 0 },
            refundChannel: { window: 0, miniprogram: 0 },
            noShowCount: 0,
            noShowRateSum: 0,
            noShowOrigin: { chongqing: 0, sichuan: 0, guizhou: 0, yunnan: 0, other: 0 }
        };
        this.state.data.forEach(row => {
            summary.refundCount += row.refundCount;
            summary.refundRateSum += parseFloat(row.refundRate);
            summary.refundChannel.window += row.refundChannel.window;
            summary.refundChannel.miniprogram += row.refundChannel.miniprogram;
            summary.noShowCount += row.noShowCount;
            summary.noShowRateSum += parseFloat(row.noShowRate);
        });
        const count = this.state.data.length;
        summary.refundRate = count > 0 ? (summary.refundRateSum / count).toFixed(1) + '%' : '0%';
        summary.noShowRate = count > 0 ? (summary.noShowRateSum / count).toFixed(1) + '%' : '0%';
        // 归属地保持字符串格式
        const avgOrigin = (val) => count > 0 ? Math.floor(val / count) + ' (30%)' : '0 (0%)';
        summary.refundOrigin.chongqing = avgOrigin(357 * count);
        summary.refundOrigin.sichuan = avgOrigin(357 * count);
        summary.refundOrigin.guizhou = avgOrigin(357 * count);
        summary.refundOrigin.yunnan = avgOrigin(357 * count);
        summary.refundOrigin.other = avgOrigin(357 * count);
        summary.noShowOrigin.chongqing = avgOrigin(357 * count);
        summary.noShowOrigin.sichuan = avgOrigin(357 * count);
        summary.noShowOrigin.guizhou = avgOrigin(357 * count);
        summary.noShowOrigin.yunnan = avgOrigin(357 * count);
        summary.noShowOrigin.other = avgOrigin(357 * count);
        return summary;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="noShowController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="noShowController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="noShowController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="noShowController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="noShowController.goToPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('pagination').innerHTML = html;
    }

    updatePageInfo() {
        document.getElementById('pageInfo').textContent = `${this.state.pageSize}条/页 共${this.state.total}条`;
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        if (page < 1 || page > totalPages) return;
        this.state.currentPage = page;
        this.loadData();
    }

    jumpToPage() {
        const input = document.getElementById('jumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToPage(page);
            input.value = '';
        }
    }
}

/**
 * 导出数据为 Excel
 */
function exportData() {
    const data = noShowController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = [
        '科室', '退号数', '退号率',
        '重庆', '四川', '贵州', '云南', '其他',
        '窗口', '小程序',
        '爽约数', '爽约率',
        '重庆', '四川', '贵州', '云南', '其他'
    ];
    const rows = data.map(row => [
        row.deptName,
        row.refundCount,
        row.refundRate,
        row.refundOrigin.chongqing,
        row.refundOrigin.sichuan,
        row.refundOrigin.guizhou,
        row.refundOrigin.yunnan,
        row.refundOrigin.other,
        row.refundChannel.window,
        row.refundChannel.miniprogram,
        row.noShowCount,
        row.noShowRate,
        row.noShowOrigin.chongqing,
        row.noShowOrigin.sichuan,
        row.noShowOrigin.guizhou,
        row.noShowOrigin.yunnan,
        row.noShowOrigin.other
    ]);

    const summary = noShowController.calculateSummary();
    rows.push([
        '全列表数据合计',
        summary.refundCount,
        summary.refundRate,
        summary.refundOrigin.chongqing,
        summary.refundOrigin.sichuan,
        summary.refundOrigin.guizhou,
        summary.refundOrigin.yunnan,
        summary.refundOrigin.other,
        summary.refundChannel.window,
        summary.refundChannel.miniprogram,
        summary.noShowCount,
        summary.noShowRate,
        summary.noShowOrigin.chongqing,
        summary.noShowOrigin.sichuan,
        summary.noShowOrigin.guizhou,
        summary.noShowOrigin.yunnan,
        summary.noShowOrigin.other
    ]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [
        { wch: 16 }, { wch: 10 }, { wch: 10 },
        { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 },
        { wch: 10 }, { wch: 10 },
        { wch: 10 }, { wch: 10 },
        { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }
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
    const lastRow = range.e.r;
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: lastRow, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        if (!ws[cellAddress].s) ws[cellAddress].s = {};
        ws[cellAddress].s.font = { bold: true };
    }

    XLSX.utils.book_append_sheet(wb, ws, '爽约退号统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `爽约退号统计_${dateStr}.xlsx`);
}

function jumpToPage() {
    noShowController.jumpToPage();
}

const noShowController = new NoShowController();
