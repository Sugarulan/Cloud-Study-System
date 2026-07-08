-- =====================================================
-- GAC-LMS 初始化数据
-- 覆盖（模块归属见 docs/sql/full-schema.sql）：
--   王茗瑾：账号 / 角色 / 权限 / 部门 / 人员 / 标签 /
--           题目 / 试卷 / 考试
--   方雨菲：知识库 / 站内消息
--
-- ⚠️ 本文件由 spring.sql.init.mode: always 在每次启动时自动执行
--    schema.sql 已 DROP + CREATE 所有表，因此 INSERT 永远面对空表
--    如改成手工执行，请先确认数据库为空，否则会因主键冲突失败
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- ⚠️  BCrypt 密码哈希说明（必须先读这一段）
-- =====================================================
-- 本文件 4 个测试账号使用同一密码哈希：
--     $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- 该哈希对应的明文密码为： admin123  /  student123  /  teacher123
-- （4 个账号的明文密码完全相同，仅用于本地开发）
--
-- 重新生成（推荐）：
--   new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
--       .encode("你的明文密码");
-- 验证：
--   encoder.matches("你的明文密码", "数据库中的哈希");
--
-- 故障排除：登录失败时，用上述 matches() 校验当前哈希与目标密码是否匹配；
--          不匹配则用 encode() 重新生成后 UPDATE account.password_hash。
-- =====================================================

-- -----------------------------------------------------
-- 1. 角色（3 个）
-- -----------------------------------------------------
INSERT INTO `role` (`id`, `code`, `name`, `description`, `sort`) VALUES
(1, 'ADMIN',   '超级管理员', '拥有系统全部权限', 1),
(2, 'TEACHER', '教师',       '题库与试卷管理员', 2),
(3, 'STUDENT', '学员',       '可参加考试与查看个人成绩', 3);

-- -----------------------------------------------------
-- 2. 权限（菜单树示例 + 关键操作）
-- -----------------------------------------------------
INSERT INTO `permission` (`id`, `code`, `name`, `type`, `parent_id`, `sort`) VALUES
(1,  'account',           '账号管理',       1, 0,  1),
(2,  'account:list',      '账号列表',       2, 1,  1),
(3,  'account:create',    '创建账号',       2, 1,  2),
(11, 'person',            '人员管理',       1, 0,  2),
(12, 'person:list',       '人员列表',       2, 11, 1),
(13, 'person:import',     '批量导入',       2, 11, 2),
(21, 'question',          '题目管理',       1, 0,  3),
(22, 'question:list',     '题目列表',       2, 21, 1),
(23, 'question:create',   '新建题目',       2, 21, 2),
(31, 'paper',             '试卷管理',       1, 0,  4),
(32, 'paper:list',        '试卷列表',       2, 31, 1),
(33, 'paper:publish',     '发布试卷',       2, 31, 2),
(41, 'exam',              '考试管理',       1, 0,  5),
(42, 'exam:list',         '考试列表',       2, 41, 1),
(43, 'exam:publish',      '发布考试',       2, 41, 2),
(51, 'evaluation',        '评卷管理',       1, 0,  6),
(61, 'grade',             '成绩管理',       1, 0,  7),
(71, 'knowledge',         '知识库',         1, 0,  8);

-- ADMIN → 全部权限
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(1,  1, 1),  (2,  1, 2),  (3,  1, 3),
(4,  1, 11), (5,  1, 12), (6,  1, 13),
(7,  1, 21), (8,  1, 22), (9,  1, 23),
(10, 1, 31), (11, 1, 32), (12, 1, 33),
(13, 1, 41), (14, 1, 42), (15, 1, 43),
(16, 1, 51), (17, 1, 61), (18, 1, 71);

-- TEACHER → 题库 / 试卷 / 知识库 / 评卷
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(19, 2, 21), (20, 2, 22), (21, 2, 23),
(22, 2, 31), (23, 2, 32), (24, 2, 33),
(25, 2, 51), (26, 2, 71);

-- STUDENT → 考试参加 / 成绩查看 / 知识库浏览
--   仅授予菜单查看权限；具体操作（参考/交卷/看分）走业务层校验
INSERT INTO `role_permission` (`id`, `role_id`, `permission_id`) VALUES
(27, 3, 41), (28, 3, 42),   -- 考试菜单 + 考试列表
(29, 3, 61),                 -- 成绩菜单
(30, 3, 71);                 -- 知识库菜单

