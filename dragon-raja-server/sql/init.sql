-- ============================================
-- 龙族守夜人讨论区 - 数据库初始化脚本
-- 数据库：dragon_raja_forum
-- 字符集：utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS `dragon_raja_forum`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `dragon_raja_forum`;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
  `username`       VARCHAR(50)  NOT NULL                 COMMENT '用户名（登录账号）',
  `password`       VARCHAR(100) NOT NULL                 COMMENT '密码（BCrypt加密）',
  `nickname`       VARCHAR(50)  DEFAULT NULL             COMMENT '昵称',
  `avatar`         VARCHAR(255) DEFAULT NULL             COMMENT '头像URL',
  `bloodline_grade` CHAR(1)     NOT NULL DEFAULT 'D'     COMMENT '血统等级：D/C/B/A/S',
  `faction`        VARCHAR(20)  DEFAULT NULL             COMMENT '派系：狮心会/学生会/执行部/教授',
  `status`         TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-待审核 1-正常 2-封禁',
  `online_status`  TINYINT      NOT NULL DEFAULT 0       COMMENT '在线状态：0-离线 1-在线',
  `last_active_time` DATETIME   DEFAULT NULL             COMMENT '最后活跃时间（心跳）',
  `role`           VARCHAR(20)  NOT NULL DEFAULT 'USER'  COMMENT '角色：USER-普通用户 ADMIN-管理员',
  `signature`      VARCHAR(200) DEFAULT NULL             COMMENT '个性签名',
  `yanling`        VARCHAR(50)  DEFAULT NULL             COMMENT '言灵',
  `blood_type`     VARCHAR(10)  DEFAULT NULL             COMMENT '血型',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                  COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 帖子表
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '帖子ID',
  `title`        VARCHAR(200)  NOT NULL                 COMMENT '标题',
  `content`      TEXT          NOT NULL                 COMMENT '正文内容',
  `category`     TINYINT       NOT NULL DEFAULT 1       COMMENT '分类：1-闲聊 2-史料 3-委托 4-交换',
  `user_id`      BIGINT        NOT NULL                 COMMENT '发布者用户ID',
  `view_count`   INT           NOT NULL DEFAULT 0       COMMENT '浏览量',
  `comment_count` INT          NOT NULL DEFAULT 0       COMMENT '评论数',
  `is_top`       TINYINT       NOT NULL DEFAULT 0       COMMENT '是否置顶：0-否 1-是',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP                  COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- ----------------------------
-- 3. 评论表
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
  `post_id`      BIGINT        NOT NULL                 COMMENT '所属帖子ID',
  `user_id`      BIGINT        NOT NULL                 COMMENT '评论者用户ID',
  `content`      VARCHAR(1000) NOT NULL                 COMMENT '评论内容',
  `reply_to_id`  BIGINT        DEFAULT NULL             COMMENT '回复的评论ID（一级评论为null）',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reply_to_id` (`reply_to_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ----------------------------
-- 4. 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '公告ID',
  `title`        VARCHAR(200)  NOT NULL                 COMMENT '公告标题',
  `content`      TEXT          NOT NULL                 COMMENT '公告内容',
  `is_top`       TINYINT       NOT NULL DEFAULT 1       COMMENT '是否置顶：0-否 1-是',
  `user_id`      BIGINT        NOT NULL                 COMMENT '发布者用户ID（管理员）',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ----------------------------
-- 默认数据：管理员账号 norma / 123456
-- 密码 123456 的 BCrypt 加密值
-- ----------------------------
INSERT INTO `user` (`username`, `password`, `nickname`, `bloodline_grade`, `faction`, `status`, `online_status`, `role`, `signature`)
VALUES ('norma', '$2b$10$Juf20db.8y6sw.WFwasFpe0Rx/eQV.AyUGWfJcOGkWVHoC8IU0mGm', '守夜人· norma', 'S', '教授', 1, 0, 'ADMIN', '卡塞尔学院守夜人，永恒守望');

