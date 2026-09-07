/**
 * 门诊财务报表 - 页面逻辑
 * 请求走统一网关 apiRequest('reports.cash.outpatient-finance')：
 *   主请求（不带 pieTypes）：一次调用返回 indicator（卡片）/ detailList（明细表）/ barList（柱图 1~4）
 *   饼图按需请求（带 pieTypes，如 "1,2,3"）：点内层分析 tab 时才查询对应类型的饼图，前端按 outerType+日期范围+tab 缓存
 * 外层tab切换时请求一次并缓存，内层tab切换读缓存，避免重复请求。
 */

// ===== 日期范围选择器变量 =====
var pickerMode = 'month';
var monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
var weekDays = ['日', '一', '二', '三', '四', '五', '六'];
var rangeStep = 'start';
var rangeStart = { year: 2026, month: 4, day: 1 };
var rangeEnd = { year: 2026, month: 4, day: 1 };

// ===== 统计类型映射 =====
var statisticTypeMap = { summary: '1', income: '2', refund: '3' };
var cardsIdMap = { summary: 'cards-summary', income: 'cards-income', refund: 'cards-refund' };
var currentOuter = 'summary';

// 复合响应缓存（按 outerType + 日期范围 + 时间粒度 缓存）
var cachedData = null;
var cachedOuter = null;
var cachedDateRange = null; // "startTime|endTime|timeType"

// ===== 全局加载计数器 =====
var loadingCount = 0;
function showLoading() {
    loadingCount++;
    document.getElementById('loadingOverlay').classList.add('show');
}
function hideLoading() {
    loadingCount--;
    if (loadingCount <= 0) {
        loadingCount = 0;
        document.getElementById('loadingOverlay').classList.remove('show');
    }
}

function getDateRange() {
    var startText = document.getElementById('rangeStartDisplay').textContent;
    var endText = document.getElementById('rangeEndDisplay').textContent;
    return { startTime: startText, endTime: endText };
}

// ===== 单接口复合请求 =====
function pad2(n) { return String(n).padStart(2, '0'); }

// 月模式：start=当月1号；end与start同月→当月最后一天，否则→前一个月最后一天
function toQueryDateRange() {
    if (pickerMode === 'day') {
        return {
            startDate: rangeStart.year + '-' + pad2(rangeStart.month) + '-' + pad2(rangeStart.day),
            endDate: rangeEnd.year + '-' + pad2(rangeEnd.month) + '-' + pad2(rangeEnd.day)
        };
    }
    var startDate = rangeStart.year + '-' + pad2(rangeStart.month) + '-01';
    var endDate;
    if (rangeStart.year === rangeEnd.year && rangeStart.month === rangeEnd.month) {
        endDate = rangeEnd.year + '-' + pad2(rangeEnd.month) + '-' + getDaysInMonth(rangeEnd.year, rangeEnd.month);
    } else {
        var ly = rangeEnd.month === 1 ? rangeEnd.year - 1 : rangeEnd.year;
        var lm = rangeEnd.month === 1 ? 12 : rangeEnd.month - 1;
        endDate = ly + '-' + pad2(lm) + '-' + getDaysInMonth(ly, lm);
    }
    return { startDate: startDate, endDate: endDate };
}

async function fetchAll(outerType) {
    var queryRange = toQueryDateRange();
    var params = {
        statisticType: parseInt(statisticTypeMap[outerType]),
        timeType: pickerMode === 'day' ? 2 : 1,
        startDate: queryRange.startDate,
        endDate: queryRange.endDate
    };

    var dateRangeKey = queryRange.startDate + '|' + queryRange.endDate + '|' + params.timeType;
    console.log('[门诊财务] 请求:', outerType, params);
    showLoading();
    try {
        var data = await apiRequest('reports.cash.outpatient-finance', 'endpoint', params);
        cachedData = data;
        cachedOuter = outerType;
        cachedDateRange = dateRangeKey;

        updateCards(outerType, data.indicator || {});

        listState[outerType].data = data.detailList || [];
        listState[outerType].page = 1;
        renderTable(outerType);

        var suffix = getActiveInnerSuffix(outerType);
        updateBarFromCache(suffix);
        ensurePies(outerType, suffix);
    } catch (err) {
        console.error('[门诊财务] 接口调用失败:', err);
        cachedData = null;
        cachedOuter = null;
        cachedDateRange = null;
        updateCards(outerType, {});
        listState[outerType].data = [];
        renderTable(outerType);
        updateBarFromCache(getActiveInnerSuffix(outerType));
        ensurePies(outerType, getActiveInnerSuffix(outerType));
    } finally {
        hideLoading();
    }
}

