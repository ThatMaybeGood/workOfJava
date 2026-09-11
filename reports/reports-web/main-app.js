/**
 * 报表系统首页 - 主导航逻辑
 */

// ==================== 菜单配置 ====================
const menuConfig = [
    {
        group: '门诊报表',
        icon: 'bi-hospital-fill',
        items: [
            { label: '门诊运行数据统计', file: 'outpatient/outpatient-operation.html', icon: 'bi-graph-up' },
            { label: '门诊收入分析',     file: 'outpatient/outpatient-revenue.html',     icon: 'bi-currency-yen' },
            { label: '患者画像分析',     file: 'outpatient/outpatient-patient-portrait.html', icon: 'bi-person-badge' },
            { label: '人工窗口统计',     file: 'outpatient/outpatient-window-stats.html',   icon: 'bi-shop' },
            { label: '检验统计',         file: 'outpatient/outpatient-lab-stats.html',      icon: 'bi-beaker' },
            { label: '医技统计',         file: 'outpatient/outpatient-med-tech.html',       icon: 'bi-mortarboard' },
            { label: '服务质量分析',     file: 'outpatient/outpatient-service-quality.html', icon: 'bi-award' },
            { label: '质量控制',         file: 'outpatient/outpatient-quality-control.html', icon: 'bi-clipboard-check' },
            { label: '互医质控运营月报', file: 'outpatient/outpatient-internet-hospital.html', icon: 'bi-clipboard-data' },
            { label: '门诊预警统计',     file: 'outpatient/outpatient-alert.html',          icon: 'bi-bell-fill' },
            { label: '诊室使用率',       file: 'outpatient/outpatient-room-usage.html',     icon: 'bi-door-open' },
            { label: '专科治疗量',       file: 'outpatient/outpatient-specialty-treatment.html', icon: 'bi-activity' },
            { label: '预测门诊量',       file: 'outpatient/outpatient-forecast.html',       icon: 'bi-graph-down' },
            { label: '爽约退号分析',     file: 'outpatient/outpatient-no-show.html',        icon: 'bi-x-circle-fill' },
        ]
    },
    {
        group: '住院报表',
        icon: 'bi-hospital',
        items: [
            { label: '出院结算报表',     file: 'cash/cash-discharge-settlement.html',     icon: 'bi-file-medical' },
            { label: '收费员结账统计',   file: 'cash/cash-cashier-settlement.html',       icon: 'bi-cash-stack' },
            { label: '住院预交金统计',   file: 'cash/cash-inpatient-prepayment.html',     icon: 'bi-wallet2' },
            { label: '门诊财务报表',     file: 'cash/cash-outpatient-finance.html',       icon: 'bi-file-earmark-bar-graph' },
        ]
    }
];

// ==================== 渲染菜单 ====================
function renderMenu() {
    const nav = document.getElementById('sidebarNav');
    let html = '';
    menuConfig.forEach(group => {
        html += `<div class="menu-group">
            <div class="menu-group-title"><i class="bi ${group.icon}"></i> ${group.group}</div>`;
        group.items.forEach(item => {
            html += `<div class="menu-item" data-file="${item.file}" data-label="${item.label}">
                <i class="bi ${item.icon}"></i>
                <span class="menu-text">${item.label}</span>
            </div>`;
        });
        html += `</div>`;
    });
    nav.innerHTML = html;

    nav.querySelectorAll('.menu-item').forEach(el => {
        el.addEventListener('click', () => openPage(el.dataset.file, el.dataset.label));
    });
}

// ==================== 切换页面 ====================
function openPage(file, label) {
    // 更新标题
    document.getElementById('pageTitle').textContent = label;

    // 更新高亮
    document.querySelectorAll('.menu-item').forEach(el => el.classList.remove('active'));
    document.querySelector(`.menu-item[data-file="${file}"]`)?.classList.add('active');

    // 切换 iframe
    const iframe = document.getElementById('contentFrame');
    iframe.src = file;

    // 移动端自动收起侧边栏
    if (window.innerWidth <= 768) {
        document.getElementById('sidebar').classList.add('collapsed');
    }
}

// ==================== 侧边栏折叠 ====================
document.getElementById('sidebarToggle').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('collapsed');
});

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', () => {
    renderMenu();

    // 默认打开第一个页面
    const defaultFile = menuConfig[0].items[0].file;
    const defaultLabel = menuConfig[0].items[0].label;
    openPage(defaultFile, defaultLabel);
});
