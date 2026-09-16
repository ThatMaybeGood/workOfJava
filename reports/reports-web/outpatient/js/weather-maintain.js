/**
 * 页内轻提示，替代原生 window.alert()（报告页可能跑在 sandbox iframe 里，alert 会被屏蔽）
 */
function showNotice(message, type) {
    let el = document.getElementById('appNotice');
    if (!el) {
        el = document.createElement('div');
        el.id = 'appNotice';
        el.style.cssText = 'position:fixed;top:16px;left:50%;transform:translateX(-50%);z-index:20000;' +
            'padding:8px 18px;border-radius:4px;font-size:13px;color:#fff;white-space:nowrap;' +
            'box-shadow:0 2px 8px rgba(0,0,0,.2);transition:opacity .3s;opacity:0;pointer-events:none';
        document.body.appendChild(el);
    }
    el.textContent = message;
    el.style.background = type === 'error' ? '#ff4d4f' : '#52c41a';
    el.style.opacity = '1';
    clearTimeout(el._noticeTimer);
    el._noticeTimer = setTimeout(() => { el.style.opacity = '0'; }, 2200);
}

/**
 * 页内确认框，替代原生 window.confirm()（浏览器可勾选「阻止对话框」，之后 confirm 直接返回 false）
 */
function confirmDialog(message) {
    return new Promise((resolve) => {
        const id = 'appConfirmModal';
        let el = document.getElementById(id);
        if (!el) {
            el = document.createElement('div');
            el.id = id;
            el.className = 'modal fade';
            el.innerHTML =
                '<div class="modal-dialog modal-dialog-centered modal-sm">' +
                '  <div class="modal-content">' +
                '    <div class="modal-body">' +
                '      <div class="d-flex align-items-start gap-2">' +
                '        <i class="bi bi-exclamation-triangle-fill text-warning"></i>' +
                '        <div class="confirm-message"></div>' +
                '      </div>' +
                '    </div>' +
                '    <div class="modal-footer py-2">' +
                '      <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">取消</button>' +
                '      <button type="button" class="btn btn-sm btn-danger confirm-ok">确定</button>' +
                '    </div>' +
                '  </div>' +
                '</div>';
            document.body.appendChild(el);
        }
        el.querySelector('.confirm-message').textContent = message;
        const modal = bootstrap.Modal.getOrCreateInstance(el);
        const okBtn = el.querySelector('.confirm-ok');
        const cleanup = () => {
            okBtn.removeEventListener('click', onOk);
            el.removeEventListener('hidden.bs.modal', onHide);
        };
        const onOk = () => { cleanup(); modal.hide(); resolve(true); };
        const onHide = () => { cleanup(); resolve(false); };
        okBtn.addEventListener('click', onOk);
        el.addEventListener('hidden.bs.modal', onHide);
        modal.show();
    });
}

/**
 * 天气维护弹窗逻辑
 * 查询/新增/删除 tr_fc_weather 每日天气，界面登记来源固定为「人工登记」
 */
class WeatherMaintainManager {
    constructor() {
        this.state = {
            startDate: null,
            endDate: null,
            page: 1,
            pageSize: 10,
            total: 0,
            rows: [],
            newRows: []
        };
        // 天气类型取自通用字典 weather_type，字典为空时用这套兜底
        this.defaultTypes = ['晴', '多云', '阴', '小雨', '中雨', '大雨', '雪'];
        this.weatherTypes = this.defaultTypes.slice();
        this.datePicker = null;
        this.loadTypes();
        this.bindEvents();
    }

    /** 从通用字典加载天气类型 */
    async loadTypes() {
        try {
            const body = await ReportAPI.getDataDict({ action: 'query', dictType: 'weather_type' });
            const names = ((body && body.list) || []).map(i => i.dictName).filter(Boolean);
            if (names.length) this.weatherTypes = names;
        } catch (error) {
            console.error('Load weather types failed:', error);
        }
    }

    bindEvents() {
        document.getElementById('weatherQueryBtn').addEventListener('click', () => {
            this.state.page = 1;
            this.load();
        });
        document.getElementById('weatherAddRowBtn').addEventListener('click', () => this.addRow());
        document.getElementById('weatherSaveBtn').addEventListener('click', () => this.save());
        document.getElementById('weatherModal').addEventListener('shown.bs.modal', () => this.init());
        document.getElementById('weatherTableBody').addEventListener('click', async (e) => {
            const link = e.target.closest('a.weather-delete');
            if (!link) return;
            e.preventDefault();
            const tr = link.closest('tr');
            if (tr.dataset.date) {
                // 已有记录：先落库删除
                if (!(await confirmDialog(`确定删除 ${tr.dataset.date} 的天气记录吗？`))) return;
                this.remove(tr.dataset.date);
            } else {
                // 未保存的新行：直接从界面移除
                tr.remove();
                this.toggleEmpty();
            }
        });
    }

