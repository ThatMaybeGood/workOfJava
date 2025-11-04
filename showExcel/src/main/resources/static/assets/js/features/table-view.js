// 改进版的渲染函数
function renderImprovedCashTable(data) {
    const container = document.getElementById('cashTableContainer');
    container.innerHTML = '';

    // 创建标题
    if (data.title) {
        const titleDiv = document.createElement('div');
        titleDiv.className = 'table-title';
        titleDiv.textContent = data.title;
        container.appendChild(titleDiv);
    }

    // 创建表格
    const table = document.createElement('table');
    table.className = 'cash-statistics-table improved';

    // 创建表头
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    data.headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // 创建表体
    const tbody = document.createElement('tbody');

    // 创建基础行框架
    const totalRows = data.metadata?.totalRows || 19;
    for (let i = 0; i < totalRows; i++) {
        const row = document.createElement('tr');
        row.setAttribute('data-row-index', i);
        data.headers.forEach((_, colIndex) => {
            const cell = document.createElement('td');
            cell.setAttribute('data-col-index', colIndex);
            row.appendChild(cell);
        });
        tbody.appendChild(row);
    }

    table.appendChild(tbody);
    container.appendChild(table);

    // 应用布局
    applyLayoutConfig(table, data.layout);

    // 填充数据
    fillSectionData(table, data.sections);
}

// 应用布局配置
function applyLayoutConfig(table, layout) {
    if (!layout) return;

    const allRows = Array.from(table.querySelectorAll('tr'));

    // 处理区域标题
    if (layout.sectionHeaders) {
        layout.sectionHeaders.forEach(header => {
            const row = allRows[header.row + 1]; // +1 for header row
            if (row) {
                const cell = row.cells[header.col];
                cell.colSpan = header.colSpan;
                cell.textContent = header.content;
                cell.className = `section-header ${header.style}`;
            }
        });
    }

    // 处理汇总标签
    if (layout.summaryRows) {
        layout.summaryRows.forEach(summary => {
            const row = allRows[summary.row + 1];
            if (row) {
                const cell = row.cells[summary.col];
                cell.colSpan = summary.colSpan;
                cell.textContent = summary.content;
                cell.className = `summary-label ${summary.style}`;
            }
        });
    }

    // 处理特殊单元格
    if (layout.specialCells) {
        layout.specialCells.forEach(special => {
            const row = allRows[special.row + 1];
            if (row && row.cells[special.col]) {
                const cell = row.cells[special.col];
                if (special.colSpan) cell.colSpan = special.colSpan;
                cell.textContent = special.content;
                cell.className = `special-cell ${special.style}`;
            }
        });
    }
}

// 填充分区数据
function fillSectionData(table, sections) {
    if (!sections) return;

    const allRows = Array.from(table.querySelectorAll('tbody tr'));

    sections.forEach(section => {
        section.rows.forEach(rowData => {
            const rowIndex = rowData.index;
            const row = allRows[rowIndex];
            if (!row) return;

            // 设置行类型
            if (rowData.type) {
                row.className = `row-${rowData.type} section-${section.type}`;
            }

            // 填充数据
            fillRowData(row, rowData);
        });
    });
}

// 填充行数据
function fillRowData(row, rowData) {
    const cells = row.cells;

    // 序号
    if (cells[0]) cells[0].textContent = rowData.index + 1;

    // 名称
    if (cells[1]) cells[1].textContent = rowData.name;

    // 数值字段
    const data = rowData.data || {};
    const fieldMap = [
        { index: 2, field: 'hisAdvancePayment' },
        { index: 3, field: 'hisMedicalIncome' },
        { index: 4, field: 'hisRegistrationIncome' },
        { index: 5, field: 'reportAmount' },
        { index: 6, field: 'previousTemporaryReceipt' },
        { index: 7, field: 'actualReportAmount' },
        { index: 8, field: 'currentTemporaryReceipt' },
        { index: 9, field: 'actualCashAmount' },
        { index: 10, field: 'retainedDifference' },
        { index: 11, field: 'retainedCash' },
        { index: 12, field: 'pettyCash' }
    ];

    fieldMap.forEach(({ index, field }) => {
        if (cells[index] && data[field] !== undefined) {
            cells[index].textContent = formatNumber(data[field]);
        }
    });

    // 备注
    if (cells[13]) cells[13].textContent = rowData.remarks || '';
}


// 查询按钮事件处理
function setupQueryButton() {
    const queryBtn = document.getElementById('queryBtn');
    const queryDate = document.getElementById('queryDate');

    if (queryBtn && queryDate) {
        // 设置默认日期为今天
        const today = new Date().toISOString().split('T')[0];
        queryDate.value = today;

        // 添加点击事件监听器
        queryBtn.addEventListener('click', function() {
            const selectedDate = queryDate.value;
            if (!selectedDate) {
                alert('请选择日期');
                return;
            }
            console.log('查询按钮点击，日期:', selectedDate);
            loadCashStatisticsByDate(selectedDate);
        });

        console.log('查询按钮事件监听器绑定成功');
    } else {
        console.warn('查询按钮或日期输入框未找到');
    }
}

// 根据日期加载数据的函数
async function loadCashStatisticsByDate(date) {
    try {
        console.log('开始加载日期数据:', date);
        showLoading();
        const response = await fetch(`/api/cash-statistics/date/${date}`);

        // 检查响应状态
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('接收到数据:', data);
        renderCashStatisticsTable(data);
        hideLoading();
    } catch (error) {
        console.error('加载数据失败:', error);
        hideLoading();
        alert('加载数据失败，请稍后重试');
    }
}

// 显示加载状态
function showLoading() {
    const container = document.getElementById('cashTableContainer');
    if (container) {
        container.innerHTML = '<div class="loading">加载中...</div>';
    }
}

// 隐藏加载状态
function hideLoading() {
    // 加载状态会在renderCashStatisticsTable中被清除
}

// 页面加载完成后设置查询按钮
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM加载完成');
    // 等待页面完全加载后设置查询按钮
    setTimeout(setupQueryButton, 100);
});