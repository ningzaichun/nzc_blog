---
highlight: a11y-dark
theme: scrolls-light
---
携手创作，共同成长！这是我参与「掘金日新计划 · 8 月更文挑战」的第8天，[点击查看活动详情](https://juejin.cn/post/7123120819437322247 "https://juejin.cn/post/7123120819437322247")
> 属于是今天实时编写的文章啦~，今天也属于是学习到新知识的一天
>
> `TypeHandler` 是我使用 `MybatisPlus` 这么久以来，第一次见到和使用~，属于是`弱鸡`无疑了。😀

## 前言

**今天遇上这样的一个情况，数据库类型与Java对象属性类型不对应，这种情况该如何处理**

在 MySQL 中，字段的属性为 `BigInt`，按道理来说，对应`Java`中的`Long`类型。

但实际上项目中与之对应的 Java对象中的属性的类型是 `Date`类型，**直接给我这个废物当头一棒**

而且不是一两张表，是比较多的表处于`Date` 和 `BigInt`混用的情况，

>你说要好好用Date就好好用Date，要好好用时间戳就好好用时间戳啊，还混用，类型还不对应，麻了

（别问这个项目怎么出现这种事情的，就是来了人，又走了人，然后填坑）

>保持微笑😀(此处口吐芬芳xxxxxx）

## 一、思考

> 我想知道出现这种情况，你是如何思考的？

我的思考是，到底是改数据库，还是改程序代码比较好。

但是无论哪一种我都不敢轻举妄动，所以我做的第一步是把数据库和代码备份，确保不会被玩坏。

我也问了同事，他的建议是让我改程序。

但是怎么说勒，我细细比较了改代码和改程序的麻烦程度，改数据表麻烦会少很多，我就在表结构中的Bigint 类型改为 datatime 类型，而且当时我的任务，是只局限于一两张业务表，影响范围不大，引用也不多。

>我就兴冲冲的把表结构改了，然后把任务完成了~

***

等到今天上午，我之前询问的那个同事也遇到这个问题，他就向上面的经理提了一嘴，说时间类型不对，问他标准是哪一种，经理说是时间戳，我心里一凉~，麻了，（此处省略一万句）

***

**听完，我就苦逼的把表结构改回来了**，此时备份就发生作用了~

还原完数据表后，我就打算去改程序代码了

**周一写 bug，bug 改一周**

突然他和我聊到，xxx，你知道MybatisPlus，有什么方法可以做这种转换吗？

这每一个都要改，太麻烦了，而且业务代码中肯定也用到了，这改起来代价太大了，有没有注解的方式可以解决转换问题。

***

>**很浅显的思考，但是我能够感觉到自己的经验的不足，对于很多偷懒（思考），我还是差的太远了。**

## 二、解决方式

因为用到的 ORM 框架是 MybatisPlus，所以首先找的就是有没有官方的支持。

继而就在官网找到一个字段类型处理器，一看才发现，是学过的东西啊，只怪用的太少，知道的太少啊。


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ee1818d5bb344832922651a8b18be073~tplv-k3u1fbpfcp-watermark.image?)

然后根据这个线索继续找，就了解到 `MyBatis-Plus 字段类型处理器 TypeHandler`

**这个 TypeHandler 处于的位置，就是应用程序和数据库之间的拦截器，所有的操作，都会走一遍这里。**

就翻看源码，想用一个东西，最快的方式就是看一下源码的实现

### 2.1、TypeHandler源码

```
 
 public interface TypeHandler<T> {
 
   /**
     * 入库前的类型转换
    */
   void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;
 
   /**
     * 得到结果。
     * 查询后的数据处理
    */
   T getResult(ResultSet rs, String columnName) throws SQLException;
 
     
   T getResult(ResultSet rs, int columnIndex) throws SQLException;
     
   T getResult(CallableStatement cs, int columnIndex) throws SQLException;
 
 }
```

找到接口,看一下源码中针对已有属性是如何处理,我们仿写一份,达到我们的要求即可啊.


![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/19da0e41e68347f0bcb0f22caa718983~tplv-k3u1fbpfcp-watermark.image?)

### 2.2、BaseTypeHandler 源码

有这么多,我们直接看一下 `BaseTypeHandler` 是什么样的处理逻辑,

一方面 base 吗,基础吗,我们就看看基础是什么样的处理啦，另外一方面他是抽象类吗，说明它其他实现类的基类吗。

```
 public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {
 
   @Override
   public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
     if (parameter == null) {
       if (jdbcType == null) {
         throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
       }
       try {
         ps.setNull(i, jdbcType.TYPE_CODE);
       } catch (SQLException e) {
         throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
               + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
               + "Cause: " + e, e);
       }
     } else {
       try {
         setNonNullParameter(ps, i, parameter, jdbcType);
       } catch (Exception e) {
         throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . "
               + "Try setting a different JdbcType for this parameter or a different configuration property. "
               + "Cause: " + e, e);
       }
     }
   }
 
   @Override
   public T getResult(ResultSet rs, String columnName) throws SQLException {
     try {
       return getNullableResult(rs, columnName);
     } catch (Exception e) {
       throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
     }
   }
 
   @Override
   public T getResult(ResultSet rs, int columnIndex) throws SQLException {
     try {
       return getNullableResult(rs, columnIndex);
     } catch (Exception e) {
       throw new ResultMapException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e, e);
     }
   }
 
   @Override
   public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
     try {
       return getNullableResult(cs, columnIndex);
     } catch (Exception e) {
       throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e, e);
     }
   }
 
     // 这里就是设置为 不为 null 时的入库
   public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;
 
   /**
    * 获取可为空的结果。
    */
   public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;
 
   public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;
 
   public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;
 
 }
 
```

看起来好像很长很多的样子：当我们去掉那些判断，精简一下：

```
 public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {
 
   @Override
   public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
       // 设置不为null的参数，进行入库 ，此处是抽象类，下层还有实现类，
       // 记住这里，待会带你看实现类，你就知道了
       setNonNullParameter(ps, i, parameter, jdbcType);
   }
 
   @Override
   public T getResult(ResultSet rs, String columnName) throws SQLException {
       //  这里从数据库中获取到数据，然后进行类型的一个设置
       return getNullableResult(rs, columnName);
   }
 
   @Override
   public T getResult(ResultSet rs, int columnIndex) throws SQLException {
       //这两个抽象方法，给我的感觉是一模一样的，包括下一个也是如此
       return getNullableResult(rs, columnIndex);
   }
 
   @Override
   public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
       return getNullableResult(cs, columnIndex);
   }
 
 }
```

### 2.3、BigIntegerTypeHandler 源码中的实现类

```
 public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {
 
   @Override
   public void setNonNullParameter(PreparedStatement ps, int i, BigInteger parameter, JdbcType jdbcType) throws SQLException {
     // 这里是转为 BigDecimal ,所以这里就算 setBigDecimal，
     // 那么我们就可以猜测，它还支持其他的方法，Date的话，那就是setDate
     ps.setBigDecimal(i, new BigDecimal(parameter));
   }
 
   @Override
   public BigInteger getNullableResult(ResultSet rs, String columnName) throws SQLException {
     BigDecimal bigDecimal = rs.getBigDecimal(columnName);
       // 这里是rs.getBigDecimal ,我们待会去试一下能否getDate就可以了
     return bigDecimal == null ? null : bigDecimal.toBigInteger();
   }
 
    // 这两个暂时没有做了解，Debug的时候，断点没有执行到这，后期再补一块的知识
    // 但是为了以防万一，我们待会也会照着它的方式将代码改成这样
   @Override
   public BigInteger getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
     BigDecimal bigDecimal = rs.getBigDecimal(columnIndex);
     return bigDecimal == null ? null : bigDecimal.toBigInteger();
   }
 
   @Override
   public BigInteger getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
     BigDecimal bigDecimal = cs.getBigDecimal(columnIndex);
     return bigDecimal == null ? null : bigDecimal.toBigInteger();
   }
 }
 
```

这个实现类，没什么代码，而且就是set、get ，并没有其他的一些处理逻辑什么的。

那么我们也照这样的方式实现一个。

### 2.4、尝试

先明确目标，我们Mysql 中的字段类型 为 `BigInt`，Java程序中的属性类型为 `Date`，

所以我们在入库的时候就是要将 Date 类型转化为 `Long`进行入库，

在从数据库中取出来的时候，要从 `Long` 类型转化为 `Date` 映射到 JavaBean中

我们直接copy上面的代码，然后进行一些更改

```
 public class MyDateTypeHandler implements TypeHandler<Date>{
 
     /**
      * 入库前的类型转换 即执行insert、update方法时会执行
      */
     @Override
     public void setParameter(PreparedStatement ps, int i, Date parameter,
             JdbcType jdbcType) throws SQLException {
         log.info("setParameter(PreparedStatement ps, int i, Date parameter,JdbcType jdbcType)....");
         log.info("[{}],[{}]",parameter,jdbcType);
         ps.setLong(i, parameter.getTime());
     }
 
     /**
      * 查询后的数据处理
      * 这就是查询出来，进行映射的时候，会执行这段代码
      */
     @Override
     public Date getResult(ResultSet rs, String columnName) throws SQLException {
         log.info("getResult(ResultSet rs, String columnName)....",columnName);
         return new Date(rs.getLong(columnName));
     }
     @Override
     public Date getResult(ResultSet rs, int columnIndex) throws SQLException {
         log.info("getResult(ResultSet rs, int columnIndex)....");
         return new Date(rs.getLong(columnIndex));
     }
     @Override
     public Date getResult(CallableStatement cs, int columnIndex)
             throws SQLException {
         log.info("getResult(CallableStatement cs, int columnIndex)....");
         return cs.getDate(columnIndex);
     }
 }
```

咋一眼好像成功啦，但是我们忽略了一个问题，就是MybatisPlus怎么知道它的存在？

那些默认允许进行相互进行类型转换的Handler，它在程序启动的时候，就已经被注册了。

**但是我们写了这个类，一方面没有被MybatisPlus知晓，另一方面还没有指明给谁使用，我们又该怎么使用?**

基于此，我写了一个小Demo，希望大家能够弄明白，以后遇上也能够解决一些问题

## 三、实践案例

**实现目标**：

>**Mysql 中的表的字段为`Bigint`,Java程序中为 Date 类型，我们希望还是可以一如既往的使用MybatisPlus的方法，实现save、list类似这种方法的正常调用，而无需我在保存的时候，将前端传过来的数据手动转换为时间戳，再存放至数据库。查询时亦是如此**

### 3.1、数据库


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d0c4db1184b2477e81aea4b784690a28~tplv-k3u1fbpfcp-watermark.image?)
数据库

