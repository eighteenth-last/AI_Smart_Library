package com.library.module.ai.controller;

import com.library.common.result.Result;
import com.library.module.ai.dto.TokenStatsDTO;
import com.library.module.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Token 统计控制器（管理端）
 */
@Slf4j
@Tag(name = "Token 统计管理", description = "Token 使用统计和可视化接口")
@RestController
@RequestMapping("/admin/token-stats")
public class TokenStatsController {
    
    @Autowired
    private AiService aiService;
    
    /**
     * 获取 Token 统计数据
     *
     * @param days 统计天数（默认 30 天）
     * @return Token 统计数据
     */
    @Operation(summary = "获取 Token 统计数据", description = "获取总量、每日趋势、用户排行等统计数据")
    @GetMapping
    public Result<TokenStatsDTO> getTokenStats(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(required = false, defaultValue = "30") Integer days
    ) {
        log.info("查询 Token 统计数据，天数: {}", days);
        
        TokenStatsDTO stats = aiService.getTokenStats(days);
        
        return Result.success(stats);
    }
    
    /**
     * 获取总 Token 使用量
     *
     * @return 总 Token 统计
     */
    @Operation(summary = "获取总 Token 使用量", description = "获取所有时间的 Token 使用统计")
    @GetMapping("/total")
    public Result<TokenStatsDTO.TotalTokenStats> getTotalStats() {
        log.info("查询总 Token 使用量");
        
        TokenStatsDTO stats = aiService.getTokenStats(30);
        
        return Result.success(stats.getTotalStats());
    }
    
    /**
     * 获取每日 Token 趋势
     *
     * @param days 统计天数
     * @return 每日趋势数据
     */
    @Operation(summary = "获取每日 Token 趋势", description = "获取最近 N 天的 Token 使用趋势")
    @GetMapping("/daily-trend")
    public Result<java.util.List<TokenStatsDTO.DailyTokenStats>> getDailyTrend(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(required = false, defaultValue = "30") Integer days
    ) {
        log.info("查询每日 Token 趋势，天数: {}", days);
        
        TokenStatsDTO stats = aiService.getTokenStats(days);
        
        return Result.success(stats.getDailyTrend());
    }
    
    /**
     * 获取用户 Token 排行
     *
     * @return 用户排行榜
     */
    @Operation(summary = "获取用户 Token 排行", description = "获取 Token 使用量最多的用户 Top 10")
    @GetMapping("/user-ranking")
    public Result<java.util.List<TokenStatsDTO.UserTokenStats>> getUserRanking() {
        log.info("查询用户 Token 排行");
        
        TokenStatsDTO stats = aiService.getTokenStats(30);
        
        return Result.success(stats.getUserRanking());
    }
}
