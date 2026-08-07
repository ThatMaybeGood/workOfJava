import { useState } from 'react';
import { useTheme, ACCENTS } from '../context/ThemeContext';

export default function ThemeSwitcher() {
  const { theme, accent, setTheme, setAccent } = useTheme();
  const [open, setOpen] = useState(false);

  return (
    <div className="theme-switcher">
      <button
        className="theme-switcher-trigger"
        onClick={() => setOpen(o => !o)}
        title="切换主题"
        aria-label="切换主题"
      >
        {theme === 'light' ? '☀' : '☾'}
      </button>

      {open && (
        <div className="theme-popover" onClick={e => e.stopPropagation()}>
          <div className="theme-popover-title">主题模式</div>
          <div className="theme-mode-row">
            <button
              className={`theme-mode-btn${theme === 'light' ? ' active' : ''}`}
              onClick={() => setTheme('light')}
            >
              <span className="theme-mode-icon">☀</span> 亮色
            </button>
            <button
              className={`theme-mode-btn${theme === 'dark' ? ' active' : ''}`}
              onClick={() => setTheme('dark')}
            >
              <span className="theme-mode-icon">☾</span> 深色
            </button>
          </div>

          <div className="theme-popover-title">主题色</div>
          <div className="accent-row">
            {ACCENTS.map(a => (
              <button
                key={a.key}
                className={`accent-dot${accent === a.key ? ' active' : ''}`}
                style={{ background: a.color }}
                onClick={() => setAccent(a.key)}
                title={a.label}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
