-- 仅供全新环境的模拟数据：用户、地点、图片路径、活动与业务统计均为虚构，不代表真实服务。
SET NAMES utf8mb4;
USE nexushub;
SET @demo_now = NOW();

INSERT INTO tb_user (id, phone, nick_name, icon) VALUES
(1, '13800000001', '模拟用户1', ''),
(2, '13800000002', '模拟用户2', '');

INSERT INTO tb_user_info (user_id, city, introduce, fans, followee, gender, birthday, credits, level) VALUES
(1, '杭州', '喜欢 Java、篮球和志愿服务', 0, 0, 0, '2000-01-01', 100, 0),
(2, '杭州', '关注 AI、摄影和读书活动', 0, 0, 1, '2001-02-02', 80, 0);

INSERT INTO user_interest (id, user_id, tag) VALUES
(1, 1, 'Java'),
(2, 1, '篮球'),
(3, 1, '志愿服务'),
(4, 2, 'AI'),
(5, 2, '摄影');

INSERT INTO place_category (id, name, icon, sort) VALUES
(1, '餐饮美食', '/types/canteen.png', 1),
(2, '休闲娱乐', '/types/study.png', 2),
(3, '运动健身', '/types/sports.png', 3),
(4, '生活服务', '/types/service.png', 4);

INSERT INTO place (id, name, category_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours) VALUES
(1, '街角风味餐厅', 1, '/places/canteen.png', '东区', '东区生活广场一层', 120.1542, 30.2871, 18, 2600, 218, 46, '06:30-21:00'),
(2, '城市书咖', 2, '/places/library.png', '中心区', '文化路 8 号', 120.1584, 30.2859, 0, 3200, 176, 48, '08:00-22:30'),
(3, '城市运动中心', 3, '/places/gym.png', '西区', '体育路 12 号', 120.1498, 30.2848, 0, 1800, 96, 45, '09:00-21:30'),
(4, '社区服务中心', 4, '/places/express.png', '北区', '社区公寓 5 号楼旁', 120.1520, 30.2915, 0, 2100, 88, 44, '09:00-19:00');

INSERT INTO post (id, place_id, user_id, title, images, content, liked, comments) VALUES
(1, 2, 1, '城市书咖三楼安静角落推荐', '/posts/library.png', '靠窗区域插座多，适合阅读与交流。', 10, 0),
(2, NULL, 2, '城市摄影交流活动报名体验', '/posts/photo.png', '路线安排紧凑，适合想认识同好的人。', 8, 0);

INSERT INTO event (id, place_id, title, description, category, location, cover, tags, start_time, end_time, registration_start_time, registration_end_time, capacity, status) VALUES
(1, 4, 'Java 后端技术交流会', '围绕 Spring Boot、Redis 和高并发实践展开的社区技术交流。', '技术交流', '社区服务中心交流室', '/events/java.png', 'Java,后端,Redis', DATE_ADD(@demo_now, INTERVAL 3 DAY), DATE_ADD(@demo_now, INTERVAL 3 DAY) + INTERVAL 2 HOUR, DATE_SUB(@demo_now, INTERVAL 1 DAY), DATE_ADD(@demo_now, INTERVAL 2 DAY), 100, 1),
(2, 3, '城市周末篮球活动', '面向社区爱好者的 3v3 篮球赛。', '体育赛事', '城市运动中心', '/events/basketball.png', '篮球,体育', DATE_ADD(@demo_now, INTERVAL 7 DAY), DATE_ADD(@demo_now, INTERVAL 7 DAY) + INTERVAL 4 HOUR, DATE_SUB(@demo_now, INTERVAL 1 DAY), DATE_ADD(@demo_now, INTERVAL 5 DAY), 60, 1),
(3, 2, 'AI 技术主题沙龙', '介绍大模型应用、工程实践与社区实践项目。', '兴趣沙龙', '城市书咖报告厅', '/events/ai.png', 'AI,技术,读书', DATE_ADD(@demo_now, INTERVAL 5 DAY), DATE_ADD(@demo_now, INTERVAL 5 DAY) + INTERVAL 2 HOUR, DATE_SUB(@demo_now, INTERVAL 1 DAY), DATE_ADD(@demo_now, INTERVAL 4 DAY), 120, 1);

INSERT INTO event (id, place_id, title, description, category, location, cover, tags, start_time, end_time, registration_start_time, registration_end_time, capacity, status) VALUES
(4, 2, '城市摄影交流活动', '模拟活动：分享城市街拍经验，交流构图与光线技巧。', '兴趣沙龙', '城市书咖交流区', '', '摄影,城市生活', DATE_ADD(@demo_now, INTERVAL 6 DAY), DATE_ADD(@demo_now, INTERVAL 6 DAY) + INTERVAL 2 HOUR, DATE_SUB(@demo_now, INTERVAL 1 DAY), DATE_ADD(@demo_now, INTERVAL 5 DAY), 30, 1);

INSERT INTO event_capacity (event_id, remaining_slots, registration_start_time, registration_end_time)
SELECT id, capacity, registration_start_time, registration_end_time FROM event;
