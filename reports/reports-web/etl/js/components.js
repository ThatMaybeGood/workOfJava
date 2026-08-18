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
     * @returns {string} HTML
     */
    function renderStepper(currentStep, steps) {
        const list = steps && steps.length ? steps : [
            { key: 'source', name: '抽取来源' },
            { key: 'transform', name: '转换提取' },
            { key: 'mapping', name: '映射匹配' }
        ];
        const items = list.map((s, i) => {
            const idx = i + 1;
            const state = idx < currentStep ? 'done' : (idx === currentStep ? 'active' : '');
            const stationState = state === 'done' ? 'done' : (state === 'active' ? 'active' : '');
            const mark = state === 'done' ? '&#10003;' : String(idx).padStart(2, '0');
            return '<div class="etl-step ' + state + '">' +
                '<div class="etl-step-track ' + (idx <= currentStep ? 'done' : '') + '"></div>' +
                '<div class="etl-step-station ' + stationState + '">' + mark + '</div>' +
                '<div class="etl-step-eyebrow">STEP ' + String(idx).padStart(2, '0') + '</div>' +
                '<div class="etl-step-name">' + esc(s.name) + '</div>' +
                '</div>';
        });
        return '<div class="etl-stepper">' + items.join('') + '</div>';
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

    window.etlComponents = {
        renderStepper: renderStepper,
        renderTree: renderTree,
        bindTreePick: bindTreePick,
        renderDebugPanel: renderDebugPanel,
        renderTimeline: renderTimeline,
        toast: toast,
        fmtTime: fmtTime,
        fmtDuration: fmtDuration,
        esc: esc
    };
})();
