/**
 * 任务配置页面
 */
const TaskApp = {
    tasks: [],
    datasourceMap: new Map(),
    editing: null,

    async render(container) {
        await this.loadDatasources();
        container.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-tasks"></i> 抽取任务配置</h5>
                            <button class="btn btn-primary btn-sm" onclick="TaskApp.showAdd()">
                                <i class="bi bi-plus"></i> 新建任务
                            </button>
                        </div>
                        <div class="card-body">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>任务名称</th>
                                        <th>抽取类型</th>
                                        <th>源数据源</th>
                                        <th>目标表</th>
                                        <th>写入模式</th>
                                        <th>调度Cron</th>
                                        <th>状态</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody id="task-table"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal fade" id="taskModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">任务配置</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" id="task-id">
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label>任务名称</label>
                                    <input type="text" class="form-control" id="task-name">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label>抽取类型</label>
                                    <select class="form-control" id="task-extract-type" onchange="TaskApp.onExtractTypeChange()">
                                        <option value="WEBSERVICE">WebService</option>
                                        <option value="PROCEDURE">存储过程</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label>源数据源</label>
                                    <select class="form-control" id="task-source-ds">
                                        <option value="">-- 选择 --</option>
                                    </select>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label>目标数据源</label>
                                    <select class="form-control" id="task-target-ds">
                                        <option value="">-- 选择 --</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label>目标表名</label>
                                    <input type="text" class="form-control" id="task-target-table">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label>写入模式</label>
                                    <select class="form-control" id="task-write-mode">
                                        <option value="INSERT">INSERT（插入）</option>
                                        <option value="UPDATE">UPDATE（更新）</option>
                                        <option value="UPSERT">UPSERT（存在更新否则插入）</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row" id="write-mode-extra">
                                <div class="col-md-6 mb-3">
                                    <label>查询索引列（唯一键，逗号分隔）</label>
                                    <input type="text" class="form-control" id="task-query-index-cols" placeholder="如：id,sn">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label>更新字段（逗号分隔）</label>
                                    <input type="text" class="form-control" id="task-update-cols" placeholder="如：name,status">
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label>定时表达式 (Cron)</label>
                                    <input type="text" class="form-control" id="task-cron" placeholder="0 0 * * * ?">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label>最大行数保护</label>
                                    <input type="number" class="form-control" id="task-max-rows" value="10000">
                                </div>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" id="task-enabled">
                                <label class="form-check-label">启用定时调度</label>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                            <button class="btn btn-primary" onclick="TaskApp.save()">保存</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        this.loadTasks();
    },

    async loadDatasources() {
        const res = await etlApi.get('/datasource/list?page=1&size=100');
        if (res.result?.success) {
            (res.body?.records || []).forEach(ds => this.datasourceMap.set(ds.id, ds));
            this.populateDsSelects();
        }
    },

    populateDsSelects() {
        const sourceSelect = document.getElementById('task-source-ds');
        const targetSelect = document.getElementById('task-target-ds');
        if (!sourceSelect) return;

        const sourceOptions = Array.from(this.datasourceMap.values())
            .filter(ds => ds.role === 'SOURCE')
            .map(ds => `<option value="${ds.id}">${ds.name} (${ds.dbType})</option>`).join('');
        const targetOptions = Array.from(this.datasourceMap.values())
            .filter(ds => ds.role === 'TARGET')
            .map(ds => `<option value="${ds.id}">${ds.name} (${ds.dbType})</option>`).join('');

        sourceSelect.innerHTML = '<option value="">-- 选择源数据源 --</option>' + sourceOptions;
        targetSelect.innerHTML = '<option value="">-- 选择目标数据源 --</option>' + targetOptions;
    },

    onExtractTypeChange() {
        // 可扩展：根据抽取类型显示不同配置项
    },

    onWriteModeChange() {
        const mode = document.getElementById('task-write-mode').value;
        const extra = document.getElementById('write-mode-extra');
        extra.style.display = (mode === 'UPDATE' || mode === 'UPSERT') ? 'flex' : 'none';
    },

    async loadTasks() {
        const res = await etlApi.get('/task/list');
        if (res.result?.success) {
            this.tasks = res.body?.records || [];
            this.renderTable();
        }
    },

    renderTable() {
        const tbody = document.getElementById('task-table');
        tbody.innerHTML = this.tasks.map(t => {
            const sourceDs = this.datasourceMap.get(t.sourceDsId);
            const targetDs = this.datasourceMap.get(t.targetDsId);
            return `
                <tr>
                    <td>${t.id}</td>
                    <td>${t.name}</td>
                    <td><span class="badge bg-${t.extractType === 'WEBSERVICE' ? 'info' : 'warning'}">${t.extractType}</span></td>
                    <td>${sourceDs ? sourceDs.name : '-'}</td>
                    <td>${t.targetTable}</td>
                    <td><span class="badge bg-secondary">${t.writeMode}</span></td>
                    <td><code>${t.cron || '-'}</code></td>
                    <td><span class="badge bg-${t.enabled === 1 ? 'success' : 'secondary'}">${t.enabled === 1 ? '运行中' : '停止'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="TaskApp.edit(${t.id})"><i class="bi bi-pencil"></i></button>
                        <button class="btn btn-sm btn-outline-success" onclick="TaskApp.run(${t.id})"><i class="bi bi-play"></i> 调试</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="TaskApp.delete(${t.id})"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>
            `;
        }).join('');
    },

    showAdd() {
        this.editing = null;
        document.getElementById('task-id').value = '';
        document.getElementById('task-name').value = '';
        document.getElementById('task-extract-type').value = 'WEBSERVICE';
        document.getElementById('task-source-ds').value = '';
        document.getElementById('task-target-ds').value = '';
        document.getElementById('task-target-table').value = '';
        document.getElementById('task-write-mode').value = 'INSERT';
        document.getElementById('task-query-index-cols').value = '';
        document.getElementById('task-update-cols').value = '';
        document.getElementById('task-cron').value = '0 0 2 * * ?';
        document.getElementById('task-max-rows').value = '10000';
        document.getElementById('task-enabled').checked = false;
        document.getElementById('write-mode-extra').style.display = 'none';
        new bootstrap.Modal(document.getElementById('taskModal')).show();
    },

    edit(id) {
        const t = this.tasks.find(x => x.id === id);
        if (!t) return;
        this.editing = t;
        document.getElementById('task-id').value = t.id;
        document.getElementById('task-name').value = t.name;
        document.getElementById('task-extract-type').value = t.extractType;
        document.getElementById('task-source-ds').value = t.sourceDsId || '';
        document.getElementById('task-target-ds').value = t.targetDsId || '';
        document.getElementById('task-target-table').value = t.targetTable;
        document.getElementById('task-write-mode').value = t.writeMode;
        document.getElementById('task-query-index-cols').value = t.queryIndexCols || '';
        document.getElementById('task-update-cols').value = t.updateCols || '';
        document.getElementById('task-cron').value = t.cron || '';
        document.getElementById('task-max-rows').value = t.maxRows || 10000;
        document.getElementById('task-enabled').checked = t.enabled === 1;
        this.onWriteModeChange();
        new bootstrap.Modal(document.getElementById('taskModal')).show();
    },

    async save() {
        const id = document.getElementById('task-id').value;
        const data = {
            name: document.getElementById('task-name').value,
            extractType: document.getElementById('task-extract-type').value,
            sourceDsId: document.getElementById('task-source-ds').value || null,
            targetDsId: document.getElementById('task-target-ds').value || null,
            targetTable: document.getElementById('task-target-table').value,
            writeMode: document.getElementById('task-write-mode').value,
            queryIndexCols: document.getElementById('task-query-index-cols').value,
            updateCols: document.getElementById('task-update-cols').value,
            cron: document.getElementById('task-cron').value,
            maxRows: parseInt(document.getElementById('task-max-rows').value),
            enabled: document.getElementById('task-enabled').checked ? 1 : 0
        };
        if (!data.name || !data.targetTable) {
            alert('请填写任务名称和目标表');
            return;
        }
        const res = id ? await etlApi.put(`/task/${id}`, data) : await etlApi.post('/task', data);
        if (res.result?.success) {
            bootstrap.Modal.getInstance(document.getElementById('taskModal')).hide();
            this.loadTasks();
        } else {
            alert(res.result?.subMsg || '保存失败');
        }
    },

    async run(id) {
        const res = await etlApi.post(`/task/${id}/run`, {});
        if (res.result?.success) {
            const log = res.body;
            alert(`执行完成：${log.status}\n读取: ${log.extractedRows} 行\n写入: ${log.writtenRows} 行`);
            this.loadTasks();
        } else {
            alert('执行失败: ' + (res.result?.subMsg || '未知错误'));
        }
    },

    async delete(id) {
        if (!confirm('确定删除此任务？')) return;
        const res = await etlApi.del(`/task/${id}`);
        if (res.result?.success) this.loadTasks();
    }
};