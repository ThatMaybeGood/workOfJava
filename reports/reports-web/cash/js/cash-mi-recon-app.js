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
        this.recState = { pageNum: 1, pageSize: 10, insurType: '', dataDate: '' };
        this.recTotal = 0;
        this.bindEvents();
        this.initMonthFilter();
        this.initUploadDatePicker();
        this.initRecordsDatePicker();
        this.initResizableModals();
        this.fillPage();
        window.addEventListener('resize', () => this.fillPage());
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
            } else if (btn.classList.contains('enable-btn')) {
                this.enableFile(id);
            } else if (btn.classList.contains('delete-btn')) {
                this.deleteFile(id);
            }
        });
        // 上传记录: 筛选(类型/数据日期)与分页
        document.getElementById('recTypeFilter').addEventListener('change', (e) => {
            this.recState.insurType = e.target.value;
            this.recState.pageNum = 1;
            this.loadRecords();
        });
        document.getElementById('recQueryBtn').addEventListener('click', () => {
            this.recState.pageNum = 1;
            this.loadRecords();
        });
        document.getElementById('recDateClear').addEventListener('click', () => {
            this.recDatePicker.clear();
            this.recState.dataDate = '';
            this.recState.pageNum = 1;
            this.loadRecords();
        });
        document.getElementById('recPrevBtn').addEventListener('click', () => {
            if (this.recState.pageNum > 1) {
                this.recState.pageNum--;
                this.loadRecords();
            }
        });
        document.getElementById('recNextBtn').addEventListener('click', () => {
            this.recState.pageNum++;
            this.loadRecords();
        });
        document.getElementById('recPageSize').addEventListener('change', (e) => {
            this.recState.pageSize = Number(e.target.value);
            this.recState.pageNum = 1;
            this.loadRecords();
        });
        // 点对比表金额弹该日来源类型构成(机构/医保机构), 不平徽章弹账平差异
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
        // 两表差异弹窗列头筛选(数据来源/医保标志/HIS标志), 纯前端过滤已加载数据
        ['zpFilterSide', 'zpFilterPay', 'zpFilterBiz'].forEach(id => {
            document.getElementById(id).addEventListener('change', () => this.refreshZpDiff());
        });
    }

    initMonthFilter() {
        const now = new Date();
        this.state.month = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
        document.getElementById('monthFilter').value = this.state.month;
    }

    /** 本页所有弹窗支持拖拉边界/角调整长宽: 左/右边拉宽, 下边拉高, 右下角同时拉 */
    initResizableModals() {
        document.querySelectorAll('#uploadModal, #recordsModal, #orgDtlModal, #zpDiffModal').forEach(modal => {
            const dialog = modal.querySelector('.modal-dialog');
            const content = modal.querySelector('.modal-content');
            if (!dialog || !content) return;
            ['e', 'w', 's', 'se'].forEach(dir => {
                const handle = document.createElement('div');
                handle.className = 'rz-handle rz-' + dir;
                content.appendChild(handle);
                handle.addEventListener('mousedown', (e) => {
                    e.preventDefault();
                    const startX = e.clientX, startY = e.clientY;
                    const startW = dialog.offsetWidth, startH = content.offsetHeight;
                    const onMove = (ev) => {
                        if (dir !== 's') {
                            // 左/右边拖宽(弹窗居中, 宽度对称伸缩)
                            const dx = dir === 'w' ? startX - ev.clientX : ev.clientX - startX;
                            dialog.style.width = Math.max(380, startW + dx) + 'px';
                        }
                        if (dir !== 'e' && dir !== 'w') {
                            content.style.height = Math.max(260, startH + ev.clientY - startY) + 'px';
                        }
                        content.classList.add('resized');
                    };
                    const onUp = () => {
                        document.removeEventListener('mousemove', onMove);
                        document.removeEventListener('mouseup', onUp);
                    };
                    document.addEventListener('mousemove', onMove);
                    document.addEventListener('mouseup', onUp);
                });
            });
        });
    }

    /** 表格区铺满剩余高度: CSS flex为主, 个别老内核iframe里flex高度失效时JS兜底, 杜绝下方留白 */
    fillPage() {
        const container = document.querySelector('body > .container-fluid');
        const section = container && container.querySelector('.table-section');
        if (!section) return;
        container.style.position = 'relative';
        const remain = container.clientHeight - section.offsetTop - 8;
        if (remain > 200) {
            section.style.height = remain + 'px';
        }
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

    /** 上传记录弹窗的数据日期筛选 */
    initRecordsDatePicker() {
        this.recDatePicker = flatpickr(document.getElementById('recDateFilter'), {
            dateFormat: 'Y/m/d',
            locale: 'zh',
            allowInput: false,
            onChange: (dates) => {
                this.recState.dataDate = dates[0] ? this.formatDate(dates[0]) : '';
            }
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
                       title="点击查看该日医保机构来源类型构成">${fmtFee(item.miOrgFee)}</button>`
                : '—';
            const orgBadge = item.matched === false
                ? `<button class="badge bg-danger border-0 zp-diff-btn" data-date="${item.settleDate}"
                       title="点击查看两表差异明细(含单边/金额不等)">不平</button>`
                : '<span class="badge bg-success">对平</span>';
            const miBadge = item.miOrgMatched === false
                ? `<button class="badge bg-danger border-0 zp-diff-btn" data-date="${item.settleDate}"
                       title="点击查看两表差异明细(含单边/金额不等)">不平</button>`
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

    /** 单日明细弹窗: 该日按来源类型展示(side=org医疗机构/miOrg医保机构); 机构侧附带未匹配负交易 */
    openOrgDtl(settleDate, side = 'org') {
        const item = this.state.list.find(r => r.settleDate === settleDate);
        if (!item) return;
        const isMi = side === 'miOrg';
        const typeName = item.insurTypeName || insurTypeName(item.insurType);
        this.renderCatModal(
            `${isMi ? '医保机构' : '医疗机构'}明细（${item.settleDate} ${typeName}）`,
            (isMi ? item.miOrgDtl : item.orgDtl) || [],
            c => c.insurTypeName || typeName);
        document.getElementById('orgUnmatchedWrap').classList.add('d-none');
        if (!isMi) {
            this.loadUnmatchedNeg(settleDate);
        }
    }

    /** 未匹配负交易: 机构侧内连接找不到对应正交易的退费, 展示在医疗机构明细弹窗底部(不影响对平状态) */
    async loadUnmatchedNeg(settleDate) {
        let list = [];
        try {
            const body = await ReportAPI.miRecon({
                action: 'unmatchedNeg',
                insurType: this.state.insurType,
                settleDate: settleDate
            });
            list = body.list || [];
        } catch (error) {
            console.error('Load unmatched neg failed:', error);
            return;
        }
        if (list.length === 0) {
            return;
        }
        const tbody = document.getElementById('orgUnmatchedBody');
        tbody.innerHTML = '';
        let sumFee = 0, hasFee = false;
        list.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${c.setlId || '—'}</td>
                <td class="text-end">${fmtFee(c.medfeeSumamt)}</td>`;
            tbody.appendChild(tr);
            if (c.medfeeSumamt !== null && c.medfeeSumamt !== undefined) { sumFee += Number(c.medfeeSumamt); hasFee = true; }
        });
        document.getElementById('orgUnmatchedCnt').textContent = fmtQty(list.length) + ' 条';
        document.getElementById('orgUnmatchedSumFee').textContent = hasFee ? fmtFee(sumFee) : '—';
        document.getElementById('orgUnmatchedWrap').classList.remove('d-none');
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
            `${isMi ? '医保机构' : '医疗机构'}来源类型分摊（${this.state.month} ${insurTypeName(this.state.insurType)}）`,
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

    /** 自冲销行配对重排: 按就诊号分组、组内按金额绝对值降序两两配对(互反金额), 配上的两行紧挨排一起并编号 */
    pairOffsetRows(list) {
        const used = new Set();
        const ordered = [];
        let pairNo = 0;
        list.forEach(c => {
            if (used.has(c)) return;
            if (!c.offsetPair) {
                ordered.push(c);
                used.add(c);
                return;
            }
            // 同就诊号、金额互反且未配对的行作为对端
            const partner = list.find(x => x !== c && !used.has(x) && x.offsetPair
                && x.pairKey === c.pairKey
                && Number(x.payFee) === -Number(c.payFee));
            if (partner) {
                pairNo++;
                used.add(c);
                used.add(partner);
                // 正金额在前, 负(退费)在后
                const first = Number(c.payFee) >= 0 ? c : partner;
                const second = first === c ? partner : c;
                first._pairNo = pairNo; first._pairFirst = true;
                second._pairNo = pairNo; second._pairFirst = false;
                ordered.push(first, second);
            } else {
                ordered.push(c);
                used.add(c);
            }
        });
        return ordered;
    }

    /** 两表差异弹窗: HIS业务表 vs 医保支付表按setl_id全量比对(单边存在+账平标志不一致), 不限账平标志 */
    async openZpDiff(settleDate) {
        document.getElementById('zpDiffTitle').textContent =
            `两表差异明细（${settleDate} ${insurTypeName(this.state.insurType)}）`;
        document.getElementById('zpDiffBody').innerHTML = '';
        document.getElementById('zpDiffEmpty').classList.add('d-none');
        document.getElementById('zpDiffTotals').innerHTML = '';
        this.zpDiffList = [];
        try {
            const body = await ReportAPI.miRecon({
                action: 'zpDiff',
                insurType: this.state.insurType,
                settleDate: settleDate
            });
            this.zpDiffList = body.list || [];
            this.renderZpTotals(body.totals || []);
        } catch (error) {
            console.error('Load zp diff failed:', error);
        }
        this.populateZpFilters();
        this.refreshZpDiff();
    }

    /** 差异表列头筛选下拉(数据来源/医保标志/HIS标志): 选项从本次数据动态生成 */
    populateZpFilters() {
        const fill = (id, values) => {
            const sel = document.getElementById(id);
            const prev = sel.value;
            sel.innerHTML = '<option value="">全部</option>'
                + values.map(v => `<option value="${v}">${v}</option>`).join('');
            sel.value = values.includes(prev) ? prev : '';
        };
        const distinct = fn => [...new Set(this.zpDiffList.map(fn)
            .filter(v => v !== null && v !== undefined && v !== ''))];
        fill('zpFilterSide', distinct(c => c.diffSide));
        fill('zpFilterPay', distinct(c => c.payZpName));
        fill('zpFilterBiz', distinct(c => c.bizZpName));
    }

    /** 按列头筛选条件重渲染差异表(纯前端, 所见即所得) */
    refreshZpDiff() {
        const side = document.getElementById('zpFilterSide').value;
        const pay = document.getElementById('zpFilterPay').value;
        const biz = document.getElementById('zpFilterBiz').value;
        const full = this.zpDiffList || [];
        const list = full.filter(c => (!side || c.diffSide === side)
            && (!pay || c.payZpName === pay)
            && (!biz || c.bizZpName === biz));
        const emptyEl = document.getElementById('zpDiffEmpty');
        emptyEl.textContent = full.length === 0 ? '两表一致，无差异' : '无符合筛选条件的差异';
        emptyEl.classList.toggle('d-none', list.length > 0);
        const tbody = document.getElementById('zpDiffBody');
        tbody.innerHTML = '';
        let sumPayFee = 0, sumBizFee = 0, hasPayFee = false, hasBizFee = false;
        const cntOf = s => list.filter(c => c.diffSide === s).length;
        const offsetRows = list.filter(c => c.offsetPair).length;
        // 自冲销配对: 按就诊号分组、组内按金额绝对值降序两两配对(金额互反), 配好对的两行紧挨排一起
        const ordered = this.pairOffsetRows(list);
        ordered.forEach(c => {
            const tr = document.createElement('tr');
            // 关联列: 一对的首行用rowspan=2的大括号跨两行, 非配对行留空
            const bracketCell = c._pairNo
                ? (c._pairFirst
                    ? `<td rowspan="2" class="pair-cell"><span class="pair-bracket">[</span></td>`
                    : '')
                : '<td class="pair-cell"></td>';
            const offsetCell = c.offsetPair
                ? `<span class="badge bg-info">自相冲销${c._pairNo ? ' 对' + c._pairNo : ''}</span>`
                : '—';
            tr.innerHTML = `${bracketCell}
                <td>${c.setlId || '—'}</td>
                <td>${c.insurTypeName || insurTypeName(c.insurType)}</td>
                <td>${c.diffSide || '—'}</td>
                <td>${offsetCell}</td>
                <td>${c.payZpName || '—'}</td>
                <td>${c.bizZpName || '—'}</td>
                <td class="text-end">${c.payFee === null || c.payFee === undefined ? '—' : fmtFee(c.payFee)}</td>
                <td class="text-end">${c.bizFee === null || c.bizFee === undefined ? '—' : fmtFee(c.bizFee)}</td>`;
            tbody.appendChild(tr);
            if (c.payFee !== null && c.payFee !== undefined) { sumPayFee += Number(c.payFee); hasPayFee = true; }
            if (c.bizFee !== null && c.bizFee !== undefined) { sumBizFee += Number(c.bizFee); hasBizFee = true; }
        });
        document.getElementById('zpDiffCnt').textContent = fmtQty(list.length) + ' 条';
        document.getElementById('zpDiffSumPayFee').textContent = hasPayFee ? fmtFee(sumPayFee) : '—';
        document.getElementById('zpDiffSumBizFee').textContent = hasBizFee ? fmtFee(sumBizFee) : '—';
        document.getElementById('zpDiffSummary').textContent = list.length === 0 ? ''
            : `仅医保支付表 ${fmtQty(cntOf('仅医保支付表'))} 条，仅HIS业务表 ${fmtQty(cntOf('仅HIS业务表'))} 条，账平标志不一致 ${fmtQty(cntOf('账平标志不一致'))} 条`
                + (offsetRows > 0 ? `；其中医保自对冲 ${fmtQty(offsetRows / 2)} 对（${fmtQty(offsetRows)} 条，金额自相抵消，非真实差异）` : '');
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
        await this.loadRecords();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('recordsModal')).show();
    }

    /** 上传记录分页加载: 类型/数据日期筛选 + 页码, 同类型后端排一起 */
    async loadRecords() {
        try {
            const body = await ReportAPI.miRecon({
                action: 'records',
                insurType: this.recState.insurType,
                dataDate: this.recState.dataDate,
                pageNum: this.recState.pageNum,
                pageSize: this.recState.pageSize
            });
            this.records = body.list || [];
            this.recTotal = body.total || 0;
        } catch (error) {
            console.error('Load mi recon records failed:', error);
            this.records = [];
            this.recTotal = 0;
        }
        this.renderRecords();
        this.updateRecPager();
    }

    /** 上传记录分页条: 总条数/页码/翻页按钮状态 */
    updateRecPager() {
        const pages = Math.max(1, Math.ceil(this.recTotal / this.recState.pageSize));
        if (this.recState.pageNum > pages) {
            this.recState.pageNum = pages;
        }
        document.getElementById('recTotal').textContent = `共 ${fmtQty(this.recTotal)} 条`;
        document.getElementById('recPageInfo').textContent = `第 ${this.recState.pageNum} / ${pages} 页`;
        document.getElementById('recPrevBtn').disabled = this.recState.pageNum <= 1;
        document.getElementById('recNextBtn').disabled = this.recState.pageNum >= pages;
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
                : `<button class="btn btn-outline-primary btn-sm edit-type-btn" data-id="${item.id}"><i class="bi bi-pencil me-1"></i>改类型</button>
                   ${item.status === '0' ? `<button class="btn btn-outline-success btn-sm enable-btn" data-id="${item.id}" title="启用后该文件数据用于对比，同类型冲突日期的启用文件自动作废">启用</button>` : ''}
                   <button class="btn btn-outline-danger btn-sm delete-btn" data-id="${item.id}" title="删除该记录及其明细数据，不可恢复">删除</button>`;
            const statusCell = item.status === '1'
                ? '<span class="badge bg-success">启用</span>'
                : '<span class="badge bg-secondary">已作废</span>';
            tr.innerHTML = `
                <td>${item.dataDate || '—'}</td>
                <td>${typeCell}</td>
                <td class="filename-cell" title="${item.fileName || ''}">${item.fileName || '—'}</td>
                <td>${fmtQty(item.rowCount)}</td>
                <td>${item.createTime || '—'}</td>
                <td>${statusCell}</td>
                <td>${opCell}</td>`;
            tbody.appendChild(tr);
        });
    }

    /** 启用历史上传记录: 其明细用于对比, 同类型冲突日期的启用文件自动作废 */
    async enableFile(id) {
        if (!confirm('启用后该文件数据将用于对比，同类型冲突日期的其他启用文件会自动作废，确定启用？')) {
            return;
        }
        try {
            await ReportAPI.miRecon({ action: 'enableFile', id });
            await this.loadRecords();
            this.load();
        } catch (error) {
            console.error('Enable mi recon file failed:', error);
            alert('启用失败：' + error.message);
        }
    }

    /** 删除上传记录及其明细(物理删除不可恢复; 删除启用记录后对应日期将无标准对账数据) */
    async deleteFile(id) {
        if (!confirm('删除后不可恢复！该记录的明细数据将一并删除；若删除的是"启用"记录，对应日期的标准对账数据将缺失，确定删除？')) {
            return;
        }
        try {
            await ReportAPI.miRecon({ action: 'deleteFile', id });
            await this.loadRecords();
            this.load();
        } catch (error) {
            console.error('Delete mi recon file failed:', error);
            alert('删除失败：' + error.message);
        }
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
            await this.loadRecords();
            this.load();
        } catch (error) {
            console.error('Update mi recon type failed:', error);
            alert('类型修改失败：' + error.message);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => new MiReconApp());
