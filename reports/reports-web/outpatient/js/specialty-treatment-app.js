/**
 * 专科治疗量统计页面主逻辑
 */
class SpecialtyTreatmentController {
    constructor() {
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc',
            filter: {
                timeRange: 'today',
                startDate: '2025-09-22',
                endDate: '2025-10-22',
                deptName: ''
            }
        };

        this.init();
    }

    init() {
        this.bindEvents();
        this.initDateRangePicker();
        this.loadOverview();
        this.loadTableData();
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
                    this.state.filter.startDate = formatDate(selectedDates[0]);
                    this.state.filter.endDate = formatDate(selectedDates[1]);
                    this.state.currentPage = 1;
                    this.loadTableData();
                }
            }
        });
    }

    bindEvents() {
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        document.getElementById('deptSelect').addEventListener('change', (e) => {
            this.state.filter.deptName = e.target.value;
            this.state.currentPage = 1;
            this.loadTableData();
        });

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadTableData();
        });

        document.querySelectorAll('#specialtyTable .sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e));
        });
    }

    handleTimeFilter(e) {
        const btn = e.target;
        document.querySelectorAll('#timeFilter .filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.state.filter.timeRange = btn.dataset.value;
        this.state.currentPage = 1;
        this.loadOverview();
        this.loadTableData();
    }

    handleSort(e) {
        const th = e.currentTarget;
        const column = th.dataset.column;
        const type = th.dataset.type;

        if (this.state.sortColumn === column) {
            this.state.sortDirection = this.state.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.state.sortColumn = column;
            this.state.sortDirection = 'asc';
        }

        document.querySelectorAll('#specialtyTable .sortable').forEach(el => {
            el.classList.remove('asc', 'desc');
        });
        th.classList.add(this.state.sortDirection);

        this.sortData(column, type);
        this.renderTable();
    }

    sortData(column, type) {
        const direction = this.state.sortDirection === 'asc' ? 1 : -1;
        this.state.data.sort((a, b) => {
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
            const body = await ReportAPI.getSpecialtyTreatmentStats({
                timeRange: this.state.filter.timeRange,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            if (body && body.overview) {
                this.renderOverview(body.overview);
            }
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        document.getElementById('treatmentCount').textContent = data.treatmentCount.toLocaleString();
        document.getElementById('treatmentAmount').textContent = data.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '元';
        document.getElementById('patientCount').textContent = data.patientCount.toLocaleString();
    }

    async loadTableData() {
        try {
            const body = await ReportAPI.getSpecialtyTreatmentStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                deptName: this.state.filter.deptName,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            if (body && body.table) {
                this.state.data = body.table.list;
                this.state.total = body.table.total;
                this.renderTable();
                this.renderPagination();
                this.updatePageInfo();
            }
        } catch (error) {
            console.error('Load table data failed:', error);
        }
    }

    renderTable() {
        const tbody = document.getElementById('tableBody');
        let html = '';

        this.state.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.deptName}</td>
                    <td>${row.treatmentCount}</td>
                    <td>${row.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                    <td>${row.patientCount}</td>
                </tr>
            `;
        });

        if (this.state.data.length > 0) {
            const summary = this.calculateSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td>${summary.treatmentCount}</td>
                    <td>${summary.treatmentAmount.toLocaleString('zh-CN', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</td>
                    <td>${summary.patientCount}</td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    calculateSummary() {
        const summary = {
            treatmentCount: 0,
            treatmentAmount: 0,
            patientCount: 0
        };
        this.state.data.forEach(row => {
            summary.treatmentCount += row.treatmentCount;
            summary.treatmentAmount += row.treatmentAmount;
            summary.patientCount += row.patientCount;
        });
        return summary;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="specialtyTreatmentController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="specialtyTreatmentController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="specialtyTreatmentController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="specialtyTreatmentController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="specialtyTreatmentController.goToPage(${current + 1}); return false;">></a>
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
    const data = specialtyTreatmentController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = ['科室', '已治疗次数', '已治疗金额', '治疗患者数'];
    const rows = data.map(row => [
        row.deptName,
        row.treatmentCount,
        row.treatmentAmount,
        row.patientCount
    ]);

    const summary = specialtyTreatmentController.calculateSummary();
    rows.push([
        '全列表数据合计',
        summary.treatmentCount,
        summary.treatmentAmount,
        summary.patientCount
    ]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 20 }, { wch: 14 }, { wch: 16 }, { wch: 14 }];

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

    XLSX.utils.book_append_sheet(wb, ws, '专科治疗量统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `专科治疗量统计_${dateStr}.xlsx`);
}

function jumpToPage() {
    specialtyTreatmentController.jumpToPage();
}

const specialtyTreatmentController = new SpecialtyTreatmentController();
