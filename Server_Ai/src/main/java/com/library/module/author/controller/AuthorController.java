package com.library.module.author.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.library.common.result.PageResult;
import com.library.common.result.Result;
import com.library.module.author.dto.QuestionReplyDTO;
import com.library.module.author.entity.Author;
import com.library.module.author.mapper.AuthorMapper;
import com.library.module.author.service.AuthorService;
import com.library.module.author.vo.AuthorBookVO;
import com.library.module.author.vo.AuthorStatsVO;
import com.library.module.author.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者控制器
 */
@RestController
@RequestMapping("/author")
@RequiredArgsConstructor
@Tag(name = "作者模块", description = "作者相关接口")
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    /**
     * 获取作者数据统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取作者数据统计", description = "获取当前作者账号下所有作品的总数据统计")
    @SaCheckLogin
    public Result<AuthorStatsVO> getAuthorStats() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 使用QueryWrapper查询作者信息
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("deleted", 0);
        
        Author author = authorMapper.selectOne(queryWrapper);
        
        if (author == null) {
            // 新作者，返回默认空数据
            AuthorStatsVO stats = new AuthorStatsVO();
            stats.setTotalBooks(0);
            stats.setTotalBorrows(0);
            stats.setTotalReviews(0);
            stats.setAverageRating(BigDecimal.ZERO);
            stats.setPendingQuestions(0);
            stats.setTopBooks(new ArrayList<>());
            stats.setMonthlyTrends(new ArrayList<>());
            return Result.success(stats);
        }
        
        AuthorStatsVO stats = authorService.getAuthorStats(author.getAuthorId());
        return Result.success(stats);
    }

    /**
     * 获取作者作品列表
     */
    @GetMapping("/books")
    @Operation(summary = "获取作者作品列表", description = "获取当前作者账号下的所有作品，支持按ISBN/书名模糊查询")
    @SaCheckLogin
    public Result<List<AuthorBookVO>> getAuthorBooks(
            @RequestParam(required = false) String keyword) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 使用QueryWrapper查询作者信息
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("deleted", 0);
        
        Author author = authorMapper.selectOne(queryWrapper);
        
        if (author == null) {
            // 新作者，返回空列表
            return Result.success(new ArrayList<>());
        }
        
        List<AuthorBookVO> books = authorService.getAuthorBooks(author.getAuthorId(), keyword);
        return Result.success(books);
    }

    /**
     * 获取读者提问列表（分页）
     */
    @GetMapping("/questions")
    @Operation(summary = "获取读者提问列表", description = "分页获取针对当前作者的读者提问")
    @SaCheckLogin
    public Result<PageResult<QuestionVO>> getQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("deleted", 0);
        
        Author author = authorMapper.selectOne(queryWrapper);
        
        if (author == null) {
            // 新作者，返回空分页结果
            PageResult<QuestionVO> emptyResult = new PageResult<>();
            emptyResult.setRecords(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage((long) page);
            emptyResult.setSize((long) size);
            return Result.success(emptyResult);
        }
        
        PageResult<QuestionVO> pageResult = authorService.getQuestions(author.getAuthorId(), status, page, size);
        return Result.success(pageResult);
    }

    /**
     * 回复读者提问
     */
    @PostMapping("/answer")
    @Operation(summary = "回复读者提问", description = "作者回复读者的提问")
    @SaCheckLogin
    public Result<Void> answerQuestion(@Valid @RequestBody QuestionReplyDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("deleted", 0);
        
        Author author = authorMapper.selectOne(queryWrapper);
        
        if (author == null) {
            return Result.error(404, "未找到作者信息");
        }
        
        authorService.answerQuestion(author.getAuthorId(), dto);
        return Result.success();
    }
}