-- 默认公告
INSERT INTO `announcement` (`title`, `content`, `is_top`, `user_id`)
VALUES ('欢迎来到守夜人讨论区', '各位学员，欢迎来到卡塞尔学院守夜人讨论区。请遵守学院规章，文明讨论。血统等级达到B级以上方可接取委托任务。', 1, 1);

-- ============================================
-- V2.0 新增表：龙族粉丝社区
-- ============================================

-- ----------------------------
-- 5. 新闻表
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '新闻ID',
  `title`        VARCHAR(200)  NOT NULL                 COMMENT '新闻标题',
  `content`      TEXT                                   COMMENT '新闻内容',
  `cover`        VARCHAR(255)  DEFAULT NULL             COMMENT '封面图片URL',
  `summary`      VARCHAR(300)  DEFAULT NULL             COMMENT '摘要',
  `author`       VARCHAR(50)   DEFAULT '诺玛'           COMMENT '作者',
  `is_top`       TINYINT       NOT NULL DEFAULT 0       COMMENT '是否置顶：0-否 1-是',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻表';

-- ----------------------------
-- 6. 好友关系表
-- ----------------------------
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '关系ID',
  `user_id`      BIGINT        NOT NULL                 COMMENT '用户ID',
  `friend_id`    BIGINT        NOT NULL                 COMMENT '好友ID',
  `status`       TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0-待接受 1-已接受 2-已拒绝',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- ----------------------------
-- 7. 消息表（诺玛分发）
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  `sender_id`    BIGINT        DEFAULT NULL             COMMENT '发送者用户ID',
  `receiver_id`  BIGINT        NOT NULL                 COMMENT '接收者用户ID',
  `title`        VARCHAR(200)  NOT NULL                 COMMENT '消息标题',
  `content`      TEXT                                   COMMENT '消息内容',
  `sender`       VARCHAR(50)   DEFAULT '诺玛'           COMMENT '发送者',
  `type`         VARCHAR(20)   DEFAULT 'SYSTEM'         COMMENT '消息类型：SYSTEM-系统消息 REPLY-回复通知 PRIVATE-私信',
  `post_id`      BIGINT        DEFAULT NULL             COMMENT '关联帖子ID',
  `is_read`      TINYINT       NOT NULL DEFAULT 0       COMMENT '是否已读：0-未读 1-已读',
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ----------------------------
-- 默认新闻数据（龙族世界观）
-- ----------------------------
INSERT INTO `news` (`title`, `content`, `summary`, `author`, `is_top`) VALUES
('卡塞尔学院2026年度新生入学典礼即将举行',
 '亲爱的各位学员，卡塞尔学院2026年度新生入学典礼将于下周一上午9时在学院大礼堂准时举行。届时校长希尔伯特·让·昂热将发表致辞，各院系导师将介绍学院的学术传统与研究领域。\n\n入学典礼后将进行新生血统等级测评，请各位新生提前半小时到达礼堂签到。典礼结束后，各社团将在广场设立招新展台，狮心会、学生会、执行部均欢迎新成员的加入。\n\n——卡塞尔学院教务处',
 '新生入学典礼将于下周一举行，昂热校长致辞，血统等级测评同步进行。', '卡塞尔学院教务处', 1),

('秘党最新研究：龙族血统与元素共鸣的深层关联',
 '秘党研究院今日发布了一份关于龙族血统与元素共鸣关联性的最新研究报告。报告指出，高等级血统（A级及以上）的混血种在与特定元素接触时，表现出显著高于普通混血种的共鸣频率。\n\n研究团队通过对57名志愿者的血统样本分析发现，S级血统拥有者对火元素的共鸣强度是D级血统的约12倍。这一发现可能为开发新型言灵之术提供理论基础。\n\n报告全文已在秘党内刊发布，感兴趣的学员可前往图书馆查阅。',
 '秘党发布血统研究新成果，S级血统火元素共鸣强度为D级的12倍。', '秘党研究院', 0),

