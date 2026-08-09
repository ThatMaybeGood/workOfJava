import { useState, useRef, useCallback, useEffect } from 'react';

/**
 * 管线 SSE 流式执行 hook。
 * 复用 DebugPanel 的实时流逻辑，供管线列表页"运行观察"内联展示每步状态。
 *
 * 调用：const stream = usePipelineStream(); stream.start(pipelineCode, withWrite, limit);
 * 事件流：init(初始化步骤) → step_start(某步运行中) → step(某步完成) → done(全部完成)
 */
export default function usePipelineStream() {
  const [running, setRunning] = useState(false);
  const [steps, setSteps] = useState([]);
  const [meta, setMeta] = useState(null);   // { executionId, pipelineName, totalSteps, steps }
  const [status, setStatus] = useState(null); // { status, totalDurationMs, errorMessage }
  const [done, setDone] = useState(false);
  const [error, setError] = useState(null);
  const sseRef = useRef(null);
  const finishedRef = useRef(false);

  const close = useCallback(() => {
    if (sseRef.current) { sseRef.current.close(); sseRef.current = null; }
  }, []);

  const reset = useCallback(() => {
    close();
    finishedRef.current = false;
    setSteps([]); setMeta(null); setStatus(null); setDone(false); setRunning(false); setError(null);
  }, [close]);

  const start = useCallback((pipelineCode, withWrite = true, limit = 100) => {
    if (!pipelineCode) return;
    reset();
    setRunning(true);
    const url = `/api/etl/pipeline/${encodeURIComponent(pipelineCode)}/debug/stream?limit=${limit}&write=${withWrite}`;
    const es = new EventSource(url);
    sseRef.current = es;

    const finish = (data, isError) => {
      if (isError) setError(data && data.message);
      else setStatus(data);
      setDone(true);
      setRunning(false);
      finishedRef.current = true;
      es.close();
      sseRef.current = null;
    };

    es.addEventListener('init', (e) => {
      const data = JSON.parse(e.data);
      setMeta(data);
      setSteps((data.steps || []).map(s => ({
        stepId: s.stepId, stepName: s.stepName, stepType: s.stepType,
        stepOrder: s.orderIndex || 0,
        status: 'PENDING', inputRows: 0, outputRows: 0, durationMs: 0,
      })));
    });

    es.addEventListener('step_start', (e) => {
      const data = JSON.parse(e.data);
      setSteps(prev => prev.map(s =>
        s.stepId === data.stepId ? { ...s, status: 'RUNNING' } : s
      ));
    });

    es.addEventListener('step', (e) => {
      const data = JSON.parse(e.data);
      setSteps(prev => prev.map(s =>
        s.stepId === data.stepId ? {
          ...s,
          status: data.status,
          inputRows: data.inputRows || 0,
          outputRows: data.outputRows || 0,
          durationMs: data.durationMs || 0,
          errorMessage: data.errorMessage,
          stepType: data.stepType || s.stepType,
        } : s
      ));
    });

    es.addEventListener('done', (e) => {
      try { finish(JSON.parse(e.data), false); } catch {}
    });

    es.addEventListener('error', (e) => {
      let msg = null;
      try {
        const data = e.data ? JSON.parse(e.data) : null;
        if (data && data.message) msg = data.message;
      } catch {}
      finish(msg ? { message: msg } : null, true);
    });

    es.onerror = () => {
      // EventSource 网络错误会自动重连；已完成则停止
      if (finishedRef.current) { es.close(); sseRef.current = null; }
    };
  }, [reset]);

  useEffect(() => () => close(), [close]);

  return { steps, meta, status, done, running, error, start, close, reset };
}
