-- =====================================================
-- GAC-LMS 数据库 Schema (W2 完整版)
-- 覆盖全部业务模块的 21 张表 + 公共字段
-- 模块归属详见 docs/sql/full-schema.sql（设计文档）
--
-- 模块负责人：
--   王茗瑾：账号 / 人员 / 题库 / 试卷 / 考试      （本文件 1~5 段）
--   方雨菲：作答 / 评卷 / 知识库 / 消息 / AI 日志  （本文件 6~9 段）
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 通用：所有业务表统一字段
--   id          bigint       主键（雪花算法）
--   create_by   bigint       创建人
--   create_time datetime     创建时间
--   update_by   bigint       更新人
--   update_time datetime     更新时间
--   deleted     tinyint(1)   逻辑删除（0=未删，1=已删）
--   version     int          乐观锁版本号
-- -----------------------------------------------------

-- =====================================================
-- 一、账号模块（王茗瑾）
-- =====================================================

-- -----------------------------------------------------
-- 3.3.1 账号：登录态、密码、状态
-- -----------------------------------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id`            bigint       NOT NULL COMMENT '主键',
  `username`      varchar(64)  NOT NULL COMMENT '登录名',
  `password_hash` varchar(100) NOT NULL COMMENT 'BCrypt 密码哈希',
  `email`         varchar(128) DEFAULT NULL,
  `phone`         varchar(20)  DEFAULT NULL,
  `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `last_login_at` datetime     DEFAULT NULL COMMENT '最近登录时间',
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `version`       int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `deleted`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号';

-- -----------------------------------------------------
-- 3.3.1 角色：ADMIN / TEACHER / STUDENT
-- -----------------------------------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id`          bigint       NOT NULL,
  `code`        varchar(32)  NOT NULL COMMENT '角色编码：ADMIN / TEACHER / STUDENT',
  `name`        varchar(64)  NOT NULL,
  `description` varchar(256) DEFAULT NULL,
  `sort`        int          NOT NULL DEFAULT 0,
  `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     tinyint(1)   NOT NULL DEFAULT 0,
  `version`     int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- -----------------------------------------------------
-- 3.3.1 权限：菜单（type=1）+ 按钮（type=2）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id`          bigint       NOT NULL,
  `code`        varchar(64)  NOT NULL COMMENT '权限编码：account / account:list 等',
  `name`        varchar(128) NOT NULL,
  `type`        tinyint      NOT NULL DEFAULT 2 COMMENT '1=菜单 2=按钮',
  `parent_id`   bigint       NOT NULL DEFAULT 0,
  `sort`        int          NOT NULL DEFAULT 0,
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     tinyint(1)   NOT NULL DEFAULT 0,
  `version`     int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`, `deleted`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限';

