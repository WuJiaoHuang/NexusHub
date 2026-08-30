# CityEats Phase 2 Completion Report

## 1. 原项目状态

Phase 0 审计确认：

- Spring Boot 2.3.12 项目结构完整，Controller / Service / ServiceImpl / Mapper / Interceptor / Listener / Utils 已存在。
- RabbitMQ Consumer 真实存在：`VoucherOrderListener`。
- Redis Lua 秒杀脚本真实存在，`ARGV[1]=voucherId`、`ARGV[2]=userId` 与 Java 调用一致。
- Redisson 用户-券维度锁真实存在：`lock:order:{voucherId}:{userId}`。
- MySQL 条件扣库存真实存在：`stock = stock - 1` 且 `stock > 0`。
- `tb_voucher_order` 唯一索引真实存在：`uk_user_voucher (user_id, voucher_id)`。
- `CacheClient` 已有 `queryWithPassThrough`、`queryWithMutex`、`queryWithLogicalExpire`。
- 原 README / 报告仍包含“不引入 Elasticsearch”和秒杀链路 `NOT VERIFIED` 的旧阶段说明。
- 当前仓库在本轮开始时未接入 Elasticsearch。

## 2. 本轮目标

- 接入 Elasticsearch 7.6.2，用于商户全文搜索。
- 实现 MySQL 到 Elasticsearch 的全量重建和业务事件增量同步。
- 引入真实使用的 Strategy、Factory、Chain of Responsibility、Observer/Event。
- 增强 RabbitMQ 秒杀订单链路：有限重试、DLQ、失败补偿和补偿幂等。
- 补充测试并保证 Maven 构建通过。
- 更新 README 和完成报告，使简历描述有真实代码支撑。

## 3. 修改文件

