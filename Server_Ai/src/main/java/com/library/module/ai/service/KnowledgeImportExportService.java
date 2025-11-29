package com.library.module.ai.service;

import com.library.module.ai.dto.KnowledgeImportDTO;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.vo.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 知识库导入导出服务接口
 */
public interface KnowledgeImportExportService {
    
    /**
     * 导入知识库（Excel 或 CSV）
     * 
     * @param file 上传的文件
     * @return 导入结果
     */
    ImportResultVO importKnowledge(MultipartFile file) throws IOException;
    
    /**
     * 导出知识库为 Excel
     * 
     * @param response HTTP 响应
     * @param category 分类（可选）
     */
    void exportToExcel(HttpServletResponse response, String category) throws IOException;
    
    /**
     * 导出知识库为 CSV
     * 
     * @param response HTTP 响应
     * @param category 分类（可选）
     */
    void exportToCSV(HttpServletResponse response, String category) throws IOException;
}
