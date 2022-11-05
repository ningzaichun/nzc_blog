# 你知道 Java 中关键字 enum 是一个语法糖吗？

写下这篇文章，也纯属于是一个机缘巧合，我一个非常要好的朋友**程**，也是刚刚踏上工作岗位。

这个问题也是他踏上岗位的两三天，遇到的~ 起初我也是不会的~ `菜鸡==我`

然后就写下了这篇文章~ 

## 奇怪的开始

他最初的问题，把我问的完全摸不着头脑

![image-20221024193908385](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024193908385.png)

当时我看到就很懵~ 给了一个当时的第一印象的回答

![image-20221024194138657](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024194138657.png)

（图片说明：两个打错别字的憨憨） 

随后他手敲了代码扔给了我~

### 奇怪的代码

接口信息

```java
/**
 * 可自描述的枚举类型的基类
 * @author 宁在春
 */
public interface DescribableEnum {

    /**
     * @return 获取数字型的代码
     */
    int getCode();

    /**
     * @return 获取该枚举的描述名（PS：不是枚举字段的英文名，而是中文描述）
     */
    String getName();

    /**
     * @return 获取该枚举的原生名（PS：不是枚举的中文描述，而是字段的英文名）
     */
    String name();

    static <T extends DescribableEnum> T getByCode(Class<T> enumClazz, Integer code) {
        return Arrays.stream(enumClazz.getEnumConstants())
                .filter(candidate -> Objects.equals(candidate.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    static <T extends DescribableEnum, P> T getByProperty(Class<T> enumClazz, Function<T, P> property, P value) {
        return Arrays.stream(enumClazz.getEnumConstants())
                .filter(candidate -> Objects.equals(property.apply(candidate), value))
                .findFirst()
                .orElse(null);
    }
}

```

实现接口的枚举类

```java
/**
 *  @author 宁在春
 */
@Getter
@RequiredArgsConstructor
public enum SeasonEnum implements DescribableEnum {

    SPRING(1,"春天"),
    SUMMER(2,"夏天"),
    AUTUMN(3,"秋天"),
    WINTER(4,"冬天");

    private final int code;

    private final String name;
}

```

不瞒你说，我看完代码我也是仍然是十分懵逼的。

一方面它说这个代码运行起来没有报错，另外一方面，这个枚举类它确实没实现全部的接口，这和我学的Java就有所相斥。

虽然我很肯定，这个枚举类一定是实现了全部的接口方法的，但是我没敲，没在IDE中跑过代码，没底气讲，哈哈。然后我就说~

![image-20221024200956987](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024200956987.png)

感觉自己真的菜的**扣jio**~

## 有了idea，立马我就又行了

回到住房，我立马就把他给的代码敲了一遍~ 然后我就发现 idea 立马就给出了答案。

接口中总共有三个方法~

``` JAVA
int getCode();
String getName();
String name();
//但真正没被实现的方法就只有 String name();
//其他的两个都已经由 lombok 的注解 @Getter 实现啦~
```

![image-20221024201231330](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024201231330.png)

但是在idea中冒出了这个小绿标，证明肯定是被实现啦的~ 

我就直接点了一下~

然后我发现我们所写的枚举类，在我们不知道的情况下

![image-20221024201819875](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024201819875.png)

还给我们直接继承了底层的抽象枚举类`enum`，让我的`Java`基础再一次深受打击，感觉自己好菜吧菜🤡~~

看完这里我很好奇，我的idea能点（2022版本），为啥他的不行 ，理应找到这个抽象类，顺着往下找就能解决问题啦。



## 好奇

但是找到这里的时候，我自己好奇起来啦~

我好奇我们写的枚举类~ 在什么时候给我继承了那个抽象类~ 

我去看了编译完的class文件，并没有看到，这就让我更好奇啦

![image-20221024202952565](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024202952565.png)

我接着就去互联网搜索资料~ 

看到很多的小伙伴说，在idea看到的class文件，是不会体现 enum 的这个继承类的，都说要反编译这个class文件才行。



反编译这事本来是很简单的，Jdk 自带反编译命令，用 javac 就行，但是我电脑的jdk，不知道是犯了啥问题，让我整了半小时，都没成功用上 javac 命令。

最后放弃了，~~看见就想砸啦~~

## 反编译

### 下载 jad

用了另外一个反编译工具 jad

