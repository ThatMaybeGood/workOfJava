package com.reports.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 运营数据统计(互医库+powerrsp跨用户, SQL在 StatDataSummaryMapper.xml)
 */
public interface StatDataSummaryMapper {

    /** bt1 分析tab： 特殊制剂快递 */
    List<Map<String, Object>> bt1SpecialExpress(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt2 分析tab： 长时间不接诊 */
    List<Map<String, Object>> bt2NoAcceptLong(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt3 分析tab： 超时未接诊 */
    List<Map<String, Object>> bt3NoAccept24h(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt4 分析tab： 医生响应排名 */
    List<Map<String, Object>> bt4DoctorResponse();

    /** bt5 分析tab： 处方审核时长(汇总) */
    Map<String, Object> bt5RxAuditOverall(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt5 分析tab： 处方审核时长(按日) */
    List<Map<String, Object>> bt5RxAuditDaily(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt7 分析tab： 患者评价(汇总) */
    Map<String, Object> bt7EvalOverall(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt7 分析tab： 患者评价(按日) */
    List<Map<String, Object>> bt7EvalDaily(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt8 分析tab： 预约取消原因(分页) */
    List<Map<String, Object>> bt8CancelReasonPaged(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                   @Param("startRow") int startRow, @Param("endRow") int endRow);

    /** bt8 分析tab： 预约取消原因(总数) */
    long bt8CancelReasonCount(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt9 分析tab： 退号未再挂号(分页) */
    List<Map<String, Object>> bt9ReturnNoRegPaged(@Param("startDate") String startDate, @Param("endDate") String endDate,
                                                  @Param("startRow") int startRow, @Param("endRow") int endRow);

    /** bt9 分析tab： 退号未再挂号(总数) */
    long bt9ReturnNoRegCount(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt10 分析tab： 先后支付退号 */
    List<Map<String, Object>> bt10PayReturn(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt11 分析tab： 科室候诊时长 */
    List<Map<String, Object>> bt11DeptWaitTime(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt12 分析tab： 预约成功挂号量 */
    List<Map<String, Object>> bt12ApptSuccess(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /** bt13 分析tab： 复诊预约量 */
    List<Map<String, Object>> bt13RecheckAppt();

}
