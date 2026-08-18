import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * ETL 测试种子数据初始化
 * 用法: cd reports && mvn compile && java -cp target/classes:$(mvn dependency:build-classpath -q -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout) com.reports.etl.SeedData
 */
public class SeedData {

    static final String KEY = "etlDsMetaKey2026";
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = now.minusDays(3);

        createSourceDb(base);
        createTargetDb();
        insertEtlMeta(base);

        System.out.println("\n=== 种子数据初始化完成 ===");
        System.out.println("SOURCE: ./data/etl_source_test.mv.db (50员工 + 30订单)");
        System.out.println("TARGET: ./data/etl_target_test.mv.db");
        System.out.println("元数据: ./data/etl_meta.mv.db (2数据源+3来源+3任务+15映射+7执行日志)");
    }

    private static String fmt(LocalDateTime t) { return t.format(F); }

    private static void createSourceDb(LocalDateTime base) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:./data/etl_source_test;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS employee ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), dept VARCHAR(50), "
                + "salary DECIMAL(10,2), hire_date DATE, status VARCHAR(20))");
        stmt.execute("CREATE TABLE IF NOT EXISTS orders ("
                + "order_id INT AUTO_INCREMENT PRIMARY KEY, customer_name VARCHAR(50), "
                + "amount DECIMAL(10,2), order_date DATE, status VARCHAR(20))");
        // 清空已有数据
        stmt.execute("DELETE FROM employee");
        stmt.execute("ALTER TABLE employee ALTER COLUMN id RESTART WITH 1");
        stmt.execute("DELETE FROM orders");
        stmt.execute("ALTER TABLE orders ALTER COLUMN order_id RESTART WITH 1");

        Random r = new Random(42);
        String[] depts = {"研发部", "市场部", "财务部", "人事部", "运营部"};
        String[] statuses = {"active", "active", "active", "on_leave", "active"};
        for (int i = 1; i <= 50; i++) {
            java.sql.Date hd = java.sql.Date.valueOf(base.toLocalDate().minusDays(r.nextInt(365)));
            stmt.execute(String.format("INSERT INTO employee(name,dept,salary,hire_date,status) VALUES('员工%d','%s',%.2f,'%s','%s')",
                    i, depts[r.nextInt(depts.length)], 8000 + r.nextDouble() * 20000, hd, statuses[r.nextInt(statuses.length)]));
        }
        String[] ost = {"pending", "shipped", "completed", "completed", "completed"};
        for (int i = 1; i <= 30; i++) {
            java.sql.Date od = java.sql.Date.valueOf(base.toLocalDate().minusDays(r.nextInt(90)));
            stmt.execute(String.format("INSERT INTO orders(customer_name,amount,order_date,status) VALUES('客户%d',%.2f,'%s','%s')",
                    i, 50 + r.nextDouble() * 500, od, ost[r.nextInt(ost.length)]));
        }
        stmt.close(); conn.close();
        System.out.println("[OK] SOURCE 测试库已创建");
    }

    private static void createTargetDb() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:./data/etl_target_test;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS dim_employee ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, emp_name VARCHAR(50), department VARCHAR(50), "
                + "salary NUMBER, hire_date DATE, status VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS fact_orders ("
                + "order_id INT AUTO_INCREMENT PRIMARY KEY, customer_name VARCHAR(50), "
                + "amount DECIMAL(10,2), order_date DATE, order_status VARCHAR(20))");
        stmt.close(); conn.close();
        System.out.println("[OK] TARGET 测试库已创建");
    }

    private static void insertEtlMeta(LocalDateTime base) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:./data/etl_meta;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
        Statement stmt = conn.createStatement();

        // 建表（幂等）
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_datasource (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), db_type VARCHAR(30), driver_class VARCHAR(255), url VARCHAR(500), username VARCHAR(100), password VARCHAR(500), role VARCHAR(20), pool_initial_size INT DEFAULT 2, pool_max_active INT DEFAULT 10, enabled SMALLINT DEFAULT 1, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_source (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(200), type VARCHAR(20), source_ds_id BIGINT, config_json TEXT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_task (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(200), extract_type VARCHAR(30), source_id BIGINT, source_ds_id BIGINT, target_ds_id BIGINT, target_table VARCHAR(200), write_mode VARCHAR(20) DEFAULT 'INSERT', query_index_cols VARCHAR(500), update_cols VARCHAR(500), cron VARCHAR(100), enabled SMALLINT DEFAULT 0, max_rows INT DEFAULT 10000, batch_size INT DEFAULT 500, incremental SMALLINT DEFAULT 0, inc_field VARCHAR(100), inc_placeholder VARCHAR(100), retry_count INT DEFAULT 0, alert_config_json TEXT, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_mapping (id BIGINT AUTO_INCREMENT PRIMARY KEY, task_id BIGINT, src_field VARCHAR(300), tgt_field VARCHAR(200), default_value VARCHAR(500), is_update_col SMALLINT DEFAULT 0, src_type VARCHAR(50), tgt_type VARCHAR(50), sort_order INT DEFAULT 0, create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_task_log (id BIGINT AUTO_INCREMENT PRIMARY KEY, task_id BIGINT, task_name VARCHAR(200), start_time TIMESTAMP, end_time TIMESTAMP, status VARCHAR(20), extracted_rows INT DEFAULT 0, written_rows INT DEFAULT 0, error_msg VARCHAR(2000), trigger_type VARCHAR(20), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_step_log (id BIGINT AUTO_INCREMENT PRIMARY KEY, log_id BIGINT, step_name VARCHAR(30), status VARCHAR(20), start_time TIMESTAMP, end_time TIMESTAMP, duration_ms BIGINT, rows_count INT DEFAULT 0, detail VARCHAR(2000), create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("CREATE TABLE IF NOT EXISTS etl_log_config (id BIGINT DEFAULT 1 PRIMARY KEY, save_days INT DEFAULT 30, auto_clean SMALLINT DEFAULT 1, update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        stmt.execute("MERGE INTO etl_log_config (id, save_days, auto_clean) KEY(id) VALUES(1, 30, 1)");

        // 数据源
        stmt.execute("INSERT INTO etl_datasource(name, db_type, driver_class, url, username, password, role, enabled) "
                + "VALUES('测试来源H2', 'H2', 'org.h2.Driver', 'jdbc:h2:./data/etl_source_test', 'sa', '', 'SOURCE', 1)");
        stmt.execute("INSERT INTO etl_datasource(name, db_type, driver_class, url, username, password, role, enabled) "
                + "VALUES('测试目标H2', 'H2', 'org.h2.Driver', 'jdbc:h2:./data/etl_target_test', 'sa', '', 'TARGET', 1)");

        // 来源1：PROC 员工表抽取
        long sourceProcId = 1;
        String procCfg = "{\"procName\":\"EMPLOYEE\",\"callTemplate\":\"SELECT * FROM employee\",\"cursorParamName\":\"\",\"cursorParamIdx\":0,\"inParamsJson\":\"\",\"maxPages\":10,\"maxRows\":1000,\"batchSize\":100}";
        stmt.execute("INSERT INTO etl_source(name, type, source_ds_id, config_json) VALUES('员工表抽取(PROC)', 'PROC', 1, '" + procCfg.replace("'", "''") + "')");

        // 来源2：WS API
        long sourceWsId = 2;
        String wsCfg = "{\"wsType\":\"REST\",\"url\":\"https://jsonplaceholder.typicode.com/posts\",\"soapAction\":\"\",\"requestBodyTemplate\":\"\",\"responsePath\":\"\",\"headersJson\":\"{}\",\"extractParamsJson\":\"{}\",\"maxPages\":1,\"maxRows\":100,\"batchSize\":100}";
        stmt.execute("INSERT INTO etl_source(name, type, source_ds_id, config_json) VALUES('Posts API(WS)', 'WS', NULL, '" + wsCfg.replace("'", "''") + "')");

        // 任务1：员工 ETL
        long taskId1 = 1;
        stmt.execute("INSERT INTO etl_task(name, extract_type, source_id, source_ds_id, target_ds_id, target_table, write_mode, query_index_cols, update_cols, cron, enabled, max_rows, batch_size, incremental, inc_field, inc_placeholder, retry_count) "
                + "VALUES('员工数据同步', 'PROCEDURE', " + sourceProcId + ", 1, 2, 'dim_employee', 'INSERT', 'id', '', '', 0, 10000, 500, 0, '', '', 0)");

        // 映射：员工
        String[][] m1 = {
                {"id", "id", "", "0", "number", "NUMBER"},
                {"name", "emp_name", "", "0", "string", "VARCHAR"},
                {"dept", "department", "", "0", "string", "VARCHAR"},
                {"salary", "salary", "0", "0", "number", "DECIMAL"},
                {"hire_date", "hire_date", "", "0", "date", "DATE"},
                {"status", "status", "", "0", "string", "VARCHAR"}
        };
        for (int i = 0; i < m1.length; i++) {
            stmt.execute(String.format("INSERT INTO etl_mapping(task_id,src_field,tgt_field,default_value,is_update_col,src_type,tgt_type,sort_order) VALUES(%d,'%s','%s','%s',%s,'%s','%s',%d)",
                    taskId1, m1[i][0], m1[i][1], m1[i][2], m1[i][3], m1[i][4], m1[i][5], i + 1));
        }

        // 任务2：订单 ETL
        long taskId2 = 2;
        stmt.execute("INSERT INTO etl_task(name, extract_type, source_id, source_ds_id, target_ds_id, target_table, write_mode, query_index_cols, update_cols, cron, enabled, max_rows, batch_size, incremental, inc_field, inc_placeholder, retry_count) "
                + "VALUES('订单数据同步', 'PROCEDURE', " + sourceProcId + ", 1, 2, 'fact_orders', 'INSERT', 'order_id', '', '', 0, 10000, 500, 0, '', '', 0)");
        String[][] m2 = {
                {"order_id", "order_id", "", "0", "number", "NUMBER"},
                {"customer_name", "customer_name", "", "0", "string", "VARCHAR"},
                {"amount", "amount", "0", "0", "number", "DECIMAL"},
                {"order_date", "order_date", "", "0", "date", "DATE"},
                {"status", "order_status", "", "0", "string", "VARCHAR"}
        };
        for (int i = 0; i < m2.length; i++) {
            stmt.execute(String.format("INSERT INTO etl_mapping(task_id,src_field,tgt_field,default_value,is_update_col,src_type,tgt_type,sort_order) VALUES(%d,'%s','%s','%s',%s,'%s','%s',%d)",
                    taskId2, m2[i][0], m2[i][1], m2[i][2], m2[i][3], m2[i][4], m2[i][5], i + 1));
        }

        // 任务3：Posts API ETL
        long taskId3 = 3;
        stmt.execute("INSERT INTO etl_task(name, extract_type, source_id, source_ds_id, target_ds_id, target_table, write_mode, query_index_cols, update_cols, cron, enabled, max_rows, batch_size, incremental, inc_field, inc_placeholder, retry_count) "
                + "VALUES('Posts API同步', 'WEBSERVICE', " + sourceWsId + ", NULL, 2, 'dim_employee', 'INSERT', 'id', '', '', 0, 100, 50, 0, '', '', 0)");

        // 执行历史日志
        insertTaskLogs(stmt, base);
        insertStepLogs(stmt, base);

        stmt.close(); conn.close();
        System.out.println("[OK] ETL 元数据已写入 etl_meta.mv.db");
    }

    private static void insertTaskLogs(Statement stmt, LocalDateTime base) throws Exception {
        // log_id=1: 任务1成功（base+2天）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,trigger_type) "
                + "VALUES(1,'员工数据同步','"+fmt(base.plusDays(2).plusHours(10).plusMinutes(0))+"','"+fmt(base.plusDays(2).plusHours(10).plusMinutes(15))+"','SUCCESS',50,50,'SCHEDULED')");
        // log_id=2: 任务1成功（base+1天）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,trigger_type) "
                + "VALUES(1,'员工数据同步','"+fmt(base.plusDays(1).plusHours(10).plusMinutes(0))+"','"+fmt(base.plusDays(1).plusHours(10).plusMinutes(12))+"','SUCCESS',48,48,'SCHEDULED')");
        // log_id=3: 任务1失败（今天）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,error_msg,trigger_type) "
                + "VALUES(1,'员工数据同步','"+fmt(base.plusHours(10).plusMinutes(0))+"','"+fmt(base.plusHours(10).plusMinutes(3))+"','FAILED',0,0,'连接超时','SCHEDULED')");
        // log_id=4: 任务2成功（base+2天，手动）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,trigger_type) "
                + "VALUES(2,'订单数据同步','"+fmt(base.plusDays(2).plusHours(11).plusMinutes(0))+"','"+fmt(base.plusDays(2).plusHours(11).plusMinutes(8))+"','SUCCESS',30,30,'MANUAL')");
        // log_id=5: 任务2成功（base+1天）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,trigger_type) "
                + "VALUES(2,'订单数据同步','"+fmt(base.plusDays(1).plusHours(11).plusMinutes(0))+"','"+fmt(base.plusDays(1).plusHours(11).plusMinutes(7))+"','SUCCESS',30,30,'SCHEDULED')");
        // log_id=6: 任务3成功（base+2天）
        stmt.execute("INSERT INTO etl_task_log(task_id,task_name,start_time,end_time,status,extracted_rows,written_rows,trigger_type) "
                + "VALUES(3,'Posts API同步','"+fmt(base.plusDays(2).plusHours(12).plusMinutes(0))+"','"+fmt(base.plusDays(2).plusHours(12).plusMinutes(5))+"','SUCCESS',100,100,'SCHEDULED')");
    }

    private static void insertStepLogs(Statement stmt, LocalDateTime base) throws Exception {
        // log_id=1 步骤
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(1,'EXTRACT','SUCCESS','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(0)) + "','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(14)) + "',14000,50)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(1,'TRANSFORM','SUCCESS','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(14)) + "','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(14)) + "',100,50)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(1,'LOAD','SUCCESS','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(14)) + "','" + fmt(base.plusDays(2).plusHours(10).plusMinutes(15)) + "',500,50)");
        // log_id=3 失败步骤
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count,detail) VALUES(3,'EXTRACT','FAILED','" + fmt(base.plusHours(10).plusMinutes(0)) + "','" + fmt(base.plusHours(10).plusMinutes(3)) + "',3000,0,'Connection timeout after 3000ms')");
        // log_id=4 步骤
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(4,'EXTRACT','SUCCESS','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(0)) + "','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(7)) + "',7000,30)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(4,'TRANSFORM','SUCCESS','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(7)) + "','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(7)) + "',50,30)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(4,'LOAD','SUCCESS','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(7)) + "','" + fmt(base.plusDays(2).plusHours(11).plusMinutes(8)) + "',800,30)");
        // log_id=6 步骤
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(6,'EXTRACT','SUCCESS','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(0)) + "','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(4)) + "',4200,100)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(6,'TRANSFORM','SUCCESS','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(4)) + "','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(4)) + "',20,100)");
        stmt.execute("INSERT INTO etl_step_log(log_id,step_name,status,start_time,end_time,duration_ms,rows_count) VALUES(6,'LOAD','SUCCESS','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(4)) + "','" + fmt(base.plusDays(2).plusHours(12).plusMinutes(5)) + "',800,100)");
    }
}
