# Jenkinsfile 脚本放置源码仓库中 && 相关构建器

之前说到过，如果使用 Pipeline 的 Jenkinsfile 的方式进行构建，将脚本文件放置在Jenkins的配置中，是不太合理的，如果想要修改的话，还必须特意登录Jenkins打开配置进行修改，这中间的过程是完全可以省下来的。

就是将Jenkinsfile 同源码放在一起，这样要修改构建方式或者有什么改动的话，可以直接在本地进行修改，然后push到远程仓库中，然后Jenkins进行构建，不用再经历中间的过程。

## 移动Jenkins脚本文件

将之前任务中的jenkinsfile脚本文件copy出来，在代码中建立一个`Jenkinsfile`，

![image-20220822230237306](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822230237306.png)



脚本文件还是像之前的那样，无需修改。

> 记得push上去啊，别等到配完后面，点击构建了，报错找不到原因哈~。

![image-20220822232515747](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822232515747.png)



没看过前文的小伙伴可以点击，脚本文件可以在以下文章中找到~

（1）[Jenkins Pipeline 流水线方式部署 SpringBoot 项目](https://juejin.cn/post/7132503611786919972)

（2）[Jenkins Pipeline 流水线任务 补充篇](https://juejin.cn/post/7133245897080569892)

这里不再复述。

另外搭建Jenkins、 Jenkins 自动化构建 maven、vue 项目，专栏中也都有可实现案例~

## 修改Jenkins 任务配置项

打开 jenkins 的任务配置，在流水线的选项中，选择`Pipeline script from SCM`

![image-20220822230932857](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822230932857.png)

`Pipeline script from SCM`这里的意思就是流水线脚本来自于供应者，在下面就是选择我们的供应方是谁~

这里就选择git，然后填写相关的信息，选好你的分支信息即可

![image-20220822231534506](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822231534506.png)

另外这个脚本路径，你可以简单理解为当前项目的根路径即可。

![image-20220822231951677](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822231951677.png)

（图片说明：记得点击保存~）

测试说明：

一键构建成功~

![image-20220822232604237](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822232604237.png)





## Jenkins 相关构建器

其实讲到这里，构建单体项目是完全没有问题了，我一直鸽着没更那个多项目的部署，还有依赖构建，就是这个项目依赖上一个项目构建的结果这种。

依赖构建倒是不难的，你要依赖那个项目，你指定一下就可以，这一点在微服务中常常要用到~

如下图：

![image-20220822233226938](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220822233226938.png)

这几个构建器都非常容易弄，大家可以亲自下场试一试~

## 后记

> 欠下的帐都快还不完了....
>
> 躺平一下🛌
>
> 咋说勒，最近心思有些飘，有点焦虑，陷入很多思考中。

写于 2022 年 8 月 22日晚，作者：宁在春

