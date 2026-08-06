import { useState, useEffect, useCallback } from 'react';
import { MonitorAPI, TaskAPI } from '../api/etl';
import { useToast } from '../components/useToast';

export default function Log() {
  const [tasks, setTasks] = useState([]);
  const [allLogs, setAllLogs] = useState([]);
  const [filteredLogs, setFilteredLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [taskFilter, setTaskFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [searchText, setSearchText] = useState('');
  const [expanded, setExpanded] = useState({});
  const { addToast, ToastContainer } = useToast();

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [taskRes] = await Promise.all([TaskAPI.list()]);
      const taskList = taskRes.success ? (taskRes.data || []) : [];
      setTasks(taskList);

      const logs = [];
      for (const task of taskList) {
        try {
          const res = await MonitorAPI.getLogs(task.taskCode);
          if (res.success && res.data) logs.push(...res.data);
        } catch {}
      }
      logs.sort((a, b) => new Date(b.startTime) - new Date(a.startTime));
      setAllLogs(logs);
      setFilteredLogs(logs);
    } catch (e) { addToast('加载失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  useEffect(() => { loadData(); }, [loadData]);

  useEffect(() => {
    let result = allLogs;
    if (taskFilter) result = result.filter(l => l.taskCode === taskFilter);
    if (statusFilter) result = result.filter(l => l.status === statusFilter);
    if (searchText) {
      const lower = searchText.toLowerCase();
      result = result.filter(l =>
        (l.taskCode && l.taskCode.toLowerCase().includes(lower)) ||
        (l.taskName && l.taskName.toLowerCase().includes(lower))
      );
    }
    setFilteredLogs(result);
  }, [taskFilter, statusFilter, searchText, allLogs]);

  const toggleExpand = (idx) => {
    setExpanded(prev => ({ ...prev, [idx]: !prev[idx] }));
  };

  const formatDur = (s) => s ? `${s}s` : '-';

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">◎</span> 执行日志</h2>
        <div className="top-bar-actions">
          <button className="btn btn-secondary btn-sm" onClick={loadData}>⟳ 刷新</button>
        </div>
      </div>

      <div className="content-area">
        <div className="filter-bar">
          <select value={taskFilter} onChange={e => setTaskFilter(e.target.value)}>
            <option value="">所有引擎</option>
            {tasks.map(t => <option key={t.taskCode} value={t.taskCode}>{t.taskCode} — {t.taskName}</option>)}
          </select>
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">所有状态</option>
            <option value="RUNNING">执行中</option>
            <option value="SUCCESS">成功</option>
            <option value="FAILED">失败</option>
          </select>
          <input type="text" placeholder="搜索任务编码或名称..." value={searchText} onChange={e => setSearchText(e.target.value)} />
        </div>

        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>流水号</th>
                    <th>任务编码</th>
                    <th>任务名称</th>
                    <th>状态</th>
                    <th>触发类型</th>
                    <th>开始时间</th>
                    <th>耗时</th>
                    <th>行数</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40 }}>
                      <div className="loader" style={{ margin: '0 auto' }} />
                    </td></tr>
                  ) : filteredLogs.length === 0 ? (
                    <tr><td colSpan={9} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      暂无执行记录
                    </td></tr>
                  ) : filteredLogs.map((log, i) => (
                    <>
                      <tr key={`row-${i}`} onClick={() => toggleExpand(i)} style={{ cursor: 'pointer' }}>
                        <td className="text-mono text-muted text-sm">{log.executionId || '-'}</td>
                        <td className="text-cyan" style={{ fontWeight: 600 }}>{log.taskCode}</td>
                        <td>{log.taskName || '-'}</td>
                        <td>
                          <span className={`tag ${log.status === 'SUCCESS' ? 'tag-green' : log.status === 'FAILED' ? 'tag-red' : log.status === 'RUNNING' ? 'tag-cyan tag-pulse' : 'tag-dim'}`}>
                            {log.status}
                          </span>
                        </td>
                        <td><span className="tag tag-dim">{log.triggerType || '-'}</span></td>
                        <td className="text-muted text-sm">{log.startTime ? new Date(log.startTime).toLocaleString('zh-CN') : '-'}</td>
                        <td className="text-mono text-sm">{formatDur(log.executionDuration)}</td>
                        <td className="text-mono" style={{ fontWeight: 600 }}>{log.totalRows || 0}</td>
                        <td><span className="text-muted">{expanded[i] ? '▴' : '▾'}</span></td>
                      </tr>
                      {expanded[i] && (
                        <tr key={`detail-${i}`}>
                          <td colSpan={9} style={{ background: 'var(--bg-surface)', padding: 20 }}>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px 24px', fontSize: 13 }}>
                              <div><span className="text-muted">执行ID：</span><span className="text-mono">{log.executionId || '-'}</span></div>
                              <div><span className="text-muted">任务编码：</span><span className="text-cyan" style={{ fontWeight: 600 }}>{log.taskCode}</span></div>
                              <div><span className="text-muted">任务名称：</span>{log.taskName || '-'}</div>
                              <div><span className="text-muted">开始时间：</span>{log.startTime ? new Date(log.startTime).toLocaleString('zh-CN') : '-'}</div>
                              <div><span className="text-muted">结束时间：</span>{log.endTime ? new Date(log.endTime).toLocaleString('zh-CN') : '-'}</div>
                              <div><span className="text-muted">耗时：</span><span className="text-mono">{formatDur(log.executionDuration)}</span></div>
                              <div><span className="text-muted">总行数：</span><span className="text-mono" style={{ fontWeight: 600, color: 'var(--accent-cyan)' }}>{log.totalRows || 0}</span></div>
                              <div><span className="text-muted">成功行数：</span><span className="text-mono" style={{ color: 'var(--accent-green)' }}>{log.successRows || 0}</span></div>
                              <div><span className="text-muted">失败行数：</span><span className="text-mono" style={{ color: 'var(--accent-red)' }}>{log.failedRows || 0}</span></div>
                              <div><span className="text-muted">触发类型：</span>{log.triggerType || '-'}</div>
                              <div><span className="text-muted">触发用户：</span>{log.triggerUser || '-'}</div>
                              <div><span className="text-muted">状态：</span>{log.status}</div>
                            </div>
                            {log.errorMessage && (
                              <div style={{ marginTop: 12, background: 'rgba(255,23,68,0.08)', border: '1px solid rgba(255,23,68,0.2)', borderRadius: 'var(--radius-sm)', padding: 12, fontFamily: 'var(--font-mono)', fontSize: 12, color: 'var(--accent-red)', whiteSpace: 'pre-wrap', wordBreak: 'break-all', maxHeight: 200, overflowY: 'auto' }}>
                                {log.errorMessage}
                              </div>
                            )}
                          </td>
                        </tr>
                      )}
                    </>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
