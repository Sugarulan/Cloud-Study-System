package com.gac.lms.common.constants;

/**
 * 全局常量。
 */
public final class CommonConstants {

    private CommonConstants() {}

    /** API 前缀 */
    public static final String API_PREFIX = "/api/v1";

    /** 当前用户 HTTP Header（由网关 / 拦截器注入） */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** 系统默认超级管理员 ID */
    public static final long SUPER_ADMIN_ID = 1L;

    /** Redis Key 前缀 */
    public static final class RedisKey {
        private RedisKey() {}
        /** 在线作答答题快照 v2：exam:taking:v2:{examId}:{userId} */
        public static final String EXAM_TAKING_V2 = "exam:taking:v2:%d:%d";
        /** 答题快照 TTL：2 小时（秒） */
        public static final long EXAM_TAKING_TTL_SECONDS = 2 * 60 * 60L;
    }
}
