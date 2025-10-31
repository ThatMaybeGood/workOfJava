// 应用初始化

// 加载主内容区域
async function loadMainContent() {
    try {
        const mainContent = document.getElementById('mainContent');
        
        // 并行加载所有视图
        const [tableView, paramView, holidayView] = await Promise.all([
            fetch('./assets/templates/table-view.html').then(r => r.text()),
            fetch('./assets/templates/param-view.html').then(r => r.text()),
            fetch('./assets/templates/holiday-view.html').then(r => r.text())
        ]);
        
        // 组合所有视图
        mainContent.innerHTML = tableView + paramView + holidayView;

        // 默认显示表格视图
        switchView('table');
    } catch (error) {
        console.error('加载主内容失败:', error);
        // 备用方案：直接显示表格视图
        showErrorView();
    }
}

// 初始化事件监听器
function initializeEventListeners() {
    // 使用事件委托处理侧边栏菜单点击
    document.addEventListener('click', function(e) {
        const menuItem = e.target.closest('.sidebar-menu li');
        if (menuItem) {
            const viewType = menuItem.dataset.view;

            // 更新菜单激活状态
            document.querySelectorAll('.sidebar-menu li').forEach(item => {
                item.classList.remove('active');
            });
            menuItem.classList.add('active');

            // 切换视图
            switchView(viewType);
        }
    });

    // 全屏功能
    document.getElementById('fullscreenBtn')?.addEventListener('click', toggleFullscreen);
}

// 全屏切换
function toggleFullscreen() {
    const fullscreenBtn = document.getElementById('fullscreenBtn');

    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen().catch(err => {
            console.error(`全屏请求错误: ${err.message}`);
        });
        fullscreenBtn.innerHTML = '<i class="fas fa-compress"></i> 退出全屏';
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen();
            fullscreenBtn.innerHTML = '<i class="fas fa-expand"></i> 全屏显示';
        }
    }
}

// 加载初始数据
function loadInitialData() {
    console.log('加载初始数据...');
}

// 页面初始化
async function initializeApp() {
    // 加载主内容区域
    await loadMainContent();

    // 初始化事件监听
    initializeEventListeners();

    // 加载初始数据
    loadInitialData();

    // 启动自动刷新（可选）
    // startAutoRefresh(300000); // 5分钟刷新一次

    // 更新时间戳
    updateTimestamp();
    setInterval(updateTimestamp, 60000);
}

