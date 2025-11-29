package com.library.module.dashboard.vo;

import lombok.Data;

/**
 * 最近操作VO
 */
@Data
public class RecentActivityVO {
    
    /**
     * 操作ID
     */
    private Long id;
    
    /**
     * 操作类型：info/success/warning/error
     */
    private String type;
    
    /**
     * 操作标题
     */
    private String title;
    
    /**
     * 操作内容描述
     */
    private String content;
    
    /**
     * 操作时间
     */
    private String time;
}
