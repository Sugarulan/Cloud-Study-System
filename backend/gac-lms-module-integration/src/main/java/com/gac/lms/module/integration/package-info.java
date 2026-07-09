/**
 * 系统集成模块（3.3.11）。
 *
 * <p>负责与外部系统的对接：</p>
 * <ul>
 *   <li>邮件通知（SMTP）—— 考试提醒、成绩发布</li>
 *   <li>Webhook 推送 —— 异步事件回调</li>
 *   <li>站内信触发器 —— 与 {@code sys_message} 表打通</li>
 *   <li>第三方登录接入预留 —— W6 视情况集成企业 SSO</li>
 * </ul>
 *
 * <p>W4 实现：邮件发送 + 站内信写入 + Webhook 推送</p>
 * <p>W5 实现：异步事件总线 + 失败重试</p>
 */
package com.gac.lms.module.integration;
