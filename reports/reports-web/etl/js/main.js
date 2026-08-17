/**
 * ETL 管理主界面
 */
document.addEventListener('DOMContentLoaded', () => {
    const navLinks = document.querySelectorAll('.etl-nav .nav-link');
    const contentDiv = document.getElementById('page-content');

    function loadPage(pageName) {
        navLinks.forEach(link => link.classList.remove('active'));
        document.querySelector(`[data-page="${pageName}"]`)?.classList.add('active');

        switch(pageName) {
            case 'datasource': DatasourceApp.render(contentDiv); break;
            case 'task': TaskApp.render(contentDiv); break;
            case 'mapping': MappingApp.render(contentDiv); break;
            case 'debug': DebugApp.render(contentDiv); break;
            case 'schedule': ScheduleApp.render(contentDiv); break;
            default: contentDiv.innerHTML = '<p class="text-muted">请选择左侧菜单</p>';
        }
    }

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            loadPage(link.dataset.page);
        });
    });

    // 默认加载数据源页面
    loadPage('datasource');
});