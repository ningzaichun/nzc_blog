# Jenkins + Docker + Github 实现自动化部署 Maven 项目

> 昨日编写了 Docker 安装 Nginx 镜像部署前端Vue项目，也算是我写 Docker 集成 Jenkins 自动化部署专栏开篇文。
>
> 趁着周末写出了这篇文章，原本只打算写一点点，写上头，就直接一篇解决了~

补充：我书写此文时是 2022 年 7 月 22 日晚，其实我之前有在某乎刷到过 Jenkins 跟不上时代的问题，现在究竟还需不需要学习它。我的观点还是值得去学习的，因为学习知识本身就是个渐循渐进的过程。

## 一、Jenkins 介绍

看到这篇文章的你，或多或少都已经对 Jenkins 有过一定了解，就算没有也一定已经听过他的相关话题。

在我们学习阶段，常会听到持续集成和持续部署这样的词语，有些小伙伴们已经亲手实践过，还有些没有过，今天就让我们一起对 Jenkins 做一个了解吧。

### 1、持续集成和持续部署是什么

**持续集成**：CI 是一种开发实践，其中开发人员一天几次将代码集成到共享存储库中。当有人将新代码推送到共享存储库中时，测试会在非开发人员（测试人员）的计算机上自动运行。这种纯手动的构建测试,效率非常的低,开发人员必须等待测试人员的反馈后才知道结果,如果错了,还要修改bug , 这个过程一方面需要沟通成本,另外一方面效率是非常低的.

**持续部署**：我们都知道，项目最终是会部署到服务器上去，在没用Jenkins之前，大都是我们或专业的运维将项目进行部署。如果项目非常多或者部署完后出现bug，需要人手动的一个个部署或者能力强些的大佬，就是用脚本文件部署，但是看起来还是非常麻烦.

### 2、关于 Jenkins 

Jenkins 是一个用 Java 编写的开源自动化工具，带有用于持续集成的插件。

Jenkins 用于持续构建和测试您的软件项目，从而使开发人员更容易将更改集成到项目中，并使用户更容易获得新的构建。它还允许您通过与大量测试和部署技术集成来持续交付软件。

Jenkins 集成了各种开发生命周期过程，包括构建、文档、测试、打包、模拟、部署、静态分析等等。

Jenkins 借助插件实现了持续集成。插件允许集成各种 DevOps 阶段。如果要集成特定工具，则需要安装该工具的插件。例如 Git、Maven、Node 项目等。

### 3、Jenkins 的工作流程

我画了一张简易的Jenkins 的工作流程图，希望能带给你一些帮助。

