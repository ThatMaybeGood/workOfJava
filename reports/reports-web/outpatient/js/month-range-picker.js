/**
 * 月份范围选择器(与门诊财务报表按月选择同款交互)。
 *
 * 用法:
 *   const picker = new MonthRangePicker(document.getElementById('monthRangePicker'), {
 *       start: '2026-09',            // 初始起始月,格式 YYYY-MM
 *       end: '2026-10',              // 初始结束月
 *       maxMonths: 12,               // 最多跨多少个月,默认12
 *       onConfirm: (start, end) => { // 点完起始月+结束月后回调,格式 YYYY-MM
 *           // 自行转成日期范围(起始月1号 ~ 结束月最后一天)再查询
 *       }
 *   });
 *   picker.setRange('2026-09', '2026-10');  // 外部更新显示
 *   picker.destroy();                        // 切换按天控件时销毁
 */
class MonthRangePicker {
    constructor(container, options) {
        this.container = container;
        this.maxMonths = options.maxMonths || 12;
        this.onConfirm = options.onConfirm || function () {};
        const parse = (s) => {
            const parts = String(s || '').split('-');
            return { year: parseInt(parts[0], 10), month: parseInt(parts[1], 10) };
        };
        this.start = parse(options.start);
        this.end = parse(options.end);
        this.step = 'start';
        this.viewYear = this.start.year;

        container.classList.add('month-picker-wrap');
        container.innerHTML =
            '<div class="month-picker-trigger">'
            + '<i class="bi bi-calendar cal-icon"></i>'
            + '<span class="range-start"></span><span class="range-sep">~</span><span class="range-end"></span>'
            + '</div>'
            + '<div class="month-picker-dropdown"></div>';

        this.triggerEl = container.querySelector('.month-picker-trigger');
        this.dropdownEl = container.querySelector('.month-picker-dropdown');
        this.startEl = container.querySelector('.range-start');
        this.endEl = container.querySelector('.range-end');

        this.onTriggerClick = (ev) => {
            ev.stopPropagation();
            this.toggle();
        };
        this.onDocClick = (ev) => {
            if (!ev.target.closest('.month-picker-wrap')) {
                this.close();
            }
        };
        this.triggerEl.addEventListener('click', this.onTriggerClick);
        document.addEventListener('click', this.onDocClick);

        this.updateDisplay();
    }

    toggle() {
        if (this.dropdownEl.classList.contains('show')) {
            this.close();
        } else {
            this.step = 'start';
            this.viewYear = this.start.year;
            this.dropdownEl.dataset.year = String(this.viewYear);
            this.updateDisplay();
            this.render();
            this.dropdownEl.classList.add('show');
        }
    }

    close() {
        this.dropdownEl.classList.remove('show');
    }

    render() {
        const year = this.viewYear;
        const now = new Date();
        let html = '<div class="mp-header">'
            + '<button type="button" data-delta="-1">&lsaquo;</button>'
            + '<span class="mp-year">' + year + '年</span>'
            + '<button type="button" data-delta="1">&rsaquo;</button>'
            + '</div><div class="mp-months">';
        for (let m = 1; m <= 12; m++) {
            const cur = year * 12 + m;
            const cls = ['mp-month'];
            if (year === this.start.year && m === this.start.month) cls.push('selected');
            if (year === this.end.year && m === this.end.month) cls.push('selected');
            if (cur > this.start.year * 12 + this.start.month
                && cur < this.end.year * 12 + this.end.month) cls.push('in-range');
            if (year === now.getFullYear() && m === now.getMonth() + 1) cls.push('current');
            html += '<div class="' + cls.join(' ') + '" data-month="' + m + '">' + m + '月</div>';
        }
        html += '</div>';
        this.dropdownEl.innerHTML = html;
        this.dropdownEl.querySelectorAll('.mp-header button').forEach((btn) => {
            btn.addEventListener('click', (ev) => {
                ev.stopPropagation();
                this.viewYear += parseInt(btn.dataset.delta, 10);
                this.render();
            });
        });
        this.dropdownEl.querySelectorAll('.mp-month').forEach((cell) => {
            cell.addEventListener('click', (ev) => {
                ev.stopPropagation();
                this.pick(parseInt(cell.dataset.month, 10));
            });
        });
    }

    pick(month) {
        const picked = { year: this.viewYear, month: month };
        if (this.step === 'start') {
            this.start = picked;
            this.end = picked;
            this.step = 'end';
            this.updateDisplay();
            this.render();
        } else {
            let s = this.start;
            let e = picked;
            if (picked.year * 12 + picked.month < this.start.year * 12 + this.start.month) {
                s = picked;
                e = this.start;
            }
            const span = (e.year - s.year) * 12 + (e.month - s.month);
            if (span > this.maxMonths) {
                alert('按月统计时，开始时间与结束时间最多相差一年（' + this.maxMonths + '个月）');
                return;
            }
            this.start = s;
            this.end = e;
            this.step = 'start';
            this.updateDisplay();
            this.close();
            this.onConfirm(this.format(this.start), this.format(this.end));
        }
    }

    format(o) {
        return o.year + '-' + String(o.month).padStart(2, '0');
    }

    updateDisplay() {
        this.startEl.textContent = this.format(this.start);
        this.endEl.textContent = this.format(this.end);
        this.startEl.classList.toggle('picking', this.step === 'start');
        this.endEl.classList.toggle('picking', this.step === 'end');
    }

    setRange(start, end) {
        const parse = (s) => {
            const parts = String(s || '').split('-');
            return { year: parseInt(parts[0], 10), month: parseInt(parts[1], 10) };
        };
        this.start = parse(start);
        this.end = parse(end);
        this.updateDisplay();
    }

    destroy() {
        document.removeEventListener('click', this.onDocClick);
        this.container.innerHTML = '';
        this.container.classList.remove('month-picker-wrap');
    }
}

window.MonthRangePicker = MonthRangePicker;
