> 起因：公司让我在网上找一个 ubuntu 上的 Redis 的管理工具，找了一圈，怎么说合适勒，就是搜索到的文章基本上长一个样😂，内容都是偏向于介绍，然后我就想着来写一篇评测相关的文章，来让大家更好的做出选择。

虽然它们都是可视化工具，但是有些侧重点还是稍显不同的。

如果 Windows 上还无 Redis，可以看看国外开发出来的 [Redis5.0版本](https://github.com/tporadowski/redis)

## 一、介绍

### 1、参与评测的工具

本次评测的`免费工具`有如下：

1.  [RedisPlus](https://gitee.com/MaxBill/RedisPlus)
1.  [Another-Redis-Desktop-Manager](https://gitee.com/qishibo/AnotherRedisDesktopManager)
1.  [QuickRedis](https://gitee.com/quick123official/quick_redis_blog)
1.  [Redis Desktop Manager 0.9.3 此版本为免费版本](https://github.com/uglide/RedisDesktopManager/releases/tag/0.9.3)
1.  [Idea 中的 Redis Simple 插件](https://plugins.jetbrains.com/plugin/13099-redis-simple)
1.  [Redis Assistant](http://www.redisant.cn/)
1.  [Redis Insight](https://docs.redis.com/latest/ri/installing/install-redis-desktop/)

其实除了以外，还有不少可视化的管理工具，如 FastoNoSQL（收费），另外 Mac 上的 Red、[Medis](https://getmedis.com/)（免费）工具。

但因为我没有 Mac 电脑，也就没有这方面的评测，非常不好意思。

### 2、评测的方向

-   安装方便程度
-   学习成本高低
-   命令补齐
-   Redis 新特性、Redis 各数据对象存取
-   UI 界面
-   JSON 格式查看序列化对象
-   状态监控、内存分析
-   主从模式、集群模式管理、哨兵模式
-   批量删除、新增
-   搜索是否方便

## 二、测评开始

### 2.1、[Redis Plus](https://gitee.com/MaxBill/RedisPlus)

简单介绍下，Redis Plus 是 Gitee 上我们国人开发的一款开源软件，基于javafx11 开发，支持 windows 和 liunx 系统，免费，不过目前开发者已经停止维护和版本更新。暂无继续迭代的公告。目前最新的更新是 2019 年，是可用的状态，但是如果是用于生产需求的话，是不推荐使用的。

主界面UI

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e7f0b5a62057483d8341aafb6256537f~tplv-k3u1fbpfcp-zoom-1.image)

连接页面

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/625a9e12323d40ca846269770785bdf9~tplv-k3u1fbpfcp-zoom-1.image)

这里有支持 SSH 连接，但是这点我没有去评测了。勿怪。集群模式的评测在后面有。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/898ae80208604c1b9c3f3efa9e5f8baa~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/61b83adbfad544aea444d298eef58efc~tplv-k3u1fbpfcp-zoom-1.image)

支持三种格式的格式数据查看分别为：TEXT、JSON、RAWS

另外可以直接在这里修改数据，

我将Redis中现有的数据类型进行了测试，其中 普通的 key-value、List、HashMap、Set和Zset，另外 对于 geo 数据也还好。但是对于 redis 5.0 以后出现的 Stream 是不支持的。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/26020cd3e7ed4136a685f29aa53da37b~tplv-k3u1fbpfcp-zoom-1.image)

命令输入窗口、内存监控和配置文件修改稍显粗糙，简单使用当然还是没有问题的。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/05cce309e8614fc1b8c0c493b6d4eae8~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ad8002231b584aeeb9d7edce8ddc1065~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b500910142d14a76a4c2ce37dda71fc2~tplv-k3u1fbpfcp-zoom-1.image)

**总结**

优点：

1.  免费，开源软件，有能力可以基于此继续扩展
1.  安装方便，全中文，使用方便
1.  大都数情况下是足够应用的

缺点：

1.  界面 UI 一般，应用中偶尔存在反应慢的情况
1.  内存分析较为粗糙，没有慢日志查询等
1.  命令模式下，没有代码提示和补齐
1.  不支持 Stream

如果只是使用可视化软件查看 Redis 数据库情况，那么还是什么问题的。

### 2.2、[Another-Redis-Desktop-Manager](https://gitee.com/qishibo/AnotherRedisDesktopManager)

简要：开源软件，免费，国人开发，支持 windows、liunx、Mac 三端

感兴趣的可以去点个 star

界面UI 支持 明亮、暗黑两种主题，同时支持多种语言

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/af19424831c54a4eb97b9fae7d21168f~tplv-k3u1fbpfcp-zoom-1.image)

支持多种连接模式

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bb1592f2800643b5a9ba7cded49d11b1~tplv-k3u1fbpfcp-zoom-1.image)

在 Redis Plus 中 不支持的 Stream ，在此也是同样支持

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a0b94604098c47e0ba60104abbace71c~tplv-k3u1fbpfcp-zoom-1.image)

另外也可以点击👀 查看，得到更好查看体验

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7c160553621b414396923706de1c77e8~tplv-k3u1fbpfcp-zoom-1.image)

