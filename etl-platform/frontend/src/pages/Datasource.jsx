import { useState, useEffect, useCallback } from 'react';
import { DataSourceAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const PROTOCOLS = [
  { value: 'JDBC', label: '数据库 (JDBC)', icon: '⬡' },
  { value: 'HTTP', label: 'HTTP 接口', icon: '⇄' },
  { value: 'SOAP', label: 'WebService/SOAP', icon: '⎔' },
  { value: 'FILE', label: '文件', icon: '▤' },
];

const DS_TYPES = [
  { value: 'ORACLE', label: 'Oracle', driver: 'oracle.jdbc.OracleDriver', validate: 'SELECT 1 FROM DUAL' },
  { value: 'MYSQL', label: 'MySQL', driver: 'com.mysql.cj.jdbc.Driver', validate: 'SELECT 1' },
  { value: 'POSTGRESQL', label: 'PostgreSQL', driver: 'org.postgresql.Driver', validate: 'SELECT 1' },
  { value: 'SQLSERVER', label: 'SQL Server', driver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver', validate: 'SELECT 1' },
];

const EMPTY_DS = {
  dsName: '', dsType: 'ORACLE', protocol: 'JDBC',
  driverClass: 'oracle.jdbc.OracleDriver',
  jdbcUrl: '', username: '', password: '',
  initialSize: 5, minIdle: 5, maxActive: 20, maxWait: 60000,
  validationQuery: 'SELECT 1 FROM DUAL',
  authType: 'NONE', authToken: '', timeout: 30000,
  encoding: 'UTF-8', description: '',
};

export default function Datasource() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_DS });
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
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

  const update = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleProtocolChange = (protocol) => {
    const updates = { protocol };
    if (protocol === 'JDBC') {
      updates.dsType = 'ORACLE';
      updates.driverClass = 'oracle.jdbc.OracleDriver';
      updates.validationQuery = 'SELECT 1 FROM DUAL';
      updates.jdbcUrl = '';
    } else if (protocol === 'HTTP') {
      updates.dsType = 'HTTP';
      updates.driverClass = 'http';
      updates.jdbcUrl = '';
      updates.authType = 'NONE';
    } else if (protocol === 'SOAP') {
      updates.dsType = 'SOAP';
      updates.driverClass = 'soap';
      updates.jdbcUrl = '';
    } else if (protocol === 'FILE') {
      updates.dsType = 'FILE';
      updates.driverClass = 'file';
      updates.jdbcUrl = '';
    }
    setForm(prev => ({ ...prev, ...updates }));
  };

  const handleDbTypeChange = (dsType) => {
    const info = DS_TYPES.find(t => t.value === dsType);
    setForm(prev => ({
      ...prev,
      dsType,
      driverClass: info ? info.driver : prev.driverClass,
      validationQuery: info ? info.validate : prev.validationQuery,
    }));
  };

  const handleSave = async () => {
    if (!form.dsName) { addToast('请填写数据源名称', 'error'); return; }
    if (!form.jdbcUrl) { addToast('请填写连接地址', 'error'); return; }

    const protocol = form.protocol || 'JDBC';
    if (protocol === 'JDBC' && !form.username) {
      addToast('请填写用户名', 'error'); return;
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

  const typeLabel = (t) => ({ ORACLE: 'Oracle', MYSQL: 'MySQL', POSTGRESQL: 'PostgreSQL', SQLSERVER: 'SQL Server', HTTP: 'HTTP', SOAP: 'SOAP', FILE: '文件' })[t] || t;
  const protocolLabel = (p) => ({ JDBC: '数据库', HTTP: 'HTTP接口', SOAP: 'WebService', FILE: '文件' })[p] || p;

  const formProtocol = form.protocol || 'JDBC';

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
                    <th>协议</th>
                    <th>类型</th>
                    <th>连接地址</th>
                    <th>用户</th>
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
                      <td><span className="tag tag-purple">{protocolLabel(ds.protocol || 'JDBC')}</span></td>
                      <td><span className="tag tag-blue">{typeLabel(ds.dsType)}</span></td>
                      <td className="text-mono text-sm" style={{ maxWidth: 260, overflow: 'hidden', textOverflow: 'ellipsis' }} title={ds.jdbcUrl}>{ds.jdbcUrl}</td>
                      <td className="text-muted">{ds.username || '--'}</td>
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
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 760 }}>
            <div className="modal-header">
              <h3>{editing ? '✎ 编辑节点' : '＋ 新增节点'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              {/* 协议选择 */}
              <div className="form-row">
                <div className="form-group" style={{ gridColumn: '1/-1' }}>
                  <label>连接协议 <span className="required">*</span></label>
                  <div className="protocol-selector" style={{ display: 'flex', gap: 8 }}>
                    {PROTOCOLS.map(p => (
                      <button
                        key={p.value}
                        type="button"
                        onClick={() => handleProtocolChange(p.value)}
                        className={`btn ${formProtocol === p.value ? 'btn-primary' : 'btn-secondary'} btn-sm`}
                        style={{ flex: 1 }}
                      >
                        <span style={{ marginRight: 4 }}>{p.icon}</span> {p.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* 基础信息 */}
              <div className="form-row">
                <div className="form-group">
                  <label>节点名称 <span className="required">*</span></label>
                  <input value={form.dsName} onChange={e => update('dsName', e.target.value)}
                    placeholder={formProtocol === 'JDBC' ? '例如：生产Oracle库' : '例如：用户查询接口'} />
                </div>
                {formProtocol === 'JDBC' && (
                  <div className="form-group">
                    <label>数据库类型 <span className="required">*</span></label>
                    <select value={form.dsType} onChange={e => handleDbTypeChange(e.target.value)}>
                      {DS_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
                    </select>
                  </div>
                )}
                {formProtocol !== 'JDBC' && (
                  <div className="form-group">
                    <label>编码</label>
                    <select value={form.encoding || 'UTF-8'} onChange={e => update('encoding', e.target.value)}>
                      <option value="UTF-8">UTF-8</option>
                      <option value="GBK">GBK</option>
                      <option value="ISO-8859-1">ISO-8859-1</option>
                    </select>
                  </div>
                )}
              </div>

              {/* JDBC 配置 */}
              {formProtocol === 'JDBC' && (
                <>
                  <div className="form-row">
                    <div className="form-group"><label>驱动类</label><input value={form.driverClass} onChange={e => update('driverClass', e.target.value)} /></div>
                  </div>
                  <div className="form-row">
                    <div className="form-group" style={{ gridColumn: '1/-1' }}>
                      <label>JDBC URL <span className="required">*</span></label>
                      <input value={form.jdbcUrl} onChange={e => update('jdbcUrl', e.target.value)} placeholder="jdbc:oracle:thin:@host:1521/SID" />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>用户名 <span className="required">*</span></label>
                      <input value={form.username} onChange={e => update('username', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label>密码</label>
                      <input type="password" value={form.password} onChange={e => update('password', e.target.value)} placeholder={editing ? '留空不修改' : ''} />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>初始连接</label><input type="number" value={form.initialSize || 5} onChange={e => update('initialSize', parseInt(e.target.value) || 5)} /></div>
                    <div className="form-group"><label>最小空闲</label><input type="number" value={form.minIdle || 5} onChange={e => update('minIdle', parseInt(e.target.value) || 5)} /></div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>最大连接</label><input type="number" value={form.maxActive || 20} onChange={e => update('maxActive', parseInt(e.target.value) || 20)} /></div>
                    <div className="form-group"><label>最大等待(ms)</label><input type="number" value={form.maxWait || 60000} onChange={e => update('maxWait', parseInt(e.target.value) || 60000)} /></div>
                  </div>
                  <div className="form-row">
                    <div className="form-group" style={{ gridColumn: '1/-1' }}>
                      <label>验证 SQL</label>
                      <input value={form.validationQuery || ''} onChange={e => update('validationQuery', e.target.value)} />
                    </div>
                  </div>
                </>
              )}

              {/* HTTP / SOAP 配置 */}
              {(formProtocol === 'HTTP' || formProtocol === 'SOAP') && (
                <>
                  <div className="form-row">
                    <div className="form-group" style={{ gridColumn: '1/-1' }}>
                      <label>{formProtocol === 'SOAP' ? 'SOAP Endpoint URL' : 'API URL'} <span className="required">*</span></label>
                      <input value={form.jdbcUrl} onChange={e => update('jdbcUrl', e.target.value)}
                        placeholder={formProtocol === 'HTTP' ? 'https://api.example.com/data' : 'http://host:port/service?wsdl'} />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>认证方式</label>
                      <select value={form.authType || 'NONE'} onChange={e => update('authType', e.target.value)}>
                        <option value="NONE">无认证</option>
                        <option value="BASIC">Basic Auth</option>
                        <option value="TOKEN">Bearer Token</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>超时(ms)</label>
                      <input type="number" value={form.timeout || 30000} onChange={e => update('timeout', parseInt(e.target.value) || 30000)} />
                    </div>
                  </div>
                  {(form.authType === 'BASIC') && (
                    <div className="form-row">
                      <div className="form-group">
                        <label>认证用户名</label>
                        <input value={form.username || ''} onChange={e => update('username', e.target.value)} />
                      </div>
                      <div className="form-group">
                        <label>认证密码</label>
                        <input type="password" value={form.password || ''} onChange={e => update('password', e.target.value)} placeholder={editing ? '留空不修改' : ''} />
                      </div>
                    </div>
                  )}
                  {(form.authType === 'TOKEN') && (
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>Bearer Token</label>
                        <input value={form.authToken || ''} onChange={e => update('authToken', e.target.value)} placeholder="eyJ..." />
                      </div>
                    </div>
                  )}
                </>
              )}

              {/* 文件配置 */}
              {formProtocol === 'FILE' && (
                <div className="form-row">
                  <div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>文件根路径</label>
                    <input value={form.jdbcUrl} onChange={e => update('jdbcUrl', e.target.value)} placeholder="D:/data/csv/" />
                  </div>
                </div>
              )}

              <div className="form-row">
                <div className="form-group" style={{ gridColumn: '1/-1' }}>
                  <label>描述</label>
                  <textarea value={form.description || ''} onChange={e => update('description', e.target.value)} rows={2} />
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={closeModal}>取消</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? '保存中...' : '确认保存'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
