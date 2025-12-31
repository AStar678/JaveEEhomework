-- 创建数据库
CREATE DATABASE IF NOT EXISTS `db_finance` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_finance`;

-- ----------------------------
-- Table structure for sharing_ratio
-- 需求来源: 不同的主播有不同的分成比例
-- ----------------------------
DROP TABLE IF EXISTS `sharing_ratio`;
CREATE TABLE `sharing_ratio` (
                                 `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
                                 `anchor_name` varchar(64) DEFAULT NULL COMMENT '主播姓名',
                                 `ratio` decimal(4,2) NOT NULL COMMENT '分成比例 (例如 0.50 表示 50%)',
                                 PRIMARY KEY (`anchor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主播分成比例表';

-- ----------------------------
-- Table structure for settlement
-- 需求来源: 记录结算金额和已提取金额
-- ----------------------------
DROP TABLE IF EXISTS `settlement`;
CREATE TABLE `settlement` (
                              `anchor_id` bigint(20) NOT NULL COMMENT '主播ID',
                              `anchor_name` varchar(64) DEFAULT NULL COMMENT '主播姓名',
                              `total_settled_amount` decimal(12,2) DEFAULT 0.00 COMMENT '累计结算金额 (总收入 * 比例)',
                              `total_withdrawn_amount` decimal(12,2) DEFAULT 0.00 COMMENT '已提取金额',
                              `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`anchor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务结算表';

-- ----------------------------
-- Mock Data
-- ----------------------------
-- 预置两个主播的分成比例
INSERT INTO `sharing_ratio` (`anchor_id`, `anchor_name`, `ratio`) VALUES
                                                                      (101, '主播Alice', 0.60), -- Alice 拿 60%
                                                                      (102, '主播Bob', 0.40);   -- Bob 拿 40%