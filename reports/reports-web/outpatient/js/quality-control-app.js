/**
 * 门诊质量控制报表页面主逻辑
 */

/**
 * 默认统计月份区间：往前 12 个月 ~ 当月。
 * 原先写死成 2025-01 ~ 2025-12，是过期的固定区间，页面一打开就查不到数据。
 * 返回 {startDate, endDate}，格式 yyyy-MM。
 */
function defaultMonthRange() {
    const fmt = (d) => d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0');
    const end = new Date();
    const start = new Date(end.getFullYear(), end.getMonth() - 12, 1);
    return { startDate: fmt(start), endDate: fmt(end) };
}

class QualityControlController {
    constructor() {
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            overview: {},
            filter: defaultMonthRange()
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
        this.initMonthRangePicker();
        this.bindEvents();
        this.loadData();
    }

    /** 初始化月份范围选择面板 */
    initMonthRangePicker() {
        this.monthRange = {
            start: this.state.filter.startDate,
            end: this.state.filter.endDate
        };
        const input = document.getElementById('monthRange');
        const panel = document.getElementById('monthRangePanel');

        input.addEventListener('click', (e) => {
            e.stopPropagation();
            const opening = panel.style.display === 'none';
            panel.style.display = opening ? 'block' : 'none';
            if (opening) {
                this.renderMonthRangePanel();
            }
        });
        panel.addEventListener('click', (e) => e.stopPropagation());
        document.addEventListener('click', () => {
            panel.style.display = 'none';
        });

        panel.querySelectorAll('.mrp-nav-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const target = btn.dataset.target;
                const dir = parseInt(btn.dataset.dir, 10);
                const [y, m] = this.monthRange[target].split('-').map(Number);
                this.monthRange[target] = `${y + dir}-${String(m).padStart(2, '0')}`;
                this.renderMonthRangePanel();
            });
        });

        document.getElementById('mrpCancel').addEventListener('click', () => {
            panel.style.display = 'none';
        });
        document.getElementById('mrpConfirm').addEventListener('click', () => {
            input.value = `${this.monthRange.start} ~ ${this.monthRange.end}`;
            panel.style.display = 'none';
            input.dispatchEvent(new Event('change'));
        });
    }

    renderMonthRangePanel() {
        const { start, end } = this.monthRange;
        const [startYear, startMonth] = start.split('-').map(Number);
        const [endYear, endMonth] = end.split('-').map(Number);
        document.getElementById('mrpStartYear').textContent = startYear;
        document.getElementById('mrpEndYear').textContent = endYear;
        this.renderMonthGrid('mrpStartMonths', startYear, startMonth, 'start');
        this.renderMonthGrid('mrpEndMonths', endYear, endMonth, 'end');
    }

    renderMonthGrid(containerId, year, selectedMonth, target) {
        const container = document.getElementById(containerId);
        container.innerHTML = '';
        for (let m = 1; m <= 12; m++) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'mrp-month' + (m === selectedMonth ? ' active' : '');
            btn.textContent = `${m}月`;
            btn.addEventListener('click', () => {
                const value = `${year}-${String(m).padStart(2, '0')}`;
                // 保持 start <= end
                if (target === 'start' && value > this.monthRange.end) {
                    this.monthRange.end = value;
                }
                if (target === 'end' && value < this.monthRange.start) {
                    this.monthRange.start = value;
                }
                this.monthRange[target] = value;
                this.renderMonthRangePanel();
            });
            container.appendChild(btn);
        }
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

        // 导出Word弹窗：打开/切粒度/改月份时按真实数据刷新预览
        const wordModal = document.getElementById('wordModal');
        if (wordModal) {
            wordModal.addEventListener('shown.bs.modal', () => {
                const exportMonth = document.getElementById('exportMonth');
                if (!exportMonth.value) {
                    exportMonth.value = this.state.filter.endDate;
                }
                this.renderWordPreview();
            });
            document.getElementById('exportMonth').addEventListener('change', () => this.renderWordPreview());
            document.querySelectorAll('input[name="exportPeriod"]').forEach(radio => {
                radio.addEventListener('change', () => this.renderWordPreview());
            });
        }

        // 数据维护弹窗：打开/切月份时加载已维护值，保存提交
        const maintainModal = document.getElementById('maintainModal');
        if (maintainModal) {
            maintainModal.addEventListener('shown.bs.modal', () => this.loadMaintain());
            document.getElementById('maintainMonth').addEventListener('change', () => this.loadMaintain());
            document.getElementById('maintainSaveBtn').addEventListener('click', () => this.saveMaintain());
        }
    }

    /** 维护弹窗指标编码 -> 名称（与表 maintainTableBody 的 data-indicator 对应） */
    maintainIndicatorName(code) {
        const found = this.indicatorConfig.find(i => {
            const snake = i.key.replace(/([A-Z])/g, '_$1').toLowerCase();
            return snake === code;
        });
        return found ? found.label : code;
    }

    async loadMaintain() {
        const month = document.getElementById('maintainMonth').value;
        if (!month) return;
        try {
            const body = await ReportAPI.maintainQualityControl({ action: 'query', statMonth: month });
            const values = {};
            (body.list || []).forEach(item => {
                values[item.indicatorCode + ':' + 'numerator'] = item.numerator;
                values[item.indicatorCode + ':' + 'denominator'] = item.denominator;
            });
            document.querySelectorAll('#maintainTableBody input[data-indicator]').forEach(input => {
                const key = input.dataset.indicator + ':' + input.dataset.part;
                input.value = values[key] != null ? values[key] : '';
            });
        } catch (error) {
            console.error('Load maintain failed:', error);
        }
    }

    async saveMaintain() {
        const month = document.getElementById('maintainMonth').value;
        if (!month) return;
        const items = {};
        document.querySelectorAll('#maintainTableBody input[data-indicator]').forEach(input => {
            const code = input.dataset.indicator;
            if (!items[code]) {
                items[code] = { indicatorCode: code, indicatorName: this.maintainIndicatorName(code) };
            }
            const val = parseFloat(input.value);
            items[code][input.dataset.part] = isNaN(val) ? null : val;
        });
        try {
            await ReportAPI.maintainQualityControl({ action: 'save', statMonth: month, list: Object.values(items) });
            alert('保存成功！');
            bootstrap.Modal.getInstance(document.getElementById('maintainModal')).hide();
            this.loadData();
        } catch (error) {
            console.error('Save maintain failed:', error);
            alert('保存失败，请重试');
        }
    }

    // ==================== 导出Word ====================

    /** 导出粒度：month / quarter */
    exportPeriod() {
        const checked = document.querySelector('input[name="exportPeriod"]:checked');
        return checked ? checked.value : 'month';
    }

    /**
     * 导出弹窗对应的统计区间。
     * 按月即该月；按季度取该月所属季度的首末月，交给后端汇总成季度平均值。
     */
    exportRange() {
        const month = document.getElementById('exportMonth').value || this.state.filter.endDate;
        const [year, mon] = month.split('-').map(Number);
        const quarter = Math.floor((mon - 1) / 3) + 1;
        const byMonth = this.exportPeriod() === 'month';
        return {
            month,
            year,
            quarter,
            byMonth,
            startMonth: byMonth ? month : `${year}-${String((quarter - 1) * 3 + 1).padStart(2, '0')}`,
            endMonth: byMonth ? month : `${year}-${String(quarter * 3).padStart(2, '0')}`
        };
    }

    /** 报告期文案：2025年9月 / 2025年第三季度 */
    exportPeriodLabel(range) {
        if (range.byMonth) {
            return `${range.year}年${parseInt(range.month.split('-')[1], 10)}月`;
        }
        return `${range.year}年第${'一二三四'[range.quarter - 1]}季度`;
    }

    /** 取导出区间的概览值（= 区间内各月平均） */
    async fetchOverview(startMonth, endMonth) {
        const body = await ReportAPI.getQualityControlStats({
            page: 1,
            pageSize: 1,
            startMonth: startMonth,
            endMonth: endMonth
        });
        return (body && body.overview) ? body.overview : {};
    }

    async renderWordPreview() {
        const range = this.exportRange();
        const hint = document.getElementById('exportPeriodHint');
        if (hint) {
            hint.textContent = range.byMonth ? '' : `汇总区间：${range.startMonth} ~ ${range.endMonth}`;
        }

        let overview = {};
        try {
            overview = await this.fetchOverview(range.startMonth, range.endMonth);
        } catch (error) {
            console.error('Load word preview failed:', error);
        }

        const hasData = this.indicatorConfig.some(ind => overview[ind.key] != null);
        const button = document.getElementById('exportWordBtn');
        if (button) button.disabled = !hasData;

        document.getElementById('wordPreview').innerHTML = this.wordPreviewHtml(range, overview);
    }

    /** 预览文案与导出的Word完全一致 */
    wordPreviewHtml(range, overview) {
        const label = this.exportPeriodLabel(range);
        if (!this.indicatorConfig.some(ind => overview[ind.key] != null)) {
            return '<p class="text-muted">该区间暂无数据</p>';
        }
        const paragraphs = this.indicatorConfig.map((ind, i) => {
            const value = overview[ind.key] != null ? overview[ind.key] : '-';
            return `<p><strong>（${chineseNumber(i + 1)}）${ind.label}</strong></p>
                <p>${label}我院${ind.label}为${value}。</p>`;
        }).join('');
        return `<h4>门诊质控指标情况</h4>
            <p><strong>一、国家《门诊管理治疗质量控制指标》${label}数据情况</strong></p>
            ${paragraphs}`;
    }

    async loadData() {
        try {
            const body = await ReportAPI.getQualityControlStats({
                page: this.state.currentPage,
                pageSize: this.state.pageSize,
                startMonth: this.state.filter.startDate,
                endMonth: this.state.filter.endDate
            });
            this.state.overview = (body && body.overview) ? body.overview : {};
            this.renderOverview(this.state.overview);
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
            // 整列表(不止当前页)的平均值，与顶部概览同源
            html += `<tr><td>全列表数据平均值</td>`;
            this.indicatorConfig.forEach(ind => {
                const val = this.state.overview[ind.key];
                html += `<td>${val != null ? val : '-'}</td>`;
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

    const overview = qualityControlController.state.overview;
    rows.push(['全列表数据平均值', ...qualityControlController.indicatorConfig.map(ind => {
        const val = overview[ind.key];
        return val != null ? val : '-';
    })]);

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

/** 1 -> 一 …… 10 -> 十，用于（一）（二）序号 */
function chineseNumber(n) {
    const digits = ['一', '二', '三', '四', '五', '六', '七', '八', '九'];
    if (n <= 9) return digits[n - 1];
    if (n === 10) return '十';
    return '十' + digits[n % 10 - 1];
}

function downloadBlob(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

/** 导出Word：按弹窗选的月份/季度取数，生成公文式叙述文档 */
async function exportWord() {
    const controller = qualityControlController;
    const range = controller.exportRange();

    let overview;
    try {
        overview = await controller.fetchOverview(range.startMonth, range.endMonth);
    } catch (error) {
        console.error('Export word failed:', error);
        alert('导出失败，请重试');
        return;
    }
    if (!controller.indicatorConfig.some(ind => overview[ind.key] != null)) {
        alert('暂无数据可导出');
        return;
    }

    const label = controller.exportPeriodLabel(range);
    const children = [
        new docx.Paragraph({
            alignment: docx.AlignmentType.CENTER,
            children: [new docx.TextRun({ text: '门诊质控指标情况', bold: true, size: 32, font: '黑体' })]
        }),
        new docx.Paragraph({
            spacing: { before: 240, after: 120 },
            children: [new docx.TextRun({
                text: `一、国家《门诊管理治疗质量控制指标》${label}数据情况`,
                bold: true, size: 24, font: '黑体'
            })]
        })
    ];
    controller.indicatorConfig.forEach((ind, i) => {
        children.push(new docx.Paragraph({
            spacing: { before: 120 },
            children: [new docx.TextRun({
                text: `（${chineseNumber(i + 1)}）${ind.label}`,
                bold: true, size: 21, font: '黑体'
            })]
        }));
        children.push(new docx.Paragraph({
            indent: { firstLine: 420 },
            children: [new docx.TextRun({
                text: `${label}我院${ind.label}为${overview[ind.key]}。`,
                size: 21, font: '宋体'
            })]
        }));
    });

    const suffix = range.byMonth ? range.month.replace('-', '') : `${range.year}Q${range.quarter}`;
    try {
        const blob = await docx.Packer.toBlob(new docx.Document({ sections: [{ children }] }));
        downloadBlob(blob, `门诊质控指标情况_${suffix}.docx`);
        bootstrap.Modal.getInstance(document.getElementById('wordModal')).hide();
    } catch (error) {
        console.error('Export word failed:', error);
        alert('导出失败，请重试');
    }
}

function jumpToPage() {
    qualityControlController.jumpToPage();
}

const qualityControlController = new QualityControlController();
