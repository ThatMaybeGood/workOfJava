/**
 * 医保对账每日对比
 * 类型下拉单类型查看, 医保上传数据 vs 医疗机构数据按结算日期双边对比, 金额不等整行标红
 * 上传走 multipart 独立接口(API_CONFIG.miReconUploadUrl), 不走JSON网关
 */

const INSUR_TYPES = [
    { code: '310', name: '职工' },
    { code: '390', name: '居民' },
    { code: '3101', name: '异地职工' },
    { code: '3901', name: '异地居民' },
    { code: '515253', name: '生育' }
];

/** 支付构成10个金额列(0=key对应接口字段, 1=合计行td的id后缀) */
const PAY_COLS = [
    ['medfeeSumamt', 'Medfee'], ['acctPay', 'Acct'], ['hlfpPay', 'Hlfp'],
    ['cashPay', 'Cash'], ['cvlservPay', 'Cvlser'], ['hlfesPay', 'Hlfes'],
    ['hlfmiPay', 'Hlfmi'], ['hlfobPay', 'Hlfob'], ['mafPay', 'Maf'], ['othPay', 'Oth']
];

function insurTypeName(code) {
    const t = INSUR_TYPES.find(t => t.code === code);
    return t ? t.name : (code || '');
}

/** 险种统一显示: 职工(310) */
function typeLabel(code, name) {
    return `${name || insurTypeName(code)}(${code || '—'})`;
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
        // 支付构成
        this.payState = { month: null, insurType: '', sumList: [], dayList: [], crossRows: [] };
        this.payCtx = { insurType: '', insurTypeName: '' };
        this.bindEvents();
        this.initMonthFilter();
        this.initUploadDatePicker();
        this.initRecordsDatePicker();
        this.initResizableModals();
        this.fillPage();
        window.addEventListener('resize', () => this.fillPage());
        this.load();
        // 地址加 #pay 直达支付构成Tab
        if (location.hash === '#pay') {
            document.querySelector('#mainTabBar .tab[data-pane="panePay"]').click();
        }
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
        // 主Tab切换: 每日对比 / 支付构成
        document.getElementById('mainTabBar').addEventListener('click', (e) => {
            const tab = e.target.closest('.tab');
            if (!tab) return;
            document.querySelectorAll('#mainTabBar .tab').forEach(t => t.classList.toggle('active', t === tab));
            document.getElementById('paneDaily').style.display = tab.dataset.pane === 'paneDaily' ? '' : 'none';
            document.getElementById('panePay').style.display = tab.dataset.pane === 'panePay' ? '' : 'none';
            this.fillPage();
            if (tab.dataset.pane === 'panePay') this.loadPaySum();
        });
        // 支付构成筛选
        document.getElementById('payTypeFilter').addEventListener('change', (e) => {
            this.payState.insurType = e.target.value;
            this.loadPaySum();
        });
        document.getElementById('payQueryBtn').addEventListener('click', () => this.loadPaySum());
        document.getElementById('payMonthFilter').addEventListener('change', (e) => {
            if (e.target.value) {
                this.payState.month = e.target.value;
                this.loadPaySum();
            }
        });
        // 支付构成主表/每日维度行尾按钮(分类明细/每日维度/当日分类) + 分类弹窗子Tab
        document.getElementById('paySumBody').addEventListener('click', (e) => this.payRowBtn(e, 'cat'));
        document.getElementById('payDayBody').addEventListener('click', (e) => this.payRowBtn(e, 'dayCat'));
        document.querySelectorAll('#payCatModal .sub-tab-bar .tab').forEach(t => {
            t.addEventListener('click', () => this.switchPaySub(Number(t.dataset.sub)));
        });
        // 每日维度弹窗内下钻: 返回每日维度列表层
        document.getElementById('payDayBackBtn').addEventListener('click', () => {
            this.showPayDayPane('list');
            document.getElementById('payDayTitle').textContent =
                `${this.payCtx.insurTypeName} 每日维度（${this.payState.month}）`;
        });
    }

    initMonthFilter() {
        const now = new Date();
        this.state.month = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
        document.getElementById('monthFilter').value = this.state.month;
    }

    /** 本页所有弹窗支持拖拉边界/角调整长宽: 左/右边拉宽, 下边拉高, 右下角同时拉 */
    initResizableModals() {
        document.querySelectorAll('#uploadModal, #recordsModal, #orgDtlModal, #zpDiffModal, #payCatModal, #payDayModal').forEach(modal => {
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

    /** 表格区铺满剩余高度: CSS flex为主, 个别老内核iframe里flex高度失效时JS兜底, 杜绝下方留白; 当前可见Tab的表格区生效 */
    fillPage() {
        const container = document.querySelector('body > .container-fluid');
        if (!container) return;
        container.style.position = 'relative';
        const daily = document.getElementById('paneDaily');
        const pay = document.getElementById('panePay');
        const pane = (pay && pay.style.display !== 'none') ? pay : daily;
        const section = pane && pane.querySelector(':scope > .table-section');
        if (!section) return;
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
                <td>${typeLabel(item.insurType, item.insurTypeName)}</td>
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
                const a = agg.get(key) || { name: key, qty: 0, fee: 0, excluded: !!c.excluded };
                if (c.excluded) a.excluded = true;
                if (c.qty !== null && c.qty !== undefined) a.qty += Number(c.qty);
                if (c.fee !== null && c.fee !== undefined) a.fee += Number(c.fee);
                agg.set(key, a);
            });
        });
        // 来源类型固定顺序: 账平/负金额块在前, 未知排后
        const order = ['账平', '医保负金额-his正交易', '医保负金额-医保正交易', '医保负金额', '医保正金额', '金额不等'];
        const rows = [...agg.values()].sort((a, b) => {
            const ia = order.indexOf(a.name), ib = order.indexOf(b.name);
            return (ia === -1 ? 99 : ia) - (ib === -1 ? 99 : ib) || a.name.localeCompare(b.name, 'zh');
        });
        this.renderCatModal(
            `${isMi ? '医保机构' : '医疗机构'}来源类型分摊（${this.state.month} ${insurTypeName(this.state.insurType)}）`,
            rows, () => insurTypeName(this.state.insurType));
    }

    /** 来源类型构成弹窗公共渲染: rows=[{name/sourceTypeName, qty, fee, excluded}], excluded行带'不计入合计'徽章且合计跳过 */
    renderCatModal(title, rows, typeNameOf) {
        document.getElementById('orgDtlTitle').textContent = title;
        const tbody = document.getElementById('orgDtlBody');
        tbody.innerHTML = '';
        document.getElementById('orgDtlEmpty').classList.toggle('d-none', rows.length > 0);
        let sumQty = 0, sumFee = 0, hasQty = false, hasFee = false;
        rows.forEach(c => {
            const tr = document.createElement('tr');
            const exclBadge = c.excluded ? ' <span class="badge bg-secondary">不计入合计</span>' : '';
            tr.innerHTML = `
                <td>${c.name || c.sourceTypeName || c.sourceType || '—'}${exclBadge}</td>
                <td>${typeNameOf(c)}</td>
                <td class="text-end">${c.qty === null || c.qty === undefined ? '—' : fmtQty(c.qty)}</td>
                <td class="text-end">${c.fee === null || c.fee === undefined ? '—' : fmtFee(c.fee)}</td>`;
            tbody.appendChild(tr);
            if (c.excluded) return;
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

    /* ==================== 支付构成 Tab ==================== */

    /** 金额单元格: 负数标红 */
    payFeeTd(v) {
        if (v === null || v === undefined) return '<td class="text-end">—</td>';
        const cls = Number(v) < 0 ? 'text-end text-danger' : 'text-end';
        return `<td class="${cls}">${fmtFee(v)}</td>`;
    }

    /** 底部合计: 条数+10金额列写入 foot, footId 控制显隐 */
    renderPayFoot(footId, prefix, rows) {
        document.getElementById(footId).style.display = rows.length > 0 ? '' : 'none';
        let qty = 0;
        const sums = {};
        let any = false;
        rows.forEach(r => {
            if (r.qty !== null && r.qty !== undefined) { qty += Number(r.qty); any = true; }
            PAY_COLS.forEach(([k]) => {
                if (r[k] !== null && r[k] !== undefined) { sums[k] = (sums[k] || 0) + Number(r[k]); any = true; }
            });
        });
        document.getElementById(prefix + 'Qty').textContent = any ? fmtQty(qty) : '—';
        PAY_COLS.forEach(([k, suffix]) => {
            document.getElementById(prefix + suffix).textContent = sums[k] !== undefined ? fmtFee(sums[k]) : '—';
        });
    }

    /** 主表: 险种×月合计(账平-退费+收费) */
    async loadPaySum() {
        if (!this.payState.month) {
            const now = new Date();
            this.payState.month = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
            document.getElementById('payMonthFilter').value = this.payState.month;
        }
        document.getElementById('payTypeFilter').value = this.payState.insurType;
        let rows = [];
        let warnRows = [];
        try {
            const [sumBody, warnBody] = await Promise.all([
                ReportAPI.miRecon({
                    action: 'payComp', dim: 'sum',
                    insurType: this.payState.insurType, month: this.payState.month
                }),
                ReportAPI.miRecon({
                    action: 'payComp', dim: 'warn',
                    insurType: this.payState.insurType, month: this.payState.month
                })
            ]);
            rows = sumBody.list || [];
            warnRows = warnBody.list || [];
        } catch (error) {
            console.error('Load pay comp sum failed:', error);
        }
        this.payState.sumList = rows;
        const warnMap = {};
        warnRows.forEach(w => { warnMap[w.insurType] = w; });
        document.getElementById('payTableTitle').textContent =
            `支付构成总统计（${this.payState.month}${this.payState.insurType ? ' ' + insurTypeName(this.payState.insurType) : ' 全部类型'}）`;
        const tbody = document.getElementById('paySumBody');
        tbody.innerHTML = '';
        document.getElementById('paySumEmpty').classList.toggle('d-none', rows.length > 0);
        rows.forEach(r => {
            const tr = document.createElement('tr');
            const w = warnMap[r.insurType];
            const warnCell = this.renderPayWarnCell(w, r.insurType, r.insurTypeName);
            tr.innerHTML = `
                <td>${typeLabel(r.insurType, r.insurTypeName)}</td>
                <td class="text-end">${fmtQty(r.qty)}</td>
                ${PAY_COLS.map(([k]) => this.payFeeTd(r[k])).join('')}
                <td class="small">${warnCell}</td>
                <td class="pay-op-cell"><button class="btn btn-outline-primary btn-sm pay-cat-btn" data-code="${r.insurType}" data-name="${r.insurTypeName || insurTypeName(r.insurType)}">分类明细</button>
                    <button class="btn btn-outline-secondary btn-sm pay-day-btn" data-code="${r.insurType}" data-name="${r.insurTypeName || insurTypeName(r.insurType)}">每日维度</button></td>`;
            tbody.appendChild(tr);
        });
        // 警示列"查看明细"直达跨月Tab
        tbody.querySelectorAll('button.pay-warn-btn').forEach(b => b.addEventListener('click', () => {
            this.openPayCat(b.dataset.code, b.dataset.name).then(() => this.switchPaySub(2));
        }));
        this.renderPayFoot('paySumFoot', 'paySum', rows);
    }

    /** 主表退费匹配警示单元格(简约徽章即按钮): 无退费(灰)/全匹配(绿)/跨月N(黄)、找不到N(红), 点徽章直达跨月明细 */
    renderPayWarnCell(w, code, name) {
        if (!w || !w.refundTotal) {
            return '<span class="text-muted">无退费</span>';
        }
        if (!w.crossCnt && !w.noneCnt) {
            return '<span class="badge bg-success">全匹配</span>';
        }
        const parts = [];
        if (w.crossCnt) {
            parts.push(`<button class="badge bg-warning text-dark border-0 pay-warn-btn" data-code="${code}" data-name="${name}" title="点击查看跨月配对明细">跨月${w.crossCnt}</button>`);
        }
        if (w.noneCnt) {
            parts.push(`<button class="badge bg-danger border-0 pay-warn-btn" data-code="${code}" data-name="${name}" title="点击查看找不到明细">找不到${w.noneCnt}</button>`);
        }
        return parts.join('<br>');
    }

    /** 行尾按钮委托: kind=cat(主表→月分类弹窗) / dayCat(每日表→当日分类弹窗) */
    payRowBtn(e, kind) {
        const btn = e.target.closest('button');
        if (!btn) return;
        if (btn.classList.contains('pay-cat-btn')) {
            this.openPayCat(btn.dataset.code, btn.dataset.name);
        } else if (btn.classList.contains('pay-day-btn')) {
            this.openPayDay(btn.dataset.code, btn.dataset.name);
        } else if (btn.classList.contains('pay-daycat-btn')) {
            this.openPayDayCat(btn.dataset.code, btn.dataset.name, btn.dataset.date);
        }
    }

    /** 分类明细弹窗子Tab切换 */
    switchPaySub(n) {
        document.getElementById('paySubTab1').classList.toggle('active', n === 1);
        document.getElementById('paySubTab2').classList.toggle('active', n === 2);
        document.getElementById('paySubPane1').style.display = n === 1 ? '' : 'none';
        document.getElementById('paySubPane2').style.display = n === 2 ? '' : 'none';
    }

    /** 月分类弹窗: 本月匹配Tab(三类各行) + 跨月匹配Tab(退费逐条配对) */
    async openPayCat(code, name) {
        this.payCtx = { insurType: code, insurTypeName: name };
        document.getElementById('payCatTitle').textContent = `${name} 分类明细（${this.payState.month}）`;
        let catRows = [], crossRows = [];
        try {
            const [catBody, crossBody] = await Promise.all([
                ReportAPI.miRecon({ action: 'payComp', dim: 'cat', insurType: code, month: this.payState.month }),
                ReportAPI.miRecon({ action: 'payComp', dim: 'cross', insurType: code, month: this.payState.month })
            ]);
            catRows = catBody.list || [];
            crossRows = crossBody.list || [];
        } catch (error) {
            console.error('Load pay comp cat failed:', error);
        }
        this.payState.crossRows = crossRows;
        this.renderPayCat(catRows, crossRows);
        this.renderPayCross(crossRows);
        this.switchPaySub(1);
        bootstrap.Modal.getOrCreateInstance(document.getElementById('payCatModal')).show();
    }

    /** 分类汇总Tab: 账平/退费/收费三类各行; 退费行显示完整匹配breakdown(退费含跨月匹配口径) */
    renderPayCat(rows, crossRows) {
        const totalRefund = crossRows.length;
        const thisMonth = crossRows.filter(c => c.matchStatus === '本月匹配').length;
        const crossCnt = crossRows.filter(c => c.matchStatus === '跨月匹配').length;
        const noneCnt = crossRows.filter(c => c.matchStatus === '找不到').length;
        const tbody = document.getElementById('payCatBody');
        tbody.innerHTML = '';
        document.getElementById('payCatEmpty').classList.toggle('d-none', rows.length > 0);
        rows.forEach(r => {
            const tr = document.createElement('tr');
            let matchCell = '<span class="small text-muted">—</span>';
            if (r.category === '医保负金额-退费') {
                matchCell = totalRefund === 0
                    ? '<span class="small text-muted">当月无退费</span>'
                    : `单边退费${fmtQty(totalRefund)}条：本月匹配${fmtQty(thisMonth)} / 跨月${fmtQty(crossCnt)}`
                        + (noneCnt > 0 ? ` / <span class="text-danger fw-bold">找不到${fmtQty(noneCnt)}</span>` : '')
                        + (noneCnt > 0 || crossCnt > 0
                            ? `<br><button class="btn btn-link btn-sm p-0" data-sub-link="2">查看逐条配对 →</button>` : '');
                if (noneCnt > 0) tr.className = 'table-warning';
            }
            tr.innerHTML = `
                <td><span class="badge bg-light text-dark border">${r.category || '—'}</span></td>
                <td class="text-end">${fmtQty(r.qty)}</td>
                ${PAY_COLS.map(([k]) => this.payFeeTd(r[k])).join('')}
                <td class="small">${matchCell}</td>`;
            tbody.appendChild(tr);
        });
        // 跨月查找链接(动态生成)切子Tab
        tbody.querySelectorAll('button[data-sub-link]').forEach(b =>
            b.addEventListener('click', () => this.switchPaySub(Number(b.dataset.subLink))));
        this.renderPayFoot('payCatFoot', 'payCat', rows);
    }

    /** 跨月匹配Tab: 退费↔正交易逐条配对, 找不到红底 */
    renderPayCross(rows) {
        const cnt = s => rows.filter(r => r.matchStatus === s).length;
        const crossCnt = cnt('跨月匹配'), noneCnt = cnt('找不到');
        const monthDist = {};
        rows.filter(r => r.matchStatus === '跨月匹配').forEach(r => {
            monthDist[r.posMonth] = (monthDist[r.posMonth] || 0) + 1;
        });
        const distText = Object.entries(monthDist).map(([m, c]) => `${m} ${c}条`).join(' / ');
        document.getElementById('payCrossAlert').innerHTML =
            `退费共 <b>${fmtQty(rows.length)}</b> 条：本月匹配 <b>${fmtQty(cnt('本月匹配'))}</b>，跨月 <b>${fmtQty(crossCnt)}</b>`
            + (distText ? `（${distText}）` : '')
            + (noneCnt > 0 ? `，<span class="text-danger fw-bold">找不到 ${fmtQty(noneCnt)} 条</span>` : '')
            + ' · 查找范围：上月月初 ~ 退费当天（正交易日期≤退费日期，仅查询不改数据）';
        const tbody = document.getElementById('payCrossBody');
        tbody.innerHTML = '';
        document.getElementById('payCrossEmpty').classList.toggle('d-none', rows.length > 0);
        let sumFee = 0, hasFee = false;
        rows.forEach(r => {
            const tr = document.createElement('tr');
            if (r.matchStatus === '跨月匹配') tr.className = 'pay-cross';
            if (r.matchStatus === '找不到') tr.className = 'pay-none';
            const badge = r.matchStatus === '本月匹配' ? '<span class="badge bg-success">本月匹配</span>'
                : r.matchStatus === '跨月匹配' ? `<span class="badge bg-warning text-dark">跨月匹配</span>`
                : '<span class="badge bg-danger">找不到</span>';
            const fmtDay = d => (d && d.length >= 10) ? d.slice(5) : (d || '—');
            tr.innerHTML = `
                <td>${r.refundSetlId || '—'}</td>
                <td>${fmtDay(r.refundDate)}</td>
                <td class="text-end ${Number(r.refundFee) < 0 ? 'text-danger' : ''}">${r.refundFee === null || r.refundFee === undefined ? '—' : fmtFee(r.refundFee)}</td>
                <td class="text-center">${badge}</td>
                <td class="text-end">${r.posFee === null || r.posFee === undefined ? '—' : fmtFee(r.posFee)}</td>
                <td>${fmtDay(r.posDate)}</td>
                <td>${r.posSetlId || '—'}</td>
                <td class="small text-muted">${r.matchStatus === '跨月匹配' ? '本月未匹配，跨月找到' : r.matchStatus === '找不到' ? '上月月初至退费当天均无对应正交易' : '正常'}</td>`;
            tbody.appendChild(tr);
            if (r.refundFee !== null && r.refundFee !== undefined) { sumFee += Number(r.refundFee); hasFee = true; }
        });
        document.getElementById('payCrossCnt').textContent = fmtQty(rows.length) + ' 条退费';
        document.getElementById('payCrossSumFee').textContent = hasFee ? fmtFee(sumFee) : '—';
        document.getElementById('payCrossSummary').textContent =
            `本月${cnt('本月匹配')} / 跨月${crossCnt}` + (noneCnt > 0 ? ` / 找不到${noneCnt}` : '');
    }

    /** 每日维度弹窗: 每天一行总数, 行尾[分类明细]展开当天三类 */
    async openPayDay(code, name) {
        this.payCtx = { insurType: code, insurTypeName: name };
        document.getElementById('payDayTitle').textContent = `${name} 每日维度（${this.payState.month}）`;
        let rows = [], crossRows = [];
        try {
            const [dayBody, crossBody] = await Promise.all([
                ReportAPI.miRecon({ action: 'payComp', dim: 'day', insurType: code, month: this.payState.month }),
                ReportAPI.miRecon({ action: 'payComp', dim: 'cross', insurType: code, month: this.payState.month })
            ]);
            rows = dayBody.list || [];
            crossRows = crossBody.list || [];
        } catch (error) {
            console.error('Load pay comp day failed:', error);
        }
        this.payState.dayList = rows;
        this.payState.crossRows = crossRows;
        const tbody = document.getElementById('payDayBody');
        tbody.innerHTML = '';
        document.getElementById('payDayEmpty').classList.toggle('d-none', rows.length > 0);
        rows.forEach(r => {
            const tr = document.createElement('tr');
            const dateStr = r.settleDate || '';
            tr.innerHTML = `
                <td>${dateStr.slice(5) || '—'}</td>
                <td class="text-end">${fmtQty(r.qty)}</td>
                ${PAY_COLS.map(([k]) => this.payFeeTd(r[k])).join('')}
                <td><button class="btn btn-outline-primary btn-sm pay-daycat-btn" data-code="${code}" data-name="${name}" data-date="${dateStr}">分类明细</button></td>`;
            tbody.appendChild(tr);
        });
        this.renderPayFoot('payDayFoot', 'payDay', rows);
        this.showPayDayPane('list');
        bootstrap.Modal.getOrCreateInstance(document.getElementById('payDayModal')).show();
    }

    /** 每日维度弹窗内切换: list=每天一行总数层, cat=当日分类下钻层(不叠弹窗) */
    showPayDayPane(which) {
        document.getElementById('payDayPaneList').style.display = which === 'list' ? '' : 'none';
        document.getElementById('payDayPaneCat').style.display = which === 'cat' ? '' : 'none';
        document.getElementById('payDayBackBtn').classList.toggle('d-none', which !== 'cat');
    }

    /** 当日分类下钻: 同一弹窗内切换内容(标题变+返回按钮), 不叠第二层弹窗 */
    async openPayDayCat(code, name, date) {
        document.getElementById('payDayTitle').textContent = `${name} ${date} 当日分类明细`;
        let dayCatRows = [];
        try {
            const body = await ReportAPI.miRecon({ action: 'payComp', dim: 'dayCat', insurType: code, month: this.payState.month });
            dayCatRows = (body.list || []).filter(r => r.settleDate === date);
        } catch (error) {
            console.error('Load pay comp dayCat failed:', error);
        }
        const dayCross = (this.payState.crossRows || []).filter(r => r.refundDate === date);
        const tbody = document.getElementById('payDayCatBody');
        tbody.innerHTML = '';
        document.getElementById('payDayCatEmpty').classList.toggle('d-none', dayCatRows.length > 0);
        dayCatRows.forEach(r => {
            const tr = document.createElement('tr');
            let matchCell = '<span class="small text-muted">—</span>';
            if (r.category === '医保负金额-退费') {
                if (dayCross.length === 0) {
                    matchCell = '<span class="small text-muted">当日无退费</span>';
                } else {
                    matchCell = dayCross.map(c => {
                        if (c.matchStatus === '跨月匹配') {
                            return `<span class="badge bg-warning text-dark">跨月(${c.posMonth})</span> <span class="small">正交易${c.posSetlId || '—'}</span>`;
                        }
                        if (c.matchStatus === '找不到') return '<span class="badge bg-danger">找不到</span>';
                        return '<span class="badge bg-success">本月匹配</span>';
                    }).join('<br>');
                }
                if (dayCross.some(c => c.matchStatus !== '本月匹配')) tr.className = 'table-warning';
            }
            tr.innerHTML = `
                <td>${date.slice(5) || '—'}</td>
                <td><span class="badge bg-light text-dark border">${r.category || '—'}</span></td>
                <td class="text-end">${fmtQty(r.qty)}</td>
                ${PAY_COLS.map(([k]) => this.payFeeTd(r[k])).join('')}
                <td class="small">${matchCell}</td>`;
            tbody.appendChild(tr);
        });
        this.renderPayFoot('payDayCatFoot', 'payDayCat', dayCatRows);
        this.showPayDayPane('cat');
    }
}

document.addEventListener('DOMContentLoaded', () => new MiReconApp());