![Jenkins工作流程图简易图](https://s2.loli.net/2022/07/24/Yu48gPJ5bXZQyHw.png)

（图片说明：Jenkins 一项配置简单的工作流程图）

流程说明：

1. 开发者在本地开发，然后提交到 Source Respository 中，
2. 触发GitHub或者 GitLab 配置的钩子函数程序，继而通知 Jenkins 
3. Jenkins 收到通知，会通过 Git/SVN 插件，重新从项目配置中的代码仓库中拉取最新代码，放置于 Workspace （Jenkins 存放任务的目录）
4. 之后重新触发构建任务，Jenkins 有很多的构建的插件，Java常用的 Maven 、Gradle，前端的 Node 等
5. 如果有安装发送邮件的插件并且进行了配置，那么可以在项目中进行配置，构建失败或者成功都可以选择是否给开发者发送邮件
6. 构建成功后，Jenkins 会通过一个 SSH 插件，来远程执行 Shell 命令，发布项目，在项目中可以配置多台服务器，也就可以一次性部署到多台服务器上去。
7. `补充`：当然很多时候，构建成功后，并不会直接部署到服务器上，而是打包到另外一台服务器上存储（应用服务器）或者存储为软件仓库中的一个新版本。
   - 原因是一方面为了更好的回退版本，出现错误可以及时恢复，因为一个大型项目，它的构建过程时间说不上短；
   - 另外一方面也是为了更好的扩展，如果出现紧急情况，需要横向扩展，可以在备用机器上，直接进行拉取部署即可。

一个简易的自动部署化的过程，大致是如此的。

但其实中间还有不少东西的，例如代码审查和 Jenkins 自动化测试等等，对一门技术了解的越多，不知道的也就越多了。

## 二、Docker 安装 Jenkins

Jenkins 其实支持各个系统安装，Windows 、Liunx 、Mac 都可以的。选择 Docker 是方便哈，因为我其他的环境都是用 Docker 搭建的~~ 所以我这里介绍的也是 Docker 安装 Jenkins，后续的文章也都是基于此。

我目前的环境：Jenkins 2.346.2、阿里云服务器centos7、Docker version 20.10.7

### 2.1、搜索Jenkins 镜像

```bash
docker search jenkins
```

![image-20220723105217630](https://s2.loli.net/2022/07/24/hAQKajwb58CBkU2.png)

`deprecated` 是弃用的意思，第一条搜索记录就是告诉我们 jenkins 镜像已经弃用，让我们使用 `jenkins/jenkins:lts` 镜像名进行拉取。

### **2.2、拉取 Jenkins 镜像**

```bash
docker pull jenkins/jenkins:lts
docker images #查看镜像
```

既然是学习，就得上手最新的啦，错了再降。

![image-20220723105806587](https://s2.loli.net/2022/07/24/lfYEbZ1Hh4SaGKq.png)

### 2.3、启动Jenkins 容器

在宿主机创建挂载目录

```bash
mkdir -p /home/jenkins/workspace
```

启动 Jenkins 容器

```bash
docker run -uroot -d --restart=always -p 9001:8080 \
-v /home/jenkins/workspace/:/var/jenkins_home/workspace \
-v /var/run/docker.sock:/var/run/docker.sock \
--name jenkins jenkins/jenkins:lts
```

![image-20220723110524382](https://s2.loli.net/2022/07/24/V6UtwgsANK8yIzh.png)

### 2.4、使用 Jenkins

这个时候就可以直接访问了。

![image-20220723110811270](https://s2.loli.net/2022/07/24/kvhHAEYjIWgsr6f.png)

会看到这样的一个界面，我们就要进入容器，去拿到这个密码。

```shell
docker exec -it -uroot jenkins bash # -uroot 是以管理员身份登入容器
```

![image-20220723110929164](https://s2.loli.net/2022/07/24/ZfaiMKbtxpO4jQ8.png)



然后复制粘贴上去后，会看到这样的一个界面。

![image-20220723112156113](https://s2.loli.net/2022/07/25/i9Ah4O2zRGBeK3Z.png)

如果和我一样是个小白的话，直接安装推荐的插件吧，稍微省事点，不然很多插件都需要自己一个个的查。

![image-20220723112512633](https://s2.loli.net/2022/07/24/5xRMptUojCYvdaV.png)

耐心等待它下载完吧

大家根据自己的需求进行操作，后续也是根据自己的想法一直点击下去就好了，反正咱们还在学习，无妨的。

![image-20220723112954636](https://s2.loli.net/2022/07/24/ZMdzAVRr3fBqiKu.png)



主界面

![image-20220723113129740](https://s2.loli.net/2022/07/24/gxdnXJwcVtMiQT8.png)

一些简单页面，大家点进去都能看的明白，我就不再多嘴了。

着重说一下 Manger Jenkins 界面一些东西

![image-20220723113937092](https://s2.loli.net/2022/07/24/FNwsStz51H7COGR.png)

### 2.5、配置 Jenkins 密钥

其实在很多时候你可以把 Jenkins 容器看成一台独立的服务器，因为你运行项目的那些环境，都可以安装在它的内部。

还是先说说配置密钥，配置密钥主要作用就是为了去 Github、Gitee、GitLab上拉取代码，这点相信大家都是能够理解的吧。

生成密钥：之前我们已经进入Jenkins的终端，直接输入下面的命令就好了。

```shell
ssh-keygen -t rsa -C "root"  #输入完一直回车就结束了
cat /root/.ssh/id_rsa.pub #查看公钥
```

![image-20220723115528195](https://s2.loli.net/2022/07/24/5A9ioQhwWOXyR1u.png)

如果没有的话，就先输入 下面命令进入 Jenkins 容器终端

```bash
docker exec -it -uroot jenkins bash # jenkins 是我启动的容器名 换成容器id 也可以的
```

拿到 Jenkins 公钥后，就放到 Github、Gitee或者是 GitLab 上去，我放的 Github，如下：

![image-20220723115932581](https://s2.loli.net/2022/07/24/wOaYfPLMcvyRsVd.png)

![image-20220723120019516](https://s2.loli.net/2022/07/24/xkIeBS6JLhzQ2Z1.png)

这样就算是添加完成了。

到这一步，可以进行测试一下，是否已经可以从Github上拉取项目

![image-20220723120914139](https://s2.loli.net/2022/07/24/etI43WZUwyf5Fzg.png)

## 三、 Jenkins 插件安装、添加凭据、系统配置、全局工具配置

实际上 Jenkins 的功能基本上都是依靠插件来完成，所以不同项目也会要安装不同的插件。

### 3.1、插件安装

我演示的是一个 SpringBoot 后端项目的部署，中间也没有穿插复杂的操作，所以装的插件也不多哈。

![image-20220723121352442](https://s2.loli.net/2022/07/24/oy94TN3nRIht6Dk.png)

安装的插件的名称：

-   [Maven Integration](https://plugins.jenkins.io/maven-plugin) ：Maven 项目打包工具
-   [SSH](https://plugins.jenkins.io/ssh) ： SSH 连接工具
- [ Publish Over SSH ](https://plugins.jenkins.io/publish-over-ssh) ：SSH 发布工具
- 如果要运行前端 Vue 项目，记得下载一个 NodeJS 的插件（我会的前端就只有Vue哈）



![image-20220723121523426](https://s2.loli.net/2022/07/24/RU3q4ri7IYwLZog.png)



![image-20220723121804409](https://s2.loli.net/2022/07/24/sOg5db3XJaEwUvG.png)

等待下载完即可

### 3.2、添加凭据

凭据其实就是账号密码，你访问Github、远程服务器都需要账号密码才行，这里的凭据就是相应的账号密码。

![image-20220723122147117](https://s2.loli.net/2022/07/24/pRdrOsPvEC9F7ya.png)

![image-20220723122837210](https://s2.loli.net/2022/07/24/sd5hGzAC9pvHfr1.png)

添加 github 的账号密码

![image-20220723123233427](https://s2.loli.net/2022/07/24/QaYGdDNXj129qo7.png)

添加服务器的登录账号和密码

![image-20220723123352486](https://s2.loli.net/2022/07/24/mUxFtOyBbvqDAip.png)

补充：这些都是可以填加多个的。

最后就是这样的：

![image-20220723123453014](https://s2.loli.net/2022/07/25/HZ2Q6b9lKuPkoan.png)

稍后在项目中都是需要用到的。

### 3.3、系统配置



![image-20220723123647297](https://s2.loli.net/2022/07/24/RAt6Di9VfkBJcUs.png)



找到两个配置：

1、SSH remote hosts

2、SSH Servers

![image-20220723124508598](https://s2.loli.net/2022/07/25/vcgPD19RBtQTwWh.png)

![image-20220723125259942](https://s2.loli.net/2022/07/24/4oSJiwjV9HMguCh.png)





![image-20220723124931631](https://s2.loli.net/2022/07/24/6wYSsoW9kRymLT7.png)

对了记得点击保存哈，不然又得重现填写。

### 3.4、全局工具配置

我上文有提到很多时候我们可以把 Jenkins 看成一台单独的电脑，尤其是在工具设置的时候。

![image-20220723125320276](https://s2.loli.net/2022/07/24/Fs4fqAlXME9KzdQ.png)

在这里其实就是配置一些项目中需要用到的环境，如JDK、Maven、NodeJS等等

![image-20220723134637915](https://s2.loli.net/2022/07/24/ziKj3AN67Zeh2qW.png)

#### 1、Maven 配置

这里可以用默认的，也可以用宿主机文件系统中的。

![image-20220723134827332](https://s2.loli.net/2022/07/24/8k9sG65IPwAEDtc.png)

我这里用的是默认的，因为我Liunx服务器上的环境全部都是基于 Docker 搭建的。

选择默认的话，就需要在 Jenkins 中新增一个，然后Jenkins在你构建项目的时候，如果是选择默认的话，没有的Maven情况下，它会主动给你下载一个Maven

![image-20220723135222264](https://s2.loli.net/2022/07/24/nVQMcWez7Ojms6i.png)



#### 2、JDK 配置

JDK 也是可以选择自动安装和使用宿主机原有的 JDK 两种方案。

![image-20220723135844130](https://s2.loli.net/2022/07/25/tUxvHB7MmTJgDne.png)

点击 `Please enter your username/password`蓝色小字后，会跳转至下面的界面

![image-20220723135858863](https://s2.loli.net/2022/07/24/NbCHl6qQcokdze7.png)

(图片说明：在这里输入 Oracle 官网的账号密码即可)

没有账号需要去注册 [Oracle](https://www.oracle.com/) 账号

`补充`：如果这里选择 Oracle 官网下载JDK，最高支持JDK 版本为 9，如果要选择更高的稳定的 JDK 版本，一个是使用宿主机的 JDK，另外就是使用压缩包方式，然后解压等。只要想用，互联网应该还是能够满足的。

关于 Git ，我们直接用 Jenkins 默认的即可，在安装 Jenkins 推荐的插件的时候，其中就有Git，当然Git，这里Jenkins也允许你使用 宿主机的Git。

**最后记得点击保存**

## 四、Jenkins 部署 Maven 项目

相关要求：

1. 本地需要Java、Git 环境
2. 需要有一个 Github/Gitee 账号
3. 不管在那里安装的 Jenkins 都要确保它能够访问网络

### 4.1、本地创建一个Maven项目

创建一个SpringBoot 项目，controller，自己看着写就好了 

![image-20220723142435374](https://s2.loli.net/2022/07/24/DRtma8ULjSQ5BzM.png)

pom.xml

```xml
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.1</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>2.5.1</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.5.1</version>
        </dependency>
    </dependencies>

    <!-- 这个插件，可以将应用打包成一个可执行的jar包  -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

```

Dockerfile文件

```dockerfile
FROM adoptopenjdk/openjdk8

WORKDIR app  #切换到镜像中的指定路径，设置工作目录

COPY target/*.jar app.jar  #会将宿主机的target/*.jar文件复制到 镜像的工作目录 /app/ 下 

CMD ["java", "-jar", "app.jar"]  #执行java -jar 命令
```

因为不是本文关注的重点，更多详情可能还需朋友们去查询。

### 4.2、推送到远程仓库 github 上

- 首先在 Github 上创建一个仓库

- 然后点击 idea 中下方菜单栏 Terminal 命令行终端中输入

  ```bash
  git init 
  git add .  #这里 . 默认提交所有修改文件
  git commit -m "init"
  git branch -m main # 因为gitgub 仓库默认主分支为main ，而我们本地初始化时 主分支为 master 
  git remote add  远程仓库地址 # 添加远程仓库
  git push origin main  # push 上去
  ```

- 然后刷新 github 界面，就可以看到已经push成功了

### 4.3、Jenkins 项目配置

新建任务

![image-20220723143758004](https://s2.loli.net/2022/07/24/tbXDxyTdlI9mOoj.png)

选择构建一个 Maven 项目

![image-20220723143827889](https://s2.loli.net/2022/07/24/dZILMAb2s68x3t1.png)

项目配置

![image-20220723143922119](https://s2.loli.net/2022/07/24/XmWMzr9kt2HwO5A.png)

源码管理

![image-20220723144152929](https://s2.loli.net/2022/07/24/UxsTmF1DvGNcfrz.png)



![image-20220723144444730](https://s2.loli.net/2022/07/24/SV78z2foXaRcG1J.png)



构建环境没啥说的

![image-20220723144550407](https://s2.loli.net/2022/07/24/B5oigMsQlCf31uz.png)

Build 那不用改啥，用默认的就可以。

然后来到 **Post Steps**

![image-20220723144756098](https://s2.loli.net/2022/07/24/pwTNKjPdGYzE3Wr.png)

选择这个 `send files or execute commands over SSH` 就是`通过SSH发送文件或执行命令`

我们要将构建好的 jar/war 发送至相应的服务器，然后执行相关的命令，进行部署。



![image-20220723145447021](https://s2.loli.net/2022/07/24/9IHA6Z5c1fiGyke.png)



![image-20220723145615649](https://s2.loli.net/2022/07/25/jNQly72SZ3raCc4.png)

（图片说明：那个映射的端口应为8080，我写漏了）

```shell
#/bin/bash
# 注意 其实在这里输入的命令，就是在服务器上的命令，我们所处于的位置就是当前登录用户的根目录下 
echo ">>>>>>>>>>>>>cd 到宿主机映射 Jenkins 的项目路径下>>>>>>>>>>>>>"

cd /home/jenkins/workspace/hello-springboot

echo ">>>>>>>>>>>>>停止容器>>>>>>>>>>>>>"

docker stop hellospringboot

echo ">>>>>>>>>>>>>删除容器>>>>>>>?>>>2>22"

docker rm hellospringboot

echo ">>>>>>>>>>>>>删除镜像>>>>>>>>>>>> >"

docker rmi nzc/hellospringboot:1.0

echo ">>>>>>>>>>>>>制作镜像>>>>>>>>>>>>>"

docker build -f Dockerfile -t nzc/hellospringboot:1.0  .

echo ">>>>>>>>>>>>>启动容器>>>>>>>>>>>>>"

docker run -p 8080:8080 --name hellospringboot -d nzc/hellospringboot:1.0

echo ">>>>>>>>>>>>自动部署结束>>>>>>>>>>>>>"
```

**记得点击保存**

### 4.4、部署和测试

然后点击立即构建就好了，但因为是第一次构建，要从 github 拉取代码，下载 jdk、maven等，还有相应 jar 包等，所以时间会相对久一些。

![image-20220723152329041](https://s2.loli.net/2022/07/25/6G9haMrex3KI1mO.png)

点击这个，可以看到控制台输出

![image-20220723152700696](https://s2.loli.net/2022/07/24/bf13tlOqRUCm8My.png)



控制台输出，日志比较多，就挑了一点

![image-20220723154632451](https://s2.loli.net/2022/07/25/T7PMGKpcHLE3lye.png)



![image-20220723154722014](https://s2.loli.net/2022/07/24/pRFH7AyfY5VsJvS.png)

末尾是`Finished: SUCCESS` 就证明是构建成功啦。

我们来看看Jenkins 的工作空间

![image-20220723154817581](https://s2.loli.net/2022/07/24/HJW6hVF3mXDateN.png)

这里已经是存在项目啦。

我们再去看看 Docker 镜像有没有构建成功

![image-20220723155019541](https://s2.loli.net/2022/07/25/YOaD5FBsXCfq16K.png)

再去看一眼 有没有在运行 

![image-20220723155506088](https://s2.loli.net/2022/07/24/A3mVuH9e4MgpkDb.png)

最后就是看看能不能访问到了

![image-20220723155605581](https://s2.loli.net/2022/07/24/fVmRLAEal6ySNY7.png)

已经是可以访问到了。

当然现在的话，还没有做到自动化部署，就是我提交完，jenkins 就能自己知道，然后进行构建，我们现在要做的就是把手动构建修改成 github 更新或合并就构建。



## 五、GitHub提交代码时触发 Jenkins 自动构建

其实主要就三步，因为前面我们已经搭建好了，所以就只要修改一下即可。

### 5.1、GitHub 上配置 Jenkins 的 webhook

![image-20220723162219771](https://s2.loli.net/2022/07/24/RrygcB35bo264Sv.png)

![image-20220723162409957](https://s2.loli.net/2022/07/24/tPHYx2jipXTgvNS.png)

像我jenkins是部署在服务器上的 我的 地址就是服务器 `IP:port/github-webhook/`

### 5.2、GitHub上创建一个access token

Jenkins做一些需要权限的操作的时候就用这个access token去鉴权

![image-20220723162600975](https://s2.loli.net/2022/07/25/2ZFTVr5sytPdIW7.png)

![image-20220723162616329](https://s2.loli.net/2022/07/24/4YXirBdCemw1kUZ.png)

就是命名，然后勾选你需要的权限就可以了

最后完成的时候，记得复制

![image-20220723162706514](https://s2.loli.net/2022/07/24/DwOHldBxMLmtyAs.png)

### 5.3、修改 Jenkins 配置

首先先要修改一下系统配置 `Configure System`

![image-20220723162820377](https://s2.loli.net/2022/07/24/yRQqnP3jV9aAugc.png)

点添加的时候，会弹出一个框

![image-20220723162857612](https://s2.loli.net/2022/07/24/p78gtDdbOcUwqX2.png)



![image-20220723162929203](https://s2.loli.net/2022/07/25/9UcMfBuZRzlg4bD.png)

描述自己写就行~~

![image-20220723162956186](https://s2.loli.net/2022/07/24/zEoGt29WpUvKyn5.png)



第一步完成，记得点击保存，接下来去修改一下项目配置



找到项目，点击配置

把构建触发器中的 `GitHub hook trigger for GITScm polling` 勾上

![image-20220723163041973](https://s2.loli.net/2022/07/24/c6Kdl1r9Pxpoewf.png)



![image-20220723163206419](https://s2.loli.net/2022/07/24/w8ZuzdGBhcgW9lK.png)

记得点击保存。

然后就可以进行测试啦。



### 5.4、Push 测试

![image-20220723163348007](https://s2.loli.net/2022/07/25/651bWkuSFGaIDqy.png)

这是我本地还没更新的代码，现在push上去哈。

![image-20220723163501207](https://s2.loli.net/2022/07/24/dwYQeBh2KkFqJTs.png)

push 成功后，刷新一下就可以看到构建任务正在执行了。

![image-20220723163454764](https://s2.loli.net/2022/07/24/FeZEQPWsdDqlCbf.png)

在构建日志中也可以看到 最新 的提交记录

![image-20220723163709417](https://s2.loli.net/2022/07/24/KoYXeBHM61FqVrx.png)

![image-20220724231528704](https://s2.loli.net/2022/07/24/5xlTNy4fjJLA8ia.png)



## 六、总结

文章的脉络大致：

首先是介绍了 Jenkins，以及 Jenkins 的简单工作流程；

而后是教大家如何用 Docker 安装 Jenkins ；

然后再是对 Jenkins 进行一些插件安装和配置；

再以 Jenkins 部署 Maven 项目为案例，讲解如何使用 Jenkins；

最后是对上一步操作的改早，Jenkins + Github + Docker 实现自动化部署。

---

如果可以的话，我觉得还是实操一遍比较好~

## 后记

写到这里，这篇文章也算是结束了，我尽可能的将里面牵扯到的知识点都写的通俗易懂，但是究竟是如何的，我也还不知晓，希望能够得到你们的反馈和支持。

自我感觉这篇文章的质量以及实用程度，应该算是初级文章中，比较合格了的吧。

---

> 其实在写文的过程中，能够自我反省，很多时候，一些东西用了就忘了，但是当静下心来将知识慢慢输出时，能够发现诸多奥妙，以及当时未注意到的知识点，挺有收获的，同时希望你也是如此。



看到的一个评论，好有趣啊 哈哈

![[哦呼]](http://i0.hdslb.com/bfs/emote/362bded07ea5434886271d23fa25f5d85d8af06c.png)![[哈欠]](http://i0.hdslb.com/bfs/emote/888d877729cbec444ddbd1cf4c9af155a7a06086.png)![[大笑]](http://i0.hdslb.com/bfs/emote/ca94ad1c7e6dac895eb5b33b7836b634c614d1c0.png)![[抓狂]](http://i0.hdslb.com/bfs/emote/4c87afff88c22439c45b79e9d2035d21d5622eba.png)![[给心心]](http://i0.hdslb.com/bfs/emote/1597302b98827463f5b75c7cac1f29ea6ce572c4.png)![[爱心]](http://i0.hdslb.com/bfs/emote/ed04066ea7124106d17ffcaf75600700e5442f5c.png)



![[给心心]](http://i0.hdslb.com/bfs/emote/1597302b98827463f5b75c7cac1f29ea6ce572c4.png)![[抓狂]](http://i0.hdslb.com/bfs/emote/4c87afff88c22439c45b79e9d2035d21d5622eba.png)![[哈欠]](http://i0.hdslb.com/bfs/emote/888d877729cbec444ddbd1cf4c9af155a7a06086.png)![[尴尬]](http://i0.hdslb.com/bfs/emote/cb321684ed5ce6eacdc2699092ab8fe7679e4fda.png)![[吓]](http://i0.hdslb.com/bfs/emote/9c10c5ebc7bef27ec641b8a1877674e0c65fea5d.png)![[疼]](http://i0.hdslb.com/bfs/emote/905fd9a99ec316e353b9bd4ecd49a5f0a301eabf.png)![[生病]](http://i0.hdslb.com/bfs/emote/0f25ce04ae1d7baf98650986454c634f6612cb76.png)![[吐]](http://i0.hdslb.com/bfs/emote/06946bfe71ac48a6078a0b662181bb5cad09decc.png)![[灵魂出窍]](http://i0.hdslb.com/bfs/emote/43d3db7d97343c01b47e22cfabeca84b4251f35a.png)

