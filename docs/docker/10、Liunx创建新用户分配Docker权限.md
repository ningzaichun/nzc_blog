# Liunx 创建新用户 | 分配运行Docker权限

> 最近在写 Jenkins 专栏的文章，从一开始没人看，到上了热榜，越来越多的朋友看到，同时也收到很多正向反馈，就想把他写得更加完善一点，希望能够帮助到更多的朋友。

**昨晚在写 Jenkins Pipeline 任务时，还是感觉到了一台服务器的窘迫，一些最佳实践没法用案例复现出来**。

---

就找朋友借台服务器来用一段时间~，今天就是用他的服务器给我创建个用户，分配一下权限。

## 一、Liunx 创建新用户

创建一个新用户 `ningzaichun` 是新用户名

```bash
sudo useradd -m ningzaichun
```

参数 `-m` ：自动建立用户的登入目录。加上 `-m`就是会自动在 `/home`文件夹下自动创建一个`ningzaichun`用户文件夹

![image-20220817223656121](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817223656121.png)

（图片说明：执行完无返回结果，正常现象）

---

设置新用户的密码

```bash
sudo passwd ningzaichun
```

说明：这条命令的意思就是给`ningzaichun`用户设置密码，密码的设定记住不要含有**用户名**，否则会设置失败，我之前就是不想记密码，就想将密码设置的和密码一模一样。

![image-20220817223913402](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817223913402.png)

（图片说明：设置密码，两遍密码需要一致，出现 `successfully`即是成功）

## 二、分配用户权限

给创建出来的新用户分配执行Docker命令的权限，因为我暂时只需要这个权限~

---

1、如果没有docker用户组的话，先要创建docker用户组

```bash
sudo groupadd docker
```

![image-20220817224257053](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817224257053.png)

（图片说明：出现这个就是说已经存在了，就不用管了）

2、将用户添加进docker组中

```bash
sudo gpasswd -a ${USER} docker #${USER} 变量就是当前登录用户
sudo gpasswd -a ningzaichun docker #写明白 则是将写明的这个用户加入组中
```

![image-20220817224550404](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817224550404.png)

3、查看docker组下的所有用户

```bash
grep 'docker' /etc/group # 将 docker 换成其他的，就是查看其他的组下的用户
```

![image-20220817224647390](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817224647390.png)

（图片说明：出现你添加的用户即是添加成功）



## 三、切换用户测试

到这里就不再那么顺利~，又是学废新知识的一天

先说个关乎我自己的前提，我非常依赖 Docker，这在一定程度上影响了我并非是十分懂 liunx的人，你甚至可以把我理解为 liunx 中的小白。

---

我一开始用的切换用户命令为：`su ningzaichun`，从root 用户切换到其他用户，是无需密码的。

然后就出现了下面的错误：

![image-20220817225340309](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817225340309.png)

错误`ls: cannot open directory '.': Permission denied`，说是拒绝访问。

我就感到非常奇怪，因为这是个最基础的命令了，一开始还以为是权限分配的问题，谁知道了就是没加 `-`。

将命令改为   `su - ningzaichun`   或者    `su -l ningzaichun` 即可以正常使用。

![image-20220817230028942](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817230028942.png)

![image-20220817230035175](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817230035175.png)

出现上面那个错误的原因是因为在切换用户的时候，没有切换环境变量造成的。

- `-`：当前用户不仅切换为指定用户的身份，同时所用的工作环境也切换为此用户的环境（包括 PATH 变量、MAIL 变量等），使用 - 选项可省略用户名，默认会切换为 root 用户。
- `-l`：同 - 的使用类似，也就是在切换用户身份的同时，完整切换工作环境，但后面需要添加欲切换的使用者账号。 -- 来自于网上

然后测试在当前用户下，执行 docker 命令是成功的

![image-20220817230216992](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817230216992.png)

（图片说明：`docker images` 查看所有镜像）

你以为到这里就结束了，还没有勒，我们只是创建了用户，但还是要测试一下远程的ssh连接成不成功，毕竟之后的操作都是通过ssh远程连接来实现的。

---

## 四、远程连接

我先说，家人们，我又学到新知识了，知道一个配置文件的存在啦。

---

我在windows机器上直接拿 `ssh ningzaichun@IP地址` 的时候，出现了这样的场景：

```bash
The authenticity of host ' ' can't be established.
ECDSA key fingerprint is SHA256:.
Are you sure you want to continue connecting (yes/no/[fingerprint])?
```

输完 `yes `后就被拒绝连接了.

因为输完`yes`了，IP被远程服务器记录， 没法复现出来截图给大家看了,

之后再尝试,就是下面这样的啦

![image-20220817230832699](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817230832699.png)

```bash
ningzaichun@xxxxxxxx: Permission denied (publickey,gssapi-keyex,gssapi-with-mic).
```

大伙遇到想了解去查一下就知道了,我直接给解决方法了.

---

这个牵扯到的是SSH主机的验证，网上有两种方案，一种是直接在配置文件中去掉验证，还有一种就是允许用户远程登录，我用的是第二种，第一种有安全问题，还是不合适。

在root用户下，编辑`/etc/ssh/sshd_config`文件

```bash
vi /etc/ssh/sshd_config
```

在最后一行加入

```bash
AllowUsers ningzaichun
# 另外将最后一行的 PasswordAuthentication 的配置项改为 yes ，允许密码登录
```

 即可。（insert 进入编辑模式，退出 按 esc ，再按 `:`，输入wq即可保存退出）

![image-20220817232402824](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817232402824.png)



最后输入下面命令，重启 ssh 服务。

```bash
service sshd restart
```

![image-20220817232423866](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817232423866.png)

最后再来测试远程连接：

![image-20220817232504466](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817232504466.png)

---

最后完结撒花~

**删除用户**

```bash
userdel -r ningzaichun
```

参数 -r 递归删除

提一嘴：如果不完全删除用户信息，下次再创建相同用户的账号时会出现报错~

## 后记

> 希望自己能做一些有意义的事情，我将自己的知识分享，你觉得有收获，给予我正向的反馈，我收到你的反馈，开心的起飞，这又是一个好的开始哈哈~

你要热爱生活，生活才能热爱你丫。

文章内容不多，照片来凑~👩‍💻

![image-20220817233515580](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220817233515580.png)

写于 2022 年 8 月 17 日晚，作者：宁在春