// 导出功能处理

// 打印处理
function handlePrint() {
    showLoading();
    setTimeout(() => {
        window.print();
        hideLoading();
    }, 500);
}

// Excel导出处理
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

// PDF导出处理
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
        const body = [];

        const headerRows = table.querySelectorAll('thead tr');
        const headerRow = headerRows.length ? headerRows[headerRows.length - 1] : null;
        const headerCells = headerRow ? headerRow.querySelectorAll('th') : [];
        const head = [];
        headerCells.forEach(cell => head.push(cell.textContent.trim()));

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

