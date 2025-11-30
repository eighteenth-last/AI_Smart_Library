-- 修复 borrow_record 表的字段定义
-- 将 borrow_time 和 due_time 改为允许 NULL
-- 因为在 pending 状态下，这些字段应该为空

USE ai_smart_library;

-- 修改 borrow_time 字段为允许 NULL
ALTER TABLE `borrow_record` 
MODIFY COLUMN `borrow_time` datetime NULL DEFAULT NULL COMMENT '借书时间';

-- 修改 due_time 字段为允许 NULL
ALTER TABLE `borrow_record` 
MODIFY COLUMN `due_time` datetime NULL DEFAULT NULL COMMENT '应还时间';

-- 验证修改
DESCRIBE `borrow_record`;
