# CampusLink 前端 API Contract

本文档以当前 Spring Boot Controller、Service、DTO、Entity、SQL 为准。README 只作为辅助说明；当 README 与代码不一致时，以代码实现为准。

## 1. 通用约定

### Base URL

开发环境默认后端端口：

```text
http://localhost:8080
```

### Response Wrapper

所有业务接口返回 `Result`：

```ts
interface ApiResult<T> {
  success: boolean
  errorMsg: string | null
  data: T | null
  total: number | null
}
```

成功：

```json
{ "success": true, "errorMsg": null, "data": {}, "total": null }
```

失败：

```json
{ "success": false, "errorMsg": "验证码错误", "data": null, "total": null }
```

### Authentication

Token 由 `POST /auth/login` 返回，前端请求受保护接口时必须放入请求头：

```text
authorization: <token>
```

后端刷新拦截器同时兼容 `authorization` 与 `Authorization`，但 `logout` 只读取小写 `authorization`。

Token Redis Key：

```text
login:token:{token}
```

TTL：30 分钟，每次带 token 请求会刷新。

### 401 与业务未登录

`LoginInterceptor` 未登录时直接返回 HTTP 401，无 `Result` body。

部分 Service 也会返回：

```json
{ "success": false, "errorMsg": "请先登录", "data": null, "total": null }
```

前端需要同时处理 HTTP 401 与业务层 `请先登录`。

### 当前后端登录放行风险

当前 `MvcConfig` 放行路径是：

```text
/user/login
/user/code
```

但真实 Controller 映射是：

```text
/auth/login
/auth/code
/users/login
/users/code
```

因此未登录用户调用真实登录接口会被 `LoginInterceptor` 401 拦截。前端联调前需要做最小修复：放行 `/auth/login`、`/auth/code`、`/users/login`、`/users/code`。

### Pagination

普通分页接口使用 `current`，多数页面大小由后端常量控制：

```text
DEFAULT_PAGE_SIZE = 5
MAX_PAGE_SIZE = 10
```

关注 Feed 使用滚动分页：

```text
lastId: 上一页返回的 minTime，首次建议传当前时间戳
offset: 上一页返回的 offset，首次传 0
```

Feed 返回 `ScrollResult`：

```ts
interface ScrollResult<T> {
  list: T[]
  minTime: number
  offset: number
}
```

注意：当关注 Feed 为空时，当前实现返回 `data: []`，而不是 `ScrollResult`。

## 2. 数据模型

### UserDTO

```ts
interface UserDTO {
  id: number
  nickName: string
  icon: string
}
```

### UserInfo

```ts
interface UserInfo {
  userId: number
  city?: string
  introduce?: string
  fans?: number
  followee?: number
  gender?: boolean
  birthday?: string
  credits?: number
  level?: boolean
}
```

### PlaceCategory

```ts
interface PlaceCategory {
  id: number
  name: string
  icon: string
  sort: number
}
```

### CampusPlace

```ts
interface CampusPlace {
  id: number
  name: string
  categoryId: number
  images?: string
  area?: string
  address?: string
  x?: number
  y?: number
  avgPrice?: number
  sold?: number
  comments?: number
  score?: number
  openHours?: string
  createTime?: string
  updateTime?: string
  distance?: number
}
```

`distance` 仅在 `/places/nearby` 且传入坐标时由 Redis GEO 返回，单位是 km。

### CampusPost

```ts
interface CampusPost {
  id: number
  placeId?: number | null
  userId: number
  icon?: string
  name?: string
  isLike?: boolean
  title: string
  images?: string
  content?: string
  liked: number
  comments: number
  createTime?: string
  updateTime?: string
}
```

`icon`、`name`、`isLike` 是非数据库字段，由查询 Service 填充。

### CampusEvent

```ts
interface CampusEvent {
  id: number
  placeId?: number | null
  title: string
  description?: string
  category?: string
  location?: string
  cover?: string
  tags?: string
  startTime?: string
  endTime?: string
  registrationStartTime?: string
  registrationEndTime?: string
  capacity?: number
  status?: number
  createTime?: string
  updateTime?: string
}
```

### EventRegistration

```ts
interface EventRegistration {
  id: number
  userId: number
  eventId: number
  status: number
  createTime?: string
  updateTime?: string
}
```

当前 `status=1` 表示已落库报名记录；代码中没有暴露 PROCESSING/FAILED 状态。

## 3. API 详情

### 认证

#### POST /auth/code

发送验证码。

Request：

```text
query: phone=13800000001
```

Response：

```ts
ApiResult<string>
```

开发环境当前直接返回 6 位验证码。需要未登录可访问，但当前拦截器配置需修复。

