package com.reports.mapper;

import com.reports.dto.response.cash.prehosp.PreHospTransItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

/**
 * 院前转住院统计中间表 Mapper（报表库 tr_prehosp_trans_d）
 * 口径同 PreHospTransMapper.queryStats(HIS直查), 方法签名一致; 操作员搜索仍走HIS不变
 */
@Mapper
public interface PreHospTransMidMapper {

    /** 院前转住院统计: 就诊日×操作员出负交易收据张数/人次(HIS侧SP已按口径过滤负交易) */
    List<PreHospTransItem> queryStats(@Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                      @Param("timeDimension") String timeDimension,
                                      @Param("operatorNos") List<String> operatorNos,
                                      @Param("operatorNo") String operatorNo);
}
