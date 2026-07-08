-- =====================================================
-- GAC-LMS 完整数据库 Schema（文档版）
-- 实际执行请使用 backend/gac-lms-app/src/main/resources/db/schema.sql
-- 此处仅作为设计文档，方便评审与讨论
--
-- W1 状态：
--   ✅ 已并入 schema.sql（方雨菲）：grade_record / grade_detail /
--                                exam_taking / wrong_question /
--                                knowledge_category / knowledge_doc /
--                                knowledge_doc_version / sys_message
--   ⏳ W2 补充（王茗瑾）：account / person / department / role /
--                       permission / question / paper / exam / ...
-- =====================================================

-- ===== 公共字段约定 =====
-- 所有业务表统一包含：
--   id            bigint       主键（雪花算法）
--   create_by     bigint       创建人 ID
--   create_time   datetime     创建时间
--   update_by     bigint       更新人 ID
--   update_time   datetime     更新时间
--   deleted       tinyint(1)   逻辑删除（0=未删，1=已删）
--   version       int          乐观锁版本号

-- ===== ER 图概览（文字版） =====

-- [账号模块]        account ──< account_role >── role ──< role_permission >── permission
--      │
--      └── 1:1 ──> person ──> department
--                       │
--                       └──< exam_scope >── exam ──> paper ──> question
--                                                              │
--                                                              └──< question_tag >── tag
--
-- [考试作答]        exam_taking ──> exam
--      │                  │
--      │                  └──< grade_record ──< grade_detail >── question
--      │
--      └── 1:N ──> wrong_question ──> question
--
-- [知识库]          knowledge_category (树形)
--                       │
--                       └──< knowledge_doc ──< knowledge_doc_version
--
-- [消息]            sys_message ──> account
--
-- [AI 调用日志]     ai_invoke_log ──> account

-- ===== 表清单 =====

-- 公共 / 账号（王茗瑾）
-- account              账号
-- person               人员信息
-- department           部门
-- role                 角色
-- permission           权限
-- account_role         账号-角色关联
-- role_permission      角色-权限关联

-- 题目 / 试卷 / 考试（王茗瑾）
-- question             题目
-- question_option      题目选项
-- question_tag         题目-标签
-- tag                  标签
-- paper                试卷
-- paper_question       试卷-题目
-- exam                 考试
-- exam_scope           考试-参考范围

-- 评卷 / 成绩 / 作答 / 测评（方雨菲）
-- exam_taking          考试作答记录
-- grade_record         成绩主表
-- grade_detail         成绩明细
-- wrong_question       错题本

-- 知识库（方雨菲）
-- knowledge_category   知识库分类
-- knowledge_doc        知识库文档
-- knowledge_doc_version 文档版本

-- 通用（方雨菲）
-- sys_message          站内消息
-- ai_invoke_log        AI 调用日志
