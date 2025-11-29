package com.library.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.config.AutoKnowledgeConfig;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.mapper.KnowledgeBaseMapper;
import com.library.module.ai.service.AutoKnowledgeGenerateService;
import com.library.module.ai.service.VectorService;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.book.vo.BookDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库自动生成服务实现
 */
@Slf4j
@Service
public class AutoKnowledgeGenerateServiceImpl implements AutoKnowledgeGenerateService {
    
    @Autowired
    private BookMapper bookMapper;
    
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    
    @Autowired(required = false)
    private VectorService vectorService;
    
    @Autowired
    private AutoKnowledgeConfig config;
    
    private static final String CATEGORY = "图书信息";
    private static final String SOURCE_PREFIX = "book_";
    
    @Override
    @Transactional
    public int generateKnowledgeFromBook(Long bookId) {
        log.info("开始为图书生成知识库 - bookId: {}", bookId);
        
        // 查询图书详情
        BookDetailVO book = bookMapper.selectBookDetailById(bookId);
        if (book == null) {
            log.warn("图书不存在 - bookId: {}", bookId);
            return 0;
        }
        
        // 检查是否满足生成条件
        if (!isEligibleForGeneration(book)) {
            log.info("图书不满足生成条件 - bookId: {}, title: {}", bookId, book.getTitle());
            return 0;
        }
        
        // 删除旧的知识库（如果存在）
        deleteBookKnowledge(bookId);
        
        // 生成知识库问答对
        List<KnowledgeBase> knowledgeList = generateKnowledgePairs(book);
        
        // 批量插入知识库
        int count = 0;
        for (KnowledgeBase knowledge : knowledgeList) {
            try {
                knowledgeBaseMapper.insert(knowledge);
                count++;
                
                // 生成向量（如果配置了向量服务）
                if (config.getVectorCache().getEnabled() && vectorService != null) {
                    generateEmbedding(knowledge);
                }
            } catch (Exception e) {
                log.error("插入知识库失败 - question: {}, error: {}", 
                    knowledge.getQuestion(), e.getMessage());
            }
        }
        
        log.info("图书知识库生成完成 - bookId: {}, count: {}", bookId, count);
        return count;
    }
    
    @Override
    @Transactional
    public int batchGenerateKnowledge(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return 0;
        }
        
        log.info("批量生成知识库 - bookIds: {}", bookIds.size());
        int totalCount = 0;
        
        for (Long bookId : bookIds) {
            try {
                int count = generateKnowledgeFromBook(bookId);
                totalCount += count;
            } catch (Exception e) {
                log.error("生成知识库失败 - bookId: {}, error: {}", bookId, e.getMessage());
            }
        }
        
