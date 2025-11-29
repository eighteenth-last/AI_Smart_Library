package com.library.common.result;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果类
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页数量
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {}

    public PageResult(List<T> records, Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long page, Long size) {
        return new PageResult<>(records, total, page, size);
    }

    // Getter and Setter methods
    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }
}