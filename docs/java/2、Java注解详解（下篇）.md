# Javac编译注解及分析Lombok的注解实现

![Java注解](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112303182.png)

在上一篇中，我留下了几个疑问，我们使用`lombok`的注解时，为什么加了个注解就可以帮我们自动生成代码呢？是谁给我们做了这件事情呢？它的原理是什么样的呢？

本篇就是以我们最常用的 `lombok`作为主线来引出 `javac` 注解处理器，Lombok 插件注解功能很多，出了有自动 set、get 方法外，还有链式调用、建造者模式等等，但是我们就讨论最简单的 set、get 方法的生成。

## 一、用Lombok引出问题

#### 1.1、引入

1、idea 中打开 settings （快捷键：ctrl+alt+s） ，搜索 plugin ，在 plugins 里面搜索 lombok ，安装

<img src="https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252011.png" alt="image-20220708205632890" style="zoom:50%;" />

2、在项目中引入 lombok 的依赖

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.24</version>
</dependency>
```



#### 1.2、优缺

Lombok 是一个 Java 库，能自动插入编辑器并构建工具，简化 Java 开发。通过添加注解的方式，不需要为类编写 getter或 eques 方法，同时可以自动化日志变量。[官网链接](https://www.projectlombok.org/)

**优点**：简化 Java 开发，减少了许多重复代码。

**缺点**：

- 降低了源码的可读性和完整性；

- 有可能会破坏封装性，因为有些属性并不需要向外暴露； 

- 降低了可调试性；

  Lombok 会帮我们自动生成很多代码，但这些代码是在编译期生成的，因此在开发和调试阶段这些代码可能是“丢失的”，这就给调试代码带来了很大的不便。

如果不考虑的那么严谨，我觉得还是要用的，因为我懒。

#### 1.3、使用

写一个类来分析一下：

我们自己手写的一个`JavaBean`

```java
/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年07月06日 20:23
 */

public class Student {

    private String name;

    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

```

用了 lombok 注解的 JavaBean

```java
/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年07月06日 20:23
 */
@Data
public class StudentLombok {
    private String name;
    private String age;
}
```

我们编译一下，Idea 中点击顶部菜单 Build ，下拉选择  `Recompile` 看看他们生成的 class文件是什么样的。

![image-20220708210057363](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252984.png)

![image-20220708210351664](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252033.png)

可以明显看出，使用了 @Setter、@Getter 注解后，和我们手动编写的 Java 代码，编译完的结果是一样的。

它直接帮我们生成了这些方法，这些步骤究竟是谁做的勒？我们是否也可以自己编写这样的注解呢？

## 二、Lombok 原理分析

其实这里面用到了 AOP 编程的编译时织入技术，就是在编译的时候修改最终 class 文件。

大部分的程序代码从开始编译到最终转化成物理机的目标代码或虚拟机能执行的指令集之前，都会按照如下图所示的各个步骤进行：

![image-20220708213204555](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252476.png)

Javac 的编译过程

![image-20220708213142498](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252584.png)

归纳起来主要是由以下三个过程组成：

- 分析和输入到符号表
- 注解处理
- 语义分析和生成 class 文件

而Lombok 正是利用**注解处理**这一步来进行实现的。Lombok 使用的是 JDK 6 实现的 JSR 269: Pluggable Annotation Processing API (编译期的注解处理器) ，它允许在编译期处理注解，读取、修改、添加抽象语法树中的内容。

其实说到这里，我们还只是知道它是在这一步处理的，但如何处理的，我们还是一无所知。

稍后我们会手动实现 Lombok 中的 @Getter、@Setter 注解，这里先事先说明可能会牵扯到的知识。

- 主要使用到的都是 jdk 源码的 tools.ja 包
- 使用的 api 主要是`com.sun.tools.javac`包下的
- 抽象语法 JCTree 使用

不懂也没关系，我也不是很懂，哈哈，我也只是因为好奇，才来探寻的

其中最主要的就是牵扯到的`AbstractProcessor`抽象注解处理类，还有就是 JCTree 相关的api，这些的话，我也用的不多，不敢胡乱发言。

> 要实现注解处理器首先要做的就是继承抽象类 javax.annotation.processing.AbstractProcessor，然后重写它的 process() 方法，process() 方法是 javac 编译器在执行注解处理器代码时要执行的过程。

```java
/**
一个抽象注释处理器，旨在成为大多数具体注释处理器的方便超类。
 */
public abstract class AbstractProcessor implements Processor {
    /**
     * Processing environment providing by the tool framework.
     */
    protected ProcessingEnvironment processingEnv;
    private boolean initialized = false;


