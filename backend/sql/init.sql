-- ============================================
-- FitTracker Pro - 数据库初始化脚本
-- MySQL 8.0+
-- ============================================

DROP DATABASE IF EXISTS `fitness_tracker`;
CREATE DATABASE `fitness_tracker` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `fitness_tracker`;

-- -------------------------------------------
-- 用户表
-- -------------------------------------------
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别:0-未知,1-男,2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `height` DECIMAL(5,1) DEFAULT NULL COMMENT '身高(cm)',
    `weight` DECIMAL(5,1) DEFAULT NULL COMMENT '当前体重(kg)',
    `activity_level` TINYINT DEFAULT 2 COMMENT '活动水平:1-久坐,2-轻度活动,3-中度活动,4-高强度,5-极高',
    `fitness_goal` TINYINT DEFAULT 2 COMMENT '健身目标:1-增肌,2-减脂,3-维持,4-力量提升',
    `daily_calorie_target` INT DEFAULT 2000 COMMENT '每日卡路里目标(kcal)',
    `daily_protein_target` INT DEFAULT 150 COMMENT '每日蛋白质目标(g)',
    `daily_carb_target` INT DEFAULT 250 COMMENT '每日碳水目标(g)',
    `daily_fat_target` INT DEFAULT 65 COMMENT '每日脂肪目标(g)',
    `daily_water_target` INT DEFAULT 2000 COMMENT '每日饮水目标(ml)',
    `weekly_training_target` INT DEFAULT 5 COMMENT '每周训练目标(次)',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------
-- 动作库表
-- -------------------------------------------
CREATE TABLE `exercise_library` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '动作ID',
    `name` VARCHAR(100) NOT NULL COMMENT '动作名称',
    `name_en` VARCHAR(100) DEFAULT NULL COMMENT '英文名称',
    `category` VARCHAR(50) NOT NULL COMMENT '动作分类',
    `muscle_group` VARCHAR(500) NOT NULL COMMENT '主要肌群',
    `secondary_muscle` VARCHAR(500) DEFAULT NULL COMMENT '次要肌群',
    `equipment` VARCHAR(100) DEFAULT NULL COMMENT '所需器械',
    `difficulty` TINYINT DEFAULT 1 COMMENT '难度:1-初级,2-中级,3-高级',
    `description` TEXT DEFAULT NULL COMMENT '动作描述',
    `instructions` TEXT DEFAULT NULL COMMENT '动作要领',
    `tips` TEXT DEFAULT NULL COMMENT '训练提示',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '演示图片/GIF',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '演示视频',
    `is_compound` TINYINT DEFAULT 0 COMMENT '是否复合动作:0-否,1-是',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_muscle_group` (`muscle_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动作库表';

-- -------------------------------------------
-- 训练记录表
-- -------------------------------------------
CREATE TABLE `training_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `training_date` DATE NOT NULL COMMENT '训练日期',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `duration` INT DEFAULT 0 COMMENT '训练时长(分钟)',
    `training_type` VARCHAR(50) DEFAULT NULL COMMENT '训练类型',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '训练名称',
    `note` TEXT DEFAULT NULL COMMENT '训练笔记',
    `feeling_score` TINYINT DEFAULT 0 COMMENT '感受评分:1-5星',
    `total_volume` DECIMAL(12,2) DEFAULT 0 COMMENT '总训练容量(kg)',
    `total_sets` INT DEFAULT 0 COMMENT '总组数',
    `calories_burned` INT DEFAULT 0 COMMENT '消耗卡路里(kcal)',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0-草稿,1-已完成',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `training_date`),
    KEY `idx_user_type` (`user_id`, `training_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练记录表';

-- -------------------------------------------
-- 训练动作表
-- -------------------------------------------
CREATE TABLE `training_exercise` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `training_record_id` BIGINT NOT NULL COMMENT '训练记录ID',
    `exercise_library_id` BIGINT NOT NULL COMMENT '动作库ID',
    `exercise_name` VARCHAR(100) NOT NULL COMMENT '动作名称(冗余)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `is_superset` TINYINT DEFAULT 0 COMMENT '是否超级组',
    `superset_group` INT DEFAULT NULL COMMENT '超级组分组',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_training` (`training_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练动作表';

