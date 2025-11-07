package com.showexcel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.showexcel.model.CashStatistics;
import com.showexcel.response.RowData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2025/11/7 11:14
 */
@Mapper
public interface CashStatisticsMapper extends BaseMapper<CashStatistics> {
     List<CashStatistics> findAll();

    List<CashStatistics> findByTableDate(String date);


    List<RowData> findByTableDateNew(String date);

}
