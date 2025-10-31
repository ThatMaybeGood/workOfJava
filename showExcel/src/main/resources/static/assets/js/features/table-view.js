// 表格视图功能

const FALLBACK_CASH_TABLE_DATA = {
    title: '模拟现金统计表',
    headers: [
        '序号',
        '名称',
        '预交金收入',
        '医疗收入',
        '挂号收入',
        '应交报表数',
        '前日暂收款',
        '实交报表数',
        '当日暂收款',
        '实收现金数',
        '留存数差额',
        '留存现金数',
        '备用金',
        '备注'
    ],
    rows: [
        {
            rowType: 0,
            rowIndex: 0,
            data: {
                id: 1,
                name: '王玉莹',
                hisAdvancePayment: 0,
                hisMedicalIncome: 18.35,
                hisRegistrationIncome: 9.08,
                reportAmount: 10.0,
                previousTemporaryReceipt: 1.2,
                actualReportAmount: 10.0,
                currentTemporaryReceipt: 8.5,
                actualCashAmount: 5.0,
                retainedDifference: 2.3,
                retainedCash: 20,
                pettyCash: 0,
                remarks: '示例记录'
            }
        },
        {
            rowType: 0,
            rowIndex: 1,
            data: {
                id: 2,
                name: '吕笳熙',
                hisAdvancePayment: 12.5,
                hisMedicalIncome: 0,
                hisRegistrationIncome: 0,
                reportAmount: 12.5,
                previousTemporaryReceipt: 0,
                actualReportAmount: 12.5,
                currentTemporaryReceipt: 0,
                actualCashAmount: 12.5,
                retainedDifference: 0,
                retainedCash: 0,
                pettyCash: 0,
                remarks: ''
            }
        },
        {
            rowType: 0,
            rowIndex: 2,
            data: {
                id: 3,
                name: '陈燕',
                hisAdvancePayment: 0,
                hisMedicalIncome: 20.2,
                hisRegistrationIncome: 9.08,
                reportAmount: 10.0,
                previousTemporaryReceipt: 1.0,
                actualReportAmount: 0,
                currentTemporaryReceipt: 10.0,
                actualCashAmount: 0,
                retainedDifference: 0,
                retainedCash: 0,
                pettyCash: 0,
                remarks: ''
            }
        },
        {
            rowType: 0,
            rowIndex: 3,
            data: {
                id: 4,
                name: '会计室合计',
                hisAdvancePayment: 32.7,
                hisMedicalIncome: 38.55,
                hisRegistrationIncome: 18.16,
                reportAmount: 32.5,
                previousTemporaryReceipt: 2.2,
                actualReportAmount: 22.5,
                currentTemporaryReceipt: 18.5,
                actualCashAmount: 17.5,
                retainedDifference: 4.6,
                retainedCash: 20,
                pettyCash: 0,
                remarks: ''
            }
        },
        {
            rowType: 0,
            rowIndex: 5,
            data: {
                id: 5,
                name: '预约A岗',
                hisAdvancePayment: 5,
                hisMedicalIncome: 10,
                hisRegistrationIncome: 5,
                reportAmount: 8,
                previousTemporaryReceipt: 0,
                actualReportAmount: 8,
                currentTemporaryReceipt: 0,
                actualCashAmount: 8,
                retainedDifference: 0,
                retainedCash: 0,
                pettyCash: 0,
                remarks: ''
            }
        },
        {
            rowType: 1,
            rowIndex: 6,
            data: {
                id: 6,
                name: '预约B岗',
                hisAdvancePayment: 8,
                hisMedicalIncome: 9,
                hisRegistrationIncome: 4,
                reportAmount: 10,
                previousTemporaryReceipt: 0,
                actualReportAmount: 0,
                currentTemporaryReceipt: 10,
                actualCashAmount: 0,
                retainedDifference: 0,
                retainedCash: 0,
                pettyCash: 0,
                remarks: ''
            }
        }
    ],
    mergeConfigs: [
        {
            startRow: 4,
            startCol: 0,
            rowSpan: 1,
            colSpan: 14,
            content: '门诊收费小计'
        },
        {
            startRow: 5,
            startCol: 1,
            rowSpan: 2,
            colSpan: 2,
            content: '预约中心'
        }
    ]
};

