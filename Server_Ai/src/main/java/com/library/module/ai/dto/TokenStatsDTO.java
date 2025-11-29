package com.library.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Token 统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatsDTO {
    
    /**
     * 总 Token 使用量
     */
    private TotalTokenStats totalStats;
    
    /**
     * 每日 Token 趋势（最近 30 天）
     */
    private List<DailyTokenStats> dailyTrend;
    
    /**
     * 用户 Token 排行（Top 10）
     */
    private List<UserTokenStats> userRanking;
    
    /**
     * 角色 Token 统计
     */
    private List<RoleTokenStats> roleStats;
    
    /**
     * 总 Token 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalTokenStats {
        /**
         * 总输入 Token
         */
        private Long totalInputTokens;
        
        /**
         * 总输出 Token
         */
        private Long totalOutputTokens;
        
        /**
         * 总 Token（输入+输出）
         */
        private Long totalTokens;
        
        /**
         * 总对话次数
         */
        private Long totalChats;
        
        /**
         * 平均每次对话 Token 数
         */
        private Double avgTokensPerChat;
        
        /**
         * 今日 Token 使用量
         */
        private Long todayTokens;
        
        /**
         * 昨日 Token 使用量
         */
        private Long yesterdayTokens;
        
        /**
         * 较昨日增长率（%）
         */
        private Double growthRate;
    }
    
    /**
     * 每日 Token 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTokenStats {
        /**
         * 日期（格式：yyyy-MM-dd）
         */
        private String date;
        
        /**
         * 当日 Token 使用量
         */
        private Long tokens;
        
        /**
         * 当日对话次数
         */
        private Long chats;
        
        /**
         * 当日平均每次对话 Token 数
         */
        private Double avgTokens;
    }
    
    /**
     * 用户 Token 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserTokenStats {
        /**
         * 用户 ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * Token 使用量
         */
        private Long tokens;
        
        /**
         * 对话次数
         */
        private Long chats;
        
        /**
         * 排名
         */
        private Integer rank;
    }
    
    /**
     * 角色 Token 统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleTokenStats {
        /**
         * 角色类型（reader/admin/author）
         */
        private String role;
        
        /**
         * 角色中文名称
         */
        private String roleName;
        
        /**
         * 该角色 Token 使用量
         */
        private Long tokens;
        
        /**
         * 该角色对话次数
         */
        private Long chats;
        
        /**
         * 该角色平均每次对话 Token 数
         */
        private Double avgTokens;
        
        /**
         * 该角色用户数
         */
        private Long userCount;
    }
}
