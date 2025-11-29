@echo off
set REPO_DEST=./offline-repo
set REPO_NAME=offline-repo

echo.
echo ===============================================
echo 1. 清理并准备离线仓库目录: %REPO_DEST%
echo ===============================================
if exist %REPO_DEST% (
    echo 正在删除旧目录...
    rd /s /q %REPO_DEST%
)
mkdir %REPO_DEST%
echo 目录创建完毕。

echo.
echo ===============================================
echo 2. 开始复制项目及其传递性依赖到离线仓库...
echo    (这会包含完整的 GAV 目录结构和 .pom 文件)
echo ===============================================

REM 使用 dependency:go-offline 目标：它会解析所有依赖，并将其下载到本地仓库
REM 但我们需要将它复制到指定的目录，所以使用 dependency:copy

REM 核心命令：将依赖复制到指定目录，并保留仓库布局
mvn dependency:copy-dependencies -DoutputDirectory=%REPO_DEST% -DuseRepositoryLayout=true

if errorlevel 1 (
    echo.
    echo 错误：Maven 依赖复制失败！请检查网络和 Maven 配置。
    goto :end
)

echo.
echo ===============================================
echo 3. 复制完成！
echo    请将 "%REPO_DEST%" 文件夹和项目源代码一起拷贝到离线电脑。
echo ===============================================
echo.
pause
:end