下载地址：[jad](https://varaneckas.com/jad/)

![image-20221024205631886](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024205631886.png)

选择自己合适的版本，我是windows 10 选的是第一个~

下载之后解压就是这样的

![image-20221024205657367](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024205657367.png)

配置Path 路径，方便全局使用~

![image-20221024205902566](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024205902566.png)

配置完，直接在命令行，输入个 jad 看成功莫

![image-20221024210028119](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024210028119.png)

展示为上图这样，则表示已经成功啦。

### 反编译

接下来我们就来反编译我们的代码吧

> jad 类名.class 

![image-20221024210705279](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024210705279.png)

当执行这条命令，就会看到一个.jad结尾的文件生成~ 

拿 idea 打开或者是 nodepad++ 打开都可~ 

![image-20221024211030374](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024211030374.png)

从这个文件可以看到，我们编写 SeasonEnum 类确确实实是继承了 enum类~ 

> 也让我想起了一个比较常识性的面试题：
>
> 为什么枚举类不能够继承啊？看到这个反编译出来的文件就全都明白啦，它是个`final`修饰的类啊~



### 补充：idea 集成 jad



![image-20221024211638832](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024211638832.png)

![image-20221024211652867](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024211652867.png)

使用：

直接在`class`文件上鼠标右键：

![image-20221024211755965](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221024211755965.png)



## 两个泛型方法的使用

对于接口中定义的那两个方法，可能会有小伙伴不太会用~

我这里重新贴一下，一个一个的补充说明一下



第一个方法：`getByCode`

```java
  static <T extends DescribableEnum> T getByCode(Class<T> enumClazz, Integer code) {
        return Arrays.stream(enumClazz.getEnumConstants())
                .filter(candidate -> Objects.equals(candidate.getCode(), code))
                .findFirst()
                .orElse(null);
    }


```

我先说这个方法的作用吧，就是根据你传入枚举类和code值，获取枚举类对象。

这个还是非常简单的，就是用到了 Java 8 的 `Stream` 流操作，还有 `optional` 的操作~ 都是见名知意的，不懂也明白意思。

我拆开说一下：

- `enumClazz.getEnumConstants()`的意思是获取传过来的枚举类的所有枚举元素，如果`enumClazz`不为枚举类则返回空
- `Arrays.stream(）`这个就是以指定的数组作为来源创建一个流
- `filter`是`Stream` 流中的一个过滤数据的方法，过滤的逻辑是自己写的。
- `findFirst`就是取第一个值
- `orElse(null)` 没有值就为空。
- `T`就是泛型，这个我寻思大家都知道吧~

使用：

```java
SeasonEnum seasonEnum = DescribableEnum.getByCode(SeasonEnum.class, 1);
System.out.println(seasonEnum);
/**
* out
*/
```

再来看第二个：`getByProperty`

```java
static <T extends DescribableEnum, P> T getByProperty(Class<T> enumClazz, Function<T, P> property, P value) {
    return Arrays.stream(enumClazz.getEnumConstants())
        .filter(candidate -> Objects.equals(property.apply(candidate), value))
        .findFirst()
        .orElse(null);
}
```

这个方法的流程：

- 就是通过你传入的枚举类class对象、Function 函数和值，

- 在传入的枚举类class对象中通过`getEnumConstants()`方法获取到所有的枚举元素，

- 然后再通过你传入的 function 函数获取到当前枚举元素的值和你传入的值进行比较，成功则保留在此次的流数据中，过滤完后，依然是取第一个枚举类对象的。

使用方法：

```java
public static void main(String[] args) {
    SeasonEnum seasonEnum = DescribableEnum.getByCode(SeasonEnum.class, 1);
    System.out.println(seasonEnum);

    SeasonEnum byProperty = DescribableEnum.getByProperty(SeasonEnum.class, (s -> s.getCode()), 2);
    System.out.println(byProperty);

    SeasonEnum byProperty2 = DescribableEnum.getByProperty(SeasonEnum.class, (s -> s.getName()), "冬天");
    System.out.println(byProperty2);
    /**
     * out
     * SPRING
     * SUMMER
     * WINTER
     */
}
```

`function` 函数不懂的小伙伴，可以去看看 `Java 8` 实战的这本书，讲的很好滴~ 对于`lambda`表达式、`stream `流、`optional` 操作等都做了很好的讲解，还有 `Java 8` 的` LocalDatetime` 的讲解.

## 后记

今天就写到了这里啦~ 感觉自己还好菜啊~  一起努力哦~

不知道你是否也知道 enum类也是一个近似语法糖的东西吗？

希望你是满载而归的~ 

写于 2022 年 10 月 24 日，作者：宁在春

小伙伴们，帮我看一下，下面的这个logo肿么样~ 可以给一点点建议吗

非常非常感谢，爱你~

![3](C:\Users\ASUS\Desktop\nzc_blog\img\3.png)

