# SpringBoot SPI机制和实现自定义 starter

> 本文适合于实操~，主要讲述如何自定义start，实现一些自定义类的自动装配。
>
> **面向入门、有需求和想了解的小伙伴们，以免浪费小伙伴们的时间**~

实现starter，其实就是SpringBoot的自动装配原理的一个实践，以前我也写过SpringBoot的自动状态原理的文章，[文章链接](https://juejin.cn/post/7012862822082150413#comment)

**细心认真对待，没有什么是很难的**。

***

## 一、SpringBoot 中的SPI机制

什么是spi呢，全称是`Service Provider Interface`。简单翻译的话，就是服务提供者接口，是一种寻找服务实现的机制。

其实就是一个规范定义、或者说是实现的标准。

***

用生活中的例子说就是，你买了一台小米的手机。

但是你用的充电器并不一定非要是小米充电器，你可以拿其他厂商的充电器来进行充电，只要满足协议、端口等要求，那么就是可以充电的。这也是一种热拔插的思想，并不是固定死的。

> 换成代码来说也是一样的，我定义了一个接口，但是不想固定死具体的实现类，因为那样如果要更换实现类就要改动源代码，这往往是不合适的。
>
> 那么我也可以定义一个规范，在之后需要更换实现类或增加其他实现类时，遵守这个规范，我也可以动态的去发现这些实现类。

换在SpringBoot中，就是现在的SpringBoot这个平台定义了一些规范和标准，我现在想要让SpringBoot平台接纳我。

我该如何做呢？

**很简单，按照它的标准和规范做事**。

> SpringBoot在启动的时候，会扫描所有jar包`resource/META-INF/spring.factories`文件，依据类的全限定名，利用反射机制将`Bean`装载进容器中。

看完这段话，我想你应该对今天的文章应该有个大概的理解啦。

## 二、自定义 starter

说一说我的小实践：

在这个 starter 中，实现

0.  发送短线的Template
0.  对象存储的Template

的自动装配~

大致就是四步~

0.  用于映射配置文件中的配置的类xxxxProperties
0.  用于操作xxxx的接口和客户端等等，如本文中的OssTemplate
0.  自动配置类xxxxAutoConfiguration ，并且向容器中注入xxxxTemplate
0.  在spring.factories中将xxxxAutoConfiguration添加进EnableAutoConfiguration的vaule集合中

对象存储我用的是阿里云的oss，里面的配置都是可以用的， 短信的话，就是个模拟的啦~，勿怪啦


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aba201ffb68f417589074a56d0bf55b5~tplv-k3u1fbpfcp-watermark.image?)

### 2.1、准备一个Maven项目

删除src目录，

然后再创建两个 Maven项目（我个人习惯，习惯创建空Maven项目，实际上创建SpringBoot项目也是一样）


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ed442ef805c94e77b4c64d4bb3474aca~tplv-k3u1fbpfcp-watermark.image?)

最外层的pom.xml

```
 <parent>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-parent</artifactId>
     <version>2.5.2</version>
     <relativePath/>
 </parent>
 
 <properties>
     <maven.compiler.source>8</maven.compiler.source>
     <maven.compiler.target>8</maven.compiler.target>
 </properties>
 
 
 <dependencies>
     <dependency>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-test</artifactId>
     </dependency>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-configuration-processor</artifactId>
         <optional>true</optional>
     </dependency>
 </dependencies>
```

### 2.2、准备Properties类

就是用来映射配置文件的~

```
 /**
  * @author Ning Zaichun
  */
 @Data
 @ConfigurationProperties(prefix = "nzc.oss")
 public class OssProperties {
 
     private String accessKey; 
     private String secret;
     private String bucketName;
     private String url;
     private String endpoint;
 }
```

```
 @Data
 @ConfigurationProperties(prefix = "nzc.sms")
 public class SmsProperties {
 
     private String name;
 }
 
```

### 2.3、准备要注入的类

就是我们最后要通过自动装配注入进SpringBoot操作的类

我这里分别是OssTemplate 和 SmsTemplate

