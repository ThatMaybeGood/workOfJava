package com.mergedata.util;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

    /**
     * 节假日数据管理引擎 - 2026精准校准版
     */
    public class ChineseHolidayManager {

        private static final String DATA_DIR = "data";
        private static final String API_URL_PREFIX = "https://timor.tech/api/holiday/year/";

        public static void main(String[] args) {
            // 抓取 2026 年数据
            int year = 2025;
            List<String> results = getOrFetchHolidays(year);

            verify(results, "2026-01-01", "元旦节");
            verify(results, "2026-01-03", "元旦周六");
            verify(results, "2026-01-04", "补班周日(应剔除)");
            verify(results, "2026-02-14", "春节前补班(应剔除)");
            verify(results, "2026-02-15", "春节除夕");

            System.out.println("\n结果已保存至: " + System.getProperty("user.dir") + "\\data\\" + year + "\\rest_days.txt");
        }

        private static void verify(List<String> list, String date, String label) {
            boolean exists = false;
            String info = "";
            for (String s : list) {
                if (s.startsWith(date)) {
                    exists = true;
                    info = s;
                    break;
                }
            }
            if (label.contains("应剔除")) {
                System.out.println(date + " [" + label + "]: " + (exists ? "❌ 错误 (仍在列表中: " + info + ")" : "✅ 正确 (已剔除)"));
            } else {
                System.out.println(date + " [" + label + "]: " + (exists ? "✅ 存在 (" + info + ")" : "❌ 缺失"));
            }
        }

        public static List<String> getOrFetchHolidays(int year) {
            Path yearDirPath = Paths.get(System.getProperty("user.dir"), DATA_DIR, String.valueOf(year));
            File yearFile = new File(yearDirPath.toFile(), "rest_days.txt");

            // 1. 本地读取逻辑
            if (yearFile.exists()) {
                try {
                    return Files.readAllLines(yearFile.toPath(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("读取本地文件失败，尝试联网更新...");
                }
            }

            // 2. 联网获取并处理
            String json = fetchFromApi(API_URL_PREFIX + year);
            List<String> finalData = new ArrayList<>();

            if (json == null || !json.contains("\"code\":0")) {
                System.err.println("无法从 API 获取有效数据，请检查网络。");
                return finalData;
            }

            LocalDate date = LocalDate.of(year, 1, 1);
            while (date.getYear() == year) {
                String dStr = date.toString();
                DayOfWeek dw = date.getDayOfWeek();
                boolean isWeekend = (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY);

                // 0: 未定义, 1: 放假(true), 2: 补班(false)
                int apiStatus = 0;
                if (json.contains("\"" + dStr + "\"")) {
                    int dateIdx = json.indexOf("\"" + dStr + "\"");
                    // 截取该日期对象范围内的 JSON 字符串
                    String segment = json.substring(dateIdx, Math.min(dateIdx + 150, json.length()));

                    // 严谨判断：寻找日期后的第一个 holiday 字段
                    if (segment.contains("\"holiday\":true")) {
                        apiStatus = 1;
                    } else if (segment.contains("\"holiday\":false")) {
                        apiStatus = 2;
                    }
                }

                String category = null;
                String code = "";

                // --- 判定优先级排序 ---
                if (apiStatus == 1) {
                    // API 明确说休息
                    category = "法定节假日";
                    code = "1";
                } else if (apiStatus == 2) {
                    // API 明确说补班：即便这天是周末，category 依然为 null，从而被剔除
                    category = null;
                } else if (isWeekend) {
                    // API 没提到这天，且它是周末：正常休息
                    category = "正常周末";
                    code = "0";
                }

                if (category != null) {
                    finalData.add(dStr + "|" + category + "|" + code);
                }
                date = date.plusDays(1);
            }

            // 3. 保存到本地
            saveToLocal(yearFile, finalData);
            return finalData;
        }


        public static List<LocalDate> getHolidaysByYear(int year) {

            // 2. 联网获取并处理
            String json = fetchFromApi(API_URL_PREFIX + year);
            List<LocalDate> finalData = new ArrayList<>();

            if (json == null || !json.contains("\"code\":0")) {
                System.err.println("无法从 API 获取有效数据，请检查网络。");
                return new ArrayList<>();
            }

            LocalDate date = LocalDate.of(year, 1, 1);
            while (date.getYear() == year) {
                LocalDate dStr = date;
                DayOfWeek dw = date.getDayOfWeek();
                boolean isWeekend = (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY);

                // 0: 未定义, 1: 放假(true), 2: 补班(false)
                int apiStatus = 0;
                if (json.contains("\"" + dStr + "\"")) {
                    int dateIdx = json.indexOf("\"" + dStr + "\"");
                    // 截取该日期对象范围内的 JSON 字符串
                    String segment = json.substring(dateIdx, Math.min(dateIdx + 150, json.length()));

                    // 严谨判断：寻找日期后的第一个 holiday 字段
                    if (segment.contains("\"holiday\":true")) {
                        apiStatus = 1;
                    } else if (segment.contains("\"holiday\":false")) {
                        apiStatus = 2;
                    }
                }

                String category = null;
                String code = "";

                // --- 判定优先级排序 ---
                if (apiStatus == 1) {
                    // API 明确说休息
                    category = "法定节假日";
                    code = "1";
                } else if (apiStatus == 2) {
                    // API 明确说补班：即便这天是周末，category 依然为 null，从而被剔除
                    category = null;
                } else if (isWeekend) {
                    // API 没提到这天，且它是周末：正常休息
                    category = "正常周末";
                    code = "0";
                }

                if (category != null) {
                    finalData.add(dStr);
                }
                date = date.plusDays(1);
            }

            // 3. 保存到本地
             return finalData;
        }

        private static String fetchFromApi(String urlStr) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                if (conn.getResponseCode() == 200) {
                    try (Scanner s = new Scanner(conn.getInputStream(), "UTF-8")) {
                        return s.useDelimiter("\\A").hasNext() ? s.next() : "";
                    }
                }
            } catch (Exception e) {
                System.err.println("请求接口出错: " + e.getMessage());
            }
            return null;
        }

        private static void saveToLocal(File file, List<String> data) {
            try {
                if (file.getParentFile() != null) file.getParentFile().mkdirs();
                Files.write(file.toPath(), data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }