# Java注解详解和自定义注解实战，用代码讲解

关于我为啥突然又想要了解Java注解和反射

1.  好奇心来啦
1.  打算看源码（只是有想法，flag中，实现挺难）
1.  巩固Java基础知识（基础不牢，地动山摇）

### 一、逻辑思维图🧐

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4e09b26d037e4688afe33ec607a84e3c~tplv-k3u1fbpfcp-zoom-1.image)

第 1-5 小节均偏向于理论知识，若只是想要了解如何自定义注解和如何应用注解，请跳转至第6小节开始阅读。

在本篇中，主要是针对`注解的概念`和`运行时注解`进行解释说明，附带有三个实战的案例，尽可能的让大家能够理解透彻并且能够加以应用。

### 二、什么是注解👨‍🏫

> ` Java  `注解(`Annotation`)用于为 Java 代码提供元数据。作为元数据，注解不直接影响你的代码执行，但也有一些类型的注解实际上可以用于这一目的。` Java  `注解是从 Java5 开始添加到 Java 的。--官方文档

#### 2.1、注解

`Annotion`(注解)是一个接口，程序可以通过反射来获取指定程序元素的`Annotion`对象，然后通过`Annotion`对象来获取注解里面的元数据。

我们常常使用的注解，`@Data、@Controller`等等，这些都是注解，创建一个注解，也很简单，创建一个类，然后将`class`改为 `@interface`就是一个注解啦。

#### 2.2、注解出现的位置

`Java`代码中的`包、类型、构造方法、方法、成员变量、参数、本地变量的声明`都可以用注解来修饰。注解本质上可以看作是一种特殊的标记，程序在`编译`或者`运行时`可以检测到这些标记而进行一些特殊的处理。

#### 2.3、关于注解的处理

我们一般将利用反射来处理注解的方式称之为`运行时注解`。

另外一种则是编译时注解，如我们常常使用的 lombok 里的注解，`@Data`，它能够帮我们省略`set/get`方法，我们在`Class`上加上这个注解后，在编译的时候，`lombok`其实是修改了`.class`文件的，将`set/get`方法放进去了，不然的话，你可以看看编译完后的`.class`文件。诸如这种，我们常称为`编译时注解`，也就是使用`javac`处理注解。

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/20300e48f9a441ddbd71b02f962527d5~tplv-k3u1fbpfcp-zoom-1.image)

--图：来自于[极客学院](https://wiki.jikexueyuan.com/project/java-vm/java-debug.html)

这幅图就是从`.java`文件到`class`文件的，再到`class`文件被 JVM 加载的过程。

而其中的`注解抽象语法树`这一阶段，就是去解析注解，然后根据定义的注解处理器进行相关的逻辑处理。

> 这一块不是我的关注点，略过略过啦，朋友们，好奇可以去研究研究噢

### 三、注解的目的或作用💞

-   **生成文档**。这是最常见的，也是 Java 最早提供的注解。如`@param、@return`等等
-   **跟踪代码依赖性，实现替代配置文件功能。**作用就是减少配置，如 `Spring`中`Bean`的装载注入，而且现在的框架基本上都是使用注解来减少配置文件的数量，同时这样也使得编程更加简洁，代码更加清晰。
-   **在编译时进行格式检查。**如`@Override`放在方法前，如果你这个方法并不是覆盖了超类方法，则编译时就能检查出；
-   **标识作用。**当`Java`编译时或运行时，检测到这里的注解，做什么的处理，自定义注解一般如此。
-   **携带信息。** 注解的成员提供了程序元素的关联信息，` Annotation  `的成员在 `Annotation`类型中以无参数的方法的形式被声明。其方法名和返回值定义了该成员的名字和类型。在此有一个特定的默认 语法：允许声明任何`Annotation`成员的默认值。一个`Annotation`可以将`name=value`对作为没有定义默认值的` Annotation  `成员的值，当然也可以使用`name=value`对来覆盖其它成员默认值。这一点有些近似类的继承特性，父类的构造函数可以作为子类的默认构造函数，但是也 可以被子类覆盖。
-   这么一大段话，其实就是关于注解中成员的解释。
-

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/107f5fb40b9b4723a0b1126311267021~tplv-k3u1fbpfcp-zoom-1.image)