function getActiveInnerSuffix(outerType) {
    var outerEl = document.getElementById('outer-' + outerType);
    var activeInner = outerEl ? outerEl.querySelector('.inner-tab.active') : null;
    if (activeInner) {
        var id = activeInner.dataset.inner;
        return id.split('-').pop();
    }
    return 'visit';
}

// ===== 指标卡片 =====
function formatNumber(v) {
    if (v === undefined || v === null || v === '') return '000';
    var n = parseFloat(v);
    if (isNaN(n)) return '000';
    return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 });
}

function updateCards(outerType, indicator) {
    var container = document.getElementById(cardsIdMap[outerType]);
    if (!container) return;
    var fieldMap = {
        outpatient_volume: 'outpatientVolume',
        number_charges: 'numberCharges',
        number_receipt: 'numberReceipt',
        amount: 'amount'
    };
    var yoyMap = {
        outpatient_volume: 'outpatientVolumeYoy',
        number_charges: 'numberChargesYoy',
        number_receipt: 'numberReceiptYoy',
        amount: 'amountYoy'
    };
    ['outpatient_volume', 'number_charges', 'number_receipt', 'amount'].forEach(function (field) {
        var valEl = container.querySelector('[data-field="' + field + '"]');
        var yoyEl = container.querySelector('[data-field="' + field + '_yoy"]');
        if (valEl && indicator[fieldMap[field]] !== undefined) {
            valEl.textContent = formatNumber(indicator[fieldMap[field]]);
        }
        if (yoyEl && indicator[yoyMap[field]]) {
            yoyEl.textContent = '同比 ' + indicator[yoyMap[field]];
        }
    });
}

// ===== 明细表 =====
var tableIdMap = { summary: 'table-summary', income: 'table-income', refund: 'table-refund' };
var emptyIdMap = { summary: 'empty-summary', income: 'empty-income', refund: 'empty-refund' };
var pagiIdMap = { summary: 'pagi-summary', income: 'pagi-income', refund: 'pagi-refund' };

var listState = {
    summary: { data: [], page: 1, pageSize: 10 },
    income: { data: [], page: 1, pageSize: 10 },
    refund: { data: [], page: 1, pageSize: 10 }
};

function calcYoy(current, lastYear) {
    var cur = parseFloat(current) || 0;
    var last = parseFloat(lastYear) || 0;
    if (last === 0) return '--';
    return ((cur - last) / last * 100).toFixed(2) + '%';
}

function renderTable(outerType) {
    var state = listState[outerType];
    var allData = state.data;
    var page = state.page;
    var pageSize = state.pageSize;
    var total = allData.length;
    var totalPages = Math.max(1, Math.ceil(total / pageSize));
    if (page > totalPages) page = totalPages;
    state.page = page;

    var tbody = document.querySelector('#' + tableIdMap[outerType] + ' tbody');
    var emptyEl = document.getElementById(emptyIdMap[outerType]);
    var pagiEl = document.getElementById(pagiIdMap[outerType]);

    if (total === 0) {
        tbody.innerHTML = '';
        emptyEl.style.display = 'block';
        pagiEl.innerHTML = '<span class="pagi-info">' + pageSize + '条/页 共 0 条</span>';
        return;
    }

    emptyEl.style.display = 'none';
    var start = (page - 1) * pageSize;
    var end = Math.min(start + pageSize, total);
    var pageData = allData.slice(start, end);

    var html = '';
    pageData.forEach(function (row) {
        var ovYoy = calcYoy(row.currentDateOutpatientVolume, row.lastYearOutpatientVolume);
        var ncYoy = calcYoy(row.currentDateNumberCharges, row.lastYearNumberCharges);
        var nrYoy = calcYoy(row.currentDateNumberReceipt, row.lastYearNumberReceipt);
        var amYoy = calcYoy(row.currentDateAmount, row.lastYearAmount);

        html += '<tr>';
        html += '<td>' + (row.dateTime || '') + '</td>';
        html += '<td>' + fmtCell(row.lastYearOutpatientVolume) + '</td>';
        html += '<td>' + fmtCell(row.currentDateOutpatientVolume) + '</td>';
        html += '<td>' + ovYoy + '</td>';
        html += '<td>' + fmtCell(row.lastYearNumberCharges) + '</td>';
        html += '<td>' + fmtCell(row.currentDateNumberCharges) + '</td>';
        html += '<td>' + ncYoy + '</td>';
        html += '<td>' + fmtCell(row.lastYearNumberReceipt) + '</td>';
        html += '<td>' + fmtCell(row.currentDateNumberReceipt) + '</td>';
        html += '<td>' + nrYoy + '</td>';
        html += '<td>' + fmtCell(row.lastYearAmount) + '</td>';
        html += '<td>' + fmtCell(row.currentDateAmount) + '</td>';
        html += '<td>' + amYoy + '</td>';
        html += '</tr>';
    });
    tbody.innerHTML = html;

    var ph = '';
    ph += '<span class="pagi-info">' + pageSize + '条/页 共 ' + total + ' 条</span>';
    ph += '<button ' + (page <= 1 ? 'disabled' : '') + ' onclick="gotoPage(\'' + outerType + '\', ' + (page - 1) + ')">&lt;</button>';
    for (var p = 1; p <= totalPages; p++) {
        if (totalPages > 7 && Math.abs(p - page) > 2 && p !== 1 && p !== totalPages) {
            if (p === 2 || p === totalPages - 1) ph += '<span>...</span>';
            continue;
        }
        ph += '<button class="' + (p === page ? 'pagi-current' : '') + '" onclick="gotoPage(\'' + outerType + '\', ' + p + ')">' + p + '</button>';
    }
    ph += '<button ' + (page >= totalPages ? 'disabled' : '') + ' onclick="gotoPage(\'' + outerType + '\', ' + (page + 1) + ')">&gt;</button>';
    ph += '<select onchange="changePageSize(\'' + outerType + '\', this.value)">';
    [10, 20, 50].forEach(function (s) {
        ph += '<option value="' + s + '"' + (s === pageSize ? ' selected' : '') + '>' + s + ' 条/页</option>';
    });
    ph += '</select>';
    ph += '<span class="pagi-jump">跳至 <input type="number" min="1" max="' + totalPages + '" value="' + page + '" onkeydown="if(event.key===\'Enter\')gotoPage(\'' + outerType + '\',parseInt(this.value))"> 页</span>';
    ph += '<button onclick="var v=this.previousElementSibling.querySelector(\'input\').value;gotoPage(\'' + outerType + '\',parseInt(v))">确定</button>';
    pagiEl.innerHTML = ph;
}

