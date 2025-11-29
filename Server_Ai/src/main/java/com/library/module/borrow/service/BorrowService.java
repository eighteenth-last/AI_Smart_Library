package com.library.module.borrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.module.borrow.dto.*;
import com.library.module.borrow.entity.BorrowRecord;
import com.library.module.borrow.vo.*;

/**
 * 借阅服务接口
 */
public interface BorrowService extends IService<BorrowRecord> {

    /**
     * 借阅图书
     *
     * @param requestDTO 借阅请求
     * @return 借阅结果
     */
    BorrowResultVO borrowBook(BorrowRequestDTO requestDTO);

    /**
     * 归还图书
     *
     * @param requestDTO 归还请求
     * @return 归还结果
     */
    ReturnResultVO returnBook(ReturnRequestDTO requestDTO);

    /**
     * 续借图书
     *
     * @param requestDTO 续借请求
     * @return 续借结果
     */
    RenewResultVO renewBook(RenewRequestDTO requestDTO);

    /**
     * 获取用户借阅记录
     *
     * @param userId 用户ID
     * @param status 状态筛选
     * @param page   页码
     * @param size   每页数量
     * @return 借阅记录列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<BorrowRecordVO> getUserBorrowRecords(Long userId, String status, Integer page, Integer size);

    /**
     * 获取所有借阅记录（管理员）
     *
     * @param userId 用户ID筛选
     * @param bookId 图书ID筛选
     * @param status 状态筛选
     * @param page   页码
     * @param size   每页数量
     * @return 借阅记录列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<BorrowRecordVO> getAllBorrowRecords(Long userId, Long bookId, String status, Integer page, Integer size);

    /**
     * 计算逾期费用
     *
     * @param dueTime    应还时间
     * @param returnTime 归还时间
     * @return 逾期费用
     */
    java.math.BigDecimal calculateOverdueFee(java.time.LocalDateTime dueTime, java.time.LocalDateTime returnTime);

    /**
     * 检查用户是否可以借阅
     *
     * @param userId 用户ID
     * @return 是否可以借阅
     */
    boolean canBorrow(Long userId);

    /**
     * 获取用户当前借阅数量
     *
     * @param userId 用户ID
     * @return 当前借阅数量
     */
    int getCurrentBorrowCount(Long userId);

    /**
     * 审批借阅申请（管理员）
     *
     * @param recordId 借阅记录ID
     * @param approved 是否同意：true-同意，false-拒绝
     * @param rejectReason 拒绝原因（拒绝时必填）
     * @param approverId 审批人ID
     * @return 是否审批成功
     */
    boolean approveBorrow(Long recordId, Boolean approved, String rejectReason, Long approverId);
}