    /**
   如果处理器类使用SupportedOptions进行注释，则返回一个不可修改的集合，该集合与注释的字符串集相同。
   如果类没有这样注释，则返回一个空集
     */
    public Set<String> getSupportedOptions() {
        SupportedOptions so = this.getClass().getAnnotation(SupportedOptions.class);
        if  (so == null)
            return Collections.emptySet();
        else
            return arrayToSet(so.value());
    }

    /**
如果处理器类使用SupportedAnnotationTypes进行注释，则返回一个不可修改的集合，
该集合具有与注释相同的字符串集。如果类没有这样注释，则返回一个空集。
return：
此处理器支持的注释类型的名称，如果没有则为空集
     */
    public Set<String> getSupportedAnnotationTypes() {
            SupportedAnnotationTypes sat = this.getClass().getAnnotation(SupportedAnnotationTypes.class);
            if  (sat == null) {
                if (isInitialized())
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                             "No SupportedAnnotationTypes annotation " +
                                                             "found on " + this.getClass().getName() +
                                                             ", returning an empty set.");
                return Collections.emptySet();
            }
            else
                return arrayToSet(sat.value());
        }

    /**
如果处理器类使用SupportedSourceVersion进行注解，则在注解中返回源版本。
如果类没有这样注释，则返回SourceVersion.RELEASE_6 
     */
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv = null;
        if (ssv == null) {
            sv = SourceVersion.RELEASE_6;
            if (isInitialized())
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                                                         "No SupportedSourceVersion annotation " +
                                                         "found on " + this.getClass().getName() +
                                                         ", returning " + sv + ".");
        } else
            sv = ssv.value();
        return sv;
    }

    /**
该方法有两个参数，“annotations” 表示此处理器所要处理的注解集合；
“roundEnv” 表示当前这个 Round 中的语法树节点，
每个语法树节点都表示一个 Element（javax.lang.model.element.ElementKind 可以查看到相关 Element）。
     */
    public abstract boolean process(Set<? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv);

  
}

```



另外还有这两个用来配合的 注解：

```java
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
```

`@SupportedAnnotationTypes` 表示注解处理器对哪些注解感兴趣，“*” 表示对所有的注解都感兴趣；`@SupportedSourceVersion` 指出这个注解处理器可以处理最高哪个版本的 Java 代码。



## 三、简易版 Lombok 实现

#### 简要说明

先说说我们要实现的东西，为了简单的去理解，我这里只讨论get、set 方法，其实里面的实现都差不多，如果偏要说不同的话，就是调用的javac的api不同吧。

写了两个注解 ：`@MyGetter` 和 `@MySetter` 和他们的处理器 `MyAnnotationProcessor`

注解处理器，顾名思义就是用来处理注解的啦。

项目结构：

![image-20220710022004491](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252209.png)

由于是maven项目，这里面引用了com.sun.tools的东西，所以，需要在maven的pom文件里面加上，这样，在使用maven打包的时候，才不会报错。

```java
<dependency>
    <groupId>com.sun</groupId>
    <artifactId>tools</artifactId>
    <version>1.8</version>
    <scope>system</scope>
    <systemPath>jdk路径/lib/tools.jar</systemPath>
 </dependency>
```

我们这里利用 `Java SPI` 加载自定义注解器的方式，生成一个 jar 包，类似于 Lombok ，这样之后其它应用一旦引用了这个 jar 包，自定义注解器就能自动生效了。

SPI是java提供的一种服务发现的标准，具体请看[SPI介绍](https://www.cnblogs.com/strongmore/p/13284433.html)，但每次我们都需要自己创建services目录，以及配置文件，google的autoservice就可以帮我们省去这一步。

```xml
    <dependency>
        <groupId>com.google.auto.service</groupId>
        <artifactId>auto-service</artifactId>
        <version>1.0-rc5</version>
    </dependency>
```

如果你使用Processor（javax.annotation.processing.Processor），并且你的元数据文件被包含在了一个jar包中，同时这个jar包是在javac（java编译）的classpath路径下时，javac会自动的执行通过该方式注入进去的Processor的实现类，以实现对于该项目内的相关数据的扩展。

使用 AutoService 会自动的生成**META-INF./services/javax.annotation.processing.Processor** 文件，并且文件中内容就是我们动态注入进去的类。

然后在编译的时候就会执行对应的扩展方法，同时写入文件。

![image-20220711211243651](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220711211243651.png)



#### 项目代码

代码都很简单，所以除了注解处理器，其他的都没有带啥注释啦哈。

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface MyGetter {
}

```



```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface MySetter {
}

```



