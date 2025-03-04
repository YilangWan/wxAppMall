USE wxmall;

-- 插入用户数据（密码使用BCrypt加密，这里的密码都是"123456"）
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `phone`, `email`, `role`, `status`, `create_time`, `update_time`, `last_login_time`, `deleted`, `version`)
VALUES
('admin', '$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy', '系统管理员', 'https://img.example.com/avatar/admin.jpg', '13800138000', 'admin@wxmall.com', 1, 1, NOW(), NOW(), NULL, 0, 1),
('user1', '$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy', '测试用户1', 'https://img.example.com/avatar/user1.jpg', '13800138001', 'user1@wxmall.com', 0, 1, NOW(), NOW(), NULL, 0, 1),
('user2', '$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy', '测试用户2', 'https://img.example.com/avatar/user2.jpg', '13800138002', 'user2@wxmall.com', 0, 1, NOW(), NOW(), NULL, 0, 1);

-- 插入商品分类数据
INSERT INTO `product_category` (`id`, `name`, `parent_id`, `level`, `visible`, `sort`, `icon`, `description`, `create_time`, `update_time`, `deleted`, `version`)
VALUES
(1, '手机数码', 0, 1, 1, 1, 'https://img.example.com/icons/phone.png', '手机、平板电脑、数码产品', NOW(), NOW(), 0, 1),
(2, '家用电器', 0, 1, 1, 2, 'https://img.example.com/icons/appliance.png', '电视、冰箱、洗衣机等家用电器', NOW(), NOW(), 0, 1),
(3, '电脑办公', 0, 1, 1, 3, 'https://img.example.com/icons/computer.png', '笔记本电脑、台式机、办公设备', NOW(), NOW(), 0, 1),
(4, '服装鞋包', 0, 1, 1, 4, 'https://img.example.com/icons/clothing.png', '男装、女装、童装、鞋靴、箱包', NOW(), NOW(), 0, 1),
(5, '食品生鲜', 0, 1, 1, 5, 'https://img.example.com/icons/food.png', '零食、生鲜、特产', NOW(), NOW(), 0, 1),
(101, '手机', 1, 2, 1, 1, 'https://img.example.com/icons/smartphone.png', '智能手机', NOW(), NOW(), 0, 1),
(102, '平板电脑', 1, 2, 1, 2, 'https://img.example.com/icons/tablet.png', '平板电脑', NOW(), NOW(), 0, 1),
(103, '智能穿戴', 1, 2, 1, 3, 'https://img.example.com/icons/wearable.png', '智能手表、手环', NOW(), NOW(), 0, 1),
(104, '摄影摄像', 1, 2, 1, 4, 'https://img.example.com/icons/camera.png', '相机、摄像机', NOW(), NOW(), 0, 1);

-- 插入测试商品数据
INSERT INTO `product` (`id`, `name`, `description`, `price`, `original_price`, `stock`, `pic`, `sale`, `unit`, `weight`, `keywords`, `detail`, `category_id`, `publish_status`, `create_time`, `update_time`, `deleted`, `version`)
VALUES
(10001, 'iPhone 13 Pro', 'iPhone 13 Pro 256G 远峰蓝', 7999.00, 8999.00, 100, 'https://img.example.com/iphone13.jpg', 0, '台', 220.00, 'iPhone,苹果,手机', 'iPhone 13 Pro详细介绍...', 101, 1, NOW(), NOW(), 0, 1),
(10002, '华为Mate 40 Pro', '华为Mate 40 Pro 256G 黑色', 6999.00, 7999.00, 50, 'https://img.example.com/mate40.jpg', 0, '台', 200.00, '华为,Mate,手机', '华为Mate 40 Pro详细介绍...', 101, 1, NOW(), NOW(), 0, 1),
(10003, '小米12', '小米12 256G 蓝色', 3999.00, 4999.00, 200, 'https://img.example.com/mi12.jpg', 0, '台', 180.00, '小米,手机', '小米12详细介绍...', 101, 1, NOW(), NOW(), 0, 1),
(10004, '苹果iPad Pro', 'iPad Pro 11英寸 256G WLAN版', 6299.00, 6799.00, 80, 'https://img.example.com/ipadpro.jpg', 0, '台', 470.00, 'iPad,苹果,平板', 'iPad Pro详细介绍...', 102, 1, NOW(), NOW(), 0, 1),
(10005, '华为MatePad Pro', 'MatePad Pro 10.8英寸 256G WLAN版', 3799.00, 4299.00, 60, 'https://img.example.com/matepad.jpg', 0, '台', 460.00, '华为,平板', 'MatePad Pro详细介绍...', 102, 1, NOW(), NOW(), 0, 1),
(10006, 'Apple Watch Series 7', 'Apple Watch Series 7 GPS版 45mm', 3199.00, 3499.00, 120, 'https://img.example.com/applewatch.jpg', 0, '个', 38.00, 'Apple Watch,苹果,智能手表', 'Apple Watch Series 7详细介绍...', 103, 1, NOW(), NOW(), 0, 1),
(10007, '华为Watch GT3', '华为Watch GT3 46mm 黑色', 1499.00, 1699.00, 150, 'https://img.example.com/huaweiwatch.jpg', 0, '个', 42.00, '华为,智能手表', '华为Watch GT3详细介绍...', 103, 1, NOW(), NOW(), 0, 1),
(10008, '佳能EOS R5', '佳能EOS R5 全画幅专业微单', 25999.00, 27999.00, 30, 'https://img.example.com/canonr5.jpg', 0, '台', 650.00, '佳能,相机,微单', '佳能EOS R5详细介绍...', 104, 1, NOW(), NOW(), 0, 1);
