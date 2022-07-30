# Docker 安装 Nginx 部署前端项目

>本文算是我编写 Docker + Jenkins 自动化部署专栏的开篇文吧
>
>很久没有尝试连续写一整个系列了，希望此专栏能帮助到一些小伙伴们 😃

![image-20220721201843246](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721201843246.png)

## 一、关于 Nginx 服务器

其实说到web服务器，著名的有 Apache 、Apache下的顶级项目 Tomcat、微软的 IIS、以及我们今天学的的Nginx。

不同服务器的侧重点是不同的，像Tomcat就是一个开放源代码、运行servlet和JSP Web应用软件的基于Java的Web应用软件容器。而Nginx是一款轻量级的Web 服务器/反向代理反向代理服务器及电子邮件（IMAP/POP3）代理服务器，在BSD-like 协议下发行。其特点是占有内存少，并发能力强，事实上nginx的并发能力在同类型的网页服务器中表现较好。

他的介绍也代表了他的应用性广，总结起来，Nginx 的应用场景或者能够实现的功能大致如以下几点：

1. 正向代理
2. 反向代理
3. 负载均衡
4. 动静分离
5. 静态资源web服务器

而我们今天主要利用到的就是 第五点将 Nginx 作为一个静态资源 Web服务器。

## 二、Docker 安装 Nginx 

搜索镜像命令，会列出所有的可下载的镜像 

```shell
docker search nginx
```

![image-20220721203433290](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721203433290.png) 

拉取镜像

```bash
docker pull nginx # 不加版本号 默认拉取最新版
docker pull nginx:[tag] # : 号 后面就可以加版本号 如 nginx:1.16.1 这种，但需要确定它是存在过的版本号才可以。
```

学习的话，一般直接上手最新，不行了再降。

![image-20220721203817272](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721203817272.png)

---

`注意`:这里有一个需要注意的点，Nginx 一般是根据配置文件启动的。

如果我们在第一次启动的时候就挂载目录，那么因为我们宿主机是空文件，会直接导致 Nginx 容器内的配置文件被覆盖，致使启动失败。

所以的步骤如下：

### 1、宿主机创建好要挂载的目录

```bash
mkdir -p /home/nginx/
mkdir -p /home/nginx/logs  
mkdir -p /home/nginx/html 
```

`-p` 参数的作用就是允许创建多级目录

### 2、启动一个不挂载的容器

```bash
docker run -d --name nzc-nginx  -p 80:80 nginx
```

![image-20220721205223050](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721205223050.png)

为了让大家更进一步理解 Nginx 目录结构，我们用命令进入 Nginx 容器

```bash
docker exec -it nzc-nginx bash
```

`-it` 以交互式进入容器 ，`bash`保留为容器终端的输入形式，所以结合起来就是进入容器终端并且的保留为容器终端的输入形式(-it和bash的结合作用)

![image-20220721210045411](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721210045411.png)

`/etc/nginx/nginx.conf`是nginx的主配置文件，具体内容留在后一章节再说吧。

`/etc/nginx/conf.d`下的default.conf 就是默认 server 配置

### 3、从容器中把配置文件复制出来

退出容器的终端，直接在终端里输入 exit 即可。

```bash
docker cp nzc-nginx:/etc/nginx/nginx.conf /home/nginx/nginx.conf
docker cp nzc-nginx:/etc/nginx/conf.d /home/nginx/
docker cp nzc-nginx:/usr/share/nginx/html /home/nginx/ #此处就是网站站点目录
```

![image-20220721212521238](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721212521238.png)

### 4、暂停、删除容器

查看所有正在运行的容器

```bash
docker ps
docker ps -a #查看所有容器
```

![image-20220721211251296](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721211251296.png)



暂停、删除容器

```bash
docker stop nzc-nginx # nzc-nginx 容器| 容器ID 也可以，只需要前3位数字即可
docker rm nzc-nginx
docker rm -f nzc-nginx #直接删除正在运行的容器
```

![image-20220721211348733](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721211348733.png)



### 5、重新启动一个挂载目录的容器

```bash
docker run \
-p 80:80 \
--name nzc-nginx \
-v /home/nginx/nginx.conf:/etc/nginx/nginx.conf \
-v /home/nginx/conf.d:/etc/nginx/conf.d \
-v /home/nginx/logs:/var/log/nginx \
-v /home/nginx/html:/usr/share/nginx/html \
-d nginx:latest
```

![image-20220721212550737](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721212550737.png)

测试：可以成功访问就是成功启动啦。

