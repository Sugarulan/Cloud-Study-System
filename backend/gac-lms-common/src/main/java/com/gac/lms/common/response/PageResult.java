package com.gac.lms.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 数据列表 */
    private List<T> records;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(long total, long pageNum, long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return new PageResult<>(0, pageNum, pageSize, Collections.emptyList());
    }
}
