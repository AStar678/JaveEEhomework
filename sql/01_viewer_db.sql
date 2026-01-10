-- =================================================================
-- 观众服务数据库 (viewer_db)
-- 包含：打赏记录、主播信息、观众信息
-- =================================================================

CREATE DATABASE IF NOT EXISTS `viewer_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `viewer_db`;

-- ----------------------------
-- 1. 主播信息表 (anchor)
-- ----------------------------
DROP TABLE IF EXISTS `anchor`;
CREATE TABLE `anchor` (
  `id` bigint(20) NOT NULL COMMENT '主播ID',
  `name` varchar(64) NOT NULL COMMENT '主播姓名',
  `gender` tinyint(4) NOT NULL DEFAULT 1 COMMENT '性别: 1-男, 2-女',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主播信息表';

-- 初始化 100 个主播数据
DROP PROCEDURE IF EXISTS init_anchors;
DELIMITER //
CREATE PROCEDURE init_anchors()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 100 DO
    INSERT INTO `anchor` (`id`, `name`, `gender`) 
    VALUES (i, CONCAT('主播', i), IF(RAND() > 0.5, 1, 2));
    SET i = i + 1;
  END WHILE;
END //
DELIMITER ;
CALL init_anchors();
DROP PROCEDURE init_anchors;

-- ----------------------------
-- 2. 观众信息表 (viewer)
-- ----------------------------
DROP TABLE IF EXISTS `viewer`;
CREATE TABLE `viewer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '观众ID',
  `name` varchar(64) NOT NULL COMMENT '观众姓名',
  `gender` tinyint(4) DEFAULT 1 COMMENT '性别: 1-男, 2-女',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT '姓名唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观众信息表';

-- 预置几个测试观众
INSERT INTO `viewer` (`name`, `gender`) VALUES ('观众张三', 1), ('观众李四', 1), ('观众王五', 2);

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观众打赏记录表';
