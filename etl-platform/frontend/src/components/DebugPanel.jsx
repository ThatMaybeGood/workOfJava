import { useState } from 'react';
import { TaskAPI } from '../api/etl';
import { useToast } from './useToast';

const STEP_LABELS = { EXTRACT: '数据抽取', TRANSFORM: '字段转换', LOAD: '目标写入' };
const STEP_ICONS = { EXTRACT: '⇣', TRANSFORM: '⇄', LOAD: '⇧' };

function StepCard({ step, index, last }) {
  const [open, setOpen] = useState(false);
  const statusClass = step.status === 'SUCCESS' ? 'tag-green'
    : step.status === 'FAILED' ? 'tag-red' : 'tag-dim';

  return (
    <div className="debug-step">
      <div className="debug-step-head" onClick={() => setOpen(o => !o)}>
        <div className="debug-step-index">{step.stepOrder}</div>
        <div className="debug-step-icon">{STEP_ICONS[step.stepType]}</div>
        <div className="debug-step-info">
          <div className="debug-step-name">{step.stepName}</div>
          <div className="debug-step-meta">
            <span className="tag tag-dim">{step.stepType}</span>
            <span>入参 {step.inputRows} 行</span>
            <span>出参 {step.outputRows} 行</span>
            <span>{step.durationMs} ms</span>
          </div>
        </div>
        <span className={`tag ${statusClass}`}>{step.status === 'SUCCESS' ? '成功' : step.status === 'FAILED' ? '失败' : step.status}</span>
        <span className="debug-step-arrow">{open ? '▾' : '▸'}</span>
      </div>

      <div className="debug-step-route">
        {step.nextOnSuccess && <span className="route-next">成功 → 下一步</span>}
        {!step.nextOnSuccess && step.stepType !== 'LOAD' && <span className="route-stop">成功 → 停止</span>}
        {step.stepType === 'LOAD' && <span className="route-end">成功 → 结束</span>}
        {step.nextOnFail && <span className="route-fail">失败 → 跳过继续</span>}
        {!step.nextOnFail && step.status === 'FAILED' && <span className="route-stop">失败 → 终止流程</span>}
        {index < last && <span className="route-connector">↓</span>}
      </div>

      {step.errorMessage && (
        <div className="debug-step-error">
          {step.errorMessage}
        </div>
      )}

      {open && step.outputData && step.outputData.length > 0 && (
        <div className="debug-step-data">
          <div className="debug-data-title">出参数据（{step.outputData.length} 条）</div>
          <div className="table-wrap" style={{ borderRadius: 10, border: '1px solid var(--border-dim)' }}>
            <table className="data-table" style={{ fontSize: 12 }}>
              <thead>
                <tr>
                  {(step.outputColumns || Object.keys(step.outputData[0] || {})).map(c => (
                    <th key={c}>{c}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {step.outputData.map((row, ri) => (
                  <tr key={ri}>
                    {Object.entries(row).map(([k, v]) => (
                      <td key={k} className="text-mono text-sm">{formatVal(v)}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

function formatVal(v) {
  if (v === null || v === undefined) return <span className="text-muted">NULL</span>;
  if (typeof v === 'object') return JSON.stringify(v);
  return String(v);
}

export default function DebugPanel({ taskCode, taskName, onClose }) {
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState(null);
  const [write, setWrite] = useState(false);
  const [limit, setLimit] = useState(100);
  const { addToast, ToastContainer } = useToast();

  const runDebug = async (withWrite) => {
    setRunning(true);
    setResult(null);
    try {
      const res = await TaskAPI.debug(taskCode, { limit, write: withWrite });
      if (res.success) setResult(res.data);
    } catch (e) {
      addToast('调试失败: ' + e.message, 'error');
    } finally {
      setRunning(false);
    }
  };

  const overallClass = result?.status === 'SUCCESS' ? 'tag-green'
    : result?.status === 'FAILED' ? 'tag-red' : 'tag-dim';

  return (
    <div className="modal-overlay" onClick={onClose}>
      <ToastContainer />
      <div className="modal debug-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>⛭ 任务调试 — {taskName || taskCode}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="modal-body">
          <div className="debug-toolbar">
            <div className="debug-toolbar-controls">
              <label className="debug-control-label">
                抽取行数
                <input type="number" min="1" value={limit}
                  onChange={e => setLimit(parseInt(e.target.value) || 100)}
                  style={{ width: 90 }} />
              </label>
              <label className="debug-control-label">
                <input type="checkbox" checked={write}
                  onChange={e => setWrite(e.target.checked)} />
                执行写入目标库
              </label>
            </div>
            <div className="debug-toolbar-actions">
              <button className="btn btn-secondary btn-sm" onClick={() => runDebug(false)} disabled={running}>
                {running ? '运行中...' : '⇣ 仅抽取调试'}
              </button>
              <button className="btn btn-primary btn-sm" onClick={() => runDebug(true)} disabled={running}>
                {running ? '运行中...' : '▶ 完整流程调试'}
              </button>
            </div>
          </div>

          {running && (
            <div className="loading-screen" style={{ minHeight: 120 }}>
              <div className="loader" />
              <div className="loading-text">步骤执行中...</div>
            </div>
          )}

          {!running && !result && (
            <div className="empty-state" style={{ padding: 40 }}>
              <div className="empty-icon">⛭</div>
              <p>选择调试方式开始执行</p>
              <span>仅抽取：只验证抽取步骤并查看出参结果，不写库<br/>完整流程：抽取 → 转换 → 写入目标库</span>
            </div>
          )}

          {result && (
            <div className="debug-result">
              <div className="debug-result-header">
                <div>
                  <span className="debug-result-label">执行ID</span>
                  <span className="text-mono text-sm">{result.executionId}</span>
                </div>
                <div>
                  <span className="debug-result-label">总体耗时</span>
                  <span className="text-mono">{result.totalDurationMs} ms</span>
                </div>
                <div>
                  <span className="debug-result-label">总体状态</span>
                  <span className={`tag ${overallClass}`}>{result.status === 'SUCCESS' ? '成功' : '失败'}</span>
                </div>
              </div>

              <div className="debug-steps">
                {(result.steps || []).map((s, i) => (
                  <StepCard key={i} step={s} index={i} last={(result.steps || []).length - 1} />
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>关闭</button>
        </div>
      </div>
    </div>
  );
}
