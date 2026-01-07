package com.example.auto_demo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String getCurrentDate(){
        return "2023-05-01";
    }

    public static void main(String[] args) {
        System.out.println(formatDate("2023-05-01","yyyy-MM-dd","yyyy年MM月dd日"));
    }

    public static String formatDate(String dateStr,String orgFormat,String targetFormat){

        SimpleDateFormat sdf = new SimpleDateFormat(orgFormat);
        String newDate =  "";
        try {
            // 解析字符串为Date
            Date date = sdf.parse(dateStr);
            SimpleDateFormat sdf1 = new SimpleDateFormat(targetFormat);
            newDate = sdf1.format(date);
          return newDate;

        } catch (ParseException e) {
            e.printStackTrace(); // 格式不匹配时抛出异常
        }

        return newDate;
    }
}
