# NexusHub 数据保留与迁移

本流程是运维人员在维护窗口内执行的独立方案。本次代码重构不自动连接或修改任何已有业务数据库、Redis、消息队列、索引或 Docker 数据卷。

## 全新环境

`sql/schema.sql` 和 `sql/data.sql` 只用于全新空数据库，Compose 仅在 MySQL 数据目录首次初始化时加载。生产 schema 已移除所有 DROP TABLE；已有表会报错，应立即停止，禁止使用 `mysql --force`。演示数据均为模拟数据，不能灌入业务数据库。H2 测试专用 schema 的 DROP 仅用于重置内存测试库。

## 迁移前保护

1. 记录当前应用版本、环境变量、数据库版本、卷的真实名称和挂载路径。用 `docker inspect`、`docker volume ls` 和 `docker compose config` 核对，不凭前缀推测卷名。
2. 进入维护窗口，阻止新写入及新报名，保留旧消费者运行，等待报名队列无 ready/unacked 消息；检查并处理死信队列。确认所有预扣减已经落库或补偿后，才停止旧应用和消费者。不要在有在途报名时启动新实例的名额预热。
3. 对 MySQL 做一致性备份，对 Redis、RabbitMQ、Elasticsearch 的持久化目录做可恢复快照，并验证备份可恢复。备份文件含业务数据和可能的凭据，应放在受保护的外部目录，不提交 Git。

## MySQL：保留旧库，恢复到新库

MySQL 没有 RENAME DATABASE。本方案复制到独立空库后，在新库内重命名四张表，原库一直保留。以下为 Bash 命令示例，主机、端口、用户及备份位置按实际环境填写，密码交互输入：

```bash
mysqldump --host=127.0.0.1 --port=13306 --user=root --password \
  --single-transaction --routines --events --triggers --no-tablespaces \
  --set-gtid-purged=OFF campuslink > /secure-backup/community-before.sql

# 故意不加 IF NOT EXISTS：目标库已存在则停止，避免合并/覆盖数据。
mysql --host=127.0.0.1 --port=13306 --user=root --password \
  -e 'CREATE DATABASE nexushub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci'
mysql --host=127.0.0.1 --port=13306 --user=root --password nexushub \
  < /secure-backup/community-before.sql
mysql --host=127.0.0.1 --port=13306 --user=root --password nexushub \
  < sql/migrations/001_rename_community_tables.sql
```

不要使用 `mysqldump --databases`，否则恢复文件会含旧库的 CREATE/USE 指令。执行恢复前审查备份中的数据库限定名、触发器、视图、事件及存储过程；仓库当前仅定义普通 InnoDB 表，自定义扩展需单独审核。目标库必须为空，任何一步失败均停止，不接着导入模拟数据。不要把初始化 schema 用于迁移。

迁移脚本使用单条 RENAME TABLE，保留数据、主键、自增计数和所有索引。`event_registration`、`event_capacity` 不参与改名，`uk_user_event(user_id,event_id)`、条件扣减及事务语义保持不变。

切换前核对：11 张表逐表行数（四张表映射旧名）、主键范围、重要业务记录、`SHOW CREATE TABLE` 与所有索引；检查活动与地点关联、容量和报名记录的一致性。确保数据库用户获得新库所需权限，再设置 `DB_NAME=nexushub`。不要删除旧库。

## Docker 数据卷

默认新部署卷为 `nexushub-mysql-data`、`nexushub-redis-data`、`nexushub-rabbitmq-data`、`nexushub-elasticsearch-data`。改名不会复制旧卷；创建新卷也不代表旧数据丢失。Compose 支持 `MYSQL_DATA_VOLUME`、`REDIS_DATA_VOLUME`、`RABBITMQ_DATA_VOLUME`、`ELASTICSEARCH_DATA_VOLUME` 显式选择卷。

有两种方式：保持原中间件运行，仅切换应用配置；或在已验证备份、旧服务停写并停止后，将对应变量设置为核实过的旧卷名并迁移容器。第二种方式必须先确认卷存在，否则 Docker 可能创建同名空卷。绝不能让两个数据库/中间件进程同时挂载同一个数据目录。禁止执行 `docker compose down -v` 或删除旧卷。

复用 MySQL 卷时，`MYSQL_DATABASE` 环境变量不会重建或迁移已存在的数据目录，仍须手动完成新库恢复。密码也不会随新环境变量自动修改。Redis 必须复用原凭据；保留所有通用 Key，包括登录、GEO、Feed、点赞和补偿标记。RabbitMQ 复用数据时必须保留原节点名、hostname、cookie 和用户凭据，不能仅凭替换卷名启动；先记录旧容器配置，必要时使用专门 Compose override。`RABBITMQ_DEFAULT_USER/PASS` 只用于空目录初始化。

RabbitMQ Exchange、Queue、Routing Key 保持原值。包名更改会影响 JSON 消息的 Java 类型头；消费者的具体 `EventRegistrationMessage` 参数使用 Spring AMQP 默认的推断类型优先策略，回归测试覆盖旧类型头。仍应按上文排空队列后再切换，避免新旧消费者混跑。

## Elasticsearch：保留旧索引，重建新索引

1. 先确认 MySQL 迁移及数据校验完成，暂停地点写入，备份 ES 并记录旧索引 `campuslink_place` 的文档数量和 mapping。
2. 新应用配置 `PLACE_SEARCH_INDEX=nexushub_place`。使用有效登录 token 调用 `POST /admin/search/places/rebuild`，从新库批量写入新索引。此接口会重建**当前配置的目标索引**，执行前确认目标不是旧索引；不要将 `PLACE_SEARCH_INDEX` 指回旧索引后调用重建。
3. 刷新新索引，比较 MySQL 地点数和 ES `_count`，验证关键词、分类、地区、价格、评分、排序及分页。通过地点新增/更新确认增量同步后恢复写入。
4. 旧索引保持原样，禁止自动删除。若需运维侧 `_reindex`，先独立创建目标 mapping，检查任务结果、失败项、数量与搜索结果；本项目推荐从 MySQL 重建以数据库为准。

## 回退

切换验收前保持业务停写。若验收失败，停止新应用，恢复旧应用版本、`DB_NAME=campuslink`、旧索引和原中间件连接；原库、原索引、原卷仍在。若新系统已接收写入，两个库已分叉，不能直接回切，必须先备份新增数据并设计增量回灌。备份及旧数据的后续清理由维护人员另行确认，本任务不执行。