function fmtCell(v) {
    if (v === undefined || v === null || v === '') return '0';
    var n = parseFloat(v);
    return (isNaN(n) ? '0' : n.toLocaleString('zh-CN', { maximumFractionDigits: 2 }));
}

function gotoPage(outerType, page) {
    var state = listState[outerType];
    var totalPages = Math.max(1, Math.ceil(state.data.length / state.pageSize));
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;
    state.page = page;
    renderTable(outerType);
}

function changePageSize(outerType, size) {
    listState[outerType].pageSize = parseInt(size);
    listState[outerType].page = 1;
    renderTable(outerType);
}

// ===== 外层标签切换 =====
document.querySelectorAll('.outer-tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
        document.querySelectorAll('.outer-tab').forEach(function (t) { t.classList.remove('active'); });
        this.classList.add('active');
        var id = this.dataset.outer;
        currentOuter = id;
        document.querySelectorAll('.container > .tab-content').forEach(function (c) { c.classList.remove('active'); });
        document.getElementById('outer-' + id).classList.add('active');
        setTimeout(resizeAllCharts, 50);
        // 检查缓存：outerType + 查询日期范围 + 时间粒度一致则直接用缓存
        var queryRange = toQueryDateRange();
        var dateRangeKey = queryRange.startDate + '|' + queryRange.endDate + '|' + (pickerMode === 'day' ? 2 : 1);
        if (cachedOuter === id && cachedDateRange === dateRangeKey && cachedData) {
            updateCards(id, cachedData.indicator || {});
            listState[id].data = cachedData.detailList || [];
            listState[id].page = 1;
            renderTable(id);
            var suffix = getActiveInnerSuffix(id);
            updateBarFromCache(suffix);
            ensurePies(id, suffix);
        } else {
            fetchAll(id);
        }
    });
});

// ===== 图表 =====
var allCharts = [];
var barChartInstances = {};

// 内层tab后缀 -> business_type 映射
var innerTabBusinessMap = { 'visit': '1', 'pay': '2', 'receipt': '3', 'net': '4', 'amount': '4' };

var barChartMap = {
    summary: { '1': 'chart-summary-visit', '2': 'chart-summary-pay', '3': 'chart-summary-receipt', '4': 'chart-summary-net' },
    income: { '1': 'chart-income-visit', '2': 'chart-income-pay', '3': 'chart-income-receipt', '4': 'chart-income-amount' },
    refund: { '1': 'chart-refund-visit', '2': 'chart-refund-pay', '3': 'chart-refund-receipt', '4': 'chart-refund-amount' }
};

function getOrCreateBarChart(domId) {
    if (typeof echarts === 'undefined') { console.error('echarts 未加载'); return null; }
    if (barChartInstances[domId]) return barChartInstances[domId];
    var dom = document.getElementById(domId);
    if (!dom) return null;
    var chart = echarts.init(dom);
    allCharts.push(chart);
    barChartInstances[domId] = chart;
    return chart;
}

