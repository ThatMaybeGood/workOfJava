import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PipelineAPI, DataSourceAPI, MonitorAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const StatCard = ({ color, icon, value, label }) => (
  <div className={`stat-card stat-${color} count-reveal`}>
    <div className="stat-icon-wrap">{icon}</div>
    <div>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  </div>
);

export default function Dashboard() {
  const [stats, setStats] = useState({
    totalTasks: 0, running: 0, success: 0, failed: 0,
    datasources: 0, scheduled: 0,
  });
  const [recentLogs, setRecentLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToast, ToastContainer } = useToast();
  const navigate = useNavigate();

  useEffect(() => { loadData(); }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [pipeRes, dsRes, logRes] = await Promise.all([
        PipelineAPI.list(),
        DataSourceAPI.list(),
        MonitorAPI.getAllLogs(50),
      ]);
      const pipelineList = pipeRes.success ? (pipeRes.data || []) : [];
      const dsList = dsRes.success ? (dsRes.data || []) : [];
      const allLogs = logRes.success ? (logRes.data || []) : [];

      let successCount = 0, failedCount = 0, runningCount = 0;
      for (const log of allLogs) {
        if (log.status === 'SUCCESS') successCount++;
        else if (log.status === 'FAILED') failedCount++;
        else if (log.status === 'RUNNING') runningCount++;
      }

      setStats({
        totalTasks: pipelineList.length,
        datasources: dsList.length,
        scheduled: pipelineList.filter(p => p.cronExpr).length,
        running: runningCount,
        success: successCount,
        failed: failedCount,
      });
      setRecentLogs(allLogs.slice(0, 10));
    } catch (e) {
      addToast('数据加载失败: ' + e.message, 'error');
    } finally {
      setLoading(false);
    }
  }

  const formatDur = (s) => s ? `${s}s` : '-';

  if (loading) {
    return (
      <div className="main-area">
        <div className="top-bar"><h2><span className="bar-icon">◇</span> 仪表盘</h2></div>
        <div className="loading-screen"><div className="loader" /><div className="loading-text">加载中...</div></div>
      </div>
    );
  }

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2>
          <span className="status-indicator" />
          <span className="bar-icon">◇</span> NEXUS 仪表盘
        </h2>
        <div className="top-bar-actions">
          <span className="text-muted text-sm font-display" style={{ letterSpacing: 2 }}>
            {new Date().toLocaleString('zh-CN')}
          </span>
          <button className="btn btn-secondary btn-sm" onClick={loadData}>
            ⟳ 刷新
          </button>
        </div>
      </div>

      <div className="content-area">
        <div className="stats-grid">
          <StatCard color="cyan" icon="◈" value={stats.totalTasks} label="管线总数" />
          <StatCard color="blue" icon="▶" value={stats.running} label="活跃执行" />
          <StatCard color="green" icon="◆" value={stats.success} label="执行成功" />
          <StatCard color="red" icon="✕" value={stats.failed} label="执行失败" />
          <StatCard color="purple" icon="⬡" value={stats.datasources} label="数据节点" />
          <StatCard color="orange" icon="◎" value={stats.scheduled} label="定时调度" />
        </div>

        <div className="section-header">
          <h3>◈ 最近执行记录</h3>
        </div>

        <div className="card">
          <div className="card-body" style={{ padding: 0 }}>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>流水号</th>
                    <th>管线编码</th>
                    <th>管线名称</th>
                    <th>状态</th>
                    <th>开始时间</th>
                    <th>耗时</th>
                    <th>影响行数</th>
                  </tr>
                </thead>
                <tbody>
                  {recentLogs.length === 0 ? (
                    <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
                      暂无执行记录 — 创建并运行你的第一个 ETL 管线
                    </td></tr>
                  ) : recentLogs.map((log, i) => (
                    <tr key={i}>
                      <td className="text-mono text-muted" style={{ fontSize: 11 }}>{log.executionId || '-'}</td>
                      <td className="text-cyan" style={{ fontWeight: 600 }}>{log.taskCode}</td>
                      <td>{log.taskName || '-'}</td>
                      <td>
                        <span className={`tag ${log.status === 'SUCCESS' ? 'tag-green' : log.status === 'FAILED' ? 'tag-red' : log.status === 'RUNNING' ? 'tag-cyan tag-pulse' : 'tag-dim'}`}>
                          {log.status}
                        </span>
                      </td>
                      <td className="text-muted text-sm">{log.startTime ? new Date(log.startTime).toLocaleString('zh-CN') : '-'}</td>
                      <td className="text-mono text-sm">{formatDur(log.executionDuration)}</td>
                      <td className="text-mono" style={{ fontWeight: 600 }}>{log.totalRows || 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div className="section-header mt-24">
          <h3>◈ 快捷操作</h3>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-primary" onClick={() => navigate('/datasource')}>
            ＋ 新增数据节点
          </button>
          <button className="btn btn-secondary" onClick={() => navigate('/pipeline')}>
            ＋ 新建管线
          </button>
        </div>
      </div>
    </div>
  );
}
