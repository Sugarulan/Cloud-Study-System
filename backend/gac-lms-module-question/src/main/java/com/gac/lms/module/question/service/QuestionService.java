package com.gac.lms.module.question.service;

import com.gac.lms.common.response.PageResult;
import com.gac.lms.module.question.dto.BatchDeleteCmd;
import com.gac.lms.module.question.dto.QuestionCreateCmd;
import com.gac.lms.module.question.dto.QuestionQuery;
import com.gac.lms.module.question.dto.QuestionUpdateCmd;
import com.gac.lms.module.question.vo.QuestionVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 题目业务接口。
 *
 * @author 王茗瑾
 */
public interface QuestionService {

    /** 分页查询 */
    PageResult<QuestionVO> page(QuestionQuery query);

    /** 详情（含选项与标签） */
    QuestionVO getById(Long id);

    /** 创建题目（校验答案格式 + 写入选项 + 关联标签，全程事务） */
    QuestionVO create(QuestionCreateCmd cmd);

    /** 更新题目（不可改 type；选项与标签替换式写入） */
    QuestionVO update(Long id, QuestionUpdateCmd cmd);

    /** 软删单个 */
    void delete(Long id);

    /** 批量软删（逐个校验引用） */
    void batchDelete(BatchDeleteCmd cmd);

    /** 单个发布（status 0→1） */
    void publish(Long id);

    /** 批量发布 */
    void batchPublish(List<Long> ids);

    /** 取消发布（status 1→0） */
    void unpublish(Long id);

    /** 按 ID 列表批量取（试卷模块抽题用，仅返回 id/stem/type/score） */
    List<QuestionVO> listByIds(Collection<Long> ids);

    /** 按 ID 取精简信息（试卷渲染用，不含答案） */
    Map<Long, QuestionVO> mapByIds(Collection<Long> ids);
}
