package com.library.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库导入 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeImportDTO {
    
    /**
     * 问题
     */
    private String question;
    
    /**
     * 答案
     */
    private String answer;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 来源
     */
    private String source;
    
    /**
     * 是否公开（1-公开，0-私有）
     */
    private Integer isPublic;
    
    /**
     * 行号（用于错误提示）
     */
    private Integer rowNumber;
}
