package com.library.module.author.service;

import com.library.common.result.PageResult;
import com.library.module.author.dto.QuestionReplyDTO;
import com.library.module.author.vo.AuthorBookVO;
import com.library.module.author.vo.AuthorStatsVO;
import com.library.module.author.vo.QuestionVO;

import java.util.List;

/**
 * 作者服务接口
 */
public interface AuthorService {

    /**
     * 获取作者数据统计
     *
     * @param authorId 作者ID
     * @return 作者数据统计
     */
    AuthorStatsVO getAuthorStats(Long authorId);

    /**
     * 获取作者作品列表
     *
     * @param authorId 作者ID
     * @param keyword 关键词（模糊查询ISBN或书名）
     * @return 作品列表
     */
    List<AuthorBookVO> getAuthorBooks(Long authorId, String keyword);

    /**
     * 获取读者提问列表（分页）
     *
     * @param authorId 作者ID
     * @param status 状态筛选：pending/answered
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<QuestionVO> getQuestions(Long authorId, String status, Integer page, Integer size);

    /**
     * 回复读者提问
     *
     * @param authorId 作者ID
     * @param dto 回复内容
     */
    void answerQuestion(Long authorId, QuestionReplyDTO dto);
}
