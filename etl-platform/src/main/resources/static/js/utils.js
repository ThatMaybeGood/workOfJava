/**
 * Utility functions for ETL Platform frontend
 */

// Show loading overlay
function showLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.style.display = 'flex';
}

// Hide loading overlay
function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.style.display = 'none';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// Format date
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN');
}

// Confirm dialog
function confirmDialog(message) {
    return confirm(message);
}

// Toggle modal
function toggleModal(modalId, show) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = show ? 'flex' : 'none';
    }
}

// Get form data as object
function getFormData(formId) {
    const form = document.getElementById(formId);
    const data = {};
    const elements = form.querySelectorAll('input, select, textarea');
    elements.forEach(el => {
        if (el.name) {
            if (el.type === 'checkbox') {
                data[el.name] = el.checked ? 'Y' : 'N';
            } else {
                data[el.name] = el.value;
            }
        }
    });
    return data;
}

// Set form data from object
function setFormData(formId, data) {
    const form = document.getElementById(formId);
    const elements = form.querySelectorAll('input, select, textarea');
    elements.forEach(el => {
        if (el.name && data[el.name] !== undefined) {
            if (el.type === 'checkbox') {
                el.checked = data[el.name] === 'Y';
            } else {
                el.value = data[el.name] || '';
            }
        }
    });
}

// Clear form
function clearForm(formId) {
    const form = document.getElementById(formId);
    if (form) form.reset();
}

// Render table rows
function renderTable(tbodyId, data, rowRenderer) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    tbody.innerHTML = '';
    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="99" style="text-align:center;color:#999;">暂无数据</td></tr>';
        return;
    }
    data.forEach(item => {
        tbody.appendChild(rowRenderer(item));
    });
}
