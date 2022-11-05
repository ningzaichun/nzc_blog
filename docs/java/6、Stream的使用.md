# Stream 的使用，我觉得使用它已经是一个基础

> 如果你了解过 Liunx ，了解过 Liunx 的中管道命令 | ，那么你会发现，其实 Java 8 的 stream 和 Liunx 非常类似的。

Liunx 中的管道命令也是将上一个命令的输出流作为下一条命令的输入流。

## 前言

今天主要聊起的是**如何使用 stream 流**，关于它为什么被引入，有什么样的优势，还有一些平时未曾注意到的知识点的话，就在下一次再讲吧~

> 能基础的使用，是深入了解它的一个基础吧，我觉得~

在本文中，你将会看到Stream API支持的许多操作。这些操作能让你快速完成复杂的数据查 询，如筛选、切片、映射、查找、匹配和归约。

## 一、筛选和切片

### 1.1、筛选 filter

filter 会接受一个  Predicate 接口的参数，其本质就是一个布尔值函数（官方称为谓词，说成白话，即为一个布尔值函数）

准备好的数据~

```java
     static   List<Student> students = new ArrayList<>();

    static {
               students.add(new Student("学生A", "大学1", 18));
        students.add(new Student("学生A", "大学1", 18));
        students.add(new Student("学生A", "大学1", 18));
        students.add(new Student("学生A", "大学1", 18));
        students.add(new Student("学生B", "大学1", 18));
        students.add(new Student("学生C", "大学1", 19));
        students.add(new Student("学生D", "大学2", 20));
        students.add(new Student("学生E", "大学2", 21));
        students.add(new Student("学生F", "大学2", 20));
        students.add(new Student("学生G", "大学3", 22));
        students.add(new Student("学生H", "大学3", 23));
        students.add(new Student("学生I", "大学3", 19));
        students.add(new Student("学生J", "大学4", 20));
    }
```



1、从中筛选出小于20的学生们组成一个新的集合

jdk 8 **之前**的写法：

```java
List<Student> result = new ArrayList<>();
for (Student student : students) {
    if (student.getAge() < 20) {
        result.add(student);
    }
}
```

Jdk 8 及之后的写法：使用stream流操作

```java
/**
     * 选出小于20的学生组成一个集合
     *
     * @param students
     */
private static List<Student> selectAgeLt18(List<Student> students) {
    // 最基础的写法， filter的参数是一个 Predicate,而它是一个FunctionalInterface 式的接口， 唯一的接口就是表示一个参数的谓词（布尔值函数）。
    //        List<Student> list = students.stream().filter(new Predicate<Student>() {
    //            @Override
    //            public boolean test(Student student) {
    //                return student.getAge()<20;
    //            }
    //        }).collect(Collectors.toList());
    // 因此可以简化写成 以下这种写法
    //        List<Student> list = students.stream().filter(student -> {
    //            return student.getAge() < 20;
    //        }).collect(Collectors.toList());
    //又因为filter 的参数实际上是一个lambda表达式，当只有一条返回语句时，又可以省略大括号和return
    List<Student> list = students.stream().filter(student -> student.getAge() < 20).collect(Collectors.toList());
    return list;
}
```

### 1.2、去重 distinct

distinct（）它会返回一个元素各异（根据流所生成元素的 hashCode和equals方法实现）的流。

jdk 8之前对集合的一些去重方式

```java
/**
     * 去重操作，去除掉数据集合中重复的数据
     */
private static void selectSchoolRepresent(List<Student> students) {
    //         jdk 8之前的一些方式，
    //         1、set集合去重
    HashSet<Student> set = new HashSet<>();
    for (Student student : students) {
        set.add(student);
    }
    //         还可以简写成
    List<Student> newList = new ArrayList<>(new HashSet<>(students));

    //         2、 利用 list的contains() 方法
    List<Student> list = new ArrayList<>();
    for (Student student : students) {
        if(!list.contains(student)){
            list.add(student);
        }
    }
}
```