- `pom.xml`
- `docker-compose.yml`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml.example`
- `src/main/java/com/cityeats/common/MqConstants.java`
- `src/main/java/com/cityeats/config/QueueConfig.java`
- `src/main/java/com/cityeats/controller/ShopController.java`
- `src/main/java/com/cityeats/listener/VoucherOrderListener.java`
- `src/main/java/com/cityeats/service/IShopService.java`
- `src/main/java/com/cityeats/service/impl/ShopServiceImpl.java`
- `src/main/java/com/cityeats/service/impl/VoucherOrderServiceImpl.java`
- `src/main/java/com/cityeats/utils/RedisConstants.java`
- `README.md`
- `COMPLETION_REPORT.md`

## 4. 新增文件

- `src/main/java/com/cityeats/config/ElasticsearchConfig.java`
- `src/main/java/com/cityeats/controller/AdminSearchController.java`
- `src/main/java/com/cityeats/dto/ShopDTO.java`
- `src/main/java/com/cityeats/dto/ShopSearchRequest.java`
- `src/main/java/com/cityeats/dto/ShopSearchResponse.java`
- `src/main/java/com/cityeats/dto/VoucherOrderMessage.java`
- `src/main/java/com/cityeats/cache/*`
- `src/main/java/com/cityeats/search/*`
- `src/main/java/com/cityeats/seckill/*`
- `src/main/java/com/cityeats/listener/ShopSearchSyncListener.java`
- `src/main/java/com/cityeats/listener/VoucherOrderDeadLetterListener.java`
- `src/main/java/com/cityeats/service/VoucherOrderCompensationService.java`
- `src/main/java/com/cityeats/service/impl/VoucherOrderCompensationServiceImpl.java`
- `src/main/resources/lua/compensate_seckill.lua`
- `src/test/java/com/cityeats/cache/*`
- `src/test/java/com/cityeats/search/*`
- `src/test/java/com/cityeats/seckill/SeckillValidationChainTest.java`
- `src/test/java/com/cityeats/service/VoucherOrderCompensationTest.java`

## 5. 新增 Maven 依赖

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.6.2</version>
</dependency>
```

版本选择原因：项目使用 Spring Boot 2.3.12，选择 Elasticsearch 7.6.2 与该生态兼容，避免 ES 8 服务端和 7.x Java 客户端混用。

## 6. Docker Compose 变化

- 新增 `elasticsearch:7.6.2` 单节点服务。
- 默认映射 `19200:9200`。
- 关闭开发环境安全认证。
- 新增 ES 持久化 volume 和 healthcheck。
- MySQL 默认 root 密码改为 `Wjh20050705`。
- Redis 默认启用 `requirepass Wjh20050705`。
- 保留 MySQL、Redis、RabbitMQ 原有服务。

## 7. Elasticsearch 架构

```text
MySQL tb_shop
  ↓
ShopSearchConverter
  ↓
ShopDocument
  ↓
Elasticsearch cityeats_shop
  ↓
GET /shop/search
```

定位：

- MySQL = Source of Truth
- Elasticsearch = Search Index
- Redis = Cache / GEO / High-concurrency Infrastructure

## 8. Elasticsearch 索引结构

索引名：

```text
cityeats_shop
```

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
- `comments`: integer
- `x`: double
- `y`: double
- `openHours`: keyword
- `createTime`: date
- `updateTime`: date

## 9. 搜索接口

```text
GET /shop/search
```

支持：

- keyword 搜索 `name / description / address / area`
- typeId 精确过滤
- area 精确过滤
- minPrice / maxPrice 区间过滤
- minScore 过滤
- current / size 分页
- sort：`score_desc`、`sold_desc`、`avgPrice_asc`、`avgPrice_desc`

返回：

```text
ShopSearchResponse(records, total, current, size)
```

## 10. 全量同步流程

接口：

```text
POST /admin/search/shop/rebuild
```

流程：

```text
删除旧索引
  ↓
创建 mapping
  ↓
分页读取 MySQL 商户
  ↓
转换 ShopDocument
  ↓
Bulk 写入 ES
  ↓
返回同步数量
```

## 11. 增量同步流程

```text
ShopServiceImpl.update
  ↓
MySQL 更新成功
  ↓
删除 Redis 商户缓存
  ↓
publishEvent(new ShopChangedEvent(...))
  ↓
ShopSearchSyncListener AFTER_COMMIT
  ↓
重新读取 MySQL 最新商户
  ↓
更新 Elasticsearch 文档
```

同步失败时记录 ERROR 日志，不回滚已提交的 MySQL 事务。

## 12. 使用的设计模式

- Strategy：缓存查询方案抽象。
- Factory：从 Spring 容器注册并选择缓存策略。
- Chain of Responsibility：秒杀前置校验链。
- Observer/Event：Spring 事件监听完成 ES 增量同步。

## 13. Redis 缓存 Strategy 实现

- `CacheQueryStrategy`
- `PassThroughCacheStrategy`
- `MutexCacheStrategy`
- `LogicalExpireCacheStrategy`
- `CacheStrategyFactory`
- `CacheStrategyType`

商户详情 `ShopServiceImpl.queryById` 已通过 `CacheStrategyFactory` 选择策略，默认 `PASS_THROUGH`。

## 14. 秒杀责任链实现

- `SeckillContext`
- `SeckillValidator`
- `VoucherExistValidator`
- `SeckillTimeValidator`
- `SeckillValidationChain`

责任链只做业务前置校验。Redis Lua 仍负责最终库存判断、库存扣减和一人一单原子校验。

## 15. RabbitMQ Retry

`application.yml` 配置：

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        default-requeue-rejected: false
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2
          max-interval: 5000
```

## 16. DLQ

新增常量：

- `VOUCHER_ORDER_DLX`
- `VOUCHER_ORDER_DLQ`
- `VOUCHER_ORDER_DLQ_ROUTING_KEY`

`QueueConfig` 中主队列绑定 DLX，死信队列绑定 DLX。

## 17. Redis 补偿机制

新增 `compensate_seckill.lua`：

```text
如果 seckill:compensated:{orderId} 已存在，直接返回
否则：
  seckill:stock:{voucherId} + 1
  seckill:order:{voucherId} 删除 userId
  写入补偿标记并设置 TTL
```

`VoucherOrderDeadLetterListener` 在补偿前按 `orderId` 查询 MySQL；订单已存在时不补偿，避免数据库成功但 Redis 被错误恢复。

## 18. 新增测试

- `ShopSearchConverterTest`
- `ShopSearchServiceTest`
- `ShopChangedEventTest`
- `CacheStrategyFactoryTest`
- `PassThroughCacheStrategyTest`
- `SeckillValidationChainTest`
- `VoucherOrderCompensationTest`

保留并通过原有测试：

- `CityEatsApplicationTests`
- `SeckillLuaScriptTest`
- `VoucherOrderTransactionalServiceTest`
- `CacheClientTest`
- `RedisIdWorkerTest`

## 19. mvn clean test 结果

```text
Confirmed Working
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Finished at: 2026-08-30T21:40:12+08:00
```

## 20. mvn clean package 结果

```text
Confirmed Working
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Jar: target/cityeats-0.0.1-SNAPSHOT.jar
Finished at: 2026-08-30T21:41:23+08:00
```

## 21. Docker 集成验证结果

```text
Not Fully Verified
```

本轮已尝试：

- `docker version`
- 启动 Docker Desktop
- 轮询 Docker daemon
- `docker ps`

结果：Docker Desktop 进程已启动，但 Docker CLI 连接 `dockerDesktopLinuxEngine` 时挂起或无法获得 daemon 响应，因此未能完成 `docker compose down -v`、`docker compose up -d`、容器 healthcheck、HTTP API 和 RabbitMQ 真链路复验。

## 22. 实际验证 API

本轮由于 Docker daemon 未可用，以下 HTTP API 未能完成最新代码的真实调用验证：

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
- `POST /voucher-order/seckill/1`
- `POST /admin/search/shop/rebuild`
- `GET /shop/search`

这些接口已有真实代码实现和单元/构建验证，但不能在本报告中标记为本轮 Docker 集成 Confirmed Working。

## 23. 当前剩余限制

- Docker daemon 当前不可用，导致 Docker + HTTP API + RabbitMQ 秒杀落库链路未完成本轮真实复验。
- Elasticsearch 真实容器启动、索引文档数与 MySQL 行数一致性、关键词/typeId/area/分页/排序实际 HTTP 调用仍需在 Docker daemon 恢复后验证。
- RabbitMQ retry 到 DLQ 的真实 broker 级流转和死信补偿仍需在 Docker daemon 恢复后验证。
- 本项目未引入 IK 中文分词器，开发环境使用 Elasticsearch 默认 analyzer。
- 不提供 production profile 下的危险故障触发 API；失败补偿通过单元测试覆盖核心 Lua 调用路径，真实 DLQ 可在开发环境通过让 Consumer 抛异常验证。

## Confirmed Working

- Phase 0 仓库审计完成。
- Elasticsearch 7.6.2 Java 客户端依赖接入。
- Docker Compose 已新增 Elasticsearch 服务定义。
- `ShopDocument` 与 MyBatis `Shop` 分离。
- `cityeats_shop` mapping 代码存在。
- `/shop/search` 搜索接口代码存在。
- `/admin/search/shop/rebuild` 全量重建接口代码存在。
- `ShopChangedEvent` + `ShopSearchSyncListener` 增量同步代码存在。
- Strategy + Factory 已真实用于商户详情缓存查询。
- Responsibility Chain 已真实用于秒杀前置校验。
- Observer/Event 已真实用于 ES 增量同步。
- Redis Lua 秒杀最终原子检查未被破坏。
- RabbitMQ 主队列已配置 DLX / DLQ。
- RabbitMQ Consumer retry 已配置有限次数。
- Redis Lua 补偿脚本已新增，并带幂等补偿标记。
- `mvn clean test` 通过。
- `mvn clean package` 通过。
- README 已按真实代码更新。

## Not Fully Verified

- Docker Compose 最终复验。
- Spring Boot 连接 Docker MySQL / Redis / RabbitMQ / Elasticsearch 启动复验。
- 最新代码版本 HTTP API 实际调用。
- 秒杀 RabbitMQ 从接口到 MySQL 落库的最新链路复验。
- ES 全量 rebuild 后文档数与 MySQL 行数一致的真实验证。
- ES keyword/typeId/area/分页/排序真实 HTTP 验证。
- MySQL 更新后 Event 到 ES 的真实集成验证。
- RabbitMQ retry 超限进入 DLQ 的真实 broker 验证。
- DLQ Consumer 触发 Redis 补偿的真实 broker 验证。