说了这么多，其实一句话也能表达完。

注解就是一张便利贴，它贴在那里，你看到的那一刻，就明白该做什么事啦。

> 如出门前，门上贴着一张便利贴📌，上面写着"出门记得带钥匙"，当你看到的那一刻，你就会去检查一下自己是否带钥匙啦。
>
> 在Java中也是一样的，你定义了一个注解，注解上可以写一些东西，然后你再将它贴在某个上面，说明白执行规则，当编译到这里的时候需要干嘛干嘛，又或者是当运行到这里的时候需要干嘛干嘛。
>
> 因为注解写的东西的不同，或者是处理注解的规则不同，而产生了不同的注解及作用。

### 四、JDK内置注解💫

Java中 内置的注解有5类，具体包括：

**`@Deprecated`**：过时注解，用于标记已过时 & 被抛弃的元素（类、方法等）

**`@Override`**：复写注解，用于标记该方法需要被子类复写

**`@SuppressWarnings`**：阻止警告注解，用于标记的元素会阻止编译器发出警告提醒

**`@SafeVarargs`**：参数安全类型注解，用于提醒开发者不要用参数做不安全的操作 & 阻止编译器产生 unchecked警告，Java 1.7 后引入

### 五、元注解 🎯

何为元注解？就是**注解的注解**，就是给你自己定义的注解添加注解，你自己定义了一个注解，但你想要你的注解有什么样的功能，此时就需要用元注解对你的注解进行说明了。

接着上一个比喻

> 注解有很多很多吗，门上贴一个，冰箱上贴一个，书桌上贴一个等等
>
> 元注解勒就是把他们整合起来称呼的，像上面这些可以统称为生活类注解啊。所以也就是注解的注解。

#### 5.1、@Target

在 @Target 注解中指定的每一个 ElementType 就是一个约束，它告诉编译器，这 个自定义的注解只能用于指定的类型。

说明了注解所修饰的对象范围：注解可被用于 packages、types（类、接口、枚举、Annotation类型）、类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch参数）。

#### 5.2、@Retention

定义了该注解的生命周期：

1.  某些注解仅出现在源代码中，而被编译器丢弃； （源码级）
1.  而另一些却被编译在class文件中； （字节码级）
1.  编译在class文件中的注解可能会被虚拟机忽略，而另一些在class被装载时将被读取（请注意并不影响class的执行，因为注解与class在使用上是被分离的）。绝大多数开发者都是使用RUNTIME，因为我们期望在程序运行时，能够获取到这些注解，并干点有意思的事儿，而只有RetentionPolicy.RUNTIME，能确保自定义的注解在运行时依然可见。（运行级）

使用这个元注解可以对自定义注解的“生命周期”进行限制。

