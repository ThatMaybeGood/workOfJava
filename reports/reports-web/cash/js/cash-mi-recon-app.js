/**
 * 医保对账每日对比
 * 类型下拉单类型查看, 医保上传数据 vs 医疗机构数据按结算日期双边对比, 金额不等整行标红
 * 上传走 multipart 独立接口(API_CONFIG.miReconUploadUrl), 不走JSON网关
 */

const INSUR_TYPES = [
    { code: '310', name: '职工' },
    { code: '390', name: '居民' },
    { code: '3101', name: '异地职工' },
    { code: '3901', name: '异地居民' }
];

function insurTypeName(code) {
    const t = INSUR_TYPES.find(t => t.code === code);
    return t ? t.name : (code || '');
}

function fmtNum(v, decimals = 0) {
    if (v === null || v === undefined || v === '') {
        return '—';
    }
    const n = Number(v);
    if (isNaN(n)) {
        return '—';
    }
    return n.toLocaleString('zh-CN', { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
}

function fmtFee(v) {
    return fmtNum(v, 2);
}

function fmtQty(v) {
    return fmtNum(v, 0);
}

class MiReconApp {
    constructor() {
        this.state = {
            insurType: '310',
            month: null,
            status: '',
            list: []
        };
        this.records = [];
        this.editingId = null;
        this.bindEvents();
        this.initMonthFilter();
        this.initUploadDatePicker();
        this.load();
    }

    bindEvents() {
        document.getElementById('typeFilter').addEventListener('change', (e) => {
            this.state.insurType = e.target.value;
            this.load();
        });
        document.getElementById('monthFilter').addEventListener('change', (e) => {
            if (e.target.value) {
                this.state.month = e.target.value;
                this.load();
            }
        });
        // 状态筛选只过滤已加载数据, 不重查
        document.getElementById('statusFilter').addEventListener('change', (e) => {
            this.state.status = e.target.value;
            this.renderBody();
        });
        document.getElementById('queryBtn').addEventListener('click', () => this.load());
        document.getElementById('uploadBtn').addEventListener('click', () => this.openUploadModal());
        document.getElementById('recordsBtn').addEventListener('click', () => this.openRecordsModal());
        document.getElementById('uploadSubmit').addEventListener('click', () => this.submitUpload());
        document.getElementById('recordsBody').addEventListener('click', (e) => {
            const btn = e.target.closest('button');
            if (!btn) return;
            const id = Number(btn.dataset.id);
            if (btn.classList.contains('edit-type-btn')) {
                this.editingId = id;
                this.renderRecords();
            } else if (btn.classList.contains('save-type-btn')) {
                this.saveType(id);
            } else if (btn.classList.contains('cancel-type-btn')) {
                this.editingId = null;
                this.renderRecords();
            }
        });
        // 点对比表金额弹该日来源类型构成(机构/医保数据), 不平徽章弹账平差异
        document.getElementById('tableBody').addEventListener('click', (e) => {
            const btn = e.target.closest('button');
            if (!btn) return;
            if (btn.classList.contains('org-dtl-btn')) {
                this.openOrgDtl(btn.dataset.date, 'org');
            } else if (btn.classList.contains('mi-org-dtl-btn')) {
                this.openOrgDtl(btn.dataset.date, 'miOrg');
            } else if (btn.classList.contains('zp-diff-btn')) {
                this.openZpDiff(btn.dataset.date);
            }
        });
        // 合计行点击金额弹整月来源类型分摊
        document.getElementById('footOrgFeeBtn').addEventListener('click', () => this.openCatMonth('org'));
        document.getElementById('footMiOrgFeeBtn').addEventListener('click', () => this.openCatMonth('miOrg'));
    }

    initMonthFilter() {
        const now = new Date();
        this.state.month = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
        document.getElementById('monthFilter').value = this.state.month;
    }

    initUploadDatePicker() {
        const today = new Date();
        this.uploadPicker = flatpickr(document.getElementById('uploadDate'), {
            dateFormat: 'Y/m/d',
            defaultDate: [this.formatDate(today, '/')],
            locale: 'zh',
            allowInput: false
        });
    }

    formatDate(d, sep = '-') {
        return d.getFullYear() + sep + String(d.getMonth() + 1).padStart(2, '0') + sep + String(d.getDate()).padStart(2, '0');
    }

    async load() {
        try {
            const body = await ReportAPI.miRecon({
                action: 'compare',
                insurType: this.state.insurType,
                month: this.state.month
            });
            this.state.list = body.list || [];
            this.renderBody();
        } catch (error) {
            console.error('Load mi recon failed:', error);
            document.getElementById('tableTitle').textContent = '医保对账每日对比（加载失败，请强刷页面 Ctrl+F5）';
            this.state.list = [];
            this.renderBody();
        }
    }

    renderBody() {
        // 状态筛选(全部/对平/不平), 合计跟随筛选结果——所见即所得; 任一侧不平都算不平
        const isUnmatched = item => !(item.matched === true && item.miOrgMatched === true);
        const all = this.state.list;
        const list = all.filter(item => !this.state.status
            || (this.state.status === 'matched' ? !isUnmatched(item) : isUnmatched(item)));
        const mismatchTotal = all.filter(isUnmatched).length;
        document.getElementById('tableTitle').textContent =
            `医保对账每日对比（${insurTypeName(this.state.insurType)} ${this.state.month}，共 ${list.length} 天${mismatchTotal > 0 ? '，不平 ' + mismatchTotal + ' 天' : ''}）`;
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        document.getElementById('tableEmpty').classList.toggle('d-none', list.length > 0);
        document.getElementById('tableFoot').style.display = list.length > 0 ? '' : 'none';

        let sumMiQty = 0, sumMiFee = 0, sumOrgQty = 0, sumOrgFee = 0,
            sumMiOrgQty = 0, sumMiOrgFee = 0,
            hasMi = false, hasOrg = false, hasMiOrg = false, mismatchOrgCnt = 0, mismatchMiCnt = 0;
        list.forEach(item => {
            const tr = document.createElement('tr');
            if (isUnmatched(item)) {
                tr.className = 'table-danger';
            }
            if (item.matched === false) mismatchOrgCnt++;
            if (item.miOrgMatched === false) mismatchMiCnt++;
            const hasOrgFee = item.orgFee !== null && item.orgFee !== undefined;
            const hasMiOrgFee = item.miOrgFee !== null && item.miOrgFee !== undefined;
            // 点绿色金额弹该日来源类型构成
            const orgFeeCell = hasOrgFee
                ? `<button class="btn btn-sm p-0 org-dtl-btn" data-date="${item.settleDate}"
                       title="点击查看该日医疗机构来源类型构成">${fmtFee(item.orgFee)}</button>`
                : '—';
            const miOrgFeeCell = hasMiOrgFee
                ? `<button class="btn btn-sm p-0 mi-org-dtl-btn" data-date="${item.settleDate}"
                       title="点击查看该日医保数据来源类型构成">${fmtFee(item.miOrgFee)}</button>`
                : '—';
            const orgBadge = item.matched === false
                ? `<button class="badge bg-danger border-0 zp-diff-btn" data-date="${item.settleDate}"
                       title="点击查看HIS业务表与医保支付表账平差异">不平</button>`
                : '<span class="badge bg-success">对平</span>';
            const miBadge = item.miOrgMatched === false
                ? `<button class="badge bg-danger border-0 zp-diff-btn" data-date="${item.settleDate}"
                       title="点击查看HIS业务表与医保支付表账平差异">不平</button>`
                : '<span class="badge bg-success">对平</span>';
            tr.innerHTML = `
                <td>${item.settleDate || '—'}</td>
                <td>${item.insurTypeName || insurTypeName(item.insurType)}</td>
                <td class="text-end">${fmtQty(item.miQty)}</td>
                <td class="text-end">${fmtFee(item.miFee)}</td>
                <td class="text-end">${fmtQty(item.orgQty)}</td>
                <td class="text-end">${orgFeeCell}</td>
                <td class="text-end">${item.diffFee === null || item.diffFee === undefined ? '—' : fmtFee(item.diffFee)}</td>
                <td class="text-end">${fmtQty(item.miOrgQty)}</td>
                <td class="text-end">${miOrgFeeCell}</td>
                <td class="text-end">${item.miOrgDiffFee === null || item.miOrgDiffFee === undefined ? '—' : fmtFee(item.miOrgDiffFee)}</td>
                <td class="text-center"><div class="d-flex flex-column align-items-center gap-1">
                    <span>机构 ${orgBadge}</span><span>医保 ${miBadge}</span></div></td>`;
            tbody.appendChild(tr);
            if (item.miQty !== null && item.miQty !== undefined) { sumMiQty += Number(item.miQty); hasMi = true; }
            if (item.miFee !== null && item.miFee !== undefined) { sumMiFee += Number(item.miFee); hasMi = true; }
            if (item.orgQty !== null && item.orgQty !== undefined) { sumOrgQty += Number(item.orgQty); hasOrg = true; }
            if (item.orgFee !== null && item.orgFee !== undefined) { sumOrgFee += Number(item.orgFee); hasOrg = true; }
            if (item.miOrgQty !== null && item.miOrgQty !== undefined) { sumMiOrgQty += Number(item.miOrgQty); hasMiOrg = true; }
            if (item.miOrgFee !== null && item.miOrgFee !== undefined) { sumMiOrgFee += Number(item.miOrgFee); hasMiOrg = true; }
        });

        document.getElementById('sumMiQty').textContent = hasMi ? fmtQty(sumMiQty) : '—';
        document.getElementById('sumMiFee').textContent = hasMi ? fmtFee(sumMiFee) : '—';
        document.getElementById('sumOrgQty').textContent = hasOrg ? fmtQty(sumOrgQty) : '—';
        document.getElementById('footOrgFeeBtn').textContent = hasOrg ? fmtFee(sumOrgFee) : '—';
        document.getElementById('sumDiff').textContent = (hasMi && hasOrg) ? fmtFee(sumMiFee - sumOrgFee) : '—';
        document.getElementById('sumMiOrgQty').textContent = hasMiOrg ? fmtQty(sumMiOrgQty) : '—';
        document.getElementById('footMiOrgFeeBtn').textContent = hasMiOrg ? fmtFee(sumMiOrgFee) : '—';
        document.getElementById('sumMiOrgDiff').textContent = (hasMi && hasMiOrg) ? fmtFee(sumMiFee - sumMiOrgFee) : '—';
        const parts = [];
        if (mismatchOrgCnt > 0) parts.push(`机构不平${mismatchOrgCnt}天`);
        if (mismatchMiCnt > 0) parts.push(`医保不平${mismatchMiCnt}天`);
        document.getElementById('sumStatus').textContent = parts.length > 0 ? parts.join(' ') : '对平';
    }

    /** 单日明细弹窗: 该日按来源类型展示(side=org医疗机构/miOrg医保数据) */
    openOrgDtl(settleDate, side = 'org') {
        const item = this.state.list.find(r => r.settleDate === settleDate);
        if (!item) return;
        const isMi = side === 'miOrg';
        const typeName = item.insurTypeName || insurTypeName(item.insurType);
        this.renderCatModal(
            `${isMi ? '医保数据' : '医疗机构'}明细（${item.settleDate} ${typeName}）`,
            (isMi ? item.miOrgDtl : item.orgDtl) || [],
            c => c.insurTypeName || typeName);
    }

    /** 整月来源类型分摊弹窗: 合计行点绿色金额触发, 当月每天明细按来源类型加总 */
    openCatMonth(side = 'org') {
        const isMi = side === 'miOrg';
        const agg = new Map();
        this.state.list.forEach(item => {
            const dtl = (isMi ? item.miOrgDtl : item.orgDtl) || [];
            dtl.forEach(c => {
                const key = c.sourceTypeName || c.sourceType || '';
                const a = agg.get(key) || { name: key, qty: 0, fee: 0 };
                if (c.qty !== null && c.qty !== undefined) a.qty += Number(c.qty);
                if (c.fee !== null && c.fee !== undefined) a.fee += Number(c.fee);
                agg.set(key, a);
            });
        });
        // 来源类型固定顺序: 账平/负金额块在前, 未知排后
        const order = ['账平', '医保负金额-his正交易', '医保负金额-医保正交易', '医保负金额', '医保正金额'];
        const rows = [...agg.values()].sort((a, b) => {
            const ia = order.indexOf(a.name), ib = order.indexOf(b.name);
            return (ia === -1 ? 99 : ia) - (ib === -1 ? 99 : ib) || a.name.localeCompare(b.name, 'zh');
        });
        this.renderCatModal(
            `${isMi ? '医保数据' : '医疗机构'}来源类型分摊（${this.state.month} ${insurTypeName(this.state.insurType)}）`,
            rows, () => insurTypeName(this.state.insurType));
    }

    /** 来源类型构成弹窗公共渲染: rows=[{name/sourceTypeName, qty, fee}], typeNameOf取类型列显示 */
    renderCatModal(title, rows, typeNameOf) {
        document.getElementById('orgDtlTitle').textContent = title;
        const tbody = document.getElementById('orgDtlBody');
        tbody.innerHTML = '';
        document.getElementById('orgDtlEmpty').classList.toggle('d-none', rows.length > 0);
        let sumQty = 0, sumFee = 0, hasQty = false, hasFee = false;
        rows.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${c.name || c.sourceTypeName || c.sourceType || '—'}</td>
                <td>${typeNameOf(c)}</td>
                <td class="text-end">${c.qty === null || c.qty === undefined ? '—' : fmtQty(c.qty)}</td>
                <td class="text-end">${c.fee === null || c.fee === undefined ? '—' : fmtFee(c.fee)}</td>`;
            tbody.appendChild(tr);
            if (c.qty !== null && c.qty !== undefined) { sumQty += Number(c.qty); hasQty = true; }
            if (c.fee !== null && c.fee !== undefined) { sumFee += Number(c.fee); hasFee = true; }
        });
        document.getElementById('orgDtlSumQty').textContent = hasQty ? fmtQty(sumQty) : '—';
        document.getElementById('orgDtlSumFee').textContent = hasFee ? fmtFee(sumFee) : '—';
        bootstrap.Modal.getOrCreateInstance(document.getElementById('orgDtlModal')).show();
    }

    /** 两表全量对照渲染(不限账平): 各表总条数/总金额 + 医保-医院差值, 条数差大金额差小=正负自对冲 */
    renderZpTotals(totals) {
        const t = {};
        totals.forEach(x => { t[x.tableName] = x; });
        const his = t['HIS业务表'], mi = t['医保支付表'];
        const cell = (name, x) => `<div class="border rounded px-2 py-1">${name}：
            <b>${fmtQty(x.qty)}</b> 条 / <b>${fmtFee(x.fee)}</b> 元</div>`;
        let html = '';
        if (his) html += cell('HIS业务表', his);
        if (mi) html += cell('医保支付表', mi);
        if (his && mi) {
            const dq = Number(mi.qty || 0) - Number(his.qty || 0);
            const df = Number(mi.fee || 0) - Number(his.fee || 0);
            html += `<div class="border rounded px-2 py-1 text-muted">医保-医院：
                条数${dq > 0 ? '多 ' + fmtQty(dq) : dq < 0 ? '少 ' + fmtQty(-dq) : '相同'}，
                金额差 ${fmtFee(df)} 元</div>`;
        }
        document.getElementById('zpDiffTotals').innerHTML = html;
    }

    /** 账平差异弹窗: HIS业务表ips_dz_biz_bills vs 医保支付表ips_dz_pay_bills, 双边账平按setl_id比对 */
    async openZpDiff(settleDate) {
        document.getElementById('zpDiffTitle').textContent =
            `账平差异明细（${settleDate} ${insurTypeName(this.state.insurType)}）`;
        const tbody = document.getElementById('zpDiffBody');
        tbody.innerHTML = '';
        document.getElementById('zpDiffEmpty').classList.add('d-none');
        document.getElementById('zpDiffTotals').innerHTML = '';
        let list = [];
        try {
            const body = await ReportAPI.miRecon({
                action: 'zpDiff',
                insurType: this.state.insurType,
                settleDate: settleDate
            });
            list = body.list || [];
            this.renderZpTotals(body.totals || []);
        } catch (error) {
            console.error('Load zp diff failed:', error);
        }
        document.getElementById('zpDiffEmpty').classList.toggle('d-none', list.length > 0);
        let sumFee = 0, hasFee = false, payCnt = 0, bizCnt = 0;
        list.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${c.setlId || '—'}</td>
                <td>${c.insurTypeName || insurTypeName(c.insurType)}</td>
                <td>${c.diffSide || '—'}</td>
                <td class="text-end">${fmtFee(c.medfeeSumamt)}</td>`;
            tbody.appendChild(tr);
            if (c.medfeeSumamt !== null && c.medfeeSumamt !== undefined) { sumFee += Number(c.medfeeSumamt); hasFee = true; }
            if (c.diffSide === '医保支付表') payCnt++;
            if (c.diffSide === 'HIS业务表') bizCnt++;
        });
        document.getElementById('zpDiffCnt').textContent = fmtQty(list.length) + ' 条';
        document.getElementById('zpDiffSumFee').textContent = hasFee ? fmtFee(sumFee) : '—';
        document.getElementById('zpDiffSummary').textContent = list.length === 0 ? ''
            : `医保支付表多出 ${fmtQty(payCnt)} 条，HIS业务表多出 ${fmtQty(bizCnt)} 条（来源类型=账平仅存在的一方）`;
        bootstrap.Modal.getOrCreateInstance(document.getElementById('zpDiffModal')).show();
    }

    openUploadModal() {
        this.showUploadMsg('', '');
        document.getElementById('uploadFile').value = '';
        bootstrap.Modal.getOrCreateInstance(document.getElementById('uploadModal')).show();
    }

    showUploadMsg(text, kind) {
        const msg = document.getElementById('uploadMsg');
        msg.className = 'small ' + (kind === 'error' ? 'text-danger' : kind === 'success' ? 'text-success' : 'd-none');
        msg.textContent = text;
    }

    async submitUpload() {
        const fileInput = document.getElementById('uploadFile');
        const insurType = document.getElementById('uploadType').value;
        const dataDateRaw = this.uploadPicker.selectedDates[0];
        if (!fileInput.files || fileInput.files.length === 0) {
            this.showUploadMsg('请选择要上传的对账文件', 'error');
            return;
        }
        if (!dataDateRaw) {
            this.showUploadMsg('请选择数据日期', 'error');
            return;
        }
        if (API_CONFIG.useMock) {
            this.showUploadMsg('Mock 模式下不支持上传，请切换到接口模式', 'error');
            return;
        }
        const form = new FormData();
        form.append('file', fileInput.files[0]);
        form.append('insurType', insurType);
        form.append('dataDate', this.formatDate(dataDateRaw));
        const btn = document.getElementById('uploadSubmit');
        btn.disabled = true;
        this.showUploadMsg('上传解析中...', 'success');
        try {
            const resp = await fetch(API_CONFIG.miReconUploadUrl, { method: 'POST', body: form });
            const data = await resp.json();
            if (data.result && data.result.code === '10000' && data.result.success) {
                // 不自动关闭弹窗, 成功信息留给用户确认, 手动点关闭
                this.showUploadMsg(`上传成功：${data.body.fileName}（${data.body.insurTypeName}），共 ${data.body.rowCount} 条明细`, 'success');
                this.load();
            } else {
                this.showUploadMsg('上传失败：' + (data.result ? (data.result.sub_msg || data.result.msg) : '未知错误'), 'error');
            }
        } catch (error) {
            console.error('Upload mi recon file failed:', error);
            this.showUploadMsg('上传失败：' + (error.message || '网络异常'), 'error');
        } finally {
            btn.disabled = false;
        }
    }

    async openRecordsModal() {
        this.editingId = null;
        try {
            const body = await ReportAPI.miRecon({ action: 'records' });
            this.records = body.list || [];
        } catch (error) {
            console.error('Load mi recon records failed:', error);
            this.records = [];
        }
        this.renderRecords();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('recordsModal')).show();
    }

    renderRecords() {
        const tbody = document.getElementById('recordsBody');
        tbody.innerHTML = '';
        document.getElementById('recordsEmpty').classList.toggle('d-none', this.records.length > 0);
        this.records.forEach(item => {
            const tr = document.createElement('tr');
            const editing = this.editingId === item.id;
            const typeCell = editing
                ? `<select class="form-select form-select-sm edit-type-select" data-id="${item.id}">
                       ${INSUR_TYPES.map(t => `<option value="${t.code}" ${t.code === item.insurType ? 'selected' : ''}>${t.name}</option>`).join('')}
                   </select>`
                : `<span class="badge bg-light text-dark border">${item.insurTypeName || insurTypeName(item.insurType)}</span>`;
            const opCell = editing
                ? `<button class="btn btn-success btn-sm save-type-btn" data-id="${item.id}">保存</button>
                   <button class="btn btn-outline-secondary btn-sm cancel-type-btn" data-id="${item.id}">取消</button>`
                : `<button class="btn btn-outline-primary btn-sm edit-type-btn" data-id="${item.id}"><i class="bi bi-pencil me-1"></i>改类型</button>`;
            tr.innerHTML = `
                <td title="${item.fileName || ''}">${item.fileName || '—'}</td>
                <td>${typeCell}</td>
                <td>${item.dataDate || '—'}</td>
                <td>${fmtQty(item.rowCount)}</td>
                <td>${item.createTime || '—'}</td>
                <td>${opCell}</td>`;
            tbody.appendChild(tr);
        });
    }

    async saveType(id) {
        const select = document.querySelector(`select.edit-type-select[data-id="${id}"]`);
        if (!select) return;
        const record = this.records.find(r => r.id === id);
        const oldType = record ? record.insurType : '';
        if (select.value === oldType) {
            this.editingId = null;
            this.renderRecords();
            return;
        }
        try {
            await ReportAPI.miRecon({ action: 'updateType', id, insurType: select.value });
            this.editingId = null;
            if (record) {
                record.insurType = select.value;
                record.insurTypeName = insurTypeName(select.value);
            }
            this.renderRecords();
            this.load();
        } catch (error) {
            console.error('Update mi recon type failed:', error);
            alert('类型修改失败：' + error.message);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => new MiReconApp());
