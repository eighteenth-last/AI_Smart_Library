package com.library.module.book.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.module.book.entity.Book;
import com.library.module.book.vo.BookDetailVO;
import com.library.module.book.vo.BookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图书Mapper接口
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 分页查询图书列表（带关联信息）
     */
    Page<BookVO> selectBookListWithInfo(Page<BookVO> page, @Param("keyword") String keyword,
                                       @Param("authorId") Long authorId, @Param("categoryId") Long categoryId);

    /**
     * 根据ID查询图书详情（带关联信息）
     */
    BookDetailVO selectBookDetailById(@Param("bookId") Long bookId);

    /**
     * 查询图书的标签列表
     */
    List<String> selectBookTags(@Param("bookId") Long bookId);

    /**
     * 查询图书的标签ID列表
     */
    List<Long> selectBookTagIds(@Param("bookId") Long bookId);

    /**
     * 根据标签查询相似图书
     */
    List<BookVO> selectSimilarBooksByTags(@Param("bookId") Long bookId, @Param("tagIds") List<Long> tagIds, @Param("limit") Integer limit);

    /**
     * 更新图书库存
     */
    void updateBookStock(@Param("bookId") Long bookId, @Param("stockChange") Integer stockChange);

    /**
     * 更新借阅次数
     */
    void incrementBorrowCount(@Param("bookId") Long bookId);

    /**
     * 获取热门借阅图书
     */
    List<BookVO> selectHotBooks(@Param("limit") Integer limit);

    /**
     * 批量插入图书标签关联
     */
    void insertBookTags(@Param("bookId") Long bookId, @Param("tagIds") List<Long> tagIds);

    /**
     * 删除图书的所有标签关联
     */
    void deleteBookTags(@Param("bookId") Long bookId);

    /**
     * 查询所有上架的图书（用于推荐算法）
     */
    List<Book> selectAllBooks();
}