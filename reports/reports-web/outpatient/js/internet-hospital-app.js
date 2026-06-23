/**
 * 互医质控运营月报页面主逻辑
 */
class InternetHospitalController {
    constructor() {
        this.state = {
            filter: {
                month: '2025-12'
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
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            legend: {
                data: ['2025-11', '2025-12'],
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
                    name: '2025-11',
                    type: 'bar',
                    barWidth: '30%',
                    itemStyle: { color: '#1890ff' },
                    data: lastData
                },
                {
                    name: '2025-12',
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
                text: '互联网医院患者增长科室TOP20',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                formatter: '{b}: {c}'
            },
            grid: {
                left: 50,
                right: 30,
                bottom: 60,
                top: 40,
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
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
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
