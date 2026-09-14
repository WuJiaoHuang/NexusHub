export default defineAppConfig({
  pages: [
    'pages/home/index',
    'pages/discover/index',
    'pages/publish/index',
    'pages/events/index',
    'pages/profile/index',
    'pages/login/index',
    'pages/search/index',
    'pages/place-detail/index',
    'pages/post-detail/index',
    'pages/event-detail/index',
    'pages/interests/index',
    'pages/my-events/index'
  ],
  window: {
    backgroundTextStyle: 'dark',
    navigationBarBackgroundColor: '#F7F7F3',
    navigationBarTitleText: 'CampusLink',
    navigationBarTextStyle: 'black',
    backgroundColor: '#F7F7F3',
    enablePullDownRefresh: true
  },
  tabBar: {
    color: '#74756F',
    selectedColor: '#376B5A',
    backgroundColor: '#FFFFFF',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '首页'
      },
      {
        pagePath: 'pages/discover/index',
        text: '发现'
      },
      {
        pagePath: 'pages/publish/index',
        text: '发布'
      },
      {
        pagePath: 'pages/events/index',
        text: '活动'
      },
      {
        pagePath: 'pages/profile/index',
        text: '我的'
      }
    ]
  }
})
