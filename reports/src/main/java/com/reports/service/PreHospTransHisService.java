package com.reports.service;

import com.reports.annotation.DataSource;
import com.reports.dto.response.cash.prehosp.PreHospTransItem;
import com.reports.dto.response.cash.prehosp.PreHospUserItem;
import com.reports.mapper.PreHospTransMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 院前转住院统计 HIS 侧服务
 * 经 @DataSource("his") 切换数据源, 由报表库侧服务注入调用(内部自调用不走代理, 必须独立bean)
 */
@Service
@DataSource("his")
public class PreHospTransHisService {

    private final PreHospTransMapper preHospTransMapper;

    @Autowired
    public PreHospTransHisService(PreHospTransMapper preHospTransMapper) {
        this.preHospTransMapper = preHospTransMapper;
    }

    public List<PreHospTransItem> queryStats(Date startDate, Date endDate, String timeDimension,
                                             List<String> operatorNos, String operatorNo) {
        return preHospTransMapper.queryStats(startDate, endDate, timeDimension, operatorNos, operatorNo);
    }

    /** 按USER_ID批量取操作员姓名(US7ASCII, RAWTOHEX传回待GBK解码) */
    public List<PreHospUserItem> queryUsersByIds(List<String> ids) {
        return preHospTransMapper.queryUsersByIds(ids);
    }

    /** 候选人员搜索(模糊): 工号/登录ID模糊或姓名GBK-hex模糊, 分页行号[startRow,endRow] */
    public List<PreHospUserItem> searchUsers(String kw, String kwHex, int startRow, int endRow) {
        return preHospTransMapper.searchUsers(kw, kwHex, startRow, endRow);
    }

    /** 候选人员总数(同搜索条件) */
    public int countUsers(String kw, String kwHex) {
        return preHospTransMapper.countUsers(kw, kwHex);
    }

    /** 按登录ID取操作员(DB_USER不唯一时取ROWNUM=1) */
    public PreHospUserItem queryUserByDbuser(String dbuser) {
        return preHospTransMapper.queryUserByDbuser(dbuser);
    }
}
