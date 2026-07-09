package com.gac.lms.module.exam.service.impl;

import com.gac.lms.module.exam.service.PaperQueryService;
import com.gac.lms.module.exam.vo.PaperRenderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock 试卷服务 —— W2 阶段默认启用。
 *
 * <p>当配置 {@code gac.lms.exam-taking.paper-source=remote} 时切换为真实 REST 调用实现。</p>
 *
 * @author 方雨菲
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "gac.lms.exam-taking", name = "paper-source", havingValue = "mock", matchIfMissing = true)
public class MockPaperQueryServiceImpl implements PaperQueryService {

    @Override
    public PaperRenderVO getPaper(Long paperId) {
        log.info("[MockPaperQueryService] returning mock paper for paperId={}", paperId);

        // Mock：构造一份包含 5 道题的试卷
        PaperRenderVO vo = new PaperRenderVO();
        vo.setExamId(1001L);
        vo.setPaperId(paperId);
        vo.setTitle("（示例）Java 高级开发考试");
        vo.setStartTime(LocalDateTime.now());
        vo.setEndTime(LocalDateTime.now().plusHours(1));
        vo.setDurationMinutes(60);
        vo.setTotalScore(100);

        List<PaperRenderVO.QuestionVO> questions = new ArrayList<>();

        // 题 1：单选
        PaperRenderVO.QuestionVO q1 = new PaperRenderVO.QuestionVO();
        q1.setId(1L);
        q1.setType("SINGLE");
        q1.setStem("下列关于 JVM 内存模型的描述正确的是？");
        q1.setScore(20);
        q1.setDifficulty("MEDIUM");
        q1.setOptions(List.of(
                new PaperRenderVO.Option("A", "方法区存储线程私有变量"),
                new PaperRenderVO.Option("B", "堆是线程共享的"),
                new PaperRenderVO.Option("C", "虚拟机栈是线程共享的"),
                new PaperRenderVO.Option("D", "程序计数器是线程共享的")
        ));
        questions.add(q1);

        // 题 2：多选
        PaperRenderVO.QuestionVO q2 = new PaperRenderVO.QuestionVO();
        q2.setId(2L);
        q2.setType("MULTI");
        q2.setStem("下列哪些是 Java 集合框架的接口？（多选）");
        q2.setScore(20);
        q2.setDifficulty("EASY");
        q2.setOptions(List.of(
                new PaperRenderVO.Option("A", "List"),
                new PaperRenderVO.Option("B", "Set"),
                new PaperRenderVO.Option("C", "Map"),
                new PaperRenderVO.Option("D", "Iterator")
        ));
        questions.add(q2);

        // 题 3：判断
        PaperRenderVO.QuestionVO q3 = new PaperRenderVO.QuestionVO();
        q3.setId(3L);
        q3.setType("JUDGE");
        q3.setStem("Spring Boot 默认内嵌 Tomcat 服务器。");
        q3.setScore(20);
        q3.setDifficulty("EASY");
        q3.setOptions(List.of(
                new PaperRenderVO.Option("TRUE", "正确"),
                new PaperRenderVO.Option("FALSE", "错误")
        ));
        questions.add(q3);

        // 题 4：填空
        PaperRenderVO.QuestionVO q4 = new PaperRenderVO.QuestionVO();
        q4.setId(4L);
        q4.setType("FILL");
        q4.setStem("MyBatis-Plus 中用于标记主键的注解是 __________ 。");
        q4.setScore(20);
        q4.setDifficulty("MEDIUM");
        q4.setOptions(List.of());
        questions.add(q4);

        // 题 5：主观题
        PaperRenderVO.QuestionVO q5 = new PaperRenderVO.QuestionVO();
        q5.setId(5L);
        q5.setType("ESSAY");
        q5.setStem("请简述 Spring IoC 容器的生命周期。");
        q5.setScore(20);
        q5.setDifficulty("HARD");
        q5.setOptions(List.of());
        questions.add(q5);

        vo.setQuestions(questions);
        return vo;
    }

    @Override
    public Integer getPaperTotalScore(Long paperId) {
        return getPaper(paperId).getTotalScore();
    }

    @Override
    public Integer getPaperQuestionCount(Long paperId) {
        return getPaper(paperId).getQuestions().size();
    }
}
