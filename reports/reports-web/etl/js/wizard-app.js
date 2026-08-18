/**
 * ETL 流水线创建向导 — 三步：抽取来源 → 转换提取 → 映射匹配
 * 导出 window.WizardApp = { render(container) }
 * 依赖：window.etlApi / window.etlStore / window.etlComponents
 * 状态：跨刷新由 etlStore（sessionStorage）恢复，模块态 S 仅页面会话内有效
 */
(function () {
    const STEPS = [
        { key: 'source', name: '抽取来源' },
        { key: 'transform', name: '转换提取' },
        { key: 'mapping', name: '映射匹配' }
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
        datasources: null,
        existingId: '',
        srcType: 'PROC',
        form: {},
        debugHtml: '',
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
        leafMap: {}
    };

    /* ---------- 小工具 ---------- */
    function esc(s) { return window.etlComponents.esc(s); }
    function toast(m, t) { window.etlComponents.toast(m, t); }
    function el(id) { return document.getElementById(id); }
    function val(id) { const n = el(id); return n ? n.value : ''; }
    function checked(id) { const n = el(id); return n ? n.checked : false; }
    function numOrNull(v) { const n = parseInt(v, 10); return isNaN(n) ? null : n; }
    function alive(t) { return t === S.token && S.container; }

    function normList(data) {
        if (Array.isArray(data)) return data;
        if (data && Array.isArray(data.records)) return data.records;
        if (data && Array.isArray(data.tables)) return data.tables;
        if (data && Array.isArray(data.columns)) return data.columns;
        return [];
    }
    function colName(c) { return typeof c === 'string' ? c : (c.name || c.columnName || ''); }
    function colType(c) { return typeof c === 'string' ? '' : (c.type || c.dataType || ''); }
    function tblName(t) { return typeof t === 'string' ? t : (t.name || t.tableName || ''); }

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
        S.step = window.etlStore.getStep();
        if (S.step > 1 && !window.etlStore.get().sourceId) {
            toast('请先完成第一步：选择或新建抽取来源', 'warn');
            window.etlStore.setStep(1);
            S.step = 1;
        }
        container.innerHTML = window.etlComponents.renderStepper(S.step, STEPS) +
            '<div class="etl-card" id="wiz-body"></div>' +
            '<div class="d-flex justify-content-between align-items-center mt-3" id="wiz-footer"></div>';
        if (S.step === 1) renderStep1();
        else if (S.step === 2) renderStep2();
        else renderStep3();
    }

    function setFooter(leftHtml, rightHtml) {
        const f = el('wiz-footer');
        if (f) f.innerHTML =
            '<div class="d-flex gap-2">' + (leftHtml || '') + '</div>' +
            '<div class="d-flex gap-2">' + (rightHtml || '') + '</div>';
    }

    /* ============================================================
     * 第一步：抽取来源
     * ============================================================ */
    async function renderStep1() {
        const t = S.token;
        el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载来源库与数据源…</div></div>';
        try {
            const rs = await Promise.all([
                window.etlApi.get('/source/list'),
                window.etlApi.get('/datasource/list')
            ]);
            if (!alive(t)) return;
            S.sources = normList(rs[0]);
            S.datasources = normList(rs[1]);
        } catch (e) {
            if (!alive(t)) return;
            S.sources = S.sources || [];
            S.datasources = S.datasources || [];
        }
        paintStep1();
    }

    function sourceDsOptions(selected) {
        const list = (S.datasources || []).filter(function (d) { return d.role === 'SOURCE'; });
        if (!list.length) return '<option value="">（无 SOURCE 数据源，请先到数据源管理新建）</option>';
        return '<option value="">请选择源数据库</option>' + list.map(function (d) {
            return '<option value="' + esc(d.id) + '"' + (String(d.id) === String(selected || '') ? ' selected' : '') + '>' +
                esc(d.name) + '（' + esc(d.dbType) + '）</option>';
        }).join('');
    }

    function paintStep1() {
        const f = S.form;
        const isProc = S.srcType === 'PROC';
        const cardStyle = function (type) { return S.srcType === type ? SELECTED_CARD : 'cursor:pointer;'; };

        // 已有来源
        let existingHtml;
        if (!S.sources.length) {
            existingHtml = '<div class="etl-empty"><div class="etl-empty-text">来源库暂无记录，请在下方新建来源</div></div>';
        } else {
            const opts = '<option value="">— 不使用已有来源（下方新建） —</option>' + S.sources.map(function (s) {
                return '<option value="' + esc(s.id) + '"' + (String(s.id) === String(S.existingId) ? ' selected' : '') + '>' +
                    esc(s.name) + '（' + esc(s.type) + '）</option>';
            }).join('');
            let info = '';
            const picked = S.sources.filter(function (s) { return String(s.id) === String(S.existingId); })[0];
            if (picked) {
                info = '<div class="mt-2"><span class="etl-badge info">' + esc(picked.type) + '</span> ' +
                    '<span style="' + MONO + 'font-size:12px;">' + esc(picked.name) + '</span> ' +
                    '<span class="text-muted" style="font-size:12px;">已选择已有来源，可直接下一步</span></div>';
            }
            existingHtml = '<select class="form-select" id="wiz-existing">' + opts + '</select>' + info;
        }

        // 新建表单
        let typeForm;
        if (isProc) {
            typeForm =
                '<div class="row g-3">' +
                '<div class="col-md-6"><label class="form-label">源数据库</label>' +
                '<select class="form-select" id="wiz-proc-ds">' + sourceDsOptions(f.procDs) + '</select></div>' +
                '<div class="col-md-6"><label class="form-label">存储过程名</label>' +
                '<input class="form-control" id="wiz-proc-name" style="' + MONO + '" value="' + esc(f.procName || '') + '" placeholder="PKG_ETL.PRC_EXPORT"></div>' +
                '<div class="col-12"><label class="form-label">调用模板</label>' +
                '<input class="form-control" id="wiz-proc-call" style="' + MONO + '" value="' + esc(f.procCall || '') + '" placeholder="{call PRC_EXPORT(?, ?)}"></div>' +
                '<div class="col-md-3"><label class="form-label">游标参数名</label>' +
                '<input class="form-control" id="wiz-proc-cursor-name" style="' + MONO + '" value="' + esc(f.cursorName || '') + '"></div>' +
                '<div class="col-md-3"><label class="form-label">游标参数位置</label>' +
                '<input type="number" class="form-control" id="wiz-proc-cursor-idx" value="' + esc(f.cursorIdx || '') + '" min="1"></div>' +
                '<div class="col-12"><label class="form-label">IN 参数 JSON</label>' +
                '<textarea class="form-control" id="wiz-proc-inparams" rows="2" style="' + MONO + '" placeholder=' + "'[{\"name\":\"p_date\",\"value\":\"20260101\"}]'" + '>' + esc(f.inParams || '') + '</textarea></div>' +
                '</div>';
        } else {
            const isSoap = (f.wsType || 'REST') === 'SOAP';
            typeForm =
                '<div class="row g-3">' +
                '<div class="col-md-3"><label class="form-label">接口类型</label>' +
                '<select class="form-select" id="wiz-ws-type">' +
                '<option value="REST"' + (!isSoap ? ' selected' : '') + '>REST</option>' +
                '<option value="SOAP"' + (isSoap ? ' selected' : '') + '>SOAP</option></select></div>' +
                '<div class="col-md-9"><label class="form-label">URL</label>' +
                '<input class="form-control" id="wiz-ws-url" style="' + MONO + '" value="' + esc(f.wsUrl || '') + '" placeholder="https://example.com/api/data"></div>' +
                (isSoap ? '<div class="col-12"><label class="form-label">SOAPAction</label>' +
                    '<input class="form-control" id="wiz-ws-soap" style="' + MONO + '" value="' + esc(f.wsSoap || '') + '"></div>' : '') +
                '<div class="col-md-6"><label class="form-label">请求头 JSON</label>' +
                '<textarea class="form-control" id="wiz-ws-headers" rows="2" style="' + MONO + '" placeholder=' + "'{\"Authorization\":\"Bearer ...\"}'" + '>' + esc(f.wsHeaders || '') + '</textarea></div>' +
                '<div class="col-md-6"><label class="form-label">请求体模板</label>' +
                '<textarea class="form-control" id="wiz-ws-body" rows="2" style="' + MONO + '">' + esc(f.wsBody || '') + '</textarea></div>' +
                '<div class="col-md-6"><label class="form-label">出参定位路径 responsePath</label>' +
                '<input class="form-control" id="wiz-ws-path" style="' + MONO + '" value="' + esc(f.wsPath || '') + '" placeholder="data.list"></div>' +
                '<div class="col-md-6"><label class="form-label">分页参数 extractParamsJson</label>' +
                '<textarea class="form-control" id="wiz-ws-extract" rows="2" style="' + MONO + '" placeholder=' + "'{\"pageNo\":\"{page}\"}'" + '>' + esc(f.wsExtract || '') + '</textarea></div>' +
                '</div>';
        }

        el('wiz-body').innerHTML =
            '<h6 class="etl-card-title">从来源库选择已有来源</h6>' + existingHtml +
            '<hr class="my-4"><h6 class="etl-card-title">或新建来源</h6>' +
            '<div class="row g-3 mb-3">' +
            '<div class="col-md-6"><div class="etl-source-card type-proc" id="wiz-card-proc" style="' + cardStyle('PROC') + '">' +
            '<div class="etl-source-name">数据库存储过程</div>' +
            '<div class="etl-source-meta">PROCEDURE · 游标分页抽取</div></div></div>' +
            '<div class="col-md-6"><div class="etl-source-card type-ws" id="wiz-card-ws" style="' + cardStyle('WS') + '">' +
            '<div class="etl-source-name">接口调用</div>' +
            '<div class="etl-source-meta">WEBSERVICE · REST / SOAP</div></div></div></div>' +
            '<div class="row g-3 mb-3"><div class="col-md-6"><label class="form-label">来源名称</label>' +
            '<input class="form-control" id="wiz-name" value="' + esc(f.name || '') + '" placeholder="如：订单抽取"></div></div>' +
            typeForm +
            '<div class="row g-3 mt-0">' +
            '<div class="col-md-2"><label class="form-label">最大页数</label>' +
            '<input type="number" class="form-control" id="wiz-max-pages" value="' + esc(f.maxPages || '') + '" min="1"></div>' +
            '<div class="col-md-2"><label class="form-label">最大行数</label>' +
            '<input type="number" class="form-control" id="wiz-max-rows" value="' + esc(f.maxRows || '') + '" min="1"></div>' +
            '<div class="col-md-2"><label class="form-label">批大小</label>' +
            '<input type="number" class="form-control" id="wiz-batch-size" value="' + esc(f.batchSize || '') + '" min="1"></div>' +
            '</div>' +
            '<div id="wiz-debug" class="mt-3">' + S.debugHtml + '</div>';

        setFooter(
            '<button class="etl-btn etl-btn-ghost" id="wiz-debug-btn"><i class="bi bi-bug"></i> 调试出参</button>',
            '<button class="etl-btn etl-btn-primary" id="wiz-next1">保存并下一步 <i class="bi bi-arrow-right"></i></button>'
        );

        // 绑定
        el('wiz-card-proc').addEventListener('click', function () { collectStep1Form(); S.srcType = 'PROC'; paintStep1(); });
        el('wiz-card-ws').addEventListener('click', function () { collectStep1Form(); S.srcType = 'WS'; paintStep1(); });
        const ex = el('wiz-existing');
        if (ex) ex.addEventListener('change', function () { collectStep1Form(); S.existingId = ex.value; paintStep1(); });
        const wt = el('wiz-ws-type');
        if (wt) wt.addEventListener('change', function () { collectStep1Form(); paintStep1(); });
        el('wiz-debug-btn').addEventListener('click', onDebug);
        el('wiz-next1').addEventListener('click', onSaveNext1);
    }

    function collectStep1Form() {
        S.form = {
            name: val('wiz-name'),
            procDs: val('wiz-proc-ds'),
            procName: val('wiz-proc-name'),
            procCall: val('wiz-proc-call'),
            cursorName: val('wiz-proc-cursor-name'),
            cursorIdx: val('wiz-proc-cursor-idx'),
            inParams: val('wiz-proc-inparams'),
            wsType: val('wiz-ws-type') || S.form.wsType || 'REST',
            wsUrl: val('wiz-ws-url'),
            wsSoap: val('wiz-ws-soap'),
            wsHeaders: val('wiz-ws-headers'),
            wsBody: val('wiz-ws-body'),
            wsPath: val('wiz-ws-path'),
            wsExtract: val('wiz-ws-extract'),
            maxPages: val('wiz-max-pages'),
            maxRows: val('wiz-max-rows'),
            batchSize: val('wiz-batch-size')
        };
    }

    function validJsonOrEmpty(text, label) {
        if (!text || !text.trim()) return true;
        try { JSON.parse(text); return true; }
        catch (e) { toast(label + ' 不是合法 JSON', 'err'); return false; }
    }

    /** 由模块态表单拼来源配置；返回 {config, sourceDsId, error} */
    function buildSourceConfig(needName) {
        const f = S.form;
        if (needName && !f.name.trim()) return { error: '请填写来源名称' };
        if (S.srcType === 'PROC') {
            if (!f.procDs) return { error: '请选择源数据库' };
            if (!f.procName.trim()) return { error: '请填写存储过程名' };
            if (!validJsonOrEmpty(f.inParams, 'IN 参数 JSON')) return { error: '' };
            return {
                sourceDsId: f.procDs,
                config: {
                    procName: f.procName.trim(),
                    callTemplate: f.procCall,
                    cursorParamName: f.cursorName,
                    cursorParamIdx: numOrNull(f.cursorIdx),
                    inParamsJson: f.inParams,
                    maxPages: numOrNull(f.maxPages),
                    maxRows: numOrNull(f.maxRows),
                    batchSize: numOrNull(f.batchSize)
                }
            };
        }
        if (!f.wsUrl.trim()) return { error: '请填写接口 URL' };
        if (!validJsonOrEmpty(f.wsHeaders, '请求头 JSON')) return { error: '' };
        if (!validJsonOrEmpty(f.wsExtract, '分页参数 JSON')) return { error: '' };
        return {
            sourceDsId: null,
            config: {
                wsType: f.wsType || 'REST',
                url: f.wsUrl.trim(),
                soapAction: f.wsSoap,
                requestBodyTemplate: f.wsBody,
                responsePath: f.wsPath,
                headersJson: f.wsHeaders,
                extractParamsJson: f.wsExtract,
                maxPages: numOrNull(f.maxPages),
                maxRows: numOrNull(f.maxRows),
                batchSize: numOrNull(f.batchSize)
            }
        };
    }

    async function onDebug() {
        collectStep1Form();
        const built = buildSourceConfig(false);
        if (built.error) { toast(built.error, 'warn'); return; }
        if (built.error === '') return; // JSON 校验已 toast
        const btn = el('wiz-debug-btn');
        btn.disabled = true;
        const body = Object.assign({ type: S.srcType }, built.config);
        if (built.sourceDsId) body.sourceDsId = built.sourceDsId;
        try {
            const res = await window.etlApi.post('/source/preview-debug', body);
            if (!S.container) return;
            S.debugHtml = paintDebugOk(res);
        } catch (e) {
            if (!S.container) return;
            S.debugHtml = window.etlComponents.renderDebugPanel('PREVIEW DEBUG', [
                { label: 'STATUS', status: 'err', content: (e && e.message) || '调试失败' }
            ]);
        }
        btn.disabled = false;
        const box = el('wiz-debug');
        if (box) box.innerHTML = S.debugHtml;
    }

    function paintDebugOk(res) {
        const cols = res.columns || [];
        const rows = res.rows || [];
        const panel = window.etlComponents.renderDebugPanel('PREVIEW DEBUG', [
            { label: 'STATUS', status: 'ok', content: '耗时 ' + window.etlComponents.fmtDuration(res.durationMs) +
                ' · 总行数 ' + (res.totalRows != null ? res.totalRows : rows.length) + ' · 样例 ' + rows.length + ' 行' },
            { label: 'COLUMNS', content: cols.join('  ') }
        ]);
        let table = '';
        if (rows.length && cols.length) {
            table = '<div class="table-responsive mt-2"><table class="table table-sm table-bordered mb-0" style="font-size:12px;background:#fff;">' +
                '<thead><tr>' + cols.map(function (c) { return '<th style="' + MONO + '">' + esc(c) + '</th>'; }).join('') + '</tr></thead><tbody>' +
                rows.slice(0, 20).map(function (r) {
                    return '<tr>' + cols.map(function (c) {
                        const v = Array.isArray(r) ? r[cols.indexOf(c)] : r[c];
                        return '<td>' + esc(v == null ? '' : (typeof v === 'object' ? JSON.stringify(v) : v)) + '</td>';
                    }).join('') + '</tr>';
                }).join('') + '</tbody></table></div>';
        }
        return panel + table;
    }

    async function onSaveNext1() {
        // 已选已有来源：直接进入第二步
        if (S.existingId) {
            const picked = S.sources.filter(function (s) { return String(s.id) === String(S.existingId); })[0];
            if (!picked) { toast('所选来源不存在', 'err'); return; }
            window.etlStore.set({ sourceId: picked.id, source: picked, structure: null, transforms: [], mappings: [], task: null });
            window.etlStore.setStep(2);
            render(S.container);
            return;
        }
        collectStep1Form();
        const built = buildSourceConfig(true);
        if (built.error) { toast(built.error, 'warn'); return; }
        if (built.error === '') return;
        const btn = el('wiz-next1');
        btn.disabled = true;
        try {
            const body = {
                name: S.form.name.trim(),
                type: S.srcType,
                configJson: JSON.stringify(built.config)
            };
            if (built.sourceDsId) body.sourceDsId = built.sourceDsId;
            const saved = await window.etlApi.post('/source', body);
            const savedId = (saved && typeof saved === 'object') ? saved.id : saved;
            if (!savedId) throw new Error('来源保存成功但未返回 id');
            window.etlStore.set({ sourceId: savedId, source: saved, structure: null, transforms: [], mappings: [], task: null });
            toast('来源已保存', 'ok');
            window.etlStore.setStep(2);
            render(S.container);
        } catch (e) {
            btn.disabled = false;
        }
    }

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

        // 行集合说明
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

        // 转换规则表
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
            '<div class="mb-3">' + typeBadge + '<span class="etl-card-title" style="margin:0;">' + esc(src.name || '来源 #' + store.sourceId) + '</span></div>' +
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
            '<button class="etl-btn etl-btn-ghost" id="wiz-back2"><i class="bi bi-arrow-left"></i> 上一步</button>',
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

        // 结构兜底（直接跳到第三步且 store 无结构时）
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

        // 已选目标数据源/表时补齐下拉数据
        if (S.targetDsId) {
            try {
                const tables = await window.etlApi.get('/datasource/' + S.targetDsId + '/tables');
                if (!alive(t)) return;
                S.targetTables = normList(tables).map(tblName);
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
            return '<option value="' + esc(n) + '"' + (n === selected ? ' selected' : '') + '>' +
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
                '<div class="col-md-6"><label class="form-label">目标数据源</label>' +
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
            '<input class="form-control form-control-sm" id="wiz-cron" style="' + MONO + '" value="' + esc(c.cron) + '" placeholder="0 0/5 * * * ?"></div>' +
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
            '<button class="etl-btn etl-btn-ghost" id="wiz-back3"><i class="bi bi-arrow-left"></i> 上一步</button>',
            '<button class="etl-btn etl-btn-primary" id="wiz-create"><i class="bi bi-check2"></i> 创建流水线</button>'
        );

        // 绑定：树点选
        window.etlComponents.bindTreePick(el('wiz-src-tree'), function (path) {
            const node = S.leafMap[path];
            if (!node) { toast('请选择叶子字段', 'warn'); return; }
            addMapping({ srcField: path, tgtField: '', defaultValue: '', isUpdateCol: false, isIndexCol: false });
        });
        // 目标数据源/表
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
                    S.targetTables = normList(tables).map(tblName);
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
        // 映射区事件委托
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
        // 任务设置
        el('wiz-write-mode').addEventListener('change', function () {
            collectTaskCfg();
            const hint = el('wiz-wm-hint');
            const need = S.taskCfg.writeMode === 'UPDATE' || S.taskCfg.writeMode === 'UPSERT';
            if (hint) hint.style.display = need ? '' : 'none';
            persistStep3();
        });
        el('wiz-task-details').addEventListener('change', function () { collectTaskCfg(); persistStep3(); });
        el('wiz-back3').addEventListener('click', function () {
            collectTaskCfg(); persistStep3();
            window.etlStore.setStep(2);
            render(S.container);
        });
        el('wiz-create').addEventListener('click', onCreate);
    }

    function onMapEdit(e) {
        const row = e.target.closest('.etl-map-row');
        if (!row) return;
        const i = parseInt(row.getAttribute('data-i'), 10);
        const k = e.target.getAttribute('data-k');
        if (!k || !S.mappings[i]) return;
        S.mappings[i][k] = (e.target.type === 'checkbox') ? e.target.checked : e.target.value;
        // 手动行的源字段选定后，重绘使其变为 chip
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

    async function onCreate() {
        collectTaskCfg();
        // 校验
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
        const srcType = (store.source && store.source.type) || S.srcType;
        const transByPath = {};
        (store.transforms || []).forEach(function (t) { transByPath[t.path] = t; });
        const colTypeByName = {};
        S.targetColumns.forEach(function (col) { colTypeByName[colName(col)] = colType(col); });

        const taskBody = {
            name: c.name.trim(),
            extractType: srcType === 'WS' ? 'WEBSERVICE' : 'PROCEDURE',
            sourceId: store.sourceId,
            sourceDsId: (store.source && store.source.sourceDsId) || null,
            targetDsId: S.targetDsId,
            targetTable: S.targetTable,
            writeMode: c.writeMode,
            queryIndexCols: indexCols.join(','),
            updateCols: valid.filter(function (m) { return m.isUpdateCol; }).map(function (m) { return m.tgtField; }).join(','),
            cron: c.cron,
            enabled: c.enabled,
            maxRows: numOrNull(c.maxRows),
            batchSize: numOrNull(c.batchSize),
            incremental: c.incremental,
            incField: c.incField,
            incPlaceholder: c.incPlaceholder,
            retryCount: c.retryCount
        };

        const btn = el('wiz-create');
        btn.disabled = true;
        try {
            const task = await window.etlApi.post('/task', { task: taskBody });
            const taskId = (task && typeof task === 'object') ? task.id : task;
            if (!taskId) throw new Error('任务创建成功但未返回 id');
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
            toast('流水线已创建', 'ok');
            window.etlStore.clear();
            location.hash = '#/history';
        } catch (e) {
            btn.disabled = false;
        }
    }

    window.WizardApp = { render: render };
})();
