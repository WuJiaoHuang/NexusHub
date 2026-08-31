# CityEats

## Project Overview

CityEats 是一个面向本地生活场景的 Spring Boot 后端项目，覆盖用户登录、商户查询、附近商户、签到、关注 Feed、优惠券秒杀、异步订单和商户全文搜索等业务链路。项目以 MySQL 作为主数据源，以 Redis 支撑缓存、GEO、Bitmap、ZSet 和高并发秒杀预扣减，以 RabbitMQ 异步削峰创建订单，以 Elasticsearch 构建商户搜索索引。

## Tech Stack

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

## Architecture

```text
Client
  ↓
Spring Boot Web
  ↓
Controller
  ↓
Service / Domain Event / Listener
  ↓
MyBatis-Plus / Redis / RabbitMQ / Elasticsearch
```

数据职责：

- MySQL：业务主存储和最终事实源。
- Redis：缓存、附近商户 GEO、签到 Bitmap、Feed/点赞 ZSet、秒杀库存预扣减。
- RabbitMQ：秒杀订单异步化、削峰和失败转入死信队列。
- Elasticsearch：仅用于商户搜索索引，可通过 MySQL 全量重建。

## Core Features

- 用户登录：手机号验证码登录，登录 token 存入 Redis，拦截器恢复用户上下文。
- 商户查询：商户详情走 Redis 缓存策略，未命中查询 MySQL，不存在数据缓存空值防止缓存穿透。
- 商户搜索：基于 Elasticsearch 索引 `cityeats_shop`，支持关键词、多条件筛选、分页和排序。
- 商户类型：按 sort 排序查询，并缓存到 Redis。
- 附近商户：启动时将商户坐标写入 Redis GEO，接口按距离查询并保持排序。
- 签到：使用 Redis Bitmap 记录每日签到，并通过 BITFIELD 统计连续签到天数。
- 关注：支持关注、取关、是否关注和共同关注。
- Feed：发布博客后推送到粉丝 inbox ZSet，拉取时使用滚动分页。
- 点赞：使用 Redis ZSet 记录点赞用户和时间顺序。
- 秒杀：Java 责任链做前置校验，Redis Lua 原子判断库存和一人一单，成功后发送 RabbitMQ 消息。
- 异步订单：RabbitMQ Consumer 在 Redisson 锁保护下执行 MySQL 条件扣库存和订单插入。
- 失败补偿：Consumer 有限重试后进入 DLQ，死信消费者先做订单幂等检查，再通过 Lua 原子恢复 Redis 库存和购买标记。

## Redis Usage

- Cache：商户详情支持缓存穿透、互斥重建和逻辑过期三种查询策略。
- GEO：附近商户使用 `geo:shop:type:{typeId}` 存储坐标并按距离查询。
- Bitmap：签到使用 `sign:{userId}:{yyyyMM}` 记录每日签到并统计连续签到。
- ZSet：Feed 使用 `feed:{userId}`，点赞使用 `blog:liked:{blogId}`。
- Lua：秒杀使用 Redis Lua 原子完成库存判断、重复购买判断和预扣减。

## Elasticsearch Search

搜索接口：

```text
GET /shop/search
```

支持参数：

```text
keyword   关键词，搜索 name / description / address / area
typeId    商户类型精确过滤
area      商圈精确过滤
minPrice  最低人均价格
maxPrice  最高人均价格
minScore  最低评分
current   页码，默认 1
size      每页大小，默认 10，最大 50
sort      score_desc / sold_desc / avgPrice_asc / avgPrice_desc
```

示例：

```bash
curl "http://localhost:8080/shop/search?keyword=烧烤&typeId=1&area=浦东&current=1&size=10&sort=score_desc"
```

返回对象不会暴露 Elasticsearch SearchHit，而是统一包装为：

```text
ShopSearchResponse
  records: List<ShopDTO>
  total: long
  current: int
  size: int
```

本项目为了保证 Docker 一键启动，不强制依赖 IK 中文分词器；生产环境可按需要为 `name`、`description` 等字段接入 IK。

## MySQL -> Elasticsearch Sync

全量重建接口：

```text
POST /admin/search/shop/rebuild
```

流程：

```text
删除旧 cityeats_shop 索引
  ↓
创建 mapping
  ↓
分页读取 MySQL tb_shop
  ↓
ShopSearchConverter 转换为 ShopDocument
  ↓
Bulk 写入 Elasticsearch
```