在这里也可以看到，它支持更多的数据格式的查看，这一点做的非常完善。

内存监控、配置文件查看和设置自动刷新

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/29887ad7da9945f89a685fc6843fa681~tplv-k3u1fbpfcp-zoom-1.image)

不过点开这个，还发现了一个 内存分析

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/eb262337bd584a33b5c7679748609a6c~tplv-k3u1fbpfcp-zoom-1.image)

命令模式有提示

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/57cd6e02087648ebb0dcd2e8888ec444~tplv-k3u1fbpfcp-zoom-1.image)

**总结**

优点：

1.  界面 UI 好看
1.  使用流畅，非常舒服
1.  数据格式支持多种
1.  命令模式中有提示
1.  支持多种语言和字体
1.  支持多种模式连接
1.  支持Redis新特性
1.  支持自动刷新

缺点：

-   无法直接动态修改配置文件
-   没有慢日志分析
-   监控模块较弱

结论：这是一款非常值得推荐的软件，无论是从界面UI，还是使用体验来说，它都让用户非常舒服，并且从它的一些局部可以看出开发者的用心及野心。希望可以看到它走的更远

### 2.3、[QuickRedis](https://gitee.com/quick123official/quick_redis_blog)

简要：免费、开源软件、国人开发、支持 Windows & Mac OS X & Linux 、支持多模式连接

界面UI & 连接

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7d11dad33ac742b19f4b2ed341b3d254~tplv-k3u1fbpfcp-zoom-1.image)

命令行模式，按 tab 键可以展示命令的帮助文档，但是没有命令提示

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3df52d11b4b948c4b2bb81be46d60c42~tplv-k3u1fbpfcp-zoom-1.image)

查看

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d88e50e3078a467a98dfed4577492505~tplv-k3u1fbpfcp-zoom-1.image)

支持json 数据格式查看、表格样式查看，支持 文件夹式管理连接。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6bbc89ff0b90426ab871fdbf93fcbeea~tplv-k3u1fbpfcp-zoom-1.image)

不支持 Stream 数据的查看

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8d31644f95fd4e9d992c5100c368382e~tplv-k3u1fbpfcp-zoom-1.image)

没有那些其他的内存分析、监控日志等杂七杂八的东西。一个词形容 `简约`

**总结：**

优点：

1.  简约风，个人平时完全够用
1.  界面UI 挺好
1.  使用流畅且方便

缺点：

-   页面布局稍感不适
-   缺少内存使用情况等等
-   命令模式下没有代码提示
-   不能查看 Stream 数据

结论：这款软件更适合于开发者个人使用，简约，有时候我们并不太需要去那么关注内存使用情况，专注于应用层开发就好，少一些监控的页面，也并非不可。如果是运维人员的话，这款可能就略显简单了。

