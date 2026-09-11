/**
 * 门诊服务质量分析页面主逻辑
 */
class ServiceQualityController {
    constructor() {
        const today = this.formatDate(new Date());
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            activeTab: 'complaint',
            filter: {
                timeRange: 'today',
                startDate: today,
                endDate: today,
                deptName: '',
                deptCode: ''
            }
        };

        this.init();
    }

    formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    async init() {
        this.bindEvents();
        this.initDateRangePicker();
        await this.initDeptSelect();
        this.loadOverview();
        this.loadTableData();
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
                    this.state.filter.startDate = this.formatDate(selectedDates[0]);
                    this.state.filter.endDate = this.formatDate(selectedDates[1]);
                    this.state.currentPage = 1;
                    this.loadTableData();
                }
            }
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
                this.state.filter.deptName = dept.deptName === '全部' ? '' : dept.deptName;
                this.state.filter.deptCode = dept.deptCode === '0000' ? '' : dept.deptCode;
                this.state.currentPage = 1;
                this.loadOverview();
                this.loadTableData();
            },
            ...options
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        document.querySelectorAll('.service-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.handleTabSwitch(e));
        });

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadTableData();
        });
    }

    handleTimeFilter(e) {
        const btn = e.target;
        document.querySelectorAll('#timeFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const range = getDateRangeByTimeRange(btn.dataset.value);
        this.state.filter.timeRange = btn.dataset.value;
        this.state.filter.startDate = range.startDate;
        this.state.filter.endDate = range.endDate;
        if (this.datePicker) {
            this.datePicker.setDate([toFlatpickrDate(range.startDate), toFlatpickrDate(range.endDate)], false);
        }
        this.state.currentPage = 1;
        this.loadOverview();
        this.loadTableData();
    }

    handleTabSwitch(e) {
        const tab = e.target;
        document.querySelectorAll('.service-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        this.state.activeTab = tab.dataset.tab;
        this.state.currentPage = 1;
        this.loadTableData();
        this.updateTableTitle();
    }

    updateTableTitle() {
        const titleEl = document.getElementById('tableTitleText');
        titleEl.textContent = this.state.activeTab === 'complaint' ? '各科室投诉表明细' : '各科室表扬表明细';
    }

    async loadOverview() {
        try {
            const body = await ReportAPI.getServiceQualityStats({
                timeRange: this.state.filter.timeRange,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate,
                deptName: this.state.filter.deptName,
                deptCode: this.state.filter.deptCode
            });
            this.renderOverview(body ? body.overview : null);
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        document.getElementById('complaintCount').textContent = safe(data && data.complaintCount);
        document.getElementById('praiseCount').textContent = safe(data && data.praiseCount);
    }

    async loadTableData() {
        try {
            const body = await ReportAPI.getServiceQualityStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                tab: this.state.activeTab,
                deptName: this.state.filter.deptName,
                deptCode: this.state.filter.deptCode,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            const tabData = (body && body[this.state.activeTab]) ? body[this.state.activeTab] : { list: [], total: 0 };
            this.state.data = tabData.list || [];
            this.state.total = tabData.total || 0;
            this.renderTable();
            this.renderPagination();
            this.updatePageInfo();
        } catch (error) {
            console.error('Load table data failed:', error);
        }
    }

    renderTable() {
        const tbody = document.getElementById('tableBody');
        const thead = document.getElementById('tableHead');
        let html = '';

        if (this.state.activeTab === 'complaint') {
            thead.innerHTML = `
                <tr>
                    <th>投诉时间</th>
                    <th>被投诉科室</th>
                    <th>被投诉人员</th>
                    <th>岗位类别</th>
                    <th>投诉分类</th>
                    <th>处理结果</th>
                    <th>备注</th>
                </tr>
            `;
            this.state.data.forEach(row => {
                html += `
                    <tr>
                        <td>${row.time}</td>
                        <td>${row.dept}</td>
                        <td>${row.person}</td>
                        <td>${row.position}</td>
                        <td>${row.category}</td>
                        <td>${row.result}</td>
                        <td>${row.remark || ''}</td>
                    </tr>
                `;
            });
        } else {
            thead.innerHTML = `
                <tr>
                    <th>表扬时间</th>
                    <th>岗位类别</th>
                    <th>表扬方式</th>
                    <th>是否反馈科室</th>
                    <th>备注</th>
                </tr>
            `;
            this.state.data.forEach(row => {
                html += `
                    <tr>
                        <td>${row.time}</td>
                        <td>${row.position}</td>
                        <td>${row.method}</td>
                        <td>${row.feedback}</td>
                        <td>${row.remark || ''}</td>
                    </tr>
                `;
            });
        }

        if (this.state.data.length === 0) {
            const colSpan = this.state.activeTab === 'complaint' ? 7 : 5;
            html += `<tr><td colspan="${colSpan}" class="text-center text-muted py-4">暂无数据</td></tr>`;
        }

        tbody.innerHTML = html;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="serviceQualityController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="serviceQualityController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="serviceQualityController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="serviceQualityController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="serviceQualityController.goToPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('pagination').innerHTML = html;
    }

    updatePageInfo() {
        document.getElementById('pageInfo').textContent = `${this.state.pageSize}条/页 共${this.state.total}条`;
    }

    goToPage(page) {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        if (page < 1 || page > totalPages) return;
        this.state.currentPage = page;
        this.loadTableData();
    }

    jumpToPage() {
        const input = document.getElementById('jumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToPage(page);
            input.value = '';
        }
    }
}

function exportData() {
    const data = serviceQualityController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const tab = serviceQualityController.state.activeTab;
    let headers, rows;

    if (tab === 'complaint') {
        headers = ['投诉时间', '被投诉科室', '被投诉人员', '岗位类别', '投诉分类', '处理结果', '备注'];
        rows = data.map(row => [row.time, row.dept, row.person, row.position, row.category, row.result, row.remark || '']);
    } else {
        headers = ['表扬时间', '岗位类别', '表扬方式', '是否反馈科室', '备注'];
        rows = data.map(row => [row.time, row.position, row.method, row.feedback, row.remark || '']);
    }

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = tab === 'complaint'
        ? [{ wch: 16 }, { wch: 18 }, { wch: 12 }, { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 20 }]
        : [{ wch: 16 }, { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 20 }];

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

    const sheetName = tab === 'complaint' ? '投诉明细' : '表扬明细';
    XLSX.utils.book_append_sheet(wb, ws, sheetName);
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `${sheetName}_${dateStr}.xlsx`);
}

function jumpToPage() {
    serviceQualityController.jumpToPage();
}

/**
 * 字典管理弹窗逻辑
 */
class DictManager {
    constructor() {
        this.state = {
            category: 'position',
            page: 1,
            pageSize: 10,
            total: 0,
            data: []
        };
        this.bindEvents();
    }

    bindEvents() {
        document.querySelectorAll('#dictCategoryList a').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                document.querySelectorAll('#dictCategoryList a').forEach(a => a.classList.remove('active'));
                item.classList.add('active');
                this.state.category = item.dataset.category;
                this.state.page = 1;
                this.load();
            });
        });

        document.getElementById('dictAddBtn').addEventListener('click', () => this.addValue());
        document.getElementById('dictNewValue').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this.addValue();
        });

        document.getElementById('dictModal').addEventListener('shown.bs.modal', () => {
            this.state.page = 1;
            this.load();
        });
    }

    async load() {
        try {
            const body = await ReportAPI.getDataDict({ action: 'query', dictType: this.state.category });
            const list = (body && body.list) ? body.list : [];
            this.state.total = list.length;
            const start = (this.state.page - 1) * this.state.pageSize;
            this.state.data = list.slice(start, start + this.state.pageSize);
            this.render();
            this.renderPagination();
        } catch (error) {
            console.error('Load dict failed:', error);
        }
    }

    render() {
        const tbody = document.getElementById('dictValueBody');
        if (this.state.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="text-center text-muted py-3">暂无字典值</td></tr>';
            return;
        }
        tbody.innerHTML = this.state.data.map(item => `
            <tr>
                <td>${item.dictName}</td>
                <td><a href="#" class="text-danger" data-id="${item.id}">删除</a></td>
            </tr>
        `).join('');
        tbody.querySelectorAll('a[data-id]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.deleteValue(parseInt(link.dataset.id, 10));
            });
        });
    }

    renderPagination() {
        const totalPages = Math.max(1, Math.ceil(this.state.total / this.state.pageSize));
        const current = this.state.page;
        let html = '';
        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current - 1}">&lt;</a>
        </li>`;
        for (let i = 1; i <= totalPages; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>`;
        }
        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current + 1}">&gt;</a>
        </li>`;
        const pager = document.getElementById('dictPagination');
        pager.innerHTML = html;
        pager.querySelectorAll('a[data-page]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.dataset.page, 10);
                if (page >= 1 && page <= totalPages) {
                    this.state.page = page;
                    this.load();
                }
            });
        });
    }

    async addValue() {
        const input = document.getElementById('dictNewValue');
        const value = input.value.trim();
        if (!value) {
            alert('请输入字典值');
            return;
        }
        try {
            await ReportAPI.getDataDict({ action: 'add', dictType: this.state.category, dictName: value });
            input.value = '';
            this.state.page = Math.ceil((this.state.total + 1) / this.state.pageSize);
            this.load();
        } catch (error) {
            console.error('Add dict failed:', error);
            alert('新增失败');
        }
    }

    async deleteValue(id) {
        if (!confirm('确认删除该字典值？')) return;
        try {
            await ReportAPI.getDataDict({ action: 'delete', dictType: this.state.category, id });
            this.load();
        } catch (error) {
            console.error('Delete dict failed:', error);
            alert('删除失败');
        }
    }
}

/**
 * 数据维护弹窗逻辑
 */
class MaintainManager {
    constructor() {
        const today = serviceQualityController.formatDate(new Date());
        this.state = {
            type: 'complaint',
            date: today,
            page: 1,
            pageSize: 10,
            total: 0,
            rows: []
        };
        this.deptList = [];
        this.staffList = [];
        this.dictCache = {};
        this.inited = false;
        this.bindEvents();
    }

    bindEvents() {
        document.querySelectorAll('#maintainTypeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#maintainTypeFilter .filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.state.type = btn.dataset.value;
                this.state.page = 1;
                if (this.inited) this.load();
            });
        });

        document.getElementById('maintainQueryBtn').addEventListener('click', () => {
            this.state.page = 1;
            this.load();
        });

        document.getElementById('maintainAddRowBtn').addEventListener('click', () => {
            this.state.rows.push({ id: null, time: this.state.date + ' 08:00' });
            this.render();
        });

        document.getElementById('maintainSaveBtn').addEventListener('click', () => this.save());

        document.getElementById('maintainModal').addEventListener('shown.bs.modal', () => this.init());

        // 表格内事件委托
        const tbody = document.getElementById('maintainTableBody');
        tbody.addEventListener('change', (e) => {
            const tr = e.target.closest('tr');
            if (!tr) return;
            if (e.target.classList.contains('maintain-dept')) {
                this.refreshPersonOptions(tr);
            } else if (e.target.classList.contains('maintain-person')) {
                this.fillPositionByPerson(tr);
            }
        });
        tbody.addEventListener('click', (e) => {
            const link = e.target.closest('a.maintain-delete');
            if (!link) return;
            e.preventDefault();
            this.deleteRow(link.closest('tr'));
        });
    }

    async init() {
        if (this.inited) {
            this.load();
            return;
        }
        // 日期控件
        this.datePicker = flatpickr(document.getElementById('maintainDate'), {
            dateFormat: 'Y-m-d',
            defaultDate: this.state.date,
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 1) {
                    this.state.date = serviceQualityController.formatDate(selectedDates[0]);
                    this.state.page = 1;
                    if (this.inited) this.load();
                }
            }
        });
        try {
            const [deptBody, staffBody, dictBody] = await Promise.all([
                ReportAPI.getDeptDict({ deptType: 0 }),
                ReportAPI.getStaffDict({}),
                ReportAPI.getDataDict({ action: 'query' })
            ]);
            this.deptList = ((deptBody && deptBody.list) || []).filter(d => d.deptCode !== '0000');
            this.staffList = (staffBody && staffBody.list) || [];
            ((dictBody && dictBody.list) || []).forEach(item => {
                if (!this.dictCache[item.dictType]) this.dictCache[item.dictType] = [];
                this.dictCache[item.dictType].push(item.dictName);
            });
            this.inited = true;
            this.load();
        } catch (error) {
            console.error('Init maintain failed:', error);
        }
    }

    needDictTypes() {
        return this.state.type === 'complaint'
            ? ['position', 'complaintCategory', 'complaintResult']
            : ['position', 'praiseMethod', 'feedback'];
    }

    async load() {
        try {
            const body = await ReportAPI.maintainServiceQuality({
                action: 'query',
                type: this.state.type,
                startDate: this.state.date,
                endDate: this.state.date,
                page: this.state.page,
                pageSize: this.state.pageSize
            });
            this.state.rows = (body && body.list) ? body.list : [];
            this.state.total = (body && body.total) ? body.total : 0;
            this.render();
            this.renderPagination();
        } catch (error) {
            console.error('Load maintain failed:', error);
        }
    }

    render() {
        const isComplaint = this.state.type === 'complaint';
        const title = isComplaint ? '被投诉科室' : '被表扬科室';
        const personTitle = isComplaint ? '被投诉人员' : '被表扬人员';
        document.getElementById('maintainTableHead').innerHTML = `
            <tr>
                <th style="min-width:150px">${isComplaint ? '投诉时间' : '表扬时间'}</th>
                <th style="min-width:130px">${title}</th>
                <th style="min-width:110px">${personTitle}</th>
                <th style="min-width:110px">岗位类别</th>
                <th style="min-width:110px">${isComplaint ? '投诉分类' : '表扬方式'}</th>
                <th style="min-width:110px">${isComplaint ? '处理结果' : '是否反馈科室'}</th>
                <th style="min-width:120px">备注</th>
                <th style="min-width:60px">操作</th>
            </tr>
        `;

        const tbody = document.getElementById('maintainTableBody');
        if (this.state.rows.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">暂无数据</td></tr>';
            return;
        }
        tbody.innerHTML = this.state.rows.map(row => `
            <tr data-id="${row.id != null ? row.id : ''}">
                <td><input type="text" class="form-control form-control-sm maintain-time" value="${row.time || ''}"></td>
                <td><select class="form-select form-select-sm maintain-dept">${this.deptOptions(row.deptCode)}</select></td>
                <td><select class="form-select form-select-sm maintain-person">${this.personOptions(row.deptCode, row.personName)}</select></td>
                <td><select class="form-select form-select-sm maintain-position">${this.dictOptions('position', row.position)}</select></td>
                <td><select class="form-select form-select-sm maintain-attr1">${this.dictOptions(isComplaint ? 'complaintCategory' : 'praiseMethod', isComplaint ? row.category : row.method)}</select></td>
                <td><select class="form-select form-select-sm maintain-attr2">${this.dictOptions(isComplaint ? 'complaintResult' : 'feedback', isComplaint ? row.result : row.feedback)}</select></td>
                <td><input type="text" class="form-control form-control-sm maintain-remark" value="${row.remark || ''}"></td>
                <td><a href="#" class="text-danger maintain-delete">删除</a></td>
            </tr>
        `).join('');

        tbody.querySelectorAll('.maintain-time').forEach(input => {
            flatpickr(input, {
                dateFormat: 'Y-m-d H:i',
                enableTime: true,
                locale: 'zh',
                allowInput: false
            });
        });
    }

    deptOptions(selectedCode) {
        const options = ['<option value="">请选择</option>'];
        this.deptList.forEach(d => {
            const sel = d.deptCode === selectedCode ? ' selected' : '';
            options.push(`<option value="${d.deptCode}" data-dept-name="${d.deptName}"${sel}>${d.deptName}</option>`);
        });
        return options.join('');
    }

    personOptions(deptCode, selectedName) {
        const list = (deptCode && this.staffList.some(s => s.deptCode === deptCode))
            ? this.staffList.filter(s => s.deptCode === deptCode)
            : this.staffList;
        const options = ['<option value="">请选择</option>'];
        list.forEach(s => {
            const sel = s.staffName === selectedName ? ' selected' : '';
            options.push(`<option value="${s.staffName}" data-position="${s.position || ''}"${sel}>${s.staffName}</option>`);
        });
        return options.join('');
    }

    dictOptions(dictType, selectedValue) {
        const values = this.dictCache[dictType] || [];
        const options = ['<option value=""></option>'];
        values.forEach(v => {
            const sel = v === selectedValue ? ' selected' : '';
            options.push(`<option value="${v}"${sel}>${v}</option>`);
        });
        return options.join('');
    }

    /** 科室变化后刷新该行人员下拉 */
    refreshPersonOptions(tr) {
        const deptCode = tr.querySelector('.maintain-dept').value;
        const personSelect = tr.querySelector('.maintain-person');
        const currentName = personSelect.value;
        personSelect.innerHTML = this.personOptions(deptCode, currentName);
    }

    /** 人员选中后自动带出岗位类别 */
    fillPositionByPerson(tr) {
        const option = tr.querySelector('.maintain-person').selectedOptions[0];
        if (option && option.dataset.position) {
            tr.querySelector('.maintain-position').value = option.dataset.position;
        }
    }

    renderPagination() {
        const totalPages = Math.max(1, Math.ceil(this.state.total / this.state.pageSize));
        const current = this.state.page;
        let html = '';
        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current - 1}">&lt;</a>
        </li>`;
        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }
        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
            </li>`;
        }
        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${current + 1}">&gt;</a>
        </li>`;
        document.getElementById('maintainPageInfo').textContent = `${this.state.pageSize}条/页 共${this.state.total}条`;
        const pager = document.getElementById('maintainPagination');
        pager.innerHTML = html;
        pager.querySelectorAll('a[data-page]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.dataset.page, 10);
                if (page >= 1 && page <= totalPages) {
                    this.state.page = page;
                    this.load();
                }
            });
        });
    }

    collectRows() {
        const isComplaint = this.state.type === 'complaint';
        const items = [];
        document.querySelectorAll('#maintainTableBody tr[data-id]').forEach(tr => {
            const deptOption = tr.querySelector('.maintain-dept').selectedOptions[0];
            const item = {
                id: tr.dataset.id ? parseInt(tr.dataset.id, 10) : null,
                time: tr.querySelector('.maintain-time').value || null,
                deptCode: tr.querySelector('.maintain-dept').value || null,
                deptName: deptOption ? (deptOption.dataset.deptName || '') : '',
                personName: tr.querySelector('.maintain-person').value || null,
                position: tr.querySelector('.maintain-position').value || null,
                remark: tr.querySelector('.maintain-remark').value || null
            };
            if (isComplaint) {
                item.category = tr.querySelector('.maintain-attr1').value || null;
                item.result = tr.querySelector('.maintain-attr2').value || null;
            } else {
                item.method = tr.querySelector('.maintain-attr1').value || null;
                item.feedback = tr.querySelector('.maintain-attr2').value || null;
            }
            items.push(item);
        });
        return items;
    }

    async save() {
        const items = this.collectRows();
        if (items.length === 0) {
            alert('暂无可保存的数据');
            return;
        }
        try {
            await ReportAPI.maintainServiceQuality({
                action: 'save',
                type: this.state.type,
                list: items
            });
            alert('保存成功');
            this.load();
        } catch (error) {
            console.error('Save maintain failed:', error);
            alert('保存失败');
        }
    }

    async deleteRow(tr) {
        const id = tr.dataset.id;
        if (!id) {
            // 未保存的新行，直接从界面移除
            tr.remove();
            return;
        }
        if (!confirm('确认删除该条数据？')) return;
        try {
            await ReportAPI.maintainServiceQuality({
                action: 'delete',
                type: this.state.type,
                id: parseInt(id, 10)
            });
            this.load();
        } catch (error) {
            console.error('Delete maintain failed:', error);
            alert('删除失败');
        }
    }
}

const serviceQualityController = new ServiceQualityController();
const dictManager = new DictManager();
const maintainManager = new MaintainManager();
