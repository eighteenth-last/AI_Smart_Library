package com.library.module.favorite.service;

import com.library.common.result.PageResult;
import com.library.module.favorite.vo.FavoriteVO;
import com.library.module.favorite.vo.FavoriteUserVO;

/**
 * 收藏服务接口
 */
public interface FavoriteService {
    
    /**
     * 添加收藏
     * @param bookId 图书ID
     */
    void addFavorite(Long bookId);
    
    /**
     * 取消收藏
     * @param bookId 图书ID
     */
    void removeFavorite(Long bookId);
    
    /**
     * 获取我的收藏列表
     * @param page 页码
     * @param size 每页数量
     * @return 收藏列表
     */
    PageResult<FavoriteVO> getMyFavorites(Long page, Long size);
    
    /**
     * 检查是否已收藏
     * @param bookId 图书ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long bookId);
    
    /**
     * 获取收藏某本图书的用户列表
     * @param bookId 图书ID
     * @param page 页码
     * @param size 每页数量
     * @return 收藏用户列表
     */
    PageResult<FavoriteUserVO> getBookFavorites(Long bookId, Long page, Long size);
}