#### POST /auth/login

手机号验证码登录。

Request：

```ts
interface LoginFormDTO {
  phone: string
  code: string
  password?: string
}
```

Response：

```ts
ApiResult<string> // token
```

#### POST /auth/logout

退出登录。

Authentication：需要 `authorization` header。

Response：

```ts
ApiResult<null>
```

#### GET /users/me

获取当前用户摘要。

Authentication：需要登录。

Response：

```ts
ApiResult<UserDTO>
```

#### GET /users/{id}

按用户 ID 获取用户摘要。

Authentication：需要登录。

Response：

```ts
ApiResult<UserDTO | null>
```

#### GET /users/info/{id}

获取用户扩展资料。

Authentication：需要登录。

Response：

```ts
ApiResult<UserInfo | null>
```

### 兴趣

#### GET /users/interests

获取我的兴趣标签。

Authentication：需要登录。

Response：

```ts
ApiResult<string[]>
```

#### PUT /users/interests

覆盖保存我的兴趣标签。

Authentication：需要登录。

Request：

```ts
string[]
```

规则：后端会 trim、去重，最多保存 20 个。

Response：

```ts
ApiResult<string[]>
```

### 地点分类

#### GET /place-categories/list

获取地点分类，按 `sort` 升序。

Authentication：不需要登录。

Response：

```ts
ApiResult<PlaceCategory[]>
```

### 地点

#### GET /places/{id}

获取地点详情。

Authentication：不需要登录。

Response：

```ts
ApiResult<CampusPlace>
```

Error：

```text
校园地点不存在
```

#### GET /places/nearby

按分类查询地点；传入坐标时使用 Redis GEO 查询 5km 内地点。

Authentication：不需要登录。

Request：

```ts
interface NearbyPlacesQuery {
  categoryId: number
  current?: number
  x?: number // 经度
  y?: number // 纬度
}
```

Response：

```ts
ApiResult<CampusPlace[]>
```

Pagination：`current`，每页 5 条。

#### GET /places/search

Elasticsearch 地点搜索。

Authentication：不需要登录。

Request：

```ts
interface CampusPlaceSearchRequest {
  keyword?: string
  categoryId?: number
  area?: string
  minPrice?: number
  maxPrice?: number
  minScore?: number
  current?: number
  size?: number // 最大 50
  sort?: 'score_desc' | 'sold_desc' | 'avgPrice_asc' | 'avgPrice_desc'
}
```

Response：

```ts
interface CampusPlaceSearchResponse {
  records: CampusPlace[]
  total: number
  current: number
  size: number
}

ApiResult<CampusPlaceSearchResponse>
```

#### GET /places/of/name

按名称模糊搜索 MySQL 地点。

Authentication：不需要登录。

Request：

```text
name?: string
current?: number
```

Response：

```ts
ApiResult<CampusPlace[]>
```

### 动态

#### POST /posts

发布校园动态。

Authentication：需要登录。

Request：

```ts
Partial<CampusPost> & {
  title: string
  content?: string
  placeId?: number | null
  images?: string
}
```

Response：

```ts
ApiResult<number> // postId
```

当前后端允许 `images` 字符串，但图片上传链路为 `/upload/post`，生产前端可先不展示图片入口。

#### GET /posts/{id}

动态详情。

Authentication：不需要登录；登录时会填充 `isLike`。

Response：

```ts
ApiResult<CampusPost>
```

#### GET /posts/hot

热门动态，按点赞数倒序。

Authentication：不需要登录；登录时会填充 `isLike`。

Request：

```text
current?: number
```

Response：

```ts
ApiResult<CampusPost[]>
```

Pagination：`current`，每页 10 条。

#### PUT /posts/like/{id}

点赞或取消点赞。后端根据当前用户是否在 Redis ZSet 中切换状态。

Authentication：需要登录。

Response：

```ts
ApiResult<null>
```

前端可做 optimistic update，失败后 rollback。

#### GET /posts/likes/{id}

获取点赞用户 Top 5。

Authentication：不需要登录。

Response：

```ts
ApiResult<UserDTO[]>
```

#### GET /posts/of/me

我的动态。

Authentication：需要登录。

Request：

```text
current?: number
```

Response：

```ts
ApiResult<CampusPost[]>
```

#### GET /posts/of/user

指定用户动态。

Authentication：需要登录。

Request：

```text
id: number
current?: number
```

Response：

```ts
ApiResult<CampusPost[]>
```

### Feed

#### GET /feeds

关注动态滚动分页。

Authentication：需要登录。

Request：

```text
lastId: number
offset?: number
```

Response：

```ts
ApiResult<ScrollResult<CampusPost> | []>
```

