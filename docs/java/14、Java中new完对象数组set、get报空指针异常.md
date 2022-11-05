# Java 中 new 一个对象数组，操作时报空指针异常

今天的文章素材又是来自我和我的朋友程交流~



## 又是学废基础知识的一天

日常开头~

![image-20221027224018505](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027224018505.png)

![image-20221027224113529](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027224113529.png)

别慌，懵就懂了，因为我也很懵~ 

然后开始告诉我错误是什么~

![image-20221027224535627](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027224535627.png)

一开始看到数组对象时，我是有想法的，包括他这个错误，我隐隐约约感觉我学过这部分的知识，有点久远的感觉~

## 发来了有趣的代码

```java
ublic class ThirdInfo {
    private String title;
    private int number;
    private String pay;
    private String count;

    @Override
    public String toString() {
        return "ThirdInfo{" +
                "title='" + title + '\'' +
                ", number=" + number +
                ", pay='" + pay + '\'' +
                ", count='" + count + '\'' +
                '}';
    }

    public ThirdInfo(String title, String count, String pay) {
        this.title = title;
        this.pay = pay;
        this.count = count;
    }

    public ThirdInfo(String title, int number, String pay) {
        this.title = title;
        this.number = number;
        this.pay = pay;
    }
    public ThirdInfo(String title, String pay) {
        this.title = title;
        this.pay = pay;
    }
    public String getTitle() {   return title;  }

    public void setTitle(String title) {    this.title = title; }

    public int getNumber() {   return number;  }

    public void setNumber(int number) {  this.number = number; }

    public String getPay() { return pay;}

    public void setPay(String pay) { this.pay = pay;}

    public String getCount() {  return count; }

    public void setCount(String count) {this.count = count;}
}

class testal{
    public static void main(String[] args) {
        ThirdInfo [] thirdInfos = new ThirdInfo[6];

        String vipPay[] = new String[]{
                "3笔", "¥1000.00", "¥100.00", "100积分", "¥10.00", "100积分"
        };
        String vipPaytitle[] = new String[]{
                "会员消费笔数","储值余额消费","赠送余额消费","积分抵线变动","积分抵扣金额","积分赠送变动"
        };

        for (int i = 0;i<vipPay.length;i++){
            thirdInfos[i].setTitle(vipPaytitle[i]);
            thirdInfos[i].setPay(vipPay[i]);
        }

        for (ThirdInfo info : thirdInfos) {
            System.out.println(info);
        }
    }
}
```

如果不事先说他会报错，你顺着看下来，甚至还会觉得是对的。

因为就算没见过上面这样的代码，也可能见过下面这样的代码

```java
String[] str = new String[6];
String vipPaytitle[] = new String[]{
    "会员消费笔数", "储值余额消费", "赠送余额消费", "积分抵线变动", "积分抵扣金额", "积分赠送变动"
};
for (int i=0;i<vipPaytitle.length;i++){
    str[i]=vipPaytitle[i];
}
for (int i=0;i<vipPaytitle.length;i++){
    System.out.println(str[i]);
}
```

说 `String`是对象没人会骂我吧，那接着说`new String[6]` 创建了一个对象数组也没人反对吧。

那再说，为什么`new ThirdInfo[6]`是不可以操作的，但是我`new String[6]`是可以赋值，并且不会报错呢？

> 一起思考一下~

## 我的想法

我自己在测了之后，光从代码逻辑层面看不出什么问题，我就想去看一下底层的字节码文件是怎么样的。（我当时只是隐约记得这是实例化的一个问题，但是我知道逻辑层面看不出，但是在字节码中肯定有所不同）

然后就有了下面的测试：

首先看的是原来测试代码的字节码文件

![image-20221027232633859](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027232633859.png)

只看 `new ThirdInfo[6];和  thirdInfos[i].setTitle(vipPaytitle[i]);`部分的字节码文件

![image-20221027232841440](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027232841440.png)

圈出来的这三行字节码代码就是`ThirdInfo [] thirdInfos = new ThirdInfo[6];`的展示

