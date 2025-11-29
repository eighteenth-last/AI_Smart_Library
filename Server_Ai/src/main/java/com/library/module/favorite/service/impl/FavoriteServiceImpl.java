package com.library.module.favorite.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.favorite.entity.Favorite;
import com.library.module.favorite.mapper.FavoriteMapper;
import com.library.module.favorite.service.FavoriteService;
import com.library.module.favorite.vo.FavoriteVO;
import com.library.module.favorite.vo.FavoriteUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    
    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;
    
    @Override
    public void addFavorite(Long bookId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查图书是否存在
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        
        // 检查是否已收藏
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getBookId, bookId);
        Long count = favoriteMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new BusinessException("已收藏该图书");
        }
        
        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(bookId);
        favoriteMapper.insert(favorite);
    }
    
    @Override
    public void removeFavorite(Long bookId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getBookId, bookId);
        
        int deleted = favoriteMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException("未收藏该图书");
        }
    }
    
    @Override
    public PageResult<FavoriteVO> getMyFavorites(Long page, Long size) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询所有收藏（带图书信息）
        List<FavoriteVO> allFavorites = favoriteMapper.selectFavoriteListByUserId(userId);
        
        // 手动分页
        int total = allFavorites.size();
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + size.intValue(), total);
        
        List<FavoriteVO> pagedList = start < total ? 
            allFavorites.subList(start, end) : List.of();
        
        return PageResult.of(pagedList, (long) total, page, size);
    }
    
    @Override
    public boolean isFavorite(Long bookId) {
        if (!StpUtil.isLogin()) {
            return false;
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getBookId, bookId);
        
        return favoriteMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public PageResult<FavoriteUserVO> getBookFavorites(Long bookId, Long page, Long size) {
        // 查询收藏该图书的所有用户（带用户信息）
        List<FavoriteUserVO> allFavorites = favoriteMapper.selectFavoriteUsersByBookId(bookId);
        
        // 手动分页
        int total = allFavorites.size();
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + size.intValue(), total);
        
        List<FavoriteUserVO> pagedList = start < total ? 
            allFavorites.subList(start, end) : List.of();
        
        return PageResult.of(pagedList, (long) total, page, size);
    }
}