### 2.4、[Redis Desktop Manager 0.9.3](https://github.com/uglide/RedisDesktopManager/releases/tag/0.9.3)

简要：现已收费、迭代时间长、redis 可视化管理工具中的老大哥

我用的是我以前下载的 2020 的版本，比我在文章内放的链接还要高几个版本，但是目前不付费是没法连接到云服务器啦。这点非常让人失望的哈。

因为我windows上的Redis 版本是 3.0 就没法测 Stream 这种 5.0+的特性，这步就省了，另外就是它是收费的，我测的可能不是那么认真哈。

界面

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/095f119851a7480a9ed149d11334bf9a~tplv-k3u1fbpfcp-zoom-1.image)

不支持 JSON 格式数据以树形方式查看

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/95e076f497684ca2be001c2fe9528296~tplv-k3u1fbpfcp-zoom-1.image)

有日志记录

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/95a4860a00d54f1bb4406af1d1f4674f~tplv-k3u1fbpfcp-zoom-1.image)

**总结**

优点：

1.  迭代多，有人稳定维护
1.  老牌工具

缺点：

-   界面有点没跟上时代
-   付费软件
-   平平无奇

结论：不推荐使用啦，大款另说。

### 2.5、[Idea 中的 Redis Simple 插件](https://plugins.jetbrains.com/plugin/13099-redis-simple)

简要：Idea 中的插件，免费（其余的都收费），内嵌于IDE，较为方便

下好插件后点击 setting—>Other Settings，然后配置一个连接即可。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a53a0000cac6472fb96d64606b30bedc~tplv-k3u1fbpfcp-zoom-1.image)

之后在侧边栏会出现一个 NoSql 的按钮，点开就会看到下面的页面

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/330f41fd377f4dc981a0aa5da8e18027~tplv-k3u1fbpfcp-zoom-1.image)

打开后，在数据上直接双击就能够打开修改，非常方便

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/83adf43188d147cc983b880f8a00b1e1~tplv-k3u1fbpfcp-zoom-1.image)

除了查看和修改功能，其他的话，就暂时没有发现了。

**总结**

1.  内嵌于idea中，使用非常方便、轻巧
1.  功能只有查看和修改，较为简单
1.  适合于开发者使用

结论：挺值得推荐的，因为个人在开发的时候，其实并不需要时时刻刻注意redis 的情况，查看的时候，多半是debug的时候了。

### 2.6、[ Redis Assistant](http://www.redisant.cn/)

简要：收费软件（价格还好）、有试用期 3-7天、国人开发

界面UI

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2f233afba8a94d5bbb6d78e01d3bed58~tplv-k3u1fbpfcp-zoom-1.image)

使用界面

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bb54a2b095b649de81187a9d5ba1ea9e~tplv-k3u1fbpfcp-zoom-1.image)

支持 lua 脚本

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ad1157925f1a48c3b997b8c8a182aa98~tplv-k3u1fbpfcp-zoom-1.image)

命令行模式，没有代码提示或补齐

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/62f1559891c74c259a92da9557ee5744~tplv-k3u1fbpfcp-zoom-1.image)

编辑非常方便，点击完可以直接在展示的那边进行修改

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3f842c5c6f23452a81699546f4d674a9~tplv-k3u1fbpfcp-zoom-1.image)

不过也是不支持 Stream 类型数据的查看

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a632a7a0a1214bb6af3767d7ec0605ad~tplv-k3u1fbpfcp-zoom-1.image)

**总结**

优点：

1.  界面美观，使用流畅
1.  每条数据都直接用不同颜色标明了数据类型，这个点我个人非常喜欢
1.  支持 lua 脚本的执行
1.  支持多种模式连接
1.  支持多样化搜索（官网描述）
1.  虽然是付费软件，但是价格还是比较美丽（永久49rmb）

缺点：

-   命令行模式无提示
-   不支持 Stream 数据类型

结论：使用体验挺好的，挺值得推荐的，界面 UI 让人也非常舒适，喜欢的小伙伴，可以支持一下，开源不易。

