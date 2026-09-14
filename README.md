# CampusLink

## 1. 项目简介

CampusLink 是一个面向高校学生的校园生活与活动社区后端项目。项目从本地生活点评业务重构为校园场景，支持校园地点浏览与搜索、附近设施检索、校园动态发布与点赞、用户关注 Feed、校园活动浏览与限额报名，以及基于兴趣标签、活动热度和时效性的轻量级规则活动推荐。

项目保留原有成熟的 Spring Boot + Redis 技术实现，以 MySQL 作为主数据源，以 Redis 支撑缓存、登录态、GEO、Bitmap、ZSet Feed 和高并发活动报名预扣减，以 RabbitMQ 异步削峰处理报名落库，以 Elasticsearch 构建校园地点搜索索引。

## 2. 核心功能

- 用户认证：手机号验证码登录，token 存入 Redis，拦截器恢复用户上下文。
- 校园地点：浏览地点详情、按分类查询、按位置查询附近校园设施。
- 校园地点搜索：基于 Elasticsearch 索引 `campuslink_place`，支持关键词、多条件筛选、分页和排序。
- 校园社区：发布校园动态、查询热门动态、点赞动态、查看动态点赞用户。
- 关注 Feed：发布动态后推送到粉丝 inbox ZSet，关注用户按时间线滚动分页查看动态。
- 校园活动：浏览活动、查看活动详情、按校园地点查看相关活动。
- 高并发活动报名：Redis Lua 原子校验剩余名额与重复报名，RabbitMQ 异步创建报名记录，数据库唯一索引兜底。
- 用户兴趣标签：用户可设置 Java、AI、篮球、摄影等兴趣标签。
- 活动推荐：基于兴趣标签、报名热度和活动时效性的轻量级规则推荐，不依赖 AI 模型。

## 3. 技术栈

```text
Spring Boot 2.3.12
Spring MVC
MyBatis-Plus
MySQL 8
Redis 6
RabbitMQ 3.12
Redisson
Elasticsearch 7.6.2
RestHighLevelClient
Lua
Hutool
Lombok
JUnit 5
H2 Test Database
Docker Compose
```

## 4. 项目架构

```text
Client
  ↓
Spring Boot API
  ↓
Controller
  ↓
Service / Domain Event / Listener
  ↓
MyBatis-Plus / Redis / RabbitMQ / Elasticsearch
```

```text
校园地点缓存
Client -> /places/{id} -> Redis Cache -> MySQL -> Redis Cache
```

```text
附近校园地点
Client -> /places/nearby -> Redis GEO geo:place:type:{categoryId} -> MySQL 回表排序
```

```text
关注 Feed
CampusPost 发布 -> 查询粉丝 -> Redis ZSet feed:{userId} -> /feeds 滚动分页
```

```text
热门活动报名
Client -> /events/{eventId}/registrations -> Lua 原子校验 -> RabbitMQ -> Redisson 锁 -> MySQL 落库
```

## 5. Redis 使用场景

- 缓存校园地点详情：`cache:place:{id}`，减少热点地点的 MySQL 压力。
- 缓存地点分类：`cache:place:type`，加速分类列表读取。
- 登录态：`login:token:{token}` 保存登录用户摘要信息。
- GEO 附近地点：`geo:place:type:{categoryId}` 保存校园地点坐标并按距离查询。
- Bitmap 签到：`sign:{userId}:{yyyyMM}` 保存每日签到状态。
- ZSet 动态点赞：`post:liked:{postId}` 保存点赞用户和点赞时间。
- ZSet 关注 Feed：`feed:{userId}` 以发布时间戳作为 score 实现时间线。
- 活动报名容量控制：`event:slots:{eventId}` 与 `event:registration:{eventId}` 配合 Lua 原子预扣减。

CampusLink 对校园地点等高频读取数据进行 Redis 缓存，通过空值缓存缓解缓存穿透，并通过互斥重建或逻辑过期策略降低热点校园地点的缓存击穿风险。

CampusLink 使用 Redis ZSet 实现基于时间线的关注 Feed 流，并通过 score + offset 实现滚动分页，解决传统 offset 分页在动态内容持续插入时可能产生的重复或遗漏问题。

## 6. 高并发活动报名流程

```text
请求报名
  ↓
RegistrationValidationChain 前置校验活动存在与报名时间窗口
  ↓
Lua 原子检查活动剩余名额
  ↓
Lua 原子检查用户是否已报名
  ↓
Redis 扣减剩余名额并记录用户报名标记
  ↓
发送 EventRegistrationMessage 到 RabbitMQ
  ↓
Consumer 使用 Redisson 用户-活动维度锁
  ↓
MySQL 条件扣减 event_capacity.remaining_slots
  ↓
插入 event_registration
```

