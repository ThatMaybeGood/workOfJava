
// 渲染现金统计表格 - 修复版本
function renderCashStatisticsTable(data, options = {}) {
    const container = document.getElementById('cashTableContainer');
    if (!container) {
        console.warn('表格容器未找到');
        return;
    }

    // 数据验证
    if (!data || typeof data !== 'object') {
        console.warn('无效的数据格式:', data);
        renderTablePlaceholder(container, '数据格式错误');
        return;
    }

    // 检查必要的数据结构
    if (!Array.isArray(data.headers)) {
        console.warn('headers不存在或不是数组:', data.headers);
        data = {...data, headers: FALLBACK_CASH_TABLE_DATA.headers};
    }

    if (data.headers.length === 0) {
        console.warn('headers为空');
        renderTablePlaceholder(container, '暂无数据');
        return;
    }

    // 清空容器
    container.innerHTML = '';

    // 创建标题
    if (data.title) {
        const titleDiv = document.createElement('div');
        titleDiv.className = 'table-title';
        titleDiv.textContent = data.title;
        titleDiv.style.textAlign = 'center';
        titleDiv.style.fontSize = '18px';
        titleDiv.style.fontWeight = 'bold';
        titleDiv.style.marginBottom = '20px';
        container.appendChild(titleDiv);
    }

    // 创建表格
    const table = document.createElement('table');
    table.className = 'cash-statistics-table';
    table.style.borderCollapse = 'collapse';
    table.style.width = '100%';

    // 获取准确的列数
    const colCount = data.headers.length;
    const rowCount = data.rowCount || 0;

    console.log(`表格尺寸: ${rowCount}行 x ${colCount}列`);

    // 创建表头
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');

    data.headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        th.style.border = '1px solid #ddd';
        th.style.padding = '8px';
        th.style.backgroundColor = '#f5f5f5';
        th.style.textAlign = 'center';
        headerRow.appendChild(th);
    });

    thead.appendChild(headerRow);
    table.appendChild(thead);

    // 创建表体 - 修复：先应用合并配置，再填充数据
    const tbody = document.createElement('tbody');

    // 步骤1: 创建基础表格框架
    for (let i = 0; i < rowCount; i++) {
        const tableRow = document.createElement('tr');
        tableRow.setAttribute('data-row-index', i);

        for (let j = 0; j < colCount; j++) {
            const cell = document.createElement('td');
            cell.textContent = '';
            cell.style.border = '1px solid #ddd';
            cell.style.padding = '6px';
            cell.style.textAlign = 'right';
            cell.setAttribute('data-col-index', j);
            tableRow.appendChild(cell);
        }
        tbody.appendChild(tableRow);
    }

    table.appendChild(tbody);
    container.appendChild(table);

    // 步骤2: 先应用合并配置
    if (data.mergeConfigs && typeof data.mergeConfigs === 'object') {
        console.log('开始应用合并配置...');
        applyMergeConfigsV2(table, data.mergeConfigs, colCount, rowCount);
    }

    // 步骤3: 最后填充数据（避免覆盖合并单元格）
    console.log('开始填充数据...');
    fillTableDataV2(table, data);
}

// 新版合并配置处理 - 修复行索引问题
function applyMergeConfigsV2(table, mergeConfigs, totalColumns, totalRows) {
    if (!table || !mergeConfigs) return;

    console.log('合并配置详情:', mergeConfigs);
    console.log(`表格尺寸: ${totalRows}行 x ${totalColumns}列`);

    const tbody = table.querySelector('tbody');
    const allRows = Array.from(table.querySelectorAll('tr'));
    const headerRowCount = 1; // 只有一行表头

    // 按startRow排序配置
    const sortedConfigs = Object.values(mergeConfigs).sort((a, b) => {
        return (a.startRow || 0) - (b.startRow || 0);
    });

    // 处理每个合并配置
    sortedConfigs.forEach((config, index) => {
        if (!config || typeof config !== 'object') return;

        const dataRowIndex = config.startRow || 0;
        const htmlRowIndex = dataRowIndex + headerRowCount;

        console.log(`处理配置 ${index}: 数据行${dataRowIndex} -> HTML行${htmlRowIndex}, 类型: ${config.style}, 内容: ${config.content}`);

        if (config.style === 1) {
            // 区域标题 - 插入新行
            insertSectionRowV2(table, config, htmlRowIndex, totalColumns);
        } else {
            // 数据合并 - 合并现有单元格
            mergeDataCellsV2(table, config, htmlRowIndex, totalColumns);
        }
    });
}