### 2.7、[ Redis Insight](https://docs.redis.com/latest/ri/installing/install-redis-desktop/)

简要：背景较为正统，是一家 Redis 云服务商开发出来的可视化管理工具，支持多系统安装，也有web版，支持 docker 部署、k8s 部署等，同时也是免费软件

连接界面UI & 主界面UI & 有深色和两种主题，可以手动设置

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/33187cfde74940e9bac306c465405ec8~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f3e2786890dd4cd3924fa4cab03fd86d~tplv-k3u1fbpfcp-zoom-1.image)

支持 Stream 数据类型的查看，同时也能查看 Redis 服务的基本情况，

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e482bb9a94b141239bbbd52e447043c4~tplv-k3u1fbpfcp-zoom-1.image)

支持消息订阅的查看

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a37d854b71a5454f8a526b778dab6f8b~tplv-k3u1fbpfcp-zoom-1.image)

可以在页面上查看 慢日志 ，相关配置就得靠自己了。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7f9fa12222ee4d77929de60b7fafe47f~tplv-k3u1fbpfcp-zoom-1.image)

命令模式，有两处可以打开，并且都有代码提示，

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d3d065aa7df64cc399e56d573b549faf~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fd91dee9c972433fb8d1e14acfbfaf9e~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0756dbef222245eeb6fd0e6aa8fe0731~tplv-k3u1fbpfcp-zoom-1.image)

补充：redis lnsight 也是可以用 docker 部署，用在线的web版来对redis 进行查看和管理的。

[docker 安装 redis Insight](https://docs.redis.com/latest/ri/installing/install-docker/)

**总结**

优点：

1.  正统背景，有专业的维护团队
1.  使用流程，页面美观
1.  有慢日志查看
1.  命令有提示，并附有帮助文档

缺点：

-   无法手动切换 database，只能在连接的时候选择好
-   目前看来，只有英语一种语言，可能对恐英的人有点不友善

结论：这款软件各方面都比较好，也有自己的特色，同时开发商也算是 Redis 的正统背景，有专业的维护团队，较为稳定，我觉得是可以放心食用的。

## 三、测评总结

本文均为我的个人真实感受和讲述，并不代表其他开发者，同时也不代表上述软件的任一作者。

按照我今天的使用体验来说，最让我喜欢的是 `Another-Redis-Desktop-Manager`，它各方面都做的非常好了，就我说的那几点问题，你说说有没有一种可能已经是在作者的计划中啦勒。😄

追求稳定的话，我比较推荐 `Redis Insight`，并竟能算的上是官方出品的啦，有专业的开发维护团队，客户群体也大，我觉得是可以的。

如果只是想在开发的时候查看 Redis 的key值情况，也没有什么特殊的需求，我认为在 idea 中下载一下插件使用也挺好的。

## 四、一些想说的话

如果可以的话，希望大家能够给上述的各个开源软件点个 `star`，希望开源软件能做的越来越好，也希望开源作者能够发展的越来越好，也不枉我在文末特意说这样的一番话吧。

***

记：心血来潮写了这样的一篇评测文章，一方面是当时在寻找的时候，没有找到一篇靠谱的博客，另外一方面也是就是想试写一下这样的文章。

补充：文章所评测的软件，均为我真实下载使用。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4c30789d374f404684045bfa686c39a4~tplv-k3u1fbpfcp-zoom-1.image)

> 非常感谢大家的阅读，也希望大家通过这篇文章找到自己想要的软件。


有评论区的小伙伴说“ another 用了大半年了，key一多，他的树状结构就会丟 key 筛选下又出来了，就很坑，一直没修复 ”，大家使用的时候，多试两个，我测试的时候没有长期使用，非常不好意思。

我正在参与掘金技术社区创作者签约计划招募活动，[点击链接报名投稿](https://juejin.cn/post/7112770927082864653 "https://juejin.cn/post/7112770927082864653")。
