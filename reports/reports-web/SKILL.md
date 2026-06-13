---
name: bootstrap-report-page
description: 基于 Bootstrap 5 的前端报表页面开发规范。适用于需要高保真还原原型设计、构建数据报表类页面的场景，涵盖 UI 层、数据层、逻辑层的完整开发规范。
---

# Bootstrap 报表页面开发规范

## 概述

本规范定义了使用 Bootstrap 5 开发前端报表页面的标准流程和技术要求，确保页面高保真还原原型设计，同时保证代码的可维护性和可扩展性。

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| UI 层 | Bootstrap 5 + Bootstrap Icons | 组件库和图标库 |
| 数据层 | fetch / axios | 网络请求（默认 fetch，复杂场景用 axios） |
| 逻辑层 | 原生 JavaScript / jQuery | 页面交互逻辑 |

## 1. UI 层规范

### 1.1 引入方式

通过 CDN 引入 Bootstrap 5 和 Bootstrap Icons：

```html
<!-- Bootstrap 5 CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
<!-- Bootstrap Icons -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
```

### 1.2 布局规范

使用 Bootstrap 栅格系统实现响应式布局：

```html
<!-- 页面容器 -->
<div class="container-fluid py-4">
    <!-- 标题区 -->
    <div class="page-title">页面标题</div>
    
    <!-- 筛选区 -->
    <div class="filter-section">
        <!-- 使用 flex 布局排列筛选条件 -->
        <div class="d-flex align-items-center gap-4 flex-wrap">
            <!-- 筛选项 -->
        </div>
    </div>
    
    <!-- 统计卡片区 -->
    <div class="row g-3">
        <div class="col-md-4">
            <!-- 统计卡片 -->
        </div>
    </div>
    
    <!-- 表格区 -->
    <div class="table-section">
        <!-- 表格内容 -->
    </div>
</div>
```

### 1.3 组件使用规范

#### 按钮
```html
<!-- 主按钮 -->
<button class="btn btn-primary">主要按钮</button>
<!-- 导出按钮（带图标） -->
<button class="btn btn-primary btn-sm" onclick="exportData()">
    <i class="bi bi-download me-1"></i>导出Excel
</button>
```

#### 卡片
```html
<div class="card stat-card">
    <div class="card-body d-flex align-items-center">
        <div class="stat-icon blue">
            <i class="bi bi-bar-chart-fill"></i>
        </div>
        <div class="ms-3">
            <div class="stat-value">12,536</div>
            <div class="stat-label">门诊量</div>
        </div>
    </div>
</div>
```

#### 表格
```html
<div class="table-responsive">
    <table class="table table-hover">
        <thead class="table-light">
            <tr>
                <th>列标题</th>
            </tr>
        </thead>
        <tbody>
            <!-- 数据行 -->
        </tbody>
    </table>
</div>
```

#### 分页
```html
<nav aria-label="Page navigation">
    <ul class="pagination">
        <li class="page-item"><a class="page-link" href="#">上一页</a></li>
        <li class="page-item active"><a class="page-link" href="#">1</a></li>
        <li class="page-item"><a class="page-link" href="#">下一页</a></li>
    </ul>
</nav>
```

### 1.4 图标使用规范

统一使用 Bootstrap Icons，禁止混用其他图标库：

```html
<!-- 统计图标 -->
<i class="bi bi-bar-chart-fill"></i>      <!-- 柱状图 -->
<i class="bi bi-calendar-check-fill"></i> <!-- 日历 -->
<i class="bi bi-people-fill"></i>          <!-- 人群 -->
<i class="bi bi-clipboard-check-fill"></i> <!-- 剪贴板 -->
<i class="bi bi-speedometer2"></i>         <!-- 仪表盘 -->
<i class="bi bi-hospital-fill"></i>        <!-- 医院 -->
<i class="bi bi-download"></i>             <!-- 下载 -->
<i class="bi bi-sort-up"></i>              <!-- 排序 -->
<i class="bi bi-info-circle"></i>          <!-- 信息提示 -->
```