-- -------------------------------------------
-- 训练组数表
-- -------------------------------------------
CREATE TABLE `training_set` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `training_exercise_id` BIGINT NOT NULL COMMENT '训练动作ID',
    `set_number` INT NOT NULL COMMENT '组号',
    `set_type` VARCHAR(20) DEFAULT 'normal' COMMENT '组类型:normal/warmup/dropset/failure',
    `reps` INT DEFAULT 0 COMMENT '次数',
    `weight` DECIMAL(8,2) DEFAULT 0 COMMENT '重量(kg)',
    `duration` INT DEFAULT 0 COMMENT '持续时间(秒)',
    `rest_time` INT DEFAULT 0 COMMENT '休息时间(秒)',
    `is_completed` TINYINT DEFAULT 1 COMMENT '是否完成',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_exercise` (`training_exercise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练组数表';

-- -------------------------------------------
-- 训练模板表
-- -------------------------------------------
CREATE TABLE `training_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `training_type` VARCHAR(50) DEFAULT NULL COMMENT '训练类型',
    `estimated_duration` INT DEFAULT 0 COMMENT '预计时长(分钟)',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练模板表';

-- -------------------------------------------
-- 模板动作表
-- -------------------------------------------
CREATE TABLE `template_exercise` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `exercise_library_id` BIGINT NOT NULL COMMENT '动作库ID',
    `exercise_name` VARCHAR(100) NOT NULL COMMENT '动作名称',
    `target_sets` INT DEFAULT 3 COMMENT '目标组数',
    `target_reps` VARCHAR(20) DEFAULT '10' COMMENT '目标次数',
    `target_weight` DECIMAL(8,2) DEFAULT NULL COMMENT '目标重量(kg)',
    `rest_time` INT DEFAULT 90 COMMENT '休息时间(秒)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板动作表';

-- -------------------------------------------
-- 食物表
-- -------------------------------------------
CREATE TABLE `food` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '食物ID',
    `name` VARCHAR(100) NOT NULL COMMENT '食物名称',
    `category` VARCHAR(50) NOT NULL COMMENT '食物分类',
    `brand` VARCHAR(100) DEFAULT NULL COMMENT '品牌',
    `calories` DECIMAL(8,1) NOT NULL COMMENT '卡路里(kcal/100g)',
    `protein` DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '蛋白质(g/100g)',
    `carbs` DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '碳水化合物(g/100g)',
    `fat` DECIMAL(8,1) NOT NULL DEFAULT 0 COMMENT '脂肪(g/100g)',
    `fiber` DECIMAL(8,1) DEFAULT 0 COMMENT '膳食纤维(g/100g)',
    `sugar` DECIMAL(8,1) DEFAULT 0 COMMENT '糖(g/100g)',
    `sodium` DECIMAL(8,1) DEFAULT 0 COMMENT '钠(mg/100g)',
    `serving_size` DECIMAL(8,1) DEFAULT 100 COMMENT '标准份量(g)',
    `serving_unit` VARCHAR(20) DEFAULT 'g' COMMENT '份量单位',
    `barcode` VARCHAR(50) DEFAULT NULL COMMENT '条形码',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片',
    `is_user_created` TINYINT DEFAULT 0 COMMENT '是否用户创建',
    `user_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_name` (`name`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食物表';

-- -------------------------------------------
-- 饮食记录表
-- -------------------------------------------
CREATE TABLE `diet_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `meal_type` TINYINT NOT NULL COMMENT '餐次:1-早餐,2-午餐,3-晚餐,4-加餐',
    `food_id` BIGINT NOT NULL COMMENT '食物ID',
    `food_name` VARCHAR(100) NOT NULL COMMENT '食物名称(冗余)',
    `amount` DECIMAL(8,1) NOT NULL COMMENT '食用量(g)',
    `calories` DECIMAL(8,1) NOT NULL COMMENT '卡路里(kcal)',
    `protein` DECIMAL(8,1) DEFAULT 0 COMMENT '蛋白质(g)',
    `carbs` DECIMAL(8,1) DEFAULT 0 COMMENT '碳水(g)',
    `fat` DECIMAL(8,1) DEFAULT 0 COMMENT '脂肪(g)',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮食记录表';

