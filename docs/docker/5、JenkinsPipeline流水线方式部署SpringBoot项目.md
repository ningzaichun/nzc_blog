# Jenkins Pipeline 流水线方式部署 SpringBoot 项目

## 前言

### 抱歉

**首先要跟前面阅读的小伙伴说一声抱歉，十分不好意思。**

> 因为我个人的疏忽，在安装环境的那篇文章 ☞【[Docker安装Jenkins](https://juejin.cn/post/7127302949797101604/#heading-20)】 中的启动 Jenkins 的命令中缺少了**关于  docker 的映射**，如果按照那个方式安装  Jenkins 是无法在 Jenkins 内部中直接运行 docker 命令的。
>
> 在发现的第一时间，我也尝试了其他的方式进行调整，但是始终无果，我最后采取的方式就删除了原来的容器，重新走了一遍安装环境的流程。
>
> 所以如果是按照【[Docker安装Jenkins](https://juejin.cn/post/7127302949797101604/#heading-20)】文章安装环境的小伙伴，**需要重新搭建一遍环境**。

### 感谢

> 写了前几篇文章，大家给予的反馈我都有一一收到，非常感谢大家的阅读，让我越发想要将要这个专栏写好，也希望让每一位阅读的朋友，有所收获。我想那就是我的写文章的快乐吧~😁

---

如果大家看完前几篇文章，大家对 Jenkins 的基本使用肯定是没有问题的，如使用Maven方式部署一个SpringBoot项目，又或者是使用自由风格方式部署前端项目等。

今天继续聊到的是**使用流水线方式部署一个 SpringBoot 项目**，会从浅至深，一步步带领大家实操，以及说明其中为什么需要如此做，当然，如有疑问，大家留下评论即可，我会在看到的第一时间给予回复。

## 安装环境

### 2.1、启动命令

之前出现疏忽的启动命令：

```bash
 docker run -uroot -d --restart=always -p 9001:8080 \
 -v /home/jenkins/workspace/:/var/jenkins_home/workspace \
 -v /var/run/docker.sock:/var/run/docker.sock \
 --name jenkins jenkins/jenkins:lts
```

**修正后的启动命令**：！！！

```bash
 docker run -uroot -d --restart=always --privileged=true  -p 9001:8080 \
 -v /home/jenkins/workspace/:/var/jenkins_home/workspace \
 -v /var/run/docker.sock:/var/run/docker.sock \
-v /usr/bin/docker:/usr/bin/docker \
 --name jenkins jenkins/jenkins:lts
```

修正后的启动命令多了关于 docker 的映射：`-v /usr/bin/docker:/usr/bin/docker `

还有`--privileged=true` 指定容器为特权容器~

其他的配置的都可以参考前面的 【[Docker安装Jenkins](https://juejin.cn/post/7127302949797101604/#heading-20)】文章。

至于配置环境变量，则是在本文中~



### 2.2、配置环境变量

![image-20220816220329072](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816220329072.png)

（图片说明：打开全局系统配置）

找到`全局属性配置`:

![image-20220816220422795](https://s2.loli.net/2022/08/16/VFiROaguWk5ZMUY.png)

(图片说明：配置环境变量)

**补充**：我部署的是Springboot项目，所以主要依赖的环境变量就是jdk和maven

```bash
key:JAVA_HOME
value:/var/jenkins_home/tools/hudson.model.JDK/jdk8

key:M2_HOME
value:/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/Maven3.6.3

key:PATH+EXTRA
value:$M2_HOME/bin
```

这里的jdk8、Maven3.6.3均我在下载的时候，自定义的名称。

环境变量的值的由来：

其实在Jenkins容器中的目录下均可查看。如果不知道自己的，可以进入jenkins容器中查看：

```bash
docker exec -it -uroot jenkins bash
cd /var/jenkins_home/tools
ls
pwd
```

> 注意：如果你没有使用过jdk、maven，jenkins是不会给你自动下载的，所以你得回顾一下之前的内容，使用以下maven编译下项目，才能在容器中看到如下目录。

![image-20220816220922084](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816220922084.png)

![image-20220816221034192](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816221034192.png)

![image-20220816221054114](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816221054114.png)

留下个小思考：

> 为什么Maven要配`/bin`执行命令的全局变量，而`Java`不用？

## 一、浅提一下 Pipeline 

浅提一下：

大伙整个干净的SpringBoot项目放到github上~，待会要用，没有的话，直接复刻我这个 [hello world](https://github.com/ningzaichun/hello-springboot)，也行。

---

### 1）Pipeline 简介

**Pipeline，简单来说，就是一套运行在 Jenkins 上的工作流框架，将原来独立运行于单个或者多个节点 的任务连接起来，实现单个任务难以完成的复杂流程编排和可视化的工作**。 

看到这个概念大家应该也能猜到，它的最佳实践应当是微服务部署~

### 2）使用Pipeline 优点

（来自翻译自官方文档）：

代码：Pipeline以代码的形式实现，通常被检入源代码控制，使团队能够编辑，审查和迭代其传送流程。

持久：无论是计划内的还是计划外的服务器重启，Pipeline都是可恢复的。 

可停止：Pipeline可接收交互式输入，以确定是否继续执Pipeline。 

多功能：Pipeline支持现实世界中复杂的持续交付要求。它支持fork/join、循环执行，并行执行任务的功能。 

可扩展：Pipeline插件支持其DSL的自定义扩展 ，以及与其他插件集成的多个选项。 



### 3）如何创建 Jenkins Pipeline

Pipeline 脚本是由 Groovy 语言实现的，但是我们没必要单独去学习

Groovy Pipeline 支持两种语法：Declarative(声明式)和 Scripted Pipeline(脚本式)语法 

Pipeline 也有两种创建方法：

- 可以直接在 Jenkins 的 Web UI 界面中输入脚本；
- 也可以通过创建一 个 Jenkinsfile 脚本文件放入项目源码库中。（我们一般都推荐使用在 Jenkins 中直接从源代码控制(SCM) 中直接载入 Jenkinsfile Pipeline 这种方法）。

---

### 4）本文的大致思路

这篇文章的思路：我是将总的 Jenkins 脚本文件，一步一步拆出来讲解

1. 创建 Pipeline 任务
2. 从 Github 拉取代码
3. 使用 Maven 打包编译
4. 打包成镜像，发布到镜像仓库或私有仓库（这一步图方便省去了~，代码中其实是有的）
5. 停止旧容器，删除旧版本镜像
6. 拉取镜像 （从私服或者是如我演示的一样，从阿里云拉取）
7. 运行容器

## 二、创建 Pipeline 任务 

多打开一个 Jenkins 页面，打开管理凭据页面

![image-20220809213051896](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809213051896.png)

（图片说明：点击打开管理凭据管理界面）

![image-20220809213231368](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809213231368.png)

（图片说明：拿到Github 的唯一标识，稍后要用到）



![image-20220816223450926](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816223450926.png)

(图片说明：创建一个 pipeline任务)



![image-20220816223519949](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816223519949.png)

（图片说明：配置 Github 项目路径，其实可以省去的，只是我恰巧填了，就写下来了哈哈）

## 三、脚本的总体框架

我是一步一步的去完善的，也是一步一步测试的，为了能让大家更清晰一些，不要嫌我如此啰嗦。

脚本文件大致是这样的，我们之后会一步一步进行填充

```bash
pipeline {
    agent any
    stages {
        stage('拉取代码') {
            steps {
             echo '拉取代码'
            }
        }
        stage('编译构建') {
            steps {
             echo '编译构建'
            }
        }
         stage('项目部署') {
              steps {
                echo '项目部署'
            }
        }
    }
}
```

**Node**：节点，一个 Node 就是一个 Jenkins 节点，Master 或者 Agent，是执行 Step 的具体运行 环境，后续讲到Jenkins的Master-Slave架构的时候用到。

**Stage**：阶段，一个 Pipeline 可以划分为若干个 Stage，每个 Stage 代表一组操作，比如： Build、Test、Deploy，Stage 是一个逻辑分组的概念。 

**Step**：步骤，Step 是最基本的操作单元，可以是打印一句话，也可以是构建一个 Docker 镜像， 由各类 Jenkins 插件提供，比如命令：sh ‘make’，就相当于我们平时 shell 终端中执行 make 命令 一样。

---

先说说这个代码该放在那里：在配置任务界面划到最底部

![image-20220816223604607](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816223604607.png)

本篇文章是基于这种方式的。

`为啥呢`？

**因为简单，咱们学的时候，就是从浅至深，因为这样在这个学习的过程中，它能给你正向反馈，不会直接开始就被劝退了**~

如果是想要了解Jenkins 放在源码文件中的那种方式，可能需要等到下一篇文章。

这个简单的脚本文件的结果应该如下：

![image-20220809221426233](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221426233.png)



## 四、Git Pull Code

我们做第一步，就是编写一个Jenkins 脚本，拉取 Github 上的代码。

```bash
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
              git branch: 'main', credentialsId: 'b6013989-c0b0-4511111111f7', url: 'https://github.com/ningzaichun/hello-springboot'
           }
        }
        stage('编译构建') {
            steps {
             echo '编译构建'
            }
        }
         stage('项目部署') {
              steps {
                echo '项目部署'
            }
        }
    }
}
```



这样的执行结果应当如下：

![image-20220816224709702](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816224709702.png)



相关日志说明：

![image-20220816224736589](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816224736589.png)

但其实这一栏还有许多可以配置的东西，但是目前没用上，就暂且不说，等到后来用上，再去查文档和检索。

## 五、Maven 编译

我们在上一个步骤中增加一个步骤，即增加一个 `stage(){}`

最简单的编译构建方式莫过于`maven clean package` 哈哈

```bash
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
              git branch: 'main', credentialsId: 'b6013989-c0b0-11-b11133bf7', url: 'https://github.com/ningzaichun/hello-springboot'
           }
        }
       stage('编译构建') {
            steps {
                sh label: '', script: 'mvn clean package'
            }
        }
         stage('项目部署') {
              steps {
                echo '项目部署'
            }
        }
    }
}
```

执行结果应当如下：

![image-20220816225613762](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816225613762.png)



在日志中也是可以看到我们已经构建成功啦

![image-20220816225813696](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816225813696.png)



## 六、打包镜像，发布至镜像仓库

编译成功后，就是打包成镜像

在helloSpringBoot的项目中，我有编写了一个十分简单的Dockerfile文件的，如下：

```dockerfile
FROM java:8

WORKDIR app

COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
```

我们可以利用这个Dockerfile 来进行Docker打包

docker 一些基础命令，我就不再提起了，

如果有不太了解Dockerfile编写的小伙伴，可以看看这篇文章☞：[Dockerfile](https://juejin.cn/post/7025763722060627981)

如果是对docker 一些命令不是非常熟悉的小伙伴，可以看这篇☞：[Docker 基本命令](https://juejin.cn/post/6990919004730687495)

```bash
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
              git branch: 'main', credentialsId: 'b6013111113bf7', url: 'https://github.com/ningzaichun/hello-springboot'
           }
        }
       stage('编译构建') {
            steps {
                sh label: '', script: 'mvn clean package'
            }
        }
         stage('清除旧镜像及容器') {
              steps {
                 //定义镜像名称
            	// 构建之前，需要先删除原有的镜像
				sh "docker rm -f hellonzc"
				sh "docker rmi -f hellospringboot:v0.0.1"
				sh "docker rmi -f registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
            }
        }
          stage('构建与发布镜像') {
              steps {
   //编译，构建本地镜像
                sh "mvn clean package"
                sh "docker build -t hellospringboot:v0.0.1 . "
                // 登录阿里云镜像
                sh "docker login --username=nzc1115 --password=1111111 registry-vpc.cn-heyuan.aliyuncs.com"
                sh " docker tag hellospringboot:v0.0.1 registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
                //发布镜像
                sh " docker push registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
            }
        }
    }
}
```

执行结果：

![image-20220816232326906](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816232326906.png)

相关日志：

![image-20220816232357020](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816232357020.png)



## 七、拉取镜像，运行容器

就是再多个步骤罢了~

```bash
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
              git branch: 'main', credentialsId: 'b60111111133bf7', url: 'https://github.com/ningzaichun/hello-springboot'
           }
        }
       stage('编译构建') {
            steps {
                sh label: '', script: 'mvn clean package'
            }
        }
         stage('清除旧镜像及容器') {
              steps {
                 //定义镜像名称
            	// 构建之前，需要先删除原有的镜像
				sh "docker rm -f hellonzc"
				sh "docker rmi -f hellospringboot:v0.0.1"
            }
        }
          stage('构建与发布镜像') {
              steps {
   //编译，构建本地镜像
                sh "mvn clean package"
                sh "docker build -t hellospringboot:v0.0.1 . "
                // 登录阿里云镜像
                sh "docker login --username=nzc15 --password=11113 registry-vpc.cn-heyuan.aliyuncs.com"
                sh " docker tag hellospringboot:v0.0.1 registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
                //发布镜像
                sh " docker push registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
            }
        }
        
         stage('运行容器') {
              steps {
			sh "docker rmi -f registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
         sh " docker pull registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
         sh "docker run -d -p 8080:8080 --name hellonzc registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
            }
        }
    }
}
```

**补充**：

关于脚本中构建好本地镜像，在拉取前又删除本地镜像，再拉取的矛盾问题，

- 其实我就是将命令效果全部展示出来，
- 另一方面就是如果是要部署到其他服务器上的话，
- 这条命令再加上另一台服务器的信息，就是可以用上的啦。

执行结果：

![image-20220816233428247](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816233428247.png)



相关日志：

![image-20220816233825189](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220816233825189.png)







**小结**：

其实如果是看过我那篇Maven部署的朋友，应该能够感受到，在这篇文章中，好像pipeline好像和那篇文章差不多，可能这篇还更显繁琐。

我要说明，这篇文章顶多是pipeline流水线任务的入门篇，看一眼大概，知道有pipeline任务，它的优势更多的体现在多任务构建，微服务构建，还有源码存储jenkins Pipeline  脚本文件。

以单体项目来执行，可能没法完全体现它的优势，并且我也没有弄它的最佳实践，所以**可能看完会略显失望**。

慢慢来啦~，这个东东详细说起来，也没有大家想象的那么简短

## 后记

> 首先还是要对之前到这个系列文章的小伙伴，说一句抱歉，非常不好意思。
>
> **因为我个人的疏忽错误，导致对照着前面文章安装环境的小伙伴，来看这篇文章进行实操时，仍然需要重新搭建一遍实操环境**。

**对此感到十分抱歉，希望各位读者能够见谅，前文我也已经修正错误，同时也希望大家继续支持这个专栏，给予我一些正向反馈**。

做的不好的地方也请各位读者、朋友、小伙伴能够及时指出，非常感谢各位！

> 如存有疑问，也请大家友善留言，我会在看到的第一时间尽快回复你。



写于 2022 年 8 月 16 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559)





















