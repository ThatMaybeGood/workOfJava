/**
 * 时间范围快捷按钮对应的日期计算工具
 * 返回 yyyy-MM-dd 格式的起止日期
 */
function getDateRangeByTimeRange(timeRange) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const startDate = new Date(today);
    const endDate = new Date(today);

    switch (timeRange) {
        case 'lastMonth':
            startDate.setDate(today.getDate() - 30);
            break;
        case 'lastWeek':
            startDate.setDate(today.getDate() - 7);
            break;
        case 'yesterday':
            startDate.setDate(today.getDate() - 1);
            endDate.setDate(today.getDate() - 1);
            break;
        case 'today':
        default:
            break;
    }

    const formatDate = (date) => {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    };

    return {
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
    };
}

/**
 * 将 yyyy-MM-dd 转换为 flatpickr 使用的 Y/m/d 格式
 */
function toFlatpickrDate(dateStr) {
    return dateStr.replace(/-/g, '/');
}
