/**
 * 运营数据统计页面主逻辑(13个页签单接口, body.tab 区分)
 */

/** 页签定义: time=是否按时间筛选, paged=是否后端分页 */
const TABS = [
    { id: 'special-express',   label: '特殊制剂快递',     time: true,  paged: false },
    { id: 'no-accept-long',    label: '长时间不接诊',     time: true,  paged: false },
    { id: 'no-accept-24h',     label: '超时未接诊',       time: true,  paged: false },
    { id: 'doctor-response',   label: '医生响应排名',     time: false, paged: false },
    { id: 'rx-audit-time',     label: '处方审核时长',     time: true,  paged: false },
    { id: 'clinic-unit',       label: '医生出诊单元',     time: true,  paged: false },
    { id: 'patient-eval',      label: '患者评价',         time: true,  paged: false },
    { id: 'cancel-reason',     label: '预约取消原因',     time: true,  paged: true },
    { id: 'return-no-reg',     label: '退号未再挂号',     time: true,  paged: true },
    { id: 'pay-return',        label: '先后支付退号',     time: true,  paged: false },
    { id: 'dept-wait-time',    label: '科室候诊时长',     time: true,  paged: false },
    { id: 'appt-success',      label: '预约成功挂号量',   time: true,  paged: false },
    { id: 'recheck-appt',      label: '复诊预约量',       time: false, paged: false }
];

const CARD_ICONS = ['blue', 'green', 'purple', 'cyan', 'orange'];

function fmtDate(d) {
    return d.getFullYear() + '-' +
        String(d.getMonth() + 1).padStart(2, '0') + '-' +
        String(d.getDate()).padStart(2, '0');
}

/** 默认按月=上个月(如今天11-17则默认2026-10-01~2026-10-31) */
function lastMonthRange() {
    const end = new Date();
    end.setDate(1);
    end.setDate(0);
    const start = new Date(end.getFullYear(), end.getMonth(), 1);
    return { startDate: fmtDate(start), endDate: fmtDate(end) };
}

/** 默认按天=最近7天(含昨天) */
function last7DaysRange() {
    const end = new Date();
    end.setDate(end.getDate() - 1);
    const start = new Date(end.getTime() - 6 * 86400000);
    return { startDate: fmtDate(start), endDate: fmtDate(end) };
}

function currentTab() {
    return TABS.find(t => t.id === StatDataSummaryApp.filter.tab) || TABS[0];
}

