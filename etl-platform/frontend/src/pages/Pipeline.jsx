import { useState, useEffect, useCallback, Fragment } from 'react';
import { PipelineAPI, DataSourceAPI, StepAPI, ExtractAPI } from '../api/etl';
import { useToast } from '../components/useToast';
import DebugPanel from '../components/DebugPanel';
import usePipelineStream from '../components/usePipelineStream';

const SOURCE_TYPES = [
  { value: 'SQL', label: 'SQL查询', icon: '▣' },
  { value: 'TABLE', label: '全表', icon: '⊞' },
  { value: 'VIEW', label: '视图', icon: '⊟' },
  { value: 'PROCEDURE', label: '存储过程', icon: '⚙' },
  { value: 'HTTP', label: 'HTTP接口', icon: '⇄' },
  { value: 'SOAP', label: 'WebService', icon: '⎔' },
  { value: 'FILE', label: '文件', icon: '▤' },
];

const STEP_TYPES = [
  { value: 'EXTRACT', label: '抽取', icon: '⇣', desc: '从数据源读取数据' },
  { value: 'TRANSFORM', label: '转换', icon: '↻', desc: '字段映射/合并' },
  { value: 'LOAD', label: '加载', icon: '⇡', desc: '写入目标' },
];

const TRANSFORM_SUB_TYPES = [
  { value: 'FIELD_MAP', label: '字段映射', desc: '源字段→目标字段' },
  { value: 'JOIN', label: '横向合并(JOIN)', desc: '多源按条件横向拼接' },
  { value: 'UNION', label: '纵向合并(UNION)', desc: '多源数据纵向追加' },
];

const LOAD_SUB_TYPES = [
  { value: 'DB_INSERT', label: '写入数据库表', desc: 'INSERT/MERGE 到数据库' },
  { value: 'FILE_CSV', label: '导出 CSV', desc: '写入CSV文件' },
  { value: 'FILE_JSON', label: '导出 JSON', desc: '写入JSON文件' },
];

const EMPTY_PIPELINE = {
  pipelineCode: '', pipelineName: '', cronExpr: '', enabled: 'Y', description: '',
  steps: [], edges: [],
};

const STEP_TEMPLATE = {
  _tempId: '', stepCode: '', stepName: '', stepType: 'EXTRACT', stepSubType: '',
  orderIndex: 0,
  sourceDsName: '', sourceType: 'SQL',
  sourceConfig: '{}',
  targetDsName: '', targetConfig: '{}', writeMode: 'INSERT',
  batchSize: 2000, timeoutSeconds: 1800,
};

let _tempIdCounter = 0;
const nextTempId = () => `_new_${++_tempIdCounter}`;

// ── Inline run-observation helpers ──
const STEP_FLOW_COLORS = { EXTRACT: 'var(--accent-cyan)', TRANSFORM: 'var(--accent-purple)', LOAD: 'var(--accent-orange)' };
const STEP_STATUS_LABEL = { SUCCESS: '✓ 成功', FAILED: '✕ 失败', RUNNING: '◌ 运行中', SKIPPED: '↷ 跳过', PENDING: '○ 等待' };

function statusClass(s) {
  if (s.status === 'SUCCESS') return 'tag-green';
  if (s.status === 'FAILED') return 'tag-red';
  if (s.status === 'RUNNING') return 'tag-blue';
  return 'tag-dim';
}

// 卡片式步骤流：每步一个状态卡片，运行中高亮脉动，失败红色高亮
function StepFlow({ steps }) {
  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'stretch', gap: 8, flexWrap: 'wrap' }}>
        {steps.map((s, i) => {
          const st = STEP_TYPES.find(t => t.value === s.stepType);
          const isFail = s.status === 'FAILED';
          const isRun = s.status === 'RUNNING';
          const isDone = s.status === 'SUCCESS';
          return (
            <Fragment key={s.stepId ?? i}>
              <div style={{
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
                padding: '10px 14px', minWidth: 96,
                borderRadius: 10,
                background: isFail ? 'rgba(239,68,68,0.08)' : isRun ? 'rgba(34,211,238,0.07)' : 'var(--bg-card)',
                border: isFail ? '1.5px solid rgba(239,68,68,0.6)'
                      : isRun ? '1.5px solid var(--accent-cyan)'
                      : isDone ? '1px solid rgba(16,185,129,0.5)'
                      : '1px solid var(--border-dim)',
                boxShadow: isRun ? '0 0 14px rgba(34,211,238,0.18)' : undefined,
                animation: isRun ? 'pulseGlow 1.6s ease-in-out infinite' : undefined,
              }}>
                <div style={{ fontSize: 20, lineHeight: 1, color: isFail ? 'var(--accent-red)' : (STEP_FLOW_COLORS[s.stepType] || 'var(--accent-cyan)') }}>
                  {st?.icon || '○'}
                </div>
                <div style={{ fontSize: 12, fontWeight: 600, whiteSpace: 'nowrap' }}>{s.stepName}</div>
                <div style={{ fontSize: 10, color: 'var(--text-muted)' }}>{st?.label || s.stepType}</div>
                <span className={`tag ${statusClass(s)}`} style={{ fontSize: 10, marginTop: 2 }}>
                  {STEP_STATUS_LABEL[s.status] || '○ 等待'}
                </span>
                {s.durationMs > 0 && <div style={{ fontSize: 10, color: 'var(--text-muted)' }}>{s.durationMs} ms</div>}
                {(s.inputRows > 0 || s.outputRows > 0) && (
                  <div style={{ fontSize: 10, color: 'var(--text-muted)' }}>入 {s.inputRows} · 出 {s.outputRows}</div>
                )}
              </div>
              {i < steps.length - 1 && (
                <div style={{ display: 'flex', alignItems: 'center', color: isDone ? 'var(--accent-cyan)' : 'var(--text-muted)', fontWeight: 700 }}>→</div>
              )}
            </Fragment>
          );
        })}
      </div>
    </div>
  );
}

