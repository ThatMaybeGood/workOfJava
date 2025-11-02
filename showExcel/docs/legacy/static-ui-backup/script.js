// 应用状态管理
const AppState = {
    currentView: 'table',
    currentParams: {
        targetIncome: 100000,
        warningThreshold: 80,
        dataRetentionDays: 90,
        autoBackup: true
    },
    holidays: [],
    currentCalendarDate: new Date(),
    tableData: []
};

// 页面初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

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

// 加载主内容区域
async function loadMainContent() {
    try {
        const response = await fetch('views/main-content.html');
        const html = await response.text();
        document.getElementById('mainContent').innerHTML = html;

        // 默认显示表格视图
        switchView('table');
    } catch (error) {
        console.error('加载主内容失败:', error);
        // 备用方案：直接显示表格视图
        showErrorView();
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
                initializeTableView();
                break;
            case 'param':
                initializeParamView();
                break;
            case 'holiday':
                initializeHolidayView();
                break;
        }
    }
}

// 表格视图初始化
function initializeTableView() {
    loadCashStatistics();

    // 绑定表格视图的事件
    const printBtn = document.getElementById('printBtn');
    const exportExcelBtn = document.getElementById('exportExcelBtn');
    const exportPdfBtn = document.getElementById('exportPdfBtn');

    if (printBtn) printBtn.addEventListener('click', handlePrint);
    if (exportExcelBtn) exportExcelBtn.addEventListener('click', handleExportExcel);
    if (exportPdfBtn) exportPdfBtn.addEventListener('click', handleExportPdf);
}

// 参数维护视图初始化
function initializeParamView() {
    loadParams();

    // 绑定参数视图的事件
    const paramForm = document.getElementById('paramForm');
    const resetBtn = document.getElementById('resetParams');

    if (paramForm) paramForm.addEventListener('submit', handleParamSave);
    if (resetBtn) resetBtn.addEventListener('click', handleParamReset);
}

// 节假日维护视图初始化
function initializeHolidayView() {
    loadHolidays();
    renderCalendar();
    updateHolidayList();

    // 绑定节假日视图的事件
    const saveBtn = document.getElementById('saveHolidays');
    const prevYearBtn = document.getElementById('prevYear');
    const prevMonthBtn = document.getElementById('prevMonth');
    const nextMonthBtn = document.getElementById('nextMonth');
    const nextYearBtn = document.getElementById('nextYear');

    if (saveBtn) saveBtn.addEventListener('click', handleHolidaySave);
    if (prevYearBtn) prevYearBtn.addEventListener('click', () => navigateCalendar('prevYear'));
    if (prevMonthBtn) prevMonthBtn.addEventListener('click', () => navigateCalendar('prevMonth'));
    if (nextMonthBtn) nextMonthBtn.addEventListener('click', () => navigateCalendar('nextMonth'));
    if (nextYearBtn) nextYearBtn.addEventListener('click', () => navigateCalendar('nextYear'));
}

// 加载初始数据
function loadInitialData() {
    console.log('加载初始数据...');
}

