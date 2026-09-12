package com.reports.tools;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 报表库测试数据生成器。
 *
 * <p>为 {@link ReportDbInitializer} 建好的 56 张报表表灌入一套<b>自洽</b>的测试数据：
 * <ul>
 *   <li>日期覆盖「今天往前 90 天」的连续区间，保证按日/按月的报表都能查出数据；</li>
 *   <li>科室统一取自 TR_DEPT_DICT 的 12 个科室，所有明细表都用同一套 dept_code，
 *       JOIN TR_DEPT_DICT 的报表不会出现空行；</li>
 *   <li>财务表（19 号文件）保持外键一致：收据号 / 结账号互相引用，能通过 mapper 的 JOIN；</li>
 *   <li>随机数使用固定种子，重复执行结果一致，便于对比排查。</li>
 * </ul>
 *
 * <p>重复执行会先 DELETE 各表再写入，因此可以直接反复跑。
 */
public final class ReportTestDataSeeder {

    // ------------------------------------------------------------------
    // 参考数据
    // ------------------------------------------------------------------

    /**
     * 科室字典：{dept_code, dept_name, 内外科标志}。
     * 第 3 位是 internal_or_sergery（0内科/1外科），要和科室名称对得上 ——
     * DeptDictMapper 在 deptType=0 时会过滤 internal_or_sergery = 0，
     * 若把「神经内科」标成外科，那个科室就不会出现在下拉里。
     */
    private static final String[][] DEPTS = {
            {"D001", "内科", "0"},
            {"D002", "外科", "1"},
            {"D003", "儿科", "0"},
            {"D004", "妇产科", "1"},
            {"D005", "骨科", "1"},
            {"D006", "皮肤科", "0"},
            {"D007", "眼科", "1"},
            {"D008", "口腔科", "1"},
            {"D009", "神经内科", "0"},
            {"D010", "心血管内科", "0"},
            {"D011", "消化内科", "0"},
            {"D012", "呼吸内科", "0"},
    };

    /** 医生姓名池，按科室轮流分配。 */
    private static final String[] DOCTORS = {
            "王建国", "李秀英", "张伟", "刘芳", "陈静", "杨帆",
            "赵敏", "黄磊", "周涛", "吴倩", "徐斌", "孙丽",
            "马晓东", "朱红", "胡强", "郭磊", "何静", "高鹏",
    };

    private static final String[] TITLES = {"主任医师", "副主任医师", "主治医师", "住院医师"};

    private static final String[] AGE_GROUPS = {
            "0-14", "15-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80-89", "90+"
    };

    /** 患者类型：页面实际传的是缩写 inp/outp。 */
    private static final String[] PATIENT_TYPES = {"outp", "inp"};

    /**
     * 收费员结账页图表的两张图（用 TR_CASH_SETTLE_CHT.chart_title 区分，
     * 表里没有 chart_type 列）。名字必须和前端 mock 里的 title 一致。
     */
    private static final String[] CASHIER_CHART_TITLES = {"收费员业务工作量分析", "来源方式工作量分析"};

    /** 治疗项目（TR_TREAT_STAT_ITEM.item_name），项目名取自前端 mock 的 TOP10 列表。 */
    private static final String[] TREAT_ITEMS = {
            "推拿按摩", "针灸治疗", "拔罐疗法", "艾灸治疗", "刮痧治疗",
            "中药熏蒸", "穴位贴敷", "经络检测", "电针治疗", "耳穴压豆",
            "中药封包", "康复训练"};

    private static final String[] INSURANCE = {"城镇职工医保", "城乡居民医保", "自费", "商业保险"};

    private static final String[] IDENTITY = {"普通患者", "离退休", "低保"};

    private static final String[] REG_SOURCE = {"窗口", "自助机", "微信公众号", "APP"};

    private static final String[] ARC_SOURCE = {"现场建档", "线上建档", "批量导入"};

    /** 时段标签与前端 mock / 图表表头保持一致（用 ~ 分隔，含午间时段）。 */
    private static final String[] TIME_SLOTS = {
            "08:00~09:00", "09:00~10:00", "10:00~11:00", "11:00~12:00", "12:00~13:00",
            "13:00~14:00", "14:00~15:00", "15:00~16:00", "16:00~17:00"
    };

    private static final String[] LAB_ITEMS = {
            "血常规", "尿常规", "生化全套", "肝功能", "肾功能", "血糖", "凝血四项", "心肌酶"
    };

    private static final String[] MEDTECH_DEPTS = {
            "放射科", "超声科", "心电图室", "内镜中心", "核医学科", "病理科", "检验科", "功能检查室"
    };

    /** 人工窗口的业务类型，对应 TR_WIN_STAT_TM.business_type。 */
    private static final String[] WINDOW_BIZ_TYPES = {"挂号", "收费", "退费"};

    /**
     * 收费员结账页「按收费员统计」页签的列名。
     * 页面把收费员名写死在 JS 里（收费员1..收费员8），数据里就得用同样的名字，
     * 否则表格每列都取不到值显示成 '-'
     */
    private static final String[] CASHIER_NAMES = {
            "收费员1", "收费员2", "收费员3", "收费员4",
            "收费员5", "收费员6", "收费员7", "收费员8"};

    /**
     * 收费员结账页「按来源方式统计」页签的列名（对应 TR_CASH_SETTLE_DTL.item_type）。
     * 「有效挂号量」不在那个页签的固定列里，但「工作量报表」页签要用，所以一起造。
     */
    private static final String[] CASHIER_ITEM_TYPES = {
            "预约挂号量", "预约取号量", "当日挂号量", "有效挂号量", "退号量", "门诊收费量",
            "门诊退费量", "收预交金量", "退院量", "出院结算量"};

    /** 住院预交金渠道图的渠道（TR_INPAT_PREPAY_CHT.category）。 */
    private static final String[] INPAT_CHANNELS = {"窗口", "自助机", "掌上医院"};

    /** 住院预交金支付方式（TR_INPAT_PREPAY_CHT.series_name）。 */
    private static final String[] INPAT_PAY_TYPES = {"微信", "支付宝", "银行卡", "现金"};

    private static final String[] MONEY_TYPES = {
            "现金", "银行卡", "微信", "支付宝", "医保统筹", "自助POS", "信用卡", "支票"
    };

    /**
     * 统计区间天数：今天往前 540 天（含今天），约 18 个月。
     *
     * <p>为什么要这么长：
     * <ul>
     *   <li>门诊财务页默认区间是 2026-04（写死的），同比要查 2025-04，
     *       区间不够长的话同比字段全是空；</li>
     *   <li>互医质控等按月查询的页面有月份下拉，需要一年以上的月份数据。</li>
     * </ul>
     */
    private static final int DAYS = 540;

    private static LocalDate startDate;
    private static LocalDate endDate;
    private static Random rnd;
    private static PrintStream out;

    // ------------------------------------------------------------------
    // 入口
    // ------------------------------------------------------------------

    public static void seed(Connection conn) throws SQLException {
        try {
            out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8");
        } catch (Exception e) {
            out = System.out;
        }

        endDate = LocalDate.now();
        startDate = endDate.minusDays(DAYS - 1L);
        rnd = new Random(20260912L);

        out.println("  统计区间: " + startDate + " ~ " + endDate + " (" + DAYS + " 天)");

        // 先清空，保证可重复执行
        clearAll(conn);

        seedDicts(conn);
        seedOutpatientOperation(conn);
        seedOutpatientAlert(conn);
        seedForecast(conn);
        seedInternetHospital(conn);
        seedLabStats(conn);
        seedMedTech(conn);
        seedPatientPortrait(conn);
        seedQualityControl(conn);
        seedRevenue(conn);
        seedRoomUsage(conn);
        seedServiceQuality(conn);
        seedSpecialtyTreatment(conn);
        seedWindowStats(conn);
        seedCashSettle(conn);
        seedDischSettle(conn);
        seedTreatmentStats(conn);
        seedInpatPrepay(conn);
        seedOutpatientFinance(conn);
        seedDailyWideTable(conn);

        conn.commit();
    }

