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
            this.datePicker.setDate([toFlatpickrDate(range.startDate), toFlatpickrDate(range.endDate)]);
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
                deptName: this.state.filter.deptName
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
                    <th>被表扬科室</th>
                    <th>被表扬人员</th>
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
                        <td>${row.dept}</td>
                        <td>${row.person}</td>
                        <td>${row.position}</td>
                        <td>${row.method}</td>
                        <td>${row.feedback}</td>
                        <td>${row.remark || ''}</td>
                    </tr>
                `;
            });
        }

        if (this.state.data.length === 0) {
            html += '<tr><td colspan="7" class="text-center text-muted py-4">暂无数据</td></tr>';
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
        headers = ['表扬时间', '被表扬科室', '被表扬人员', '岗位类别', '表扬方式', '是否反馈科室', '备注'];
        rows = data.map(row => [row.time, row.dept, row.person, row.position, row.method, row.feedback, row.remark || '']);
    }

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 16 }, { wch: 18 }, { wch: 12 }, { wch: 14 }, { wch: 14 }, { wch: 14 }, { wch: 20 }];

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

const serviceQualityController = new ServiceQualityController();