`event_capacity.remaining_slots` 是活动报名容量的唯一数据库事实来源，`campus_event` 只保存活动基础展示信息和总容量。数据库唯一索引 `uk_user_event(user_id, event_id)` 作为“同一用户同一活动只能报名一次”的最终兜底。数据库扣减使用条件更新：

```sql
UPDATE event_capacity
SET remaining_slots = remaining_slots - 1
WHERE event_id = ?
  AND remaining_slots > 0;
```

失败补偿通过 `compensate_event_registration.lua` 原子恢复 Redis 剩余名额、删除报名标记，并写入补偿幂等标记 `event:registration:compensated:{registrationId}`。

## 7. 项目亮点

- 以校园地点、校园动态、校园活动报名构成新的业务闭环。
- Redis 缓存、GEO、Bitmap、ZSet、Lua 多场景组合应用。
- ZSet Feed 使用 score + offset 支持滚动分页。
- Redis GEO 支持按分类检索附近食堂、自习室、运动场馆、校园服务点。
- Lua + RabbitMQ + Redisson + MySQL 唯一索引共同支撑热门活动限额报名。
- Elasticsearch 支持校园地点搜索索引的全量重建与增量同步。
- 基于兴趣标签、活动热度、报名时效性的轻量级规则活动推荐。
- 媒体上传：支持头像与动态图片上传，开发默认使用 mock 地址，生产可切换到阿里云 OSS。

## 8. API 示例

认证：

```text
POST /auth/code
POST /auth/login
POST /auth/logout
GET  /users/me
PUT  /users/avatar
PUT  /users/profile
```

媒体上传：

```text
POST /uploads/image
Content-Type: multipart/form-data
fields: file, scene
scene: AVATAR | POST | EVENT | PLACE
```

校园地点：

```text
GET  /places
POST /places
GET  /places/{id}
PUT  /places
GET  /places/nearby?categoryId=1&current=1&x=120.1542&y=30.2871
GET  /places/search?keyword=图书馆&current=1&size=10
GET  /place-categories/list
```

校园动态与 Feed：

```text
POST /posts
GET  /posts/{id}
GET  /posts/hot
PUT  /posts/like/{id}
GET  /posts/likes/{id}
GET  /feeds?lastId=1690000000000&offset=0
```

关注：

```text
PUT /follows/{userId}/{isFollow}
GET /follows/or/not/{userId}
GET /follows/common/{userId}
```

校园活动：

```text
GET  /events
GET  /events/{id}
POST /events
GET  /events/hot
GET  /events/by-place/{placeId}
POST /events/{eventId}/registrations
GET  /events/{eventId}/registrations/status
GET  /events/recommendations?limit=10
GET  /event-registrations/me
```

用户兴趣：

```text
PUT /users/interests
GET /users/interests
```

搜索索引维护：

```text
POST /admin/search/places/rebuild
```

## 9. 数据库

初始化脚本：

```text
sql/schema.sql
sql/data.sql
```

核心表：

- `tb_user`：用户基础信息。
- `tb_user_info`：用户扩展信息。
- `user_interest`：用户兴趣标签，唯一索引 `uk_user_interest(user_id, tag)`。
- `place_category`：校园地点分类。
- `campus_place`：校园地点。
- `campus_post`：校园动态，`place_id` 允许为空。
- `campus_post_comments`：校园动态评论。
- `tb_follow`：用户关注关系。
- `campus_event`：校园活动主数据。
- `event_capacity`：热门活动报名剩余名额和报名时间窗口。
- `event_registration`：活动报名记录，唯一索引 `uk_user_event(user_id, event_id)`。

## 10. Docker Quick Start

启动 MySQL、Redis、RabbitMQ、Elasticsearch：

```bash
docker compose up -d
```

默认宿主机端口：

```text
MySQL:         13306 -> 3306
Redis:         16379 -> 6379
RabbitMQ:      5673  -> 5672
RabbitMQ UI:   15673 -> 15672
Elasticsearch: 19200 -> 9200
```

默认开发配置：

