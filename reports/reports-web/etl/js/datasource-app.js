/**
 * 数据源管理页面
 */
const DatasourceApp = {
    datasources: [],
    editing: null,

    render(container) {
        container.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="mb-0"><i class="bi bi-database"></i> 数据源配置</h5>
                            <button class="btn btn-primary btn-sm" onclick="DatasourceApp.showAdd()">
                                <i class="bi bi-plus"></i> 新建数据源
                            </button>
                        </div>
                        <div class="card-body">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>名称</th>
                                        <th>类型</th>
                                        <th>URL</th>
                                        <th>用途</th>
                                        <th>状态</th>
                                        <th>操作</th>
                                    </tr>
                                </thead>
                                <tbody id="ds-table"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 编辑弹窗 -->
            <div class="modal fade" id="dsModal" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">数据源配置</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" id="ds-id">
                            <div class="mb-3">
                                <label>名称</label>
                                <input type="text" class="form-control" id="ds-name">
                            </div>
                            <div class="mb-3">
                                <label>数据库类型</label>
                                <select class="form-control" id="ds-db-type" onchange="DatasourceApp.onDbTypeChange()">
                                    <option value="ORACLE">Oracle</option>
                                    <option value="MYSQL">MySQL</option>
                                    <option value="POSTGRESQL">PostgreSQL</option>
                                    <option value="SQLSERVER">SQL Server</option>
                                    <option value="DM">达梦</option>
                                    <option value="H2">H2</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label>JDBC URL</label>
                                <input type="text" class="form-control" id="ds-url" placeholder="jdbc:oracle:thin:@//host:port/db">
                            </div>
                            <div class="mb-3">
                                <label>用户名</label>
                                <input type="text" class="form-control" id="ds-username">
                            </div>
                            <div class="mb-3">
                                <label>密码</label>
                                <input type="password" class="form-control" id="ds-password">
                            </div>
                            <div class="mb-3">
                                <label>用途</label>
                                <select class="form-control" id="ds-role">
                                    <option value="SOURCE">抽取源</option>
                                    <option value="TARGET">目标源</option>
                                </select>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                            <button class="btn btn-primary" onclick="DatasourceApp.save()">保存</button>
                            <button class="btn btn-info" onclick="DatasourceApp.testConnection()">测试连接</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        this.loadDatasources();
    },

    async loadDatasources() {
        const res = await etlApi.get('/datasource/list');
        if (res.result?.success) {
            this.datasources = res.body?.records || [];
            this.renderTable();
        }
    },

    renderTable() {
        const tbody = document.getElementById('ds-table');
        tbody.innerHTML = this.datasources.map(ds => `
            <tr>
                <td>${ds.id}</td>
                <td>${ds.name}</td>
                <td><span class="badge bg-${this.getDbTypeBadge(ds.dbType)}">${ds.dbType}</span></td>
                <td title="${ds.url}">${ds.url.substring(0, 40)}...</td>
                <td><span class="badge bg-${ds.role === 'SOURCE' ? 'info' : 'warning'}">${ds.role}</span></td>
                <td><span class="badge bg-${ds.enabled === 1 ? 'success' : 'secondary'}">${ds.enabled === 1 ? '启用' : '禁用'}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="DatasourceApp.edit(${ds.id})"><i class="bi bi-pencil"></i></button>
                    <button class="btn btn-sm btn-outline-danger" onclick="DatasourceApp.delete(${ds.id})"><i class="bi bi-trash"></i></button>
                </td>
            </tr>
        `).join('');
    },

    getDbTypeBadge(dbType) {
        const map = { ORACLE: 'danger', MYSQL: 'success', POSTGRESQL: 'primary', SQLSERVER: 'info', DM: 'warning', H2: 'secondary' };
        return map[dbType] || 'secondary';
    },

    showAdd() {
        this.editing = null;
        document.getElementById('ds-id').value = '';
        document.getElementById('ds-name').value = '';
        document.getElementById('ds-url').value = '';
        document.getElementById('ds-username').value = '';
        document.getElementById('ds-password').value = '';
        document.getElementById('ds-role').value = 'SOURCE';
        new bootstrap.Modal(document.getElementById('dsModal')).show();
    },

    edit(id) {
        const ds = this.datasources.find(d => d.id === id);
        if (!ds) return;
        this.editing = ds;
        document.getElementById('ds-id').value = ds.id;
        document.getElementById('ds-name').value = ds.name;
        document.getElementById('ds-db-type').value = ds.dbType;
        document.getElementById('ds-url').value = ds.url;
        document.getElementById('ds-username').value = ds.username;
        document.getElementById('ds-password').value = '';
        document.getElementById('ds-role').value = ds.role;
        new bootstrap.Modal(document.getElementById('dsModal')).show();
    },

    async save() {
        const id = document.getElementById('ds-id').value;
        const data = {
            name: document.getElementById('ds-name').value,
            dbType: document.getElementById('ds-db-type').value,
            url: document.getElementById('ds-url').value,
            username: document.getElementById('ds-username').value,
            password: document.getElementById('ds-password').value,
            role: document.getElementById('ds-role').value,
            enabled: 1
        };
        if (!data.name || !data.url) {
            alert('请填写名称和URL');
            return;
        }
        const res = id ? await etlApi.put(`/datasource/${id}`, data) : await etlApi.post('/datasource', data);
        if (res.result?.success) {
            bootstrap.Modal.getInstance(document.getElementById('dsModal')).hide();
            this.loadDatasources();
        } else {
            alert(res.result?.subMsg || '保存失败');
        }
    },

    async testConnection() {
        const id = document.getElementById('ds-id').value;
        if (!id) {
            alert('请先保存数据源');
            return;
        }
        const res = await etlApi.post(`/datasource/${id}/test`, {});
        if (res.result?.success) {
            const info = res.body;
            alert(`连接测试成功！\n驱动: ${info.driverName}\n数据库: ${info.databaseProductName}\nURL: ${info.url}`);
        } else {
            alert(`连接失败: ${res.body?.error || '未知错误'}`);
        }
    },

    async delete(id) {
        if (!confirm('确定删除此数据源？')) return;
        const res = await etlApi.del(`/datasource/${id}`);
        if (res.result?.success) this.loadDatasources();
    }
};