/**
 * 可搜索下拉组件(零依赖):输入即过滤、键盘上下选择、回车选定
 * 取值放 hidden input,通过 hiddenClass 挂原 select 的 class,上层读取契约不变
 *
 * new FuzzySelect(mountElement, {
 *     hiddenClass: 'maintain-dept',        // 隐藏域 class(承载 .value)
 *     options: [{value, label}],           // 候选项
 *     value: '',                           // 初始值
 *     allowFree: false,                    // true 失焦允许保留手输文本(人员用)
 *     placeholder: '搜索选择',
 *     onChange: (value) => {}              // 选中后回调
 * })
 */
class FuzzySelect {
    constructor(mount, opts = {}) {
        this.options = opts.options || [];
        this.allowFree = !!opts.allowFree;
        this.onChange = opts.onChange || null;
        this.selected = null;

        mount.classList.add('fz-mount');
        mount.innerHTML = '';
        this.hidden = document.createElement('input');
        this.hidden.type = 'hidden';
        if (opts.hiddenClass) this.hidden.className = opts.hiddenClass;
        this.input = document.createElement('input');
        this.input.type = 'text';
        this.input.className = 'form-control form-control-sm fz-input';
        this.input.placeholder = opts.placeholder || '搜索选择';
        this.input.autocomplete = 'off';
        this.panel = document.createElement('div');
        this.panel.className = 'fz-panel';
        this.panel.style.display = 'none';
        this.list = document.createElement('ul');
        this.list.className = 'fz-list';
        this.panel.appendChild(this.list);
        mount.appendChild(this.hidden);
        mount.appendChild(this.input);
        mount.appendChild(this.panel);
        this.bind();

        this.setValue(opts.value || '', true);
    }

    bind() {
        this.input.addEventListener('focus', () => this.open());
        this.input.addEventListener('input', () => this.renderList(this.input.value));
        this.input.addEventListener('keydown', e => this.onKey(e));
        this.input.addEventListener('blur', () => setTimeout(() => this.onBlur(), 150));
        this.list.addEventListener('mousedown', e => {
            const li = e.target.closest('li.opt');
            if (li) {
                e.preventDefault();
                this.pick(li.dataset.value);
            }
        });
    }

    filter(kw) {
        const k = (kw || '').trim().toLowerCase();
        if (!k) return this.options;
        return this.options.filter(o =>
            (o.label || '').toLowerCase().includes(k) || (o.value || '').toLowerCase().includes(k));
    }

    renderList(kw) {
        const matched = this.filter(kw);
        this.list.innerHTML = '';
        if (matched.length === 0) {
            const empty = document.createElement('li');
            empty.className = 'empty';
            empty.textContent = '无匹配项';
            this.list.appendChild(empty);
            return;
        }
        matched.forEach((o, i) => {
            const li = document.createElement('li');
            li.className = 'opt' + (i === 0 ? ' active' : '');
            li.dataset.value = o.value;
            li.textContent = o.label;
            this.list.appendChild(li);
        });
    }

    open() {
        this.renderList(this.input.value);
        this.panel.style.display = '';
        // 下面放不下就往上弹：弹窗底部那几行往下拉会被 modal 的 overflow 裁掉
        const rect = this.input.getBoundingClientRect();
        const roomBelow = window.innerHeight - rect.bottom;
        const roomAbove = rect.top;
        const need = this.panel.scrollHeight || 220;
        this.panel.classList.toggle('fz-panel-up', roomBelow < need && roomAbove > roomBelow);
    }

    close() {
        this.panel.style.display = 'none';
    }

    pick(value) {
        this.setValue(value);
        this.close();
    }

    /** 设置值并同步显示;slient=true 时不触发 change 事件(初始回显用) */
    setValue(value, silent) {
        const opt = this.options.find(o => o.value === value);
        this.selected = opt || null;
        this.hidden.value = value;
        this.input.value = opt ? opt.label : (value || '');
        if (!silent) {
            this.hidden.dispatchEvent(new Event('change', { bubbles: true }));
            if (this.onChange) this.onChange(value);
        }
    }

    /** 换候选项;value 传了则重置选中值 */
    setOptions(options, value) {
        this.options = options || [];
        this.renderList(this.input.value);
        if (value !== undefined) this.setValue(value, true);
    }

    onKey(e) {
        if (this.panel.style.display === 'none') {
            if (e.key === 'ArrowDown' || e.key === 'Enter') {
                e.preventDefault();
                this.open();
            }
            return;
        }
        const items = Array.from(this.list.querySelectorAll('li.opt'));
        if (items.length === 0) {
            if (e.key === 'Escape') this.close();
            return;
        }
        let idx = items.findIndex(li => li.classList.contains('active'));
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            idx = idx < 0 ? 0 : Math.min(idx + 1, items.length - 1);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            idx = idx <= 0 ? 0 : idx - 1;
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (idx < 0) idx = 0;
            this.pick(items[idx].dataset.value);
            return;
        } else if (e.key === 'Escape') {
            this.close();
            return;
        } else {
            return;
        }
        items.forEach(li => li.classList.remove('active'));
        items[idx].classList.add('active');
        items[idx].scrollIntoView({ block: 'nearest' });
    }

    onBlur() {
        const text = this.input.value.trim();
        if (this.allowFree) {
            // 人员允许手输:与字典项完全同名时仍按选项处理(可带出岗位)
            const exact = this.options.find(o => o.label === text);
            this.setValue(exact ? exact.value : text);
            this.close();
            return;
        }
        // 科室必须命中选项:空值清空,唯一完全匹配则选中,其余回退到原值
        if (!text) {
            this.setValue('');
        } else {
            const exact = this.options.find(o => o.label === text);
            this.setValue(exact ? exact.value : this.hidden.value);
        }
        this.close();
    }
}