-- -------------------------------------------
-- 饮水记录表
-- -------------------------------------------
CREATE TABLE `water_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `amount` INT NOT NULL COMMENT '饮水量(ml)',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饮水记录表';

-- -------------------------------------------
-- 身体数据表
-- -------------------------------------------
CREATE TABLE `body_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `weight` DECIMAL(5,1) DEFAULT NULL COMMENT '体重(kg)',
    `body_fat_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '体脂率(%)',
    `muscle_mass` DECIMAL(5,1) DEFAULT NULL COMMENT '肌肉量(kg)',
    `bmi` DECIMAL(5,2) DEFAULT NULL COMMENT 'BMI',
    `chest` DECIMAL(5,1) DEFAULT NULL COMMENT '胸围(cm)',
    `waist` DECIMAL(5,1) DEFAULT NULL COMMENT '腰围(cm)',
    `hip` DECIMAL(5,1) DEFAULT NULL COMMENT '臀围(cm)',
    `upper_arm` DECIMAL(5,1) DEFAULT NULL COMMENT '上臂围(cm)',
    `thigh` DECIMAL(5,1) DEFAULT NULL COMMENT '大腿围(cm)',
    `neck` DECIMAL(5,1) DEFAULT NULL COMMENT '颈围(cm)',
    `shoulder` DECIMAL(5,1) DEFAULT NULL COMMENT '肩宽(cm)',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体数据表';

-- -------------------------------------------
-- 成就表
-- -------------------------------------------
CREATE TABLE `achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成就ID',
    `name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description` VARCHAR(500) NOT NULL COMMENT '成就描述',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标',
    `category` VARCHAR(50) NOT NULL COMMENT '成就分类',
    `condition_type` VARCHAR(50) NOT NULL COMMENT '条件类型',
    `condition_value` INT NOT NULL COMMENT '条件值',
    `points` INT DEFAULT 0 COMMENT '积分',
    `rarity` TINYINT DEFAULT 1 COMMENT '稀有度:1-普通,2-稀有,3-史诗,4-传说',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就表';

