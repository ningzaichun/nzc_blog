# Jenkins Pipeline 流水线任务~

> **上次其实已经写过一篇☞ [关于 Jenkins Pipeline 流水线任务的文章](https://juejin.cn/post/7132503611786919972)** ，说起来那篇真是比较简陋，只是简单用一个小案例来说明 Jenkins Pipeline 任务的存在吧，但完全没办法去体现那个优势，并且是在一台服务器上进行操作，文中的小案例甚至都**有些显得不太妥当**~

**本文适合的读者**：

1、想要了解Jenkins 的朋友们

2、想要扩展知识面的朋友们

3、对Jenkins有过一点点了解的小伙伴

4、对Liunx有过了解的小伙伴~

5、在追此系列文章的朋友们，

写明的原因，主要是以免浪费大伙的时间~

---

所以昨天找朋友又借来了一台服务器，两台还是比一台好玩一些~，

还有[游戏圈圈主&神转折评审官-Ylimhs](https://juejin.cn/user/2999123452115005)，打算支援我一下下，哈哈，我给他记小本子上啦。

大家多多参与沸点活动噢~

不过我还是先坚持写到后期再说吧~好难写啊

## 前言

上次我在文章最后说了，有一个非常矛盾的操作，就是删了镜像又再拉镜像...

不得不说，在一台服务器确实有点点憨~

---

先说说今天的目标

1. 将上篇文章的一些操作完善，Jenkins 服务器位于我自己的服务器上，镜像的存储仓库使用阿里云镜像，最后部署的仓库又是另一台服务器。

   ![image-20220818205429231](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818205429231.png)

2. 使用 `Pipeline Script from SCM` 方式，即将上次的脚本文件从任务配置中，改为放在源代码仓库中。

---

原本是有第二个目标的，但是出现了一些意外状况，导致我没写完，要放个鸽子🕊了，请各位见谅见谅👨‍💻

## 完善上一篇的部署脚本

[上一篇文章链接](https://juejin.cn/post/7132503611786919972)

上篇最后部署的脚本文件如下：

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

### 1.1、准备环境

现在我们要将原本部署在同 Jenkins 在一起的服务器上的服务，部署到另一台服务器上去。

- 首先就是要先在Jenkins 中添加另外的那台部署服务器的凭据（登录的账号密码）
- 配置远程服务器的相关信息

其实这一步我有想过要不要省略的...，下次吧，下次我把过于简单的东西省去吧，还是担心会有不了解的小伙伴，所以还是写的繁琐了些，就当看着我操作了遍吧。

![image-20220818210238534](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818210238534.png)

![image-20220818210302073](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818210302073.png)

![image-20220818210319018](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818210319018.png)

（图片说明：填入相关信息即可）



配置远程服务器信息，在全局配置中找到下面的两个配置

- **SSH remote hosts**
- Publish over SSH

![image-20220818210521406](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818210521406.png)

SSH remote hosts

![image-20220818211958823](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818211958823.png)

Publish over SSH 下的 SSH Servers

我这里是点击是新增：填写好相关信息就好。

![image-20220818212151522](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818212151522.png)

（图片说明：记得点击保存）

`补充`:这里的服务器名称是要用到的，记得记一下~

### 1.2、完善脚本

其实只要将最后一步的操作用SSH连接发送到部署服务器就可以了。

就是在另一台服务器中，进行最后部署的流程。

这里其实也是利用了 `Publish over SSH` 插件功能来实现的~，和 Maven 的机制是一样的。

```bash
pipeline {
    agent any
    stages {
         stage('部署服务') {
              steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'heyajunServer',
                            transfers: [ sshTransfer (
                                            cleanRemote: false,
                                            excludes: '',
                                            execCommand:"/home/ningzaichun/jenkins/deploy.sh",
                                            execTimeout: 120000,
                                            flatten: false,
                                            makeEmptyDirs: false,
                                            noDefaultExcludes: false,
                                            patternSeparator: '[, ]',
                                            remoteDirectory: '',
                                            remoteDirectorySDF: false,
                                            removePrefix: '',
                                            sourceFiles: '')
                                         ],
                            usePromotionTimestamp: false,
                            useWorkspaceInPromotion: false,
                            verbose: false
                       )
                  ]
              )
            }
        }
    }
}
```

其实就是将之前的可视化中的一些操作，变成了键值对放到了这里

`configName:这里填写的是之前配置服务器时所写的name`，

最重要的一点就是`execCommand`，这里就是用来执行shell命令的，其实这里是可以执行一些shell命令的，但是实在太过于繁琐，就写成了 shell 文件用来执行。

---

然后在`/home/ningzaichun/jenkins/`目录下编写了一个部署的 shell 脚本，名字为`deploy.sh`

```shell
#! /bin/sh

#查询容器是否存在，存在则删除
containerId=`docker ps -a | grep -w  hellonzc | awk '{print $1}'`

if [ "$containerId" !=  "" ] ; then
    #停掉容器
    docker stop $containerId
    #删除容器
    docker rm $containerId
    
    # 强制删除容器命令 docker rmi [容器名|Id]
	
	echo "成功删除容器"
fi

imageName='registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1'

#查询镜像是否存在，存在则删除
imageId=`docker images | grep -w ${imageName}  | awk '{print $3}'`
if [ "$imageId" !=  "" ] ; then
      
    #删除镜像
    docker rmi -f $imageId
	
	echo "成功删除镜像"
fi

# 登录阿里云镜像仓库
docker login --username=xxxxxx --password='xxxxx' registry.cn-heyuan.aliyuncs.com

# 下载镜像
docker pull registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1

# 启动容器
docker run -d -p 8080:8080 --name hellonzc registry.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1

echo "容器启动成功"
```

编写完之后要记得给`deploy.sh`文件授权，不然是执行不了，`chmod +x ./deploy.sh `

编写完之后，最好先测试一遍：

![image-20220818233304672](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818233304672.png)

脚本仅供参考，不咋会写 shell 脚本.....别让我去祸害大伙拉



完整脚本文件：

```groovy
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
              git branch: 'main', credentialsId: 'b6013989-c0b0-4545-bdf6-6a6ca5e33bf7', url: 'https://github.com/ningzaichun/hello-springboot'
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
                sh "docker login --username=wyh_lxb17670930115 --password=wangyihui123@ registry-vpc.cn-heyuan.aliyuncs.com"
                sh " docker tag hellospringboot:v0.0.1 registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
                //发布镜像
                sh " docker push registry-vpc.cn-heyuan.aliyuncs.com/ningzaichun/jenkins:v0.0.1"
            }
        }

         stage('运行容器') {
              steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'heyajunServer',
                            transfers: [ sshTransfer (
                                            cleanRemote: false,
                                            excludes: '',
                                            execCommand:"/home/ningzaichun/jenkins/deploy.sh",
                                            execTimeout: 120000,
                                            flatten: false,
                                            makeEmptyDirs: false,
                                            noDefaultExcludes: false,
                                            patternSeparator: '[, ]',
                                            remoteDirectory: '',
                                            remoteDirectorySDF: false,
                                            removePrefix: '',
                                            sourceFiles: '')
                                         ],
                            usePromotionTimestamp: false,
                            useWorkspaceInPromotion: false,
                            verbose: false
                       )
                  ]
              )
            }
        }
    }
}
```

测试结果：

![image-20220818233646329](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220818233646329.png)

### 1.3、小结

完善了上一篇的矛盾操作，但同时也埋下了更多的坑~

1. 如果要部署到多台机器，每台机器都要有一个 `deploy.sh `文件，不符合偷懒的原则，不过这个不是我的重点，因为liunx着实菜了点...
2. `deploy.sh`里面中的项目名称、镜像名称都是我手动写死的，这是不合适的，应当是动态的，在命令后传输进去的，如此一个`deploy.sh`可以达到复用的效果
3. Jenkinfile 脚本文件不应该在配置中管理，一方面不好编写，另一方面不好管理，每次都要点进任务配置中进行修改，是不符合偷懒的原则的。



后面继续写的话，可能会关注到的点

1. 上面的问题
2. 参数化构建
3. 构建器方面的问题
4. 项目构建依赖
5. springcloud的项目构建
6. ..... 

还是省略号好，能给大伙带来无限想象~

## 后记

> 各位读者老爷们，原谅我今天的文章，这篇文章确实没有达标这个专栏的文章质量，写糙了。
>
> **这周休，我一定努力不躺平**！！！



写于 2022 年 8 月 18 日晚，作者：宁在春

