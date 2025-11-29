package com.library.module.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库导入结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultVO {
    
    /**
     * 总条数
     */
    private Integer totalCount;
    
    /**
     * 成功条数
     */
    private Integer successCount;
    
    /**
     * 失败条数
     */
    private Integer failedCount;
    
    /**
     * 错误详情列表
     */
    private List<ErrorDetail> errorDetails;
    
    /**
     * 错误详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        /**
         * 行号
         */
        private Integer rowNumber;
        
        /**
         * 问题内容
         */
        private String question;
        
        /**
         * 错误原因
         */
        private String errorMessage;
    }
    
    /**
     * 添加错误详情
     */
    public void addError(Integer rowNumber, String question, String errorMessage) {
        if (errorDetails == null) {
            errorDetails = new ArrayList<>();
        }
        errorDetails.add(new ErrorDetail(rowNumber, question, errorMessage));
        this.failedCount++;
    }
}
