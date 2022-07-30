# Jenkins + Github + Nginx 自动化部署 Vue 项目

>算起来，这应该是建立这个专栏来的第三篇文章啦，可喜可贺，虽然菜，但还在坚持写。
>
>上一篇文章其实已经介绍过如何利用 Jenkins + Github + Docker 部署一个 Maven 项目，同时也包含了如何使用Docker 安装 Jenkins ，以及一些基本概念 📌文章链接
>
>有了后端，那么必然也少不了前端，所以就诞生了本文。

## 前言

**看起来好像 Jenkins 非常复杂，但其实只要自己多实操几次，一次又一次的去想如何偷懒，你就可以一步一步发现更多的知识点，要相信好奇永远是你的第一老师。**

先说说本文最后做出来的效果：

1. 本地开发，push 到 github 仓库后
2. 触发 Github 的钩子函数，通知 Jenkins ，进行重新构建
3. Jenkins 构建完成后，将前端打包出来的 dist 目录，发送到部署的服务器上的 Nginx 容器挂载的部署目录下
4. 进行访问测试

除了第一步是需要自己动手外，其余部分实现自动化。

前一篇文章主要介绍了 Jenkins 如何构建一个 Maven 项目，但其实大家可以看到 Jenkisn 还有其他几钟不同的构建项目的方式。

![image-20220725231423445](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725231423445.png)

本篇文章用到的是自由风格式（Freestyle project）部署前端项目。也很简单的哈。

## 一、初始化项目

如果需要跟着文章实操，大致需要以下环境：

- 一台云服务器或本地虚拟机
- 服务器或虚拟机上需要联网、Docker 环境
- 一个 Github 账号
- 开发机器上需要有 Git 和 开发环境

### 1、初始化 Vue 项目

其实我也不知道这一步该不该写......

重点就是大家准备一个可以运行和打包的 Vue 项目。

如果有小伙伴，没的话，我有~，给你指路： **[jenkins-vue-demo](https://github.com/ningzaichun/jenkins-vue-demo)**

拉下来之后，把 .git 文件删除掉，然后重新关联你的github 仓库就好~

### 2、推送至 Github 仓库

1. 在 github 建立一个仓库 （默认大家都会哈~ 不会可以留言的，摸鱼的时候会回复的，别慌）

2. 然后在本地项目目录下执行下面的命令，其实不写，在你创建仓库的时候也会给出这些提示命令

   ```bash
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

![image-20220725234238980](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725234238980.png)

![image-20220725234704506](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725234704506.png)

之后点击创建即可



### 2、创建一个 Personal access tokens



![image-20220725235047549](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235047549.png)



![image-20220725235149381](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235149381.png)

![image-20220725235309522](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235309522.png)

![image-20220725235431597](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235431597.png)





## 三 、Jenkins 

### 1、安装 Nodejs 插件

![image-20220725235601294](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235601294.png)



![image-20220725235637764](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235637764.png)



等待完成即可

![image-20220725235704877](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235704877.png)



### 2、配置 Nodejs 



![image-20220725235746956](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220725235746956.png)



本地机器查看 node 版本 命令为 `node -v`

![image-20220726000026696](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726000026696.png)



### 3、创建一个自由风格式任务

![image-20220726001018326](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726001018326.png)





![image-20220726001057300](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726001057300.png)





![image-20220726001420957](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726001420957.png)

(图片说明：指定分支应为 `main`，图中有误)





![image-20220726001637797](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726001637797.png)





![image-20220726001753543](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726001753543.png)







![image-20220726002005243](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002005243.png)





![image-20220726002041791](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002041791.png)





![image-20220726002056727](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002056727.png)

（图片说明：变量无需填写）

![image-20220726002136595](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002136595.png)

（图片说明：选择 secret text）





![image-20220726002227276](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002227276.png)

(图片说明：描述就是取一个名称)



![image-20220726002323326](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002323326.png)

（图片说明：选择自己添加的那个 凭据）





![image-20220726002442954](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002442954.png)





![image-20220726002542464](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002542464.png)





```shell
npm cache clear --force # 清理 npm 缓存，环境正确，不清除也可以的~
npm --registry https://registry.npm.taobao.org install cluster # 配置淘宝镜像
npm install --force 
npm run build
echo "打包完成"
```





![image-20220726002723667](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726002723667.png)





![image-20220726003441164](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726003441164.png)









第一次构建的时间会稍长，因为还需要下载 Nodejs 环境

![image-20220726003805414](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726003805414.png)

![image-20220726003814903](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726003814903.png)



![image-20220726003845074](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726003845074.png)





![image-20220726003857289](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220726003857289.png)