INSERT INTO tb_user (id, phone, nick_name, icon) VALUES (1, '13800000001', '测试用户', '');
INSERT INTO tb_shop_type (id, name, icon, sort) VALUES (1, '咖啡饮品', '/types/coffee.png', 1);
INSERT INTO tb_shop (id, name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours)
VALUES (1, '梧桐巷咖啡', 1, '/shops/coffee.png', '徐汇', '衡山路 100 号', 121.4462, 31.2043, 38, 1200, 218, 46, '09:00-22:00');
INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status)
VALUES (1, 1, '咖啡立减券', '满 30 减 10', '单用户限购一张', 3000, 2000, 1, 1);
INSERT INTO tb_seckill_voucher (voucher_id, stock, begin_time, end_time)
VALUES (1, 20, '2026-01-01 00:00:00', '2030-01-01 00:00:00');
