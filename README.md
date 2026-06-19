CityEats（城市食客）

一、项目简介

CityEats（城市食客）是一个基于Spring Boot的本地生活服务点评平台，提供了商铺管理、优惠券秒杀、用户关注、博客分享、签到打卡等完整功能，并采用了Redis、RabbitMQ等技术实现高性能和高并发处理。

二、技术栈

后端框架：Spring Boot、MyBatis Plus、Spring MVC
数据存储：MySQL（关系型数据库，存储核心业务数据）、Redis（缓存、分布式锁、点赞、关注关系、签到等功能）、RabbitMQ（消息队列，异步处理秒杀订单）
其他技术：Redisson（Redis分布式锁实现）、Lua脚本（Redis原子操作）、Hutool（Java工具类库）、Lombok（简化Java代码）

三、项目结构

com.点评生活
├── controller          # 控制器层，处理HTTP请求
│   ├── BlogController
│   ├── FollowController
│   ├── ShopController
│   ├── UserController
│   └── VoucherOrderController
├── service             # 业务逻辑层
│   ├── impl           # 服务实现类
│   ├── IBlogService
│   ├── IFollowService
│   └── IVoucherOrderService
├── mapper              # MyBatis数据访问层
├── entity              # 实体类
├── dto                 # 数据传输对象
├── interceptor         # 拦截器（登录拦截、token刷新）
├── listener            # 消息监听器
├── utils               # 工具类
│   ├── RedisConstants
│   ├── UserHolder      # 线程本地存储用户信息
│   ├── RedisIdWorker   # 全局ID生成器
│   └── CacheClient     # 缓存客户端工具
└── config              # 配置类

四、核心功能模块

1. 用户模块：手机号验证码登录、Token认证（基于Redis存储）、用户签到功能（使用Redis BitMap）、连续签到统计

2. 商铺模块：商铺信息缓存（解决缓存穿透、击穿问题）、商铺类型分类、基于地理位置（GEO）的商铺搜索、商铺信息更新（缓存同步）

3. 优惠券秒杀模块：秒杀券发布与管理、基于Lua脚本的原子性库存扣减、Redis分布式锁防止超卖、RabbitMQ异步订单处理、一人一单限制

4. 博客模块：探店博客发布、博客点赞（使用Redis ZSet）、点赞排行榜（Top5）、关注推送（Feed流）、滚动分页查询

5. 社交模块：用户关注/取关、共同关注查询（Redis Set交集）、关注推送博客流

五、数据库设计

主要数据表：
tb_user - 用户表
tb_user_info - 用户信息表
tb_shop - 商铺表
tb_shop_type - 商铺类型表
tb_voucher - 优惠券表
tb_seckill_voucher - 秒杀券表
tb_voucher_order - 订单表
tb_blog - 博客表
tb_blog_comments - 博客评论表
tb_follow - 关注关系表

六、核心业务流程

秒杀抢购流程：
1. 用户发起秒杀请求
2. 执行Lua脚本检查库存和重复购买
3. 库存充足且未购买则生成订单ID
4. 发送订单消息到RabbitMQ
5. 异步消费消息创建订单并扣减库存
6. 返回订单ID给前端

博客推送流程：
1. 用户发布新博客
2. 查询该用户的所有粉丝
3. 将博客ID推送到每个粉丝的Feed流（Redis ZSet）
4. 粉丝拉取时按时间排序获取博客列表

七、API接口文档

用户接口：
POST /user/code - 发送验证码
POST /user/login - 用户登录
POST /user/sign - 用户签到
GET /user/sign/count - 获取连续签到天数

商铺接口：
GET /shop/{id} - 查询商铺详情
PUT /shop - 更新商铺信息
GET /shop/of/type - 按类型分页查询商铺

秒杀接口：
POST /voucher-order/seckill/{id} - 秒杀抢购
POST /voucher/seckill - 新增秒杀券

博客接口：
POST /blog - 发布博客
PUT /blog/like/{id} - 点赞/取消点赞
GET /blog/hot - 热门博客列表
GET /blog/of/follow - 关注推送博客

八、环境配置

配置文件示例 (application.yml)：
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lifereview
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

Redis Key设计：
login:user:token - 用户登录信息
login:code:phone - 验证码
cache:shop:id - 商铺缓存
seckill:stock:voucherId - 秒杀库存
blog:liked:blogId - 博客点赞集合
feed:userId - 用户Feed流
follows:userId - 用户关注集合
sign:userId:yyyyMM - 用户签到记录

九、项目特色

1. 缓存解决方案：缓存穿透（缓存空对象）、缓存击穿（互斥锁 + 逻辑过期）、缓存雪崩（设置不同过期时间）

2. 高并发处理：Redis原子操作（Lua脚本）、分布式锁（Redisson）、消息队列异步处理、全局ID生成器（雪花算法）

3. 性能优化：Feed流推送（时间线排序）、点赞排行榜（Redis ZSet）、地理位置搜索（Redis GEO）、BitMap签到统计

十、部署说明

前置条件：JDK 8+、MySQL 5.7+、Redis 6.0+、RabbitMQ 3.8+、Maven 3.6+

部署步骤：
1. 克隆项目代码
2. 导入数据库脚本（位于sql/目录）
3. 修改配置文件中的数据库、Redis、RabbitMQ连接信息
4. 使用Maven打包：mvn clean package
5. 运行JAR包：java -jar lifereview.jar

十一、注意事项

1. 秒杀场景：确保RabbitMQ服务正常运行，否则订单会丢失
2. Redis连接：建议使用连接池，避免连接超时
3. 分布式锁：使用Redisson确保锁的可靠性
4. 缓存一致性：更新商铺信息后需同步删除缓存