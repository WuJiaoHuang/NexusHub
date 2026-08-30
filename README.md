# CityEats

CityEats 是一个面向本地生活场景的 Spring Boot 后端项目，覆盖用户登录、商户查询、附近商户、签到、关注 Feed、优惠券秒杀与异步订单等业务链路。项目重点展示 MySQL 持久化、Redis 缓存与高并发削峰、RabbitMQ 异步处理、Lua 原子校验、Redisson 分布式锁、Bitmap、GEO 与 ZSet 等后端工程能力。

当前仓库已经补齐可运行所需的 Controller、Service、ServiceImpl、Mapper、Interceptor、Listener、Utils、配置文件、SQL 初始化脚本和基础测试，不再是仅保留核心代码子集的展示仓库。

## 技术栈

```text
Spring Boot 2.3.12
Spring MVC
MyBatis-Plus
MySQL 8
Redis 6
RabbitMQ
Redisson
Lua
Hutool
Lombok
JUnit 5
H2 Test Database
Docker Compose
```

## 架构

```text
Client
  ↓
Spring Boot Web
  ↓
Controller
  ↓
Service / ServiceImpl
  ↓
Mapper / Redis / RabbitMQ
  ↓
MySQL / Redis / RabbitMQ Consumer
```

## 核心功能

- 用户登录：手机号验证码登录，登录 token 存入 Redis，拦截器恢复用户上下文。
- 商户查询：商户详情走 Redis 缓存，未命中查询 MySQL，不存在数据缓存空值防止缓存穿透。
- 商户类型：按 sort 排序查询，并缓存到 Redis。
- 附近商户：启动时将商户坐标写入 Redis GEO，接口按距离查询并保持排序。
- 签到：使用 Redis Bitmap 记录每日签到，并通过 BITFIELD 统计连续签到天数。
- 关注：支持关注、取关、是否关注和共同关注。
- Feed：发布博客后推送到粉丝 inbox ZSet，拉取时使用滚动分页。
- 点赞：使用 Redis ZSet 记录点赞用户和时间顺序。
- 秒杀：Redis Lua 原子判断库存和一人一单，成功后发送 RabbitMQ 消息。
- 异步订单：RabbitMQ Consumer 在事务中条件扣库存并插入订单。
- 分布式锁：异步落库阶段使用 Redisson 对用户-券维度加锁，配合 Redis Lua 和数据库唯一索引做兜底。
- 统一异常：通过全局异常处理返回统一 Result，不向客户端暴露完整堆栈。

## 高并发秒杀流程

```text
POST /voucher-order/seckill/{id}
  ↓
校验登录用户和秒杀时间
  ↓
Redis Lua 原子执行
  ├─ 判断库存 > 0
  ├─ 判断用户未购买
  ├─ 扣减 Redis 库存
  └─ 记录用户购买集合
  ↓
生成 Redis 分布式 ID
  ↓
发送 RabbitMQ 消息
  ↓
Consumer 消费订单
  ↓
Redisson 用户-券维度锁
  ↓
MySQL 条件扣库存：stock > 0
  ↓
插入 tb_voucher_order
```

数据库侧通过 `tb_voucher_order` 的 `(user_id, voucher_id)` 唯一索引做一人一单最终兜底。

## Redis 使用

### Cache

`CacheClient` 提供：

- `set`
- `setWithLogicalExpire`
- `queryWithPassThrough`
- `queryWithMutex`
- `queryWithLogicalExpire`

商户详情接口当前使用 `queryWithPassThrough`，实现缓存命中、缓存空值和数据库回源。

### GEO

`GeoShopInitializer` 在应用启动后读取 `tb_shop`，按商户类型写入：

```text
geo:shop:type:{typeId}
```

附近商户接口：

```text
GET /shop/of/type?typeId=1&current=1&x=121.4460&y=31.2040
```

### Bitmap

签到 Key：

```text
sign:{userId}:{yyyyMM}
```

接口：

```text
POST /user/sign
GET /user/sign/count
```

### ZSet

Feed Key：

```text
feed:{userId}
```

点赞 Key：

```text
blog:liked:{blogId}
```

## 项目结构

```text
src/main/java/com/cityeats
├── common        通用常量、业务异常、GEO 初始化
├── config        MVC、MyBatis-Plus、RabbitMQ、Redisson、异常处理配置
├── controller    HTTP API
├── dto           请求与响应对象
├── entity        MyBatis-Plus 实体
├── interceptor   登录校验与 token 刷新拦截器
├── listener      RabbitMQ 消费者
├── mapper        MyBatis-Plus Mapper
├── service       Service 接口
├── service/impl  业务实现
└── utils         Redis 缓存、ID、用户上下文、常量工具

src/main/resources
├── application.yml
├── application-local.yml.example
├── logback-spring.xml
└── lua/seckill.lua

sql
├── schema.sql
└── data.sql
```

## 快速启动

启动 MySQL、Redis、RabbitMQ：

```bash
docker compose up -d
```

默认宿主机端口：

```text
MySQL:    13306 -> 3306
Redis:    16379 -> 6379
RabbitMQ: 5673  -> 5672
管理页面: 15673 -> 15672
```

启动应用：

```bash
set DB_PORT=13306
set REDIS_PORT=16379
set RABBITMQ_PORT=5673
mvn spring-boot:run
```

PowerShell：

```powershell
$env:DB_PORT="13306"
$env:REDIS_PORT="16379"
$env:RABBITMQ_PORT="5673"
mvn spring-boot:run
```

## 配置

`application.yml` 使用环境变量读取数据库和中间件配置，仓库不会写死真实密码。

```text
DB_HOST=localhost
DB_PORT=13306
DB_NAME=cityeats
DB_USERNAME=root
DB_PASSWORD=123456
REDIS_HOST=localhost
REDIS_PORT=16379
REDIS_PASSWORD=
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5673
RABBITMQ_USERNAME=cityeats
RABBITMQ_PASSWORD=cityeats
```

如果你连接自己的本机 MySQL/Redis，请通过环境变量覆盖密码，不要把真实密码提交到仓库。

## API 示例

发送验证码：

```bash
curl -X POST "http://localhost:8080/user/code?phone=13800000003"
```

登录：

```bash
curl -X POST "http://localhost:8080/user/login" \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"13800000003\",\"code\":\"123456\"}"
```

商户详情：

```bash
curl "http://localhost:8080/shop/1"
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

## 数据库

初始化脚本：

```text
sql/schema.sql
sql/data.sql
```

主要表：

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

`tb_voucher_order` 包含唯一索引：

```sql
UNIQUE KEY uk_user_voucher (user_id, voucher_id)
```

## 测试

```bash
mvn clean test
mvn clean package
```

测试覆盖：

- Spring Context 与 Mapper 基础查询
- RedisIdWorker 并发生成 ID 唯一性
- CacheClient 空值缓存
- Lua 秒杀脚本关键原子操作
- VoucherOrder 事务落库的条件扣库存路径

## 与 AIOps Sentinel 的关系

CityEats 首先是一个独立的本地生活后端项目。后续它也可以作为 AIOps Sentinel 的第一个真实业务系统 Target，用于产生日志、指标、健康状态等观测数据。

本阶段 CityEats 不引入 Actuator、Micrometer、Prometheus、Grafana、Elasticsearch 或 Doris。可观测性接入会在后续专门阶段单独实现。