-- -------------------------------------------
-- 用户成就表
-- -------------------------------------------
CREATE TABLE `user_achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
    `unlock_time` DATETIME NOT NULL COMMENT '解锁时间',
    `progress` INT DEFAULT 0 COMMENT '当前进度',
    `is_notified` TINYINT DEFAULT 0 COMMENT '是否已通知',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户成就表';

-- -------------------------------------------
-- 睡眠记录表
-- -------------------------------------------
CREATE TABLE `sleep_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_date` DATE NOT NULL COMMENT '记录日期',
    `sleep_time` DATETIME NOT NULL COMMENT '入睡时间',
    `wake_time` DATETIME NOT NULL COMMENT '醒来时间',
    `duration` INT NOT NULL COMMENT '睡眠时长(分钟)',
    `quality` TINYINT DEFAULT 3 COMMENT '睡眠质量:1-5',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='睡眠记录表';

-- -------------------------------------------
-- 初始数据 - 动作库
-- -------------------------------------------
INSERT INTO `exercise_library` (`name`, `name_en`, `category`, `muscle_group`, `secondary_muscle`, `equipment`, `difficulty`, `description`, `instructions`, `is_compound`, `sort_order`) VALUES
-- 胸部
('杠铃卧推', 'Barbell Bench Press', '胸部', '胸大肌', '三角肌前束,肱三头肌', '杠铃,卧推架', 2, '经典胸部训练动作', '1.躺在卧推凳上，握距略宽于肩\n2.下放杠铃至胸部中段\n3.推起至手臂伸直', 1, 1),
('哑铃飞鸟', 'Dumbbell Fly', '胸部', '胸大肌', '三角肌前束', '哑铃,卧推凳', 1, '胸部孤立训练动作', '1.仰卧，双手持哑铃\n2.微屈肘部，向两侧展开\n3.沿弧线收拢', 0, 2),
('上斜杠铃卧推', 'Incline Barbell Press', '胸部', '胸大肌上部', '三角肌前束,肱三头肌', '杠铃,上斜卧推架', 2, '针对上胸部的训练', '1.躺在30-45度上斜凳上\n2.下放杠铃至上胸部\n3.推起至手臂伸直', 1, 3),
('俯卧撑', 'Push Up', '胸部', '胸大肌', '三角肌前束,肱三头肌', '自重', 1, '经典自重胸部训练', '1.双手撑地，略宽于肩\n2.身体保持一条直线\n3.下降至胸部接近地面后推起', 1, 4),
('龙门架夹胸', 'Cable Crossover', '胸部', '胸大肌', '三角肌前束', '龙门架', 2, '胸部孤立训练', '1.站在龙门架中间\n2.双手抓住把手，微屈肘\n3.向前合拢双手', 0, 5),

-- 背部
('杠铃划船', 'Barbell Row', '背部', '背阔肌,斜方肌', '肱二头肌,三角肌后束', '杠铃', 2, '经典背部训练动作', '1.俯身约45度，握杠铃\n2.将杠铃拉向腹部\n3.缓慢下放', 1, 10),
('引体向上', 'Pull Up', '背部', '背阔肌', '肱二头肌,三角肌后束', '单杠', 2, '经典自重背部训练', '1.双手正握单杠，略宽于肩\n2.拉起至下巴过杠\n3.控制下放', 1, 11),
('高位下拉', 'Lat Pulldown', '背部', '背阔肌', '肱二头肌', '高位下拉器', 1, '背部训练入门动作', '1.坐姿，双手宽握把手\n2.拉向下胸部\n3.控制回放', 1, 12),
('坐姿划船', 'Seated Cable Row', '背部', '背阔肌,中背部', '肱二头肌', '坐姿划船器', 1, '中背部训练', '1.坐姿，脚踏踏板\n2.拉向腹部\n3.挺胸收肩', 1, 13),
('哑铃单臂划船', 'Dumbbell Row', '背部', '背阔肌', '肱二头肌', '哑铃,卧推凳', 1, '单侧背部训练', '1.一手一膝支撑在凳上\n2.将哑铃拉向腰部\n3.控制下放', 1, 14),

-- 腿部
('杠铃深蹲', 'Barbell Squat', '腿部', '股四头肌,臀大肌', '腘绳肌,核心', '杠铃,深蹲架', 2, '腿部训练之王', '1.杠铃置于斜方肌上\n2.下蹲至大腿平行地面\n3.站起至起始位置', 1, 20),
('罗马尼亚硬拉', 'Romanian Deadlift', '腿部', '腘绳肌,臀大肌', '下背部', '杠铃', 2, '后链训练动作', '1.双手持杠铃站立\n2.屈髋向后推，微屈膝\n3.沿腿前下放杠铃', 1, 21),
('腿举', 'Leg Press', '腿部', '股四头肌,臀大肌', '腘绳肌', '腿举机', 1, '安全的腿部训练', '1.坐在腿举机上\n2.下放至膝盖约90度\n3.推起至腿伸直', 1, 22),
('腿弯举', 'Leg Curl', '腿部', '腘绳肌', '', '腿弯举机', 1, '腘绳肌孤立训练', '1.俯卧在器械上\n2.弯曲膝盖\n3.控制回放', 0, 23),
('保加利亚分腿蹲', 'Bulgarian Split Squat', '腿部', '股四头肌,臀大肌', '腘绳肌', '哑铃,卧推凳', 2, '单腿训练动作', '1.后脚搭在凳上\n2.下蹲至前腿约90度\n3.站起', 1, 24),
('小腿提踵', 'Calf Raise', '腿部', '腓肠肌', '', '提踵机', 1, '小腿训练', '1.站在提踵机上\n2.踮起脚尖\n3.缓慢放下', 0, 25),

-- 肩部
('杠铃推举', 'Overhead Press', '肩部', '三角肌', '肱三头肌,斜方肌', '杠铃', 2, '肩部基础训练', '1.双手持杠铃于锁骨位置\n2.推举过头\n3.控制下放', 1, 30),
('哑铃侧平举', 'Lateral Raise', '肩部', '三角肌中束', '', '哑铃', 1, '肩部孤立训练', '1.双手持哑铃于体侧\n2.向两侧举起至肩平\n3.控制下放', 0, 31),
('哑铃前平举', 'Front Raise', '肩部', '三角肌前束', '', '哑铃', 1, '前束训练', '1.双手持哑铃于大腿前\n2.向前举起至肩平\n3.控制下放', 0, 32),
('俯身飞鸟', 'Reverse Fly', '肩部', '三角肌后束', '斜方肌', '哑铃', 1, '后束训练', '1.俯身，双手持哑铃\n2.向两侧展开\n3.控制收拢', 0, 33),
('面拉', 'Face Pull', '肩部', '三角肌后束', '斜方肌', '绳索', 1, '肩部健康训练', '1.绳索设在面部高度\n2.拉向面部两侧\n3.外旋手臂', 0, 34),

-- 手臂
('杠铃弯举', 'Barbell Curl', '手臂', '肱二头肌', '前臂', '杠铃', 1, '二头肌基础训练', '1.双手持杠铃于体前\n2.弯举至肩部\n3.控制下放', 0, 40),
('锤式弯举', 'Hammer Curl', '手臂', '肱二头肌,肱肌', '前臂', '哑铃', 1, '锤式握法弯举', '1.双手持哑铃，掌心相对\n2.弯举至肩部\n3.控制下放', 0, 41),
('三头绳索下压', 'Tricep Pushdown', '手臂', '肱三头肌', '', '绳索', 1, '三头肌训练', '1.面对绳索机，双手握把\n2.下压至手臂伸直\n3.控制回放', 0, 42),
('仰卧臂屈伸', 'Skull Crusher', '手臂', '肱三头肌', '', '杠铃/哑铃', 2, '三头肌训练', '1.仰卧，双手持杠铃\n2.下放至额头位置\n3.伸直手臂', 0, 43),

-- 核心
('平板支撑', 'Plank', '核心', '腹直肌,腹横肌', '全身', '自重', 1, '核心稳定训练', '1.前臂撑地，身体成一条直线\n2.收紧核心\n3.保持姿势', 0, 50),
('卷腹', 'Crunch', '核心', '腹直肌', '', '自重', 1, '基础腹肌训练', '1.仰卧，双手置于耳侧\n2.卷起上背部\n3.缓慢放下', 0, 51),
('悬垂举腿', 'Hanging Leg Raise', '核心', '腹直肌,腹斜肌', '', '单杠', 2, '高级腹肌训练', '1.悬挂在单杠上\n2.举起双腿至平行\n3.控制下放', 0, 52),
('俄罗斯转体', 'Russian Twist', '核心', '腹斜肌', '腹直肌', '自重/哑铃', 1, '腹斜肌训练', '1.坐地，上身后倾\n2.双脚离地\n3.左右转动躯干', 0, 53),

-- 有氧
('跑步', 'Running', '有氧', '全身', '', '跑步机/户外', 1, '经典有氧运动', '保持中等强度跑步', 1, 60),
('划船机', 'Rowing', '有氧', '全身', '', '划船机', 1, '全身有氧训练', '保持节奏划船', 1, 61),
('跳绳', 'Jump Rope', '有氧', '小腿,核心', '', '跳绳', 1, '高效有氧训练', '保持节奏跳跃', 0, 62),
('椭圆机', 'Elliptical', '有氧', '全身', '', '椭圆机', 1, '低冲击有氧', '保持中等强度', 1, 63),
('爬坡', 'Incline Walk', '有氧', '臀大肌,股四头肌', '小腿,核心', '跑步机', 1, '坡度行走，高效燃脂', '1.跑步机设置坡度8-15%\n2.保持中等步速\n3.身体略前倾\n4.持续20-40分钟', 1, 64);

-- -------------------------------------------
-- 初始数据 - 食物库
-- -------------------------------------------
INSERT INTO `food` (`name`, `category`, `calories`, `protein`, `carbs`, `fat`, `fiber`, `serving_size`, `serving_unit`) VALUES
-- 主食
('白米饭', '主食', 116, 2.6, 25.9, 0.3, 0.3, 100, 'g'),
('糙米饭', '主食', 111, 2.5, 23.0, 0.9, 1.6, 100, 'g'),
('全麦面包', '主食', 246, 12.0, 41.0, 3.5, 6.0, 100, 'g'),
('燕麦', '主食', 367, 13.5, 67.0, 6.5, 10.0, 100, 'g'),
('红薯', '主食', 86, 1.6, 20.1, 0.1, 3.0, 100, 'g'),
('土豆', '主食', 77, 2.0, 17.5, 0.1, 2.2, 100, 'g'),
('意大利面(熟)', '主食', 131, 5.0, 25.0, 1.1, 1.8, 100, 'g'),
('馒头', '主食', 221, 7.0, 44.2, 1.1, 1.3, 100, 'g'),

-- 肉类
('鸡胸肉', '肉类', 165, 31.0, 0, 3.6, 0, 100, 'g'),
('鸡腿肉', '肉类', 177, 26.0, 0, 7.2, 0, 100, 'g'),
('牛里脊', '肉类', 140, 26.0, 0, 4.0, 0, 100, 'g'),
('牛腱子', '肉类', 106, 20.0, 0, 2.0, 0, 100, 'g'),
('猪里脊', '肉类', 143, 21.0, 0, 6.0, 0, 100, 'g'),
('三文鱼', '肉类', 208, 20.0, 0, 13.0, 0, 100, 'g'),
('虾仁', '肉类', 85, 18.0, 0.8, 1.0, 0, 100, 'g'),
('金枪鱼(罐头)', '肉类', 116, 26.0, 0, 1.0, 0, 100, 'g'),
('瘦牛肉', '肉类', 190, 26.0, 0, 9.0, 0, 100, 'g'),

-- 蛋类奶类
('鸡蛋(全蛋)', '蛋奶', 144, 13.0, 1.1, 10.0, 0, 100, 'g'),
('鸡蛋白', '蛋奶', 47, 11.0, 0.7, 0.2, 0, 100, 'g'),
('脱脂牛奶', '蛋奶', 34, 3.4, 5.0, 0.1, 0, 100, 'ml'),
('全脂牛奶', '蛋奶', 61, 3.2, 4.8, 3.3, 0, 100, 'ml'),
('希腊酸奶', '蛋奶', 59, 10.0, 3.6, 0.7, 0, 100, 'g'),
('低脂酸奶', '蛋奶', 63, 5.0, 7.0, 1.5, 0, 100, 'g'),

-- 蔬菜
('西兰花', '蔬菜', 34, 2.8, 6.6, 0.4, 2.6, 100, 'g'),
('菠菜', '蔬菜', 23, 2.9, 3.6, 0.4, 2.2, 100, 'g'),
('番茄', '蔬菜', 18, 0.9, 3.9, 0.2, 1.2, 100, 'g'),
('黄瓜', '蔬菜', 16, 0.7, 3.6, 0.1, 0.5, 100, 'g'),
('胡萝卜', '蔬菜', 41, 0.9, 9.6, 0.2, 2.8, 100, 'g'),
('生菜', '蔬菜', 15, 1.4, 2.9, 0.2, 1.3, 100, 'g'),
('芦笋', '蔬菜', 20, 2.2, 3.9, 0.1, 2.1, 100, 'g'),
('秋葵', '蔬菜', 33, 1.9, 7.0, 0.1, 3.2, 100, 'g'),

-- 水果
('苹果', '水果', 52, 0.3, 13.8, 0.2, 2.4, 100, 'g'),
('香蕉', '水果', 89, 1.1, 22.8, 0.3, 2.6, 100, 'g'),
('蓝莓', '水果', 57, 0.7, 14.5, 0.3, 2.4, 100, 'g'),
('橙子', '水果', 47, 0.9, 11.8, 0.1, 2.4, 100, 'g'),
('猕猴桃', '水果', 61, 1.1, 14.7, 0.5, 3.0, 100, 'g'),
('草莓', '水果', 32, 0.7, 7.7, 0.3, 2.0, 100, 'g'),

-- 坚果
('杏仁', '坚果', 579, 21.0, 22.0, 50.0, 12.0, 100, 'g'),
('核桃', '坚果', 654, 15.0, 14.0, 65.0, 7.0, 100, 'g'),
('花生', '坚果', 567, 26.0, 16.0, 49.0, 9.0, 100, 'g'),
('腰果', '坚果', 553, 18.0, 30.0, 44.0, 3.3, 100, 'g'),

-- 调味品/其他
('橄榄油', '调味品', 884, 0, 0, 100.0, 0, 10, 'ml'),
('蜂蜜', '调味品', 304, 0.3, 82.0, 0, 0.2, 10, 'g'),
('蛋白粉(乳清)', '补剂', 370, 80.0, 8.0, 2.0, 0, 30, 'g'),
('花生酱', '调味品', 588, 25.0, 20.0, 50.0, 6.0, 15, 'g');

-- -------------------------------------------
-- 初始数据 - 成就
-- -------------------------------------------
INSERT INTO `achievement` (`name`, `description`, `icon`, `category`, `condition_type`, `condition_value`, `points`, `rarity`, `sort_order`) VALUES
('初次训练', '完成你的第一次训练记录', '🏋️', '训练', 'training_count', 1, 10, 1, 1),
('训练新手', '累计完成10次训练', '💪', '训练', 'training_count', 10, 30, 1, 2),
('训练达人', '累计完成50次训练', '🔥', '训练', 'training_count', 50, 100, 2, 3),
('训练狂人', '累计完成100次训练', '⚡', '训练', 'training_count', 100, 200, 3, 4),
('训练传奇', '累计完成500次训练', '👑', '训练', 'training_count', 500, 500, 4, 5),
('连续3天', '连续训练3天', '📅', '打卡', 'streak_days', 3, 20, 1, 10),
('连续7天', '连续训练7天', '🗓️', '打卡', 'streak_days', 7, 50, 2, 11),
('连续30天', '连续训练30天', '🏆', '打卡', 'streak_days', 30, 200, 3, 12),
('连续100天', '连续训练100天', '💎', '打卡', 'streak_days', 100, 500, 4, 13),
('卧推之王', '卧推重量达到100kg', '👑', '力量', 'bench_press', 100, 150, 3, 20),
('深蹲大师', '深蹲重量达到140kg', '🦵', '力量', 'squat', 140, 200, 3, 21),
('硬拉勇士', '硬拉重量达到180kg', '🛡️', '力量', 'deadlift', 180, 250, 4, 22),
('饮食记录新手', '记录第一餐饮食', '🍽️', '饮食', 'diet_count', 1, 10, 1, 30),
('饮食达人', '累计记录100餐饮食', '🥗', '饮食', 'diet_count', 100, 100, 2, 31),
('蛋白质达标', '单日蛋白质摄入达标', '🥩', '饮食', 'protein_target', 1, 20, 1, 32),
('早起鸟', '在早上7点前完成训练', '🐦', '特殊', 'early_bird', 1, 30, 2, 40),
('夜猫子', '在晚上10点后训练', '🦉', '特殊', 'night_owl', 1, 30, 2, 41),
('周末战士', '在周末完成训练', '⚔️', '特殊', 'weekend_warrior', 1, 20, 1, 42);

-- -------------------------------------------
-- 初始管理员用户 (密码: admin123)
-- -------------------------------------------
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `gender`, `height`, `weight`, `fitness_goal`, `daily_calorie_target`, `daily_protein_target`, `daily_carb_target`, `daily_fat_target`) VALUES
('admin', '$2a$10$XviTz19KOp3aUzOydF4SRef8T7i1UJ4fTtN9tWPot2hvO8ViY3Ikm', '管理员', 'admin@fittracker.com', 1, 175.0, 75.0, 2, 2000, 150, 250, 65);


delete  from exercise_library;