package com.gac.lms.module.question.service;

import com.gac.lms.module.question.vo.TagVO;

import java.util.List;

/**
 * 标签业务接口。
 *
 * @author 王茗瑾
 */
public interface TagService {

    /** 列出所有标签（可按 category 过滤） */
    List<TagVO> listAll(String category);

    /** 创建标签 */
    TagVO create(String name, String category);

    /** 更新标签 */
    TagVO update(Long id, String name, String category);

    /** 删除标签（被引用时禁止） */
    void delete(Long id);
}
