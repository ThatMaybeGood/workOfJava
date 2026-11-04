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
        const list = this.state.list;
        document.getElementById('tableTitle').textContent =
            `医保对账每日对比（${insurTypeName(this.state.insurType)} ${this.state.month}）`;
        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '';
        document.getElementById('tableEmpty').classList.toggle('d-none', list.length > 0);
        document.getElementById('tableFoot').style.display = list.length > 0 ? '' : 'none';

        let sumMiQty = 0, sumMiFee = 0, sumOrgQty = 0, sumOrgFee = 0, hasMi = false, hasOrg = false, mismatchCnt = 0;
        list.forEach(item => {
            const tr = document.createElement('tr');
            if (item.matched === false) {
                tr.className = 'table-danger';
                mismatchCnt++;
            }
            tr.innerHTML = `
                <td>${item.settleDate || '—'}</td>
                <td>${item.insurTypeName || insurTypeName(item.insurType)}</td>
                <td class="text-end">${fmtQty(item.miQty)}</td>
                <td class="text-end">${fmtFee(item.miFee)}</td>
                <td class="text-end">${fmtQty(item.orgQty)}</td>
                <td class="text-end">${fmtFee(item.orgFee)}</td>
                <td class="text-end">${item.diffFee === null || item.diffFee === undefined ? '—' : fmtFee(item.diffFee)}</td>
                <td class="text-center">${item.matched
                    ? '<span class="badge bg-success">对平</span>'
                    : '<span class="badge bg-danger">不平</span>'}</td>`;
            tbody.appendChild(tr);
            if (item.miQty !== null && item.miQty !== undefined) { sumMiQty += Number(item.miQty); hasMi = true; }
            if (item.miFee !== null && item.miFee !== undefined) { sumMiFee += Number(item.miFee); hasMi = true; }
            if (item.orgQty !== null && item.orgQty !== undefined) { sumOrgQty += Number(item.orgQty); hasOrg = true; }
            if (item.orgFee !== null && item.orgFee !== undefined) { sumOrgFee += Number(item.orgFee); hasOrg = true; }
        });

        document.getElementById('sumMiQty').textContent = hasMi ? fmtQty(sumMiQty) : '—';
        document.getElementById('sumMiFee').textContent = hasMi ? fmtFee(sumMiFee) : '—';
        document.getElementById('sumOrgQty').textContent = hasOrg ? fmtQty(sumOrgQty) : '—';
        document.getElementById('sumOrgFee').textContent = hasOrg ? fmtFee(sumOrgFee) : '—';
        document.getElementById('sumDiff').textContent = (hasMi && hasOrg) ? fmtFee(sumMiFee - sumOrgFee) : '—';
        document.getElementById('sumStatus').textContent = mismatchCnt > 0 ? `不平${mismatchCnt}天` : '对平';
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
                this.showUploadMsg(`上传成功：${data.body.fileName}（${data.body.insurTypeName}），共 ${data.body.rowCount} 条明细`, 'success');
                setTimeout(() => bootstrap.Modal.getInstance(document.getElementById('uploadModal')).hide(), 1200);
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
                       ${INSUR_TYPES.map(t => `<option value="${t.code}" ${t.code === item.insurType ? 'selected' : ''}>${t.name}(${t.code})</option>`).join('')}
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
