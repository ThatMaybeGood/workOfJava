/**
 * 互医质控运营月报页面主逻辑
 */

/**
 * 默认统计月份：当月（格式 yyyy-MM）。
 * 原先写死成 2025-12，是过期的固定月份，页面一打开就查不到数据。
 */
function currentMonth() {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
}

class InternetHospitalController {
    constructor() {
        this.state = {
            filter: {
                month: currentMonth()
            },
            deptPage: {
                currentPage: 1,
                pageSize: 10,
                total: 0
            },
            doctorPage: {
                currentPage: 1,
                pageSize: 10,
                total: 0
            }
        };
        this.charts = {};

        this.init();
    }

    init() {
        this.initCharts();
        this.bindEvents();
        this.updateMonthHeaders();
        this.loadData();
    }

    initCharts() {
        const businessChartEl = document.getElementById('businessChart');
        const growthChartEl = document.getElementById('growthChart');

        if (businessChartEl) {
            this.charts.business = echarts.init(businessChartEl);
        }
        if (growthChartEl) {
            this.charts.growth = echarts.init(growthChartEl);
        }

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    bindEvents() {
        document.getElementById('monthSelect').addEventListener('change', (e) => {
            this.state.filter.month = e.target.value;
            this.state.deptPage.currentPage = 1;
            this.state.doctorPage.currentPage = 1;
            this.updateMonthHeaders();
            this.loadData();
        });

        document.getElementById('deptPageSizeSelect').addEventListener('change', (e) => {
            this.state.deptPage.pageSize = parseInt(e.target.value);
            this.state.deptPage.currentPage = 1;
            this.loadDeptRanking();
        });

        document.getElementById('doctorPageSizeSelect').addEventListener('change', (e) => {
            this.state.doctorPage.pageSize = parseInt(e.target.value);
            this.state.doctorPage.currentPage = 1;
            this.loadDoctorRanking();
        });
    }

    /** 当月/上月标签，用于表头与图表图例 */
    monthLabels() {
        const [year, month] = this.state.filter.month.split('-').map(Number);
        const pad = (n) => String(n).padStart(2, '0');
        const last = month === 1 ? { year: year - 1, month: 12 } : { year, month: month - 1 };
        return {
            current: `${year}-${pad(month)}`,
            last: `${last.year}-${pad(last.month)}`
        };
    }

    updateMonthHeaders() {
        const { current, last } = this.monthLabels();
        // 同步月份输入框的显示值：原先只在 change 时更新，
        // 初次加载会一直显示 HTML 里写死的 2025-12，与实际查询的月份对不上
        const sel = document.getElementById('monthSelect');
        if (sel) {
            sel.value = this.state.filter.month;
        }
        const setText = (id, text) => {
            const el = document.getElementById(id);
            if (el) {
                el.textContent = text;
            }
        };
        setText('opMonthCurrent', current);
        setText('opMonthLast', last);
        setText('deptMonthCurrent', current);
        setText('deptMonthLast', last);
        setText('doctorMonthCurrent', current);
    }

    async loadData() {
        try {
            const body = await ReportAPI.getInternetHospitalStats({
                month: this.state.filter.month,
                deptPage: this.state.deptPage.currentPage,
                deptPageSize: this.state.deptPage.pageSize,
                doctorPage: this.state.doctorPage.currentPage,
                doctorPageSize: this.state.doctorPage.pageSize
            });
            this.renderOverview(body ? body.overview : null);
            this.renderOperationTable(body ? (body.operationTable || []) : []);
            this.renderBusinessChart(body ? body.businessChart : null);
            this.state.deptPage.total = (body && body.deptRanking && body.deptRanking.total) ? body.deptRanking.total : 0;
            this.renderDeptRanking(body ? (body.deptRanking && body.deptRanking.list ? body.deptRanking.list : []) : []);
            this.renderDeptPagination();
            this.updateDeptPageInfo();
            this.state.doctorPage.total = (body && body.doctorRanking && body.doctorRanking.total) ? body.doctorRanking.total : 0;
            this.renderDoctorRanking(body ? (body.doctorRanking && body.doctorRanking.list ? body.doctorRanking.list : []) : []);
            this.renderDoctorPagination();
            this.updateDoctorPageInfo();
            this.renderGrowthChart(body ? body.growthChart : null);
        } catch (error) {
            console.error('Load internet hospital data failed:', error);
        }
    }

    async loadDeptRanking() {
        try {
            const body = await ReportAPI.getInternetHospitalStats({
                month: this.state.filter.month,
                deptPage: this.state.deptPage.currentPage,
                deptPageSize: this.state.deptPage.pageSize,
                doctorPage: this.state.doctorPage.currentPage,
                doctorPageSize: this.state.doctorPage.pageSize
            });
            this.state.deptPage.total = (body && body.deptRanking && body.deptRanking.total) ? body.deptRanking.total : 0;
            this.renderDeptRanking(body ? (body.deptRanking && body.deptRanking.list ? body.deptRanking.list : []) : []);
            this.renderDeptPagination();
            this.updateDeptPageInfo();
        } catch (error) {
            console.error('Load dept ranking failed:', error);
        }
    }

    async loadDoctorRanking() {
        try {
            const body = await ReportAPI.getInternetHospitalStats({
                month: this.state.filter.month,
                deptPage: this.state.deptPage.currentPage,
                deptPageSize: this.state.deptPage.pageSize,
                doctorPage: this.state.doctorPage.currentPage,
                doctorPageSize: this.state.doctorPage.pageSize
            });
            this.state.doctorPage.total = (body && body.doctorRanking && body.doctorRanking.total) ? body.doctorRanking.total : 0;
            this.renderDoctorRanking(body ? (body.doctorRanking && body.doctorRanking.list ? body.doctorRanking.list : []) : []);
            this.renderDoctorPagination();
            this.updateDoctorPageInfo();
        } catch (error) {
            console.error('Load doctor ranking failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        const safeRate = (val) => val != null ? val : '-';
        document.getElementById('outpatientVolume').textContent = safe(data && data.outpatientVolume).toLocaleString();
        document.getElementById('doctorRatio').textContent = safeRate(data && data.doctorRatio);
        document.getElementById('receptionRate').textContent = safeRate(data && data.receptionRate);
        document.getElementById('prescriptionRate').textContent = safeRate(data && data.prescriptionRate);
        document.getElementById('recordRate').textContent = safeRate(data && data.recordRate);
        document.getElementById('reviewRate').textContent = safeRate(data && data.reviewRate);
        document.getElementById('executionRate').textContent = safeRate(data && data.executionRate);
    }

    renderOperationTable(data) {
        const tbody = document.getElementById('operationTableBody');
        let html = '';
        const tableData = (data && Array.isArray(data)) ? data : [];
        tableData.forEach(row => {
            const isNegative = row.growth && row.growth.includes('-');
            const growthColor = isNegative ? 'text-danger' : 'text-success';
            html += `
                <tr>
                    <td>${row.name || ''}</td>
                    <td>${row.current || 0}</td>
                    <td>${row.last || 0}</td>
                    <td class="${growthColor}">${row.growth || '-'}</td>
                </tr>
            `;
        });
        if (tableData.length === 0) {
            html += '<tr><td colspan="4" class="text-center text-muted py-4">暂无数据</td></tr>';
        }
        tbody.innerHTML = html;
    }

    renderBusinessChart(chartData) {
        const categories = (chartData && chartData.categories) ? chartData.categories : [];
        const lastData = (chartData && chartData.last) ? chartData.last : [];
        const currentData = (chartData && chartData.current) ? chartData.current : [];
        const { current, last } = this.monthLabels();
        const option = {
            title: {
                text: '互联网医院各业务运行情况',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            legend: {
                data: [last, current],
                right: 10,
                top: 0
            },
            grid: {
                left: 50,
                right: 30,
                bottom: 30,
                top: 40,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c', fontSize: 11, rotate: 15 }
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
                    name: last,
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: { color: '#1890ff' },
                    data: lastData
                },
                {
                    name: current,
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: { color: '#52c41a' },
                    data: currentData
                }
            ]
        };
        this.charts.business.setOption(option);
    }

    renderDeptRanking(data) {
        const tbody = document.getElementById('deptRankingBody');
        let html = '';
        const rankData = (data && Array.isArray(data)) ? data : [];
        rankData.forEach(row => {
            const isNegative = row.growth && row.growth.includes('-');
            const growthColor = isNegative ? 'text-danger' : 'text-success';
            html += `
                <tr>
                    <td>${row.rank || ''}</td>
                    <td>${row.deptName || ''}</td>
                    <td>${row.currentMonth || 0}</td>
                    <td>${row.lastMonth || 0}</td>
                    <td class="${growthColor}">${row.growth || '-'}</td>
                </tr>
            `;
        });
        if (rankData.length === 0) {
            html += '<tr><td colspan="5" class="text-center text-muted py-4">暂无数据</td></tr>';
        }
        tbody.innerHTML = html;
    }

    renderDoctorRanking(data) {
        const tbody = document.getElementById('doctorRankingBody');
        let html = '';
        const rankData = (data && Array.isArray(data)) ? data : [];
        rankData.forEach(row => {
            html += `
                <tr>
                    <td>${row.rank || ''}</td>
                    <td>${row.doctorName || ''}</td>
                    <td>${row.deptName || ''}</td>
                    <td>${row.title || ''}</td>
                    <td>${row.currentMonth || 0}</td>
                </tr>
            `;
        });
        if (rankData.length === 0) {
            html += '<tr><td colspan="5" class="text-center text-muted py-4">暂无数据</td></tr>';
        }
        tbody.innerHTML = html;
    }

    renderGrowthChart(chartData) {
        const categories = (chartData && chartData.categories) ? chartData.categories : [];
        const data = (chartData && chartData.data) ? chartData.data : [];
        const option = {
            title: {
                text: '互联网医院平均候诊时长科室TOP20',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                formatter: (params) => {
                    const item = Array.isArray(params) ? params[0] : params;
                    const month = parseInt(this.state.filter.month.split('-')[1], 10);
                    return `${item.name}<br/>${month}月平均候诊时长: ${item.value}`;
                }
            },
            grid: {
                left: 50,
                right: 30,
                bottom: 60,
                top: 60,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c', fontSize: 10, rotate: 30 }
            },
            yAxis: {
                type: 'value',
                name: '分钟',
                nameGap: 10,
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' },
                nameTextStyle: { color: '#8c8c8c', align: 'left' }
            },
            series: [
                {
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 6,
                    lineStyle: { width: 2, color: '#13c2c2' },
                    itemStyle: { color: '#13c2c2' },
                    areaStyle: {
                        color: {
                            type: 'linear',
                            x: 0, y: 0, x2: 0, y2: 1,
                            colorStops: [
                                { offset: 0, color: 'rgba(19,194,194,0.4)' },
                                { offset: 1, color: 'rgba(19,194,194,0.05)' }
                            ]
                        }
                    },
                    data: data
                }
            ]
        };
        this.charts.growth.setOption(option);
    }

    renderDeptPagination() {
        const state = this.state.deptPage;
        const totalPages = Math.ceil(state.total / state.pageSize);
        const current = state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="internetHospitalController.goToDeptPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="internetHospitalController.goToDeptPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="internetHospitalController.goToDeptPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="internetHospitalController.goToDeptPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="internetHospitalController.goToDeptPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('deptPagination').innerHTML = html;
    }

    renderDoctorPagination() {
        const state = this.state.doctorPage;
        const totalPages = Math.ceil(state.total / state.pageSize);
        const current = state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="internetHospitalController.goToDoctorPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="internetHospitalController.goToDoctorPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="internetHospitalController.goToDoctorPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="internetHospitalController.goToDoctorPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="internetHospitalController.goToDoctorPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('doctorPagination').innerHTML = html;
    }

    updateDeptPageInfo() {
        document.getElementById('deptPageInfo').textContent = `${this.state.deptPage.pageSize}条/页 共${this.state.deptPage.total}条`;
    }

    updateDoctorPageInfo() {
        document.getElementById('doctorPageInfo').textContent = `${this.state.doctorPage.pageSize}条/页 共${this.state.doctorPage.total}条`;
    }

    goToDeptPage(page) {
        const totalPages = Math.ceil(this.state.deptPage.total / this.state.deptPage.pageSize);
        if (page < 1 || page > totalPages) return;
        this.state.deptPage.currentPage = page;
        this.loadDeptRanking();
    }

    goToDoctorPage(page) {
        const totalPages = Math.ceil(this.state.doctorPage.total / this.state.doctorPage.pageSize);
        if (page < 1 || page > totalPages) return;
        this.state.doctorPage.currentPage = page;
        this.loadDoctorRanking();
    }

    jumpToDeptPage() {
        const input = document.getElementById('deptJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToDeptPage(page);
            input.value = '';
        }
    }

    jumpToDoctorPage() {
        const input = document.getElementById('doctorJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToDoctorPage(page);
            input.value = '';
        }
    }
}

const internetHospitalController = new InternetHospitalController();