```java
package com.nzc.my_annotation;


import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author nzc
 */
@SupportedAnnotationTypes({"com.nzc.my_annotation.MyGetter","com.nzc.my_annotation.MySetter"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class MyAnnotationProcessor extends AbstractProcessor {

    private JavacTrees javacTrees; // 提供了待处理的抽象语法树
    private TreeMaker treeMaker; // 封装了创建AST节点的一些方法
    private Names names; // 提供了创建标识符的方法

    /**
     * 从Context中初始化JavacTrees，TreeMaker，Names
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        javacTrees = JavacTrees.instance(processingEnv);
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 返回使用给定注释类型注释的元素的集合。
        Set<? extends Element> get = roundEnv.getElementsAnnotatedWith(MyGetter.class);
        for (Element element : get) {
            // 获取当前类的抽象语法树
            JCTree tree = javacTrees.getTree(element);
            // 获取抽象语法树的所有节点
            // Visitor 抽象内部类，内部定义了访问各种语法节点的方法
            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    // 在抽象树中找出所有的变量
                    // 过滤，只处理变量类型
                    jcClassDecl.defs.stream()
                            .filter(it -> it.getKind().equals(Tree.Kind.VARIABLE))
                            // 类型强转
                            .map(it -> (JCTree.JCVariableDecl) it)
                            .forEach(it -> {
                                // 对于变量进行生成方法的操作
                                jcClassDecl.defs = jcClassDecl.defs.prepend(genGetterMethod(it));
                            });

                    super.visitClassDef(jcClassDecl);
                }
            });

        }
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(MySetter.class);
        for (Element element : set) {
            JCTree tree = javacTrees.getTree(element);
            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    jcClassDecl.defs.stream()
                            .filter(it -> it.getKind().equals(Tree.Kind.VARIABLE))
                            .map(it -> (JCTree.JCVariableDecl) it)
                            .forEach(it -> {
                                jcClassDecl.defs = jcClassDecl.defs.prepend(genSetterMethod(it));
                            });

                    super.visitClassDef(jcClassDecl);
                }
            });

        }


        return true;
    }

    private JCTree.JCMethodDecl genGetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
        // 生成return语句，return this.xxx
        JCTree.JCReturn returnStatement = treeMaker.Return(
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")),
                        jcVariableDecl.getName()
                )
        );

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(returnStatement);

        // public 方法访问级别修饰
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        // 方法名 getXXX ，根据字段名生成首字母大写的get方法
        Name getMethodName = createGetMethodName(jcVariableDecl.getName());
        // 返回值类型，get类型的返回值类型与字段类型一致
        JCTree.JCExpression returnMethodType = jcVariableDecl.vartype;
        // 生成方法体
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        // 泛型参数列表
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();
        // 参数值列表
        List<JCTree.JCVariableDecl> parameterList = List.nil();
        // 异常抛出列表
        List<JCTree.JCExpression> throwCauseList = List.nil();

        // 生成方法定义树节点
        return treeMaker.MethodDef(
                // 方法访问级别修饰符
                modifiers,
                // get 方法名
                getMethodName,
                // 返回值类型
                returnMethodType,
                // 泛型参数列表
                methodGenericParamList,
                //参数值列表
                parameterList,
                // 异常抛出列表
                throwCauseList,
                // 方法默认体
                body,
                // 默认值
                null
        );

    }

    private JCTree.JCMethodDecl genSetterMethod(JCTree.JCVariableDecl jcVariableDecl) {
        // this.xxx=xxx
        JCTree.JCExpressionStatement statement = treeMaker.Exec(
                treeMaker.Assign(
                        treeMaker.Select(
                                treeMaker.Ident(names.fromString("this")),
                                jcVariableDecl.getName()
                        ),
                        treeMaker.Ident(jcVariableDecl.getName())
                )
        );

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<JCTree.JCStatement>().append(statement);

        // set方法参数
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                // 访问修饰符
                treeMaker.Modifiers(Flags.PARAMETER, List.nil()),
                // 变量名
                jcVariableDecl.name,
                //变量类型
                jcVariableDecl.vartype,
                // 变量初始值
                null
        );

        // 方法访问修饰符 public
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        // 方法名(setXxx)，根据字段名生成首选字母大写的set方法
        Name setMethodName = createSetMethodName(jcVariableDecl.getName());
        // 返回值类型void
        JCTree.JCExpression returnMethodType = treeMaker.Type(new Type.JCVoidType());
        // 生成方法体
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        // 泛型参数列表
        List<JCTree.JCTypeParameter> methodGenericParamList = List.nil();
        // 参数值列表
        List<JCTree.JCVariableDecl> parameterList = List.of(param);
        // 异常抛出列表
        List<JCTree.JCExpression> throwCauseList = List.nil();
        // 生成方法定义语法树节点
        return treeMaker.MethodDef(
                // 方法级别访问修饰符
                modifiers,
                // set 方法名
                setMethodName,
                // 返回值类型
                returnMethodType,
                // 泛型参数列表
                methodGenericParamList,
                // 参数值列表
                parameterList,
                // 异常抛出列表
                throwCauseList,
                // 方法体
                body,
                // 默认值
                null
        );

    }

    private Name createGetMethodName(Name variableName) {
        String fieldName = variableName.toString();
        return names.fromString("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));

    }

    private Name createSetMethodName(Name variableName) {
        String fieldName = variableName.toString();
        return names.fromString("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));

    }
}

```



