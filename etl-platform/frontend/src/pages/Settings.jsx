import { useState, useEffect, useCallback } from 'react';
import { SystemSettingsAPI } from '../api/etl';
import { useToast } from '../components/useToast';

const EMPTY_FORM = {
  port: '18880',
  dbMode: 'h2',
  h2DbPath: './data/etl_platform',
  dbUrl: '',
  dbUsername: '',
  dbPassword: '',
  logPath: 'logs/etl-platform-prod.log',
};

export default function Settings() {
  const [form, setForm] = useState({ ...EMPTY_FORM });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const { addToast, ToastContainer } = useToast();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await SystemSettingsAPI.get();
      if (res.success && res.data) {
        const d = res.data;
        setForm({
          port: d.port || '18880',
          dbMode: d.dbMode || 'h2',
          h2DbPath: d.h2DbPath || './data/etl_platform',
          dbUrl: d.dbUrl || '',
          dbUsername: d.dbUsername || '',
          dbPassword: '',
          logPath: d.logPath || 'logs/etl-platform-prod.log',
        });
      }
    } catch (e) { addToast('加载设置失败: ' + e.message, 'error'); }
    finally { setLoading(false); }
  }, [addToast]);

  useEffect(() => { load(); }, [load]);

  const update = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleSave = async () => {
    if (!form.port) { addToast('端口不能为空', 'error'); return; }
    const portNum = parseInt(form.port);
    if (isNaN(portNum) || portNum < 1 || portNum > 65535) {
      addToast('端口必须在 1-65535 之间', 'error'); return;
    }
    if (form.dbMode === 'external' && !form.dbUrl) {
      addToast('外部数据库连接URL不能为空', 'error'); return;
    }
    setSaving(true);
    try {
      const res = await SystemSettingsAPI.save({
        port: form.port,
        dbMode: form.dbMode,
        h2DbPath: form.dbMode === 'h2' ? form.h2DbPath : '',
        dbUrl: form.dbMode === 'external' ? form.dbUrl : '',
        dbUsername: form.dbMode === 'external' ? form.dbUsername : '',
        dbPassword: form.dbMode === 'external' ? form.dbPassword : '',
        logPath: form.logPath,
      });
      if (res.success) {
        addToast('设置已保存，重启服务后生效', 'success');
      } else {
        addToast('保存失败: ' + (res.message || '未知错误'), 'error');
      }
    } catch (e) { addToast('保存失败: ' + e.message, 'error'); }
    finally { setSaving(false); }
  };

  return (
    <div className="main-area">
      <ToastContainer />
      <div className="top-bar">
        <h2><span className="bar-icon">⚙</span> 系统设置</h2>
        <div className="top-bar-actions">
          <button className="btn btn-primary" onClick={handleSave} disabled={saving || loading}>
            {saving ? '保存中...' : '💾 保存设置'}
          </button>
        </div>
      </div>

      <div className="content-area">
        {loading ? (
          <div className="loading-screen"><div className="loader" /><div className="loading-text">加载中...</div></div>
        ) : (
          <div style={{ maxWidth: 760 }}>
            {/* 服务端口 */}
            <fieldset className="fieldset-card">
              <legend><span className="bar-icon">⇄</span> 服务端口</legend>
              <div className="form-row">
                <div className="form-group">
                  <label>监听端口 <span className="required">*</span></label>
                  <input type="number" value={form.port} onChange={e => update('port', e.target.value)} placeholder="18880" />
                </div>
                <div className="form-group">
                  <label>日志文件路径</label>
                  <input value={form.logPath} onChange={e => update('logPath', e.target.value)} placeholder="logs/etl-platform-prod.log" />
                </div>
              </div>
            </fieldset>

            {/* 元数据库 */}
            <fieldset className="fieldset-card" style={{ marginTop: 16 }}>
              <legend><span className="bar-icon">◈</span> 元数据库</legend>
              <div className="form-row">
                <div className="form-group" style={{ gridColumn: '1/-1' }}>
                  <label>数据库类型</label>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button type="button"
                      className={`btn btn-xs ${form.dbMode === 'h2' ? 'btn-primary' : 'btn-secondary'}`}
                      onClick={() => update('dbMode', 'h2')}>
                      内置 H2 文件库（推荐）
                    </button>
                    <button type="button"
                      className={`btn btn-xs ${form.dbMode === 'external' ? 'btn-primary' : 'btn-secondary'}`}
                      onClick={() => update('dbMode', 'external')}>
                      外部数据库
                    </button>
                  </div>
                </div>
              </div>

              {form.dbMode === 'h2' ? (
                <div className="form-row">
                  <div className="form-group" style={{ gridColumn: '1/-1' }}>
                    <label>H2 数据库文件路径</label>
                    <input value={form.h2DbPath} onChange={e => update('h2DbPath', e.target.value)} placeholder="./data/etl_platform" />
                    <div className="text-muted" style={{ fontSize: 11, marginTop: 6 }}>
                      使用文件库后数据持久化，重启不丢失。推荐保持默认，相对程序运行目录。
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  <div className="form-row">
                    <div className="form-group" style={{ gridColumn: '1/-1' }}>
                      <label>JDBC URL <span className="required">*</span></label>
                      <input value={form.dbUrl} onChange={e => update('dbUrl', e.target.value)}
                        placeholder="jdbc:oracle:thin:@host:1521:ORCL" style={{ fontFamily: 'var(--font-mono)', fontSize: 12 }} />
                    </div>
                  </div>
                  <div className="form-row">
                    <div className="form-group"><label>用户名</label><input value={form.dbUsername} onChange={e => update('dbUsername', e.target.value)} /></div>
                    <div className="form-group"><label>密码</label><input type="password" value={form.dbPassword} onChange={e => update('dbPassword', e.target.value)} /></div>
                  </div>
                </>
              )}
            </fieldset>

            {/* 提示 */}
            <div className="card" style={{ marginTop: 16, borderColor: 'var(--border-glow)' }}>
              <div className="card-body">
                <div style={{ fontSize: 13, color: 'var(--accent-cyan)', fontWeight: 600, marginBottom: 8 }}>⚠ 重启后生效</div>
                <div style={{ fontSize: 12.5, color: 'var(--text-secondary)', lineHeight: 1.7 }}>
                  端口、元数据库、日志路径在服务启动时加载。保存后需要<strong>重启服务</strong>才能生效。
                  <br />· jar 方式：停止进程后重新执行 <code className="text-mono" style={{ background: 'var(--bg-input)', padding: '2px 6px', borderRadius: 4 }}>java -jar etl-platform.jar</code>
                  <br />· 配置保存于程序目录 <code className="text-mono" style={{ background: 'var(--bg-input)', padding: '2px 6px', borderRadius: 4 }}>./config/etl-runtime.yml</code>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
