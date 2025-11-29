package com.library.module.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.result.ResultCode;
import com.library.common.exception.BusinessException;
import com.library.module.borrow.dto.*;
import com.library.module.borrow.entity.BorrowRecord;
import com.library.module.borrow.mapper.BorrowRecordMapper;
import com.library.module.borrow.service.BorrowService;
import com.library.module.borrow.vo.*;
import com.library.module.book.entity.Book;
import com.library.module.book.mapper.BookMapper;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 借阅服务实现类
 */
@Slf4j
@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 最大借阅数量
     */
    private static final int MAX_BORROW_LIMIT = 5;

    /**
     * 借阅期限（天）
     */
    private static final int BORROW_DURATION = 30;

    /**
     * 续借期限（天）
     */
    private static final int RENEW_DURATION = 15;

    /**
     * 逾期费用（元/天）
     */
    private static final BigDecimal OVERDUE_FEE_PER_DAY = new BigDecimal("0.5");

    @Override
    @Transactional
    public BorrowResultVO borrowBook(BorrowRequestDTO requestDTO) {
        Long userId = requestDTO.getUserId();
        Long bookId = requestDTO.getBookId();

        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 检查用户是否可以借阅
        if (!canBorrow(userId)) {
            throw new BusinessException(ResultCode.BORROW_LIMIT_EXCEEDED);
        }

        // 3. 检查图书库存（不需要使用乐观锁，触发器会自动处理库存）
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_EXIST);
        }

        // 检查图书库存
        if (book.getAvailableStock() <= 0) {
            throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
        }

        // 4. 创建借阅记录（状态为 pending，需要管理员审批）
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(userId);
        borrowRecord.setBookId(bookId);
        borrowRecord.setStatus("pending");  // 待审批状态
        borrowRecord.setIsRenewed(0);
        borrowRecord.setOverdueFee(BigDecimal.ZERO);

        this.save(borrowRecord);

        // 5. 返回结果
        BorrowResultVO resultVO = new BorrowResultVO();
        resultVO.setRecordId(borrowRecord.getRecordId());
        resultVO.setBookId(bookId);
        resultVO.setBookTitle(book.getTitle());
        resultVO.setBorrowTime(null);  // 审批前没有借阅时间
        resultVO.setDueTime(null);  // 审批前没有应还时间

        return resultVO;
    }

    @Override
    @Transactional
    public ReturnResultVO returnBook(ReturnRequestDTO requestDTO) {
        Long recordId = requestDTO.getRecordId();
        Long userId = requestDTO.getUserId();

        // 1. 查询借阅记录
        BorrowRecord borrowRecord = this.getById(recordId);
        if (borrowRecord == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_EXIST);
        }

        // 2. 检查是否是该用户的借阅记录
        if (!borrowRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        // 3. 检查是否已经归还
        if ("returned".equals(borrowRecord.getStatus())) {
            throw new BusinessException(ResultCode.BOOK_ALREADY_RETURNED);
        }

        // 4. 查询图书信息
        Book book = bookMapper.selectById(borrowRecord.getBookId());
        if (book == null) {
            throw new BusinessException(ResultCode.BOOK_NOT_EXIST);
        }

        // 5. 计算逾期费用
        LocalDateTime returnTime = LocalDateTime.now();
        BigDecimal overdueFee = calculateOverdueFee(borrowRecord.getDueTime(), returnTime);

        // 6. 更新借阅记录（触发器会自动增加available_stock）
        borrowRecord.setReturnTime(returnTime);
        borrowRecord.setStatus(overdueFee.compareTo(BigDecimal.ZERO) > 0 ? "overdue" : "returned");
        borrowRecord.setOverdueFee(overdueFee);
        this.updateById(borrowRecord);

        // 7. 返回结果
        ReturnResultVO resultVO = new ReturnResultVO();
        resultVO.setRecordId(recordId);
        resultVO.setReturnTime(returnTime);
        resultVO.setOverdueFee(overdueFee);
        resultVO.setIsOverdue(overdueFee.compareTo(BigDecimal.ZERO) > 0);

        return resultVO;
    }

    @Override
    @Transactional
    public RenewResultVO renewBook(RenewRequestDTO requestDTO) {
        Long recordId = requestDTO.getRecordId();
        Long userId = requestDTO.getUserId();

        // 1. 查询借阅记录
        BorrowRecord borrowRecord = this.getById(recordId);
        if (borrowRecord == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_EXIST);
        }

        // 2. 检查是否是该用户的借阅记录
        if (!borrowRecord.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }

        // 3. 检查是否已经归还
        if ("returned".equals(borrowRecord.getStatus())) {
            throw new BusinessException(ResultCode.BOOK_ALREADY_RETURNED);
        }

        // 4. 检查是否已经续借过
        if (borrowRecord.getIsRenewed() == 1) {
            throw new BusinessException(ResultCode.BOOK_ALREADY_RENEWED);
        }

        // 5. 检查是否逾期
        if ("overdue".equals(borrowRecord.getStatus())) {
            throw new BusinessException(ResultCode.BOOK_OVERDUE);
        }

        // 6. 更新应还时间
        LocalDateTime newDueTime = borrowRecord.getDueTime().plusDays(RENEW_DURATION);
        borrowRecord.setDueTime(newDueTime);
        borrowRecord.setIsRenewed(1);
        this.updateById(borrowRecord);

        // 7. 返回结果
        RenewResultVO resultVO = new RenewResultVO();
        resultVO.setRecordId(recordId);
        resultVO.setNewDueTime(newDueTime);

        return resultVO;
    }

    @Override
    public Page<BorrowRecordVO> getUserBorrowRecords(Long userId, String status, Integer page, Integer size) {
        try {
            log.info("查询用户借阅记录: userId={}, status={}, page={}, size={}", userId, status, page, size);
            
            Page<BorrowRecord> borrowRecordPage = new Page<>(page, size);
            LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                    .eq(BorrowRecord::getUserId, userId)
                    .eq(status != null && !status.isEmpty(), BorrowRecord::getStatus, status)
                    .orderByDesc(BorrowRecord::getCreatedAt);

            Page<BorrowRecord> resultPage = this.page(borrowRecordPage, wrapper);
            log.info("查询到借阅记录: total={}, pages={}", resultPage.getTotal(), resultPage.getPages());

            return convertToVO(resultPage);
        } catch (Exception e) {
            log.error("获取用户借阅记录失败: userId={}", userId, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "获取借阅记录失败: " + e.getMessage());
        }
    }

    @Override
    public Page<BorrowRecordVO> getAllBorrowRecords(Long userId, Long bookId, String status, Integer page, Integer size) {
        Page<BorrowRecord> borrowRecordPage = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(userId != null, BorrowRecord::getUserId, userId)
                .eq(bookId != null, BorrowRecord::getBookId, bookId)
                .eq(status != null, BorrowRecord::getStatus, status)
                .orderByDesc(BorrowRecord::getCreatedAt);

        Page<BorrowRecord> resultPage = this.page(borrowRecordPage, wrapper);

        return convertToVO(resultPage);
    }

    @Override
    public BigDecimal calculateOverdueFee(LocalDateTime dueTime, LocalDateTime returnTime) {
        if (returnTime.isBefore(dueTime) || returnTime.isEqual(dueTime)) {
            return BigDecimal.ZERO;
        }

        long overdueDays = ChronoUnit.DAYS.between(dueTime, returnTime);
        return OVERDUE_FEE_PER_DAY.multiply(new BigDecimal(overdueDays));
    }

    @Override
    public boolean canBorrow(Long userId) {
        return getCurrentBorrowCount(userId) < MAX_BORROW_LIMIT;
    }

    @Override
    public int getCurrentBorrowCount(Long userId) {
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, userId)
                .eq(BorrowRecord::getStatus, "borrowed");

        return (int) this.count(wrapper);
    }

    /**
     * 转换为VO对象
     */
    private Page<BorrowRecordVO> convertToVO(Page<BorrowRecord> recordPage) {
        try {
            Page<BorrowRecordVO> voPage = new Page<>();
            BeanUtils.copyProperties(recordPage, voPage, "records");

            List<BorrowRecordVO> voList = recordPage.getRecords().stream().map(record -> {
                try {
                    BorrowRecordVO vo = new BorrowRecordVO();
                    BeanUtils.copyProperties(record, vo);

                    // 查询用户名
                    try {
                        User user = userMapper.selectById(record.getUserId());
                        if (user != null) {
                            vo.setUsername(user.getUsername());
                        } else {
                            log.warn("用户ID {} 不存在", record.getUserId());
                            vo.setUsername("未知用户");
                        }
                    } catch (Exception e) {
                        log.error("查询用户信息失败: userId={}", record.getUserId(), e);
                        vo.setUsername("未知用户");
                    }

                    // 查询图书信息
                    try {
                        Book book = bookMapper.selectById(record.getBookId());
                        if (book != null) {
                            vo.setBookTitle(book.getTitle());
                            vo.setCoverUrl(book.getCoverUrl());
                        } else {
                            log.warn("图书ID {} 不存在", record.getBookId());
                            vo.setBookTitle("未知图书");
                            vo.setCoverUrl("");
                        }
                    } catch (Exception e) {
                        log.error("查询图书信息失败: bookId={}", record.getBookId(), e);
                        vo.setBookTitle("未知图书");
                        vo.setCoverUrl("");
                    }

                    // 计算是否逾期和剩余天数
                    LocalDateTime now = LocalDateTime.now();
                    boolean isOverdue = now.isAfter(record.getDueTime());
                    vo.setIsOverdue(isOverdue);

                    int daysRemaining = (int) ChronoUnit.DAYS.between(now, record.getDueTime());
                    vo.setDaysRemaining(daysRemaining);

                    return vo;
                } catch (Exception e) {
                    log.error("转换BorrowRecord失败: recordId={}", record.getRecordId(), e);
                    throw new RuntimeException("借阅记录数据转换失败", e);
                }
            }).collect(Collectors.toList());

            voPage.setRecords(voList);
            return voPage;
        } catch (Exception e) {
            log.error("转换借阅记录分页数据失败", e);
            throw new RuntimeException("借阅记录查询失败", e);
        }
    }

    @Override
    @Transactional
    public boolean approveBorrow(Long recordId, Boolean approved, String rejectReason, Long approverId) {
        // 1. 查询借阅记录
        BorrowRecord borrowRecord = this.getById(recordId);
        if (borrowRecord == null) {
            throw new BusinessException(ResultCode.BORROW_RECORD_NOT_EXIST);
        }

        // 2. 检查是否已经审批过
        if (!"pending".equals(borrowRecord.getStatus())) {
            throw new BusinessException("该借阅申请已被审批，无法重复审批");
        }

        // 3. 设置审批信息
        borrowRecord.setApproveTime(LocalDateTime.now());
        borrowRecord.setApproverId(approverId);

        if (approved) {
            // 同意借阅
            // 4. 检查图书库存
            Book book = bookMapper.selectById(borrowRecord.getBookId());
            if (book == null) {
                throw new BusinessException(ResultCode.BOOK_NOT_EXIST);
            }

            if (book.getAvailableStock() <= 0) {
                throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
            }

            // 5. 更新状态为"借阅中"
            borrowRecord.setStatus("borrowed");
            borrowRecord.setBorrowTime(LocalDateTime.now());
            borrowRecord.setDueTime(LocalDateTime.now().plusDays(BORROW_DURATION));
            
            log.info("审批通过借阅申请 - RecordId: {}, ApproverId: {}", recordId, approverId);
        } else {
            // 拒绝借阅
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                throw new BusinessException("拒绝借阅必须填写原因");
            }
            
            borrowRecord.setStatus("rejected");
            borrowRecord.setRejectReason(rejectReason);
            
            log.info("拒绝借阅申请 - RecordId: {}, ApproverId: {}, Reason: {}", recordId, approverId, rejectReason);
        }

        return this.updateById(borrowRecord);
    }
}