Java 8 及之后使用stream中的 distinct（）方法，其实咋说勒，就是方便，其他的也木有

```java
/**
     * 去重操作，去除掉数据集合中重复的数据
     */
private static void selectSchoolRepresent(List<Student> students) {
    List<Student> collect = students.stream().distinct().collect(Collectors.toList());
    collect.forEach(System.out::println);
}

```



### 1.3、切片 limit

流支持limit(n)方法，该方法会返回一个不超过给定长度的流。

如果流是有序的，则最多会返回前n个元素。无序的则不会以任何方式排序。

Jdk 8 之前的写法

```java
/**
     * 选出集合中前五位同学 组成一个新的集合
     *
     * @param students
     */
private static void selectLimit(List<Student> students) {
    List<Student> list = new ArrayList<>();
    for (int i = 0; i < students.size(); i++) {
        if (i < 5) {
            list.add(students.get(i));
        }
    }
    list.forEach(System.out::println);
}
```



Jdk 8的 stream 流中的 limit 的写法

```java
/**
     * 选出集合中前五位同学 组成一个新的集合
     *
     * @param students
     */
private static void selectLimit(List<Student> students) {

    List<Student> collect = students.stream().limit(5).collect(Collectors.toList());

    collect.forEach(System.out::println);
}
```





### 1.4、跳过元素 skip

流还支持skip(n)方法，返回一个扔掉了前n个元素的流。如果流中元素不足n个，则返回一 个空流。



```java
/**
     * 从第二个同学开始组成新的集合
     *
     * @param students
     */
private static void selectSkip(List<Student> students) {
    List<Student> collect = students.stream().skip(2).collect(Collectors.toList());
    collect.forEach(System.out::println);
    /**
         * Student(name=学生A, school=大学1, age=18, score=90.0)
         * Student(name=学生A, school=大学1, age=18, score=76.0)
         * Student(name=学生B, school=大学1, age=18, score=91.0)
         * Student(name=学生C, school=大学1, age=19, score=65.0)
         * Student(name=学生D, school=大学2, age=20, score=80.0)
         * Student(name=学生E, school=大学2, age=21, score=78.0)
         * Student(name=学生F, school=大学2, age=20, score=67.0)
         * Student(name=学生G, school=大学3, age=22, score=87.0)
         * Student(name=学生H, school=大学3, age=23, score=79.0)
         * Student(name=学生I, school=大学3, age=19, score=92.0)
         * Student(name=学生J, school=大学4, age=20, score=84.0)
         */
}
```

### 1.5、排序 sorted 

这个就是排序啦，没啥能说的啦吧~偷个懒哈

```java
    /**
     * 给这群学生按年龄排序
     * 
     *
     * @param students
     */
    private static void sortedDemo(List<Student> students) {
        List<Student> collect = students.stream()
                .sorted((student1, student2) -> student1.getAge() - student2.getAge())
                .collect(Collectors.toList());
        collect.forEach(System.out::println);

        /**
         * Student(name=学生A, school=大学1, age=18, score=98.0)
         * Student(name=学生A, school=大学1, age=18, score=91.0)
         * Student(name=学生A, school=大学1, age=18, score=90.0)
         * Student(name=学生A, school=大学1, age=18, score=76.0)
         * Student(name=学生B, school=大学1, age=18, score=91.0)
         * Student(name=学生C, school=大学1, age=19, score=65.0)
         * Student(name=学生I, school=大学3, age=19, score=92.0)
         * Student(name=学生D, school=大学2, age=20, score=80.0)
         * Student(name=学生F, school=大学2, age=20, score=67.0)
         * Student(name=学生J, school=大学4, age=20, score=84.0)
         * Student(name=学生E, school=大学2, age=21, score=78.0)
         * Student(name=学生G, school=大学3, age=22, score=87.0)
         * Student(name=学生H, school=大学3, age=23, score=79.0)
         */
    }
```



### 1.6、小结与综合应用

filter 、distinct、limit、skip、sorted 对比起 Java 8 之前的一些实现，从我个人看来是方便了许多的。