// 更新时间戳
function updateTimestamp() {
    const now = new Date();
    const updateTimeElement = document.getElementById('updateTime');
    if (updateTimeElement) {
        updateTimeElement.textContent = now.toLocaleString('zh-CN');
    }
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



// 表格数据加载和渲染函数 - 修复边框问题
function loadCashStatistics() {
    showLoading();

    fetch('/api/cash-statistics')
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(data => {
            renderTable(data);
            hideLoading();
        })
        .catch(error => {
            console.error('加载数据失败:', error);
            showErrorMessage('数据加载失败: ' + error.message);
            hideLoading();
        });
}
// async function loadCashStatistics() {
//     try {
//         const apiUrl = window.location.hostname === 'localhost'
//             ? 'http://localhost:8080/api/cash-statistics'
//             : '/api/cash-statistics';
//
//         console.log('正在请求API:', apiUrl);
//
//         const response = await fetch(apiUrl);
//
//         if (!response.ok) {
//             throw new Error(`HTTP错误! 状态: ${response.status}`);
//         }
//
//         const result = await response.json();
//
//         // 重要修正：后端返回的是数组，取第一个元素
//         const tableData = Array.isArray(result) ? result[0] : result;
//
//         if (!tableData) {
//             throw new Error('数据格式错误：未找到表格数据');
//         }
//
//         console.log('API响应数据:', tableData);
//         renderTable(tableData);
//         hideLoading();
//     } catch (error) {
//         console.error('获取数据失败:', error);
//         showErrorMessage('数据加载失败: ' + error.message);
//         hideLoading();
//
//         // 使用您提供的实际数据进行回退
//         // const fallbackData = {
//         //     "headers": ["序号","名称","预交金收入","医疗收入","挂号收入","应交报表数","前日暂收款","实交报表数","当日暂收款","实收现金数","留存数差额","留存现金数","备用金","备注"],
//         //     "rows": [
//         //         {"rowType":0,"data":{"id":1,"tableType":0,"name":"王玉莹","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":0},
//         //         {"rowType":0,"data":{"id":2,"tableType":0,"name":"吕笳熙","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":1},
//         //         {"rowType":0,"data":{"id":3,"tableType":0,"name":"陈燕","hisAdvancePayment":0.0,"hisMedicalIncome":20.2,"hisRegistrationIncome":9.08,"reportAmount":10.0,"previousTemporaryReceipt":1.0,"actualReportAmount":0.0,"currentTemporaryReceipt":10.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":2},
//         //         {"rowType":0,"data":{"id":4,"tableType":0,"name":"袁贤梅","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":3},
//         //         {"rowType":0,"data":{"id":5,"tableType":0,"name":"陈静","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":4},
//         //         {"rowType":0,"data":{"id":6,"tableType":0,"name":"刘曼","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":50.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":20.0,"pettyCash":0.0},"rowIndex":5},
//         //         {"rowType":0,"data":{"id":7,"tableType":0,"name":"贺揄佳","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":6},
//         //         {"rowType":0,"data":{"id":8,"tableType":0,"name":"李春香","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":10.0},"rowIndex":7},
//         //         {"rowType":0,"data":{"id":9,"tableType":0,"name":"卢语","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":8},
//         //         {"rowType":0,"data":{"id":10,"tableType":0,"name":"孔庆霞","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":200.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":300.0,"pettyCash":300.0},"rowIndex":9},
//         //         {"rowType":0,"data":{"id":11,"tableType":0,"name":"李家莲","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":10},
//         //         {"rowType":0,"data":{"id":12,"tableType":0,"name":"聂佳","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":11},
//         //         {"rowType":0,"data":{"id":13,"tableType":0,"name":"胡梦潭","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":12},
//         //         {"rowType":0,"data":{"id":14,"tableType":0,"name":"杨红","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":13},
//         //         {"rowType":0,"data":{"id":15,"tableType":0,"name":"甘玉佳","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":20.0,"pettyCash":0.0},"rowIndex":14},
//         //         {"rowType":1,"data":{"id":16,"tableType":1,"name":"田雪琴","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":16},
//         //         {"rowType":1,"data":{"id":17,"tableType":1,"name":"卢枕枕","hisAdvancePayment":20.0,"hisMedicalIncome":5.0,"hisRegistrationIncome":10.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":17},
//         //         {"rowType":1,"data":{"id":18,"tableType":1,"name":"杨文佳","hisAdvancePayment":0.0,"hisMedicalIncome":10.0,"hisRegistrationIncome":0.0,"reportAmount":10.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":18},
//         //         {"rowType":1,"data":{"id":19,"tableType":1,"name":"黄玉明","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":19},
//         //         {"rowType":1,"data":{"id":20,"tableType":1,"name":"靳东","hisAdvancePayment":0.0,"hisMedicalIncome":0.0,"hisRegistrationIncome":0.0,"reportAmount":0.0,"previousTemporaryReceipt":0.0,"actualReportAmount":0.0,"currentTemporaryReceipt":0.0,"actualCashAmount":0.0,"retainedDifference":0.0,"retainedCash":0.0,"pettyCash":0.0},"rowIndex":20}
//         //     ],
//         //     "mergeConfigs":[
//         //         {"startRow":15,"startCol":0,"rowSpan":1,"colSpan":13,"content":"名字第二段"},
//         //         {"startRow":16,"startCol":11,"rowSpan":3,"colSpan":2,"content":"合并内容1"},
//         //         // {"startRow":21,"startCol":11,"rowSpan":3,"colSpan":5,"content":"合并内容2"}
//         //     ],
//         //     "title":"模拟现金统计表"
//         // };
//
//         // console.log('使用回退数据');
//
//     }
// }







// 显示错误信息
function showErrorMessage(message) {
    const tbody = document.getElementById('tableBody');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="14" style="text-align: center; color: #d32f2f; padding: 40px;">
                    <i class="fas fa-exclamation-triangle" style="font-size: 24px; margin-bottom: 10px; display: block;"></i>
                    <div>${message}</div>
                    <button class="action-btn" onclick="loadCashStatistics()" style="margin-top: 15px;">
                        <i class="fas fa-redo"></i> 重新加载
                    </button>
                </td>
            </tr>
        `;
    }
}


function renderTable(data) {
    const container = document.getElementById('table-container');
    container.innerHTML = '';

    const table = document.createElement('table');
    table.className = 'cash-table';
    table.id = 'cash-statistics-table';

    // 1. 创建标题行（合并整行）
    const titleRow = document.createElement('tr');
    const titleCell = document.createElement('th');
    titleCell.textContent = data.title;
    titleCell.colSpan = data.headers.length;
    titleCell.className = 'title-row';
    titleRow.appendChild(titleCell);
    table.appendChild(titleRow);

    // 2. 创建表头行
    const headerRow = document.createElement('tr');
    data.headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        headerRow.appendChild(th);
    });
    table.appendChild(headerRow);

    // 3. 创建数据行容器
    const tbody = document.createElement('tbody');

    // 计算需要的总行数 - 修正：考虑合并配置可能需要的额外行
    const maxRowIndex = Math.max(...data.rows.map(row => row.rowIndex));
    const maxMergeRow = Math.max(...data.mergeConfigs.map(merge => merge.startRow + merge.rowSpan - 1), 0);
    const totalRows = Math.max(maxRowIndex, maxMergeRow) + 1;

    console.log('需要创建的数据行数:', totalRows, 'maxRowIndex:', maxRowIndex, 'maxMergeRow:', maxMergeRow);

    // 创建所有数据行框架
    for (let i = 0; i < totalRows; i++) {
        const row = document.createElement('tr');
        row.className = `data-row row-type-default`;
        row.dataset.originalIndex = i;

        // 创建所有单元格
        for (let j = 0; j < data.headers.length; j++) {
            const cell = document.createElement('td');
            cell.textContent = ''; // 初始为空
            cell.dataset.colIndex = j;
            row.appendChild(cell);
        }

        tbody.appendChild(row);
    }

    table.appendChild(tbody);
    container.appendChild(table);

    // 4. 填充数据
    fillTableData(table, data);

    // 5. 应用合并单元格
    applyCellMerges(table, data.mergeConfigs);

    console.log('表格渲染完成');
}

function fillTableData(table, data) {
    console.log('开始填充数据...');

    // 按rowIndex排序
    const sortedRows = [...data.rows].sort((a, b) => a.rowIndex - b.rowIndex);

    sortedRows.forEach(rowData => {
        const rowIndex = rowData.rowIndex;
        const rowDataObj = rowData.data;
        const rowType = rowData.rowType;

        // +2 因为前面有标题行和表头行
        const tableRow = table.rows[rowIndex + 2];

        if (tableRow) {
            // 设置行样式
            tableRow.className = `data-row row-type-${rowType}`;

            // 填充序号
            tableRow.cells[0].textContent = rowIndex + 1;

            // 填充名称
            tableRow.cells[1].textContent = rowDataObj.name || '';

            // 填充数值字段
            const numericFields = [
                'hisAdvancePayment', 'hisMedicalIncome', 'hisRegistrationIncome',
                'reportAmount', 'previousTemporaryReceipt', 'actualReportAmount',
                'currentTemporaryReceipt', 'actualCashAmount', 'retainedDifference',
                'retainedCash', 'pettyCash'
            ];

            numericFields.forEach((field, index) => {
                const value = rowDataObj[formatNumber(field)];
                if (value !== null && value !== undefined) {
                    tableRow.cells[index + 2].textContent = formatValue(value);
                }
            });

            console.log(`填充第${rowIndex}行: ${rowDataObj.name}`);
        } else {
            console.warn(`找不到第${rowIndex}行`);
        }
    });
}

function applyCellMerges(table, mergeConfigs) {
    console.log('开始应用合并单元格...');

    mergeConfigs.forEach(config => {
        // +2 因为前面有标题行和表头行
        const startRowIndex = config.startRow + 2;
        const startColIndex = config.startCol;

        console.log(`处理合并: 行${startRowIndex}, 列${startColIndex}, 跨度${config.rowSpan}x${config.colSpan}`);

        const startRow = table.rows[startRowIndex];
        if (!startRow) {
            console.warn(`找不到起始行: ${startRowIndex}`);
            return;
        }

        const startCell = startRow.cells[startColIndex];
        if (!startCell) {
            console.warn(`找不到起始单元格: 列${startColIndex}`);
            return;
        }

        // 设置合并属性
        startCell.rowSpan = config.rowSpan;
        startCell.colSpan = config.colSpan;
        startCell.className = 'merged-cell';
        startCell.textContent = config.content || '';

        // 移除被合并的单元格
        for (let r = startRowIndex; r < startRowIndex + config.rowSpan; r++) {
            const row = table.rows[r];
            if (!row) continue;

            let cellsRemoved = 0;
            for (let c = startColIndex; c < startColIndex + config.colSpan; c++) {
                if (r === startRowIndex && c === startColIndex) {
                    continue; // 跳过起始单元格
                }

                const cellIndex = c - cellsRemoved;
                if (row.cells[cellIndex]) {
                    row.removeChild(row.cells[cellIndex]);
                    cellsRemoved++;
                }
            }
        }

        console.log(`成功合并单元格`);
    });
}

function formatValue(value) {
    if (typeof value === 'number') {
        return value.toFixed(2);
    }
    return value || '';
}


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

// 添加自动刷新功能（可选）
function startAutoRefresh(interval = 300000) { // 默认5分钟刷新一次
    setInterval(() => {
        if (AppState.currentView === 'table') {
            loadCashStatistics();
        }
    }, interval);
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

// 节假日管理函数
async function loadHolidays() {
    try {
        // 模拟API调用
        const holidays = ['2024-01-01', '2024-02-10', '2024-02-11', '2024-02-12'];
        AppState.holidays = holidays;
    } catch (error) {
        console.error('加载节假日失败:', error);
        AppState.holidays = [];
    }
}

async function saveHolidays() {
    showLoading();

    try {
        // 准备请求数据
        const holidaysData = AppState.holidays.map(date => ({
            date,
            type: 'PUBLIC' // 默认公共假期类型
        }));

        const requestData = holidaysData.map(item => ({
            holidayDate: item.date,
            holidayName: '自定义节假日',
            isHoliday: true,
            holidayType: item.type,
            year: new Date(item.date).getFullYear(),
            description: '通过前端设置的节假日'
        }));

        // 打印详细的请求参数
        console.group('API请求详情');
        console.log('请求URL:', '/api/holiday/batch-update');
        console.log('请求方法:', 'POST');
        console.log('请求头:', {
            'Content-Type': 'application/json'
        });
        console.log('请求体:', requestData);
        console.log('节假日数量:', holidaysData.length);
        console.groupEnd();

        const response = await fetch('/api/holiday/batch-update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        // 打印响应详情
        console.group('API响应详情');
        console.log('响应状态:', response.status);
        console.log('响应状态文本:', response.statusText);
        console.log('响应头:', Object.fromEntries(response.headers.entries()));

        if (!response.ok) {
            const errorText = await response.text();
            console.error('响应错误:', errorText);
            throw new Error(errorText);
        }

        const result = await response.json();
        console.log('响应结果:', result);
        console.groupEnd();

        if (result.success) {
            hideLoading();
            alert('节假日设置保存成功！');
        } else {
            throw new Error(result.message || '保存失败');
        }
    } catch (error) {
        console.error('保存失败:', error);
        console.error('错误堆栈:', error.stack);
        hideLoading();
        alert('节假日设置保存失败: ' + error.message);
    }
}

function renderCalendar() {
    const calendar = document.getElementById('calendar');
    const currentMonthYear = document.getElementById('currentMonthYear');

    if (!calendar || !currentMonthYear) return;

    const year = AppState.currentCalendarDate.getFullYear();
    const month = AppState.currentCalendarDate.getMonth();

    // 设置当前月份显示
    currentMonthYear.textContent = `${year}年${month + 1}月`;

    // 清空日历
    calendar.innerHTML = '';

    // 获取当月第一天和最后一天
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    // 获取当月第一天是星期几（0是周日）
    const firstDayOfWeek = firstDay.getDay();

    // 获取上个月的最后几天
    const prevMonthLastDay = new Date(year, month, 0).getDate();

    // 生成日历
    let dayCount = 0;

    // 上个月的日期
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
        const dayElement = createCalendarDay(prevMonthLastDay - i, 'other-month');
        calendar.appendChild(dayElement);
        dayCount++;
    }

    // 当月的日期
    for (let day = 1; day <= lastDay.getDate(); day++) {
        const dateStr = `${year}-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
        const isHoliday = AppState.holidays.includes(dateStr);
        const isWeekend = [0, 6].includes(new Date(year, month, day).getDay());

        let className = 'calendar-day';
        if (isWeekend) className += ' weekend';
        if (isHoliday) className += ' holiday';

        const dayElement = createCalendarDay(day, className, dateStr);
        calendar.appendChild(dayElement);
        dayCount++;
    }

    // 下个月的日期
    let nextMonthDay = 1;
    while (dayCount < 42) {
        const dayElement = createCalendarDay(nextMonthDay, 'other-month');
        calendar.appendChild(dayElement);
        nextMonthDay++;
        dayCount++;
    }
}

