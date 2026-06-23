/**
 * 人工窗口统计页面主逻辑
 */
class WindowStatsController {
    constructor() {
        const today = this.formatDate(new Date());
        this.state = {
            timeRange: 'today',
            startDate: today,
            endDate: today
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

    init() {
        this.initCharts();
        this.bindEvents();
        this.initDateRangePicker();
        this.loadData();
    }

    initCharts() {
        this.charts.origin = echarts.init(document.getElementById('originChart'));
        this.charts.age = echarts.init(document.getElementById('ageChart'));
        this.charts.time = echarts.init(document.getElementById('timeChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
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
                    this.state.startDate = this.formatDate(selectedDates[0]);
                    this.state.endDate = this.formatDate(selectedDates[1]);
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
        this.state.timeRange = btn.dataset.value;
        this.state.startDate = range.startDate;
        this.state.endDate = range.endDate;
        if (this.datePicker) {
            this.datePicker.setDate([toFlatpickrDate(range.startDate), toFlatpickrDate(range.endDate)]);
        }
        this.loadData();
    }

    async loadData() {
        try {
            const body = await ReportAPI.getWindowStats({
                timeRange: this.state.timeRange,
                startDate: this.state.startDate,
                endDate: this.state.endDate
            });
            this.renderOverview(body ? body.overview : null);
            this.renderOriginChart(body ? body.originAnalysis : null);
            this.renderAgeChart(body ? body.ageAnalysis : null);
            this.renderTimeChart(body ? body.timeAnalysis : null);
            this.renderTable(body ? (body.workloadTable || { headers: [], rows: [] }) : { headers: [], rows: [] });
        } catch (error) {
            console.error('Load window stats data failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        document.getElementById('registerCount').textContent = safe(data && data.registerCount).toLocaleString();
        document.getElementById('paymentCount').textContent = safe(data && data.paymentCount).toLocaleString();
        document.getElementById('refundCount').textContent = safe(data && data.refundCount).toLocaleString();
    }

    renderOriginChart(data) {
        const chartData = (data && Array.isArray(data)) ? data : [];
        const option = {
            title: {
                text: '窗口患者归属地分析',
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
            color: ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#f5222d'],
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
        this.charts.origin.setOption(option, true);
    }

    renderAgeChart(ageData) {
        const categories = (ageData && ageData.categories) ? ageData.categories : [];
        const data = (ageData && ageData.data) ? ageData.data : [];
        const option = {
            title: {
                text: '患者年龄区间分析',
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
                right: 30,
                bottom: 30,
                top: 40,
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

    renderTimeChart(timeData) {
        const categories = (timeData && timeData.categories) ? timeData.categories : [];
        const data = (timeData && timeData.data) ? timeData.data : [];
        const option = {
            title: {
                text: '患者分时段分析',
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
                boundaryGap: false,
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
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    lineStyle: { width: 0 },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(24, 144, 255, 0.6)' },
                            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
                        ])
                    },
                    data: data
                }
            ]
        };
        this.charts.time.setOption(option);
    }

    renderTable(tableData) {
        const thead = document.getElementById('workloadTableHead');
        const tbody = document.getElementById('workloadTableBody');

        if (!tableData || !tableData.headers || tableData.headers.length === 0) {
            thead.innerHTML = '<tr><th>业务</th></tr>';
            tbody.innerHTML = '<tr><td colspan="1" class="text-center text-muted py-4">暂无数据</td></tr>';
            return;
        }

        // 渲染表头
        let headHtml = '<tr><th>业务</th>';
        tableData.headers.forEach(h => {
            headHtml += `<th>${h}</th>`;
        });
        headHtml += '</tr>';
        thead.innerHTML = headHtml;

        // 渲染数据行
        let bodyHtml = '';
        const colSums = new Array(tableData.headers.length).fill(0);
        tableData.rows.forEach(row => {
            bodyHtml += `<tr><td>${row.business}</td>`;
            row.data.forEach((val, idx) => {
                bodyHtml += `<td>${val}</td>`;
                colSums[idx] += val;
            });
            bodyHtml += '</tr>';
        });

        // 合计行
        bodyHtml += '<tr><td>全列表数据合计</td>';
        colSums.forEach(sum => {
            bodyHtml += `<td>${sum}</td>`;
        });
        bodyHtml += '</tr>';

        tbody.innerHTML = bodyHtml;
    }
}

/**
 * 导出窗口工作量数据为 Excel
 */
function exportWorkloadData() {
    const table = document.getElementById('workloadTable');
    const rows = [];
    table.querySelectorAll('tr').forEach(tr => {
        const row = [];
        tr.querySelectorAll('th, td').forEach(cell => {
            row.push(cell.textContent.trim());
        });
        rows.push(row);
    });

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet(rows);

    const colCount = rows[0].length;
    ws['!cols'] = [{ wch: 12 }];
    for (let i = 1; i < colCount; i++) {
        ws['!cols'].push({ wch: 14 });
    }

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

    XLSX.utils.book_append_sheet(wb, ws, '窗口业务分时段工作量统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `窗口业务分时段工作量统计_${dateStr}.xlsx`);
}

const windowStatsController = new WindowStatsController();
