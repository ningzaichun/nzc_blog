> 前面的几篇文章，分别讲述了如何 [使用 Jenkins 自动化部署前端](https://juejin.cn/post/7127671229707714591) 和 [Jenkins 自动化部署后端的 Maven 项目](https://juejin.cn/post/7127302949797101604)，接下来讲一讲 Jenkins 构建后的邮件通知，相对来说也是比较实用的一个点吧~

所有的操作都是可以实现的，希望感兴趣的读者，可以实操一下，有服务器或者虚拟机的小伙伴们都可以尝试尝试，不懂的可以留言，我也非常开心收到的评价和回复。

今天有小伙伴问我，2G 的云服务器可不可以搭建 Jenkins ，答案是可以的，Jenkins 占的内存空间在 1-1.1 G左右，而一般 2G 的云服务器一般可用内存在 1.7G左右，所以部署Jenkins + 一个小型的项目，还是扛的住的。

至于学习如何使用 Jenkins ，那是完全没问题的（单体项目及Demo项目）

## 一、前言

在构建部署完全自动化后，说真的，我们很少会专门去打开 Jenkins 看看他有没有构建成功或失败。

因此一些必要的通知相对来说是一个必需品。

让我们能感知到它的构建是否成功，测试的小伙伴是否可以开始测试等等等~

如果你对之前的知识稍有不熟悉：

1、[Docker 安装 Nginx 部署 Vue 项目](https://juejin.cn/post/7126146371198910478)

2、[Docker + Jenkins + Github 实现自动化部署 Maven 项目](https://juejin.cn/post/7127302949797101604)（包含如何使用Docker安装Jenkins、下载插件、系统配置、环境配置等等，可以实操部署成功）

3、[Docker + Jenkins + Nginx + Github 实现自动化部署前端项目](https://juejin.cn/post/7127671229707714591)

可以点过去复习一下~

## 二、Jenkins 安装插件

### 1、安装插件

Jenkins 本身含有邮件相关的配置，但是相对于不能满足一些大佬的需求，就有了相关的插件的出现~

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/24cbcbf4b15b4c63a0f49caf650bc06c~tplv-k3u1fbpfcp-zoom-1.image)

然后的话，我使用的是这款插件

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9195c26c65064d6da1022421cb1728bc~tplv-k3u1fbpfcp-zoom-1.image)

勾选完直接点击 install 即可~

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b86dcf333e984f64b7bf46ac508df4eb~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：出现 success or 完成即表示安装成功）

### 2、系统配置

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8b5b00a960d8432287f3bba318b042c5~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：点击进去进行系统配置）

#### 2.1、配置 Jenkins Location

在这个页面，找到 `Jenkins Location` 的配置项，这里的填写的邮箱，就是你刚刚拿到授权码的那个邮箱

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9d922abf1d684bf5a40d67d9c0d504bf~tplv-k3u1fbpfcp-zoom-1.image)

#### 2.2、配置 Extended E-mail Notification

然后找到 `Extended E-mail Notification` 配置项，我这里使用的是 qq 邮箱，第三方终端登录，都是授权码登录的方式，这一步大家百度一下下就行，很简单的，就是要花`一毛钱`的短信费用~

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8133c1f046cc4b8188283ee29f1c54ba~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：点击添加一个凭据）


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aa229c27f1404ecf87d8ca0500d18836~tplv-k3u1fbpfcp-watermark.image?)
（图片说明：大伙们记得点保存噢）


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/86b5b85fcbd84254b67ed888daea2512~tplv-k3u1fbpfcp-watermark.image?)

（图片说明：配置完这里，直接往下划，找到`Default Subject`配置项，下一步从这里开始）

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2da5af4a0c3e4230a4df83d7b8a6e9ec~tplv-k3u1fbpfcp-zoom-1.image)

**说明:**

1、`Default Subject`我们修改为

```
【构建通知】$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!
```

其中 `PROJECT_NAME、BUILD_NUMBER、BUILD_STATUS`都是 Jenkins 内置的环境变量

更多的环境变量的大家可参考官网给出的信息：[链接](https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-belowJenkinsSetEnvironmentVariables)

2、`Maximum Attachment Size`的意思设置限制发送邮件时所带附件的大小，不写或是`-1`就是不限制大小

3、`Default Content` 就是发送邮件时的内容，这里的内容我是从网上粘贴而来：

