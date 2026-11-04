package com.reports.mapper;

import com.reports.entity.MiReconDetailEntity;
import com.reports.entity.MiReconFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医保对账每日对比 Mapper(报表库)
 */
@Mapper
public interface MiReconMapper {

    /** 上传记录(最近100条, 新的在前) */
    List<MiReconFileEntity> queryFileList();

    /** 医保侧按结算日期聚合 */
    List<MiReconDetailEntity> queryMiAgg(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    int insertFile(MiReconFileEntity entity);

    int updateFileType(@Param("id") Long id, @Param("insurType") String insurType,
                       @Param("insurTypeName") String insurTypeName);

    /** 类型修改级联明细 */
    int cascadeDetailType(@Param("id") Long id, @Param("insurType") String insurType,
                          @Param("insurTypeName") String insurTypeName);

    /** 同类型同结算日期以最新上传为准: 先删后插 */
    int deleteDetailByTypeAndDates(@Param("insurType") String insurType,
                                   @Param("settleDates") List<String> settleDates);

    int insertDetail(MiReconDetailEntity entity);
}