![image-20220721212606636](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721212606636.png)

如果需要每次都自启动的话，可以加上 ``

## 三、Nginx 配置文件讲解

本小章节只是针对与项目有关联配置文件进行一番简单的讲解，更详细的可能就需要大家去找找其他创作者所写的文章啦。望大家见谅

我们先看看之前上文提了一嘴的主配置文件：

nginx.conf

```shell
user  nginx;
worker_processes  auto;
#  error_log 输出目录
error_log  /var/log/nginx/error.log notice;
pid        /var/run/nginx.pid;

events {
  # 单个工作进程可以允许同时建立外部连接的数量
    worker_connections  1024;
}
http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
    access_log  /var/log/nginx/access.log  main;
    sendfile        on;
    #tcp_nopush     on; 
    keepalive_timeout  65;  #连接存活时间

    #gzip  on;  支持传递压缩文件
 	#  nginx 配置文件中支持 include ，即支持多配置文件组合
    include /etc/nginx/conf.d/*.conf;
}
```

你可别小瞧这个文件，里面有不少设置的开关勒，不过这次不是写这里~~

继续来到 `default.conf`

```shell
server {
	# 这里就是表示监听的端口
    listen       80;
    listen  [::]:80;
    # 这里表示服务地址 写域名或者ip
    server_name  localhost;
    #access_log  /var/log/nginx/host.access.log  main;
    
    # 这里就是我们今天要接触的东西了 
    # / 表示的是 ip:port后面跟着的路径 / 就是 ip:port/
    # 如果是 /nzc 访问的时候就是  ip:port/nzc/
    #基于这个逻辑，我们就可以运行多个站点
    # 这里还可以写表达式、正则表达式等 
    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }
    #error_page  404              /404.html;

    # redirect server error pages to the static page /50x.html
    #错误页面转发
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }

    # 反向代理的例子
    # proxy the PHP scripts to Apache listening on 127.0.0.1:80
    #
    #location ~ \.php$ {
    #    proxy_pass   http://127.0.0.1;
    #}

    # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
    #
    #location ~ \.php$ {
    #    root           html;
    #    fastcgi_pass   127.0.0.1:9000;
    #    fastcgi_index  index.php;
    #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
    #    include        fastcgi_params;
    #}

    # deny access to .htaccess files, if Apache's document root
    # concurs with nginx's one
    #
    # 黑名单白名单功能
    #location ~ /\.ht {
    #    deny  all;
    #}
}
```

我们在 default.conf 中加上一个 location ,等会部署我们的项目

```
server {
    location  /nzc {
        # alias 后面跟着的是容器内部的目录，但是我们是挂载出来的，实际上我们放在宿主机相应的挂载目录下即可
        alias   /usr/share/nginx/html/www/blog/dist;
        # 这里的crush是我项目前缀
        index  index.html index.htm;
        try_files $uri $uri/  /nzc/index.html;
     }
}
```

关于这里的牵扯到的 [alias](http://nginx.org/en/docs/http/ngx_http_core_module.html?spm=a2c6h.12873639.article-detail.9.676f283cN66m0r#alias) 

也可以看看 [alias 与 root 区别](https://developer.aliyun.com/article/603563#:~:text=root%E4%B8%8Eali,%E5%88%B0%E6%9C%8D%E5%8A%A1%E5%99%A8%E6%96%87%E4%BB%B6%E4%B8%8A%E3%80%82)

## 四、部署前端项目

对了修改完 `nginx`配置文件，记得重启一下，不然不生效。

```bash
docker restart nzc-nginx
```

我这里前端是 Vue 项目，打包相信大家都会打包吧~

没有vue项目，其实扔个 html 页面上去测试也可以的，莫慌。

**可能会出现的错误**

打包的时候可能会出现静态资源访问不到的错误。

把 vue.config.js文件中的 `publicPath` 改成 `./`

![image-20220721225717565](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721225717565.png)

大家记得创建一下目录，别忘啦。

![image-20220721222053617](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721222053617.png)

其实上传到指定目录后，就可以直接访问啦，不然看我。	

测试：

![image-20220721230000749](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721230000749.png)

或者直接 curl 测试也可

![image-20220721230058278](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220721230058278.png)

今天第一天结束啦~~

## 五、总结

相信大家对于 Docker 怎么玩、Nginx 是什么样子大致应该是有所了解了吧，希望大家有所收获，我们一起加油！

Nginx 可以做的事情有很多，很好玩的，感兴趣的可以多试试，也可以试着自己记录记录！！

明天继续~~



















