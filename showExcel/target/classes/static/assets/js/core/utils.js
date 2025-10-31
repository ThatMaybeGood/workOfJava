// 工具函数

// 数字格式化函数
function formatNumber(value) {
    if (value === null || value === undefined || value === 0 || value === 0.0) {
        return '';
    }
    const num = Number(value);
    if (isNaN(num)) {
        return '';
    }
    return num.toFixed(2);
}

// 显示/隐藏加载提示
function showLoading() {
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        loadingElement.style.display = 'block';
    }
}

function hideLoading() {
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        loadingElement.style.display = 'none';
    }
}

// 更新时间戳
function updateTimestamp() {
    const now = new Date();
    const updateTimeElement = document.getElementById('updateTime');
    if (updateTimeElement) {
        updateTimeElement.textContent = now.toLocaleString('zh-CN');
    }
}

// 显示错误视图
function showErrorView() {
    document.getElementById('mainContent').innerHTML = `
        <div class="view-content active">
            <div class="content-header">
                <h2>系统加载失败</h2>
            </div>
            <div class="view-body" style="text-align: center; padding: 50px;">
                <i class="fas fa-exclamation-triangle" style="font-size: 48px; color: #ff9800; margin-bottom: 20px;"></i>
                <h3>无法加载系统界面</h3>
                <p>请检查网络连接或联系系统管理员</p>
                <button class="action-btn primary" onclick="location.reload()" style="margin-top: 20px;">
                    <i class="fas fa-redo"></i> 重新加载
                </button>
            </div>
        </div>
    `;
}

