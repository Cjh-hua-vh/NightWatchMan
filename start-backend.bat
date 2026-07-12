@echo off
chcp 65001 >nul
cd /d "D:\DaiMa\NightWatchMan\dragon-raja-server"
echo ==============================
echo   卡塞尔之门 - 后端启动
echo ==============================
echo.
echo 启动中... 关闭此窗口即可停止
echo.
mvn spring-boot:run
pause
