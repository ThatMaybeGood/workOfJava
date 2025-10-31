# 模块化文件结构说明

## 概述

项目已从单一的大文件拆分为模块化的独立文件，便于维护和修改。

## 目录结构

```
static/
├── index.html                 # 主HTML文件
├── js/                       # JavaScript模块目录
│   ├── app-state.js         # 应用状态管理
│   ├── utils.js             # 工具函数（格式化、加载提示等）
│   ├── view-switcher.js     # 视图切换管理
│   ├── table-view.js        # 表格视图功能
│   ├── param-view.js        # 参数维护视图功能
│   ├── holiday-view.js      # 节假日维护视图功能
│   ├── export-handlers.js   # 导出功能（打印、Excel、PDF）
│   ├── app-init.js          # 应用初始化
│   └── main.js              # 主入口文件
├── css/                      # CSS样式模块目录
│   ├── base.css            # 基础样式
│   ├── layout.css          # 布局样式
│   ├── components.css      # 组件样式（按钮、表单等）
│   ├── table.css           # 表格样式
│   ├── param.css           # 参数维护样式
│   ├── holiday.css         # 节假日维护样式
│   └── print.css           # 打印样式
└── views/                    # 视图HTML文件目录
    ├── main-content.html    # 主内容容器（已简化）
    ├── table-view.html      # 表格视图HTML
    ├── param-view.html      # 参数维护视图HTML
    └── holiday-view.html    # 节假日维护视图HTML
```

## 模块说明

### JavaScript模块

#### 1. `app-state.js`
- **功能**: 应用全局状态管理
- **内容**: AppState 对象，包含当前视图、参数、节假日、表格数据等状态

#### 2. `utils.js`
- **功能**: 通用工具函数
- **内容**: 
  - `formatNumber()` - 数字格式化
  - `showLoading()` / `hideLoading()` - 加载提示
  - `updateTimestamp()` - 更新时间戳
  - `showErrorView()` - 显示错误视图

#### 3. `view-switcher.js`
- **功能**: 视图切换逻辑
- **内容**: `switchView()` 函数，负责在不同视图间切换

#### 4. `table-view.js`
- **功能**: 表格视图相关功能
- **内容**:
  - `initializeTableView()` - 初始化表格视图
  - `loadCashStatistics()` - 加载统计数据
  - `renderTable()` - 渲染表格数据
  - `showErrorMessage()` - 显示错误信息

#### 5. `param-view.js`
- **功能**: 参数维护视图相关功能
- **内容**:
  - `initializeParamView()` - 初始化参数视图
  - `loadParams()` - 加载参数
  - `saveParams()` - 保存参数
  - `updateParamDisplay()` - 更新参数显示

#### 6. `holiday-view.js`
- **功能**: 节假日维护视图相关功能
- **内容**:
  - `initializeHolidayView()` - 初始化节假日视图
  - `loadHolidays()` - 加载节假日
  - `renderCalendar()` - 渲染日历
  - `toggleHoliday()` - 切换节假日状态

#### 7. `export-handlers.js`
- **功能**: 导出功能处理
- **内容**:
  - `handlePrint()` - 打印处理
  - `handleExportExcel()` - Excel导出
  - `handleExportPdf()` - PDF导出

#### 8. `app-init.js`
- **功能**: 应用初始化逻辑
- **内容**:
  - `loadMainContent()` - 加载主内容区域
  - `initializeEventListeners()` - 初始化事件监听
  - `toggleFullscreen()` - 全屏切换
  - `initializeApp()` - 应用主初始化函数

#### 9. `main.js`
- **功能**: 应用入口
- **内容**: DOMContentLoaded 事件监听，启动应用

### CSS模块

#### 1. `base.css`
- 基础样式（重置、body、加载动画）

#### 2. `layout.css`
- 布局样式（header、sidebar、main-content、footer）

#### 3. `components.css`
- 组件样式（按钮、表单、空状态等）

#### 4. `table.css`
- 表格专用样式

#### 5. `param.css`
- 参数维护视图专用样式

#### 6. `holiday.css`
- 节假日维护视图专用样式（日历、节假日列表等）

#### 7. `print.css`
- 打印样式和响应式样式

### 视图HTML模块

#### 1. `table-view.html`
- 表格视图的HTML结构

#### 2. `param-view.html`
- 参数维护视图的HTML结构

#### 3. `holiday-view.html`
- 节假日维护视图的HTML结构

## 使用说明

### 修改功能
- **修改表格功能**: 编辑 `js/table-view.js`
- **修改参数功能**: 编辑 `js/param-view.js`
- **修改节假日功能**: 编辑 `js/holiday-view.js`
- **修改导出功能**: 编辑 `js/export-handlers.js`

### 修改样式
- **修改基础样式**: 编辑 `css/base.css`
- **修改布局**: 编辑 `css/layout.css`
- **修改表格样式**: 编辑 `css/table.css`
- **修改特定视图样式**: 编辑对应的 CSS 文件

### 修改视图HTML
- **修改表格视图**: 编辑 `views/table-view.html`
- **修改参数视图**: 编辑 `views/param-view.html`
- **修改节假日视图**: 编辑 `views/holiday-view.html`

## 注意事项

1. **JavaScript加载顺序**: 在 `index.html` 中，JavaScript文件必须按照依赖顺序加载（见上面的文件列表顺序）

2. **CSS加载顺序**: CSS文件可以按任意顺序加载，但建议保持当前顺序以保证样式优先级

3. **视图加载**: 视图文件通过 `app-init.js` 中的 `loadMainContent()` 函数动态加载

4. **向后兼容**: 原有的 `script.js` 和 `styles.css` 文件可以保留作为备份，但不建议继续使用

## 优势

1. **易于维护**: 每个模块职责明确，修改某个功能时只需编辑对应的文件
2. **代码清晰**: 文件结构清晰，便于理解代码组织
3. **便于协作**: 多人开发时可以减少代码冲突
4. **易于扩展**: 添加新功能时只需创建新的模块文件

