/**
 * ETL 流水线创建向导 — 精简三步：选择来源 → 转换提取 → 映射匹配
 * 导出 window.WizardApp = { render(container) }
 * 依赖：window.etlApi / window.etlStore / window.etlComponents
 * 状态：跨刷新由 etlStore（sessionStorage）恢复，模块态 S 仅页面会话内有效
 *
 * 变化：
 *   1. 第一步改为可搜索、可键盘选择、可分页的下拉列表。
 *   2. 步骤条下方显示当前步骤说明，支持点击已完成的步骤回退。
 *   3. 每一步增加「调试此步」按钮，结果以弹窗展示，不保存任务。
 */
(function () {
    const STEPS = [
        { key: 'source', name: '抽取来源' },
        { key: 'transform', name: '转换提取' },
        { key: 'mapping', name: '映射写入' }
    ];
    const STEPS_DESC = [
        { title: '第一步：抽取来源（来源库）', desc: '来源库 = 数据从哪里抽取。选择一个已配置的 PROC/WS 抽取来源，确定要读取的数据。' },
        { title: '第二步：转换提取', desc: '预览来源出参结构，指定行集合（数组=每条一行），为每个叶子字段设置目标类型、默认值与空值策略。' },
        { title: '第三步：映射写入（数据源）', desc: '数据源 = 目的数据写入的库。选择目标数据源与目标表，建立字段映射，配置写入模式与任务设置。' }
    ];
    const TARGET_TYPES = ['string', 'number', 'date', 'boolean'];
    const NULL_POLICIES = [
        { v: 'null', t: '置空' },
        { v: 'default', t: '用默认值' },
        { v: 'skip', t: '跳过该行' }
    ];
    const MONO = 'font-family:var(--etl-font-mono);';
    const SELECTED_CARD = 'border-color:var(--etl-color-primary);' +
        'box-shadow:0 0 0 2px var(--etl-color-primary-weak);background:var(--etl-color-primary-weak);';

    /* 模块态 */
    const S = {
        container: null,
        token: 0,
        step: 1,
        // 第一步
        sources: null,
        dsMap: {},
        existingId: '',
        pickedSource: null,
        sourceKeyword: '',
        sourcePage: 1,
        sourceSize: 10,
        sourceTotal: 0,
        sourceRecords: [],
        sourceDropdownOpen: false,
        sourceActiveIdx: -1,
        sourceSearchTimer: null,
        // 第二步
        structure: null,
        listPaths: [],
        rowListPath: '',
        transforms: [],
        // 第三步
        targetDsId: '',
        targetTables: [],
        targetTable: '',
        targetColumns: [],
        mappings: [],
        taskCfg: null,
        leafMap: {},
        // 调试缓存
        lastDebug: { extract: null, transform: null, load: null },
        // 编辑模式（#/wizard?edit=任务ID）
        editId: null,
        editLoaded: null
    };

    /* ---------- 小工具 ---------- */
    function esc(s) { return window.etlComponents.esc(s); }
    function toast(m, t) { window.etlComponents.toast(m, t); }
    function el(id) { return document.getElementById(id); }
    function val(id) { const n = el(id); return n ? n.value : ''; }
    function checked(id) { const n = el(id); return n ? n.checked : false; }
    function numOrNull(v) { const n = parseInt(v, 10); return isNaN(n) ? null : n; }
    function alive(t) { return t === S.token && S.container; }
    function parseEditId() {
        const m = (location.hash || '').match(/[?&]edit=(\d+)/);
        return m ? Number(m[1]) : null;
    }

    function normList(data) {
        if (Array.isArray(data)) return data;
        if (data && Array.isArray(data.records)) return data.records;
        return [];
    }
    function normTotal(data, records) {
        if (data && typeof data.total === 'number') return data.total;
        return records.length;
    }
    function colName(c) { return typeof c === 'string' ? c : (c.name || c.columnName || ''); }
    function colType(c) { return typeof c === 'string' ? '' : (c.type || c.dataType || ''); }
    function tblName(t) { return typeof t === 'string' ? t : (t.name || t.tableName || ''); }
    const SYSTEM_TABLES = ['CONSTANTS', 'ENUM_VALUES', 'INDEXES', 'INDEX_COLUMNS',
        'INFORMATION_SCHEMA_CATALOG_NAME', 'IN_DOUBT', 'LOCKS', 'QUERY_STATISTICS',
        'RIGHTS', 'ROLES', 'SESSIONS', 'SESSION_STATE', 'SETTINGS', 'SYNONYMS', 'USERS'];
    function filterTables(list) {
        return normList(list).map(tblName).filter(function (n) {
            return n && SYSTEM_TABLES.indexOf(n.toUpperCase()) < 0;
        });
    }

    function collectLeaves(nodes, out) {
        out = out || [];
        (nodes || []).forEach(function (n) {
            if (n.children && n.children.length) collectLeaves(n.children, out);
            else out.push(n);
        });
        return out;
    }
    function collectListPaths(nodes, out) {
        out = out || [];
        (nodes || []).forEach(function (n) {
            if (n.isList && n.path) out.push(n.path);
            if (n.children) collectListPaths(n.children, out);
        });
        return out;
    }
    function inferTargetType(t) {
        const s = (t || '').toLowerCase();
        if (/int|number|decimal|float|double|numeric/.test(s)) return 'number';
        if (/date|time/.test(s)) return 'date';
        if (/bool/.test(s)) return 'boolean';
        return 'string';
    }
    /** 结构树只展开一层：孙节点收起并以类型位标注子项数 */
    function trimTree(nodes) {
        return (nodes || []).map(function (n) {
            const copy = Object.assign({}, n);
            if (copy.children && copy.children.length) {
                copy.children = copy.children.map(function (c) {
                    const cc = Object.assign({}, c);
                    if (cc.children && cc.children.length) {
                        cc.type = (cc.type ? cc.type + ' · ' : '') + cc.children.length + ' 子项';
                        cc.children = null;
                    }
                    return cc;
                });
            }
            return copy;
        });
    }

    /* ---------- 入口 ---------- */
    function render(container) {
        S.container = container;
        S.token += 1;
        closeDebug();
        const editId = parseEditId();
        if (editId !== null) {
            S.editId = editId;
            if (S.editLoaded !== editId) {
                container.innerHTML = '<div class="etl-card"><div class="etl-empty"><div class="etl-empty-text">加载任务详情…</div></div></div>';
                loadTaskForEdit(editId).then(function () { render(S.container); });
                return;
            }
        } else {
            S.editId = null;
            S.editLoaded = null;
        }
        S.step = window.etlStore.getStep();
        if (S.step > 1 && !window.etlStore.get().sourceId) {
            toast('请先完成第一步：选择抽取来源', 'warn');
            window.etlStore.setStep(1);
            S.step = 1;
        }
        container.innerHTML = window.etlComponents.renderStepper(S.step, STEPS, onStepClick) +
            renderStepDesc(S.step) +
            '<div class="etl-card" id="wiz-body"></div>' +
            '<div class="d-flex justify-content-between align-items-center mt-3" id="wiz-footer"></div>';
        bindStepperClick();
        if (S.step === 1) renderStep1();
        else if (S.step === 2) renderStep2();
        else renderStep3();
    }

    function renderStepDesc(step) {
        const d = STEPS_DESC[step - 1] || { title: '', desc: '' };
        return '<div class="etl-step-desc etl-card" id="wiz-step-desc">' +
            '<div class="etl-step-desc-title">' + esc(d.title) + '</div>' +
            '<div class="etl-step-desc-text">' + esc(d.desc) + '</div>' +
            '</div>';
    }

    function onStepClick(stepIdx) {
        if (stepIdx >= S.step) return;
        const store = window.etlStore.get();
        if (stepIdx === 2 && !store.sourceId) {
            toast('请先选择来源', 'warn');
            return;
        }
        if (stepIdx === 3 && (!store.sourceId || !store.structure)) {
            toast('请先完成转换规则配置', 'warn');
            return;
        }
        window.etlStore.setStep(stepIdx);
        render(S.container);
    }

    function bindStepperClick() {
        const stepper = S.container.querySelector('.etl-stepper');
        if (!stepper) return;
        stepper.addEventListener('click', function (e) {
            const stepEl = e.target.closest('.etl-step');
            if (!stepEl) return;
            const idx = Array.from(stepper.children).indexOf(stepEl) + 1;
            if (idx > 0) onStepClick(idx);
        });
    }

    function setFooter(leftHtml, rightHtml) {
        const f = el('wiz-footer');
        if (f) f.innerHTML =
            '<div class="d-flex gap-2">' + (leftHtml || '') + '</div>' +
            '<div class="d-flex gap-2">' + (rightHtml || '') + '</div>';
    }

    /* ---------- 编辑模式：加载已有任务到向导 ---------- */
    async function loadTaskForEdit(taskId) {
        try {
            const detail = await window.etlApi.get('/task/' + taskId + '/detail');
            const task = detail && detail.task;
            const source = detail && detail.source;
            if (!task) throw new Error('任务不存在');
            window.etlStore.clear();
            const sourceId = source ? source.id : task.sourceId;
            const patch = { sourceId: sourceId, source: source, transforms: [] };
            if (sourceId) {
                try {
                    patch.structure = await window.etlApi.get('/source/structure/' + sourceId);
                } catch (e) { patch.structure = null; }
            }
            const indexCols = (task.queryIndexCols || '').split(',').filter(Boolean);
            patch.mappings = (detail.mappings || []).map(function (m) {
                return {
                    srcField: m.srcField || '',
                    tgtField: m.tgtField || '',
                    defaultValue: m.defaultValue || '',
                    isUpdateCol: !!m.isUpdateCol,
                    isIndexCol: indexCols.indexOf(m.tgtField) >= 0
                };
            });
            patch.task = {
                id: task.id,
                name: task.name || '',
                writeMode: task.writeMode || 'INSERT',
                cron: task.cron || '',
                enabled: task.enabled === 1,
                maxRows: task.maxRows,
                batchSize: task.batchSize,
                incremental: task.incremental === 1,
                incField: task.incField || '',
                incPlaceholder: task.incPlaceholder || '',
                retryCount: task.retryCount != null ? task.retryCount : 0
            };
            window.etlStore.set(patch);
            S.structure = patch.structure;
            S.targetDsId = task.targetDsId != null ? String(task.targetDsId) : '';
            S.targetTable = task.targetTable || '';
            S.targetTables = [];
            S.targetColumns = [];
            S.editLoaded = taskId;
            window.etlStore.setStep(3);
        } catch (e) {
            toast('加载任务详情失败：' + (e && e.message ? e.message : '未知错误'), 'err');
            window.etlStore.clear();
            window.etlStore.setStep(1);
            S.editId = null;
        }
    }

    /* ============================================================
     * 第一步：选择来源（可搜索下拉 + 分页）
     * ============================================================ */
    async function renderStep1() {
        const t = S.token;
        el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载来源库…</div></div>';

        const store = window.etlStore.get();
        if (store.sourceId && store.source) {
            S.existingId = String(store.sourceId);
            S.pickedSource = store.source;
        }

        try {
            await Promise.all([loadSources(false), loadDsMap()]);
        } catch (e) {
            if (!alive(t)) return;
        }
        paintStep1();
    }

    async function loadSources(repaint) {
        const t = S.token;
        try {
            const qs = '?page=' + S.sourcePage + '&size=' + S.sourceSize + '&keyword=' + encodeURIComponent(S.sourceKeyword);
            const rs = await window.etlApi.get('/source/list' + qs);
            if (!alive(t)) return;
            S.sourceRecords = normList(rs);
            S.sourceTotal = normTotal(rs, S.sourceRecords);
        } catch (e) {
            if (!alive(t)) return;
            S.sourceRecords = [];
            S.sourceTotal = 0;
        }
        if (repaint !== false) paintStep1();
    }

    function sourceTypeBadge(type) {
        const t = (type || '?').toUpperCase();
        return '<span class="etl-badge ' + (t === 'PROC' ? 'warn' : 'info') + '">' + esc(t) + '</span>';
    }

    async function loadDsMap() {
        try {
            const rs = await window.etlApi.get('/datasource/list?size=1000');
            const list = Array.isArray(rs) ? rs : (rs && Array.isArray(rs.records) ? rs.records : []);
            const map = {};
            list.forEach(function (d) { map[d.id] = d; });
            S.dsMap = map;
        } catch (e) { /* 数据源名称为可选展示，失败时回退 DS#id */ }
    }

    function dsNameOf(s) {
        const ds = S.dsMap[s.sourceDsId];
        return ds ? esc(ds.name) : (s.sourceDsId ? 'DS#' + esc(s.sourceDsId) : '-');
    }

    function paintStep1() {
        const hasRecords = S.sourceRecords && S.sourceRecords.length;
        const totalPages = Math.max(1, Math.ceil(S.sourceTotal / S.sourceSize));

        let listHtml = '';
        if (hasRecords) {
            listHtml = S.sourceRecords.map(function (s, i) {
                const typeCls = s.type === 'PROC' ? 'type-proc' : 'type-ws';
                const active = i === S.sourceActiveIdx ? ' active' : '';
                const selected = S.existingId === String(s.id);
                return '<div class="wiz-source-option ' + typeCls + active + '" data-idx="' + i + '" data-id="' + s.id + '" role="option" aria-selected="' + selected + '">' +
                    '<div class="wiz-source-option-main">' +
                    '<span class="wiz-source-option-name">' + esc(s.name) + '</span> ' +
                    sourceTypeBadge(s.type) +
                    '</div>' +
                    '<div class="wiz-source-option-meta">' + dsNameOf(s) + ' · ' + esc(window.etlComponents.fmtTime(s.createTime)) + '</div>' +
                    '</div>';
            }).join('');
        } else {
            listHtml = '<div class="wiz-source-option-empty">无匹配来源</div>';
        }

        const pagerHtml = totalPages > 1
            ? '<div class="wiz-source-pager">' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-src-prev" ' + (S.sourcePage <= 1 ? 'disabled' : '') + '><i class="bi bi-chevron-left"></i></button>' +
            '<span class="wiz-source-pageinfo">' + S.sourcePage + ' / ' + totalPages + '（共 ' + S.sourceTotal + ' 条）</span>' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-src-next" ' + (S.sourcePage >= totalPages ? 'disabled' : '') + '><i class="bi bi-chevron-right"></i></button>' +
            '</div>'
            : (S.sourceTotal > 0 ? '<div class="wiz-source-pageinfo">共 ' + S.sourceTotal + ' 条</div>' : '');

        const summaryHtml = S.pickedSource
            ? '<div class="wiz-source-summary">' +
            '<div class="wiz-source-summary-title">已选来源</div>' +
            '<div class="wiz-source-summary-body">' +
            '<div><strong>' + esc(S.pickedSource.name) + '</strong> ' + sourceTypeBadge(S.pickedSource.type) + '</div>' +
            '<div class="wiz-source-summary-meta">数据源：' + dsNameOf(S.pickedSource) + '</div>' +
            '</div></div>'
            : '<div class="wiz-source-summary wiz-source-summary-empty">尚未选择来源</div>';

        el('wiz-body').innerHTML =
            '<h6 class="etl-card-title mb-3">选择抽取来源</h6>' +
            '<div class="wiz-source-search" id="wiz-source-search">' +
            '<div class="wiz-source-input-wrap">' +
            '<i class="bi bi-search wiz-source-search-icon"></i>' +
            '<input type="text" class="form-control" id="wiz-source-input" value="' + esc(S.sourceKeyword) + '"' +
            ' placeholder="搜索来源名称…" autocomplete="off" aria-autocomplete="list" aria-controls="wiz-source-dropdown">' +
            '<button class="wiz-source-clear' + (S.sourceKeyword ? '' : ' hidden') + '" id="wiz-source-clear" type="button"><i class="bi bi-x-lg"></i></button>' +
            '</div>' +
            '<div class="wiz-source-dropdown" id="wiz-source-dropdown" role="listbox" ' + (S.sourceDropdownOpen ? 'style="display:block;"' : '') + '>' +
            listHtml + pagerHtml + '</div>' +
            '</div>' +
            summaryHtml;

        const btnDisabled = !S.existingId ? ' disabled' : '';
        setFooter(
            '',
            '<button class="etl-btn etl-btn-ghost" id="wiz-debug1"><i class="bi bi-bug"></i> 调试此步</button>' +
            '<button class="etl-btn etl-btn-primary" id="wiz-next1"' + btnDisabled + '>下一步 <i class="bi bi-arrow-right"></i></button>'
        );

        bindSourceSearch();

        el('wiz-next1').addEventListener('click', function () {
            if (!S.existingId || !S.pickedSource) { toast('请先选择一个来源', 'warn'); return; }
            window.etlStore.set({ sourceId: S.pickedSource.id, source: S.pickedSource, structure: null, transforms: [], mappings: [], task: null });
            window.etlStore.setStep(2);
            render(S.container);
        });

        el('wiz-debug1').addEventListener('click', debugStep1);
    }

    function bindSourceSearch() {
        const wrap = el('wiz-source-search');
        const input = el('wiz-source-input');
        const dropdown = el('wiz-source-dropdown');
        const clearBtn = el('wiz-source-clear');
        if (!wrap || !input || !dropdown) return;

        function open() { S.sourceDropdownOpen = true; dropdown.style.display = 'block'; }
        function close() { S.sourceDropdownOpen = false; dropdown.style.display = 'none'; S.sourceActiveIdx = -1; }

        input.addEventListener('focus', function () { open(); });
        input.addEventListener('input', function () {
            S.sourceKeyword = input.value.trim();
            S.sourcePage = 1;
            S.sourceActiveIdx = -1;
            if (clearBtn) clearBtn.classList.toggle('hidden', !S.sourceKeyword);
            clearTimeout(S.sourceSearchTimer);
            S.sourceSearchTimer = setTimeout(function () { loadSources(); }, 250);
        });
        if (clearBtn) clearBtn.addEventListener('click', function () {
            input.value = '';
            S.sourceKeyword = '';
            S.sourcePage = 1;
            S.sourceActiveIdx = -1;
            clearBtn.classList.add('hidden');
            loadSources();
            input.focus();
        });
        input.addEventListener('keydown', function (e) {
            if (!S.sourceRecords.length) return;
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                S.sourceActiveIdx = Math.min(S.sourceActiveIdx + 1, S.sourceRecords.length - 1);
                scrollActiveOption();
                paintStep1();
                open();
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                S.sourceActiveIdx = Math.max(S.sourceActiveIdx - 1, 0);
                scrollActiveOption();
                paintStep1();
                open();
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (S.sourceActiveIdx >= 0 && S.sourceRecords[S.sourceActiveIdx]) {
                    selectSource(S.sourceRecords[S.sourceActiveIdx]);
                } else if (S.sourceRecords.length === 1) {
                    selectSource(S.sourceRecords[0]);
                }
            } else if (e.key === 'Escape') {
                close();
            }
        });
        dropdown.addEventListener('click', function (e) {
            const opt = e.target.closest('.wiz-source-option[data-id]');
            if (!opt) return;
            const id = opt.getAttribute('data-id');
            const s = S.sourceRecords.find(function (x) { return String(x.id) === id; });
            if (s) selectSource(s);
        });
        document.addEventListener('click', function (e) {
            if (!wrap.contains(e.target)) close();
        });

        const prev = el('wiz-src-prev');
        const next = el('wiz-src-next');
        if (prev) prev.addEventListener('click', function (e) { e.stopPropagation(); if (S.sourcePage > 1) { S.sourcePage -= 1; loadSources(); } });
        if (next) next.addEventListener('click', function (e) { e.stopPropagation(); const total = Math.ceil(S.sourceTotal / S.sourceSize); if (S.sourcePage < total) { S.sourcePage += 1; loadSources(); } });
    }

    function scrollActiveOption() {
        const dropdown = el('wiz-source-dropdown');
        const active = dropdown && dropdown.querySelector('.wiz-source-option.active');
        if (active && dropdown) dropdown.scrollTop = active.offsetTop - dropdown.offsetTop - dropdown.clientHeight / 2 + active.clientHeight / 2;
    }

    function selectSource(s) {
        S.existingId = String(s.id);
        S.pickedSource = s;
        S.sourceActiveIdx = -1;
        S.sourceDropdownOpen = false;
        paintStep1();
    }

    window.WizardApp = {
        selectSource: function (id) {
            const s = S.sourceRecords.find(function (x) { return String(x.id) === String(id); });
            if (s) selectSource(s);
        }
    };

    /* ============================================================
     * 第二步：转换提取
     * ============================================================ */
    async function renderStep2() {
        const t = S.token;
        const store = window.etlStore.get();
        S.structure = store.structure || null;
        S.transforms = store.transforms || [];
        S.rowListPath = store.rowListPath || '';

        if (!S.structure) {
            el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载来源结构…</div></div>';
            try {
                const data = await window.etlApi.get('/source/structure/' + store.sourceId);
                if (!alive(t)) return;
                S.structure = data;
                window.etlStore.set({ structure: data });
            } catch (e) {
                if (!alive(t)) return;
                el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">结构加载失败，请返回第一步重试</div></div>';
                setFooter('<button class="etl-btn etl-btn-ghost" id="wiz-back2"><i class="bi bi-arrow-left"></i> 上一步</button>', '');
                el('wiz-back2').addEventListener('click', function () { window.etlStore.setStep(1); render(S.container); });
                return;
            }
        }

        S.listPaths = collectListPaths(S.structure && S.structure.tree);
        if (!S.rowListPath || S.listPaths.indexOf(S.rowListPath) < 0) {
            S.rowListPath = S.listPaths[0] || '';
        }
        syncTransforms();
        paintStep2();
    }

    function syncTransforms() {
        const leaves = collectLeaves(S.structure && S.structure.tree);
        const byPath = {};
        S.transforms.forEach(function (t) { byPath[t.path] = t; });
        S.transforms = leaves.map(function (leaf) {
            const old = byPath[leaf.path];
            if (old) { old.inferred = leaf.type || ''; return old; }
            return {
                path: leaf.path,
                inferred: leaf.type || '',
                targetType: inferTargetType(leaf.type),
                defaultValue: '',
                nullPolicy: 'null'
            };
        });
    }

    function paintStep2() {
        const store = window.etlStore.get();
        const src = store.source || {};
        const typeBadge = src.type ? '<span class="etl-badge info">' + esc(src.type) + '</span> ' : '';

        let rowSetHtml;
        if (S.listPaths.length > 1) {
            rowSetHtml = '<label class="form-label">行集合（list 节点，一行=一条记录）</label>' +
                '<select class="form-select form-select-sm" id="wiz-rowlist" style="max-width:420px;' + MONO + '">' +
                S.listPaths.map(function (p) {
                    return '<option value="' + esc(p) + '"' + (p === S.rowListPath ? ' selected' : '') + '>' + esc(p) + '</option>';
                }).join('') + '</select>';
        } else if (S.listPaths.length === 1) {
            rowSetHtml = '<div class="text-muted" style="font-size:13px;">行集合：<code style="' + MONO + '">' + esc(S.listPaths[0]) + '</code>（该数组的每个元素 = 一行记录）</div>';
        } else {
            rowSetHtml = '<div class="text-muted" style="font-size:13px;">未检测到数组节点，将以根节点作为单行记录处理</div>';
        }

        const rows = S.transforms.map(function (t, i) {
            return '<tr data-i="' + i + '">' +
                '<td style="' + MONO + 'font-size:12px;">' + esc(t.path) + '</td>' +
                '<td><span class="etl-badge info">' + esc(t.inferred || '-') + '</span></td>' +
                '<td><select class="form-select form-select-sm" data-k="targetType">' +
                TARGET_TYPES.map(function (tp) {
                    return '<option value="' + tp + '"' + (t.targetType === tp ? ' selected' : '') + '>' + tp + '</option>';
                }).join('') + '</select></td>' +
                '<td><input class="form-control form-control-sm" data-k="defaultValue" value="' + esc(t.defaultValue) + '" placeholder="默认值"></td>' +
                '<td><select class="form-select form-select-sm" data-k="nullPolicy">' +
                NULL_POLICIES.map(function (p) {
                    return '<option value="' + p.v + '"' + (t.nullPolicy === p.v ? ' selected' : '') + '>' + p.t + '</option>';
                }).join('') + '</select></td></tr>';
        }).join('');

        el('wiz-body').innerHTML =
            '<div class="mb-3">' + typeBadge + '<span class="etl-card-title" style="margin:0;">' + esc(src.name || '来源 #' + store.sourceId) + '</span>' +
            '<div class="text-muted" style="font-size:12px;">来源库（抽取）出参结构 · 下方为来源返回的字段，按需设置转换规则</div></div>' +
            '<div class="row g-3">' +
            '<div class="col-md-5"><h6 style="font-size:13px;">出参结构（展开一层）</h6>' +
            window.etlComponents.renderTree(trimTree(S.structure && S.structure.tree)) +
            '<div class="mt-2">' + rowSetHtml + '</div></div>' +
            '<div class="col-md-7"><h6 style="font-size:13px;">转换规则（叶子字段）</h6>' +
            '<div class="table-responsive"><table class="table table-sm align-middle" id="wiz-trans-table" style="font-size:13px;">' +
            '<thead><tr><th>源字段</th><th>推断类型</th><th>目标类型</th><th>默认值</th><th>空值策略</th></tr></thead>' +
            '<tbody>' + (rows || '<tr><td colspan="5" class="text-muted">无叶子字段</td></tr>') + '</tbody></table></div></div>' +
            '</div>';

        setFooter(
            '<button class="etl-btn etl-btn-ghost" id="wiz-back2"><i class="bi bi-arrow-left"></i> 上一步</button>' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-debug2"><i class="bi bi-bug"></i> 调试此步</button>',
            '<button class="etl-btn etl-btn-primary" id="wiz-next2">下一步 <i class="bi bi-arrow-right"></i></button>'
        );

        const tbl = el('wiz-trans-table');
        if (tbl) tbl.addEventListener('change', function (e) {
            const tr = e.target.closest('tr[data-i]');
            if (!tr) return;
            const i = parseInt(tr.getAttribute('data-i'), 10);
            const k = e.target.getAttribute('data-k');
            if (k && S.transforms[i]) {
                S.transforms[i][k] = e.target.value;
                window.etlStore.set({ transforms: S.transforms });
            }
        });
        const rl = el('wiz-rowlist');
        if (rl) rl.addEventListener('change', function () {
            S.rowListPath = rl.value;
            window.etlStore.set({ rowListPath: S.rowListPath });
        });
        el('wiz-back2').addEventListener('click', function () {
            window.etlStore.set({ transforms: S.transforms, rowListPath: S.rowListPath });
            window.etlStore.setStep(1);
            render(S.container);
        });
        el('wiz-next2').addEventListener('click', function () {
            window.etlStore.set({ structure: S.structure, transforms: S.transforms, rowListPath: S.rowListPath });
            window.etlStore.setStep(3);
            render(S.container);
        });
        el('wiz-debug2').addEventListener('click', debugStep2);
    }

    /* ============================================================
     * 第三步：映射匹配
     * ============================================================ */
    function defaultTaskCfg(source) {
        return {
            name: ((source && source.name) || 'ETL') + '流水线',
            writeMode: 'INSERT',
            cron: '',
            enabled: false,
            maxRows: '',
            batchSize: '',
            incremental: false,
            incField: '',
            incPlaceholder: '',
            retryCount: 0
        };
    }

    async function renderStep3() {
        const t = S.token;
        const store = window.etlStore.get();
        S.structure = store.structure || S.structure;
        S.mappings = store.mappings || [];
        S.taskCfg = store.task || defaultTaskCfg(store.source);

        if (!S.structure && store.sourceId) {
            el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载来源结构…</div></div>';
            try {
                const data = await window.etlApi.get('/source/structure/' + store.sourceId);
                if (!alive(t)) return;
                S.structure = data;
                window.etlStore.set({ structure: data });
            } catch (e) { if (!alive(t)) return; }
        }
        S.leafMap = {};
        collectLeaves(S.structure && S.structure.tree).forEach(function (n) { S.leafMap[n.path] = n; });

        if (!S.datasources) {
            el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载数据源…</div></div>';
            try {
                const ds = await window.etlApi.get('/datasource/list');
                if (!alive(t)) return;
                S.datasources = normList(ds);
            } catch (e) {
                if (!alive(t)) return;
                S.datasources = [];
            }
        }

        if (S.targetDsId) {
            try {
                const tables = await window.etlApi.get('/datasource/' + S.targetDsId + '/tables');
                if (!alive(t)) return;
                S.targetTables = filterTables(tables);
                // 历史任务可能存了小写表名，按实际表名大小写归一化，保证回显选中
                if (S.targetTable) {
                    const actual = S.targetTables.find(function (n) {
                        return n && n.toUpperCase() === String(S.targetTable).toUpperCase();
                    });
                    if (actual) S.targetTable = actual;
                }
            } catch (e) { if (!alive(t)) return; }
            if (S.targetTable) {
                try {
                    const cols = await window.etlApi.get('/datasource/' + S.targetDsId + '/columns/' + encodeURIComponent(S.targetTable));
                    if (!alive(t)) return;
                    S.targetColumns = normList(cols);
                } catch (e) { if (!alive(t)) return; }
            }
        }
        paintStep3();
    }

    function targetDsOptions() {
        const list = (S.datasources || []).filter(function (d) { return d.role === 'TARGET'; });
        return '<option value="">请选择目标数据源</option>' + list.map(function (d) {
            return '<option value="' + esc(d.id) + '"' + (String(d.id) === String(S.targetDsId) ? ' selected' : '') + '>' +
                esc(d.name) + '（' + esc(d.dbType) + '）</option>';
        }).join('');
    }

    function srcLeafOptions(selected) {
        const paths = Object.keys(S.leafMap);
        return '<option value="">选择源字段</option>' + paths.map(function (p) {
            return '<option value="' + esc(p) + '"' + (p === selected ? ' selected' : '') + '>' + esc(p) + '</option>';
        }).join('');
    }

    function targetColOptions(selected) {
        return '<option value="">选择目标列</option>' + S.targetColumns.map(function (c) {
            const n = colName(c);
            return '<option value="' + esc(n) + '"' +
                (selected && n && n.toUpperCase() === String(selected).toUpperCase() ? ' selected' : '') + '>' +
                esc(n) + (colType(c) ? ' · ' + esc(colType(c)) : '') + '</option>';
        }).join('');
    }

    function paintMapRows() {
        const box = el('wiz-map-list');
        if (!box) return;
        if (!S.mappings.length) {
            box.innerHTML = '<div class="etl-empty"><div class="etl-empty-text">暂无映射，点击左侧字段树或「添加映射」</div></div>';
            return;
        }
        box.innerHTML = S.mappings.map(function (m, i) {
            const src = m.srcField
                ? '<span class="etl-map-src" title="' + esc(m.srcField) + '">' + esc(m.srcField) + '</span>'
                : '<select class="form-select form-select-sm" data-k="srcField" style="max-width:240px;' + MONO + 'font-size:12px;">' + srcLeafOptions('') + '</select>';
            return '<div class="etl-map-row" data-i="' + i + '">' + src +
                '<span class="etl-map-arrow">→</span>' +
                '<select class="form-select form-select-sm etl-map-target" data-k="tgtField" style="' + MONO + 'font-size:12px;">' +
                targetColOptions(m.tgtField) + '</select>' +
                '<input class="form-control form-control-sm" data-k="defaultValue" style="width:100px;" value="' + esc(m.defaultValue || '') + '" placeholder="默认值">' +
                '<div class="etl-map-flags">' +
                '<label><input type="checkbox" data-k="isUpdateCol"' + (m.isUpdateCol ? ' checked' : '') + '> 更新</label>' +
                '<label><input type="checkbox" data-k="isIndexCol"' + (m.isIndexCol ? ' checked' : '') + '> 索引</label>' +
                '</div>' +
                '<button class="etl-btn etl-btn-danger" data-act="del" style="padding:2px 8px;" title="删除">×</button>' +
                '</div>';
        }).join('');
    }

    function paintStep3() {
        const targets = (S.datasources || []).filter(function (d) { return d.role === 'TARGET'; });
        const c = S.taskCfg;
        const needIndexHint = c.writeMode === 'UPDATE' || c.writeMode === 'UPSERT';

        let rightTop;
        if (!targets.length) {
            rightTop = '<div class="etl-empty"><div class="etl-empty-text">暂无 TARGET 数据源</div>' +
                '<a class="etl-btn etl-btn-ghost" href="#/datasources">去数据源管理新建</a></div>';
        } else {
            rightTop =
                '<div class="row g-2 mb-3">' +
                '<div class="col-md-6"><label class="form-label">数据源 <span class="tt-label-sub">（目的写入）</span></label>' +
                '<select class="form-select form-select-sm" id="wiz-tgt-ds">' + targetDsOptions() + '</select></div>' +
                '<div class="col-md-6"><label class="form-label">目标表</label>' +
                '<select class="form-select form-select-sm" id="wiz-tgt-table" style="' + MONO + '"' + (S.targetDsId ? '' : ' disabled') + '>' +
                '<option value="">' + (S.targetDsId ? '请选择目标表' : '先选数据源') + '</option>' +
                S.targetTables.map(function (n) {
                    return '<option value="' + esc(n) + '"' + (n === S.targetTable ? ' selected' : '') + '>' + esc(n) + '</option>';
                }).join('') + '</select></div></div>' +
                (S.targetTable
                    ? '<div class="text-muted mb-2" style="font-size:12px;">列信息：' + S.targetColumns.length + ' 列</div>'
                    : '');
        }

        el('wiz-body').innerHTML =
            '<div class="etl-flow-banner">' +
            '<span class="etl-flow-node">来源库 <i class="bi bi-boxes"></i></span>' +
            '<i class="bi bi-arrow-right"></i>' +
            '<span class="etl-flow-node">转换提取</span>' +
            '<i class="bi bi-arrow-right"></i>' +
            '<span class="etl-flow-node">数据源（目的写入） <i class="bi bi-database"></i></span>' +
            '<span class="etl-flow-note">来源库负责「抽取数据」，数据源负责「把结果写进去」。</span>' +
            '</div>' +
            '<div class="row g-3">' +
            '<div class="col-md-5"><h6 style="font-size:13px;">来源字段（点击叶子加入映射）</h6>' +
            '<div id="wiz-src-tree">' + window.etlComponents.renderTree(S.structure && S.structure.tree, { selectable: true }) + '</div></div>' +
            '<div class="col-md-7">' + rightTop +
            '<div class="d-flex justify-content-between align-items-center mb-2">' +
            '<h6 style="font-size:13px;margin:0;">字段映射</h6>' +
            '<div class="d-flex gap-2">' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-automap" style="padding:4px 10px;font-size:12px;">自动按名匹配</button>' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-addmap" style="padding:4px 10px;font-size:12px;"><i class="bi bi-plus"></i> 添加映射</button>' +
            '</div></div>' +
            '<div id="wiz-map-list"></div></div></div>' +
            '<details class="mt-3" id="wiz-task-details">' +
            '<summary style="cursor:pointer;font-weight:600;">任务设置</summary>' +
            '<div class="row g-3 mt-1">' +
            '<div class="col-md-4"><label class="form-label">任务名</label>' +
            '<input class="form-control form-control-sm" id="wiz-task-name" value="' + esc(c.name) + '"></div>' +
            '<div class="col-md-3"><label class="form-label">写入模式</label>' +
            '<select class="form-select form-select-sm" id="wiz-write-mode">' +
            ['INSERT', 'UPDATE', 'UPSERT'].map(function (m) {
                return '<option value="' + m + '"' + (c.writeMode === m ? ' selected' : '') + '>' + m + '</option>';
            }).join('') + '</select>' +
            '<div class="form-text" id="wiz-wm-hint" style="color:var(--etl-color-warning);' + (needIndexHint ? '' : 'display:none;') + '">UPDATE/UPSERT 必须勾选至少一个「索引」列</div></div>' +
            '<div class="col-md-3"><label class="form-label">cron（可空=仅手动）</label>' +
            '<input class="form-control form-control-sm" id="wiz-cron" style="' + MONO + '" value="' + esc(c.cron) + '" placeholder="0 0/5 * * * ?">' +
            '<div class="wiz-cron-preview" id="wiz-cron-preview">' + cronPreviewHtml(c.cron) + '</div></div>' +
            '<div class="col-md-2 d-flex align-items-end"><div class="form-check">' +
            '<input class="form-check-input" type="checkbox" id="wiz-enabled"' + (c.enabled ? ' checked' : '') + '>' +
            '<label class="form-check-label" for="wiz-enabled">启用定时</label></div></div>' +
            '<div class="col-md-2"><label class="form-label">maxRows</label>' +
            '<input type="number" class="form-control form-control-sm" id="wiz-max-rows" value="' + esc(c.maxRows) + '" min="1"></div>' +
            '<div class="col-md-2"><label class="form-label">batchSize</label>' +
            '<input type="number" class="form-control form-control-sm" id="wiz-batch" value="' + esc(c.batchSize) + '" min="1"></div>' +
            '<div class="col-md-2"><label class="form-label">重试次数</label>' +
            '<input type="number" class="form-control form-control-sm" id="wiz-retry" value="' + esc(c.retryCount) + '" min="0"></div>' +
            '<div class="col-md-2 d-flex align-items-end"><div class="form-check">' +
            '<input class="form-check-input" type="checkbox" id="wiz-inc"' + (c.incremental ? ' checked' : '') + '>' +
            '<label class="form-check-label" for="wiz-inc">增量抽取</label></div></div>' +
            '<div class="col-md-2"><label class="form-label">增量字段</label>' +
            '<input class="form-control form-control-sm" id="wiz-inc-field" style="' + MONO + '" value="' + esc(c.incField) + '"></div>' +
            '<div class="col-md-2"><label class="form-label">占位符</label>' +
            '<input class="form-control form-control-sm" id="wiz-inc-ph" style="' + MONO + '" value="' + esc(c.incPlaceholder) + '" placeholder=":lastValue"></div>' +
            '</div></details>';

        paintMapRows();
        setFooter(
            '<button class="etl-btn etl-btn-ghost" id="wiz-back3"><i class="bi bi-arrow-left"></i> 上一步</button>' +
            '<button class="etl-btn etl-btn-ghost" id="wiz-debug3"><i class="bi bi-bug"></i> 调试此步</button>',
            '<button class="etl-btn etl-btn-primary" id="wiz-create"><i class="bi bi-check2"></i> ' +
            (S.editId ? '保存修改' : '创建流水线') + '</button>'
        );

        window.etlComponents.bindTreePick(el('wiz-src-tree'), function (path) {
            const node = S.leafMap[path];
            if (!node) { toast('请选择叶子字段', 'warn'); return; }
            addMapping({ srcField: path, tgtField: '', defaultValue: '', isUpdateCol: false, isIndexCol: false });
        });
        const tds = el('wiz-tgt-ds');
        if (tds) tds.addEventListener('change', async function () {
            S.targetDsId = tds.value;
            S.targetTable = '';
            S.targetTables = [];
            S.targetColumns = [];
            persistStep3();
            paintStep3();
            if (S.targetDsId) {
                const tk = S.token;
                try {
                    const tables = await window.etlApi.get('/datasource/' + S.targetDsId + '/tables');
                    if (!alive(tk)) return;
                    S.targetTables = filterTables(tables);
                    paintStep3();
                } catch (e) { /* toast 已由 api 处理 */ }
            }
        });
        const tt = el('wiz-tgt-table');
        if (tt) tt.addEventListener('change', async function () {
            S.targetTable = tt.value;
            S.targetColumns = [];
            persistStep3();
            paintStep3();
            if (S.targetTable) {
                const tk = S.token;
                try {
                    const cols = await window.etlApi.get('/datasource/' + S.targetDsId + '/columns/' + encodeURIComponent(S.targetTable));
                    if (!alive(tk)) return;
                    S.targetColumns = normList(cols);
                    paintStep3();
                } catch (e) { /* toast 已由 api 处理 */ }
            }
        });
        const list = el('wiz-map-list');
        list.addEventListener('change', onMapEdit);
        list.addEventListener('input', onMapEdit);
        list.addEventListener('click', function (e) {
            const btn = e.target.closest('button[data-act="del"]');
            if (!btn) return;
            const row = btn.closest('.etl-map-row');
            const i = parseInt(row.getAttribute('data-i'), 10);
            S.mappings.splice(i, 1);
            persistStep3();
            paintMapRows();
        });
        el('wiz-addmap').addEventListener('click', function () {
            addMapping({ srcField: '', tgtField: '', defaultValue: '', isUpdateCol: false, isIndexCol: false });
        });
        el('wiz-automap').addEventListener('click', autoMatch);
        el('wiz-write-mode').addEventListener('change', function () {
            collectTaskCfg();
            const hint = el('wiz-wm-hint');
            const need = S.taskCfg.writeMode === 'UPDATE' || S.taskCfg.writeMode === 'UPSERT';
            if (hint) hint.style.display = need ? '' : 'none';
            persistStep3();
        });
        el('wiz-task-details').addEventListener('change', function () { collectTaskCfg(); persistStep3(); });
        const cronInp = el('wiz-cron');
        if (cronInp) cronInp.addEventListener('input', function () {
            const pv = el('wiz-cron-preview');
            if (pv) pv.innerHTML = cronPreviewHtml(cronInp.value);
        });
        el('wiz-back3').addEventListener('click', function () {
            collectTaskCfg(); persistStep3();
            window.etlStore.setStep(2);
            render(S.container);
        });
        el('wiz-debug3').addEventListener('click', debugStep3);
        el('wiz-create').addEventListener('click', onCreate);
    }

    function onMapEdit(e) {
        const row = e.target.closest('.etl-map-row');
        if (!row) return;
        const i = parseInt(row.getAttribute('data-i'), 10);
        const k = e.target.getAttribute('data-k');
        if (!k || !S.mappings[i]) return;
        S.mappings[i][k] = (e.target.type === 'checkbox') ? e.target.checked : e.target.value;
        if (k === 'srcField' && e.target.value) { persistStep3(); paintMapRows(); return; }
        persistStep3();
    }

    function addMapping(m) {
        S.mappings.push(m);
        persistStep3();
        paintMapRows();
    }

    function autoMatch() {
        if (!S.targetColumns.length) { toast('请先选择目标表', 'warn'); return; }
        const colByLower = {};
        S.targetColumns.forEach(function (c) { colByLower[colName(c).toLowerCase()] = colName(c); });
        const usedSrc = {};
        S.mappings.forEach(function (m) { if (m.srcField) usedSrc[m.srcField] = true; });
        let added = 0;
        Object.keys(S.leafMap).forEach(function (p) {
            if (usedSrc[p]) return;
            const seg = p.split('.').pop().toLowerCase();
            if (colByLower[seg]) {
                S.mappings.push({ srcField: p, tgtField: colByLower[seg], defaultValue: '', isUpdateCol: false, isIndexCol: false });
                added += 1;
            }
        });
        persistStep3();
        paintMapRows();
        toast(added ? '自动匹配 ' + added + ' 条' : '无可按名匹配的字段', added ? 'ok' : 'warn');
    }

    function collectTaskCfg() {
        S.taskCfg = {
            name: val('wiz-task-name'),
            writeMode: val('wiz-write-mode') || 'INSERT',
            cron: val('wiz-cron'),
            enabled: checked('wiz-enabled'),
            maxRows: val('wiz-max-rows'),
            batchSize: val('wiz-batch'),
            incremental: checked('wiz-inc'),
            incField: val('wiz-inc-field'),
            incPlaceholder: val('wiz-inc-ph'),
            retryCount: numOrNull(val('wiz-retry')) || 0
        };
    }

    function persistStep3() {
        window.etlStore.set({ mappings: S.mappings, task: S.taskCfg });
    }

    function cronPreviewHtml(cron) {
        if (!cron || !String(cron).trim()) {
            return '<span class="wiz-cron-preview-empty">留空 = 仅手动运行</span>';
        }
        const txt = window.etlComponents.fmtCron(cron);
        return txt && txt !== String(cron).trim()
            ? '<span class="wiz-cron-preview-text"><i class="bi bi-alarm"></i> ' + esc(txt) + '</span>'
            : '<span class="wiz-cron-preview-empty">无法解析，请参考 0 0/5 * * * ?</span>';
    }

    async function onCreate() {
        collectTaskCfg();
        if (!S.targetTable) { toast('请选择目标表', 'warn'); return; }
        const valid = S.mappings.filter(function (m) { return m.srcField && m.tgtField; });
        if (!valid.length) { toast('至少需要一条完整映射（源字段 + 目标列）', 'warn'); return; }
        if (S.mappings.length !== valid.length) { toast('存在未完整的映射行，请补全或删除', 'warn'); return; }
        const c = S.taskCfg;
        if (!c.name.trim()) { toast('请填写任务名', 'warn'); return; }
        const indexCols = valid.filter(function (m) { return m.isIndexCol; }).map(function (m) { return m.tgtField; });
        if ((c.writeMode === 'UPDATE' || c.writeMode === 'UPSERT') && !indexCols.length) {
            toast(c.writeMode + ' 模式必须勾选至少一个「索引」列', 'warn');
            return;
        }
        if (c.incremental && !c.incField.trim()) { toast('增量抽取需填写增量字段', 'warn'); return; }

        const store = window.etlStore.get();
        const transByPath = {};
        (store.transforms || []).forEach(function (t) { transByPath[t.path] = t; });
        const colTypeByName = {};
        S.targetColumns.forEach(function (col) { colTypeByName[colName(col)] = colType(col); });

        const taskBody = {
            name: c.name.trim(),
            extractType: (store.source && store.source.type === 'WS') ? 'WEBSERVICE' : 'PROCEDURE',
            sourceId: store.sourceId,
            sourceDsId: (store.source && store.source.sourceDsId) || null,
            targetDsId: S.targetDsId,
            targetTable: S.targetTable,
            writeMode: c.writeMode,
            queryIndexCols: indexCols.join(','),
            updateCols: valid.filter(function (m) { return m.isUpdateCol; }).map(function (m) { return m.tgtField; }).join(','),
            cron: c.cron,
            enabled: c.enabled ? 1 : 0,
            maxRows: numOrNull(c.maxRows),
            batchSize: numOrNull(c.batchSize),
            incremental: c.incremental ? 1 : 0,
            incField: c.incField,
            incPlaceholder: c.incPlaceholder,
            retryCount: c.retryCount
        };

        const btn = el('wiz-create');
        btn.disabled = true;
        const isEdit = !!S.editId;
        try {
            let taskId;
            if (isEdit) {
                await window.etlApi.put('/task/' + S.editId, { task: taskBody });
                taskId = S.editId;
            } else {
                const task = await window.etlApi.post('/task', { task: taskBody });
                taskId = (task && typeof task === 'object') ? task.id : task;
                if (!taskId) throw new Error('任务创建成功但未返回 id');
            }
            await window.etlApi.post('/mapping/batch', {
                taskId: taskId,
                mappings: valid.map(function (m, i) {
                    const tr = transByPath[m.srcField];
                    return {
                        srcField: m.srcField,
                        tgtField: m.tgtField,
                        defaultValue: m.defaultValue || (tr ? tr.defaultValue : '') || '',
                        isUpdateCol: m.isUpdateCol ? 1 : 0,
                        srcType: tr ? (tr.inferred || '') : '',
                        tgtType: colTypeByName[m.tgtField] || '',
                        sortOrder: i + 1
                    };
                })
            });
            toast(isEdit ? '流水线已更新' : '流水线已创建', 'ok');
            window.etlStore.clear();
            S.editId = null;
            S.editLoaded = null;
            location.hash = '#/tasks';
        } catch (e) {
            btn.disabled = false;
        }
    }

    /* ============================================================
     * 单步调试（不保存任务）
     * ============================================================ */
    async function debugStep1() {
        if (!S.pickedSource && !window.etlStore.get().source) {
            toast('请先选择一个来源', 'warn');
            return;
        }
        const source = S.pickedSource || window.etlStore.get().source;
        const taskId = (window.etlStore.get().task || {}).id;

        showDebugLoading('调试：抽取来源');
        try {
            let r;
            if (taskId) {
                r = await window.etlApi.post('/task/' + taskId + '/debug-extract', {});
            } else {
                r = await window.etlApi.post('/source/preview-debug', {
                    id: source.id,
                    name: source.name,
                    type: source.type,
                    sourceDsId: source.sourceDsId,
                    configJson: source.configJson
                });
            }
            S.lastDebug.extract = r;
            const columns = r.columns || [];
            const rows = r.rows || [];
            const meta = '共 ' + (r.totalRows != null ? r.totalRows : rows.length) + ' 行' +
                (r.durationMs != null ? ' · 耗时 ' + window.etlComponents.fmtDuration(r.durationMs) : '');
            showDebugTable('调试：抽取来源 — ' + esc(source.name) + '（' + esc(source.type || '?') + '）', meta, columns, rows);
        } catch (e) {
            showDebugError('调试：抽取来源', '接口调用失败：' + (e && e.message ? e.message : '未知错误') +
                '。若后端接口暂未实现，可忽略此提示，前端代码已预留。');
        }
    }

    async function debugStep2() {
        const store = window.etlStore.get();
        if (!store.sourceId || (!S.transforms.length && !(store.task || {}).id)) {
            toast('请先完成来源选择与转换规则配置', 'warn');
            return;
        }
        const taskId = (store.task || {}).id;

        // 准备输入行：优先使用第一步调试结果；否则给出提示
        const inputRows = buildTransformInput();
        if (!inputRows.length) {
            toast('没有可用于转换的样例行，请先在第一步执行「调试此步」', 'warn');
            return;
        }

        showDebugLoading('调试：转换提取');
        try {
            let r;
            if (taskId) {
                r = await window.etlApi.post('/task/' + taskId + '/debug-transform', { rows: inputRows });
            } else {
                // 未保存任务时本地模拟转换
                r = localTransformPreview(inputRows);
            }
            S.lastDebug.transform = r;
            const rows = r.rows || [];
            const meta = '输出 ' + rows.length + ' 行' +
                (r.durationMs != null ? ' · 耗时 ' + window.etlComponents.fmtDuration(r.durationMs) : '') +
                (taskId ? '' : '（本地预览，未调用后端）');
            const columns = rows.length ? Object.keys(rows[0]) : [];
            showDebugTable('调试：转换提取', meta, columns, rows);
        } catch (e) {
            showDebugError('调试：转换提取', '接口调用失败：' + (e && e.message ? e.message : '未知错误') +
                '。若后端接口暂未实现，可忽略此提示，前端代码已预留。');
        }
    }

    async function debugStep3() {
        const store = window.etlStore.get();
        if (!store.sourceId || !S.mappings.length) {
            toast('请先完成映射配置', 'warn');
            return;
        }
        const taskId = (store.task || {}).id;
        const inputRows = S.lastDebug.transform ? S.lastDebug.transform.rows : (S.lastDebug.extract ? S.lastDebug.extract.rows : []);
        if (!inputRows.length) {
            toast('没有可用于加载预览的样例行，请先在前序步骤执行「调试此步」', 'warn');
            return;
        }

        showDebugLoading('调试：加载预览');
        try {
            let r;
            if (taskId) {
                r = await window.etlApi.post('/task/' + taskId + '/debug-load-preview', { rows: inputRows });
            } else {
                r = localLoadPreview(inputRows);
            }
            S.lastDebug.load = r;
            const columns = r.columns || [];
            const rows = r.rows || [];
            const meta = (r.targetTable ? '目标表：' + esc(r.targetTable) + ' · ' : '') +
                '共 ' + (r.count != null ? r.count : rows.length) + ' 行' +
                (taskId ? '' : '（本地预览，未调用后端）');
            showDebugTable('调试：加载预览', meta, columns, rows);
        } catch (e) {
            showDebugError('调试：加载预览', '接口调用失败：' + (e && e.message ? e.message : '未知错误') +
                '。若后端接口暂未实现，可忽略此提示，前端代码已预留。');
        }
    }

    function buildTransformInput() {
        if (S.lastDebug.extract && S.lastDebug.extract.rows) return S.lastDebug.extract.rows;
        // 无第一步结果时，尝试从 structure 的 sample 构造一行空记录
        return [];
    }

    function localTransformPreview(rows) {
        const transByPath = {};
        S.transforms.forEach(function (t) { transByPath[t.path] = t; });
        const out = rows.map(function (row) {
            const obj = {};
            S.transforms.forEach(function (t) {
                const raw = row && typeof row === 'object' ? row[t.path] : undefined;
                let v = raw;
                if (v === null || v === undefined || v === '') {
                    if (t.nullPolicy === 'skip') return;
                    if (t.nullPolicy === 'default') v = t.defaultValue;
                    else v = null;
                }
                v = castValue(v, t.targetType);
                obj[t.path] = v;
            });
            return obj;
        }).filter(Boolean);
        return { rows: out, durationMs: 0 };
    }

    function localLoadPreview(rows) {
        const valid = S.mappings.filter(function (m) { return m.srcField && m.tgtField; });
        const out = rows.map(function (row) {
            const obj = {};
            valid.forEach(function (m) {
                const tr = S.transforms.find(function (t) { return t.path === m.srcField; });
                let v = row && typeof row === 'object' ? row[m.srcField] : undefined;
                if ((v === null || v === undefined || v === '') && m.defaultValue) v = m.defaultValue;
                if (tr) v = castValue(v, tr.targetType);
                obj[m.tgtField] = v;
            });
            return obj;
        });
        const columns = valid.map(function (m) { return m.tgtField; });
        return { targetTable: S.targetTable || '(未选目标表)', columns: columns, rows: out, count: out.length };
    }

    function castValue(v, type) {
        if (v === null || v === undefined) return null;
        if (type === 'number') {
            const n = Number(v);
            return isNaN(n) ? null : n;
        }
        if (type === 'boolean') return Boolean(v);
        if (type === 'date') {
            const d = new Date(v);
            return isNaN(d.getTime()) ? String(v) : d.toISOString();
        }
        return String(v);
    }

    /* ---------- 调试结果弹窗 ---------- */
    function showDebugLoading(title) {
        showDebugOverlay(
            '<div class="etl-debug-panel-header">' +
            '<h6><i class="bi bi-bug"></i> ' + esc(title) + '</h6>' +
            '<button class="etl-modal-close" onclick="WizardApp.closeDebug()"><i class="bi bi-x-lg"></i></button>' +
            '</div>' +
            '<div class="etl-debug-panel-body"><div class="etl-empty"><div class="etl-empty-text">加载中…</div></div></div>'
        );
    }

    function showDebugError(title, message) {
        showDebugOverlay(
            '<div class="etl-debug-panel-header">' +
            '<h6><i class="bi bi-bug"></i> ' + esc(title) + '</h6>' +
            '<button class="etl-modal-close" onclick="WizardApp.closeDebug()"><i class="bi bi-x-lg"></i></button>' +
            '</div>' +
            '<div class="etl-debug-panel-body">' +
            '<div class="alert alert-warning" role="alert">' + esc(message) + '</div>' +
            '</div>'
        );
    }

    function showDebugTable(title, meta, columns, rows) {
        const MAX_ROWS = 100;
        const displayRows = (rows || []).slice(0, MAX_ROWS);
        const cols = (columns || []).map(function (c) { return typeof c === 'string' ? c : (c.name || c.columnName || '?'); });
        const hasMore = (rows || []).length > MAX_ROWS;

        let thead, tbody;
        if (!cols.length && displayRows.length) {
            thead = '<tr><th>（无列名）</th></tr>';
            tbody = displayRows.map(function (row) {
                return '<tr><td><pre style="margin:0;">' + esc(JSON.stringify(row, null, 2)) + '</pre></td></tr>';
            }).join('');
        } else {
            thead = '<tr>' + cols.map(function (c) {
                return '<th style="' + MONO + 'font-size:11px;white-space:nowrap;">' + esc(c) + '</th>';
            }).join('') + '</tr>';
            tbody = displayRows.map(function (row) {
                return '<tr>' + cols.map(function (c) {
                    const v = Array.isArray(row) ? row[cols.indexOf(c)] : row[c];
                    const text = v === null || v === undefined ? '' : String(v);
                    return '<td style="' + MONO + '" title="' + esc(text) + '">' + esc(text.length > 120 ? text.slice(0, 120) + '…' : text) + '</td>';
                }).join('') + '</tr>';
            }).join('');
        }

        const tableHtml = '<div class="debug-table-wrap-full"><table class="table table-sm table-bordered mb-0">' +
            '<thead>' + thead + '</thead>' +
            '<tbody>' + (tbody || '<tr><td colspan="' + Math.max(1, cols.length) + '" class="text-muted">无数据</td></tr>') + '</tbody>' +
            '</table></div>' +
            (hasMore ? '<div class="text-muted mt-2" style="font-size:12px;">仅展示前 ' + MAX_ROWS + ' 行</div>' : '');

        showDebugOverlay(
            '<div class="etl-debug-panel-header">' +
            '<h6><i class="bi bi-bug"></i> ' + esc(title) + '</h6>' +
            '<button class="etl-modal-close" onclick="WizardApp.closeDebug()"><i class="bi bi-x-lg"></i></button>' +
            '</div>' +
            '<div class="etl-debug-panel-body">' +
            '<div class="mb-2" style="font-size:13px;"><span class="etl-badge info">' + esc(meta) + '</span></div>' +
            tableHtml +
            '</div>'
        );
    }

    function showDebugOverlay(html) {
        closeDebug();
        const overlay = document.createElement('div');
        overlay.className = 'etl-debug-overlay';
        overlay.id = 'wiz-debug-overlay';
        overlay.innerHTML = '<div class="etl-debug-panel-inner">' + html + '</div>';
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) closeDebug();
        });
        document.body.appendChild(overlay);
        document.addEventListener('keydown', closeDebugOnEsc);
    }

    function closeDebugOnEsc(e) {
        if (e.key === 'Escape') closeDebug();
    }

    function closeDebug() {
        const overlay = el('wiz-debug-overlay');
        if (overlay) overlay.remove();
        document.removeEventListener('keydown', closeDebugOnEsc);
    }

    window.WizardApp.render = render;
    window.WizardApp.closeDebug = closeDebug;
})();