如果是看起来不习惯，我觉得可以试着多用上几次，会慢慢爱上它的。

**综合应用**

filter 、distinct、limit、skip、sorted 这些操作，他们的执行结果的返回值仍然是 stream，所以在使用中,他们完全可以无缝链接.

> 如: 我要去这一群学生中找到 年龄在 20 岁以下，分数在90分以上的前3名学生。

```java
    /**
     * 如: 我要去这一群学生中找到 年龄在 20 岁以下，分数在90分以上的前3名学生。
     *
     * @param students
     */
    private static void select(List<Student> students) {
        List<Student> collect = students.stream()
                .filter(student -> student.getAge() < 20)
                .filter(student -> student.getScore() > 90.0)
                .limit(3)
                .collect(Collectors.toList());
        collect.forEach(System.out::println);
        /**
         * Student(name=学生A, school=大学1, age=18, score=98.0)
         * Student(name=学生A, school=大学1, age=18, score=91.0)
         * Student(name=学生B, school=大学1, age=18, score=91.0)
         */
    }
```

## 二、映射 map

这个map的映射其实不光Java 有，JavaScript 也是有的，用法我感觉是一样的~

**一个非常常见的数据处理套路就是从某些对象中选择信息**。比如在SQL里，你可以从表中选 择一列。

用我个人的话来说，filter 是用来过滤元素的，而这一小节的 map 是用来创建一个新的元素。（在官方中的使用的映射一词，是因为map 会接受一个函数作为参数，并且将其映射成一个新的元素。）

> 可能说起来还是不如实践来的实在。

数据还是上一节造的那些数据。

如：找出集合中所有学生的姓名，去除掉重复的名称，组成一个 List<String> 集合

```java
/**
     * 找出集合中所有学生的姓名，去除掉重复的名称，组成一个 List<String> 集合
     *
     * @param students
     */
private static void selectAllStudentName(List<Student> students) {

    List<String> collect = students.stream().map(new Function<Student, String>() {
        @Override
        public String apply(Student student) {
            return student.getName();
        }
    }).distinct().collect(Collectors.toList());

    List<String> list = students.stream().map(student -> {
        return student.getName();
    }).distinct().collect(Collectors.toList());

    List<String> collect1 = students.stream()
        .map(student -> student.getName())
        .distinct()
        .collect(Collectors.toList());
    collect1.forEach(System.out::println);

    /**
         * 学生A
         * 学生B
         * 学生C
         * 学生D
         * 学生E
         * 学生F
         * 学生G
         * 学生H
         * 学生I
         * 学生J
         */
}
```



## 三、查找和匹配

### 3.1、匹配 anyMatch、allMatch和noneMatch 方法

anyMatch方法可以回答“流中是否有一个元素能匹配给定的谓词”

这里的谓词也就是filter那部分所说的一个 布尔值函数。

其实看到 any 的第一眼，大家也明白，任一，只有集合中含有你需要的，那就是返回 true。

```java
    /**
     * 判断这群学生中有木有年龄大于20岁的学生
     *
     * @param students
     */
    private static void anyMatchDemo(List<Student> students) {
        boolean anyMatch = students.stream().anyMatch(student -> student.getAge() > 20);
        System.out.println(anyMatch);
        /**
         * true
         */
    }
```

还有 allMatch 和  noneMatch 他们都和 anyMatch 类似。

allMatch 要求全部元素都满足要求，

noneMatch  则是要求全部元素都不满足要求时返回true。



### 3.2、查找 findAny 与 findFirst

findAny 方法将返回当前流中的任意元素。

它的搭档一般是 filter，和 filter 使用可以实现很多操作。

> 如我想要当确定这群学生中有20岁以上的学生时立马返回结果。

```java
/**
     * 当确定这群学生中有20岁以上的学生时即返回。
     *
     * @param students
     */
private static void findAnyDemo(List<Student> students) {
    Optional<Student> student1 = students.stream().filter(student -> student.getAge() > 20).findAny();
    Student student = student1.get();
    System.out.println(student);
    /**
         * Student(name=学生E, school=大学2, age=21, score=78.0)
         */
}
```

