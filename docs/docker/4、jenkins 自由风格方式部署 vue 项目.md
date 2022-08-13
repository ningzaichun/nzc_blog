**祝各位七夕快乐，如果有机会，就不要再错过她或是他了。**

> 算起来，这应该是建立这个专栏来的第三篇文章啦，可喜可贺，虽然菜，但还在坚持写。
>
> 上一篇文章其实已经介绍过如何[利用 Jenkins + Github + Docker 部署一个 Maven 项目](https://juejin.cn/post/7127302949797101604)，同时也包含了如何使用Docker 安装 Jenkins ，以及一些基本概念 📌文章链接
>
> 有了后端，那么必然也少不了前端，所以就诞生了本文。

## 前言

**看起来好像 Jenkins 非常复杂，但其实只要自己多实操几次，一次又一次的去想如何偷懒，你就可以一步一步发现更多的知识点，要相信好奇永远是你的第一老师。**

关于如何使用 Docker 安装 Jenkins，Jenkins 插件安装配置、系统配置等，都已在 [关于 Docker + Jenkins +Github 实现自动化](https://juejin.cn/post/7127302949797101604) 中全部陈述。

先说说本文最后做出来的效果：

0.  本地开发，push 到 github 仓库后
0.  触发 Github 的钩子函数，通知 Jenkins ，进行重新构建
0.  Jenkins 构建完成后，将前端打包出来的 dist 目录，发送到部署的服务器上的 Nginx 容器挂载的部署目录下
0.  进行访问测试

除了第一步是需要自己动手外，其余部分实现自动化。

前一篇文章主要介绍了 Jenkins 如何构建一个 Maven 项目，但其实大家可以看到 Jenkisn 还有其他几钟不同的构建项目的方式。

![image-20220725231423445](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b6d25d03eb514d4f8d087c85e866e88c~tplv-k3u1fbpfcp-zoom-1.image)

本篇文章用到的是自由风格式（Freestyle project）部署前端项目。也很简单的哈。

## 一、初始化项目

如果需要跟着文章实操，大致需要以下环境：

-   一台云服务器或本地虚拟机
-   服务器或虚拟机上需要联网、Docker 环境
-   一个 Github 账号
-   开发机器上需要有 Git 和 开发环境

### 1、初始化 Vue 项目

其实我也不知道这一步该不该写......

重点就是大家准备一个可以运行和打包的 Vue 项目。

如果有小伙伴，没的话，我有~，给你指路： **[jenkins-vue-demo](https://github.com/ningzaichun/jenkins-vue-demo)**

拉下来之后，把 .git 文件删除掉，然后重新关联你的github 仓库就好~

### 2、推送至 Github 仓库

-   在 github 建立一个仓库 （默认大家都会哈~ 不会可以留言的，摸鱼的时候会回复的，别慌）
-   然后在本地项目目录下执行下面的命令，其实不写，在你创建仓库的时候也会给出这些提示命令

```
  git init 
  git add .
  git commit -m "init"
  git branch -M main
  git remote add  远程仓库地址
  git push origin main
```

## 二、设置 Github

### 1、设置通知 WebHook

在github 上点击仓库，按下图顺序

![image-20220725234238980](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4023eefe01774adb8259e6a8b9245cdc~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220725234704506](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fec810b1f97646dab09ea6bd4f297d74~tplv-k3u1fbpfcp-zoom-1.image)

之后点击创建即可

### 2、创建一个 Personal access tokens

![image-20220725235047549](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3e802995c3914839b2b1be45476b8da9~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220725235149381](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/616aabcc4a7d425890d03a7430d93413~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220725235309522](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ff6a5097ac274680a3c8c44d3f490f32~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220725235431597](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9b50cb2a280b43d1afb75ee63b48309e~tplv-k3u1fbpfcp-zoom-1.image)

## 三 、Jenkins 部署 Vue 项目

### 1、安装 Nodejs 插件

![image-20220725235601294](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/626c05a7ef7a43298c474861e706924c~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220725235637764](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/503e852c382f4c01a8e4de395d0cef54~tplv-k3u1fbpfcp-zoom-1.image)

等待完成即可

![image-20220725235704877](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/384917c7ef6c4f6abccc28cc255de33d~tplv-k3u1fbpfcp-zoom-1.image)

### 2、配置 Nodejs

![image-20220725235746956](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/553c4cddd5cb49ccbf9d7807bca8a63f~tplv-k3u1fbpfcp-zoom-1.image)

本地机器查看 node 版本 命令为 `node -v`

![image-20220726000026696](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/21e4a2a9d0974388a46fad89b9694e8e~tplv-k3u1fbpfcp-zoom-1.image)

### 3、系统配置

之前我们在第四小节，只是在Jenkins中进行了打包，并未发布在服务器上。

如果要发布在服务器上，我们还需要配置一下 远程服务器信息。

此处还需要下载两个插件

-   [SSH](https://plugins.jenkins.io/ssh) ： SSH 连接工具
-   [Publish Over SSH ](https://plugins.jenkins.io/publish-over-ssh)：SSH 发布工具

稍详细的描述在我上一篇文章：[Docker + Jenkins + GitHub 自动化部署 Maven 项目](https://juejin.cn/post/7127302949797101604)

![image-20220723123647297](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ab309c0497624ce4a2323a68fd11af96~tplv-k3u1fbpfcp-zoom-1.image)

找到两个配置：

1、SSH remote hosts

2、SSH Servers

![image-20220723124508598](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d01d609e80594c218925f0608678251a~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220723125259942](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8c21102af108484ea4abd419c9077df5~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220723124931631](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fc278bc825f545d6848be0765601f436~tplv-k3u1fbpfcp-zoom-1.image)

对了记得点击保存哈，不然又得重现填写。

### 4、创建一个自由风格式任务

![image-20220726001018326](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/911e4722a3c541289bc24a27236dfb08~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726001057300](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/32af1df8b67a4011a9738b66b091eecb~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726001420957](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6b77358e33294da6886e066c8f609938~tplv-k3u1fbpfcp-zoom-1.image)

(图片说明：指定分支应为 `main`，图中有误)

![image-20220726001637797](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a78e82eaf7854d5e925a3900efa0cf7d~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726001753543](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e1b3f494cdf74b04b9066893566774df~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726002005243](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2afe1693e7c846b7b983802872fc1256~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726002041791](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6fd853b668b6453a9339625d2967534c~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726002056727](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/19f8e8dfbae24b418ee6e460533920ec~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：变量无需填写）

![image-20220726002136595](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b4c4bb24a5694d41994a178381fd01ac~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：选择 secret text）

![image-20220726002227276](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b4ac11a422924dce9d21fe9ac7689791~tplv-k3u1fbpfcp-zoom-1.image)

(图片说明：描述就是取一个名称)

![image-20220726002323326](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/04a3406739d04b3fbea3a04f1ae5cbda~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：选择自己添加的那个 凭据）

![image-20220726002442954](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/797c5e8660474b7183a55662e60516e2~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726002542464](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7ee1440376db414999b326f5481886b3~tplv-k3u1fbpfcp-zoom-1.image)

```
  npm cache clear --force # 清理 npm 缓存，之前我一直报错，第一次之后大家可以修改修改 ~
  npm --registry https://registry.npm.taobao.org install cluster # 配置淘宝镜像
  npm install --force 
  npm run build
  echo "打包完成"
```

***

执行到这一步时，我们已经可以测试我们当前的这个自由风格的任务了。

点击立即构建，看看git有没有成功拉取，有没有打包成功。

第一次构建的时间也会稍长，需要拉取项目，下载Nodejs，下载依赖等，这些信息都会在控制台上可查看：

![image-20220726003805414](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/424736d064f544f098d8daedd2191eb2~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726003814903](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d111e6ff86c14af5ad013be45d749972~tplv-k3u1fbpfcp-zoom-1.image)

成功的输出应该如下：

![image-20220801232543788](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/addb09954a1e49caa0d2d027de4d80af~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220726003845074](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e40b7f4e850b4594a10d5522d5a3b5e9~tplv-k3u1fbpfcp-zoom-1.image)

### 5、浅提一下Nginx

谈到部署前端项目时，大部分情况下我们不可避免的会谈到Nginx服务器。

Nginx 这个中间件，不管是对于后端还是前端，都是需要了解的一个服务器。

想要深入的了解它，可能还需要你好好的花费一些时间。

关于Docker 安装 Nginx 部署 前端项目 ，我之前已经写好，链接：[Docker 安装 Nginx](https://juejin.cn/post/7126146371198910478)

***

后面的小节，都是默认大家已经安装好了Nginx~

我的Nginx 的 server配置如下：

```
  location /hello {
      alias    /usr/share/nginx/html/www/hello/dist;
      try_files $uri $uri/ /hello/index.html;
      index  index.html index.htm;
  }
```

我们部署成功访问的路径是：IP : Port/hello/ ,例如：192.168.1.100/hello/ 就是访问此项目的的地址。

详细的还是得大家去了解一下。

### 6、修改Jenkins 任务配置

打开任务配置，直接划到最下面

![image-20220802002542798](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6bb7aa1aa4964f1ca809808ef98a768f~tplv-k3u1fbpfcp-zoom-1.image)

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/43254807659a48e8a004bc41a1257114~tplv-k3u1fbpfcp-zoom-1.image)

然后选中之前配置的服务器

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5659756fabb54c1383a18aed32dbb4f8~tplv-k3u1fbpfcp-zoom-1.image)

```
 #/bin/bash
 # 其实在这里执行的命令，就是在你选择的那台服务器上执行的命令
  echo ">>>>>>>>>>>>>开始执行   此处的 /home/nginx/html/web 是我 nginx 容器 挂载的目录 >>>>>>>>>>>>>"
  cd /home/nginx/html/www/hello/dist/
  rm -rf  dist
  
  echo ">>>>>>>>>>>>>cd到Jenkins工作挂载目录下>>>>>>>>>>>>>"
  cd /home/jenkins/workspace/jenkins-vue-demo
  
  echo ">>>>>>>>>>>> 将dist文件夹复制到 nginx 的挂载目录下 >>>>>>>>>>>>>"
  
  cp -r dist /home/nginx/html/www/hello/
  
  echo ">>>>>>>>>>>>复制成功  启动成功>>>>>>>>>>>>>"
```

`注意`：此处我是直接采取将 dist 目录直接放在了 Nginx 部署的目录下的，**请注意**：我这里并非是一个合格的方式，只能说是用来写Demo倒也无妨。请大家不要照抄~.

### 7、最佳实践

看过上一篇文章的读者可能知道,真正的应用场景中这样的部署并不安全,一旦出现bug,甚至都没法立马回退版本,或者出现意外情况没法快速横向扩容.

所以大概率下,最佳实践应当是

0.  在 vue 项目中增加 Dockerfile 文件 和 nginx.conf 配置文件
0.  部署时,首先将 dist + Dockerfile + nginx.conf 打成镜像 (docker build 相关明令)
0.  将打包出来的镜像上传至存储应用的服务器或DockerHub(私服仓库)
0.  最后在部署服务器上从存储镜像的那台服务器上拉取镜像,执行 `docker run` 相关命令进行发布.
0.  测试

***

> 原本是没有这一小节的，但是读了一遍，感觉有点遗漏，就新增了这一段。
>
> 当时在写的时候，前期的Nginx环境已经搭好，一定程度上少了一些思考，所以就补了这一小节。
>
> 关于 Vue 项目利用 Dockerfile 打包成镜像部署，以往也写过一篇不成熟的博客，如果有想改造成镜像发布的小伙伴，可以参考一下。链接：[Docker 部署 vue项目](https://juejin.cn/post/6989805769508012068)

### 8、测试自动构建

我们在本地修改文件，然后推送到远程仓库中，你刷新jenkins页面，就会发现它已经开始在构建啦。

![image-20220802001152688](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3463ddeecca248e8811f86944f4140d9~tplv-k3u1fbpfcp-zoom-1.image)

![image-20220802001441253.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4aae8a1cce044eaaae398da1bba44277~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：此处我放上的是刚写文的测试）

请注意：Github 因为是公用资源，有一定程度延迟，这一点在自己搭建的GitLab的私有仓库上是没有的。

看看我滴测试结果~

![image-20220802002453089](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b9700a4b90064cdeb97e0e22ca488e69~tplv-k3u1fbpfcp-zoom-1.image)

## 后记

写到这里本文也算是结束了，感觉这篇文章的质量比起上一篇的质量略低，可能里面带有一些重复性的内容，导致写起来有些懈怠了，望各位见谅。

> 看完这两篇文章，可能会有小伙伴产生一种感觉自己会用 Jenkins 了，哈哈，我也有这种感觉，但其实还差得很常远的，不过自己简单使用是肯定没啥问题了。

这个专栏还在继续更新，两篇入门文章总算是搞定了~

希望让你有所收获~，很开心你读到这里，听我讲完废话

> 如果今天的你也没有收到情书的话，那就让我送一封情书给你吧💌，
>
> **你不是一个人，你还有我。**
>
> **玫瑰已然到了花期**
>
> **我想把我这一生都奉献给你，让光阴将我烧成灰烬，化做风独舞。**

写于 2022 年 8 月 3 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559)