import type { CampusEvent, CampusPlace, CampusPost, PlaceCategory, UserDTO } from '@/types/api'

export const mockUser: UserDTO = {
  id: 1,
  nickName: '林同学',
  icon: ''
}

export const mockCategories: PlaceCategory[] = [
  { id: 1, name: '食堂', icon: '/types/canteen.png', sort: 1 },
  { id: 2, name: '自习空间', icon: '/types/study.png', sort: 2 },
  { id: 3, name: '运动场馆', icon: '/types/sports.png', sort: 3 },
  { id: 4, name: '校园服务', icon: '/types/service.png', sort: 4 }
]

export const mockPlaces: CampusPlace[] = [
  {
    id: 1,
    name: '沙河校区图书馆',
    categoryId: 2,
    area: '中心区',
    address: '北京市昌平区南丰路北邮沙河校区图书馆',
    x: 116.2808,
    y: 40.1576,
    score: 48,
    sold: 3200,
    comments: 176,
    openHours: '08:00-22:30',
    distance: 0.18
  },
  {
    id: 2,
    name: '学生食堂',
    categoryId: 1,
    area: '生活区',
    address: '学生公寓东侧',
    x: 116.282,
    y: 40.1582,
    score: 46,
    sold: 2600,
    comments: 218,
    openHours: '06:30-21:00',
    distance: 0.42
  },
  {
    id: 3,
    name: '篮球场',
    categoryId: 3,
    area: '运动区',
    address: '操场西侧',
    x: 116.2792,
    y: 40.1569,
    score: 45,
    sold: 1800,
    comments: 96,
    openHours: '09:00-21:30',
    distance: 0.65
  },
  {
    id: 4,
    name: '快递服务中心',
    categoryId: 4,
    area: '北区',
    address: '学生公寓 5 号楼旁',
    x: 116.281,
    y: 40.1592,
    score: 44,
    sold: 2100,
    comments: 88,
    openHours: '09:00-19:00',
    distance: 0.53
  }
]

export const mockPosts: CampusPost[] = [
  {
    id: 1,
    placeId: 1,
    userId: 1,
    name: '林同学',
    icon: '',
    isLike: false,
    title: '图书馆三楼安静角落推荐',
    content: '靠窗区域插座多，下午复习很稳。晚饭前人会少一点。',
    liked: 10,
    comments: 0,
    createTime: new Date().toISOString()
  },
  {
    id: 2,
    placeId: 3,
    userId: 2,
    name: '周同学',
    icon: '',
    isLike: true,
    title: '篮球场今晚有 3v3 约球',
    content: '差两个人，七点半开始，轻松局。',
    liked: 8,
    comments: 0,
    createTime: new Date().toISOString()
  }
]

export const mockEvents: CampusEvent[] = [
  {
    id: 1,
    placeId: 4,
    title: 'Java 后端技术分享会',
    description: '围绕 Spring Boot、Redis 和高并发实践展开的校园技术分享。',
    category: '技术讲座',
    location: '教学楼 B201',
    tags: 'Java,后端,Redis',
    startTime: new Date(Date.now() + 3 * 86400000).toISOString(),
    endTime: new Date(Date.now() + 3 * 86400000 + 7200000).toISOString(),
    registrationStartTime: new Date(Date.now() - 86400000).toISOString(),
    registrationEndTime: new Date(Date.now() + 2 * 86400000).toISOString(),
    capacity: 100,
    status: 1
  },
  {
    id: 2,
    placeId: 3,
    title: '校园篮球联赛',
    description: '面向全校学生的 3v3 篮球赛。',
    category: '体育赛事',
    location: '篮球场',
    tags: '篮球,体育',
    startTime: new Date(Date.now() + 7 * 86400000).toISOString(),
    endTime: new Date(Date.now() + 7 * 86400000 + 14400000).toISOString(),
    registrationStartTime: new Date(Date.now() - 86400000).toISOString(),
    registrationEndTime: new Date(Date.now() + 5 * 86400000).toISOString(),
    capacity: 60,
    status: 1
  }
]