RetentionPolicy.SOURCE 一般开发者很少用到，大都是Java内置的注解。如`@Override`

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {
}
```

```
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
```

这些注解只是在编译的时候用到，一旦编译完成后，运行时没有任何意义，所以他们被称作源码级别注解。

如果有了解过 lombok 一些简单原理的开发者， 都知道它是通过注解在编译时自动生成一部分代码，让源码看起来更简洁，字节码却很强大。

当然，这种方式有它自身的缺陷，譬如不一致性，问题排解时的困扰，以及依赖问题，不是本篇重点，扯回来。

-   提供信息给编译器： 编译器可以利用注解来检测出错误或者警告信息，打印出日志。
-   编译阶段时的处理： 软件工具可以用来利用注解信息来自动生成代码、文档或者做其它相应的自动处理。
-   运行时处理： 某些注解可以在程序运行的时候接受代码的提取，自动做相应的操作。

#### 5.3、@Documented

用于描述其它类型的annotation应该被作为被标注的程序成员的公共API，因此可以被例如 `javadoc`此类的工具文档化。是一个`标记注解`，没有成员。

#### 5.4、@Inherited

是一个标记注解阐述了某个被标注的类型是被继承的。使用了`@Inherited`修饰的注解类型**被用于一个class时该class的子类也有了该注解**。

#### 5.5、@Repeatable

允许一个注解可以被使用一次或者多次（Java 8）。

### 六、自定义注解📸

自定义注解实际上就是一种类型而已,也就是引用类型（Java中除了8种基本类型之外,我们见到的任何类型都是引用类型）

#### 6.1、定义注解

自定义注解过程：

1.  声明一个类MyAnnotation
1.  把class关键字改为@interface

这样我们就声明了一个自定义的注解，当我们用`@interface`声明一个注解的时候，实际上是声明了一个接口，这个接口自动的继承了`java.lang.annotation.Annotation`，但是我们只需要`@interface`这个关键字来声明注解，编译器会自动的完成相关的操作，不需要我们手动的指明继承`Annotation`接口

另外在定义注解时，不能再继承其他的注解或接口。

我举了四个例子，这四个注解分别是放在 类（接口、枚举类上）、构造函数、方法级别、成员属性上的。

```
@Documented    //定义可以被文档工具文档化
@Retention(RetentionPolicy.RUNTIME)//声明周期为runtime，运行时可以通过反射拿到
@Target(ElementType.TYPE)//注解修饰范围为类、接口、枚举
public @interface ClassAnnotation {
    public String name() default "defaultService";
    public String version() default "1.1.0";
}
```

```
@Documented
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructorAnnotatin {
    String constructorName() default "";
    String remark() default "构造器";
}
```

```
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAnnotation {
    public String name() default "defaultName";

    public String value() default "defaultValue";
}
```

```
 @Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface MethodAnnotation {
     public String name() default "defaultName";
     public MethodTypeEnum type() default MethodTypeEnum.TYPE1;
 }
```

```
public enum MethodTypeEnum {
    TYPE1,TYPE2
}
```

#### 6.2、注解的成员变量

1.  成员以无参数无异常的方式声明 `String constructorName() default "";`
1.  可以使用default为成员指定一个默认值`  public String name() default "defaultName"; `
1.  成员类型是受限的，合法的类型包括原始类型以及String、Class、Annotation、Enumeration （JAVA的基本数据类型有8种：byte(字节)、short(短整型)、int(整数型)、long(长整型)、float(单精度浮点数类型)、double(双精度浮点数类型)、char(字符类型)、boolean(布尔类型）
1.  `public MethodTypeEnum type() default MethodTypeEnum.TYPE1;`
1.  注解类可以没有成员，没有成员的注解称为**标识注解**，例如JDK注解中的@Override、@Deprecation
1.  如果注解只有一个成员，并且把成员取名为value()，则在使用时可以忽略成员名和赋值号“=”
1.  例如JDK注解的@SuppviseWarnings ；如果成员名 不为value，则使用时需指明成员名和赋值号"="

#### 6.3、使用注解

因为我们在注解中声明了属性,所以在使用注解的时候必须要指明属性值 ,多个属性之间没有顺序,多个属性之间通过逗号分隔

```
@ClassAnnotation(name = "personBean", version = "1.2.1")
public class Person {

//    告诉大家是可以用的,但是影响我测试,我就又注释掉了.
//    @ConstructorAnnotatin(constructorName="Person()")
//    public Person(String description) {
//        this.description = description;
//    }

