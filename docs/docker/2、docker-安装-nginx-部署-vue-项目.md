#  Docker 安装 Nginx 部署前端项目

> 本文算是我编写 Docker + Jenkins 自动化部署专栏的开篇文吧
>
> 虽然看起来和Jenkins没啥关系~，但是后续还是会用到Nginx部署前端项目的。
>
> 很久没有尝试连续写一整个系列了，希望此专栏能帮助到一些小伙伴们 😃


## 一、关于 Nginx 服务器

其实说到web服务器，著名的有 Apache 、Apache下的顶级项目 Tomcat、微软的 IIS、以及我们今天学的的Nginx。

不同服务器的侧重点是不同的，像Tomcat就是一个开放源代码、运行servlet和JSP Web应用软件的基于Java的Web应用软件容器。而Nginx是一款轻量级的Web 服务器/反向代理反向代理服务器及电子邮件（IMAP/POP3）代理服务器，在BSD-like 协议下发行。其特点是占有内存少，并发能力强，事实上nginx的并发能力在同类型的网页服务器中表现较好。

他的介绍也代表了他的应用性广，总结起来，Nginx 的应用场景或者能够实现的功能大致如以下几点：

0.  正向代理
0.  反向代理
0.  负载均衡
0.  动静分离
0.  静态资源web服务器

而我们今天主要利用到的就是 第五点将 Nginx 作为一个静态资源 Web服务器。

## 二、Docker 安装 Nginx

搜索镜像命令，会列出所有的可下载的镜像

```
 docker search nginx
```


![image-20220721203433290.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/80cd20487770406cb1987282d00ac75d~tplv-k3u1fbpfcp-watermark.image?)

拉取镜像

```
 docker pull nginx # 不加版本号 默认拉取最新版
 docker pull nginx:[tag] # : 号 后面就可以加版本号 如 nginx:1.16.1 这种，但需要确定它是存在过的版本号才可以。
```

学习的话，一般直接上手最新，不行了再降。

![image-20220721203817272.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e1794aad608b4111bee7221db351dec5~tplv-k3u1fbpfcp-watermark.image?)

---

`注意`:这里有一个需要注意的点，Nginx 一般是根据配置文件启动的。

如果我们在第一次启动的时候就挂载目录，那么因为我们宿主机是空文件，会直接导致 Nginx 容器内的配置文件被覆盖，致使启动失败。

所以的步骤如下：

### 1、宿主机创建好要挂载的目录

```
 mkdir -p /home/nginx/
 mkdir -p /home/nginx/logs  
 mkdir -p /home/nginx/html 
```

`-p` 参数的作用就是允许创建多级目录

### 2、启动一个不挂载的容器

```
 docker run -d --name nzc-nginx  -p 80:80 nginx
```

![image-20220721205223050.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/421f4c807bd8485fa49bdc6acb419fbb~tplv-k3u1fbpfcp-watermark.image?)

为了让大家更进一步理解 Nginx 目录结构，我们用命令进入 Nginx 容器

```
 docker exec -it nzc-nginx bash
```

`-it` 以交互式进入容器 ，`bash`保留为容器终端的输入形式，所以结合起来就是进入容器终端并且的保留为容器终端的输入形式(-it和bash的结合作用)

![image-20220721210045411.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/49f42e1c008e45078229c85040f54838~tplv-k3u1fbpfcp-watermark.image?)

`/etc/nginx/nginx.conf`是nginx的主配置文件，具体内容留在后一章节再说吧。

`/etc/nginx/conf.d`下的default.conf 就是默认 server 配置

### 3、从容器中把配置文件复制出来

退出容器的终端，直接在终端里输入 exit 即可。

```
 docker cp nzc-nginx:/etc/nginx/nginx.conf /home/nginx/nginx.conf
 docker cp nzc-nginx:/etc/nginx/conf.d /home/nginx/
 docker cp nzc-nginx:/usr/share/nginx/html /home/nginx/ #此处就是网站站点目录
```

![image-20220721211251296.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6379169667b84de1a95140bc0e6e50a1~tplv-k3u1fbpfcp-watermark.image?)


### 4、暂停、删除容器

查看所有正在运行的容器

```
 docker ps
 docker ps -a #查看所有容器
```

![image-20220721211348733.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/22b1b775a13f4f73bd7e1195eaa6c248~tplv-k3u1fbpfcp-watermark.image?)



暂停、删除容器

```
 docker stop nzc-nginx # nzc-nginx 容器| 容器ID 也可以，只需要前3位数字即可
 docker rm nzc-nginx
 docker rm -f nzc-nginx #直接删除正在运行的容器
```

![image-20220721212521238.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0cdd9a9102bb4e08a2a25431d8bc3b2f~tplv-k3u1fbpfcp-watermark.image?)




### 5、重新启动一个挂载目录的容器

```
 docker run \
 -p 80:80 \
 --name nzc-nginx \
 -v /home/nginx/nginx.conf:/etc/nginx/nginx.conf \
 -v /home/nginx/conf.d:/etc/nginx/conf.d \
 -v /home/nginx/logs:/var/log/nginx \
 -v /home/nginx/html:/usr/share/nginx/html \
 -d nginx:latest
```

![image-20220721212550737.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0072b8bb0bc54c10ad5e2fbdfec54aa1~tplv-k3u1fbpfcp-watermark.image?)



测试：可以成功访问就是成功启动啦。

![image-20220721212606636.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b4f36f3b2ade49ff8295bc657668e61a~tplv-k3u1fbpfcp-watermark.image?)



如果需要每次都自启动的话，可以加上 ``

## 三、Nginx 配置文件讲解

本小章节只是针对与项目有关联配置文件进行一番简单的讲解，更详细的可能就需要大家去找找其他创作者所写的文章啦。望大家见谅

我们先看看之前上文提了一嘴的主配置文件：

nginx.conf

```
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

```
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
     #location ~ .php$ {
     #    proxy_pass   http://127.0.0.1;
     #}
 
     # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
     #
     #location ~ .php$ {
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
     #location ~ /.ht {
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

```
 docker restart nzc-nginx
```

我这里前端是 Vue 项目，打包相信大家都会打包吧~

没有vue项目，其实扔个 html 页面上去测试也可以的，莫慌。

**可能会出现的错误**

打包的时候可能会出现静态资源访问不到的错误。

把 vue.config.js文件中的 `publicPath` 改成 `./`

![image-20220721222053617.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2d3acd5b05584556a783332cc483cb22~tplv-k3u1fbpfcp-watermark.image?)



大家记得创建一下目录，别忘啦。
![image-20220721225717565.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aa11a6524dc743ff84cafe639a251718~tplv-k3u1fbpfcp-watermark.image?)



其实上传到指定目录后，就可以直接访问啦，不然看我。

测试：

![image-20220721230000749.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9ff4e7c4cd584314bf1c70e97198ed53~tplv-k3u1fbpfcp-watermark.image?)

或者直接 curl 测试也可

![image-20220721230058278.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f80be15675c846ecac3cf6d7b3275561~tplv-k3u1fbpfcp-watermark.image?)
今天第一天结束啦~~

## 五、总结

相信大家对于 Docker 怎么玩、Nginx 是什么样子大致应该是有所了解了吧，希望大家有所收获，我们一起加油！

Nginx 可以做的事情有很多，很好玩的，感兴趣的可以多试试，也可以试着自己记录记录！！

明天继续~~
