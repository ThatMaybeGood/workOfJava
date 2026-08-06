import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!username || !password) {
      setError('请输入用户名和密码');
      return;
    }
    setLoading(true);
    // Simulate slight delay for effect
    await new Promise(r => setTimeout(r, 400));
    const result = login(username, password);
    setLoading(false);
    if (result.success) {
      navigate('/');
    } else {
      setError(result.message);
    }
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div className="login-logo">
          <span className="logo-icon">⚡</span>
          <h1><span>ETL</span> NEXUS</h1>
          <p>数据流转平台</p>
        </div>

        {error && <div className="login-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>IDENT / 用户名</label>
            <input
              type="text"
              placeholder="输入用户名"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label>ACCESS CODE / 密码</label>
            <input
              type="password"
              placeholder="输入访问密码"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? '◈ 验证中...' : '◈ 接入系统'}
          </button>
        </form>

        <div className="login-hint">
          演示账号：<span>admin</span> / <span>admin123</span><br />
          操作员：<span>operator</span> / <span>operator123</span>
        </div>
      </div>
    </div>
  );
}