增量同步流程：

```text
ShopServiceImpl 更新 MySQL 成功
  ↓
发布 ShopChangedEvent
  ↓
TransactionalEventListener(AFTER_COMMIT)
  ↓
ShopSearchSyncListener 读取 MySQL 最新数据
  ↓
写入或删除 Elasticsearch 文档
```

一致性说明：MySQL 是 Source of Truth，Elasticsearch 是可重建搜索索引。增量同步失败时记录 ERROR 日志，不回滚已提交的 MySQL 事务，后续可通过全量 rebuild 修复，属于最终一致性设计。

## High-Concurrency Seckill

```text
POST /voucher-order/seckill/{id}
  ↓
SeckillValidationChain 前置校验
  ↓
Redis Lua 原子判断库存和一人一单
  ↓
生成订单 ID
  ↓
发送 VoucherOrderMessage 到 RabbitMQ
  ↓
Consumer + Redisson 用户-券维度锁
  ↓
MySQL 条件扣库存
  ↓
插入 tb_voucher_order
```

数据库唯一索引 `uk_user_voucher(user_id, voucher_id)` 作为一人一单最终兜底。

## RabbitMQ Reliability

秒杀订单消息模型使用 `VoucherOrderMessage`，只传输订单创建所需字段：

```text
orderId
userId
voucherId
createTime
```

可靠性流程：

```text
Redis Lua pre-deduct
  ↓
RabbitMQ
  ↓
VoucherOrderListener
  ↓
Consumer retry，最多 3 次
  ↓
超过重试进入 DLQ
  ↓
VoucherOrderDeadLetterListener
  ↓
按 orderId 查询 MySQL 幂等检查
  ↓
订单不存在时执行 compensate_seckill.lua
  ↓
Redis 库存 +1，删除用户购买集合标记，写入补偿幂等标记
```

关键点：

- 主队列绑定 Dead Letter Exchange，不无限重试。
- 死信补偿前必须检查 `tb_voucher_order` 是否已有订单，避免数据库成功但 Redis 被错误回滚。
- Redis 补偿通过 Lua 保证库存恢复、购买标记删除、补偿标记写入的原子性。
- 补偿标记 `seckill:compensated:{orderId}` 设置 TTL，避免重复死信消费导致库存多次增加。

## Design Patterns

- Strategy：`CacheQueryStrategy` 抽象不同 Redis 缓存查询方案。
- Factory：`CacheStrategyFactory` 从 Spring 容器收集策略，根据 `CacheStrategyType` 选择实现。
- Chain of Responsibility：`SeckillValidationChain` 顺序执行秒杀前置校验，包括优惠券存在性和秒杀时间窗口。
- Observer/Event：基于 Spring `ApplicationEvent` 与 `TransactionalEventListener` 实现商户变更后的 ES 增量同步。

缓存策略说明：

```text
PassThroughCacheStrategy      缓存穿透治理，缓存空值
MutexCacheStrategy            缓存击穿治理，互斥重建
LogicalExpireCacheStrategy    热点数据逻辑过期，异步重建
```

商户详情接口通过 `CacheStrategyFactory` 选择缓存策略，默认：

```text
SHOP_CACHE_STRATEGY=PASS_THROUGH
```

秒杀责任链只做业务前置校验和快速失败，不承担并发安全职责。库存判断、扣减和一人一单最终并发检查仍由 Redis Lua 原子执行。

## Database

初始化脚本：

```text
sql/schema.sql
sql/data.sql
```

`tb_voucher_order` 包含唯一索引：

```sql
UNIQUE KEY uk_user_voucher (user_id, voucher_id)
```

秒杀订单落库时使用 MySQL 条件扣库存：

```sql
stock = stock - 1
WHERE voucher_id = ?
  AND stock > 0
```

## Docker Quick Start

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

默认开发密码：

```text
MySQL root password: cityeats_dev
Redis password:      cityeats_dev
RabbitMQ username:   cityeats
RabbitMQ password:   cityeats_dev
```

PowerShell 启动应用：

```powershell
$env:DB_PORT="13306"
$env:DB_PASSWORD="cityeats_dev"
$env:REDIS_PORT="16379"
$env:REDIS_PASSWORD="cityeats_dev"
$env:RABBITMQ_PORT="5673"
$env:RABBITMQ_USERNAME="cityeats"
$env:RABBITMQ_PASSWORD="cityeats_dev"
$env:ELASTICSEARCH_PORT="19200"
mvn spring-boot:run
```

