// 参数维护视图功能

// 参数维护视图初始化
function initializeParamView() {
    loadParams();

    // 绑定参数视图的事件
    const paramForm = document.getElementById('paramForm');
    const resetBtn = document.getElementById('resetParams');

    if (paramForm) paramForm.addEventListener('submit', handleParamSave);
    if (resetBtn) resetBtn.addEventListener('click', handleParamReset);
}

// 参数管理函数
async function loadParams() {
    try {
        // 模拟API调用
        const params = {
            targetIncome: 150000,
            warningThreshold: 85,
            dataRetentionDays: 120,
            autoBackup: true
        };

        AppState.currentParams = params;
        updateParamDisplay(params);
        populateParamForm(params);
    } catch (error) {
        console.error('加载参数失败:', error);
        updateParamDisplay(AppState.currentParams);
        populateParamForm(AppState.currentParams);
    }
}

function updateParamDisplay(params) {
    const targetIncomeEl = document.getElementById('currentTargetIncome');
    const warningThresholdEl = document.getElementById('currentWarningThreshold');
    const dataRetentionEl = document.getElementById('currentDataRetention');
    const autoBackupEl = document.getElementById('currentAutoBackup');

    if (targetIncomeEl) targetIncomeEl.textContent = `¥${params.targetIncome.toLocaleString()}`;
    if (warningThresholdEl) warningThresholdEl.textContent = `${params.warningThreshold}%`;
    if (dataRetentionEl) dataRetentionEl.textContent = `${params.dataRetentionDays}天`;
    if (autoBackupEl) autoBackupEl.textContent = params.autoBackup ? '开启' : '关闭';
}

function populateParamForm(params) {
    const targetIncome = document.getElementById('targetIncome');
    const warningThreshold = document.getElementById('warningThreshold');
    const dataRetentionDays = document.getElementById('dataRetentionDays');
    const autoBackup = document.getElementById('autoBackup');

    if (targetIncome) targetIncome.value = params.targetIncome;
    if (warningThreshold) warningThreshold.value = params.warningThreshold;
    if (dataRetentionDays) dataRetentionDays.value = params.dataRetentionDays;
    if (autoBackup) autoBackup.value = params.autoBackup.toString();
}

async function saveParams(params) {
    showLoading();

    try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000));

        AppState.currentParams = params;
        updateParamDisplay(params);
        hideLoading();
        alert('参数保存成功！');
    } catch (error) {
        hideLoading();
        alert('参数保存失败: ' + error.message);
    }
}

// 事件处理函数
function handleParamSave(e) {
    e.preventDefault();

    const formData = {
        targetIncome: parseFloat(document.getElementById('targetIncome').value),
        warningThreshold: parseFloat(document.getElementById('warningThreshold').value),
        dataRetentionDays: parseInt(document.getElementById('dataRetentionDays').value),
        autoBackup: document.getElementById('autoBackup').value === 'true'
    };

    saveParams(formData);
}

function handleParamReset() {
    if (confirm('确定要恢复默认参数吗？')) {
        const defaultParams = {
            targetIncome: 100000,
            warningThreshold: 80,
            dataRetentionDays: 90,
            autoBackup: true
        };
        populateParamForm(defaultParams);
    }
}