export default function Pipeline() {
  const [list, setList] = useState([]);
  const [dsList, setDsList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_PIPELINE });
  const [saving, setSaving] = useState(false);
  const [activeStepIdx, setActiveStepIdx] = useState(-1);
  const [debugPipeline, setDebugPipeline] = useState(null);

  // Inline run observation (SSE step status)
  const stream = usePipelineStream();
  const [expandedCode, setExpandedCode] = useState(null);
  const [runCode, setRunCode] = useState(null);
  const [stepsCache, setStepsCache] = useState({});
  const [stepsLoadingCode, setStepsLoadingCode] = useState(null);

  // Inline extract test
  const [testResult, setTestResult] = useState(null);
  const [testing, setTesting] = useState(false);

  const { addToast, ToastContainer } = useToast();

  const loadList = useCallback(async () => {
    setLoading(true);
    try {
      const [pipeRes, dsRes] = await Promise.all([PipelineAPI.list(), DataSourceAPI.list()]);
      if (pipeRes.success) setList(pipeRes.data || []);
      if (dsRes.success) setDsList(dsRes.data || []);
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  useEffect(() => { loadList(); }, [loadList]);

  const openModal = (pipeline = null) => {
    if (pipeline) {
      setEditingId(pipeline.id);
      // 加载完整的管线详情（含步骤和连线）
      PipelineAPI.get(pipeline.id).then(res => {
        if (res.success) {
          const d = res.data;
          const steps = (d.steps || []).map(s => ({ ...s, _tempId: String(s.id) }));
          setForm({
            pipelineCode: d.pipeline.pipelineCode,
            pipelineName: d.pipeline.pipelineName,
            cronExpr: d.pipeline.cronExpr || '',
            enabled: d.pipeline.enabled || 'Y',
            description: d.pipeline.description || '',
            steps,
            edges: d.edges || [],
          });
        }
      }).catch(() => {});
    } else {
      setEditingId(null);
      setForm({ ...EMPTY_PIPELINE });
    }
    setActiveStepIdx(-1);
    setTestResult(null);
    setModalOpen(true);
  };

  const closeModal = () => setModalOpen(false);

  const getDsNamesByProtocol = (protocol) =>
    dsList.filter(d => (d.protocol || 'JDBC') === protocol).map(d => d.dsName);

  // ── Step management ──
  const addStep = () => {
    const step = { ...STEP_TEMPLATE, _tempId: nextTempId(), stepCode: `step_${form.steps.length + 1}`, stepName: `步骤${form.steps.length + 1}`, orderIndex: form.steps.length + 1 };
    setForm(prev => ({ ...prev, steps: [...prev.steps, step] }));
    setActiveStepIdx(form.steps.length);
  };

  const removeStep = (idx) => {
    setForm(prev => {
      const steps = prev.steps.filter((_, i) => i !== idx);
      const removedId = prev.steps[idx]._tempId;
      // 同时清理相关连线
      const edges = prev.edges.filter(e => e.fromStepId !== removedId && e.toStepId !== removedId);
      return { ...prev, steps, edges };
    });
    if (activeStepIdx >= idx) setActiveStepIdx(Math.max(-1, activeStepIdx - 1));
  };

  const updateStep = (idx, field, value) => {
    setForm(prev => {
      const steps = [...prev.steps];
      steps[idx] = { ...steps[idx], [field]: value };
      return { ...prev, steps };
    });
  };

  const updateStepSourceConfig = (idx, key, value) => {
    setForm(prev => {
      const steps = [...prev.steps];
      let config;
      try { config = JSON.parse(steps[idx].sourceConfig || '{}'); } catch { config = {}; }
      config[key] = value;
      steps[idx] = { ...steps[idx], sourceConfig: JSON.stringify(config) };
      return { ...prev, steps };
    });
  };

  const updateStepTargetConfig = (idx, key, value) => {
    setForm(prev => {
      const steps = [...prev.steps];
      let config;
      try { config = JSON.parse(steps[idx].targetConfig || '{}'); } catch { config = {}; }
      config[key] = value;
      steps[idx] = { ...steps[idx], targetConfig: JSON.stringify(config) };
      return { ...prev, steps };
    });
  };

  const getStepSourceConfig = (step, key, defaultVal = '') => {
    try { const c = JSON.parse(step.sourceConfig || '{}'); return c[key] !== undefined ? c[key] : defaultVal; }
    catch { return defaultVal; }
  };

  const getStepTargetConfig = (step, key, defaultVal = '') => {
    try { const c = JSON.parse(step.targetConfig || '{}'); return c[key] !== undefined ? c[key] : defaultVal; }
    catch { return defaultVal; }
  };

  // ── Edge management ──
  const addEdge = (fromId, toId) => {
    if (!fromId || !toId || fromId === toId) return;
    const exists = form.edges.some(e => e.fromStepId === fromId && e.toStepId === toId);
    if (exists) {
      removeEdge(fromId, toId);
      return;
    }
    setForm(prev => ({ ...prev, edges: [...prev.edges, { fromStepId: fromId, toStepId: toId, edgeType: 'PASS', edgeConfig: '{}' }] }));
  };

  const removeEdge = (fromId, toId) => {
    setForm(prev => ({ ...prev, edges: prev.edges.filter(e => !(e.fromStepId === fromId && e.toStepId === toId)) }));
  };

  // 自动串联所有步骤（按顺序）
  const autoChain = () => {
    const newEdges = [];
    for (let i = 0; i < form.steps.length - 1; i++) {
      newEdges.push({
        fromStepId: form.steps[i]._tempId,
        toStepId: form.steps[i + 1]._tempId,
        edgeType: 'PASS',
        edgeConfig: '{}',
      });
    }
    setForm(prev => ({ ...prev, edges: newEdges }));
  };

  // ── Save ──
  const handleSave = async () => {
    if (!form.pipelineCode || !form.pipelineName) {
      addToast('请填写管线编码和名称', 'error'); return;
    }
    if (form.steps.length === 0) {
      addToast('请至少添加一个步骤', 'error'); return;
    }
    setSaving(true);
    try {
      const data = {
        pipelineCode: form.pipelineCode,
        pipelineName: form.pipelineName,
        cronExpr: form.cronExpr || null,
        enabled: form.enabled,
        description: form.description,
        steps: form.steps.map(s => ({
          _tempId: s._tempId,
          stepCode: s.stepCode, stepName: s.stepName, stepType: s.stepType,
          stepSubType: s.stepSubType || null, orderIndex: s.orderIndex,
          sourceDsName: s.sourceDsName || null, sourceType: s.sourceType || null,
          sourceConfig: s.sourceConfig,
          targetDsName: s.targetDsName || null, targetConfig: s.targetConfig,
          writeMode: s.writeMode || null, batchSize: s.batchSize, timeoutSeconds: s.timeoutSeconds,
          enabled: 'Y',
        })),
        edges: form.edges.map(e => ({
          fromStepId: e.fromStepId, toStepId: e.toStepId,
          edgeType: e.edgeType || 'PASS', edgeConfig: e.edgeConfig || '{}',
        })),
      };
      if (editingId) data.id = editingId;
      // For edit mode, use PUT for basic info + manage steps/edges separately via step API
      // For simplicity, use a combined approach: PUT pipeline info + POST steps
      const res = await PipelineAPI.save(data);
      if (res.success) {
        addToast(editingId ? '更新成功' : '创建成功', 'success');
        closeModal();
        await loadList();
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('确认删除此管线？将同时删除所有步骤和连线。')) return;
    try { await PipelineAPI.delete(id); addToast('已删除', 'success'); loadList(); }
    catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  // ── Inline run observation ──
  const toggleExpand = async (p) => {
    const willExpand = expandedCode !== p.pipelineCode;
    stream.close(); // 收起/切换时停止正在进行的流
    setRunCode(null);
    setExpandedCode(willExpand ? p.pipelineCode : null);
    if (willExpand && !stepsCache[p.id]) {
      setStepsLoadingCode(p.pipelineCode);
      try {
        const res = await PipelineAPI.get(p.id);
        if (res.success) setStepsCache(prev => ({ ...prev, [p.id]: res.data.steps || [] }));
      } catch (e) { addToast('加载步骤失败: ' + e.message, 'error'); }
      finally { setStepsLoadingCode(null); }
    }
  };

  const handleRunObserve = (p) => {
    setExpandedCode(p.pipelineCode);
    setRunCode(p.pipelineCode);
    stream.start(p.pipelineCode, true); // 完整流程（含写入）
    if (!stepsCache[p.id]) {
      PipelineAPI.get(p.id).then(res => {
        if (res.success) setStepsCache(prev => ({ ...prev, [p.id]: res.data.steps || [] }));
      }).catch(() => {});
    }
  };

  const getFlowSteps = (p) => {
    if (runCode === p.pipelineCode && stream.meta) return stream.steps;
    const cached = stepsCache[p.id];
    if (cached) return cached.map(s => ({
      stepId: s.id, stepName: s.stepName, stepType: s.stepType,
      status: 'PENDING', inputRows: 0, outputRows: 0,
    }));
    return null;
  };

  // ── Extract test ──
  const handleTestExtract = async () => {
    if (activeStepIdx < 0) return;
    const step = form.steps[activeStepIdx];
    setTesting(true);
    setTestResult(null);
    try {
      const req = {
        sourceType: step.sourceType,
        dataSourceName: step.sourceDsName,
        tableName: getStepSourceConfig(step, 'sourceTable'),
        procedureName: getStepSourceConfig(step, 'sourceProcedure'),
        sqlText: getStepSourceConfig(step, 'sourceSql'),
        viewName: getStepSourceConfig(step, 'sourceView'),
        sourceParams: getStepSourceConfig(step, 'sourceParams'),
        url: getStepSourceConfig(step, 'httpUrl'),
        httpMethod: getStepSourceConfig(step, 'httpMethod', 'GET'),
        headers: getStepSourceConfig(step, 'httpHeaders'),
        requestBody: getStepSourceConfig(step, 'httpBody'),
        authType: getStepSourceConfig(step, 'httpAuthType', 'NONE'),
        authUsername: getStepSourceConfig(step, 'httpUsername'),
        authPassword: getStepSourceConfig(step, 'httpPassword'),
        authToken: getStepSourceConfig(step, 'httpToken'),
        responseType: getStepSourceConfig(step, 'httpResponseType', 'JSON'),
        dataPath: getStepSourceConfig(step, 'httpDataPath'),
        pagination: getStepSourceConfig(step, 'httpPagination', 'N'),
        pageParam: getStepSourceConfig(step, 'httpPageParam', 'page'),
        sizeParam: getStepSourceConfig(step, 'httpSizeParam', 'size'),
        pageSize: parseInt(getStepSourceConfig(step, 'httpPageSize', '1000')),
        timeout: parseInt(getStepSourceConfig(step, 'httpTimeout', '30000')),
        soapAction: getStepSourceConfig(step, 'soapAction'),
        soapBinding: getStepSourceConfig(step, 'soapBinding', 'SOAP11'),
        soapNamespace: getStepSourceConfig(step, 'soapNamespace'),
        limit: 20,
      };
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
    } finally { setTesting(false); }
  };

  const activeStep = activeStepIdx >= 0 ? form.steps[activeStepIdx] : null;
  const st = activeStep?.sourceType || 'SQL';
  const isDb = ['SQL', 'TABLE', 'VIEW', 'PROCEDURE'].includes(st);
  const isHttp = st === 'HTTP';
  const isSoap = st === 'SOAP';
  const isFile = st === 'FILE';

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">◈</span> 管线编排中心</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={() => openModal()}>＋ 新建管线</button>
        </div>
      </div>

      <div className="content-area">
        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>管线编码</th>
                    <th>管线名称</th>
                    <th>步骤数</th>
                    <th>Cron</th>
                    <th>状态</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : list.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      尚未创建管线 — 点击"新建管线"开始编排 ETL 流程
                    </td></tr>
                  ) : list.map(p => {
                    const flowSteps = getFlowSteps(p);
                    const isLive = runCode === p.pipelineCode;
                    return (
                      <Fragment key={p.id}>
                        <tr>
                          <td className="text-cyan" style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                            <button className="btn btn-xs btn-secondary" onClick={() => toggleExpand(p)}
                              title={expandedCode === p.pipelineCode ? '收起步骤' : '展开步骤流'}
                              style={{ marginRight: 8, padding: '1px 6px' }}>
                              {expandedCode === p.pipelineCode ? '▾' : '▸'}
                            </button>
                            {p.pipelineCode}
                          </td>
                          <td style={{ fontWeight: 600 }}>{p.pipelineName}</td>
                          <td><span className="tag tag-purple">{p.stepCount || 0} 步</span></td>
                          <td className="text-mono text-sm">{p.cronExpr || <span className="text-muted">--</span>}</td>
                          <td><span className={`tag ${p.enabled === 'Y' ? 'tag-green' : 'tag-dim'}`}>{p.enabled === 'Y' ? '已激活' : '未激活'}</span></td>
                          <td>
                            <div className="btn-group">
                              <button className="btn btn-xs btn-primary" onClick={() => handleRunObserve(p)} disabled={isLive && stream.running} title="内联运行观察（实时点亮每步状态）">▶ 运行观察</button>
                              <button className="btn btn-xs btn-secondary" onClick={() => setDebugPipeline(p)} title="分步调试">⛭</button>
                              <button className="btn btn-xs btn-secondary" onClick={() => openModal(p)} title="编辑">✎</button>
                              <button className="btn btn-xs btn-danger" onClick={() => handleDelete(p.id)} title="删除">✕</button>
                            </div>
                          </td>
                        </tr>
                        {expandedCode === p.pipelineCode && (
                          <tr style={{ background: 'var(--bg-card-alt, rgba(0,255,255,0.02))' }}>
                            <td colSpan={6} style={{ padding: '12px 16px', borderTop: 'none' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                                <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--accent-cyan)' }}>⛁ 步骤执行流</span>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                  {flowSteps && flowSteps.length > 0 && (
                                    <span className="tag tag-purple" style={{ fontSize: 11 }}>
                                      进度 {flowSteps.filter(s => ['SUCCESS','FAILED','SKIPPED'].includes(s.status)).length}/{flowSteps.length}
                                    </span>
                                  )}
                                  {isLive && stream.running && <span className="tag tag-blue">◌ 执行中</span>}
                                  {isLive && stream.done && stream.status && (
                                    <span className={`tag ${stream.status.status === 'SUCCESS' ? 'tag-green' : 'tag-red'}`}>
                                      {stream.status.status === 'SUCCESS' ? '✓ 全部成功' : '✕ 执行失败'}
                                    </span>
                                  )}
                                </div>
                              </div>

                              {flowSteps === null ? (
                                <div className="text-muted" style={{ fontSize: 12 }}>
                                  <div className="loader" style={{ width: 14, height: 14, borderWidth: 2, display: 'inline-block', marginRight: 8, verticalAlign: 'middle' }} />
                                  {isLive ? '正在启动执行...' : stepsLoadingCode === p.pipelineCode ? '加载步骤中...' : '点击"运行观察"开始执行，或编辑查看步骤'}
                                </div>
                              ) : (
                                <>
                                  <StepFlow steps={flowSteps} />
                                  {flowSteps.filter(s => s.errorMessage).map(s => (
                                    <div key={s.stepId} className="debug-step-error" style={{ marginTop: 8, fontSize: 12 }}>{s.stepName}: {s.errorMessage}</div>
                                  ))}
                                </>
                              )}

                              {isLive && stream.done && stream.status && (
                                <div style={{ marginTop: 10, fontSize: 12 }}>
                                  {stream.status.totalDurationMs > 0 && <span className="text-muted">总耗时 {stream.status.totalDurationMs} ms</span>}
                                  {stream.status.errorMessage && <span className="text-muted" style={{ marginLeft: 8 }}>{stream.status.errorMessage}</span>}
                                </div>
                              )}
                              {isLive && stream.error && <div className="debug-step-error" style={{ marginTop: 8, fontSize: 12 }}>{stream.error}</div>}
                            </td>
                          </tr>
                        )}
                      </Fragment>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* ── Pipeline Editor Modal ── */}
      {modalOpen && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 900, maxHeight: '92vh', overflow: 'auto' }}>
            <div className="modal-header">
              <h3>{editingId ? '✎ 编辑管线' : '＋ 新建管线'}</h3>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <div className="modal-body">

              {/* ── Basic Info ── */}
              <fieldset className="fieldset-card">
                <legend><span className="bar-icon">◈</span> 基础信息</legend>
                <div className="form-row">
                  <div className="form-group"><label>管线编码 <span className="required">*</span></label><input value={form.pipelineCode} onChange={e => setForm(prev => ({ ...prev, pipelineCode: e.target.value }))} placeholder="如: ETL_CUSTOMER" /></div>
                  <div className="form-group"><label>管线名称 <span className="required">*</span></label><input value={form.pipelineName} onChange={e => setForm(prev => ({ ...prev, pipelineName: e.target.value }))} placeholder="如: 客户数据汇集" /></div>
                </div>
                <div className="form-row">
                  <div className="form-group"><label>Cron 表达式</label><input value={form.cronExpr} onChange={e => setForm(prev => ({ ...prev, cronExpr: e.target.value }))} placeholder="0 0 * * * ?" /></div>
                  <div className="form-group"><label>状态</label><select value={form.enabled} onChange={e => setForm(prev => ({ ...prev, enabled: e.target.value }))}><option value="Y">启用</option><option value="N">禁用</option></select></div>
                </div>
                <div className="form-row">
                  <div className="form-group" style={{ gridColumn: '1/-1' }}><label>描述</label><textarea value={form.description || ''} onChange={e => setForm(prev => ({ ...prev, description: e.target.value }))} rows={1} /></div>
                </div>
              </fieldset>

              {/* ── Steps + Edges ── */}
              <fieldset className="fieldset-card" style={{ marginTop: 16 }}>
                <legend><span className="bar-icon">⛁</span> 步骤编排
                  <button className="btn btn-xs btn-primary" style={{ marginLeft: 12 }} onClick={addStep}>＋ 添加步骤</button>
                </legend>

                {/* DAG Visualization */}
                {form.steps.length > 0 && (
                  <div style={{ padding: 12, marginBottom: 12, background: 'var(--bg-card-alt, rgba(0,255,255,0.02))', borderRadius: 8, border: '1px solid var(--border-dim)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                      <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--accent-cyan)' }}>⛁ 数据流向</span>
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-xs btn-primary" onClick={autoChain} title="按顺序自动连接所有步骤">
                          ⇉ 串联所有步骤
                        </button>
                      </div>
                    </div>

                    {/* Vertical flow visualization */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
                      {form.steps.map((step, i) => (
                        <div key={step._tempId}>
                          {/* Step node */}
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <button
                              onClick={() => setActiveStepIdx(i)}
                              className={`btn btn-xs ${activeStepIdx === i ? 'btn-primary' : 'btn-secondary'}`}
                              style={{ fontSize: 12, padding: '6px 12px', minWidth: 160, textAlign: 'left' }}
                            >
                              <span style={{ marginRight: 6 }}>{STEP_TYPES.find(t => t.value === step.stepType)?.icon || '○'}</span>
                              {step.stepName}
                              <span className="tag tag-dim" style={{ marginLeft: 8, fontSize: 10 }}>{STEP_TYPES.find(t => t.value === step.stepType)?.label}</span>
                            </button>

                            {/* Manual edge target selector */}
                            {i < form.steps.length - 1 && (
                              <div style={{ display: 'flex', alignItems: 'center', gap: 4, flex: 1 }}>
                                {/* Arrow to next step — click to toggle edge */}
                                <button
                                  className="btn btn-xs"
                                  onClick={() => addEdge(step._tempId, form.steps[i + 1]._tempId)}
                                  style={{
                                    fontSize: 10, padding: '2px 8px', cursor: 'pointer',
                                    background: form.edges.some(e => e.fromStepId === step._tempId && e.toStepId === form.steps[i + 1]._tempId)
                                      ? 'var(--accent-cyan)' : 'var(--bg-card)',
                                    color: form.edges.some(e => e.fromStepId === step._tempId && e.toStepId === form.steps[i + 1]._tempId)
                                      ? '#000' : 'var(--text-muted)',
                                    border: '1px dashed var(--border-dim)',
                                    borderRadius: 4,
                                  }}
                                  title={form.edges.some(e => e.fromStepId === step._tempId && e.toStepId === form.steps[i + 1]._tempId)
                                    ? '已连接，点击断开' : '未连接，点击建立连线'}
                                >
                                  {form.edges.some(e => e.fromStepId === step._tempId && e.toStepId === form.steps[i + 1]._tempId)
                                    ? '──→' : '- - →'}
                                </button>
                              </div>
                            )}
                            {i === form.steps.length - 1 && <div style={{ flex: 1 }} />}
                          </div>

                          {/* Connector line */}
                          {i < form.steps.length - 1 && (
                            <div style={{
                              marginLeft: 12, paddingLeft: 18,
                              borderLeft: form.edges.some(e => e.fromStepId === step._tempId && e.toStepId === form.steps[i + 1]._tempId)
                                ? '2px solid var(--accent-cyan)' : '2px dotted var(--border-dim)',
                              height: 18,
                            }} />
                          )}
                        </div>
                      ))}
                    </div>

                    {/* Edge summary */}
                    <div style={{ marginTop: 10, fontSize: 11, color: 'var(--text-muted)', borderTop: '1px solid var(--border-dim)', paddingTop: 8 }}>
                      {form.edges.length === 0 ? (
                        <span style={{ color: 'var(--warning, orange)' }}>⚠ 尚未建立任何连线，点击步骤间的 → 按钮建立，或点击"串联所有步骤"自动连接</span>
                      ) : (
                        <span>已建立 {form.edges.length} 条连线：
                          {form.edges.map((e, i) => {
                            const fromStep = form.steps.find(s => s._tempId === e.fromStepId);
                            const toStep = form.steps.find(s => s._tempId === e.toStepId);
                            if (!fromStep || !toStep) return null;
                            return <span key={i} style={{ marginLeft: 8, display: 'inline-flex', alignItems: 'center', gap: 2 }}>
                              <span className="text-cyan">{fromStep.stepName}</span>→<span style={{ color: 'var(--accent-purple)' }}>{toStep.stepName}</span>
                              <button className="btn btn-xs btn-danger" style={{ fontSize: 9, padding: '0px 4px', lineHeight: '16px' }}
                                onClick={() => removeEdge(e.fromStepId, e.toStepId)}>×</button>
                            </span>;
                          })}
                        </span>
                      )}
                    </div>
                  </div>
                )}

                {form.steps.length === 0 && (
                  <div className="text-muted" style={{ textAlign: 'center', padding: 20 }}>
                    尚未添加步骤，点击上方"添加步骤"开始编排
                  </div>
                )}

                {/* ── Active Step Config ── */}
                {activeStep && (
                  <div style={{ borderTop: '1px solid var(--border-dim)', paddingTop: 12, marginTop: 8 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                      <h4 style={{ margin: 0, fontSize: 14 }}>配置: {activeStep.stepName}</h4>
                      <button className="btn btn-xs btn-danger" onClick={() => removeStep(activeStepIdx)}>删除此步骤</button>
                    </div>

                    <div className="form-row">
                      <div className="form-group"><label>步骤编码</label><input value={activeStep.stepCode} onChange={e => updateStep(activeStepIdx, 'stepCode', e.target.value)} /></div>
                      <div className="form-group"><label>步骤名称</label><input value={activeStep.stepName} onChange={e => updateStep(activeStepIdx, 'stepName', e.target.value)} /></div>
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>步骤类型 <span className="required">*</span></label>
                        <select value={activeStep.stepType} onChange={e => { updateStep(activeStepIdx, 'stepType', e.target.value); updateStep(activeStepIdx, 'stepSubType', ''); }}>
                          {STEP_TYPES.map(t => <option key={t.value} value={t.value}>{t.icon} {t.label} — {t.desc}</option>)}
                        </select>
                      </div>
                      <div className="form-group"><label>排序</label><input type="number" value={activeStep.orderIndex} onChange={e => updateStep(activeStepIdx, 'orderIndex', parseInt(e.target.value) || 0)} /></div>
                    </div>

                    {/* ── EXTRACT config ── */}
                    {activeStep.stepType === 'EXTRACT' && (
                      <>
                        <div className="form-row">
                          <div className="form-group" style={{ gridColumn: '1/-1' }}>
                            <label>抽取方式 <span className="required">*</span></label>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))', gap: 4 }}>
                              {SOURCE_TYPES.map(st2 => (
                                <button key={st2.value} type="button" onClick={() => updateStep(activeStepIdx, 'sourceType', st2.value)}
                                  className={`btn btn-xs ${activeStep.sourceType === st2.value ? 'btn-primary' : 'btn-secondary'}`}
                                  style={{ textAlign: 'center', padding: '4px 4px', fontSize: 11 }}>
                                  <div>{st2.icon}</div><div>{st2.label}</div>
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>
                        <div className="form-row">
                          <div className="form-group">
                            <label>源数据源</label>
                            <select value={activeStep.sourceDsName || ''} onChange={e => {
                              const dsName = e.target.value;
                              updateStep(activeStepIdx, 'sourceDsName', dsName);
                              if (!dsName) return;
                              // 选中数据源时自动填充 URL 和认证信息
                              const ds = dsList.find(d => d.dsName === dsName);
                              if (!ds) return;
                              const st = activeStep.sourceType;
                              if ((st === 'HTTP' || st === 'SOAP') && ds.jdbcUrl) {
                                updateStepSourceConfig(activeStepIdx, 'httpUrl', ds.jdbcUrl);
                              }
                              if ((st === 'HTTP' || st === 'SOAP') && ds.authType && ds.authType !== 'NONE') {
                                updateStepSourceConfig(activeStepIdx, 'httpAuthType', ds.authType);
                                if (ds.authType === 'BASIC') {
                                  if (ds.username) updateStepSourceConfig(activeStepIdx, 'httpUsername', ds.username);
                                  if (ds.password) updateStepSourceConfig(activeStepIdx, 'httpPassword', ds.password);
                                } else if (ds.authType === 'TOKEN') {
                                  if (ds.authToken) updateStepSourceConfig(activeStepIdx, 'httpToken', ds.authToken);
                                }
                              }
                              if ((st === 'HTTP' || st === 'SOAP') && ds.timeout) {
                                updateStepSourceConfig(activeStepIdx, 'httpTimeout', String(ds.timeout));
                              }
                              if (st === 'FILE' && ds.jdbcUrl) {
                                updateStepSourceConfig(activeStepIdx, 'filePath', ds.jdbcUrl);
                              }
                            }}>
                              <option value="">请选择</option>
                              {dsList.map(d => <option key={d.dsName} value={d.dsName}>{d.dsName} ({d.protocol || 'JDBC'})</option>)}
                            </select>
                          </div>
                        </div>
                        {isDb && (
                          <>
                            {st === 'SQL' && (
                              <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}><label>SQL 语句</label><textarea rows={3} value={getStepSourceConfig(activeStep, 'sourceSql')} onChange={e => updateStepSourceConfig(activeStepIdx, 'sourceSql', e.target.value)} style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} /></div></div>
                            )}
                            {st === 'TABLE' && (
                              <div className="form-row"><div className="form-group"><label>表名</label><input value={getStepSourceConfig(activeStep, 'sourceTable')} onChange={e => updateStepSourceConfig(activeStepIdx, 'sourceTable', e.target.value)} /></div></div>
                            )}
                            {st === 'VIEW' && (
                              <div className="form-row"><div className="form-group"><label>视图名</label><input value={getStepSourceConfig(activeStep, 'sourceView')} onChange={e => updateStepSourceConfig(activeStepIdx, 'sourceView', e.target.value)} /></div></div>
                            )}
                            {st === 'PROCEDURE' && (
                              <div className="form-row">
                                <div className="form-group"><label>存储过程名</label><input value={getStepSourceConfig(activeStep, 'sourceProcedure')} onChange={e => updateStepSourceConfig(activeStepIdx, 'sourceProcedure', e.target.value)} /></div>
                                <div className="form-group"><label>参数</label><input value={getStepSourceConfig(activeStep, 'sourceParams')} onChange={e => updateStepSourceConfig(activeStepIdx, 'sourceParams', e.target.value)} /></div>
                              </div>
                            )}
                          </>
                        )}
                        {isHttp && (
                          <>
                            <div className="form-row">
                              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                                <label>请求 URL <span className="required">*</span></label>
                                <input value={getStepSourceConfig(activeStep, 'httpUrl')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpUrl', e.target.value)}
                                  placeholder="https://api.example.com/v1/data" />
                              </div>
                            </div>
                            <div className="form-row">
                              <div className="form-group">
                                <label>HTTP 方法</label>
                                <select value={getStepSourceConfig(activeStep, 'httpMethod', 'GET')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpMethod', e.target.value)}>
                                  <option value="GET">GET</option><option value="POST">POST</option><option value="PUT">PUT</option>
                                </select>
                              </div>
                              <div className="form-group">
                                <label>响应格式</label>
                                <select value={getStepSourceConfig(activeStep, 'httpResponseType', 'JSON')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpResponseType', e.target.value)}>
                                  <option value="JSON">JSON</option><option value="XML">XML</option>
                                </select>
                              </div>
                            </div>
                            <div className="form-row">
                              <div className="form-group" style={{ gridColumn: '1/-1' }}>
                                <label>数据路径 (JSONPath / XPath)</label>
                                <input value={getStepSourceConfig(activeStep, 'httpDataPath')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpDataPath', e.target.value)} placeholder="$.data.list 或 Body.GetDataResponse.return" />
                              </div>
                            </div>
                            {/* 请求体 (POST/PUT) */}
                            {getStepSourceConfig(activeStep, 'httpMethod', 'GET') !== 'GET' && (
                              <div className="form-row">
                                <div className="form-group" style={{ gridColumn: '1/-1' }}>
                                  <label>请求体 (JSON/XML)</label>
                                  <textarea rows={3} value={getStepSourceConfig(activeStep, 'httpBody')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpBody', e.target.value)}
                                    placeholder='{"key":"value"}' style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} />
                                </div>
                              </div>
                            )}
                            {/* 认证配置 */}
                            <div className="form-row">
                              <div className="form-group">
                                <label>认证方式</label>
                                <select value={getStepSourceConfig(activeStep, 'httpAuthType', 'NONE')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpAuthType', e.target.value)}>
                                  <option value="NONE">无认证</option><option value="BASIC">Basic Auth</option><option value="TOKEN">Bearer Token</option>
                                </select>
                              </div>
                              <div className="form-group">
                                <label>超时(ms)</label>
                                <input type="number" value={getStepSourceConfig(activeStep, 'httpTimeout', '30000')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpTimeout', e.target.value)} />
                              </div>
                            </div>
                            {getStepSourceConfig(activeStep, 'httpAuthType') === 'BASIC' && (
                              <div className="form-row">
                                <div className="form-group"><label>用户名</label><input value={getStepSourceConfig(activeStep, 'httpUsername')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpUsername', e.target.value)} /></div>
                                <div className="form-group"><label>密码</label><input type="password" value={getStepSourceConfig(activeStep, 'httpPassword')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpPassword', e.target.value)} /></div>
                              </div>
                            )}
                            {getStepSourceConfig(activeStep, 'httpAuthType') === 'TOKEN' && (
                              <div className="form-row">
                                <div className="form-group" style={{ gridColumn: '1/-1' }}>
                                  <label>Token</label>
                                  <input value={getStepSourceConfig(activeStep, 'httpToken')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpToken', e.target.value)} placeholder="Bearer Token" />
                                </div>
                              </div>
                            )}
                            {/* 分页配置 */}
                            <details style={{ marginTop: 8 }}>
                              <summary style={{ cursor: 'pointer', color: 'var(--accent-cyan)', fontSize: 13 }}>▶ 高级配置（请求头 / 分页）</summary>
                              <div style={{ marginTop: 8 }}>
                                <div className="form-row">
                                  <div className="form-group" style={{ gridColumn: '1/-1' }}>
                                    <label>请求头 (JSON)</label>
                                    <textarea rows={2} value={getStepSourceConfig(activeStep, 'httpHeaders')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpHeaders', e.target.value)}
                                      placeholder='{"Authorization":"Bearer xxx","Content-Type":"application/json"}' style={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} />
                                  </div>
                                </div>
                                <div className="form-row">
                                  <div className="form-group">
                                    <label>分页</label>
                                    <select value={getStepSourceConfig(activeStep, 'httpPagination', 'N')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpPagination', e.target.value)}>
                                      <option value="N">不分页</option><option value="Y">分页</option>
                                    </select>
                                  </div>
                                  {getStepSourceConfig(activeStep, 'httpPagination') === 'Y' && (
                                    <>
                                      <div className="form-group"><label>页码参数</label><input value={getStepSourceConfig(activeStep, 'httpPageParam', 'page')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpPageParam', e.target.value)} /></div>
                                      <div className="form-group"><label>每页大小</label><input type="number" value={getStepSourceConfig(activeStep, 'httpPageSize', '1000')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpPageSize', e.target.value)} /></div>
                                    </>
                                  )}
                                </div>
                              </div>
                            </details>
                          </>
                        )}
                        {isSoap && (
                          <>
                            <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}><label>Endpoint URL <span className="required">*</span></label><input value={getStepSourceConfig(activeStep, 'httpUrl')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpUrl', e.target.value)} /></div></div>
                            <div className="form-row">
                              <div className="form-group"><label>SOAP 版本</label><select value={getStepSourceConfig(activeStep, 'soapBinding', 'SOAP11')} onChange={e => updateStepSourceConfig(activeStepIdx, 'soapBinding', e.target.value)}><option value="SOAP11">SOAP 1.1</option><option value="SOAP12">SOAP 1.2</option></select></div>
                              <div className="form-group"><label>SOAP Action</label><input value={getStepSourceConfig(activeStep, 'soapAction')} onChange={e => updateStepSourceConfig(activeStepIdx, 'soapAction', e.target.value)} /></div>
                            </div>
                            <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}><label>SOAP Envelope (XML 请求体) <span className="required">*</span></label><textarea rows={6} value={getStepSourceConfig(activeStep, 'httpBody')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpBody', e.target.value)} style={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} /></div></div>
                            <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}><label>数据路径 (XPath 风格)</label><input value={getStepSourceConfig(activeStep, 'httpDataPath')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpDataPath', e.target.value)} placeholder="Body.GetDataResponse.return" /></div></div>
                            <div className="form-row">
                              <div className="form-group"><label>命名空间</label><input value={getStepSourceConfig(activeStep, 'soapNamespace')} onChange={e => updateStepSourceConfig(activeStepIdx, 'soapNamespace', e.target.value)} /></div>
                              <div className="form-group"><label>超时(ms)</label><input type="number" value={getStepSourceConfig(activeStep, 'httpTimeout', '60000')} onChange={e => updateStepSourceConfig(activeStepIdx, 'httpTimeout', e.target.value)} /></div>
                            </div>
                          </>
                        )}
                        {isFile && (
                          <>
                            <div className="form-row">
                              <div className="form-group"><label>文件路径</label><input value={getStepSourceConfig(activeStep, 'filePath')} onChange={e => updateStepSourceConfig(activeStepIdx, 'filePath', e.target.value)} /></div>
                              <div className="form-group">
                                <label>文件格式</label>
                                <select value={getStepSourceConfig(activeStep, 'fileFormat', 'CSV')} onChange={e => updateStepSourceConfig(activeStepIdx, 'fileFormat', e.target.value)}>
                                  <option value="CSV">CSV</option><option value="JSON">JSON</option><option value="EXCEL">Excel</option>
                                </select>
                              </div>
                            </div>
                            <div className="form-row">
                              <div className="form-group"><label>分隔符</label><input value={getStepSourceConfig(activeStep, 'fileDelimiter', ',')} onChange={e => updateStepSourceConfig(activeStepIdx, 'fileDelimiter', e.target.value)} /></div>
                              <div className="form-group"><label>编码</label><input value={getStepSourceConfig(activeStep, 'fileEncoding', 'UTF-8')} onChange={e => updateStepSourceConfig(activeStepIdx, 'fileEncoding', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                              <div className="form-group">
                                <label>首行是表头</label>
                                <select value={getStepSourceConfig(activeStep, 'fileHeader', 'Y')} onChange={e => updateStepSourceConfig(activeStepIdx, 'fileHeader', e.target.value)}>
                                  <option value="Y">是</option><option value="N">否</option>
                                </select>
                              </div>
                            </div>
                          </>
                        )}

                        {/* Extract Test — 支持所有抽取类型 */}
                        <div style={{ marginTop: 12, padding: 10, background: 'var(--bg-card-alt, rgba(0,255,255,0.03))', borderRadius: 8, border: '1px solid var(--border-dim)' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                            <span style={{ fontWeight: 600, fontSize: 13, color: 'var(--accent-cyan)' }}>⇣ 抽取测试</span>
                            <button className="btn btn-xs btn-primary" onClick={handleTestExtract} disabled={testing}>{testing ? '测试中...' : '▶ 测试抽取'}</button>
                          </div>
                          {testResult && (
                            <div>
                              <span className={`tag ${testResult.success ? 'tag-green' : 'tag-red'}`} style={{ fontSize: 11 }}>{testResult.success ? `成功: ${testResult.totalRows || 0} 条` : `失败: ${testResult.errorMessage}`}</span>
                              {testResult.success && testResult.parsedData && testResult.parsedData.length > 0 && (
                                <div style={{ maxHeight: 160, overflow: 'auto', marginTop: 6 }}>
                                  <table className="data-table" style={{ fontSize: 11 }}>
                                    <thead><tr>{Object.keys(testResult.parsedData[0]).map(k => <th key={k}>{k}</th>)}</tr></thead>
                                    <tbody>{testResult.parsedData.slice(0, 5).map((row, i) => <tr key={i}>{Object.values(row).map((v, j) => <td key={j}>{v === null ? 'NULL' : String(v)}</td>)}</tr>)}</tbody>
                                  </table>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </>
                    )}

                    {/* ── TRANSFORM config ── */}
                    {activeStep.stepType === 'TRANSFORM' && (
                      <>
                        <div className="form-row">
                          <div className="form-group" style={{ gridColumn: '1/-1' }}>
                            <label>转换子类型</label>
                            <div style={{ display: 'flex', gap: 6 }}>
                              {TRANSFORM_SUB_TYPES.map(st2 => (
                                <button key={st2.value} type="button" onClick={() => updateStep(activeStepIdx, 'stepSubType', st2.value)}
                                  className={`btn btn-xs ${activeStep.stepSubType === st2.value ? 'btn-primary' : 'btn-secondary'}`}>
                                  {st2.label}
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>
                        {activeStep.stepSubType === 'FIELD_MAP' && (
                          <div className="text-muted" style={{ fontSize: 12, padding: 8, background: 'var(--bg-card-alt)', borderRadius: 6 }}>
                            保存管线后，在 <strong>字段映射</strong> 页面选择此步骤进行映射配置。
                          </div>
                        )}
                        {activeStep.stepSubType === 'JOIN' && (
                          <div className="form-row"><div className="form-group" style={{ gridColumn: '1/-1' }}><label>JOIN 条件 (JSON)</label><textarea rows={2} value={getStepSourceConfig(activeStep, 'joinConfig', '{"type":"LEFT","on":"id"}')} onChange={e => updateStepSourceConfig(activeStepIdx, 'joinConfig', e.target.value)} style={{ fontFamily: 'var(--font-mono)', fontSize: 11 }} /></div></div>
                        )}
                      </>
                    )}

                    {/* ── LOAD config ── */}
                    {activeStep.stepType === 'LOAD' && (
                      <>
                        <div className="form-row">
                          <div className="form-group" style={{ gridColumn: '1/-1' }}>
                            <label>加载子类型</label>
                            <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                              {LOAD_SUB_TYPES.map(st2 => (
                                <button key={st2.value} type="button" onClick={() => updateStep(activeStepIdx, 'stepSubType', st2.value)}
                                  className={`btn btn-xs ${activeStep.stepSubType === st2.value ? 'btn-primary' : 'btn-secondary'}`}>
                                  {st2.label}
                                </button>
                              ))}
                            </div>
                          </div>
                        </div>
                        {(activeStep.stepSubType === 'DB_INSERT') && (
                          <>
                            <div className="form-row">
                              <div className="form-group"><label>目标数据源</label><select value={activeStep.targetDsName || ''} onChange={e => updateStep(activeStepIdx, 'targetDsName', e.target.value)}><option value="">请选择</option>{dsList.map(d => <option key={d.dsName} value={d.dsName}>{d.dsName}</option>)}</select></div>
                              <div className="form-group"><label>目标表 <span className="required">*</span></label><input value={getStepTargetConfig(activeStep, 'targetTable')} onChange={e => updateStepTargetConfig(activeStepIdx, 'targetTable', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                              <div className="form-group"><label>写入模式</label><select value={activeStep.writeMode || 'INSERT'} onChange={e => updateStep(activeStepIdx, 'writeMode', e.target.value)}><option value="INSERT">INSERT</option><option value="MERGE">MERGE</option></select></div>
                              <div className="form-group"><label>批量大小</label><input type="number" value={activeStep.batchSize} onChange={e => updateStep(activeStepIdx, 'batchSize', parseInt(e.target.value) || 2000)} /></div>
                            </div>
                          </>
                        )}
                        {(activeStep.stepSubType || '').startsWith('FILE_') && (
                          <>
                            <div className="form-row"><div className="form-group"><label>文件路径</label><input value={getStepTargetConfig(activeStep, 'filePath')} onChange={e => updateStepTargetConfig(activeStepIdx, 'filePath', e.target.value)} /></div></div>
                            <div className="form-row">
                              <div className="form-group"><label>文件格式</label><select value={getStepTargetConfig(activeStep, 'fileFormat', 'CSV')} onChange={e => updateStepTargetConfig(activeStepIdx, 'fileFormat', e.target.value)}><option value="CSV">CSV</option><option value="JSON">JSON</option></select></div>
                              <div className="form-group"><label>编码</label><input value={getStepTargetConfig(activeStep, 'fileEncoding', 'UTF-8')} onChange={e => updateStepTargetConfig(activeStepIdx, 'fileEncoding', e.target.value)} /></div>
                            </div>
                          </>
                        )}
                      </>
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

      {/* Debug Panel */}
      {debugPipeline && (
        <DebugPanel
          taskCode={debugPipeline.pipelineCode}
          taskName={debugPipeline.pipelineName}
          isPipeline={true}
          onClose={() => setDebugPipeline(null)}
        />
      )}
    </div>
  );
}
