/**
 * 调度与历史页面
 */
const ScheduleApp = {
    tasks: [],
    logs: [],

    render(container) {
        container.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0"><i class="bi bi-clock-history"></i> 执行历史</h5>
                        </div>
                        <div class="card-body">
                            <table class="table table-sm table-hover">
                                <thead>
                                    <tr>
                                        <th>任务名称</th>
                                        <th>触发方式</th>
                                        <th>状态</th>
                                        <th>读取行数</th>
                                        <th>写入行数</th>
                                        <th>开始时间</th>
                                        <th>耗时</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody id="log-table"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row mt-3">
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-header"><h6 class="mb-0">日志保留策略</h6></div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label>保存天数</label>
                                <input type="number" class="form-control" id="log-save-days" value="30">
                            </div>
                            <button class="btn btn-primary" onclick="ScheduleApp.saveLogConfig()">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        this.loadLogs();
    },

    async loadLogs() {
        const res = await etlApi.get('/task/list?page=1&size=100');
        if (res.result?.success) {
            this.tasks = res.body?.records || [];
            // 加载所有任务的最近日志
            const allLogs = [];
            for (const t of this.tasks) {
                const logRes = await etlApi.get(`/task/${t.id}/logs?page=1&size=10`);
                if (logRes.result?.success) {
                    (logRes.body || []).forEach(l => allLogs.push({ ...l, taskName: t.name }));
                }
            }
            allLogs.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
            this.logs = allLogs.slice(0, 50);
            this.renderTable();
        }
    },

    renderTable() {
        const tbody = document.getElementById('log-table');
        tbody.innerHTML = this.logs.map(l => `
            <tr>
                <td>${l.taskName || '-'}</td>
                <td><span class="badge bg-${l.triggerType === 'MANUAL' ? 'info' : 'secondary'}">${l.triggerType || '-'}</span></td>
                <td><span class="badge bg-${l.status === 'SUCCESS' ? 'success' : l.status === 'FAILED' ? 'danger' : 'warning'}">${l.status || '-'}</span></td>
                <td>${l.extractedRows || 0}</td>
                <td>${l.writtenRows || 0}</td>
                <td>${new Date(l.startTime).toLocaleString()}</td>
                <td>${l.startTime && l.endTime ? this.formatDuration(new Date(l.startTime), new Date(l.endTime)) : '-'}</td>
                <td><button class="btn btn-sm btn-link" onclick="ScheduleApp.showDetail(${l.id})">详情</button></td>
            </tr>
        `).join('');
    },

    formatDuration(start, end) {
        const ms = end - start;
        if (ms < 1000) return ms + 'ms';
        return (ms / 1000).toFixed(1) + 's';
    },

    async showDetail(logId) {
        // 简化：弹出告警显示日志ID，可扩展为弹窗
        alert('日志ID: ' + logId + '（可在此处展开查看步骤日志）');
    },

    async saveLogConfig() {
        const days = document.getElementById('log-save-days').value;
        const res = await etlApi.put('/log/config', { saveDays: parseInt(days) });
        if (res.result?.success) alert('保存成功');
    }
};