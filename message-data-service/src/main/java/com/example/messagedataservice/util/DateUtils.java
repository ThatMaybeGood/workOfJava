package com.example.messagedataservice.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * 使用 Calendar 进行日期加减
     */
    public static Date addDaysWithCalendar(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     * 使用 LocalDate 进行日期加减（推荐）
     */
    public static Date addDaysWithLocalDate(Date date, int days) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate resultLocalDate = localDate.plusDays(days);

        return Date.from(resultLocalDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * 获取前一天
     */
    public static Date getPreviousDay(Date date) {
        return addDaysWithLocalDate(date, -1);
    }

    /**
     * 获取后一天
     */
    public static Date getNextDay(Date date) {
        return addDaysWithLocalDate(date, 1);
    }

    public static void main(String[] args) {
        Date currentDate = new Date();
        System.out.println("原日期: " + currentDate);

        Date previousDay = getPreviousDay(currentDate);
        Date nextDay = getNextDay(currentDate);

        System.out.println("前一天: " + previousDay);
        System.out.println("后一天: " + nextDay);

        // 加减多天
        Date in3Days = addDaysWithLocalDate(currentDate, 3);
        Date before3Days = addDaysWithLocalDate(currentDate, -3);
        System.out.println("3天后: " + in3Days);
        System.out.println("3天前: " + before3Days);
    }
}