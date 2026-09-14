# CampusLink 前端面试讲解

## 为什么选 Taro？

CampusLink 的主产品端是微信小程序，但也需要 H5 Demo 给 GitHub、简历和面试使用。Taro 能复用 React + TypeScript 业务代码，同时输出 WeApp 和 H5，避免维护两套前端。

## 为什么不是原生微信小程序？

原生小程序适合单端产品，但 CampusLink 需要 H5 展示端。用 Taro 可以保留小程序交互习惯，同时把业务组件、API、状态管理、表单和测试复用到 H5。

## 为什么需要 H5？

面试官通常在桌面浏览器打开链接。H5 Demo 可以快速体验核心链路，不要求安装微信开发者工具或配置小程序 AppID。

## 为什么 Server State 用 TanStack Query？

后端数据包括 Feed、地点、活动、报名状态和兴趣，这些是典型 Server State。TanStack Query 负责缓存、重试、失效、轮询和乐观更新，Zustand 只保存 token、用户快照和少量 UI 状态。

## 为什么点赞要 optimistic update？

点赞是高频轻交互。点击后立即变色和更新数量，用户感知更自然；如果 `PUT /posts/like/{id}` 失败，再回滚并提示。

## 为什么报名要轮询 status？

报名链路不是同步落库。后端先用 Redis Lua 原子校验和预扣减，再发 RabbitMQ 异步写 MySQL。POST 成功只代表请求提交成功，最终状态要通过 `/events/{eventId}/registrations/status` 确认。

## 为什么要 Platform Adapter？

定位、存储、分享、震动、导航在微信和 H5 的 API 不一样。页面只调用 `platform/location`、`storage/storage` 这类适配器，可以减少平台判断散落在业务代码里。

## 为什么 Redis GEO 对应附近地点？

校园地点天然有经纬度和分类。Redis GEO 可以按分类 key 存坐标，用半径查询返回距离，适合“附近食堂、自习室、运动场馆”这类移动端场景。

## 为什么 ZSet Feed 适合滚动分页？

关注 Feed 按发布时间排序，Redis ZSet 用时间戳做 score。后端使用 `lastId + offset` 做滚动分页，比传统 offset 分页更能避免新动态插入导致的重复或遗漏。

## 为什么 H5 有 Demo Mode？

H5 展示环境可能没有后端、CORS 或内网访问能力。`TARO_APP_DEMO_MODE=true` 时使用与真实 DTO 一致的 mock 数据，保证面试展示可打开，同时真实联调仍走 Spring Boot API。
