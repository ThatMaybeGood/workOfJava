/**
 * ETL 向导状态存储
 * - sessionStorage 持久化（key='etl-wizard'）
 * - 状态结构：{sourceId, source, structure, transforms, mappings, task, step}
 * - 全局导出 window.etlStore
 */
(function () {
    const STORAGE_KEY = 'etl-wizard';

    const DEFAULT_STATE = {
        sourceId: null,
        source: null,
        structure: null,
        transforms: [],
        mappings: [],
        task: null,
        step: 1
    };

    function read() {
        try {
            const raw = sessionStorage.getItem(STORAGE_KEY);
            if (!raw) return Object.assign({}, DEFAULT_STATE);
            const parsed = JSON.parse(raw);
            return Object.assign({}, DEFAULT_STATE, parsed);
        } catch (e) {
            return Object.assign({}, DEFAULT_STATE);
        }
    }

    function write(state) {
        try {
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
        } catch (e) {
            /* sessionStorage 不可用时静默降级为内存态 */
        }
    }

    window.etlStore = {
        /** 读取完整向导状态 */
        get() {
            return read();
        },

        /** 合并写入补丁，返回合并后的状态 */
        set(patch) {
            const next = Object.assign(read(), patch || {});
            write(next);
            return next;
        },

        /** 清空向导状态 */
        clear() {
            try {
                sessionStorage.removeItem(STORAGE_KEY);
            } catch (e) { /* ignore */ }
        },

        /** 当前步骤号（1 起） */
        getStep() {
            return read().step || 1;
        },

        /** 设置当前步骤号 */
        setStep(n) {
            const next = read();
            next.step = n;
            write(next);
        }
    };
})();
