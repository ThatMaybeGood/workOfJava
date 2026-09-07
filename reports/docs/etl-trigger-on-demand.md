# 报表空结果按需触发 ETL 补数（reports 侧）

> 定位：`reports` 作为**调用方**，当某报表查询结果为空时，尽力触发 ETL 平台（外部 `…/api/etl/run`）去抽数，让后续刷新能读到数据。
> 它**不是** ETL 平台自身（那个见 [etl-module.md](./etl-module.md)），也不做强校验——**任何失败都只记日志，绝不报表查询主流程**。

---

## 一、一句话原理

```
报表查询 mapper 方法
  └─ 返回空(空 List/Map/Optional/数组)？
       ├─ 否 → 正常返回
       └─ 是 → @EtlTask 切面：按 @Param 抽出 vars(可按 dateFormats 转格式)
               → EtlClient.ensure(ds, taskToken, vars)【公共方法，绝不上抛】
                    ├─ 总开关关 / 缺 url/token / 任务被停用 → 短路
                    ├─ 同(任务+参数)在 cooldown 内 → 去重跳过
                    └─ 通过 → 丢独立线程池【异步】POST /api/etl/run 触发
                            → 本次请求原样返回空（非强校验，不阻断）
```

关键红线（都已在代码里保证）：

- **异步**：HTTP 触发在线程池执行，不阻塞报表请求线程；
- **不抛错**：补数整段 try-catch，任何异常只记日志，报表照常返回空；
- **短超时**：到 ETL 的连接 connect 2s / read 5s，卡住不拖垮主流程；
- **去重**：同一 taskToken+参数在 `cooldown-seconds` 内只触发一次；
- **独立线程池**：饱和 = 丢弃 + 告警，绝不反噬主流程。

---

## 二、涉及代码（reports 内新增，不依赖/不侵入现有查询）

| 文件 | 作用 |
|---|---|
| `annotation/EtlTask.java` | 方法级注解：`taskToken()`（ETL 任务 ID）、`params()`（透传 @Param 白名单）、`dateFormats()`（可选，日期转字符串格式，如 `"startDate=yyyy-MM-dd"`） |
| `constant/EtlTaskConst.java` | taskToken 常量集中（每报表/任务一个），注解引用避免魔法字符串 |
| `config/EtlProperties.java` | `reports.etl.*` 配置项 |
| `service/EtlClient.java` | 公共触发客户端（开关/冷却/异步/HTTP/停用名单 都在这里） |
| `aspect/EtlTriggerAspect.java` | 切面：判空 → 抽 vars → 调 `EtlClient.ensure`；`@Order(0)`（在 `DataSourceAspect` 之后） |
| `resources/application.yml` | 新增 `reports.etl` 配置段 |
| `src/test/.../EtlClientTest`、`EtlTriggerAspectTest` | 不依赖数据库的桩验证 |

---

## 三、配置项（`application.yml`）

```yaml
reports:
  etl:
    enabled: true                       # ① 总开关：false = 整个 ETL 触发关闭
    run-url: ${ETL_RUN_URL:http://localhost:18090/api/etl/run}
    api-token: ${ETL_API_TOKEN:}        # 鉴权令牌：用环境变量注入，勿明文入库
    cooldown-seconds: 600               # 同一(任务+参数)两次触发最小间隔，防空表反复刷新
    disabled-task-tokens: []            # ② 单任务停用：填真实 taskToken 即停那一个
```

**三级停用控制：**

| 粒度 | 手段 | 用途 |
|---|---|---|
| 全局 | `reports.etl.enabled: false` | 出问题一键全关 |
| 单任务 | `disabled-task-tokens: ["真实token"]` | 只停某个报表的补数，其余保留（不动代码/注解） |
| 永久弃用某报表 | 删掉该 mapper 方法上的 `@EtlTask` 注解 | 彻底不再要补数 |

---

## 四、怎么给一个报表加上（三步）

**① 在 `EtlTaskConst` 加该任务常量**（值 = ETL 平台「编辑任务 → 开启外部调用」生成的真实 taskToken）：

```java
public static final String OUTP_FINANCE_PIE = "（真实 taskToken）";
```

