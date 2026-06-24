/**
 * 门诊量统计页面主逻辑
 * 管理页面状态、事件绑定、数据渲染
 */
class ReportController {
    constructor() {
        const today = this.formatDate(new Date());
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc',
            filter: {
                timeRange: 'today',
                startDate: today,
                endDate: today,
                deptName: ''
            }
        };

        this.init();
    }

    /**
     * 格式化日期 yyyy-MM-dd
     */
    formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    /**
     * 初始化方法
     */
    async init() {
        this.bindEvents();
        this.initDateRangePicker();
        await this.initDeptSelect({ triggerDefault: false });
        this.loadData();
    }

    /**
     * 初始化科室下拉框
     */
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
                this.loadData();
            },
            ...options
        });
        // 默认选中全部时，不传给后端科室条件
        this.state.filter.deptName = '';
        this.state.filter.deptCode = '';
    }

    /**
     * 初始化日期范围选择器
     */
    initDateRangePicker() {
        const dateRangeInput = document.getElementById('dateRange');
        if (!dateRangeInput) return;

        const today = new Date();
        const todayStr = `${today.getFullYear()}/${String(today.getMonth() + 1).padStart(2, '0')}/${String(today.getDate()).padStart(2, '0')}`;

        this.datePicker = flatpickr(dateRangeInput, {
            mode: 'range',
            dateFormat: 'Y/m/d',
            locale: 'zh',
            allowInput: false,
            onChange: (selectedDates) => {
                if (selectedDates.length === 2) {
                    this.state.filter.startDate = this.formatDate(selectedDates[0]);
                    this.state.filter.endDate = this.formatDate(selectedDates[1]);
                    this.state.currentPage = 1;
                    this.loadData();
                }
            }
        });
        // 初始化日期但不触发 onChange
        this.datePicker.setDate([todayStr, todayStr], false);
        this.state.filter.startDate = this.formatDate(today);
        this.state.filter.endDate = this.formatDate(today);
    }

    /**
     * 绑定页面事件
     */
    bindEvents() {
        // 时间筛选按钮事件
        document.querySelectorAll('#timeFilter .filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleTimeFilter(e));
        });

        // 分页大小变更
        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadData();
        });

        // 表格排序事件
        document.querySelectorAll('.sortable').forEach(th => {
            th.addEventListener('click', (e) => this.handleSort(e));
        });

    }

    /**
     * 处理时间筛选
     */
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
        this.loadData();
    }

    /**
     * 处理表格排序
     */
    handleSort(e) {
        const th = e.currentTarget;
        const column = th.dataset.column;
        const type = th.dataset.type;

        // 切换排序方向
        if (this.state.sortColumn === column) {
            this.state.sortDirection = this.state.sortDirection === 'asc' ? 'desc' : 'asc';
        } else {
            this.state.sortColumn = column;
            this.state.sortDirection = 'asc';
        }

        // 更新排序图标
        document.querySelectorAll('.sortable').forEach(el => {
            el.classList.remove('asc', 'desc');
        });
        th.classList.add(this.state.sortDirection);

        this.sortData(column, type);
        this.renderTable();
    }

    /**
     * 排序数据
     */
    sortData(column, type) {
        const direction = this.state.sortDirection === 'asc' ? 1 : -1;

        this.state.data.sort((a, b) => {
            let valA = this.getColumnValue(a, column);
            let valB = this.getColumnValue(b, column);

            if (type === 'number' || type === 'percent') {
                valA = parseFloat(String(valA).replace('%', ''));
                valB = parseFloat(String(valB).replace('%', ''));
            } else if (type === 'ratio') {
                const partsA = String(valA).split('/');
                const partsB = String(valB).split('/');
                valA = partsA.length === 2 ? parseFloat(partsA[0]) / parseFloat(partsA[1]) : 0;
                valB = partsB.length === 2 ? parseFloat(partsB[0]) / parseFloat(partsB[1]) : 0;
            }

            if (valA < valB) return -1 * direction;
            if (valA > valB) return 1 * direction;
            return 0;
        });
    }

    /**
     * 获取列值
     */
    getColumnValue(row, column) {
        const valueMap = {
            'totalVisits': row.totalVisits,
            'appointmentRate': row.appointmentRate,
            'examRate': row.examRate,
            'efficiency': row.efficiency,
            'visitCount': row.visitCount,
            'famousExpert': row.famousExpert,
            'specialExpert': row.specialExpert,
            'knownExpert': row.knownExpert,
            'expertA': row.expertA,
            'expertB': row.expertB,
            'ordinary': row.ordinary,
            'effectiveUnits': `${row.effectiveUnits}/${row.totalUnits}`,
            'unitFamous': `${row.unitFamousEffective}/${row.unitFamousTotal}`,
            'unitSpecial': `${row.unitSpecialEffective}/${row.unitSpecialTotal}`,
            'unitKnown': `${row.unitKnownEffective}/${row.unitKnownTotal}`,
            'unitExpertA': `${row.unitAEffective}/${row.unitATotal}`,
            'unitExpertB': `${row.unitBEffective}/${row.unitBTotal}`,
            'unitOrdinary': `${row.unitOrdinaryEffective}/${row.unitOrdinaryTotal}`
        };
        return valueMap[column] || '';
    }

    /**
     * 加载门诊运行数据（概览 + 表格，单次请求）
     */
    async loadData() {
        try {
            const params = {
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                deptCode: this.state.filter.deptCode || '',
                deptName: this.state.filter.deptName || '',
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            };
            const body = await ReportAPI.getOperationStats(params);
            if (body) {
                this.state.data = (body.table && body.table.list) ? body.table.list : [];
                this.state.total = (body.table && body.table.total) ? body.table.total : 0;
                this.renderOverview(body.overview);
                this.renderTable();
                this.renderPagination();
                this.updatePageInfo();
            }
        } catch (error) {
            console.error('Load data failed:', error);
        }
    }

    /**
     * 渲染概览数据
     */
    renderOverview(data) {
        const safe = (val) => val != null ? val : 0;
        const safeRate = (val) => val != null ? val : '-';

        document.getElementById('totalVisits').textContent = safe(data && data.totalVisits).toLocaleString();
        document.getElementById('appointmentRate').textContent = safeRate(data && data.appointmentRate);
        document.getElementById('visitCount').textContent = safe(data && data.visitCount);
        document.getElementById('examRate').textContent = safeRate(data && data.examRate);
        document.getElementById('efficiency').textContent = safe(data && data.efficiency);
        document.getElementById('effectiveUnits').textContent = safe(data && data.effectiveUnits);
        document.getElementById('totalUnits').textContent = safe(data && data.totalUnits);

        const colorMap = {
            famousExpert: 'blue',
            specialExpert: 'purple',
            knownExpert: 'cyan',
            expertA: 'green',
            expertB: 'orange',
            ordinary: 'red'
        };

        const expertTypes = [
            { key: 'famousExpert', label: '名医专家' },
            { key: 'specialExpert', label: '特需专家' },
            { key: 'knownExpert', label: '知名专家' },
            { key: 'expertA', label: '专家A类' },
            { key: 'expertB', label: '专家B类' },
            { key: 'ordinary', label: '普通门诊' }
        ];

        // 渲染出诊人次明细
        const visitDetailEl = document.getElementById('visitCountDetail');
        visitDetailEl.innerHTML = expertTypes.map(({ key, label }) => {
            const color = colorMap[key];
            return `<div class="detail-item"><div class="detail-label"><span class="dot ${color}"></span>${label}</div><div class="detail-num text-${color}">${safe(data && data[key])}</div></div>`;
        }).join('');

        const unitFields = [
            { colorKey: 'famousExpert', label: '名医专家', effectiveKey: 'unitFamousEffective', totalKey: 'unitFamousTotal' },
            { colorKey: 'specialExpert', label: '特需专家', effectiveKey: 'unitSpecialEffective', totalKey: 'unitSpecialTotal' },
            { colorKey: 'knownExpert', label: '知名专家', effectiveKey: 'unitKnownEffective', totalKey: 'unitKnownTotal' },
            { colorKey: 'expertA', label: '专家A类', effectiveKey: 'unitAEffective', totalKey: 'unitATotal' },
            { colorKey: 'expertB', label: '专家B类', effectiveKey: 'unitBEffective', totalKey: 'unitBTotal' },
            { colorKey: 'ordinary', label: '普通门诊', effectiveKey: 'unitOrdinaryEffective', totalKey: 'unitOrdinaryTotal' }
        ];

        // 渲染出诊单元明细
        const unitDetailEl = document.getElementById('unitDetail');
        unitDetailEl.innerHTML = unitFields.map(({ colorKey, label, effectiveKey, totalKey }) => {
            const color = colorMap[colorKey];
            return `<div class="detail-item"><div class="detail-label"><span class="dot ${color}"></span>${label}</div><div class="detail-num"><span class="text-${color}">${safe(data && data[effectiveKey])}</span><span class="text-secondary"> / ${safe(data && data[totalKey])}</span></div></div>`;
        }).join('');
    }

    /**
     * 加载表格数据
     */
    async loadTableData() {
        try {
            const body = await ReportAPI.getDepartmentStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                deptName: this.state.filter.deptName,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });

            if (body) {
                this.state.data = (body.table && body.table.list) ? body.table.list : [];
                this.state.total = (body.table && body.table.total) ? body.table.total : 0;
                this.renderTable();
                this.renderPagination();
                this.updatePageInfo();
            }
        } catch (error) {
            console.error('Load table data failed:', error);
        }
    }

    /**
     * 渲染表格
     */
    renderTable() {
        const tbody = document.getElementById('tableBody');
        let html = '';

        // 数据行
        this.state.data.forEach(row => {
            html += `
                <tr>
                    <td>${row.deptName}</td>
                    <td>${row.totalVisits}</td>
                    <td>${row.appointmentRate}</td>
                    <td>${row.examRate}</td>
                    <td>${row.efficiency}</td>
                    <td>${row.visitCount}</td>
                    <td>${row.famousExpert}</td>
                    <td>${row.specialExpert}</td>
                    <td>${row.knownExpert}</td>
                    <td>${row.expertA}</td>
                    <td>${row.expertB}</td>
                    <td>${row.ordinary}</td>
                    <td>${row.effectiveUnits}/${row.totalUnits}</td>
                    <td>${row.unitFamousEffective}/${row.unitFamousTotal}</td>
                    <td>${row.unitSpecialEffective}/${row.unitSpecialTotal}</td>
                    <td>${row.unitKnownEffective}/${row.unitKnownTotal}</td>
                    <td>${row.unitAEffective}/${row.unitATotal}</td>
                    <td>${row.unitBEffective}/${row.unitBTotal}</td>
                    <td>${row.unitOrdinaryEffective}/${row.unitOrdinaryTotal}</td>
                </tr>
            `;
        });

        // 合计行
        if (this.state.data.length > 0) {
            const summary = this.calculateSummary();
            html += `
                <tr>
                    <td>全列表数据合计</td>
                    <td>${summary.totalVisits}</td>
                    <td>${summary.appointmentRate}%</td>
                    <td>${summary.examRate}%</td>
                    <td>${summary.efficiency.toFixed(2)}</td>
                    <td>${summary.visitCount}</td>
                    <td>${summary.famousExpert}</td>
                    <td>${summary.specialExpert}</td>
                    <td>${summary.knownExpert}</td>
                    <td>${summary.expertA}</td>
                    <td>${summary.expertB}</td>
                    <td>${summary.ordinary}</td>
                    <td>${summary.effectiveUnits}/${summary.totalUnits}</td>
                    <td>${summary.unitFamousEffective}/${summary.unitFamousTotal}</td>
                    <td>${summary.unitSpecialEffective}/${summary.unitSpecialTotal}</td>
                    <td>${summary.unitKnownEffective}/${summary.unitKnownTotal}</td>
                    <td>${summary.unitAEffective}/${summary.unitATotal}</td>
                    <td>${summary.unitBEffective}/${summary.unitBTotal}</td>
                    <td>${summary.unitOrdinaryEffective}/${summary.unitOrdinaryTotal}</td>
                </tr>
            `;
        } else {
            html += `<tr><td colspan="19" class="text-center text-muted py-4">暂无数据</td></tr>`;
        }

        tbody.innerHTML = html;
    }

    /**
     * 计算合计
     */
    calculateSummary() {
        const summary = {
            totalVisits: 0,
            appointmentSum: 0,
            examSum: 0,
            efficiencySum: 0,
            visitCount: 0,
            famousExpert: 0,
            specialExpert: 0,
            knownExpert: 0,
            expertA: 0,
            expertB: 0,
            ordinary: 0,
            effectiveUnits: 0,
            totalUnits: 0,
            unitFamousEffective: 0,
            unitFamousTotal: 0,
            unitSpecialEffective: 0,
            unitSpecialTotal: 0,
            unitKnownEffective: 0,
            unitKnownTotal: 0,
            unitAEffective: 0,
            unitATotal: 0,
            unitBEffective: 0,
            unitBTotal: 0,
            unitOrdinaryEffective: 0,
            unitOrdinaryTotal: 0
        };

        this.state.data.forEach(row => {
            summary.totalVisits += row.totalVisits;
            summary.appointmentSum += parseFloat(row.appointmentRate);
            summary.examSum += parseFloat(row.examRate);
            summary.efficiencySum += parseFloat(row.efficiency);
            summary.visitCount += row.visitCount;
            summary.famousExpert += row.famousExpert;
            summary.specialExpert += row.specialExpert;
            summary.knownExpert += row.knownExpert;
            summary.expertA += row.expertA;
            summary.expertB += row.expertB;
            summary.ordinary += row.ordinary;
            summary.effectiveUnits += row.effectiveUnits;
            summary.totalUnits += row.totalUnits;
            summary.unitFamousEffective += row.unitFamousEffective;
            summary.unitFamousTotal += row.unitFamousTotal;
            summary.unitSpecialEffective += row.unitSpecialEffective;
            summary.unitSpecialTotal += row.unitSpecialTotal;
            summary.unitKnownEffective += row.unitKnownEffective;
            summary.unitKnownTotal += row.unitKnownTotal;
            summary.unitAEffective += row.unitAEffective;
            summary.unitATotal += row.unitATotal;
            summary.unitBEffective += row.unitBEffective;
            summary.unitBTotal += row.unitBTotal;
            summary.unitOrdinaryEffective += row.unitOrdinaryEffective;
            summary.unitOrdinaryTotal += row.unitOrdinaryTotal;
        });

        const count = this.state.data.length;
        summary.appointmentRate = count > 0 ? (summary.appointmentSum / count).toFixed(2) : 0;
        summary.examRate = count > 0 ? (summary.examSum / count).toFixed(2) : 0;
        summary.efficiency = count > 0 ? (summary.efficiencySum / count) : 0;

        return summary;
    }

    /**
     * 渲染分页
     */
    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        // 上一页
        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="reportController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        // 页码
        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);

        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="reportController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="reportController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="reportController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        // 下一页
        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="reportController.goToPage(${current + 1}); return false;">></a>
        </li>`;

        document.getElementById('pagination').innerHTML = html;
    }

    /**
     * 更新分页信息
     */
    updatePageInfo() {
        document.getElementById('pageInfo').textContent = `${this.state.pageSize}条/页 共${this.state.total}条`;
    }

    /**
     * 跳转到指定页
     */
    goToPage(page) {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        if (page < 1 || page > totalPages) return;
        this.state.currentPage = page;
        this.loadData();
    }

    /**
     * 跳转到输入的页码
     */
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
 * 导出当前页面展示的数据和表头样式
 */
function exportData() {
    const data = reportController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    // 表头
    const headers = [
        '科室', '门诊量', '预约挂号率', '预约诊察率', '接诊效率', '出诊人次',
        '名医专家', '特需专家', '知名专家', '专家A类', '专家B类', '普通门诊',
        '有效出诊单元/出诊单元', '名医专家', '特需专家', '知名专家', '专家A类', '专家B类', '普通门诊'
    ];

    // 数据行
    const rows = data.map(row => [
        row.deptName,
        row.totalVisits,
        row.appointmentRate,
        row.examRate,
        row.efficiency,
        row.visitCount,
        row.famousExpert,
        row.specialExpert,
        row.knownExpert,
        row.expertA,
        row.expertB,
        row.ordinary,
        `${row.effectiveUnits}/${row.totalUnits}`,
        `${row.unitFamousEffective}/${row.unitFamousTotal}`,
        `${row.unitSpecialEffective}/${row.unitSpecialTotal}`,
        `${row.unitKnownEffective}/${row.unitKnownTotal}`,
        `${row.unitAEffective}/${row.unitATotal}`,
        `${row.unitBEffective}/${row.unitBTotal}`,
        `${row.unitOrdinaryEffective}/${row.unitOrdinaryTotal}`
    ]);

    // 合计行
    const summary = reportController.calculateSummary();
    rows.push([
        '全列表数据合计',
        summary.totalVisits,
        summary.appointmentRate + '%',
        summary.examRate + '%',
        summary.efficiency.toFixed(2),
        summary.visitCount,
        summary.famousExpert,
        summary.specialExpert,
        summary.knownExpert,
        summary.expertA,
        summary.expertB,
        summary.ordinary,
        `${summary.effectiveUnits}/${summary.totalUnits}`,
        `${summary.unitFamousEffective}/${summary.unitFamousTotal}`,
        `${summary.unitSpecialEffective}/${summary.unitSpecialTotal}`,
        `${summary.unitKnownEffective}/${summary.unitKnownTotal}`,
        `${summary.unitAEffective}/${summary.unitATotal}`,
        `${summary.unitBEffective}/${summary.unitBTotal}`,
        `${summary.unitOrdinaryEffective}/${summary.unitOrdinaryTotal}`
    ]);

    // 创建工作簿
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);

    // 设置列宽
    ws['!cols'] = [
        { wch: 16 }, { wch: 10 }, { wch: 14 }, { wch: 14 }, { wch: 12 }, { wch: 10 },
        { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 10 }, { wch: 10 },
        { wch: 20 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }
    ];

    // 设置表头背景色和样式
    const headerColors = [
        'FAFAFA', 'E6F7FF', 'E6F7FF', 'E6F7FF', 'E6F7FF', 'F6FFED',
        'F6FFED', 'F6FFED', 'F6FFED', 'F6FFED', 'F6FFED', 'F6FFED',
        'FFF7E6', 'FFF7E6', 'FFF7E6', 'FFF7E6', 'FFF7E6', 'FFF7E6', 'FFF7E6'
    ];

    const range = XLSX.utils.decode_range(ws['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: 0, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        ws[cellAddress].s = {
            font: { bold: true, sz: 11 },
            fill: { fgColor: { rgb: headerColors[C] || 'FAFAFA' } },
            alignment: { horizontal: 'center', vertical: 'center' },
            border: {
                top: { style: 'thin', color: { rgb: 'D9D9D9' } },
                bottom: { style: 'thin', color: { rgb: 'D9D9D9' } },
                left: { style: 'thin', color: { rgb: 'D9D9D9' } },
                right: { style: 'thin', color: { rgb: 'D9D9D9' } }
            }
        };
    }

    // 数据单元格添加边框
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

    // 合计行加粗
    const lastRow = range.e.r;
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: lastRow, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        if (!ws[cellAddress].s) ws[cellAddress].s = {};
        ws[cellAddress].s.font = { bold: true };
    }

    XLSX.utils.book_append_sheet(wb, ws, '门诊量统计');

    // 导出文件
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `门诊量统计_${dateStr}.xlsx`);
}

/**
 * 跳转到输入的页码（全局函数供HTML调用）
 */
function jumpToPage() {
    reportController.jumpToPage();
}

// 页面加载完成后初始化
const reportController = new ReportController();
