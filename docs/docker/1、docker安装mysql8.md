# Docker 安装 MySQL8.0

## 一、搜索拉取镜像

```
docker search mysql

docker pull mysql #不写版本号 默认拉取最新版本

docker pull mysql:8.0.21 #写版本号，则拉取确定的版本
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0786067c80ce4b949264edad0a9a7fc3~tplv-k3u1fbpfcp-zoom-1.image)

## 二、查看镜像

```
docker images
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9eab89183396403a9d2578af1324c7be~tplv-k3u1fbpfcp-zoom-1.image)

## 三、启动镜像

```
docker run  -d  \
--name mysql8 \
--privileged=true \
--restart=always \
-p 3310:3306 \
-v /home/mysql8/data:/var/lib/mysql \
-v /home/mysql8/config:/etc/mysql/conf.d  \
-v /home/mysql8/logs:/logs \
-e MYSQL_ROOT_PASSWORD=123456 \
-e TZ=Asia/Shanghai mysql \
--lower_case_table_names=1  
```

记住 `` 后面不能有空格，不然会提前执行命令，导致执行失败，已踩坑。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bac001ff55dc41968c86ad7aa329739f~tplv-k3u1fbpfcp-zoom-1.image)

命令看起来很长，但其实分析起来是非常简单的。

-   `--name mysql8`：给容器命名
-   `--privileged=true`：
-   `--restart=always`：服务器启动时，自启动
-   `-p 3310:3306`：端口映射，第一个 3310 是映射出去的端口，第二个 3306 是这个容器的端口
-   远程访问：IP 地址:3310 即可，如果是本地虚拟机就是 虚拟机 IP:3310
-   `注意`：服务器上记得打开安全组，虚拟机上则记得注意防火墙
-   `-v /home/mysql8/data:/var/lib/mysql`：文件挂载， **:** 前为宿主机的目录位置，后为容器内文件对应位置
-   `-e MYSQL_ROOT_PASSWORD=123456`：环境变量设置，此处是设置 ROOT 用户登录密码
-   `-e TZ=Asia/Shanghai mysql`：此处是设置 MySQL 的时区，请注意这点，有时候你可能会发现你的服务器时区和你当前的电脑的时区是不一样的，这很有可能有一些隐藏问题噢。此处的 `mysql`为镜像名。
-   `--lower_case_table_names=1`：让表名忽略大小写

补充：

```
--lower_case_table_names=1    :忽略大小写，docker mysql默认区分大小写的
```

```
注:参数顺序一定要对，--lower_case_table_names=1要加在镜像名后面，镜像名前面是参数，后面是mysql配置
```

查看MySQL官方文档，有记录：

> lower_case_table_names can only be configured when initializing the server. Changing the lower_case_table_names setting after the server is initialized is prohibited.

**另外只有在初始化的时候设置 lower_case_table_names=1 才有效**

## 四、检查启动情况

### 1.查看镜像

```
docker ps  # 查看所有正在运行的容器
docker ps -a  #-a 表示查看所有容器 包括未删除的历史容器
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d7fc54c11f9341ef8470303334353b3f~tplv-k3u1fbpfcp-zoom-1.image)

### 2.查看日志

```
docker logs [容器名|容器id] #查看容器的执行日志
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/01e50bbc189343428b6630a0283f59d0~tplv-k3u1fbpfcp-zoom-1.image)

查看实时日志

```
docker logs  [容器名|容器id] -f
```

具体的参数有几个：

```
--details        Show extra details provided to logs
-f, --follow     Follow log output
--since string   Show logs since timestamp (e.g. 2013-01-02T13:23:37Z) or relative (e.g. 42m for 42 minutes)
-n, --tail string    Number of lines to show from the end of the logs (default "all")
-t, --timestamps     Show timestamps
--until string   Show logs before a timestamp (e.g. 2013-01-02T13:23:37Z) or relative (e.g. 42m for 42 minutes)
```

```
-f / --follow 跟踪输出日志
--since  显示自时间戳(例如2013-01-02T13:23:37Z)或相对(例如42分钟为42m)开始的日志 
-- details 显示提供给日志的额外详细信息 
-n, --tail  从日志末尾开始显示的行数(默认为"all")
-t，-- timestamps 显示时间戳
--until 显示时间戳(如2013-01-02T13:23:37Z)或相对(如42分钟42m)之前的日志 
```

### 3.容器交互

像这种简单的，就是用来测试的，像是启动个 centos 的容器，进去还可以玩的更好。

```
docker exec -it [容器名|容器id] bash
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6b63545804d247af8aa4c58e1a00a1d4~tplv-k3u1fbpfcp-zoom-1.image)

以往在 mysql 5.0的时候，大家还会去说支持以下远程连接，但是在docker中，其实它是默认支持远程连接的。

所以其实可以不用执行下面的命令，也可以远程连接。

进入容器的目的是让mysql支持远程连接，

我在网上看到有些人说，默认支持远程连接，但是我测试了，行不通，还是需要通过以下命令修改：

```
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/97dee50bff6448978f9fd95d5eae5a88~tplv-k3u1fbpfcp-zoom-1.image)

```
flush privileges;
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/75037ba747ec48968954ed5a4a77e612~tplv-k3u1fbpfcp-zoom-1.image)

连接测试，注意我开放的端口就是3310，并非3306哈，记得开安全组规则。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/75b1c1ea1d9349d880a2e90f219784e9~tplv-k3u1fbpfcp-zoom-1.image)

### 4.遇到错误

```
[root@crush mysql8]# docker run -it -d --name=mysql8  -v /home/mysql8/data/:/var/lib/mysql -v /home/mysql8/config/:/etc/mysql/ -e MYSQL_ROOT_PASSWORD=123456 -e TZ=Asia/Shanghai mysql --lower_case_table_names=1
9bc3479694ce1356c81b5caba3dcafb85b25dc93abb50cca5a29b0172dba8adf
[root@crush mysql8]# docker logs 9bc
2022-07-14 13:24:37+08:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.29-1.el8 started.
2022-07-14 13:24:37+08:00 [ERROR] [Entrypoint]: mysqld failed while attempting to check config
  command was: mysqld --lower_case_table_names=1 --verbose --help --log-bin-index=/tmp/tmp.AYT8xnMNTv
  mysqld: Can't read dir of '/etc/mysql/conf.d/' (OS errno 2 - No such file or directory)
mysqld: [ERROR] Stopped processing the 'includedir' directive in file /etc/my.cnf at line 36.
mysqld: [ERROR] Fatal error in defaults handling. Program aborted!
```

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ea73533e592246f3b9ab07180ffbb1dd~tplv-k3u1fbpfcp-zoom-1.image)

原因因为 Centos7 安全给 linux禁止了一些安全权限，导致mysql和mariadb在进行挂载/var/lib/mysql的时候会提示如下信息。

解决方法在docker run中加入 `--privileged=true` 给容器加上特定权限。

***

其实还遇到一个小问题，但是没有找到产生的原因，解决的方式也很玄学......

出错了的话，就是暂停删除。

```
docker stop [容器名|容器ID]
docker rm [容器名|容器ID]
```

## 五、自言自语

今天想弄两个版本的mysql，准备再重新学习 mysql，所以就在服务器上整了个8.0的版本的。

这篇严格说起来算是复习篇。