    @FieldAnnotation(name = "description", value = "This is my personal annotation")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @MethodAnnotation(name = "sayHello", type = MethodTypeEnum.TYPE2)
    public void sayHello() {
        System.out.println("Hello Annotation!");
    }
}
```

#### 6.4、浅提一下反射

想要去获取注解就不得不提到反射啦，但 Java 反射会带来一定的耗时，因此使用运行注解需要考虑对性能的影响。

我们声明一个`Student`类用来描述学生对象的信息的

```
class Student{
   String name;
   String school;
   //...set/get
}
```

当我们创建一个学生对象时，学生对象的信息是保存在 Student 类中，所以 Student 类会提供获取这些信息的方法。

在Java类中，每个类都会有对应的Class，要想执行反射操作，必须先要获取指定类名的Class

***

**了解Class对象**：

类是程序的一部分，每个类都有一个 Class 对象。换言之，每当我们编写并且编译 了一个新类，就会产生一个 Class 对象（更恰当的说，是被保存在一个同名的 .class 文件中）。为了生成这个类的对象，Java 虚拟机 (JVM) 先会调用 “类加载器” 子系统把 这个类加载到内存中。

`Class`类：简单说就是用来描述类对象的信息的

类对象的信息包括：

1.  类的基本信息：包名、修饰符、类名、基类，实现的接口
1.  属性的信息：修饰符、属性类型、属性名称、属性值，
1.  方法的信息：修饰符、返回类型、方法名称、参数列表、抛出的异常
1.  构造方法的信息：修饰符、类名、参数列表、抛出的异常
1.  注解的相关信息：
1.  因为：类对象的相关信息全部保存在Class类
1.  所以：Class类会提供获取这些信息的方法

一旦某个类的 Class 对象被载入内存，它就可以用来创建这个类的所有对象。

***

通过 Class 获取类的相关信息，通过Class创建对象，通过 Class 调用对象上面的属性，调用对象上面的方法，这种操作就称为反射，要想执行反射操作，必须先要获取到指定的类名的 Class

获取Class的不同方式

-   获取基本类型的Class
-   1）基本类型Class：如 int.Class获取的就是 int 类型的 Class
-   获取引用类型的Class：
-   1）引用类型的Class：如String.Class获取的就是String类对应的Class
-   2）通过对象来获取：如：String obj="hello"，Class calz = obj.getClass()，获取的就是String类对应的Class
-   3）Class.forName("java.lang.String")，获取的就是对应的Class

***

#### 6.5、提取注解

```
public class TestClassAnnotation {

    private static Person person = new Person();

    public static void main(String[] args) {
        Class<?> clazz = person.getClass();
        //因为注解是作用于类上面的，所以可以通过isAnnotationPresent来判断是否是一个具有指定注解的类
        if (clazz.isAnnotationPresent(ClassAnnotation.class)) {
            System.out.println("This is a class with annotation ClassAnnotation!");
            //通过getAnnotation可以获取注解对象
            ClassAnnotation annotation = clazz.getAnnotation(ClassAnnotation.class);
            if (null != annotation) {
                System.out.println("BeanName = " + annotation.name());
                System.out.println("BeanVersion = " + annotation.version());
            } else {
                System.out.println("the annotation that we get is null");
            }
        } else {
            System.out.println("This is not the class that with ClassAnnotation");
        }
    }
}
```

```
This is a class with annotation ClassAnnotation!
BeanName = personBean
BeanVersion = 1.2.1
```

```
public class AnnotationTest {
 
  public static void main(String[] args) throws ClassNotFoundException {
    Class<?> clazz = Class.forName("com.nzc.my_annotation.shang.Person");
    System.out.println("==============类注解解析==============");
    printClassAnno(clazz);
    
    System.out.println("==============成员变量注解解析==============");
    printFieldAnno(clazz);
    
    System.out.println("==============成员方法注解解析==============");
    printMethodAnno(clazz);
    
    System.out.println("==============构造器注解解析==============");
    printConstructorAnno(clazz);
    
  }
  
