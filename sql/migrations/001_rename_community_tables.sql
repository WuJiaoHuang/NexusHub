-- 仅在维护窗口内，对旧库备份恢复得到的全新 nexushub 数据库执行一次。
-- 完整前置条件与回退流程见 docs/migration.md。
-- 不创建/覆盖表，不删除数据，不改动原 campuslink 数据库。
-- 任一源表缺失或目标表已存在时，MySQL 8 / InnoDB 的单条 RENAME TABLE 整体失败。
USE nexushub;

RENAME TABLE
  campus_place TO place,
  campus_post TO post,
  campus_post_comments TO post_comment,
  campus_event TO event;

-- 原有主键、自增值、索引及数据随表保留；报名与容量表完全不变。
SHOW INDEX FROM event_registration WHERE Key_name = 'uk_user_event';
SHOW CREATE TABLE event_capacity;
