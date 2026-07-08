package com.gac.lms.module.evaluation;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.PackageInfo;

/**
 * 评卷模块边界（Spring Modulith 风格）。
 *
 * <p>本包内的类只允许通过 {@code EvaluationController} / 事件 与外部通信。</p>
 */
@PackageInfo
@ApplicationModule(displayName = "评卷模块")
class EvaluationModule {
}
