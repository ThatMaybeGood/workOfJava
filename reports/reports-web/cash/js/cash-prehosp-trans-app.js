/**
 * 院前转住院统计
 * 日期×操作员 行(收据张数/人次)，只统计维护的有效操作员
 * 操作员维护在弹窗内完成(搜索添加/有效开关/删除)，维护后刷新下拉
 */

const TABLE_COLUMNS = [
    { key: 'itemDate', label: '日期' },
    { key: 'operNo', label: '操作员工号' },
    { key: 'operName', label: '操作员姓名' },
    { key: 'rcptCnt', label: '收据张数' },
    { key: 'personCnt', label: '人次' }
];

// 日期列纵向合并(后端按 日期,操作员 排序, 相邻行日期相同则合并)
const MERGE_KEYS = ['itemDate'];

class PreHospTransApp {
    constructor() {
        this.state = {
            timeDimension: 'day',
            startDate: null,
            endDate: null,
            operatorNo: '',
            page: 1,
            pageSize: 10,
            allList: []
        };
        this.datePicker = null;
        this.maintList = [];
        this.maintPage = 1;
        this.maintPageSize = 10;
        this.delTarget = null;
        this.bindEvents();
        this.initDatePicker();
        this.refreshOperatorOptions();
        this.load();
    }

