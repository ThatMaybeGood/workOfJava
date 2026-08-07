-- ============================================
-- ETL 演示源库（H2 内存库）
-- 模拟业务源：患者/门诊数据，用于抽取调试演示
-- ============================================

DROP TABLE IF EXISTS src_customer;
CREATE TABLE src_customer (
    id          BIGINT PRIMARY KEY,
    cust_no     VARCHAR(20),
    name        VARCHAR(50),
    age         INT,
    gender      VARCHAR(10),
    phone       VARCHAR(20),
    address     VARCHAR(200),
    balance     DECIMAL(12,2),
    status      VARCHAR(10) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELETE FROM src_customer;
INSERT INTO src_customer (id, cust_no, name, age, gender, phone, address, balance, status) VALUES
(1, 'C0001', '张伟', 35, 'M', '13800000001', '北京市朝阳区', 1234.50, 'ACTIVE'),
(2, 'C0002', '李娜', 28, 'F', '13800000002', '上海市浦东新区', 567.80, 'ACTIVE'),
(3, 'C0003', '王强', 42, 'M', '13800000003', '广州市天河区', 89.00, 'INACTIVE'),
(4, 'C0004', '刘洋', 30, 'F', '13800000004', '深圳市南山区', 3456.20, 'ACTIVE'),
(5, 'C0005', '陈静', 25, 'F', '13800000005', '杭州市西湖区', 123.45, 'ACTIVE'),
(6, 'C0006', '杨帆', 38, 'M', '13800000006', '成都市锦江区', 7890.10, 'ACTIVE'),
(7, 'C0007', '赵敏', 33, 'F', '13800000007', '武汉市武昌区', 45.67, 'INACTIVE'),
(8, 'C0008', '孙磊', 27, 'M', '13800000008', '南京市鼓楼区', 234.56, 'ACTIVE'),
(9, 'C0009', '周婷', 45, 'F', '13800000009', '西安市雁塔区', 6789.00, 'ACTIVE'),
(10, 'C0010', '吴斌', 31, 'M', '13800000010', '重庆市渝中区', 890.12, 'ACTIVE');

-- 目标表（演示写入/更新）
DROP TABLE IF EXISTS tgt_customer;
CREATE TABLE tgt_customer (
    cust_no     VARCHAR(20) PRIMARY KEY,
    name        VARCHAR(50),
    age         INT,
    gender      VARCHAR(10),
    balance     DECIMAL(12,2),
    etl_time    TIMESTAMP
);

DROP TABLE IF EXISTS src_order;
CREATE TABLE src_order (
    id          BIGINT PRIMARY KEY,
    order_no    VARCHAR(30),
    cust_no     VARCHAR(20),
    product     VARCHAR(100),
    amount      DECIMAL(12,2),
    order_date  DATE
);

DELETE FROM src_order;
INSERT INTO src_order (id, order_no, cust_no, product, amount, order_date) VALUES
(1, 'ORD20260801001', 'C0001', '门诊挂号', 50.00, DATE '2026-08-01'),
(2, 'ORD20260801002', 'C0001', '血常规检查', 120.00, DATE '2026-08-01'),
(3, 'ORD20260802001', 'C0002', 'CT影像', 680.00, DATE '2026-08-02'),
(4, 'ORD20260802002', 'C0003', '药品-阿莫西林', 45.00, DATE '2026-08-02'),
(5, 'ORD20260803001', 'C0004', '门诊挂号', 50.00, DATE '2026-08-03'),
(6, 'ORD20260803002', 'C0005', '心电图', 180.00, DATE '2026-08-03'),
(7, 'ORD20260804001', 'C0006', '住院押金', 5000.00, DATE '2026-08-04'),
(8, 'ORD20260804002', 'C0007', '药品-头孢', 68.00, DATE '2026-08-04'),
(9, 'ORD20260805001', 'C0008', '门诊挂号', 50.00, DATE '2026-08-05'),
(10, 'ORD20260805002', 'C0009', '超声检查', 260.00, DATE '2026-08-05');
