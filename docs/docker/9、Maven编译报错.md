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

```
JAVA_HOME
/var/jenkins_home/tools/hudson.model.JDK/jdk8
M2_HOME
/var/jenkins_home/tools/hudson.tasks.Maven_MavenInstallation/Maven3.6.3
PATH+EXTRA
$M2_HOME/bin
```



兄弟们，在这里我还犯了一个错，记得也要把jdk的环境变量配置一下。

都讲到这里了，咱们一起把 Jenkins 中的Maven的settings.xml文件改了吧。



> 保存完后，重复我们之前的 Maven 编译



![image-20220809225546548](https://s2.loli.net/2022/08/09/BKPUSaIMRHNtnqm.png)

（图片说明：图片编译结果）

查看日志也可以看到已经编译完成

![image-20220809225623328](https://s2.loli.net/2022/08/09/jPAMchrglB4a9zH.png)

