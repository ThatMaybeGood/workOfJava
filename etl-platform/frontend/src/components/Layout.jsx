import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ThemeSwitcher from './ThemeSwitcher';

const NAV_ITEMS = [
  { to: '/', icon: '◇', label: '仪表盘', exact: true },
  { to: '/datasource', icon: '⬡', label: '数据源' },
  { to: '/task', icon: '◈', label: '任务管理' },
  { to: '/mapping', icon: '▣', label: '字段映射' },
  { to: '/log', icon: '◎', label: '执行日志' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h1>
            <span className="logo-mark">⚡</span>
            NEXUS
          </h1>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section">
            <div className="nav-section-title">核心模块</div>
            {NAV_ITEMS.map(item => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.exact}
                className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
              >
                <span className="nav-icon">{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            ))}
          </div>
        </nav>

        <ThemeSwitcher />

        <div className="sidebar-footer">
          <div className="user-avatar">{user?.username?.[0]?.toUpperCase() || 'A'}</div>
          <div className="user-info">
            <div className="user-name">{user?.username || 'admin'}</div>
            <div className="user-role">{user?.roleLabel || '系统管理员'}</div>
          </div>
          <button className="logout-btn" onClick={handleLogout} title="退出登录">⏻</button>
        </div>
      </aside>

      <div className="main-area">
        <Outlet />
      </div>
    </div>
  );
}
