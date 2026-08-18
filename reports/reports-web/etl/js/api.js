/**
 * ETL API 统一封装
 * - baseURL 相对路径 '/api/etl'
 * - 响应体协议 {result:{code,success,msg,subMsg}, body}；result.success!==true 时 reject 并 toast 错误
 * - 网络异常同样 toast 并 reject
 * - 全局导出 window.etlApi，签名：get(path) / post(path, body) / put(path, body) / del(path)
 */
(function () {
    // 同源部署（http://host/etl/）走相对路径；file:// 直接打开时回退到本地后端
    const ETL_API_BASE = (window.location && window.location.protocol === 'file:')
        ? 'http://localhost:18089/api/etl'
        : '/api/etl';

    function showError(message) {
        if (window.etlComponents && typeof window.etlComponents.toast === 'function') {
            window.etlComponents.toast(message || '请求失败', 'err');
        } else {
            // components.js 未加载时的兜底：简易 toast
            let el = document.querySelector('.etl-toast');
            if (!el) {
                el = document.createElement('div');
                el.className = 'etl-toast err';
                document.body.appendChild(el);
            }
            el.textContent = message || '请求失败';
            el.classList.add('show');
            setTimeout(() => el.classList.remove('show'), 3000);
        }
    }

    async function request(method, path, body) {
        let res;
        try {
            const opts = { method, headers: {} };
            if (body !== undefined && body !== null) {
                opts.headers['Content-Type'] = 'application/json';
                opts.body = JSON.stringify(body);
            }
            res = await fetch(ETL_API_BASE + path, opts);
        } catch (e) {
            showError('网络异常：' + (e && e.message ? e.message : '无法连接服务器'));
            return Promise.reject(e);
        }

        let payload;
        try {
            payload = await res.json();
        } catch (e) {
            showError('服务响应异常（HTTP ' + res.status + '）');
            return Promise.reject(new Error('Invalid JSON response, HTTP ' + res.status));
        }

        // 后端统一协议：{result:{code,success,msg,subMsg}, body}
        if (payload && typeof payload === 'object' && payload.result && typeof payload.result === 'object') {
            const r = payload.result;
            if (r.success !== true) {
                showError(r.subMsg || r.msg || '请求失败（code=' + r.code + '）');
                return Promise.reject(new Error(r.subMsg || r.msg || 'code=' + r.code));
            }
            return payload.body;
        }

        // 非协议响应（兜底兼容）
        if (!res.ok) {
            showError('请求失败（HTTP ' + res.status + '）');
            return Promise.reject(new Error('HTTP ' + res.status));
        }
        return payload;
    }

    window.etlApi = {
        get(path) { return request('GET', path); },
        post(path, body) { return request('POST', path, body); },
        put(path, body) { return request('PUT', path, body); },
        del(path) { return request('DELETE', path); }
    };
})();
