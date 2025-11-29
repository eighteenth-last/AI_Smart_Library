package com.library.module.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.result.Result;
import com.library.module.ai.dto.ChatRequestDTO;
import com.library.module.ai.dto.ChatMessageDTO;
import com.library.module.ai.entity.AiChatLog;
import com.library.module.ai.entity.AiSession;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.service.AiService;
import com.library.module.ai.service.AutoKnowledgeGenerateService;
import com.library.module.ai.service.ContextManagerService;
import com.library.module.ai.service.KnowledgeImportExportService;
import com.library.module.ai.task.AutoKnowledgeSyncTask;
import com.library.module.ai.vo.ChatResponseVO;
import com.library.module.ai.vo.ImportResultVO;
import com.library.module.ai.vo.SessionVO;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * AI智能客服控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI智能客服", description = "AI对话相关接口")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private KnowledgeImportExportService importExportService;
    
    @Autowired
    private ContextManagerService contextManager;
    
    @Autowired
    private AutoKnowledgeGenerateService autoKnowledgeGenerateService;
    
    @Autowired
    private AutoKnowledgeSyncTask autoKnowledgeSyncTask;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * AI对话
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI智能客服进行对话")
    public Result<ChatResponseVO> chat(@RequestBody @Valid ChatRequestDTO requestDTO) {
        // 设置用户ID（如果已登录）
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            requestDTO.setUserId(userId);
        }
        
        ChatResponseVO response = aiService.chat(requestDTO);
        return Result.success(response);
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history")
    @Operation(summary = "对话历史", description = "获取用户的AI对话历史")
    @SaCheckLogin
    public Result<Page<AiChatLog>> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<AiChatLog> result = aiService.getChatHistory(userId, sessionId, page, size);
        return Result.success(result);
    }

    /**
     * 添加知识库条目（管理员）
     */
    @PostMapping("/admin/knowledge")
    @Operation(summary = "添加知识库", description = "管理员添加知识库条目")
    @SaCheckLogin
    public Result<Boolean> addKnowledgeBase(@RequestBody KnowledgeBase knowledgeBase) {
        boolean result = aiService.addKnowledgeBase(knowledgeBase);
        return Result.success(result);
    }

    /**
     * 更新知识库条目（管理员）
     */
    @PutMapping("/admin/knowledge/{kbId}")
    @Operation(summary = "更新知识库", description = "管理员更新知识库条目")
    @SaCheckLogin
    public Result<Boolean> updateKnowledgeBase(
            @PathVariable("kbId") Long kbId,
            @RequestBody KnowledgeBase knowledgeBase) {
        boolean result = aiService.updateKnowledgeBase(kbId, knowledgeBase);
        return Result.success(result);
    }

    /**
     * 删除知识库条目（管理员）
     */
    @DeleteMapping("/admin/knowledge/{kbId}")
    @Operation(summary = "删除知识库", description = "管理员删除知识库条目")
    @SaCheckLogin
    public Result<Boolean> deleteKnowledgeBase(@PathVariable("kbId") Long kbId) {
        boolean result = aiService.deleteKnowledgeBase(kbId);
        return Result.success(result);
    }

    /**
     * 获取知识库列表（管理员）
     */
    @GetMapping("/admin/knowledge")
    @Operation(summary = "知识库列表", description = "管理员查看知识库列表")
    @SaCheckLogin
    public Result<Page<KnowledgeBase>> getKnowledgeBaseList(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<KnowledgeBase> result = aiService.getKnowledgeBaseList(category, page, size);
        return Result.success(result);
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "会话列表", description = "获取用户的AI对话会话列表")
    @SaCheckLogin
    public Result<List<SessionVO>> getUserSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SessionVO> result = aiService.getUserSessions(userId);
        return Result.success(result);
    }

    /**
     * 保存或更新会话
     */
    @PostMapping("/sessions")
    @Operation(summary = "保存会话", description = "保存或更新AI对话会话")
    @SaCheckLogin
    public Result<Boolean> saveSession(@RequestBody AiSession session) {
        Long userId = StpUtil.getLoginIdAsLong();
        session.setUserId(userId);
        
        // 获取用户角色
        User user = userMapper.selectById(userId);
        if (user != null) {
            session.setUserRole(user.getRole());
        }
        
        boolean result = aiService.saveOrUpdateSession(session);
        return Result.success(result);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定AI对话会话")
    @SaCheckLogin
    public Result<Boolean> deleteSession(@PathVariable("sessionId") String sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = aiService.deleteSession(sessionId, userId);
        return Result.success(result);
    }

    /**
     * 清空所有会话
     */
    @DeleteMapping("/sessions")
    @Operation(summary = "清空会话", description = "清空用户所有AI对话会话")
    @SaCheckLogin
    public Result<Boolean> clearAllSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = aiService.clearAllSessions(userId);
        return Result.success(result);
    }

    /**
     * 批量导入知识库（Excel/CSV）
     */
    @PostMapping("/admin/knowledge/import")
    @Operation(summary = "批量导入知识库", description = "管理员通过 Excel 或 CSV 批量导入知识库")
    @SaCheckLogin
    public Result<ImportResultVO> importKnowledge(@RequestParam("file") MultipartFile file) {
        try {
            ImportResultVO result = importExportService.importKnowledge(file);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error(500, "文件解析失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 导出知识库为 Excel
     */
    @GetMapping("/admin/knowledge/export/excel")
    @Operation(summary = "导出知识库为 Excel", description = "管理员导出知识库为 Excel 格式")
    @SaCheckLogin
    public void exportToExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String category) {
        try {
            importExportService.exportToExcel(response, category);
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出知识库为 CSV
     */
    @GetMapping("/admin/knowledge/export/csv")
    @Operation(summary = "导出知识库为 CSV", description = "管理员导出知识库为 CSV 格式")
    @SaCheckLogin
    public void exportToCSV(
            HttpServletResponse response,
            @RequestParam(required = false) String category) {
        try {
            importExportService.exportToCSV(response, category);
        } catch (IOException e) {
            throw new RuntimeException("导出 CSV 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取会话上下文（用于调试）
     */
    @GetMapping("/context/{sessionId}")
    @Operation(summary = "获取会话上下文", description = "获取指定会话的上下文信息")
    public Result<List<ChatMessageDTO>> getContext(@PathVariable("sessionId") String sessionId) {
        List<ChatMessageDTO> context = contextManager.getContext(sessionId);
        return Result.success(context);
    }
    
    /**
     * 获取会话上下文 Token 统计
     */
    @GetMapping("/context/{sessionId}/tokens")
    @Operation(summary = "获取上下文 Token 统计", description = "获取指定会话的上下文 Token 数量")
    public Result<Integer> getContextTokens(@PathVariable("sessionId") String sessionId) {
        int tokens = contextManager.getContextTokenCount(sessionId);
        return Result.success(tokens);
    }
    
    /**
     * 清空会话上下文
     */
    @DeleteMapping("/context/{sessionId}")
    @Operation(summary = "清空会话上下文", description = "清空指定会话的上下文历史")
    public Result<Boolean> clearContext(@PathVariable("sessionId") String sessionId) {
        contextManager.clearContext(sessionId);
        return Result.success(true);
    }
    
    // ==================== 知识库自动生成相关接口 ====================
    
    /**
     * 从单本图书生成知识库
     */
    @PostMapping("/admin/knowledge/generate/{bookId}")
    @Operation(summary = "从图书生成知识库", description = "管理员从单本图书自动生成知识库")
    @SaCheckLogin
    public Result<Integer> generateKnowledgeFromBook(@PathVariable("bookId") Long bookId) {
        int count = autoKnowledgeGenerateService.generateKnowledgeFromBook(bookId);
        return Result.success(count);
    }
    
    /**
     * 批量生成知识库
     */
    @PostMapping("/admin/knowledge/generate/batch")
    @Operation(summary = "批量生成知识库", description = "管理员批量从图书生成知识库")
    @SaCheckLogin
    public Result<Integer> batchGenerateKnowledge(@RequestBody List<Long> bookIds) {
        int count = autoKnowledgeGenerateService.batchGenerateKnowledge(bookIds);
        return Result.success(count);
    }
    
    /**
     * 从所有图书生成知识库
     */
    @PostMapping("/admin/knowledge/generate/all")
    @Operation(summary = "全量生成知识库", description = "管理员从所有上架图书生成知识库")
    @SaCheckLogin
    public Result<Integer> generateAllKnowledge() {
        int count = autoKnowledgeGenerateService.generateKnowledgeFromAllBooks();
        return Result.success(count);
    }
    
    /**
     * 更新图书知识库
     */
    @PutMapping("/admin/knowledge/generate/{bookId}")
    @Operation(summary = "更新图书知识库", description = "管理员更新指定图书的知识库")
    @SaCheckLogin
    public Result<Integer> updateBookKnowledge(@PathVariable("bookId") Long bookId) {
        int count = autoKnowledgeGenerateService.updateBookKnowledge(bookId);
        return Result.success(count);
    }
    
    /**
     * 删除图书知识库
     */
    @DeleteMapping("/admin/knowledge/generate/{bookId}")
    @Operation(summary = "删除图书知识库", description = "管理员删除指定图书的所有知识库")
    @SaCheckLogin
    public Result<Integer> deleteBookKnowledge(@PathVariable("bookId") Long bookId) {
        int count = autoKnowledgeGenerateService.deleteBookKnowledge(bookId);
        return Result.success(count);
    }
    
    /**
     * 手动触发知识库同步
     */
    @PostMapping("/admin/knowledge/sync")
    @Operation(summary = "手动同步知识库", description = "管理员手动触发知识库同步任务")
    @SaCheckLogin
    public Result<Integer> syncKnowledge() {
        int count = autoKnowledgeSyncTask.manualSync();
        return Result.success(count);
    }
}