```text
DB_NAME=campuslink
DB_PASSWORD=campuslink_dev
REDIS_PASSWORD=campuslink_dev
RABBITMQ_USERNAME=campuslink
RABBITMQ_PASSWORD=campuslink_dev
PLACE_CACHE_STRATEGY=PASS_THROUGH
PLACE_SEARCH_INDEX=campuslink_place
EVENT_SLOTS_WARMUP_ENABLED=true
ALIYUN_OSS_ENABLED=false
ALIYUN_OSS_ENDPOINT=https://oss-cn-beijing.aliyuncs.com
ALIYUN_OSS_BUCKET_NAME=java-ai-wujiaohuang
ALIYUN_OSS_PUBLIC_BASE_URL=https://java-ai-wujiaohuang.oss-cn-beijing.aliyuncs.com
```

PowerShell 启动应用：

```powershell
$env:DB_PORT="13306"
$env:DB_NAME="campuslink"
$env:DB_PASSWORD="campuslink_dev"
$env:REDIS_PORT="16379"
$env:REDIS_PASSWORD="campuslink_dev"
$env:RABBITMQ_PORT="5673"
$env:RABBITMQ_USERNAME="campuslink"
$env:RABBITMQ_PASSWORD="campuslink_dev"
$env:ELASTICSEARCH_PORT="19200"
mvn spring-boot:run
```

### 阿里云 OSS 媒体上传

默认 `ALIYUN_OSS_ENABLED=false`，后端会返回本地 mock URL，便于无云账号环境完成联调。生产或真实联调时设置：

```powershell
$env:ALIYUN_OSS_ENABLED="true"
$env:ALIYUN_OSS_ENDPOINT="https://oss-cn-beijing.aliyuncs.com"
$env:ALIYUN_OSS_BUCKET_NAME="java-ai-wujiaohuang"
$env:ALIYUN_OSS_PUBLIC_BASE_URL="https://java-ai-wujiaohuang.oss-cn-beijing.aliyuncs.com"
$env:ALIYUN_OSS_ACCESS_KEY_ID="<YOUR_KEY_ID>"
$env:ALIYUN_OSS_ACCESS_KEY_SECRET="<YOUR_KEY_SECRET>"
mvn spring-boot:run
```

上传接口要求登录态。普通用户仅允许 `AVATAR` 和 `POST` 场景，图片类型限制为 `jpg/jpeg/png/webp`；头像最大 5MB，动态图片最大 10MB。对象路径按业务隔离：头像写入 `avatars/{userId}/`，动态图片写入 `posts/{userId}/{yyyyMM}/`。更新头像时，后端会校验 URL 必须属于当前用户上传前缀，避免越权绑定他人图片。

## 11. 测试

```bash
mvn clean compile
mvn test
```

当前测试覆盖 Spring Context、Mapper 基础查询、RedisIdWorker、缓存策略、校园地点搜索转换/查询/同步事件、报名责任链、Lua 报名脚本、报名事务落库与 Redis 补偿，以及媒体上传校验、mock 存储、头像 URL 越权校验和上传 Controller。

## 12. CampusLink Frontend

本仓库新增 `frontend/`，提供一套面向微信小程序主端与 H5 Demo 的 Taro + React + TypeScript 前端。

```text
Spring Boot Backend
  ↓
REST API
  ↓
Taro Frontend
  ↙        ↘
WeChat     H5
```

### 前端能力

- 首页：今日校园、热门活动、校园动态。
- 发现：地点分类、附近地点、定位失败回退默认校园位置。
- 搜索：300ms debounce、历史搜索、地点实时结果。
- 动态：热门动态、发布文字动态、点赞乐观更新。
- 媒体：H5 与微信小程序选择图片、头像上传、动态图片并发上传、预览和删除。
- 活动：推荐、热门、全部活动，活动详情与异步报名轮询。
- 我的：登录、退出、我的活动、兴趣标签。
- H5 Demo Mode：`TARO_APP_DEMO_MODE=true` 时使用北邮校园语境 mock 数据。

### 启动

```bash
cd frontend
pnpm install
pnpm dev:weapp
pnpm dev:h5
```

### 构建

```bash
cd frontend
pnpm lint
pnpm test
pnpm build:weapp
pnpm build:h5
```

### 环境变量

复制 `frontend/.env.example` 并按环境调整：

```text
TARO_APP_API_BASE_URL=http://localhost:8080
TARO_APP_DEMO_MODE=false
```

生产微信小程序请求域名必须配置 HTTPS 合法域名。开发阶段可在微信开发者工具中关闭域名校验。

### 文档

- 前端真实接口契约：`docs/frontend-api-contract.md`
- 60 秒演示路线：`docs/demo-script.md`
- 面试讲解：`docs/frontend-interview.md`