function updateBarChart(domId, xData, lastYearData, currentData) {
    var chart = getOrCreateBarChart(domId);
    if (!chart) return;
    chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['去年同期', '当前日期'], top: 0, right: 20, textStyle: { fontSize: 12 } },
        grid: { left: 50, right: 30, top: 35, bottom: 40 },
        xAxis: { type: 'category', data: xData },
        yAxis: { type: 'value' },
        series: [
            { name: '去年同期', type: 'bar', data: lastYearData, barWidth: 20, itemStyle: { color: '#1890ff' } },
            { name: '当前日期', type: 'bar', data: currentData, barWidth: 20, itemStyle: { color: '#36cbcb' } }
        ]
    }, true);
}

function updateBarFromCache(innerSuffix) {
    if (!cachedData) return;
    var bt = innerTabBusinessMap[innerSuffix];
    if (!bt) return;
    var domId = barChartMap[currentOuter][String(bt)];
    var bars = (cachedData.barList || {})[String(bt)] || [];
    var xData = [], lastYearData = [], currentData = [];
    bars.forEach(function (item) {
        xData.push(item.dateTime || '');
        lastYearData.push(parseInt(item.lastYearNumber) || 0);
        currentData.push(parseInt(item.currentDateNumber) || 0);
    });
    updateBarChart(domId, xData, lastYearData, currentData);
}

// ===== 内层标签切换 =====
document.querySelectorAll('.inner-tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
        var parent = this.closest('.tab-content');
        parent.querySelectorAll('.inner-tab').forEach(function (t) { t.classList.remove('active'); });
        this.classList.add('active');
        var id = this.dataset.inner;
        var children = parent.children;
        for (var i = 0; i < children.length; i++) {
            if (children[i].classList.contains('tab-content')) {
                children[i].classList.remove('active');
            }
        }
        document.getElementById('inner-' + id).classList.add('active');
        setTimeout(resizeAllCharts, 50);
        var parts = id.split('-');
        var suffix = parts[parts.length - 1];
        if (cachedOuter === currentOuter && cachedData) {
            updateBarFromCache(suffix);
            ensurePies(currentOuter, suffix);
        } else {
            fetchAll(currentOuter);
        }
    });
});

