package com.mergedata;

import com.mergedata.model.entity.YQHolidayEntity;
import com.mergedata.server.YQHolidayService;
import com.mergedata.util.ChineseHolidayManager;
import com.mergedata.util.HolidayDataParser;
import com.mergedata.util.PrimaryKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
public class HolidayTests {

    @Autowired
    YQHolidayService holidayService;

    @Test
    void contextLoads() {
        HolidayDataParser parser = new HolidayDataParser();
        PrimaryKeyGenerator pk = new PrimaryKeyGenerator();

        int year = 2025;
//        String filePath = "data/" + year + "/rest_days.txt";
//        List<YQHolidayEntity> holidays = parser.parseHolidayFile(filePath, year);

        List<LocalDate> results = ChineseHolidayManager.getHolidaysByYear(year);

        List<YQHolidayEntity> holidays = new ArrayList<>();

        for (LocalDate date : results) {
            YQHolidayEntity holiday = new YQHolidayEntity();
            holiday.setSerialNo(pk.generateKey());
            holiday.setHolidayYear(String.valueOf(year));
            holiday.setHolidayMonth(String.valueOf(date.getMonthValue()));
            holiday.setHolidayType("1");
            holiday.setCategory("2");
            holiday.setHolidayDate(date);

            holidays.add(holiday);
        }

        holidayService.batchInsertList(holidays);


    }


}