```
<!DOCTYPE html>    
<html>    
<head>    
<meta charset="UTF-8">    
<title>${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志</title>    
</head>    
    
<body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4"    
    offset="0">    
    <table width="95%" cellpadding="0" cellspacing="0"  style="font-size: 11pt; font-family: Tahoma, Arial, Helvetica, sans-serif">    
        <tr>    
            本邮件由系统自动发出，无需回复！<br/>            
            各位同事，大家好，以下为${PROJECT_NAME }项目构建信息</br> 
            <td><font color="#CC0000">构建结果 - ${BUILD_STATUS}</font></td>   
        </tr>    
        <tr>    
            <td><br />    
            <b><font color="#0B610B">构建信息</font></b>    
            <hr size="2" width="100%" align="center" /></td>    
        </tr>    
        <tr>    
            <td>    
                <ul>    
                    <li>项目名称 ： ${PROJECT_NAME}</li>    
                    <li>构建编号 ： 第${BUILD_NUMBER}次构建</li>    
                    <li>触发原因： ${CAUSE}</li>    
                    <li>构建状态： ${BUILD_STATUS}</li>    
                    <li>构建日志： <a href="${BUILD_URL}console">${BUILD_URL}console</a></li>    
                    <li>构建  Url ： <a href="${BUILD_URL}">${BUILD_URL}</a></li>    
                    <li>工作目录 ： <a href="${PROJECT_URL}ws">${PROJECT_URL}ws</a></li>    
                    <li>项目  Url ： <a href="${PROJECT_URL}">${PROJECT_URL}</a></li>    
                </ul>    
 
<h4><font color="#0B610B">失败用例</font></h4>
<hr size="2" width="100%" />
$FAILED_TESTS<br/>
 
<h4><font color="#0B610B">最近提交(#$SVN_REVISION)</font></h4>
<hr size="2" width="100%" />
<ul>
${CHANGES_SINCE_LAST_SUCCESS, reverse=true, format="%c", changesFormat="<li>%d [%a] %m</li>"}
</ul>
详细提交: <a href="${PROJECT_URL}changes">${PROJECT_URL}changes</a><br/>
 
            </td>    
        </tr>    
    </table>    
</body>    
</html>
```

配置完展示

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/85b0c48867424cc094b8719ad0f81b84~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：展示效果）

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0099e235a56e41acb0eaf693753c0968~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：`Default Triggers` 即设置默认的触发器）

不过为了一些细节问题，我把上面勾选的 `Enable Debug Mode` 也说一说吧，这里的意思是发送邮件的时候，将向构建日志添加额外的日志输出，方便查看错误。(请注意：这对于调试很有用，但不应该在完全生产模式下使用，这会影响性能)

下面几个的意思分别是：1、需要管理员进行模板测试 2、启用监视工作 3、允许发送给未注册用户

***

下面再来看看触发器。勾上的 Always 意思就是总是，触发事件就算出现任何错误或构建成功都将发送邮件。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/85fd1dcdc54b46a3827b0bfc2f9c5324~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：此配置项，配置到这里就算是结束了）

#### 2.3、配置邮件通知

在此页面找到邮件通知的配置项

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3064a9a7cbac4b07b7afe9be4f9f598c~tplv-k3u1fbpfcp-zoom-1.image)

打开高级选项后

<img src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6f06faa3354f48d095a82eb00f2392d9~tplv-k3u1fbpfcp-zoom-1.image" alt="image-20220803000556595" style="zoom:25%;" />

配置到这里，配置就算是结束了，我们先来进行测试一番~

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/13bf9faba8f943fd8a11a10337c37d77~tplv-k3u1fbpfcp-zoom-1.image)

成功是这样的：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/abc5b55792f84d5b83f3d8c4dd2e9a40~tplv-k3u1fbpfcp-zoom-1.image)

收到的邮件是这样的：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3faef71a25a34c0d89f68b7f36b9d17a~tplv-k3u1fbpfcp-zoom-1.image)

## 三、修改任务配置

点进你已经拥有的任务，我们来修改配置

### 3.1、增加构建后配置

直接划到配置的最下方，构建后操作

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/85d645e8f0d440198ebd85eaca94b354~tplv-k3u1fbpfcp-zoom-1.image)

`Project Recipient List`：项目收件人列表

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/848a4b9d144d42049296ebd0a9a66c35~tplv-k3u1fbpfcp-zoom-1.image)

`Project Reply-To List`：项目回复列表，这里我就一个账号，默认的就是我们配置的那个账号~

接着往下看

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ba2265f6d54d496e8f2b50405c736df1~tplv-k3u1fbpfcp-watermark.image?)
![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8928869cd2674a948be59df7f3a19b20~tplv-k3u1fbpfcp-zoom-1.image)


Attachments的读取路径是主目录下的workspace文件夹 ，所以想要将报告以附件的形式发送的话，就需要在build文件中将报告的生成路径更改为项目的工作空间~~

第一次弄吗，我们整简单的（我懈怠了~）

### 3.2、测试

修改本地代码，push到远程仓库上，查看构建结果。


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/07576b32826b44069b6f5cc5ef4ffd6a~tplv-k3u1fbpfcp-watermark.image?)
![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/219a1f9712da4739bd8be5ee346308fa~tplv-k3u1fbpfcp-zoom-1.image)

## 后记

> 虽然文章较为基础，但是有收获到的一些朋友的喜欢，这让我感觉到十分的开心。
>
> 你作为读者收获到知识，我作为创作者收获到你的认可或点赞，相互认可~

很开心获得你们的阅读，谢谢大家

今晚大家的身边应该有他或是她的陪伴吧，记得好好珍惜。

**如果没有的话，你还有我。**

写于2022 年 8 月 4 日晚，作者：**宁在春**
