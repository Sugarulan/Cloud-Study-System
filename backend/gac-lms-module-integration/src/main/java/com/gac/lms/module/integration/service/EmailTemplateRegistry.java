package com.gac.lms.module.integration.service;

import java.util.Map;

/**
 * 邮件模板注册表。
 *
 * <p>W2 阶段：硬编码 2 个模板（考试提醒 / 成绩发布）。</p>
 * <p>W4 阶段：可改为数据库存储 + 后台管理。</p>
 *
 * @author 方雨菲
 */
public final class EmailTemplateRegistry {

    private EmailTemplateRegistry() {}

    /** 模板代码常量 */
    public static final String TPL_EXAM_REMIND = "EXAM_REMIND";
    public static final String TPL_GRADE_PUBLISH = "GRADE_PUBLISH";

    /**
     * 根据模板代码 + 参数渲染邮件主题与正文。
     *
     * @return [subject, content]
     */
    public static String[] render(String templateCode, Map<String, String> params) {
        if (params == null) params = Map.of();
        switch (templateCode == null ? "" : templateCode) {
            case TPL_EXAM_REMIND:
                return new String[]{
                        "【考试提醒】" + params.getOrDefault("examName", "（未命名考试）") + " 即将开始",
                        "尊敬的" + params.getOrDefault("userName", "学员") + "：\n\n" +
                                "您报名的考试 \"" + params.getOrDefault("examName", "（未命名）") + "\" " +
                                "将于 " + params.getOrDefault("startTime", "（待定）") + " 开始，" +
                                "时长 " + params.getOrDefault("duration", "60") + " 分钟。\n\n" +
                                "请准时参加，祝考试顺利！\n\n广汽云学习系统"
                };
            case TPL_GRADE_PUBLISH:
                return new String[]{
                        "【成绩发布】" + params.getOrDefault("examName", "（未命名考试）") + " 成绩已发布",
                        "尊敬的" + params.getOrDefault("userName", "学员") + "：\n\n" +
                                "您参加的 \"" + params.getOrDefault("examName", "（未命名）") + "\" 成绩已发布：\n" +
                                "总分：" + params.getOrDefault("score", "0") + "\n" +
                                "是否通过：" + ("true".equalsIgnoreCase(params.getOrDefault("passed", "false")) ? "通过" : "未通过") + "\n\n" +
                                "请登录系统查看详情。\n\n���汽云学习系统"
                };
            default:
                return new String[]{
                        "【通知】",
                        params.isEmpty() ? "（无内容）" : params.toString()
                };
        }
    }
}