    /**
     * 逐表计数，确认 56 张表都有数据。
     */
    public static void verify(Connection conn) throws SQLException {
        String[] tables = {
                "TR_DEPT_DICT", "TR_OUTP_OP", "TR_OUTP_OP_DTL", "TR_OUTP_ALT_OV",
                "TR_FC_APPOINT", "TR_FC_WEATHER", "TR_FC_HOLIDAY",
                "TR_INET_HOSP_OV", "TR_INET_HOSP_OP", "TR_INET_HOSP_BIZ",
                "TR_INET_HOSP_DEPT_RNK", "TR_INET_HOSP_DOC_RNK", "TR_INET_HOSP_GRW",
                "TR_LABSTAT_OV", "TR_LABSTAT_RNK", "TR_LABSTAT_TM",
                "TR_MEDTECH_OV", "TR_MEDTECH_DTL",
                "TR_PAT_PORTRAIT_AGE", "TR_PAT_PORTRAIT_INSUR", "TR_PAT_PORTRAIT_IDTY",
                "TR_PAT_PORTRAIT_REG", "TR_PAT_PORTRAIT_ARC",
                "TR_QC_OV", "TR_QC_DTL", "TR_REV_OV",
                "TR_ROOM_USE_OV", "TR_ROOM_USE_DTL",
                "TR_SVC_QUALITY_CMPL", "TR_SVC_QUALITY_PRZ",
                "TR_COMMON_DICT", "TR_STAFF_DICT", "TR_SPEC_TREAT_OV",
                "TR_WIN_STAT_OV", "TR_WIN_STAT_AGE", "TR_WIN_STAT_TM", "TR_WIN_STAT_SRC",
                "TR_CASH_SETTLE_OV", "TR_CASH_SETTLE_DTL", "TR_CASH_SETTLE_CHT",
                "TR_DISCH_SETTLE_OV", "TR_DISCH_SETTLE_DTL", "TR_DISCH_SETTLE_CHT",
                "TR_TREAT_STAT_OV", "TR_TREAT_STAT_DTL", "TR_TREAT_STAT_TREND",
                "TR_INPAT_PREPAY_OV", "TR_INPAT_PREPAY_DTL", "TR_INPAT_PREPAY_CHT",
                "TR_OUTP_FIN_CLINIC_MASTER", "TR_OUTP_FIN_RCPT_ACCT", "TR_OUTP_FIN_ACCT_MASTER",
                "TR_OUTP_FIN_PAYMENTS_MONEY", "TR_OUTP_FIN_MOP_QUEUE", "TR_OUTP_FIN_ACCT_MONEY",
                "TR_TREAT_STAT_ITEM",
                "TR_OUTPATIENT_STATS_DAY_RESULT",
        };
        int empty = 0;
        int total = 0;
        for (String t : tables) {
            int n = count(conn, t);
            total += n;
            if (n == 0) {
                empty++;
                out.println(String.format("  [空表] %s", t));
            }
        }
        out.println(String.format("  %d 张表合计 %d 行；空表 %d 张", Integer.valueOf(tables.length),
                Integer.valueOf(total), Integer.valueOf(empty)));
    }

    private static int count(Connection conn, String table) throws SQLException {
        Statement s = null;
        ResultSet r = null;
        try {
            s = conn.createStatement();
            r = s.executeQuery("SELECT COUNT(*) FROM " + table);
            r.next();
            return r.getInt(1);
        } finally {
            close(r, s);
        }
    }