        log.info("批量生成完成 - total: {}", totalCount);
        return totalCount;
    }
    
    @Override
    @Transactional
    public int generateKnowledgeFromAllBooks() {
        log.info("开始从所有上架图书生成知识库");
        
        // 查询所有上架图书
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1);
        List<Book> books = bookMapper.selectList(wrapper);
        
        log.info("找到上架图书 {} 本", books.size());
        
        // 批量处理
        int batchSize = config.getBookSync().getBatchSize();
        int totalCount = 0;
        List<Long> batch = new ArrayList<>();
        
        for (Book book : books) {
            batch.add(book.getBookId());
            
            if (batch.size() >= batchSize) {
                totalCount += batchGenerateKnowledge(batch);
                batch.clear();
            }
        }
        
        // 处理剩余的
        if (!batch.isEmpty()) {
            totalCount += batchGenerateKnowledge(batch);
        }
        
        log.info("全量生成完成 - total: {}", totalCount);
        return totalCount;
    }
    
    @Override
    @Transactional
    public int updateBookKnowledge(Long bookId) {
        log.info("更新图书知识库 - bookId: {}", bookId);
        
        // 先删除再生成
        deleteBookKnowledge(bookId);
        return generateKnowledgeFromBook(bookId);
    }
    
    @Override
    @Transactional
    public int deleteBookKnowledge(Long bookId) {
        String source = SOURCE_PREFIX + bookId;
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getSource, source);
        
        int count = knowledgeBaseMapper.delete(wrapper);
        log.info("删除图书知识库 - bookId: {}, count: {}", bookId, count);
        return count;
    }
    
    /**
     * 检查图书是否满足生成条件
     */
    private boolean isEligibleForGeneration(BookDetailVO book) {
        // 必须是上架状态
        if (book.getStatus() == null || book.getStatus() != 1) {
            return false;
        }
        
        // 必须有简介
        if (!StringUtils.hasText(book.getDescription())) {
            return false;
        }
        
        // 简介长度至少20字符
        if (book.getDescription().length() < 20) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成知识库问答对列表
     */
    private List<KnowledgeBase> generateKnowledgePairs(BookDetailVO book) {
        List<KnowledgeBase> list = new ArrayList<>();
        String title = book.getTitle();
        String source = SOURCE_PREFIX + book.getBookId();
        
        // 1. 图书简介（必选）
        list.add(createKnowledge(
            String.format("《%s》讲的是什么？", title),
            formatBookDescription(book),
            source
        ));
        
        // 2. 作者信息（必选）
        if (book.getAuthor() != null && StringUtils.hasText(book.getAuthor().getName())) {
            list.add(createKnowledge(
                String.format("《%s》是谁写的？", title),
                formatAuthorInfo(book),
                source
            ));
        }
        
        // 3. 库存信息（必选）
        list.add(createKnowledge(
            String.format("《%s》还有书吗？", title),
            formatStockInfo(book),
            source
        ));
        
        // 4. 出版信息（可选）
        if (book.getPublisher() != null && book.getPublishYear() != null) {
            list.add(createKnowledge(
                String.format("《%s》什么时候出版的？", title),
                formatPublishInfo(book),
                source
            ));
        }
        
        // 5. 分类信息（可选）
        if (book.getCategory() != null && StringUtils.hasText(book.getCategory().getName())) {
            list.add(createKnowledge(
                String.format("《%s》属于什么分类？", title),
                formatCategoryInfo(book),
                source
            ));
        }
        
        // 6. 评分信息（可选）
        if (book.getAverageRating() != null && book.getAverageRating() > 0) {
            list.add(createKnowledge(
                String.format("《%s》评分多少？", title),
                formatRatingInfo(book),
                source
            ));
        }
        
        // 7. ISBN（可选）
        if (StringUtils.hasText(book.getIsbn())) {
            list.add(createKnowledge(
                String.format("《%s》的ISBN是多少？", title),
                String.format("《%s》的ISBN是%s。", title, book.getIsbn()),
                source
            ));
        }
        
        return list;
    }
    
    /**
     * 创建知识库对象
     */
    private KnowledgeBase createKnowledge(String question, String answer, String source) {
        KnowledgeBase knowledge = new KnowledgeBase();
        knowledge.setQuestion(question);
        knowledge.setAnswer(answer);
        knowledge.setCategory(CATEGORY);
        knowledge.setSource(source);
        knowledge.setIsPublic(1);
        knowledge.setHitCount(0);
        knowledge.setCreatedAt(LocalDateTime.now());
        knowledge.setUpdatedAt(LocalDateTime.now());
        return knowledge;
    }
    
    /**
     * 格式化图书简介
     */
    private String formatBookDescription(BookDetailVO book) {
        return String.format("《%s》%s", book.getTitle(), book.getDescription());
    }
    
    /**
     * 格式化作者信息
     */
    private String formatAuthorInfo(BookDetailVO book) {
        return String.format("《%s》的作者是%s。", book.getTitle(), book.getAuthor().getName());
    }
    
    /**
     * 格式化库存信息
     */
    private String formatStockInfo(BookDetailVO book) {
        int available = book.getAvailableStock() != null ? book.getAvailableStock() : 0;
        int total = book.getTotalStock() != null ? book.getTotalStock() : 0;
        
        if (available > 0) {
            return String.format("目前《%s》有%d本可借，总馆藏%d本。", 
                book.getTitle(), available, total);
        } else {
            return String.format("抱歉，《%s》暂时没有可借图书，总馆藏%d本，请稍后再试或预约借阅。", 
                book.getTitle(), total);
        }
    }
    
    /**
     * 格式化出版信息
     */
    private String formatPublishInfo(BookDetailVO book) {
        return String.format("《%s》由%s于%d年出版。", 
            book.getTitle(), book.getPublisher(), book.getPublishYear());
    }
    
    /**
     * 格式化分类信息
     */
    private String formatCategoryInfo(BookDetailVO book) {
        return String.format("《%s》属于%s类图书。", 
            book.getTitle(), book.getCategory().getName());
    }
    
    /**
     * 格式化评分信息
     */
    private String formatRatingInfo(BookDetailVO book) {
        double rating = book.getAverageRating();
        int reviewCount = book.getReviewCount() != null ? book.getReviewCount() : 0;
        
        String evaluation;
        if (rating >= 4.5) {
            evaluation = "非常受读者欢迎";
        } else if (rating >= 4.0) {
            evaluation = "很受读者欢迎";
        } else if (rating >= 3.5) {
            evaluation = "较受读者欢迎";
        } else {
            evaluation = "口碑一般";
        }
        
        return String.format("《%s》的平均评分是%.1f分（基于%d条评价），%s。", 
            book.getTitle(), rating, reviewCount, evaluation);
    }
    
    /**
     * 生成向量
     */
    private void generateEmbedding(KnowledgeBase knowledge) {
        try {
            String text = knowledge.getQuestion() + " " + knowledge.getAnswer();
            List<Double> embedding = vectorService.embedText(text);
            
            // 注意：KnowledgeBase 实体没有 embedding 字段
            // 向量数据存储在单独的 knowledge_vector 表中
            // 这里仅作示例，实际需要调用 VectorService 存储向量
            
            log.debug("向量生成成功 - kbId: {}", knowledge.getKbId());
        } catch (Exception e) {
            log.warn("向量生成失败 - kbId: {}, error: {}", 
                knowledge.getKbId(), e.getMessage());
        }
    }
}
