# Jenkins的参数化构建

>放一个小招数，周末躺平，写篇实用的小短文~

我们平时开发的时候，代码仓库中可能不止存在一条分支，可能是多条分支进行开发的，那么我们直接固定的将分支名写在代码中是不合适的。

Jenkins 本身是支持参数化构建的，就让我们一起看看吧~

## 一、准备工作

还是以前的项目，这次我们推送一个新的分支上去~

```bash
git checkout -b v1  # 创建 v1分支并切换到此分支
git branch # 查看本地分支，并标记当前分支
git add . #提交所有改动
git commit -m "xxx" #提交到本地仓库
git push origin v1 #提交到远程仓库分支v1，v1不存在时会自动创建
```



![image-20220820215836313](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820215836313.png)

![image-20220820215845807](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820215845807.png)

![image-20220820220212909](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820220212909.png)

之后在github上也可以看到出现v1分支啦~



## 二、修改 Jenkins 任务配置

### 2.1、添加参数配置

其实Jenkins 中是支持多种参数类型的，下图便可看出

![image-20220820222522224](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820222522224.png)

我们选择添加一个 `String Paramter`参数

![image-20220820222724573](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820222724573.png)

（图片说明：照着填写即可~）



### 2.2、修改构建脚本

要让构建脚本接收外部输入的参数，其实也就只用修改配置文件拉取代码的模块，将分支名称变为动态的。

```groovy
pipeline {
    agent any
    stages {
       stage('git pull 拉取代码') {
            steps {
              // Get some code from a GitHub repository
                git branch: '${branch}, credentialsId: 'xxxxxxx', url: 'https://github.com/ningzaichun/hello-springboot'
           }
        }
    }
}
```



### 2.3、点击构建

之前是点击构建即会立马触发构建，现在添加参数构建后，会让你输入要构建的分支昵称。

![image-20220820225918210](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820225918210.png)

构建结果：

![image-20220820230024579](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220820230024579.png)

这都是可行的。

当然还有其他的参数类型，大家可以一一去尝试~

## 后记

> 原谅我这个周末躺平的小伙子吧~
>
> 这该死的夏天都快把人烤焦了，动不动四十度，简直要疯了~



写于 2022 年 8 月 20日晚，作者：宁在春