    /** 清空所有报表表（TR_ 前缀 + 宽表），不碰库里原有的其它表。 */
    private static void clearAll(Connection conn) throws SQLException {
        String sql = "SELECT table_name FROM user_tables WHERE table_name LIKE 'TR!_%' ESCAPE '!'";
        List<String> names = new ArrayList<String>();
        Statement s = null;
        ResultSet r = null;
        try {
            s = conn.createStatement();
            r = s.executeQuery(sql);
            while (r.next()) {
                names.add(r.getString(1));
            }
        } finally {
            close(r, s);
        }
        for (String n : names) {
            // 有外键时用 DELETE；本套表无外键，DELETE 更安全（不影响序列）
            try (Statement d = conn.createStatement()) {
                d.executeUpdate("DELETE FROM " + n);
            } catch (SQLException e) {
                out.println("  [WARN] 清空 " + n + " 失败: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // 各报表
    // ------------------------------------------------------------------

    private static void seedDicts(Connection conn) throws SQLException {
        int no = 0;
        List<Object[]> deptRows = new ArrayList<Object[]>();
        for (String[] d : DEPTS) {
            no++;
            // clinic_attr / outp_or_inp / internal_or_sergery 都是 NUMBER, 对应实体的 Integer 字段
            // (clinic_attr: 0临床 1辅诊 2护理单元 3机关 9其他; outp_or_inp: 0门诊 1住院 2门诊住院;
            //  internal_or_sergery: 0内科 1外科)
            deptRows.add(new Object[]{
                    Integer.valueOf(no * 10), d[0], d[1], d[1] + "门诊",
                    Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(d[2]),
                    "S" + d[0], "T" + (no % 3 + 1), "门诊单元" + ((no - 1) / 4 + 1)
            });
        }
        batch(conn, "INSERT INTO TR_DEPT_DICT (serial_no, dept_code, dept_name, dept_alias, clinic_attr,"
                        + " outp_or_inp, internal_or_sergery, input_code, type_code, group_unit_name)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?)", deptRows);

        String[] positions = {"医生", "护士", "收费员", "药师", "技师", "导医"};
        String[] complaintCategories = {"服务态度", "候诊时间", "医疗质量", "收费问题", "环境设施", "沟通问题"};
        String[] complaintResults = {"已处理", "处理中", "待处理", "已回访"};
        String[] praiseMethods = {"锦旗", "感谢信", "口头表扬", "电话表扬", "留言表扬"};
        String[] yesNo = {"是", "否"};

        List<Object[]> dictRows = new ArrayList<Object[]>();
        addDict(dictRows, "position", positions);
        addDict(dictRows, "complaintCategory", complaintCategories);
        addDict(dictRows, "complaintResult", complaintResults);
        addDict(dictRows, "praiseMethod", praiseMethods);
        addDict(dictRows, "feedback", yesNo);
        batch(conn, "INSERT INTO TR_COMMON_DICT (id, dict_type, dict_code, dict_name, sort_no, status)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,1)", dictRows);

        List<Object[]> staffRows = new ArrayList<Object[]>();
        for (int i = 0; i < 24; i++) {
            String[] dept = DEPTS[i % DEPTS.length];
            staffRows.add(new Object[]{
                    "S" + String.format("%04d", Integer.valueOf(1001 + i)),
                    DOCTORS[i % DOCTORS.length],
                    dept[0], dept[1],
                    positions[i % positions.length],
                    Integer.valueOf(1)
            });
        }
        batch(conn, "INSERT INTO TR_STAFF_DICT (id, staff_code, staff_name, dept_code, dept_name, position, status)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", staffRows);

        out.println("  [字典] TR_DEPT_DICT=" + DEPTS.length + ", TR_COMMON_DICT=" + dictRows.size()
                + ", TR_STAFF_DICT=" + staffRows.size());
    }

    private static void addDict(List<Object[]> rows, String type, String[] values) {
        for (int i = 0; i < values.length; i++) {
            rows.add(new Object[]{type, type + "_" + (i + 1), values[i], Integer.valueOf((i + 1) * 10)});
        }
    }

    private static void seedOutpatientOperation(Connection conn) throws SQLException {
        List<Object[]> op = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (int i = 0; i < DEPTS.length; i++) {
                String code = DEPTS[i][0];
                String name = DEPTS[i][1];

                int famous = rnd(5, 30);
                int special = rnd(3, 20);
                int known = rnd(4, 25);
                int expertA = rnd(8, 40);
                int expertB = rnd(6, 35);
                int ordinary = rnd(40, 160);
                int total = famous + special + known + expertA + expertB + ordinary;

                int uFamousE = rnd(2, 8);
                int uFamousT = uFamousE + rnd(1, 4);
                int uSpecialE = rnd(2, 7);
                int uSpecialT = uSpecialE + rnd(1, 3);
                int uKnownE = rnd(2, 9);
                int uKnownT = uKnownE + rnd(1, 4);
                int uAE = rnd(3, 12);
                int uAT = uAE + rnd(1, 5);
                int uBE = rnd(3, 11);
                int uBT = uBE + rnd(1, 5);
                int uOrdE = rnd(10, 30);
                int uOrdT = uOrdE + rnd(2, 8);

                int appointTotal = total + rnd(0, 30);
                int appointCount = Math.max(1, (int) (appointTotal * 0.6));

                // TR_OUTP_OP —— 门诊运行源表(按日期+科室)
                op.add(new Object[]{
                        day, code, Integer.valueOf(total),
                        Integer.valueOf(famous), Integer.valueOf(special), Integer.valueOf(known),
                        Integer.valueOf(expertA), Integer.valueOf(expertB), Integer.valueOf(ordinary),
                        Integer.valueOf(uFamousE), Integer.valueOf(uFamousT),
                        Integer.valueOf(uSpecialE), Integer.valueOf(uSpecialT),
                        Integer.valueOf(uKnownE), Integer.valueOf(uKnownT),
                        Integer.valueOf(uAE), Integer.valueOf(uAT),
                        Integer.valueOf(uBE), Integer.valueOf(uBT),
                        Integer.valueOf(uOrdE), Integer.valueOf(uOrdT),
                        Integer.valueOf(appointTotal), Integer.valueOf(appointCount),
                        Integer.valueOf(rnd(5, 40)), Integer.valueOf(rnd(10, 60)),
                        (d % 2 == 0) ? "上午" : "下午"
                });

                int effTotal = uFamousE + uSpecialE + uKnownE + uAE + uBE + uOrdE;
                int allTotal = uFamousT + uSpecialT + uKnownT + uAT + uBT + uOrdT;

                // TR_OUTP_OP_DTL —— 门诊运行科室明细(同粒度, 另带比率列)
                dtl.add(new Object[]{
                        day, code, name, Integer.valueOf(total),
                        rate(60, 95), rate(40, 80), dec(3, 12), Integer.valueOf(total),
                        Integer.valueOf(famous), Integer.valueOf(special), Integer.valueOf(known),
                        Integer.valueOf(expertA), Integer.valueOf(expertB), Integer.valueOf(ordinary),
                        Integer.valueOf(effTotal), Integer.valueOf(effTotal - rnd(0, 5)),
                        Integer.valueOf(allTotal)
                });
            }
        }
        batch(conn, "INSERT INTO TR_OUTP_OP (id, stat_date, dept_code, total_visits, famous_expert,"
                + " special_expert, known_expert, expert_a, expert_b, ordinary, unit_famous_effective,"
                + " unit_famous_total, unit_special_effective, unit_special_total, unit_known_effective,"
                + " unit_known_total, unit_a_effective, unit_a_total, unit_b_effective, unit_b_total,"
                + " unit_ordinary_effective, unit_ordinary_total, appointment_total, appointment_count,"
                + " return_visits, treat_count, unit)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", op);

        batch(conn, "INSERT INTO TR_OUTP_OP_DTL (id, stat_date, dept_code, dept_name, visits,"
                + " appointment_rate, exam_rate, efficiency, visit_count, famous_expert, special_expert,"
                + " known_expert, expert_a, expert_b, ordinary, effective_total, effective_detail, total_detail)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", dtl);

        out.println("  [门诊运行] TR_OUTP_OP=" + op.size() + ", TR_OUTP_OP_DTL=" + dtl.size());
    }

    private static void seedOutpatientAlert(Connection conn) throws SQLException {
        List<Object[]> rows = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (int i = 0; i < DEPTS.length; i++) {
                String period = (i % 2 == 0) ? "上午" : "下午";
                for (int k = 0; k < 2; k++) {
                    String doctor = DOCTORS[(i + k * 6) % DOCTORS.length];
                    rows.add(new Object[]{
                            day, DEPTS[i][0], DEPTS[i][1], doctor, period,
                            String.format("%02d:%02d", Integer.valueOf(rnd(11, 18)), Integer.valueOf(rnd(0, 59))),
                            Integer.valueOf(rnd(0, 5)), Integer.valueOf(rnd(0, 4)), Integer.valueOf(rnd(0, 3))
                    });
                }
            }
        }
        batch(conn, "INSERT INTO TR_OUTP_ALT_OV (id, stat_date, dept_code, dept_name, doctor_name,"
                + " clinic_period, his_logout_time, remain_alert, appointment_alert, early_leave)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", rows);
        out.println("  [门诊预警] TR_OUTP_ALT_OV=" + rows.size());
    }

    private static void seedForecast(Connection conn) throws SQLException {
        // 预约量表：今天起未来 60 天
        List<Object[]> app = new ArrayList<Object[]>();
        for (int d = 0; d < 60; d++) {
            Date day = dayAt(DAYS + d);
            for (String[] dept : DEPTS) {
                app.add(new Object[]{day, dept[0], Integer.valueOf(rnd(5, 80))});
            }
        }
        batch(conn, "INSERT INTO tr_fc_appoint (id, appoint_date, dept_code, appoint_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", app);

        // 天气要一直造到未来 120 天：预测门诊量模块会按「明天 / 下周」查天气系数，
        // 只造到今天的话未来日期查出来是 0 行
        String[] weathers = {"晴", "多云", "阴", "小雨", "中雨", "大雨", "雪"};
        List<Object[]> wea = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS + 120; d++) {
            wea.add(new Object[]{dayAt(d), weathers[rnd(0, weathers.length - 1)], Double.valueOf(1.0d)});
        }
        batch(conn, "INSERT INTO tr_fc_weather (id, weather_date, weather_type, weather_coef)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", wea);

        // 节假日：前一年/当年/次年各来一套，这样「近一年法定节假日」这类查询在任意今天都能查到数据。
        // hol_date 上有唯一索引，用 Set 去重，避免与下面的调休日撞车。
        List<Object[]> hol = new ArrayList<Object[]>();
        Set<LocalDate> used = new HashSet<LocalDate>();
        int y = endDate.getYear();
        for (int yy = y - 1; yy <= y + 1; yy++) {
            addHoliday(hol, used, LocalDate.of(yy, 1, 1), "元旦", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 4, 4), "清明节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 5, 1), "劳动节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 5, 2), "劳动节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 5, 3), "劳动节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 10, 1), "国庆节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 10, 2), "国庆节", "LEGAL_HOLIDAY");
            addHoliday(hol, used, LocalDate.of(yy, 10, 3), "国庆节", "LEGAL_HOLIDAY");
        }
        addHoliday(hol, used, endDate.plusDays(7), "周末调休上班", "WORKDAY_ADJUST");
        addHoliday(hol, used, endDate.plusDays(8), "周末调休上班", "WORKDAY_ADJUST");
        batch(conn, "INSERT INTO tr_fc_holiday (id, hol_date, hol_name, hol_type)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", hol);

