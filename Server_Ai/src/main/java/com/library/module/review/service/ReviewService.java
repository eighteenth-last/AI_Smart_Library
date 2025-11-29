package com.library.module.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.module.review.vo.ReviewVO;

/**
 * 评价服务接口
 */
public interface ReviewService {

    /**
     * 获取所有评价（管理员）
     */
    Page<ReviewVO> getAllReviews(Long userId, Long bookId, Integer rating, Integer page, Integer size);

    /**
     * 删除评价
     */
    void deleteReview(Long reviewId);

    /**
     * 获取评价统计
     */
    ReviewStats getReviewStats();

    /**
     * 创建评价（用户）
     */
    Long createReview(Long userId, Long bookId, Integer rating, String content);

    /**
     * 获取图书评价列表（用户）
     */
    Page<ReviewVO> getBookReviews(Long bookId, Integer rating, Integer page, Integer size);

    /**
     * 评价统计VO
     */
    class ReviewStats {
        public Long total;
        public Long fiveStar;
        public Long fourStar;
        public Long threeStar;
        public Long twoStar;
        public Long oneStar;
        public Double avgRating;
    }
}
