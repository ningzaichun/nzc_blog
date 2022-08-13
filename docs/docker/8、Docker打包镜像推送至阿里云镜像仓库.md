---
highlight: a11y-dark
theme: scrolls-light
---
携手创作，共同成长！这是我参与「掘金日新计划 · 8 月更文挑战」的第9天，[点击查看活动详情](https://juejin.cn/post/7123120819437322247 "https://juejin.cn/post/7123120819437322247")

> 之前写的时候稍嫌麻烦，就一直没讲如何将镜像发布到公共镜像或私有仓库的问题

## 前言

阅读过之前的 Jenkins + Github + Docker自动化部署Maven 项目 文章的朋友应该知道

![image-20220810123758695](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/150c0dd5a560480aa53009d22e55ecb8~tplv-k3u1fbpfcp-zoom-1.image)

> 我当时在介绍 Jenkins 流程时，补充中有这样的一句话，不过那时嫌麻烦就被我省去了😙

这次就来说一说，如何打包镜像至阿里云仓库（实际上在开发过程中，通常是打包发布至 `Docker hub` 私服上，而非是公共仓库），**不过这其中的流程都大致相似**，不同之处，就是存放的地方不同罢了~

大家看完之后，也可以在之前的实践小项目中，将这一步完善进去，如果是使用阿里云的服务器的话，这样做比较好，可以用内网登录，如果不是的话，**走公网给人的感觉有那么点慢**，知道应该是这样做就好。到实际应用时，是发布到自己的 Docker hub 私服上的。

***

## 一、创建阿里云镜像仓库

直接在控制台搜索`容器镜像服务`


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3abb9658678f466cbe94443811a24f1b~tplv-k3u1fbpfcp-watermark.image?)

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3cf7500bf56347e2a3963c47954b1e66~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：点击个人实例，进入下面页面）



![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/22cb0368c44e4abaac53c7491cb504ad~tplv-k3u1fbpfcp-watermark.image?)
（图片说明：必须要先创建命名空间才能创建镜像仓库）


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/59fbb9d350eb4bd9bac953cbf577b8ff~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：就按照你自己的想法填即可）


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9a6b0562b63b468082afd4b500ff1a72~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：选择的是使用命令发布的本地仓库）


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/16b435a41df14b0482bea13be5d9c9ac~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：创建完可以看到一份完整操作指南）

## 二、打包镜像

我拿来打包的就是前面文章演示的 `Hello-SpringBoot`的项目的Jar，Dockerfile 编写的也比较简单哈

Dockerfile 文件

```
 FROM java:8
 
 WORKDIR app  #容器数据卷，用于数据保存和持久化工作 指定在创建容器后，终端默认登录的进来工作目录
 
 MAINTAINER 宁在春<nzc_wyh@163.com>
 
 COPY *.jar app.jar #我是放置在同一个目录下，所以这里的*.jar能直接搜索到我的jar包
 
 CMD ["java", "-jar", "app.jar"]
```

如果对编写 `Dockerfile`没有那么熟悉，可以看下这篇文章复习一下：[Dockerfile](https://juejin.cn/post/7025763722060627981/)

***

将它们都上传到服务器上，我是放置在同一个目录下


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/33fa7817b8574192a50ed8b57912349c~tplv-k3u1fbpfcp-watermark.image?)

上传之前，我们要先在本地打包成镜像

```
 docker build -t hellospringoot:0.0.1 -f Dockerfile .
```

-   `-t` 给镜像打上标签，也可以说是给定一个名称吧
-   `-f` 是指定 `Dockerfile`文件，这里的小数点`.` 的意思是，Dockerfile文件就在当前目录 如果在其他目录，点换成其他目录下的Dockerfile 文件即可

当然参数不止这么几个,还有`-m`限制内存 , `-c`cpu占比等等


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/af3300aea1ef4e19b9f1dddfdc9b7387~tplv-k3u1fbpfcp-watermark.image?)
（图片说明：如此便是成功）

也可用`docker images`查看一遍


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ef67373aa0994a0f816a7ee5abd403d9~tplv-k3u1fbpfcp-watermark.image?)

## 三、发布至阿里云镜像仓库

#### 3.1、登录阿里云Docker Registry

```
 docker login --username=wyh_lxb17670930115 registry.cn-heyuan.aliyuncs.com
```

如果本身用的就是阿里云的服务器，可以把登录地址改成内网地址，会快很多

```
 docker login --username=wyh_lxb17670930115 registry-vpc.cn-heyuan.aliyuncs.com
```

用于登录的用户名为阿里云账号全名，密码为开通服务时设置的密码。

您可以在访问凭证页面修改凭证密码。


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3c76b2f96b4f4498b0f1b63b69148753~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：登录成功）

#### 3.2、将镜像推送到Registry

```
 docker tag [ImageId] registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:[镜像版本号]
 
 # ImageId 就是你要打标签的镜像Id，基本上镜像id的前三位就可以确定一个镜像了
 #registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins
 #路径中的 ningzaichun 是你的命名空间
 #路径末尾的 jenkins 是仓库名
 #镜像版本号你自己定义即可
 # docker tag 命令其实就是复制一份镜像并且重命名的意思
 
 # 我的例子
 docker tag 6316 registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1
 
 docker tag 6316 registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1
 
```




![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c03febb84fa44e909419a3ea14059cc4~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：执行完是不会有显示的）

我们此时用`docker images` 命令查看一下


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c06d7116f404499b91e36cdedd715784~tplv-k3u1fbpfcp-watermark.image?)

#### 3.4、推送镜像

```
 docker push registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1
```


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5e4fe2d4304c41839f165846e0d39209~tplv-k3u1fbpfcp-watermark.image?)

推送成功后，就可以在阿里云仓库中看到了


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b0dc655a8dce46daa52b75de807266d9~tplv-k3u1fbpfcp-watermark.image?)


#### 3.5、从Registry中拉取镜像

本地此时还有原打包的镜像，先用 `docker rmi [镜像名|镜像id]` 删除后，再进行拉取

```
 docker rmi [镜像名|镜像id]
 docker pull registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:[镜像版本号]
```


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3c636818c4244b57ae4b2bf44c48c911~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：先进行删除）


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/05660fe544134d86bca01c00e411f11e~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：拉取成功）

***

如果本身使用的就是阿里云的服务器，建议登录的时候，改成内网登录，

阿里云内网地址是：`registry-vpc.cn-heyuan.aliyuncs.com`，传播速度还是有增强的


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d23bb3d7c9e84a66a1991b006c0516e9~tplv-k3u1fbpfcp-watermark.image?)

## 后记

> 写完这篇文章的第一感觉就是我想去搭建一个 Docker hub的镜像仓库了，阿里云仓库的公网传输稍微有点点慢，第一次测试的时候，走的是公网，只要网速稍微拉一点，传输速度就有一点点感人

原本想在写下一篇文章的时候，把流程走完善，但是看到这个速度，快把我**劝退**了。

上一次只花了一张 Jenkins 的简图，这一次看还有时间，画了一张较为完善的图，也给下篇文章一个偷懒的机会~


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4581195b94a344bc9766412f107556d8~tplv-k3u1fbpfcp-watermark.image?)

专栏的下一篇已经在构思了，有一些些准备了，希望每位朋友看完都能有所收获，那应该是我最大的快乐了吧。

也多亏了各位朋友的阅读，让我坚定的想要把本专栏好好的写下去。

非常感谢！！

>于 2022 年 8 月 10 日 作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)
