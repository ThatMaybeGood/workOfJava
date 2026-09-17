package com.reports.mapper;

import com.reports.entity.ServiceQualityCmplEntity;
import com.reports.entity.ServiceQualityPrzEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 门诊服务质量分析-人工登记表 Mapper
 * <p>
 * 明细列表的源库部分（yq_powersfp/院领导信箱）只查不写，走
 * {@link ServiceQualitySourceMapper}；本 mapper 只管人工登记表的增删改查。
 */
@Mapper
public interface ServiceQualityMapper {

    /**
     * 按日期范围查询人工登记的投诉记录（数据维护弹窗用）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 投诉记录
     */
    List<ServiceQualityCmplEntity> queryComplaintByDate(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate);

    /**
     * 按日期范围查询人工登记的表扬记录（数据维护弹窗用）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 表扬记录
     */
    List<ServiceQualityPrzEntity> queryPraiseByDate(@Param("startDate") Date startDate,
                                                    @Param("endDate") Date endDate);

    /**
     * 人工登记的投诉记录（报表列表用，按时间倒序）
     */
    List<ServiceQualityCmplEntity> listComplaint(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate);

    /**
     * 人工登记的表扬记录（报表列表用，按时间倒序）
     */
    List<ServiceQualityPrzEntity> listPraise(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);

    /**
     * 新增投诉记录
     */
    int insertComplaint(ServiceQualityCmplEntity entity);

    /**
     * 更新投诉记录
     */
    int updateComplaint(ServiceQualityCmplEntity entity);

    /**
     * 删除投诉记录
     */
    int deleteComplaintById(@Param("id") Long id);

    /**
     * 新增表扬记录
     */
    int insertPraise(ServiceQualityPrzEntity entity);

    /**
     * 更新表扬记录
     */
    int updatePraise(ServiceQualityPrzEntity entity);

    /**
     * 删除表扬记录
     */
    int deletePraiseById(@Param("id") Long id);
}
