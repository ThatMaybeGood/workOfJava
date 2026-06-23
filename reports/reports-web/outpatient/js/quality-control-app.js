/**
 * 门诊质量控制报表页面主逻辑
 */
class QualityControlController {
    constructor() {
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            filter: {
                startDate: '2025-01',
                endDate: '2025-12'
            }
        };

        this.indicatorConfig = [
            { key: 'emrUsageRate', label: '门诊电子病历使用率', formula: '门诊电子病历份数/门诊总人次数' },
            { key: 'standardDiagnosisRate', label: '门诊标准诊断使用率', formula: '使用标准诊断的病历数/门诊病历总数' },
            { key: 'onTimeRate', label: '门诊准时出诊率', formula: '准时出诊单元数/出诊单元总数' },
            { key: 'stopRate', label: '门诊停诊率', formula: '停诊且无替代的单元数/计划门诊单元数' },
            { key: 'chemoRecordRate', label: '门诊化疗病历记录完整率', formula: '完整化疗病例数/化疗病历总数' },
            { key: 'chemoAdverseRate', label: '门诊化疗严重不良反应发生率', formula: '≥3级不良反应患者人次数/门诊化疗总人次数' },
            { key: 'chemoInfusionRate', label: '门诊化疗患者静脉治疗相关不良事件发生率', formula: '不良事件人次数/静脉治疗化疗总人次数' },
            { key: 'criticalValueRate', label: '门诊危急值30分钟内通报完成率', formula: '30分钟内通知的危急值例数/危急值总例数' },
            { key: 'bloodDrawErrorRate', label: '门诊静脉采血相关差错发生率', formula: '采血出差例数/采血总例数' },
            { key: 'surgeryComplicationRate', label: '门诊手术并发症发生率', formula: '并发症例数/门诊手术总例数' },
            { key: 'adverseEventRate', label: '每千门诊诊疗人次不良事件发生率', formula: '门诊不良事件总数/门诊总人次数*1000' }
        ];

        this.init();
    }

    init() {
        this.bindEvents();
        this.loadData();
    }

    bindEvents() {
        document.getElementById('monthRange').addEventListener('change', (e) => {
            const val = e.target.value;
            if (val && val.includes(' ~ ')) {
                const [start, end] = val.split(' ~ ');
                this.state.filter.startDate = start;
                this.state.filter.endDate = end;
                this.state.currentPage = 1;
                this.loadData();
            }
        });

        document.getElementById('pageSizeSelect').addEventListener('change', (e) => {
            this.state.pageSize = parseInt(e.target.value);
            this.state.currentPage = 1;
            this.loadData();
        });
    }

    async loadData() {
        try {
            const body = await ReportAPI.getQualityControlStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                startDate: this.state.filter.startDate,
                endDate: this.state.filter.endDate
            });
            this.renderOverview(body ? body.overview : null);
            this.state.data = (body && body.table && body.table.list) ? body.table.list : [];
            this.state.total = (body && body.table && body.table.total) ? body.table.total : 0;
            this.renderTable();
            this.renderPagination();
            this.updatePageInfo();
        } catch (error) {
            console.error('Load quality control data failed:', error);
        }
    }

    renderOverview(data) {
        const safe = (val) => val != null ? val : '-';
        this.indicatorConfig.forEach(ind => {
            const el = document.getElementById(ind.key);
            if (el) el.textContent = safe(data && data[ind.key]);
        });
    }

    renderTable() {
        const tbody = document.getElementById('tableBody');
        let html = '';

        this.state.data.forEach(row => {
            html += `<tr><td>${row.month}</td>`;
            this.indicatorConfig.forEach(ind => {
                html += `<td>${row[ind.key]}</td>`;
            });
            html += '</tr>';
        });

        if (this.state.data.length > 0) {
            html += `<tr><td>全列表数据合计</td>`;
            this.indicatorConfig.forEach(() => {
                html += `<td>62.2%</td>`;
            });
            html += '</tr>';
        } else {
            html += `<tr><td colspan="${this.indicatorConfig.length + 1}" class="text-center text-muted py-4">暂无数据</td></tr>`;
        }

        tbody.innerHTML = html;
    }

    renderPagination() {
        const totalPages = Math.ceil(this.state.total / this.state.pageSize);
        const current = this.state.currentPage;
        let html = '';

        html += `<li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="qualityControlController.goToPage(${current - 1}); return false;"><</a>
        </li>`;

        const maxVisible = 5;
        let start = Math.max(1, current - Math.floor(maxVisible / 2));
        let end = Math.min(totalPages, start + maxVisible - 1);
        if (end - start + 1 < maxVisible) {
            start = Math.max(1, end - maxVisible + 1);
        }

        if (start > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" onclick="qualityControlController.goToPage(1); return false;">1</a></li>`;
            if (start > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }

        for (let i = start; i <= end; i++) {
            html += `<li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="qualityControlController.goToPage(${i}); return false;">${i}</a>
            </li>`;
        }

        if (end < totalPages) {
            if (end < totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" onclick="qualityControlController.goToPage(${totalPages}); return false;">${totalPages}</a></li>`;
        }

        html += `<li class="page-item ${current === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="qualityControlController.goToPage(${current + 1}); return false;">></a>
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
        this.loadData();
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

function exportExcel() {
    const data = qualityControlController.state.data;
    if (data.length === 0) {
        alert('暂无数据可导出');
        return;
    }

    const headers = ['日期', ...qualityControlController.indicatorConfig.map(ind => ind.label)];
    const rows = data.map(row => [
        row.month,
        ...qualityControlController.indicatorConfig.map(ind => row[ind.key])
    ]);

    rows.push(['全列表数据合计', ...qualityControlController.indicatorConfig.map(() => '62.2%')]);

    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([headers, ...rows]);
    const colWidths = [{ wch: 12 }, ...qualityControlController.indicatorConfig.map(() => ({ wch: 16 }))];
    ws['!cols'] = colWidths;

    const range = XLSX.utils.decode_range(ws['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: 0, c: C });
        if (!ws[cellAddress]) ws[cellAddress] = {};
        ws[cellAddress].s = {
            font: { bold: true, sz: 11 },
            fill: { fgColor: { rgb: 'E6F7FF' } },
            alignment: { horizontal: 'center', vertical: 'center', wrapText: true },
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

    XLSX.utils.book_append_sheet(wb, ws, '门诊质量控制指标');
    const dateStr = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    XLSX.writeFile(wb, `门诊质量控制指标_${dateStr}.xlsx`);
}

function jumpToPage() {
    qualityControlController.jumpToPage();
}

const qualityControlController = new QualityControlController();
