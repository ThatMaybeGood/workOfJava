package com.reports.mapper;

import com.reports.entity.MiReconDetailEntity;
import com.reports.entity.MiReconFileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医保对账每日对比 Mapper(报表库)
 * 上传明细多版本共存(tr_mi_recon_detail不再物理删除), 生效性由tr_mi_recon_file.status控制,
 * 对比查询只统计启用文件(status='1')的明细
 */
@Mapper
public interface MiReconMapper {

    /** 上传记录分页(同类型排一起: 类型→数据日期倒序→上传时间倒序), Oracle 11g ROWNUM分页 */
    List<MiReconFileEntity> queryFileList(@Param("insurType") String insurType,
                                          @Param("dataDate") String dataDate,
                                          @Param("startRow") int startRow,
                                          @Param("endRow") int endRow);

    /** 上传记录总条数(带筛选) */
    int queryFileCount(@Param("insurType") String insurType,
                       @Param("dataDate") String dataDate);

    /** 删除上传记录的明细 */
    int deleteDetailByFileId(@Param("fileId") Long fileId);

    /** 删除上传记录 */
    int deleteFileById(@Param("id") Long id);

    /** 按id查上传记录 */
    MiReconFileEntity queryFileById(@Param("id") Long id);

    /** 医保侧按结算日期聚合(仅启用文件的明细) */
    List<MiReconDetailEntity> queryMiAgg(@Param("insurType") String insurType,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    int insertFile(MiReconFileEntity entity);

    int updateFileType(@Param("id") Long id, @Param("insurType") String insurType,
                       @Param("insurTypeName") String insurTypeName);

    /** 启用/作废上传记录 */
    int updateFileStatus(@Param("id") Long id, @Param("status") String status);

    /** 类型修改级联明细 */
    int cascadeDetailType(@Param("id") Long id, @Param("insurType") String insurType,
                          @Param("insurTypeName") String insurTypeName);

    /** 某上传记录的明细结算日期(yyyy-MM-dd字符串) */
    List<String> queryDetailDatesByFileId(@Param("fileId") Long fileId);

    /** 作废同类型下与给定结算日期重叠的其他启用文件(上传/启用切换时保持同一类型同一天只有一个启用版本) */
    int disableOverlappedFiles(@Param("insurType") String insurType,
                               @Param("excludeId") Long excludeId,
                               @Param("settleDates") List<String> settleDates);

    /** 同类型下与给定结算日期重叠的启用文件数(改类型/启用前的冲突校验) */
    int countOverlappedEnabledFiles(@Param("insurType") String insurType,
                                    @Param("excludeId") Long excludeId,
                                    @Param("settleDates") List<String> settleDates);

    int insertDetail(MiReconDetailEntity entity);
}