  /**
   * 打印类的注解
   */
  private static void printClassAnno(Class<?> clazz) throws ClassNotFoundException {
    //判断是否有AuthorAnnotatin注解
    if(clazz.isAnnotationPresent(ClassAnnotation.class)) {
      //获取AuthorAnnotatin类型的注解
      ClassAnnotation annotation = clazz.getAnnotation(ClassAnnotation.class);
      System.out.println(annotation.name()+"\t"+annotation.version());
    }
  }
  
  
  /**
   * 打印成员变量的注解
   */
  private static void printFieldAnno(Class<?> clazz) throws ClassNotFoundException {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if(field.isAnnotationPresent(FieldAnnotation.class)) {
        FieldAnnotation annotation = field.getAnnotation(FieldAnnotation.class);
        System.out.println(annotation.name()+"\t"+annotation.value());
      }
    }
  }
  
  /**
   * 打印成员变量的注解
   */
  private static void printMethodAnno(Class<?> clazz) throws ClassNotFoundException {
    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {
      if(method.isAnnotationPresent(MethodAnnotation.class)) {
        MethodAnnotation annotation = method.getAnnotation(MethodAnnotation.class);
        System.out.println(annotation.name()+"\t"+annotation.type());
      }
    }
  }
  
  /**
   * 打印成员变量的注解
   */
  private static void printConstructorAnno(Class<?> clazz) throws ClassNotFoundException {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    for (Constructor<?> constructor : constructors) {
      if(constructor.isAnnotationPresent(ConstructorAnnotatin.class)) {
        ConstructorAnnotatin annotation = constructor.getAnnotation(ConstructorAnnotatin.class);
        System.out.println(annotation.constructorName()+"\t"+annotation.remark());
      }
    }
    System.out.println("无");
  }
  
}
```

```
==============类注解解析==============
personBean  1.2.1
==============成员变量注解解析==============
description  This is my personal annotation
==============成员方法注解解析==============
sayHello  TYPE2
==============构造器注解解析==============
无
```

### 七、自定义注解实战🐱‍🏍

**注解大多时候与反射或者 AOP 切面结合使用**，它的作用有很多，比如**标记和检查**，最重要的一点就是**简化代码，降低耦合性，提高执行效率**。

#### 7.1、自定义注解 + SpringMVC 拦截器实现权限控制功能

还有一种应用场景，权限判断或者说是登录校验。

> 这个是我当时还没有学习市面上的权限框架，就是使用了这种自定义注解+拦截器的方式来实现简单的权限控制。

`注意：`此案例不可CV直接运行，代码很容易实现，大家理解思路即可。

***

定义注解：

```
@Target({ElementType.METHOD,ElementType.TYPE}) // 这个注解可以放在也可以放在方法上的。
@Retention(RetentionPolicy.RUNTIME)
public @interface Authority {
    Role[] roles() ;
}
```

```
public enum Role {
    SADMIN,
    ADMIN,
    TEACHER,
    STUDENT
}
```

***

使用注解：

```
@Authority(roles = {Role.ADMIN, Role.SADMIN}) // 放在类上 说明这个类下所有的方法都需要有这个权限才可以进行访问
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/hello")
    public String Hello(){
        return "hello 你最近还好吗";
    }
}
```

```
@Controller
@RequestMapping("/student")
public class StudentController {


  @Authority(roles = {Role.STUDENT}) // 放在方法上则说明此方法需要注解上的权限才能进行访问
    @GetMapping("/test")
    public String test(){
        return "你好，我已经不是一名学生啦";
    }

}
```

***

编写 ` SpringMVC  `拦截器及处理注解的`Handler`

在其中进行 Token 的判断，和访问方法的权限判断，看方法上是否有注解，有的话，

就和当前用户对比，成功就可以访问，失败就直接拒绝。

> 当时用的是`SSM`框架，所以才会看到有 `response.sendRedirect(contextPath + "/login");`这样的。

```
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String url = request.getRequestURI();
//        log.info(request.getMethod()+" 请求URL："+url);

        //从Token中解析User信息
        User user = TokenUtil.verifyToken(request);

        String contextPath = request.getContextPath();
        //user 为空则 表示 Token 不存在
        if (user != null) {
            if (user.getRole().equals("sadmin")) {
                //检查方法上 是否有注解的 Role.SADMIN 或者 Role.ADMIN 权限 , 没有则检查类上有没有 如果符合要求则放行
                if (HandlerUitl.checkAuthority(handler, new Role[]{Role.SADMIN, Role.ADMIN})) {
                    request.setAttribute("user", user);
                    return true;
                }
            }
            if (user.getRole().equals("admin")) {
                if (HandlerUitl.checkAuthority(handler, new Role[]{Role.ADMIN})) {
                    request.setAttribute("user", user);
                    return true;
                }else {
                    response.sendRedirect(contextPath + "/login");
                }
            }

            if (user.getRole().equals("teacher")) {
                if (HandlerUitl.checkAuthority(handler, new Role[]{Role.TEACHER})) {

                    return true;
                } else {
                    response.sendRedirect(contextPath + "/login");
                }
            }
            if (user.getRole().equals("student")) {
                if (HandlerUitl.checkAuthority(handler, new Role[]{Role.STUDENT})) {

                    return true;
                } else {

                    response.sendRedirect(contextPath + "/student/login");
                }
            }
        }else {
            response.sendRedirect(contextPath + "/login");
        }


        return false;
    }
}
```

-   用于检查 方法 或者 类 是否需要权限
-   并和 拥有的权限做对比
-   如果方法上有 ，则以方法的 优先

```
public class HandlerUitl {