// 表格视图初始化
function initializeTableView() {
    loadCashStatistics();

    const printBtn = document.getElementById('printBtn');
    const exportExcelBtn = document.getElementById('exportExcelBtn');
    const exportPdfBtn = document.getElementById('exportPdfBtn');

    if (printBtn) printBtn.addEventListener('click', handlePrint);
    if (exportExcelBtn) exportExcelBtn.addEventListener('click', handleExportExcel);
    if (exportPdfBtn) exportPdfBtn.addEventListener('click', handleExportPdf);
}

async function loadCashStatistics() {
    showLoading();

    try {
        const apiUrl = getCashStatisticsApiUrl();
        const response = await fetch(apiUrl, { cache: 'no-store' });

        if (!response.ok) {
            throw new Error(`网络响应异常: ${response.status}`);
        }

        const payload = await response.json();
        const tableData = Array.isArray(payload) ? payload[0] : payload;

        if (!tableData || !Array.isArray(tableData.headers)) {
            throw new Error('数据格式不正确');
        }

        AppState.tableData = tableData;
        renderCashStatisticsTable(tableData);
    } catch (error) {
        console.error('加载数据失败:', error);
        AppState.tableData = FALLBACK_CASH_TABLE_DATA;
        renderCashStatisticsTable(FALLBACK_CASH_TABLE_DATA, {
            message: '数据加载失败，已展示示例数据',
            messageType: 'warning'
        });
    } finally {
        hideLoading();
    }
}

function getCashStatisticsApiUrl() {
    if (window.location.hostname === 'localhost') {
        return 'http://localhost:8080/api/cash-statistics/new';
    }
    return '/api/cash-statistics/new';
}