这里的 Optional 是 Java 8 新增的一个 容器类，作用就是用来判断存在和不存在。也就是大家常谈到的更优雅的判空操作。

Optional 几个常见的Api

- `isPresent()`将在Optional包含值的时候返回true, 否则返回false
- `ifPresent(Consumer<T> block)`会在值存在的时候执行给定的代码块。
- `T get()`会在值存在时返回值，否则抛出一个NoSuchElement异常。
- `T orElse(T other)`会在值存在时返回值，否则返回一个默认值。

详细的用法，大家也可以去了解了解，这也是非常好用的一个东东。



findFirst 其实就是确定返回第一个元素。它也和 filter 一起搭配使用。

咋一看， findany 和 findFirst 不是一样吗，其实在你对于返回的第一个元素没有明确要求时，你可以理解成他们确实就是一样的。

**但其实他们真实区别并非体现如此，而是在 stream 中的并行流中**。 

今天没谈这个，大家可以去了解了解，了解并行流就会和常常聊到的性能相关啦，到底那种好一些啥的~

### 3.3、小结

anyMatch、allMatch和noneMatch这三个操作都用到了我们所谓的短路。

就是我们刚学语法时的 && 和 || 运算符，这也算是他们在 stream 的实现。

> 最简单的理解方式，就是他们通过遍历，组成了一个很长很长的布尔表达式。

除去他们能实现短路操作， findAny 与 findFirst 也是同样如此，并非都需要遍历结束才会得到最终的结果。只要在其中某一次中达成条件，即可返回结果。

## 四、归约

官方的说法，成为归约，如果用简单的话语来说的话，可以理解为将多个东西归为一堆。

### 4.1、元素求和 reduce



```java
    private static void reduceDemo() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Integer reduce = list.stream().reduce(0, (a, b) -> a + b);
        System.out.println("list集合的总和：==>" + reduce);

        Integer reduce1 = list.stream().reduce(1, (a, b) -> a * b);
        System.out.println("list集合中的元素相乘结果==>" + reduce1);

        Optional<Integer> reduce2 = list.stream().reduce((a, b) -> a + b);
        Integer integer = reduce2.get();
        System.out.println("list 集合的总和==>"+integer);
        /**
         * list集合的总和：==>55
         * list集合中的元素相乘结果==>3628800
         * list 集合的总和==>55
         */
    }

```

reduce接受两个参数： 

一个初始值，这里是0；

 一个 BinaryOperator 来将两个元素结合起来产生一个新值，BinaryOperator 也是funcational 接口，所以也可以使用lambda 表达式 lambda  (a, b) -> a + b 来表示。

```java
Integer reduce = list.stream().reduce(0, (a, b) -> a + b);
```

另外还有一个重载函数，就是没有初始值版本的，它的返回值是`Optional<Integer>` 的容器类。

```java
  Optional<Integer> reduce2 = list.stream().reduce((a, b) -> a + b);
```



最大值与最小值：

```java
private static void reduceDemo2(){
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    Optional<Integer> max = list.stream().reduce(Integer::max);
    Optional<Integer> min = list.stream().reduce(Integer::min);
    System.out.println("max==>"+max.get());
    System.out.println("min==>"+min.get());
    /**
         * max==>10
         * min==>1
         */
}
```



## 后记

> **其实  stream 的操作还有很多，这一篇算不上结束，真正意义而言，它也算不得开始**。
>
> 不过谈起 stream 流，对于操作部分我倒是觉得它是最简单的模块，如果你感兴趣的话，我更建议你去深层次的思考，它为什么引入、它是为了解决什么问题而存在、lambda函数的设计、lambda 函数推导以及在此基础上有没有更优的方式。

说这么多，其实这也是我追求的。

**希望你有收获，那将会是我最大的快乐**。

写于 2022 年 8 月 29 日晚，作者：宁在春





