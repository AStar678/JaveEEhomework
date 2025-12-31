-- 创建数据库
CREATE DATABASE IF NOT EXISTS `db_viewer` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_viewer`;

-- ----------------------------
-- Table structure for donation_record
-- 需求来源:
-- ----------------------------
DROP TABLE IF EXISTS `donation_record`;
CREATE TABLE `donation_record` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
                                   `anchor_name` varchar(64) NOT NULL COMMENT '主播姓名 ',
                                   `viewer_id` bigint(20) NOT NULL COMMENT '打赏人ID',
                                   `viewer_name` varchar(64) NOT NULL COMMENT '打赏人姓名 ',
                                   `amount` decimal(10,2) NOT NULL COMMENT '打赏金额',
                                   `donate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打赏时间',
                                   `trace_id` varchar(64) NOT NULL COMMENT '全链路追踪ID，用于幂等性校验 ',
                                   `sync_status` tinyint(4) DEFAULT 0 COMMENT '同步给财务服务的状态: 0-未同步, 1-已同步 ',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_trace_id` (`trace_id`) USING BTREE COMMENT '核心：防止重复打赏的唯一索引 ',
                                   KEY `idx_anchor_amount` (`anchor_id`,`amount`) USING BTREE COMMENT '辅助查询：某主播打赏金额Top10 ',
                                   KEY `idx_sync` (`sync_status`) USING BTREE COMMENT '辅助查询：拉取未同步数据'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观众打赏记录表';

-- ----------------------------
-- Mock Data (预置几条测试数据)
-- ----------------------------
INSERT INTO `donation_record` (`anchor_id`, `anchor_name`, `viewer_id`, `viewer_name`, `amount`, `donate_time`, `trace_id`, `sync_status`) VALUES
                                                                                                                                               (101, '主播Alice', 2001, '观众张三', 100.00, NOW(), 'trace-001', 0),
                                                                                                                                               (101, '主播Alice', 2002, '观众李四', 500.00, NOW(), 'trace-002', 0),
                                                                                                                                               (102, '主播Bob', 2001, '观众张三', 50.00, NOW(), 'trace-003', 0);