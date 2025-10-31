// 节假日维护视图功能

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
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000));
        hideLoading();
        alert('节假日设置保存成功！');
    } catch (error) {
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
function handleHolidaySave() {
    saveHolidays();
}

// 全局函数，供HTML调用
window.removeHoliday = removeHoliday;

