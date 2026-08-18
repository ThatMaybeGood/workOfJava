/**
 * 调度历史页面（window.ScheduleApp）
 * - 上半区：流水线任务表格（启用开关 / 立即运行 / 逐环节调试）
 * - 下半区：执行历史 .etl-timeline（点击展开步骤日志）
 * - 日志保留小卡片：GET/PUT /log/config
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }

    window.ScheduleApp = {
        tasks: [],
        logs: [],
        logConfig: { id: 1, saveDays: 30, autoClean: 1 },
        running: {},
        debugOpen: {},
        debugData: {},   // taskId -> {extract, transformed}
        stepsOpen: {},
        stepsCache: {},

        render(container) {
            this.container = container;
            container.innerHTML =
                '<div class="etl-card">' +
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h6 class="etl-card-title mb-0"><i class="bi bi-diagram-3"></i> 流水线任务</h6>' +
                '<button class="etl-btn etl-btn-ghost" onclick="ScheduleApp.loadTasks()">' +
                '<i class="bi bi-arrow-clockwise"></i> 刷新</button>' +
                '</div>' +
                '<div id="sch-task-box"><div class="etl-empty"><div class="etl-empty-text">加载中…</div></div></div>' +
                '</div>' +

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

            this.loadTasks();
            this.loadLogs();
            this.loadLogConfig();
        },

        /* ---------- 流水线任务 ---------- */

        async loadTasks() {
            try {
                const data = await window.etlApi.get('/task/list');
                this.tasks = (data && data.records) || [];
                this.renderTasks();
            } catch (e) {
                const box = document.getElementById('sch-task-box');
                if (box) box.innerHTML = '<div class="etl-empty"><div class="etl-empty-text">任务加载失败</div></div>';
            }
        },

        renderTasks() {
            const box = document.getElementById('sch-task-box');
            if (!box) return;
            if (!this.tasks.length) {
                box.innerHTML = '<div class="etl-empty">' +
                    '<div class="etl-empty-text">还没有流水线任务</div>' +
                    '<a class="etl-btn etl-btn-primary" href="#/wizard"><i class="bi bi-diagram-3"></i> 去创建</a>' +
                    '</div>';
                return;
            }
            const rows = this.tasks.map((t) => {
                const typeBadge = t.extractType === 'PROCEDURE'
                    ? '<span class="etl-badge warn">PROC</span>'
                    : '<span class="etl-badge info">WS</span>';
                const running = !!this.running[t.id];
                return '<tr>' +
                    '<td style="font-weight:500;">' + esc(t.name) + '</td>' +
                    '<td>' + typeBadge + '</td>' +
                    '<td style="font-family:var(--etl-font-mono);font-size:12px;">' + esc(t.targetTable || '-') + '</td>' +
                    '<td style="font-family:var(--etl-font-mono);font-size:12px;color:var(--etl-color-text-weak);">' + esc(t.cron || '-') + '</td>' +
                    '<td><div class="form-check form-switch mb-0">' +
                    '<input class="form-check-input" type="checkbox" ' + (t.enabled === 1 ? 'checked ' : '') +
                    'onchange="ScheduleApp.toggleEnabled(' + t.id + ', this)">' +
                    '</div></td>' +
                    '<td><div class="d-flex gap-2 flex-wrap">' +
                    '<button class="etl-btn etl-btn-primary" ' + (running ? 'disabled ' : '') +
                    'onclick="ScheduleApp.run(' + t.id + ')">' +
                    '<i class="bi bi-play-fill"></i> ' + (running ? '运行中…' : '立即运行') + '</button>' +
                    '<button class="etl-btn etl-btn-ghost" onclick="ScheduleApp.toggleDebug(' + t.id + ')">' +
                    '<i class="bi bi-bug"></i> 逐环节调试</button>' +
                    '</div></td>' +
                    '</tr>' +
                    '<tr id="sch-debug-row-' + t.id + '" style="display:none;"><td colspan="6">' +
                    '<div id="sch-debug-' + t.id + '"></div></td></tr>';
            }).join('');
            box.innerHTML =
                '<div class="table-responsive"><table class="table table-hover align-middle mb-0" style="font-size:13px;">' +
                '<thead><tr>' +
                '<th>任务名</th><th>来源类型</th><th>目标表</th><th>Cron</th><th>启用</th><th>操作</th>' +
                '</tr></thead><tbody>' + rows + '</tbody></table></div>';
        },

        /** 启用开关：先取全量详情再整体 PUT */
        async toggleEnabled(id, checkbox) {
            checkbox.disabled = true;
            try {
                const detail = await window.etlApi.get('/task/' + id + '/detail');
                if (!detail || !detail.task) throw new Error('任务详情为空');
                detail.task.enabled = checkbox.checked ? 1 : 0;
                await window.etlApi.put('/task/' + id, {
                    task: detail.task,
                    wsConfig: detail.wsConfig || null,
                    procConfig: detail.procConfig || null
                });
                toast(checkbox.checked ? '已启用调度' : '已停止调度', 'ok');
                this.loadTasks();
            } catch (e) {
                checkbox.checked = !checkbox.checked;
            } finally {
                checkbox.disabled = false;
            }
        },

        async run(id) {
            if (!confirm('手动执行一次完整链路（真实写入目标库），确认？')) return;
            this.running[id] = true;
            this.renderTasks();
            try {
                const log = await window.etlApi.post('/task/' + id + '/run', {});
                if (log && (log.status === 'SUCCESS' || log.status === 'OK')) {
                    toast('执行成功：读 ' + (log.extractedRows != null ? log.extractedRows : 0) +
                        ' / 写 ' + (log.writtenRows != null ? log.writtenRows : 0) + ' 行', 'ok');
                } else {
                    toast('执行失败：' + ((log && log.errorMsg) || '未知错误'), 'err');
                }
            } catch (e) { /* 已 toast */ } finally {
                this.running[id] = false;
                this.renderTasks();
                this.loadLogs();
            }
        },

        /* ---------- 逐环节调试 ---------- */

        toggleDebug(id) {
            const row = document.getElementById('sch-debug-row-' + id);
            if (!row) return;
            if (this.debugOpen[id]) {
                this.debugOpen[id] = false;
                row.style.display = 'none';
                return;
            }
            this.debugOpen[id] = true;
            row.style.display = '';
            this.renderDebugArea(id);
        },

        renderDebugArea(id) {
            const box = document.getElementById('sch-debug-' + id);
            if (!box) return;
            box.innerHTML =
                '<div class="etl-card" style="background:var(--etl-color-bg);">' +
                '<div class="d-flex gap-2 flex-wrap align-items-center mb-2">' +
                '<button class="etl-btn" onclick="ScheduleApp.debugExtract(' + id + ')">① 调试抽取</button>' +
                '<button class="etl-btn" onclick="ScheduleApp.debugTransform(' + id + ')">② 调试转换</button>' +
                '<button class="etl-btn" onclick="ScheduleApp.debugLoad(' + id + ')">③ 调试写入</button>' +
                '<div class="form-check ms-2">' +
                '<input class="form-check-input" type="checkbox" id="sch-dryrun-' + id + '" checked>' +
                '<label class="form-check-label" for="sch-dryrun-' + id + '">Dry-run（不真实写库，默认）</label>' +
                '</div></div>' +
                '<div id="sch-debug-panel-' + id + '">' +
                C().renderDebugPanel('STEP DEBUG', [
                    { label: 'IDLE', status: '', content: '按顺序点击按钮：抽取 → 转换 → 写入' }
                ]) +
                '</div></div>';
        },

        paintDebug(id, sections) {
            const box = document.getElementById('sch-debug-panel-' + id);
            if (box) box.innerHTML = C().renderDebugPanel('STEP DEBUG · TASK #' + id, sections);
        },

        async debugExtract(id) {
            this.paintDebug(id, [{ label: 'EXTRACT', status: '', content: '抽取中…' }]);
            try {
                const r = await window.etlApi.post('/debug/extract', { taskId: id, batchSize: 5 });
                this.debugData[id] = this.debugData[id] || {};
                this.debugData[id].extract = r;
                this.paintDebug(id, [
                    { label: 'EXTRACT', status: 'ok', content: 'totalRows: ' + r.totalRows + ' · duration: ' + C().fmtDuration(r.durationMs) },
                    { label: 'SAMPLE ROWS', status: '', content: JSON.stringify((r.rows || []).slice(0, 5), null, 2) }
                ]);
            } catch (e) {
                this.paintDebug(id, [{ label: 'EXTRACT', status: 'err', content: (e && e.message) || '抽取失败' }]);
            }
        },

        async debugTransform(id) {
            const data = this.debugData[id];
            if (!data || !data.extract) {
                toast('请先执行「① 调试抽取」', 'warn');
                return;
            }
            this.paintDebug(id, [{ label: 'TRANSFORM', status: '', content: '转换中…' }]);
            try {
                const r = await window.etlApi.post('/debug/transform', {
                    taskId: id,
                    rows: data.extract.rows
                });
                data.transformed = r.sampleRows || [];
                this.paintDebug(id, [
                    { label: 'TRANSFORM', status: 'ok', content: 'rows: ' + data.transformed.length + ' · duration: ' + C().fmtDuration(r.durationMs) },
                    { label: 'SAMPLE ROWS', status: '', content: JSON.stringify(data.transformed.slice(0, 5), null, 2) }
                ]);
            } catch (e) {
                this.paintDebug(id, [{ label: 'TRANSFORM', status: 'err', content: (e && e.message) || '转换失败' }]);
            }
        },

        async debugLoad(id) {
            const data = this.debugData[id];
            if (!data || !data.transformed) {
                toast('请先执行「② 调试转换」', 'warn');
                return;
            }
            const dryRunEl = document.getElementById('sch-dryrun-' + id);
            const dryRun = dryRunEl ? dryRunEl.checked : true;
            if (!dryRun && !confirm('将真实写入目标库，确认？')) return;
            this.paintDebug(id, [{ label: 'LOAD', status: '', content: dryRun ? 'Dry-run 校验中…' : '真实写入中…' }]);
            try {
                const r = await window.etlApi.post('/debug/load', {
                    taskId: id,
                    rows: data.transformed,
                    dryRun: dryRun
                });
                const ok = r && r.status === 'SUCCESS';
                this.paintDebug(id, [
                    {
                        label: 'LOAD' + (r && r.dryRun ? ' (DRY-RUN)' : ''),
                        status: ok ? 'ok' : 'err',
                        content: ok
                            ? (r.dryRun ? 'Dry-run 通过，未真实写库 · ' : '已写入 ') + 'writtenRows: ' + r.writtenRows + ' · duration: ' + C().fmtDuration(r.durationMs)
                            : ('写入失败：' + ((r && r.error) || '未知错误'))
                    }
                ]);
            } catch (e) {
                this.paintDebug(id, [{ label: 'LOAD', status: 'err', content: (e && e.message) || '写入失败' }]);
            }
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
