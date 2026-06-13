/**
 * 治疗统计报表页面主逻辑
 */
class TreatmentStatsController {
    constructor() {
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc',
            filter: {
                timeRange: 'today',
                startDate: '2025-09-22',
                endDate: '2025-10-22',
                visitType: '',
                patientSource: '',
                ageRange: ''
            }
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
        this.charts.trend = echarts.init(document.getElementById('trendChart'));
        this.charts.topProjects = echarts.init(document.getElementById('topProjectsChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        document.getElementById('visitTypeSelect').addEventListener('change', (e) => {
            this.state.filter.visitType = e.target.value;
            this.state.currentPage = 1;
            this.loadData();
        });

        document.getElementById('sourceSelect').addEventListener('change', (e) => {
            this.state.filter.patientSource = e.target.value;
            this.state.currentPage = 1;
            this.loadData();
        });

        document.getElementById('ageSelect').addEventListener('change', (e) => {
            this.state.filter.ageRange = e.target.value;
            this.state.currentPage = 1;
            this.loadData();
        });

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadTableData();
        });

        document.querySelectorAll('#treatmentTable .sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e));
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
                    this.state.filter.startDate = formatDate(selectedDates[0]);
                    this.state.filter.endDate = formatDate(selectedDates[1]);
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
        this.state.filter.timeRange = btn.dataset.value;
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

        document.querySelectorAll('#treatmentTable .sortable').forEach(el => {
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
            }
            if (valA < valB) return -1 * direction;
            if (valA > valB) return 1 * direction;
            return 0;
        });
    }

    async loadData() {
        try {
            const result = await ReportAPI.getTreatmentStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate,
                visitType: this.state.filter.visitType,
                patientSource: this.state.filter.patientSource,
                ageRange: this.state.filter.ageRange
            });
            if (result.code === 200) {
                this.renderOverview(result.data.overview);
                this.renderTrendChart(result.data.trend);
                this.renderTopProjectsChart(result.data.topProjects);
                this.state.data = result.data.table.list;
                this.state.total = result.data.table.total;
                this.renderTable();
                this.renderPagination();
                this.updatePageInfo();
            }
        } catch (error) {
            console.error('Load treatment stats failed:', error);
        }
    }

    async loadTableData() {
        this.loadData();
    }

    renderOverview(data) {
        document.getElementById('patientCount').textContent = data.patientCount.toLocaleString();
        document.getElementById('treatmentCount').textContent = data.treatmentCount.toLocaleString();
        document.getElementById('treatmentAmount').textContent = '¥' + data.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
        document.getElementById('avgAmount').textContent = '¥' + data.avgAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
    }

    renderTrendChart(trendData) {
        const option = {
            title: {
                text: '治疗人次趋势',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'cross' }
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
                data: trendData.dates,
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
                    type: 'line',
                    smooth: true,
                    symbol: 'circle',
                    symbolSize: 6,
                    itemStyle: { color: '#1890ff' },
                    lineStyle: { width: 2 },
                    areaStyle: {
                        color: {
                            type: 'linear',
                            x: 0, y: 0, x2: 0, y2: 1,
                            colorStops: [
                                { offset: 0, color: 'rgba(24,144,255,0.3)' },
                                { offset: 1, color: 'rgba(24,144,255,0.05)' }
                            ]
                        }
                    },
                    data: trendData.data
                }
            ]
        };
        this.charts.trend.setOption(option);
    }

    renderTopProjectsChart(topProjects) {
        const option = {
            title: {
                text: '治疗项目TOP10',
                left: 'left',
                top: 0,
                textStyle: { fontSize: 14, fontWeight: 600, color: '#262626' }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'shadow' }
            },
            grid: {
                left: 10,
                right: 30,
                bottom: 10,
                top: 40,
                containLabel: true
            },
            xAxis: {
                type: 'value',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            yAxis: {
                type: 'category',
                data: topProjects.map(item => item.name).reverse(),
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#595959', fontSize: 11 },
                axisTick: { show: false }
            },
            series: [
                {
                    type: 'bar',
                    barWidth: '60%',
                    itemStyle: {
                        color: {
                            type: 'linear',
                            x: 0, y: 0, x2: 1, y2: 0,
                            colorStops: [
                                { offset: 0, color: '#69c0ff' },
                                { offset: 1, color: '#1890ff' }
                            ]
                        },
                        borderRadius: [0, 4, 4, 0]
                    },
                    label: {
                        show: true,
                        position: 'right',
                        color: '#595959',
                        fontSize: 11
                    },
                    data: topProjects.map(item => item.value).reverse()
                }
            ]
        };
        this.charts.topProjects.setOption(option);
    }

    renderTable() {
        const tbody = document.getElementById('tableBody');
        let html = '';

        this.state.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.rank}</td>
                    <td>${row.deptName}</td>
                    <td>${row.patientCount}</td>
                    <td>${row.treatmentCount}</td>
                    <td>${row.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                    <td>${row.avgAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                </tr>
            `;
        });

        if (this.state.data.length > 0) {
            const summary = this.calculateSummary();
            html += `
                <tr>
                    <td></td>
                    <td>全列表数据合计</td>
                    <td>${summary.patientCount}</td>
                    <td>${summary.treatmentCount}</td>
                    <td>${summary.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                    <td>${summary.avgAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    calculateSummary() {
        const summary = {
            patientCount: 0,
            treatmentCount: 0,
            treatmentAmount: 0
        };
        this.state.data.forEach(row => {
            summary.patientCount += row.patientCount;
            summary.treatmentCount += row.treatmentCount;
            summary.treatmentAmount += row.treatmentAmount;
        });
        summary.avgAmount = summary.treatmentCount > 0 ? parseFloat((summary.treatmentAmount / summary.treatmentCount).toFixed(1)) : 0;
        return summary;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="treatmentStatsController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="treatmentStatsController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="treatmentStatsController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="treatmentStatsController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="treatmentStatsController.goToPage(${current + 1}); return false;">></a>
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

function exportData() {
    const data = treatmentStatsController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = ['排名', '科室', '治疗人数', '治疗次数', '治疗金额', '平均治疗金额'];
    const rows = data.map(row => [
        row.rank,
        row.deptName,
        row.patientCount,
        row.treatmentCount,
        row.treatmentAmount,
        row.avgAmount
    ]);

    const summary = treatmentStatsController.calculateSummary();
    rows.push([
        '',
        '全列表数据合计',
        summary.patientCount,
        summary.treatmentCount,
        summary.treatmentAmount,
        summary.avgAmount
    ]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 8 }, { wch: 20 }, { wch: 12 }, { wch: 12 }, { wch: 14 }, { wch: 14 }];

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

    XLSX.utils.book_append_sheet(wb, ws, '治疗统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `治疗统计_${dateStr}.xlsx`);
}

function jumpToPage() {
    treatmentStatsController.jumpToPage();
}

const treatmentStatsController = new TreatmentStatsController();
