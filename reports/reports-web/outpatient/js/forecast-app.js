/**
 * 预测门诊量报表页面主逻辑
 */
class ForecastController {
    constructor() {
        this.state = {
            filter: {
                deptName: ''
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
        this.charts.monthForecast = echarts.init(document.getElementById('monthForecastChart'));
        this.charts.yearForecast = echarts.init(document.getElementById('yearForecastChart'));

        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => chart.resize());
        });
    }

    bindEvents() {
        document.getElementById('deptSelect').addEventListener('change', (e) => {
            this.state.filter.deptName = e.target.value;
            this.loadData();
        });
    }

    async loadData() {
        try {
            const body = await ReportAPI.getForecastStats({
                deptName: this.state.filter.deptName
            });
            this.renderOverview(body ? body.overview : null);
            this.renderMonthForecastChart(body ? body.monthForecast : null);
            this.renderYearForecastChart(body ? body.yearForecast : null);
        } catch (error) {
            console.error('Load forecast data failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        document.getElementById('tomorrowCount').textContent = safe(data && data.tomorrow);
        document.getElementById('nextWeekCount').textContent = safe(data && data.nextWeek);
        document.getElementById('nextMonthCount').textContent = safe(data && data.nextMonth);
        document.getElementById('nextYearCount').textContent = safe(data && data.nextYear);
    }

    renderMonthForecastChart(forecastData) {
        const dates = (forecastData && forecastData.dates) ? forecastData.dates : [];
        const data = (forecastData && forecastData.data) ? forecastData.data : [];
        const option = {
            title: {
                text: '预测未来一个月门诊量',
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
                bottom: 40,
                top: 40,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: dates,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c', fontSize: 10, interval: 0 }
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
                    data: data
                }
            ]
        };
        this.charts.monthForecast.setOption(option);
    }

    renderYearForecastChart(forecastData) {
        const months = (forecastData && forecastData.months) ? forecastData.months : [];
        const data = (forecastData && forecastData.data) ? forecastData.data : [];
        const option = {
            title: {
                text: '预测未来一年门诊量',
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
                data: months,
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
        this.charts.yearForecast.setOption(option);
    }
}

const forecastController = new ForecastController();