```
 /**
  * @author Ning Zaichun
  */
 public class OssTemplate {
 
     private OssProperties ossProperties;
 
     public OssTemplate(OssProperties ossProperties) {
         this.ossProperties = ossProperties;
     }
 
     public String test() {
         System.out.println(ossProperties.getBucketName());
         return "test";
     }
 
     public String upload(String filename, InputStream is) {
         // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
         String endpoint = ossProperties.getEndpoint();
         // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
         String accessKeyId = ossProperties.getAccessKey();
         String accessKeySecret = ossProperties.getSecret();
 
         // 创建OSSClient实例。
         OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
 
         String storePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + UUID.randomUUID() + filename.substring(filename.lastIndexOf("."));
 
         System.out.println(storePath);
         // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
         ossClient.putObject(ossProperties.getBucketName(), storePath, is);
 
         String url = ossProperties.getUrl() + storePath;
 
         // 关闭OSSClient。
         ossClient.shutdown();
         return url + "#" + storePath;
     }
 
     public void remove(String fileUrl) {
         // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
         String endpoint = ossProperties.getEndpoint();
         // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
         String accessKeyId = ossProperties.getAccessKey();
         String accessKeySecret = ossProperties.getSecret();
         // 填写Bucket名称。
         String bucketName = ossProperties.getBucketName();
         // 填写文件完整路径。文件完整路径中不能包含Bucket名称。
         //2022/01/21/f0870eb3-4714-4fae-9fc3-35e72202f193.jpg
         String objectName = fileUrl;
 
         // 创建OSSClient实例。
         OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
 
         // 删除文件或目录。如果要删除目录，目录必须为空。
         ossClient.deleteObject(bucketName, objectName);
 
         // 关闭OSSClient。
         ossClient.shutdown();
     }
 }
```

```
 public class SmsTemplate {
 
     private SmsProperties properties;
 
     public SmsTemplate(SmsProperties properties) {
         this.properties = properties;
     }
 
     public void sendSms(String mobile, String code){
         System.out.println(properties.getName()+"=="+mobile+"===="+code);
     }
 }
 
```

### 2.4、AutoConfiguration

```
 @EnableConfigurationProperties({
     SmsProperties.class,
     OssProperties.class
         })
 public class CommonAutoConfig {
 
     @Bean
     public SmsTemplate smsTemplate(SmsProperties smsProperties){
         return new SmsTemplate(smsProperties);
     }
 
     @Bean
     public OssTemplate ossTemplate(OssProperties ossProperties){
         return new OssTemplate(ossProperties);
     }
 
 }
 
```

### 2.5、编写spring.factories

在resource目录下，创建一个META-INF文件夹，

在META-INF文件夹下创建一个spring.factories文件

内容是

```
 org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
   com.nzc.CommonAutoConfig
```

如果有多个就是:

```
 org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
   com.nzc.CommonAutoConfig \
   com.xxx.xxx
```


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0ddd90e43e894db0afc56f3aca5efeb5~tplv-k3u1fbpfcp-watermark.image?)

***

到这一步之后，我们将这个项目，达成Jar包，然后在要使用的项目中进行引入。


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/cd95350a43a3441f9ec0db2e62f3d1fa~tplv-k3u1fbpfcp-watermark.image?)

### 2.6、应用测试

1、创建一个SpringBoot 的启动类，有启动类才能进行测试，不然没上下文环境~

2、编写配置文件

```
 spring:
   application:
     name: app-server
 nzc:
   sms:
     name: ningzaichun
   oss:
     accessKey: xxx
     secret: xxx
     endpoint: oss-cn-shenzhen.aliyuncs.com
     bucketName: xxx
     url: xxx
```

将oss的配置修改正确是可以用的~

编写测试类：

```
 @RunWith(SpringRunner.class)
 @SpringBootTest(classes = AppServerApplication.class)
 public class TemplateTest {
 
     @Autowired
     private OssTemplate ossTemplate;
 
     @Test
     public void testOss(){
         String s = ossTemplate.test();
         System.out.println(s);
     }
 
     @Test
     public void testUpload(){
         try {
             File file = new File("D:\evectionflow01.png");
             InputStream inputStream = new FileInputStream(file);
             ossTemplate.upload("123.jpg",inputStream);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     @Autowired
     private SmsTemplate smsTemplate;
 
     @Test
     public void testSendSms(){
         smsTemplate.sendSms("17670090715","123456");
     }
 }
```

证明是可以使用的~


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8a9f7963a7a54820a8c5de4f795b6f91~tplv-k3u1fbpfcp-watermark.image?)

## 后记

写得较为简单，也通俗易懂~，应该能明白吧，哈哈，

如果文中有存在错误或者是不对的地方，请留下您的评论，我会及时修正，非常感谢！

> 这次可能是说点废话，以前我觉得工作三四年的Java开发工作者，应该是会懂很多很多东西。
>
> 因为是后端开发吗，很多时候不可避免的要接触到一些其他东西，`运维部署、测试、前端、底层、架构`什么的，我说的是接触，而不是精通~，不要骂我。只是个人看法。
>
> 因为三年我感觉时间很长很长了，不敢说掌握很多，但是三四年多少应该在一个区域内，有比较大的收获了吧。 但是有些时候躺平了就真的是躺平了~
>
> 当然可能是我被卷习惯了，bb赖赖一会儿，勿怪~

写于 2022 年 8 月 15 日，作者：[宁在春](https://juejin.cn/user/2859142558267559)