import { useState, useRef, useCallback } from 'react';
import { TaskAPI, PipelineAPI } from '../api/etl';
import { useToast } from './useToast';
import SchemaTree from './SchemaTree';

const STEP_LABELS = { EXTRACT: '抽取', TRANSFORM: '转换', LOAD: '加载' };
const STEP_ICONS = { EXTRACT: '⇣', TRANSFORM: '⇄', LOAD: '⇧' };
const STEP_COLORS = { EXTRACT: 'var(--accent-cyan)', TRANSFORM: 'var(--accent-purple)', LOAD: 'var(--accent-orange, #f0a040)' };

function StepCard({ step, index, last, autoExpand }) {
  const [open, setOpen] = useState(autoExpand || false);
  const [viewMode, setViewMode] = useState('table'); // 'table' | 'json' | 'schema'

  // Status visualization
  let statusTag, statusClass;
  if (step.status === 'SUCCESS') { statusTag = '✓ 成功'; statusClass = 'tag-green'; }
  else if (step.status === 'FAILED') { statusTag = '✕ 失败'; statusClass = 'tag-red'; }
  else if (step.status === 'RUNNING') { statusTag = '◌ 运行中'; statusClass = 'tag-blue'; }
  else { statusTag = '○ 等待'; statusClass = 'tag-dim'; }

  const hasData = step.outputData && step.outputData.length > 0;
  const columns = step.outputColumns || (hasData ? Object.keys(step.outputData[0] || {}) : []);

  return (
    <div className="debug-step" style={{
      borderLeft: `3px solid ${step.status === 'RUNNING' ? STEP_COLORS[step.stepType] || 'var(--accent-cyan)' : 'transparent'}`,
      opacity: step.status === 'PENDING' ? 0.5 : 1,
      transition: 'opacity 0.3s',
    }}>
      <div className="debug-step-head" onClick={() => setOpen(o => !o)}>
        <div className="debug-step-index" style={{
          background: step.status === 'RUNNING' ? (STEP_COLORS[step.stepType] || 'var(--accent-cyan)') : undefined,
          color: step.status === 'RUNNING' ? '#000' : undefined,
        }}>{index + 1}</div>
        <div className="debug-step-icon">{STEP_ICONS[step.stepType]}</div>
        <div className="debug-step-info">
          <div className="debug-step-name">{step.stepName}</div>
          <div className="debug-step-meta">
            <span className="tag tag-dim">{STEP_LABELS[step.stepType] || step.stepType}</span>
            {step.inputRows > 0 && <span>入 {step.inputRows} 行</span>}
            {step.outputRows > 0 && <span>出 {step.outputRows} 行</span>}
            {step.durationMs > 0 && <span>{step.durationMs} ms</span>}
            {step.status === 'RUNNING' && <span className="loader" style={{ width: 12, height: 12, borderWidth: 2 }} />}
          </div>
        </div>
        <span className={`tag ${statusClass}`}>{statusTag}</span>
        <span className="debug-step-arrow">{open ? '▾' : '▸'}</span>
      </div>

      {/* 流转信息 */}
      <div className="debug-step-route">
        {step.status === 'SUCCESS' && step.stepType !== 'LOAD' && <span className="route-next">成功 → 下一步</span>}
        {step.status === 'RUNNING' && <span className="route-next" style={{ color: 'var(--accent-cyan)' }}>⚡ 正在执行...</span>}
        {step.stepType === 'LOAD' && step.status === 'SUCCESS' && <span className="route-end">成功 → 结束</span>}
        {step.status === 'FAILED' && <span className="route-stop">失败 → 终止流程</span>}
        {index < last && <span className="route-connector">↓</span>}
      </div>

      {step.errorMessage && (
        <div className="debug-step-error">{step.errorMessage}</div>
      )}

      {/* 展开数据 */}
      {open && hasData && (
        <div className="debug-step-data">
          <div className="debug-data-toolbar">
            <div className="debug-data-title">出参数据（{step.outputData.length} 条，{columns.length} 列）</div>
            <div className="btn-group">
              <button className={`btn btn-xs ${viewMode === 'table' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setViewMode('table')}>⊞ 表格</button>
              <button className={`btn btn-xs ${viewMode === 'json' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setViewMode('json')}>{} JSON</button>
              {step.rawResponse && (
                <button className={`btn btn-xs ${viewMode === 'schema' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setViewMode('schema')}>⊟ 结构</button>
              )}
            </div>
          </div>
          {viewMode === 'table' ? (
            <div className="table-wrap" style={{ borderRadius: 10, border: '1px solid var(--border-dim)', maxHeight: 400, overflow: 'auto' }}>
              <table className="data-table" style={{ fontSize: 12 }}>
                <thead><tr><th style={{ width: 40 }}>#</th>{columns.map(c => <th key={c}>{c}</th>)}</tr></thead>
                <tbody>
                  {step.outputData.slice(0, 100).map((row, ri) => (
                    <tr key={ri}><td className="text-muted">{ri + 1}</td>
                      {columns.map(col => <td key={col} className="text-mono text-sm">{formatVal(row[col])}</td>)}
                    </tr>
                  ))}
                </tbody>
              </table>
              {step.outputData.length > 100 && <div style={{ textAlign: 'center', padding: 8, color: 'var(--text-muted)', fontSize: 12 }}>... 仅显示前 100 条，共 {step.outputData.length} 条</div>}
            </div>
          ) : (
            <pre style={{ background: 'var(--bg-card)', border: '1px solid var(--border-dim)', borderRadius: 10, padding: 12, maxHeight: 400, overflow: 'auto', fontFamily: 'var(--font-mono)', fontSize: 12, whiteSpace: 'pre-wrap' }}>
              {JSON.stringify(step.outputData.slice(0, 50), null, 2)}
              {step.outputData.length > 50 && `\n\n... 仅显示前 50 条，共 ${step.outputData.length} 条`}
            </pre>
          )}

          {/* 结构树视图（仅 HTTP 源有 rawResponse 时显示） */}
          {viewMode === 'schema' && step.rawResponse && (
            <div style={{ background: 'var(--bg-card)', border: '1px solid var(--border-dim)', borderRadius: 10, padding: 12, maxHeight: 400, overflow: 'auto', fontFamily: 'var(--font-mono)', fontSize: 12 }}>
              <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 8 }}>
                点击字段路径可复制到剪贴板
              </div>
              <SchemaTree
                nodes={parseSchema(step.rawResponse)}
                onSelect={path => {
                  navigator.clipboard.writeText(path);
                  addToast(`已复制路径: ${path}`, 'success');
                }}
              />
            </div>
          )}
        </div>
      )}

      {/* HTTP 原始响应 */}
      {open && step.stepType === 'EXTRACT' && step.rawResponse && (
        <div className="debug-step-data" style={{ marginTop: 12, borderTop: '1px dashed var(--border-dim)', paddingTop: 12 }}>
          <div className="debug-data-toolbar">
            <div className="debug-data-title" style={{ color: 'var(--accent-cyan)' }}>⇄ 原始 HTTP 响应</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', fontSize: 12, color: 'var(--text-muted)' }}>
              {step.finalMethod && <span className="tag tag-blue">{step.finalMethod}</span>}
              {step.statusCode && <span className={`tag ${step.statusCode < 400 ? 'tag-green' : 'tag-red'}`}>{step.statusCode}</span>}
            </div>
          </div>
          {step.finalUrl && <div style={{ fontSize: 12, marginBottom: 8, wordBreak: 'break-all' }}><span style={{ color: 'var(--text-muted)' }}>URL: </span><span className="text-mono" style={{ color: 'var(--accent-cyan)' }}>{step.finalUrl}</span></div>}
          <pre style={{ background: 'var(--bg-card)', border: '1px solid var(--border-dim)', borderRadius: 10, padding: 12, maxHeight: 400, overflow: 'auto', fontFamily: 'var(--font-mono)', fontSize: 12, whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
            {(() => { try { return JSON.stringify(JSON.parse(step.rawResponse), null, 2); } catch { return step.rawResponse; } })()}
          </pre>
        </div>
      )}
    </div>
  );
}

/** 从 rawResponse JSON 中提取 schema 节点列表 */
function parseSchema(rawResponse) {
  if (!rawResponse) return [];
  try {
    const parsed = JSON.parse(rawResponse);
    return extractSchemaNodes(parsed);
  } catch {
    return [];
  }
}

function extractSchemaNodes(node) {
  if (node === null || node === undefined) return [];
  if (Array.isArray(node)) {
    const result = [{ path: '$', type: 'array', size: node.length }];
    if (node.length > 0 && node[0] !== null) {
      result[0].children = extractSchemaNodes(node[0]);
    }
    return result;
  }
  if (typeof node === 'object') {
    return Object.entries(node).map(([key, value]) => {
      const path = key;
      if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
        return { path, type: 'object', children: extractSchemaNodes(value) };
      }
      if (Array.isArray(value)) {
        const arrNode = { path, type: 'array', size: value.length };
        if (value.length > 0 && value[0] !== null) {
          arrNode.children = extractSchemaNodes(value[0]);
        }
        return arrNode;
      }
      return { path, type: schemaType(value), sample: value };
    });
  }
  return [{ path: '$', type: schemaType(node), sample: node }];
}

function schemaType(val) {
  if (val === null || val === undefined) return 'null';
  if (Array.isArray(val)) return 'array';
  switch (typeof val) {
    case 'string': return 'string';
    case 'number': return 'number';
    case 'boolean': return 'boolean';
    default: return 'unknown';
  }
}

function formatVal(v) {
  if (v === null || v === undefined) return <span className="text-muted">NULL</span>;
  if (typeof v === 'object') return JSON.stringify(v);
  return String(v);
}

export default function DebugPanel({ taskCode, taskName, isPipeline, onClose }) {
  const [running, setRunning] = useState(false);
  const [mode, setMode] = useState('stream'); // 'stream' | 'batch'
  const [result, setResult] = useState(null); // batch mode result
  const [streamSteps, setStreamSteps] = useState([]); // streaming mode: step list
  const [streamMeta, setStreamMeta] = useState(null); // { executionId, pipelineName, totalSteps }
  const [streamDone, setStreamDone] = useState(false);
  const [streamStatus, setStreamStatus] = useState(null); // final status
  const [write, setWrite] = useState(false);
  const [limit, setLimit] = useState(100);
  const sseRef = useRef(null);
  const { addToast, ToastContainer } = useToast();

  const closeSSE = useCallback(() => {
    if (sseRef.current) { sseRef.current.close(); sseRef.current = null; }
  }, []);

  // Batch mode (traditional)
  const runBatch = async (withWrite) => {
    setRunning(true);
    setResult(null);
    setStreamSteps([]);
    try {
      const api = isPipeline ? PipelineAPI : TaskAPI;
      const res = await api.debug(taskCode, { limit, write: withWrite });
      if (res.success) setResult(res.data);
      else addToast('调试失败: ' + (res.message || '未知错误'), 'error');
    } catch (e) {
      addToast('调试失败: ' + e.message, 'error');
    } finally {
      setRunning(false);
    }
  };

  // Streaming mode (SSE)
  const runStream = async (withWrite) => {
    setRunning(true);
    setResult(null);
    setStreamSteps([]);
    setStreamMeta(null);
    setStreamDone(false);
    setStreamStatus(null);
    closeSSE();

    try {
      const apiBase = '/api/etl/pipeline';
      const url = `${apiBase}/${encodeURIComponent(taskCode)}/debug/stream?limit=${limit}&write=${withWrite}`;

      const es = new EventSource(url);
      sseRef.current = es;

      es.addEventListener('init', (e) => {
        const data = JSON.parse(e.data);
        setStreamMeta(data);
        // Initialize all steps as PENDING
        setStreamSteps((data.steps || []).map(s => ({
          stepId: s.stepId, stepName: s.stepName, stepType: s.stepType,
          stepOrder: s.orderIndex || 0,
          status: 'PENDING', inputRows: 0, outputRows: 0, durationMs: 0,
        })));
      });

      es.addEventListener('step_start', (e) => {
        const data = JSON.parse(e.data);
        setStreamSteps(prev => prev.map(s =>
          s.stepId === data.stepId ? { ...s, status: 'RUNNING' } : s
        ));
      });

      es.addEventListener('step', (e) => {
        const data = JSON.parse(e.data);
        setStreamSteps(prev => prev.map(s =>
          s.stepId === data.stepId ? {
            ...s,
            status: data.status,
            inputRows: data.inputRows || 0,
            outputRows: data.outputRows || 0,
            durationMs: data.durationMs || 0,
            errorMessage: data.errorMessage,
            outputColumns: data.outputColumns,
            outputData: data.outputData,
            rawResponse: data.rawResponse,
            statusCode: data.statusCode,
            responseHeaders: data.responseHeaders,
            finalUrl: data.finalUrl,
            finalMethod: data.finalMethod,
            stepType: data.stepType || s.stepType,
          } : s
        ));
      });

      es.addEventListener('done', (e) => {
        const data = JSON.parse(e.data);
        setStreamStatus(data);
        setStreamDone(true);
        setRunning(false);
        es.close();
        sseRef.current = null;
      });

      es.addEventListener('error', (e) => {
        try {
          const data = e.data ? JSON.parse(e.data) : null;
          if (data && data.message) addToast('错误: ' + data.message, 'error');
        } catch {}
        setStreamDone(true);
        setRunning(false);
        es.close();
        sseRef.current = null;
      });

      es.onerror = () => {
        // EventSource auto-reconnects; stop if already done
        if (streamDone) { es.close(); sseRef.current = null; }
      };

    } catch (e) {
      addToast('流式调试启动失败: ' + e.message, 'error');
      setRunning(false);
    }
  };

  const handleClose = () => {
    closeSSE();
    onClose();
  };

  const overallClass = streamStatus?.status === 'SUCCESS' ? 'tag-green'
    : streamStatus?.status === 'FAILED' ? 'tag-red'
    : (result?.status === 'SUCCESS' ? 'tag-green' : result?.status === 'FAILED' ? 'tag-red' : 'tag-dim');

  const overallLabel = streamStatus?.status || result?.status || '--';

  const isStreamMode = mode === 'stream' && isPipeline;
  const stepList = isStreamMode ? streamSteps : (result?.steps || []);
  const isDone = isStreamMode ? streamDone : !!result;

  // Running step index for auto-highlight
  const runningIdx = streamSteps.findIndex(s => s.status === 'RUNNING');

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <ToastContainer />
      <div className="modal debug-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 780 }}>
        <div className="modal-header">
          <h3>⛭ 管线调试 — {streamMeta?.pipelineName || taskName || taskCode}</h3>
          <button className="modal-close" onClick={handleClose}>×</button>
        </div>

        <div className="modal-body">
          {/* Toolbar */}
          <div className="debug-toolbar">
            <div className="debug-toolbar-controls">
              <label className="debug-control-label">
                行数 <input type="number" min="1" max="10000" value={limit}
                  onChange={e => setLimit(parseInt(e.target.value) || 100)} style={{ width: 70 }} />
              </label>
              <label className="debug-control-label">
                <input type="checkbox" checked={write} onChange={e => setWrite(e.target.checked)} />
                写入目标
              </label>
              {isPipeline && (
                <div className="btn-group" style={{ marginLeft: 8 }}>
                  <button className={`btn btn-xs ${mode === 'stream' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => { closeSSE(); setMode('stream'); setStreamSteps([]); setStreamDone(false); setRunning(false); }}>
                    实时流
                  </button>
                  <button className={`btn btn-xs ${mode === 'batch' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => { closeSSE(); setMode('batch'); setResult(null); setRunning(false); }}>
                    一次性
                  </button>
                </div>
              )}
            </div>
            <div className="debug-toolbar-actions">
              {isStreamMode ? (
                <>
                  <button className="btn btn-secondary btn-sm" onClick={() => runStream(false)} disabled={running}>
                    {running ? '执行中...' : '⇣ 仅抽取(流式)'}
                  </button>
                  <button className="btn btn-primary btn-sm" onClick={() => runStream(true)} disabled={running}>
                    {running ? '执行中...' : '▶ 完整流程(流式)'}
                  </button>
                </>
              ) : (
                <>
                  <button className="btn btn-secondary btn-sm" onClick={() => runBatch(false)} disabled={running}>
                    {running ? '运行中...' : '⇣ 仅抽取调试'}
                  </button>
                  <button className="btn btn-primary btn-sm" onClick={() => runBatch(true)} disabled={running}>
                    {running ? '运行中...' : '▶ 完整流程调试'}
                  </button>
                </>
              )}
            </div>
          </div>

          {/* Idle state */}
          {!running && !isDone && (
            <div className="empty-state" style={{ padding: 40 }}>
              <div className="empty-icon">⛭</div>
              <p>选择调试方式开始执行</p>
              <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                {isPipeline
                  ? '实时流模式：逐步推送每个步骤的执行状态，像 Kettle 一样看到流程走到哪一步'
                  : '一次性模式：所有步骤执行完后统一展示结果'}
              </span>
            </div>
          )}

          {/* Pipeline flow visualization (streaming mode) */}
          {isStreamMode && streamMeta && stepList.length > 0 && (
            <div style={{
              marginBottom: 12, padding: 12, borderRadius: 10,
              background: 'var(--bg-card-alt, rgba(0,255,255,0.02))',
              border: '1px solid var(--border-dim)',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap', fontSize: 13 }}>
                <span style={{ fontWeight: 600, color: 'var(--accent-cyan)', marginRight: 4 }}>⛁ 执行流程</span>
                {stepList.map((s, i) => (
                  <span key={s.stepId} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <span className={`tag ${
                      s.status === 'SUCCESS' ? 'tag-green' :
                      s.status === 'RUNNING' ? 'tag-blue' :
                      s.status === 'FAILED' ? 'tag-red' : 'tag-dim'
                    }`} style={{
                      fontSize: 11,
                      animation: s.status === 'RUNNING' ? 'pulseGlow 1.5s ease-in-out infinite' : undefined,
                    }}>
                      {STEP_ICONS[s.stepType]} {s.stepName}
                      {s.status === 'RUNNING' && <span style={{ marginLeft: 4 }}>⏳</span>}
                    </span>
                    {i < stepList.length - 1 && (
                      <span style={{
                        color: stepList[i].status === 'SUCCESS' ? 'var(--accent-cyan)' : 'var(--text-muted)',
                        fontWeight: 700,
                      }}>→</span>
                    )}
                  </span>
                ))}
              </div>
              {streamDone && streamStatus && (
                <div style={{ marginTop: 8, fontSize: 12 }}>
                  <span className={`tag ${overallClass}`}>{overallLabel}</span>
                  {streamStatus.totalDurationMs > 0 && <span className="text-muted" style={{ marginLeft: 8 }}>总耗时 {streamStatus.totalDurationMs} ms</span>}
                </div>
              )}
            </div>
          )}

          {/* Step cards */}
          {stepList.length > 0 && (
            <div className="debug-steps">
              {stepList.map((s, i) => (
                <StepCard key={s.stepId || i} step={s} index={i} last={stepList.length - 1}
                  autoExpand={isStreamMode && (s.status === 'RUNNING' || (i === runningIdx))} />
              ))}
            </div>
          )}

          {/* Batch mode result header */}
          {result && !isStreamMode && (
            <div className="debug-result">
              <div className="debug-result-header">
                <div><span className="debug-result-label">执行ID</span><span className="text-mono text-sm">{result.executionId}</span></div>
                <div><span className="debug-result-label">耗时</span><span className="text-mono">{result.totalDurationMs} ms</span></div>
                <div>
                  <span className="debug-result-label">状态</span>
                  <span className={`tag ${overallClass}`}>{overallLabel}</span>
                </div>
              </div>
              {result.errorMessage && <div className="debug-step-error" style={{ margin: '8px 0' }}>{result.errorMessage}</div>}
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={handleClose}>关闭</button>
        </div>
      </div>
    </div>
  );
}
