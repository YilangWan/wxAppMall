-- 创建数据库
CREATE DATABASE IF NOT EXISTS wxmall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE wxmall;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `role` tinyint(1) DEFAULT '0' COMMENT '用户角色：0-普通用户，1-管理员',
  `status` tinyint(1) DEFAULT '1' COMMENT '账号状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL COMMENT '商品ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `description` varchar(255) DEFAULT NULL COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '商品原价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '商品库存',
  `pic` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `sale` int(11) DEFAULT '0' COMMENT '商品销量',
  `unit` varchar(20) DEFAULT NULL COMMENT '商品单位',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '商品重量，默认为克',
  `keywords` varchar(255) DEFAULT NULL COMMENT '商品关键字',
  `detail` text COMMENT '商品详情',
  `category_id` bigint(20) DEFAULT NULL COMMENT '商品分类ID',
  `publish_status` tinyint(1) DEFAULT '0' COMMENT '上架状态：0->下架；1->上架',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint(20) NOT NULL COMMENT '分类ID',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父分类ID',
  `level` int(1) DEFAULT '1' COMMENT '分类层级',
  `visible` tinyint(1) DEFAULT '1' COMMENT '是否显示：0->不显示；1->显示',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 支付订单表
CREATE TABLE IF NOT EXISTS `pay_order` (
  [id](cci:1://file:///Users/yilangwan/Desktop/PersonalProject/AI/wxMallWindSurf/src/main/java/com/wxmall/service/impl/WxServiceImpl.java:133:4-143:5) bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付订单ID',
  `order_id` bigint(20) NOT NULL COMMENT '商城订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付订单号',
  `out_trade_no` varchar(64) NOT NULL COMMENT '商户订单号',
  `total_fee` int(11) NOT NULL COMMENT '订单金额(分)',
  `body` varchar(128) NOT NULL COMMENT '商品描述',
  `status` tinyint(1) DEFAULT '0' COMMENT '支付状态：0-未支付，1-支付成功，2-支付失败，3-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY ([id](cci:1://file:///Users/yilangwan/Desktop/PersonalProject/AI/wxMallWindSurf/src/main/java/com/wxmall/service/impl/WxServiceImpl.java:133:4-143:5)),
  UNIQUE KEY `idx_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `pay_log` (
  [id](cci:1://file:///Users/yilangwan/Desktop/PersonalProject/AI/wxMallWindSurf/src/main/java/com/wxmall/service/impl/WxServiceImpl.java:133:4-143:5) bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付日志ID',
  `pay_order_id` bigint(20) NOT NULL COMMENT '支付订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` tinyint(1) NOT NULL COMMENT '类型：1-支付，2-退款',
  `status` tinyint(1) NOT NULL COMMENT '状态：0-失败，1-成功',
  `amount` int(11) NOT NULL COMMENT '金额(分)',
  `content` text COMMENT '详细内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY ([id](cci:1://file:///Users/yilangwan/Desktop/PersonalProject/AI/wxMallWindSurf/src/main/java/com/wxmall/service/impl/WxServiceImpl.java:133:4-143:5)),
  KEY `idx_pay_order_id` (`pay_order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';