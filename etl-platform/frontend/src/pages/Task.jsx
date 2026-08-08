import { useState, useEffect, useCallback } from 'react';
import { TaskAPI, DataSourceAPI, ExtractAPI } from '../api/etl';
import { useToast } from '../components/useToast';
import DebugPanel from '../components/DebugPanel';

const SOURCE_TYPES = [
  { value: 'SQL', label: 'SQL查询', icon: '▣', desc: '自定义 SQL 语句抽取' },
  { value: 'TABLE', label: '全表', icon: '⊞', desc: '整表抽取' },
  { value: 'VIEW', label: '视图', icon: '⊟', desc: '数据库视图抽取' },
  { value: 'PROCEDURE', label: '存储过程', icon: '⚙', desc: '调用存储过程获取数据' },
  { value: 'HTTP', label: 'HTTP接口', icon: '⇄', desc: 'REST API / JSON / XML 接口' },
  { value: 'SOAP', label: 'WebService', icon: '⎔', desc: 'SOAP 1.1/1.2 接口' },
  { value: 'FILE', label: '文件', icon: '▤', desc: 'CSV / JSON / Excel 文件' },
];

const EMPTY_TASK = {
  taskCode: '', taskName: '', sourceDsName: '', targetDsName: '',
  sourceType: 'SQL', writeMode: 'INSERT', targetTable: '', cronExpr: '',
  batchSize: 2000, timeoutSeconds: 1800, description: '',
  sourceProcedure: '', sourceTable: '', sourceSql: '', sourceView: '', sourceParams: '',
  httpUrl: '', httpMethod: 'GET', httpResponseType: 'JSON',
  httpHeaders: '', httpBody: '', httpAuthType: 'NONE',
  httpUsername: '', httpPassword: '', httpToken: '',
  httpDataPath: '', httpPagination: 'N',
  httpPageParam: 'page', httpSizeParam: 'size', httpPageSize: 1000,
  httpTimeout: 30000, httpEncoding: 'UTF-8',
  soapAction: '', soapBinding: 'SOAP11', soapNamespace: '',
  filePath: '', fileFormat: '', fileDelimiter: ',', fileEncoding: 'UTF-8', fileHeader: 'Y',
};

