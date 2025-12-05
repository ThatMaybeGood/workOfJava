@echo off
setlocal enabledelayedexpansion

set PROJECT_NAME=my-project
set OFFLINE_PACKAGE=offline-package-%PROJECT_NAME%-%DATE:/=-%_%TIME::=-%
set OFFLINE_PACKAGE=!OFFLINE_PACKAGE: =!

echo.
echo ===============================================
echo 创建完整离线包: !OFFLINE_PACKAGE!
echo ===============================================

REM 1. 创建目录结构
mkdir !OFFLINE_PACKAGE!
mkdir !OFFLINE_PACKAGE!\repository
mkdir !OFFLINE_PACKAGE!\project

echo.
echo 步骤1: 下载所有依赖和插件...
echo ===============================================

REM 2. 下载所有依赖到临时仓库
set TEMP_REPO=temp-repo
if exist !TEMP_REPO! rd /s /q !TEMP_REPO!
mkdir !TEMP_REPO!

REM 关键：使用 go-offline 下载所有东西
mvn clean dependency:go-offline -Dmaven.repo.local=!TEMP_REPO! -DskipTests

if errorlevel 1 (
    echo [警告] 部分插件可能下载失败，继续...
)

echo.
echo 步骤2: 复制仓库...
echo ===============================================

REM 复制整个仓库
xcopy "!TEMP_REPO!\*" "!OFFLINE_PACKAGE!\repository\" /E /I /Y /Q

echo.
echo 步骤3: 打包项目文件...
echo ===============================================

REM 复制项目文件（排除 target 等）
xcopy ".\pom.xml" "!OFFLINE_PACKAGE!\project\" /Y
xcopy ".\src" "!OFFLINE_PACKAGE!\project\src\" /E /I /Y
if exist ".\*.md" xcopy ".\*.md" "!OFFLINE_PACKAGE!\project\" /Y
if exist "\.mvn" xcopy "\.mvn" "!OFFLINE_PACKAGE!\project\.mvn\" /E /I /Y

REM 创建使用说明
echo # 离线构建说明 > "!OFFLINE_PACKAGE!\README.txt"
echo. >> "!OFFLINE_PACKAGE!\README.txt"
echo ## 方法1：命令行构建 >> "!OFFLINE_PACKAGE!\README.txt"
echo 1. 复制 project 文件夹到工作目录 >> "!OFFLINE_PACKAGE!\README.txt"
echo 2. 执行以下命令： >> "!OFFLINE_PACKAGE!\README.txt"
echo    set MAVEN_OPTS=-Dmaven.repo.local=../repository >> "!OFFLINE_PACKAGE!\README.txt"
echo    mvn clean install >> "!OFFLINE_PACKAGE!\README.txt"
echo. >> "!OFFLINE_PACKAGE!\README.txt"
echo ## 方法2：IDEA 设置 >> "!OFFLINE_PACKAGE!\README.txt"
echo 1. File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven >> "!OFFLINE_PACKAGE!\README.txt"
echo 2. 修改 "Local repository" 为 repository 文件夹的绝对路径 >> "!OFFLINE_PACKAGE!\README.txt"
echo 3. 勾选 "Work offline" >> "!OFFLINE_PACKAGE!\README.txt"

echo.
echo 步骤4: 创建快速启动脚本...
echo ===============================================

REM 创建批处理文件
echo @echo off > "!OFFLINE_PACKAGE!\build.bat"
echo set REPO_PATH=%%~dp0repository >> "!OFFLINE_PACKAGE!\build.bat"
echo echo 使用离线仓库构建... >> "!OFFLINE_PACKAGE!\build.bat"
echo mvn clean install -Dmaven.repo.local="%%REPO_PATH%%" %%* >> "!OFFLINE_PACKAGE!\build.bat"
echo pause >> "!OFFLINE_PACKAGE!\build.bat"

REM 创建 IDEA 配置文件
echo <?xml version="1.0" encoding="UTF-8"?> > "!OFFLINE_PACKAGE!\idea-settings.xml"
echo <project version="4"> >> "!OFFLINE_PACKAGE!\idea-settings.xml"
echo   <component name="MavenSettings"> >> "!OFFLINE_PACKAGE!\idea-settings.xml"
echo     <option name="localRepository" value="!CD!\repository" /> >> "!OFFLINE_PACKAGE!\idea-settings.xml"
echo     <option name="offlineMode" value="true" /> >> "!OFFLINE_PACKAGE!\idea-settings.xml"
echo   </component> >> "!OFFLINE_PACKAGE!\idea-settings.xml"
echo </project> >> "!OFFLINE_PACKAGE!\idea-settings.xml"

echo.
echo ===============================================
echo 离线包创建完成！
echo 目录: !OFFLINE_PACKAGE!
echo.
echo 【在离线电脑的操作步骤】：
echo 1. 将整个 !OFFLINE_PACKAGE! 文件夹复制到离线电脑
echo 2. 进入 project 目录
echo 3. 运行 ..\build.bat 或按照 README.txt 配置 IDEA
echo ===============================================

REM 压缩（可选）
echo.
echo 是否压缩为ZIP？(y/n)
set /p compress=
if /i "!compress!"=="y" (
    powershell Compress-Archive -Path "!OFFLINE_PACKAGE!\*" -DestinationPath "!OFFLINE_PACKAGE!.zip"
    echo 已创建: !OFFLINE_PACKAGE!.zip
)

endlocal
pause