        out.println("  [预测门诊量] tr_fc_appoint=" + app.size() + ", tr_fc_weather=" + wea.size()
                + ", tr_fc_holiday=" + hol.size());
    }

    private static void addHoliday(List<Object[]> rows, Set<LocalDate> used, LocalDate date,
                                   String name, String type) {
        if (used.add(date)) {
            rows.add(new Object[]{Date.valueOf(date), name, type});
        }
    }

    private static void seedInternetHospital(Connection conn) throws SQLException {
        // 最近 13 个自然月（页面上的月份下拉默认是 2025-12，窗口要盖得住）
        String[] months = new String[13];
        LocalDate m = endDate.withDayOfMonth(1);
        for (int i = 12; i >= 0; i--) {
            months[12 - i] = m.minusMonths(i).toString().substring(0, 7);
        }

        String[] items = {"门诊量", "接诊量", "处方量", "病历量", "审方量", "执行量", "退号量"};

        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> op = new ArrayList<Object[]>();
        List<Object[]> biz = new ArrayList<Object[]>();
        List<Object[]> deptRnk = new ArrayList<Object[]>();
        List<Object[]> docRnk = new ArrayList<Object[]>();
        List<Object[]> grw = new ArrayList<Object[]>();

        for (String mo : months) {
            int volume = rnd(3000, 9000);
            ov.add(new Object[]{
                    mo, Integer.valueOf(volume), rate(30, 70), rate(85, 99), rate(70, 95),
                    rate(80, 99), rate(60, 95), rate(75, 98)
            });
            for (String item : items) {
                int cur = rnd(500, 5000);
                int last = cur + rnd(-500, 500);
                op.add(new Object[]{mo, item, Integer.valueOf(cur), Integer.valueOf(Math.max(0, last)),
                        growth(cur, last)});
            }
            String[] bizCats = {"图文问诊", "视频问诊", "电话问诊", "复诊续方", "药品配送"};
            for (String c : bizCats) {
                int cur = rnd(100, 2000);
                int last = cur + rnd(-200, 200);
                biz.add(new Object[]{mo, c, Integer.valueOf(cur), Integer.valueOf(Math.max(0, last))});
            }
            for (int i = 0; i < DEPTS.length; i++) {
                int cur = rnd(100, 1200);
                int last = cur + rnd(-150, 150);
                deptRnk.add(new Object[]{mo, Integer.valueOf(i + 1), DEPTS[i][1],
                        Integer.valueOf(cur), Integer.valueOf(Math.max(0, last)), growth(cur, last)});
                docRnk.add(new Object[]{mo, Integer.valueOf(i + 1), DOCTORS[i % DOCTORS.length],
                        DEPTS[i][1], TITLES[i % TITLES.length], Integer.valueOf(rnd(50, 400))});
                // grw 按 mapper 用途是「平均候诊时长(科室TOP20)」
                grw.add(new Object[]{mo, DEPTS[i][1], Integer.valueOf(rnd(8, 45))});
            }
        }
        batch(conn, "INSERT INTO TR_INET_HOSP_OV (id, stat_month, outpatient_volume, doctor_ratio,"
                + " reception_rate, prescription_rate, record_rate, review_rate, execution_rate)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_INET_HOSP_OP (id, stat_month, item_name, current_value, last_value, growth_rate)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", op);
        batch(conn, "INSERT INTO TR_INET_HOSP_BIZ (id, stat_month, category, current_value, last_value)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", biz);
        batch(conn, "INSERT INTO TR_INET_HOSP_DEPT_RNK (id, stat_month, rank_num, dept_name, current_month,"
                + " last_month, growth_rate) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", deptRnk);
        batch(conn, "INSERT INTO TR_INET_HOSP_DOC_RNK (id, stat_month, rank_num, doctor_name, dept_name,"
                + " title, current_month) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", docRnk);
        batch(conn, "INSERT INTO TR_INET_HOSP_GRW (id, stat_month, category, data_value)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", grw);

        out.println("  [互医质控] OV=" + ov.size() + " OP=" + op.size() + " BIZ=" + biz.size()
                + " DEPT_RNK=" + deptRnk.size() + " DOC_RNK=" + docRnk.size() + " GRW=" + grw.size());
    }

    private static void seedLabStats(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> rnk = new ArrayList<Object[]>();
        List<Object[]> tm = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            ov.add(new Object[]{day, Integer.valueOf(rnd(200, 800)), rate(70, 98), rate(65, 95)});
            for (int i = 0; i < LAB_ITEMS.length; i++) {
                rnk.add(new Object[]{day, (i % 2 == 0) ? "BLOOD" : "LAB", Integer.valueOf(i + 1),
                        LAB_ITEMS[i], Integer.valueOf(rnd(30, 400))});
            }
            for (int i = 0; i < TIME_SLOTS.length; i++) {
                tm.add(new Object[]{day, TIME_SLOTS[i], Integer.valueOf(rnd(10, 120)),
                        Integer.valueOf(rnd(20, 200))});
            }
        }
        batch(conn, "INSERT INTO TR_LABSTAT_OV (id, stat_date, blood_collection, blood_efficiency,"
                + " lab_efficiency) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_LABSTAT_RNK (id, stat_date, rank_type, rank_num, item_name, item_value)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", rnk);
        batch(conn, "INSERT INTO TR_LABSTAT_TM (id, stat_date, time_slot, blood_count, lab_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", tm);
        out.println("  [检验统计] OV=" + ov.size() + " RNK=" + rnk.size() + " TM=" + tm.size());
    }

    private static void seedMedTech(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            ov.add(new Object[]{day, Integer.valueOf(rnd(150, 600)), rate(75, 99), dec(5, 30),
                    dec(1, 15), dec(20, 90)});
            for (String dept : MEDTECH_DEPTS) {
                dtl.add(new Object[]{day, dept, Integer.valueOf(rnd(20, 120)), rate(70, 99),
                        Double.valueOf(round2(rnd(5, 30))), Double.valueOf(round2(rnd(1, 15))),
                        Double.valueOf(round2(rnd(20, 90)))});
            }
        }
        batch(conn, "INSERT INTO TR_MEDTECH_OV (id, stat_date, check_count, on_time_rate, wait_time,"
                + " avg_wait_late, avg_report_time) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_MEDTECH_DTL (id, stat_date, dept_name, check_count, on_time_rate,"
                + " wait_time, avg_wait_late, avg_report_time)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?)", dtl);
        out.println("  [医技统计] OV=" + ov.size() + " DTL=" + dtl.size());
    }

    private static void seedPatientPortrait(Connection conn) throws SQLException {
        List<Object[]> age = new ArrayList<Object[]>();
        List<Object[]> insur = new ArrayList<Object[]>();
        List<Object[]> idty = new ArrayList<Object[]>();
        List<Object[]> reg = new ArrayList<Object[]>();
        List<Object[]> arc = new ArrayList<Object[]>();

        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (String[] dept : DEPTS) {
                // 页面分「门诊患者 / 住院患者」两个页签，patient_type 用的是缩写 outp / inp，
                // 不是 DDL 注释里写的 outpatient / inpatient（照注释造过一版，查询全空）
                for (String ptype : PATIENT_TYPES) {
                    for (String g : AGE_GROUPS) {
                        age.add(new Object[]{day, dept[0], ptype, g,
                                Integer.valueOf(rnd(0, 60)), Integer.valueOf(rnd(5, 200))});
                    }
                    for (String ins : INSURANCE) {
                        insur.add(new Object[]{day, dept[0], ptype, ins, Integer.valueOf(rnd(5, 200))});
                    }
                    for (String id : IDENTITY) {
                        idty.add(new Object[]{day, dept[0], ptype, id, Integer.valueOf(rnd(10, 300))});
                    }
                    for (String s : REG_SOURCE) {
                        reg.add(new Object[]{day, dept[0], ptype, s, Integer.valueOf(rnd(10, 300))});
                    }
                    for (String s : ARC_SOURCE) {
                        arc.add(new Object[]{day, dept[0], ptype, s, Integer.valueOf(rnd(5, 150))});
                    }
                }
            }
        }
        batch(conn, "INSERT INTO TR_PAT_PORTRAIT_AGE (id, stat_date, dept_code, patient_type, age_group,"
                + " archive_count, outpatient_count) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", age);
        batch(conn, "INSERT INTO TR_PAT_PORTRAIT_INSUR (id, stat_date, dept_code, patient_type,"
                + " insurance_name, patient_count) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", insur);
        batch(conn, "INSERT INTO TR_PAT_PORTRAIT_IDTY (id, stat_date, dept_code, patient_type,"
                + " identity_name, patient_count) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", idty);
        batch(conn, "INSERT INTO TR_PAT_PORTRAIT_REG (id, stat_date, dept_code, patient_type,"
                + " source_name, patient_count) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", reg);
        batch(conn, "INSERT INTO TR_PAT_PORTRAIT_ARC (id, stat_date, dept_code, patient_type,"
                + " source_name, patient_count) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", arc);

        out.println("  [患者画像] AGE=" + age.size() + " INSUR=" + insur.size() + " IDTY=" + idty.size()
                + " REG=" + reg.size() + " ARC=" + arc.size());
    }

    private static void seedQualityControl(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            ov.add(new Object[]{dayAt(d), rate(70, 99), rate(80, 99), rate(85, 99),
                    rate(0, 8), rate(60, 99), rate(0, 5), rate(70, 99), rate(90, 100),
                    rate(0, 3), rate(0, 2), rate(0, 2)});
        }
        batch(conn, "INSERT INTO TR_QC_OV (id, stat_date, emr_usage_rate, standard_diagnosis_rate,"
                + " on_time_rate, stop_rate, chemo_record_rate, chemo_adverse_rate, chemo_infusion_rate,"
                + " critical_value_rate, blood_draw_error_rate, surgery_complication_rate, adverse_event_rate)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)", ov);

        List<Object[]> dtl = new ArrayList<Object[]>();
        LocalDate m = endDate.withDayOfMonth(1);
        for (int i = 12; i >= 0; i--) {
            String mo = m.minusMonths(i).toString().substring(0, 7);
            dtl.add(new Object[]{mo, rate(70, 99), rate(80, 99), rate(85, 99),
                    rate(0, 8), rate(60, 99), rate(0, 5), rate(70, 99), rate(90, 100),
                    rate(0, 3), rate(0, 2), rate(0, 2)});
        }
        batch(conn, "INSERT INTO TR_QC_DTL (id, stat_month, emr_usage_rate, standard_diagnosis_rate,"
                + " on_time_rate, stop_rate, chemo_record_rate, chemo_adverse_rate, chemo_infusion_rate,"
                + " critical_value_rate, blood_draw_error_rate, surgery_complication_rate, adverse_event_rate)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?)", dtl);

        out.println("  [门诊质控] OV=" + ov.size() + " DTL=" + dtl.size());
    }

    private static void seedRevenue(Connection conn) throws SQLException {
        List<Object[]> rows = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (int i = 0; i < DEPTS.length; i++) {
                for (int k = 0; k < 3; k++) {
                    String doctor = DOCTORS[(i * 3 + k) % DOCTORS.length];
                    double register = round2(rnd(500, 3000));
                    double medical = round2(rnd(3000, 20000));
                    double service = round2(rnd(500, 5000));
                    rows.add(new Object[]{day, DEPTS[i][0], doctor,
                            Double.valueOf(register), Double.valueOf(medical),
                            Double.valueOf(round2(register + medical + service)), Double.valueOf(service)});
                }
            }
        }
        batch(conn, "INSERT INTO TR_REV_OV (id, stat_date, dept_code, doctor_name, register_revenue,"
                + " medical_revenue, outpatient_revenue, service_revenue)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?)", rows);
        out.println("  [门诊收入] TR_REV_OV=" + rows.size());
    }

    private static void seedRoomUsage(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            ov.add(new Object[]{day, rate(40, 90), rate(50, 95), rate(30, 85), rate(10, 60)});
            for (String[] dept : DEPTS) {
                dtl.add(new Object[]{day, dept[0], dept[1],
                        rate(40, 90), rate(50, 95), rate(30, 85), rate(10, 60)});
            }
        }
        batch(conn, "INSERT INTO TR_ROOM_USE_OV (id, stat_date, avg_usage, am_usage, pm_usage, holiday_usage)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_ROOM_USE_DTL (id, stat_date, dept_code, dept_name, avg_usage,"
                + " am_usage, pm_usage, holiday_usage)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?)", dtl);
        out.println("  [诊室使用率] OV=" + ov.size() + " DTL=" + dtl.size());
    }

    private static void seedServiceQuality(Connection conn) throws SQLException {
        String[] categories = {"服务态度", "候诊时间", "医疗质量", "收费问题", "环境设施", "沟通问题"};
        String[] results = {"已处理", "处理中", "待处理", "已回访"};
        String[] methods = {"锦旗", "感谢信", "口头表扬", "电话表扬", "留言表扬"};
        String[] positions = {"医生", "护士", "收费员", "药师", "技师", "导医"};
        String[] remarks = {"患者反馈属实，已整改", "已电话回访，患者表示理解", "转交科室处理", "无"};

        // 每天都放几条：页面默认区间是「今日」，只在随机日期撒 180 条的话
        // 大部分日子（包括今天）会查不到数据，看起来像"没有测试数据"
        List<Object[]> cmpl = new ArrayList<Object[]>();
        List<Object[]> prz = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            int complaintCount = rnd(1, 3);
            for (int i = 0; i < complaintCount; i++) {
                int di = rnd(0, DEPTS.length - 1);
                cmpl.add(new Object[]{
                        day, new Timestamp(day.getTime() + rnd(8, 18) * 3600L * 1000),
                        DEPTS[di][0], DEPTS[di][1],
                        DOCTORS[rnd(0, DOCTORS.length - 1)],
                        positions[rnd(0, positions.length - 1)],
                        categories[rnd(0, categories.length - 1)],
                        results[rnd(0, results.length - 1)],
                        remarks[rnd(0, remarks.length - 1)]
                });
            }
            int praiseCount = rnd(1, 3);
            for (int i = 0; i < praiseCount; i++) {
                int di = rnd(0, DEPTS.length - 1);
                prz.add(new Object[]{
                        day, new Timestamp(day.getTime() + rnd(8, 18) * 3600L * 1000),
                        DEPTS[di][0], DEPTS[di][1],
                        DOCTORS[rnd(0, DOCTORS.length - 1)],
                        positions[rnd(0, positions.length - 1)],
                        methods[rnd(0, methods.length - 1)],
                        "感谢医护人员的耐心诊治，服务态度好。",
                        remarks[rnd(0, remarks.length - 1)]
                });
            }
        }
        batch(conn, "INSERT INTO TR_SVC_QUALITY_CMPL (id, stat_date, complaint_time, dept_code, dept_name,"
                + " person_name, position, category, result, remark)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", cmpl);
        batch(conn, "INSERT INTO TR_SVC_QUALITY_PRZ (id, stat_date, praise_time, dept_code, dept_name,"
                + " person_name, position, method, feedback, remark)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", prz);
        out.println("  [服务质量] CMPL=" + cmpl.size() + " PRZ=" + prz.size());
    }

    private static void seedSpecialtyTreatment(Connection conn) throws SQLException {
        List<Object[]> rows = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (String[] dept : DEPTS) {
                int cnt = rnd(10, 120);
                rows.add(new Object[]{day, dept[0], dept[1], Integer.valueOf(cnt),
                        Double.valueOf(round2(cnt * (30 + rnd(0, 200)))), Integer.valueOf(rnd(5, 100))});
            }
        }
        batch(conn, "INSERT INTO TR_SPEC_TREAT_OV (stat_date, dept_code, dept_name, treatment_count,"
                + " treatment_amount, patient_count) VALUES (?,?,?,?,?,?)", rows);
        out.println("  [专科治疗量] TR_SPEC_TREAT_OV=" + rows.size());
    }

    private static void seedWindowStats(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> age = new ArrayList<Object[]>();
        List<Object[]> tm = new ArrayList<Object[]>();
        List<Object[]> src = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            ov.add(new Object[]{day, Integer.valueOf(rnd(100, 600)), Integer.valueOf(rnd(80, 500)),
                    Integer.valueOf(rnd(5, 60))});
            for (String g : AGE_GROUPS) {
                age.add(new Object[]{day, g, Integer.valueOf(rnd(5, 150))});
            }
            for (String bt : WINDOW_BIZ_TYPES) {
                for (String t : TIME_SLOTS) {
                    tm.add(new Object[]{day, bt, t, Integer.valueOf(rnd(10, 200))});
                }
            }
            for (String s : REG_SOURCE) {
                src.add(new Object[]{day, s, Integer.valueOf(rnd(20, 300))});
            }
        }
        batch(conn, "INSERT INTO TR_WIN_STAT_OV (id, stat_date, register_count, payment_count, refund_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_WIN_STAT_AGE (id, stat_date, age_group, patient_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", age);
        batch(conn, "INSERT INTO TR_WIN_STAT_TM (id, stat_date, business_type, time_slot, business_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", tm);
        batch(conn, "INSERT INTO TR_WIN_STAT_SRC (id, stat_date, source_name, source_count)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", src);
        out.println("  [人工窗口] OV=" + ov.size() + " AGE=" + age.size() + " TM=" + tm.size()
                + " SRC=" + src.size());
    }

    private static void seedCashSettle(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        List<Object[]> cht = new ArrayList<Object[]>();
        // 概览表走 selectOne (CashSettleMapper.queryOverview 无聚合), 只能有 1 行, 写多了报 TooManyResults
        int[] vals = new int[9];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = rnd(20, 400);
        }
        ov.add(new Object[]{dayAt(DAYS - 1),
                Integer.valueOf(vals[0]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[1]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[2]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[3]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[4]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[5]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[6]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[7]), Integer.valueOf(rnd(-50, 50)),
                Integer.valueOf(vals[8]), Integer.valueOf(rnd(-50, 50))});
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            // 每个收费员 × 每种业务项都要有数据。三个页签各自的分组口径：
            //   按收费员统计 → 按 cashier_name 汇总
            //   按来源方式统计 → 按 item_type 汇总
            //   工作量报表 → 按 (日期, 收费员) 透视出 5 个业务列
            // 如果只给每个业务项轮换一个收费员，工作量报表里每个收费员只有 1~2 列有值
            for (String cashier : CASHIER_NAMES) {
                for (String itemType : CASHIER_ITEM_TYPES) {
                    dtl.add(new Object[]{day, day, cashier, itemType,
                            Double.valueOf(round2(rnd(1000, 20000)))});
                }
            }
        }
        // 图表数据：页面这张柱图是「日期 × 每日业务量」（副标题显示统计口径），
        // 所以按天造。表里没有 chart_type 列，用 chart_title 区分两张图：
        //   按收费员统计 → 收费员业务工作量分析
        //   按来源方式统计 → 来源方式工作量分析
        java.text.SimpleDateFormat mdFmt = new java.text.SimpleDateFormat("MM-dd");
        java.text.SimpleDateFormat ymdFmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String range = ymdFmt.format(dayAt(0)) + "~" + ymdFmt.format(dayAt(DAYS - 1));
        for (String title : CASHIER_CHART_TITLES) {
            String subTitle = title.startsWith("收费员") ? "全部收费员" : "全部来源方式";
            for (int d = 0; d < DAYS; d++) {
                Date day = dayAt(d);
                cht.add(new Object[]{day, title, subTitle, range, mdFmt.format(day),
                        Integer.valueOf(rnd(500, 3000))});
            }
        }
        batch(conn, "INSERT INTO TR_CASH_SETTLE_OV (id, stat_date, appointment_register,"
                + " appointment_register_compare, appointment_fetch, appointment_fetch_compare,"
                + " today_register, today_register_compare, refund, refund_compare, outpatient_charge,"
                + " outpatient_charge_compare, outpatient_refund, outpatient_refund_compare, prepayment,"
                + " prepayment_compare, hospital_refund, hospital_refund_compare, discharge_settlement,"
                + " discharge_settlement_compare)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_CASH_SETTLE_DTL (id, stat_date, item_date, cashier_name, item_type,"
                + " item_value) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", dtl);
        batch(conn, "INSERT INTO TR_CASH_SETTLE_CHT (id, stat_date, chart_title, chart_subtitle,"
                + " date_range, category, data_value) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?)", cht);
        out.println("  [收费员结账] OV=" + ov.size() + " DTL=" + dtl.size() + " CHT=" + cht.size());
    }

    private static void seedDischSettle(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        List<Object[]> cht = new ArrayList<Object[]>();
        // 概览表走 selectOne (DischSettleMapper.queryOverview 无聚合), 只能有 1 行
        int total0 = rnd(30, 200);
        int discharged0 = rnd(10, total0);
        ov.add(new Object[]{dayAt(DAYS - 1), Integer.valueOf(total0), Integer.valueOf(rnd(-20, 20)),
                Integer.valueOf(discharged0), Integer.valueOf(rnd(-20, 20)),
                Integer.valueOf(total0 - discharged0), Integer.valueOf(rnd(-20, 20)),
                Double.valueOf(round2(rnd(50000, 500000))), Integer.valueOf(rnd(-20, 20))});
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            int total = rnd(30, 200);
            int discharged = rnd(10, total);
            int notDischarged = total - discharged;
            dtl.add(new Object[]{day, day,
                    Integer.valueOf(rnd(30, 180)), Integer.valueOf(total), Integer.valueOf(rnd(-20, 20)),
                    Integer.valueOf(rnd(10, 150)), Integer.valueOf(discharged), Integer.valueOf(rnd(-20, 20)),
                    Integer.valueOf(rnd(5, 80)), Integer.valueOf(notDischarged), Integer.valueOf(rnd(-20, 20)),
                    Double.valueOf(round2(rnd(40000, 400000))), Double.valueOf(round2(rnd(50000, 500000))),
                    Integer.valueOf(rnd(-20, 20))});
        }

        // 图表数据是「一次报告一份快照」，不是按天：
        // 页面把整个数组直接喂给饼图，按天写会出现 124 个同名扇区。
        // 三种 chart_type 都要有，少一种对应那块图表就是空的。
        String[] dischChartTypes = {"CHANNEL", "PATIENT_TYPE", "AMOUNT_TYPE"};
        String[][] dischItems = {
                {"窗口", "自助机", "医保", "自费"},
                {"现金", "银行卡", "微信", "支付宝"},
                {"检查费", "药费", "治疗费", "床位费", "欠费", "预交金"},
        };
        for (int i = 0; i < dischChartTypes.length; i++) {
            for (String item : dischItems[i]) {
                cht.add(new Object[]{dayAt(DAYS - 1), dischChartTypes[i], item,
                        Integer.valueOf(rnd(10, 500)), Integer.valueOf(rnd(-50, 50))});
            }
        }
        batch(conn, "INSERT INTO TR_DISCH_SETTLE_OV (id, stat_date, total_discharge_count,"
                + " total_discharge_compare, discharged_count, discharged_compare, not_discharged_count,"
                + " not_discharged_compare, settlement_amount, settlement_amount_compare)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_DISCH_SETTLE_DTL (id, stat_date, item_date, total_last, total_current,"
                + " total_compare, discharged_last, discharged_current, discharged_compare,"
                + " not_discharged_last, not_discharged_current, not_discharged_compare, amount_last,"
                + " amount_current, amount_compare)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", dtl);
        batch(conn, "INSERT INTO TR_DISCH_SETTLE_CHT (id, stat_date, chart_type, item_name, item_value,"
                + " item_compare) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", cht);
        out.println("  [出院结算] OV=" + ov.size() + " DTL=" + dtl.size() + " CHT=" + cht.size());
    }

    private static void seedTreatmentStats(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        List<Object[]> trend = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            int pc = rnd(60, 400);
            int tc = pc + rnd(20, 200);
            double amt = round2(rnd(10000, 150000));
            ov.add(new Object[]{day, Integer.valueOf(pc), Integer.valueOf(tc), Double.valueOf(amt)});
            for (String[] dept : DEPTS) {
                int dpc = rnd(5, 80);
                int dtc = dpc + rnd(2, 40);
                dtl.add(new Object[]{day, dept[0], Integer.valueOf(dpc), Integer.valueOf(dtc),
                        Double.valueOf(round2(rnd(1000, 30000)))});
            }
            for (int k = 0; k < 30; k++) {
                trend.add(new Object[]{day, dayAt(d - k >= 0 ? d - k : 0), Integer.valueOf(rnd(10, 200))});
            }
        }
        batch(conn, "INSERT INTO TR_TREAT_STAT_OV (id, stat_date, patient_count, treatment_count,"
                + " treatment_amount) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_TREAT_STAT_DTL (id, stat_date, dept_code, patient_count,"
                + " treatment_count, treatment_amount) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", dtl);
        batch(conn, "INSERT INTO TR_TREAT_STAT_TREND (id, stat_date, trend_date, trend_value)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?)", trend);

        // 治疗项目明细：页面「TOP10 治疗项目」图用，按天造，后端按项目汇总取前 10
        List<Object[]> item = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (String name : TREAT_ITEMS) {
                int cnt = rnd(5, 120);
                item.add(new Object[]{day, name, Integer.valueOf(cnt),
                        Double.valueOf(round2(cnt * (50 + rnd(0, 300))))});
            }
        }
        batch(conn, "INSERT INTO TR_TREAT_STAT_ITEM (id, stat_date, item_name, treatment_count,"
                + " treatment_amount) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", item);

        out.println("  [治疗统计] OV=" + ov.size() + " DTL=" + dtl.size() + " TREND=" + trend.size()
                + " ITEM=" + item.size());
    }

    private static void seedInpatPrepay(Connection conn) throws SQLException {
        List<Object[]> ov = new ArrayList<Object[]>();
        List<Object[]> dtl = new ArrayList<Object[]>();
        List<Object[]> cht = new ArrayList<Object[]>();
        // 概览表走 selectOne (InpatPrepayMapper.queryOverview 无聚合), 只能有 1 行
        int cnt0 = rnd(20, 200);
        double amt0 = round2(rnd(20000, 300000));
        ov.add(new Object[]{dayAt(DAYS - 1), Integer.valueOf(cnt0), Integer.valueOf(rnd(-20, 20)),
                Double.valueOf(amt0), Integer.valueOf(rnd(-20, 20))});
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            String[] dataTypes = {"SUMMARY", "INCOME", "REFUND"};
            for (String t : dataTypes) {
                // 每类单独取值：原来 cnt/amt 在循环外算一次，导致汇总/进项/退项三张表数值完全一样
                int cnt = rnd(20, 200);
                double amt = round2(rnd(20000, 300000));
                dtl.add(new Object[]{day, day, t,
                        Integer.valueOf(rnd(20, 180)), Integer.valueOf(cnt), Integer.valueOf(rnd(-20, 20)),
                        Double.valueOf(round2(rnd(20000, 250000))), Double.valueOf(amt),
                        Integer.valueOf(rnd(-20, 20))});
            }
            // TREND: 一天一个点。data_value=本期值，compare_value=上期值（后端直接当两个系列用）
            cht.add(new Object[]{day, "TREND", "住院预交金趋势", "近 30 天", "30天",
                    String.format("%02d-%02d", Integer.valueOf(day.toLocalDate().getMonthValue()),
                            Integer.valueOf(day.toLocalDate().getDayOfMonth())),
                    "本期", Integer.valueOf(rnd(50, 2000)), Integer.valueOf(rnd(50, 2000))});

        }

        // 渠道 / 支付方式两类图各一份快照（趋势图才是按天一个点）：
        // 页面把数组直接喂给饼图和堆叠图，按天写会出现几百个同名扇区/类目。
        // CHANNEL: category=渠道，series_name=支付方式 —— 一行同时供三张图用：
        //   按 category 汇总得渠道饼图，按 series_name 汇总得支付方式饼图，交叉得堆叠图。
        for (String ch : INPAT_CHANNELS) {
            for (String pt : INPAT_PAY_TYPES) {
                cht.add(new Object[]{dayAt(DAYS - 1), "CHANNEL", "渠道支付方式分析", "近 30 天", "30天",
                        ch, pt, Integer.valueOf(rnd(20, 400)), Integer.valueOf(rnd(20, 400))});
            }
        }
        // PAY_TYPE: category=支付方式（退项用）
        for (String pt : INPAT_PAY_TYPES) {
            cht.add(new Object[]{dayAt(DAYS - 1), "PAY_TYPE", "退项支付方式分析", "近 30 天", "30天",
                    pt, "退项", Integer.valueOf(rnd(10, 200)), Integer.valueOf(rnd(10, 200))});
        }
        batch(conn, "INSERT INTO TR_INPAT_PREPAY_OV (id, stat_date, prepayment_count,"
                + " prepayment_count_compare, prepayment_amount, prepayment_amount_compare)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?)", ov);
        batch(conn, "INSERT INTO TR_INPAT_PREPAY_DTL (id, stat_date, item_date, data_type, count_last,"
                + " count_current, count_compare, amount_last, amount_current, amount_compare)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", dtl);
        batch(conn, "INSERT INTO TR_INPAT_PREPAY_CHT (id, stat_date, chart_type, chart_title,"
                + " chart_subtitle, date_range, category, series_name, data_value, compare_value)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", cht);
        out.println("  [住院预交金] OV=" + ov.size() + " DTL=" + dtl.size() + " CHT=" + cht.size());
    }

    /**
     * 门诊财务表：保持 RCPT_NO / ACCT_NO 的引用一致，让 bt1~bt12 的 JOIN 都能命中。
     */
    private static void seedOutpatientFinance(Connection conn) throws SQLException {
        List<Object[]> clinic = new ArrayList<Object[]>();
        List<Object[]> rcpt = new ArrayList<Object[]>();
        List<Object[]> acct = new ArrayList<Object[]>();
        List<Object[]> pay = new ArrayList<Object[]>();
        List<Object[]> queue = new ArrayList<Object[]>();
        List<Object[]> acctMoney = new ArrayList<Object[]>();

        String[] operators = {"9101", "C746", "W001", "W002", "W003"};
        String[] sources = {"窗口", "自助机", "微信公众号", "APP"};

        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            String acctNo = "A" + String.format("%08d", Integer.valueOf(d + 1));
            int rcptsNum = 0;
            int refundNum = 0;
            double totalCosts = 0;
            double refundAmount = 0;

            //
            // 票号按「患者 + 当天 + 前缀 + 连号」组织:
            // queryVisitCounts / queryRcptCountByOperator 会把同患者同日同前缀的连号合并成 1 人次,
            // 所以每组给 1~4 张连号票, 组与组之间留号码空档, 让报表的人次口径能算出有意义的数字。
            //
            int n = rnd(25, 45);
            int receiptNo = d * 10000;
            int i = 0;
            while (i < n) {
                int groupSize = Math.min(rnd(1, 4), n - i);
                String patient = "P" + String.format("%05d", Integer.valueOf(rnd(1, 60)));
                String operator = operators[rnd(0, operators.length - 1)];
                String prefix = "L" + String.format("%04d", Integer.valueOf(d));
                String visitNo = "V" + String.format("%08d", Integer.valueOf(d * 1000 + i));
                Date visitDate = visitDate(day);
                String firstRcptNo = prefix + String.format("%04d", Integer.valueOf(receiptNo));

                for (int k = 0; k < groupSize; k++) {
                    String rcptNo = prefix + String.format("%04d", Integer.valueOf(receiptNo + k));
                    // 每组最多一张退费票, 且必须是组首票 —— 与 REFUNDED_RCPT_NO 的指向一致
                    boolean isRefund = k == 0 && rnd(0, 100) < 12;
                    double charges = round2(rnd(50, 2000));
                    if (isRefund) {
                        charges = -charges;
                        refundNum++;
                        refundAmount += charges;
                    } else {
                        rcptsNum++;
                        totalCosts += charges;
                    }

                    // IS_RETURN_TYPE 的语义（看 OutpatientFinanceMapper 的三个分支）：
                    //   1  = 正常就诊（进项，净量 +1）
                    //   -1 = 退号（退项，净量 -1）
                    // 写反了的话「净量」会变成负数，柱状图从顶部零点往下长（看着就是倒的）
                    clinic.add(new Object[]{
                            patient, day, visitDate, visitNo,
                            isRefund ? day : null, Integer.valueOf(isRefund ? -1 : 1),
                            Double.valueOf(round2(rnd(5, 50))), Double.valueOf(round2(rnd(10, 120))),
                            DEPTS[rnd(0, DEPTS.length - 1)][1] + "|" + day,
                            (rnd(0, 1) == 0) ? "上午" : "下午", operator
                    });
                    rcpt.add(new Object[]{
                            rcptNo, patient, day, visitDate, Double.valueOf(charges),
                            Double.valueOf(charges), isRefund ? firstRcptNo : null,
                            operator, (rnd(0, 1) == 0) ? "1" : "2"
                    });
                    for (int m = 0; m < 2; m++) {
                        String mt = MONEY_TYPES[rnd(0, MONEY_TYPES.length - 1)];
                        pay.add(new Object[]{rcptNo, mt,
                                Double.valueOf(isRefund ? 0d : charges),
                                Double.valueOf(isRefund ? charges : 0d)});
                    }
                    if (rnd(0, 100) < 60) {
                        queue.add(new Object[]{
                                "S" + String.format("%08d", Integer.valueOf(d * 1000 + i)), patient,
                                day, visitNo, sources[rnd(0, sources.length - 1)],
                                "reserve", "used", visitDate
                        });
                    }
                }
                receiptNo += groupSize + rnd(1, 3);
                i += groupSize;
            }
            acct.add(new Object[]{acctNo, day, day, operators[rnd(0, operators.length - 1)],
                    Double.valueOf(round2(totalCosts)), Double.valueOf(round2(refundAmount)),
                    Integer.valueOf(rcptsNum), Integer.valueOf(refundNum)});
            for (String mt : MONEY_TYPES) {
                acctMoney.add(new Object[]{acctNo, mt, Double.valueOf(round2(rnd(1000, 50000))),
                        Double.valueOf(round2(rnd(0, 5000)))});
            }
        }

        batch(conn, "INSERT INTO TR_OUTP_FIN_CLINIC_MASTER (id, PATIENT_ID, STAT_DATE, VISIT_DATE, VISIT_NO,"
                + " RETURNED_DATE, IS_RETURN_TYPE, REGIST_FEE, CLINIC_FEE, CLINIC_LABEL, VISIT_TIME_DESC,"
                + " OPERATOR_NO) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?)", clinic);
        batch(conn, "INSERT INTO TR_OUTP_FIN_RCPT_ACCT (id, RCPT_NO, PATIENT_ID, STAT_DATE, VISIT_DATE,"
                + " TOTAL_CHARGES, TOTAL_COSTS, REFUNDED_RCPT_NO, OPERATOR_NO, BILL_CLASS)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?,?)", rcpt);
        batch(conn, "INSERT INTO TR_OUTP_FIN_ACCT_MASTER (id, ACCT_NO, ACCT_DATE, STAT_DATE, OPERATOR_NO,"
                + " TOTAL_COSTS, REFUND_AMOUNT, RCPTS_NUM, REFUND_NUM)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?)", acct);
        batch(conn, "INSERT INTO TR_OUTP_FIN_PAYMENTS_MONEY (id, RCPT_NO, MONEY_TYPE, PAYMENT_AMOUNT,"
                + " REFUNDED_AMOUNT) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", pay);
        batch(conn, "INSERT INTO TR_OUTP_FIN_MOP_QUEUE (id, SCHEDULE_ID, PATIENT_ID, STAT_DATE, VISIT_NO,"
                + " SOURCE_NAME, QUEUE_TYPE, IS_USED, VISIT_DATE)"
                + " VALUES (seq_tr_reports.NEXTVAL,?,?,?,?,?,?,?,?)", queue);
        batch(conn, "INSERT INTO TR_OUTP_FIN_ACCT_MONEY (id, ACCT_NO, MONEY_TYPE, INCOME_AMOUNT,"
                + " REFUNDED_AMOUNT) VALUES (seq_tr_reports.NEXTVAL,?,?,?,?)", acctMoney);

        out.println("  [门诊财务] CLINIC=" + clinic.size() + " RCPT=" + rcpt.size() + " ACCT=" + acct.size()
                + " PAY=" + pay.size() + " QUEUE=" + queue.size() + " ACCT_MONEY=" + acctMoney.size());
    }

    private static void seedDailyWideTable(Connection conn) throws SQLException {
        List<Object[]> rows = new ArrayList<Object[]>();
        for (int d = 0; d < DAYS; d++) {
            Date day = dayAt(d);
            for (String[] dept : DEPTS) {
                int total = rnd(80, 500);
                int tuiHao = rnd(2, Math.max(3, total / 12));
                int shuangYue = rnd(2, Math.max(3, total / 15));
                int[] tuiRegion = split(tuiHao, 5);
                int[] shuangRegion = split(shuangYue, 5);
                int window = rnd(0, tuiHao);
                int self = tuiHao - window;
                int[] tuiAge = split(tuiHao, AGE_GROUPS.length);
                int[] shuangAge = split(shuangYue, AGE_GROUPS.length);

                List<Object> row = new ArrayList<Object>();
                row.add(day);
                row.add(dept[0]);
                row.add(Integer.valueOf(total));
                row.add(Integer.valueOf(tuiHao));
                row.add(Integer.valueOf(shuangYue));
                for (int v : tuiRegion) {
                    row.add(Integer.valueOf(v));
                }
                for (int v : shuangRegion) {
                    row.add(Integer.valueOf(v));
                }
                row.add(Integer.valueOf(window));
                row.add(Integer.valueOf(self));
                for (int v : tuiAge) {
                    row.add(Integer.valueOf(v));
                }
                for (int v : shuangAge) {
                    row.add(Integer.valueOf(v));
                }
                rows.add(row.toArray());
            }
        }
        StringBuilder cols = new StringBuilder();
        cols.append("stats_date, dept_code, total_guahao, tui_hao_shu, shuang_yue_shu,");
        cols.append(" tui_hao_chong_qing, tui_hao_si_chuan, tui_hao_gui_zhou, tui_hao_yun_nan, tui_hao_qi_ta,");
        cols.append(" shuang_yue_chong_qing, shuang_yue_si_chuan, shuang_yue_gui_zhou, shuang_yue_yun_nan,");
        cols.append(" shuang_yue_qi_ta, tui_hao_chuang_kou, tui_hao_zi_zhu_ji");
        for (int i = 0; i < AGE_GROUPS.length; i++) {
            cols.append(", tui_hao_age_").append(ageSuffix(i));
        }
        for (int i = 0; i < AGE_GROUPS.length; i++) {
            cols.append(", shuang_yue_age_").append(ageSuffix(i));
        }

        StringBuilder marks = new StringBuilder("seq_tr_reports.NEXTVAL");
        for (int i = 0; i < 2 + 3 + 5 + 5 + 2 + AGE_GROUPS.length + AGE_GROUPS.length; i++) {
            marks.append(",?");
        }

        batch(conn, "INSERT INTO TR_OUTPATIENT_STATS_DAY_RESULT (id, " + cols + ") VALUES (" + marks + ")",
                rows);
        out.println("  [门诊每日宽表] TR_OUTPATIENT_STATS_DAY_RESULT=" + rows.size());
    }

    /** 年龄列后缀，与 20 号 DDL 的列名一一对应。 */
    private static String ageSuffix(int i) {
        String[] suffixes = {"0_14", "15_19", "20_29", "30_39", "40_49",
                "50_59", "60_69", "70_79", "80_89", "90_up"};
        return suffixes[i];
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    /** 第 d 天的日期，d=0 表示统计区间第一天。 */
    private static Date dayAt(int d) {
        return Date.valueOf(startDate.plusDays(d));
    }

    /**
     * 就诊日期：大部分与统计日期同日，少量跨日，另有一小部分远早于 2025-02-08，
     * 以便 bt8 的「当日挂号 / 门诊缴费」两个分支都有数据。
     */
    private static Date visitDate(Date statDate) {
        int r = rnd(0, 99);
        LocalDate base = statDate.toLocalDate();
        if (r < 70) {
            return statDate;
        }
        if (r < 90) {
            return Date.valueOf(base.minusDays(rnd(1, 5)));
        }
        return Date.valueOf(base.minusDays(rnd(200, 900)));
    }

    /** 把一个总数拆成 parts 份，合计保持不变。 */
    private static int[] split(int total, int parts) {
        int[] res = new int[parts];
        int left = total;
        for (int i = 0; i < parts - 1 && left > 0; i++) {
            int v = rnd(0, left);
            res[i] = v;
            left -= v;
        }
        res[parts - 1] = Math.max(0, left);
        return res;
    }

    /** 百分比字符串，如 "87.35"。 */
    private static String rate(int min, int max) {
        return String.valueOf(round2(min + rnd.nextDouble() * (max - min)));
    }

    /** 按月环比增长率字符串。 */
    private static String growth(int current, int last) {
        if (last == 0) {
            return "0.00";
        }
        return String.valueOf(round2((current - last) * 100.0d / last));
    }

    /** 一位小数。 */
    private static String dec(int min, int max) {
        return String.valueOf(round2(min + rnd.nextDouble() * (max - min)));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0d) / 100.0d;
    }

    private static int rnd(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + rnd.nextInt(max - min + 1);
    }

    /**
     * 批量插入。
     */
    private static void batch(Connection conn, String sql, List<Object[]> rows) throws SQLException {
        if (rows.isEmpty()) {
            return;
        }
        // 先校验参数个数，否则 Oracle 只会报 ORA-00947 之类难以定位的错误
        int expected = countParams(sql);
        int got = rows.get(0).length;
        if (expected != got) {
            throw new SQLException("参数个数不匹配: SQL 需要 " + expected + " 个, 实际提供 " + got
                    + " 个。SQL = " + sql);
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            int n = 0;
            for (Object[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    ps.setObject(i + 1, row[i]);
                }
                ps.addBatch();
                if (++n % 500 == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private static int countParams(String sql) {
        int n = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                n++;
            }
        }
        return n;
    }

    private static void close(ResultSet r, Statement s) {
        if (r != null) {
            try {
                r.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
        if (s != null) {
            try {
                s.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    private ReportTestDataSeeder() {
    }
}
