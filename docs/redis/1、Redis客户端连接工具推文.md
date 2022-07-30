

# 关于市面目前流行的 Redis 可视化管理工具的详细评测

起因：公司让我在网上找一个 ubuntu 上的 Redis 的管理工具，找了一圈，怎么说合适勒，就是搜索到的文章基本上长一个样😂，内容都是偏向于介绍，然后我就想着来写一篇评测相关的文章，来让大家更好的做出选择。

虽然它们都是可视化工具，但是有些侧重点还是稍显不同的。

如果 Windows 上还无 Redis，可以看看国外开发出来的 [Redis5.0版本](https://github.com/tporadowski/redis)

## 一、介绍

### 1、参与评测的工具

本次评测的`免费工具`有如下：

1. **[RedisPlus](https://gitee.com/MaxBill/RedisPlus)**
2. [Another-Redis-Desktop-Manager](https://gitee.com/qishibo/AnotherRedisDesktopManager)
3. [QuickRedis](https://gitee.com/quick123official/quick_redis_blog)
4. [Redis Desktop Manager 0.9.3 此版本为免费版本](https://github.com/uglide/RedisDesktopManager/releases/tag/0.9.3) 
5. [Idea 中的 Redis Simple 插件](https://plugins.jetbrains.com/plugin/13099-redis-simple)
6. [ Redis Assistant](http://www.redisant.cn/)
7. [Redis Insight](https://docs.redis.com/latest/ri/installing/install-redis-desktop/)

其实除了以外，还有不少可视化的管理工具，如 FastoNoSQL（收费），另外 Mac 上的 Red、[Medis](https://getmedis.com/)（免费）工具。

但因为我没有 Mac 电脑，也就没有这方面的评测，非常不好意思。

### 2、评测的方向

- 安装方便程度
- 学习成本高低
- 命令补齐
- Redis 新特性、Redis 各数据对象存取
- UI 界面
- JSON 格式查看序列化对象
- 状态监控、内存分析
- 主从模式、集群模式管理、哨兵模式
- 批量删除、新增
- 搜索是否方便

## 二、测评开始

### 2.1、[Redis Plus](https://gitee.com/MaxBill/RedisPlus)

简单介绍下，Redis Plus 是 Gitee 上我们国人开发的一款开源软件，基于javafx11 开发，支持 windows 和 liunx 系统，免费，不过目前开发者已经停止维护和版本更新。暂无继续迭代的公告。目前最新的更新是 2019 年，是可用的状态，但是如果是用于生产需求的话，是不推荐使用的。

主界面UI 

![image-20220716110906821](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346717.png)

连接页面

![image-20220716120936631](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346783.png)

这里有支持 SSH 连接，但是这点我没有去评测了。勿怪。集群模式的评测在后面有。

![image-20220716145010457](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346124.png)

![image-20220716145503921](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346713.png)

支持三种格式的格式数据查看分别为：TEXT、JSON、RAWS

另外可以直接在这里修改数据，

我将Redis中现有的数据类型进行了测试，其中 普通的 key-value、List、HashMap、Set和Zset，另外 对于 geo 数据也还好。但是对于 redis 5.0 以后出现的 Stream 是不支持的。

![image-20220716151334028](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346285.png)

命令输入窗口、内存监控和配置文件修改稍显粗糙，简单使用当然还是没有问题的。

![image-20220716164116576](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346869.png)

![image-20220716162055603](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207170025588.png)

![image-20220716162118600](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346685.png)

**总结**

优点：

1. 免费，开源软件，有能力可以基于此继续扩展
2. 安装方便，全中文，使用方便
3. 大都数情况下是足够应用的

缺点：

1. 界面 UI 一般，应用中偶尔存在反应慢的情况
2. 内存分析较为粗糙，没有慢日志查询等
3. 命令模式下，没有代码提示和补齐
4. 不支持 Stream

如果只是使用可视化软件查看 Redis 数据库情况，那么还是什么问题的。

### 2.2、[Another-Redis-Desktop-Manager](https://gitee.com/qishibo/AnotherRedisDesktopManager)

简要：开源软件，免费，国人开发，支持 windows、liunx、Mac 三端

感兴趣的可以去点个 star 

界面UI 支持 明亮、暗黑两种主题，同时支持多种语言

![image-20220716163512300](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346181.png)

支持多种连接模式

![image-20220716163918062](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346583.png)

在 Redis Plus 中 不支持的 Stream ，在此也是同样支持

![image-20220716163638251](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346908.png)

另外也可以点击👀 查看，得到更好查看体验

![image-20220716163733018](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346197.png)

在这里也可以看到，它支持更多的数据格式的查看，这一点做的非常完善。

内存监控、配置文件查看和设置自动刷新

![image-20220716164213641](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346828.png)

不过点开这个，还发现了一个 内存分析

![image-20220716164353288](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346156.png)

命令模式有提示

![image-20220716164427213](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346885.png)



**总结**

优点：

1. 界面 UI 好看
2. 使用流畅，非常舒服
3. 数据格式支持多种
4. 命令模式中有提示
5. 支持多种语言和字体
6. 支持多种模式连接
7. 支持Redis新特性
8. 支持自动刷新

缺点：

- 无法直接动态修改配置文件
- 没有慢日志分析
- 监控模块较弱

结论：这是一款非常值得推荐的软件，无论是从界面UI，还是使用体验来说，它都让用户非常舒服，并且从它的一些局部可以看出开发者的用心及野心。希望可以看到它走的更远

### 2.3、[QuickRedis](https://gitee.com/quick123official/quick_redis_blog)

简要：免费、开源软件、国人开发、支持 Windows & Mac OS X & Linux 、支持多模式连接

界面UI  & 连接

![image-20220716165340385](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346558.png)

命令行模式，按 tab 键可以展示命令的帮助文档，但是没有命令提示

![image-20220716170754350](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346220.png)

查看

![image-20220716170244176](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220716170244176.png)

支持json 数据格式查看、表格样式查看，支持 文件夹式管理连接。

![image-20220716170536843](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162346700.png)

不支持 Stream 数据的查看

![image-20220716170254534](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347093.png)

没有那些其他的内存分析、监控日志等杂七杂八的东西。一个词形容 `简约`

**总结：**

优点：

1. 简约风，个人平时完全够用
2. 界面UI 挺好
3. 使用流畅且方便

缺点：

- 页面布局稍感不适
- 缺少内存使用情况等等
- 命令模式下没有代码提示
- 不能查看 Stream 数据

结论：这款软件更适合于开发者个人使用，简约，有时候我们并不太需要去那么关注内存使用情况，专注于应用层开发就好，少一些监控的页面，也并非不可。如果是运维人员的话，这款可能就略显简单了。

### 2.4、[Redis Desktop Manager 0.9.3 ](https://github.com/uglide/RedisDesktopManager/releases/tag/0.9.3) 

简要：现已收费、迭代时间长、redis 可视化管理工具中的老大哥

我用的是我以前下载的 2020 的版本，比我在文章内放的链接还要高几个版本，但是目前不付费是没法连接到云服务器啦。这点非常让人失望的哈。

因为我windows上的Redis 版本是 3.0 就没法测 Stream 这种 5.0+的特性，这步就省了，另外就是它是收费的，我测的可能不是那么认真哈。

界面

![image-20220716172339648](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347276.png)

不支持 JSON 格式数据以树形方式查看

![image-20220716172438627](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347852.png)

有日志记录

![image-20220716172742376](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347036.png)



**总结**

优点：

1. 迭代多，有人稳定维护
2. 老牌工具

缺点：

- 界面有点没跟上时代
- 付费软件
- 平平无奇

结论：不推荐使用啦，大款另说。

### 2.5、[Idea 中的 Redis Simple 插件](https://plugins.jetbrains.com/plugin/13099-redis-simple)

简要：Idea 中的插件，免费（其余的都收费），内嵌于IDE，较为方便

下好插件后点击 setting—>Other Settings，然后配置一个连接即可。

![image-20220716173451669](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347939.png)



之后在侧边栏会出现一个 NoSql 的按钮，点开就会看到下面的页面

![image-20220716173641842](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347693.png)

打开后，在数据上直接双击就能够打开修改，非常方便

![image-20220716173854101](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347379.png)

除了查看和修改功能，其他的话，就暂时没有发现了。

**总结**

1. 内嵌于idea中，使用非常方便、轻巧
2. 功能只有查看和修改，较为简单
3. 适合于开发者使用

结论：挺值得推荐的，因为个人在开发的时候，其实并不需要时时刻刻注意redis 的情况，查看的时候，多半是debug的时候了。

### 2.6、[ Redis Assistant](http://www.redisant.cn/)

简要：收费软件（价格还好）、有试用期 3-7天、国人开发

界面UI

![image-20220716175115368](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347140.png)

使用界面
![image-20220716175355042](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347324.png)

支持 lua 脚本

![image-20220716175435688](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347284.png)

命令行模式，没有代码提示或补齐

![image-20220716175522162](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347516.png)

编辑非常方便，点击完可以直接在展示的那边进行修改

![image-20220717000837165](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220717000837165.png)

不过也是不支持 Stream 类型数据的查看

![image-20220716175707474](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347883.png)

**总结**

优点：

1. 界面美观，使用流畅
2. 每条数据都直接用不同颜色标明了数据类型，这个点我个人非常喜欢
3. 支持 lua 脚本的执行
4. 支持多种模式连接
5. 支持多样化搜索（官网描述）
6. 虽然是付费软件，但是价格还是比较美丽（永久49rmb）

缺点：

- 命令行模式无提示
- 不支持 Stream 数据类型

结论：使用体验挺好的，挺值得推荐的，界面 UI 让人也非常舒适，喜欢的小伙伴，可以支持一下，开源不易。

### 2.7、[ Redis Insight](https://docs.redis.com/latest/ri/installing/install-redis-desktop/)

简要：背景较为正统，是一家 Redis 云服务商开发出来的可视化管理工具，支持多系统安装，也有web版，支持 docker 部署、k8s 部署等，同时也是免费软件



连接界面UI & 主界面UI & 有深色和两种主题，可以手动设置

![image-20220716180925067](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347636.png)

![image-20220716230411462](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347253.png)

支持 Stream 数据类型的查看，同时也能查看 Redis 服务的基本情况，

![image-20220716230829411](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220716230829411.png)

支持消息订阅的查看

![image-20220716231013575](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347868.png)

可以在页面上查看 慢日志 ，相关配置就得靠自己了。

![image-20220716231058270](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347020.png)



命令模式，有两处可以打开，并且都有代码提示，

![image-20220716231154474](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347059.png)

![image-20220716231420634](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347702.png)

![image-20220716231533162](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162347724.png)

补充：redis lnsight 也是可以用 docker 部署，用在线的web版来对redis 进行查看和管理的。

[docker 安装 redis Insight](https://docs.redis.com/latest/ri/installing/install-docker/)

**总结**

优点：

1. 正统背景，有专业的维护团队
2. 使用流程，页面美观
3. 有慢日志查看
4. 命令有提示，并附有帮助文档

缺点：

- 无法手动切换 database，只能在连接的时候选择好
- 目前看来，只有英语一种语言，可能对恐英的人有点不友善

结论：这款软件各方面都比较好，也有自己的特色，同时开发商也算是 Redis 的正统背景，有专业的维护团队，较为稳定，我觉得是可以放心食用的。



## 三、测评总结

本文均为我的个人真实感受和讲述，并不代表其他开发者，同时也不代表上述软件的任一作者。

按照我今天的使用体验来说，最让我喜欢的是 `Another-Redis-Desktop-Manager`，它各方面都做的非常好了，就我说的那几点问题，你说说有没有一种可能已经是在作者的计划中啦勒。😄

追求稳定的话，我比较推荐 `Redis Insight`，并竟能算的上是官方出品的啦，有专业的开发维护团队，客户群体也大，我觉得是可以的。

如果只是想在开发的时候查看 Redis 的key值情况，也没有什么特殊的需求，我认为在 idea 中下载一下插件使用也挺好的。

## 四、一些想说的话

如果可以的话，希望大家能够给上述的各个开源软件点个 `star`，希望开源软件能做的越来越好，也希望开源作者能够发展的越来越好，也不枉我在文末特意说这样的一番话吧。

---

记：心血来潮写了这样的一篇评测文章，一方面是当时在寻找的时候，没有找到一篇靠谱的博客，另外一方面也是我想要尝试这样的文章。

补充：文章所评测的软件，均为我真实下载使用。

![image-20220716235136793](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207162351955.png)













