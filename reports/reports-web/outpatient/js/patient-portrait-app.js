/**
 * 患者画像页面主逻辑
 */
class PatientPortraitController {
    constructor() {
        const today = this.formatDate(new Date());
        this.state = {
            patientType: 'outpatient',
            timeRange: 'today',
            startDate: today,
            endDate: today,
            deptName: '',
            deptCode: ''
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
        this.charts.age = echarts.init(document.getElementById('ageChart'));
        this.charts.insurance = echarts.init(document.getElementById('insuranceChart'));
        this.charts.identity = echarts.init(document.getElementById('identityChart'));
        this.charts.registerOrigin = echarts.init(document.getElementById('registerOriginChart'));
        this.charts.archiveOrigin = echarts.init(document.getElementById('archiveOriginChart'));

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
                this.state.deptName = dept.deptName === '全部' ? '' : dept.deptName;
                this.state.deptCode = dept.deptCode === '0000' ? '' : dept.deptCode;
                this.loadData();
            },
            ...options
        });
    }

    bindEvents() {
        // 患者类型切换
        document.querySelectorAll('.patient-type-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handlePatientTypeChange(e));
        });

        // 时间筛选
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

    handlePatientTypeChange(e) {
        const tab = e.target;
        document.querySelectorAll('.patient-type-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.state.patientType = tab.dataset.type;
        this.loadData();
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
            const body = await ReportAPI.getPatientPortrait({
                patientType: this.state.patientType,
                startDate: this.state.startDate,
                endDate: this.state.endDate,
                deptName: this.state.deptName,
                deptCode: this.state.deptCode
            });
            this.renderCharts(body || {});
        } catch (error) {
            console.error('Load patient portrait data failed:', error);
        }
    }

    renderCharts(data) {
        this.renderAgeChart(data.ageAnalysis || { categories: [], archiveData: [], outpatientData: [] });
        this.renderPieChart(this.charts.insurance, data.insuranceAnalysis || [], '患者医保身份构成分析');
        this.renderPieChart(this.charts.identity, data.identityAnalysis || [], '患者身份类别构成分析');
        this.renderPieChart(this.charts.registerOrigin, data.registerOriginAnalysis || [], '挂号患者归属地分析');
        this.renderPieChart(this.charts.archiveOrigin, data.archiveOriginAnalysis || [], '建档患者归属地分析');
    }

    renderAgeChart(ageData) {
        const categories = (ageData && ageData.categories) ? ageData.categories : [];
        const archiveData = (ageData && ageData.archiveData) ? ageData.archiveData : [];
        const outpatientData = (ageData && ageData.outpatientData) ? ageData.outpatientData : [];
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { type: 'cross' }
            },
            legend: {
                data: ['建档量', '门诊量'],
                right: 60,
                top: 0
            },
            grid: {
                left: 60,
                right: 60,
                bottom: 30,
                top: 40,
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: categories,
                axisLine: { lineStyle: { color: '#d9d9d9' } },
                axisLabel: { color: '#8c8c8c' }
            },
            yAxis: [
                {
                    type: 'value',
                    name: '人次',
                    position: 'left',
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { lineStyle: { color: '#f0f0f0' } },
                    axisLabel: { color: '#8c8c8c' }
                },
                {
                    type: 'value',
                    name: '人',
                    position: 'right',
                    axisLine: { show: false },
                    axisTick: { show: false },
                    splitLine: { show: false },
                    axisLabel: { color: '#8c8c8c' }
                }
            ],
            series: [
                {
                    name: '建档量',
                    type: 'bar',
                    barWidth: '40%',
                    itemStyle: { color: '#1890ff' },
                    data: archiveData,
                    label: {
                        show: true,
                        position: 'top',
                        color: '#1890ff',
                        fontSize: 11
                    }
                },
                {
                    name: '门诊量',
                    type: 'line',
                    yAxisIndex: 1,
                    smooth: false,
                    itemStyle: { color: '#fa8c16' },
                    lineStyle: { color: '#fa8c16', width: 2 },
                    symbol: 'circle',
                    symbolSize: 6,
                    data: outpatientData
                }
            ]
        };
        this.charts.age.setOption(option);
    }

    renderPieChart(chart, data, title) {
        const colors = ['#1890ff', '#52c41a', '#13c2c2', '#faad14', '#f5222d', '#722ed1', '#eb2f96', '#fa541c'];
        const chartData = (data && Array.isArray(data)) ? data : [];
        const option = {
            title: {
                text: title,
                left: 'left',
                top: 0,
                textStyle: {
                    fontSize: 14,
                    fontWeight: 600,
                    color: '#262626'
                }
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
}

const patientPortraitController = new PatientPortraitController();
