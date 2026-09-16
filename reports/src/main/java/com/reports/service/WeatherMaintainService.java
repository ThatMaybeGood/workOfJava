package com.reports.service;

import com.reports.dto.common.PageResult;
import com.reports.dto.request.WeatherMaintainRequest;
import com.reports.dto.response.outpatient.forecast.WeatherItem;

/**
 * 天气数据维护服务
 */
public interface WeatherMaintainService {

    /**
     * 查询天气维护明细
     *
     * @param request  查询条件
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<WeatherItem> queryMaintainList(WeatherMaintainRequest request, Integer page, Integer pageSize);

    /**
     * 保存天气明细（按日期覆盖保存，来源记为人工登记）
     *
     * @param request 保存内容
     * @return 影响行数
     */
    int saveMaintain(WeatherMaintainRequest request);

    /**
     * 按日期删除天气
     *
     * @param request 删除条件(weatherDate)
     * @return 影响行数
     */
    int deleteMaintain(WeatherMaintainRequest request);
}
