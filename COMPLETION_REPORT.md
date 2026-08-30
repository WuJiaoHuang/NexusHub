# CityEats Completion Report

## 总览

本轮目标是对 CityEats 做一次完整性修复与工程化补全，使项目从课程代码子集整理为可编译、可测试、可打包的本地生活高并发后端项目。

本阶段没有引入 Actuator、Micrometer、Prometheus、Grafana、Elasticsearch、Doris、微服务拆分或新的复杂技术栈。当前重点是补齐核心业务链路、数据库脚本、Redis/RabbitMQ 集成、测试和中文 README。

## 原项目存在的问题

- 部分核心业务模块缺失或不完整，包括 service、mapper、interceptor、utils、listener 等。
- 项目命名仍有历史课程痕迹，需要统一为 CityEats。
- 数据库初始化脚本、Docker 依赖启动方式和测试环境不完整。
- 秒杀链路缺少完整的 Redis Lua、RabbitMQ 异步消费、事务落库和并发兜底验证。
- README 容易让人误解为仍是代码子集，需要同步改成当前真实实现状态。

## 补齐的模块

- 启动入口：`CityEatsApplication`
- 通用模块：业务异常、MQ 常量、GEO 初始化
- 配置模块：MVC、MyBatis-Plus、RabbitMQ、Redisson、全局异常处理
- 拦截器：Token 刷新、登录校验
- Mapper：用户、商户、商户类型、关注、博客、优惠券、秒杀券、订单等
- Service：用户登录、商户缓存、GEO 查询、关注 Feed、点赞、签到、秒杀下单
- Listener：RabbitMQ 秒杀订单消费者
- Utils：Redis 缓存工具、分布式 ID、用户上下文、正则校验、常量

## 新增文件

- `src/main/java/com/cityeats/CityEatsApplication.java`
- `src/main/java/com/cityeats/common/BusinessException.java`
- `src/main/java/com/cityeats/common/MqConstants.java`
- `src/main/java/com/cityeats/common/GeoShopInitializer.java`
- `src/main/java/com/cityeats/interceptor/LoginInterceptor.java`
- `src/main/java/com/cityeats/interceptor/RefreshTokenInterceptor.java`
- `src/main/java/com/cityeats/listener/VoucherOrderListener.java`
- `src/main/java/com/cityeats/mapper/*Mapper.java`
- `src/main/java/com/cityeats/service/I*Service.java`
- `src/main/java/com/cityeats/service/impl/*ServiceImpl.java`
- `src/main/java/com/cityeats/utils/*.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml.example`
- `src/main/resources/logback-spring.xml`
- `src/main/resources/lua/seckill.lua`
- `sql/schema.sql`
- `sql/data.sql`
- `docker-compose.yml`
- `src/test/resources/application-test.yml`
- `src/test/resources/schema-h2.sql`
- `src/test/resources/data-h2.sql`
- `src/test/java/com/cityeats/**/*Test.java`

## 修改文件

- `README.md`：重写为中文工程 README，删除“代码子集/待补齐”旧描述，说明当前真实能力、启动方式、测试方式和阶段边界。
- `docker-compose.yml`：默认端口改为 13306、16379、5673、15673，减少与本机 MySQL/Redis/RabbitMQ 冲突。
- `src/main/resources/application.yml`：数据库 URL 增加 `allowPublicKeyRetrieval=true`，适配 MySQL 8 本地连接。
- `src/main/java/com/cityeats/service/impl/VoucherOrderTransactionalServiceImpl.java`：秒杀订单落库改为字符串字段 QueryWrapper/UpdateWrapper，避免 Java 17 反射限制导致 RabbitMQ Consumer 失败；同时增加 Redisson 用户-券维度锁。

## 删除文件

本轮没有主动删除核心业务文件。

## 数据库结构变化

- 数据库统一为 `cityeats`。
- 初始化脚本位于 `sql/schema.sql` 和 `sql/data.sql`。
- 主要表包括：
  - `tb_user`
  - `tb_user_info`
  - `tb_shop_type`
  - `tb_shop`
  - `tb_follow`
  - `tb_blog`
  - `tb_blog_comments`
  - `tb_voucher`
  - `tb_seckill_voucher`
  - `tb_voucher_order`
- `tb_voucher_order` 增加 `(user_id, voucher_id)` 唯一索引，用于一人一单最终兜底。

## Redis 使用方式

