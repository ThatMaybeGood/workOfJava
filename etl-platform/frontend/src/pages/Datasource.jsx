import { useState, useEffect, useCallback } from 'react';
import { DataSourceAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const EMPTY_DS = {
  dsName: '', dsType: 'ORACLE', driverClass: 'oracle.jdbc.OracleDriver',
  jdbcUrl: '', username: '', password: '',
  initialSize: 5, minIdle: 5, maxActive: 20, maxWait: 60000,
  validationQuery: 'SELECT 1 FROM DUAL', description: '',
};

export default function Datasource() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_DS });
  const [saving, setSaving] = useState(false);
  const { addToast, ToastContainer } = useToast();

  const loadList = useCallback(async () => {
    setLoading(true);
    try {
      const res = await DataSourceAPI.list();
      if (res.success) setList(res.data || []);
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  useEffect(() => { loadList(); }, [loadList]);

  const openModal = (ds = null) => {
    if (ds) {
      setEditing(ds.id);
      setForm({ ...EMPTY_DS, ...ds, password: '' });
    } else {
      setEditing(null);
      setForm({ ...EMPTY_DS });
    }
    setModalOpen(true);
  };

  const closeModal = () => setModalOpen(false);

  const updateField = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async () => {
    if (!form.dsName || !form.jdbcUrl || !form.username) {
      addToast('请填写名称、JDBC URL 和用户名', 'error');
      return;
    }
    setSaving(true);
    try {
      const data = { ...form };
      if (editing) data.id = editing;
      const res = await DataSourceAPI.save(data);
      if (res.success) {
        addToast(editing ? '更新成功' : '创建成功', 'success');
        closeModal();
        await loadList();
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('确认删除该数据源？')) return;
    try {
      await DataSourceAPI.delete(id);
      addToast('已删除', 'success');
      loadList();
    } catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  const handleTest = async (id) => {
    try {
      const res = await DataSourceAPI.test(id);
      if (res.success) addToast('连接测试成功', 'success');
      else addToast('连接测试失败', 'error');
    } catch { addToast('连接测试失败', 'error'); }
  };

  const typeLabel = (t) => ({ ORACLE: 'Oracle', MYSQL: 'MySQL', POSTGRESQL: 'PostgreSQL', SQLSERVER: 'SQL Server' })[t] || t;

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">⬡</span> 数据节点管理</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={() => openModal()}>＋ 新增节点</button>
        </div>
      </div>

      <div className="content-area">
        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>节点名称</th>
                    <th>类型</th>
                    <th>JDBC URL</th>
                    <th>用户</th>
                    <th>最大连接</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : list.length === 0 ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      尚未配置数据节点
                    </td></tr>
                  ) : list.map(ds => (
                    <tr key={ds.id}>
                      <td style={{ fontWeight: 600, color: 'var(--accent-cyan)' }}>{ds.dsName}</td>
                      <td><span className="tag tag-blue">{typeLabel(ds.dsType)}</span></td>
                      <td className="text-mono text-sm" style={{ maxWidth: 260, overflow: 'hidden', textOverflow: 'ellipsis' }} title={ds.jdbcUrl}>{ds.jdbcUrl}</td>
                      <td className="text-muted">{ds.username}</td>
                      <td>{ds.maxActive || 20}</td>
                      <td><span className={`tag ${ds.enabled === 'Y' ? 'tag-green' : 'tag-dim'}`}>{ds.enabled === 'Y' ? '已激活' : '未激活'}</span></td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-xs btn-success" onClick={() => handleTest(ds.id)} title="测试连接">⇌</button>
                          <button className="btn btn-xs btn-secondary" onClick={() => openModal(ds)} title="编辑">✎</button>
                          <button className="btn btn-xs btn-danger" onClick={() => handleDelete(ds.id)} title="删除">✕</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {modalOpen && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 700 }}>
            <div className="modal-header">
              <h3>{editing ? '✎ 编辑节点' : '＋ 新增节点'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label>节点名称 <span className="required">*</span></label>
                <input value={form.dsName} onChange={e => updateField('dsName', e.target.value)} placeholder="例如：生产Oracle库" />
              </div></div>
              <div className="form-row">
                <div className="form-group">
                  <label>数据库类型 <span className="required">*</span></label>
                  <select value={form.dsType} onChange={e => { updateField('dsType', e.target.value); }}>
                    <option value="ORACLE">Oracle</option>
                    <option value="MYSQL">MySQL</option>
                    <option value="POSTGRESQL">PostgreSQL</option>
                    <option value="SQLSERVER">SQL Server</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>驱动类</label>
                  <input value={form.driverClass} onChange={e => updateField('driverClass', e.target.value)} />
                </div>
              </div>
              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label>JDBC URL <span className="required">*</span></label>
                <input value={form.jdbcUrl} onChange={e => updateField('jdbcUrl', e.target.value)} placeholder="jdbc:oracle:thin:@host:1521/SID" />
              </div></div>
              <div className="form-row">
                <div className="form-group">
                  <label>用户名 <span className="required">*</span></label>
                  <input value={form.username} onChange={e => updateField('username', e.target.value)} />
                </div>
                <div className="form-group">
                  <label>密码</label>
                  <input type="password" value={form.password} onChange={e => updateField('password', e.target.value)} placeholder={editing ? '留空不修改' : ''} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group"><label>初始连接</label><input type="number" value={form.initialSize} onChange={e => updateField('initialSize', parseInt(e.target.value))} /></div>
                <div className="form-group"><label>最小空闲</label><input type="number" value={form.minIdle} onChange={e => updateField('minIdle', parseInt(e.target.value))} /></div>
              </div>
              <div className="form-row">
                <div className="form-group"><label>最大连接</label><input type="number" value={form.maxActive} onChange={e => updateField('maxActive', parseInt(e.target.value))} /></div>
                <div className="form-group"><label>最大等待(ms)</label><input type="number" value={form.maxWait} onChange={e => updateField('maxWait', parseInt(e.target.value))} /></div>
              </div>
              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label>验证 SQL</label>
                <input value={form.validationQuery} onChange={e => updateField('validationQuery', e.target.value)} />
              </div></div>
              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label>描述</label>
                <textarea value={form.description} onChange={e => updateField('description', e.target.value)} rows={2} />
              </div></div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={closeModal}>取消</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? '保存中...' : '确认保存'}</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
