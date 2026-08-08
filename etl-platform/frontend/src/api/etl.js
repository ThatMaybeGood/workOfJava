const API_BASE = '/api/etl/';

async function request(url, options = {}) {
  const token = localStorage.getItem('etl_token');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };
  const response = await fetch(url, { ...options, headers });
  const data = await response.json();
  if (!data.success) {
    throw new Error(data.message || '请求失败');
  }
  return data;
}

export const DataSourceAPI = {
  list: () => request(`${API_BASE}datasource`),
  listEnabled: () => request(`${API_BASE}datasource/enabled`),
  get: (id) => request(`${API_BASE}datasource/${id}`),
  save: (data) => {
    const isEdit = data.id;
    return request(`${API_BASE}datasource${isEdit ? '/' + data.id : ''}`, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  delete: (id) => request(`${API_BASE}datasource/${id}`, { method: 'DELETE' }),
  test: (id) => request(`${API_BASE}datasource/${id}/test`),
};

export const TaskAPI = {
  list: () => request(`${API_BASE}task`),
  get: (id) => request(`${API_BASE}task/${id}`),
  save: (data) => {
    const isEdit = data.id;
    return request(`${API_BASE}task${isEdit ? '/' + data.id : ''}`, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  delete: (id) => request(`${API_BASE}task/${id}`, { method: 'DELETE' }),
  execute: (taskCode) => request(`${API_BASE}task/${taskCode}/execute`, { method: 'POST' }),
  schedule: (taskCode) => request(`${API_BASE}task/${taskCode}/schedule`, { method: 'POST' }),
  pause: (taskCode) => request(`${API_BASE}task/${taskCode}/pause`, { method: 'POST' }),
  resume: (taskCode) => request(`${API_BASE}task/${taskCode}/resume`, { method: 'POST' }),
  reloadSchedules: () => request(`${API_BASE}task/reload-schedules`, { method: 'POST' }),
  preview: (taskCode, limit) => request(`${API_BASE}task/${taskCode}/preview/${limit}`),
  debug: (taskCode, { limit = 100, write = false } = {}) =>
    request(`${API_BASE}task/${taskCode}/debug?limit=${limit}&write=${write}`, { method: 'POST' }),
  debugExtract: (taskCode, limit = 100) =>
    request(`${API_BASE}task/${taskCode}/debug/extract?limit=${limit}`, { method: 'POST' }),
};

export const MappingAPI = {
  list: (taskCode) => request(`${API_BASE}mapping/task/${taskCode}`),
  get: (id) => request(`${API_BASE}mapping/${id}`),
  save: (data) => {
    const isEdit = data.id;
    return request(`${API_BASE}mapping${isEdit ? '/' + data.id : ''}`, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  delete: (id) => request(`${API_BASE}mapping/${id}`, { method: 'DELETE' }),
};

export const MonitorAPI = {
  getLogs: (taskCode) => request(`${API_BASE}monitor/logs/task/${taskCode}`),
  getLogByExecutionId: (executionId) => request(`${API_BASE}monitor/logs/execution/${executionId}`),
  getRunningTasks: () => request(`${API_BASE}monitor/logs/running`),
  getDashboard: () => request(`${API_BASE}monitor/dashboard`),
};

export const ExtractAPI = {
  test: (data) => request(`${API_BASE}extract/test`, {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  raw: (data) => request(`${API_BASE}extract/raw`, {
    method: 'POST',
    body: JSON.stringify(data),
  }),
};
