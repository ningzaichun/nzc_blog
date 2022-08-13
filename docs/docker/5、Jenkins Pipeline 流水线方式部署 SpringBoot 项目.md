# Jenkins Pipeline 流水线方式部署 SpringBoot 项目

>鸽了几天，又继续来写这个专栏啦~
>
>前面已经写过几篇文章来玩 Jenkins 啦，内容涵盖的范围为 [Docker 安装Jenkins、Jenkins 插件安装、系统配置、环境配置、Jenkins + Github 自动化部署Maven 项目](https://juejin.cn/post/7127302949797101604)、[自动化部署前端项目](https://juejin.cn/post/7127671229707714591)、以及[Jenkins 构建后发送邮件通知](https://juejin.cn/post/7128046538802069511)等。
>
>每一步都有明确的图片及说明，也确保按照实操可以得到确切的结果。

## 前言

> 写了前几篇文章，大家给予的反馈我都有一一收到，非常感谢大家的阅读，让我越发想要将要这个专栏写好，也希望让每一位阅读的朋友，有所收获。

如果大家看完前几篇文章，大家对 Jenkins 的基本使用肯定是没有问题的，如使用Maven方式部署一个SpringBoot项目，又或者是使用自由风格方式部署前端项目等。

今天继续聊到的是**使用流水线方式部署一个 SpringBoot 项目**，会从浅至深，一步步带领大家实操，以及说明其中为什么需要如此做，当然，如有疑问，大家留下评论即可，我会在看到的第一时间给予回复。



```

```



## 一、浅提一下 Pipeline 

浅提一下：

大伙整个干净的SpringBoot项目放到github上~，待会要用，没有的话，直接复刻我这个 [hello world](https://github.com/ningzaichun/hello-springboot)，也行。

Pipeline，简单来说，就是一套运行在 Jenkins 上的工作流框架，将原来独立运行于单个或者多个节点 的任务连接起来，实现单个任务难以完成的复杂流程编排和可视化的工作。 

2）使用Pipeline有以下好处（来自翻译自官方文档）：

 代码：Pipeline以代码的形式实现，通常被检入源代码控制，使团队能够编辑，审查和迭代其传送流 程。 持久：无论是计划内的还是计划外的服务器重启，Pipeline都是可恢复的。 可停止：Pipeline可接 收交互式输入，以确定是否继续执行Pipeline。 多功能：Pipeline支持现实世界中复杂的持续交付要 求。它支持fork/join、循环执行，并行执行任务的功能。 可扩展：Pipeline插件支持其DSL的自定义扩 展 ，以及与其他插件集成的多个选项。 

3）如何创建 Jenkins Pipeline呢？ Pipeline 脚本是由 Groovy 语言实现的，但是我们没必要单独去学习 Groovy Pipeline 支持两种语法：Declarative(声明式)和 Scripted Pipeline(脚本式)语法 Pipeline 也有两种创建方法：可以直接在 Jenkins 的 Web UI 界面中输入脚本；也可以通过创建一 个 Jenkinsfile 脚本文件放入项目源码库中（一般我们都推荐在 Jenkins 中直接从源代码控制(SCM) 中直接载入 Jenkinsfile Pipeline 这种方法）。

---

如果没有环境配置的话，可以看这篇文章：[Jenkins 安装、系统配置、环境配置](https://juejin.cn/post/7127302949797101604)

这篇文章的思路：我是将总的 Jenkins 脚本文件，一步一步拆出来讲解

1. 创建 Pipeline 任务
2. 从 Github 拉取代码
3. 使用 Maven 打包编译
4. 打包成镜像，发布到镜像仓库或私有仓库（这一步我省了，懒了）
5. 停止旧容器，删除旧版本镜像
6. 拉取镜像 （一样的被我省略啦）
7. 运行容器



## 二、创建 Pipeline 任务 

![image-20220809211252814](https://s2.loli.net/2022/08/09/rY2WJO1SB6T9hcj.png)





配置Github 路径：

![image-20220809212853410](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809212853410.png)





多打开一个 Jenkins 页面，打开管理凭据页面

![image-20220809213051896](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809213051896.png)

（图片说明：点击打开管理凭据管理界面）

![image-20220809213231368](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809213231368.png)

（图片说明：拿到Github 的唯一标识，稍后要用到）



## 三、Git Pull Code

我是一步一步的去完善的，也是一步一步测试的，为了能让大家更清晰一些，不要嫌我如此啰嗦。

我们做第一步，就是编写一个Jenkins 脚本，拉取 Github 上的代码。

脚本文件大致是这样的，我们一步一步进行填充

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

过程大致如下

![image-20220809221024896](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221024896.png)



Node：节点，一个 Node 就是一个 Jenkins 节点，Master 或者 Agent，是执行 Step 的具体运行 环境，后续讲到Jenkins的Master-Slave架构的时候用到。

 Stage：阶段，一个 Pipeline 可以划分为若干个 Stage，每个 Stage 代表一组操作，比如： Build、Test、Deploy，Stage 是一个逻辑分组的概念。 

Step：步骤，Step 是最基本的操作单元，可以是打印一句话，也可以是构建一个 Docker 镜像， 由各类 Jenkins 插件提供，比如命令：sh ‘make’，就相当于我们平时 shell 终端中执行 make 命令 一样。



我们先来尝试最简单的

```bash
node {
   
   stage('git pull 拉取代码') { 
      // Get some code from a GitHub repository
      git branch: 'main', credentialsId: 'a1d57d49e 上文刚刚提到的github的那个凭据的id', url: 'https://github.com/ningzaichun/hello-springboot'
   }

}
```



```bash
pipeline {
    agent any
    stages {
        stage('拉取代码') {
             steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']],
                doGenerateSubmoduleConfigurations: false, 
                extensions: [], 
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'a1d57d4c-3873-4920-9cb1-c99b0fea9e9e', url:'https://github.com/ningzaichun/hello-springboot']]
                ])
             }
        }
    }
}
```



执行结果：

![image-20220809221426233](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221426233.png)



相关日志

![image-20220809221514790](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221514790.png)



## 四、Maven 编译

我们在上一个步骤中增加一个步骤，即增加一个 `stage(){}`

```bash
pipeline {
    agent any
    stages {
        stage('拉取代码') {
             steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']],
                doGenerateSubmoduleConfigurations: false, 
                extensions: [], 
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'a1d57d4c-3873-4920-9cb1-c99b0fea9e9e', url:'https://github.com/ningzaichun/hello-springboot']]
                ])
             }
        }
       stage('编译构建') {
            steps {
                sh label: '', script: 'mvn clean package'
            }
        }
    }

}
```

修改配置之后，执行的时候报错了

![image-20220809221904148](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221904148.png)



查看日志信息：

![image-20220809221925824](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809221925824.png)

（图片说明：说Maven 没有找到）

### 4.1、Maven 报错解决

遇到报错不要慌，它说没有找到maven，我们让它能找到即可。

判断思路：

首先我先运行了我之前使用Maven任务构建的Springboot项目，发现是正常运行。

![image-20220809222446815](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809222446815.png)

那么证明我之前的下载的maven插件是正确的，那么在Jenkins 中实际上还是有Maven的存在的

构建日志：

![image-20220809222605256](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809222605256.png)



> 看到这个路径，突然想起环境变量这个东西，我就去找了全局系统配置，看看有没有这个选项

然后发现是有这个东西的

![image-20220809222726869](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809222726869.png)



填写之前，我打算先进入jenkins 容器中确认一下 Maven 的路径

```bash
docker exec -it [容器名|容器Id] bash
```

![image-20220809222955277](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809222955277.png)



我们 cd 到我们之前在日志中看到的那个 maven 目录去

![image-20220809223050648](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809223050648.png)

```bash
/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/Maven3.6.3
```

我们就拿到这个路径了，就可以开始环境变量的配置了。

![image-20220809225220906](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220809225220906.png)

（图片说明：兄弟们，记得点保存）

兄弟们，在这里我还犯了一个错，记得也要把jdk的环境变量配置一下。

都讲到这里了，咱们一起把 Jenkins 中的Maven的settings.xml文件改了吧。



> 保存完后，重复我们之前的 Maven 编译



![image-20220809225546548](https://s2.loli.net/2022/08/09/BKPUSaIMRHNtnqm.png)

（图片说明：图片编译结果）

查看日志也可以看到已经编译完成

![image-20220809225623328](https://s2.loli.net/2022/08/09/jPAMchrglB4a9zH.png)





## 五、打包镜像，运行容器

编译成功后，就是打包成镜像

在helloSpringBoot的项目中，我编写了一个十分简单的Dockerfile文件的，如下：

```dockerfile
FROM java:8

WORKDIR app

COPY target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
```





```bash
pipeline {
    agent any
    stages {
        stage('拉取代码') {
             steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']],
                doGenerateSubmoduleConfigurations: false, 
                extensions: [], 
                submoduleCfg: [],
                userRemoteConfigs: [[credentialsId: 'a1d57d4c-3873-4920-9cb1-c99b0fea9e9e', url:'https://github.com/ningzaichun/hello-springboot']]
                ])
             }
        }
        stage('编译，构建镜像') {
            steps {
                 //定义镜像名称
				def imageName="helloSpringBoot:v0.0.1"
				def containerName="helloSpringBoot"
            	// 构建之前，需要先删除原有的镜像
				sh "docker rmi -f ${containerName}"
				sh "docker rmi -f ${imageName}"
				
                //编译，构建本地镜像
                sh "mvn clean package"
                sh "docker build -t ${imageName} . "
                
                sh "docker run -d -p 8080:8080  . "
            }
        }
    }

}
```



写到这一步的时候，直接把心态写崩了~

拉取镜像 （一样的被我省略啦）





```bash
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCkbZReEaWPFpN7syboGDTTlcewaKPapIPp5dHmLcG6hWYLy8ne2E8GyLOFkk7z4lXdADxEeH+Zz0sjtp7oCZVQqfcs7uVSV6Ha2Sctju2Grt6QSl1lJkeEHdymUOsHFfdwXsvsMHqjiji3EnXsb4TrS3m1HrYY+HlceZBNewdevgMmnmPINtq1lKjzcg58AYC5zrohmDYOQcQAfIkNe9c9VVXxYmzlWMBZlC7Ur+GJUgxEJ59KT2furFpQA4vlKZV8t4KJN2FXf1FXy/RemmhxCLfA+UYyHckDak7PeEi9U1UbmznqOIW58a93NFNzydgElJHV87i+mKINaoiuVAc0/MY3QOyyChJ6O9w6U7jTI8/BOkksQ0kELbHBxSxawMxirbPG3TCbZ1PdGkWJE7B+7Dr6C/Zy1anzk8imCN6hhaKwmp4FFXPhNm533m72zuSJISWL21GeZEOSe7byVGb/LNqRV4xXX0uG5KSuJYDeyeLvLlCA1in0Z8pI+1a1c10= root
```



















