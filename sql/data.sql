USE campuslink;

INSERT INTO tb_user (id, phone, nick_name, icon) VALUES
(1, '13800000001', '校园用户1', ''),
(2, '13800000002', '校园用户2', '');

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
(1, '食堂', '/types/canteen.png', 1),
(2, '自习空间', '/types/study.png', 2),
(3, '运动场馆', '/types/sports.png', 3),
(4, '校园服务', '/types/service.png', 4);

INSERT INTO campus_place (id, name, category_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours) VALUES
(1, '第一食堂', 1, '/places/canteen.png', '东区', '东区生活广场一层', 120.1542, 30.2871, 18, 2600, 218, 46, '06:30-21:00'),
(2, '图书馆', 2, '/places/library.png', '中心区', '求知路 8 号', 120.1584, 30.2859, 0, 3200, 176, 48, '08:00-22:30'),
(3, '综合体育馆', 3, '/places/gym.png', '西区', '体育路 12 号', 120.1498, 30.2848, 0, 1800, 96, 45, '09:00-21:30'),
(4, '快递服务中心', 4, '/places/express.png', '北区', '学生公寓 5 号楼旁', 120.1520, 30.2915, 0, 2100, 88, 44, '09:00-19:00');

INSERT INTO campus_post (id, place_id, user_id, title, images, content, liked, comments) VALUES
(1, 2, 1, '图书馆三楼安静角落推荐', '/posts/library.png', '靠窗区域插座多，适合下午复习。', 10, 0),
(2, NULL, 2, '摄影社秋季采风报名体验', '/posts/photo.png', '路线安排紧凑，适合想认识同好的人。', 8, 0);

INSERT INTO campus_event (id, place_id, title, description, category, location, cover, tags, start_time, end_time, registration_start_time, registration_end_time, capacity, remaining_slots, status) VALUES
(1, 4, 'Java 后端技术分享会', '围绕 Spring Boot、Redis 和高并发实践展开的校园技术分享。', '技术讲座', '创新实验室 201', '/events/java.png', 'Java,后端,Redis', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 100, 100, 1),
(2, 3, '校园篮球联赛', '面向全校学生的 3v3 篮球赛。', '体育赛事', '综合体育馆', '/events/basketball.png', '篮球,体育', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 4 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 60, 60, 1),
(3, 2, '人工智能学术讲座', '介绍大模型应用、科研方法与校园创新项目。', '学术讲座', '图书馆报告厅', '/events/ai.png', 'AI,科研,读书', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 2 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 120, 120, 1);

INSERT INTO event_capacity (event_id, remaining_slots, registration_start_time, registration_end_time) VALUES
(1, 100, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY)),
(2, 60, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY)),
(3, 120, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY));