const StatDataSummaryApp = {
    filter: Object.assign({ tab: TABS[0].id, dimension: 'month' }, lastMonthRange()),
    tableState: { currentPage: 1, pageSize: 10, total: 0 },
    monthPicker: null,
    dayPicker: null,
    lastResponse: null,

    init() {
        this.renderTabs();
        this.bindEvents();
        this.syncDimensionButtons();
        this.buildPickers();
        this.load();
    },

    renderTabs() {
        const wrap = document.getElementById('tabFilter');
        wrap.innerHTML = TABS.map(t =>
            `<button class="service-tab${t.id === this.filter.tab ? ' active' : ''}" data-tab="${t.id}">${t.label}</button>`
        ).join('');
        wrap.querySelectorAll('.service-tab').forEach(btn => {
            btn.addEventListener('click', () => this.handleTabChange(btn.dataset.tab));
        });
    },

    bindEvents() {
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', () => this.handleDimensionChange(btn.dataset.value));
        });
        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.tableState.pageSize = parseInt(e.target.value, 10);
            this.tableState.currentPage = 1;
            this.load();
        });
        document.getElementById('exportBtn').addEventListener('click', () => this.exportExcel());
    },

    handleTabChange(tabId) {
        if (this.filter.tab === tabId) return;
        this.filter.tab = tabId;
        this.tableState.currentPage = 1;
        document.querySelectorAll('#tabFilter .service-tab').forEach(b =>
            b.classList.toggle('active', b.dataset.tab === tabId));
        this.buildPickers();
        this.load();
    },

    handleDimensionChange(dimension) {
        if (this.filter.dimension === dimension) return;
        this.filter.dimension = dimension;
        // 切换维度时对齐默认区间: 按月=上个月, 按天=最近7天
        Object.assign(this.filter, dimension === 'month' ? lastMonthRange() : last7DaysRange());
        this.syncDimensionButtons();
        this.buildPickers();
        this.load();
    },

    syncDimensionButtons() {
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn =>
            btn.classList.toggle('active', btn.dataset.value === this.filter.dimension));
    },

    /** 按当前页签/维度重建时间控件; 无时间页签整体隐藏 */
    buildPickers() {
        const tab = currentTab();
        document.getElementById('timeGroup').style.display = tab.time ? '' : 'none';
        if (!tab.time) return;

        const monthMode = this.filter.dimension === 'month';
        document.getElementById('monthRangePicker').style.display = monthMode ? '' : 'none';
        document.getElementById('dayRangeWrapper').style.display = monthMode ? 'none' : '';

        if (this.monthPicker) { this.monthPicker.destroy(); this.monthPicker = null; }
        if (this.dayPicker) { this.dayPicker.destroy(); this.dayPicker = null; }

        if (monthMode) {
            this.monthPicker = new MonthRangePicker(document.getElementById('monthRangePicker'), {
                start: this.filter.startDate.slice(0, 7),
                end: this.filter.endDate.slice(0, 7),
                onConfirm: (start, end) => {
                    const [sy, sm] = start.split('-').map(Number);
                    const [ey, em] = end.split('-').map(Number);
                    this.filter.startDate = fmtDate(new Date(sy, sm - 1, 1));
                    this.filter.endDate = fmtDate(new Date(ey, em, 0));
                    this.tableState.currentPage = 1;
                    this.load();
                }
            });
        } else {
            this.dayPicker = flatpickr('#dateRange', {
                mode: 'range',
                dateFormat: 'Y-m-d',
                locale: 'zh',
                defaultDate: [this.filter.startDate, this.filter.endDate],
                onChange: (selectedDates) => {
                    if (selectedDates.length === 2) {
                        this.filter.startDate = fmtDate(selectedDates[0]);
                        this.filter.endDate = fmtDate(selectedDates[1]);
                        this.tableState.currentPage = 1;
                        this.load();
                    }
                }
            });
        }
    },

    async load() {
        const tab = currentTab();
        const body = { tab: this.filter.tab };
        if (tab.time) {
            body.dimension = this.filter.dimension;
            body.startDate = this.filter.startDate;
            body.endDate = this.filter.endDate;
        }
        if (tab.paged) {
            body.page = this.tableState.currentPage;
            body.pageSize = this.tableState.pageSize;
        }
        try {
            const data = await apiRequest('reports.stat.data-summary', 'endpoint', body);
            this.lastResponse = data || {};
            this.tableState.total = this.lastResponse.total || 0;
            this.renderCards(this.lastResponse.cards);
            this.renderTable(this.lastResponse.columns, this.lastResponse.list);
            this.renderPager(this.lastResponse.paged);
        } catch (e) {
            console.error('Load stat data failed:', e);
        }
    },

    renderCards(cards) {
        const section = document.getElementById('cardsSection');
        if (!cards || cards.length === 0) {
            section.style.display = 'none';
            section.innerHTML = '';
            return;
        }
        section.style.display = '';
        section.innerHTML = cards.map((c, i) => {
            const icon = CARD_ICONS[i % CARD_ICONS.length];
            return `<div class="col-6 col-md-3">
                <div class="card stat-card">
                    <div class="card-body d-flex align-items-center">
                        <div class="stat-icon ${icon}"><i class="bi bi-bar-chart-fill"></i></div>
                        <div class="ms-3">
                            <div class="stat-value text-${icon}">${c.value == null ? '0' : c.value}${c.unit ? `<span class="stat-unit"> ${c.unit}</span>` : ''}</div>
                            <div class="stat-label">${c.label}</div>
                        </div>
                    </div>
                </div>
            </div>`;
        }).join('');
    },

    renderTable(columns, list) {
        const head = document.getElementById('statTableHead');
        const body = document.getElementById('statTableBody');
        columns = columns || [];
        list = list || [];
        head.innerHTML = columns.length
            ? '<tr>' + columns.map(c => `<th>${c.label}</th>`).join('') + '</tr>'
            : '';
        if (!columns.length || !list.length) {
            body.innerHTML = `<tr><td colspan="${Math.max(columns.length, 1)}" class="text-center text-secondary py-4">暂无数据</td></tr>`;
            return;
        }
        body.innerHTML = list.map(row =>
            '<tr>' + columns.map(c => `<td>${row[c.key] == null ? '' : row[c.key]}</td>`).join('') + '</tr>'
        ).join('');
    },

    renderPager(paged) {
        const wrapper = document.getElementById('pagerWrapper');
        if (!paged) {
            wrapper.style.display = 'none';
            return;
        }
        wrapper.style.display = '';
        const { currentPage, pageSize, total } = this.tableState;
        const totalPages = Math.max(1, Math.ceil(total / pageSize));
        document.getElementById('pageInfo').textContent =
            `${pageSize}条/页 共${total}条 第${currentPage}/${totalPages}页`;

        const pages = [];
        const from = Math.max(1, Math.min(currentPage - 2, totalPages - 4));
        for (let p = from; p <= Math.min(totalPages, from + 4); p++) pages.push(p);
        const li = (p, label, disabled, active) =>
            `<li class="page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}">
                <a class="page-link" href="javascript:void(0)" data-page="${p}">${label}</a>
            </li>`;
        let html = li(currentPage - 1, '上一页', currentPage <= 1, false);
        if (from > 1) html += li(1, '1', false, false) + (from > 2 ? '<li class="page-item disabled"><span class="page-link">…</span></li>' : '');
        pages.forEach(p => { html += li(p, p, false, p === currentPage); });
        if (pages[pages.length - 1] < totalPages) {
            const last = pages[pages.length - 1];
            html += (last < totalPages - 1 ? '<li class="page-item disabled"><span class="page-link">…</span></li>' : '') + li(totalPages, totalPages, false, false);
        }
        html += li(currentPage + 1, '下一页', currentPage >= totalPages, false);
        const pager = document.getElementById('pagination');
        pager.innerHTML = html;
        pager.querySelectorAll('a[data-page]').forEach(a => {
            a.addEventListener('click', () => {
                const p = parseInt(a.dataset.page, 10);
                if (p >= 1 && p <= totalPages && p !== currentPage) {
                    this.tableState.currentPage = p;
                    this.load();
                }
            });
        });
    },

    exportExcel() {
        const resp = this.lastResponse;
        if (!resp || !resp.columns || !resp.columns.length) {
            alert('暂无可导出的数据');
            return;
        }
        const rows = (resp.list || []).map(row => resp.columns.map(c => row[c.key] == null ? '' : row[c.key]));
        const ws = XLSX.utils.aoa_to_sheet([resp.columns.map(c => c.label)].concat(rows));
        ws['!cols'] = resp.columns.map(() => ({ wch: 18 }));
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, currentTab().label);
        XLSX.writeFile(wb, `运营数据统计-${currentTab().label}-${this.filter.startDate || ''}~${this.filter.endDate || ''}.xlsx`);
    }
};

StatDataSummaryApp.init();