// 新版区域标题插入 - 修复位置问题
function insertSectionRowV2(table, config, htmlRowIndex, totalColumns) {
    const allRows = Array.from(table.querySelectorAll('tr'));
    const tbody = table.querySelector('tbody');

    // 修正列数
    const colSpan = Math.min(config.colSpan || totalColumns, totalColumns);

    console.log(`插入区域标题: HTML行${htmlRowIndex}, 合并${colSpan}列, 内容: "${config.content}"`);

    // 创建区域标题行
    const sectionRow = document.createElement('tr');
    sectionRow.className = 'section-title-row';
    sectionRow.style.backgroundColor = '#e6f7ff';

    const sectionCell = document.createElement('td');
    sectionCell.colSpan = colSpan;
    sectionCell.textContent = config.content || '';
    sectionCell.style.padding = '8px';
    sectionCell.style.fontWeight = 'bold';
    sectionCell.style.textAlign = 'center';
    sectionCell.style.border = '1px solid #ddd';

    sectionRow.appendChild(sectionCell);

    // 填充剩余列（如果需要）
    if (colSpan < totalColumns) {
        const remainingCols = totalColumns - colSpan;
        for (let i = 0; i < remainingCols; i++) {
            const emptyCell = document.createElement('td');
            emptyCell.style.border = '1px solid #ddd';
            emptyCell.style.padding = '6px';
            sectionRow.appendChild(emptyCell);
        }
    }

    // 插入到正确位置
    if (htmlRowIndex < allRows.length) {
        // 在指定行之前插入
        const referenceRow = allRows[htmlRowIndex];
        referenceRow.parentNode.insertBefore(sectionRow, referenceRow);
        console.log(`成功插入区域标题在行 ${htmlRowIndex} 之前`);
    } else {
        // 添加到表格末尾
        tbody.appendChild(sectionRow);
        console.log(`区域标题添加到表格末尾`);
    }
}

// 新版数据合并 - 修复位置问题
function mergeDataCellsV2(table, config, htmlRowIndex, totalColumns) {
    const allRows = Array.from(table.querySelectorAll('tr'));

    if (htmlRowIndex >= allRows.length) {
        console.warn(`合并配置行索引超出范围: ${htmlRowIndex}, 总行数: ${allRows.length}`);
        return;
    }

    const targetRow = allRows[htmlRowIndex];
    const cells = targetRow.querySelectorAll('td, th');
    const startCol = config.startCol || 0;

    if (startCol >= cells.length) {
        console.warn(`合并配置列索引超出范围: ${startCol}, 总列数: ${cells.length}`);
        return;
    }

    const targetCell = cells[startCol];

    // 应用合并
    if (config.rowSpan > 1) {
        targetCell.rowSpan = config.rowSpan;
        console.log(`设置行合并: ${config.rowSpan}`);
    }

    if (config.colSpan > 1) {
        targetCell.colSpan = config.colSpan;
        console.log(`设置列合并: ${config.colSpan}`);
    }

    // 设置内容
    if (config.content) {
        targetCell.textContent = config.content;
        targetCell.style.fontWeight = 'bold';
        targetCell.style.textAlign = 'center';
        console.log(`设置合并单元格内容: "${config.content}"`);
    }
}

