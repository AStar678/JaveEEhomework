-- =================================================================
-- 经营分析服务数据库 (analysis_db)
-- 包含：聚合报表、观众画像、ETL进度
-- =================================================================

CREATE DATABASE IF NOT EXISTS `analysis_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `analysis_db`;

-- ----------------------------
-- 1. 聚合报表 (hourly_stats)
-- 支持按小时/分钟、主播、性别等多维度查询
-- ----------------------------
DROP TABLE IF EXISTS `hourly_stats`;
CREATE TABLE `hourly_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `stat_hour` datetime NOT NULL COMMENT '统计时间点',
  `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
  `anchor_name` varchar(64) DEFAULT NULL,
  `anchor_gender` tinyint(4) DEFAULT 1 COMMENT '主播性别',
  `viewer_gender` tinyint(4) DEFAULT 1 COMMENT '观众性别',
  `total_amount` decimal(12,2) DEFAULT 0.00 COMMENT '打赏汇总',
  PRIMARY KEY (`id`),
  KEY `idx_query` (`stat_hour`, `anchor_id`, `viewer_gender`, `anchor_gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营数据聚合表';

-- ----------------------------
-- 2. 观众画像表 (viewer_profile)
-- ----------------------------
DROP TABLE IF EXISTS `viewer_profile`;
CREATE TABLE `viewer_profile` (
  `viewer_id` bigint(20) NOT NULL COMMENT '打赏人ID',
  `viewer_name` varchar(64) DEFAULT NULL COMMENT '打赏人姓名',
  `total_amount` decimal(12,2) DEFAULT 0.00 COMMENT '历史打赏总金额',
  `percentile` decimal(6,4) DEFAULT 0.0000 COMMENT '打赏分位数',
  `tag_label` varchar(32) DEFAULT NULL COMMENT '画像标签',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`viewer_id`),
  KEY `idx_amount` (`total_amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观众画像表';

-- ----------------------------
-- 3. ETL进度表 (etl_progress)
-- ----------------------------
DROP TABLE IF EXISTS `etl_progress`;
CREATE TABLE `etl_progress` (
  `task_name` varchar(64) NOT NULL COMMENT '任务名称',
  `last_processed_id` bigint(20) DEFAULT 0 COMMENT '上次处理到的ID',
  `last_processed_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据处理进度表';

-- 初始化进度记录
INSERT INTO `etl_progress` (`task_name`, `last_processed_id`) VALUES ('HOURLY_STATS', 0), ('PROFILE_CALC', 0);
