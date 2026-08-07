/**
 * Theme manager — shared across all ETL Platform pages
 */
(function() {
    const THEME_KEY = 'etl-theme';

    function getSavedTheme() {
        return localStorage.getItem(THEME_KEY) || 'light';
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(THEME_KEY, theme);
        updateToggleIcon(theme);
    }

    function updateToggleIcon(theme) {
        const btn = document.getElementById('themeToggleBtn');
        if (!btn) return;
        if (theme === 'dark') {
            btn.innerHTML = '<i class="fas fa-sun"></i>';
            btn.title = '切换浅色模式';
        } else {
            btn.innerHTML = '<i class="fas fa-moon"></i>';
            btn.title = '切换深色模式';
        }
    }

    function toggleTheme() {
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        const next = current === 'dark' ? 'light' : 'dark';
        applyTheme(next);
    }

    // Initialize on load
    document.addEventListener('DOMContentLoaded', function() {
        applyTheme(getSavedTheme());
        const btn = document.getElementById('themeToggleBtn');
        if (btn) {
            btn.addEventListener('click', toggleTheme);
        }
    });

    // Expose
    window.toggleTheme = toggleTheme;
    window.getCurrentTheme = function() {
        return document.documentElement.getAttribute('data-theme') || 'light';
    };
})();