- 登录 token：保存用户登录态，拦截器从 Redis 恢复用户上下文。
- 商户缓存：商户详情使用缓存空值防止缓存穿透。
- 秒杀库存：Lua 脚本在 Redis 内完成库存判断、重复购买判断和预扣减。
- 分布式 ID：基于 Redis 自增序列生成订单 ID。
- 签到：使用 Bitmap 记录每日签到并统计连续签到。
- 附近商户：使用 GEO 存储商户坐标并按距离查询。
- Feed 和点赞：使用 ZSet 维护时间顺序和点赞顺序。

## RabbitMQ 使用方式

- 秒杀资格校验通过后，接口线程发送订单消息到 RabbitMQ。
- `VoucherOrderListener` 异步消费订单消息。
- 消费端在事务内完成：
  - 查询是否重复下单
  - 条件扣减 MySQL 库存
  - 插入订单记录
- 消费端失败会记录错误日志，不在接口线程中直接阻塞用户请求。

## 秒杀完整流程

```text
用户请求秒杀
  ↓
校验登录态和秒杀券时间
  ↓
Redis Lua 原子判断库存和一人一单
  ↓
生成订单 ID
  ↓
发送 RabbitMQ 消息
  ↓
RabbitMQ Consumer 消费
  ↓
Redisson 用户-券维度锁
  ↓
MySQL 条件扣库存
  ↓
插入订单记录
```

## 修复的工程问题

- 补齐 Spring Boot 启动入口和主配置。
- 补齐核心业务 Service/Mapper/Listener/Utils。
- 修复 MySQL 8 本地连接的 public key retrieval 问题。
- 修复默认 Docker 端口容易和本机服务冲突的问题。
- 修复 Java 17 下 MyBatis-Plus LambdaWrapper 在异步消费链路中的反射访问问题。
- 修复 README 与当前代码状态不一致的问题。
- 增加 `.gitignore`，避免提交 `target/`、日志和本地配置。

## 新增测试

- `CityEatsApplicationTests`：验证 Spring Context 和 Mapper 基础查询。
- `RedisIdWorkerTest`：验证 Redis 分布式 ID 并发唯一性。
- `CacheClientTest`：验证缓存空值逻辑。
- `SeckillLuaScriptTest`：验证 Lua 秒杀脚本关键原子逻辑。
- `VoucherOrderTransactionalServiceTest`：验证事务落库和条件扣库存路径。

## Docker 启动方式

```bash
docker compose up -d
```

默认端口：

```text
MySQL: 13306
Redis: 16379
RabbitMQ: 5673
RabbitMQ Management: 15673
```

当前环境状态：

```text
NOT VERIFIED
```

原因：本次最终验收时 Docker Desktop daemon 未运行，命令返回无法连接 `dockerDesktopLinuxEngine`。此前曾成功启动过 MySQL、Redis、RabbitMQ 容器并完成接口链路验证，但本报告不把最终环境复验写成 PASS。

## Maven 验证

### mvn clean test

```text
Confirmed Working
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Finished at: 2026-08-30T13:33:44+08:00
```

### mvn clean package

```text
Confirmed Working
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Jar: target/cityeats-0.0.1-SNAPSHOT.jar
Finished at: 2026-08-30T13:34:58+08:00
```

## 实际验证过的 API

以下 API 曾在 Docker 依赖启动成功时完成验证：

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

本轮最终修复了 RabbitMQ Consumer 落库时的 Java 17 LambdaWrapper 反射问题，但由于 Docker daemon 在最终复验阶段不可用，秒杀 MQ 落库链路的最新代码版本标记为：

```text
NOT VERIFIED
```

## 当前仍存在限制

- 本阶段没有接入 Actuator、Micrometer、Prometheus、Grafana。
- 本阶段没有接入 AIOps Sentinel 的真实观测数据闭环。
- 本阶段没有实现故障注入、chaos profile 或数据库连接池耗尽 Demo。
- 最终 Docker 依赖和接口联调因为本机 Docker Desktop daemon 未运行，未完成最新代码版本复验。
- 秒杀链路使用 Redis 预扣减和 MySQL 最终落库，若异步消费失败，需要后续增加补偿机制或可靠消息方案。

## 验收结论

### Confirmed Working

- 项目可以编译。
- 项目可以运行单元/集成测试。
- 项目可以打包生成 Spring Boot Jar。
- README 已改为中文，并与当前阶段代码能力保持一致。
- 项目命名已统一为 CityEats。
- 核心业务模块已补齐。
- 数据库脚本、Docker Compose、测试配置已补齐。

### Not Fully Verified

- Docker Compose 最终复验。
- 最新代码版本下的完整 HTTP API 联调。
- 最新代码版本下的 RabbitMQ 秒杀订单落库。

