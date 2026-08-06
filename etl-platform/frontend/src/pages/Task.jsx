import { useState, useEffect, useCallback } from 'react';
import { TaskAPI, DataSourceAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const EMPTY_TASK = {
  taskCode: '', taskName: '', sourceDsName: '', targetDsName: '',
  sourceType: 'SQL', writeMode: 'INSERT', targetTable: '', cronExpr: '',
  batchSize: 2000, timeoutSeconds: 1800, description: '',
  sourceProcedure: '', sourceTable: '', sourceSql: '', sourceView: '', sourceParams: '',
  httpUrl: '', httpMethod: 'GET', httpResponseType: 'JSON',
  filePath: '', fileFormat: '',
};

const SOURCE_TYPE_LABELS = {
  PROCEDURE: '存储过程', SQL: 'SQL查询', VIEW: '视图', TABLE: '全表', HTTP: 'HTTP接口', FILE: '文件',
};

export default function Task() {
  const [list, setList] = useState([]);
  const [dsNames, setDsNames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_TASK });
  const [saving, setSaving] = useState(false);
  const [activeTab, setActiveTab] = useState('basic');
  const { addToast, ToastContainer } = useToast();

  const loadList = useCallback(async () => {
    setLoading(true);
    try {
      const [taskRes, dsRes] = await Promise.all([TaskAPI.list(), DataSourceAPI.list()]);
      if (taskRes.success) setList(taskRes.data || []);
      if (dsRes.success) setDsNames(dsRes.data.map(d => d.dsName));
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  useEffect(() => { loadList(); }, [loadList]);

  const openModal = (task = null) => {
    if (task) {
      setEditing(task.id);
      setForm({ ...EMPTY_TASK, ...task });
    } else {
      setEditing(null);
      setForm({ ...EMPTY_TASK });
    }
    setActiveTab('basic');
    setModalOpen(true);
  };

  const closeModal = () => setModalOpen(false);

  const update = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleSave = async () => {
    if (!form.taskCode || !form.taskName || !form.targetTable) {
      addToast('请填写任务编码、名称和目标表', 'error');
      return;
    }
    setSaving(true);
    try {
      const data = { ...form };
      if (editing) data.id = editing;
      const res = await TaskAPI.save(data);
      if (res.success) {
        addToast(editing ? '更新成功' : '创建成功', 'success');
        closeModal();
        await loadList();
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('确认删除此任务？')) return;
    try {
      await TaskAPI.delete(id);
      addToast('已删除', 'success');
      loadList();
    } catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  const handleExecute = async (taskCode) => {
    if (!window.confirm(`确认手动执行任务 ${taskCode} ？`)) return;
    try {
      const res = await TaskAPI.execute(taskCode);
      if (res.success) addToast(`任务 ${taskCode} 执行成功`, 'success');
    } catch (e) { addToast('执行失败: ' + e.message, 'error'); }
  };

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">◈</span> 任务管理中心</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={() => openModal()}>＋ 新建任务</button>
        </div>
      </div>

      <div className="content-area">
        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>任务编码</th>
                    <th>任务名称</th>
                    <th>抽取类型</th>
                    <th>目标表</th>
                    <th>写入模式</th>
                    <th>Cron</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : list.length === 0 ? (
                    <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      尚未创建采集任务
                    </td></tr>
                  ) : list.map(t => (
                    <tr key={t.id}>
                      <td className="text-cyan" style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>{t.taskCode}</td>
                      <td style={{ fontWeight: 600 }}>{t.taskName}</td>
                      <td><span className="tag tag-purple">{SOURCE_TYPE_LABELS[t.sourceType] || t.sourceType}</span></td>
                      <td className="text-mono text-sm">{t.targetTable}</td>
                      <td><span className="tag tag-orange">{t.writeMode}</span></td>
                      <td className="text-mono text-sm">{t.cronExpr || <span className="text-muted">--</span>}</td>
                      <td><span className={`tag ${t.enabled === 'Y' ? 'tag-green' : 'tag-dim'}`}>{t.enabled === 'Y' ? '已激活' : '未激活'}</span></td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-xs btn-success" onClick={() => handleExecute(t.taskCode)} title="手动执行">▶</button>
                          <button className="btn btn-xs btn-secondary" onClick={() => openModal(t)} title="编辑">✎</button>
                          <button className="btn btn-xs btn-danger" onClick={() => handleDelete(t.id)} title="删除">✕</button>
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
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 780 }}>
            <div className="modal-header">
              <h3>{editing ? '✎ 编辑任务' : '＋ 新建任务'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="tabs">
                <div className={`tab ${activeTab === 'basic' ? 'active' : ''}`} onClick={() => setActiveTab('basic')}>基础信息</div>
                <div className={`tab ${activeTab === 'source' ? 'active' : ''}`} onClick={() => setActiveTab('source')}>源配置</div>
                <div className={`tab ${activeTab === 'http' ? 'active' : ''}`} onClick={() => setActiveTab('http')}>HTTP/文件</div>
              </div>

              {activeTab === 'basic' && (
                <div>
                  <div className="form-row">
                    <div className="form-group"><label>任务编码 <span className="required">*</span></label><input value={form.taskCode} onChange={e => update('taskCode', e.target.value)} /></div>
                    <div className="form-group"><label>任务名称 <span className="required">*</span></label><input value={form.taskName} onChange={e => update('taskName', e.target.value)} /></div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>源数据源</label>
                      <select value={form.sourceDsName} onChange={e => update('sourceDsName', e.target.value)}>
                        <option value="">请选择</option>
                        {dsNames.map(n => <option key={n} value={n}>{n}</option>)}
                      </select>
                    </div>
                    <div className="form-group">
                      <label>目标数据源</label>
                      <select value={form.targetDsName} onChange={e => update('targetDsName', e.target.value)}>
                        <option value="">请选择</option>
                        {dsNames.map(n => <option key={n} value={n}>{n}</option>)}
                      </select>
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>抽取类型</label>
                      <select value={form.sourceType} onChange={e => update('sourceType', e.target.value)}>
                        {Object.entries(SOURCE_TYPE_LABELS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                      </select>
                    </div>
                    <div className="form-group">
                      <label>写入模式</label>
                      <select value={form.writeMode} onChange={e => update('writeMode', e.target.value)}>
                        <option value="INSERT">INSERT</option>
                        <option value="MERGE">MERGE</option>
                      </select>
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>目标表 <span className="required">*</span></label><input value={form.targetTable} onChange={e => update('targetTable', e.target.value)} /></div>
                    <div className="form-group"><label>Cron 表达式</label><input value={form.cronExpr} onChange={e => update('cronExpr', e.target.value)} placeholder="0 0 * * * ?" /></div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>批量大小</label><input type="number" value={form.batchSize} onChange={e => update('batchSize', parseInt(e.target.value))} /></div>
                    <div className="form-group"><label>超时(秒)</label><input type="number" value={form.timeoutSeconds} onChange={e => update('timeoutSeconds', parseInt(e.target.value))} /></div>
                  </div>
                  <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>描述</label><textarea value={form.description} onChange={e => update('description', e.target.value)} rows={2} />
                  </div></div>
                </div>
              )}

              {activeTab === 'source' && (
                <div>
                  <div className="form-row">
                    <div className="form-group"><label>存储过程名</label><input value={form.sourceProcedure} onChange={e => update('sourceProcedure', e.target.value)} /></div>
                    <div className="form-group"><label>源表名</label><input value={form.sourceTable} onChange={e => update('sourceTable', e.target.value)} /></div>
                  </div>
                  <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>SQL 语句</label><textarea value={form.sourceSql} onChange={e => update('sourceSql', e.target.value)} rows={4} placeholder="SELECT * FROM ..." />
                  </div></div>
                  <div className="form-row">
                    <div className="form-group"><label>源视图名</label><input value={form.sourceView} onChange={e => update('sourceView', e.target.value)} /></div>
                    <div className="form-group"><label>参数 (JSON)</label><input value={form.sourceParams} onChange={e => update('sourceParams', e.target.value)} placeholder='{"key":"value"}' /></div>
                  </div>
                </div>
              )}

              {activeTab === 'http' && (
                <div>
                  <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>HTTP URL</label><input value={form.httpUrl} onChange={e => update('httpUrl', e.target.value)} />
                  </div></div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>HTTP 方法</label>
                      <select value={form.httpMethod} onChange={e => update('httpMethod', e.target.value)}>
                        <option value="GET">GET</option><option value="POST">POST</option><option value="PUT">PUT</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>响应类型</label>
                      <select value={form.httpResponseType} onChange={e => update('httpResponseType', e.target.value)}>
                        <option value="JSON">JSON</option><option value="XML">XML</option>
                      </select>
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>文件路径</label><input value={form.filePath} onChange={e => update('filePath', e.target.value)} /></div>
                    <div className="form-group">
                      <label>文件格式</label>
                      <select value={form.fileFormat} onChange={e => update('fileFormat', e.target.value)}>
                        <option value="">--</option><option value="CSV">CSV</option><option value="JSON">JSON</option><option value="EXCEL">Excel</option>
                      </select>
                    </div>
                  </div>
                </div>
              )}
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
