# 犯错总结--工厂模式和策略模式傻傻没分清

> 这篇文章出现的缘由是因为昨天我写的那篇[将策略模式应用到项目的文章](https://juejin.cn/post/7158472864499236900)的一位掘友的评论。 现在标题已经修正为`将简单工厂模式改造应用到项目中`

起初我以为是掘友分不清工厂模式和策略模式的区别，实际上是我自己傻傻分不清。

![image-20221026222736607](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221026222736607.png)



我在咋一眼看到这条评论时，都懵了，~~就差点怀疑自己写错了~~（这次可以说，把怀疑去掉，自信点说就是写错了）

也是这条评论让我再一次回到住房之后仔细去看了工厂模式的实现，以及策略模式和工厂模式的区别。

写下了这篇犯错总结。

## 策略模式

**策略（Strategy）模式**的定义：**该模式定义了一系列算法，并将每个算法封装起来，使它们可以相互替换，且算法的变化不会影响使用算法的客户**。策略模式属于`对象行为模式`，它通过对算法进行封装，把使用算法的责任和算法的实现分割开来，并委派给不同的对象对这些算法进行管理。

### 使用场景：

1、 多个类只区别在表现行为不同，可以使用`Strategy`模式，在运行时动态选择具体要执行的行为。

2、 需要在不同情况下使用不同的策略(算法)，或者策略还可能在未来用其它方式来实现。

3、 对客户隐藏具体策略(算法)的实现细节，彼此完全独立。

4、如果一个对象有很多的行为，如果不用恰当的模式，这些行为就只好使用多重的条件选择语句来实现。

## 策略模式的案例

【例】促销活动

一家百货公司在定年度的促销活动。针对不同的节日（春节、中秋节、圣诞节）推出不同的促销活动，由促销员将促销活动展示给客户。

定义相关接口和实现类：

```java
public interface FestivalStrategy {

    FestivalEnum getFestivalEnum();

    void show();
}
```

```java
@Service
public class NewYearStrategy implements FestivalStrategy{

    @Override
    public FestivalEnum getFestivalEnum() {
        return FestivalEnum.CHINESE_NEW_YEAR;
    }

    @Override
    public void show() {
        System.out.println("春节活动：买一送一");
    }
}

//为中秋准备的促销活动B
@Service
public class MidAutumnStrategy implements FestivalStrategy {

    @Override
    public FestivalEnum getFestivalEnum() {
        return FestivalEnum.MID_AUTUMN;
    }

    @Override
    public void show() {
        System.out.println("中秋活动：满200元减50元");
    }
}

@Service
public class ChristmasStrategy implements FestivalStrategy {

    @Override
    public FestivalEnum getFestivalEnum() {
        return FestivalEnum.CHRISTMAS;
    }

    @Override
    public void show() {
        System.out.println("圣诞活动：满1000元加一元换购任意200元以下商品");
    }
}
```

### 使用：

```java
@Component
public class FestivalStrategyUseService implements ApplicationContextAware {
    private Map<FestivalEnum,FestivalStrategy>  festivalStrategyMap =new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, FestivalStrategy> tmepMap = applicationContext.getBeansOfType(FestivalStrategy.class);
        tmepMap.values().forEach(strategyService -> festivalStrategyMap.put(strategyService.getFestivalEnum(), strategyService));
    }

    public void showFestivalAndContext(FestivalEnum festivalEnum) {
        FestivalStrategy festivalStrategy = festivalStrategyMap.get(festivalEnum);
        if (festivalStrategy != null) {
            System.out.println("现在正值"+festivalStrategy.getFestivalEnum().getName()+"佳节");
            festivalStrategy.show();
        }
    }
}
```

```java
@Autowired
FestivalStrategyUseService festivalStrategyUseService;

@Test
public void test3(){
    festivalStrategyUseService.showFestivalAndContext(FestivalEnum.CHINESE_NEW_YEAR);
    /**
         * 现在正值春节佳节
         * 春节活动：买一送一
         */
}
```

上面这种方式，就是策略模式的定义实现和使用。



但是我只要修改`FestivalStrategyUseService`一点点代码，它就变得和工厂模式一样了。

```java
@Component
public class FestivalStrategyUseService implements ApplicationContextAware {
    private Map<FestivalEnum,FestivalStrategy>  festivalStrategyMap =new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, FestivalStrategy> tmepMap = applicationContext.getBeansOfType(FestivalStrategy.class);
        tmepMap.values().forEach(strategyService -> festivalStrategyMap.put(strategyService.getFestivalEnum(), strategyService));
    }

    /**
     * 改为去获取这个对象
     */
    public  FestivalStrategy getFestivalStrategy(FestivalEnum festivalEnum) {
        return  festivalStrategyMap.get(festivalEnum);
    }
}

```

我想要实现刚刚策略模式需要实现的方法调用，则在使用的时候需要：

```java
@Autowired
FestivalStrategyUseService festivalStrategyUseService;

@Test
public void test4(){
    FestivalStrategy festivalStrategy = festivalStrategyUseService.getFestivalStrategy(FestivalEnum.CHINESE_NEW_YEAR);
    System.out.println(festivalStrategy.getFestivalEnum());
    festivalStrategy.show();
    /**
         * 现在正值春节佳节
         * 春节活动：买一送一
         */
}
```

实现的效果是一样的，但是我们具体的调用需要我们自己来规划，而并非是直接像在策略模式中一样，已经定义好相关的规则，我们直接调用策略服务类中的方法即可。

其实写到这里已经可以发现策略模式和工厂模式的不同了。

## 策略模式和工厂方法模式的区别

相似点：两者在结构上确实是十分相似的。

差异点：

- 侧重点不一样，工厂模式主要是返回的接口实现类的实例化对象（关注对象的创建），而策略模式是在实例化策略模式的时候已经创建好（关注对象的封装）。

- 用途不同。策略模式属于**行为型模式**，而工厂模式属于**创建型模式**。

  工厂模式的作用就是用来创建对象，而策略模式是让一个对象在许多行为中选择一种行为，或者说定义了一些行为~

  ![image-20221026214551672](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221026214551672.png)

总结：

工厂模式中只管生产实例，具体怎么使用工厂实例由调用方决定，

策略模式是将生成实例的使用策略放在策略类中配置后才提供调用方使用。 

工厂模式调用方可以直接调用工厂实例的方法属性等，策略模式不能直接调用实例的方法属性，需要在策略类中封装策略后调用。

参考文章：[个人理解简单工厂模式和策略模式的区别](https://blog.csdn.net/lmx125254/article/details/86625960)- 关注一下评论区，不比文章内容少。

## 后语

工厂和策略的区别其实不是很大。用的场景也完全取决于个人。很多时候一些问题工厂和策略模式都可以完成。只不过工厂模式侧重的是对象的创建，而策略模式注重的是算法的拼接。

**实际使用中混合使用占多数**。

任重而道远，我好菜啊。

> 同时今天的错误也给自己敲响了一个警钟，对于内容的正确性，一定要多加审查。一次内容的错误，影响的不仅仅是我个人，而可能是好几位读过文章的朋友。如果是初学者，则会更加不妥当。
>
> **我会吸取教训，争取下次写得更好**~ 
>
> 也希望各位小伙伴们多多提建议~  



写于 2022 年 10 月 26 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)





