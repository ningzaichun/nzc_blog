# 将简单工厂模式改造应用到项目中

10月26日晚补充：经过掘友的提醒，我才发现之前我这篇所写的`策略模式`，其本身更偏向于工厂模式，我起初以为是掘友分不清工厂模式和策略模式，实际上是我自己把自己绕进去，看不清工厂模式和策略模式的区别。

因此出现的错误，为此感到十分的抱歉，实属学艺不精，下次在输出自己的知识时，一定会进行内容正确性的确认，一定一定会多次审稿。

非常感谢小伙伴给予的提醒，写下此段文字，也是为了警醒自己，以免再犯此类错误。



这篇文章的产出也是最近在写代码的时候，遇到的一个很简单的问题，就是大家嘴边常常挂着的`if...else if...else`问题。

其实一两个`if...else if...else`也没啥问题的，如果好几个地方用到了的话， 就显得有些磨人了，每次执行那一步操作之前，都必须先判断一遍~。

肿么说勒，其实在你不知道可以优化代码的方式时，你不会觉得自己写出来的代码有啥问题，甚至还会觉得自己写的还不错。

但是如果你知道可以把这件事情做的更好一些，然后你再回头看看自己为了完成任务写出的代码时，就有种自己看着自己把代码写成一副烂代码的感觉。

## 开始的缘由

让我去整理一个上传文件的模块，然后项目中的话，就本地和云都混合的，并且也没有确定一定是使用某个存储服务。

![image-20221025213646929](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221025213646929.png)

目前的话，就支持`local、minio、阿里云`，但是我在另外的一个项目中（不是现在我弄的项目），看到了`七牛云` 。

项目中就写了一个所谓的全局上传的方法：

```java
public static String upload(MultipartFile file, String bizPath, String uploadType) {
    String url = "";
    if(CommonConstant.UPLOAD_TYPE_MINIO.equals(uploadType)){
        url = MinioUtil.upload(file,bizPath);
    }else{
        url = OssBootUtil.upload(file,bizPath);
    }
    return url;
}
```

本地上传接口没囊括在内....，是嵌入在业务代码中判断的。

咋一看这个代码，其实不会觉得有啥的，哈哈。

肿么说我自己勒，我在最开始的时候，就按照这个方法，在业务代码中嵌入了一些判断，然后调用的这个方法去上传。

---

等到功能实现之后，再回头看自己写的代码，是真的觉得不好，然后就想动一动它。

在这期间也看了许多文章。

推荐大佬的文章：