-- -----------------------------------------------------
-- 3. 账号（4 个）
-- -----------------------------------------------------
INSERT INTO `account` (`id`, `username`, `password_hash`, `email`, `phone`, `status`) VALUES
(1,    'admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@gac-lms.local',     '13800000001', 1),
(1001, 'student01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'student01@gac-lms.local', '13800000002', 1),
(1002, 'student02', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'student02@gac-lms.local', '13800000003', 1),
(2001, 'teacher01', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'teacher01@gac-lms.local', '13800000004', 1);

-- 账号-角色
INSERT INTO `account_role` (`id`, `account_id`, `role_id`) VALUES
(1, 1,    1),    -- admin     → ADMIN
(2, 1001, 3),    -- student01 → STUDENT
(3, 1002, 3),    -- student02 → STUDENT
(4, 2001, 2);    -- teacher01 → TEACHER

-- -----------------------------------------------------
-- 4. 部门（4 个，2 层树）
-- -----------------------------------------------------
INSERT INTO `department` (`id`, `parent_id`, `name`, `code`, `sort`, `leader_id`) VALUES
(1, 0, '广汽集团',     'GAC',       1, NULL),
(2, 1, '研发中心',     'GAC-RD',    1, NULL),
(3, 1, '人力资源中心', 'GAC-HR',    2, NULL),
(4, 2, '智能网联部',   'GAC-RD-IC', 1, NULL);

-- -----------------------------------------------------
-- 5. 人员（4 个，对应 4 个账号）
-- -----------------------------------------------------
INSERT INTO `person` (`id`, `account_id`, `employee_no`, `name`, `gender`, `mobile`, `email`, `status`, `hired_at`) VALUES
(1,    1,    'E0001', '王茗瑾',   1, '13800000001', 'admin@gac-lms.local',     1, '2024-01-01'),
(1001, 1001, 'E1001', '学员小张', 1, '13800000002', 'student01@gac-lms.local', 1, '2025-09-01'),
(1002, 1002, 'E1002', '学员小李', 2, '13800000003', 'student02@gac-lms.local', 1, '2025-09-01'),
(2001, 2001, 'E2001', '方老师',   1, '13800000004', 'teacher01@gac-lms.local', 1, '2024-06-01');

-- 人员-部门（每人一主部门）
INSERT INTO `person_department` (`id`, `person_id`, `department_id`, `is_primary`) VALUES
(1, 1,    2, 1),
(2, 1001, 4, 1),
(3, 1002, 4, 1),
(4, 2001, 2, 1);

-- -----------------------------------------------------
-- 6. 标签
-- -----------------------------------------------------
INSERT INTO `tag` (`id`, `name`, `category`) VALUES
(1, 'Java',     '题目'),
(2, '产品知识', '题目'),
(3, '安全',     '题目'),
(4, 'JVM',      '题目'),
(5, 'AI',       '题目'),
(6, '初级',     '题目'),
(7, '高级',     '题目');

-- -----------------------------------------------------
-- 7. 题目（4 道，覆盖 SINGLE / MULTI / JUDGE / ESSAY）
-- -----------------------------------------------------
INSERT INTO `question` (`id`, `type`, `stem`, `analysis`, `difficulty`, `default_score`, `category_id`, `answer_json`, `status`) VALUES
(1, 'SINGLE', 'Java 中，用于保证多个线程间共享变量可见性的关键字是？', 'volatile 强制线程从主内存读写变量的最新值。', 2, 5.00, NULL, '{"answer":"C"}', 1),
(2, 'MULTI',  '下列哪些是 JVM 内存区域？（多选）', 'JVM 内存区域主要包括堆、方法区、虚拟机栈、本地方法栈、程序计数器。', 2, 5.00, NULL, '{"answer":["A","B","D"]}', 1),
(3, 'JUDGE',  '广汽埃安是广汽集团旗下的纯电品牌。', '广汽埃安（GAC Aion）2017 年成立，专注纯电动智能汽车。', 1, 2.00, NULL, '{"answer":true}', 1),
(4, 'ESSAY',  '请简述 Spring AI 与 Spring Boot 的集成方式，并说明如何实现 AI 故障降级。', NULL, 3, 20.00, NULL, NULL, 1);

-- 题目 1 的选项
INSERT INTO `question_option` (`id`, `question_id`, `opt_key`, `opt_value`, `is_correct`, `sort`) VALUES
(1, 1, 'A', 'synchronized', 0, 1),
(2, 1, 'B', 'final',       0, 2),
(3, 1, 'C', 'volatile',    1, 3),
(4, 1, 'D', 'static',      0, 4);

-- 题目 2 的选项
INSERT INTO `question_option` (`id`, `question_id`, `opt_key`, `opt_value`, `is_correct`, `sort`) VALUES
(5, 2, 'A', '堆 (Heap)',          1, 1),
(6, 2, 'B', '方法区 (Method Area)', 1, 2),
(7, 2, 'C', '寄存器组',            0, 3),
(8, 2, 'D', '虚拟机栈 (VM Stack)', 1, 4);

-- 题目-标签
INSERT INTO `question_tag` (`id`, `question_id`, `tag_id`) VALUES
(1, 1, 1),
(2, 1, 4),
(3, 1, 7),
(4, 2, 1),
(5, 2, 4),
(6, 2, 7),
(7, 3, 2),
(8, 3, 6),
(9, 4, 1),
(10, 4, 5),
(11, 4, 7);

-- -----------------------------------------------------
-- 8. 试卷（1 份示例）
-- -----------------------------------------------------
INSERT INTO `paper` (`id`, `title`, `description`, `total_score`, `duration_min`, `pass_score`, `question_count`, `status`, `published_at`) VALUES
(1, 'Java & 产品知识 综合测试', 'W2 示例试卷，覆盖单选/多选/判断/简答', 32.00, 30, 18.00, 4, 1, NOW());

-- 试卷-题目（带分大题、组卷排序）
INSERT INTO `paper_question` (`id`, `paper_id`, `question_id`, `section`, `score`, `sort`) VALUES
(1, 1, 1, '一、单项选择题', 5.00,  1),
(2, 1, 2, '二、多项选择题', 5.00,  2),
(3, 1, 3, '三、判断题',     2.00,  3),
(4, 1, 4, '四、简答题',     20.00, 4);

-- -----------------------------------------------------
-- 9. 考试（1 场示例，状态=进行中）
-- -----------------------------------------------------
INSERT INTO `exam` (`id`, `title`, `paper_id`, `start_time`, `end_time`, `duration_min`, `submission_rule`, `max_attempts`, `status`, `published_at`, `description`) VALUES
(1, '【示例】Java & 产品知识 综合测试', 1, '2024-01-01 00:00:00', '2099-12-31 23:59:59', 30, 1, 1, 2, NOW(), '参考范围：STUDENT 角色；可手动/到时自动交卷；只允许 1 次');

-- 考试范围：限定为 STUDENT 角色（role_id=3）
INSERT INTO `exam_scope` (`id`, `exam_id`, `scope_type`, `target_id`) VALUES
(1, 1, 4, 3);

-- -----------------------------------------------------
-- 10. 知识库分类（来自 W1）
-- -----------------------------------------------------
INSERT INTO `knowledge_category` (`id`, `parent_id`, `name`, `sort`) VALUES
(1, 0, '产品手册', 1),
(2, 0, '设计文档', 2),
(3, 0, '运维手册', 3),
(4, 0, '培训课件', 4),
(11, 1, '广汽埃安', 1),
(12, 1, '广汽传祺', 2);

-- -----------------------------------------------------
-- 11. 知识库文档
-- -----------------------------------------------------
INSERT INTO `knowledge_doc` (`id`, `category_id`, `title`, `summary`, `content`, `tags`, `status`, `version`, `author_id`) VALUES
(1, 11, '埃安 LX Plus 用户手册', '介绍车辆基础操作',
 '# 埃安 LX Plus 用户手册\n\n本手册介绍车辆基础操作、充电指南、ADAS 使用方法等。',
 '产品,手册,埃安', 2, 1, 1),
(2, 2, '云学习系统架构设计', 'W1 系统架构总览',
 '# 系统架构\n\n## 总体架构\n\n...',
 '架构,设计', 2, 1, 1);

-- -----------------------------------------------------
-- 12. 站内消息
-- -----------------------------------------------------
INSERT INTO `sys_message` (`id`, `user_id`, `type`, `title`, `content`) VALUES
(1, 1001, 'EXAM_REMIND',   '【考试提醒】您有一场待考', '《Java 高级开发》考试将于明日 14:00 开始，请准时参加。'),
(2, 1001, 'GRADE_PUBLISH', '【成绩发布】Java 高级开发 成绩已发布', '您的成绩：85.0 分（已通过），查看详情请进入个人测评。');