// 新版数据填充 - 修复：跳过合并行和区域标题行
function fillTableDataV2(table, data) {
    const tbody = table.querySelector('tbody');
    if (!tbody) return;

    // 处理数据行
    let rowsArray = [];
    if (data.rows && typeof data.rows === 'object') {
        if (Array.isArray(data.rows)) {
            rowsArray = [...data.rows];
        } else {
            rowsArray = Object.values(data.rows);
        }

        // 按rowIndex排序
        rowsArray.sort((a, b) => {
            const aIdx = a.rowIndex !== undefined ? a.rowIndex : 0;
            const bIdx = b.rowIndex !== undefined ? b.rowIndex : 0;
            return aIdx - bIdx;
        });
    }

    console.log('要填充的数据行数量:', rowsArray.length);

    // 获取所有数据行（排除区域标题行）
    const dataRows = Array.from(tbody.querySelectorAll('tr:not(.section-title-row)'));
    console.log('可用的数据行数量:', dataRows.length);

    // 填充数据
    rowsArray.forEach(rowData => {
        const dataRowIndex = rowData.rowIndex !== undefined ? parseInt(rowData.rowIndex) : -1;

        if (dataRowIndex < 0 || dataRowIndex >= dataRows.length) {
            console.warn(`数据行索引 ${dataRowIndex} 超出范围, 可用行数: ${dataRows.length}`);
            return;
        }

        const targetRow = dataRows[dataRowIndex];
        const rowValues = rowData.data || {};

        console.log(`填充数据行 ${dataRowIndex}:`, rowValues.name || '未命名');

        // 设置行类型
        if (rowData.rowType !== undefined) {
            targetRow.className = `row-type-${rowData.rowType}`;
        }

        // 填充单元格数据
        fillRowCells(targetRow, rowValues);
    });
}

// 填充行单元格数据
function fillRowCells(row, rowValues) {
    const cells = row.querySelectorAll('td');

    // 只填充未被合并的单元格
    cells.forEach((cell, index) => {
        // 跳过已被合并的单元格
        if (cell.style.display === 'none' || cell.colSpan > 1 || cell.rowSpan > 1) {
            return;
        }

        let value = '';

        switch (index) {
            case 0: // 序号
                value = (Array.from(row.parentNode.children).indexOf(row) + 1).toString();
                cell.style.textAlign = 'center';
                break;
            case 1: // 名称
                value = rowValues.name || '';
                cell.style.textAlign = 'left';
                break;
            case 2: // 预交金收入
                value = formatNumber(rowValues.hisAdvancePayment);
                break;
            case 3: // 医疗收入
                value = formatNumber(rowValues.hisMedicalIncome);
                break;
            case 4: // 挂号收入
                value = formatNumber(rowValues.hisRegistrationIncome);
                break;
            case 5: // 应交报表数
                value = formatNumber(rowValues.reportAmount);
                break;
            case 6: // 前日暂收款
                value = formatNumber(rowValues.previousTemporaryReceipt);
                break;
            case 7: // 实交报表数
                value = formatNumber(rowValues.actualReportAmount);
                break;
            case 8: // 当日暂收款
                value = formatNumber(rowValues.currentTemporaryReceipt);
                break;
            case 9: // 实收现金数
                value = formatNumber(rowValues.actualCashAmount);
                break;
            case 10: // 留存数差额
                value = formatNumber(rowValues.retainedDifference);
                break;
            case 11: // 留存现金数
                value = formatNumber(rowValues.retainedCash);
                break;
            case 12: // 备用金
                value = formatNumber(rowValues.pettyCash);
                break;
            case 13: // 备注
                value = rowValues.remarks || '';
                cell.style.textAlign = 'left';
                break;
        }

        cell.textContent = value;
    });
}

// 数字格式化
function formatNumber(value) {
    if (value === undefined || value === null) return '';
    const num = parseFloat(value);
    return isNaN(num) ? '' : num.toFixed(2);
}

// 渲染占位符
function renderTablePlaceholder(container, text) {
    const placeholder = document.createElement('div');
    placeholder.className = 'table-placeholder';
    placeholder.textContent = text;
    placeholder.style.textAlign = 'center';
    placeholder.style.padding = '40px';
    placeholder.style.color = '#666';
    container.appendChild(placeholder);
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