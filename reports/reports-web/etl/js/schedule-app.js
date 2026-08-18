/**
 * 调度历史页面（window.ScheduleApp）
 * - 执行历史 .etl-timeline（点击展开步骤日志）
 * - 日志保留小卡片：GET/PUT /log/config
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }

    window.ScheduleApp = {
        logs: [],
        logConfig: { id: 1, saveDays: 30, autoClean: 1 },
        stepsOpen: {},
        stepsCache: {},

        render(container) {
            this.container = container;
            container.innerHTML =
                '<div class="etl-card">' +
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h6 class="etl-card-title mb-0"><i class="bi bi-clock-history"></i> 执行历史</h6>' +
                '<button class="etl-btn etl-btn-ghost" onclick="ScheduleApp.loadLogs()">' +
                '<i class="bi bi-arrow-clockwise"></i> 刷新</button>' +
                '</div>' +
                '<div id="sch-log-box"><div class="etl-empty"><div class="etl-empty-text">加载中…</div></div></div>' +
                '</div>' +

                '<div class="etl-card">' +
                '<h6 class="etl-card-title"><i class="bi bi-gear"></i> 日志保留</h6>' +
                '<div class="row g-3 align-items-end">' +
                '<div class="col-md-3"><label class="form-label">保存天数</label>' +
                '<input type="number" class="form-control" id="log-save-days" min="1" value="30"></div>' +
                '<div class="col-md-4"><div class="form-check form-switch mb-2">' +
                '<input class="form-check-input" type="checkbox" id="log-auto-clean" checked>' +
                '<label class="form-check-label" for="log-auto-clean">自动清理过期日志</label>' +
                '</div></div>' +
                '<div class="col-md-3"><button class="etl-btn etl-btn-primary" onclick="ScheduleApp.saveLogConfig()">' +
                '<i class="bi bi-check-lg"></i> 保存</button></div>' +
                '</div></div>';

            this.loadLogs();
            this.loadLogConfig();
        },

        /* ---------- 执行历史 ---------- */

        async loadLogs() {
            try {
                const data = await window.etlApi.get('/log/history?size=100');
                this.logs = data || [];
                this.renderLogs();
            } catch (e) {
                const box = document.getElementById('sch-log-box');
                if (box) box.innerHTML = '<div class="etl-empty"><div class="etl-empty-text">执行历史加载失败</div></div>';
            }
        },

        renderLogs() {
            const box = document.getElementById('sch-log-box');
            if (!box) return;
            if (!this.logs.length) {
                box.innerHTML = '<div class="etl-empty"><div class="etl-empty-text">暂无执行记录</div></div>';
                return;
            }
            const items = this.logs.map((l) => {
                const ok = l.status === 'SUCCESS' || l.status === 'OK';
                const cls = ok ? 'ok' : 'err';
                const triggerBadge = l.triggerType === 'MANUAL'
                    ? '<span class="etl-badge info">MANUAL</span>'
                    : '<span class="etl-badge">SCHEDULED</span>';
                const duration = (l.startTime && l.endTime)
                    ? C().fmtDuration(new Date(l.endTime) - new Date(l.startTime))
                    : '-';
                const meta =
                    esc(C().fmtTime(l.startTime)) + ' → ' + esc(C().fmtTime(l.endTime)) +
                    ' · ' + esc(duration) +
                    ' · 读 ' + esc(l.extractedRows != null ? l.extractedRows : 0) +
                    ' / 写 ' + esc(l.writtenRows != null ? l.writtenRows : 0) + ' 行';
                return '<div class="etl-timeline-item ' + cls + '">' +
                    '<div class="etl-timeline-dot"></div>' +
                    '<div class="etl-timeline-time">' +
                    esc(l.taskName || '任务 #' + (l.taskId != null ? l.taskId : '?')) + ' ' +
                    triggerBadge + ' ' +
                    '<span class="etl-badge ' + cls + '">' + (ok ? '成功' : '失败') + '</span> ' +
                    '<button class="etl-btn etl-btn-ghost" style="padding:1px 8px;font-size:12px;" ' +
                    'onclick="ScheduleApp.toggleSteps(' + l.id + ')">步骤</button>' +
                    '</div>' +
                    '<div class="etl-timeline-meta">' + meta + '</div>' +
                    (l.errorMsg ? '<div class="etl-timeline-err">' + esc(l.errorMsg) + '</div>' : '') +
                    '<div id="sch-steps-' + l.id + '" style="display:none;" class="mt-2"></div>' +
                    '</div>';
            }).join('');
            box.innerHTML = '<div class="etl-timeline">' + items + '</div>';
        },

        async toggleSteps(logId) {
            const box = document.getElementById('sch-steps-' + logId);
            if (!box) return;
            if (this.stepsOpen[logId]) {
                this.stepsOpen[logId] = false;
                box.style.display = 'none';
                return;
            }
            this.stepsOpen[logId] = true;
            box.style.display = 'block';
            if (this.stepsCache[logId]) {
                box.innerHTML = this.stepsCache[logId];
                return;
            }
            box.innerHTML = '<div class="etl-timeline-meta">加载步骤…</div>';
            try {
                const steps = await window.etlApi.get('/log/steps/' + logId);
                const html = (steps && steps.length)
                    ? '<div style="border:1px solid var(--etl-color-border);border-radius:var(--etl-radius-sm);' +
                      'padding:var(--etl-space-2) var(--etl-space-3);font-size:12px;">' +
                      steps.map((s) => {
                          const sOk = s.status === 'SUCCESS' || s.status === 'OK';
                          return '<div class="d-flex align-items-center gap-2 flex-wrap" style="padding:3px 0;">' +
                              '<span class="etl-badge ' + (sOk ? 'ok' : 'err') + '">' + esc(s.stepName || '?') + '</span>' +
                              '<span style="font-family:var(--etl-font-mono);color:var(--etl-color-text-weak);">' +
                              esc(s.rowsCount != null ? s.rowsCount : 0) + ' 行 · ' + esc(C().fmtDuration(s.durationMs)) + '</span>' +
                              (s.detail ? '<span style="color:var(--etl-color-text-weak);word-break:break-all;">' + esc(s.detail) + '</span>' : '') +
                              '</div>';
                      }).join('') + '</div>'
                    : '<div class="etl-timeline-meta">无步骤日志</div>';
                this.stepsCache[logId] = html;
                box.innerHTML = html;
            } catch (e) {
                box.innerHTML = '<div class="etl-timeline-err">步骤日志加载失败</div>';
            }
        },

        /* ---------- 日志保留 ---------- */

        async loadLogConfig() {
            try {
                const cfg = await window.etlApi.get('/log/config');
                if (cfg) {
                    this.logConfig = cfg;
                    const daysEl = document.getElementById('log-save-days');
                    const cleanEl = document.getElementById('log-auto-clean');
                    if (daysEl) daysEl.value = cfg.saveDays != null ? cfg.saveDays : 30;
                    if (cleanEl) cleanEl.checked = cfg.autoClean !== 0;
                }
            } catch (e) { /* 已 toast */ }
        },

        async saveLogConfig() {
            const days = parseInt(document.getElementById('log-save-days').value, 10);
            if (!days || days < 1) {
                toast('保存天数需为正整数', 'warn');
                return;
            }
            const autoClean = document.getElementById('log-auto-clean').checked ? 1 : 0;
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
        }
    };
})();
