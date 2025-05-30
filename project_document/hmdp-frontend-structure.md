# HMDP 前端项目结构

## 项目概览
HMDP（黑马点评）是一个点评类应用的前端项目，基于HTML、CSS、JavaScript构建，使用Vue.js作为前端框架，并通过Nginx进行部署。

## 目录结构

```
hmdp-front/
├── logs/                           # 日志文件目录
├── nginx-1.18.0/                   # Nginx服务器
│   ├── conf/                       # Nginx配置文件
│   ├── html/                       # 网站根目录
│   │   └── hmdp/                   # 项目主目录
│   │       ├── css/                # 样式文件
│   │       │   ├── fonts/          # 字体文件
│   │       │   ├── blog-detail.css # 博客详情样式
│   │       │   ├── blog-edit.css   # 博客编辑样式
│   │       │   ├── element.css     # Element UI样式
│   │       │   ├── index.css       # 首页样式
│   │       │   ├── info.css        # 用户信息样式
│   │       │   ├── login.css       # 登录页样式
│   │       │   ├── main.css        # 主样式文件
│   │       │   ├── shop-detail.css # 商铺详情样式
│   │       │   ├── shop-list.css   # 商铺列表样式
│   │       │   └── user-blogs.css  # 用户博客样式
│   │       ├── imgs/               # 图片资源
│   │       │   ├── blogs/          # 博客图片
│   │       │   ├── icons/          # 图标
│   │       │   └── types/          # 类型图标
│   │       ├── js/                 # JavaScript文件
│   │       │   ├── axios.min.js    # Axios HTTP客户端
│   │       │   ├── common.js       # 公共JS函数
│   │       │   ├── element.js      # Element UI库
│   │       │   ├── footer.js       # 页脚相关JS
│   │       │   ├── stagewise.js    # 分步操作JS
│   │       │   └── vue.js          # Vue.js框架
│   │       ├── admin-content.html          # 管理员内容页面
│   │       ├── admin-dashboard.html        # 管理员仪表盘
│   │       ├── admin-login.html            # 管理员登录
│   │       ├── admin-merchants.html        # 管理员商家管理
│   │       ├── admin-shop-types.html       # 管理员商铺类型管理
│   │       ├── admin-statistics.html       # 管理员统计页面
│   │       ├── admin-users.html            # 管理员用户管理
│   │       ├── favicon.ico                 # 网站图标
│   │       ├── index.html                  # 首页
│   │       ├── merchant-account.html       # 商家账户页面
│   │       ├── merchant-center.html        # 商家中心
│   │       ├── merchant-comment.html       # 商家评论管理
│   │       ├── merchant-login.html         # 商家登录
│   │       ├── merchant-order.html         # 商家订单管理
│   │       ├── merchant-register.html      # 商家注册
│   │       ├── merchant-shop.html          # 商家店铺管理
│   │       ├── merchant-voucher.html       # 商家优惠券管理
│   │       ├── user-blog-detail.html       # 用户博客详情
│   │       ├── user-blog-edit.html         # 用户博客编辑
│   │       ├── user-blogs.html             # 用户博客列表
│   │       ├── user-hop-detail.html        # 用户店铺详情
│   │       ├── user-hop-list.html          # 用户店铺列表
│   │       ├── user-index.html             # 用户首页
│   │       ├── user-info-edit.html         # 用户信息编辑
│   │       ├── user-info.html              # 用户信息页面
│   │       ├── user-info_fixed.html        # 用户信息页面(固定版)
│   │       ├── user-login.html             # 用户登录
│   │       └── user-other-info.html        # 其他用户信息页面
│   ├── logs/                       # Nginx日志
│   └── temp/                       # Nginx临时文件
└── temp/                           # 临时文件目录
```

## 页面功能分类

### 用户相关页面
- **user-login.html**: 用户登录页面
- **user-info.html**: 用户个人信息页面
- **user-info_fixed.html**: 用户个人信息页面(固定版本)
- **user-info-edit.html**: 用户信息编辑页面
- **user-blogs.html**: 用户博客列表页面
- **user-blog-edit.html**: 用户博客编辑页面
- **user-blog-detail.html**: 用户博客详情页面
- **user-hop-list.html**: 用户店铺列表页面
- **user-hop-detail.html**: 用户店铺详情页面
- **user-index.html**: 用户首页
- **user-other-info.html**: 查看其他用户信息页面

### 商家相关页面
- **merchant-login.html**: 商家登录页面
- **merchant-register.html**: 商家注册页面
- **merchant-center.html**: 商家中心页面
- **merchant-account.html**: 商家账户管理页面
- **merchant-shop.html**: 商家店铺管理页面
- **merchant-comment.html**: 商家评论管理页面
- **merchant-order.html**: 商家订单管理页面
- **merchant-voucher.html**: 商家优惠券管理页面

### 管理员相关页面
- **admin-login.html**: 管理员登录页面
- **admin-dashboard.html**: 管理员仪表盘页面
- **admin-users.html**: 管理员用户管理页面
- **admin-merchants.html**: 管理员商家管理页面
- **admin-shop-types.html**: 管理员商铺类型管理页面
- **admin-content.html**: 管理员内容管理页面
- **admin-statistics.html**: 管理员统计页面

### 公共页面
- **index.html**: 网站首页

## 技术栈
- **前端框架**: Vue.js
- **UI组件库**: Element UI
- **HTTP客户端**: Axios
- **Web服务器**: Nginx 1.18.0 