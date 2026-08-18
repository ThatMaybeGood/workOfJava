/**
 * 数据源管理页面（window.DatasourceApp）
 * - 卡片式列表：名称 + dbType 徽标 + role 徽标 + url mono 弱字 + 操作
 * - 内联 .etl-card 表单：新建/编辑 + 测试连接（.etl-badge ok/err 内联）
 * - 表/列浏览：tables → columns（mono 列名 + 类型）
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }

    const DB_TYPES = ['ORACLE', 'MYSQL', 'SQLSERVER', 'POSTGRESQL', 'DM', 'H2'];

    window.DatasourceApp = {
        datasources: [],
        editingId: null,
        formOpen: false,
        browseOpen: {},
        tablesCache: {},
        columnsCache: {},

        render(container) {
            this.container = container;
            this.editingId = null;
            this.formOpen = false;
            container.innerHTML =
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h5 class="mb-0"><i class="bi bi-database"></i> 数据源</h5>' +
                '<button class="etl-btn etl-btn-primary" id="ds-new-btn" onclick="DatasourceApp.showForm(null)">' +
                '<i class="bi bi-plus-lg"></i> 新建数据源</button>' +
                '</div>' +
                '<div id="ds-form-wrap" class="mb-3" style="display:none;"></div>' +
                '<div id="ds-list"><div class="etl-card"><div class="etl-empty">' +
                '<div class="etl-empty-text">加载中…</div></div></div></div>';
            this.load();
        },

        async load() {
            try {
                const data = await window.etlApi.get('/datasource/list');
                this.datasources = (data && data.records) || [];
                this.renderList();
            } catch (e) {
                const box = document.getElementById('ds-list');
                if (box) {
                    box.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                        '<div class="etl-empty-text">数据源加载失败</div></div></div>';
                }
            }
        },

        renderList() {
            const box = document.getElementById('ds-list');
            if (!box) return;
            if (!this.datasources.length) {
                box.innerHTML = '<div class="etl-card"><div class="etl-empty">' +
                    '<div class="etl-empty-text">还没有数据源</div>' +
                    '<button class="etl-btn etl-btn-primary" onclick="DatasourceApp.showForm(null)">' +
                    '<i class="bi bi-plus-lg"></i> 新建数据源</button>' +
                    '</div></div>';
                return;
            }
            box.innerHTML = '<div class="etl-card" style="padding:0;">' +
                this.datasources.map((ds) => this.renderRow(ds)).join('') + '</div>';
        },

        renderRow(ds) {
            const roleBadge = ds.role === 'TARGET'
                ? '<span class="etl-badge warn">TARGET</span>'
                : '<span class="etl-badge info">SOURCE</span>';
            const enabledBadge = ds.enabled === 1
                ? '<span class="etl-badge ok">启用</span>'
                : '<span class="etl-badge">禁用</span>';
            return '<div style="padding:var(--etl-space-3) var(--etl-space-4);border-bottom:1px solid var(--etl-color-border);">' +
                '<div class="d-flex justify-content-between align-items-start flex-wrap gap-2">' +
                '<div>' +
                '<span style="font-weight:500;">' + esc(ds.name) + '</span> ' +
                '<span class="etl-badge">' + esc(ds.dbType || '?') + '</span> ' +
                roleBadge + ' ' + enabledBadge +
                '<div class="etl-source-meta" style="font-family:var(--etl-font-mono);font-size:12px;color:var(--etl-color-text-weak);margin-top:4px;">' +
                esc(ds.url || '') + '</div>' +
                '</div>' +
                '<div class="d-flex gap-2 flex-wrap align-items-center">' +
                '<span id="ds-test-' + ds.id + '"></span>' +
                '<button class="etl-btn etl-btn-ghost" onclick="DatasourceApp.test(' + ds.id + ')">' +
                '<i class="bi bi-plug"></i> 测试连接</button>' +
                '<button class="etl-btn etl-btn-ghost" onclick="DatasourceApp.toggleBrowse(' + ds.id + ')">' +
                '<i class="bi bi-table"></i> 浏览表结构</button>' +
                '<button class="etl-btn" onclick="DatasourceApp.showForm(' + ds.id + ')">' +
                '<i class="bi bi-pencil"></i> 编辑</button>' +
                '<button class="etl-btn etl-btn-danger" onclick="DatasourceApp.remove(' + ds.id + ')">' +
                '<i class="bi bi-trash"></i> 删除</button>' +
                '</div>' +
                '</div>' +
                '<div id="ds-browse-' + ds.id + '" style="display:none;"></div>' +
                '</div>';
        },

        /* ---------- 表单 ---------- */

        showForm(id) {
            this.editingId = id || null;
            this.formOpen = true;
            const ds = id ? this.datasources.find((d) => d.id === id) : null;
            const wrap = document.getElementById('ds-form-wrap');
            const typeOptions = DB_TYPES.map((t) =>
                '<option value="' + t + '"' + (ds && ds.dbType === t ? ' selected' : '') + '>' + t + '</option>'
            ).join('');
            wrap.style.display = 'block';
            wrap.innerHTML =
                '<div class="etl-card">' +
                '<h6 class="etl-card-title">' + (ds ? '编辑数据源 #' + ds.id : '新建数据源') + '</h6>' +
                '<div class="row g-3">' +
                '<div class="col-md-6"><label class="form-label">名称</label>' +
                '<input type="text" class="form-control" id="ds-f-name" value="' + esc(ds ? ds.name : '') + '"></div>' +
                '<div class="col-md-3"><label class="form-label">数据库类型</label>' +
                '<select class="form-select" id="ds-f-dbtype">' + typeOptions + '</select></div>' +
                '<div class="col-md-3"><label class="form-label">用途</label>' +
                '<select class="form-select" id="ds-f-role">' +
                '<option value="SOURCE"' + (!ds || ds.role === 'SOURCE' ? ' selected' : '') + '>SOURCE（抽取源）</option>' +
                '<option value="TARGET"' + (ds && ds.role === 'TARGET' ? ' selected' : '') + '>TARGET（目标源）</option>' +
                '</select></div>' +
                '<div class="col-12"><label class="form-label">JDBC URL</label>' +
                '<input type="text" class="form-control" id="ds-f-url" style="font-family:var(--etl-font-mono);font-size:12px;" ' +
                'placeholder="jdbc:oracle:thin:@//host:port/db" value="' + esc(ds ? ds.url : '') + '"></div>' +
                '<div class="col-md-6"><label class="form-label">用户名</label>' +
                '<input type="text" class="form-control" id="ds-f-username" value="' + esc(ds ? ds.username : '') + '"></div>' +
                '<div class="col-md-6"><label class="form-label">密码' + (ds ? '（留空则不修改）' : '') + '</label>' +
                '<input type="password" class="form-control" id="ds-f-password" value=""></div>' +
                '<div class="col-md-4"><label class="form-label">最大连接数</label>' +
                '<input type="number" class="form-control" id="ds-f-maxpool" value="' + esc(ds && ds.maxPoolSize != null ? ds.maxPoolSize : '') + '" placeholder="默认"></div>' +
                '<div class="col-md-4"><label class="form-label">连接超时（ms）</label>' +
                '<input type="number" class="form-control" id="ds-f-timeout" value="' + esc(ds && ds.connectionTimeoutMs != null ? ds.connectionTimeoutMs : '') + '" placeholder="默认"></div>' +
                '<div class="col-md-4 d-flex align-items-end">' +
                '<div class="form-check form-switch mb-2">' +
                '<input class="form-check-input" type="checkbox" id="ds-f-enabled"' + (!ds || ds.enabled === 1 ? ' checked' : '') + '>' +
                '<label class="form-check-label" for="ds-f-enabled">启用</label>' +
                '</div></div>' +
                '</div>' +
                '<div class="d-flex gap-2 mt-3 align-items-center flex-wrap">' +
                '<button class="etl-btn etl-btn-primary" onclick="DatasourceApp.save()"><i class="bi bi-check-lg"></i> 保存</button>' +
                (ds ? '<button class="etl-btn" onclick="DatasourceApp.test(' + ds.id + ')"><i class="bi bi-plug"></i> 测试连接</button>' : '') +
                '<button class="etl-btn" onclick="DatasourceApp.hideForm()">取消</button>' +
                '<span id="ds-form-test"></span>' +
                '</div></div>';
            wrap.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        },

        hideForm() {
            this.formOpen = false;
            this.editingId = null;
            document.getElementById('ds-form-wrap').style.display = 'none';
        },

        async save() {
            const data = {
                name: document.getElementById('ds-f-name').value.trim(),
                dbType: document.getElementById('ds-f-dbtype').value,
                url: document.getElementById('ds-f-url').value.trim(),
                username: document.getElementById('ds-f-username').value.trim(),
                password: document.getElementById('ds-f-password').value,
                role: document.getElementById('ds-f-role').value,
                enabled: document.getElementById('ds-f-enabled').checked ? 1 : 0
            };
            const maxPool = document.getElementById('ds-f-maxpool').value;
            const timeout = document.getElementById('ds-f-timeout').value;
            if (maxPool !== '') data.maxPoolSize = parseInt(maxPool, 10);
            if (timeout !== '') data.connectionTimeoutMs = parseInt(timeout, 10);
            if (!data.name || !data.url) {
                toast('请填写名称和 JDBC URL', 'warn');
                return;
            }
            try {
                if (this.editingId) {
                    await window.etlApi.put('/datasource/' + this.editingId, data);
                } else {
                    await window.etlApi.post('/datasource', data);
                }
                toast('数据源已保存', 'ok');
                this.hideForm();
                this.load();
            } catch (e) { /* 已 toast */ }
        },

        /* ---------- 测试连接 ---------- */

        async test(id) {
            const badgeBox = document.getElementById('ds-test-' + id);
            const formBox = document.getElementById('ds-form-test');
            const paint = function (html) {
                if (badgeBox) badgeBox.innerHTML = html;
                if (formBox) formBox.innerHTML = html;
            };
            paint('<span class="etl-badge">测试中…</span>');
            try {
                const r = await window.etlApi.post('/datasource/' + id + '/test', {});
                if (r && r.success) {
                    paint('<span class="etl-badge ok">连接成功 · ' + esc(r.databaseProductName || r.driverName || '') + '</span>');
                    toast('连接测试成功', 'ok');
                } else {
                    paint('<span class="etl-badge err">连接失败</span>');
                    toast('连接失败：' + ((r && r.error) || '未知错误'), 'err');
                }
            } catch (e) {
                paint('<span class="etl-badge err">连接失败</span>');
            }
        },

        /* ---------- 表/列浏览 ---------- */

        async toggleBrowse(id) {
            const box = document.getElementById('ds-browse-' + id);
            if (!box) return;
            if (this.browseOpen[id]) {
                this.browseOpen[id] = false;
                box.style.display = 'none';
                return;
            }
            this.browseOpen[id] = true;
            box.style.display = 'block';
            if (this.tablesCache[id]) {
                this.renderTables(id);
                return;
            }
            box.innerHTML = '<div class="etl-source-meta mt-2">加载表列表…</div>';
            try {
                const r = await window.etlApi.get('/datasource/' + id + '/tables');
                const tables = (r || []).map((t) =>
                    typeof t === 'string' ? t : (t.tableName || t.name || String(t))
                );
                this.tablesCache[id] = tables;
                this.renderTables(id);
            } catch (e) {
                box.innerHTML = '<div class="etl-timeline-err mt-2">表列表加载失败</div>';
            }
        },

        renderTables(id) {
            const box = document.getElementById('ds-browse-' + id);
            if (!box) return;
            const tables = this.tablesCache[id] || [];
            const list = tables.length
                ? tables.map((t) =>
                    '<button class="etl-btn etl-btn-ghost" style="font-family:var(--etl-font-mono);font-size:12px;padding:2px 8px;" ' +
                    'onclick="DatasourceApp.showColumns(' + id + ', \'' + esc(t).replace(/'/g, "\\'") + '\')">' + esc(t) + '</button>'
                ).join(' ')
                : '<span class="etl-source-meta">无表</span>';
            box.innerHTML =
                '<div class="mt-3" style="border-top:1px dashed var(--etl-color-border);padding-top:var(--etl-space-3);">' +
                '<div class="d-flex flex-wrap gap-1" style="max-height:160px;overflow:auto;">' + list + '</div>' +
                '<div id="ds-cols-' + id + '" class="mt-2"></div>' +
                '</div>';
        },

        async showColumns(id, table) {
            const box = document.getElementById('ds-cols-' + id);
            if (!box) return;
            const key = id + ':' + table;
            box.innerHTML = '<div class="etl-source-meta">加载列…</div>';
            try {
                if (!this.columnsCache[key]) {
                    const r = await window.etlApi.get('/datasource/' + id + '/columns/' + encodeURIComponent(table));
                    this.columnsCache[key] = (r || []).map((c) => {
                        if (typeof c === 'string') return { name: c, type: '' };
                        return {
                            name: c.name || c.columnName || '?',
                            type: c.type || c.dataType || ''
                        };
                    });
                }
                const cols = this.columnsCache[key];
                box.innerHTML =
                    '<div style="font-family:var(--etl-font-mono);font-size:12px;border:1px solid var(--etl-color-border);' +
                    'border-radius:var(--etl-radius-sm);padding:var(--etl-space-2) var(--etl-space-3);max-height:220px;overflow:auto;">' +
                    '<div style="color:var(--etl-color-primary);font-weight:600;margin-bottom:4px;">' + esc(table) + '</div>' +
                    (cols.length ? cols.map((c) =>
                        '<div class="d-flex justify-content-between gap-3">' +
                        '<span>' + esc(c.name) + '</span>' +
                        '<span style="color:var(--etl-color-text-weak);">' + esc(c.type) + '</span>' +
                        '</div>'
                    ).join('') : '<div style="color:var(--etl-color-text-weak);">无列信息</div>') +
                    '</div>';
            } catch (e) {
                box.innerHTML = '<div class="etl-timeline-err">列信息加载失败</div>';
            }
        },

        /* ---------- 删除 ---------- */

        async remove(id) {
            const ds = this.datasources.find((d) => d.id === id);
            if (!confirm('确定删除数据源「' + (ds ? ds.name : id) + '」？')) return;
            try {
                await window.etlApi.del('/datasource/' + id);
                toast('数据源已删除', 'ok');
                this.load();
            } catch (e) { /* 已 toast */ }
        }
    };
})();
