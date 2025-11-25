```sql
好的，根据您代码中提到的日期类型分类需求（0 正常，1 节假日，2 节假日前一天，3 节假日后一天），我将为您演示如何使用 Java 枚举（Enum） 来定义这些类型，以及如何在您的服务类中使用它们。

使用枚举比使用硬编码的数字或常量字符串更加健壮、易读和类型安全。

1. 定义日期类型枚举
我们创建一个名为 ReportDateType 的枚举类。

Java

package com.example.messagedataservice.server.util; // 建议放在一个 utility 包中

import java.util.stream.Stream;

/**
 * 报表日期类型枚举
 */
public enum ReportDateType {
    
    /** 0: 正常日 */
    NORMAL(0, "正常日"),
    
    /** 1: 节假日 */
    HOLIDAY(1, "节假日"),
    
    /** 2: 节假日前一天 (通常指的是连假开始前一天) */
    PRE_HOLIDAY(2, "节假日前一天"),
    
    /** 3: 节假日后一天 (通常指的是连假结束后的第一天) */
    POST_HOLIDAY(3, "节假日后一天");

    // 内部字段
    private final int code;
    private final String description;

    // 构造函数
    ReportDateType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter 方法
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据Code查找对应的枚举 (方便外部通过数字使用)
     * @param code 日期类型代码
     * @return 对应的 ReportDateType，如果找不到则返回 NORMAL
     */
    public static ReportDateType fromCode(int code) {
        return Stream.of(ReportDateType.values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(NORMAL); 
    }
}
2. 在 ReportServiceImpl 中使用枚举
接下来，我们将您的 ReportServiceImpl 中的逻辑进行调整，使用 ReportDateType 枚举来代替数字和注释。

A. 修改 getAll 方法
Java

// 在 ReportServiceImpl 中
// ... (省略 import 和 class 定义)

@Override
public List<ReportDTO> getAll(LocalDate reportDate) {
    
    // 1、判断当前日期类型
    ReportDateType dateType = getReportDateType(reportDate); 
    log.info("报表日期 {} 的类型为: {} ({})", reportDate, dateType.getCode(), dateType.getDescription());

    // 2、根据日期类型获取对应的操作员数据 (如果业务有此需求)
    // List<HisOperator> operators = getOperatorsByDateType(dateType); 
    
    // 3、将获取到的数据转换为DTO对象，并填充到结果集中
    return getAllReportData(reportDate, dateType); // 传入类型，供后续逻辑使用
}

// 调整 getAllReportData 方法签名
public List<ReportDTO> getAllReportData(LocalDate reportDate, ReportDateType dateType) {
    // ... 原有逻辑
    // 现在可以在这个方法内部使用 dateType 进行逻辑判断
    if (dateType == ReportDateType.HOLIDAY) {
        // ... 节假日逻辑
    }
    // ...
}
B. 定义 getReportDateType 方法（用于判断日期类型）
我们将原注释中的逻辑独立出来，专注于判断当前日期的类型。

Java

// 在 ReportServiceImpl 中新增方法

/**
 * 判断给定日期的报表类型：正常日、节假日、节假日前一天、节假日后一天。
 * @param reportDate 需要判断的日期
 * @return ReportDateType 枚举值
 */
public ReportDateType getReportDateType(LocalDate reportDate) {
    
    // 为了进行日期判断，我们需要查询 reportDate 及其前一天、后一天的节假日状态。
    // 假设 findData() 能获取当前所需判断范围内的所有节假日
    List<HolidayCalendar> holidayCalendars = holidayCalendarService.findData(); // 假设这个方法能获取足够范围的数据
    
    // 转换为 Set 提高查找效率
    Set<LocalDate> holidaySet = holidayCalendars.stream()
            .map(HolidayCalendar::getHolidayDate)
            .collect(Collectors.toSet());
            
    // 当前日期
    boolean isCurrentHoliday = holidaySet.contains(reportDate);
    // 前一天
    boolean isPreviousHoliday = holidaySet.contains(reportDate.minusDays(1));
    // 后一天
    boolean isNextHoliday = holidaySet.contains(reportDate.plusDays(1));

    // 1. 是否为节假日
    if (isCurrentHoliday) {
        return ReportDateType.HOLIDAY;
    }
    
    // 2. 是否为节假日前一天 (即：今天不是节假日，但明天是节假日)
    if (isNextHoliday) {
        return ReportDateType.PRE_HOLIDAY;
    }
    
    // 3. 是否为节假日后一天 (即：今天不是节假日，但昨天是节假日)
    if (isPreviousHoliday) {
        return ReportDateType.POST_HOLIDAY;
    }

    // 4. 正常情况
    return ReportDateType.NORMAL;
}
```sql