```java
@MyGetter
@MySetter
public class School {

    private String name;
    private String address;
}
```



测试结果：

![image-20220710024246724](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112252441.png)



**编译**：

编译的话，直接编译根项目就好，我原本想着先编译子项目，再编译另外一个项目，但是会报错，不想纠结了，放出来的，是经过测试的，可以正常编译出来的。

![image-20220710024341769](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202207112310979.png)



## 四、思考

相信大家都有使用Lombok的过程，但是不知道大家有没有注意到我上面的demo是存在一些问题的呢？

我们之前分析了 lombok 它是在编译时为我们添加了诸如 set、get 方法的，但实际上我们在开发的时候就已经可以调用对象上的 set、get 方法啦，这又是如何实现的呢？

> 我个人的测试及思考

这里一定是会牵扯到 idea 中的 lombok 插件的问题，如果你只是引入了 lombok 的依赖，没有安装 lombok 插件的话，那么在 idea 是不会有方法提示的。

我新建了一个空白项目进行测试，如果你引入依赖，没安装插件，idea 只会爆红，但是是可以通过编译的，也不会报错。写一个main 方法也是可以运行的，只不过安装了插件才会提供提示。

按照这个思路，我又返回去测试了上面的那个 Demo，答案是失败的。

测试如下：

1. 创建了 Demo 类，里面写了 main 方法

2. 创建了 School 对象，调用了 set、get 方法

3. 使用 maven 编译，成功过，也失败过（问我也是白问，我都测麻了）

   这个可能跟我的机器环境有关，理论上应该是可以成功的，后来新建的一个项目又是可以的。大家也可以去玩一玩。

4. 启动 main 方法，是直接报错，起不来，原因不知道，如果我还有时间，我再去找找。

```bash
java: java.lang.ClassCastException: class com.sun.proxy.$Proxy26 cannot be cast to class com.sun.tools.javac.processing.JavacProcessingEnvironment (com.sun.proxy.$Proxy26 is in unnamed module of loader java.net.URLClassLoader @4fccd51b; com.sun.tools.javac.processing.JavacProcessingEnvironment is in module jdk.compiler of loader 'app')
```



---

lombok 的插件在其中肯定是做了一些事情的，但是我在各大搜索引擎上搜索这方面的知识，也没有找到相关的一些资料，倒是看到有几个小伙伴问出了和我相似的问题。

如下：大家使用过 lombok 的 `@Slf4j` 注解吧，为什么有了这个注解，我们就可以直接在类里面使用 log 对象，这个对象又是在哪里创建出来的呢？

> 说到这，其实我还是没说出什么道理，因为我也不明白，所以最后这一小节，我的命名才是直接明了的为思考。
>
> 如果有明白的大佬，请不啬赐教，非常感谢！

**此外的补充：**：

其实自定义注解处理器，给我的感觉就像 SpringMVC 中拦截器一样，SpringMVC是拦截请求，自定义注解是拦截在编译前，而且给我的感觉的话，自定义注解编译器应该更好玩，并竟可以改 class 文件，感觉之后还有空的话，会继续整一整这个注解处理器。

## 参考

如果想要了解 JCTree和编译相关的信息的话，可以看下面的这篇文章，写的真的非常详细。

我不是推销啥啥的，只是单纯觉得作者写的非常优秀，值得看。我帮大家确认过，无广告，无推销，阅读体验很好。

[Java-JSR-269-插入式注解处理器](https://liuyehcf.github.io/2018/02/02/Java-JSR-269-%E6%8F%92%E5%85%A5%E5%BC%8F%E6%B3%A8%E8%A7%A3%E5%A4%84%E7%90%86%E5%99%A8/)

![image-20220711224618528](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220711224618528.png)



[Lombok经常用，但是你知道它的原理是什么吗？](https://juejin.cn/post/6844904082084233223)

[Javac 编译过程](https://wiki.jikexueyuan.com/project/java-vm/javac-jit.html)

[JVM系列六（自定义插入式注解器）. ](https://www.cnblogs.com/jmcui/p/12159541.html)

代码参考：《深入理解JVM字节码》