// ===== 圆环图（饼图） =====
var pieChartInstances = {};
var PIE_COLORS = ['#1890ff', '#36cbcb', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#eb2f96'];
var pieLegendState = {};
var PIE_LEGEND_PAGE_SIZE = 5;

// 金额类饼图（显示保留两位小数）；人次/张数类（bt1-5）显示整数
var AMOUNT_BT_MAP = { '6': true, '7': true, '8': true, '9': true, '10': true };

// 饼图 DOM ID -> business_type 映射（新增第5个：应收金额 -> 10）
var pieChartMap = {
    summary: {
        'pie-summary-visit-1': '1', 'pie-summary-visit-2': '2', 'pie-summary-visit-3': '3',
        'pie-summary-pay-1': '4', 'pie-summary-pay-2': '5',
        'pie-summary-receipt-1': '4', 'pie-summary-receipt-2': '5',
        'pie-summary-net-1': '6', 'pie-summary-net-2': '7', 'pie-summary-net-3': '8', 'pie-summary-net-4': '9', 'pie-summary-net-5': '10'
    },
    income: {
        'pie-income-visit-1': '1', 'pie-income-visit-2': '2', 'pie-income-visit-3': '3',
        'pie-income-pay-1': '4', 'pie-income-pay-2': '5',
        'pie-income-receipt-1': '4', 'pie-income-receipt-2': '5',
        'pie-income-amount-1': '6', 'pie-income-amount-2': '7', 'pie-income-amount-3': '8', 'pie-income-amount-4': '9', 'pie-income-amount-5': '10'
    },
    refund: {
        'pie-refund-visit-1': '1', 'pie-refund-visit-2': '2', 'pie-refund-visit-3': '3',
        'pie-refund-pay-1': '4', 'pie-refund-pay-2': '5',
        'pie-refund-receipt-1': '4', 'pie-refund-receipt-2': '5',
        'pie-refund-amount-1': '6', 'pie-refund-amount-2': '7', 'pie-refund-amount-3': '8', 'pie-refund-amount-4': '9', 'pie-refund-amount-5': '10'
    }
};

// 内层tab后缀 -> 该tab下的饼图序号列表（金额tab含第5个应收金额饼图）
var innerTabPieMap = {
    'visit': ['1', '2', '3'],
    'pay': ['1', '2'],
    'receipt': ['1', '2'],
    'net': ['1', '2', '3', '4', '5'],
    'amount': ['1', '2', '3', '4', '5']
};

function calcPer(curr, prev) {
    var c = parseFloat(curr) || 0;
    var p = parseFloat(prev) || 0;
    if (p === 0) return c === 0 ? '0.00%' : '+100.00%';
    var val = ((c - p) / Math.abs(p) * 100).toFixed(2);
    return (val >= 0 ? '+' : '') + val + '%';
}

function getOrCreatePieChart(domId) {
    if (typeof echarts === 'undefined') { console.error('echarts 未加载'); return null; }
    if (pieChartInstances[domId]) return pieChartInstances[domId];
    var dom = document.getElementById(domId);
    if (!dom) return null;
    var chart = echarts.init(dom);
    allCharts.push(chart);
    pieChartInstances[domId] = chart;
    return chart;
}

function renderPieLegend(domId, pieData) {
    if (!pieData || pieData.length === 0) return;
    var chartDom = document.getElementById(domId);
    if (!chartDom || !chartDom.parentNode) return;
    var wrapper = chartDom.parentNode;
    var listEl = wrapper.querySelector('.pie-legend-list');
    if (!listEl) {
        listEl = document.createElement('div');
        listEl.className = 'pie-legend-list';
        wrapper.appendChild(listEl);
    }
    if (!pieLegendState[domId]) pieLegendState[domId] = { page: 1 };
    var state = pieLegendState[domId];
    state.data = pieData;
    var total = 0;
    pieData.forEach(function (item) { total += parseFloat(item.value) || 0; });
    var pageSize = PIE_LEGEND_PAGE_SIZE;
    var totalPages = Math.max(1, Math.ceil(pieData.length / pageSize));
    if (state.page > totalPages) state.page = totalPages;
    var start = (state.page - 1) * pageSize;
    var end = Math.min(start + pageSize, pieData.length);
    var pageData = pieData.slice(start, end);
    var html = '';
    pageData.forEach(function (item, idx) {
        var globalIdx = start + idx;
        var color = PIE_COLORS[globalIdx % PIE_COLORS.length];
        var val = parseFloat(item.value) || 0;
        var pct = total > 0 ? (val / total * 100).toFixed(1) + '%' : '';
        html += '<div class="pie-legend-item">';
        html += '<span class="pie-legend-dot" style="background:' + color + '"></span>';
        html += '<span class="pie-legend-name">' + (item.name || '未知') + '</span>';
        html += '<span class="pie-legend-val">' + (item.disp != null ? item.disp : item.value) + '</span>';
        html += '<span class="pie-legend-pct">' + pct + '</span>';
        if (item.per) {
            var perCls = item.per.indexOf('-') === 0 ? 'pie-legend-per down' : 'pie-legend-per up';
            html += '<span class="' + perCls + '">' + item.per + '</span>';
        }
        html += '</div>';
    });
    if (totalPages > 1) {
        html += '<div class="pie-legend-pagination">';
        html += '<button ' + (state.page <= 1 ? 'disabled' : '') + ' onclick="pieLegendPage(\'' + domId + '\', ' + (state.page - 1) + ')">上一页</button>';
        html += '<span class="page-info">' + state.page + '/' + totalPages + ' 页</span>';
        html += '<button ' + (state.page >= totalPages ? 'disabled' : '') + ' onclick="pieLegendPage(\'' + domId + '\', ' + (state.page + 1) + ')">下一页</button>';
        html += '</div>';
    }
    listEl.innerHTML = html;
}

function pieLegendPage(domId, page) {
    if (!pieLegendState[domId]) return;
    var state = pieLegendState[domId];
    var totalPages = Math.max(1, Math.ceil(state.data.length / PIE_LEGEND_PAGE_SIZE));
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;
    state.page = page;
    renderPieLegend(domId, state.data);
}

function updatePieChart(domId, pieData) {
    var chart = getOrCreatePieChart(domId);
    if (!chart) return;
    var seriesData = [];
    pieData.forEach(function (item, idx) {
        var perText = item.per || '';
        var color = PIE_COLORS[idx % PIE_COLORS.length];
        seriesData.push({
            value: parseFloat(item.value) || 0,
            name: item.name || '未知',
            itemStyle: { color: color },
            per: perText
        });
    });
    chart.setOption({
        tooltip: {
            trigger: 'item',
            formatter: function (params) {
                var d = params.data;
                return d.name + ': ' + (d.disp != null ? d.disp : params.value) + ' (' + params.percent + '%)<br/>同比: ' + (d.per || '-');
            }
        },
        legend: { show: false },
        series: [{
            type: 'pie', radius: ['45%', '70%'],
            center: ['50%', '50%'],
            label: { show: false },
            emphasis: {
                label: { show: true, fontSize: 13, fontWeight: 'bold',
                    formatter: '{b}\n{c} ({d}%)'
                }
            },
            data: seriesData
        }]
    }, true);
    renderPieLegend(domId, pieData);
}

function updatePiesFromList(innerSuffix, pieList) {
    var pieNums = innerTabPieMap[innerSuffix];
    if (!pieNums || pieNums.length === 0) return;
    var map = pieChartMap[currentOuter];
    if (!map) return;
    pieNums.forEach(function (num) {
        var domId = 'pie-' + currentOuter + '-' + innerSuffix + '-' + num;
        var bt = map[domId];
        if (!bt) return;
        var list = (pieList || {})[String(bt)] || [];
        var pieData = [];
        list.forEach(function (item) {
            if (item.name && item.name.trim() !== '') {
                var v = Math.abs(parseFloat(item.currValue)) || 0;
                pieData.push({
                    name: item.name,
                    value: v,
                    disp: AMOUNT_BT_MAP[String(bt)] ? v.toFixed(2) : String(v),
                    per: calcPer(item.currValue, item.prevValue)
                });
            }
        });
        updatePieChart(domId, pieData);
    });
}

// ===== 饼图按需加载（内层 tab 点击时才请求对应类型的饼图） =====
// 内层tab后缀 -> 需要的饼图业务类型（与后端 business_type 对应）
var innerTabPieTypesMap = {
    'visit': '1,2,3',
    'pay': '4,5',
    'receipt': '4,5',
    'net': '6,7,8,9,10',
    'amount': '6,7,8,9,10'
};

// 饼图缓存：outerType|日期范围|innerSuffix -> pieList
var pieCache = {};

// tab 名称映射（与 cash-outpatient-finance.html 的 data-inner / pie-title 对应，用于后端日志标识）
var outerTypeNameMap = { summary: '汇总', income: '进项', refund: '退项' };
var innerTabNameMap = {
    'summary-visit': '门诊量分析', 'summary-pay': '缴费人次分析', 'summary-receipt': '收据张数分析', 'summary-net': '净收入金额分析',
    'income-visit': '门诊量分析', 'income-pay': '缴费人次分析', 'income-receipt': '收据张数分析', 'income-amount': '收入金额分析',
    'refund-visit': '门诊量分析', 'refund-pay': '退费人次分析', 'refund-receipt': '收据张数分析', 'refund-amount': '退费金额分析'
};

function pieTabName(outerType, innerSuffix) {
    var outer = outerTypeNameMap[outerType] || outerType;
    var inner = innerTabNameMap[outerType + '-' + innerSuffix] || innerSuffix;
    return outer + '-' + inner;
}

function currentRangeKey() {
    var queryRange = toQueryDateRange();
    return queryRange.startDate + '|' + queryRange.endDate + '|' + (pickerMode === 'day' ? 2 : 1);
}

async function ensurePies(outerType, innerSuffix) {
    var types = innerTabPieTypesMap[innerSuffix];
    if (!types) return;
    var key = outerType + '|' + currentRangeKey() + '|' + innerSuffix;
    if (pieCache[key]) {
        updatePiesFromList(innerSuffix, pieCache[key]);
        return;
    }
    var queryRange = toQueryDateRange();
    var params = {
        statisticType: parseInt(statisticTypeMap[outerType]),
        timeType: pickerMode === 'day' ? 2 : 1,
        startDate: queryRange.startDate,
        endDate: queryRange.endDate,
        pieTypes: types,
        pieTab: pieTabName(outerType, innerSuffix)
    };
    console.log('[门诊财务] 请求饼图:', outerType, innerSuffix, types);
    showLoading();
    try {
        var data = await apiRequest('reports.cash.outpatient-finance', 'endpoint', params);
        pieCache[key] = data.pieList || {};
        updatePiesFromList(innerSuffix, pieCache[key]);
    } catch (err) {
        console.error('[门诊财务] 饼图查询失败:', err);
        updatePiesFromList(innerSuffix, {});
    } finally {
        hideLoading();
    }
}

// 初始化空柱状图（页面加载后由接口填充数据）
['summary', 'income', 'refund'].forEach(function (ot) {
    for (var bt = 1; bt <= 4; bt++) {
        var domId = barChartMap[ot][String(bt)];
        updateBarChart(domId, [], [], []);
    }
});

function resizeAllCharts() {
    allCharts.forEach(function (c) { c.resize(); });
}
window.addEventListener('resize', resizeAllCharts);

// ===== 日期范围选择器（月/天模式） =====

// 切换按月/按天
document.querySelectorAll('.time-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
        document.querySelectorAll('.time-btn').forEach(function (b) { b.classList.remove('active'); });
        this.classList.add('active');
        pickerMode = this.dataset.mode;
        var now = new Date();
        var y = now.getFullYear(), m = now.getMonth() + 1, d = now.getDate();
        rangeStart = { year: y, month: m, day: d };
        rangeEnd = { year: y, month: m, day: d };
        rangeStep = 'start';
        updateRangeDisplay();
        closeRangePicker();
        fetchAll(currentOuter);
    });
});