**② 在该报表"整表业务主查询"的 mapper 方法上加注解**（门诊财务 bt1 示例）：

```java
@EtlTask(
    taskToken = EtlTaskConst.OUTP_FINANCE_PIE,                       // 任务标识
    params = {"startDate", "endDate"},                                // 透传给 ETL vars 的参数
    dateFormats = {"startDate=yyyy-MM-dd", "endDate=yyyy-MM-dd"}     // 可选：Date → 字符串
)
List<Map<String, Object>> queryClinicCountBySource(
        @Param("statisticType") Integer statisticType,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate, ...);
```

- `params` 名 = mapper 方法的 `@Param` 名；**不写 = 全部 @Param 透传**，写了 = 白名单。
- `dateFormats` **可省略**：不写时 Date 原样透传（JSON 为毫秒时间戳）；写 `"参数名=yyyy-MM-dd"` 则转成字符串。非法 pattern 会告警并按原值透传。
- 建议：**只加在主数据查询**（整表业务），不要加在联动下拉/分页等小查询上（那些空是常态）。

**③ 启动时注入 token**：

```bash
export ETL_API_TOKEN=<真实token>     # run-url 若不同环境不同，另用 ETL_RUN_URL 覆盖
```

---

## 五、真实验证步骤与日志关键字

| 步骤 | 操作 | 预期日志 |
|---|---|---|
| ① 拦截生效（地基点） | 查一个确定没数据的日期 | `检测到空结果，尝试触发 ETL 补数: taskToken=…` |
| ② ETL 触发成功 | 同一步 | `ETL 任务触发成功 … logId=… respStatus=RUNNING` |
| ③ 补数成功 | ETL 跑完后再查同日期 | 查询返回数据 |
| ④ 冷却去重 | 空时立刻再查 | `同任务在冷却期内重复触发，已忽略` |
| ⑤ 单任务停用 | `disabled-task-tokens` 加该 token | `已通过 disabled-task-tokens 停用` |

> ⚠️ **①是最关键的地基点**：mapper 是 MyBatis 的 JDK 动态代理，注解放在接口方法上、由 Spring AOP `@annotation` 拦截——普通 Bean 已验证可拦，**mapper 代理场景请在真实环境首个报表确认**。
> 若第①步日志始终不出现，说明 AOP 没拦到 mapper，需要改用 MyBatis 拦截器方案（届时联系后端改造）。

---

## 六、边界与注意事项

1. **非强校验**：空时触发是"尽力而为"，本次请求原样返回空。若某个本来就无数据的日期，只会触发一次并记 SUCCESS（冷却由 `cooldown-seconds` 兜底），不会连环打 ETL。
2. **冷却 key 含参数**：`taskToken + 规范化参数`。同任务不同日期（不同 vars）互不影响、各自可触发；同日期才会被冷却拦截。
3. **vars 与 ETL 任务 vars 要对齐**：名字、格式都按 ETL 平台任务定义来；Date 默认是时间戳，需要 `"yyyy-MM-dd"` 就用 `dateFormats`。
4. **异步写库**：真实触发会调 ETL 抽数并写目标表，验证时请用不影响生产的测试日期。
5. **不影响现有报表**：所有代码是新增；切面只拦标了 `@EtlTask` 的方法，现有 mapper 没标 → 行为零变化。`mvn test` 全量通过。

---

## 七、常见问题排查

| 现象 | 检查 |
|---|---|
| 空结果但不触发 | ① 注解 `taskToken` 是否真实 ② `enabled`/`run-url`/`api-token` 是否配好 ③ 是否命中 `disabled-task-tokens` ④ 冷却期内 |
| 触发了但 ETL 没跑/报参数错 | 看 ETL 侧日志，核对 `vars` 名与格式（`dateFormats`）、taskToken 是否对应正确任务 |
| 日志出现大量告警 | ETL 不可达/非 2xx/业务 FAILED——按告警里 dsKey/taskToken/status 查 ETL 平台 |
| IDE 报 `Could not autowire. No beans of 'EtlClient' type` | 是 IntelliJ 静态误报（新增类未刷新索引），Maven Reload 或 Rebuild 即可；`@SpringBootTest` 冒烟可证装配无误 |