分页说明：后端每次取 2 条。`lastId` 对应 Redis ZSet score，`offset` 用来处理同 score 去重。

#### GET /posts/feeds

同 `/feeds`，保留兼容入口。前端统一使用 `/feeds`。

### 关注

#### PUT /follows/{id}/{isFollow}

关注或取消关注。

Authentication：需要登录。

Path：

```text
id: 被关注用户 ID
isFollow: true 表示关注，false 表示取消
```

Response：

```ts
ApiResult<null>
```

#### GET /follows/or/not/{id}

查询是否已关注。

Authentication：需要登录可得到真实状态；未登录返回 false。

Response：

```ts
ApiResult<boolean>
```

#### GET /follows/common/{id}

共同关注。

Authentication：需要登录。

Response：

```ts
ApiResult<UserDTO[]>
```

### 活动

#### GET /events

活动列表，按创建时间倒序。

Authentication：不需要登录。

Request：

```text
current?: number
```

Response：

```ts
ApiResult<CampusEvent[]>
```

Pagination：`current`，每页 10 条。

#### GET /events/{id}

活动详情。

Authentication：不需要登录。

Response：

```ts
ApiResult<CampusEvent>
```

Error：

```text
校园活动不存在
```

#### GET /events/hot

热门活动。当前实现按剩余名额少优先，再按创建时间倒序。

Authentication：不需要登录。

Request：

```text
current?: number
```

Response：

```ts
ApiResult<CampusEvent[]>
```

#### GET /events/by-place/{placeId}

查询地点关联活动。

Authentication：不需要登录。

Response：

```ts
ApiResult<CampusEvent[]>
```

#### GET /events/recommendations

规则推荐活动。

Authentication：可选。登录后会读取我的兴趣，未登录时基于热度和时间排序。

Request：

```text
limit?: number // 默认 10，最大 50
```

Response：

```ts
ApiResult<CampusEvent[]>
```

前端文案应使用“根据你的兴趣推荐”或“猜你喜欢”，不要写“AI 推荐”。

#### POST /events/{eventId}/registrations

活动报名。

Authentication：需要登录。

Response：

```ts
ApiResult<number> // registrationId
```

真实流程：Redis Lua 预扣减并防重复，成功后发送 RabbitMQ，数据库异步落库。

成功响应只表示“报名请求已提交”，不能立即显示最终报名成功。

Error：

```text
请先登录
活动报名信息不存在
活动报名尚未开始
活动报名已结束
活动剩余名额不足
不能重复报名
```

#### GET /events/{eventId}/registrations/status

查询当前用户对某活动是否已落库报名。

Authentication：需要登录。

Response：

```ts
ApiResult<boolean>
```

前端状态映射建议：

```text
POST success -> PROCESSING
poll status true -> REGISTERED
poll timeout while false -> STILL_PROCESSING
POST fail -> FAILED
```

#### POST /event-registrations/{eventId}

报名兼容入口，与 `/events/{eventId}/registrations` 调用同一 Service。前端统一使用 `/events/{eventId}/registrations`。

#### GET /event-registrations/me

我的报名记录。

Authentication：需要登录。

Response：

```ts
ApiResult<EventRegistration[]>
```

注意：该接口只返回报名记录，不返回活动详情。前端如需展示活动标题，需要再请求 `/events/{id}` 或前端做聚合缓存。

### 上传

#### POST /upload/post

上传动态图片。

Authentication：当前拦截器放行。

Request：`multipart/form-data`，字段名 `file`。

Response：

```ts
ApiResult<string> // 文件相对路径
```

风险：当前保存路径中使用 `/postss/`，并且没有静态资源访问映射。第一版前端不展示生产图片上传入口。

#### GET /upload/posts/delete

删除动态图片。

Authentication：当前拦截器放行。

Request：

```text
name: string
```

Response：

```ts
ApiResult<null>
```

### 管理

#### POST /admin/search/places/rebuild

重建地点搜索索引。

Authentication：当前需要登录，因为未在拦截器放行。

Response：

```ts
ApiResult<number> // 索引文档数量
```

## 4. 可以直接实现的前端功能

- 手机号验证码登录、退出、当前用户。
- 地点分类、地点详情、分类地点列表、附近地点、地点搜索。
- 热门动态、动态详情、发布文字动态、点赞/取消点赞、点赞用户 Top 5。
- 关注、取消关注、是否关注、共同关注。
- 关注 Feed 滚动分页。
- 活动列表、热门活动、规则推荐活动、活动详情、地点关联活动。
- 活动报名提交、报名状态轮询、我的报名记录。
- 我的兴趣读取与保存。
- 签到与连续签到天数。

