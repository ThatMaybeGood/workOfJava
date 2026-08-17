/**
 * 调试运行页面
 */
const DebugApp = {
    tasks: [],
    currentTask: null,
    currentStep: 0, // 0=未开始, 1=抽取, 2=转换, 3=写入

    render(container) {
        container.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0"><i class="bi bi-bug"></i> 逐环节调试</h5>
                        </div>
                        <div class="card-body">
                            <div class="row mb-4">
                                <div class="col-md-4">
                                    <label>选择任务</label>
                                    <select class="form-control" id="debug-task-select" onchange="DebugApp.onTaskChange()">
                                        <option value="">-- 选择任务 --</option>
                                    </select>
                                </div>
                                <div class="col-md-4 d-flex align-items-end">
                                    <button class="btn btn-primary me-2" onclick="DebugApp.runAll()">
                                        <i class="bi bi-play-fill"></i> 全链路调试
                                    </button>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" id="debug-dryrun" checked>
                                        <label class="form-check-label">Dry-run（不真实写库）</label>
                                    </div>
                                </div>
                            </div>

                            <div id="debug-steps" style="display:none;">
                                <div class="step-indicator">
                                    <div class="step" id="step-1"><div class="step-circle">1</div><div class="ms-2">抽取</div></div>
                                    <div class="step-line"></div>
                                    <div class="step" id="step-2"><div class="step-circle">2</div><div class="ms-2">转换</div></div>
                                    <div class="step-line"></div>
                                    <div class="step" id="step-3"><div class="step-circle">3</div><div class="ms-2">写入</div></div>
                                </div>

                                <div class="row">
                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 mb-2" onclick="DebugApp.extract()">① 调试抽取</button>
                                        <div class="debug-result" id="extract-result"></div>
                                    </div>
                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 mb-2" onclick="DebugApp.transform()">② 调试转换</button>
                                        <div class="debug-result" id="transform-result"></div>
                                    </div>
                                    <div class="col-md-4">
                                        <button class="btn btn-outline-primary w-100 mb-2" onclick="DebugApp.load()">③ 调试写入</button>
                                        <div class="debug-result" id="load-result"></div>
                                    </div>
                                </div>

                                <div class="mt-3">
                                    <label>样例数据预览：</label>
                                    <pre id="debug-preview" class="bg-light p-3 rounded" style="max-height:300px;overflow:auto;"></pre>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        this.loadTasks();
    },

    async loadTasks() {
        const res = await etlApi.get('/task/list?page=1&size=100');
        const select = document.getElementById('debug-task-select');
        if (res.result?.success) {
            (res.body?.records || []).forEach(t => {
                select.innerHTML += `<option value="${t.id}">${t.name}</option>`;
            });
        }
    },

    onTaskChange() {
        this.currentTask = document.getElementById('debug-task-select').value;
        document.getElementById('debug-steps').style.display = this.currentTask ? 'block' : 'none';
    },

    async extract() {
        if (!this.currentTask) return;
        const el = document.getElementById('extract-result');
        el.innerHTML = '<span class="text-muted">抽取中...</span>';
        try {
            const res = await etlApi.post('/debug/extract', { taskId: this.currentTask, batchSize: 5 });
            if (res.result?.success) {
                const r = res.body;
                el.innerHTML = `<span class="text-success">✓ ${r.totalRows} 行, ${r.durationMs}ms</span>`;
                this.currentExtract = r;
                document.getElementById('debug-preview').textContent = JSON.stringify(r.rows, null, 2);
                this.highlightStep(1);
            } else {
                el.innerHTML = `<span class="text-danger">✗ ${res.result?.subMsg}</span>`;
            }
        } catch (e) {
            el.innerHTML = `<span class="text-danger">✗ 网络错误</span>`;
        }
    },

    async transform() {
        if (!this.currentExtract) { alert('请先执行抽取'); return; }
        const el = document.getElementById('transform-result');
        el.innerHTML = '<span class="text-muted">转换中...</span>';
        try {
            const res = await etlApi.post('/debug/transform', {
                taskId: this.currentTask,
                sampleRows: this.currentExtract.rows
            });
            if (res.result?.success) {
                const r = res.body;
                el.innerHTML = `<span class="text-success">✓ ${r.sampleRows?.length || 0} 行, ${r.durationMs}ms</span>`;
                this.currentTransformed = r.sampleRows;
                document.getElementById('debug-preview').textContent = JSON.stringify(r.sampleRows, null, 2);
                this.highlightStep(2);
            } else {
                el.innerHTML = `<span class="text-danger">✗ ${res.result?.subMsg}</span>`;
            }
        } catch (e) {
            el.innerHTML = `<span class="text-danger">✗ 网络错误</span>`;
        }
    },

    async load() {
        if (!this.currentTransformed) { alert('请先执行转换'); return; }
        const dryRun = document.getElementById('debug-dryrun').checked;
        const el = document.getElementById('load-result');
        el.innerHTML = `<span class="text-muted">${dryRun ? 'Dry-run' : '真实写入'}...</span>`;
        try {
            const res = await etlApi.post('/debug/load', {
                taskId: this.currentTask,
                rows: this.currentTransformed,
                dryRun: dryRun
            });
            if (res.result?.success) {
                const r = res.body;
                el.innerHTML = `<span class="text-success">✓ 写入 ${r.writtenRows} 行, ${r.durationMs}ms</span>`;
                this.highlightStep(3);
            } else {
                el.innerHTML = `<span class="text-danger">✗ ${res.result?.subMsg}</span>`;
            }
        } catch (e) {
            el.innerHTML = `<span class="text-danger">✗ 网络错误</span>`;
        }
    },

    async runAll() {
        if (!this.currentTask) { alert('请选择任务'); return; }
        const dryRun = document.getElementById('debug-dryrun').checked;
        // 依次执行三步
        await this.extract();
        if (!this.currentExtract) return;
        await this.transform();
        if (!this.currentTransformed) return;
        await this.load();
    },

    highlightStep(step) {
        this.currentStep = step;
        for (let i = 1; i <= 3; i++) {
            const el = document.getElementById(`step-${i}`);
            el.className = 'step' + (i < step ? ' completed' : i === step ? ' active' : '');
        }
    }
};