function formatRangeVal(obj) {
    if (pickerMode === 'month') {
        return obj.year + '-' + String(obj.month).padStart(2, '0');
    } else {
        return obj.year + '-' + String(obj.month).padStart(2, '0') + '-' + String(obj.day).padStart(2, '0');
    }
}

function updateRangeDisplay() {
    document.getElementById('rangeStartDisplay').textContent = formatRangeVal(rangeStart);
    document.getElementById('rangeEndDisplay').textContent = formatRangeVal(rangeEnd);
    var startEl = document.getElementById('rangeStartDisplay');
    var endEl = document.getElementById('rangeEndDisplay');
    startEl.classList.toggle('picking', rangeStep === 'start');
    endEl.classList.toggle('picking', rangeStep === 'end');
}

function getDaysInMonth(year, month) {
    return new Date(year, month, 0).getDate();
}

function compareDateObj(a, b) {
    if (a.year !== b.year) return a.year - b.year;
    if (a.month !== b.month) return a.month - b.month;
    return (a.day || 1) - (b.day || 1);
}

function renderRangePicker() {
    var wrap = document.getElementById('rangePicker');
    var dropdown = wrap.querySelector('.month-picker-dropdown');
    var year = parseInt(dropdown.dataset.year);
    var selMonth = parseInt(dropdown.dataset.month);

    if (pickerMode === 'month') {
        var html = '<div class="mp-header">';
        html += '<button onclick="event.stopPropagation();changePickerYear(-1)">&lsaquo;</button>';
        html += '<span class="mp-year">' + year + '年</span>';
        html += '<button onclick="event.stopPropagation();changePickerYear(1)">&rsaquo;</button>';
        html += '</div><div class="mp-months">';
        var now = new Date();
        for (var i = 1; i <= 12; i++) {
            var cls = 'mp-month';
            var thisObj = { year: year, month: i, day: 1 };
            if (year === rangeStart.year && i === rangeStart.month) cls += ' selected';
            if (year === rangeEnd.year && i === rangeEnd.month) cls += ' selected';
            if (compareDateObj(thisObj, rangeStart) > 0 && compareDateObj(thisObj, rangeEnd) < 0) cls += ' in-range';
            if (year === now.getFullYear() && i === now.getMonth() + 1) cls += ' current';
            html += '<div class="' + cls + '" onclick="event.stopPropagation();pickRangeMonth(' + i + ')">' + monthNames[i - 1] + '</div>';
        }
        html += '</div>';
        dropdown.innerHTML = html;
    } else {
        var html = '<div class="mp-header">';
        html += '<button onclick="event.stopPropagation();changePickerMonth(-1)">&lsaquo;</button>';
        html += '<span class="mp-year"><span class="mp-month-btn" onclick="event.stopPropagation();showMonthSelectForDay()">' + year + '年' + selMonth + '月</span></span>';
        html += '<button onclick="event.stopPropagation();changePickerMonth(1)">&rsaquo;</button>';
        html += '</div>';
        html += '<div class="mp-days-header">';
        for (var w = 0; w < 7; w++) html += '<span>' + weekDays[w] + '</span>';
        html += '</div><div class="mp-days">';
        var daysInMonth = getDaysInMonth(year, selMonth);
        var firstDow = new Date(year, selMonth - 1, 1).getDay();
        var prevDays = getDaysInMonth(year, selMonth - 1);
        var now = new Date();
        for (var p = firstDow - 1; p >= 0; p--) {
            html += '<div class="mp-day other-month">' + (prevDays - p) + '</div>';
        }
        for (var d = 1; d <= daysInMonth; d++) {
            var cls = 'mp-day';
            var thisObj = { year: year, month: selMonth, day: d };
            if (year === rangeStart.year && selMonth === rangeStart.month && d === rangeStart.day) cls += ' selected';
            if (year === rangeEnd.year && selMonth === rangeEnd.month && d === rangeEnd.day) cls += ' selected';
            if (compareDateObj(thisObj, rangeStart) > 0 && compareDateObj(thisObj, rangeEnd) < 0) cls += ' in-range';
            if (year === now.getFullYear() && selMonth === now.getMonth() + 1 && d === now.getDate()) cls += ' current';
            html += '<div class="' + cls + '" onclick="event.stopPropagation();pickRangeDay(' + d + ')">' + d + '</div>';
        }
        var totalCells = firstDow + daysInMonth;
        var remain = (7 - totalCells % 7) % 7;
        for (var n = 1; n <= remain; n++) {
            html += '<div class="mp-day other-month">' + n + '</div>';
        }
        html += '</div>';
        dropdown.innerHTML = html;
    }
}