export default function Task() {
  const [list, setList] = useState([]);
  const [dsNames, setDsNames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_TASK });
  const [saving, setSaving] = useState(false);
  const [debugTask, setDebugTask] = useState(null);

  // Inline extract test state
  const [testResult, setTestResult] = useState(null);
  const [testing, setTesting] = useState(false);

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
    setTestResult(null);
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
    try { await TaskAPI.delete(id); addToast('已删除', 'success'); loadList(); }
    catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  const handleExecute = async (taskCode) => {
    if (!window.confirm(`确认手动执行任务 ${taskCode} ？`)) return;
    try {
      const res = await TaskAPI.execute(taskCode);
      if (res.success) addToast(`任务 ${taskCode} 执行成功`, 'success');
    } catch (e) { addToast('执行失败: ' + e.message, 'error'); }
  };

  // Inline extract test
  const handleTestExtract = async () => {
    setTesting(true);
    setTestResult(null);
    try {
      const req = buildExtractRequest();
      const res = await ExtractAPI.test(req);
      setTestResult(res.success ? res.data : null);
      if (res.success && res.data && res.data.success) {
        addToast(`抽取测试成功，返回 ${res.data.totalRows || 0} 条数据`, 'success');
      } else if (res.success && res.data) {
        addToast(`抽取测试失败: ${res.data.errorMessage || '未知错误'}`, 'error');
      }
    } catch (e) {
      addToast('测试失败: ' + e.message, 'error');
      setTestResult({ success: false, errorMessage: e.message });
    } finally {
      setTesting(false);
    }
  };

  const buildExtractRequest = () => ({
    sourceType: form.sourceType,
    dataSourceName: form.sourceDsName,
    tableName: form.sourceTable,
    procedureName: form.sourceProcedure,
    sqlText: form.sourceSql,
    viewName: form.sourceView,
    sourceParams: form.sourceParams,
    url: form.httpUrl,
    httpMethod: form.httpMethod,
    headers: form.httpHeaders,
    requestBody: form.httpBody,
    authType: form.httpAuthType,
    authUsername: form.httpUsername,
    authPassword: form.httpPassword,
    authToken: form.httpToken,
    responseType: form.httpResponseType,
    dataPath: form.httpDataPath,
    pagination: form.httpPagination,
    pageParam: form.httpPageParam,
    sizeParam: form.httpSizeParam,
    pageSize: form.httpPageSize,
    timeout: form.httpTimeout,
    soapAction: form.soapAction,
    soapBinding: form.soapBinding,
    soapNamespace: form.soapNamespace,
    limit: 20,
  });

  const st = form.sourceType || 'SQL';
  const isDb = ['SQL', 'TABLE', 'VIEW', 'PROCEDURE'].includes(st);
  const isHttp = st === 'HTTP';
  const isSoap = st === 'SOAP';
  const isFile = st === 'FILE';

  const SOURCE_LABELS = Object.fromEntries(SOURCE_TYPES.map(s => [s.value, s.label]));

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
                      <td><span className="tag tag-purple">{SOURCE_LABELS[t.sourceType] || t.sourceType}</span></td>
                      <td className="text-mono text-sm">{t.targetTable}</td>
                      <td><span className="tag tag-orange">{t.writeMode}</span></td>
                      <td className="text-mono text-sm">{t.cronExpr || <span className="text-muted">--</span>}</td>
                      <td><span className={`tag ${t.enabled === 'Y' ? 'tag-green' : 'tag-dim'}`}>{t.enabled === 'Y' ? '已激活' : '未激活'}</span></td>
                      <td>
                        <div className="btn-group">
                          <button className="btn btn-xs btn-primary" onClick={() => setDebugTask(t)} title="分步调试">⛭</button>
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
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 820, maxHeight: '90vh', overflow: 'auto' }}>
            <div className="modal-header">
              <h3>{editing ? '✎ 编辑任务' : '＋ 新建任务'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">

              {/* ── 1. 基础信息 ── */}
              <fieldset className="fieldset-card">
                <legend><span className="bar-icon">◈</span> 基础信息</legend>
                <div className="form-row">
                  <div className="form-group"><label>任务编码 <span className="required">*</span></label><input value={form.taskCode} onChange={e => update('taskCode', e.target.value)} placeholder="如: IMP_CUSTOMER" /></div>
                  <div className="form-group"><label>任务名称 <span className="required">*</span></label><input value={form.taskName} onChange={e => update('taskName', e.target.value)} placeholder="如: 客户数据导入" /></div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>目标数据源</label>
                    <select value={form.targetDsName} onChange={e => update('targetDsName', e.target.value)}>
                      <option value="">请选择</option>
                      {dsNames.map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>目标表 <span className="required">*</span></label>
                    <input value={form.targetTable} onChange={e => update('targetTable', e.target.value)} placeholder="如: TGT_CUSTOMER" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>写入模式</label>
                    <select value={form.writeMode} onChange={e => update('writeMode', e.target.value)}>
                      <option value="INSERT">INSERT</option><option value="MERGE">MERGE</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Cron 表达式</label>
                    <input value={form.cronExpr} onChange={e => update('cronExpr', e.target.value)} placeholder="0 0 * * * ?" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group"><label>批量大小</label><input type="number" value={form.batchSize} onChange={e => update('batchSize', parseInt(e.target.value) || 2000)} /></div>
                  <div className="form-group"><label>超时(秒)</label><input type="number" value={form.timeoutSeconds} onChange={e => update('timeoutSeconds', parseInt(e.target.value) || 1800)} /></div>
                </div>
                <div className="form-row">
                  <div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>描述</label><textarea value={form.description || ''} onChange={e => update('description', e.target.value)} rows={1} />
                  </div>
                </div>
              </fieldset>

              {/* ── 2. 抽取配置 ── */}
              <fieldset className="fieldset-card" style={{ marginTop: 16 }}>
                <legend><span className="bar-icon">⇣</span> 抽取源配置</legend>

                {/* 抽取类型选择器 */}
                <div className="form-row">
                  <div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>抽取方式 <span className="required">*</span></label>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', gap: 6 }}>
                      {SOURCE_TYPES.map(s => (
                        <button
                          key={s.value}
                          type="button"
                          onClick={() => update('sourceType', s.value)}
                          className={`btn btn-sm ${st === s.value ? 'btn-primary' : 'btn-secondary'}`}
                          title={s.desc}
                          style={{ textAlign: 'center', padding: '8px 6px', fontSize: 12 }}
                        >
                          <div style={{ fontSize: 16 }}>{s.icon}</div>
                          <div>{s.label}</div>
                        </button>
                      ))}
                    </div>
                  </div>
                </div>

                {/* ── DB 类型配置 ── */}
                {isDb && (
                  <>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>源数据源</label>
                        <select value={form.sourceDsName} onChange={e => update('sourceDsName', e.target.value)}>
                          <option value="">请选择数据库数据源</option>
                          {dsNames.map(n => <option key={n} value={n}>{n}</option>)}
                        </select>
                      </div>
                    </div>
                    {st === 'SQL' && (
                      <div className="form-row">
                        <div className="form-group" style={{ gridColumn: '1/-1' }}>
                          <label>SQL 语句 <span className="required">*</span></label>
                          <textarea value={form.sourceSql || ''} onChange={e => update('sourceSql', e.target.value)} rows={4} placeholder="SELECT * FROM TABLE_NAME WHERE ..." style={{ fontFamily: 'var(--font-mono)', fontSize: 13 }} />
                        </div>
                      </div>
                    )}
                    {st === 'TABLE' && (
                      <div className="form-row">
                        <div className="form-group" style={{ gridColumn: '1/-1' }}>
                          <label>表名 <span className="required">*</span></label>
                          <input value={form.sourceTable || ''} onChange={e => update('sourceTable', e.target.value)} placeholder="SOURCE_TABLE" />
                        </div>
                      </div>
                    )}
                    {st === 'VIEW' && (
                      <div className="form-row">
                        <div className="form-group" style={{ gridColumn: '1/-1' }}>
                          <label>视图名 <span className="required">*</span></label>
                          <input value={form.sourceView || ''} onChange={e => update('sourceView', e.target.value)} placeholder="SOURCE_VIEW" />
                        </div>
                      </div>
                    )}
                    {st === 'PROCEDURE' && (
                      <div className="form-row">
                        <div className="form-group">
                          <label>存储过程名 <span className="required">*</span></label>
                          <input value={form.sourceProcedure || ''} onChange={e => update('sourceProcedure', e.target.value)} placeholder="SP_GET_DATA" />
                        </div>
                        <div className="form-group">
                          <label>参数 (JSON)</label>
                          <input value={form.sourceParams || ''} onChange={e => update('sourceParams', e.target.value)} placeholder='{"p_date":"20260801"}' />
                        </div>
                      </div>
                    )}
                  </>
                )}

                {/* ── HTTP 类型配置 ── */}
                {isHttp && (
                  <>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>请求 URL <span className="required">*</span></label>
                        <input value={form.httpUrl || ''} onChange={e => update('httpUrl', e.target.value)} placeholder="https://api.example.com/v1/data" />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>HTTP 方法</label>
                        <select value={form.httpMethod || 'GET'} onChange={e => update('httpMethod', e.target.value)}>
                          <option value="GET">GET</option><option value="POST">POST</option><option value="PUT">PUT</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>响应格式</label>
                        <select value={form.httpResponseType || 'JSON'} onChange={e => update('httpResponseType', e.target.value)}>
                          <option value="JSON">JSON</option><option value="XML">XML</option>
                        </select>
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>数据路径 (JSONPath / XPath)</label>
                        <input value={form.httpDataPath || ''} onChange={e => update('httpDataPath', e.target.value)} placeholder="$.data.list 或 response.body.items" />
                      </div>
                    </div>
                    {/* 认证配置 */}
                    <div className="form-row">
                      <div className="form-group">
                        <label>认证方式</label>
                        <select value={form.httpAuthType || 'NONE'} onChange={e => update('httpAuthType', e.target.value)}>
                          <option value="NONE">无认证</option><option value="BASIC">Basic Auth</option><option value="TOKEN">Bearer Token</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>超时(ms)</label>
                        <input type="number" value={form.httpTimeout || 30000} onChange={e => update('httpTimeout', parseInt(e.target.value) || 30000)} />
                      </div>
                    </div>
                    {(form.httpAuthType === 'BASIC') && (
                      <div className="form-row">
                        <div className="form-group"><label>用户名</label><input value={form.httpUsername || ''} onChange={e => update('httpUsername', e.target.value)} /></div>
                        <div className="form-group"><label>密码</label><input type="password" value={form.httpPassword || ''} onChange={e => update('httpPassword', e.target.value)} /></div>
                      </div>
                    )}
                    {(form.httpAuthType === 'TOKEN') && (
                      <div className="form-row">
                        <div className="form-group" style={{ gridColumn: '1/-1' }}>
                          <label>Token</label>
                          <input value={form.httpToken || ''} onChange={e => update('httpToken', e.target.value)} placeholder="Bearer Token" />
                        </div>
                      </div>
                    )}
                    {/* 高级配置 */}
                    <details style={{ marginTop: 8 }}>
                      <summary style={{ cursor: 'pointer', color: 'var(--accent-cyan)', fontSize: 13 }}>▶ 高级配置（请求头/请求体/分页）</summary>
                      <div style={{ marginTop: 8 }}>
                        <div className="form-row">
                          <div className="form-group" style={{ gridColumn: '1/-1' }}>
                            <label>请求头 (JSON)</label>
                            <textarea value={form.httpHeaders || ''} onChange={e => update('httpHeaders', e.target.value)} rows={2} placeholder='{"Authorization":"Bearer xxx","Content-Type":"application/json"}' style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} />
                          </div>
                        </div>
                        {(form.httpMethod !== 'GET') && (
                          <div className="form-row">
                            <div className="form-group" style={{ gridColumn: '1/-1' }}>
                              <label>请求体</label>
                              <textarea value={form.httpBody || ''} onChange={e => update('httpBody', e.target.value)} rows={3} placeholder='{"key":"value"}' style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} />
                            </div>
                          </div>
                        )}
                        <div className="form-row">
                          <div className="form-group">
                            <label>分页</label>
                            <select value={form.httpPagination || 'N'} onChange={e => update('httpPagination', e.target.value)}>
                              <option value="N">不分页</option><option value="Y">分页</option>
                            </select>
                          </div>
                          {form.httpPagination === 'Y' && (
                            <>
                              <div className="form-group"><label>页码参数</label><input value={form.httpPageParam || 'page'} onChange={e => update('httpPageParam', e.target.value)} /></div>
                              <div className="form-group"><label>每页参数</label><input value={form.httpSizeParam || 'size'} onChange={e => update('httpSizeParam', e.target.value)} /></div>
                              <div className="form-group"><label>每页大小</label><input type="number" value={form.httpPageSize || 1000} onChange={e => update('httpPageSize', parseInt(e.target.value) || 1000)} /></div>
                            </>
                          )}
                        </div>
                      </div>
                    </details>
                  </>
                )}

                {/* ── SOAP 类型配置 ── */}
                {isSoap && (
                  <>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>Endpoint URL <span className="required">*</span></label>
                        <input value={form.httpUrl || ''} onChange={e => update('httpUrl', e.target.value)} placeholder="http://host:port/service" />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>SOAP 版本</label>
                        <select value={form.soapBinding || 'SOAP11'} onChange={e => update('soapBinding', e.target.value)}>
                          <option value="SOAP11">SOAP 1.1</option><option value="SOAP12">SOAP 1.2</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>响应格式</label>
                        <select value={form.httpResponseType || 'XML'} onChange={e => update('httpResponseType', e.target.value)}>
                          <option value="XML">XML</option><option value="JSON">JSON</option>
                        </select>
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>SOAP Action</label>
                        <input value={form.soapAction || ''} onChange={e => update('soapAction', e.target.value)} placeholder="http://example.com/GetData" />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>SOAP Envelope (XML 请求体) <span className="required">*</span></label>
                        <textarea value={form.httpBody || ''} onChange={e => update('httpBody', e.target.value)} rows={6}
                          placeholder={`<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">\n  <soap:Body>\n    <GetData xmlns="http://example.com/">\n      <param1>value1</param1>\n    </GetData>\n  </soap:Body>\n</soap:Envelope>`}
                          style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group" style={{ gridColumn: '1/-1' }}>
                        <label>数据路径 (XPath 风格)</label>
                        <input value={form.httpDataPath || ''} onChange={e => update('httpDataPath', e.target.value)} placeholder="Body.GetDataResponse.return" />
                      </div>
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>命名空间 (可选)</label>
                        <input value={form.soapNamespace || ''} onChange={e => update('soapNamespace', e.target.value)} placeholder="http://example.com/" />
                      </div>
                      <div className="form-group">
                        <label>超时(ms)</label>
                        <input type="number" value={form.httpTimeout || 60000} onChange={e => update('httpTimeout', parseInt(e.target.value) || 60000)} />
                      </div>
                    </div>
                  </>
                )}

                {/* ── 文件类型配置 ── */}
                {isFile && (
                  <div className="form-row">
                    <div className="form-group"><label>文件路径</label><input value={form.filePath || ''} onChange={e => update('filePath', e.target.value)} /></div>
                  </div>
                )}

                {/* ── 抽取测试区域 ── */}
                {(isHttp || isSoap) && (
                  <div style={{ marginTop: 16, padding: 14, background: 'var(--bg-card-alt, rgba(0,255,255,0.03))', borderRadius: 10, border: '1px solid var(--border-dim)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--accent-cyan)' }}>
                        <span className="bar-icon">⇣</span> 抽取测试（不保存任务）
                      </div>
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={handleTestExtract}
                        disabled={testing}
                      >
                        {testing ? '测试中...' : '▶ 测试抽取'}
                      </button>
                    </div>
                    {testing && (
                      <div style={{ textAlign: 'center', padding: 20 }}>
                        <div className="loader" style={{ margin: '0 auto' }} />
                        <div className="loading-text" style={{ marginTop: 8 }}>执行请求中...</div>
                      </div>
                    )}
                    {!testing && testResult && (
                      <div>
                        <div style={{ marginBottom: 8, display: 'flex', gap: 12, fontSize: 13 }}>
                          <span className={`tag ${testResult.success ? 'tag-green' : 'tag-red'}`}>
                            {testResult.success ? '成功' : '失败'}
                          </span>
                          {testResult.success && (
                            <>
                              <span className="text-muted">返回 {testResult.totalRows || 0} 条</span>
                              <span className="text-muted">耗时 {testResult.durationMs || 0} ms</span>
                            </>
                          )}
                          {!testResult.success && (
                            <span className="text-muted" style={{ color: 'var(--danger)' }}>{testResult.errorMessage}</span>
                          )}
                        </div>
                        {testResult.success && testResult.parsedData && testResult.parsedData.length > 0 && (
                          <div style={{ maxHeight: 200, overflow: 'auto', borderRadius: 8, border: '1px solid var(--border-dim)' }}>
                            <table className="data-table" style={{ fontSize: 12 }}>
                              <thead>
                                <tr>
                                  {Object.keys(testResult.parsedData[0]).map(k => (
                                    <th key={k} style={{ color: 'var(--accent-purple)' }}>{k}</th>
                                  ))}
                                </tr>
                              </thead>
                              <tbody>
                                {testResult.parsedData.slice(0, 10).map((row, i) => (
                                  <tr key={i}>
                                    {Object.values(row).map((v, j) => (
                                      <td key={j} className="text-mono text-sm">{v === null ? <span className="text-muted">NULL</span> : String(v)}</td>
                                    ))}
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                            {testResult.parsedData.length > 10 && (
                              <div style={{ textAlign: 'center', padding: 6, color: 'var(--text-muted)', fontSize: 12 }}>
                                ... 仅显示前 10 条，共 {testResult.parsedData.length} 条
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    )}
                    {!testing && !testResult && (
                      <div className="text-muted" style={{ fontSize: 13, textAlign: 'center', padding: 8 }}>
                        配置完抽取地址和参数后，点击"测试抽取"验证配置是否正确
                      </div>
                    )}
                  </div>
                )}
              </fieldset>
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

      {debugTask && (
        <DebugPanel
          taskCode={debugTask.taskCode}
          taskName={debugTask.taskName}
          onClose={() => setDebugTask(null)}
        />
      )}
    </div>
  );
}