    init() {
        const today = this.formatDate(new Date()).replace(/-/g, '/');
        const weekAgo = this.formatDate(new Date(Date.now() - 6 * 86400000)).replace(/-/g, '/');
        if (!this.datePicker) {
            this.datePicker = flatpickr(document.getElementById('weatherDateRange'), {
                mode: 'range',
                dateFormat: 'Y/m/d',
                defaultDate: [weekAgo, today],
                locale: 'zh',
                allowInput: false,
                onChange: (selectedDates) => {
                    if (selectedDates.length === 2) {
                        this.state.startDate = this.formatDate(selectedDates[0]);
                        this.state.endDate = this.formatDate(selectedDates[1]);
                    }
                }
            });
        }
        this.state.startDate = this.formatDate(new Date(Date.now() - 6 * 86400000));
        this.state.endDate = this.formatDate(new Date());
        this.state.page = 1;
        this.state.newRows = [];
        this.load();
    }

    async load() {
        try {
            const body = await ReportAPI.maintainWeather({
                action: 'query',
                startDate: this.state.startDate,
                endDate: this.state.endDate,
                page: this.state.page,
                pageSize: this.state.pageSize
            });
            this.state.rows = body.list || [];
            this.state.total = body.total || 0;
            this.render();
        } catch (error) {
            console.error('Load weather failed:', error);
            showNotice('查询天气数据失败', 'error');
        }
    }

    render() {
        const tbody = document.getElementById('weatherTableBody');
        tbody.innerHTML = '';
        const base = (this.state.page - 1) * this.state.pageSize;
        this.state.rows.forEach((row, idx) => tbody.appendChild(this.buildRow(row, base + idx + 1)));
        this.toggleEmpty();
        this.renderPagination();
    }

    buildRow(row, seq) {
        const tr = document.createElement('tr');
        if (row && row.weatherDate) tr.dataset.date = this.normalizeDate(row.weatherDate);
        const dateVal = tr.dataset.date || '';
        const type = row ? row.weatherType || '' : '';
        // 出勤系数不手填：保存时后端按近30天同天气日期爽约退号率自动计算
        const coef = row && row.weatherCoef != null ? row.weatherCoef : (dateVal ? '' : '保存后计算');
        // 界面登记(含修改)的行来源统一显示人工登记
        const source = row ? (row.weatherSource || '') : '人工登记';
        tr.innerHTML =
            `<td class="align-middle text-muted">${seq != null ? seq : ''}</td>` +
            `<td><input type="text" class="form-control form-control-sm weather-date" value="${dateVal}" ${dateVal ? 'readonly' : ''} placeholder="选择日期"></td>` +
            `<td><select class="form-select form-select-sm weather-type">${this.typeOptions(type)}</select></td>` +
            `<td class="align-middle weather-coef">${coef}</td>` +
            `<td class="weather-source align-middle">${source}</td>` +
            `<td class="align-middle"><a href="#" class="text-danger weather-delete">删除</a></td>`;
        const dateInput = tr.querySelector('.weather-date');
        if (!dateVal) {
            tr.dataset.dirty = '1';
            flatpickr(dateInput, {
                dateFormat: 'Y-m-d', locale: 'zh', allowInput: false,
                onChange: () => { tr.dataset.dirty = '1'; }
            });
        }
        // 已有行被修改时标记脏并联动来源为人工登记
        ['weather-type', 'weather-coef'].forEach(cls => {
            tr.querySelector('.' + cls).addEventListener('change', () => {
                tr.dataset.dirty = '1';
                tr.querySelector('.weather-source').textContent = '人工登记';
            });
        });
        return tr;
    }

    typeOptions(selected) {
        return this.weatherTypes.map(t => `<option value="${t}" ${t === selected ? 'selected' : ''}>${t}</option>`).join('');
    }

    addRow() {
        document.getElementById('weatherEmpty').classList.add('d-none');
        const tbody = document.getElementById('weatherTableBody');
        tbody.appendChild(this.buildRow(null, tbody.children.length + 1));
    }

    async save() {
        // 只提交新增/修改过的行，避免把接口同步的数据误标成人工登记
        const list = [];
        const seen = new Set();
        let invalid = null;
        document.querySelectorAll('#weatherTableBody tr').forEach(tr => {
            const date = tr.dataset.date || (tr.querySelector('.weather-date').value || '').trim();
            if (!date) { invalid = invalid || '日期不能为空'; return; }
            if (seen.has(date)) { invalid = invalid || `${date} 重复登记`; return; }
            seen.add(date);
            if (!tr.dataset.dirty) return;
            list.push({
                weatherDate: date,
                weatherType: tr.querySelector('.weather-type').value || null
            });
        });
        if (invalid) {
            showNotice(invalid, 'error');
            return;
        }
        if (!list.length) {
            showNotice('没有需要保存的数据', 'error');
            return;
        }
        try {
            await ReportAPI.maintainWeather({ action: 'save', list });
            showNotice('天气数据保存成功');
            this.load();
        } catch (error) {
            console.error('Save weather failed:', error);
            showNotice('保存天气数据失败', 'error');
        }
    }