    bindEvents() {
        document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#timeDimensionFilter .filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.state.timeDimension = btn.dataset.value;
                this.state.page = 1;
                this.resetDatePicker();
                this.load();
            });
        });
        document.getElementById('operatorFilter').addEventListener('change', (e) => {
            this.state.operatorNo = e.target.value;
            this.state.page = 1;
            this.load();
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
        document.getElementById('exportBtn').addEventListener('click', () => this.exportMain());
        // 操作员维护弹窗
        document.getElementById('maintBtn').addEventListener('click', () => this.openMaintModal());
        document.getElementById('addNewBtn').addEventListener('click', () => this.startAddRow());
        document.getElementById('maintKeyword').addEventListener('input', (e) => {
            this.maintKeyword = e.target.value.trim();
            this.maintPage = 1;
            this.renderMaintList();
        });
        document.getElementById('maintPageSizeSelect').addEventListener('change', (e) => {
            this.maintPageSize = parseInt(e.target.value, 10);
            this.maintPage = 1;
            this.renderMaintList();
        });
        document.getElementById('delConfirmBtn').addEventListener('click', () => this.confirmDelete());
        const maintBody = document.getElementById('maintTableBody');
        maintBody.addEventListener('change', (e) => {
            const input = e.target.closest('input.valid-toggle');
            if (!input) return;
            this.setValid(Number(input.dataset.id), input.checked ? 1 : 0, input);
        });
        maintBody.addEventListener('click', (e) => {
            const saveBtn = e.target.closest('button.save-new-btn');
            if (saveBtn) {
                this.saveNewRow();
                return;
            }
            if (e.target.closest('button.cancel-new-btn')) {
                this.addingNew = false;
                this.renderMaintList();
                return;
            }
            const btn = e.target.closest('button.del-btn');
            if (!btn) return;
            const row = this.maintList.find(r => r.id === Number(btn.dataset.id));
            if (!row) return;
            this.delTarget = row;
            document.getElementById('delModalText').textContent =
                `确定删除操作员「${row.operName || row.dbuser || row.operNo}」？`;
            bootstrap.Modal.getOrCreateInstance(document.getElementById('delModal')).show();
        });
    }

    jumpToPage() {
        const input = document.getElementById('jumpPage');
        const page = parseInt(input.value, 10);
        const pages = Math.max(1, Math.ceil(this.state.allList.length / this.state.pageSize));
        if (page >= 1 && page <= pages) {
            this.state.page = page;
            this.renderBody();
        }
        input.value = '';
    }

    /** 操作员下拉：重建选项(全部 + 已维护有效操作员)，保持当前选中 */
    async refreshOperatorOptions() {
        const select = document.getElementById('operatorFilter');
        try {
            const body = await ReportAPI.preHospOper({ action: 'query' });
            const selected = this.state.operatorNo;
            select.innerHTML = '<option value="">全部</option>';
            (body.list || []).filter(o => o.validFlag === 1).forEach(o => {
                const opt = document.createElement('option');
                opt.value = o.operNo;
                opt.textContent = o.operName || o.dbuser || o.operNo;
                select.appendChild(opt);
            });
            select.value = selected;
            if (select.value !== selected) {
                this.state.operatorNo = '';
                select.value = '';
                this.state.page = 1;
                this.load();
            }
        } catch (error) {
            console.error('Load operators failed:', error);
        }
    }

    /** 按天/按月切换时重建日期控件 */
    resetDatePicker() {
        const monthMode = this.state.timeDimension === 'month';
        const dateRangeInput = document.getElementById('dateRange');
        const monthWrap = document.getElementById('monthRangePicker');
        if (this.datePicker) {
            this.datePicker.destroy();
            this.datePicker = null;
        }
        if (this.monthPicker) {
            this.monthPicker.destroy();
            this.monthPicker = null;
        }

        if (monthMode) {
            dateRangeInput.parentElement.style.display = 'none';
            monthWrap.style.display = '';
            const s = new Date(this.state.startDate + 'T00:00:00');
            const e = new Date(this.state.endDate + 'T00:00:00');
            this.state.startDate = this.formatDate(new Date(s.getFullYear(), s.getMonth(), 1));
            this.state.endDate = this.formatDate(new Date(e.getFullYear(), e.getMonth() + 1, 0));
            this.monthPicker = new MonthRangePicker(monthWrap, {
                start: this.state.startDate.slice(0, 7),
                end: this.state.endDate.slice(0, 7),
                onConfirm: (start, end) => {
                    const [sy, sm] = start.split('-').map(Number);
                    const [ey, em] = end.split('-').map(Number);
                    this.state.startDate = this.formatDate(new Date(sy, sm - 1, 1));
                    this.state.endDate = this.formatDate(new Date(ey, em, 0));
                    this.state.page = 1;
                    this.load();
                }
            });
            return;
        }

        monthWrap.style.display = 'none';
        dateRangeInput.parentElement.style.display = '';
        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: [this.state.startDate.replace(/-/g, '/'), this.state.endDate.replace(/-/g, '/')],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    this.state.startDate = this.formatDate(selectedDates[0]);
                    this.state.endDate = this.formatDate(selectedDates[1]);
                    this.state.page = 1;
                    this.load();
                }
            }
        });
    }

    initDatePicker() {
        const end = new Date();
        const start = new Date(end.getTime());
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
                    this.state.page = 1;
                    this.load();
                }
            }
        });
    }

    async load() {
        this.renderHead();
        try {
            const body = await ReportAPI.getPreHospTrans({
                timeDimension: this.state.timeDimension,
                startDate: this.state.startDate,
                endDate: this.state.endDate,
                operatorNo: this.state.operatorNo
            });
            this.state.allList = body.list || [];
            this.renderBody();
        } catch (error) {
            console.error('Load pre hosp trans failed:', error);
            document.getElementById('tableTitle').textContent = '院前转住院统计（加载失败，请强刷页面 Ctrl+F5）';
            this.state.allList = [];
            this.renderBody();
        }
    }

    renderHead() {
        document.getElementById('tableHead').innerHTML =
            '<tr>' + TABLE_COLUMNS.map(c => `<th>${c.label}</th>`).join('') + '</tr>';
    }

    renderBody() {
        const tbody = document.getElementById('tableBody');
        const all = this.state.allList;
        const start = (this.state.page - 1) * this.state.pageSize;
        const list = all.slice(start, start + this.state.pageSize);
        document.getElementById('tableEmpty').classList.toggle('d-none', all.length > 0);
        let html = this.buildRowsHtml(list);
        // 合计行：本页合计 + 总计，收据张数/人次各自求和
        const sumKeys = ['rcptCnt', 'personCnt'];
        const totalRow = (label, sums, cls) =>
            `<tr class="${cls} fw-bold">` + TABLE_COLUMNS.map(c => {
                if (sums[c.key] !== undefined) return `<td>${sums[c.key]}</td>`;
                if (c.key === 'itemDate') return `<td>${label}</td>`;
                return '<td></td>';
            }).join('') + '</tr>';
        html += totalRow('本页合计', sumKeys.reduce((m, k) =>
            (m[k] = list.reduce((s, r) => s + (Number(r[k]) || 0), 0), m), {}), 'table-light');
        html += totalRow('总计', sumKeys.reduce((m, k) =>
            (m[k] = all.reduce((s, r) => s + (Number(r[k]) || 0), 0), m), {}), 'table-secondary');
        tbody.innerHTML = html;
        this.renderPagination();
    }

    /** 纵向合并：相邻行 mergeKeys 的值全相同则合并，返回每列的 rowspan（被合并行记 0） */
    calcSpans(list) {
        const spans = list.map(() => ({}));
        let i = 0;
        while (i < list.length) {
            let j = i;
            while (j + 1 < list.length && MERGE_KEYS.every(k => list[j + 1][k] === list[i][k])) j++;
            MERGE_KEYS.forEach(k => spans[i][k] = j - i + 1);
            for (let r = i + 1; r <= j; r++) MERGE_KEYS.forEach(k => spans[r][k] = 0);
            i = j + 1;
        }
        return spans;
    }

    buildRowsHtml(list) {
        const spans = this.calcSpans(list);
        return list.map((row, i) => {
            const tds = TABLE_COLUMNS.map(c => {
                if (spans[i][c.key] === 0) return '';
                const rs = spans[i][c.key] > 1 ? ` rowspan="${spans[i][c.key]}"` : '';
                return `<td${rs}>${row[c.key] != null ? row[c.key] : ''}</td>`;
            }).join('');
            return `<tr>${tds}</tr>`;
        }).join('');
    }

    // ==================== 操作员维护弹窗 ====================

    async openMaintModal() {
        this.addingNew = false;
        this.maintKeyword = '';
        this.maintPage = 1;
        document.getElementById('maintKeyword').value = '';
        document.getElementById('maintPageSizeSelect').value = String(this.maintPageSize);
        await this.loadMaintList();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('maintModal')).show();
    }

    async loadMaintList() {
        try {
            const body = await ReportAPI.preHospOper({ action: 'query' });
            this.maintList = body.list || [];
            this.renderMaintList();
        } catch (error) {
            console.error('Load maint list failed:', error);
            this.maintList = [];
            this.renderMaintList();
        }
    }

    /** 已维护列表(姓名模糊筛选+本地分页) + 顶部新增编辑行 */
    renderMaintList() {
        const kw = this.maintKeyword || '';
        const filtered = kw ? this.maintList.filter(row =>
            (row.operName || '').indexOf(kw) >= 0) : this.maintList;
        // 分页: 在筛选结果上本地分页, 新增编辑行固定在首行不占分页
        const pages = Math.max(1, Math.ceil(filtered.length / this.maintPageSize));
        if (this.maintPage > pages) this.maintPage = pages;
        const startIdx = (this.maintPage - 1) * this.maintPageSize;
        const pageRows = filtered.slice(startIdx, startIdx + this.maintPageSize);
        document.getElementById('maintEmpty').classList.toggle('d-none', filtered.length > 0);
        document.getElementById('maintPagerWrap').classList.toggle('d-none', filtered.length === 0);
        let html = '';
        if (this.addingNew) {
            html += `
            <tr>
                <td><input type="text" class="form-control form-control-sm" id="newOperName" placeholder="操作员姓名"></td>
                <td><input type="text" class="form-control form-control-sm" id="newDbuser" placeholder="登录ID"></td>
                <td>-</td>
                <td>
                    <button class="btn btn-primary btn-sm save-new-btn"><i class="bi bi-check-lg me-1"></i>保存</button>
                    <button class="btn btn-outline-secondary btn-sm cancel-new-btn">取消</button>
                </td>
            </tr>`;
        }
        html += pageRows.map(row => `
            <tr>
                <td>${row.operName != null ? row.operName : ''}</td>
                <td>${row.dbuser != null ? row.dbuser : ''}</td>
                <td>
                    <div class="form-check form-switch mb-0">
                        <input type="checkbox" class="form-check-input valid-toggle" data-id="${row.id}"
                               ${row.validFlag === 1 ? 'checked' : ''}>
                    </div>
                </td>
                <td>
                    <button class="btn btn-outline-danger btn-sm del-btn" data-id="${row.id}">
                        <i class="bi bi-trash me-1"></i>删除
                    </button>
                </td>
            </tr>`).join('');
        document.getElementById('maintTableBody').innerHTML = html;
        document.getElementById('addError').classList.add('d-none');
        document.getElementById('maintPageInfo').textContent =
            `${this.maintPageSize}条/页 共${filtered.length}条`;
        const pager = document.getElementById('maintPagination');
        pager.innerHTML = '';
        const mk = (label, p, disabled, active) => {
            const li = document.createElement('li');
            li.className = `page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
            if (!disabled && !active) {
                li.addEventListener('click', (e) => {
                    e.preventDefault();
                    this.maintPage = p;
                    this.renderMaintList();
                });
            }
            return li;
        };
        pager.appendChild(mk('上一页', this.maintPage - 1, this.maintPage <= 1, false));
        for (let p = 1; p <= pages; p++) {
            pager.appendChild(mk(p, p, false, p === this.maintPage));
        }
        pager.appendChild(mk('下一页', this.maintPage + 1, this.maintPage >= pages, false));
        if (this.addingNew) {
            document.getElementById('newDbuser').addEventListener('keydown', (e) => {
                if (e.key === 'Enter') this.saveNewRow();
            });
        }
    }

    /** 新增：表格顶部插入可编辑行 */
    startAddRow() {
        if (this.addingNew) {
            document.getElementById('newOperName').focus();
            return;
        }
        this.addingNew = true;
        this.renderMaintList();
        document.getElementById('newOperName').focus();
    }

    /** 行内保存：姓名+登录ID提交add(后端按登录ID反查USER_ID), 成功退出编辑并刷新 */
    async saveNewRow() {
        const operName = document.getElementById('newOperName').value.trim();
        const dbuser = document.getElementById('newDbuser').value.trim();
        const err = document.getElementById('addError');
        if (!operName || !dbuser) {
            err.textContent = '姓名和登录ID均必填！';
            err.classList.remove('d-none');
            return;
        }
        try {
            const body = await ReportAPI.preHospOper({ action: 'add', operName, dbuser });
            if (body.affected > 0) {
                this.addingNew = false;
                await this.loadMaintList();
                this.refreshOperatorOptions();
            } else {
                err.textContent = '保存失败，请核对HIS系统中是否存在该登录ID和该操作员！';
                err.classList.remove('d-none');
            }
        } catch (error) {
            console.error('Save new row failed:', error);
        }
    }

    async setValid(id, validFlag, input) {
        try {
            const body = await ReportAPI.preHospOper({ action: 'setValid', id, validFlag });
            if (body.affected > 0) {
                const row = this.maintList.find(r => r.id === id);
                if (row) row.validFlag = validFlag;
                this.refreshOperatorOptions();
            } else {
                input.checked = !input.checked;
            }
        } catch (error) {
            console.error('Set valid failed:', error);
            input.checked = !input.checked;
        }
    }

    async confirmDelete() {
        if (!this.delTarget) return;
        try {
            await ReportAPI.preHospOper({ action: 'delete', id: this.delTarget.id });
            bootstrap.Modal.getOrCreateInstance(document.getElementById('delModal')).hide();
            if (this.delTarget.operNo === this.state.operatorNo) {
                this.state.operatorNo = '';
                this.state.page = 1;
                this.load();
            }
            this.delTarget = null;
            await this.loadMaintList();
            this.refreshOperatorOptions();
        } catch (error) {
            console.error('Delete oper failed:', error);
        }
    }

    // ==================== 导出 ====================

    /** 计算合并区间（SheetJS !merges 格式，行号含表头偏移 1） */
    calcMerges(list) {
        const merges = [];
        let i = 0;
        while (i < list.length) {
            let j = i;
            while (j + 1 < list.length && MERGE_KEYS.every(k => list[j + 1][k] === list[i][k])) j++;
            if (j > i) {
                MERGE_KEYS.forEach(k => {
                    const c = TABLE_COLUMNS.findIndex(col => col.key === k);
                    merges.push({ s: { r: i + 1, c }, e: { r: j + 1, c } });
                });
            }
            i = j + 1;
        }
        return merges;
    }

    /** 导出 Excel：表头加粗+底色，全表细边框，日期列按界面样式合并 */
    exportData(list, fileName) {
        const headers = TABLE_COLUMNS.map(c => c.label);
        const rows = list.map(row => TABLE_COLUMNS.map(c => row[c.key] != null ? row[c.key] : ''));
        const merges = this.calcMerges(list);
        merges.forEach(m => {
            for (let r = m.s.r; r <= m.e.r; r++) {
                for (let c = m.s.c; c <= m.e.c; c++) {
                    if (r !== m.s.r || c !== m.s.c) rows[r - 1][c] = '';
                }
            }
        });
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
        XLSX.utils.book_append_sheet(wb, ws, '院前转住院统计');
        XLSX.writeFile(wb, fileName);
    }

    exportMain() {
        const parts = [`${this.state.startDate}~${this.state.endDate}`, '院前转住院统计'];
        if (this.state.operatorNo) parts.push(`操作员-${this.state.operatorNo}`);
        this.exportData(this.state.allList, parts.join('_') + '.xlsx');
    }

    renderPagination() {
        const total = this.state.allList.length;
        const pages = Math.max(1, Math.ceil(total / this.state.pageSize));
        if (this.state.page > pages) this.state.page = pages;
        document.getElementById('pageInfo').textContent = `${this.state.pageSize}条/页 共${total}条`;
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

const preHospTransApp = new PreHospTransApp();
