/**
 * ETL 流水线创建向导 — 精简三步：选择来源 → 转换提取 → 映射匹配
 * 导出 window.WizardApp = { render(container) }
 * 依赖：window.etlApi / window.etlStore / window.etlComponents
 * 状态：跨刷新由 etlStore（sessionStorage）恢复，模块态 S 仅页面会话内有效
 *
 * 变化：第一步移除了内联新建表单，来源管理在「来源库」页完成；
 *       第一步只显示来源卡片网格，选源后直接进入第二步。
 */
(function () {
    const STEPS = [
        { key: 'source', name: '选择来源' },
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
        existingId: '',
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
            toast('请先完成第一步：选择抽取来源', 'warn');
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
     * 第一步：选择来源（纯列表，无内联新建）
     * ============================================================ */
    async function renderStep1() {
        const t = S.token;
        el('wiz-body').innerHTML = '<div class="etl-empty"><div class="etl-empty-text">加载来源库…</div></div>';
        try {
            const rs = await window.etlApi.get('/source/list');
            if (!alive(t)) return;
            S.sources = normList(rs);
            S.existingId = '';
        } catch (e) {
            if (!alive(t)) return;
            S.sources = [];
        }
        paintStep1();
    }

    function paintStep1() {
        if (!S.sources.length) {
            el('wiz-body').innerHTML =
                '<div class="etl-empty">' +
                '<div class="etl-empty-text">来源库暂无记录</div>' +
                '<button class="etl-btn etl-btn-primary" onclick="location.hash=\'#/sources\'">' +
                '<i class="bi bi-arrow-left"></i> 去来源库创建</button>' +
                '</div>';
            setFooter('', '');
            return;
        }

        // 来源只有一个时直接选中，多个时显示下拉列表
        if (S.sources.length === 1 && !S.existingId) {
            S.existingId = String(S.sources[0].id);
        }

        const selHtml = S.sources.map(function (s) {
            const typeCls = s.type === 'PROC' ? 'type-proc' : 'type-ws';
            const ds = window.SourceApp && window.SourceApp.dsMap ? window.SourceApp.dsMap[s.sourceDsId] : null;
            const dsName = ds ? esc(ds.name) : (s.sourceDsId ? 'DS#' + esc(s.sourceDsId) : '-');
            return '<div class="etl-source-row ' + typeCls + '" onclick="WizardApp.selectSource(' + s.id + ')" style="' +
                (S.existingId === String(s.id) ? SELECTED_CARD + 'cursor:pointer;' : '') + '">' +
                '<div class="etl-source-row-left">' +
                '<span class="etl-source-name">' + esc(s.name) + '</span> ' +
                '<span class="etl-badge ' + (s.type === 'PROC' ? 'warn' : 'info') + '">' + esc(s.type || '?') + '</span>' +
                '<span class="etl-source-meta">' + dsName + ' · ' + esc(window.etlComponents.fmtTime(s.createTime)) + '</span>' +
                '</div>' +
                '<span class="etl-badge ' + (S.existingId === String(s.id) ? 'ok' : '') + '">' +
                (S.existingId === String(s.id) ? '✓ 已选' : '点击选择') + '</span>' +
                '</div>';
        }).join('');

        let hintHtml;
        if (S.sources.length === 1) {
            hintHtml = '<div class="text-muted mt-2" style="font-size:12px;"><i class="bi bi-info-circle"></i> 仅有 1 个来源，已自动选中</div>';
        } else {
            hintHtml = '<div class="text-muted mt-2" style="font-size:12px;"><i class="bi bi-info-circle"></i> 可从下方选择抽取来源</div>';
        }

        el('wiz-body').innerHTML =
            '<h6 class="etl-card-title mb-3">选择抽取来源</h6>' +
            '<div class="etl-source-list">' + selHtml + '</div>' +
            hintHtml;

        const btnDisabled = !S.existingId ? ' disabled' : '';
        setFooter('', '<button class="etl-btn etl-btn-primary" id="wiz-next1"' + btnDisabled + '>' +
            '下一步 <i class="bi bi-arrow-right"></i></button>');

        el('wiz-next1').addEventListener('click', function () {
            if (!S.existingId) { toast('请先选择一个来源', 'warn'); return; }
            const picked = S.sources.filter(function (s) { return String(s.id) === String(S.existingId); })[0];
            if (!picked) { toast('所选来源不存在', 'err'); return; }
            window.etlStore.set({ sourceId: picked.id, source: picked, structure: null, transforms: [], mappings: [], task: null });
            window.etlStore.setStep(2);
            render(S.container);
        });
    }

    window.WizardApp = {
        selectSource: function (id) {
            S.existingId = String(id);
            paintStep1();
            const btn = el('wiz-next1');
            if (btn) btn.disabled = false;
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
