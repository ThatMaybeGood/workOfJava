## 示例 1：带输入参数和输出参数的存储过程
```sql
-- 示例 1：带输入参数和输出参数的存储过程
-- 创建存储过程：根据部门ID获取员工数量
CREATE OR REPLACE PROCEDURE GET_EMPLOYEE_COUNT(
    p_dept_id IN NUMBER,
    p_count OUT NUMBER
) AS
BEGIN
    SELECT COUNT(*) INTO p_count FROM employees WHERE department_id = p_dept_id;
END;
/

-- 创建存储过程：增加员工工资
CREATE OR REPLACE PROCEDURE INCREASE_SALARY(
    p_dept_id IN NUMBER,
    p_percent IN NUMBER,
    p_updated_count OUT NUMBER
) AS
BEGIN
    UPDATE employees 
    SET salary = salary * (1 + p_percent/100) 
    WHERE department_id = p_dept_id;
    
    p_updated_count := SQL%ROWCOUNT;
    COMMIT;
END;
/
```
## 示例 2：返回游标的存储过程
```sql
-- 创建返回游标的存储过程
CREATE OR REPLACE PROCEDURE GET_EMPLOYEES_BY_DEPT(
    p_dept_id IN NUMBER,
    p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_cursor FOR
    SELECT employee_id, employee_name, salary, department_id
    FROM employees 
    WHERE department_id = p_dept_id;
END;
/
```