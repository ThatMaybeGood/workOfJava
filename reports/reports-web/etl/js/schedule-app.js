/**
 * 调度历史页面（window.ScheduleApp）
 * - 全宽平铺执行历史：每条日志「简洁行」（状态/任务/时间/耗时/读写行数）
 * - 点击行展开「分层明细」：错误原因 + 逐步日志（抽取/转换/写入，定位失败步骤）
 * - 日志保留策略已迁移至「系统设置」（settings-app.js）
 */
(function () {
    function C() { return window.etlComponents; }
    function esc(s) { return C().esc(s); }
    function toast(msg, type) { C().toast(msg, type); }

    const STEP_CN = { EXTRACT: '抽取', TRANSFORM: '转换', LOAD: '写入' };
    function stepNameCN(name) {
        return STEP_CN[String(name || '').toUpperCase()] || esc(name || '?');
    }

    window.ScheduleApp = {
        logs: [],
        stepsOpen: {},
        stepsCache: {},
        page: 1,
        size: 10,
        total: 0,
        taskId: null,
        keyword: '',
        taskOptions: [],

        async loadTaskOptions() {
            try {
                const tasks = await window.etlApi.get('/task/simple-list');
                this.taskOptions = Array.isArray(tasks) ? tasks : [];
                const sel = document.getElementById('sch-task-filter');
                if (sel) {
                    sel.innerHTML = '<option value="">全部任务</option>' +
                        this.taskOptions.map(t =>
                            '<option value="' + esc(String(t.id)) + '"' +
                            (String(this.taskId) === String(t.id) ? ' selected' : '') +
                            '>' + esc(t.name) + '</option>'
                        ).join('');
                    sel.addEventListener('change', function () {
                        ScheduleApp.taskId = sel.value ? parseInt(sel.value, 10) : null;
                        ScheduleApp.page = 1;
                        ScheduleApp.loadLogs();
                    });
                }
            } catch (e) { /* 已 toast */ }
        },

        onKeywordSearch() {
            const input = document.getElementById('sch-keyword');
            this.keyword = input ? input.value.trim() : '';
            this.page = 1;
            this.loadLogs();
        },

        render(container) {
            this.container = container;
            container.innerHTML =
                '<div class="etl-card">' +
                '<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">' +
                '<h6 class="etl-card-title mb-0"><i class="bi bi-clock-history"></i> 执行历史</h6>' +
                '<button class="etl-btn etl-btn-ghost" onclick="ScheduleApp.loadLogs()">' +
                '<i class="bi bi-arrow-clockwise"></i> 刷新</button>' +
                '</div>' +
                '<div class="d-flex flex-wrap gap-2 mb-3 align-items-center">' +
                '<div style="display:flex;align-items:center;gap:8px;">' +
                '<span style="font-size:13px;color:var(--etl-color-text-weak);">任务：</span>' +
                '<select class="form-select form-select-sm" id="sch-task-filter" style="max-width:300px;">' +
                '<option value="">全部任务</option>' +
                '</select>' +
                '</div>' +
                '<div class="input-group" style="max-width:220px;">' +
                '<input type="text" class="form-control form-control-sm" id="sch-keyword" placeholder="按任务名搜索…" ' +
                'onkeyup="if(event.key===\'Enter\')ScheduleApp.onKeywordSearch()">' +
                '<button class="etl-btn btn-sm" onclick="ScheduleApp.onKeywordSearch()"><i class="bi bi-search"></i></button>' +
                '</div>' +
                '<span class="text-muted" style="font-size:12px;margin-left:auto;">点击任一行可展开步骤明细</span>' +
                '</div>' +
                '<div id="sch-log-box"><div class="etl-empty"><div class="etl-empty-text">加载中…</div></div></div>' +
                '<div id="sch-pagination" class="mt-3"></div>' +
                '</div>';

            this.loadTaskOptions();
            this.loadLogs();
        },

        /* ---------- 执行历史 ---------- */

        async loadLogs() {
            try {
                const params = '?page=' + this.page + '&size=' + this.size;
                const qs = [];
                if (this.taskId != null) qs.push('taskId=' + this.taskId);
                if (this.keyword) qs.push('keyword=' + encodeURIComponent(this.keyword));
                const data = await window.etlApi.get('/log/history' + params + (qs.length ? '&' + qs.join('&') : ''));
                let records = [];
                let total = 0;
                if (Array.isArray(data)) {
                    records = data;
                    total = data.length;
                } else {
                    records = (data && data.records) || [];
                    total = (data && data.total != null) ? data.total : records.length;
                }
                this.logs = records;
                this.total = total;
                this.renderLogs();
                this.renderPagination();
            } catch (e) {
                const box = document.getElementById('sch-log-box');
                if (box) box.innerHTML = '<div class="etl-empty"><div class="etl-empty-text">执行历史加载失败</div></div>';
            }
        },

        renderPagination() {
            const box = document.getElementById('sch-pagination');
            if (!box) return;
            const comp = C();
            if (typeof comp.renderPagination !== 'function' || typeof comp.bindPagination !== 'function') {
                box.innerHTML = '';
                return;
            }
            box.innerHTML = comp.renderPagination({ total: this.total, page: this.page, size: this.size });
            comp.bindPagination(box, function (p) {
                ScheduleApp.page = p;
                ScheduleApp.loadLogs();
            });
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
                    ? '<span class="etl-badge info">手动</span>'
                    : '<span class="etl-badge">定时</span>';
                const duration = (l.startTime && l.endTime)
                    ? C().fmtDuration(new Date(l.endTime) - new Date(l.startTime))
                    : '-';
                const open = !!this.stepsOpen[l.id];
                const rowsInfo = '读 <b>' + esc(l.extractedRows != null ? l.extractedRows : 0) +
                    '</b> / 写 <b>' + esc(l.writtenRows != null ? l.writtenRows : 0) + '</b> 行';
                const hasError = !!l.errorMsg;
                return '<div class="etl-log-row ' + cls + (open ? ' open' : '') + '">' +
                    '<div class="etl-log-head" role="button" tabindex="0" ' +
                    'onclick="ScheduleApp.toggleSteps(' + l.id + ')" ' +
                    'onkeydown="if(event.key===\'Enter\'||event.key===\' \'){event.preventDefault();ScheduleApp.toggleSteps(' + l.id + ')}">' +
                    '<span class="etl-log-expand"><i class="bi bi-chevron-' + (open ? 'down' : 'right') + '"></i></span>' +
                    '<span class="etl-badge ' + cls + '">' + (ok ? '成功' : '失败') + '</span>' +
                    triggerBadge +
                    '<span class="etl-log-task" title="' + esc(l.taskName || '') + '">' +
                    esc(l.taskName || ('任务 #' + (l.taskId != null ? l.taskId : '?'))) + '</span>' +
                    '<span class="etl-log-time">' + esc(C().fmtTime(l.startTime)) + ' → ' + esc(C().fmtTime(l.endTime)) + '</span>' +
                    '<span class="etl-log-rows">' + rowsInfo + '</span>' +
                    '<span class="etl-log-duration">' + esc(duration) + '</span>' +
                    (hasError ? '<span class="etl-log-failtag"><i class="bi bi-exclamation-triangle"></i> 有错误</span>' : '') +
                    '</div>' +
                    '<div class="etl-log-detail" id="sch-steps-' + l.id + '" style="' + (open ? '' : 'display:none;') + '"></div>' +
                    '</div>';
            }).join('');
            box.innerHTML = '<div class="etl-log-list">' + items + '</div>';
        },

        async toggleSteps(logId) {
            const box = document.getElementById('sch-steps-' + logId);
            const head = box && box.previousElementSibling;
            if (!box) return;
            if (this.stepsOpen[logId]) {
                this.stepsOpen[logId] = false;
                box.style.display = 'none';
                if (head) {
                    head.parentElement.classList.remove('open');
                    const ic = head.querySelector('.etl-log-expand i');
                    if (ic) ic.className = 'bi bi-chevron-right';
                }
                return;
            }
            this.stepsOpen[logId] = true;
            box.style.display = 'block';
            if (head) {
                head.parentElement.classList.add('open');
                const ic = head.querySelector('.etl-log-expand i');
                if (ic) ic.className = 'bi bi-chevron-down';
            }
            if (this.stepsCache[logId]) {
                box.innerHTML = this.stepsCache[logId];
                return;
            }
            box.innerHTML = '<div class="etl-log-loading"><span class="spinner-border spinner-border-sm"></span> 加载步骤明细…</div>';
            try {
                const steps = await window.etlApi.get('/log/steps/' + logId);
                box.innerHTML = this.buildStepDetail(logId, steps);
                this.stepsCache[logId] = box.innerHTML;
            } catch (e) {
                box.innerHTML = '<div class="etl-log-err">步骤明细加载失败</div>';
            }
        },

        buildStepDetail(logId, steps) {
            const log = this.logs.find(function (l) { return String(l.id) === String(logId); });
            const ok = log && (log.status === 'SUCCESS' || log.status === 'OK');
            let html = '';
            // 失败：错误原因条（简要）
            if (!ok && log && log.errorMsg) {
                html += '<div class="etl-log-errbar"><i class="bi bi-x-circle"></i> 失败原因：<code>' +
                    esc(log.errorMsg) + '</code>' +
                    '<button class="etl-btn etl-btn-sm etl-copy-err" onclick="ScheduleApp.copyError(' + logId + ')" ' +
                    'title="复制错误信息"><i class="bi bi-clipboard"></i> 复制</button></div>';
            }
            // 分层：逐步日志（EXTRACT / TRANSFORM / LOAD）
            if (steps && steps.length) {
                html += '<div class="etl-log-steps">';
                steps.forEach(function (s, i) {
                    const sOk = s.status === 'SUCCESS' || s.status === 'OK';
                    const dur = s.durationMs != null ? ' · 耗时 ' + C().fmtDuration(s.durationMs) : '';
                    const rows = s.rowsCount != null ? ' · ' + s.rowsCount + ' 行' : '';
                    const failed = !sOk;
                    html += '<div class="etl-log-step ' + (failed ? 'err' : 'ok') + '">' +
                        '<span class="etl-log-step-badge etl-badge ' + (failed ? 'err' : 'ok') + '">' +
                        stepNameCN(s.stepName) + '</span>' +
                        '<span class="etl-log-step-status">' + (failed ? '失败' : '成功') + '</span>' +
                        '<span class="etl-log-step-meta">' + esc(C().fmtTime(s.startTime)) + rows + dur + '</span>' +
                        (failed && s.detail ? '<div class="etl-log-step-err"><code>' + esc(s.detail) + '</code></div>' : '') +
                        '</div>';
                });
                html += '</div>';
            } else {
                html += '<div class="etl-log-steps"><div class="etl-log-step">无步骤明细</div></div>';
            }
            // 成功：可展开显示更详细的信息（当前为精简扩展位）
            return html;
        },

        copyError(logId) {
            const log = this.logs.find(function (l) { return String(l.id) === String(logId); });
            const text = (log && log.errorMsg) ? log.errorMsg : '';
            const done = function () { toast('错误信息已复制', 'ok'); };
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text).then(done, function () { fallbackCopy(text); done(); });
            } else {
                fallbackCopy(text);
                done();
            }
            function fallbackCopy(t) {
                const ta = document.createElement('textarea');
                ta.value = t;
                ta.style.position = 'fixed';
                ta.style.opacity = '0';
                document.body.appendChild(ta);
                ta.select();
                try { document.execCommand('copy'); } catch (e) { /* ignore */ }
                ta.remove();
            }
        }
    };
})();
