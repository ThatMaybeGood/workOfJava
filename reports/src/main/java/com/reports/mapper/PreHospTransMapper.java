package com.reports.mapper;

import com.reports.dto.response.cash.prehosp.PreHospTransItem;
import com.reports.dto.response.cash.prehosp.PreHospUserItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 院前转住院统计 Mapper(HIS库)
 */
@Mapper
public interface PreHospTransMapper {

    /**
     * 院前转住院统计: 就诊日×操作员出收据张数/人次
     * 只统计已维护操作员(IN列表); 院前口径=收据在outp_treat_rec_t存在且PRE_HOSPITAL&lt;=1(查不到不算)
     */
    List<PreHospTransItem> queryStats(@Param("startDate") Date startDate, @Param("endDate") Date endDate,
                                      @Param("timeDimension") String timeDimension,
                                      @Param("operatorNos") List<String> operatorNos,
                                      @Param("operatorNo") String operatorNo);

    /** 按USER_ID批量取操作员姓名(US7ASCII, RAWTOHEX传回Java按GBK解码) */
    List<PreHospUserItem> queryUsersByIds(@Param("ids") List<String> ids);

    /** 候选人员搜索(模糊): 工号/登录ID模糊或姓名hex模糊, 分页行号[startRow,endRow] */
    List<PreHospUserItem> searchUsers(@Param("kw") String kw, @Param("kwHex") String kwHex,
                                      @Param("startRow") int startRow, @Param("endRow") int endRow);

    /** 候选人员总数(同搜索条件) */
    int countUsers(@Param("kw") String kw, @Param("kwHex") String kwHex);

    /** 按登录ID取操作员(DB_USER不唯一时取ROWNUM=1) */
    PreHospUserItem queryUserByDbuser(@Param("dbuser") String dbuser);
}
