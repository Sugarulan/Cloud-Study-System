-- =====================================================
-- GAC-LMS 数据库 Schema (W1 初版)
-- 仅包含方雨菲负责的 5 个业务模块核心表 + 公共字段
-- 完整 DDL 见 docs/sql/full-schema.sql（持续完善中）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 通用：所有业务表统一字段
--   id          bigint       主键（雪花算法）
--   create_by   bigint       创建人
--   create_time datetime     创建���间
--   update_by   bigint       更新人
--   update_time datetime     更新时间
--   deleted     tinyint(1)   逻辑删除（0=未删，1=已删）
--   version     int          乐观锁版本号
-- -----------------------------------------------------

-- -----------------------------------------------------
-- 3.3.7 成绩管理：成绩主表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `grade_record`;
CREATE TABLE `grade_record` (
  `id`              bigint       NOT NULL COMMENT '主键',
  `exam_id`         bigint       NOT NULL COMMENT '考试 ID',
  `user_id`         bigint       NOT NULL COMMENT '学员 ID',
  `paper_id`        bigint       NOT NULL COMMENT '试卷 ID',
  `total_score`     decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
  `objective_score` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '客观题得分',
  `subjective_score`decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '主观题得分',
  `pass_score`      decimal(6,2) NOT NULL DEFAULT 60.00 COMMENT '通过分',
  `is_passed`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否通过',
  `status`          tinyint      NOT NULL DEFAULT 0 COMMENT '0=待发布 1=已发布',
  `submitted_at`    datetime     DEFAULT NULL COMMENT '交卷时间',
  `published_at`    datetime     DEFAULT NULL COMMENT '发布时间',
  `create_by`       bigint       DEFAULT NULL,
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`       bigint       DEFAULT NULL,
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         tinyint(1)   NOT NULL DEFAULT 0,
  `version`         int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_exam_user` (`exam_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩主表';

-- -----------------------------------------------------
-- 3.3.7 成绩管理：成绩明细（每题得分）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `grade_detail`;
CREATE TABLE `grade_detail` (
  `id`            bigint       NOT NULL,
  `grade_id`      bigint       NOT NULL COMMENT '成绩 ID',
  `question_id`   bigint       NOT NULL COMMENT '题目 ID',
  `user_answer`   text         COMMENT '学员答案（JSON）',
  `correct_answer`text         COMMENT '正确答案（JSON）',
  `is_correct`    tinyint(1)   NOT NULL DEFAULT 0,
  `score`         decimal(6,2) NOT NULL DEFAULT 0.00,
  `full_score`    decimal(6,2) NOT NULL DEFAULT 0.00,
  `evaluator_type`tinyint      NOT NULL DEFAULT 0 COMMENT '0=自动 1=AI 2=人工',
  `evaluator_id`  bigint       DEFAULT NULL COMMENT '人工评卷人',
  `comment`       text         COMMENT '评语',
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `version`       int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_grade` (`grade_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩明细';

-- -----------------------------------------------------
-- 3.3.8 在线作答：考试作答记录（持久化层，Redis 缓存答题快照）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `exam_taking`;
CREATE TABLE `exam_taking` (
  `id`            bigint       NOT NULL,
  `exam_id`       bigint       NOT NULL,
  `user_id`       bigint       NOT NULL,
  `paper_id`      bigint       NOT NULL,
  `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '0=进行中 1=已交卷 2=已评分 3=已发布',
  `start_time`    datetime     DEFAULT NULL,
  `submit_time`   datetime     DEFAULT NULL,
  `duration_sec`  int          NOT NULL DEFAULT 0 COMMENT '实际用时',
  `snapshot_version` int       NOT NULL DEFAULT 0 COMMENT '快照版本号（乐观锁）',
  `answers_json`  longtext     COMMENT '答案 JSON（最终落库）',
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `version`       int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_user` (`exam_id`, `user_id`, `deleted`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试作答记录';

-- -----------------------------------------------------
-- 3.3.9 个人测评：错题本
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
  `id`            bigint       NOT NULL,
  `user_id`       bigint       NOT NULL COMMENT '学员 ID',
  `grade_id`      bigint       NOT NULL,
  `question_id`   bigint       NOT NULL,
  `user_answer`   text,
  `correct_answer`text,
  `is_mastered`   tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否已掌握（学员标记）',
  `ai_explanation`longtext     COMMENT 'AI 解析（W4 接入）',
  `ai_explained_at` datetime   DEFAULT NULL,
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `version`       int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_grade_question` (`user_id`, `grade_id`, `question_id`, `deleted`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

-- -----------------------------------------------------
-- 3.3.10 知识管理：知识库文档
-- -----------------------------------------------------
DROP TABLE IF EXISTS `knowledge_doc`;
CREATE TABLE `knowledge_doc` (
  `id`            bigint       NOT NULL,
  `category_id`   bigint       DEFAULT NULL COMMENT '分类 ID',
  `title`         varchar(256) NOT NULL,
  `summary`       varchar(512) DEFAULT NULL,
  `content`       longtext     COMMENT '富文本内容',
  `tags`          varchar(512) DEFAULT NULL COMMENT '标签，逗号分隔',
  `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '0=DRAFT 1=PENDING 2=PUBLISHED 3=ARCHIVED 4=REJECTED',
  `version`       int          NOT NULL DEFAULT 1 COMMENT '文档版本号',
  `author_id`     bigint       DEFAULT NULL,
  `reviewer_id`   bigint       DEFAULT NULL COMMENT '审核人',
  `published_at`  datetime     DEFAULT NULL,
  `archived_at`   datetime     DEFAULT NULL,
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `row_version`   int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

-- -----------------------------------------------------
-- 3.3.10 知识管理：分类目录
-- -----------------------------------------------------
DROP TABLE IF EXISTS `knowledge_category`;
CREATE TABLE `knowledge_category` (
  `id`            bigint       NOT NULL,
  `parent_id`     bigint       NOT NULL DEFAULT 0,
  `name`          varchar(128) NOT NULL,
  `sort`          int          NOT NULL DEFAULT 0,
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类';

-- -----------------------------------------------------
-- 3.3.10 知识管理：文档版本（历史快照）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `knowledge_doc_version`;
CREATE TABLE `knowledge_doc_version` (
  `id`            bigint       NOT NULL,
  `doc_id`        bigint       NOT NULL,
  `version`       int          NOT NULL,
  `title`         varchar(256) NOT NULL,
  `content`       longtext,
  `change_log`    varchar(512) DEFAULT NULL,
  `operator_id`   bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_version` (`doc_id`, `version`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档历史版本';

-- -----------------------------------------------------
-- 通用：站内消息
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id`            bigint       NOT NULL,
  `user_id`       bigint       NOT NULL COMMENT '接收人',
  `type`          varchar(32)  NOT NULL COMMENT 'EXAM_REMIND / GRADE_PUBLISH / KNOWLEDGE_PUBLISH',
  `title`         varchar(256) NOT NULL,
  `content`       text,
  `is_read`       tinyint(1)   NOT NULL DEFAULT 0,
  `read_time`     datetime     DEFAULT NULL,
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

SET FOREIGN_KEY_CHECKS = 1;