function createCalendarDay(day, className, dateStr = null) {
    const dayElement = document.createElement('div');
    dayElement.className = className;
    dayElement.textContent = day;

    if (dateStr) {
        dayElement.addEventListener('click', function() {
            toggleHoliday(dateStr, dayElement);
        });
    }

    return dayElement;
}

function toggleHoliday(dateStr, element) {
    const index = AppState.holidays.indexOf(dateStr);

    if (index > -1) {
        // 如果是节假日，移除
        AppState.holidays.splice(index, 1);
        element.classList.remove('holiday');
    } else {
        // 如果不是节假日，添加
        AppState.holidays.push(dateStr);
        element.classList.add('holiday');
    }

    updateHolidayList();
}

function updateHolidayList() {
    const holidayList = document.getElementById('holidayList');
    if (!holidayList) return;

    if (AppState.holidays.length === 0) {
        holidayList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-calendar-plus"></i>
                <p>暂无节假日设置</p>
                <span>点击日历中的日期来设置或取消节假日</span>
            </div>
        `;
        return;
    }

    holidayList.innerHTML = AppState.holidays
        .sort()
        .map(date => {
            const dateObj = new Date(date);
            const formattedDate = `${dateObj.getFullYear()}年${dateObj.getMonth() + 1}月${dateObj.getDate()}日`;
            return `
                <div class="holiday-item">
                    <span class="holiday-date">${formattedDate}</span>
                    <span class="holiday-remove" onclick="removeHoliday('${date}')">
                        <i class="fas fa-times"></i>
                    </span>
                </div>
            `;
        })
        .join('');
}

function removeHoliday(dateStr) {
    const index = AppState.holidays.indexOf(dateStr);
    if (index > -1) {
        AppState.holidays.splice(index, 1);
        updateHolidayList();
        renderCalendar(); // 重新渲染日历以更新状态
    }
}

function navigateCalendar(direction) {
    switch (direction) {
        case 'prevYear':
            AppState.currentCalendarDate.setFullYear(AppState.currentCalendarDate.getFullYear() - 1);
            break;
        case 'prevMonth':
            AppState.currentCalendarDate.setMonth(AppState.currentCalendarDate.getMonth() - 1);
            break;
        case 'nextMonth':
            AppState.currentCalendarDate.setMonth(AppState.currentCalendarDate.getMonth() + 1);
            break;
        case 'nextYear':
            AppState.currentCalendarDate.setFullYear(AppState.currentCalendarDate.getFullYear() + 1);
            break;
    }
    renderCalendar();
}

// 事件处理函数
function handlePrint() {
    showLoading();
    setTimeout(() => {
        window.print();
        hideLoading();
    }, 500);
}

function handleExportExcel() {
    showLoading();

    try {
        const table = document.getElementById('cashTable');
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.table_to_sheet(table);

        const colWidths = [
            {wch: 8}, {wch: 10}, {wch: 12}, {wch: 12}, {wch: 12},
            {wch: 12}, {wch: 12}, {wch: 12}, {wch: 12}, {wch: 12},
            {wch: 12}, {wch: 12}, {wch: 10}, {wch: 15}
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, '门诊现金统计表');
        const fileName = `门诊现金总统计表_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, fileName);
    } catch (error) {
        console.error('导出Excel失败:', error);
        alert('导出Excel失败，请重试或联系管理员。');
    } finally {
        hideLoading();
    }
}