## 配置

`application.yml` 支持环境变量覆盖：

```text
DB_HOST=localhost
DB_PORT=13306
DB_NAME=cityeats
DB_USERNAME=root
DB_PASSWORD=cityeats_dev
REDIS_HOST=localhost
REDIS_PORT=16379
REDIS_PASSWORD=cityeats_dev
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5673
RABBITMQ_USERNAME=cityeats
RABBITMQ_PASSWORD=cityeats_dev
ELASTICSEARCH_HOST=localhost
ELASTICSEARCH_PORT=19200
SHOP_CACHE_STRATEGY=PASS_THROUGH
```

## API Examples

发送验证码：

```bash
curl -X POST "http://localhost:8080/user/code?phone=13800000003"
```

登录：

```bash
curl -X POST "http://localhost:8080/user/login" -H "Content-Type: application/json" -d "{\"phone\":\"13800000003\",\"code\":\"123456\"}"
```

商户详情：

```bash
curl "http://localhost:8080/shop/1"
```

商户搜索初始化和查询：

```bash
curl -X POST "http://localhost:8080/admin/search/shop/rebuild" -H "authorization: <token>"
curl "http://localhost:8080/shop/search?keyword=烧烤&current=1&size=10"
curl "http://localhost:8080/shop/search?typeId=1&area=浦东&sort=sold_desc"
```

附近商户：

```bash
curl "http://localhost:8080/shop/of/type?typeId=1&current=1&x=121.4460&y=31.2040"
```

签到：

```bash
curl -X POST "http://localhost:8080/user/sign" -H "authorization: <token>"
curl "http://localhost:8080/user/sign/count" -H "authorization: <token>"
```

秒杀下单：

```bash
curl -X POST "http://localhost:8080/voucher-order/seckill/1" -H "authorization: <token>"
```

## Testing

```bash
mvn clean test
mvn clean package
```

测试覆盖：

- Spring Context 与 Mapper 基础查询
- RedisIdWorker 并发生成 ID 唯一性
- CacheClient 空值缓存
- ShopSearchConverter 转换逻辑
- ShopSearchService 查询条件构造
- ShopChangedEvent 监听同步行为
- CacheStrategyFactory 和 PassThroughCacheStrategy
- SeckillValidationChain 秒杀前置校验
- Lua 秒杀脚本关键原子操作
- VoucherOrder 事务落库的条件扣库存路径
- VoucherOrderCompensation Redis Lua 补偿调用

## Interview Highlights

1. Redis 缓存穿透 / 击穿 / 逻辑过期：`CacheClient` 保留三种缓存治理方法，并由策略类复用原逻辑。
2. Strategy + Factory 管理缓存策略：`ShopServiceImpl.queryById` 通过 `CacheStrategyFactory` 选择商户缓存查询策略。
3. Redis Lua 秒杀原子校验：`seckill.lua` 在 Redis 内完成库存判断、重复购买判断和预扣减。
4. Redisson 分布式锁：异步落库时按用户和券维度加锁，降低重复消费和并发写入风险。
5. RabbitMQ 异步削峰：秒杀资格通过后发送 `VoucherOrderMessage`，订单创建由 Consumer 异步处理。
6. RabbitMQ Retry + DLQ：Consumer 有限重试，失败后进入死信队列，不做无限 requeue。
7. Redis Lua 幂等补偿：`compensate_seckill.lua` 使用补偿标记保证重复死信不会多次恢复库存。
8. Elasticsearch 全文搜索：`cityeats_shop` 索引支持商户关键词、多条件筛选、分页和排序。
9. MySQL -> Elasticsearch 最终一致同步：商户更新后发布事件，事务提交后监听器同步 ES。
10. GEO / Bitmap / ZSet：附近商户、签到、Feed 和点赞分别使用 Redis 对应数据结构实现。

## Current Limitations

本项目不引入 Doris、Agent、AIOps、Prometheus、Grafana、Actuator、Micrometer、Flink、RocketMQ、Kafka、微服务拆分或分库分表。当前增强重点是后端业务闭环、Elasticsearch 商户搜索、自然设计模式、RabbitMQ 可靠性和可验证测试。
