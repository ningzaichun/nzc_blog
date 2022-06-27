# Java





## 基础


### 重载和重写的区别（必会）

**重载**： 发生在同一个类中，方法名必须相同，参数类型不同.个数不同.顺序不同，方法返回 值和访问修饰符可以不同，发生在编译时。

**重写**： 发生在父子类中，方法名.参数列表必须相同，返回值范围小于等于父类，抛出的异常范围小于等于父类， 访问修饰符范围大于等于父类；如果父类方法访问修饰符为private 则子类就不能重写该方法。

### Java 中==和 equals 的区别（必会）

== 的话，如果是基本类型的话，比较的就是值是否相等，如果是引用类型的话，比较的是地址是否相等。



equals 的话，引用类型的话，默认比较的地址值。

但是像String、Integer以及 Date 这些类库中 equals 方法已经被重写，所以比较的是内容而不是地址。



### String、StringBuffer、StringBuilder 三者之间的区别（必会）

String 字符串常量 StringBuffer 字符串变量（线程安全） StringBuilder 字符串变量（非线程安全）

### string 常用的方法有哪些？（了解）

indexOf()：返回指定字符的索引。 

charAt()：返回指定索引处的字符。 

replace()：字符串替换。 

trim()：去除字符串两端空白。 

split()：分割字符串，返回一个分割后的字符串数组。 

getBytes()：返回字符串的 byte 类型数组。

length()：返回字符串长度。 

toLowerCase()：将字符串转成小写字母。 

toUpperCase()：将字符串转成大写字符。 

substring()：截取字符串。 

equals()：字符串比较。



### Java 的异常（必会）

![image-20220602115253215](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202206021153918.png)



Throwable 是所有 Java 程序中错误处理的父类，有两种子类，Error 和 Exception

**Error**：表示由 JVM 所侦测到的无法预料的错误，由于这是属于JVM层次的严重错误，导致 JVM 无法继续执行，因此，这是不可捕捉到的，无法采取任何恢复的操作，顶多只能显示错误信息。如 虚拟机错误，内存溢出（OutOfMemoryError，也就是常说的 OOM ），线程死锁等等。

 **Exception**：这是可捕捉到的。

又分为了 运行时异常 和 编译时异常。看名字我们就知道一个运行期间发生的异常，另一个是编译时发生的异常。

运行时异常如 空指针异常、数组下标越界异常、类型转换异常等等。

编译时异常如 IO 异常、SQL 异常等等语法上的错误等等，



### Java 中实现多态的机制是什么？

靠的是父类或接口定义的引用变量可以指向子类或具体实现类的实例对象，而程序调用的方法在运行期才动态绑定，就是引用变量所指向的具体实例对象的方法，也就是内存里正在运行的那个对象的方法，而不是引用变量的类型中定义的方法。



## 常用的设计模式

### 单例模式(必会)

**饿汉式**：

1. **优点**：饿汉式（静态常量方式）它不用担心线程安全问题，它本身就是在类的装载的时候完成实例化的。
2. **缺点**：但是缺点也在这个地方，不管用不用，只要类装载了，那么他就会直接完成实例化，不能达到懒加载的效果，如果你从始至终都没有使用过这个类，那么就会造成内存的浪费。
3. **小结**：平常在开发的过程中，对于那种使用特别频繁的类，一般采用饿汉式实现单例模式，一方面是可以保证线程安全，另一方面实现起来也是非常方便，除了用产生一些内存浪费。

**懒汉式**：

开发中一般使用静态内部类的方式来实现单例模式。

1. **优点**：起到了懒加载的效果，不会造成内存的浪费。借用 JVM 的类装载的机制来保证实例化的时候只有一个线程，所以我们通过能够保证线程的安全性。
2. **小结**：避免了线程安全问题、利用了静态内部类延迟加载（做到懒加载）、效率高。

### Java 静态代理 和 动态代理

代理的三要素：

a、有共同的行为 - 接口

b、目标角色 - 实现行为

c、代理角色 - 实现行为 增强目标对象行为

#### 1）静态代理

1. 概念：某个对象提供一个代理，代理角色固定，以控制对这个对象的访问。 
2. 特点：
   - 程序运行之前确定的，换句话说就是我们手动的固定在那里的。
   - 有可能存在多个代理 引起"类爆炸"（缺点）
   - 每一次进行扩展，都需要改动源代码，所以修改起来是非常麻烦的，并且耦合性太高。

#### 2）动态代理

因为静态代理其局限性，就产生了Java动态代理。

1. 概念：**相比于静态代理，动态代理在创建代理对象上更加的灵活，动态代理类的字节码在程序运行时，由Java反射机制动态产生**。它会根据需要，通过反射机制在程序运行期，动态的为目标对象创建代理对象，无需改动源代码。

2. 实现：Java的动态代理实现分为两种

   一种是 JDK 动态代理，另一种是 CGLIB 动态代理。

3. 特点：

   - 目标对象不固定
   - **在应用程序执行时动态创建目标对象**
   - 代理对象会增强目标对象的行为