function toggleRangePicker() {
    var wrap = document.getElementById('rangePicker');
    var dropdown = wrap.querySelector('.month-picker-dropdown');
    if (!dropdown.classList.contains('show')) {
        rangeStep = 'start';
        dropdown.dataset.year = rangeStart.year;
        dropdown.dataset.month = rangeStart.month;
        updateRangeDisplay();
        renderRangePicker();
        dropdown.classList.add('show');
    } else {
        closeRangePicker();
    }
}

function closeRangePicker() {
    var dropdown = document.querySelector('#rangePicker .month-picker-dropdown');
    if (dropdown) dropdown.classList.remove('show');
}

function changePickerYear(delta) {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    dd.dataset.year = parseInt(dd.dataset.year) + delta;
    renderRangePicker();
}

function changePickerMonth(delta) {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    var y = parseInt(dd.dataset.year), m = parseInt(dd.dataset.month);
    m += delta;
    if (m < 1) { m = 12; y--; }
    if (m > 12) { m = 1; y++; }
    dd.dataset.year = y;
    dd.dataset.month = m;
    renderRangePicker();
}

function showMonthSelectForDay() {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    var year = parseInt(dd.dataset.year);
    var selMonth = parseInt(dd.dataset.month);
    var now = new Date();
    var html = '<div class="mp-header">';
    html += '<button onclick="event.stopPropagation();changePickerYear(-1);showMonthSelectForDay()">&lsaquo;</button>';
    html += '<span class="mp-year">' + year + '年</span>';
    html += '<button onclick="event.stopPropagation();changePickerYear(1);showMonthSelectForDay()">&rsaquo;</button>';
    html += '</div><div class="mp-months">';
    for (var i = 1; i <= 12; i++) {
        var cls = 'mp-month';
        if (i === selMonth) cls += ' selected';
        if (parseInt(dd.dataset.year) === now.getFullYear() && i === now.getMonth() + 1) cls += ' current';
        html += '<div class="' + cls + '" onclick="event.stopPropagation();pickMonthForDayMode(' + i + ')">' + monthNames[i - 1] + '</div>';
    }
    html += '</div>';
    dd.innerHTML = html;
}

