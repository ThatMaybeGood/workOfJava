/**
 * ETL 公共渲染组件
 * 纯函数渲染，返回 HTML 字符串；导出到 window.etlComponents
 */
(function () {
    /** HTML 转义 */
    function esc(s) {
        if (s === null || s === undefined) return '';
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /**
     * 管道式步骤条
     * @param {number} currentStep 当前步（1 起）
     * @param {Array<{key:string,name:string}>} steps
     * @param {function(number)=} onStepClick 传入后步骤可点击跳转
     * @returns {string} HTML
     */
    function renderStepper(currentStep, steps, onStepClick) {
        const list = steps && steps.length ? steps : [
            { key: 'source', name: '抽取来源' },
            { key: 'transform', name: '转换提取' },
            { key: 'mapping', name: '映射匹配' }
        ];
        const clickable = typeof onStepClick === 'function';
        const items = list.map((s, i) => {
            const idx = i + 1;
            const state = idx < currentStep ? 'done' : (idx === currentStep ? 'active' : '');
            const stationState = state === 'done' ? 'done' : (state === 'active' ? 'active' : '');
            const mark = state === 'done' ? '&#10003;' : String(idx).padStart(2, '0');
            const clickAttr = clickable ? ' data-step="' + idx + '" tabindex="0" role="button"' : '';
            const clickCls = clickable ? ' clickable' : '';
            return '<div class="etl-step ' + state + clickCls + '"' + clickAttr + '>' +
                '<div class="etl-step-track ' + (idx <= currentStep ? 'done' : '') + '"></div>' +
                '<div class="etl-step-station ' + stationState + '">' + mark + '</div>' +
                '<div class="etl-step-eyebrow">STEP ' + String(idx).padStart(2, '0') + '</div>' +
                '<div class="etl-step-name">' + esc(s.name) + '</div>' +
                '</div>';
        });
        const wrapperCls = 'etl-stepper' + (clickable ? ' clickable' : '');
        return '<div class="' + wrapperCls + '">' + items.join('') + '</div>';
    }

    /**
     * 为可点击步骤条绑定跳转事件
     * @param {Element} container 包含 .etl-stepper 的容器
     * @param {function(number)} callback 参数为步骤序号（1 起）
     */
    function bindStepper(container, callback) {
        if (!container || typeof callback !== 'function') return;
        container.addEventListener('click', function (e) {
            const step = e.target.closest('.etl-step.clickable');
            if (!step || !container.contains(step)) return;
            const idx = parseInt(step.getAttribute('data-step'), 10);
            if (!isNaN(idx)) callback(idx);
        });
        container.addEventListener('keydown', function (e) {
            if (e.key !== 'Enter' && e.key !== ' ') return;
            const step = e.target.closest('.etl-step.clickable');
            if (!step || !container.contains(step)) return;
            e.preventDefault();
            const idx = parseInt(step.getAttribute('data-step'), 10);
            if (!isNaN(idx)) callback(idx);
        });
    }

    /**
     * 层级树
     * @param {Array} nodes 契约：[{path,name,type,isList,sampleCount,children[]}]
     * @param {object} opts {selectable:boolean, onPick:function(node)}
     * @returns {string} HTML（selectable 时通过 data-path 委托；onPick 由调用方绑定或
     *          在容器中监听 click 后调用 etlComponents.bindTreePick(container, onPick)）
     */
    function renderTree(nodes, opts) {
        opts = opts || {};
        const selectable = !!opts.selectable;

        function renderNode(node) {
            const badge = node.isList
                ? '<span class="etl-tree-listbadge">数组 ×' + esc(node.sampleCount != null ? node.sampleCount : '?') + '</span>'
                : '';
            const type = node.type ? '<span class="etl-tree-type">' + esc(node.type) + '</span>' : '';
            const cls = 'etl-tree-node' + (selectable ? ' selectable' : '');
            const attrs = selectable
                ? ' tabindex="0" role="button" data-path="' + esc(node.path) + '"'
                : ' data-path="' + esc(node.path) + '"';
            let html = '<li><div class="' + cls + '"' + attrs + '>' +
                '<span class="etl-tree-path">' + esc(node.name != null ? node.name : node.path) + '</span>' +
                type + badge +
                '</div>';
            if (node.children && node.children.length) {
                html += '<ul>' + node.children.map(renderNode).join('') + '</ul>';
            }
            return html + '</li>';
        }

        if (!nodes || !nodes.length) {
            return '<div class="etl-tree"><div class="etl-empty"><div class="etl-empty-text">暂无结构数据</div></div></div>';
        }
        return '<div class="etl-tree"><ul>' + nodes.map(renderNode).join('') + '</ul></div>';
    }

    /**
     * 为 renderTree 输出的容器绑定节点选择回调
     * @param {Element} container 包含 .etl-tree 的容器
     * @param {function(string, Element)} onPick 参数为 data-path 与节点元素
     */
    function bindTreePick(container, onPick) {
        if (!container || typeof onPick !== 'function') return;
        function handler(e) {
            const node = e.target.closest('.etl-tree-node.selectable');
            if (!node || !container.contains(node)) return;
            onPick(node.getAttribute('data-path'), node);
        }
        container.addEventListener('click', handler);
        container.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                const node = e.target.closest('.etl-tree-node.selectable');
                if (node && container.contains(node)) {
                    e.preventDefault();
                    onPick(node.getAttribute('data-path'), node);
                }
            }
        });
    }

    /**
     * 深色调试控制台面板
     * @param {string} title 面板标题
     * @param {Array<{label:string,status:string,content:string}>} sections status: 'ok'|'err'|''
     * @returns {string} HTML
     */
    function renderDebugPanel(title, sections) {
        const body = (sections || []).map(function (s) {
            const statusCls = s.status === 'ok' ? 'ok' : (s.status === 'err' ? 'err' : '');
            const statusText = s.status === 'ok' ? 'OK' : (s.status === 'err' ? 'FAIL' : '');
            return '<div class="etl-debug-section">' +
                '<div><span class="etl-debug-label">[' + esc(s.label) + ']</span> ' +
                (statusText ? '<span class="etl-debug-status ' + statusCls + '">' + statusText + '</span>' : '') +
                '</div>' +
                (s.content ? '<pre>' + esc(s.content) + '</pre>' : '') +
                '</div>';
        }).join('');
        return '<div class="etl-debug-panel">' +
            '<div class="etl-debug-title">' + esc(title || 'DEBUG CONSOLE') + '</div>' +
            '<div class="etl-debug-body">' + body + '</div>' +
            '</div>';
    }

    /**
     * 执行历史时间线
     * @param {Array<{startTime:string,status:string,durationMs:number,rowsCount:number,errorMsg:string}>} logs
     * @returns {string} HTML
     */
    function renderTimeline(logs) {
        if (!logs || !logs.length) {
            return '<div class="etl-empty"><div class="etl-empty-text">暂无执行记录</div></div>';
        }
        const items = logs.map(function (log) {
            const ok = log.status === 'SUCCESS' || log.status === 'OK' || log.status === 'success';
            const cls = ok ? 'ok' : 'err';
            const meta = [fmtDuration(log.durationMs),
                log.rowsCount != null ? esc(log.rowsCount) + ' 行' : null]
                .filter(Boolean).join(' · ');
            return '<div class="etl-timeline-item ' + cls + '">' +
                '<div class="etl-timeline-dot"></div>' +
                '<div class="etl-timeline-time">' + esc(fmtTime(log.startTime)) +
                ' <span class="etl-badge ' + cls + '">' + (ok ? '成功' : '失败') + '</span></div>' +
                '<div class="etl-timeline-meta">' + meta + '</div>' +
                (log.errorMsg ? '<div class="etl-timeline-err">' + esc(log.errorMsg) + '</div>' : '') +
                '</div>';
        });
        return '<div class="etl-timeline">' + items.join('') + '</div>';
    }

    /**
     * 顶部居中 toast
     * @param {string} message
     * @param {string} type 'ok'|'err'|'warn'|''
     */
    function toast(message, type) {
        let el = document.querySelector('.etl-toast');
        if (!el) {
            el = document.createElement('div');
            document.body.appendChild(el);
        }
        el.className = 'etl-toast ' + (type || '');
        el.textContent = message || '';
        // 强制重排以重放过渡
        void el.offsetWidth;
        el.classList.add('show');
        clearTimeout(el._etlToastTimer);
        el._etlToastTimer = setTimeout(function () {
            el.classList.remove('show');
        }, 3200);
    }

    /** 时间格式化：ISO/时间戳 → 'YYYY-MM-DD HH:mm:ss' */
    function fmtTime(t) {
        if (!t) return '-';
        const d = new Date(t);
        if (isNaN(d.getTime())) return String(t);
        const p = function (n) { return String(n).padStart(2, '0'); };
        return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) +
            ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
    }

    /** 耗时格式化：ms → '1.2s' / '320ms' / '3m 05s' */
    function fmtDuration(ms) {
        if (ms === null || ms === undefined || isNaN(ms)) return '-';
        ms = Number(ms);
        if (ms < 1000) return Math.round(ms) + 'ms';
        if (ms < 60000) return (ms / 1000).toFixed(1).replace(/\.0$/, '') + 's';
        const m = Math.floor(ms / 60000);
        const s = Math.round((ms % 60000) / 1000);
        return m + 'm ' + String(s).padStart(2, '0') + 's';
    }

    /**
     * Cron 表达式 → 中文可读描述
     * 支持 Quartz 6 段（秒 分 时 日 月 周）与常见 5 段（分 时 日 月 周）。
     * 例：0 0/5 * * * ? → 每5分钟；0 30 8 * * MON-FRI → 工作日 08:30
     */
    function fmtCron(cron) {
        if (!cron) return '';
        const s = String(cron).trim();
        if (!s) return '';
        const f = s.split(/\s+/).filter(Boolean);
        let sec, min, hour, dom, mon, dow;
        if (f.length === 6) { sec = f[0]; min = f[1]; hour = f[2]; dom = f[3]; mon = f[4]; dow = f[5]; }
        else if (f.length === 5) { sec = '0'; min = f[0]; hour = f[1]; dom = f[2]; mon = f[3]; dow = f[4]; }
        else return s;

        const isWild = function (v) { return v === '*' || v === '?'; };
        const stepOf = function (v) { const m = /^(?:\*|(\d+))\/(\d+)$/.exec(String(v)); return m ? parseInt(m[2], 10) : null; };
        const numOf = function (v) { return /^\d+$/.test(String(v)) ? parseInt(String(v), 10) : null; };
        const pad = function (n) { return String(n).padStart(2, '0'); };

        const DOW = { SUN: 0, MON: 1, TUE: 2, WED: 3, THU: 4, FRI: 5, SAT: 6 };
        const DOW_CN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        function dowName(v) {
            const up = String(v).toUpperCase();
            if (DOW[up] != null) return DOW_CN[DOW[up]];
            const n = numOf(up);
            if (n != null && n >= 0 && n <= 6) return DOW_CN[n];
            if (up === 'MON-FRI') return '工作日';
            if (up === 'MON-SAT') return '周一至周六';
            return String(v);
        }

        const hNum = numOf(hour);
        const mNum = numOf(min);
        const timeStr = (hNum != null && mNum != null) ? pad(hNum) + ':' + pad(mNum) : '';

        const minStep = stepOf(min);
        if (minStep) return '每' + minStep + '分钟';
        if (isWild(min)) {
            const hourStep = stepOf(hour);
            if (hourStep) return '每' + hourStep + '小时';
            if (isWild(hour)) return '每分钟';
            return '每小时';
        }
        const hourStep = stepOf(hour);
        if (hourStep) return '每' + hourStep + '小时';
        if (mNum != null && hNum != null) {
            if (!isWild(dow)) return dowName(dow) + ' ' + timeStr;
            const domNum = numOf(dom);
            if (domNum != null) return '每月' + domNum + '日 ' + timeStr;
            return '每天 ' + timeStr;
        }
        if (mNum != null && isWild(hour)) {
            return mNum === 0 ? '每小时' : '每小时的第' + mNum + '分';
        }
        return s;
    }

    /**
     * 计算 cron 的最近一次下次执行时间
     * @param {string} cron Quartz 6 段（秒 分 时 日 月 周）或 5 段
     * @param {Date} [from] 起始时间，默认当前
     * @returns {Date|null} 下次执行时间，无法解析或超出推算范围返回 null
     */
    function nextCronRun(cron, from) {
        if (!cron) return null;
        const s = String(cron).trim();
        if (!s) return null;
        const f = s.split(/\s+/).filter(Boolean);
        let secF, minF, hourF, domF, monF, dowF;
        if (f.length === 6) { secF = f[0]; minF = f[1]; hourF = f[2]; domF = f[3]; monF = f[4]; dowF = f[5]; }
        else if (f.length === 5) { secF = '0'; minF = f[0]; hourF = f[1]; domF = f[2]; monF = f[3]; dowF = f[4]; }
        else return null;

        const MON_NAMES = { JAN: 1, FEB: 2, MAR: 3, APR: 4, MAY: 5, JUN: 6, JUL: 7, AUG: 8, SEP: 9, OCT: 10, NOV: 11, DEC: 12 };
        const DOW_NAMES = { SUN: [7, 0], MON: [1], TUE: [2], WED: [3], THU: [4], FRI: [5], SAT: [6] };
        // 展开字段为允许值集合；null 表示无限制（* / ?）
        function expand(v, min, max, names) {
            v = String(v);
            if (v === '*' || v === '?') return null;
            const out = new Set();
            const add = function (n) {
                if (Array.isArray(n)) { n.forEach(function (x) { if (x != null) out.add(x); }); }
                else if (n != null) { out.add(n); }
            };
            String(v).split(',').forEach(function (part) {
                let m;
                if ((m = /^(.+)\/(\d+)$/.exec(part))) {
                    const step = parseInt(m[2], 10);
                    const rp = m[1];
                    let lo, hi;
                    if (rp === '*') { lo = min; hi = max; }
                    else if (rp.indexOf('-') >= 0) { const r = rp.split('-'); lo = toScalar(r[0], names); hi = toScalar(r[1], names); }
                    else { lo = toScalar(rp, names); hi = max; }
                    for (let x = lo; x <= hi; x += step) add(x);
                } else if (part.indexOf('-') >= 0) {
                    const r = part.split('-');
                    const lo = toScalar(r[0], names), hi = toScalar(r[1], names);
                    for (let x = lo; x <= hi; x += 1) add(x);
                } else {
                    add(toNum(part, names));
                }
            });
            return out;
        }
        function toNum(token, names) {
            token = String(token).trim();
            if (names && names[token.toUpperCase()] != null) return names[token.toUpperCase()];
            const n = parseInt(token, 10);
            return isNaN(n) ? null : n;
        }
        // 名称映射可能返回数组（如 SUN:[7,0]），范围/步进端点需取标量
        function toScalar(token, names) {
            const v = toNum(token, names);
            return Array.isArray(v) ? v[0] : v;
        }

        let secs, mins, hours, doms, mons, dows;
        try {
            secs = expand(secF, 0, 59);
            mins = expand(minF, 0, 59);
            hours = expand(hourF, 0, 23);
            doms = expand(domF, 1, 31);
            mons = expand(monF, 1, 12, MON_NAMES);
            dows = expand(dowF, 0, 7, DOW_NAMES);
        } catch (e) {
            return null;
        }
        const has = function (set, n) { return !set || set.has(n); };

        const domWild = domF === '*' || domF === '?';
        const dowWild = dowF === '*' || dowF === '?';
        const MAX_DAYS = 400; // 推算上限（约 13 个月）

        function addDays(date, n) {
            const d = new Date(date.getTime());
            d.setDate(d.getDate() + n);
            return d;
        }
        // 在某一天内找第一个匹配的时分秒；fromNow=true 从 t 起（严格晚于当前），否则从当天 00:00 起
        function findTimeOnDay(t, fromNow) {
            const start = fromNow ? t : new Date(t.getFullYear(), t.getMonth(), t.getDate(), 0, 0, 0, 0);
            const end = new Date(start.getTime());
            end.setHours(23, 59, 59, 0);
            for (let cur = new Date(start.getTime()); cur <= end; cur = new Date(cur.getTime() + 1000)) {
                if (has(secs, cur.getSeconds()) && has(mins, cur.getMinutes()) && has(hours, cur.getHours())) {
                    return cur;
                }
            }
            return null;
        }

        let t = from ? new Date(from.getTime()) : new Date();
        t.setMilliseconds(0);
        t = new Date(t.getTime() + 1000); // 从下一整秒开始

        for (let d = 0; d < MAX_DAYS; d++) {
            const month = t.getMonth() + 1;
            const day = t.getDate();
            const qdow = (t.getDay() === 0) ? 7 : t.getDay(); // getDay: 0=Sun..6=Sat → Quartz 7=Sun..6=Sat
            if (!has(mons, month)) { t = addDays(t, 1); continue; }
            let dayOk;
            if (domWild && dowWild) dayOk = true;
            else if (domWild) dayOk = has(dows, qdow);
            else if (dowWild) dayOk = has(doms, day);
            else dayOk = has(doms, day) || has(dows, qdow);
            if (!dayOk) { t = addDays(t, 1); continue; }
            const found = findTimeOnDay(t, d === 0);
            if (found) return found;
            t = addDays(t, 1);
        }
        return null;
    }

    /**
     * Bootstrap 风格分页
     * @param {{page:number,size:number,total:number}} opts
     * @returns {string} HTML
     */
    function renderPagination(opts) {
        opts = opts || {};
        const page = Math.max(1, parseInt(opts.page, 10) || 1);
        const size = Math.max(1, parseInt(opts.size, 10) || 10);
        const total = Math.max(0, parseInt(opts.total, 10) || 0);
        if (total <= 0) return '';
        const totalPages = Math.max(1, Math.ceil(total / size));
        const safePage = Math.min(page, totalPages);

        function item(cls, label, targetPage, disabled) {
            return '<li class="page-item ' + (cls || '') + (disabled ? ' disabled' : '') + '" data-page="' + esc(String(targetPage)) + '"' + (disabled ? ' aria-disabled="true"' : '') + '>' +
                '<a class="page-link" data-page="' + esc(String(targetPage)) + '"' + (disabled ? ' tabindex="-1"' : '') + '>' + esc(label) + '</a>' +
                '</li>';
        }

        const pages = [];
        pages.push(item('prev', '上一页', safePage - 1, safePage <= 1));

        const maxVisible = 7;
        if (totalPages <= maxVisible) {
            for (let i = 1; i <= totalPages; i++) {
                pages.push(item(i === safePage ? 'active' : '', String(i), i, i === safePage));
            }
        } else {
            pages.push(item(safePage === 1 ? 'active' : '', '1', 1, false));
            let start = Math.max(2, safePage - 2);
            let end = Math.min(totalPages - 1, safePage + 2);
            if (safePage <= 4) {
                start = 2;
                end = Math.min(totalPages - 1, 5);
            } else if (safePage >= totalPages - 3) {
                start = Math.max(2, totalPages - 4);
                end = totalPages - 1;
            }
            if (start > 2) {
                pages.push(item('ellipsis', '...', -1, true));
            }
            for (let i = start; i <= end; i++) {
                pages.push(item(i === safePage ? 'active' : '', String(i), i, i === safePage));
            }
            if (end < totalPages - 1) {
                pages.push(item('ellipsis', '...', -1, true));
            }
            pages.push(item(safePage === totalPages ? 'active' : '', String(totalPages), totalPages, false));
        }

        pages.push(item('next', '下一页', safePage + 1, safePage >= totalPages));

        return '<ul class="etl-pagination">' + pages.join('') + '</ul>' +
            '<span class="etl-pagination-info">共 ' + totalPages + ' 页 / ' + total + ' 条</span>';
    }

    /**
     * 绑定分页点击事件
     * @param {Element} container 包含 .etl-pagination 的容器
     * @param {function(number)} callback 参数为页码
     */
    function bindPagination(container, callback) {
        if (!container || typeof callback !== 'function') return;
        container.addEventListener('click', function (e) {
            const link = e.target.closest('.etl-pagination .page-link');
            if (!link || !container.contains(link)) return;
            const li = link.closest('.page-item');
            if (li && (li.classList.contains('disabled') || li.classList.contains('active'))) return;
            const p = parseInt(link.getAttribute('data-page'), 10);
            if (!isNaN(p) && p > 0) callback(p);
        });
    }

    /**
     * 可搜索下拉框
     * @param {{items:Array<{label:string,value:any}>,value:any,placeholder:string}} opts
     * @returns {string} HTML
     */
    function renderSelectSearch(opts) {
        opts = opts || {};
        const items = Array.isArray(opts.items) ? opts.items : [];
        const value = opts.value;
        const placeholder = esc(opts.placeholder || '搜索并选择...');
        const selected = items.find(function (it) { return it.value === value; });
        const inputValue = selected ? esc(selected.label) : '';

        const listHtml = items.length
            ? items.map(function (it) {
                const selectedCls = it.value === value ? ' selected' : '';
                return '<div class="etl-select-search-item' + selectedCls + '" data-value="' + esc(String(it.value)) + '" data-label="' + esc(it.label || '') + '" tabindex="0" role="option"' + (selectedCls ? ' aria-selected="true"' : '') + '>' + esc(it.label || '') + '</div>';
            }).join('')
            : '<div class="etl-select-search-empty">无数据</div>';

        return '<div class="etl-select-search">' +
            '<input type="text" class="etl-select-search-input" autocomplete="off" placeholder="' + placeholder + '" value="' + inputValue + '" aria-autocomplete="list" aria-haspopup="listbox" aria-expanded="false">' +
            '<div class="etl-select-search-dropdown" role="listbox" style="display:none;">' + listHtml + '</div>' +
            '</div>';
    }

    /**
     * 绑定下拉搜索事件
     * @param {Element} container 包含 .etl-select-search 的容器
     * @param {{items:Array,onSearch:function(string),onSelect:function(value,label)}} callbacks
     */
    function bindSelectSearch(container, callbacks) {
        if (!container) return;
        callbacks = callbacks || {};
        const input = container.querySelector('.etl-select-search-input');
        const dropdown = container.querySelector('.etl-select-search-dropdown');
        if (!input || !dropdown) return;

        let items = Array.isArray(callbacks.items) ? callbacks.items.slice() : [];
        let activeIndex = -1;

        function escapeRegExp(s) {
            return String(s).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        }

        function renderList(query) {
            const q = (query || '').toLowerCase().trim();
            const filtered = q ? items.filter(function (it) {
                return String(it.label || '').toLowerCase().indexOf(q) !== -1;
            }) : items.slice();

            activeIndex = filtered.length ? 0 : -1;
            if (!filtered.length) {
                dropdown.innerHTML = '<div class="etl-select-search-empty">无匹配结果</div>';
                input.setAttribute('aria-expanded', 'true');
                return;
            }
            dropdown.innerHTML = filtered.map(function (it, i) {
                const label = esc(String(it.label || ''));
                let html = label;
                if (q) {
                    html = label.replace(new RegExp('(' + escapeRegExp(q) + ')', 'gi'), '<span class="etl-select-search-highlight">$1</span>');
                }
                const selectedCls = it.value === input._etlSelectedValue ? ' selected' : '';
                const activeCls = i === activeIndex ? ' active' : '';
                return '<div class="etl-select-search-item' + selectedCls + activeCls + '" data-value="' + esc(String(it.value)) + '" data-label="' + esc(String(it.label || '')) + '" tabindex="0" role="option"' + (selectedCls ? ' aria-selected="true"' : '') + '>' + html + '</div>';
            }).join('');
            input.setAttribute('aria-expanded', 'true');
        }

        function show() {
            renderList(input.value);
            dropdown.style.display = 'block';
        }

        function hide() {
            dropdown.style.display = 'none';
            activeIndex = -1;
            input.setAttribute('aria-expanded', 'false');
        }

        input.addEventListener('focus', show);
        input.addEventListener('input', function () {
            show();
            if (typeof callbacks.onSearch === 'function') callbacks.onSearch(input.value);
        });
        input.addEventListener('keydown', function (e) {
            const nodes = dropdown.querySelectorAll('.etl-select-search-item');
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (nodes.length) {
                    activeIndex = Math.min(activeIndex + 1, nodes.length - 1);
                    nodes.forEach(function (n, i) { n.classList.toggle('active', i === activeIndex); });
                    nodes[activeIndex].scrollIntoView({ block: 'nearest' });
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (nodes.length) {
                    activeIndex = Math.max(activeIndex - 1, 0);
                    nodes.forEach(function (n, i) { n.classList.toggle('active', i === activeIndex); });
                    nodes[activeIndex].scrollIntoView({ block: 'nearest' });
                }
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (activeIndex >= 0 && nodes[activeIndex]) {
                    selectNode(nodes[activeIndex]);
                }
            } else if (e.key === 'Escape') {
                hide();
            }
        });

        function selectNode(node) {
            const value = node.getAttribute('data-value');
            const label = node.getAttribute('data-label') || node.textContent || '';
            input.value = label;
            input._etlSelectedValue = value;
            hide();
            if (typeof callbacks.onSelect === 'function') callbacks.onSelect(value, label);
        }

        dropdown.addEventListener('click', function (e) {
            const item = e.target.closest('.etl-select-search-item');
            if (item) selectNode(item);
        });

        dropdown.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                const item = e.target.closest('.etl-select-search-item');
                if (item) {
                    e.preventDefault();
                    selectNode(item);
                }
            }
        });

        document.addEventListener('click', function (e) {
            if (!container.contains(e.target)) hide();
        });
    }

    /**
     * 步骤说明卡片
     * @param {number} stepIndex 当前步骤（1 起）
     * @param {Array<string|{title:string,text:string}>} descriptions
     * @returns {string} HTML
     */
    function renderStepDescription(stepIndex, descriptions) {
        const list = Array.isArray(descriptions) ? descriptions : [];
        const items = list.map(function (desc, i) {
            const idx = i + 1;
            const active = idx === stepIndex ? ' active' : '';
            let title, text;
            if (typeof desc === 'string') {
                title = '步骤 ' + idx;
                text = desc;
            } else {
                title = desc.title || ('步骤 ' + idx);
                text = desc.text || '';
            }
            return '<div class="etl-step-desc-item' + active + '">' +
                '<div class="mb-1"><span class="etl-step-desc-num">' + idx + '</span><strong>' + esc(title) + '</strong></div>' +
                '<div>' + esc(text) + '</div>' +
                '</div>';
        });
        return '<div class="etl-step-desc">' + items.join('') + '</div>';
    }

    window.etlComponents = {
        renderStepper: renderStepper,
        bindStepper: bindStepper,
        renderTree: renderTree,
        bindTreePick: bindTreePick,
        renderDebugPanel: renderDebugPanel,
        renderTimeline: renderTimeline,
        renderPagination: renderPagination,
        bindPagination: bindPagination,
        renderSelectSearch: renderSelectSearch,
        bindSelectSearch: bindSelectSearch,
        renderStepDescription: renderStepDescription,
        toast: toast,
        fmtTime: fmtTime,
        fmtDuration: fmtDuration,
        fmtCron: fmtCron,
        nextCronRun: nextCronRun,
        esc: esc
    };
})();
