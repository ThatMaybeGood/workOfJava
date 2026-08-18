/**
 * 来源库页面（window.SourceApp）
 * - 来源卡片网格：WS 主色条 / PROC 警告色条
 * - 操作：用于新建流水线 / 调试出参（preview-debug）/ 内联编辑 / 删除
 * - 联查 datasource/list 映射 sourceDsId → 数据源名
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }

    window.SourceApp = {
        sources: [],
        dsMap: {},
        debugOpen: {},
        editOpen: {},

        render(container) {
            this.container = container;
            container.innerHTML =
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h5 class="mb-0"><i class="bi bi-collection"></i> 来源库</h5>' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.createNew()">' +
                '<i class="bi bi-plus-lg"></i> 新建来源</button>' +
                '</div>' +
                '<div id="source-grid"><div class="etl-card"><div class="etl-empty">' +
                '<div class="etl-empty-text">加载中…</div></div></div></div>';
            this.load();
        },

        async load() {
            try {
                const results = await Promise.all([
                    window.etlApi.get('/source/list'),
                    window.etlApi.get('/datasource/list')
                ]);
                this.sources = (results[0] && results[0].records) || [];
                this.dsMap = {};
                ((results[1] && results[1].records) || []).forEach((ds) => {
                    this.dsMap[ds.id] = ds;
                });
                this.renderGrid();
            } catch (e) {
                /* 错误已由 api.js toast */
                const grid = document.getElementById('source-grid');
                if (grid) {
                    grid.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                        '<div class="etl-empty-text">来源加载失败</div></div></div>';
                }
            }
        },

        renderGrid() {
            const grid = document.getElementById('source-grid');
            if (!grid) return;
            if (!this.sources.length) {
                grid.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                    '<div class="etl-empty-text">还没有来源，去创建第一条流水线</div>' +
                    '<button class="etl-btn etl-btn-primary" onclick="SourceApp.createNew()">' +
                    '<i class="bi bi-diagram-3"></i> 创建流水线</button>' +
                    '</div></div>';
                return;
            }
            grid.innerHTML = '<div class="row g-3">' + this.sources.map((s) => this.renderCard(s)).join('') + '</div>';
        },

        renderCard(s) {
            const typeCls = s.type === 'PROC' ? 'type-proc' : 'type-ws';
            const badgeCls = s.type === 'PROC' ? 'warn' : 'info';
            const ds = this.dsMap[s.sourceDsId];
            const dsName = ds ? ds.name : (s.sourceDsId ? 'DS#' + s.sourceDsId : '-');
            const meta = esc(dsName) + ' · ' + esc(C().fmtTime(s.createTime));
            return '<div class="col-12 col-lg-6">' +
                '<div class="etl-source-card ' + typeCls + ' h-100">' +
                '<div class="d-flex justify-content-between align-items-start flex-wrap gap-2">' +
                '<div>' +
                '<span class="etl-source-name">' + esc(s.name) + '</span> ' +
                '<span class="etl-badge ' + badgeCls + '">' + esc(s.type || '?') + '</span>' +
                '<div class="etl-source-meta">' + meta + '</div>' +
                '</div>' +
                '</div>' +
                '<div class="d-flex gap-2 flex-wrap mt-3">' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.useInWizard(' + s.id + ')">' +
                '<i class="bi bi-diagram-3"></i> 用于新建流水线</button>' +
                '<button class="etl-btn etl-btn-ghost" onclick="SourceApp.toggleDebug(' + s.id + ')">' +
                '<i class="bi bi-bug"></i> 调试出参</button>' +
                '<button class="etl-btn" onclick="SourceApp.toggleEdit(' + s.id + ')">' +
                '<i class="bi bi-pencil"></i> 编辑</button>' +
                '<button class="etl-btn etl-btn-danger" onclick="SourceApp.remove(' + s.id + ')">' +
                '<i class="bi bi-trash"></i> 删除</button>' +
                '</div>' +
                '<div id="src-edit-' + s.id + '" class="mt-3" style="display:none;"></div>' +
                '<div id="src-debug-' + s.id + '" class="mt-3" style="display:none;"></div>' +
                '</div></div>';
        },

        /** 新建来源：清空向导状态并跳转向导 */
        createNew() {
            window.etlStore.clear();
            location.hash = '#/wizard';
        },

        /** 用于新建流水线：载入该来源，向导回到第一步 */
        useInWizard(id) {
            const s = this.sources.find((x) => x.id === id);
            if (!s) return;
            window.etlStore.clear();
            window.etlStore.set({ sourceId: s.id, source: s });
            window.etlStore.setStep(1);
            location.hash = '#/wizard';
        },

        /** 调试出参：调 preview-debug，深色面板展示样例 */
        async toggleDebug(id) {
            const box = document.getElementById('src-debug-' + id);
            if (!box) return;
            if (this.debugOpen[id]) {
                this.debugOpen[id] = false;
                box.style.display = 'none';
                return;
            }
            this.debugOpen[id] = true;
            box.style.display = 'block';
            box.innerHTML = C().renderDebugPanel('PREVIEW DEBUG', [
                { label: 'RUNNING', status: '', content: '正在调试出参…' }
            ]);
            const s = this.sources.find((x) => x.id === id);
            try {
                const r = await window.etlApi.post('/source/preview-debug', {
                    id: s.id,
                    name: s.name,
                    type: s.type,
                    sourceDsId: s.sourceDsId,
                    configJson: s.configJson
                });
                if (!this.debugOpen[id]) return;
                const cols = (r.columns || []).map((c) => {
                    if (typeof c === 'string') return c;
                    return (c.name || c.columnName || '?') + (c.type || c.dataType ? ' : ' + (c.type || c.dataType) : '');
                });
                const rows = r.rows || [];
                box.innerHTML = C().renderDebugPanel('PREVIEW DEBUG · ' + s.name, [
                    { label: 'STATS', status: 'ok', content: 'totalRows: ' + (r.totalRows != null ? r.totalRows : rows.length) + ' · duration: ' + C().fmtDuration(r.durationMs) },
                    { label: 'COLUMNS', status: '', content: cols.join('\n') || '(无)' },
                    { label: 'SAMPLE ROWS', status: '', content: JSON.stringify(rows.slice(0, 5), null, 2) || '[]' }
                ]);
            } catch (e) {
                if (!this.debugOpen[id]) return;
                box.innerHTML = C().renderDebugPanel('PREVIEW DEBUG · ' + s.name, [
                    { label: 'ERROR', status: 'err', content: (e && e.message) || '调试失败' }
                ]);
            }
        },

        /** 内联编辑：name + 配置 JSON */
        toggleEdit(id) {
            const box = document.getElementById('src-edit-' + id);
            if (!box) return;
            if (this.editOpen[id]) {
                this.editOpen[id] = false;
                box.style.display = 'none';
                return;
            }
            this.editOpen[id] = true;
            const s = this.sources.find((x) => x.id === id);
            let pretty = s.configJson || '';
            try { pretty = JSON.stringify(JSON.parse(pretty), null, 2); } catch (e) { /* 原样展示 */ }
            box.style.display = 'block';
            box.innerHTML =
                '<div class="etl-card">' +
                '<div class="mb-2"><label class="form-label mb-1">名称</label>' +
                '<input type="text" class="form-control form-control-sm" id="src-edit-name-' + id + '" value="' + esc(s.name) + '"></div>' +
                '<div class="mb-2"><label class="form-label mb-1">配置 JSON</label>' +
                '<textarea class="form-control form-control-sm" id="src-edit-config-' + id + '" rows="8" ' +
                'style="font-family:var(--etl-font-mono);font-size:12px;">' + esc(pretty) + '</textarea></div>' +
                '<div class="d-flex gap-2">' +
                '<button class="etl-btn etl-btn-primary" onclick="SourceApp.saveEdit(' + id + ')"><i class="bi bi-check-lg"></i> 保存</button>' +
                '<button class="etl-btn" onclick="SourceApp.toggleEdit(' + id + ')">取消</button>' +
                '</div></div>';
        },

        async saveEdit(id) {
            const s = this.sources.find((x) => x.id === id);
            const name = document.getElementById('src-edit-name-' + id).value.trim();
            const configJson = document.getElementById('src-edit-config-' + id).value.trim();
            if (!name) { toast('请填写名称', 'warn'); return; }
            if (configJson) {
                try { JSON.parse(configJson); } catch (e) {
                    toast('配置 JSON 格式错误：' + e.message, 'warn');
                    return;
                }
            }
            try {
                await window.etlApi.put('/source/' + id, {
                    id: s.id,
                    name: name,
                    type: s.type,
                    sourceDsId: s.sourceDsId,
                    configJson: configJson
                });
                toast('来源已保存', 'ok');
                this.editOpen[id] = false;
                this.load();
            } catch (e) { /* 已 toast */ }
        },

        async remove(id) {
            const s = this.sources.find((x) => x.id === id);
            if (!confirm('确定删除来源「' + (s ? s.name : id) + '」？被任务引用的来源将无法删除。')) return;
            try {
                await window.etlApi.del('/source/' + id);
                toast('来源已删除', 'ok');
                this.load();
            } catch (e) {
                /* 后端拒绝（如被引用）的 message 已由 api.js toast 展示 */
            }
        }
    };
})();
