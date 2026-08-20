/**
 * 系统设置页面（window.SettingsApp）
 * - 左侧树状菜单：日志管理 → 日志保留策略；任务 → 全局执行参数（预留）等
 * - 右侧面板按菜单项渲染对应参数表单
 * - 日志保留策略从原「调度历史」页面迁移至此
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(m, t) { C().toast(m, t); }

    window.SettingsApp = {
        active: 'log-retention',
        logConfig: { id: 1, saveDays: 30, autoClean: 1 },

        /* 树状菜单：可在此扩展新的参数分组 / 子项 */
        MENU: [
            { group: '日志管理', icon: 'bi-journal-text', items: [
                { key: 'log-retention', label: '日志保留策略', icon: 'bi-clock-history', desc: '执行历史与步骤日志的保留天数、自动清理开关' }
            ] },
            { group: '任务管理', icon: 'bi-diagram-3', items: [
                { key: 'task-global', label: '全局执行参数', icon: 'bi-sliders', desc: '全局默认执行参数（预留扩展位）' }
            ] },
            { group: '系统', icon: 'bi-info-circle', items: [
                { key: 'about', label: '关于系统', icon: 'bi-info-lg', desc: 'ETL 模块版本与说明' }
            ] }
        ],

        render(container) {
            this.container = container;
            container.innerHTML =
                '<h5 class="mb-3"><i class="bi bi-gear-fill"></i> 系统设置</h5>' +
                '<div class="etl-settings-layout">' +
                '<aside class="etl-settings-side"><div class="etl-settings-tree" id="settings-tree"></div></aside>' +
                '<div class="etl-settings-content" id="settings-content"></div>' +
                '</div>';
            this.paintTree();
            this.navigate(this.active);
        },

        paintTree() {
            const box = document.getElementById('settings-tree');
            if (!box) return;
            const self = this;
            box.innerHTML = this.MENU.map(function (g) {
                return '<div class="etl-settings-group">' +
                    '<div class="etl-settings-group-title"><i class="bi ' + g.icon + '"></i> ' + esc(g.group) + '</div>' +
                    '<div class="etl-settings-items">' +
                    g.items.map(function (it) {
                        return '<div class="etl-settings-item' + (self.active === it.key ? ' active' : '') + '" ' +
                            'data-key="' + it.key + '" role="button" tabindex="0">' +
                            '<i class="bi ' + it.icon + '"></i> <span>' + esc(it.label) + '</span></div>';
                    }).join('') +
                    '</div></div>';
            }).join('');
            box.querySelectorAll('.etl-settings-item').forEach(function (el) {
                el.addEventListener('click', function () { self.navigate(el.getAttribute('data-key')); });
                el.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        self.navigate(el.getAttribute('data-key'));
                    }
                });
            });
        },

        navigate(key) {
            this.active = key;
            const items = document.querySelectorAll('.etl-settings-item');
            items.forEach(function (el) {
                el.classList.toggle('active', el.getAttribute('data-key') === key);
            });
            const content = document.getElementById('settings-content');
            if (!content) return;
            if (key === 'log-retention') this.paintLogRetention(content);
            else if (key === 'task-global') this.paintTaskGlobal(content);
            else this.paintAbout(content);
        },

        /* ---------- 日志保留策略 ---------- */
        paintLogRetention(content) {
            content.innerHTML =
                '<div class="etl-card">' +
                '<h6 class="etl-card-title"><i class="bi bi-clock-history"></i> 日志保留策略</h6>' +
                '<div class="text-muted mb-3" style="font-size:13px;">' +
                '设置执行历史与步骤日志的保留天数，以及是否自动清理过期日志。' +
                '对应接口：GET/PUT /api/etl/log/config。</div>' +
                '<div class="row g-3 align-items-end" style="max-width:720px;">' +
                '<div class="col-md-4"><label class="form-label">保存天数</label>' +
                '<input type="number" class="form-control" id="set-log-days" min="1" value="30"></div>' +
                '<div class="col-md-5"><div class="form-check form-switch mb-2 mt-4">' +
                '<input class="form-check-input" type="checkbox" id="set-log-clean" checked>' +
                '<label class="form-check-label" for="set-log-clean">自动清理过期日志</label></div></div>' +
                '<div class="col-md-3"><button class="etl-btn etl-btn-primary" id="set-log-save">' +
                '<i class="bi bi-check-lg"></i> 保存</button></div>' +
                '</div></div>';
            this.loadLogConfig();
            const saveBtn = document.getElementById('set-log-save');
            saveBtn.addEventListener('click', function () { SettingsApp.saveLogConfig(); });
        },

        async loadLogConfig() {
            try {
                const cfg = await window.etlApi.get('/log/config');
                if (cfg) {
                    this.logConfig = cfg;
                    const daysEl = document.getElementById('set-log-days');
                    const cleanEl = document.getElementById('set-log-clean');
                    if (daysEl) daysEl.value = cfg.saveDays != null ? cfg.saveDays : 30;
                    if (cleanEl) cleanEl.checked = cfg.autoClean !== 0;
                }
            } catch (e) { /* 已 toast */ }
        },

        async saveLogConfig() {
            const days = parseInt(document.getElementById('set-log-days').value, 10);
            if (!days || days < 1) {
                toast('保存天数需为正整数', 'warn');
                return;
            }
            const autoClean = document.getElementById('set-log-clean').checked ? 1 : 0;
            try {
                await window.etlApi.put('/log/config', {
                    id: this.logConfig.id || 1,
                    saveDays: days,
                    autoClean: autoClean
                });
                this.logConfig.saveDays = days;
                this.logConfig.autoClean = autoClean;
                toast('日志保留策略已保存', 'ok');
            } catch (e) { /* 已 toast */ }
        },

        /* ---------- 全局执行参数 ---------- */
        paintTaskGlobal(content) {
            content.innerHTML =
                '<div class="etl-card">' +
                '<h6 class="etl-card-title"><i class="bi bi-sliders"></i> 全局执行参数</h6>' +
                '<div class="text-muted mb-3" style="font-size:13px;">' +
                '默认批大小 / 最大抽取行数已接入执行引擎（任务未配置时按此生效）；' +
                '最大重试次数 / 执行超时存库待后续接线。对应接口：GET/PUT /api/etl/global/config。</div>' +
                '<div class="row g-3 align-items-end" style="max-width:720px;">' +
                '<div class="col-md-4"><label class="form-label">默认批大小</label>' +
                '<input type="number" class="form-control" id="set-gb-batch" min="1" value="100"></div>' +
                '<div class="col-md-4"><label class="form-label">最大抽取行数</label>' +
                '<input type="number" class="form-control" id="set-gb-maxrows" min="1" value="10000"></div>' +
                '<div class="col-md-4"><label class="form-label">最大重试次数</label>' +
                '<input type="number" class="form-control" id="set-gb-retry" min="0" value="0"></div>' +
                '<div class="col-md-4"><label class="form-label">执行超时（秒，0=不限）</label>' +
                '<input type="number" class="form-control" id="set-gb-timeout" min="0" value="0"></div>' +
                '<div class="col-md-3"><button class="etl-btn etl-btn-primary" id="set-gb-save">' +
                '<i class="bi bi-check-lg"></i> 保存</button></div>' +
                '</div></div>';
            this.loadGlobalConfig();
            const saveBtn = document.getElementById('set-gb-save');
            saveBtn.addEventListener('click', function () { SettingsApp.saveGlobalConfig(); });
        },

        async loadGlobalConfig() {
            try {
                const cfg = await window.etlApi.get('/global/config');
                const num = function (v, d) { const n = parseInt(v, 10); return isNaN(n) ? d : n; };
                const el = function (id) { return document.getElementById(id); };
                if (cfg) {
                    if (el('set-gb-batch')) el('set-gb-batch').value = num(cfg.defaultBatchSize, 100);
                    if (el('set-gb-maxrows')) el('set-gb-maxrows').value = num(cfg.defaultMaxRows, 10000);
                    if (el('set-gb-retry')) el('set-gb-retry').value = num(cfg.maxRetryCount, 0);
                    if (el('set-gb-timeout')) el('set-gb-timeout').value = num(cfg.timeoutSeconds, 0);
                }
            } catch (e) { /* 已 toast */ }
        },

        async saveGlobalConfig() {
            const batch = parseInt(document.getElementById('set-gb-batch').value, 10);
            const maxRows = parseInt(document.getElementById('set-gb-maxrows').value, 10);
            const retry = parseInt(document.getElementById('set-gb-retry').value, 10);
            const timeout = parseInt(document.getElementById('set-gb-timeout').value, 10);
            if (!batch || batch < 1 || !maxRows || maxRows < 1) {
                toast('默认批大小 / 最大抽取行数需为正整数', 'warn');
                return;
            }
            if (isNaN(retry) || retry < 0 || isNaN(timeout) || timeout < 0) {
                toast('最大重试次数 / 执行超时不能为负数', 'warn');
                return;
            }
            try {
                await window.etlApi.put('/global/config', {
                    defaultBatchSize: batch,
                    defaultMaxRows: maxRows,
                    maxRetryCount: retry,
                    timeoutSeconds: timeout
                });
                toast('全局执行参数已保存', 'ok');
            } catch (e) { /* 已 toast */ }
        },

        /* ---------- 关于系统 ---------- */
        paintAbout(content) {
            content.innerHTML =
                '<div class="etl-card">' +
                '<h6 class="etl-card-title"><i class="bi bi-info-lg"></i> 关于系统</h6>' +
                '<div class="etl-about-lines">' +
                '<div><span class="etl-about-k">模块</span><span>ETL 数据集成</span></div>' +
                '<div><span class="etl-about-k">架构</span><span>抽取 → 转换 → 映射写入</span></div>' +
                '<div><span class="etl-about-k">版本</span><span>2026-08-20</span></div>' +
                '</div></div>';
        }
    };
})();
