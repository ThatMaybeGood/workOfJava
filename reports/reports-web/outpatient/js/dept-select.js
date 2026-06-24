/**
 * 科室下拉框公共初始化工具
 * 页面打开时请求 reports.common.dept-dict 接口，动态渲染科室 select
 *
 * 使用方式：
 *   initDeptSelect({
 *       selectId: 'deptSelect',
 *       deptType: 0,          // 0 门诊，1 住院，2 其他
 *       deptCode: '0000',     // 默认全部
 *       deptName: '',
 *       showAll: true,        // 是否显示「全部」选项
 *       allCode: '0000',      // 「全部」对应的 code
 *       allText: '全部',
 *       onChange: (dept) => { console.log(dept); },
 *       extParams: {}         // 三个扩展参数对象，会平铺到请求 body
 *   });
 *
 * 返回 Promise，resolve 时返回当前选中的科室对象 { deptCode, deptName }
 */
async function initDeptSelect(options = {}) {
    const {
        selectId = 'deptSelect',
        deptType = 0,
        deptCode = '',
        deptName = '',
        showAll = true,
        allCode = '0000',
        allText = '全部',
        onChange = null,
        triggerDefault = true,
        extParams = {}
    } = options;

    const select = document.getElementById(selectId);
    if (!select) {
        console.warn(`[initDeptSelect] 未找到 select 元素: ${selectId}`);
        return null;
    }

    try {
        const params = {
            deptType,
            deptCode,
            deptName,
            ...extParams
        };
        const body = await ReportAPI.getDeptDict(params);
        const list = (body && body.list) ? body.list : [];

        // 如果接口返回里不包含「全部」，且需要显示，则手动前置
        let optionsList = [...list];
        if (showAll && !optionsList.some(item => item.deptCode === allCode)) {
            optionsList.unshift({ deptCode: allCode, deptName: allText, deptType });
        }

        let initializing = true;
        select.innerHTML = '';
        optionsList.forEach(item => {
            const option = document.createElement('option');
            option.value = item.deptCode;
            option.textContent = item.deptName;
            option.dataset.deptType = item.deptType || deptType;
            option.dataset.deptName = item.deptName;
            select.appendChild(option);
        });

        // change 事件：返回 { deptCode, deptName, deptType }
        select.addEventListener('change', (e) => {
            if (initializing) return;
            const selectedOption = e.target.selectedOptions[0];
            const selected = {
                deptCode: e.target.value,
                deptName: selectedOption ? selectedOption.dataset.deptName : '',
                deptType: selectedOption ? (selectedOption.dataset.deptType || deptType) : deptType
            };
            if (typeof onChange === 'function') {
                onChange(selected);
            }
        });

        const defaultOption = select.options[0];

        // 触发一次默认选中回调
        if (triggerDefault) {
            if (defaultOption && typeof onChange === 'function') {
                onChange({
                    deptCode: defaultOption.value,
                    deptName: defaultOption.dataset.deptName || defaultOption.textContent,
                    deptType: defaultOption.dataset.deptType || deptType
                });
            }
        }

        initializing = false;

        return {
            deptCode: select.value,
            deptName: defaultOption ? (defaultOption.dataset.deptName || defaultOption.textContent) : '',
            deptType: defaultOption ? (defaultOption.dataset.deptType || deptType) : deptType
        };
    } catch (error) {
        console.error('[initDeptSelect] 加载科室字典失败:', error);
        // 失败时保留一个「全部」兜底
        select.innerHTML = `<option value="${allCode}">${allText}</option>`;
        return { deptCode: allCode, deptName: allText, deptType };
    }
}
