import { useState, useEffect, useCallback } from 'react';
import { MappingAPI, TaskAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const EMPTY_MAP = {
  taskCode: '', sourceColumn: '', targetColumn: '', dataType: '',
  defaultValue: '', transformExpr: '', mappingOrder: 0,
  isPrimaryKey: 'N', enabled: 'Y', description: '',
};

const DATA_TYPES = ['STRING','INTEGER','LONG','DOUBLE','DECIMAL','DATE','DATETIME','BOOLEAN','CLOB','BLOB'];

export default function Mapping() {
  const [tasks, setTasks] = useState([]);
  const [selectedTask, setSelectedTask] = useState('');
  const [mappings, setMappings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_MAP });
  const [saving, setSaving] = useState(false);
  const { addToast, ToastContainer } = useToast();

  useEffect(() => {
    TaskAPI.list().then(res => {
      if (res.success) setTasks(res.data || []);
    }).catch(() => {});
  }, []);

  const loadMappings = useCallback(async (taskCode) => {
    if (!taskCode) { setMappings([]); return; }
    setLoading(true);
    try {
      const res = await MappingAPI.list(taskCode);
      if (res.success) setMappings(res.data || []);
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  const changeTask = (taskCode) => {
    setSelectedTask(taskCode);
    loadMappings(taskCode);
  };

  const openModal = (map = null) => {
    if (map) {
      setEditing(map.id);
      setForm({ ...EMPTY_MAP, ...map });
    } else {
      setEditing(null);
      setForm({ ...EMPTY_MAP, taskCode: selectedTask });
    }
    setModalOpen(true);
  };

  const closeModal = () => setModalOpen(false);
  const update = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleSave = async () => {
    if (!form.sourceColumn || !form.targetColumn) {
      addToast('请填写源字段和目标字段', 'error');
      return;
    }
    setSaving(true);
    try {
      const data = { ...form };
      if (editing) data.id = editing;
      const res = await MappingAPI.save(data);
      if (res.success) {
        addToast(editing ? '更新成功' : '创建成功', 'success');
        closeModal();
        await loadMappings(selectedTask);
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('确认删除此字段映射？')) return;
    try {
      await MappingAPI.delete(id);
      addToast('已删除', 'success');
      loadMappings(selectedTask);
    } catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">▣</span> 字段映射</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={() => openModal()} disabled={!selectedTask}>＋ 新增映射</button>
        </div>
      </div>

      <div className="content-area">
        <div className="filter-bar">
          <select value={selectedTask} onChange={e => changeTask(e.target.value)} style={{ minWidth: 280 }}>
            <option value="">选择任务...</option>
            {tasks.map(t => <option key={t.taskCode} value={t.taskCode}>{t.taskCode} — {t.taskName}</option>)}
          </select>
        </div>

        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>源字段</th>
                    <th>目标字段</th>
                    <th>数据类型</th>
                    <th>转换表达式</th>
                    <th>默认值</th>
                    <th>主键</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {!selectedTask ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      请先选择一个任务
                    </td></tr>
                  ) : loading ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : mappings.length === 0 ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      暂无字段映射数据
                    </td></tr>
                  ) : mappings.map(m => (
                    <tr key={m.id}>
                      <td className="text-muted">{m.mappingOrder || 0}</td>
                      <td className="text-cyan" style={{ fontWeight: 600 }}>{m.sourceColumn}</td>
                      <td style={{ fontWeight: 600, color: 'var(--accent-purple)' }}>{m.targetColumn}</td>
                      <td><span className="tag tag-blue">{m.dataType || '--'}</span></td>
                      <td className="text-mono text-sm">{m.transformExpr || <span className="text-muted">--</span>}</td>
                      <td>{m.defaultValue || <span className="text-muted">--</span>}</td>
                      <td>{m.isPrimaryKey === 'Y' ? <span className="tag tag-orange">PK</span> : <span className="text-muted">--</span>}</td>
                      <td><span className={`tag ${m.enabled === 'Y' ? 'tag-green' : 'tag-dim'}`}>{m.enabled === 'Y' ? '启用' : '禁用'}</span></td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-xs btn-secondary" onClick={() => openModal(m)}>✎</button>
                          <button className="btn btn-xs btn-danger" onClick={() => handleDelete(m.id)}>✕</button>
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
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 680 }}>
            <div className="modal-header">
              <h3>{editing ? '✎ 编辑映射' : '＋ 新增映射'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label>源字段 <span className="required">*</span></label>
                  <input value={form.sourceColumn} onChange={e => update('sourceColumn', e.target.value)} />
                </div>
                <div className="form-group">
                  <label>目标字段 <span className="required">*</span></label>
                  <input value={form.targetColumn} onChange={e => update('targetColumn', e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>数据类型</label>
                  <select value={form.dataType} onChange={e => update('dataType', e.target.value)}>
                    <option value="">--</option>
                    {DATA_TYPES.map(dt => <option key={dt} value={dt}>{dt}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>默认值</label>
                  <input value={form.defaultValue} onChange={e => update('defaultValue', e.target.value)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>转换表达式</label>
                  <input value={form.transformExpr} onChange={e => update('transformExpr', e.target.value)} placeholder="如: UPPER(#sourceColumn)" />
                </div>
                <div className="form-group">
                  <label>映射顺序</label>
                  <input type="number" value={form.mappingOrder} onChange={e => update('mappingOrder', parseInt(e.target.value) || 0)} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>是否主键</label>
                  <select value={form.isPrimaryKey} onChange={e => update('isPrimaryKey', e.target.value)}>
                    <option value="N">否</option><option value="Y">是</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>状态</label>
                  <select value={form.enabled} onChange={e => update('enabled', e.target.value)}>
                    <option value="Y">启用</option><option value="N">禁用</option>
                  </select>
                </div>
              </div>
              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}>
                <label>描述</label><textarea value={form.description} onChange={e => update('description', e.target.value)} rows={2} />
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
