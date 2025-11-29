package com.library.module.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
