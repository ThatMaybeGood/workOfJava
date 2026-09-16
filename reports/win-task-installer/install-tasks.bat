@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

rem ============================================================
rem  通用计划任务安装脚本
rem  读取 tasks.txt 配置表, 逐行创建 Windows 计划任务
rem  重复执行会自动覆盖同名任务, 不会产生重复
rem  用法: 双击运行, 或 install-tasks.bat [配置文件路径]
rem ============================================================

set "CONFIG=%~dp0tasks.txt"
if not "%~1"=="" set "CONFIG=%~1"

if not exist "%CONFIG%" (
    echo [错误] 找不到配置文件: %CONFIG%
    echo.
    pause
    exit /b 1
)

set /a OK=0
set /a FAIL=0

echo ==========================================
echo   计划任务批量安装
echo   配置文件: %CONFIG%
echo ==========================================
echo.

for /f "usebackq eol=# tokens=1,2,3,4,5 delims=|" %%a in ("%CONFIG%") do (
    set "SCRIPT=%%a"
    set "ARGS=%%b"
    set "FREQ=%%c"
    set "WHEN=%%d"
    set "RUNAS=%%e"
    if not "!SCRIPT!"=="" call :CREATE
)

echo.
echo ==========================================
echo   完成: 成功 %OK% 个, 失败 %FAIL% 个
echo ==========================================
echo.
pause
exit /b 0


rem ---------- 子程序: 创建单个任务 ----------
:CREATE
for /f "tokens=* delims= " %%x in ("!SCRIPT!") do set "SCRIPT=%%x"
for /f "tokens=* delims= " %%x in ("!ARGS!") do set "ARGS=%%x"
for /f "tokens=* delims= " %%x in ("!FREQ!") do set "FREQ=%%x"
for /f "tokens=* delims= " %%x in ("!WHEN!") do set "WHEN=%%x"
for /f "tokens=* delims= " %%x in ("!RUNAS!") do set "RUNAS=%%x"

rem every/daily/weekly 必须带第4列, 否则报错
if /i "!FREQ!"=="hourly" if "!WHEN!"=="" set "WHEN=1"
echo ,every,daily,weekly, | findstr /i ",!FREQ!," >nul && if "!WHEN!"=="" goto BADWHEN

rem 任务名: auto_ + 脚本名(不含扩展名)
for %%x in ("!SCRIPT!") do set "NAME=auto_%%~nx"

rem 相对路径转成以本脚本目录为基准
set "FULLPATH=!SCRIPT!"
echo !FULLPATH! | findstr /i "^[a-z]:\\ ^\\\\" >nul
if errorlevel 1 set "FULLPATH=%~dp0!FULLPATH!"

if not exist "!FULLPATH!" (
    echo [失败] !NAME! : 找不到脚本 !FULLPATH!
    set /a FAIL+=1
    goto :eof
)

rem 组装备命令行
set "TR=cmd /c ""!FULLPATH!"""
if not "!ARGS!"=="" set "TR=!TR! !ARGS!"

rem 运行身份
set "RUARGS="
if /i "!RUNAS!"=="SYSTEM" set "RUARGS=/ru SYSTEM /rl HIGHEST"

rem 按频率组装计划参数
set "SCHEDULE="
if /i "!FREQ!"=="every"   set "SCHEDULE=/sc minute /mo !WHEN!"
if /i "!FREQ!"=="daily"   set "SCHEDULE=/sc daily /st !WHEN!"
if /i "!FREQ!"=="hourly"  set "SCHEDULE=/sc hourly /mo !WHEN!"
if /i "!FREQ!"=="weekly"  set "SCHEDULE=/sc weekly /d !WHEN!"
if /i "!FREQ!"=="startup" set "SCHEDULE=/sc onstart"
if /i "!FREQ!"=="logon"   set "SCHEDULE=/sc onlogon"

if "!SCHEDULE!"=="" (
    echo [失败] !NAME! : 未知频率 [!FREQ!]
    set /a FAIL+=1
    goto :eof
)

schtasks /create /tn "!NAME!" /tr "!TR!" !SCHEDULE! !RUARGS! /f >nul 2>&1
if errorlevel 1 (
    echo [失败] !NAME!  ^(频率: !FREQ! !WHEN!^)
    set /a FAIL+=1
) else (
    echo [成功] !NAME!  =^> !FREQ! !WHEN!   命令: !FULLPATH! !ARGS!
    set /a OK+=1
)
goto :eof

:BADWHEN
echo [失败] !SCRIPT! : 缺少时间/间隔参数
set /a FAIL+=1
goto :eof