    async remove(weatherDate) {
        try {
            await ReportAPI.maintainWeather({ action: 'delete', weatherDate });
            showNotice('天气数据删除成功');
            // 删的是当前页最后一条时回退一页
            if (this.state.rows.length === 1 && this.state.page > 1) {
                this.state.page--;
            }
            this.load();
        } catch (error) {
            console.error('Delete weather failed:', error);
            showNotice('删除天气数据失败', 'error');
        }
    }

    toggleEmpty() {
        const hasRows = document.querySelectorAll('#weatherTableBody tr').length > 0;
        document.getElementById('weatherEmpty').classList.toggle('d-none', hasRows);
    }

    renderPagination() {
        document.getElementById('weatherPageInfo').textContent =
            `${this.state.pageSize}条/页 共${this.state.total}条`;
        const pager = document.getElementById('weatherPagination');
        pager.innerHTML = '';
        const pages = Math.max(1, Math.ceil(this.state.total / this.state.pageSize));
        const mk = (label, page, disabled, active) => {
            const li = document.createElement('li');
            li.className = `page-item${disabled ? ' disabled' : ''}${active ? ' active' : ''}`;
            li.innerHTML = `<a class="page-link" href="#">${label}</a>`;
            if (!disabled && !active) {
                li.addEventListener('click', (e) => {
                    e.preventDefault();
                    this.state.page = page;
                    this.load();
                });
            }
            return li;
        };
        pager.appendChild(mk('上一页', this.state.page - 1, this.state.page <= 1, false));
        for (let p = 1; p <= pages; p++) {
            pager.appendChild(mk(p, p, false, p === this.state.page));
        }
        pager.appendChild(mk('下一页', this.state.page + 1, this.state.page >= pages, false));
    }

    normalizeDate(value) {
        // 后端按 yyyy-MM-dd 序列化，原样取用；兜底处理时间戳/Date 对象
        if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}/.test(value)) return value.slice(0, 10);
        return this.formatDate(new Date(value));
    }

    formatDate(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }
}

/**
 * 天气类型字典管理弹窗逻辑（通用字典 dict_type = weather_type）
 */
class WeatherDictManager {
    constructor() {
        this.state = { data: [] };
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById('dictAddBtn').addEventListener('click', () => this.addValue());
        document.getElementById('dictNewValue').addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this.addValue();
        });
        document.getElementById('dictModal').addEventListener('shown.bs.modal', () => this.load());
    }

    async load() {
        try {
            const body = await ReportAPI.getDataDict({ action: 'query', dictType: 'weather_type' });
            this.state.data = ((body && body.list) || []).map(i => ({ id: i.id, dictName: i.dictName }));
            this.render();
        } catch (error) {
            console.error('Load weather dict failed:', error);
            showNotice('查询字典失败', 'error');
        }
    }

    render() {
        const tbody = document.getElementById('dictValueBody');
        if (!this.state.data.length) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">暂无字典值</td></tr>';
            return;
        }
        tbody.innerHTML = this.state.data.map((item, idx) => `
            <tr>
                <td class="text-muted">${idx + 1}</td>
                <td>${item.dictName}</td>
                <td><a href="#" class="text-danger" data-id="${item.id}">删除</a></td>
            </tr>
        `).join('');
        tbody.querySelectorAll('a[data-id]').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.deleteValue(parseInt(link.dataset.id, 10));
            });
        });
    }

    async addValue() {
        const input = document.getElementById('dictNewValue');
        const value = input.value.trim();
        if (!value) {
            showNotice('请输入字典值', 'error');
            return;
        }
        try {
            await ReportAPI.getDataDict({ action: 'add', dictType: 'weather_type', dictName: value });
            input.value = '';
            showNotice('添加成功');
            this.load();
            weatherMaintainManager.loadTypes();
        } catch (error) {
            console.error('Add weather dict failed:', error);
            showNotice('新增失败', 'error');
        }
    }

    async deleteValue(id) {
        if (!(await confirmDialog('确认删除该字典值？'))) return;
        try {
            await ReportAPI.getDataDict({ action: 'delete', dictType: 'weather_type', id });
            showNotice('删除成功');
            this.load();
            weatherMaintainManager.loadTypes();
        } catch (error) {
            console.error('Delete weather dict failed:', error);
            showNotice('删除失败', 'error');
        }
    }
}

const weatherMaintainManager = new WeatherMaintainManager();
const weatherDictManager = new WeatherDictManager();