```java
bipush 6 // 将6压入操作数堆栈。
anewarray #2 <com/ThirdInfo> //创建一个引用型(如类，接口，数组)的数组，并将其引用值压入栈顶
astore_1 //把栈顶的值存到第一个变量（thirdInfos ）
```

能不能看懂都没事，我们来看看第二个测试：

直接在方法中 ` new ThirdInfo()`，我们看看它的字节码文件是什么样的。

![image-20221027233802442](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027233802442.png)

```java
new #2 <com/ThirdInfo> // 创建一个对象
dup // 复制栈顶数值(数值不能是long或double类型的)并将复制值压入栈顶
invokespecial #3 <com/ThirdInfo.<init> : ()V> // 调用超类构造方法，实例初始化方法，私有方法
pop // 出栈
```

其实看到这里，就能够大致知道是什么原因了。

就是因为没有实例化，所以看起来`ThirdInfo [] thirdInfos = new ThirdInfo[6];`好像是创建了6个`ThirdInfo`对象的这段代码，实际上，只是分配了内存空间。 由`thirdInfos `指向了数组中的连续存储空间的首地址。

![image-20221027235239747](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221027235239747.png)



还可以换个更简单的方式来证实这个情况：

我利用反射创建`ThirdInfo `对象进行输出，和输出`ThirdInfo [] thirdInfos = new ThirdInfo[6];`第一个对象，看看他们的结果是什么~

```java
public class Test {

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> aClass = Class.forName("com.ThirdInfo");
        ThirdInfo o =(ThirdInfo) aClass.newInstance();
        System.out.println(o);

        ThirdInfo [] thirdInfos = new ThirdInfo[6];
        System.out.println(thirdInfos[0]);
    }
}
```

![image-20221028001846591](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028001846591.png)

可以看到，实际上 `thirdInfos[0]` 实际上就是`null`

```java
ThirdInfo [] thirdInfos = new ThirdInfo[6];
thirdInfos[0]=new ThirdInfo();
System.out.println(thirdInfos[0]);
```

只有进行实例化之后，才能正确使用。

反射他本质上也是进行了类的实例化的，这点从字节码中依然可以看出。

![image-20221028002247402](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028002247402.png)

之前说到了`invokespecial`是调用超类构造方法，实例初始化方法，私有方法

`invokevirtual` 调用实例方法.

所以在使用前肯定都会去进行实例化的。



聊完这个，又回到了之前的问题上。



## 为什么 `new String[6]`可以？

`String`也是个对象~

说到这个，又回到了以前学`JVM`的知识上来了.

写了一小段代码，这是可以正确执行的

```java
String[] str = new String[6];
for (int i = 0; i < 6; i++) {
    str[i] = ""+i;
}
for (int i = 0; i < 6; i++) {
    System.out.println(str[i]);
}
```

我们来看看他的字节码文件：

![image-20221028003157779](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028003157779.png)

不知道大家还记不记得`String`不可变性，当我们使用**当对字符串重新赋值时，需要重写指定内存区域赋值，不能使用原有的value进行赋值。**

因为`String`它的不可变性，看似只是改变`str[0]=null`的值，实际上将一个新的字符串（"abc"）赋值给`str[0]`时，真正改变的是`str[0]`的指向。 

看个简单的例子吧，是我以前文章里面的。

```java
public static void main(String[] args) {
    String str1 = "hello";
    String str2 = "hello";
    str1="abc,hao";
    // 判断地址， 它由true -->false
    System.out.println(str1 == str2);
}
```

![image-20210802215915762.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/28db4013199e4e57935e59b8cbc1823c~tplv-k3u1fbpfcp-zoom-in-crop-mark:3024:0:0:0.awebp)

更详细的可以看看这篇

[通过这个文章重新再深入认识认识String吧!](https://juejin.cn/post/6991319159108239396) 作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)

## 后记

今天就写到了这里啦~ 感觉自己还好菜啊~  一起努力哦~

希望你是满载而归的~ 

写于 2022 年 10 月 27 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)



















