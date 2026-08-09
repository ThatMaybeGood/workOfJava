# ETL NEXUS 部署指南

ETL 数据流转平台部署文档。覆盖 **服务器 jar 部署**、**Windows 安装版(exe)**、**可视化系统设置**。

---

## 一、部署形态概览

| 形态 | 适用场景 | 依赖 | 复杂度 |
|---|---|---|---|
| **fat jar** | 服务器 / 内网机 | 仅需 JDK 17 | 低 |
| **Windows exe 安装版** | 交付给非技术用户 | 无需 Java(自带 JRE) | 中 |
| **外部 Tomcat(war)** | 已有 Tomcat 的环境 | Tomcat 9+ | 中 |

> **重要**:部署端**不需要 npm / Node.js**——前端已构建成静态文件打进 jar/war,无需在目标机装任何前端工具链。

---

## 二、构建产物

```bash
# 在开发机执行(已有源码 + JDK17 + Maven + Node)
cd etl-platform/frontend && npm run build    # ① 先构建前端(输出到 src/main/resources/static/)
cd .. && mvn package -DskipTests            # ② 再打 jar(把前端打进 jar)
# 产物: target/etl-platform-1.0.0-SNAPSHOT.jar
```

> ⚠️ **顺序必须**:先 `npm run build` 再 `mvn package`。反之 jar 里会是旧前端。

---

## 三、方式一:服务器 fat jar 部署(推荐)

### 1. 上传 jar 到服务器

```bash
scp target/etl-platform-1.0.0-SNAPSHOT.jar user@server:/opt/etl/
```

### 2. 启动

```bash
cd /opt/etl
java -jar etl-platform-1.0.0-SNAPSHOT.jar
```

默认监听 **18880 端口**,访问 `http://服务器IP:18880`。

- 登录账号:admin / admin123(其他:operator/operator123,viewer/viewer123)
- **元数据库默认是 H2 文件库**,数据持久化在 `./data/` 目录,重启不丢失

### 3. 常用启动参数(可选)

| 参数 | 作用 |
|---|---|
| `--server.port=8080` | 临时指定端口(覆盖默认) |
| `--spring.profiles.active=prod` | 切生产 profile(外部 Oracle 元库,需配环境变量) |
| `-Xms512m -Xmx1g` | JVM 内存限制 |

### 4. 生产环境用外部 Oracle 元数据库

```bash
export DB_URL='jdbc:oracle:thin:@host:1521:ORCL'
export DB_USERNAME='etl_user'
export DB_PASSWORD='your_password'
java -jar etl-platform.jar --spring.profiles.active=prod
```

> 不设环境变量时默认走 H2 文件库,开箱即用。

---

## 四、方式二:Windows 安装版(exe / msi)

### 1. 用 JDK 自带 jpackage 打包(开发机执行)

```bash
# 前提:已构建好 jar
jpackage --input target --name "ETL Nexus" \
  --main-jar etl-platform-1.0.0-SNAPSHOT.jar \
  --type msi --win-shortcut --win-menu \
  --icon path/to/icon.ico
```

- `--type msi` 产出安装程序(.msi);也可用 `--type exe`
- 产物在 `dist/` 目录,双击即安装,自带 JRE,**目标机无需装 Java**
- 安装后开始菜单/桌面出现 "ETL Nexus" 快捷方式,双击启动

### 2. 启动后行为

- 程序运行目录会生成 `./data/`(H2 文件库)和 `./config/`(运行时配置)
- 首次启动后打开浏览器访问,端口默认 18880
- 改端口/库/日志:登录后在左下角 **系统设置** 页修改 → 重启生效

---

## 五、方式三:外部 Tomcat(war)

```bash
# 1. pom.xml 改 packaging 为 war
<packaging>war</packaging>

# 2. 主类继承 SpringBootServletInitializer
#    (com.etl.EtlApplication 加 extends + configure 重写,约5行)

# 3. 打包
mvn package -DskipTests
# 产物: target/etl-platform-1.0.0-SNAPSHOT.war

# 4. 部署
cp etl-platform.war /opt/tomcat/webapps/
```

---

## 六、可视化系统设置(端口/数据库/日志)

> 这是面向部署的**免命令行配置**能力:不用改 yml,界面上直接改。

1. 用 **admin** 账号登录(operator/viewer 看不到该菜单)
2. 左侧菜单底部点 **⚙ 系统设置**
3. 可配置项:

| 配置项 | 说明 |
|---|---|
| **监听端口** | 默认 18880,改成任意 1-65535 |
| **元数据库** | `内置 H2 文件库`(推荐,持久化到 `./data/`)或 `外部数据库`(填 JDBC URL/用户/密码) |
| **日志文件路径** | 默认 `logs/etl-platform-prod.log` |

4. 点 **💾 保存设置** → 提示"重启服务后生效"
5. **重启服务**:
   - jar 方式:停进程后重新 `java -jar etl-platform.jar`
   - exe 方式:关闭窗口重新打开
6. 配置保存在程序目录 `./config/application.yml`,可手动备份/迁移

---

## 七、目录结构(运行后)

```
etl-platform/            # jar 所在目录
├── etl-platform.jar     # 程序
├── data/                # H2 文件库(元数据,含管线/数据源/映射等配置)
├── config/
│   └── application.yml  # 运行时配置(界面保存生成,重启生效)
└── logs/                # 日志
```

> **迁移/备份**:拷走 `data/` + `config/` 即可完整迁移到新机器,管线/映射/数据源配置全保留。

---

## 八、常见问题

| 问题 | 处理 |
|---|---|
| 端口被占用 | 系统设置改端口,或启动加 `--server.port=其它` |
| 重启后数据没了 | 确认是 H2 **文件库**(`jdbc:h2:file:`),不是内存库;检查 `data/` 目录 |
| 改完端口不生效 | 端口需重启才生效;确认 `./config/application.yml` 已生成 |
| 想连真实 Oracle | 系统设置选"外部数据库",或 prod profile + 环境变量 |
| 忘记密码 | 登录校验在前端,重置需看 `AuthContext.jsx` 的 VALID_USERS |

---

## 九、从开发到上线速查

```
开发机:
  npm run build  →  mvn package  →  得到 jar

服务器/Windows:
  java -jar etl-platform.jar            # 或 双击 exe
  admin/admin123 登录
  系统设置 → 改端口/数据库 → 重启
  数据源菜单 → 建抽取源 + 目的库
  管线编排 → 建管线(抽取→字段映射→加载)
  字段映射 → 选管线步骤 → 可视化点选生成映射
```