function renderCashStatisticsTable(data, options = {}) {
    const container = document.getElementById('cashTableContainer');
    if (!container) {
        return;
    }

    container.innerHTML = '';

    if (options.message) {
        const messageEl = document.createElement('div');
        messageEl.className = `table-message table-message-${options.messageType || 'info'}`;
        messageEl.innerHTML = `<i class="fas fa-info-circle"></i> ${options.message}`;
        container.appendChild(messageEl);
    }

    if (!data || !Array.isArray(data.headers) || data.headers.length === 0) {
        renderTablePlaceholder(container, '暂无数据');
        return;
    }

    const table = document.createElement('table');
    table.className = 'cash-table';
    table.id = 'cashTable';

    const columnCount = data.headers.length;
    const thead = document.createElement('thead');

    const titleRow = document.createElement('tr');
    const titleCell = document.createElement('th');
    titleCell.colSpan = columnCount;
    titleCell.className = 'title-row';
    titleCell.textContent = data.title || '门诊现金总统计表';
    titleRow.appendChild(titleCell);
    thead.appendChild(titleRow);

    const headerRow = document.createElement('tr');
    data.headers.forEach(header => {
        const th = document.createElement('th');
        th.textContent = header;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    const mergeConfigs = Array.isArray(data.mergeConfigs) ? data.mergeConfigs : [];

    const maxRowIndex = Array.isArray(data.rows) && data.rows.length
        ? Math.max(...data.rows.map(row => Number(row.rowIndex)))
        : -1;
    const maxMergeRow = mergeConfigs.length
        ? Math.max(...mergeConfigs.map(cfg => (Number(cfg.startRow) || 0) + (Number(cfg.rowSpan) || 1) - 1))
        : -1;
    const totalRows = Math.max(maxRowIndex, maxMergeRow) + 1;

    for (let i = 0; i < totalRows; i++) {
        const row = document.createElement('tr');
        row.className = 'data-row row-type-default';
        row.dataset.originalIndex = String(i);

        for (let j = 0; j < columnCount; j++) {
            const cell = document.createElement('td');
            cell.dataset.colIndex = String(j);
            row.appendChild(cell);
        }

        tbody.appendChild(row);
    }

    table.appendChild(tbody);
    container.appendChild(table);

    fillCashStatisticsTable(table, data);
    applyCashStatisticsMerges(table, mergeConfigs);
}

function renderTablePlaceholder(container, text) {
    const placeholder = document.createElement('div');
    placeholder.className = 'table-placeholder';
    placeholder.innerHTML = `
        <i class="fas fa-inbox"></i>
        <p>${text}</p>
    `;
    container.appendChild(placeholder);
}

function fillCashStatisticsTable(table, data) {
    if (!Array.isArray(data.rows) || !data.rows.length) {
        return;
    }

    const sortedRows = [...data.rows].sort((a, b) => a.rowIndex - b.rowIndex);
    const columnCount = data.headers.length;

    sortedRows.forEach(rowData => {
        const rowIndex = Number(rowData.rowIndex);
        if (Number.isNaN(rowIndex)) {
            return;
        }

        const tableRow = table.rows[rowIndex + 2];
        if (!tableRow) {
            return;
        }

        const rowType = rowData.rowType !== undefined ? rowData.rowType : 'default';
        tableRow.className = `data-row row-type-${rowType}`;

        const rowValues = rowData.data || {};

        if (tableRow.cells[0]) {
            tableRow.cells[0].textContent = String(rowIndex + 1);
        }

        if (tableRow.cells[1]) {
            tableRow.cells[1].textContent = rowValues.name || '';
        }

        const numericFields = [
            'hisAdvancePayment',
            'hisMedicalIncome',
            'hisRegistrationIncome',
            'reportAmount',
            'previousTemporaryReceipt',
            'actualReportAmount',
            'currentTemporaryReceipt',
            'actualCashAmount',
            'retainedDifference',
            'retainedCash',
            'pettyCash'
        ];

        numericFields.forEach((field, index) => {
            const cellIndex = index + 2;
            if (cellIndex >= columnCount - 1) {
                return;
            }
            const cell = tableRow.cells[cellIndex];
            if (!cell) {
                return;
            }
            cell.textContent = formatTableValue(rowValues[field]);
        });

        const remarksCell = tableRow.cells[columnCount - 1];
        if (remarksCell) {
            remarksCell.textContent = rowValues.remarks || '';
        }
    });
}

function applyCashStatisticsMerges(table, mergeConfigs) {
    if (!mergeConfigs.length) {
        return;
    }

    mergeConfigs.forEach(config => {
        const startRowIndex = Number(config.startRow);
        const startColIndex = Number(config.startCol);
        const rowSpan = Number(config.rowSpan) || 1;
        const colSpan = Number(config.colSpan) || 1;

        if (Number.isNaN(startRowIndex) || Number.isNaN(startColIndex)) {
            return;
        }

        const targetRow = table.rows[startRowIndex + 2];
        if (!targetRow) {
            console.warn('找不到合并起始行:', startRowIndex);
            return;
        }

        const startCell = targetRow.cells[startColIndex];
        if (!startCell) {
            console.warn('找不到合并起始单元格:', startColIndex);
            return;
        }

        startCell.rowSpan = rowSpan;
        startCell.colSpan = colSpan;
        startCell.classList.add('merged-cell');

        if (config.content) {
            startCell.textContent = config.content;
        }

        for (let r = startRowIndex; r < startRowIndex + rowSpan; r++) {
            const currentRow = table.rows[r + 2];
            if (!currentRow) {
                continue;
            }

            let removed = 0;
            for (let c = startColIndex; c < startColIndex + colSpan; c++) {
                if (r === startRowIndex && c === startColIndex) {
                    continue;
                }

                const removeIndex = c - removed;
                const cellToRemove = currentRow.cells[removeIndex];
                if (cellToRemove) {
                    currentRow.removeChild(cellToRemove);
                    removed++;
                }
            }
        }
    });
}

function formatTableValue(value) {
    if (value === null || value === undefined) {
        return '';
    }
    if (typeof value === 'number') {
        return value.toFixed(2);
    }
    return String(value);
}

// 添加自动刷新功能（可选）
function startAutoRefresh(interval = 300000) {
    setInterval(() => {
        if (AppState.currentView === 'table') {
            loadCashStatistics();
        }
    }, interval);
}


