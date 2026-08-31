# CityEats Final Completion Report

## Confirmed Working

本轮已在 2026-08-31 使用 Docker Desktop + PowerShell + Maven 对最新代码完成最终验收。Docker、HTTP API、Elasticsearch、RabbitMQ 秒杀落库、DLQ 与 Redis 补偿均已真实运行通过。

## 1. 原项目状态

- Spring Boot 项目可编译，已有 Controller / Service / Mapper / Redis / RabbitMQ / Lua / Redisson 基础能力。
- 已有 Docker Compose、README 和 COMPLETION_REPORT。
- 本轮接续增强后的 CityEats，不重做项目、不降低已有功能。

## 2. 本轮目标

- 修复最后一轮验收中暴露的真实运行问题。
- 确认 Docker 四件套：MySQL、Redis、RabbitMQ、Elasticsearch。
- 确认 HTTP API、ES 搜索与同步、秒杀 MQ 异步落库、失败补偿可运行。
- 清理个人密码明文，保证 README 与代码事实一致。

## 3. 修改文件

- `.gitignore`
- `README.md`
- `COMPLETION_REPORT.md`
- `docker-compose.yml`
- `sql/data.sql`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml.example`
- `src/main/java/com/cityeats/config/RedissonConfig.java`
- `src/main/java/com/cityeats/config/QueueConfig.java`
- `src/main/java/com/cityeats/service/impl/ShopTypeServiceImpl.java`
- `src/main/java/com/cityeats/service/impl/VoucherOrderServiceImpl.java`
- `src/main/java/com/cityeats/service/impl/VoucherOrderTransactionalServiceImpl.java`
- `src/main/java/com/cityeats/utils/RedisConstants.java`
- `src/test/java/com/cityeats/service/VoucherOrderTransactionalServiceTest.java`

## 4. 新增文件

本轮之前的增强代码中已新增 Elasticsearch、缓存策略、秒杀责任链、RabbitMQ DLQ 和补偿相关类与测试。本轮收尾阶段未再新增业务模块，只补充必要修复和测试断言。

## 5. 新增 Maven 依赖

- `org.elasticsearch.client:elasticsearch-rest-high-level-client:7.6.2`

版本与 Docker 中的 `elasticsearch:7.6.2` 保持一致，未混用 ES 8。

## 6. Docker Compose 变化

- 新增 `elasticsearch:7.6.2`，单节点、关闭安全认证、宿主机端口 `19200:9200`、持久化 volume、healthcheck。
- MySQL、Redis、RabbitMQ 保持原有职责，默认开发密码统一为 `cityeats_dev`。
- `docker compose down -v` 和 `docker compose up -d` 已真实执行。
- `docker ps` 确认四个容器均为 healthy：`cityeats-mysql`、`cityeats-redis`、`cityeats-rabbitmq`、`cityeats-elasticsearch`。

## 7. Elasticsearch 架构

- MySQL 是 Source of Truth。
- Elasticsearch 仅作为商户搜索索引。
- Redis 继续负责缓存、GEO、Bitmap、ZSet 和秒杀高并发预扣减。
- 订单、用户、优惠券主数据未迁入 Elasticsearch。

## 8. Elasticsearch 索引结构

索引名：`cityeats_shop`

核心字段：

- `id`: long
- `name`: text + keyword
- `typeId`: long
- `area`: text + keyword
- `address`: text
- `description`: text
- `avgPrice`: long
- `score`: integer
- `sold`: integer
- `x` / `y`: double
- `createTime` / `updateTime`: date

生产环境可按需接入 IK 中文分词；当前项目使用默认 analyzer，保证一键启动。

## 9. 搜索接口

接口：`GET /shop/search`

已验证：

- `keyword=咖啡` 命中 `梧桐巷咖啡`
- `typeId=1` 返回咖啡类型商户
- `area=徐汇` 返回徐汇商户
- `current=1&size=1` 分页正常，total 为 2
- `sort=sold_desc` 按销量倒序返回

## 10. 全量同步流程

接口：`POST /admin/search/shop/rebuild`

本轮真实验证：

- HTTP 200
- 返回同步数量 `2`
- ES `_count` 返回 `2`
- MySQL `tb_shop` 行数为 `2`
- MySQL 与 ES 文档数量一致

## 11. 增量同步流程

已验证流程：

- `PUT /shop` 将 `id=1` 名称改为 `验收咖啡店`
- `/shop/1` 返回新名称
- Spring Event 发布 `ShopChangedEvent`
- `ShopSearchSyncListener` 在事务提交后同步 ES
- 搜索 `验收咖啡店` 命中
- 搜索旧关键词 `梧桐巷` 返回空
- 随后已恢复为 `梧桐巷咖啡`，并确认恢复后搜索命中

## 12. 使用的设计模式

- Strategy：缓存查询策略抽象。
- Factory：缓存策略注册与选择。
- Chain of Responsibility：秒杀前置校验。
- Observer/Event：基于 Spring ApplicationEvent / TransactionalEventListener 同步 ES。

均有真实业务调用，不是空类摆设。

## 13. Redis 缓存 Strategy 实现

- `PassThroughCacheStrategy`
- `MutexCacheStrategy`
- `LogicalExpireCacheStrategy`
- `CacheStrategyFactory`
- `ShopServiceImpl.queryById` 已通过工厂选择策略执行商户详情缓存查询。

## 14. 秒杀责任链实现

- `SeckillValidationChain`
- `VoucherExistValidator`
- `SeckillTimeValidator`

责任链只做前置业务校验；库存扣减和一人一单最终并发安全仍由 Redis Lua 执行。

## 15. RabbitMQ Retry

配置已启用：

- `spring.rabbitmq.listener.simple.retry.enabled=true`
- `max-attempts=3`
- `initial-interval=1000`
- `multiplier=2`
- `max-interval=5000`
- `default-requeue-rejected=false`

失败补偿验收中日志确认同一消息多次消费失败后进入 retry exhausted。

## 16. DLQ

已定义：

- 主交换机：`cityeats.voucher.order.exchange`
- 主队列：`cityeats.voucher.order.queue`
- DLX：`cityeats.voucher.order.dlx`
- DLQ：`cityeats.voucher.order.dlq`
- 死信路由键：`voucher.order.failed`

RabbitMQ `list_queues` 确认主队列和 DLQ 最终均无积压。

## 17. Redis 补偿机制

已验证失败链路：

- 人为设置 MySQL 秒杀库存为 0、Redis 秒杀库存为 1。
- 秒杀接口通过 Redis Lua 预扣减并发送 MQ。
- Consumer MySQL 条件扣库存失败，抛出 `BusinessException`。
- RabbitMQ retry 耗尽后进入 DLQ。
- DLQ Consumer 查询 MySQL 无订单。
- 执行 `compensate_seckill.lua`。
- Redis 库存恢复到 1。
- 失败用户购买标记被删除。
- `seckill:compensated:{orderId}` 写入。
- 失败订单 MySQL count 为 0。

验收结束后已将演示库存恢复为 MySQL 19、Redis 19。

## 18. 新增测试

当前测试覆盖包括：

- `ShopSearchConverterTest`
- `ShopSearchServiceTest`
- `ShopChangedEventTest`
- `CacheStrategyFactoryTest`
- `PassThroughCacheStrategyTest`
- `SeckillValidationChainTest`
- `VoucherOrderCompensationTest`
- `VoucherOrderTransactionalServiceTest`

本轮新增断言：MySQL 条件扣库存失败时必须抛出异常，以触发 MQ retry/DLQ。

## 19. mvn clean test 结果

```text
Confirmed Working
Command: mvn clean test
Tests run: 16
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
Finished at: 2026-08-31T09:52:27+08:00
```

## 20. mvn clean package 结果

```text
Confirmed Working
Command: mvn clean package
Tests run: 16
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
Finished at: 2026-08-31T09:55:35+08:00
```

Jar confirmed:

```text
target/cityeats-0.0.1-SNAPSHOT.jar
```

## Docker 集成验证结果

```text
Confirmed Working
```

- `docker version` 正常返回 Docker Desktop 4.79.0 / Engine 29.5.3。
- `docker compose down -v` 执行成功。
- `docker compose up -d` 执行成功。
- `docker ps` 显示 MySQL、Redis、RabbitMQ、Elasticsearch 全部 healthy。
- `GET http://localhost:19200` 返回 Elasticsearch 7.6.2 信息。
- Spring Boot 启动成功并连接 MySQL、Redis、Redisson、RabbitMQ。

