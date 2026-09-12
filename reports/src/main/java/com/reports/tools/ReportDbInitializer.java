package com.reports.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 报表库初始化工具。
 *
 * <p>按文件名顺序执行 {@code docs/ddl/*.sql}，把所有报表所需的表建到目标 Oracle 库；
 * 随后交给 {@link ReportTestDataSeeder} 灌入测试数据。
 *
 * <p><b>用法</b>（在 reports 目录下）：
 * <pre>
 *   mvn -q -o compile exec:java -Dexec.mainClass=com.reports.tools.ReportDbInitializer
 *
 *   # 或直接跑编译产物（需把 ojdbc8 + orai18n 放进 classpath）
 *   java -cp "target/classes;%USERPROFILE%\.m2\repository\com\oracle\database\jdbc\ojdbc8\21.8.0.0\ojdbc8-21.8.0.0.jar" \
 *        com.reports.tools.ReportDbInitializer
 * </pre>
 *
 * <p><b>参数</b>（均可省略，用默认值）：
 * <pre>
 *   --url=...       默认 jdbc:oracle:thin:@192.168.31.35:1521:orcl
 *   --user=...      默认 mine
 *   --password=...  默认 Pass1230
 *   --ddl-dir=...   默认 ./docs/ddl
 *   --ddl-only      只建表，不灌数据
 *   --seed-only     只灌数据，不建表
 *   --drop-only     只执行各 DDL 文件里的 DROP 语句（清库）
 * </pre>
 *
 * <p>可重复执行：DDL 文件开头的 DROP 语句在首次执行时会因对象不存在而报 ORA-00942，
 * 该错误被识别为预期情况跳过，不影响后续建表。
 */
public final class ReportDbInitializer {

    /** 预期内的错误码 —— 首次建库时 DROP 不存在的对象、重复创建序列等。 */
    private static final List<Integer> TOLERATED_ORA_CODES = Arrays.asList(
            942,   // ORA-00942 table or view does not exist        (首次执行 DROP)
            2289,  // ORA-02289 sequence does not exist              (首次执行 DROP SEQUENCE)
            955,   // ORA-00955 name is already used by an existing object
            1430,  // ORA-01430 column being added already exists
            1408   // ORA-01408 such column list already indexed
    );

    private static PrintStream out;

    public static void main(String[] args) throws Exception {
        try {
            out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8");
        } catch (Exception e) {
            out = System.out;
        }

        String url = arg(args, "--url", "jdbc:oracle:thin:@192.168.31.35:1521:orcl");
        String user = arg(args, "--user", "mine");
        String password = arg(args, "--password", "Pass1230");
        String ddlDir = arg(args, "--ddl-dir", "docs/ddl");
        boolean ddlOnly = has(args, "--ddl-only");
        boolean seedOnly = has(args, "--seed-only");
        boolean dropOnly = has(args, "--drop-only");

        out.println("报表库初始化");
        out.println("  URL      : " + url);
        out.println("  USER     : " + user);
        out.println("  DDL 目录 : " + new File(ddlDir).getAbsolutePath());

        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(true);
            out.println("  连接成功 : " + conn.getMetaData().getDatabaseProductVersion().trim());

            List<SchemaError> errors = new ArrayList<SchemaError>();

            if (!seedOnly) {
                out.println();
                out.println("===== 1. 执行 DDL =====");
                executeDdl(conn, ddlDir, dropOnly, errors);
            }
            if (dropOnly) {
                out.println();
                out.println("--drop-only 已指定，结束。");
                return;
            }
            if (!ddlOnly) {
                out.println();
                out.println("===== 2. 灌入测试数据 =====");
                // DDL 阶段用自动提交，灌数阶段关掉自动提交走批量提交，快很多
                conn.setAutoCommit(false);
                try {
                    ReportTestDataSeeder.seed(conn);
                } catch (SQLException e) {
                    errors.add(new SchemaError("(seed)", "写入测试数据失败: " + e.getMessage(), e));
                }
                out.println();
                out.println("===== 3. 校验 =====");
                ReportTestDataSeeder.verify(conn);
            }

            out.println();
            if (errors.isEmpty()) {
                out.println("全部完成，无错误。");
            } else {
                out.println("完成，但有 " + errors.size() + " 处错误：");
                for (SchemaError e : errors) {
                    out.println("  [FAIL] " + e.source + " -> " + e.message);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignore) {
                    // 关闭失败无实际影响
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // DDL
    // ------------------------------------------------------------------

    /**
     * 按文件名顺序执行 DDL 目录下的所有 .sql。
     */
    static void executeDdl(Connection conn, String ddlDir, boolean dropOnly, List<SchemaError> errors)
            throws Exception {
        File dir = new File(ddlDir);
        if (!dir.isDirectory()) {
            throw new IllegalStateException("DDL 目录不存在: " + dir.getAbsolutePath());
        }
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalStateException("无法列出 DDL 目录: " + dir.getAbsolutePath());
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareTo(b.getName());
            }
        });

        int ok = 0;
        int skipped = 0;
        for (File f : files) {
            if (!f.getName().toLowerCase().endsWith(".sql")) {
                continue;
            }
            String sql = readUtf8(f);
            List<String> statements = splitStatements(sql);
            int fileOk = 0;
            int fileSkipped = 0;
            for (String st : statements) {
                if (st.isEmpty()) {
                    continue;
                }
                String upper = st.toUpperCase();
                boolean isDrop = upper.startsWith("DROP ");
                if (dropOnly && !isDrop) {
                    continue;
                }
                if (!dropOnly && isDrop && isGuardOnlySkip(upper)) {
                    // 07 号文件只有说明性注释，无语句；此处保留分支以便将来加白名单
                    continue;
                }
                try (Statement s = conn.createStatement()) {
                    s.execute(st);
                    fileOk++;
                    ok++;
                } catch (SQLException e) {
                    if (TOLERATED_ORA_CODES.contains(Integer.valueOf(e.getErrorCode()))) {
                        fileSkipped++;
                        skipped++;
                    } else {
                        errors.add(new SchemaError(f.getName(),
                                firstLine(st) + "  ==> " + e.getMessage() + "\n         完整语句: "
                                        + st.replaceAll("\\s+", " "), e));
                    }
                }
            }
            out.println(String.format("  %-42s 执行 %2d 条, 跳过 %2d 条(对象不存在/已存在)",
                    f.getName(), Integer.valueOf(fileOk), Integer.valueOf(fileSkipped)));
        }
        out.println("  ---- DDL 汇总: 成功 " + ok + " 条, 预期跳过 " + skipped + " 条, 真实错误 "
                + errors.size() + " 条");
    }

    /** 预留：某些 DROP 语句按设计应跳过。当前无此类语句。 */
    private static boolean isGuardOnlySkip(String upper) {
        return false;
    }

    /**
     * 把 DDL 文本切成一条条语句。
     *
     * <p>按 {@code ;} 切分，但跳过字符串字面量里的分号与 {@code --} 行注释，避免误切。
     * 本项目的 DDL 不含 PL/SQL 块，因此无需处理 {@code /} 分隔符。
     */
    static List<String> splitStatements(String sql) {
        List<String> result = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'') {
                inQuote = !inQuote;
                cur.append(ch);
                continue;
            }
            if (!inQuote && ch == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                while (i < sql.length() && sql.charAt(i) != '\n') {
                    i++;
                }
                cur.append('\n');
                continue;
            }
            if (!inQuote && ch == ';') {
                String st = cur.toString().trim();
                if (!st.isEmpty()) {
                    result.add(st);
                }
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        String tail = cur.toString().trim();
        if (!tail.isEmpty()) {
            result.add(tail);
        }
        return result;
    }

    private static String readUtf8(File f) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }
        return sb.toString();
    }

    private static String firstLine(String s) {
        int nl = s.indexOf('\n');
        String one = (nl < 0 ? s : s.substring(0, nl)).trim();
        return one.length() > 90 ? one.substring(0, 90) + "..." : one;
    }

    // ------------------------------------------------------------------
    // 参数
    // ------------------------------------------------------------------

    static String arg(String[] args, String name, String def) {
        String prefix = name + "=";
        for (String a : args) {
            if (a.startsWith(prefix)) {
                return a.substring(prefix.length());
            }
        }
        return def;
    }

    static boolean has(String[] args, String name) {
        for (String a : args) {
            if (name.equals(a)) {
                return true;
            }
        }
        return false;
    }

    /** 一条建表/建索引错误。 */
    static final class SchemaError {
        final String source;
        final String message;
        final SQLException cause;

        SchemaError(String source, String message, SQLException cause) {
            this.source = source;
            this.message = message;
            this.cause = cause;
        }
    }

    private ReportDbInitializer() {
    }
}
