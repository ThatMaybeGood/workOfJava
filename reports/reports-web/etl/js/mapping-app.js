/**
 * 字段映射配置页面
 */
const MappingApp = {
    taskId: null,
    mappings: [],
    sampleRows: [],
    columns: [],

    render(container) {
        this.taskId = null;
        this.mappings = [];
        this.sampleRows = [];
        container.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0"><i class="bi bi-diagram-3"></i> 字段映射配置</h5>
                        </div>
                        <div class="card-body">
                            <div class="row mb-3">
                                <div class="col-md-4">
                                    <label>选择任务</label>
                                    <select class="form-control" id="mapping-task-select" onchange="MappingApp.loadTask()">
                                        <option value="">-- 选择任务 --</option>
                                    </select>
                                </div>
                            </div>
                            <div id="mapping-area" style="display:none;">
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="card">
                                            <div class="card-header">
                                                <h6>源字段（出参）</h6>
                                                <button class="btn btn-sm btn-success float-end" onclick="MappingApp.debugExtract()">调试抽取</button>
                                            </div>
                                            <div class="card-body" style="max-height:400px;overflow:auto;" id="src-fields"></div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="card">
                                            <div class="card-header">
                                                <h6>目标字段（表列）</h6>
                                            </div>
                                            <div class="card-body" style="max-height:400px;overflow:auto;" id="tgt-fields"></div>
                                        </div>
                                    </div>
                                </div>
                                <hr>
                                <div class="card">
                                    <div class="card-header">
                                        <h6>映射关系</h6>
                                        <button class="btn btn-sm btn-primary" onclick="MappingApp.addMapping()">+ 添加映射</button>
                                    </div>
                                    <div class="card-body">
                                        <table class="table table-sm">
                                            <thead><tr><th>源字段</th><th>→</th><th>目标列</th><th>默认值</th><th>更新字段</th><th>操作</th></tr></thead>
                                            <tbody id="mapping-table"></tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        this.loadTasks();
    },

    async loadTasks() {
        const res = await etlApi.get('/task/list?page=1&size=100');
        const select = document.getElementById('mapping-task-select');
        if (res.result?.success) {
            (res.body?.records || []).forEach(t => {
                select.innerHTML += `<option value="${t.id}">${t.name}</option>`;
            });
        }
    },

    async loadTask() {
        this.taskId = document.getElementById('mapping-task-select').value;
        if (!this.taskId) {
            document.getElementById('mapping-area').style.display = 'none';
            return;
        }
        document.getElementById('mapping-area').style.display = 'block';
        await this.loadMappings();
    },

    async loadMappings() {
        const res = await etlApi.get(`/mapping/list/${this.taskId}`);
        if (res.result?.success) {
            this.mappings = res.body || [];
            this.renderMappingTable();
        }
    },

    renderMappingTable() {
        const tbody = document.getElementById('mapping-table');
        tbody.innerHTML = this.mappings.map((m, i) => `
            <tr>
                <td><input class="form-control form-control-sm" value="${m.srcField}" onchange="MappingApp.updateMapping(${i},'srcField',this.value)"></td>
                <td>→</td>
                <td>
                    <select class="form-control form-control-sm" onchange="MappingApp.updateMapping(${i},'tgtField',this.value)">
                        <option value="">-- 选择 --</option>
                        ${this.columns.map(c => `<option value="${c.columnName}" ${m.tgtField===c.columnName?'selected':''}>${c.columnName}</option>`).join('')}
                    </select>
                </td>
                <td><input type="text" class="form-control form-control-sm" value="${m.defaultValue||''}" onchange="MappingApp.updateMapping(${i},'defaultValue',this.value)"></td>
                <td><input type="checkbox" ${m.isUpdateCol===1?'checked':''} onchange="MappingApp.updateMapping(${i},'isUpdateCol',this.checked?1:0)"></td>
                <td><button class="btn btn-sm btn-danger" onclick="MappingApp.removeMapping(${m.id})"><i class="bi bi-trash"></i></button></td>
            </tr>
        `).join('');
    },

    async debugExtract() {
        const res = await etlApi.post('/debug/extract', { taskId: this.taskId, batchSize: 5 });
        if (res.result?.success) {
            const result = res.body;
            this.columns = result.columns || [];
            this.sampleRows = result.rows || [];
            this.renderSrcFields();
            this.renderTgtFields();
            alert(`抽取成功：共 ${result.totalRows} 行，显示 ${result.rows.length} 行`);
        } else {
            alert('抽取调试失败: ' + (res.result?.subMsg || '未知错误'));
        }
    },

    renderSrcFields() {
        const fields = [...new Set(this.sampleRows.flatMap(r => Object.keys(r)))];
        document.getElementById('src-fields').innerHTML = fields.map(f => `
            <div class="badge bg-info m-1">${f}</div>
        `).join('');
    },

    renderTgtFields() {
        document.getElementById('tgt-fields').innerHTML = this.columns.map(c => `
            <div class="badge bg-secondary m-1">${c}</div>
        `).join('');
    },

    addMapping() {
        this.mappings.push({ taskId: this.taskId, srcField: '', tgtField: '', defaultValue: '', isUpdateCol: 0, sortOrder: this.mappings.length });
        this.renderMappingTable();
    },

    updateMapping(index, field, value) {
        this.mappings[index][field] = value;
    },

    async removeMapping(id) {
        if (id) {
            await etlApi.del(`/mapping/${id}`);
            this.mappings = this.mappings.filter(m => m.id !== id);
        }
        this.renderMappingTable();
    },

    async saveMappings() {
        for (const m of this.mappings) {
            if (m.id) {
                await etlApi.put('/mapping/' + m.id, m);
            } else {
                await etlApi.post('/mapping', m);
            }
        }
        alert('映射保存成功');
    }
};