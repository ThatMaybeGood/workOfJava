-- 本地目标库初始化：幂等（连接池每次新建连接都会执行，不能 DROP）
CREATE TABLE IF NOT EXISTS tgt_sales_order (
    order_no   VARCHAR(30) PRIMARY KEY,
    cust_no    VARCHAR(20),
    product    VARCHAR(100),
    amount     DECIMAL(12,2),
    status     VARCHAR(10),
    etl_time   TIMESTAMP
);
