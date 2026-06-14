/**
 * 门诊预警统计页面主逻辑
 */
class AlertController {
    constructor() {
        this.filter = {
            timeRange: 'today',
            startDate: '2025-09-22',
            endDate: '2025-10-22',
            deptName: ''
        };
        this.deptState = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc'
        };
        this.doctorState = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc'
        };

        this.init();
    }

    init() {
        this.bindEvents();
        this.initDateRangePicker();
        this.loadOverview();
        this.loadDeptData();
        this.loadDoctorData();
    }

    initDateRangePicker() {
        const dateRangeInput = document.getElementById('dateRange');
        if (!dateRangeInput) return;

        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            defaultDate: ['2025/09/22', '2025/10/22'],
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    const formatDate = (date) => {
                        const y = date.getFullYear();
                        const m = String(date.getMonth() + 1).padStart(2, '0');
                        const d = String(date.getDate()).padStart(2, '0');
                        return `${y}-${m}-${d}`;
                    };
                    this.filter.startDate = formatDate(selectedDates[0]);
                    this.filter.endDate = formatDate(selectedDates[1]);
                    this.deptState.currentPage = 1;
                    this.doctorState.currentPage = 1;
                    this.loadDeptData();
                    this.loadDoctorData();
                }
            }
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        document.getElementById('deptSelect').addEventListener('change', (e) => {
            this.filter.deptName = e.target.value;
            this.deptState.currentPage = 1;
            this.doctorState.currentPage = 1;
            this.loadDeptData();
            this.loadDoctorData();
        });

        document.getElementById('deptPageSizeSelect').addEventListener('change', (e) => {
            this.deptState.pageSize = parseInt(e.target.value);
            this.deptState.currentPage = 1;
            this.loadDeptData();
        });

        document.getElementById('doctorPageSizeSelect').addEventListener('change', (e) => {
            this.doctorState.pageSize = parseInt(e.target.value);
            this.doctorState.currentPage = 1;
            this.loadDoctorData();
        });

        document.querySelectorAll('#deptTable .sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e, 'dept'));
        });

        document.querySelectorAll('#doctorTable .sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e, 'doctor'));
        });
    }

    handleTimeFilter(e) {
        const btn = e.target;
        document.querySelectorAll('#timeFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.filter.timeRange = btn.dataset.value;
        this.deptState.currentPage = 1;
        this.doctorState.currentPage = 1;
        this.loadOverview();
        this.loadDeptData();
        this.loadDoctorData();
    }

    handleSort(e, tableType) {
        const th = e.currentTarget;
        const column = th.dataset.column;
        const type = th.dataset.type;
        const state = tableType === 'dept' ? this.deptState : this.doctorState;
        const tableId = tableType === 'dept' ? 'deptTable' : 'doctorTable';

        if (state.sortColumn === column) {
            state.sortDirection = state.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            state.sortColumn = column;
            state.sortDirection = 'asc';
        }

        document.querySelectorAll(`#${tableId} .sortable`).forEach(el => {
            el.classList.remove('asc', 'desc');
        });
        th.classList.add(state.sortDirection);

        this.sortData(state, column, type);
        if (tableType === 'dept') {
            this.renderDeptTable();
        } else {
            this.renderDoctorTable();
        }
    }

    sortData(state, column, type) {
        const direction = state.sortDirection === 'asc' ? 1 : -1;
        state.data.sort((a, b) => {
            let valA = a[column];
            let valB = b[column];
            if (type === 'number') {
                valA = parseFloat(valA);
                valB = parseFloat(valB);
            }
            if (valA < valB) return -1 * direction;
            if (valA > valB) return 1 * direction;
            return 0;
        });
    }

    async loadOverview() {
        try {
            const body = await ReportAPI.getAlertStats({
                timeRange: this.filter.timeRange,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate
            });
            if (body && body.overview) {
                this.renderOverview(body.overview);
            }
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        document.getElementById('remainAlert').textContent = data.remainAlert;
        document.getElementById('appointmentAlert').textContent = data.appointmentAlert;
        document.getElementById('earlyLeave').textContent = data.earlyLeave;
    }

    async loadDeptData() {
        try {
            const body = await ReportAPI.getAlertStats({
                page: this.deptState.currentPage,
                pageSize: this.deptState.pageSize,
                deptName: this.filter.deptName,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate
            });
            if (body && body.deptTable) {
                this.deptState.data = body.deptTable.list;
                this.deptState.total = body.deptTable.total;
                this.renderDeptTable();
                this.renderDeptPagination();
                this.updateDeptPageInfo();
            }
        } catch (error) {
            console.error('Load dept data failed:', error);
        }
    }

    async loadDoctorData() {
        try {
            const body = await ReportAPI.getAlertStats({
                page: this.doctorState.currentPage,
                pageSize: this.doctorState.pageSize,
                deptName: this.filter.deptName,
                startDate: this.filter.startDate,
                endDate: this.filter.endDate
            });
            if (body && body.doctorTable) {
                this.doctorState.data = body.doctorTable.list;
                this.doctorState.total = body.doctorTable.total;
                this.renderDoctorTable();
                this.renderDoctorPagination();
                this.updateDoctorPageInfo();
            }
        } catch (error) {
            console.error('Load doctor data failed:', error);
        }
    }

    renderDeptTable() {
        const tbody = document.getElementById('deptTableBody');
        let html = '';
        this.deptState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.deptName}</td>
                    <td>${row.remainAlert}</td>
                    <td>${row.appointmentAlert}</td>
                    <td>${row.earlyLeave}</td>
                </tr>
            `;
        });
        if (this.deptState.data.length > 0) {
            const summary = this.calculateDeptSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td>${summary.remainAlert}</td>
                    <td>${summary.appointmentAlert}</td>
                    <td>${summary.earlyLeave}</td>
                </tr>
            `;
        }
        tbody.innerHTML = html;
    }

    calculateDeptSummary() {
        const summary = { remainAlert: 0, appointmentAlert: 0, earlyLeave: 0 };
        this.deptState.data.forEach(row => {
            summary.remainAlert += row.remainAlert;
            summary.appointmentAlert += row.appointmentAlert;
            summary.earlyLeave += row.earlyLeave;
        });
        return summary;
    }

    renderDoctorTable() {
        const tbody = document.getElementById('doctorTableBody');
        let html = '';
        this.doctorState.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.doctorName}</td>
                    <td>${row.deptName}</td>
                    <td>${row.remainAlert}</td>
                    <td>${row.appointmentAlert}</td>
                    <td>${row.earlyLeave}</td>
                </tr>
            `;
        });
        if (this.doctorState.data.length > 0) {
            const summary = this.calculateDoctorSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td></td>
                    <td>${summary.remainAlert}</td>
                    <td>${summary.appointmentAlert}</td>
                    <td>${summary.earlyLeave}</td>
                </tr>
            `;
        }
        tbody.innerHTML = html;
    }

    calculateDoctorSummary() {
        const summary = { remainAlert: 0, appointmentAlert: 0, earlyLeave: 0 };
        this.doctorState.data.forEach(row => {
            summary.remainAlert += row.remainAlert;
            summary.appointmentAlert += row.appointmentAlert;
            summary.earlyLeave += row.earlyLeave;
        });
        return summary;
    }

    renderPagination(state, paginationId, goToFn) {
        const totalPages = Math.ceil(state.total / state.pageSize);
        const current = state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="${goToFn}(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="${goToFn}(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="${goToFn}(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="${goToFn}(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="${goToFn}(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById(paginationId).innerHTML = html;
    }

    renderDeptPagination() {
        this.renderPagination(this.deptState, 'deptPagination', 'alertController.goToDeptPage');
    }

    renderDoctorPagination() {
        this.renderPagination(this.doctorState, 'doctorPagination', 'alertController.goToDoctorPage');
    }

    updateDeptPageInfo() {
        document.getElementById('deptPageInfo').textContent = `${this.deptState.pageSize}条/页 共${this.deptState.total}条`;
    }

    updateDoctorPageInfo() {
        document.getElementById('doctorPageInfo').textContent = `${this.doctorState.pageSize}条/页 共${this.doctorState.total}条`;
    }

    goToDeptPage(page) {
        const totalPages = Math.ceil(this.deptState.total / this.deptState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.deptState.currentPage = page;
        this.loadDeptData();
    }

    goToDoctorPage(page) {
        const totalPages = Math.ceil(this.doctorState.total / this.doctorState.pageSize);
        if (page < 1 || page > totalPages) return;
        this.doctorState.currentPage = page;
        this.loadDoctorData();
    }

    jumpToDeptPage() {
        const input = document.getElementById('deptJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToDeptPage(page);
            input.value = '';
        }
    }

    jumpToDoctorPage() {
        const input = document.getElementById('doctorJumpPage');
        const page = parseInt(input.value);
        if (page) {
            this.goToDoctorPage(page);
            input.value = '';
        }
    }
}

function exportDeptData() {
    const data = alertController.deptState.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }
    const headers = ['科室', '当日余号预警频次', '号源预约预警频次', '早退统计'];
    const rows = data.map(row => [row.deptName, row.remainAlert, row.appointmentAlert, row.earlyLeave]);
    const summary = alertController.calculateDeptSummary();
    rows.push(['全列表数据合计', summary.remainAlert, summary.appointmentAlert, summary.earlyLeave]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 20 }, { wch: 16 }, { wch: 16 }, { wch: 12 }];

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
    const lastRow = range.e.r;
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: lastRow, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        if (!ws[cellAddress].s) ws[cellAddress].s = {};
        ws[cellAddress].s.font = { bold: true };
    }

    XLSX.utils.book_append_sheet(wb, ws, '各科室门诊预警统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `各科室门诊预警统计_${dateStr}.xlsx`);
}

function exportDoctorData() {
    const data = alertController.doctorState.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }
    const headers = ['医生', '科室', '当日余号预警频次', '号源预约预警频次', '早退统计'];
    const rows = data.map(row => [row.doctorName, row.deptName, row.remainAlert, row.appointmentAlert, row.earlyLeave]);
    const summary = alertController.calculateDoctorSummary();
    rows.push(['全列表数据合计', '', summary.remainAlert, summary.appointmentAlert, summary.earlyLeave]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 12 }, { wch: 20 }, { wch: 16 }, { wch: 16 }, { wch: 12 }];

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
    const lastRow = range.e.r;
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: lastRow, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        if (!ws[cellAddress].s) ws[cellAddress].s = {};
        ws[cellAddress].s.font = { bold: true };
    }

    XLSX.utils.book_append_sheet(wb, ws, '各科室医生门诊预警统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `各科室医生门诊预警统计_${dateStr}.xlsx`);
}

function jumpToDeptPage() {
    alertController.jumpToDeptPage();
}

function jumpToDoctorPage() {
    alertController.jumpToDoctorPage();
}

const alertController = new AlertController();