#### JDK 代理实现



代码实现：

```java
//创建服务接口
public interface BuyHouse {
    void buyHouse();

    void sellHouse();
}

//实现接口
public class BuyHouseImpl implements BuyHouse {
    @Override
    public void buyHouse() {
        System.out.println("我要买房子！");
    }

    @Override
    public void sellHouse() {
        System.out.println("我邀卖房子！");
    }
}
```

实现代理类：

```java
/**
 * @author ningzaichun
 */
public class DynamicProxyHandler implements InvocationHandler {

    /**
     * 目标对象 目标对象的类型不固定，创建时动态生成
     */
    private Object target;
    /**
     *通过构造器将目标对象赋值
     */
    public DynamicProxyHandler(Object target) {
        this.target = target;
    }

    /**
     *  1、调用目标对象的方法（返回Object）
     *  2、增强目标对象的行为
     * @param proxy 调用该方法的代理实例
     * @param method  目标对象的方法
     * @param args  目标对象的方法形参
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 增强行为
        System.out.println("==============方法前执行");
        // 调用目标对象的方法（返回Object）
        Object result = method.invoke(target,args);
        // 增强行为
        System.out.println("方法后执行==============");
        return result;
    }
    /**
     * 得到代理对象
     * public static Object newProxyInstance(ClassLoader loader,
     *                                       Class<?>[] interfaces,
     *                                       InvocationHandler h)
     *      loader：类加载器
     *      interfaces：接口数组
     *      h：InvocationHandler接口 (传入InvocationHandler接口的实现类)
     * @return
     */
    public Object getProxyInstance() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),target.getClass().getInterfaces(),this);
    }

}
```

测试：

```java
public static void main(String[] args) {
    //创建需要被代理的类
    BuyHouse buyHouse = new BuyHouseImpl();
    //创建动态代理处理器
    DynamicProxyHandler DP = new DynamicProxyHandler(buyHouse);
    //获得代理类
    BuyHouse proxyBuyHouse = (BuyHouse)DP.getProxyInstance();
    proxyBuyHouse.buyHouse();
    proxyBuyHouse.sellHouse();
}
```

结果：

```bash
==============方法前执行
我要买房子！
方法后执行==============
==============方法前执行
我邀卖房子！
方法后执行==============
```

#### CGLIB 代理实现



添加依赖包：

```xml
<dependency>
        <groupId>cglib</groupId>
        <artifactId>cglib</artifactId>
        <version>2.2.2</version>
</dependency>
```

```java
//创建服务接口
public interface BuyHouse {
    void buyHouse();

    void sellHouse();
}

//实现接口
public class BuyHouseImpl implements BuyHouse {
    @Override
    public void buyHouse() {
        System.out.println("我要买房子！");
    }

    @Override
    public void sellHouse() {
        System.out.println("我邀卖房子！");
    }


    public void seeHouse(){
        System.out.println("买房子之前我想先看看房子");
    }
}
```

创建一个拦截器：

```java
/**
 * @author ningzaichun
 */
public class CglibProxy implements MethodInterceptor {

        private Object target;

    /**
     * 通过构造器传入目标对象
     * @param target
     */
    public CglibProxy(Object target) {
        this.target = target;
    }
    /**
     * 获取代理对象
     * @return
     */
    public Object getInstance() {
        // 通过Enhancer对象的create()方法可以生成一个类，用于生成代理对象
        Enhancer enhancer = new Enhancer();
        // 设置父类 (将目标类作为其父类)
        enhancer.setSuperclass(target.getClass());
        // 设置拦截器 回调对象为本身对象
        enhancer.setCallback(this);
        // 生成一个代理类对象，并返回
        return enhancer.create();
    }


    /**
     * 拦截器
     *  1、目标对象的方法调用
     *  2、增强行为
     * @param object  由CGLib动态生成的代理类实例
     * @param method  实体类所调用的被代理的方法引用
     * @param objects 参数值列表
     * @param methodProxy  生成的代理类对方法的代理引用
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Object object, Method method, Object[] objects,
                            MethodProxy methodProxy) throws Throwable {
        // 增强行为
        System.out.println("==============方法前执行");
        // 调用目标对象的方法（返回Object）
        Object result = methodProxy.invoke(target,objects);
        // 增强行为
        System.out.println("方法后执行==============");
        return result;
    }
}
```



测试：

```java
public static void main(String[] args) {
    BuyHouse buyHouse = new BuyHouseImpl();
    
    CglibProxy cglibProxy = new CglibProxy(buyHouse);
    
    BuyHouseImpl buyHouseCglibProxy = (BuyHouseImpl)cglibProxy.getInstance();
    
    buyHouseCglibProxy.buyHouse();
    
    buyHouseCglibProxy.seeHouse();
}
```

结果：

```bash
======方法执行前
我要买房子！
方法执行后======
======方法执行前
买房子之前我想先看看房子
方法执行后======
```

不知道你有没有注意到，我在实现类里单独写了一个方法，我使用 CGLIB 动态代理时，它同样给我代理，而这个 在 JDK 动态代理中是无法实现的。