[实战！工作中常用到哪些设计模式](https://mp.weixin.qq.com/s?__biz=Mzg3NzU5NTIwNg==&mid=2247495616&idx=1&sn=e74c733d26351eab22646e44ea74d233&chksm=cf2230e9f855b9ffe1ddb9fe15f72a273d5de02ed91cc97f3066d4162af027299718e2bf748e&token=1183092541&lang=zh_CN#rd) 作者： 捡田螺的小男孩 

## 代码中的问题

我们先说说上面代码中存在的问题：

1. 如果我后期需要增加一个七牛云的文件存储服务，是不是必须要去动这段代码，不动的话，就只能在业务增加逻辑判断。
2. 其次，我截图的只是`upload`这一个方法，其他例如`delete、download`等等，都需要改动代码去增加一段逻辑。

多个扩展就需要做多个判断，改多次代码。

> 在维护期间，其实在能不改动源代码的情况下，尽量不要去改动源代码，一定确定能对还好，怕就怕改出了问题，就糟糕了。

并且在设计原则与思想中就有这一点，对**开闭原则**（ 对扩展开放、修改关闭）

### 开闭原则如何理解呢？

**软件实体（模块、类、方法等）应该“对扩展开放、对修改关闭”**.

添加一个新的功能，应该是通过在已有代码基础上扩展代码（新增模块、类、方法、属性等），而非修改已有代码（修改模块、类、方法、属性等）的方式来完成。

当然开闭原则并不是说完全杜绝修改，而是以最小的修改代码的代价来完成新功能的开发。

### 简单工厂模式

关于这个工厂模式，其实我一年前记过一篇刚学的笔记[Java设计模式-工厂模式](https://juejin.cn/post/6993950553445269511#heading-0)，当时就是觉得学到了那个阶段，顺着学了一遍，但都是**囫囵吞枣，一知半解**。

所以今天才是实战篇~ 我觉得是有效且可以应用上的一篇文章

当然很多设计模式的目的之一都是为了实现代码的扩展性。

简单工厂模式是属于创建型模式，又叫做静态工厂方法（Static Factory Method）模式，但不属于23种GOF设计模式之一。简单工厂模式是由一个工厂对象决定创建出哪一种产品类的实例。

说概念不好说，我们还是直接上代码~

简单工厂模式本身并不完全实现“开闭原则”，当然在我这个代码里面，也并没有完全实现开闭原则，但是可以说做较小的变动，去达到类似实现开闭原则的方法。

## 简单工厂模式改良代码的实现

### 1、类图

先来整个类图：

![image-20221026220248462](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221026220248462.png)

（图片说明：类图本身不应该有颜色的，此处是我为了观感更舒适而添加的）

整体不难~

### 2、编写一个统一的上层接口

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 19:53
 */
public interface FileStoreManagerService {

    // 此处就是一个枚举类，在后文中
    UploadTypeEnum getUploadType();

    void upload();

    void download();

    void deleteFile();

    List<String> getFiles();
}
```

我们目前有`aliyun、minio、local`三种存储文件的方式，因此我们第二步就是分别编写这三个服务类。

### 3、编写相关的实现类

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 19:59
 */
@Service
public class AliyunFileServiceImpl implements FileStoreManagerService {

    @Override
    public UploadTypeEnum getUploadType() {
        return UploadTypeEnum.ALIYUN;
    }
    @Override
    public void upload() {
        System.out.println("调用aliyun的upload方法");
    }

    @Override
    public void download() {
        System.out.println("调用aliyun的download方法");
    }

    @Override
    public void deleteFile() {
        System.out.println("调用aliyun的deleteFile方法");
    }

    @Override
    public List<String> getFiles() {
        System.out.println("调用aliyun的getFiles方法");
        return new ArrayList<>();
    }
}
```

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 20:00
 */
@Service
public class LocalFileServiceImpl implements FileStoreManagerService {

    @Override
    public UploadTypeEnum getUploadType() {
        return UploadTypeEnum.LOCAL;
    }

    @Override
    public void upload() {
        System.out.println("调用Local的upload方法");
    }

    @Override
    public void download() {
        System.out.println("调用Local的download方法");
    }

    @Override
    public void deleteFile() {
        System.out.println("调用Local的deleteFile方法");
    }

    @Override
    public List<String> getFiles() {
        System.out.println("调用Local的getFiles方法");
        return new ArrayList<>();
    }
}
```

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 19:56
 */
@Service
public class MinioServiceImpl implements FileStoreManagerService {
    @Override
    public UploadTypeEnum getUploadType() {
        return UploadTypeEnum.MINIO;
    }

    @Override
    public void upload() {
        System.out.println("调用minio的upload方法");
    }

    @Override
    public void download() {
        System.out.println("调用minio的download方法");
    }

    @Override
    public void deleteFile() {
        System.out.println("调用minio的deleteFile方法");
    }

    @Override
    public List<String> getFiles() {
        System.out.println("调用minio的getFiles方法");
        return new ArrayList<>();
    }
}
```

编写完真正实现的服务类后，我们还需要一个方式来获取这些具体的服务类。

比如我们可以实现`ApplicationContextAware`接口，把那些具体的实现的服务类，初始化到map里面，等到我们要使用时再通过`map`直接获取即可。

### 4、使用前的准备

我原本想把它叫做初始化的，但是想了想，不太符合，就还是用了这个小标题吧。

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 20:04
 */
@Component
public class FileStoreManagerStrategyService implements ApplicationContextAware {

    private Map<UploadTypeEnum,FileStoreManagerService> fileStoreManagerStrategy =new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, FileStoreManagerService> tmepMap = applicationContext.getBeansOfType(FileStoreManagerService.class);
        tmepMap.values().forEach(strategyService -> fileStoreManagerStrategy.put(strategyService.getUploadType(), strategyService));
    }

    public FileStoreManagerService getFileStoreManagerService(UploadTypeEnum uploadTypeEnum){
        return fileStoreManagerStrategy.get(uploadTypeEnum);
    }
}
```

`ApplicationContextAware`是在`spring`初始化完bean后才注入上下文的，所以在注入的上下文中，已经将我们具体的实现类加载好啦~

### 5、使用

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 20:39
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DemoApplication.class})
public class DemoTest {

    @Autowired
    private FileStoreManagerStrategyService fileStoreManagerStrategyService;

    private final String uploadType="aliyun";

    @Test
    public void test1(){
        UploadTypeEnum uploadTypeEnum = DescribableEnum.getByProperty(UploadTypeEnum.class, u -> u.getName(), uploadType);
        System.out.println(uploadType);
        FileStoreManagerService fileStoreManagerService = fileStoreManagerStrategyService.getFileStoreManagerService(uploadTypeEnum);
        fileStoreManagerService.upload();
        /**
         * out:
         * aliyun
         * 调用aliyun的upload方法
         */
    }
}
```

在测试代码中可以看到，我们直接通过`fileStoreManagerStrategyService.getFileStoreManagerService(uploadTypeEnum)`方法就可以获取到我们想要的实现类，完全不需要我们进行繁琐的`if...else`去判断到底该new哪个实现类。

> 这种思想贯穿于诸多框架之中，并且我敢肯定的是，你看到我编写的**使用前的准备**那段代码时，会觉得意外的熟悉。因为`Spring`生态中太多地方在使用啦，只是很少整体的去看待。

`补充`：枚举类的代码和使用，在上一篇文章中，因为更多的关乎思路，就没有将这些贴出来占篇幅了。

要看的可以点击👉[你知道 Java 中关键字 enum 是一个语法糖吗？反编译枚举类](https://juejin.cn/post/7158100952489132039)

### 6、扩展

假如我现在要在之前的代码中，增加一个七牛云的服务，

- 那么我们需要增加一个七牛云的服务实现类
- 其次在枚举类中增加一个`qiniuyun`的枚举值（也是因为此处对枚举类做了修改，所以并没有完全实现开闭原则）

其他的该肿么使用就还是肿么使用。

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月25日 20:00
 */
@Service
public class QiniuyunServiceImpl implements FileStoreManagerService {
    @Override
    public UploadTypeEnum getUploadType() {
        return UploadTypeEnum.QINIUYUN;
    }
    @Override
    public void upload() {
        System.out.println("调用Qiniuyun的upload方法");
    }
    @Override
    public void download() {
        System.out.println("调用Qiniuyun的download方法");
    }

    @Override
    public void deleteFile() {
        System.out.println("调用Qiniuyun的deleteFile方法");
    }
    @Override
    public List<String> getFiles() {
        System.out.println("调用Qiniuyun的getFiles方法");
        return new ArrayList<>();
    }
}
```

项目结构：

![image-20221025231735178](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221025231735178.png)

## 总结

将自己学到的知识，融合到自己的实践中，永远都是最好的方式。

“实践是检验真理的唯一标准”，学了许许多多的理论，但无处实施的话，那也只是纸上谈兵罢啦。

> **纸上得来终觉浅，绝知此事要躬行**。

## 后记

今天就写到了这里啦~ 感觉自己还好菜啊~  一起努力哦~

> **希望你是满载而归的**~  

对啦，分享一件小小的快乐，今天在自己的手机上看到了掘金 app 推送了自己的文章，被认可的感觉真的非常非常开心，我们一起加油吧。

> 在这不如意的生活里，我们也要尽情热爱生活啊，用文字去表达，用照片去记录，用身体去感受，用爱去接受爱，你会发现一切都是如此值得热爱啊。
>
> 不开心就去跑跑步吧~ 

写于 2022 年 10 月 25 日晚，作者[宁在春](https://juejin.cn/user/2859142558267559/posts)























