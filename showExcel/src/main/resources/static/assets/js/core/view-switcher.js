// 视图切换管理

// 切换视图
function switchView(viewType) {
    AppState.currentView = viewType;

    // 隐藏所有视图
    document.querySelectorAll('.view-content').forEach(view => {
        view.classList.remove('active');
    });

    // 显示当前视图
    const currentView = document.getElementById(viewType + 'View');
    if (currentView) {
        currentView.classList.add('active');

        // 视图特定的初始化
        switch(viewType) {
            case 'table':
                if (typeof initializeTableView === 'function') {
                    initializeTableView();
                }
                break;
            case 'param':
                if (typeof initializeParamView === 'function') {
                    initializeParamView();
                }
                break;
            case 'holiday':
                if (typeof initializeHolidayView === 'function') {
                    initializeHolidayView();
                }
                break;
        }
    }
}