### 1.5 颜色规范

使用 CSS 变量定义项目色板，保持与原型一致：

```css
:root {
    --primary-color: #1890ff;
    --primary-dark: #096dd9;
    --success-color: #52c41a;
    --warning-color: #fa8c16;
    --danger-color: #f5222d;
    --cyan-color: #13c2c2;
    --purple-color: #722ed1;
    --bg-color: #f0f2f5;
    --card-bg: #ffffff;
    --text-primary: #262626;
    --text-secondary: #8c8c8c;
    --border-color: #d9d9d9;
}
```

## 2. 数据层规范

### 2.1 默认使用 fetch

简单场景直接使用浏览器内置 fetch API：

```javascript
/**
 * 使用 fetch 获取数据
 * @param {string} url - 接口地址
 * @param {Object} options - 请求配置
 * @returns {Promise} 返回 Promise 对象
 */
async function fetchData(url, options = {}) {
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}
```

### 2.2 复杂场景使用 axios

需要拦截器、取消请求、自动重试等高级功能时，使用 axios：

```javascript
/**
 * axios 封装实例
 * 支持请求拦截、响应拦截、错误处理
 */
class HttpClient {
    constructor() {
        this.instance = axios.create({
            baseURL: '/api',
            timeout: 10000,
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        // 请求拦截器
        this.instance.interceptors.request.use(
            config => {
                // 添加 token、loading 等
                return config;
            },
            error => Promise.reject(error)
        );
        
        // 响应拦截器
        this.instance.interceptors.response.use(
            response => response.data,
            error => {
                // 统一错误处理
                console.error('Request error:', error);
                return Promise.reject(error);
            }
        );
    }
    
    get(url, params) {
        return this.instance.get(url, { params });
    }
    
    post(url, data) {
        return this.instance.post(url, data);
    }
}

const http = new HttpClient();
```

### 2.3 Mock 数据规范

开发阶段使用 Mock 数据模拟接口：

```javascript
/**
 * Mock 数据服务
 * 模拟后端接口返回数据
 */
const MockService = {
    /**
     * 获取表格数据
     * @param {Object} params - 查询参数
     * @returns {Promise} 返回模拟数据
     */
    getTableData(params = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        list: [
                            { id: 1, name: '示例数据', value: 100 }
                        ],
                        total: 100,
                        page: params.page || 1,
                        pageSize: params.pageSize || 10
                    }
                });
            }, 300);
        });
    }
};
```

## 3. 逻辑层规范

### 3.1 原生 JavaScript 开发

优先使用原生 JavaScript 处理页面逻辑：

```javascript
/**
 * 页面逻辑控制器
 * 管理页面状态、事件绑定、数据渲染
 */
class ReportController {
    constructor() {
        // 页面状态
        this.state = {
            currentPage: 1,
            pageSize: 10,
            total: 0,
            data: [],
            sortColumn: null,
            sortDirection: 'asc'
        };
        
        // 初始化
        this.init();
    }
    
    /**
     * 初始化方法
     * 绑定事件、加载初始数据
     */
    init() {
        this.bindEvents();
        this.loadData();
    }
    
    /**
     * 绑定页面事件
     */
    bindEvents() {
        // 筛选按钮事件
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => this.handleFilter(e));
        });
        
        // 分页事件
        document.getElementById('prevBtn').addEventListener('click', () => this.prevPage());
        document.getElementById('nextBtn').addEventListener('click', () => this.nextPage());
    }
    
    /**
     * 加载数据
     */
    async loadData() {
        try {
            const result = await MockService.getTableData({
                page: this.state.currentPage,
                pageSize: this.state.pageSize
            });
            
            this.state.data = result.data.list;
            this.state.total = result.data.total;
            this.renderTable();
        } catch (error) {
            console.error('Load data failed:', error);
        }
    }
    
    /**
     * 渲染表格
     */
    renderTable() {
        const tbody = document.getElementById('tableBody');
        // 渲染逻辑...
    }
}

// 页面加载完成后初始化
new ReportController();
```

