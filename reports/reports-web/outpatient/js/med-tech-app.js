/**
 * 医技统计页面主逻辑
 */
class MedTechController {
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
                endDate: '2025-10-22'
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

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadTableData();
        });

        document.querySelectorAll('#medTechTable .sortable').forEach(th => {
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

        document.querySelectorAll('#medTechTable .sortable').forEach(el => {
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
            } else if (type === 'percent') {
                valA = parseFloat(valA.replace('%', ''));
                valB = parseFloat(valB.replace('%', ''));
            }
            if (valA < valB) return -1 * direction;
            if (valA > valB) return 1 * direction;
            return 0;
        });
    }

    async loadOverview() {
        try {
            const result = await ReportAPI.getMedTechStats({
                timeRange: this.state.filter.timeRange,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            if (result.code === 200) {
                this.renderOverview(result.data.overview);
            }
        } catch (error) {
            console.error('Load overview failed:', error);
        }
    }

    renderOverview(data) {
        document.getElementById('checkCount').textContent = data.checkCount.toLocaleString();
        document.getElementById('onTimeRate').textContent = data.onTimeRate;
        document.getElementById('waitTime').textContent = data.waitTime;
        document.getElementById('avgWaitLate').textContent = data.avgWaitLate;
        document.getElementById('avgReportTime').textContent = data.avgReportTime;
    }

    async loadTableData() {
        try {
            const result = await ReportAPI.getMedTechStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            if (result.code === 200) {
                this.state.data = result.data.table.list;
                this.state.total = result.data.table.total;
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
                    <td>${row.checkCount}</td>
                    <td>${row.onTimeRate}</td>
                    <td>${row.waitTime}</td>
                    <td>${row.avgWaitLate}</td>
                    <td>${row.avgReportTime}</td>
                </tr>
            `;
        });

        if (this.state.data.length > 0) {
            const summary = this.calculateSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td>${summary.checkCount}</td>
                    <td>${summary.onTimeRate}</td>
                    <td>${summary.waitTime.toFixed(1)}</td>
                    <td>${summary.avgWaitLate.toFixed(1)}</td>
                    <td>${summary.avgReportTime.toFixed(1)}</td>
                </tr>
            `;
        }

        tbody.innerHTML = html;
    }

    calculateSummary() {
        const summary = {
            checkCount: 0,
            onTimeRateSum: 0,
            waitTime: 0,
            avgWaitLate: 0,
            avgReportTime: 0
        };
        this.state.data.forEach(row => {
            summary.checkCount += row.checkCount;
            summary.onTimeRateSum += parseFloat(row.onTimeRate);
            summary.waitTime += row.waitTime;
            summary.avgWaitLate += row.avgWaitLate;
            summary.avgReportTime += row.avgReportTime;
        });
        const count = this.state.data.length;
        summary.onTimeRate = count > 0 ? (summary.onTimeRateSum / count).toFixed(1) + '%' : '0%';
        summary.waitTime = count > 0 ? summary.waitTime / count : 0;
        summary.avgWaitLate = count > 0 ? summary.avgWaitLate / count : 0;
        summary.avgReportTime = count > 0 ? summary.avgReportTime / count : 0;
        return summary;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="medTechController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="medTechController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="medTechController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="medTechController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="medTechController.goToPage(${current + 1}); return false;">></a>
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

/**
 * 导出数据为 Excel
 */
function exportData() {
    const data = medTechController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = ['科室', '检查量', '按时检查率', '预约等候时间', '平均等候时长（未按时）', '平均出报告时间'];
    const rows = data.map(row => [
        row.deptName,
        row.checkCount,
        row.onTimeRate,
        row.waitTime,
        row.avgWaitLate,
        row.avgReportTime
    ]);

    const summary = medTechController.calculateSummary();
    rows.push([
        '全列表数据合计',
        summary.checkCount,
        summary.onTimeRate,
        summary.waitTime.toFixed(1),
        summary.avgWaitLate.toFixed(1),
        summary.avgReportTime.toFixed(1)
    ]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    ws['!cols'] = [{ wch: 16 }, { wch: 10 }, { wch: 14 }, { wch: 14 }, { wch: 18 }, { wch: 14 }];

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

    XLSX.utils.book_append_sheet(wb, ws, '医技统计');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `医技统计_${dateStr}.xlsx`);
}

function jumpToPage() {
    medTechController.jumpToPage();
}

const medTechController = new MedTechController();
