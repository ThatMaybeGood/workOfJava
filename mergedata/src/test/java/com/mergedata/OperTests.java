package com.mergedata;

import com.mergedata.mapper.HolidayMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class OperTests {

    @Autowired
    HolidayMapper holidayService;

    @Test
    void contextLoads() {
//        HolidayDataParser parser = new HolidayDataParser();
//
//        String year = "2025";
//        String filePath = "data/" + year + "/rest_days.txt";
//        List<YQHolidayCalendar> holidays = parser.parseHolidayFile(filePath, year);
//
//        for (YQHolidayCalendar holidayCalendar : holidays) {
//            holidayService.insert(holidayCalendar);
//        }

    }


}
