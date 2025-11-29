package com.library.module.book.service.impl;

import com.library.module.book.entity.Book;
import com.library.module.book.entity.BookTag;
import com.library.module.tag.entity.Tag;
import com.library.module.book.mapper.BookMapper;
import com.library.module.book.mapper.BookTagMapper;
import com.library.module.tag.mapper.TagMapper;
import com.library.module.book.service.RecommendService;
import com.library.module.book.vo.BookRecommendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐算法服务实现类
 * 基于Jaccard相似度计算相似图书
 */
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final BookMapper bookMapper;
    private final BookTagMapper bookTagMapper;
    private final TagMapper tagMapper;

    @Override
    public List<BookRecommendVO> getSimilarBooksByJaccard(Long bookId, Integer limit) {
        // 获取目标图书的标签集合
        Set<Long> targetTags = getBookTagIds(bookId);
        if (targetTags.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有其他图书
        List<Book> allBooks = bookMapper.selectAllBooks();

        // 计算每本书与目标图书的相似度
        List<BookRecommendVO> recommendations = new ArrayList<>();

        for (Book book : allBooks) {
            // 跳过目标图书本身
            if (book.getBookId().equals(bookId)) {
                continue;
            }

            // 获取比较图书的标签集合
            Set<Long> compareTags = getBookTagIds(book.getBookId());
            if (compareTags.isEmpty()) {
                continue;
            }

            // 计算Jaccard相似度
            double similarity = calculateJaccardSimilarity(targetTags, compareTags);

            // 只推荐相似度大于0的图书
            if (similarity > 0) {
                BookRecommendVO vo = new BookRecommendVO();
                vo.setBookId(book.getBookId());
                vo.setTitle(book.getTitle());
                vo.setCoverUrl(book.getCoverUrl());

                // 计算共享标签
                Set<Long> sharedTags = new HashSet<>(targetTags);
                sharedTags.retainAll(compareTags);

                vo.setSimilarity(similarity);
                vo.setSimilarityPercent(String.format("%.0f%%", similarity * 100));
                vo.setReason(generateRecommendationReason(sharedTags));

                // 获取共享标签名称
                String[] sharedTagNames = tagMapper.selectBatchIds(sharedTags)
                    .stream()
                    .map(Tag::getName)
                    .toArray(String[]::new);
                vo.setSharedTags(sharedTagNames);

                recommendations.add(vo);
            }
        }

        // 按相似度降序排序，限制返回数量
        return recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(limit != null && limit > 0 ? limit : 5)
                .collect(Collectors.toList());
    }

    @Override
    public double calculateJaccardSimilarity(Set<Long> tags1, Set<Long> tags2) {
        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        // 计算并集
        Set<Long> union = new HashSet<>(tags1);
        union.addAll(tags2);

        // Jaccard相似度公式: |A ∩ B| / |A ∪ B|
        return (double) intersection.size() / union.size();
    }

    @Override
    public Set<Long> getBookTagIds(Long bookId) {
        List<BookTag> bookTags = bookTagMapper.selectByBookId(bookId);
        return bookTags.stream()
                .map(BookTag::getTagId)
                .collect(Collectors.toSet());
    }

    @Override
    public String generateRecommendationReason(Set<Long> sharedTags) {
        if (sharedTags.isEmpty()) {
            return "暂无共同标签";
        }

        List<String> tagNames = tagMapper.selectBatchIds(sharedTags)
                .stream()
                .map(Tag::getName)
                .sorted() // 排序保证结果一致性
                .collect(Collectors.toList());

        return "因为共享标签：[\"" + String.join("\", \"", tagNames) + "\"]";
    }
}