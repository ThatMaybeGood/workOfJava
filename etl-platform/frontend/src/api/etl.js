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
  listTables: (dsName) => request(`${API_BASE}datasource/${encodeURIComponent(dsName)}/tables`),
  listColumns: (dsName, tableName) =>
    request(`${API_BASE}datasource/${encodeURIComponent(dsName)}/table/${encodeURIComponent(tableName)}/columns`),
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
  getAllLogs: (limit = 100) => request(`${API_BASE}monitor/logs/all?limit=${limit}`),
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

// === Pipeline API (v2) ===
export const PipelineAPI = {
  list: () => request(`${API_BASE}pipeline`),
  get: (id) => request(`${API_BASE}pipeline/${id}`),
  save: (data) => {
    const isEdit = data.id;
    return request(`${API_BASE}pipeline${isEdit ? '/' + data.id : ''}`, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  delete: (id) => request(`${API_BASE}pipeline/${id}`, { method: 'DELETE' }),
  execute: (pipelineCode) => request(`${API_BASE}pipeline/${pipelineCode}/execute`, { method: 'POST' }),
  debug: (pipelineCode, { limit = 100, write = false } = {}) =>
    request(`${API_BASE}pipeline/${pipelineCode}/debug?limit=${limit}&write=${write}`, { method: 'POST' }),
  debugExtract: (pipelineCode, limit = 100) =>
    request(`${API_BASE}pipeline/${pipelineCode}/debug/extract?limit=${limit}`, { method: 'POST' }),
  schedule: (pipelineCode) => request(`${API_BASE}pipeline/${pipelineCode}/schedule`, { method: 'POST' }),
  pause: (pipelineCode) => request(`${API_BASE}pipeline/${pipelineCode}/pause`, { method: 'POST' }),
  resume: (pipelineCode) => request(`${API_BASE}pipeline/${pipelineCode}/resume`, { method: 'POST' }),
  removeSchedule: (pipelineCode) => request(`${API_BASE}pipeline/${pipelineCode}/remove-schedule`, { method: 'POST' }),
  reloadSchedules: () => request(`${API_BASE}pipeline/reload-schedules`, { method: 'POST' }),
};

export const StepAPI = {
  listByPipeline: (pipelineId) => request(`${API_BASE}step/pipeline/${pipelineId}`),
  save: (data) => {
    const isEdit = data.id;
    return request(`${API_BASE}step${isEdit ? '/' + data.id : ''}`, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  delete: (id) => request(`${API_BASE}step/${id}`, { method: 'DELETE' }),
  getMappings: (stepId) => request(`${API_BASE}step/${stepId}/mappings`),
  saveMapping: (stepId, data) => {
    const isEdit = data.id;
    const url = `${API_BASE}step/${stepId}/mapping${isEdit ? '/' + data.id : ''}`;
    return request(url, {
      method: isEdit ? 'PUT' : 'POST',
      body: JSON.stringify(data),
    });
  },
  deleteMapping: (stepId, mappingId) => request(`${API_BASE}step/${stepId}/mapping/${mappingId}`, { method: 'DELETE' }),
};

// ── 抽取测试请求构造（从管线步骤的 sourceConfig 解析，供映射页/管线页复用）──
export function getStepSourceConfig(step, key, defaultVal = '') {
  try { const c = JSON.parse(step.sourceConfig || '{}'); return c[key] !== undefined ? c[key] : defaultVal; }
  catch { return defaultVal; }
}

export function buildExtractTestRequest(step, limit = 20) {
  return {
    sourceType: step.sourceType,
    dataSourceName: step.sourceDsName,
    tableName: getStepSourceConfig(step, 'sourceTable'),
    procedureName: getStepSourceConfig(step, 'sourceProcedure'),
    sqlText: getStepSourceConfig(step, 'sourceSql'),
    viewName: getStepSourceConfig(step, 'sourceView'),
    sourceParams: getStepSourceConfig(step, 'sourceParams'),
    url: getStepSourceConfig(step, 'httpUrl'),
    httpMethod: getStepSourceConfig(step, 'httpMethod', 'GET'),
    headers: getStepSourceConfig(step, 'httpHeaders'),
    requestBody: getStepSourceConfig(step, 'httpBody'),
    authType: getStepSourceConfig(step, 'httpAuthType', 'NONE'),
    authUsername: getStepSourceConfig(step, 'httpUsername'),
    authPassword: getStepSourceConfig(step, 'httpPassword'),
    authToken: getStepSourceConfig(step, 'httpToken'),
    responseType: getStepSourceConfig(step, 'httpResponseType', 'JSON'),
    dataPath: getStepSourceConfig(step, 'httpDataPath'),
    pagination: getStepSourceConfig(step, 'httpPagination', 'N'),
    pageParam: getStepSourceConfig(step, 'httpPageParam', 'page'),
    sizeParam: getStepSourceConfig(step, 'httpSizeParam', 'size'),
    pageSize: parseInt(getStepSourceConfig(step, 'httpPageSize', '1000')),
    timeout: parseInt(getStepSourceConfig(step, 'httpTimeout', '30000')),
    soapAction: getStepSourceConfig(step, 'soapAction'),
    soapBinding: getStepSourceConfig(step, 'soapBinding', 'SOAP11'),
    soapNamespace: getStepSourceConfig(step, 'soapNamespace'),
    limit,
  };
}
