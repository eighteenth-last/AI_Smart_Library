package com.library.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.library.module.author.entity.Author;
import com.library.module.author.mapper.AuthorMapper;
import com.library.module.borrow.entity.BorrowRecord;
import com.library.module.borrow.mapper.BorrowRecordMapper;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.category.mapper.CategoryMapper;
import com.library.module.dashboard.service.DashboardService;
import com.library.module.dashboard.vo.*;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据可视化看板服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final CategoryMapper categoryMapper;
    private final AuthorMapper authorMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public OverviewVO getOverview() {
        log.info("获取系统总览数据");
        OverviewVO overviewVO = new OverviewVO();

        try {
            // 1. 用户总数
            Long totalUsers = userMapper.selectCount(null);
            overviewVO.setTotalUsers(totalUsers != null ? totalUsers : 0L);

            // 2. 图书总数
            Long totalBooks = bookMapper.selectCount(null);
            overviewVO.setTotalBooks(totalBooks != null ? totalBooks : 0L);

            // 3. 借阅总数
            Long totalBorrows = borrowRecordMapper.selectCount(null);
            overviewVO.setTotalBorrows(totalBorrows != null ? totalBorrows : 0L);

            // 4. 当前借阅中数量
            Long activeBorrows = borrowRecordMapper.selectCount(
                    Wrappers.<BorrowRecord>lambdaQuery()
                            .eq(BorrowRecord::getStatus, "borrowed")
            );
            overviewVO.setActiveBorrows(activeBorrows != null ? activeBorrows : 0L);

            // 5. 逾期数量
            Long overdueCount = borrowRecordMapper.selectCount(
                    Wrappers.<BorrowRecord>lambdaQuery()
                            .eq(BorrowRecord::getStatus, "overdue")
            );
            overviewVO.setOverdueCount(overdueCount != null ? overdueCount : 0L);

            // 6. 今日新增用户
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            Long todayNewUsers = userMapper.selectCount(
                    Wrappers.<User>lambdaQuery()
                            .ge(User::getCreatedAt, todayStart)
            );
            overviewVO.setTodayNewUsers(todayNewUsers != null ? todayNewUsers : 0L);

            // 7. 今日新增借阅
            Long todayNewBorrows = borrowRecordMapper.selectCount(
                    Wrappers.<BorrowRecord>lambdaQuery()
                            .ge(BorrowRecord::getBorrowTime, todayStart)
            );
            overviewVO.setTodayNewBorrows(todayNewBorrows != null ? todayNewBorrows : 0L);

            log.info("系统总览数据: 用户={}, 图书={}, 借阅={}, 活跃={}, 逾期={}", 
                    overviewVO.getTotalUsers(), overviewVO.getTotalBooks(), 
                    overviewVO.getTotalBorrows(), overviewVO.getActiveBorrows(), 
                    overviewVO.getOverdueCount());

        } catch (Exception e) {
            log.error("获取系统总览数据失败", e);
            // 返回默认值
            overviewVO.setTotalUsers(0L);
            overviewVO.setTotalBooks(0L);
            overviewVO.setTotalBorrows(0L);
            overviewVO.setActiveBorrows(0L);
            overviewVO.setOverdueCount(0L);
            overviewVO.setTodayNewUsers(0L);
            overviewVO.setTodayNewBorrows(0L);
        }

        return overviewVO;
    }

    @Override
    public List<HotBookVO> getHotBooks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        log.info("获取热门图书TOP{}", limit);
        List<HotBookVO> hotBookList = new ArrayList<>();

        try {
            // 查询借阅次数最多的图书
            List<Map<String, Object>> borrowStats = borrowRecordMapper.selectMaps(
                    Wrappers.<BorrowRecord>query()
                            .select("book_id", "COUNT(*) as borrow_count")
                            .groupBy("book_id")
                            .orderByDesc("borrow_count")
                            .last("LIMIT " + limit)
            );

            int rank = 1;
            for (Map<String, Object> stat : borrowStats) {
                try {
                    Long bookId = Long.valueOf(stat.get("book_id").toString());
                    Integer borrowCount = Integer.valueOf(stat.get("borrow_count").toString());

                    // 查询图书信息
                    Book book = bookMapper.selectById(bookId);
                    if (book != null) {
                        HotBookVO hotBookVO = new HotBookVO();
                        hotBookVO.setBookId(bookId);
                        hotBookVO.setTitle(book.getTitle());
                        
                        // 查询作者名称
                        String authorName = "未知作者";
                        if (book.getAuthorId() != null) {
                            Author author = authorMapper.selectById(book.getAuthorId());
                            if (author != null) {
                                authorName = author.getName();
                            }
                        }
                        hotBookVO.setAuthorName(authorName);
                        hotBookVO.setBorrowCount(borrowCount);
                        hotBookVO.setRank(rank++);
                        
                        hotBookList.add(hotBookVO);
                    }
                } catch (Exception e) {
                    log.error("处理热门图书数据失败: {}", stat, e);
                }
            }

            log.info("成功获取{}本热门图书", hotBookList.size());

        } catch (Exception e) {
            log.error("获取热门图书失败", e);
        }

        return hotBookList;
    }

    @Override
    public List<CategoryStatsVO> getCategoryStats() {
        log.info("获取分类统计数据");
        List<CategoryStatsVO> statsList = new ArrayList<>();

        try {
            // 获取总借阅数
            Long totalBorrows = borrowRecordMapper.selectCount(null);
            if (totalBorrows == null || totalBorrows == 0) {
                log.warn("暂无借阅数据");
                return statsList;
            }

            // 查询所有分类的借阅统计
            // 注意：这里需要使用原生SQL，因为涉及多表JOIN和GROUP BY
            // 使用@Select注解或XML mapper会更好，这里为了简化直接使用原生SQL
            List<Map<String, Object>> categoryStats = borrowRecordMapper.selectMaps(
                    Wrappers.<BorrowRecord>query()
                            .apply("SELECT b.category_id, c.name as category_name, COUNT(*) as borrow_count " +
                                  "FROM borrow_record br " +
                                  "INNER JOIN book b ON br.book_id = b.book_id " +
                                  "INNER JOIN category c ON b.category_id = c.category_id " +
                                  "GROUP BY b.category_id, c.name " +
                                  "ORDER BY borrow_count DESC")
            );

            for (Map<String, Object> stat : categoryStats) {
                try {
                    CategoryStatsVO statsVO = new CategoryStatsVO();
                    statsVO.setCategoryId(Long.valueOf(stat.get("category_id").toString()));
                    statsVO.setCategoryName(stat.get("category_name").toString());
                    statsVO.setBorrowCount(Integer.valueOf(stat.get("borrow_count").toString()));

                    // 计算占比
                    double percentage = (statsVO.getBorrowCount() * 100.0) / totalBorrows;
                    statsVO.setPercentage(String.format("%.1f%%", percentage));

                    statsList.add(statsVO);
                } catch (Exception e) {
                    log.error("处理分类统计数据失败: {}", stat, e);
                }
            }

            log.info("成功获取{}个分类的统计数据", statsList.size());

        } catch (Exception e) {
            log.error("获取分类统计失败", e);
        }

        return statsList;
    }

    @Override
    public List<BorrowTrendVO> getBorrowTrends(String period, Integer limit) {
        if (period == null || period.isEmpty()) {
            period = "month";
        }
        if (limit == null || limit <= 0) {
            limit = 12;
        }

        log.info("获取借阅趋势: period={}, limit={}", period, limit);
        List<BorrowTrendVO> trendList = new ArrayList<>();

        try {
            // 根据不同的period设置不同的时间格式
            String dateFormat;
            String intervalUnit;
            
            switch (period.toLowerCase()) {
                case "day":
                    dateFormat = "%Y-%m-%d";
                    intervalUnit = "DAY";
                    break;
                case "week":
                    dateFormat = "%Y-W%v";
                    intervalUnit = "WEEK";
                    break;
                case "year":
                    dateFormat = "%Y";
                    intervalUnit = "YEAR";
                    break;
                case "month":
                default:
                    dateFormat = "%Y-%m";
                    intervalUnit = "MONTH";
                    break;
            }

            // 使用JdbcTemplate执行原生SQL
            String sql = String.format(
                    "SELECT DATE_FORMAT(borrow_time, '%s') as period, " +
                    "COUNT(*) as borrow_count, " +
                    "SUM(CASE WHEN status = 'returned' THEN 1 ELSE 0 END) as return_count " +
                    "FROM borrow_record " +
                    "WHERE deleted = 0 AND borrow_time >= DATE_SUB(NOW(), INTERVAL %d %s) " +
                    "GROUP BY DATE_FORMAT(borrow_time, '%s') " +
                    "ORDER BY period",
                    dateFormat, limit, intervalUnit, dateFormat
            );
            
            List<Map<String, Object>> trendData = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> data : trendData) {
                try {
                    BorrowTrendVO trendVO = new BorrowTrendVO();
                    trendVO.setPeriod(data.get("period").toString());
                    trendVO.setBorrowCount(Integer.valueOf(data.get("borrow_count").toString()));
                    trendVO.setReturnCount(Integer.valueOf(data.get("return_count").toString()));
                    trendList.add(trendVO);
                } catch (Exception e) {
                    log.error("处理趋势数据失败: {}", data, e);
                }
            }

            log.info("成功获取{}条趋势数据", trendList.size());

        } catch (Exception e) {
            log.error("获取借阅趋势失败", e);
        }

        return trendList;
    }

    @Override
    public AuthorStatsVO getAuthorStats(Long authorId) {
        log.info("获取作者统计数据: authorId={}", authorId);
        AuthorStatsVO authorStatsVO = new AuthorStatsVO();

        try {
            // 1. 作品总数
            Long totalBooks = bookMapper.selectCount(
                    Wrappers.<Book>lambdaQuery()
                            .eq(Book::getAuthorId, authorId)
            );
            authorStatsVO.setTotalBooks(totalBooks != null ? totalBooks.intValue() : 0);

            // 2. 查询该作者所有图书的ID
            List<Book> authorBooks = bookMapper.selectList(
                    Wrappers.<Book>lambdaQuery()
                            .eq(Book::getAuthorId, authorId)
                            .select(Book::getBookId)
            );

            if (!authorBooks.isEmpty()) {
                List<Long> bookIds = authorBooks.stream()
                        .map(Book::getBookId)
                        .toList();

                // 3. 总借阅量
                Long totalBorrows = borrowRecordMapper.selectCount(
                        Wrappers.<BorrowRecord>lambdaQuery()
                                .in(BorrowRecord::getBookId, bookIds)
                );
                authorStatsVO.setTotalBorrows(totalBorrows != null ? totalBorrows.intValue() : 0);

                // 4. 总评价数（需要review表，暂时设为0）
                authorStatsVO.setTotalReviews(0);

                // 5. 平均评分（需要review表，暂时设为0）
                authorStatsVO.setAverageRating(0.0);

                // 6. 作品列表（简化处理，只返回基本信息）
                authorStatsVO.setBooks(new ArrayList<>());
            } else {
                authorStatsVO.setTotalBorrows(0);
                authorStatsVO.setTotalReviews(0);
                authorStatsVO.setAverageRating(0.0);
                authorStatsVO.setBooks(new ArrayList<>());
            }

            log.info("作者统计: 作品数={}, 借阅量={}", 
                    authorStatsVO.getTotalBooks(), authorStatsVO.getTotalBorrows());

        } catch (Exception e) {
            log.error("获取作者统计失败: authorId={}", authorId, e);
            // 返回默认值
            authorStatsVO.setTotalBooks(0);
            authorStatsVO.setTotalBorrows(0);
            authorStatsVO.setTotalReviews(0);
            authorStatsVO.setAverageRating(0.0);
            authorStatsVO.setBooks(new ArrayList<>());
        }

        return authorStatsVO;
    }

    @Override
    public List<RecentActivityVO> getRecentActivities(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        log.info("获取最近操作记录: limit={}", limit);
        List<RecentActivityVO> activityList = new ArrayList<>();

        try {
            // 查询最近的借阅记录
            List<BorrowRecord> recentBorrows = borrowRecordMapper.selectList(
                    Wrappers.<BorrowRecord>lambdaQuery()
                            .orderByDesc(BorrowRecord::getCreatedAt)
                            .last("LIMIT " + limit)
            );

            for (BorrowRecord record : recentBorrows) {
                try {
                    RecentActivityVO activity = new RecentActivityVO();
                    activity.setId(record.getRecordId());
                    
                    // 根据状态设置类型
                    String type = "info";
                    String title = "";
                    String content = "";
                    
                    // 查询用户和图书信息
                    User user = userMapper.selectById(record.getUserId());
                    Book book = bookMapper.selectById(record.getBookId());
                    
                    String username = user != null ? user.getUsername() : "未知用户";
                    String bookTitle = book != null ? book.getTitle() : "未知图书";
                    
                    switch (record.getStatus()) {
                        case "borrowed":
                            type = "info";
                            title = "图书借阅";
                            content = String.format("用户 %s 借阅了《%s》", username, bookTitle);
                            break;
                        case "returned":
                            type = "success";
                            title = "图书归还";
                            content = String.format("用户 %s 归还了《%s》", username, bookTitle);
                            break;
                        case "overdue":
                            type = "warning";
                            title = "图书逾期";
                            content = String.format("用户 %s 借阅的《%s》已逾期", username, bookTitle);
                            break;
                        default:
                            type = "info";
                            title = "图书操作";
                            content = String.format("用户 %s 操作了《%s》", username, bookTitle);
                            break;
                    }
                    
                    activity.setType(type);
                    activity.setTitle(title);
                    activity.setContent(content);
                    
                    // 格式化时间
                    if (record.getCreatedAt() != null) {
                        activity.setTime(record.getCreatedAt().toString().replace("T", " "));
                    }
                    
                    activityList.add(activity);
                } catch (Exception e) {
                    log.error("处理操作记录失败: {}", record, e);
                }
            }

            log.info("成功获取{}条最近操作", activityList.size());

        } catch (Exception e) {
            log.error("获取最近操作失败", e);
        }

        return activityList;
    }
}