('狮心会与学生会联合声明：年度学院竞赛即将开幕',
 '卡塞尔学院两大核心社团——狮心会与学生会今日联合宣布，2026年度学院竞赛将于两周后正式开幕。本次竞赛将包含言灵对战、学术辩论、野外生存等多个项目。\n\n狮心会会长与学生会主席共同表示，本届竞赛旨在促进各社团交流，发掘优秀人才，同时为即将到来的毕业季预热。\n\n报名截止日期为本月25日，参赛者可通过各自社团提交报名申请。优胜者将获得由学院颁发的荣誉勋章及特殊权限。',
 '年度学院竞赛两周后开幕，包含言灵对战、学术辩论等项目。', '狮心会·学生会', 1),

('执行部报告：北欧地区发现疑似龙类遗迹',
 '卡塞尔学院执行部今日提交了一份关于北欧地区最新发现的勘察报告。报告显示，在挪威北部山区发现了一处疑似古龙类活动的遗迹，现场检测到微弱的元素波动。\n\n执行部已派遣先遣队前往现场进行进一步勘察，预计两周内返回初步评估结果。校方提醒各位学员，目前该区域已被划为临时禁区，未经执行部许可不得擅自前往。\n\n这处遗迹的年代初步判定为公元9世纪左右，与北欧传说中的龙类活动高峰期吻合。如有进一步消息，执行部将及时通报。',
 '挪威北部发现疑似古龙遗迹，元素波动已被检测到，执行部已派遣先遣队。', '执行部', 0),

('诺玛系统全面升级：新增邮件通讯与联系人功能',
 '尊敬的各位学员，卡塞尔学院中央控制系统「诺玛」已完成全面升级。本次升级新增以下功能：\n\n📰 新闻板块：实时获取学院官方发布的新闻与公告，了解龙族世界最新动态。\n✉️ 邮件系统：诺玛将统一分发重要通知与个人消息至您的专属邮箱，确保不错过任何关键信息。\n👥 联系人：支持通过学员ID搜索并添加好友，与同学保持联系，查看好友在线状态。\n\n请登录您的账号体验全新功能。如有任何问题或建议，请联系系统管理员。\n\n——诺玛',
 '诺玛系统全面升级！新增新闻、邮件、联系人三大板块，提升社区体验。', '诺玛', 1);

-- ============================================
-- V3.0 新增表：血统测试、图片关联
-- ============================================

-- ----------------------------
-- 8. 3E血统测试题库表
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '题目ID',
  `category`     VARCHAR(20)   NOT NULL                 COMMENT '分类：密党常识/困境选择/性格倾向',
  `question`     TEXT          NOT NULL                 COMMENT '题目内容',
  `option_a`     VARCHAR(300)  NOT NULL                 COMMENT '选项A',
  `option_b`     VARCHAR(300)  NOT NULL                 COMMENT '选项B',
  `option_c`     VARCHAR(300)  NOT NULL                 COMMENT '选项C',
  `option_d`     VARCHAR(300)  NOT NULL                 COMMENT '选项D',
  `answer`       CHAR(1)       NOT NULL                 COMMENT '正确答案：A/B/C/D',
  `created_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3E血统测试题库';

-- ----------------------------
-- 9. 帖子图片表
-- ----------------------------
DROP TABLE IF EXISTS `post_image`;
CREATE TABLE `post_image` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
  `post_id`      BIGINT        NOT NULL                 COMMENT '帖子ID',
  `url`          VARCHAR(500)  NOT NULL                 COMMENT '图片URL',
  `sort_order`   INT           DEFAULT 0                COMMENT '排序序号',
  `created_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子图片表';

-- ----------------------------
-- 10. 消息图片表
-- ----------------------------
DROP TABLE IF EXISTS `message_image`;
CREATE TABLE `message_image` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '图片ID',
  `message_id`   BIGINT        NOT NULL                 COMMENT '消息ID',
  `url`          VARCHAR(500)  NOT NULL                 COMMENT '图片URL',
  `sort_order`   INT           DEFAULT 0                COMMENT '排序序号',
  `created_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息图片表';

-- ----------------------------
-- 11. 帖子浏览记录表
-- ----------------------------
DROP TABLE IF EXISTS `post_view`;
CREATE TABLE `post_view` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
  `user_id`      BIGINT        NOT NULL                 COMMENT '用户ID',
  `post_id`      BIGINT        NOT NULL                 COMMENT '帖子ID',
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子浏览记录表';