-- -----------------------------------------------------
-- 3.3.1 账号-角色（多对多）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `account_role`;
CREATE TABLE `account_role` (
  `id`         bigint   NOT NULL,
  `account_id` bigint   NOT NULL,
  `role_id`    bigint   NOT NULL,
  `create_by`  bigint   DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_role` (`account_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号-角色';

-- -----------------------------------------------------
-- 3.3.1 角色-权限（多对多）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id`            bigint   NOT NULL,
  `role_id`       bigint   NOT NULL,
  `permission_id` bigint   NOT NULL,
  `create_by`     bigint   DEFAULT NULL,
  `create_time`   datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限';

-- =====================================================
-- 二、人员 / 部门（王茗瑾）
-- =====================================================

-- -----------------------------------------------------
-- 3.3.2 部门：树形
-- -----------------------------------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id`         bigint       NOT NULL,
  `parent_id`  bigint       NOT NULL DEFAULT 0,
  `name`       varchar(128) NOT NULL,
  `code`       varchar(64)  DEFAULT NULL,
  `sort`       int          NOT NULL DEFAULT 0,
  `leader_id`  bigint       DEFAULT NULL COMMENT '部门负责人 person_id',
  `status`     tinyint      NOT NULL DEFAULT 1,
  `create_by`  bigint       DEFAULT NULL,
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP,
  `update_by`  bigint       DEFAULT NULL,
  `update_time` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    tinyint(1)   NOT NULL DEFAULT 0,
  `version`    int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- -----------------------------------------------------
-- 3.3.2 人员：与 account 1:1
-- -----------------------------------------------------
DROP TABLE IF EXISTS `person`;
CREATE TABLE `person` (
  `id`           bigint       NOT NULL,
  `account_id`   bigint       DEFAULT NULL COMMENT '账号 ID（1:1，可空）',
  `employee_no`  varchar(32)  NOT NULL COMMENT '工号',
  `name`         varchar(64)  NOT NULL,
  `gender`       tinyint      NOT NULL DEFAULT 0 COMMENT '0=未知 1=男 2=女',
  `mobile`       varchar(20)  DEFAULT NULL,
  `email`        varchar(128) DEFAULT NULL,
  `status`       tinyint      NOT NULL DEFAULT 1 COMMENT '0=离职 1=在职',
  `hired_at`     date         DEFAULT NULL,
  `create_by`    bigint       DEFAULT NULL,
  `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    bigint       DEFAULT NULL,
  `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      tinyint(1)   NOT NULL DEFAULT 0,
  `version`      int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_no` (`employee_no`, `deleted`),
  UNIQUE KEY `uk_account` (`account_id`, `deleted`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员';

-- -----------------------------------------------------
-- 3.3.2 人员-部门（多对多，支持主部门标记）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `person_department`;
CREATE TABLE `person_department` (
  `id`            bigint     NOT NULL,
  `person_id`     bigint     NOT NULL,
  `department_id` bigint     NOT NULL,
  `is_primary`    tinyint(1) NOT NULL DEFAULT 0,
  `create_by`     bigint     DEFAULT NULL,
  `create_time`   datetime   DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_person_department` (`person_id`, `department_id`),
  KEY `idx_department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员-部门';

-- =====================================================
-- 三、题库模块（王茗瑾）
-- =====================================================

-- -----------------------------------------------------
-- 3.3.3 标签
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id`          bigint       NOT NULL,
  `name`        varchar(64)  NOT NULL,
  `category`    varchar(64)  DEFAULT NULL COMMENT '标签分类：题目 / 试卷 / 文档',
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     tinyint(1)   NOT NULL DEFAULT 0,
  `version`     int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';

-- -----------------------------------------------------
-- 3.3.3 题目
-- -----------------------------------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id`            bigint       NOT NULL,
  `type`          varchar(16)  NOT NULL COMMENT 'SINGLE / MULTI / JUDGE / ESSAY / FILL',
  `stem`          text         NOT NULL COMMENT '题干（支持富文本/Markdown）',
  `analysis`      text         DEFAULT NULL COMMENT '解析',
  `difficulty`    tinyint      NOT NULL DEFAULT 1 COMMENT '1=易 2=较易 3=中 4=较难 5=难',
  `default_score` decimal(6,2) NOT NULL DEFAULT 0.00,
  `category_id`   bigint       DEFAULT NULL COMMENT '分类 ID（预留）',
  `answer_json`   json         DEFAULT NULL COMMENT '答案：单选{answer:"C"} 多选{answer:["A","B"]} 判断{answer:true} 简答/填空{answer:"..."}',
  `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布',
  `create_by`     bigint       DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     bigint       DEFAULT NULL,
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       tinyint(1)   NOT NULL DEFAULT 0,
  `version`       int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目';

-- -----------------------------------------------------
-- 3.3.3 题目选项
-- -----------------------------------------------------
DROP TABLE IF EXISTS `question_option`;
CREATE TABLE `question_option` (
  `id`          bigint       NOT NULL,
  `question_id` bigint       NOT NULL,
  `opt_key`     varchar(8)   NOT NULL COMMENT 'A / B / C / D',
  `opt_value`   varchar(512) NOT NULL COMMENT '选项内容',
  `is_correct`  tinyint(1)   NOT NULL DEFAULT 0,
  `sort`        int          NOT NULL DEFAULT 0,
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   bigint       DEFAULT NULL,
  `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项';

-- -----------------------------------------------------
-- 3.3.3 题目-标签（多对多）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `question_tag`;
CREATE TABLE `question_tag` (
  `id`          bigint   NOT NULL,
  `question_id` bigint   NOT NULL,
  `tag_id`      bigint   NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目-标签';

-- =====================================================
-- 四、试卷模块（王茗瑾）
-- =====================================================

-- -----------------------------------------------------
-- 3.3.4 试卷
-- -----------------------------------------------------
DROP TABLE IF EXISTS `paper`;
CREATE TABLE `paper` (
  `id`             bigint       NOT NULL,
  `title`          varchar(256) NOT NULL,
  `description`    text         DEFAULT NULL,
  `total_score`    decimal(6,2) NOT NULL DEFAULT 0.00,
  `duration_min`   int          NOT NULL DEFAULT 0 COMMENT '考试时长（分钟）',
  `pass_score`     decimal(6,2) NOT NULL DEFAULT 60.00,
  `question_count` int          NOT NULL DEFAULT 0,
  `status`         tinyint      NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布',
  `published_at`   datetime     DEFAULT NULL,
  `create_by`      bigint       DEFAULT NULL,
  `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`      bigint       DEFAULT NULL,
  `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        tinyint(1)   NOT NULL DEFAULT 0,
  `version`        int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷';

-- -----------------------------------------------------
-- 3.3.4 试卷-题目（带大题与组卷排序）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `paper_question`;
CREATE TABLE `paper_question` (
  `id`          bigint       NOT NULL,
  `paper_id`    bigint       NOT NULL,
  `question_id` bigint       NOT NULL,
  `section`     varchar(64)  DEFAULT NULL COMMENT '大题标题：一、单选题',
  `score`       decimal(6,2) NOT NULL DEFAULT 0.00,
  `sort`        int          NOT NULL DEFAULT 0,
  `create_by`   bigint       DEFAULT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
  KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-题目';

-- =====================================================
-- 五、考试模块（王茗瑾）
-- =====================================================

-- -----------------------------------------------------
-- 3.3.5 考试
-- -----------------------------------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
  `id`              bigint       NOT NULL,
  `title`           varchar(256) NOT NULL,
  `paper_id`        bigint       NOT NULL,
  `description`     text         DEFAULT NULL,
  `start_time`      datetime     NOT NULL,
  `end_time`        datetime     NOT NULL,
  `duration_min`    int          NOT NULL DEFAULT 0 COMMENT '单人作答时长',
  `submission_rule` tinyint      NOT NULL DEFAULT 0 COMMENT '0=手动交卷 1=到时自动',
  `max_attempts`    int          NOT NULL DEFAULT 1 COMMENT '最大参考次数',
  `status`          tinyint      NOT NULL DEFAULT 0 COMMENT '0=草稿 1=待发布 2=进行中 3=已结束',
  `published_at`    datetime     DEFAULT NULL,
  `create_by`       bigint       DEFAULT NULL,
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `update_by`       bigint       DEFAULT NULL,
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         tinyint(1)   NOT NULL DEFAULT 0,
  `version`         int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_paper` (`paper_id`),
  KEY `idx_status_time` (`status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试';

-- -----------------------------------------------------
-- 3.3.5 考试参考范围：scope_type 决定 target_id 含义
--         1=全员 / 2=部门 / 3=人员 / 4=角色
-- -----------------------------------------------------
DROP TABLE IF EXISTS `exam_scope`;
CREATE TABLE `exam_scope` (
  `id`         bigint   NOT NULL,
  `exam_id`    bigint   NOT NULL,
  `scope_type` tinyint  NOT NULL COMMENT '1=全员 2=部门 3=人员 4=角色',
  `target_id`  bigint   NOT NULL COMMENT '对应 dept_id / person_id / role_id',
  `create_by`  bigint   DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam` (`exam_id`),
  KEY `idx_target` (`scope_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试参考范围';

-- =====================================================
-- 六、评卷 / 成绩 / 作答 / 测评（方雨菲）
-- =====================================================

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
  `exam_id`       bigint       DEFAULT NULL COMMENT '考试 ID��冗余字段，便于查询）',
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

-- =====================================================
-- 七、知识库（方雨菲）
-- =====================================================

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

-- =====================================================
-- 八、通用（方雨菲）
-- =====================================================

-- -----------------------------------------------------
-- 站内消息
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

-- -----------------------------------------------------
-- AI 调用日志（W4 接入）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ai_invoke_log`;
CREATE TABLE `ai_invoke_log` (
  `id`            bigint       NOT NULL,
  `user_id`       bigint       DEFAULT NULL COMMENT '调用人',
  `biz_type`      varchar(32)  NOT NULL COMMENT '业务类型：GRADING / EXPLAIN / GENERATE',
  `biz_id`        bigint       DEFAULT NULL COMMENT '业务主键（如 grade_detail.id）',
  `provider`      varchar(32)  DEFAULT NULL COMMENT 'AI 提供方：mock / openai-compatible',
  `model`         varchar(64)  DEFAULT NULL,
  `prompt`        longtext,
  `response`      longtext,
  `prompt_tokens` int          DEFAULT 0,
  `resp_tokens`   int          DEFAULT 0,
  `duration_ms`   int          DEFAULT 0,
  `status`        tinyint      NOT NULL DEFAULT 0 COMMENT '0=成功 1=失败',
  `error_msg`     varchar(1024) DEFAULT NULL,
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用日志';

SET FOREIGN_KEY_CHECKS = 1;