function handleExportPdf() {
    showLoading();

    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF({
            orientation: 'landscape',
            unit: 'mm',
            format: 'a4'
        });

        doc.setFont('times');
        doc.setFontSize(16);

        const title = '门诊现金总统计表';
        const pageWidth = doc.internal.pageSize.width;
        doc.text(title, pageWidth / 2, 15, { align: 'center' });

        const table = document.getElementById('cashTable');
        const headers = [];
        const body = [];

        const headerRow = table.querySelector('thead tr');
        const headerCells = headerRow.querySelectorAll('th');
        const head = [];
        headerCells.forEach(cell => {
            head.push(cell.textContent.trim());
        });

        const dataRows = table.querySelectorAll('tbody tr');
        dataRows.forEach(row => {
            const rowData = [];
            const cells = row.querySelectorAll('td');
            cells.forEach(cell => {
                rowData.push(cell.textContent.trim());
            });
            if (rowData.length > 0) {
                body.push(rowData);
            }
        });

        doc.autoTable({
            head: [head],
            body: body,
            startY: 25,
            styles: {
                fontSize: 7,
                cellPadding: 1,
                overflow: 'linebreak',
                halign: 'center'
            },
            headStyles: {
                fillColor: [76, 175, 80],
                textColor: 255,
                fontStyle: 'bold',
                fontSize: 8
            },
            margin: { top: 25 }
        });

        const fileName = `门诊现金总统计表_${new Date().toISOString().split('T')[0]}.pdf`;
        doc.save(fileName);
    } catch (error) {
        console.error('导出PDF失败:', error);
        alert('PDF导出失败，请使用打印功能或导出Excel。');
    } finally {
        hideLoading();
    }
}

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

