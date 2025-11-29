package com.library.module.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.book.entity.BookTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图书标签关联Mapper接口
 */
@Mapper
public interface BookTagMapper extends BaseMapper<BookTag> {

    /**
     * 根据图书ID查询所有标签关联
     */
    List<BookTag> selectByBookId(@Param("bookId") Long bookId);
}