package com.dragonraja.forum.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果封装
 * 格式：{records, total, current, size}
 *
 * @param <T> 列表元素类型
 * @author Kou
 */
@Data
public class PageResult<T> {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /**
     * 从 MyBatis-Plus IPage 构造分页结果
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  元素类型
     * @return PageResult
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords());
        r.setTotal(page.getTotal());
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        return r;
    }
}
