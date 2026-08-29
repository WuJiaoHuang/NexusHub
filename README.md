# CityEats

CityEats 是一个面向本地生活场景的高并发后端服务平台，包含商户查询、优惠券秒杀、Feed、签到、附近商户等功能，并通过 Redis、RabbitMQ、Lua、Redisson 等机制处理缓存、高并发和异步任务。

CityEats 是独立业务系统，不是 AIOps Sentinel 的运维模块。它也会作为 AIOps Sentinel 的第一个真实后端 Target，用于验证真实业务系统产生 Metrics、Logs、Health 后，能否被外部智能故障诊断平台通过标准化 MCP Tools 接入和分析。

## 核心功能

- 用户登录：手机号验证码登录，Token 信息存入 Redis。
- 用户签到：基于 Redis Bitmap 记录签到状态并统计连续签到。
- 商户查询：支持商户详情、商户类型、附近商户和地理位置检索。
- 缓存治理：通过 Redis cache 处理缓存穿透、缓存击穿和缓存雪崩问题。
- 优惠券秒杀：使用 Lua 脚本保证库存扣减和一人一单判断的原子性。
- 异步下单：通过 RabbitMQ 异步处理秒杀订单。
- 分布式锁：使用 Redisson distributed lock 控制并发竞争。
- Feed 流：使用 Redis ZSet 支持关注推送和滚动分页。
- 点赞排行：使用 Redis ZSet 维护点赞用户顺序。

## 技术栈

```text
Spring Boot
Spring MVC
MyBatis Plus
MySQL
Redis
RabbitMQ
Redisson
Lua
Hutool
Lombok
```

## 代码结构

```text
com.cityeats
├── config        Spring MVC、MyBatis、RabbitMQ、Redis、Redisson 配置
├── controller    用户、商户、优惠券、博客、关注等 HTTP 接口
├── dto           请求与响应 DTO
└── entity        业务实体
```

当前仓库保留的是核心业务代码子集。如果需要完整运行全部业务接口，需要补齐 service、mapper、interceptor、utils、listener 等模块。

## 重点能力

### Redis Cache

商户查询场景使用 Redis 缓存热点数据，降低 MySQL 访问压力。项目设计中覆盖：

- Cache penetration：缓存空对象。
- Cache breakdown：互斥锁与逻辑过期。
- Cache avalanche：不同 Key 设置离散过期时间。

### Lua Atomic Seckill

秒杀请求通过 Lua 脚本在 Redis 内完成库存校验、重复购买校验和扣减判断，避免高并发下库存超卖。

### RabbitMQ Async Order

秒杀资格判断通过后，订单创建进入 RabbitMQ 异步流程，减少用户请求链路等待时间。

### Redisson Distributed Lock

对一人一单等并发竞争场景使用 Redisson 分布式锁，避免多实例部署下的并发写入问题。

### Bitmap Sign-in

签到功能通过 Redis Bitmap 存储每日签到状态，支持快速统计连续签到天数。

### GEO Nearby Search

附近商户使用 Redis GEO 能力按位置查询，提高本地生活搜索场景的响应效率。

### ZSet Feed

关注 Feed 和点赞排行榜使用 Redis ZSet，以时间或分数排序支持滚动分页和 TopN 查询。

## Observability 规划

下一阶段会补充可观测性能力：

- Spring Boot Actuator：暴露 `/actuator/health`、`/actuator/info`、`/actuator/prometheus`。
- Micrometer Prometheus：导出 JVM、HTTP、HikariCP 和业务自定义指标。
- Structured Logging：输出 JSON 结构化日志到 `logs/cityeats.log`。
- Fault Injection：仅在 `chaos` profile 下启用 Redis 故障、数据库连接池压力和接口延迟注入。

这些观测数据会被 AIOps Sentinel 通过 Prometheus Provider、FileLogProvider 和 SpringBootHealthProvider 查询，用于端到端故障诊断验证。

## 本地依赖

```text
JDK 8+
Maven 3.6+
MySQL 5.7+
Redis 6+
RabbitMQ 3.8+
```

## 配置示例

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cityeats?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your-password
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

## 与 AIOps Sentinel 的关系

CityEats 首先是一个独立的本地生活高并发业务项目。AIOps Sentinel 是独立的通用智能故障诊断平台。

两者的关系是：

```text
CityEats 产生真实 Metrics / Logs / Health
        ↓
AIOps Sentinel Provider Adapter 查询这些观测数据
        ↓
MCP Tool 统一暴露给 Agent
        ↓
LLM Agent 生成根因分析、证据链和 ActionProposal
```

未来接入其他 Spring Boot 或微服务系统时，不应该修改 AIOps Sentinel 的 Agent 核心逻辑，而是新增 Target 配置和必要的 Provider Adapter。
