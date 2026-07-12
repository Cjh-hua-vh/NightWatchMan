# 卡塞尔之门（The Gate to Cassell）

龙族守夜人讨论区 —— 卡塞尔学院在线社区论坛。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vite + Element Plus |
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |

## 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **Node.js 18+** / npm
- **MySQL 8.0**（已安装并运行，root 密码推荐设为 `root`，或自行修改 `application.yml`）

## 本地启动流程

### 1. 导入数据库

```bash
mysql -uroot -proot < dragon_raja_forum_backup.sql
```

这会创建 `dragon_raja_forum` 库并导入所有表结构和数据。

管理员账号：**norma** / **123456**

### 2. 启动后端（端口 8082）

```bash
cd dragon-raja-server
mvn spring-boot:run
```

首次运行会自动下载 Maven 依赖。

### 3. 启动前端（端口 5175）

```bash
cd dragon-raja-frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5175`

## 项目结构

```
NightWatchMan/
├── dragon-raja-server/          # Spring Boot 后端
│   ├── src/
│   ├── sql/init.sql             # 完整建表脚本（11 张表）
│   └── pom.xml
├── dragon-raja-frontend/        # Vue 3 前端
│   ├── src/
│   ├── public/                  # 静态资源（背景图、Logo）
│   └── package.json
├── dragon_raja_forum_backup.sql # 数据库完整备份
└── README.md
```

## 自定义配置

### 修改 MySQL 连接

编辑 `dragon-raja-server/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dragon_raja_forum?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 图片上传路径

默认为项目内的 `./uploads` 目录，也可改为绝对路径：

```yaml
file:
  upload-dir: /你的/自定义/路径/uploads
```

## 功能概览

- 用户注册/登录（BCrypt 加密）
- 帖子发布（支持文字 + 多图）
- 评论/回复（二级评论）
- 私信（实时聊天）
- 邮件系统（诺玛分发）
- 新闻公告
- 好友/联系人
- 3E 血统测试
- 管理后台（用户管理、帖子管理）
- 消息通知（铃铛）