```
 SET NAMES utf8mb4;
 SET FOREIGN_KEY_CHECKS = 0;
 
 -- ----------------------------
 -- Table structure for handler_test
 -- ----------------------------
 DROP TABLE IF EXISTS `handler_test`;
 CREATE TABLE `handler_test`  (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
   `date` bigint(50) NOT NULL COMMENT '存时间戳',
   PRIMARY KEY (`id`) USING BTREE
 ) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
 
 -- ----------------------------
 -- Records of handler_test
 -- ----------------------------
 INSERT INTO `handler_test` VALUES (1, '测试数据1', 1659967236);
 INSERT INTO `handler_test` VALUES (2, '测试数据2', 1659967236);
 INSERT INTO `handler_test` VALUES (3, '测试插入数据', 1659968162926);
 INSERT INTO `handler_test` VALUES (4, '测试插入数据', 1659972053771);
 INSERT INTO `handler_test` VALUES (5, '测试插入数据', 1659972815670);
 
 SET FOREIGN_KEY_CHECKS = 1;
 
```

### 3.2、相关代码
我只贴出了相关的代码，其余代码在源码仓库中有，别慌，家人们

service

```
 public interface IHandlerTestService extends IService<HandlerTest> {
 
 }
 
```

**TypeHandler 实现类**

