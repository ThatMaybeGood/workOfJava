/**
 * 检验统计页面主逻辑
 */
class LabStatsController {
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
        this.charts.time = echarts.init(document.getElementById('timeChart'));
        this.charts.rank = echarts.init(document.getElementById('rankChart'));

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
            const body = await ReportAPI.getLabStats({
                timeRange: this.state.timeRange,
                startDate: this.state.startDate,
                endDate: this.state.endDate
            });
            if (body) {
                this.renderOverview(body.overview);
                this.renderTimeChart(body.timeAnalysis);
                this.renderRankChart(body.reportRank);
            }
        } catch (error) {
            console.error('Load lab stats data failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        const safeRate = (val) => val != null ? val : '-';
        document.getElementById('bloodCollection').textContent = safe(data && data.bloodCollection).toLocaleString();
        document.getElementById('bloodEfficiency').textContent = safeRate(data && data.bloodEfficiency);
        document.getElementById('labEfficiency').textContent = safeRate(data && data.labEfficiency);
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
                            { offset: 0, color: 'rgba(19, 194, 194, 0.6)' },
                            { offset: 1, color: 'rgba(19, 194, 194, 0.05)' }
                        ])
                    },
                    data: data
                }
            ]
        };
        this.charts.time.setOption(option, true);
    }

    renderRankChart(rankData) {
        const categories = (rankData && rankData.categories) ? rankData.categories : [];
        const data = (rankData && rankData.data) ? rankData.data : [];
        const option = {
            title: {
                text: '检验项目出具报告时间排行',
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
                name: '分钟',
                axisLine: { show: false },
                axisTick: { show: false },
                splitLine: { lineStyle: { color: '#f0f0f0' } },
                axisLabel: { color: '#8c8c8c' }
            },
            series: [
                {
                    type: 'bar',
                    barWidth: '40%',
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
        this.charts.rank.setOption(option, true);
    }
}

const labStatsController = new LabStatsController();
