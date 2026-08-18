/**
 * 数据源管理页面（window.DatasourceApp）
 * - 长条列表：名称 + dbType + role + 连接信息摘要 + 操作
 * - Modal 表单：新建/编辑数据源，host/port/dbName 结构化输入，自动生成 JDBC URL
 * - 表/列浏览：tables → columns
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }
    const MONO = 'font-family:var(--etl-font-mono);';
    const DB_TYPES = ['ORACLE', 'MYSQL', 'SQLSERVER', 'POSTGRESQL', 'DM', 'H2'];

    let _editId = null;

    /* ---------- JDBC URL 生成器 ---------- */
    function buildJdbcUrl(dbType, host, port, dbName) {
        host = (host || '').trim();
        port = (port || '').trim();
        dbName = (dbName || '').trim();
        if (!host) return '';
        switch (dbType) {
            case 'ORACLE':
                return 'jdbc:oracle:thin:@//' + host + ':' + (port || '1521') + '/' + (dbName || 'ORCL');
            case 'MYSQL':
                return 'jdbc:mysql://' + host + ':' + (port || '3306') + '/' + (dbName || '')
                    + '?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai';
            case 'SQLSERVER':
                return 'jdbc:sqlserver://' + host + ':' + (port || '1433') + ';databaseName=' + (dbName || '');
            case 'POSTGRESQL':
                return 'jdbc:postgresql://' + host + ':' + (port || '5432') + '/' + (dbName || 'postgres');
            case 'DM':
                return 'jdbc:dm://' + host + ':' + (port || '5236') + '/' + (dbName || 'DM');
            case 'H2':
                return 'jdbc:h2:./data/' + (dbName || 'test');
            default:
                return 'jdbc:' + dbType.toLowerCase() + '://' + host + ':' + (port || '') + '/' + (dbName || '');
        }
    }

    window.DatasourceApp = {
        datasources: [],
        debugOpen: {},
        formModalOpen: false,

        render(container) {
            this.container = container;
            this._editId = null;
            container.innerHTML =
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h5 class="mb-0"><i class="bi bi-database"></i> 数据源</h5>' +
                '<button class="etl-btn etl-btn-primary" onclick="DatasourceApp.openCreate()">' +
                '<i class="bi bi-plus-lg"></i> 新建数据源</button>' +
                '</div>' +
                '<div id="ds-list"><div class="etl-card"><div class="etl-empty">' +
                '<div class="etl-empty-text">加载中…</div></div></div></div>';
            this.renderModal();
            this.load();
        },

        /* ---------- Modal（新建/编辑弹窗） ---------- */
        renderModal() {
            if (document.getElementById('ds-modal')) return;
            const overlay = document.createElement('div');
            overlay.id = 'ds-modal';
            overlay.className = 'etl-modal-overlay';
            overlay.innerHTML =
                '<div class="etl-modal etl-modal-lg">' +
                '<div class="etl-modal-header">' +
                '<span class="etl-modal-title" id="ds-modal-title">新建数据源</span>' +
                '<button class="etl-modal-close" onclick="DatasourceApp.closeModal()">&times;</button>' +
                '</div>' +
                '<div class="etl-modal-body" id="ds-modal-body"></div>' +
                '<div class="etl-modal-footer">' +
                '<button class="etl-btn" onclick="DatasourceApp.closeModal()">关闭</button>' +
                '<button class="etl-btn" onclick="DatasourceApp.testCurrent()"><i class="bi bi-plug"></i> 测试连接</button>' +
                '<button class="etl-btn etl-btn-primary" onclick="DatasourceApp.save(false)" id="ds-modal-save">保存</button>' +
                '<button class="etl-btn etl-btn-primary" onclick="DatasourceApp.save(true)" id="ds-modal-save-keep" style="display:none;">保存并继续</button>' +
                '</div>' +
                '</div>';
            document.body.appendChild(overlay);
            overlay.addEventListener('click', function (e) {
                if (e.target === overlay) DatasourceApp.closeModal();
            });
        },

        openCreate() {
            _editId = null;
            document.getElementById('ds-modal-title').textContent = '新建数据源';
            document.getElementById('ds-modal-save-keep').style.display = 'none';
            this.renderForm(null);
            document.getElementById('ds-modal').style.display = 'flex';
        },

        openEdit(id) {
            const ds = this.datasources.find(function (d) { return d.id === id; });
            if (!ds) return;
            _editId = id;
            document.getElementById('ds-modal-title').textContent = '编辑数据源：' + ds.name;
            document.getElementById('ds-modal-save-keep').style.display = 'none';
            this.renderForm(ds);
            document.getElementById('ds-modal').style.display = 'flex';
        },

        closeModal() {
            const overlay = document.getElementById('ds-modal');
            if (overlay) overlay.style.display = 'none';
            _editId = null;
        },

        renderForm(ds) {
            const db = ds || {};
            const dbType = db.dbType || 'MYSQL';
            // 从 URL 解析 host/port/dbName（首次编辑时回填）
            const parsed = this._parseUrl(db.url || '', dbType);
            const typeOptions = DB_TYPES.map(function (t) {
                return '<option value="' + t + '"' + (t === dbType ? ' selected' : '') + '>' + t + '</option>';
            }).join('');
            const roleOptions =
                '<option value="SOURCE"' + (!db.role || db.role === 'SOURCE' ? ' selected' : '') + '>SOURCE（抽取源）</option>' +
                '<option value="TARGET"' + (db.role === 'TARGET' ? ' selected' : '') + '>TARGET（目标源）</option>';

            document.getElementById('ds-modal-body').innerHTML =
                '<div class="row g-2">' +
                '<div class="col-md-6"><label class="form-label">名称</label>' +
                '<input class="form-control form-control-sm" id="ds-f-name" value="' + esc(db.name || '') + '" placeholder="如：生产 Oracle 库"></div>' +
                '<div class="col-md-3"><label class="form-label">数据库类型</label>' +
                '<select class="form-select form-select-sm" id="ds-f-dbtype" onchange="DatasourceApp.onDbTypeChange()">' + typeOptions + '</select></div>' +
                '<div class="col-md-3"><label class="form-label">用途</label>' +
                '<select class="form-select form-select-sm" id="ds-f-role">' + roleOptions + '</select></div>' +

                '<div class="col-md-4"><label class="form-label">Host / IP</label>' +
                '<input class="form-control form-control-sm" id="ds-f-host" style="' + MONO + '" value="' + esc(parsed.host || '') + '" placeholder="127.0.0.1"></div>' +
                '<div class="col-md-2"><label class="form-label">Port</label>' +
                '<input class="form-control form-control-sm" id="ds-f-port" value="' + esc(parsed.port || '') + '" placeholder="端口"></div>' +
                '<div class="col-md-6"><label class="form-label">数据库 / Service Name</label>' +
                '<input class="form-control form-control-sm" id="ds-f-dbname" value="' + esc(parsed.dbName || '') + '" placeholder="数据库名"></div>' +

                '<div class="col-12"><label class="form-label">JDBC URL</label>' +
                '<input class="form-control form-control-sm" id="ds-f-url" style="' + MONO + 'font-size:11px;" ' +
                'placeholder="自动生成，也可手动编辑" value="" readonly></div>' +
                // 密码和用户名
                '<div class="col-12"><div class="row g-2 mt-1">' +
                '<div class="col-md-6"><label class="form-label">用户名</label>' +
                '<input class="form-control form-control-sm" id="ds-f-username" value="' + esc(db.username || '') + '"></div>' +
                '<div class="col-md-6"><label class="form-label">密码' + (db.id ? '（留空不修改）' : '') + '</label>' +
                '<input type="password" class="form-control form-control-sm" id="ds-f-password" placeholder="••••••••"></div>' +
                '</div>',
                '<div class="col-12"><div class="row g-2 mt-1">' +
                '<div class="col-md-4"><label class="form-label">最大连接数</label>' +
                '<input type="number" class="form-control form-control-sm" id="ds-f-maxpool" value="' + esc(db.maxPoolSize != null ? db.maxPoolSize : '') + '" placeholder="默认"></div>' +
                '<div class="col-md-4"><label class="form-label">连接超时（ms）</label>' +
                '<input type="number" class="form-control form-control-sm" id="ds-f-timeout" value="' + esc(db.connectionTimeoutMs != null ? db.connectionTimeoutMs : '') + '" placeholder="默认"></div>' +
                '<div class="col-md-4 d-flex align-items-end">' +
                '<div class="form-check form-switch mb-0">' +
                '<input class="form-check-input" type="checkbox" id="ds-f-enabled"' + (!db.enabled || db.enabled === 1 ? ' checked' : '') + '>' +
                '<label class="form-check-label" for="ds-f-enabled">启用</label>' +
                '</div></div>' +
                '</div></div>' +
                '<div id="ds-modal-test-result" class="mt-2"></div>';

            // 初始化 URL
            this.onDbTypeChange();
        },

        onDbTypeChange() {
            const dbType = (document.getElementById('ds-f-dbtype') || {}).value || 'MYSQL';
            const host = (document.getElementById('ds-f-host') || {}).value || '';
            const port = (document.getElementById('ds-f-port') || {}).value || '';
            const dbName = (document.getElementById('ds-f-dbname') || {}).value || '';
            const urlInput = document.getElementById('ds-f-url');
            if (urlInput) urlInput.value = buildJdbcUrl(dbType, host, port, dbName);
        },

        /** 解析 JDBC URL 回填 host/port/dbName */
        _parseUrl(url, dbType) {
            if (!url) return { host: '', port: '', dbName: '' };
            try {
                let s = url;
                // 去掉参数部分
                const qIdx = s.indexOf('?');
                if (qIdx >= 0) s = s.substring(0, qIdx);
                if (/^jdbc:mysql:\/\//.test(url)) {
                    const m = url.match(/^jdbc:mysql:\/\/([^/:]+)(?::(\d+))?\/(.+)$/);
                    return { host: m ? m[1] : '', port: m ? m[2] : '3306', dbName: m ? m[3] : '' };
                }
                if (/^jdbc:postgresql:\/\//.test(url)) {
                    const m = url.match(/^jdbc:postgresql:\/\/([^/:]+)(?::(\d+))?\/(.+)$/);
                    return { host: m ? m[1] : '', port: m ? m[2] : '5432', dbName: m ? m[3] : '' };
                }
                if (/^jdbc:sqlserver:\/\//.test(url)) {
                    const m = url.match(/^jdbc:sqlserver:\/\/([^/:]+)(?::(\d+))?/);
                    const dbMatch = url.match(/;databaseName=(.+?)(?:;|$)/);
                    return { host: m ? m[1] : '', port: m ? m[2] : '1433', dbName: dbMatch ? dbMatch[1] : '' };
                }
                if (/^jdbc:oracle:thin:@\/\//.test(url)) {
                    const m = url.match(/^jdbc:oracle:thin:@\/\/([^/:]+)(?::(\d+))?\/(.+)$/);
                    return { host: m ? m[1] : '', port: m ? m[2] : '1521', dbName: m ? m[3] : '' };
                }
                if (/^jdbc:dm:\/\//.test(url)) {
                    const m = url.match(/^jdbc:dm:\/\/([^/:]+)(?::(\d+))?\/(.+)$/);
                    return { host: m ? m[1] : '', port: m ? m[2] : '5236', dbName: m ? m[3] : '' };
                }
                if (/^jdbc:h2:/.test(url)) {
                    const m = url.match(/^jdbc:h2:(.+)$/);
                    return { host: '', port: '', dbName: m ? m[1] : '' };
                }
                return { host: '', port: '', dbName: '' };
            } catch (e) {
                return { host: '', port: '', dbName: '' };
            }
        },

        async save(keepOpen) {
            const name = (document.getElementById('ds-f-name') || {}).value.trim();
            const dbType = (document.getElementById('ds-f-dbtype') || {}).value;
            const url = (document.getElementById('ds-f-url') || {}).value.trim();
            const username = (document.getElementById('ds-f-username') || {}).value.trim();
            const password = (document.getElementById('ds-f-password') || {}).value;
            const role = (document.getElementById('ds-f-role') || {}).value;
            const enabled = document.getElementById('ds-f-enabled').checked ? 1 : 0;
            if (!name) { toast('请填写名称', 'warn'); return; }
            if (!url) { toast('请填写或生成 JDBC URL', 'warn'); return; }

            const data = { name, dbType, url, username, password, role, enabled };
            const maxPool = (document.getElementById('ds-f-maxpool') || {}).value;
            const timeout = (document.getElementById('ds-f-timeout') || {}).value;
            if (maxPool !== '') data.maxPoolSize = parseInt(maxPool, 10);
            if (timeout !== '') data.connectionTimeoutMs = parseInt(timeout, 10);

            const btnSave = document.getElementById('ds-modal-save');
            if (btnSave) btnSave.disabled = true;
            try {
                if (_editId != null) {
                    await window.etlApi.put('/datasource/' + _editId, data);
                    toast('数据源已更新', 'ok');
                } else {
                    await window.etlApi.post('/datasource', data);
                    toast('数据源已保存', 'ok');
                }
                this.load();
                if (!keepOpen) this.closeModal();
            } catch (e) {
                /* 已 toast */
            } finally {
                if (btnSave) btnSave.disabled = false;
            }
        },

        async testCurrent() {
            const resultBox = document.getElementById('ds-modal-test-result');
            if (!resultBox) return;
            resultBox.innerHTML = '<span class="etl-badge">测试中…</span>';
            const url = (document.getElementById('ds-f-url') || {}).value.trim();
            const username = (document.getElementById('ds-f-username') || {}).value.trim();
            const password = (document.getElementById('ds-f-password') || {}).value;
            if (!url) { toast('请先填写 JDBC URL', 'warn'); return; }
            try {
                if (_editId != null) {
                    // 已有数据源：用后端保存的 id 测试连接
                    const r = await window.etlApi.post('/datasource/' + _editId + '/test', {});
                    if (r && r.success) {
                        resultBox.innerHTML = '<span class="etl-badge ok">连接成功 · ' + esc(r.databaseProductName || r.driverName || '') + '</span>';
                        toast('连接测试成功', 'ok');
                    } else {
                        resultBox.innerHTML = '<span class="etl-badge err">连接失败</span>';
                        toast('连接失败：' + ((r && r.error) || '未知错误'), 'err');
                    }
                } else {
                    // 新建数据源：无后端接口，告知用户先保存再测试
                    resultBox.innerHTML = '<span class="etl-badge warn">新建数据源需先保存，保存后可使用「测试连接」按钮</span>';
                }
            } catch (e) {
                resultBox.innerHTML = '<span class="etl-badge err">连接失败</span>';
            }
        },

        async load() {
            try {
                const data = await window.etlApi.get('/datasource/list');
                this.datasources = (data && data.records) || [];
                this.renderList();
            } catch (e) {
                const box = document.getElementById('ds-list');
                if (box) box.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                    '<div class="etl-empty-text">数据源加载失败</div></div></div>';
            }
        },

        renderList() {
            const box = document.getElementById('ds-list');
            if (!box) return;
            if (!this.datasources.length) {
                box.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                    '<div class="etl-empty-text">还没有数据源，点击右上角「新建数据源」创建</div>' +
                    '</div></div>';
                return;
            }
            box.innerHTML = '<div class="etl-source-list">' + this.datasources.map((ds) => this.renderRow(ds)).join('') + '</div>';
        },

        renderRow(ds) {
            const roleBadge = ds.role === 'TARGET'
                ? '<span class="etl-badge warn">TARGET</span>'
                : '<span class="etl-badge info">SOURCE</span>';
            const enabledBadge = ds.enabled === 1
                ? '<span class="etl-badge ok">启用</span>'
                : '<span class="etl-badge">禁用</span>';
            const parsed = this._parseUrl(ds.url || '', ds.dbType || 'MYSQL');
            const connInfo = parsed.host
                ? esc(parsed.host) + (parsed.port ? ':' + esc(parsed.port) : '') + '/' + esc(parsed.dbName)
                : esc(ds.url || '');
            return '<div class="etl-source-row">' +
                '<div class="etl-source-row-left">' +
                '<span style="font-weight:600;">' + esc(ds.name) + '</span> ' +
                '<span class="etl-badge">' + esc(ds.dbType || '?') + '</span> ' +
                roleBadge + ' ' + enabledBadge +
                '<span class="etl-source-meta">' + connInfo + '</span>' +
                '</div>' +
                '<div class="etl-source-row-actions">' +
                '<button class="etl-btn etl-btn-primary btn-sm" onclick="DatasourceApp.testRow(' + ds.id + ')"><i class="bi bi-plug"></i> 测试</button>' +
                '<button class="etl-btn btn-sm" onclick="DatasourceApp.toggleBrowse(' + ds.id + ')"><i class="bi bi-table"></i> 浏览</button>' +
                '<button class="etl-btn btn-sm" onclick="DatasourceApp.openEdit(' + ds.id + ')"><i class="bi bi-pencil"></i> 编辑</button>' +
                '<button class="etl-btn etl-btn-danger btn-sm" onclick="DatasourceApp.remove(' + ds.id + ')"><i class="bi bi-trash"></i> 删除</button>' +
                '</div>' +
                '<div id="ds-browse-' + ds.id + '" class="etl-debug-expand" style="display:none;"></div>' +
                '</div>';
        },

        async testRow(id) {
            const box = document.getElementById('ds-browse-' + id);
            if (!box) return;
            box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);"><span class="etl-badge">测试中…</span></div>';
            try {
                const r = await window.etlApi.post('/datasource/' + id + '/test', {});
                if (r && r.success) {
                    box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);">' +
                        '<span class="etl-badge ok">连接成功 · ' + esc(r.databaseProductName || r.driverName || '') + '</span></div>';
                    toast('连接测试成功', 'ok');
                } else {
                    box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);">' +
                        '<span class="etl-badge err">连接失败</span></div>';
                    toast('连接失败：' + ((r && r.error) || '未知错误'), 'err');
                }
            } catch (e) {
                box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);">' +
                    '<span class="etl-badge err">连接失败</span></div>';
            }
        },

        /* ---------- 表/列浏览 ---------- */
        async toggleBrowse(id) {
            const box = document.getElementById('ds-browse-' + id);
            if (!box) return;
            if (this.debugOpen[id]) {
                this.debugOpen[id] = false;
                box.style.display = 'none';
                return;
            }
            this.debugOpen[id] = true;
            box.style.display = 'block';
            if (this.tablesCache && this.tablesCache[id]) {
                this._renderColumns(box, this.tablesCache[id], table);
                return;
            }
            box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);"><span class="etl-badge">加载表列表…</span></div>';
            try {
                const r = await window.etlApi.get('/datasource/' + id + '/tables');
                const tables = (r || []).map(function (t) {
                    return typeof t === 'string' ? t : (t.tableName || t.name || String(t));
                });
                if (!this.tablesCache) this.tablesCache = {};
                this.tablesCache[id] = tables;
                this.renderTables(id);
            } catch (e) {
                box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);color:var(--etl-color-danger);">表列表加载失败</div>';
            }
        },

        renderTables(id) {
            const box = document.getElementById('ds-browse-' + id);
            if (!box) return;
            const tables = (this.tablesCache && this.tablesCache[id]) || [];
            if (!tables.length) {
                box.innerHTML = '<div class="etl-debug-expand" style="padding:var(--etl-space-2);"><span class="etl-source-meta">无表</span></div>';
                return;
            }
            const listHtml = tables.map(function (t) {
                const safeName = esc(t).replace(/'/g, "\\'");
                return '<button class="etl-btn etl-btn-ghost btn-sm" style="' + MONO + 'font-size:11px;padding:2px 8px;" ' +
                    'onclick="DatasourceApp.showColumns(' + id + ',\'' + safeName + '\')">' + esc(t) + '</button>';
            }).join('');
            box.innerHTML =
                '<div class="etl-debug-expand" style="padding:var(--etl-space-2);">' +
                '<div style="font-size:12px;color:var(--etl-color-text-weak);margin-bottom:var(--etl-space-2);">' + tables.length + ' 张表</div>' +
                '<div style="display:flex;flex-wrap:wrap;gap:var(--etl-space-1);max-height:160px;overflow:auto;">' + listHtml + '</div>' +
                '<div id="ds-cols-' + id + '" class="mt-2"></div>' +
                '</div>';
        },

        async showColumns(id, table) {
            const box = document.getElementById('ds-cols-' + id);
            if (!box) return;
            box.innerHTML = '<div class="etl-source-meta">加载列…</div>';
            const cacheKey = id + ':' + table;
            if (this.columnsCache && this.columnsCache[cacheKey]) {
                this._renderColumns(box, this.columnsCache[cacheKey], table);
                return;
            }
            try {
                const r = await window.etlApi.get('/datasource/' + id + '/columns/' + encodeURIComponent(table));
                const cols = (r || []).map(function (c) {
                    if (typeof c === 'string') return { name: c, type: '' };
                    return { name: c.name || c.columnName || '?', type: c.type || c.dataType || '' };
                });
                if (!this.columnsCache) this.columnsCache = {};
                this.columnsCache[cacheKey] = cols;
                this._renderColumns(box, cols, table);
            } catch (e) {
                box.innerHTML = '<div style="color:var(--etl-color-danger);font-size:12px;">列信息加载失败</div>';
            }
        },

        _renderColumns(box, cols, tableName) {
            if (!box) return;
            box.innerHTML =
                '<div style="font-family:var(--etl-font-mono);font-size:12px;border:1px solid var(--etl-color-border);' +
                'border-radius:var(--etl-radius-sm);padding:var(--etl-space-2) var(--etl-space-3);max-height:220px;overflow:auto;">' +
                '<div style="color:var(--etl-color-primary);font-weight:600;margin-bottom:4px;">' + esc(tableName || '') + ' · ' + cols.length + ' 列</div>' +
                cols.map(function (c) {
                    return '<div style="display:flex;justify-content:space-between;gap:16px;padding:3px 0;border-bottom:1px solid var(--etl-color-border);">' +
                        '<span>' + esc(c.name) + '</span>' +
                        '<span style="color:var(--etl-color-text-weak);">' + esc(c.type) + '</span>' +
                        '</div>';
                }).join('') +
                '</div>';
        },

        remove(id) {
            const ds = this.datasources.find(function (d) { return d.id === id; });
            if (!confirm('确定删除数据源「' + (ds ? ds.name : id) + '」？')) return;
            window.etlApi.del('/datasource/' + id).then(function () {
                toast('数据源已删除', 'ok');
                DatasourceApp.load();
            }).catch(function () { /* 已 toast */ });
        }
    };
})();
