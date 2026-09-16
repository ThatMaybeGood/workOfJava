/**
 * 出院结算人次统计
 * 三个统计维度(按费别/按操作员/按支付类别) × 两种时间粒度(按天/按月)
 */

const DIMENSIONS = {
    summary: { title: '出院结算人次（按费别）' },
    operator: { title: '出院结算人次（按操作员）' },
    payType: { title: '出院结算人次（按支付类别）' }
};

const TABLE_COLUMNS = {
    summary: [
        { key: 'itemDate', label: '日期' },
        { key: 'feeType', label: '费别' },
        { key: 'settleChannel', label: '结算类别' },
        { key: 'cnt', label: '人次' }
    ],
    operator: [
        { key: 'itemDate', label: '日期' },
        { key: 'feeType', label: '费别' },
        { key: 'settleChannel', label: '结算类别' },
        { key: 'operatorNo', label: '操作员工号' },
        { key: 'operatorName', label: '操作员姓名' },
        { key: 'cnt', label: '人次' }
    ],
    payType: [
        { key: 'itemDate', label: '日期' },
        { key: 'feeType', label: '费别' },
        { key: 'settleChannel', label: '结算类别' },
        { key: 'payType', label: '支付类别' },
        { key: 'cnt', label: '人次' }
    ]
};

class PersonCountApp {
    constructor() {
        this.state = {
            dimension: 'summary',
            timeDimension: 'day',
            startDate: null,
            endDate: null,
            page: 1,
            pageSize: 10,
            allList: [],
            feeTypeFilter: '',
            channelFilter: ''
        };
        this.datePicker = null;
        this.bindEvents();
        this.initDatePicker();
        this.load();
    }

