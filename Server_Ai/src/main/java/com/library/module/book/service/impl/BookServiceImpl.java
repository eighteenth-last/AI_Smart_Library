package com.library.module.book.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.PageResult;
import com.library.module.book.dto.BookCreateDTO;
import com.library.module.book.dto.BookSearchDTO;
import com.library.module.book.dto.BookUpdateDTO;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.book.service.BookService;
import com.library.module.book.service.RecommendService;
import com.library.module.book.vo.BookDetailVO;
import com.library.module.book.vo.BookRecommendVO;
import com.library.module.book.vo.BookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图书服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final RecommendService recommendService;

    @Override
    public PageResult<BookVO> getBookList(BookSearchDTO searchDTO) {
        Page<BookVO> page = new Page<>(searchDTO.getPage(), searchDTO.getSize());

        // 构建查询参数
        String keyword = searchDTO.getKeyword();
        Long authorId = searchDTO.getAuthorId();
        Long categoryId = searchDTO.getCategoryId();

        Page<BookVO> result = bookMapper.selectBookListWithInfo(page, keyword, authorId, categoryId);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public BookDetailVO getBookById(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        BookDetailVO bookDetail = bookMapper.selectBookDetailById(bookId);
        if (bookDetail == null) {
            throw new RuntimeException("图书不存在");
        }

        // 获取图书标签
        List<String> tags = bookMapper.selectBookTags(bookId);
        bookDetail.setTags(tags.toArray(new String[0]));

        return bookDetail;
    }

    @Override
    public BookVO getBookDetail(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        // 查询图书基本信息
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 转换为BookVO
        BookVO bookVO = new BookVO();
        bookVO.setBookId(book.getBookId());
        bookVO.setIsbn(book.getIsbn());
        bookVO.setTitle(book.getTitle());
        bookVO.setAuthorId(book.getAuthorId());
        bookVO.setCategoryId(book.getCategoryId());
        bookVO.setPublisher(book.getPublisher());
        bookVO.setPublishYear(book.getPublishYear());
        bookVO.setCoverUrl(book.getCoverUrl());
        bookVO.setDescription(book.getDescription());
        bookVO.setTotalStock(book.getTotalStock());
        bookVO.setAvailableStock(book.getAvailableStock());
        bookVO.setAverageRating(book.getAverageRating());
        bookVO.setReviewCount(book.getReviewCount());
        bookVO.setBorrowCount(book.getBorrowCount());
        bookVO.setStatus(book.getStatus());

        // 获取标签ID列表
        List<Long> tagIds = bookMapper.selectBookTagIds(bookId);
        bookVO.setTagIds(tagIds);

        return bookVO;
    }

    @Override
    public PageResult<BookVO> searchBooks(String keyword, String type, Long page, Long size) {
        Page<BookVO> pageInfo = new Page<>(page != null ? page : 1, size != null ? size : 10);

        // 根据搜索类型构建查询参数
        String searchKeyword = keyword;
        Long authorId = null;
        Long categoryId = null;

        if ("author".equals(type)) {
            // 如果按作者搜索，这里可以增加作者名到ID的转换逻辑
            // 暂时作为关键词搜索
            searchKeyword = keyword;
        } else if ("isbn".equals(type)) {
            // ISBN搜索作为关键词搜索
            searchKeyword = keyword;
        } else {
            // 默认按书名搜索
            searchKeyword = keyword;
        }

        Page<BookVO> result = bookMapper.selectBookListWithInfo(pageInfo, searchKeyword, authorId, categoryId);

        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<BookRecommendVO> getRecommendBooks(Long bookId, Integer limit) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        // 使用推荐服务获取相似图书
        return recommendService.getSimilarBooksByJaccard(bookId, limit);
    }

    @Override
    @Transactional
    public Long addBook(BookCreateDTO createDTO) {
        if (createDTO == null) {
            throw new RuntimeException("创建图书信息不能为空");
        }

        // 构建图书实体
        Book book = new Book();
        book.setIsbn(createDTO.getIsbn());
        book.setTitle(createDTO.getTitle());
        book.setAuthorId(createDTO.getAuthorId());
        book.setCategoryId(createDTO.getCategoryId());
        book.setPublisher(createDTO.getPublisher());
        book.setPublishYear(createDTO.getPublishYear());
        book.setCoverUrl(createDTO.getCoverUrl());
        book.setDescription(createDTO.getDescription());
        book.setTotalStock(createDTO.getTotalStock());
        book.setAvailableStock(createDTO.getTotalStock());
        book.setAverageRating(0.0);
        book.setReviewCount(0);
        book.setBorrowCount(0);
        book.setStatus(1); // 默认上架
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        // 插入图书信息
        bookMapper.insert(book);

        // 处理标签关联
        if (createDTO.getTagIds() != null && !createDTO.getTagIds().isEmpty()) {
            bookMapper.insertBookTags(book.getBookId(), createDTO.getTagIds());
        }

        log.info("新增图书成功，图书ID: {}", book.getBookId());
        return book.getBookId();
    }

    @Override
    @Transactional
    public void updateBook(Long bookId, BookUpdateDTO updateDTO) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        // 检查图书是否存在
        Book existBook = bookMapper.selectById(bookId);
        if (existBook == null) {
            throw new RuntimeException("图书不存在");
        }

        // 构建更新实体
        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle(updateDTO.getTitle());
        book.setAuthorId(updateDTO.getAuthorId());
        book.setCategoryId(updateDTO.getCategoryId());
        book.setPublisher(updateDTO.getPublisher());
        book.setPublishYear(updateDTO.getPublishYear());
        book.setCoverUrl(updateDTO.getCoverUrl());
        book.setDescription(updateDTO.getDescription());
        book.setTotalStock(updateDTO.getTotalStock());
        book.setUpdatedAt(LocalDateTime.now());

        // 更新图书信息
        bookMapper.updateById(book);

        // 处理标签关联更新
        if (updateDTO.getTagIds() != null) {
            // 删除原有标签关联
            bookMapper.deleteBookTags(bookId);
            // 添加新的标签关联
            if (!updateDTO.getTagIds().isEmpty()) {
                bookMapper.insertBookTags(bookId, updateDTO.getTagIds());
            }
        }

        log.info("更新图书成功，图书ID: {}", bookId);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        // 检查图书是否存在
        Book existBook = bookMapper.selectById(bookId);
        if (existBook == null) {
            throw new RuntimeException("图书不存在");
        }

        // 逻辑删除：设置状态为下架
        Book book = new Book();
        book.setBookId(bookId);
        book.setStatus(0); // 0表示下架
        book.setUpdatedAt(LocalDateTime.now());

        bookMapper.updateById(book);

        log.info("删除图书成功，图书ID: {}", bookId);
    }

    @Override
    public List<BookVO> getHotBooks(Integer limit) {
        Integer queryLimit = limit != null && limit > 0 ? limit : 10;
        return bookMapper.selectHotBooks(queryLimit);
    }

    @Override
    @Transactional
    public void updateBookStock(Long bookId, Integer stockChange) {
        if (bookId == null || stockChange == null) {
            throw new RuntimeException("图书ID和库存变化量不能为空");
        }

        // 获取当前图书信息
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 计算新的可借数量
        Integer newAvailableStock = book.getAvailableStock() + stockChange;
        if (newAvailableStock < 0) {
            throw new RuntimeException("库存不足");
        }

        // 更新库存
        bookMapper.updateBookStock(bookId, stockChange);

        log.info("更新图书库存成功，图书ID: {}, 库存变化: {}", bookId, stockChange);
    }

    @Override
    @Transactional
    public void incrementBorrowCount(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("图书ID不能为空");
        }

        // 检查图书是否存在
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new RuntimeException("图书不存在");
        }

        // 增加借阅次数
        bookMapper.incrementBorrowCount(bookId);

        log.info("增加图书借阅次数成功，图书ID: {}", bookId);
    }
}