function pickMonthForDayMode(month) {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    dd.dataset.month = month;
    renderRangePicker();
}

function checkRangeLimit(start, end) {
    if (pickerMode === 'month') {
        var diff = (end.year - start.year) * 12 + (end.month - start.month);
        if (diff > 12) {
            alert('按月统计时，开始时间与结束时间最多相差一年（12个月）');
            return false;
        }
    } else {
        var startDate = new Date(start.year, start.month - 1, start.day);
        var endDate = new Date(end.year, end.month - 1, end.day);
        var diffDays = Math.round((endDate - startDate) / (1000 * 60 * 60 * 24));
        if (diffDays > 31) {
            alert('按天统计时，开始时间与结束时间最多相差一个月（31天）');
            return false;
        }
    }
    return true;
}

function pickRangeMonth(month) {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    var year = parseInt(dd.dataset.year);
    var picked = { year: year, month: month, day: 1 };

    if (rangeStep === 'start') {
        rangeStart = picked;
        rangeEnd = picked;
        rangeStep = 'end';
        updateRangeDisplay();
        renderRangePicker();
    } else {
        var tmpStart = rangeStart, tmpEnd = picked;
        if (compareDateObj(picked, rangeStart) < 0) {
            tmpStart = picked;
            tmpEnd = rangeStart;
        }
        if (!checkRangeLimit(tmpStart, tmpEnd)) return;
        rangeStart = tmpStart;
        rangeEnd = tmpEnd;
        rangeStep = 'start';
        updateRangeDisplay();
        closeRangePicker();
        fetchAll(currentOuter);
    }
}

function pickRangeDay(day) {
    var dd = document.querySelector('#rangePicker .month-picker-dropdown');
    var year = parseInt(dd.dataset.year);
    var month = parseInt(dd.dataset.month);
    var picked = { year: year, month: month, day: day };

    if (rangeStep === 'start') {
        rangeStart = picked;
        rangeEnd = picked;
        rangeStep = 'end';
        updateRangeDisplay();
        renderRangePicker();
    } else {
        var tmpStart = rangeStart, tmpEnd = picked;
        if (compareDateObj(picked, rangeStart) < 0) {
            tmpStart = picked;
            tmpEnd = rangeStart;
        }
        if (!checkRangeLimit(tmpStart, tmpEnd)) return;
        rangeStart = tmpStart;
        rangeEnd = tmpEnd;
        rangeStep = 'start';
        updateRangeDisplay();
        closeRangePicker();
        fetchAll(currentOuter);
    }
}

// 点击外部关闭
document.addEventListener('click', function (ev) {
    if (!ev.target.closest('.month-picker-wrap')) {
        closeRangePicker();
    }
});

// 页面加载时查询一次
fetchAll(currentOuter);
