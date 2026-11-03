# 真实中间件联调

仅在独立测试环境使用模拟数据执行。不要对生产活动注入失败、构造重投消息或修改剩余容量。先按 README 启动中间件和应用；确认所有健康检查通过、应用日志出现启动完成和 GEO 初始化完成。

## 登录与基础业务

以下示例使用 Bash，应用地址按实际端口调整。`/auth/code` 当前开发实现返回验证码，不会发送真实短信。

```bash
curl -X POST 'http://localhost:8080/auth/code?phone=13800000001'
curl -X POST 'http://localhost:8080/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800000001","code":"替换为验证码"}'
```

将登录返回的 token 直接放入 `authorization` 请求头，不能加 `Bearer ` 前缀。应检查：

1. `GET /events/1` 的标题为“Java 后端技术交流会”，中文完整；活动与地点关联有效，容量及报名时间窗口与 `event_capacity` 一致。
2. 两次 `GET /places/1` 返回相同字段；Redis 中出现 `cache:place:1`。查询不存在地点返回失败并缓存短 TTL 空值。
3. `GET /places/nearby?categoryId=1&x=120.1542&y=30.2871` 返回附近地点和距离。
4. 使用 token 调用 `POST /users/sign` 和 `GET /users/sign/count`，确认 Bitmap 签到；更新兴趣后查询推荐活动。
5. 两个不同用户建立关注，发布动态后确认关注者 Feed 可见；点赞后确认 `post:liked:{id}` 为 ZSet。
6. 上传头像验证 multipart、MIME、扩展名与大小检查；默认模拟存储返回 `https://mock.nexushub.local/...`，不代表真实文件已上传。当前用户接口只允许 AVATAR、POST，PLACE、EVENT 仍由现有权限限制拒绝。

## Elasticsearch

```bash
curl -X POST 'http://localhost:8080/admin/search/places/rebuild' \
  -H "authorization: $TOKEN"
curl -X POST 'http://localhost:19200/nexushub_place/_refresh'
curl 'http://localhost:19200/nexushub_place/_count'
curl 'http://localhost:8080/places/search?categoryId=1&minPrice=0&maxPrice=100&minScore=40&sort=score_desc&current=1&size=10'
```

确认新索引文档数与 MySQL 地点数一致，并分别检查关键词、地区、分类、价格、评分、四种排序和第二页。新增或更新地点后刷新索引并检索新名称，确认沿用事务提交后的监听器完成增量同步。旧索引如存在，整个过程不得删除它。

## 报名并发、重投与补偿

准备报名窗口开放、容量为 5 的模拟活动，创建 24 个不同用户。先让一人报名，再在尚有余量时重复报名，应明确拒绝重复。其余用户并发请求报名，最终必须满足：

- 共 5 个成功受理结果，5 条数据库报名记录，`COUNT(DISTINCT user_id)=5`。
- MySQL `remaining_slots=0`，Redis `event:slots:{eventId}=0`，报名集合成员数为 5。
- 名额耗尽后的报名请求被拒绝，不出现负容量。

等待队列消费完毕后，将一条已成功记录对应的相同 `registrationId/userId/eventId/createTime` 消息重投至原 Exchange，使用新旧两种 Java 类型头分别验证。数据库仍为 5 条，容量不变。原拓扑中的 `campuslink.event.registration.*` 名称是保留的消息协议，不属于未迁移的 Java 包。

仅在模拟活动中构造 Redis 仍有 1 个名额、MySQL 名额为 0 的不一致状态，然后发起报名。应观察消费者三次尝试、事务回滚、死信消费与补偿：没有报名记录，Redis 名额恢复为 1，用户从报名集合移除，出现 `event:registration:compensated:{registrationId}`。重复投递该死信两次，Redis 名额必须仍为 1，标记保留正 TTL。成功落库记录对应的死信不应补偿。

此处是业务正确性回归，不能据此推导 QPS/TPS。真实 OSS 写入需要有效凭据，本仓库不携带密钥。

## 本次重构验证范围

自动化测试在 JDK 17 上运行，Maven 编译目标仍为 Java 8；42 项 JUnit/H2/Mockito 测试通过。独立 Docker 验证环境使用 MySQL 8.0、Redis 6.2、RabbitMQ 3.12-management、Elasticsearch 7.6.2，按上面的基础业务、索引、并发和补偿场景执行。生产数据未参与验证。

数据库迁移验证使用旧版 schema/data 生成独立模拟源库，再备份恢复到空目标库并执行改名脚本，逐表比对 11 张表的全部记录、逻辑索引及已有报名容量；重复执行脚本应失败且不影响已迁移数据。迁移与回退步骤见 [migration.md](migration.md)。