#### JDK代理与CGLIB代理的区别

- JDK动态代理实现接口，Cglib动态代理继承思想
- JDK动态代理（目标对象存在接口时）执行效率高于Ciglib （1.8 中及之后）
- 如果目标对象有接口实现，选择JDK代理，如果没有接口实现选择Cglib代理



**性能比较**

1）使用CGLib实现动态代理，CGLib底层采用ASM字节码生成框架，使用字节码技术生成代理类，在JDK1.6之前比使用Java反射效率要高。**唯一需要注意的是，CGLib不能对声明为final的方法进行代理，因为CGLib原理是动态生成被代理类的子类**。

2）在JDK1.6、JDK1.7、JDK1.8逐步对JDK动态代理优化之后，在调用次数较少的情况下，JDK代理效率高于CGLib代理效率，只有当进行大量调用的时候，JDK1.6和JDK1.7比CGLib代理效率低一点，但是到JDK1.8的时候，JDK代理效率高于CGLib代理





#### 面试题：

```text
问：Java动态代理类中的invoke是怎么调用的？

答：在生成的动态代理类$Proxy0.class中，构造方法调用了父类Proxy.class的构造方法，给成员变量invocationHandler赋值，$Proxy0.class的static模块中创建了被代理类的方法，调用相应方法时方法体中调用了父类中的成员变量InvocationHandler的invoke()方法。
```

```txt
Spring在选择用JDK还是CGLib的依据
1）当Bean实现接口时，Spring就会用JDK的动态代理
2）当Bean没有实现接口时，Spring使用CGLib来实现
3）可以强制使用CGLib（在Spring配置中加入<aop:aspectj-autoproxy proxy-target-class=“true”/>）
```

3）JDK动态代理为什么只能代理接口而不能代理类？

通过JDK动态代理实现步骤我们就可以看到，我们通过Proxy类的newProxyInstance方法来生成代理对象，**代理类继承了Proxy类并且实现了要代理的接口，由于java不支持多继承，所以JDK动态代理不能代理类**





## 集合



### List 和 Map、Set 的实现类（必会）

(1) Connection 接口:

 List 有序,可重复 

ArrayList 优点: 底层数据结构是数组，查询快，增删慢。 缺点: 线程不安全，效率高 

Vector 优点: 底层数据结构是数组，查询快，增删慢。 缺点: 线程安全，效率低, 已给舍弃了 

LinkedList 优点: 底层数据结构是链表，查询慢，增删快。 缺点: 线程不安全，效率高 

Set 无序,唯一 

HashSet 底层数据结构是哈希表。(无序,唯一) 如何来保证元素唯一性? 依赖两个方法：hashCode()和 equals()

LinkedHashSet 底层数据结构是链表和哈希表。(FIFO 插入有序,唯一) 

1.由链表保证元素有序 2.由哈希表保证元素唯一 

TreeSet 底层数据结构是红黑树。(唯一，有序) 1. 如何保证元素排序的呢? 自然排序 比较器排序 2.如何保证元素唯一性的呢? 根据比较的返回值是否是 0 来决定 

(2)Map 接口有四个实现类： 

HashMap 基于 hash 表的 Map 接口实现，非线程安全，高效，支持 null 值和null 键，线程不安全。 

HashTable 线程安全，低效，不支持 null 值和 null 键； 

LinkedHashMap 线程不安全，是 HashMap 的一个子类，保存了记录的插入顺序；TreeMap 能够把它保存的记录根据键排序，默认是键值的升序排序，线程不安全



### HashMap(必会)

https://blog.csdn.net/weixin_44015043/article/details/105346187











## JVM





### PC 寄存器的作用

PC寄存器用来存储指向下一条指令的地址，也就是即将要执行的指令代码。由执行引擎读取下一条指令，并执行该指令。

### 为什么使用 PC寄存器 来记录当前线程的执行地址呢？

- 因为线程是一个个的顺序执行流，CPU需要不停的切换各个线程，这时候切换回来以后，就得知道接着从哪开始继续执行
- JVM的字节码解释器就需要通过改变PC寄存器的值来明确下一条应该执行什么样的字节码指令

### PC寄存器为什么被设定为私有的？

- 我们都知道所谓的多线程在一个特定的时间段内只会执行其中某一个线程的方法，CPU会不停地做任务切换，这样必然导致经常中断或恢复，如何保证分毫无差呢？
- 为了能够准确地记录各个线程正在执行的当前字节码指令地址，最好的办法自然是为每一个线程都分配一个PC寄存器，这样一来各个线程之间便可以进行独立计算，从而不会出现相互干扰的情况。
- 由于CPU时间片轮限制，众多线程在并发执行过程中，任何一个确定的时刻，一个处理器或者多核处理器中的一个内核，只会执行某个线程中的一条指令。
- 这样必然导致经常中断或恢复，如何保证分毫无差呢？每个线程在创建后，都会产生自己的程序计数器和栈帧，程序计数器在各个线程之间互不影响。





