# Docsify 配合 Github Pages 搭建一个自己的云笔记

>[上一篇文章](https://juejin.cn/post/7104301359876276260)简单说了如何使用 `Docsify` 将自己的写的 `markdown` 文件变成可浏览的网站，但那只是在本地，只能自己看到，这显然是无法满足我们的。

这次我们来说说如何将他们部署到可访问的网站上的，你可能此刻就在我部署的 `github pages` 上阅读本篇文章。

当然使用 `gitee pages` 也是可以的，两者的操作几乎一致。

直入主题：

-   在 github 上创建一个仓库
-   将 本地文件上传至 github 仓库
-   通过 github pages 部署
-   访问测试

### 🪐创建一个 github 仓库

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/272b68ff904e420e856a31e83361272c~tplv-k3u1fbpfcp-zoom-1.image)

### 🌅将本地文件 push 至 github 仓库

将我们本地的 docsify 初始化的项目及写好的文章等，push 至 github 仓库。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0aa8068184194c57a0e9e8862b7180fd~tplv-k3u1fbpfcp-zoom-1.image)

我的本地目录就是这样的，docs 就是我用docsify 创建的项目，稍后我们就是将这整个文件夹上传至 github 。

```
git init ## 初始化一个 git 本地仓库

git add file ## 添加需要 push 的文件或文件夹 

git commit -m "first commit" ## 提交到暂存区

git branch -M main  ## 将本地的分支名改为 main 因为现在 github 的主分支名是 main 而不是master

git branch ## 此命令可查看本地名称

git remote add origin https://github.com/ningzaichun/nzc_blog.git ## 关联远程仓库

git push -u origin main ## push 至远程仓库
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e8bf9d643278400a8b0617ab641ebce8~tplv-k3u1fbpfcp-zoom-1.image)

` push  `完 ` github  `上的状态

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6a01ba94b0244eefb0ebaf4adfb0c06f~tplv-k3u1fbpfcp-zoom-1.image)

### 🌝使用 github pages 部署

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/70212237c56f4358a75eec77cf478816~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c7dbfa27530f4fe48fe2fbefa4eb2955~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2d0fb5dd25354e75a4db2b010a196b59~tplv-k3u1fbpfcp-zoom-1.image)![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8fe0157f0afe4d7481b2cf9e2fa7b6cc~tplv-k3u1fbpfcp-zoom-1.image)

点击下面的 `choose a them` 可以选择自己的发布样式

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/a46b23b18d2a4c26a294cb197149a6d5~tplv-k3u1fbpfcp-zoom-1.image)

之后还会跳转一下,直接点击就好了,忘记截了,最后反正记得 ` save  `就好.

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/51bc6feeae2c49eab86a2dac60519a00~tplv-k3u1fbpfcp-zoom-1.image)

会弹出这样的一个提示消息,证明就完事啦.

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3838db04ae90454ca1b0e499af35e6c7~tplv-k3u1fbpfcp-zoom-1.image)

回到code页面会出现一个这样的.

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/17317c5a45d54046b33cabf3dca97a7f~tplv-k3u1fbpfcp-zoom-1.image)

gh-pages 已经在五分钟前 push 了.当我框起来的那个地方,变成 `active`就表示已经部署完成,可以进行访问了.

这点有一些延迟,稍等一会刷新一下即可.

我们的链接在哪里勒?

直接点击这个 `github pages`进去是这样一个界面.

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/92d27b2e15744698b53aa0f05c76f8b7~tplv-k3u1fbpfcp-zoom-1.image)

直接点击这个 `view deployment` 就可以访问啦.

### 🌁关于 Push 更新问题

就是你将你本地的更新推送上去了，但是你访问 `github pages` 仍然是你未更新前的那个页面。

可能产生的原因：

1.  本身 `github pages`官方文档有说，可能会存在延迟之类的问题。
1.  可能是浏览器缓存问题，可以试着清除再访问。
1.  可能是`github server`所在时区的问题，这点是在`google`搜到的。

还可以尝试以下解决方法，直接在网页版上更改一下文件的空格，回车之类的，提交`commit`,再次触发更新。

### 🏂自言自语

花费了一些时间，写了这几篇文章，虽然都是一些非编程类的东西，但是对于我自己仍然是新知识，是让我有收获的。

在使用 ` github pages 和 docsify  `的过程中，其实还了解到很多其他的知识，如 CDN 加速、DNS 污染、计算机如何通信、解析等等关于计算机网络的一些知识。

> 耐心期待我们下一次的相见吧。

我自己搭建出来的效果：[宁在春](https://ningzaichun.github.io/nzc_blog/#/)
