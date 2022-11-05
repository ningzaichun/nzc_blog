# 重新审视自己定义的 spring-boot-stater

> 上次我写了一篇[SpringBoot SPI 机制和实现自定义 starter](https://juejin.cn/post/7132132528810852382)的文章， 当时考虑的十分不周到，现在看来里面还是存有些许问题的。
>
> **本文的由来，还是因为收到了我心目中的一名大佬的评论，也许对别人而言是这就是一个评论，其实在我心里，却把它当作了一道面试题，一场考试**。

## 前言

今天傍晚时，收到的大佬评论👨‍💻

![image-20220823235754803](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823235754803.png)

> 就第一反应而言我真的是十分开心的，因为真的看过 [和耳朵](https://juejin.cn/user/325111173878983) 大多数文章，首次关注是因为SpringSecurity专栏文章，写的真的特别好，文章数量不多，但是质量都非常高。

但是冷静下来再看待这个问题的时候，我有一种把它当作考试的感觉，十分想给出一份比较好的答卷，总之就是这种感觉，有一种离偶像很近的感觉，想表现自己，哈哈

---

大佬给的解答和回复：🤩

![image-20220824010140912](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220824010140912.png)

解题思路是一样的~，耶✌，开心ing

> 一定会继续努力的，在文字中找寻自己的光亮，也争取下一次在自己擅长的领域，做的更好些。
>
> **他日凌云，万事胜意**

## 一、问题

回归正题：还是看看评论内容☞

**那这个版本问题怎么处理？比如原项目是 springboot2.1，你的新jar包用了2.2会有问题吗**

按照意思解释成比较通俗的话就是：

> 我现在开发的一个项目，已经确定`SpringBoot版本为2.1`，但是我现在要引入一个第三方`jar`包，并且此第三方`jar`的`SpringBoot版本为2.2`，那么会产生问题吗？

我先告诉你答案：会报错。

### 1.1、测试问题

我将之前文章中的Demo项目中两个模块的SpringBoot版本分别调整为 2.1 和 2.2 版本。

![image-20220823222027866](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823222027866.png)

三个pom文件位置如上图，`app-server`（引入方）和 `commons`（stater）目前是各自管理各自的Maven依赖，父pom文件不再进行依赖的统一管理。

首先将`commons`的springboot版本调为2.2

![image-20220823222435280](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823222435280.png)

再将 `app-server`调整为2.1.6版本，

![image-20220823222456750](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823222456750.png)

测试结果：

![image-20220823222734613](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823222734613.png)

```bash
java.lang.NoSuchMethodError: org.springframework.core.type.AnnotationMetadata.introspect(Ljava/lang/Class;)Lorg/springframework/core/type/AnnotationMetadata;
at org.springframework.context.annotation.ConfigurationClassUtils.checkConfigurationClassCandidate(ConfigurationClassUtils.java:108)
	at org.springframework.context.annotation.ConfigurationClassPostProcessor.processConfigBeanDefinitions(ConfigurationClassPostProcessor.java:282)
```

如实说：我当时第一反应想到的是SpringBoot版本兼容问题

然后我的解题思路就变成下面这样~

### 1.2、想到SpringBoot版本兼容问题

然后就搜到了下面的一篇文章：[Spring Boot 2.2.1 发布，一个有点坑的版本！](https://blog.csdn.net/youanyyou/article/details/102994051)

![image-20220823223320939](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823223320939.png)

然后博主又说到了在 2.2.1中又作了调整，

![image-20220823223422186](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823223422186.png)

![image-20220823223444377](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823223444377.png)

当时我的心里想的就是“原来反复横跳的不止我一个人啊”，哈哈

我一开始真的以为是这方面引起来的问题，然后我就针对pom文件引入的冲突的`jar`进行排除，尝试启动项目。

这里尝试的修改我就不再贴出来了。

不过错误也有所变化，变成了不能加载`ApplicationContext`~

踩坑到这里的时候，突然想到在平时开发的时候，也会引入一些第三方的stater，它们并不是官方的stater，肯定会出现版本不一致的问题，我就想去看看它们是如何解决的。

我再将版本调整的比它低或者高，会不会也出现错误勒？

然后就有了下面的一些解题思路~

## 二、解题

在这里的时候，我针对我之前写的一个第三方登录的demo，把它引入的`boot`的版本调低和调高，然后再对引入的第三方登录的`stater`，分别进行了测试，发现都是可以的，并没有出现什么不兼容的情况。然后就开始站在巨人的肩膀上看待问题。

### 2.1、站在巨人的肩膀上

翻阅我Demo中的实现第三方登录引入的`justauth-spring-boot-starter`依赖时，

在他的pom文件中看到如下图的一样的代码

![image-20220823225915843](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823225915843.png)

```xml
<dependencyManagement>
    <dependencies>
        <!-- spring boot 版本控制 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

如实说，这个`dependencyManagement`我是一直知晓的，并且也是使用过的。

**但是在看到的时候却未曾思考到这里，说起来还是基础太过于薄弱，去追逐于无穷无尽的框架知识和理论知识，在这里给自己好好的敲一番警钟**。

> 然后我先尝试仿造他，使用`dependencyManagement`来重新管理我的maven依赖。

### 2.2、使用dependencyManagement管理stater依赖

在我自定义的`stater`中，开始重新使用dependencyManagement管理Maven依赖

```xml
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
    <spring-boot-version>2.2.0.RELEASE</spring-boot-version>
</properties>
<dependencyManagement>
    <dependencies>
        <!-- spring boot 版本控制 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot-version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

在我的使用stater的引入方`app-server`中，pom文件结构已调整为

![image-20220823231933830](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823231933830.png)

修改完这里的时候，我天真的以为，这次应该是找到问题所在了，然而在测试时，又开始报错了。

错误如下：加载不了`ApplicationContext`

```bash
java.lang.IllegalStateException: Failed to load ApplicationContext

	at org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate.loadContext(DefaultCacheAwareContextLoaderDelegate.java:125)
```



在此我又返回去看我之前的demo项目是如何做到切换boot版本，而不报错的。

然后我发现之前的项目是采用继承的方式

![image-20220823232129084](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823232129084.png)

继而点进去又发现：`spring-boot-dependencies`

![image-20220823232236833](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823232236833.png)

你会发现这里和使用`dependencyManagement`管理boot版本时，引入的是一样的。

接着往下看下去，`spring-boot-dependencies`中用的也是`dependencyManagement`来管理依赖

![image-20220824001832931](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220824001832931.png)

然后我就做了下面两个测试：

---

首先，我测试将`app-server`中的`pom`文件的parant改为

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.6.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

再次进行测试，此刻已然可以正常执行

![image-20220823232920095](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220823232920095.png)

上面的测试成功后，

我另外又尝试直接继承`spring-boot-dependencies`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>2.1.6.RELEASE</version>
    <relativePath>../../spring-boot-dependencies</relativePath>
</parent>
```

测试结果和上面还是一样的，当然归根结底还是因为`dependencyManagement`管理依赖。

我又想着，如果我在stater中也采用这种方式，会不会成功勒？

答案是成功的。

> 其实越是这样，越可以说明，使用`dependencyManagement`的管理依赖的重要性。
>
> 在自定义stater时候，更是如此。



### 2.3、dependencyManagement

看到此处，其实你可能已经明白`dependencyManagement`的作用了

Maven 可以通过 `dependencyManagement` 元素对依赖进行管理，它具有以下 2 大特性：

- 在该元素下声明的依赖不会实际引入到模块中，只有在 `dependencies` 元素下同样声明了该依赖，才会引入到模块中。
- 该元素能够约束 `dependencies `下依赖的使用，即 `dependencies` 声明的依赖若未指定版本，则使用 `dependencyManagement` 中指定的版本，否则将覆盖 `dependencyManagement` 中的版本。(重点重点重点，重要的事情说三遍)

但是更加具体的话，我暂时没有合适的语言描述，更详细的术语，还请大家查一查。

了解不深刻，不敢妄言。

### 2.4、建议

1、如果引入的 jar 是第三方的，并且它也犯下了我这样的错误，没有源代码的情况下。

并且当前项目中，也是像我这样随意的管理Maven依赖，可采取的方式是采用`dependencyManagement`来重新管理Maven依赖。可以避免Maven依赖的冲突。

2、如果是自己编写stater，那么我非常建议你重新使用`dependencyManagement`来管理Maven依赖，以确保别人引用jar包时，不会产生依赖冲突或版本冲突。



### 2.5、挖掘宝藏

其实这些问题，在引入的jar包中，源码中都可以找寻到答案。

**只怪在人世间匆匆忙忙，从而忘记了周边的一些景色**。

对于遇到的问题，也要进行一些有层次的思考，多角度的思考。

**希望我，还有此刻看文章的你**，都可以继续努力，在下一次把身边的宝藏好好挖掘挖掘~

## 后记

> 今天终于可以好好的感慨上一句，今天真的是好开心的一天啊~
>
> 还要继续向各位学习，任重而道远啊~

写于 2022 年 08 月 23 日晚，作者：宁在春





首先，先让我开心一下，作为您的粉丝，得到您的回复，真的很开心，在此之前阅读过您诸多文章，令人敬佩，也一直有努力向您学习。

您文章中常常出现和现在的主页上的那句“纸上得来终觉浅，绝知此事要躬行”，我在回答这个问题前，我对这篇文章所写的内容，也经过了一番测试，确实会出现版本问题，也试着去解决了。

先回答第二个问题，以当前这篇文章的 jar 管理而言，如果说我当前项目引入的 springboot 版本为 2.1，再将我的这个手写 starter 的 Springboot 版本调整为2.2，那么整个项目是起不来的。

找到的错误原因是SpringBoot版本原因，SpringBoot2.1的版本和2.2版本是一些区别的，如`spring-boot-actuator-autoconfigure`是在SpringBoot 2.2版本之后才出现的。

版本问题处理：

1、如果引入的 jar 是第三方的，并且它也犯下了我这样的错误，没有源代码的情况下。

并且当前项目中，也是像我这样随意的管理Maven依赖，可采取的方式是采用`dependencyManagement`来重新管理Maven依赖。可以避免Maven依赖的冲突。

2、如果是自己编写stater，那么我非常建议你重新使用`dependencyManagement`来管理Maven依赖，以确保别人引用jar包时，不会产生依赖冲突或版本冲突。

如实说，我在此前没有考虑到这样的问题，缺少属于自己的思考。

非常非常感谢能遇到您提出这个问题，收获颇多。

我尝试和想到的就是如上这些，希望能得到您的解答~



Maven 可以通过 dependencyManagement 元素对依赖进行管理，它具有以下 2 大特性：

- 在该元素下声明的依赖不会实际引入到模块中，只有在 dependencies 元素下同样声明了该依赖，才会引入到模块中。
- 该元素能够约束 dependencies 下依赖的使用，即 dependencies 声明的依赖若未指定版本，则使用 dependencyManagement 中指定的版本，否则将覆盖 dependencyManagement 中的版本。