    public static boolean checkAuthority(Object handler, Role[] roles1){
            if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法上的注解
            Authority authority = handlerMethod.getMethod().getAnnotation(Authority.class);
            // 如果方法上的注解为空 则获取类的注解
            if (authority == null) {
                authority = handlerMethod.getMethod().getDeclaringClass().getAnnotation(Authority.class);
            }
            // 如果标记了注解，则判断权限
            if (authority != null) {
                Role[] roles = authority.roles();
                //如果 方法权限为 0 则通过
                if(roles.length==0){
                    return true;
                }
                //判断 拥有的权限 是否 符合 方法所需权限
                for(int i = 0; i < roles.length; i++){
                    for(int j = 0; j < roles1.length; j++){
                        if(roles[i]==roles1[j]){
//                            System.out.println("可以访问");
                            return true;
                        }
                    }
                }

            }
            return false;
        }
        return true;

    }

}
```

#### 7.2、自定义注解+AOP+Redis 防止重复提交

先简单说一下防止重复提交注解的逻辑：

1.  在需要防止重复提交的接口的方法，加上注解。
1.  发送请求写接口携带 Token
1.  请求的路径+ Token 拼接程 key，value 值为生成的 UUID 码
1.  然后 `set Redis` 分布式锁，能获取到就顺利提交（分布式锁默认 5 秒过期），不能获取就是重复提交了，报错。

定义注解

```
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {

    /**
     * 设置请求锁定时间
     * @return
     */
    int lockTime() default 5;
}
```

定义处理注解的切面类

```
import com.eshop.api.ApiResult;
import com.eshop.common.aop.NoRepeatSubmit;
import com.eshop.common.util.RedisLock;
import com.eshop.common.util.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 重复提交aop
 */
@Aspect
@Component
@Slf4j
public class RepeatSubmitAspect {

    @Autowired
    private RedisLock redisLock;

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    @Around("pointCut(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint pjp, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        int lockSeconds = noRepeatSubmit.lockTime();

        HttpServletRequest request = RequestUtils.getRequest();
        Assert.notNull(request, "request can not null");

        String bearerToken = request.getHeader("Authorization");
        String[] tokens = bearerToken.split(" ");
        String token = tokens[1];
        String path = request.getServletPath();
        String key = getKey(token, path);
        String clientId = getClientId();

        boolean isSuccess = redisLock.tryLock(key, clientId, lockSeconds);
        log.info("tryLock key = [{}], clientId = [{}]", key, clientId);

        if (isSuccess) {
            log.info("tryLock success, key = [{}], clientId = [{}]", key, clientId);
            // 获取锁成功
            Object result;

            try {
                // 执行进程
                result = pjp.proceed();
            } finally {
                // 解锁
                redisLock.releaseLock(key, clientId);
                log.info("releaseLock success, key = [{}], clientId = [{}]", key, clientId);
            }
            return result;
        } else {
            // 获取锁失败，认为是重复提交的请求
            log.info("tryLock fail, key = [{}]", key);
            return  ApiResult.fail("重复请求，请稍后再试");
        }
    }

