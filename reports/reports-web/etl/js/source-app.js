/**
 * 来源库页面（window.SourceApp）
 * - 来源卡片网格：WS 主色条 / PROC 警告色条
 * - 操作：新建来源（Modal，支持连续新建）/ 编辑来源（复用 Modal）
 *   / 用于新建流水线 / 调试出参（全宽列表展示）/ 删除
 * - 联查 datasource/list 映射 sourceDsId → 数据源名
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }
    const MONO = 'font-family:var(--etl-font-mono);';
    const TARGET_TYPES = ['string', 'number', 'date', 'boolean'];
    let _editId = null; // 当前编辑的来源 ID，null 表示新建

    window.SourceApp = {
        sources: [],
        dsMap: {},
        debugOpen: {},
        currentPage: 1,
        pageSize: 10,
        total: 0,
        keyword: '',
        typeFilter: '',

        render(container) {
            this.container = container;
            this.currentPage = 1;
            this.pageSize = 10;
            this.total = 0;
            this.keyword = '';
            this.typeFilter = '';
            container.innerHTML =
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h5 class="mb-0"><i class="bi bi-collection"></i> 来源库</h5>' +
                '<div class="d-flex gap-2">' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.openCreate()"><i class="bi bi-plus-lg"></i> 新建来源</button>' +
                '</div>' +
                '</div>' +
                '<div class="d-flex flex-wrap gap-2 mb-3 align-items-center">' +
                '<div class="input-group" style="max-width:320px;">' +
                '<input type="text" class="form-control form-control-sm" id="source-search-keyword" placeholder="搜索来源名称" ' +
                'onkeyup="if(event.key===\'Enter\')SourceApp.onSearch()">' +
                '<button class="etl-btn btn-sm" onclick="SourceApp.onSearch()"><i class="bi bi-search"></i></button>' +
                '</div>' +
                '<div class="btn-group" role="group" id="source-filter-tabs">' +
                '<button class="etl-btn btn-sm ' + (this.typeFilter === '' ? 'etl-btn-primary' : '') + '" onclick="SourceApp.onTypeFilter(\'\')">全部</button>' +
                '<button class="etl-btn btn-sm ' + (this.typeFilter === 'PROC' ? 'etl-btn-primary' : '') + '" onclick="SourceApp.onTypeFilter(\'PROC\')">PROC</button>' +
                '<button class="etl-btn btn-sm ' + (this.typeFilter === 'WS' ? 'etl-btn-primary' : '') + '" onclick="SourceApp.onTypeFilter(\'WS\')">WS</button>' +
                '</div>' +
                '</div>' +
                '<div id="source-grid"><div class="etl-card"><div class="etl-empty">' +
                '<div class="etl-empty-text">加载中…</div></div></div></div>' +
                '<div id="source-pagination" class="mt-3"></div>';
            this.renderModal();
            this.load();
        },

        /* ---------- Modal HTML（只生成一次，挂载在 body） ---------- */
        renderModal() {
            if (document.getElementById('source-modal')) return;
            const overlay = document.createElement('div');
            overlay.id = 'source-modal';
            overlay.className = 'etl-modal-overlay';
            overlay.innerHTML =
                '<div class="etl-modal etl-modal-lg">' +
                '<div class="etl-modal-header">' +
                '<span class="etl-modal-title" id="sm-title">新建来源</span>' +
                '<button class="etl-modal-close" onclick="SourceApp.closeModal()">&times;</button>' +
                '</div>' +
                '<div class="etl-modal-body" id="sm-body">' +
                '<div class="etl-empty"><div class="etl-empty-text">加载中…</div></div>' +
                '</div>' +
                '<div class="etl-modal-footer">' +
                '<button class="etl-btn" onclick="SourceApp.closeModal()">关闭</button>' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.saveModal(false)" id="sm-save">保存</button>' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.saveModal(true)" id="sm-save-keep" style="display:none;">保存并继续</button>' +
                '</div>' +
                '</div>';
            document.body.appendChild(overlay);
            overlay.addEventListener('click', function (e) {
                if (e.target === overlay) SourceApp.closeModal();
            });
        },

        /** 打开新建弹窗 */
        openCreate() {
            _editId = null;
            document.getElementById('sm-title').textContent = '新建来源';
            document.getElementById('sm-save').style.display = '';
            document.getElementById('sm-save-keep').style.display = 'none';
            this._modalType = 'PROC';
            this._modalForm = {};
            this.renderModalBody();
            document.getElementById('source-modal').style.display = 'flex';
        },

        /** 打开编辑弹窗（复用新建表单） */
        openEdit(id) {
            const s = this.sources.find(function (x) { return x.id === id; });
            if (!s) return;
            _editId = id;
            document.getElementById('sm-title').textContent = '编辑来源：' + s.name;
            document.getElementById('sm-save').style.display = '';
            document.getElementById('sm-save-keep').style.display = 'none';
            try { this._modalForm = JSON.parse(s.configJson || '{}'); } catch (e) { this._modalForm = {}; }
            this._modalType = s.type || 'PROC';
            this.renderModalBody();
            document.getElementById('source-modal').style.display = 'flex';
        },

        closeModal() {
            const overlay = document.getElementById('source-modal');
            if (overlay) overlay.style.display = 'none';
            _editId = null;
        },

        renderModalBody() {
            const body = document.getElementById('sm-body');
            if (!body) return;
            const type = this._modalType || 'PROC';
            const f = this._modalForm || {};
            const isProc = type === 'PROC';

            // 数据源下拉
            const dsOpts = isProc
                ? '<option value="">请选择源数据库</option>' +
                  (Object.values(this.dsMap || {}).filter(function (d) { return d.role === 'SOURCE'; })
                      .map(function (d) {
                          return '<option value="' + esc(d.id) + '"' +
                              (String(d.id) === String(f.procDs) ? ' selected' : '') + '>' +
                              esc(d.name) + '（' + esc(d.dbType) + '）</option>';
                      }).join(''))
                : '<div class="text-muted" style="font-size:12px;">接口调用无需选择数据库</div>';

            const typeTabs =
                '<div class="d-flex gap-2 mb-3">' +
                '<button class="etl-btn' + (isProc ? ' etl-btn-primary' : '') + '" onclick="SourceApp.setModalType(\'PROC\')">' +
                '<i class="bi bi-database"></i> 存储过程</button>' +
                '<button class="etl-btn' + (!isProc ? ' etl-btn-primary' : '') + '" onclick="SourceApp.setModalType(\'WS\')">' +
                '<i class="bi bi-globe"></i> 接口调用</button>' +
                '</div>';

            let formHtml;
            if (isProc) {
                formHtml =
                    '<div class="row g-2">' +
                    '<div class="col-md-6"><label class="form-label">名称</label>' +
                    '<input class="form-control form-control-sm" id="sm-name" placeholder="如：订单抽取" value="' + esc(f.name || '') + '"></div>' +
                    '<div class="col-md-6"><label class="form-label">源数据库</label>' +
                    '<select class="form-select form-select-sm" id="sm-proc-ds">' + dsOpts + '</select></div>' +
                    '<div class="col-md-6"><label class="form-label">存储过程名</label>' +
                    '<input class="form-control form-control-sm" id="sm-proc-name" style="' + MONO + '" placeholder="PKG_ETL.PRC_EXPORT" value="' + esc(f.procName || '') + '"></div>' +
                    '<div class="col-md-6"><label class="form-label">调用模板</label>' +
                    '<input class="form-control form-control-sm" id="sm-proc-call" style="' + MONO + '" placeholder="{call PRC_EXPORT(?, ?)}" value="' + esc(f.callTemplate || '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">游标参数名</label>' +
                    '<input class="form-control form-control-sm" id="sm-proc-cursor-name" style="' + MONO + '" value="' + esc(f.cursorParamName || '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">游标位置</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-proc-cursor-idx" value="' + esc(f.cursorParamIdx != null ? f.cursorParamIdx : '') + '" min="1"></div>' +
                    '<div class="col-md-4"><label class="form-label">IN 参数 JSON</label>' +
                    '<input class="form-control form-control-sm" id="sm-proc-inparams" style="' + MONO + '" placeholder="[{\"name\":\"p_date\",\"value\":\"20260101\"}]" value="' + esc(f.inParamsJson || '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">最大页数</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-max-pages" min="1" value="' + esc(f.maxPages != null ? f.maxPages : '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">最大行数</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-max-rows" min="1" value="' + esc(f.maxRows != null ? f.maxRows : '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">批大小</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-batch" min="1" value="' + esc(f.batchSize != null ? f.batchSize : '') + '"></div>' +
                    '</div>';
            } else {
                const wsType = f.wsType || 'REST';
                const isSoap = wsType === 'SOAP';
                formHtml =
                    '<div class="row g-2">' +
                    '<div class="col-md-6"><label class="form-label">名称</label>' +
                    '<input class="form-control form-control-sm" id="sm-name" placeholder="如：用户信息接口" value="' + esc(f.name || '') + '"></div>' +
                    '<div class="col-md-6"><label class="form-label">接口类型</label>' +
                    '<select class="form-select form-select-sm" id="sm-ws-type">' +
                    '<option value="REST"' + (!isSoap ? ' selected' : '') + '>REST</option>' +
                    '<option value="SOAP"' + (isSoap ? ' selected' : '') + '>SOAP</option></select></div>' +
                    '<div class="col-12"><label class="form-label">URL</label>' +
                    '<input class="form-control form-control-sm" id="sm-ws-url" style="' + MONO + '" placeholder="https://example.com/api/data" value="' + esc(f.url || '') + '"></div>' +
                    (isSoap ? '<div class="col-12"><label class="form-label">SOAPAction</label>' +
                        '<input class="form-control form-control-sm" id="sm-ws-soap" style="' + MONO + '" value="' + esc(f.soapAction || '') + '"></div>' : '') +
                    '<div class="col-md-6"><label class="form-label">请求头 JSON</label>' +
                    '<textarea class="form-control form-control-sm" id="sm-ws-headers" rows="2" style="' + MONO + '" placeholder=\'{"Authorization":"Bearer ..."}\'>' + esc(f.headersJson || '') + '</textarea></div>' +
                    '<div class="col-md-6"><label class="form-label">请求体模板</label>' +
                    '<textarea class="form-control form-control-sm" id="sm-ws-body" rows="2" style="' + MONO + '">' + esc(f.requestBodyTemplate || '') + '</textarea></div>' +
                    '<div class="col-md-6"><label class="form-label">出参路径 responsePath</label>' +
                    '<input class="form-control form-control-sm" id="sm-ws-path" style="' + MONO + '" placeholder="data.list" value="' + esc(f.responsePath || '') + '"></div>' +
                    '<div class="col-md-6"><label class="form-label">分页参数 JSON</label>' +
                    '<textarea class="form-control form-control-sm" id="sm-ws-extract" rows="2" style="' + MONO + '" placeholder=\'{"pageNo":"{page}"}\'>' + esc(f.extractParamsJson || '') + '</textarea></div>' +
                    '<div class="col-md-4"><label class="form-label">最大页数</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-max-pages" min="1" value="' + esc(f.maxPages != null ? f.maxPages : '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">最大行数</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-max-rows" min="1" value="' + esc(f.maxRows != null ? f.maxRows : '') + '"></div>' +
                    '<div class="col-md-4"><label class="form-label">批大小</label>' +
                    '<input type="number" class="form-control form-control-sm" id="sm-batch" min="1" value="' + esc(f.batchSize != null ? f.batchSize : '') + '"></div>' +
                    '</div>';
            }
            body.innerHTML = typeTabs + formHtml;
        },

        setModalType(type) {
            this._modalType = type;
            this.renderModalBody();
        },

        async saveModal(keepOpen) {
            const name = (document.getElementById('sm-name') || {}).value.trim();
            if (!name) { toast('请填写来源名称', 'warn'); return; }
            const type = this._modalType || 'PROC';
            const btnSave = document.getElementById('sm-save');
            if (btnSave) btnSave.disabled = true;

            let built;
            if (type === 'PROC') {
                const procDs = (document.getElementById('sm-proc-ds') || {}).value;
                const procName = (document.getElementById('sm-proc-name') || {}).value.trim();
                if (!procDs) { toast('请选择源数据库', 'warn'); if (btnSave) btnSave.disabled = false; return; }
                if (!procName) { toast('请填写存储过程名', 'warn'); if (btnSave) btnSave.disabled = false; return; }
                const inParams = (document.getElementById('sm-proc-inparams') || {}).value.trim();
                if (inParams) { try { JSON.parse(inParams); } catch (e) { toast('IN 参数 JSON 格式错误', 'warn'); if (btnSave) btnSave.disabled = false; return; } }
                built = {
                    sourceDsId: parseInt(procDs, 10),
                    config: {
                        procName: procName,
                        callTemplate: (document.getElementById('sm-proc-call') || {}).value.trim(),
                        cursorParamName: (document.getElementById('sm-proc-cursor-name') || {}).value.trim(),
                        cursorParamIdx: numOrNull((document.getElementById('sm-proc-cursor-idx') || {}).value),
                        inParamsJson: inParams,
                        maxPages: numOrNull((document.getElementById('sm-max-pages') || {}).value),
                        maxRows: numOrNull((document.getElementById('sm-max-rows') || {}).value),
                        batchSize: numOrNull((document.getElementById('sm-batch') || {}).value)
                    }
                };
            } else {
                const wsUrl = (document.getElementById('sm-ws-url') || {}).value.trim();
                if (!wsUrl) { toast('请填写接口 URL', 'warn'); if (btnSave) btnSave.disabled = false; return; }
                const wsType = (document.getElementById('sm-ws-type') || {}).value || 'REST';
                const isSoap = wsType === 'SOAP';
                const headersJson = (document.getElementById('sm-ws-headers') || {}).value.trim();
                if (headersJson) { try { JSON.parse(headersJson); } catch (e) { toast('请求头 JSON 格式错误', 'warn'); if (btnSave) btnSave.disabled = false; return; } }
                const extractJson = (document.getElementById('sm-ws-extract') || {}).value.trim();
                if (extractJson) { try { JSON.parse(extractJson); } catch (e) { toast('分页参数 JSON 格式错误', 'warn'); if (btnSave) btnSave.disabled = false; return; } }
                built = {
                    sourceDsId: null,
                    config: {
                        wsType: wsType,
                        url: wsUrl,
                        soapAction: isSoap ? (document.getElementById('sm-ws-soap') || {}).value.trim() : null,
                        requestBodyTemplate: (document.getElementById('sm-ws-body') || {}).value.trim(),
                        responsePath: (document.getElementById('sm-ws-path') || {}).value.trim(),
                        headersJson: headersJson,
                        extractParamsJson: extractJson,
                        maxPages: numOrNull((document.getElementById('sm-max-pages') || {}).value),
                        maxRows: numOrNull((document.getElementById('sm-max-rows') || {}).value),
                        batchSize: numOrNull((document.getElementById('sm-batch') || {}).value)
                    }
                };
            }

            try {
                if (_editId != null) {
                    // 编辑模式
                    await window.etlApi.put('/source/' + _editId, {
                        id: _editId,
                        name: name,
                        type: type,
                        sourceDsId: built.sourceDsId,
                        configJson: JSON.stringify(built.config)
                    });
                    toast('来源已更新', 'ok');
                } else {
                    // 新建模式
                    await window.etlApi.post('/source', {
                        name: name,
                        type: type,
                        sourceDsId: built.sourceDsId,
                        configJson: JSON.stringify(built.config)
                    });
                    toast('来源已保存', 'ok');
                }
                this.load();
                if (!keepOpen) {
                    this.closeModal();
                    _editId = null;
                }
            } catch (e) {
                /* 错误已由 api.js toast 展示 */
            } finally {
                if (btnSave) btnSave.disabled = false;
            }
        },

        async load() {
            try {
                const results = await Promise.all([
                    window.etlApi.get('/source/list?page=' + this.currentPage + '&size=' + this.pageSize +
                        '&keyword=' + encodeURIComponent(this.keyword || '') +
                        '&type=' + encodeURIComponent(this.typeFilter || '')),
                    window.etlApi.get('/datasource/list?size=1000')
                ]);
                const srcData = results[0] || {};
                let records = [];
                let total = 0;
                if (Array.isArray(srcData)) {
                    records = srcData;
                    total = srcData.length;
                } else {
                    records = srcData.records || [];
                    total = srcData.total != null ? srcData.total : records.length;
                }
                this.sources = records;
                this.total = total;
                this.dsMap = {};
                const dsData = results[1] || {};
                const dsRecords = Array.isArray(dsData) ? dsData : (dsData.records || []);
                dsRecords.forEach((ds) => {
                    this.dsMap[ds.id] = ds;
                });
                this.renderGrid();
            } catch (e) {
                const grid = document.getElementById('source-grid');
                if (grid) {
                    grid.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                        '<div class="etl-empty-text">来源加载失败</div></div></div>';
                }
                this.renderPagination();
            }
        },

        renderGrid() {
            const grid = document.getElementById('source-grid');
            if (!grid) return;
            if (!this.sources.length) {
                grid.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                    '<div class="etl-empty-text">还没有来源，点击右上角「新建来源」创建</div>' +
                    '</div></div>';
                this.renderPagination();
                return;
            }
            grid.innerHTML = '<div class="etl-source-list">' + this.sources.map((s) => this.renderCard(s)).join('') + '</div>';
            this.renderPagination();
        },

        onSearch() {
            const input = document.getElementById('source-search-keyword');
            this.keyword = (input && input.value || '').trim();
            this.currentPage = 1;
            this.load();
        },

        onTypeFilter(type) {
            this.typeFilter = type;
            this.currentPage = 1;
            this.renderFilters();
            this.load();
        },

        renderFilters() {
            const tabs = document.getElementById('source-filter-tabs');
            if (!tabs) return;
            const types = ['', 'PROC', 'WS'];
            const self = this;
            tabs.querySelectorAll('button').forEach(function (btn, idx) {
                if (types[idx] === self.typeFilter) {
                    btn.classList.add('etl-btn-primary');
                } else {
                    btn.classList.remove('etl-btn-primary');
                }
            });
        },

        renderPagination() {
            const paginationContainer = document.getElementById('source-pagination');
            if (!paginationContainer) return;
            const comp = C();
            if (typeof comp.renderPagination !== 'function' || typeof comp.bindPagination !== 'function') {
                paginationContainer.innerHTML = '';
                return;
            }
            paginationContainer.innerHTML = comp.renderPagination({
                total: this.total, page: this.currentPage, size: this.pageSize
            });
            comp.bindPagination(paginationContainer, function (page) {
                SourceApp.currentPage = page;
                SourceApp.load();
            });
        },

        renderCard(s) {
            const typeCls = s.type === 'PROC' ? 'type-proc' : 'type-ws';
            const ds = this.dsMap[s.sourceDsId];
            const dsName = ds ? esc(ds.name) : (s.sourceDsId ? 'DS#' + esc(s.sourceDsId) : '-');
            const time = esc(C().fmtTime(s.createTime));
            return '<div class="etl-source-row ' + typeCls + '">' +
                '<div class="etl-source-row-left">' +
                '<span class="etl-source-name">' + esc(s.name) + '</span>' +
                '<span class="etl-badge etl-badge-' + (s.type === 'PROC' ? 'warn' : 'info') + '">' + esc(s.type || '?') + '</span>' +
                '<span class="etl-source-meta">' + dsName + ' · ' + time + '</span>' +
                '</div>' +
                '<div class="etl-source-row-actions">' +
                '<button class="etl-btn etl-btn-primary btn-sm" onclick="SourceApp.useInWizard(' + s.id + ')"><i class="bi bi-diagram-3"></i> 新建流水线</button>' +
                '<button class="etl-btn btn-sm" onclick="SourceApp.toggleDebug(' + s.id + ')"><i class="bi bi-bug"></i> 调试</button>' +
                '<button class="etl-btn btn-sm" onclick="SourceApp.openEdit(' + s.id + ')"><i class="bi bi-pencil"></i> 编辑</button>' +
                '<button class="etl-btn etl-btn-danger btn-sm" onclick="SourceApp.remove(' + s.id + ')"><i class="bi bi-trash"></i> 删除</button>' +
                '</div>' +
                '</div>';
        },

        useInWizard(id) {
            const s = this.sources.find((x) => x.id === id);
            if (!s) return;
            window.etlStore.clear();
            window.etlStore.set({ sourceId: s.id, source: s });
            window.etlStore.setStep(1);
            location.hash = '#/wizard';
        },

        /**
         * 调试出参：独立全屏面板，标题栏固定，表格区域滚动
         */
        async toggleDebug(id) {
            // 如果当前正在查看其他来源的调试面板，先切换
            if (this.debugOpen[id]) {
                this.debugOpen[id] = false;
                this._closeDebugPanel();
                return;
            }
            // 关闭所有已打开的调试面板
            Object.keys(this.debugOpen).forEach(k => { this.debugOpen[k] = false; });
            this.debugOpen[id] = true;
            this._renderDebugPanel(id);
        },

        _closeDebugPanel() {
            const overlay = document.getElementById('src-debug-panel');
            if (overlay) overlay.remove();
        },

        async _renderDebugPanel(id) {
            // 移除旧面板
            this._closeDebugPanel();

            const s = this.sources.find((x) => x.id === id);
            if (!s) return;

            const overlay = document.createElement('div');
            overlay.id = 'src-debug-panel';
            overlay.className = 'etl-debug-overlay';
            overlay.innerHTML =
                '<div class="etl-debug-panel-inner">' +
                '<div class="etl-debug-panel-header">' +
                '<h6 class="mb-0"><i class="bi bi-bug"></i> 调试出参 · <span id="dbg-src-name"></span></h6>' +
                '<div class="d-flex align-items-center gap-2">' +
                '<span id="dbg-status" class="etl-badge">加载中…</span>' +
                '<button class="etl-btn etl-btn-ghost" style="padding:2px 6px;font-size:12px;" onclick="SourceApp.toggleDebug(' + id + ')"><i class="bi bi-x-lg"></i> 关闭</button>' +
                '</div>' +
                '</div>' +
                '<div class="etl-debug-panel-body" id="dbg-body">' +
                '<div class="etl-empty"><div class="etl-empty-text">正在调试出参…</div></div>' +
                '</div>' +
                '</div>';
            document.body.appendChild(overlay);
            document.getElementById('dbg-src-name').textContent = s.name || id;

            try {
                const r = await window.etlApi.post('/source/preview-debug', {
                    id: s.id,
                    name: s.name,
                    type: s.type,
                    sourceDsId: s.sourceDsId,
                    configJson: s.configJson
                });
                if (!this.debugOpen[id]) return;

                const cols = (r.columns || []).map(function (c) {
                    if (typeof c === 'string') return c;
                    return (c.name || c.columnName || '?') + (c.type || c.dataType ? ' : ' + (c.type || c.dataType) : '');
                });
                const rows = r.rows || [];

                document.getElementById('dbg-status').className = 'etl-badge ok';
                document.getElementById('dbg-status').textContent = rows.length + ' 行 · ' + C().fmtDuration(r.durationMs);

                // 列头
                let thead = '<tr>' + cols.map(function (c) {
                    return '<th style="' + MONO + 'font-size:11px;white-space:nowrap;">' + esc(c) + '</th>';
                }).join('') + '</tr>';

                // 行数据
                let tbody = rows.slice(0, 100).map(function (row) {
                    return '<tr>' + cols.map(function (c) {
                        const v = Array.isArray(row) ? row[cols.indexOf(c)] : row[c];
                        return '<td style="' + MONO + 'font-size:11px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">' +
                            esc(v == null ? '' : (typeof v === 'object' ? JSON.stringify(v).substring(0, 80) : String(v))) + '</td>';
                    }).join('') + '</tr>';
                }).join('');

                document.getElementById('dbg-body').innerHTML =
                    '<div class="debug-table-wrap-full">' +
                    '<table class="table table-sm table-hover mb-0">' +
                    '<thead class="table-light position-sticky top-0">' + thead + '</thead>' +
                    '<tbody>' + tbody + '</tbody>' +
                    '</table></div>' +
                    (rows.length > 100 ? '<div class="text-muted mt-2" style="font-size:12px;">仅展示前 100 行，共 ' + rows.length + ' 行</div>' : '');
            } catch (e) {
                if (!this.debugOpen[id]) return;
                document.getElementById('dbg-status').className = 'etl-badge err';
                document.getElementById('dbg-status').textContent = '调试失败';
                document.getElementById('dbg-body').innerHTML =
                    '<div class="etl-empty"><div class="etl-empty-text" style="color:var(--etl-color-danger);">' + esc((e && e.message) || '') + '</div></div>';
            }
        },

        remove(id) {
            const s = this.sources.find((x) => x.id === id);
            if (!confirm('确定删除来源「' + (s ? s.name : id) + '」？被任务引用的来源将无法删除。')) return;
            window.etlApi.del('/source/' + id).then(function () {
                toast('来源已删除', 'ok');
                SourceApp.load();
            }).catch(function () { /* 错误已由 api.js toast */ });
        }
    };

    function numOrNull(v) {
        const n = parseInt(v, 10);
        return isNaN(n) ? null : n;
    }
})();
