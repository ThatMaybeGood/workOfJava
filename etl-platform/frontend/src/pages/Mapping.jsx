import { useState, useEffect, useCallback } from 'react';
import { PipelineAPI, DataSourceAPI, ExtractAPI, StepAPI, getStepSourceConfig, buildExtractTestRequest } from '../api/etl';
import { useToast } from '../components/useToast';

const EMPTY_MAP = {
  stepId: '', sourceColumn: '', targetColumn: '', dataType: '',
  defaultValue: '', transformExpr: '', mappingOrder: 0,
  isPrimaryKey: 'N', enabled: 'Y', description: '',
};

const DATA_TYPES = ['STRING','INTEGER','LONG','DOUBLE','DECIMAL','DATE','DATETIME','BOOLEAN','CLOB','BLOB'];

// 可配置字段映射的步骤：TRANSFORM/字段映射 + LOAD/写库
// 引擎对 TRANSFORM 空子类型默认按 FIELD_MAP，对 LOAD 空子类型默认按 DB_INSERT 执行
const STEP_SUB_LABEL = { FIELD_MAP: '字段映射', DB_INSERT: '写入数据库', DB_MERGE: '合并写入' };
const isMappableStep = (s) =>
  (s.stepType === 'TRANSFORM' && (!s.stepSubType || s.stepSubType === 'FIELD_MAP')) ||
  (s.stepType === 'LOAD' && (!s.stepSubType || s.stepSubType === 'DB_INSERT' || s.stepSubType === 'DB_MERGE'));
const stepSubLabel = (s) => STEP_SUB_LABEL[s.stepSubType] || (s.stepType === 'TRANSFORM' ? '字段映射' : s.stepType);

// 从步骤 sourceConfig/targetConfig 取字段
const getStepTargetConfig = (step, key, defaultVal = '') => {
  try { const c = JSON.parse(step.targetConfig || '{}'); return c[key] !== undefined ? c[key] : defaultVal; }
  catch { return defaultVal; }
};