function handleHolidaySave() {
    saveHolidays();
}

// 生成动态合并行的函数
function generateDynamicMergedRow(item) {
    // 定义字段顺序（对应表格的列顺序）
    const fields = [
        'id', 'displayName', 'hisAdvancePayment', 'hisMedicalIncome',
        'hisRegistrationIncome', 'reportAmount', 'previousTemporaryReceipt',
        'actualReportAmount', 'currentTemporaryReceipt', 'actualCashAmount',
        'retainedDifference', 'retainedCash', 'pettyCash', 'remarks'
    ];

    let html = '';
    let currentIndex = 0;

    // 遍历所有字段，检测连续的null值
    while (currentIndex < fields.length) {
        const field = fields[currentIndex];
        const value = item[field];

        if (value === null) {
            // 找到连续的null值范围
            let nullCount = 1;
            for (let i = currentIndex + 1; i < fields.length; i++) {
                if (item[fields[i]] === null) {
                    nullCount++;
                } else {
                    break;
                }
            }

            // 如果连续null值超过1个，合并单元格
            if (nullCount > 1) {
                html += `<td colspan="${nullCount}" style="text-align: center; border: 1px solid #e0e0e0;">
                    ${item.displayName || ''}
                </td>`;
                currentIndex += nullCount;
            } else {
                // 单个null值，生成空单元格
                html += '<td style="border: 1px solid #e0e0e0;"></td>';
                currentIndex++;
            }
        } else {
            // 非null值，正常生成单元格
            if (typeof value === 'number') {
                html += `<td style="border: 1px solid #e0e0e0;">${formatNumber(value)}</td>`;
            } else if (typeof value === 'string') {
                html += `<td style="border: 1px solid #e0e0e0;">${value}</td>`;
            } else {
                html += '<td style="border: 1px solid #e0e0e0;"></td>';
            }
            currentIndex++;
        }
    }

    return html;
}

