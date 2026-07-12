@echo off
chcp 65001 >nul
title 卡塞尔之门 - 前端 Vite

cd /d "D:\DaiMa\NightWatchMan\dragon-raja-frontend"

echo.
echo   ╔══════════════════════════════════╗
echo   ║   卡塞尔之门 - 前端启动         ║
echo   ║   http://localhost:5175         ║
echo   ╚══════════════════════════════════╝
echo.
echo   关闭此窗口即可停止前端服务
echo   ────────────────────────────────
echo.

REM 调用标准 vite 启动器
call node_modules\.bin\vite.cmd --port 5175 --host 0.0.0.0

pause