    bindEvents() {
        document.querySelectorAll('#dimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#dimensionFilter .filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.state.dimension = btn.dataset.value;
                this.state.page = 1;
                this.load();
            });
        });
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.state.timeDimension = btn.dataset.value;
                this.resetDatePicker();
                this.state.page = 1;
                this.load();
            });
        });
        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value, 10);
            this.state.page = 1;
            this.renderBody();
        });
        document.getElementById('jumpBtn').addEventListener('click', () => this.jumpToPage());
        document.getElementById('jumpPage').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this.jumpToPage();
        });
        // 费别/结算类别列筛选：本地过滤当前结果集，不重新查询
        document.getElementById('feeTypeFilter').addEventListener('change', (e) => {
            this.state.feeTypeFilter = e.target.value;
            this.state.page = 1;
            this.renderBody();
        });
        document.getElementById('channelFilter').addEventListener('change', (e) => {
            this.state.channelFilter = e.target.value;
            this.state.page = 1;
            this.renderBody();
        });
    }

    jumpToPage() {
        const input = document.getElementById('jumpPage');
        const page = parseInt(input.value, 10);
        const pages = Math.max(1, Math.ceil(this.filteredList().length / this.state.pageSize));
        if (page >= 1 && page <= pages) {
            this.state.page = page;
            this.renderBody();
        }
        input.value = '';
    }

    /** 费别/结算类别筛选后的结果集 */
    filteredList() {
        return this.state.allList.filter(row =>
            (!this.state.feeTypeFilter || row.feeType === this.state.feeTypeFilter)
            && (!this.state.channelFilter || row.settleChannel === this.state.channelFilter));
    }

    /** 按天/按月切换时重建日期控件：按月显示 yyyy/MM，且起止对齐到月初/月末 */
    resetDatePicker() {
        const monthMode = this.state.timeDimension === 'month';
        this.datePicker.destroy();
        this.datePicker = flatpickr(document.getElementById('dateRange'), {
            mode: 'range',
            dateFormat: monthMode ? 'Y/m' : 'Y/m/d',
            defaultDate: [this.state.startDate.replace(/-/g, '/'), this.state.endDate.replace(/-/g, '/')],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    let start = selectedDates[0];
                    let end = selectedDates[1];
                    if (monthMode) {
                        start = new Date(start.getFullYear(), start.getMonth(), 1);
                        end = new Date(end.getFullYear(), end.getMonth() + 1, 0);
                    }
                    this.state.startDate = this.formatDate(start);
                    this.state.endDate = this.formatDate(end);
                    this.state.page = 1;
                    this.load();
                }
            }
        });
        // 已选范围对齐到粒度边界
        if (monthMode) {
            const s = new Date(this.state.startDate + 'T00:00:00');
            const e = new Date(this.state.endDate + 'T00:00:00');
            this.state.startDate = this.formatDate(new Date(s.getFullYear(), s.getMonth(), 1));
            this.state.endDate = this.formatDate(new Date(e.getFullYear(), e.getMonth() + 1, 0));
        }
    }

    initDatePicker() {
        const end = new Date();
        const start = new Date(end.getTime() - 6 * 86400000);
        this.state.startDate = this.formatDate(start);
        this.state.endDate = this.formatDate(end);
        this.datePicker = flatpickr(document.getElementById('dateRange'), {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: [this.formatDate(start, '/'), this.formatDate(end, '/')],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    this.state.startDate = this.formatDate(selectedDates[0]);
                    this.state.endDate = this.formatDate(selectedDates[1]);
                    this.load();
                }
            }
        });
    }

    async load() {
        document.getElementById('tableTitle').textContent = DIMENSIONS[this.state.dimension].title;
        this.renderHead();
        try {
            const body = await ReportAPI.getDischSettlePersonCount({
                dimension: this.state.dimension,
                timeDimension: this.state.timeDimension,
                startDate: this.state.startDate,
                endDate: this.state.endDate
            });
            this.state.allList = body.list || [];
            this.renderBody();
        } catch (error) {
            console.error('Load person count failed:', error);
            document.getElementById('tableTitle').textContent += '（加载失败，请强刷页面 Ctrl+F5）';
            this.state.allList = [];
            this.renderBody();
        }
    }

    renderHead() {
        const columns = TABLE_COLUMNS[this.state.dimension];
        document.getElementById('tableHead').innerHTML =
            '<tr>' + columns.map(c => `<th>${c.label}</th>`).join('') + '</tr>';
    }

    renderBody() {
        const columns = TABLE_COLUMNS[this.state.dimension];
        const tbody = document.getElementById('tableBody');
        const all = this.filteredList();
        const start = (this.state.page - 1) * this.state.pageSize;
        const list = all.slice(start, start + this.state.pageSize);
        document.getElementById('tableEmpty').classList.toggle('d-none', all.length > 0);
        let html = list.map(row =>
            '<tr>' + columns.map(c => `<td>${row[c.key] != null ? row[c.key] : ''}</td>`).join('') + '</tr>'
        ).join('');
        // 合计行：本页合计(当前页行求和) + 总计(筛选结果集求和)
        const pageCnt = list.reduce((sum, row) => sum + (Number(row.cnt) || 0), 0);
        const totalCnt = all.reduce((sum, row) => sum + (Number(row.cnt) || 0), 0);
        const totalRow = (label, value, cls) =>
            `<tr class="${cls} fw-bold">` + columns.map(c => {
                if (c.key === 'cnt') return `<td>${value}</td>`;
                if (c.key === 'itemDate') return `<td>${label}</td>`;
                return '<td></td>';
            }).join('') + '</tr>';
        html += totalRow('本页合计', pageCnt, 'table-light');
        html += totalRow('总计', totalCnt, 'table-secondary');
        tbody.innerHTML = html;
        this.renderPagination();
    }

    renderPagination() {
        const total = this.filteredList().length;
        const pages = Math.max(1, Math.ceil(total / this.state.pageSize));
        if (this.state.page > pages) this.state.page = pages;
        document.getElementById('pageInfo').textContent =
            `${this.state.pageSize}条/页 共${total}条`;
        const pager = document.getElementById('pagination');
        pager.innerHTML = '';
        const mk = (label, page, disabled, active) => {
            const li = document.createElement('li');
            li.className = `page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
            if (!disabled && !active) {
                li.addEventListener('click', (e) => {
                    e.preventDefault();
                    this.state.page = page;
                    this.renderBody();
                });
            }
            return li;
        };
        pager.appendChild(mk('上一页', this.state.page - 1, this.state.page <= 1, false));
        for (let p = 1; p <= pages; p++) {
            pager.appendChild(mk(p, p, false, p === this.state.page));
        }
        pager.appendChild(mk('下一页', this.state.page + 1, this.state.page >= pages, false));
    }

    formatDate(date, sep = '-') {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return sep === '/' ? `${y}/${m}/${d}` : `${y}-${m}-${d}`;
    }
}

const personCountApp = new PersonCountApp();
