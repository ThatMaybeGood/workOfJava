/**
 * ETL Platform API Client
 * Base URL: /api/etl/
 */

const API_BASE = '/api/etl/';

// Common request handler
async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    });
    const data = await response.json();
    if (!data.success) {
        throw new Error(data.message || '请求失败');
    }
    return data;
}

// ===== DataSource API =====
const DataSourceAPI = {
    async list() {
        return request(`${API_BASE}datasource`);
    },

    async listEnabled() {
        return request(`${API_BASE}datasource/enabled`);
    },

    async get(id) {
        return request(`${API_BASE}datasource/${id}`);
    },

    async save(data) {
        const isEdit = data.id;
        return request(`${API_BASE}datasource${isEdit ? '/' + data.id : ''}`, {
            method: isEdit ? 'PUT' : 'POST',
            body: JSON.stringify(data)
        });
    },

    async delete(id) {
        return request(`${API_BASE}datasource/${id}`, { method: 'DELETE' });
    },

    async test(id) {
        return request(`${API_BASE}datasource/${id}/test`);
    }
};

// ===== Task API =====
const TaskAPI = {
    async list() {
        return request(`${API_BASE}task`);
    },

    async get(id) {
        return request(`${API_BASE}task/${id}`);
    },

    async save(data) {
        const isEdit = data.id;
        return request(`${API_BASE}task${isEdit ? '/' + data.id : ''}`, {
            method: isEdit ? 'PUT' : 'POST',
            body: JSON.stringify(data)
        });
    },

    async delete(id) {
        return request(`${API_BASE}task/${id}`, { method: 'DELETE' });
    },

    async execute(taskCode) {
        return request(`${API_BASE}task/${taskCode}/execute`, { method: 'POST' });
    },

    async schedule(taskCode) {
        return request(`${API_BASE}task/${taskCode}/schedule`, { method: 'POST' });
    },

    async pause(taskCode) {
        return request(`${API_BASE}task/${taskCode}/pause`, { method: 'POST' });
    },

    async resume(taskCode) {
        return request(`${API_BASE}task/${taskCode}/resume`, { method: 'POST' });
    },

    async reloadSchedules() {
        return request(`${API_BASE}task/reload-schedules`, { method: 'POST' });
    },

    async preview(taskCode, limit) {
        return request(`${API_BASE}task/${taskCode}/preview/${limit}`);
    }
};

// ===== Mapping API =====
const MappingAPI = {
    async list(taskCode) {
        return request(`${API_BASE}mapping/task/${taskCode}`);
    },

    async get(id) {
        return request(`${API_BASE}mapping/${id}`);
    },

    async save(data) {
        const isEdit = data.id;
        return request(`${API_BASE}mapping${isEdit ? '/' + data.id : ''}`, {
            method: isEdit ? 'PUT' : 'POST',
            body: JSON.stringify(data)
        });
    },

    async delete(id) {
        return request(`${API_BASE}mapping/${id}`, { method: 'DELETE' });
    }
};

// ===== Monitor API =====
const MonitorAPI = {
    async getLogs(taskCode) {
        return request(`${API_BASE}monitor/logs/task/${taskCode}`);
    },

    async getLogByExecutionId(executionId) {
        return request(`${API_BASE}monitor/logs/execution/${executionId}`);
    },

    async getRunningTasks() {
        return request(`${API_BASE}monitor/logs/running`);
    },

    async getDashboard() {
        return request(`${API_BASE}monitor/dashboard`);
    }
};

// ===== Export =====
window.DataSourceAPI = DataSourceAPI;
window.TaskAPI = TaskAPI;
window.MappingAPI = MappingAPI;
window.MonitorAPI = MonitorAPI;
