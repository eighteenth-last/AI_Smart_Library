package com.library.module.book.service;

import com.library.common.result.PageResult;
import com.library.module.book.dto.BookCreateDTO;
import com.library.module.book.dto.BookSearchDTO;
import com.library.module.book.dto.BookUpdateDTO;
import com.library.module.book.vo.BookDetailVO;
import com.library.module.book.vo.BookRecommendVO;
import com.library.module.book.vo.BookVO;

import java.util.List;

/**
 * 图书服务接口
 */
public interface BookService {

    /**
     * 分页查询图书列表
     */
    PageResult<BookVO> getBookList(BookSearchDTO searchDTO);

    /**
     * 根据ID获取图书详情
     */
    BookDetailVO getBookById(Long bookId);

    /**
     * 获取图书详情（管理员/作者）
     */
    BookVO getBookDetail(Long bookId);

    /**
     * 搜索图书
     */
    PageResult<BookVO> searchBooks(String keyword, String type, Long page, Long size);

    /**
     * 获取相似图书推荐
     */
    List<BookRecommendVO> getRecommendBooks(Long bookId, Integer limit);

    /**
     * 管理员新增图书
     */
    Long addBook(BookCreateDTO createDTO);

    /**
     * 管理员编辑图书
     */
    void updateBook(Long bookId, BookUpdateDTO updateDTO);

    /**
     * 管理员删除图书（逻辑删除）
     */
    void deleteBook(Long bookId);

    /**
     * 获取热门借阅图书
     */
    List<BookVO> getHotBooks(Integer limit);

    /**
     * 更新图书库存
     */
    void updateBookStock(Long bookId, Integer stockChange);

    /**
     * 增加图书借阅次数
     */
    void incrementBorrowCount(Long bookId);
}