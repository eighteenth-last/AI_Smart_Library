package com.library.module.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.review.entity.Review;
import com.library.module.review.mapper.ReviewMapper;
import com.library.module.review.service.ReviewService;
import com.library.module.review.vo.ReviewVO;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    @Override
    public Page<ReviewVO> getAllReviews(Long userId, Long bookId, Integer rating, Integer page, Integer size) {
        Page<Review> reviewPage = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(userId != null, Review::getUserId, userId)
                .eq(bookId != null, Review::getBookId, bookId)
                .eq(rating != null, Review::getRating, rating)
                .orderByDesc(Review::getCreatedAt);

        Page<Review> resultPage = reviewMapper.selectPage(reviewPage, wrapper);
        return convertToVO(resultPage);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(reviewId);
    }

    @Override
    public ReviewStats getReviewStats() {
        ReviewStats stats = new ReviewStats();
        
        // 总数
        stats.total = reviewMapper.selectCount(null);
        
        // 各星级统计
        stats.fiveStar = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRating, 5));
        stats.fourStar = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRating, 4));
        stats.threeStar = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRating, 3));
        stats.twoStar = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRating, 2));
        stats.oneStar = reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getRating, 1));
        
        // 平均评分
        List<Review> allReviews = reviewMapper.selectList(null);
        if (!allReviews.isEmpty()) {
            double sum = allReviews.stream().mapToInt(Review::getRating).sum();
            stats.avgRating = sum / allReviews.size();
        } else {
            stats.avgRating = 0.0;
        }
        
        return stats;
    }

    @Override
    public Long createReview(Long userId, Long bookId, Integer rating, String content) {
        // 检查用户是否已经评价过该图书
        Long count = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getBookId, bookId)
        );
       

        // 创建评价
        Review review = new Review();
        review.setUserId(userId);
        review.setBookId(bookId);
        review.setRating(rating);
        review.setContent(content);
        
        reviewMapper.insert(review);
        return review.getReviewId();
    }

    @Override
    public Page<ReviewVO> getBookReviews(Long bookId, Integer rating, Integer page, Integer size) {
        Page<Review> reviewPage = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getBookId, bookId)
                .eq(rating != null, Review::getRating, rating)
                .orderByDesc(Review::getCreatedAt);

        Page<Review> resultPage = reviewMapper.selectPage(reviewPage, wrapper);
        return convertToVO(resultPage);
    }

    /**
     * 转换为VO
     */
    private Page<ReviewVO> convertToVO(Page<Review> reviewPage) {
        Page<ReviewVO> voPage = new Page<>();
        BeanUtils.copyProperties(reviewPage, voPage, "records");

        List<ReviewVO> voList = reviewPage.getRecords().stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(review, vo);

            // 查询用户名和nickname
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }

            // 查询图书信息
            Book book = bookMapper.selectById(review.getBookId());
            if (book != null) {
                vo.setBookTitle(book.getTitle());
                vo.setCoverUrl(book.getCoverUrl());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
