# Docsify 配合 Github Pages 搭建一个自己的云笔记

[上一篇文章](./使用doscify将文章写成文档一般丝滑.md)简单说了如何使用 `Docsify` 将自己的写的 `markdown` 文件变成可浏览的网站，但那只是在本地，只能自己看到，这显然是无法满足我们的。

这次我们来说说如何将他们部署到可访问的网站上的，你可能此刻就在我部署的 `github pages` 上阅读本篇文章。

当然使用 `gitee pages` 也是可以的，两者的操作几乎一致。



直入主题：

- 在 github 上创建一个仓库
- 将 本地文件上传至 github 仓库
- 通过 github pages 部署
- 访问测试

### 一、创建一个 github 仓库



![image-20220522211333693](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522211333693.png)

### 二、将本地文件 push 至 github 仓库

将我们本地的 docsify 初始化的项目及写好的文章等，push 至 github 仓库。

![image-20220522211912629](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522211912629.png)

我的本地目录就是这样的，docs 就是我用docsify 创建的项目，稍后我们就是将这整个文件夹上传至 github 。

```bash
git init ## 初始化一个 git 本地仓库
git add file ## 添加需要 push 的文件或文件夹 
git commit -m "first commit" ## 提交到暂存区
git branch -M main  ## 将本地的分支名改为 main 因为现在 github 的主分支名是 main 而不是master
git branch ## 此命令可查看本地名称
git remote add origin https://github.com/ningzaichun/nzc_blog.git ## 关联远程仓库
git push -u origin main ## push 至远程仓库
```

![image-20220522212206407](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522212206407.png)

`push `完 `github `上的状态

![image-20220522213114397](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522213114397.png)





### 三 使用 github pages 部署

![image-20220522214405460](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522214405460.png)

![image-20220522214423660](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522214423660.png)

![image-20220522214500969](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522214500969.png)

![image-20220522214559279](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522214559279.png)



点击下面的 `choose a them` 可以选择自己的发布样式

![image-20220522214749826](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522214749826.png)

之后还会跳转一下,直接点击就好了,忘记截了,最后反正记得 `save ` 就好.



![image-20220522215121742](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522215121742.png)

会弹出这样的一个提示消息,证明就完事啦.

![image-20220522215200347](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522215200347.png)

回到code页面会出现一个这样的.

![image-20220522215429182](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522215429182.png)



gh-pages 已经在五分钟前 push 了.当我框起来的那个地方,变成 `active`就表示已经部署完成,可以进行访问了.

这点有一些延迟,稍等一会刷新一下即可.



我们的链接在哪里勒?

直接点击这个 `github pages`进去是这样一个界面.

![image-20220522215634679](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220522215634679.png)



直接点击这个 `view deployment` 就可以访问啦.