    private String getKey(String token, String path) {
        return token + path;
    }

    private String getClientId() {
        return UUID.randomUUID().toString();
    }

}
```

```
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * Redis 分布式锁实现
 */
@Service
public class RedisLock {

    private static final Long RELEASE_SUCCESS = 1L;
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    // 当前设置 过期时间单位, EX = seconds; PX = milliseconds
    private static final String SET_WITH_EXPIRE_TIME = "EX";
    // if get(key) == value return del(key)
    private static final String RELEASE_LOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 该加锁方法仅针对单实例 Redis 可实现分布式加锁
     * 对于 Redis 集群则无法使用
     *
     * 支持重复，线程安全
     *
     * @param lockKey   加锁键
     * @param clientId  加锁客户端唯一标识(采用UUID)
     * @param seconds   锁过期时间
     * @return
     */
    public boolean tryLock(String lockKey, String clientId, long seconds) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            SetParams setParams = new SetParams();
            String result = jedis.set(lockKey, clientId, setParams.nx().px(seconds));
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        });
    }

    /**
     * 与 tryLock 相对应，用作释放锁
     *
     * @param lockKey
     * @param clientId
     * @return
     */
    public boolean releaseLock(String lockKey, String clientId) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            Jedis jedis = (Jedis) redisConnection.getNativeConnection();
            Object result = jedis.eval(RELEASE_LOCK_SCRIPT, Collections.singletonList(lockKey),
                    Collections.singletonList(clientId));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        });
    }
}
```

使用注解

```
/**
     * 添加收藏
     */
@NoRepeatSubmit
@PostMapping("/collect/add")
@ApiOperation(value = "添加收藏",notes = "添加收藏")
public ApiResult<Boolean> collectAdd(@Validated @RequestBody StoreProductRelationQueryParam param){
    // 处理业务逻辑
    return ApiResult.ok();
}
```

#### 7.3、自定义注解 + Aop 实现日志收集

有关于这个，我之前有写过一篇文章，就不再此处特意贴出来增加篇幅啦。

[自定义注解 + Aop 实现日志收集](https://juejin.cn/post/6996480742523928583)

***

### 八、自言自语💌

原本还想找点面试题的，但是到处找了找，面试大部分也就是面试上面这些知识点，所以就删掉啦。

> 本篇主要是针对`Java`运行时的注解的讲解及应用，但是你想一想，我们使用`lombok`的注解时，它的实现原理又是什么样的呢？为什么可以帮我们自动生成代码呢？是谁给我们做了这件事情呢？

下篇主要是针对上述的几个疑问来展开的，文章的大纲和构思倒是有点想法，但是不知道能不能写好下篇。

另外Java注解的下半场，主要是围绕着 ` AbstractProcessor  `相关来讲，其实也算是冷门知识了，但是好奇心还是要有的。

> 也非常感谢大家的阅读，觉得有所收获的话，可以点点赞，或者留下评论，让我收到你的反馈吧
>
> 下篇文章见。

参考

-   [Java元注解 - 生命周期 @Retention](https://blog.51cto.com/u_10705830/2164430)
-   [JAVA注解开发（精讲）](https://blog.csdn.net/qq_30347133/article/details/83686068)
-   [Java 注解完全解析](https://blog.csdn.net/siutony/article/details/118227018?spm=1001.2014.3001.5506)
-   [面试官：什么是 Java 注解？](https://cloud.tencent.com/developer/article/1935630)
-   [java自定义注解解析及相关场景实现](https://blog.csdn.net/Andyzhu_2005/article/details/81355399?spm=1001.2014.3001.5506)
-   [【对线面试官】Java注解](https://juejin.cn/post/6909692344291819533)


我正在参与掘金技术社区创作者签约计划招募活动，[点击链接报名投稿](https://juejin.cn/post/7112770927082864653 "https://juejin.cn/post/7112770927082864653")。
