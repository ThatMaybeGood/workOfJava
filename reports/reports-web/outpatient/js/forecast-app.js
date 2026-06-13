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
            const result = await ReportAPI.getForecastStats({
                deptName: this.state.filter.deptName
            });
            if (result.code === 200) {
                this.renderOverview(result.data.overview);
                this.renderMonthForecastChart(result.data.monthForecast);
                this.renderYearForecastChart(result.data.yearForecast);
            }
        } catch (error) {
            console.error('Load forecast data failed:', error);
        }
    }

    renderOverview(data) {
        document.getElementById('tomorrowCount').textContent = data.tomorrow;
        document.getElementById('nextWeekCount').textContent = data.nextWeek;
        document.getElementById('nextMonthCount').textContent = data.nextMonth;
        document.getElementById('nextYearCount').textContent = data.nextYear;
    }

    renderMonthForecastChart(forecastData) {
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
                data: forecastData.dates,
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
                    data: forecastData.data
                }
            ]
        };
        this.charts.monthForecast.setOption(option);
    }

    renderYearForecastChart(forecastData) {
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
                data: forecastData.months,
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
                    data: forecastData.data,
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
