-- =================================================================
-- 财务服务数据库 (finance_db)
-- 包含：分成比例、结算信息
-- =================================================================

CREATE DATABASE IF NOT EXISTS `finance_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `finance_db`;

-- ----------------------------
-- 1. 主播分成比例表 (sharing_ratio)
-- ----------------------------
DROP TABLE IF EXISTS `sharing_ratio`;
CREATE TABLE `sharing_ratio` (
  `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
  `anchor_name` varchar(64) DEFAULT NULL COMMENT '主播姓名',
  `ratio` decimal(4,2) NOT NULL COMMENT '分成比例 (0.00-1.00)',
  PRIMARY KEY (`anchor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主播分成比例表';

-- 初始化 100 个主播的分成比例 (默认 0.5)
DROP PROCEDURE IF EXISTS init_sharing_ratio;
DELIMITER //
CREATE PROCEDURE init_sharing_ratio()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 100 DO
    INSERT IGNORE INTO `sharing_ratio` (`anchor_id`, `anchor_name`, `ratio`) 
    VALUES (i, CONCAT('主播', i), 0.50);
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;
CALL init_sharing_ratio();
DROP PROCEDURE init_sharing_ratio;

-- ----------------------------
-- 2. 财务结算表 (settlement)
-- ----------------------------
DROP TABLE IF EXISTS `settlement`;
CREATE TABLE `settlement` (
  `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
  `anchor_name` varchar(64) DEFAULT NULL COMMENT '主播姓名',
  `total_revenue` decimal(12,2) DEFAULT 0.00 COMMENT '打赏总流水 (未分成)',
  `total_settled_amount` decimal(12,2) DEFAULT 0.00 COMMENT '累计结算金额 (净收入)',
  `total_withdrawn_amount` decimal(12,2) DEFAULT 0.00 COMMENT '已提取金额',
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`anchor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务结算表';

-- ----------------------------
-- 3. 打赏记录表 (donation_record)
-- ----------------------------
DROP TABLE IF EXISTS `donation_record`;
CREATE TABLE `donation_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
  `anchor_name` varchar(64) NOT NULL COMMENT '主播姓名',
  `anchor_gender` tinyint(4) DEFAULT 1 COMMENT '主播性别',
  `viewer_id` bigint(20) NOT NULL COMMENT '打赏人ID',
  `viewer_name` varchar(64) NOT NULL COMMENT '打赏人姓名',
  `viewer_gender` tinyint(4) DEFAULT 1 COMMENT '观众性别',
  `amount` decimal(10,2) NOT NULL COMMENT '打赏金额',
  `donate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打赏时间',
  `trace_id` varchar(64) NOT NULL COMMENT '全链路追踪ID',
  `sync_status` tinyint(4) DEFAULT 0 COMMENT '同步状态: 0-未同步, 1-已同步',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_id` (`trace_id`) USING BTREE,
  KEY `idx_anchor_amount` (`anchor_id`,`amount`) USING BTREE,
  KEY `idx_sync` (`sync_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务打赏记录表';
