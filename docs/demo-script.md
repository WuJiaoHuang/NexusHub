# CampusLink 60 秒面试演示路线

## 0-10 秒：首页

打开 H5 或微信小程序，停留在首页。

讲法：CampusLink 不是 Landing Page，学生打开后直接看到今日校园、今日活动和校园动态。

## 10-20 秒：校园动态

下滑查看 Feed Card，点击点赞。

讲法：点赞使用前端乐观更新，后端真实接口是 `PUT /posts/like/{id}`，失败会回滚。

## 20-30 秒：地点发现

切到发现页，展示分类和附近地点。

讲法：分类来自 `/place-categories/list`，附近地点来自 Redis GEO 支撑的 `/places/nearby`；定位失败会回退到默认校园坐标。

## 30-45 秒：活动详情与报名

切到活动页，进入 Java 后端技术分享会。

讲法：活动推荐是规则推荐，不写 AI 推荐；活动封面使用 Typography Poster，避免随机图。

## 45-55 秒：异步报名状态

点击立即报名，展示“报名请求已提交”和处理中状态。

讲法：后端是 Redis Lua 预扣减、RabbitMQ 异步落库，所以前端不能 POST 成功就显示报名成功，必须轮询 `/events/{id}/registrations/status`。

## 55-60 秒：兴趣推荐

进入我的兴趣，选择 Java、AI、篮球。

讲法：兴趣保存到 `/users/interests`，用于规则推荐解释，产品文案保持“根据你的兴趣推荐”。
