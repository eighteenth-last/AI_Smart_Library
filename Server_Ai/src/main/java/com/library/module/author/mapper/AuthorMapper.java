package com.library.module.author.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.author.entity.Author;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作者Mapper接口
 */
@Mapper
public interface AuthorMapper extends BaseMapper<Author> {
    // 所有查询方法已移至Service层使用QueryWrapper实现
}
