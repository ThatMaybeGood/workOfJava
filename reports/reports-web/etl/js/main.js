/**
 * ETL 应用壳 — hash 路由
 * 路由：#/wizard、#/sources、#/datasources、#/history（默认 #/wizard）
 * 模块不存在时渲染「模块加载中」占位，不报错
 * 切页不重置 sessionStorage 向导状态
 */
(function () {
    const ROUTES = {
        wizard: { global: 'WizardApp', title: '流水线向导' },
        sources: { global: 'SourceApp', title: '来源库' },
        datasources: { global: 'DatasourceApp', title: '数据源' },
        tasks: { global: 'TaskApp', title: '流水线任务' },
        history: { global: 'ScheduleApp', title: '调度历史' },
        settings: { global: 'SettingsApp', title: '系统设置' }
    };
    const DEFAULT_ROUTE = 'wizard';

    function currentRoute() {
        const hash = (location.hash || '').replace(/^#\/?/, '').split('?')[0];
        return ROUTES[hash] ? hash : DEFAULT_ROUTE;
    }

    function syncNav(route) {
        document.querySelectorAll('.etl-nav-link').forEach(function (link) {
            link.classList.toggle('active', link.getAttribute('data-route') === route);
        });
    }

    function renderPlaceholder(container, title) {
        container.innerHTML =
            '<div class="etl-card">' +
            '<div class="etl-empty">' +
            '<div class="etl-empty-text">' + title + '模块加载中…</div>' +
            '<span class="etl-badge info">待接入</span>' +
            '</div>' +
            '</div>';
    }

    function render() {
        const container = document.getElementById('etl-main');
        if (!container) return;

        const route = currentRoute();
        syncNav(route);

        const conf = ROUTES[route];
        const app = window[conf.global];
        if (app && typeof app.render === 'function') {
            try {
                app.render(container);
            } catch (e) {
                console.error('[ETL] 模块渲染失败:', conf.global, e);
                container.innerHTML =
                    '<div class="etl-card">' +
                    '<div class="etl-empty">' +
                    '<div class="etl-empty-text">' + conf.title + '渲染失败</div>' +
                    '</div>' +
                    '</div>';
                if (window.etlComponents && typeof window.etlComponents.toast === 'function') {
                    window.etlComponents.toast(conf.title + '渲染失败：' + (e && e.message ? e.message : ''), 'err');
                }
            }
        } else {
            renderPlaceholder(container, conf.title);
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        if (!location.hash) {
            location.replace('#/' + DEFAULT_ROUTE);
        }
        render();
        window.addEventListener('hashchange', render);
    });
})();
