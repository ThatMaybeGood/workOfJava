package com.example.messagedataservice.server.impl;

import com.example.messagedataservice.dto.ReportDTO;
import com.example.messagedataservice.model.HisData;
import com.example.messagedataservice.model.HisOperator;
import com.example.messagedataservice.model.HolidayCalendar;
import com.example.messagedataservice.model.YQCashRegRecord;
import com.example.messagedataservice.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Value("${api.his.url}")
    private String HIS_DATA_URL;

    @Value("${api.yq.cash_url}")
    private String YQ_DATA_URL;

    @Value("${api.yq.operator_url}")
    private String OPERATOR_URL;

    @Autowired
    private HisDataService hisDataService;
    @Autowired
    private YQCashRegRecordService yqCashRegRecordService;
    @Autowired
    private HisOperatorService hisOperatorService;
    @Autowired
    private HolidayCalendarService holidayCalendarService;


    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<ReportDTO> getAll(LocalDate reportDate) {

        // 1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
        // 2、根据日期类型获取对应的操作员数据
        // 3、将获取到的数据转换为DTO对象，并填充到结果集中
        return  getAllReportData(reportDate);
    }

    @Override
    public Boolean insert(LocalDate reportDate) {

        return null;
    }


    // 安全获取Double值
    private Double getSafeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    // 计算衍生字段
    private void calculateDerivedFields(ReportDTO dto) {
        // 根据业务规则计算其他字段
        // 例如：dto.setActualReportAmount(dto.getReportAmount() - dto.getPreviousTemporaryReceipt());
    }




    public List<ReportDTO> getAllReportData(LocalDate reportDate) {

        // 1. 获取所有数据（列表形式）
        List<HisOperator> operators = hisOperatorService.findData(); // 操作员列表
        List<HisData> hisDataList = hisDataService.findByDate(reportDate);           // His数据列表
        List<YQCashRegRecord> yqDataList = yqCashRegRecordService.findByDate(reportDate);     // YQ数据列表

        // 2. 将关联数据转换为以 operatorNo 为key的Map
        Map<String, HisData> hisDataMap = hisDataList.stream()
                .collect(Collectors.toMap(HisData::getOperatorNo, Function.identity()));

        Map<String, YQCashRegRecord> cashRecordMap = yqDataList.stream()
                .collect(Collectors.toMap(YQCashRegRecord::getOperatorNo, Function.identity()));

        // 3. 以操作员为主，构建结果集
        List<ReportDTO> result = new ArrayList<>();

        for (HisOperator operator : operators) {
            ReportDTO dto = new ReportDTO();

            // 设置操作员基础信息
            dto.setOperatorNo(operator.getOperatorNo());
            dto.setOperatorName(operator.getOperatorName());
            dto.setReportDate(reportDate);

            // 1、关联 HisData 数据（如果有）
            HisData hisData = hisDataMap.get(operator.getOperatorNo());
            if (hisData != null) {
                dto.setHisAdvancePayment(getSafeDouble(hisData.getHisAdvancePayment()));
                dto.setHisMedicalIncome(getSafeDouble(hisData.getHisMedicalIncome()));
                //目前默认为0.00，后续根据实际业务调整
                dto.setHisRegistrationIncome(0.00);

                //应交报表数
                dto.setReportAmount(dto.getHisAdvancePayment() + dto.getHisMedicalIncome()+dto.getHisRegistrationIncome());
            }

            // 2、关联 YQCashRegRecord 数据（如果有）
            YQCashRegRecord cashRecord = cashRecordMap.get(operator.getOperatorNo());
            if (cashRecord != null) {
                dto.setRetainedCash(getSafeDouble(cashRecord.getRetainedCash()));

                dto.setWindowNo(cashRecord.getWindowNo());
                dto.setOperatType(cashRecord.getOperatType());
                dto.setSechduling(cashRecord.getSechduling());
                dto.setApplyDate(cashRecord.getApplyDate());
            }

            //前日暂收款 正常情况为前一天的当日暂收款

            dto.setPreviousTemporaryReceipt(0.00);
            dto.setHolidayTemporaryReceipt(0.00);

            dto.setCurrentTemporaryReceipt(0.00);



            //实交报表数  通过计算得出
            dto.setActualReportAmount(getActualReportAmount(reportDate));

            dto.setActualCashAmount(dto.getActualReportAmount()+dto.getCurrentTemporaryReceipt());

            dto.setRetainedDifference(cashRecord.getRetainedCash() - dto.getActualReportAmount() - dto.getPettyCash());


            // 计算其他衍生字段
            calculateDerivedFields(dto);

            // 以operatorNo为key放入结果Map
            result.add(dto);
        }

        log.info("生成报表完成，共处理 {} 个操作员", result.size());

        return result;
    }





    /*
    根据日期的情况分类得到最后时间，
     1、判断当前日期类型  0 正常 ，1 节假日 ，2 节假日前一天 3 节假日后一天
     */
    public double getActualReportAmount(LocalDate reportDate) {
        Double amount = 0.00;

        //1、获取目前维护的节假日列表
        List<HolidayCalendar> holidayCalendars  = holidayCalendarService.findByDate(reportDate);

        if (holidayCalendars.isEmpty()) {
            log.warn("No holiday calendar data found.");
            return amount;
        }

        //1、判断当前日期是否为节假日
        if(isDateInvalid(holidayCalendars,reportDate)){
            //节假日
        }

        if(isDateInvalid(holidayCalendars,reportDate.minusDays(1))){
            //节假日后一天
            while (true){
                reportDate = reportDate.minusDays(1);
                if(isDateInvalid(holidayCalendars,reportDate)){
                    break;
                }else{
                    amount = amount + 0.00;
                }

            }
        }

        if(isDateInvalid(holidayCalendars,reportDate.plusDays(1))){
            //节假日前一天
        }

        //正常情况






        return amount;

    }


    /**
     * 最优解：字符串作废标志的HashSet方案
     */
    public boolean isDateInvalid(List<HolidayCalendar> holidayCalendarList, LocalDate targetDate) {
        if (holidayCalendarList == null || targetDate == null) {
            return false;
        }

        // 先把所有日期提取到Set中
        Set<LocalDate> dateSet = holidayCalendarList.stream()
                .map(HolidayCalendar::getHolidayDate)
                .collect(Collectors.toSet());

        // 然后快速检查
        boolean exists = dateSet.contains(targetDate);

        return exists;
    }


}