### 3.2 jQuery 辅助（可选）

在需要简化 DOM 操作时，可以使用 jQuery：

```javascript
/**
 * 使用 jQuery 简化事件绑定和 DOM 操作
 */
$(document).ready(function() {
    // 筛选按钮切换
    $('.filter-btn').on('click', function() {
        $(this).siblings().removeClass('active');
        $(this).addClass('active');
    });
    
    // 表格排序
    $('.sortable').on('click', function() {
        const column = $(this).data('column');
        // 排序逻辑...
    });
});
```

## 4. 开发流程

### 4.1 实现步骤

1. **分析原型**：仔细分析原型图的布局、颜色、字体、间距
2. **搭建结构**：使用 Bootstrap 栅格系统搭建页面骨架
3. **填充组件**：使用 Bootstrap 组件填充各功能区域
4. **自定义样式**：添加自定义 CSS 微调样式，确保与原型一致
5. **Mock 数据**：使用 Mock 数据模拟接口，实现数据交互
6. **添加交互**：实现筛选、排序、分页等交互功能
7. **联调接口**：替换 Mock 数据为真实接口

### 4.2 高保真还原要求

- **排版准确**：严格按照原型的布局结构，不得随意增删内容
- **颜色一致**：使用原型定义的颜色值，通过 CSS 变量管理
- **字体规范**：标题、正文、辅助文字的字号和字重与原型保持一致
- **间距统一**：padding、margin 按照原型设计执行
- **交互完整**：筛选、排序、分页、导出等功能必须实现

## 5. 注释规范

### 5.1 HTML 注释

```html
<!-- 筛选区：包含时间筛选和科室筛选 -->
<div class="filter-section">
    <!-- 时间筛选按钮组 -->
    <div class="filter-buttons">
        <button class="filter-btn active">今日</button>
    </div>
</div>
```

### 5.2 CSS 注释

```css
/**
 * 统计卡片样式
 * 包含图标、数值、标签的排版和颜色
 */
.stat-card {
    background: var(--card-bg);
    border-radius: 8px;
    padding: 20px;
}

/* 图标颜色定义 */
.stat-icon.blue { 
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); 
}
```

### 5.3 JavaScript 注释

```javascript
/**
 * 渲染表格数据
 * 根据当前页码和分页大小渲染对应数据
 * @param {Array} data - 表格数据数组
 * @param {number} page - 当前页码
 * @param {number} pageSize - 每页条数
 */
function renderTable(data, page, pageSize) {
    // 计算当前页数据起始索引
    const startIndex = (page - 1) * pageSize;
    
    // 截取当前页数据
    const pageData = data.slice(startIndex, startIndex + pageSize);
    
    // 生成表格 HTML
    const html = pageData.map(row => `<tr>...</tr>`).join('');
    
    // 更新表格内容
    document.getElementById('tableBody').innerHTML = html;
}
```

## 6. 文件结构

```
project/
├── index.html          # 主页面
├── css/
│   └── style.css       # 自定义样式
├── js/
│   ├── app.js          # 页面主逻辑
│   ├── api.js          # 接口封装
│   └── mock.js         # Mock 数据
└── assets/
    └── images/         # 图片资源
```

## 7. 示例代码

完整的报表页面示例请参考同目录下的 `index.html`，该示例实现了：
- Bootstrap 5 栅格布局
- 统计卡片展示
- 数据表格（支持排序）
- 分页功能
- 导出功能
- Mock 数据交互

## 参考资源

- [Bootstrap 5 中文文档](https://bootstrap.readdevdocs.com/docs/5.3/getting-started/introduction/)
- [Bootstrap Icons](https://icons.getbootstrap.com/)
- [Axios 文档](https://axios-http.com/)
