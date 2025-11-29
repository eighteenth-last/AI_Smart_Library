package com.library.module.author.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.module.author.dto.QuestionReplyDTO;
import com.library.module.author.entity.AuthorQa;
import com.library.module.author.mapper.AuthorMapper;
import com.library.module.author.mapper.AuthorQaMapper;
import com.library.module.author.service.AuthorService;
import com.library.module.author.vo.AuthorBookVO;
import com.library.module.author.vo.AuthorStatsVO;
import com.library.module.author.vo.QuestionVO;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.borrow.entity.BorrowRecord;
import com.library.module.borrow.mapper.BorrowRecordMapper;
import com.library.module.review.entity.Review;
import com.library.module.review.mapper.ReviewMapper;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作者服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final AuthorQaMapper authorQaMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    @Override
    public AuthorStatsVO getAuthorStats(Long authorId) {
        AuthorStatsVO stats = new AuthorStatsVO();

        // 1. 获取作品总数
        QueryWrapper<Book> bookQuery = new QueryWrapper<>();
        bookQuery.eq("author_id", authorId);
        Long totalBooks = bookMapper.selectCount(bookQuery);
        stats.setTotalBooks(totalBooks != null ? totalBooks.intValue() : 0);

        // 2. 获取所有作品
        List<Book> books = bookMapper.selectList(bookQuery);

        // 3. 统计总借阅量 - 累加book表的borrow_count字段
        int totalBorrows = books.stream()
                .mapToInt(book -> book.getBorrowCount() != null ? book.getBorrowCount() : 0)
                .sum();
        stats.setTotalBorrows(totalBorrows);

        // 4. 统计总收藏量 - 累加book表的favorite_count字段
        int totalFavorites = books.stream()
                .mapToInt(book -> book.getFavoriteCount() != null ? book.getFavoriteCount() : 0)
                .sum();
        stats.setTotalFavorites(totalFavorites);

        // 5. 统计总评价数
        int totalReviews = books.stream()
                .mapToInt(book -> book.getReviewCount() != null ? book.getReviewCount() : 0)
                .sum();
        stats.setTotalReviews(totalReviews);

        // 6. 计算平均评分 - 使用review表的rating字段总和除以作品数
        double averageRating = 0.0;
        if (!books.isEmpty()) {
            List<Long> bookIds = books.stream()
                    .map(Book::getBookId)
                    .collect(Collectors.toList());

            // 查询这些图书的所有评价
            QueryWrapper<Review> reviewQuery = new QueryWrapper<>();
            reviewQuery.in("book_id", bookIds);
            List<Review> reviews = reviewMapper.selectList(reviewQuery);

            // 计算所有评分的总和
            int totalRating = reviews.stream()
                    .mapToInt(review -> review.getRating() != null ? review.getRating() : 0)
                    .sum();

            // 平均评分 = 评分总和 / 作品总数
            if (stats.getTotalBooks() > 0) {
                averageRating = (double) totalRating / stats.getTotalBooks();
            }
        }
        stats.setAverageRating(BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP));

        // 7. 获取待回答问题数
        QueryWrapper<AuthorQa> qaQuery = new QueryWrapper<>();
        qaQuery.eq("author_id", authorId)
                .isNull("answer");
        Long pendingQuestions = authorQaMapper.selectCount(qaQuery);
        stats.setPendingQuestions(pendingQuestions != null ? pendingQuestions.intValue() : 0);

        // 8. 获取作品借阅排行TOP5 - 使用book表的borrow_count字段
        List<AuthorStatsVO.BookBorrowStats> topBookStats = new ArrayList<>();
        if (!books.isEmpty()) {
            List<Long> bookIds = books.stream()
                    .map(Book::getBookId)
                    .collect(Collectors.toList());

            // 获取每本书的评分 - 直接从review表取rating字段
            Map<Long, Integer> bookRatingMap = new HashMap<>();
            QueryWrapper<Review> allReviewQuery = new QueryWrapper<>();
            allReviewQuery.in("book_id", bookIds);
            List<Review> allReviews = reviewMapper.selectList(allReviewQuery);
            for (Review review : allReviews) {
                // 每本书取第一条评分（或者可以取最新的）
                if (!bookRatingMap.containsKey(review.getBookId()) && review.getRating() != null) {
                    bookRatingMap.put(review.getBookId(), review.getRating());
                }
            }

            // 按book表的borrow_count字段排序并取TOP5
            List<Book> topBooks = books.stream()
                    .sorted((b1, b2) -> {
                        int count1 = b1.getBorrowCount() != null ? b1.getBorrowCount() : 0;
                        int count2 = b2.getBorrowCount() != null ? b2.getBorrowCount() : 0;
                        return Integer.compare(count2, count1);
                    })
                    .limit(5)
                    .collect(Collectors.toList());

            for (Book book : topBooks) {
                AuthorStatsVO.BookBorrowStats bookStats = new AuthorStatsVO.BookBorrowStats();
                bookStats.setBookId(book.getBookId());
                bookStats.setTitle(book.getTitle());
                bookStats.setBorrowCount(book.getBorrowCount() != null ? book.getBorrowCount() : 0);

                Integer rating = bookRatingMap.get(book.getBookId());
                if (rating != null && rating > 0) {
                    bookStats.setAverageRating(BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP));
                } else {
                    bookStats.setAverageRating(BigDecimal.ZERO);
                }

                topBookStats.add(bookStats);
            }
        }
        stats.setTopBooks(topBookStats);

        // 9. 获取月度借阅趋势（最近12个月）
        List<AuthorStatsVO.MonthlyBorrowTrend> monthlyTrends = getMonthlyTrends(books, 12);
        stats.setMonthlyTrends(monthlyTrends);

        return stats;
    }

    /**
     * 获取月度借阅趋势（统计该作者所有图书在每个月的借阅总量）
     */
    private List<AuthorStatsVO.MonthlyBorrowTrend> getMonthlyTrends(List<Book> books, int months) {
        List<AuthorStatsVO.MonthlyBorrowTrend> trends = new ArrayList<>();

        if (books.isEmpty()) {
            return trends;
        }

        // 获取所有作品的ID
        List<Long> bookIds = books.stream()
                .map(Book::getBookId)
                .collect(Collectors.toList());

        // 查询最近N个月的借阅记录
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        QueryWrapper<BorrowRecord> borrowQuery = new QueryWrapper<>();
        borrowQuery.in("book_id", bookIds)
                .ge("borrow_time", startDate)
                .eq("deleted", 0);

        List<BorrowRecord> borrowRecords = borrowRecordMapper.selectList(borrowQuery);

        // 按月份分组统计
        Map<String, Integer> monthCountMap = new HashMap<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.getBorrowTime() != null) {
                String month = String.format("%d-%02d",
                        record.getBorrowTime().getYear(),
                        record.getBorrowTime().getMonthValue());
                monthCountMap.put(month, monthCountMap.getOrDefault(month, 0) + 1);
            }
        }

        // 生成完整的12个月数据（包括没有借阅记录的月份）
        LocalDateTime now = LocalDateTime.now();
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthKey = String.format("%d-%02d",
                    monthDate.getYear(),
                    monthDate.getMonthValue());

            AuthorStatsVO.MonthlyBorrowTrend trend = new AuthorStatsVO.MonthlyBorrowTrend();
            trend.setMonth(monthKey);
            trend.setBorrowCount(monthCountMap.getOrDefault(monthKey, 0));
            trends.add(trend);
        }

        return trends;
    }

    @Override
    public List<AuthorBookVO> getAuthorBooks(Long authorId, String keyword) {
        // 1. 查询作者的所有作品
        QueryWrapper<Book> bookQuery = new QueryWrapper<>();
        bookQuery.eq("author_id", authorId);
        
        // 2. 如果有关键词，添加模糊查询条件（ISBN或书名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            bookQuery.and(wrapper -> wrapper
                .like("isbn", keyword.trim())
                .or()
                .like("title", keyword.trim())
            );
        }
        
        List<Book> books = bookMapper.selectList(bookQuery);

        if (books.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> bookIds = books.stream()
                .map(Book::getBookId)
                .collect(Collectors.toList());

        // 2. 统计每本书的平均评分 - 从review表计算
        Map<Long, Double> bookRatingMap = new HashMap<>();
        QueryWrapper<Review> reviewQuery = new QueryWrapper<>();
        reviewQuery.in("book_id", bookIds);
        List<Review> reviews = reviewMapper.selectList(reviewQuery);
        Map<Long, List<Review>> bookReviewsMap = reviews.stream()
                .collect(Collectors.groupingBy(Review::getBookId));
        for (Map.Entry<Long, List<Review>> entry : bookReviewsMap.entrySet()) {
            double avgRating = entry.getValue().stream()
                    .mapToInt(r -> r.getRating() != null ? r.getRating() : 0)
                    .average()
                    .orElse(0.0);
            bookRatingMap.put(entry.getKey(), avgRating);
        }

        // 4. 转换为VO对象
        List<AuthorBookVO> bookVOList = new ArrayList<>();
        for (Book book : books) {
            AuthorBookVO vo = new AuthorBookVO();
            vo.setBookId(book.getBookId());
            vo.setTitle(book.getTitle());
            vo.setIsbn(book.getIsbn());
            vo.setPublisher(book.getPublisher());
            vo.setPublishYear(book.getPublishYear());
            vo.setCoverUrl(book.getCoverUrl());
            vo.setTotalStock(book.getTotalStock());
            vo.setAvailableStock(book.getAvailableStock());
            vo.setStatus(book.getStatus());

            // 设置借阅次数 - 直接使用book表的borrow_count字段
            vo.setBorrowCount(book.getBorrowCount() != null ? book.getBorrowCount() : 0);

            // 设置平均评分
            Double rating = bookRatingMap.get(book.getBookId());
            if (rating != null && rating > 0) {
                vo.setAverageRating(BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP));
            } else {
                vo.setAverageRating(BigDecimal.ZERO);
            }

            bookVOList.add(vo);
        }

        return bookVOList;
    }

    @Override
    public PageResult<QuestionVO> getQuestions(Long authorId, String status, Integer page, Integer size) {
        // 创建分页对象
        Page<AuthorQa> pageParam = new Page<>(page, size);

        // 构建查询条件
        QueryWrapper<AuthorQa> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);

        // 根据状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<AuthorQa> qaPage = authorQaMapper.selectPage(pageParam, queryWrapper);

        // 转换为VO对象
        List<QuestionVO> questionVOList = new ArrayList<>();
        for (AuthorQa qa : qaPage.getRecords()) {
            QuestionVO vo = new QuestionVO();
            vo.setQaId(qa.getQaId());
            vo.setUserId(qa.getUserId());
            vo.setQuestion(qa.getQuestion());
            vo.setAnswer(qa.getAnswer());
            vo.setStatus(qa.getStatus());
            vo.setIsPublic(qa.getIsPublic());
            vo.setCreatedAt(qa.getCreatedAt());
            vo.setAnsweredAt(qa.getAnsweredAt());

            // 获取用户名
            User user = userMapper.selectById(qa.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            questionVOList.add(vo);
        }

        // 返回分页结果
        PageResult<QuestionVO> pageResult = new PageResult<>();
        pageResult.setRecords(questionVOList);
        pageResult.setTotal(qaPage.getTotal());
        pageResult.setPage(qaPage.getCurrent());
        pageResult.setSize(qaPage.getSize());

        return pageResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void answerQuestion(Long authorId, QuestionReplyDTO dto) {
        // 查询问答记录
        AuthorQa qa = authorQaMapper.selectById(dto.getQaId());

        if (qa == null) {
            throw new BusinessException("问题不存在");
        }

        // 检查是否属于当前作者
        if (!qa.getAuthorId().equals(authorId)) {
            throw new BusinessException("无权回复此问题");
        }

        // 检查是否已回复
        if ("answered".equals(qa.getStatus())) {
            throw new BusinessException("问题已回复，不可重复回复");
        }

        // 更新回复内容
        qa.setAnswer(dto.getAnswer());
        qa.setStatus("answered");
        qa.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 1);
        qa.setAnsweredAt(LocalDateTime.now());

        authorQaMapper.updateById(qa);

        log.info("作者[{}]回复问题[{}]成功", authorId, dto.getQaId());
    }
}
