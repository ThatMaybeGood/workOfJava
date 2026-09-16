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

// 各维度需要纵向合并的列（按费别：日期+费别；其余：日期+费别+结算类别）
const MERGE_KEYS = {
    summary: ['itemDate', 'feeType'],
    operator: ['itemDate', 'feeType', 'settleChannel'],
    payType: ['itemDate', 'feeType', 'settleChannel']
};

const DIMENSION_NAMES = { summary: '按费别', operator: '按操作员', payType: '按支付类别' };

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
        this.drillPage = 1;
        this.drillPageSize = 20;
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
        // 按费别视图点人次钻取按操作员明细
        document.getElementById('tableBody').addEventListener('click', (e) => {
            if (this.state.dimension !== 'summary') return;
            const td = e.target.closest('td.drill-cnt');
            if (!td) return;
            const tr = td.closest('tr');
            const idx = Array.from(tr.parentNode.children).indexOf(tr);
            const all = this.filteredList();
            const list = all.slice((this.state.page - 1) * this.state.pageSize, (this.state.page - 1) * this.state.pageSize + this.state.pageSize);
            if (idx < list.length) this.drill(list[idx]);
        });
        document.getElementById('exportBtn').addEventListener('click', () => this.exportMain());
        document.getElementById('drillExportBtn').addEventListener('click', () => this.exportDrill());
        document.getElementById('drillPageSizeSelect').addEventListener('change', (e) => {
            this.drillPageSize = parseInt(e.target.value, 10);
            this.drillPage = 1;
            this.renderDrill();
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
        const mergeKeys = MERGE_KEYS[this.state.dimension];
        const tbody = document.getElementById('tableBody');
        const all = this.filteredList();
        const start = (this.state.page - 1) * this.state.pageSize;
        const list = all.slice(start, start + this.state.pageSize);
        document.getElementById('tableEmpty').classList.toggle('d-none', all.length > 0);
        const clickable = this.state.dimension === 'summary' ? 'cnt' : null;
        let html = this.buildRowsHtml(list, columns, mergeKeys, clickable);
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

    /** 纵向合并：相邻行 mergeKeys 的值全相同则合并，返回每列的 rowspan（被合并行记 0） */
    calcSpans(list, mergeKeys) {
        const spans = list.map(() => ({}));
        let i = 0;
        while (i < list.length) {
            let j = i;
            while (j + 1 < list.length && mergeKeys.every(k => list[j + 1][k] === list[i][k])) j++;
            mergeKeys.forEach(k => spans[i][k] = j - i + 1);
            for (let r = i + 1; r <= j; r++) mergeKeys.forEach(k => spans[r][k] = 0);
            i = j + 1;
        }
        return spans;
    }

    /** 构建 tbody 行；clickableKey 列渲染为可点击（人次钻取） */
    buildRowsHtml(list, columns, mergeKeys, clickableKey) {
        const spans = this.calcSpans(list, mergeKeys);
        return list.map((row, i) => {
            const tds = columns.map(c => {
                if (spans[i][c.key] === 0) return '';
                const rs = spans[i][c.key] > 1 ? ` rowspan="${spans[i][c.key]}"` : '';
                if (c.key === clickableKey) {
                    return `<td${rs} class="text-primary drill-cnt" style="cursor:pointer">${row.cnt != null ? row.cnt : ''}</td>`;
                }
                return `<td${rs}>${row[c.key] != null ? row[c.key] : ''}</td>`;
            }).join('');
            return `<tr>${tds}</tr>`;
        }).join('');
    }

    /** 人次钻取：按操作员维度查该日期+费别+结算类别，弹窗展示（带分页） */
    async drill(row) {
        this.drillContext = { itemDate: row.itemDate, feeType: row.feeType, settleChannel: row.settleChannel };
        this.drillList = [];
        this.drillPage = 1;
        document.getElementById('drillModalTitle').textContent =
            `人次明细（按操作员）：${row.itemDate} ${row.feeType} ${row.settleChannel}`;
        document.getElementById('drillTableBody').innerHTML = '';
        document.getElementById('drillEmpty').classList.remove('d-none');
        bootstrap.Modal.getOrCreateInstance(document.getElementById('drillModal')).show();
        try {
            // 按月时取整月范围
            let start = row.itemDate, end = row.itemDate;
            if (this.state.timeDimension === 'month') {
                start = row.itemDate + '-01';
                const [y, m] = row.itemDate.split('-').map(Number);
                end = this.formatDate(new Date(y, m, 0));
            }
            const body = await ReportAPI.getDischSettlePersonCount({
                dimension: 'operator',
                timeDimension: this.state.timeDimension,
                startDate: start,
                endDate: end
            });
            this.drillList = (body.list || []).filter(r => r.feeType === row.feeType && r.settleChannel === row.settleChannel);
            this.renderDrill();
        } catch (error) {
            console.error('Drill failed:', error);
        }
    }

    renderDrill() {
        const all = this.drillList;
        document.getElementById('drillEmpty').classList.toggle('d-none', all.length > 0);
        const columns = TABLE_COLUMNS.operator;
        const pages = Math.max(1, Math.ceil(all.length / this.drillPageSize));
        if (this.drillPage > pages) this.drillPage = pages;
        const list = all.slice((this.drillPage - 1) * this.drillPageSize, this.drillPage * this.drillPageSize);
        let html = this.buildRowsHtml(list, columns, MERGE_KEYS.operator, null);
        const total = all.reduce((sum, row) => sum + (Number(row.cnt) || 0), 0);
        html += `<tr class="table-secondary fw-bold">` + columns.map(c =>
            c.key === 'cnt' ? `<td>${total}</td>` : c.key === 'itemDate' ? '<td>总计</td>' : '<td></td>'
        ).join('') + '</tr>';
        document.getElementById('drillTableBody').innerHTML = html;
        this.renderDrillPagination(pages, all.length);
    }

    renderDrillPagination(pages, total) {
        document.getElementById('drillPageInfo').textContent = `${this.drillPageSize}条/页 共${total}条`;
        const pager = document.getElementById('drillPagination');
        pager.innerHTML = '';
        const mk = (label, page, disabled, active) => {
            const li = document.createElement('li');
            li.className = `page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
            if (!disabled && !active) {
                li.addEventListener('click', (e) => {
                    e.preventDefault();
                    this.drillPage = page;
                    this.renderDrill();
                });
            }
            return li;
        };
        pager.appendChild(mk('上一页', this.drillPage - 1, this.drillPage <= 1, false));
        for (let p = 1; p <= pages; p++) {
            pager.appendChild(mk(p, p, false, p === this.drillPage));
        }
        pager.appendChild(mk('下一页', this.drillPage + 1, this.drillPage >= pages, false));
    }

    /** 计算合并区间（SheetJS !merges 格式，行号含表头偏移 1） */
    calcMerges(list, mergeKeys, columns) {
        const merges = [];
        let i = 0;
        while (i < list.length) {
            let j = i;
            while (j + 1 < list.length && mergeKeys.every(k => list[j + 1][k] === list[i][k])) j++;
            if (j > i) {
                mergeKeys.forEach(k => {
                    const c = columns.findIndex(col => col.key === k);
                    merges.push({ s: { r: i + 1, c }, e: { r: j + 1, c } });
                });
            }
            i = j + 1;
        }
        return merges;
    }

    /** 导出 Excel：表头加粗+底色，全表细边框；mergeKeys 传值时按界面样式合并单元格 */
    exportData(list, columns, fileName, mergeKeys) {
        const headers = columns.map(c => c.label);
        const rows = list.map(row => columns.map(c => row[c.key] != null ? row[c.key] : ''));
        let merges = [];
        if (mergeKeys) {
            merges = this.calcMerges(list, mergeKeys, columns);
            // 被合并的单元格置空，只保留左上角值
            merges.forEach(m => {
                for (let r = m.s.r; r <= m.e.r; r++) {
                    for (let c = m.s.c; c <= m.e.c; c++) {
                        if (r !== m.s.r || c !== m.s.c) rows[r - 1][c] = '';
                    }
                }
            });
        }
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
        ws['!cols'] = headers.map(() => ({ wch: 14 }));
        if (merges.length) ws['!merges'] = merges;
        const border = {
            top: { style: 'thin', color: { rgb: 'D9D9D9' } },
            bottom: { style: 'thin', color: { rgb: 'D9D9D9' } },
            left: { style: 'thin', color: { rgb: 'D9D9D9' } },
            right: { style: 'thin', color: { rgb: 'D9D9D9' } }
        };
        const range = XLSX.utils.decode_range(ws['!ref']);
        for (let C = range.s.c; C <= range.e.c; ++C) {
            const addr = XLSX.utils.encode_cell({ r: 0, c: C });
            if (!ws[addr]) ws[addr] = {};
            ws[addr].s = {
                font: { bold: true, sz: 11 },
                fill: { fgColor: { rgb: 'E6F7FF' } },
                alignment: { horizontal: 'center', vertical: 'center' },
                border
            };
        }
        for (let R = 1; R <= range.e.r; ++R) {
            for (let C = range.s.c; C <= range.e.c; ++C) {
                const addr = XLSX.utils.encode_cell({ r: R, c: C });
                if (!ws[addr]) ws[addr] = {};
                if (!ws[addr].s) ws[addr].s = {};
                ws[addr].s.border = border;
                ws[addr].s.alignment = { horizontal: 'center', vertical: 'center' };
            }
        }
        XLSX.utils.book_append_sheet(wb, ws, '出院结算人次');
        XLSX.writeFile(wb, fileName);
    }

    /** 导出全部筛选结果（不只当前页），文件名：日期范围_维度_筛选类型 */
    exportMain() {
        const parts = [
            `${this.state.startDate}~${this.state.endDate}`,
            DIMENSION_NAMES[this.state.dimension]
        ];
        if (this.state.feeTypeFilter) parts.push(`费别-${this.state.feeTypeFilter}`);
        if (this.state.channelFilter) parts.push(`结算类别-${this.state.channelFilter}`);
        this.exportData(this.filteredList(), TABLE_COLUMNS[this.state.dimension],
            parts.join('_') + '.xlsx', MERGE_KEYS[this.state.dimension]);
    }

    exportDrill() {
        if (!this.drillContext) return;
        const { itemDate, feeType, settleChannel } = this.drillContext;
        this.exportData(this.drillList || [], TABLE_COLUMNS.operator,
            `${itemDate}_按操作员_费别-${feeType}_结算类别-${settleChannel}.xlsx`, MERGE_KEYS.operator);
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
