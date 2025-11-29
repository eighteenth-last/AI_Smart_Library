package com.library.module.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.favorite.entity.Favorite;
import com.library.module.favorite.vo.FavoriteVO;
import com.library.module.favorite.vo.FavoriteUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    
    /**
     * 查询用户收藏列表（带图书信息）
     */
    @Select("""
            SELECT 
                f.fav_id,
                f.book_id,
                b.title AS book_title,
                a.name AS author_name,
                b.cover_url,
                DATE_FORMAT(f.created_at, '%Y-%m-%dT%H:%i:%s') AS created_at
            FROM favorite f
            INNER JOIN book b ON f.book_id = b.book_id
            LEFT JOIN author a ON b.author_id = a.author_id
            WHERE f.user_id = #{userId}
            ORDER BY f.created_at DESC
            """)
    List<FavoriteVO> selectFavoriteListByUserId(@Param("userId") Long userId);
    
    /**
     * 查询收藏某本图书的用户列表（带用户信息）
     */
    @Select("""
            SELECT 
                f.fav_id,
                f.user_id,
                u.username,
                u.role,
                f.created_at
            FROM favorite f
            INNER JOIN user u ON f.user_id = u.user_id
            WHERE f.book_id = #{bookId}
            ORDER BY f.created_at DESC
            """)
    List<FavoriteUserVO> selectFavoriteUsersByBookId(@Param("bookId") Long bookId);
}
