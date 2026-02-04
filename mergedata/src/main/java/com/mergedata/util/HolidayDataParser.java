package com.mergedata.util;

import com.mergedata.model.entity.YQHolidayEntity;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

    public class HolidayDataParser {


        public static List<YQHolidayEntity> parseHolidayFile(String filePath, String year) {
            List<YQHolidayEntity> list = new ArrayList<>();
            try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    // 使用 | 分割。注意在正则中 | 是特殊字符，需要转义 \\|
                    String[] parts = line.split("\\|");
                    if (parts.length == 3) {
                        PrimaryKeyGenerator pks = new PrimaryKeyGenerator();

                        YQHolidayEntity item = new YQHolidayEntity();
                        item.setHolidayYear(year);
                        item.setHolidayDate(LocalDate.parse(parts[0]));
                        item.setHolidayType(parts[2]);
                        item.setSerialNo(pks.generateKey());
                        list.add(item);
                    }
                }
            } catch (Exception e) {
                System.err.println("读取文件失败: " + e.getMessage());
            }
            return list;
        }


    }