## 实际验证 API

均为 HTTP 200：

- `POST /user/code`
- `POST /user/login`
- `GET /shop-type/list`
- `GET /shop/1`
- `GET /shop/of/type`
- `POST /user/sign`
- `GET /user/sign/count`
- `POST /blog`
- `GET /blog/hot`
- `GET /voucher/list/1`
- `POST /admin/search/shop/rebuild`
- `GET /shop/search`
- `PUT /shop`
- `POST /voucher-order/seckill/1`

秒杀验证：

- 首次秒杀成功，返回订单号。
- 同一用户重复秒杀返回 `不能重复下单`。
- Redis 库存 20 -> 19。
- MySQL 库存 20 -> 19。
- `tb_voucher_order` 新增 1 条。
- RabbitMQ 主队列和 DLQ 无异常积压。
- Consumer 日志显示 `优惠券订单消费成功`。

## 本轮修复的真实运行 Bug

- Redisson 未读取 Redis 密码，导致 `NOAUTH Authentication required`。
- RabbitMQ JSON 转换器未注册 Java Time 模块，导致 `VoucherOrderMessage.createTime` 反序列化失败。
- MySQL 条件扣库存失败时直接 return，导致 MQ 认为消息消费成功，无法进入 retry/DLQ/补偿。
- Docker 初始化 SQL 未设置客户端字符集，导致 Java 读取中文乱码，ES 文档和搜索也受影响。

## 当前剩余限制

- 当前项目仍是单体应用，不包含管理员权限体系；`/admin/search/shop/rebuild` 是开发初始化接口。
- ES 使用默认 analyzer，中文分词能力有限，但本轮验证的商户关键词、类型、区域、分页、排序均正常。
- RabbitMQ 失败补偿通过开发态人为制造 MySQL/Redis 库存不一致验证，不提供生产危险开关。

## 最终结论

```text
READY FOR RESUME
```

简历中关于 Elasticsearch、设计模式、高并发秒杀、RabbitMQ 异步订单、DLQ 与 Redis Lua 补偿的描述均有真实代码和本轮运行验证支撑。