export default function Mapping() {
  const [pipelines, setPipelines] = useState([]);
  const [selectedPipelineId, setSelectedPipelineId] = useState('');
  const [pipelineSteps, setPipelineSteps] = useState([]);
  const [pipelineEdges, setPipelineEdges] = useState([]);
  const [mappableSteps, setMappableSteps] = useState([]);
  const [selectedStepId, setSelectedStepId] = useState('');
  const [stepLoading, setStepLoading] = useState(false);

  const [mappings, setMappings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ ...EMPTY_MAP });
  const [saving, setSaving] = useState(false);

  // ── 可视化匹配上下文 ──
  const [dsList, setDsList] = useState([]);
  const [sourceStep, setSourceStep] = useState(null);   // 上游 EXTRACT 步骤
  const [srcColumns, setSrcColumns] = useState([]);     // 抽取出参列
  const [srcRows, setSrcRows] = useState([]);           // 出参预览数据
  const [srcLoading, setSrcLoading] = useState(false);
  const [dstDsName, setDstDsName] = useState('');       // 目的库
  const [dstTable, setDstTable] = useState('');         // 目的表
  const [dstTables, setDstTables] = useState([]);       // 目的库的表列表
  const [dstColumns, setDstColumns] = useState([]);     // 目的表字段
  const [dstLoading, setDstLoading] = useState(false);
  const [selSrc, setSelSrc] = useState('');             // 高亮的源列
  const [selDst, setSelDst] = useState('');             // 高亮的目标列

  const { addToast, ToastContainer } = useToast();

  useEffect(() => {
    PipelineAPI.list().then(res => {
      if (res.success) setPipelines(res.data || []);
    }).catch(() => {});
    DataSourceAPI.list().then(res => {
      if (res.success) setDsList(res.data || []);
    }).catch(() => {});
  }, []);

  const loadMappings = useCallback(async (stepId) => {
    if (!stepId) { setMappings([]); return; }
    setLoading(true);
    try {
      const res = await StepAPI.getMappings(stepId);
      if (res.success) setMappings(res.data || []);
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  // ── 上下文解析：上游EXTRACT + 下游LOAD目标 ──
  const resolveContext = useCallback((step, steps, edges) => {
    const stepMap = Object.fromEntries(steps.map(s => [String(s.id), s]));
    const findSourceExtract = (fromId) => {
      // 沿 incoming 边向上找 EXTRACT；无 EXTRACT 则返回最近的上游步骤
      const incoming = edges.filter(e => String(e.toStepId) === String(fromId));
      for (const e of incoming) {
        const up = stepMap[String(e.fromStepId)];
        if (up) {
          if (up.stepType === 'EXTRACT') return up;
          const deeper = findSourceExtract(up.id);
          if (deeper) return deeper;
        }
      }
      return null;
    };
    const findLoadTarget = (fromId) => {
      // 沿 outgoing 边向下找 LOAD，取其目标库/表
      const outgoing = edges.filter(e => String(e.fromStepId) === String(fromId));
      for (const e of outgoing) {
        const down = stepMap[String(e.toStepId)];
        if (down) {
          if (down.stepType === 'LOAD') return down;
          const deeper = findLoadTarget(down.id);
          if (deeper) return deeper;
        }
      }
      return null;
    };

    let src = null;
    let dstDs = '';
    let dstTb = '';
    if (step.stepType === 'LOAD') {
      src = findSourceExtract(step.id);
      dstDs = step.targetDsName || '';
      dstTb = getStepTargetConfig(step, 'targetTable');
    } else {
      // TRANSFORM/FIELD_MAP：源=上游EXTRACT，目标=下游LOAD
      src = findSourceExtract(step.id);
      const loadStep = findLoadTarget(step.id);
      if (loadStep) {
        dstDs = loadStep.targetDsName || '';
        dstTb = getStepTargetConfig(loadStep, 'targetTable');
      }
    }
    return { src, dstDs, dstTb };
  }, []);

  const loadSrcColumns = useCallback(async (srcStep) => {
    if (!srcStep) return;
    setSrcLoading(true);
    try {
      const req = buildExtractTestRequest(srcStep, 5);
      const res = await ExtractAPI.test(req);
      if (res.success && res.data && res.data.success) {
        setSrcColumns((res.data.columns) || []);
        setSrcRows(res.data.parsedData || []);
        if (!res.data.columns || res.data.columns.length === 0) {
          addToast('抽取成功但未解析出列，请检查数据路径配置', 'error');
        }
      } else {
        setSrcColumns([]); setSrcRows([]);
        addToast('加载出参列失败: ' + (res.data?.errorMessage || '未知错误'), 'error');
      }
    } catch (e) {
      setSrcColumns([]); setSrcRows([]);
      addToast('加载出参列失败: ' + e.message, 'error');
    } finally { setSrcLoading(false); }
  }, [addToast]);

  const loadDstTables = useCallback(async (dsName) => {
    if (!dsName) { setDstTables([]); return; }
    try {
      const res = await DataSourceAPI.listTables(dsName);
      if (res.success) setDstTables(res.data || []);
      else setDstTables([]);
    } catch (e) { setDstTables([]); addToast('加载表列表失败: ' + e.message, 'error'); }
  }, [addToast]);

  const loadDstColumns = useCallback(async (dsName, table) => {
    if (!dsName || !table) { setDstColumns([]); return; }
    setDstLoading(true);
    try {
      const res = await DataSourceAPI.listColumns(dsName, table);
      if (res.success) setDstColumns(res.data || []);
      else { setDstColumns([]); addToast('加载表字段失败: ' + res.message, 'error'); }
    } catch (e) { setDstColumns([]); addToast('加载表字段失败: ' + e.message, 'error'); }
    finally { setDstLoading(false); }
  }, [addToast]);

  const changePipeline = async (pipelineId) => {
    setSelectedPipelineId(pipelineId);
    setSelectedStepId('');
    setMappings([]);
    setPipelineSteps([]); setPipelineEdges([]); setMappableSteps([]);
    setSourceStep(null); setSrcColumns([]); setSrcRows([]);
    setDstDsName(''); setDstTable(''); setDstTables([]); setDstColumns([]);
    if (!pipelineId) return;
    setStepLoading(true);
    try {
      const res = await PipelineAPI.get(pipelineId);
      if (res.success) {
        const steps = res.data.steps || [];
        setPipelineSteps(steps);
        setPipelineEdges(res.data.edges || []);
        setMappableSteps(steps.filter(isMappableStep));
      }
    } catch (e) { addToast('加载管线失败: ' + e.message, 'error'); }
    finally { setStepLoading(false); }
  };

  const changeStep = (stepId) => {
    setSelectedStepId(stepId);
    loadMappings(stepId);
    setSelSrc(''); setSelDst('');
    const step = mappableSteps.find(s => String(s.id) === String(stepId));
    if (!step) return;
    const { src, dstDs, dstTb } = resolveContext(step, pipelineSteps, pipelineEdges);
    setSourceStep(src);
    // 目的库：优先步骤配的，否则取第一个 JDBC 数据源；目的表回填后加载字段
    const jdbcDs = dsList.filter(d => (d.protocol || 'JDBC') === 'JDBC');
    const effectiveDs = dstDs || (jdbcDs[0]?.dsName || '');
    setDstDsName(effectiveDs);
    setDstTable(dstTb || '');
    setDstColumns([]); setDstTables([]);
    if (effectiveDs) loadDstTables(effectiveDs);
    if (src) {
      loadSrcColumns(src);
    } else {
      setSrcColumns([]); setSrcRows([]);
      addToast('未找到上游抽取步骤，无法加载源出参列', 'error');
    }
    if (effectiveDs && dstTb) loadDstColumns(effectiveDs, dstTb);
  };

  const changeDstDs = (dsName) => {
    setDstDsName(dsName);
    setDstTable('');
    setDstColumns([]);
    setSelDst('');
    if (dsName) loadDstTables(dsName);
  };

  const changeDstTable = (table) => {
    setDstTable(table);
    setSelDst('');
    if (table) loadDstColumns(dstDsName, table);
  };

  const nextMappingOrder = () =>
    mappings.reduce((max, m) => Math.max(max, m.mappingOrder || 0), 0) + 1;

  // 点选生成：源列+目标列都已选中时调用
  const createMapping = async (srcCol, dstCol) => {
    if (!selectedStepId || !srcCol || !dstCol) return;
    const exists = mappings.some(m => m.sourceColumn === srcCol && m.targetColumn === dstCol);
    if (exists) { addToast(`映射已存在: ${srcCol} → ${dstCol}`, 'error'); setSelSrc(''); setSelDst(''); return; }
    const dstColMeta = dstColumns.find(c => c.columnName === dstCol);
    const data = {
      stepId: selectedStepId,
      sourceColumn: srcCol,
      targetColumn: dstCol,
      dataType: dstColMeta?.dataTypeName || '',
      mappingOrder: nextMappingOrder(),
      isPrimaryKey: 'N',
      enabled: 'Y',
    };
    try {
      const res = await StepAPI.saveMapping(selectedStepId, data);
      if (res.success) {
        addToast(`已生成映射: ${srcCol} → ${dstCol}`, 'success');
        await loadMappings(selectedStepId);
        setSelSrc(''); setSelDst('');
      } else {
        addToast('生成失败: ' + res.message, 'error');
      }
    } catch (e) { addToast('生成失败: ' + e.message, 'error'); }
  };

  const onPickSrc = (col) => {
    const next = selSrc === col ? '' : col;
    setSelSrc(next);
    if (next && selDst) createMapping(next, selDst);
  };
  const onPickDst = (col) => {
    const next = selDst === col ? '' : col;
    setSelDst(next);
    if (next && selSrc) createMapping(selSrc, next);
  };

  // 同名自动匹配：未配置的列对批量生成
  const autoMatch = async () => {
    const existing = new Set(mappings.map(m => m.sourceColumn));
    const candidates = srcColumns
      .filter(col => dstColumns.some(d => d.columnName === col) && !existing.has(col))
      .map(col => ({ src: col, dst: col }));
    if (candidates.length === 0) {
      addToast('没有可自动匹配的同名字段', 'error');
      return;
    }
    let ok = 0;
    for (const c of candidates) {
      const dstMeta = dstColumns.find(d => d.columnName === c.dst);
      try {
        const res = await StepAPI.saveMapping(selectedStepId, {
          stepId: selectedStepId, sourceColumn: c.src, targetColumn: c.dst,
          dataType: dstMeta?.dataTypeName || '',
          mappingOrder: nextMappingOrder() + ok,
          isPrimaryKey: 'N', enabled: 'Y',
        });
        if (res.success) ok++;
      } catch {}
    }
    addToast(`自动匹配完成，新增 ${ok} 条映射`, ok > 0 ? 'success' : 'error');
    await loadMappings(selectedStepId);
  };

  const openModal = (map = null) => {
    if (map) {
      setEditing(map.id);
      setForm({ ...EMPTY_MAP, ...map });
    } else {
      setEditing(null);
      setForm({ ...EMPTY_MAP, stepId: selectedStepId });
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
      const res = await StepAPI.saveMapping(selectedStepId, data);
      if (res.success) {
        addToast(editing ? '更新成功' : '创建成功', 'success');
        closeModal();
        await loadMappings(selectedStepId);
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('确认删除此字段映射？')) return;
    try {
      await StepAPI.deleteMapping(selectedStepId, id);
      addToast('已删除', 'success');
      loadMappings(selectedStepId);
    } catch (e) { addToast('删除失败: ' + e.message, 'error'); }
  };

  // 列选择器：下拉候选 + "手动输入"自由选项
  const ColumnSelect = ({ label, value, options, onChange, placeholder }) => {
    const usingManual = value && !options.includes(value);
    return (
      <div className="form-group">
        <label>{label} <span className="required">*</span></label>
        <select value={usingManual ? '__manual__' : value} onChange={e => {
          if (e.target.value === '__manual__') return;
          onChange(e.target.value);
        }}>
          <option value="">-- 请选择 --</option>
          {options.map(o => <option key={o} value={o}>{o}</option>)}
          <option value="__manual__">✎ 手动输入...</option>
        </select>
        {usingManual && (
          <input style={{ marginTop: 6 }} value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder || '手动输入列名'} />
        )}
      </div>
    );
  };

  const jdbcDsList = dsList.filter(d => (d.protocol || 'JDBC') === 'JDBC');

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">▣</span> 字段映射</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={() => openModal()} disabled={!selectedStepId}>＋ 新增映射</button>
        </div>
      </div>

      <div className="content-area">
        <div className="filter-bar">
          <select value={selectedPipelineId} onChange={e => changePipeline(e.target.value)} style={{ minWidth: 260 }}>
            <option value="">选择管线...</option>
            {pipelines.map(p => <option key={p.id} value={p.id}>{p.pipelineCode} — {p.pipelineName}</option>)}
          </select>
          <select value={selectedStepId} onChange={e => changeStep(e.target.value)} style={{ minWidth: 300 }} disabled={!selectedPipelineId}>
            <option value="">选择步骤...</option>
            {stepLoading ? <option value="">加载步骤中...</option> : mappableSteps.map(s => (
              <option key={s.id} value={s.id}>{s.stepName} — {stepSubLabel(s)}</option>
            ))}
          </select>
        </div>

        {/* ── 可视化匹配区 ── */}
        {selectedStepId && (
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-body">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--accent-cyan)' }}>⛁ 可视化字段匹配</span>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                  <span className="text-muted text-sm">已配置 {mappings.length} 条</span>
                  <button className="btn btn-xs btn-primary" onClick={autoMatch} disabled={!srcColumns.length || !dstColumns.length}>
                    ↕ 同名自动匹配
                  </button>
                </div>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                {/* 左：源字段（抽取出参） */}
                <div style={{ border: '1px solid var(--border-dim)', borderRadius: 10, padding: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--accent-purple)', display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ fontSize: 14 }}>⇣</span> 源字段 · {sourceStep ? `${sourceStep.stepName}(抽取出参)` : '未找到抽取步骤'}
                    </span>
                    <button className="btn btn-xs btn-secondary" onClick={() => loadSrcColumns(sourceStep)} disabled={!sourceStep || srcLoading}>
                      {srcLoading ? '加载中...' : '⟳ 加载出参列'}
                    </button>
                  </div>
                  {srcColumns.length === 0 ? (
                    <div className="text-muted text-sm" style={{ padding: 12, textAlign: 'center' }}>
                      {srcLoading ? '正在调用抽取接口...' : '点击"加载出参列"获取上游抽取的真实列'}
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                      {srcColumns.map(col => (
                        <button key={col} onClick={() => onPickSrc(col)}
                          className="btn btn-xs"
                          title="点击选择此源字段"
                          style={{
                            fontFamily: 'var(--font-mono)', fontSize: 12,
                            background: selSrc === col ? 'var(--accent-cyan)' : 'var(--bg-card-hover)',
                            color: selSrc === col ? '#000' : 'var(--text-primary)',
                            border: selSrc === col ? '1.5px solid var(--accent-cyan)' : '1px solid var(--border-dim)',
                            boxShadow: selSrc === col ? '0 0 8px rgba(34,211,238,0.35)' : undefined,
                            transform: selSrc === col ? 'scale(1.06)' : undefined,
                          }}>
                          {selSrc === col ? '✓ ' : ''}{col}
                        </button>
                      ))}
                    </div>
                  )}
                  {srcRows.length > 0 && (
                    <div style={{ marginTop: 10, maxHeight: 120, overflow: 'auto' }}>
                      <table className="data-table" style={{ fontSize: 11 }}>
                        <thead><tr>{srcColumns.map(c => <th key={c}>{c}</th>)}</tr></thead>
                        <tbody>{srcRows.slice(0, 3).map((row, i) => (
                          <tr key={i}>{srcColumns.map(c => <td key={c}>{row[c] === null ? 'NULL' : String(row[c])}</td>)}</tr>
                        ))}</tbody>
                      </table>
                    </div>
                  )}
                </div>

                {/* 右：目标字段（目的库表） */}
                <div style={{ border: '1px solid var(--border-dim)', borderRadius: 10, padding: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--accent-orange)', display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ fontSize: 14 }}>⇡</span> 目标字段 · {dstDsName}.{dstTable || '?'}
                    </span>
                    <button className="btn btn-xs btn-secondary"
                      onClick={() => dstTable && loadDstColumns(dstDsName, dstTable)}
                      disabled={!dstTable || dstLoading}>
                      {dstLoading ? '加载中...' : '⟳ 加载表字段'}
                    </button>
                  </div>
                  <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                    <select value={dstDsName} onChange={e => changeDstDs(e.target.value)} style={{ flex: 1, minWidth: 0, padding: '6px 10px', background: 'var(--bg-input)', border: '1px solid var(--border-dim)', borderRadius: 8, fontSize: 12 }}>
                      <option value="">选择目的库...</option>
                      {jdbcDsList.map(d => <option key={d.dsName} value={d.dsName}>{d.dsName}</option>)}
                    </select>
                    <select value={dstTable} onChange={e => changeDstTable(e.target.value)} style={{ flex: 1, minWidth: 0, padding: '6px 10px', background: 'var(--bg-input)', border: '1px solid var(--border-dim)', borderRadius: 8, fontSize: 12 }}>
                      <option value="">选择目的表...</option>
                      {dstTables.map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                  </div>
                  {dstColumns.length === 0 ? (
                    <div className="text-muted text-sm" style={{ padding: 12, textAlign: 'center' }}>
                      {dstLoading ? '正在读取表结构...' : '选择目的库和目的表后加载字段'}
                    </div>
                  ) : (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                      {dstColumns.map(c => (
                        <button key={c.columnName} onClick={() => onPickDst(c.columnName)}
                          className="btn btn-xs"
                          title="点击选择此目标字段"
                          style={{
                            fontFamily: 'var(--font-mono)', fontSize: 12,
                            background: selDst === c.columnName ? 'var(--accent-orange, #f0a040)' : 'var(--bg-card-hover)',
                            color: selDst === c.columnName ? '#000' : 'var(--text-primary)',
                            border: selDst === c.columnName ? '1.5px solid var(--accent-orange)' : '1px solid var(--border-dim)',
                            boxShadow: selDst === c.columnName ? '0 0 8px rgba(245,158,11,0.35)' : undefined,
                            transform: selDst === c.columnName ? 'scale(1.06)' : undefined,
                          }}>
                          {selDst === c.columnName ? '✓ ' : ''}{c.columnName}
                          <span className="text-muted" style={{ fontSize: 10, marginLeft: 4 }}>{c.dataTypeName || ''}</span>
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
              <div className="text-muted" style={{ marginTop: 10, fontSize: 11 }}>
                提示：在左侧点选源列 → 再点选右侧目标列，即自动生成一条映射；或使用"同名自动匹配"批量生成。
              </div>
            </div>
          </div>
        )}

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
                  {!selectedPipelineId ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      请先选择一个管线
                    </td></tr>
                  ) : !selectedStepId ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      {mappableSteps.length === 0 && !stepLoading
                        ? '此管线没有可配置映射的步骤 — 请在管线编辑器中添加『字段映射』转换步骤或『写入数据库』加载步骤'
                        : '请选择一个步骤查看其字段映射'}
                    </td></tr>
                  ) : loading ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : mappings.length === 0 ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      暂无字段映射数据 — 在上方可视化区点选字段生成，或点击"新增映射"
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
                <ColumnSelect label="源字段" value={form.sourceColumn} options={srcColumns} onChange={v => update('sourceColumn', v)} placeholder="源列名" />
                <ColumnSelect label="目标字段" value={form.targetColumn} options={dstColumns.map(c => c.columnName)} onChange={v => update('targetColumn', v)} placeholder="目标列名" />
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
