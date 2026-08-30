USE cityeats;

INSERT INTO tb_user (id, phone, nick_name, icon) VALUES
(1, '13800000001', 'CityEats用户1', ''),
(2, '13800000002', 'CityEats用户2', '');

INSERT INTO tb_user_info (user_id, city, introduce, fans, followee, gender, birthday, credits, level) VALUES
(1, '上海', '喜欢探店和咖啡', 0, 0, 0, '2000-01-01', 100, 0),
(2, '上海', '本地生活体验官', 0, 0, 1, '2000-02-02', 80, 0);

INSERT INTO tb_shop_type (id, name, icon, sort) VALUES
(1, '咖啡饮品', '/types/coffee.png', 1),
(2, '特色餐厅', '/types/food.png', 2);

INSERT INTO tb_shop (id, name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours) VALUES
(1, '梧桐巷咖啡', 1, '/shops/coffee-1.png', '徐汇', '衡山路 100 号', 121.4462, 31.2043, 38, 1200, 218, 46, '09:00-22:00'),
(2, '江畔小馆', 2, '/shops/food-1.png', '浦东', '滨江大道 88 号', 121.5063, 31.2397, 96, 980, 176, 45, '10:30-21:30');

INSERT INTO tb_blog (id, shop_id, user_id, title, images, content, liked, comments) VALUES
(1, 1, 1, '周末咖啡路线', '/blogs/coffee.png', '手冲稳定，甜点也不错。', 10, 0);

INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status) VALUES
(1, 1, '咖啡立减券', '满 30 减 10', '单用户限购一张', 3000, 2000, 1, 1);

INSERT INTO tb_seckill_voucher (voucher_id, stock, begin_time, end_time) VALUES
(1, 20, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY));
