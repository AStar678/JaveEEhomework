-- 创建数据库
CREATE DATABASE IF NOT EXISTS `db_analysis` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_analysis`;

-- ----------------------------
-- Table structure for hourly_stats
-- 需求来源: 按小时、性别、主播查询分析数据
-- ----------------------------
DROP TABLE IF EXISTS `hourly_stats`;
CREATE TABLE `hourly_stats` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `stat_hour` datetime NOT NULL COMMENT '统计时间段 (精确到小时，例如 2023-10-01 18:00:00)',
                                `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
                                `anchor_name` varchar(64) DEFAULT NULL,
                                `viewer_gender` tinyint(4) DEFAULT 1 COMMENT '观众性别 (1:男, 2:女, 0:未知)',
                                `total_amount` decimal(12,2) DEFAULT 0.00 COMMENT '该小时段内的打赏汇总',
                                PRIMARY KEY (`id`),
                                KEY `idx_query` (`stat_hour`, `anchor_id`, `viewer_gender`) COMMENT '组合索引加速多维查询 '
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经营数据-小时聚合表';

-- ----------------------------
-- Table structure for viewer_profile
-- 需求来源: 用户画像表，包含分位数和标签
-- ----------------------------
DROP TABLE IF EXISTS `viewer_profile`;
CREATE TABLE `viewer_profile` (
                                  `viewer_id` bigint(20) NOT NULL COMMENT '打赏人ID',
                                  `viewer_name` varchar(64) DEFAULT NULL COMMENT '打赏人姓名',
                                  `total_amount` decimal(12,2) DEFAULT 0.00 COMMENT '历史打赏总金额',
                                  `percentile` decimal(6,4) DEFAULT 0.0000 COMMENT '打赏分位数 (0.0-1.0)',
                                  `tag_label` varchar(32) DEFAULT NULL COMMENT '画像描述: 高消费/中消费/低消费 ',
                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`viewer_id`),
                                  KEY `idx_amount` (`total_amount`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='观众画像表';

-- ----------------------------
-- Table structure for etl_progress
-- 需求来源: 记录同一轮次处理进度
-- ----------------------------
DROP TABLE IF EXISTS `etl_progress`;
CREATE TABLE `etl_progress` (
                                `task_name` varchar(64) NOT NULL COMMENT '任务名称 (e.g., HOURLY_STATS, PROFILE_CALC)',
                                `last_processed_id` bigint(20) DEFAULT 0 COMMENT '上次处理到的打赏记录ID',
                                `last_processed_time` datetime DEFAULT NULL COMMENT '上次处理截止时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据处理进度表';

-- ----------------------------
-- Mock Data (初始化进度记录)
-- ----------------------------
INSERT INTO `etl_progress` (`task_name`, `last_processed_id`) VALUES
                                                                  ('HOURLY_STATS', 0),
                                                                  ('PROFILE_CALC', 0);