// 生成剩余列的函数
function generateRemainingColumns(startIndex, colspan, allFields, currentItem) {
    let html = '';

    // 计算剩余列数：14 - colspan
    const remainingCols = 14 - colspan;

    // 生成合并列之前的列
    for (let i = 0; i < startIndex; i++) {
        const field = allFields[i];
        const value = currentItem[field];
        if (typeof value === 'number') {
            html += `<td style="border: 1px solid #e0e0e0;">${formatNumber(value)}</td>`;
        } else if (typeof value === 'string') {
            html += `<td style="border: 1px solid #e0e0e0;">${value}</td>`;
        } else {
            html += '<td style="border: 1px solid #e0e0e0;"></td>';
        }
    }

    // 生成合并列
    if (colspan > 1) {
        html += `<td colspan="${colspan}" style="text-align: center; border: 1px solid #e0e0e0;">
            ${currentItem.displayName || ''}
        </td>`;
    }

    // 生成合并列之后的列（如果有）
    const endIndex = startIndex + colspan;
    for (let i = endIndex; i < allFields.length; i++) {
        const field = allFields[i];
        const value = currentItem[field];
        if (i < 14) { // 确保不超过总列数
            if (typeof value === 'number') {
                html += `<td style="border: 1px solid #e0e0e0;">${formatNumber(value)}</td>`;
            } else if (typeof value === 'string') {
                html += `<td style="border: 1px solid #e0e0e0;">${value}</td>`;
            } else {
                html += '<td style="border: 1px solid #e0e0e0;"></td>';
            }
        }
    }

    // 补充剩余的空列以确保总共14列
    const totalGeneratedCols = startIndex + (colspan > 1 ? 1 : 0) + (allFields.length - endIndex);
    for (let i = totalGeneratedCols; i < 14; i++) {
        html += '<td style="border: 1px solid #e0e0e0;"></td>';
    }

    return html;
}

// 工具函数
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

// 全局函数，供HTML调用
window.removeHoliday = removeHoliday;