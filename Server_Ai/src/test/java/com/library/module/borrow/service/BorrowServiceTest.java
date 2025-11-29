package com.library.module.borrow.service;

import com.library.module.borrow.dto.BorrowRequestDTO;
import com.library.module.borrow.dto.ReturnRequestDTO;
import com.library.module.borrow.entity.BorrowRecord;
import com.library.module.borrow.vo.BorrowResultVO;
import com.library.module.borrow.vo.ReturnResultVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 借阅服务测试类
 */
@SpringBootTest
public class BorrowServiceTest {

    @Autowired
    private BorrowService borrowService;

    /**
     * 测试逾期费用计算
     */
    @Test
    public void testCalculateOverdueFee() {
        // 测试1：准时归还（无逾期）
        LocalDateTime dueTime = LocalDateTime.now().plusDays(1);
        LocalDateTime returnTime = LocalDateTime.now();
        BigDecimal fee = borrowService.calculateOverdueFee(dueTime, returnTime);
        assertEquals(BigDecimal.ZERO, fee, "准时归还应该没有逾期费用");

        // 测试2：逾期1天
        dueTime = LocalDateTime.now().minusDays(1);
        returnTime = LocalDateTime.now();
        fee = borrowService.calculateOverdueFee(dueTime, returnTime);
        assertEquals(new BigDecimal("0.5"), fee, "逾期1天费用应为0.5元");

        // 测试3：逾期10天
        dueTime = LocalDateTime.now().minusDays(10);
        returnTime = LocalDateTime.now();
        fee = borrowService.calculateOverdueFee(dueTime, returnTime);
        assertEquals(new BigDecimal("5.0"), fee, "逾期10天费用应为5元");
    }

    /**
     * 测试借阅限制
     */
    @Test
    public void testBorrowLimit() {
        // 假设用户ID为1
        Long userId = 1L;
        
        // 获取当前借阅数量
        int currentCount = borrowService.getCurrentBorrowCount(userId);
        System.out.println("当前借阅数量: " + currentCount);
        
        // 检查是否可以借阅
        boolean canBorrow = borrowService.canBorrow(userId);
        System.out.println("是否可以借阅: " + canBorrow);
        
        // 断言：当前借阅数量应该小于等于5
        assertTrue(currentCount <= 5, "借阅数量不能超过5本");
    }
}