## 5. 后端暂不完整或前端需要降级的功能

- 评论 Controller 为空，不能做评论提交或评论列表。
- 活动详情没有剩余名额字段；只能基于 `capacity` 和报名状态展示，不能准确展示剩余名额。
- 我的活动接口不返回活动详情，需要前端二次查询或展示报名记录。
- 地点详情没有专门“附近地点”“校园讨论”接口，只能用已有 `/events/by-place/{placeId}` 和 `/places/nearby` 做真实能力范围内的关联。
- 图片上传虽有接口，但缺少静态资源访问确认，第一版不在生产发布动态中展示图片上传。
- H5 跨域未配置 CORS，若前后端不同源运行需要后端加最小 CORS 配置。
- 登录接口当前被拦截器误拦，需要最小修复。
- 活动报名状态接口返回 boolean，不返回 PROCESSING/FAILED 等细粒度状态。

## 6. 前端架构设计

```text
frontend/
  src/
    api/              axios client 与接口模块
    components/       跨业务 UI 组件
    features/         auth/feed/place/event/follow/interest
    hooks/            通用 hooks
    mocks/            H5 demo mode mock
    pages/            Taro 页面
    platform/         location/navigation/share/haptics adapter
    queries/          TanStack Query key factory
    schemas/          zod form schema
    storage/          Taro/H5 storage adapter
    store/            Zustand token/user/campus/ui
    styles/           tokens.scss/global.scss
    types/            后端 DTO/Entity TS 类型
    utils/            formatters/time/distance
```

### Server State

使用 TanStack Query 管理接口缓存、分页、轮询、乐观更新。

### Client State

Zustand 只保存：

- token
- current user snapshot
- campus 默认位置
- UI settings

### Platform Adapter

业务页面不直接判断 `process.env.TARO_ENV`，统一走：

- `platform/location.ts`
- `platform/navigation.ts`
- `platform/share.ts`
- `storage/storage.ts`

### API Client

- 统一 axios/Taro adapter。
- 请求头使用 `authorization`。
- unwrap `Result`。
- HTTP 401 清 token 并跳登录。
- 业务失败抛出带 `errorMsg` 的错误。

## 7. 页面信息架构

底部 Tab：

```text
首页 / 发现 / 发布 / 活动 / 我的
```

Taro 原生 TabBar 更稳定。发布页作为中间 Tab，样式略突出但不做巨大悬浮按钮。

页面：

```text
pages/home/index
pages/discover/index
pages/community/index
pages/publish/index
pages/events/index
pages/profile/index
pages/login/index
pages/search/index
pages/place-detail/index
pages/post-detail/index
pages/event-detail/index
pages/interests/index
pages/my-events/index
```

首屏不是 Landing Page，直接进入校园生活信息流。

## 8. Design System

视觉关键词：

- 校园公告栏
- 学生证
- 课表
- 地图坐标
- 社团海报
- 图书馆借阅卡

基础 Token：

```scss
:root {
  --cl-bg: #f7f7f3;
  --cl-surface: #ffffff;
  --cl-text: #20211f;
  --cl-text-secondary: #74756f;
  --cl-border: #e9e9e3;
  --cl-primary: #376b5a;
  --cl-primary-soft: #e7f0ec;
  --cl-accent: #d99a50;
  --cl-danger: #c95852;
}
```

Spacing：

```text
4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 / 64
```

Radius：

```text
sm 6 / md 10 / lg 16 / pill 999
```

Typography：

```text
PingFang SC, Noto Sans SC, system-ui
```

避免：

- 紫蓝渐变
- 巨大 Hero
- 玻璃拟态
- 假数据统计
- emoji 图标
- generic feature cards
- 随机 Unsplash 图

## 9. 技术风险

1. 登录接口当前被拦截，必须最小修复后才能真实登录。
2. 微信小程序网络请求不能使用浏览器专用 axios adapter，需要 Taro request adapter 或封装 Taro.request。
3. H5 联调不同源需要 CORS；小程序不受浏览器 CORS 限制。
4. 小程序生产环境 request 合法域名必须 HTTPS。
5. Feed 空状态返回数组，非空返回 ScrollResult，前端类型需要兼容。
6. 报名状态只有 boolean，前端需要自建 PROCESSING/TIMEOUT UI 状态。
7. 图片路径目前可能无法直接访问，第一版用 CSS Typography Poster 和本地静态图策略。
8. 地点搜索依赖 Elasticsearch 索引；本地演示前需要调用 rebuild 或使用 DEMO_MODE。
9. 我的报名记录缺少活动详情，前端要避免展示无法证明的数据。
10. Taro 端第三方 UI 与图标库需要控制体积和小程序兼容性。