```
 /**
  * @author Ning zaichun
  */
 @Slf4j
 @MappedJdbcTypes({JdbcType.BIGINT})  //对应数据库类型
 @MappedTypes({Date.class})            //java数据类型
 public class MyDateTypeHandler implements TypeHandler<Date>{
 
     /**
      * 入库前的类型转换
      */
     @Override
     public void setParameter(PreparedStatement ps, int i, Date parameter,
             JdbcType jdbcType) throws SQLException {
         log.info("setParameter(PreparedStatement ps, int i, Date parameter,JdbcType jdbcType)....");
         log.info("[{}],[{}]",parameter,jdbcType);
         ps.setLong(i, parameter.getTime());
     }
 
     /**
      * 查询后的数据处理
      */
     @Override
     public Date getResult(ResultSet rs, String columnName) throws SQLException {
         log.info("getResult(ResultSet rs, String columnName)....");
         log.info("[{}]",columnName);
         return new Date(rs.getLong(columnName));
     }
     @Override
     public Date getResult(ResultSet rs, int columnIndex) throws SQLException {
         log.info("getResult(ResultSet rs, int columnIndex)....");
         return new Date(rs.getLong(columnIndex));
     }
     @Override
     public Date getResult(CallableStatement cs, int columnIndex)
             throws SQLException {
         log.info("getResult(CallableStatement cs, int columnIndex)....");
         return cs.getDate(columnIndex);
     }
 
 }
```

实体类的修改，有两点，第一点，需要在实体类上加上

1.  `@TableName(value = "handler_test",autoResultMap = true)` value 是对应表名，autoResultMap 说的

    ```
      是否自动构建 resultMap 并使用,
      只生效与 mp 自动注入的 method,
      如果设置 resultMap 则不会进行 resultMap 的自动构建并注入,
      只适合个别字段 设置了 typeHandler 或 jdbcType 的情况
    ```

1.  第二点就是要在需要处理的字段上加上`@TableField(typeHandler = MyDateTypeHandler.class)` 注解，class就写我们自己编写 Handler.class即可

```
 @Data
 @TableName(value = "handler_test",autoResultMap = true)
 @EqualsAndHashCode(callSuper = false)
 public class HandlerTest implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private String name;
 
     /**
      * 存时间戳
      */
     @TableField(typeHandler = MyDateTypeHandler.class)
     @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private Date date;
 }
 
```

弄完上述这两点，我们还有一个问题，我之前提到一个注册，虽然我们指定了，也写好了，但实际上，还并**没有注册到一个存储 TypeHandler 一个 Map 集合中去的**，也就是说Mybatis 在遇到的时候，其实还是不知道它的存在的~。

但其实只需要在配置文件中加一行即可，**原谅我这么绕圈子，只是希望说明白这是一步步得来的**


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/05f5ad1e65b94caa9c22366e35143e34~tplv-k3u1fbpfcp-watermark.image?)


`type-handlers-package` 后面填写的是我们Handler 存放的包路径。

有这一步即可。


### 3.3、测试

```
 @RunWith(SpringRunner.class)
 @SpringBootTest
 @ContextConfiguration(classes = HandlerApplication.class)
 public class HandlerServiceTest {
 
 
     @Autowired
     IHandlerTestService handlerTestService;
 
     @Test
     public void test1(){
         List<HandlerTest> list = handlerTestService.list();
         list.forEach(System.out::println);
     }
 
     @Test
     public void test2(){
         HandlerTest handlerTest = new HandlerTest();
         handlerTest.setDate(new Date());
         handlerTest.setName("测试插入数据");
         handlerTestService.save(handlerTest);
     }
 
 }
```


![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/60b439a2227448e7b0244a4b65ff15a6~tplv-k3u1fbpfcp-watermark.image?)


测试插入

```
 ==>  Preparing: SELECT name,date FROM handler_test
 ==> Parameters: 
 <==    Columns: name, date
 <==        Row: 测试数据1, 1659967236
 2022-08-08 23:55:25.854  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659967236
 <==        Row: 测试数据2, 1659967236
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659967236
 <==        Row: 测试插入数据, 1659968162926
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659968162926
 <==        Row: 测试插入数据, 1659972053771
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659972053771
 <==        Row: 测试插入数据, 1659972815670
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659972815670
 <==        Row: 测试插入数据, 1659974106847
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659974106847
 <==        Row: 测试插入数据, 1659974125542
 2022-08-08 23:55:25.855  INFO 7368 --- [           main] com.nzc.demo.handler.MyDateTypeHandler   : getResult(ResultSet rs, String columnName)....
 1659974125542
 <==      Total: 7
 Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@145113f]
 HandlerTest(name=测试数据1, date=Tue Jan 20 13:06:07 CST 1970)
 HandlerTest(name=测试数据2, date=Tue Jan 20 13:06:07 CST 1970)
 HandlerTest(name=测试插入数据, date=Mon Aug 08 22:16:02 CST 2022)
 HandlerTest(name=测试插入数据, date=Mon Aug 08 23:20:53 CST 2022)
 HandlerTest(name=测试插入数据, date=Mon Aug 08 23:33:35 CST 2022)
 HandlerTest(name=测试插入数据, date=Mon Aug 08 23:55:06 CST 2022)
 HandlerTest(name=测试插入数据, date=Mon Aug 08 23:55:25 CST 2022)
 2022-08-08 23:55:25.863  INFO 7368 --- [ionShutdownHook] com.alibaba.druid.pool.DruidDataSource   : {dataSource-1} closing ...
 2022-08-08 23:55:25.869  INFO 7368 --- [ionShutdownHook] com.alibaba.druid.pool.DruidDataSource   : {dataSource-1} closed
 
```

## 后记

**希望大家都能有所收获**

写时有些匆忙，TypeHandler的应用场景不止如此，之后可能会再讲上一讲

写于 2022 年 8 月 8 日 晚 作者：[宁在春](https://juejin.cn/user/2859142558267559)
