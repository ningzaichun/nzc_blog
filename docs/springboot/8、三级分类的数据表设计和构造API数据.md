# 三级分类的数据表设计和构造API数据

>如此的业务需求应该说是每个项目中的基本吧。

诸如下图这种：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/8b00b700c82a442da00f64b7c058e184~tplv-k3u1fbpfcp-zoom-1.image)

一级菜单下有二级菜单和三级菜单。与此类似的应用场景还有很多很多，如省市县的三级联动、后台管理菜单、部门展示等等。

## 一、数据表设计

先看看表结构吧

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/f2041653cf5649f091981d085944f34b~tplv-k3u1fbpfcp-zoom-1.image)

具体的 sql 文件，在贴的源码仓库中有。

其中最重要的就是理清父子层级的关系就好了，知道是个什么样子设计的，如何去管理他们，那这些就都不是事啦。

二级评论的表的设计方法其实也大致如此。

数据表中对应的数据关系大致如下：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e4c94c8e66924b7fa8edd22e9c2ea65f~tplv-k3u1fbpfcp-zoom-1.image)

## 二、项目代码

技术就是常用的 SpringBoot、Mybatis-Plus

另外还用了下Mybatis-Plus逆向生成器，还有Java的函数式编程

### 3.1、导入依赖

完整依赖如下：

```
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>2.5.2</version>
    </parent>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.1</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.2.6</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

        <!--进行Spring Boot配置文件部署时，发出警告Spring Boot Configuration Annotation Processor not configured，但是不影响运行
            它的意思是“Spring Boot配置注解执行器没有配置”，配置注解执行器的好处是什么。
            配置注解执行器配置完成后，当执行类中已经定义了对象和该对象的字段后，在配置文件中对该类赋值时，便会非常方便的弹出提示信息。
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!--mybatis-plus逆向工程-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>3.4.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.velocity</groupId>
            <artifactId>velocity-engine-core</artifactId>
            <version>2.2</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>
```

关于 Mybatis-plus 的版本，最新的那个逆向生成器不太熟，就用了旧版本的。感兴趣的可以去试一试新版本的。

### 3.2、逆向生成代码

```
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 演示例子，执行 main 方法控制台输入模块表名回车自动生成对应项目目录中
public class CodeGenerator {

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        final String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("nzc");
        gc.setOpen(false);
        // gc.setSwagger2(true); 实体属性 Swagger2 注解
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/task1?useUnicode=true&useSSL=false&characterEncoding=utf8");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        mpg.setDataSource(dsc);

        // 包配置
        final PackageConfig pc = new PackageConfig();
        pc.setModuleName(scanner("模块名"));
        pc.setParent("com.nzc");
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 如果模板引擎是 freemarker
        //String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + pc.getModuleName()
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);

        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        // 公共父类
        // 写于父类中的公共字段
        strategy.setSuperEntityColumns("id");
        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix("pms_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }

}
```

运行代码：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c7a7d50ff844486aa8cdf5adba85499e~tplv-k3u1fbpfcp-zoom-1.image)

执行结果

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/597870051a864da3b110d5b47be4c9d9~tplv-k3u1fbpfcp-zoom-1.image)

默认里面是没有方法的话，不过用来写个基本的小demo是非常方便的。

### 3.3、业务代码

controller、mapper、entity都不是重点，一笔带过了哈。

```
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    ICategoryService categoryService;
    /**
     * 查询出所有的数据，以树形结构组装起来
     * @return
     */
    @GetMapping("/tree")
    public Result listTree() {
        List<Category> entities= categoryService.listWithTree();
        return ResultUtil.success(entities);
    }
}
```

```
public interface CategoryMapper extends BaseMapper<Category> {

}
```

```
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pms_category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "cat_id", type = IdType.AUTO)
    private Long catId;

    private String name;

    private Long parentCid;

    private Integer catLevel;

    private Integer showStatus;

    private Integer sort;

    private String icon;

    private String productUnit;

    private Integer productCount;

    @TableField(exist = false)
    private List<Category> categoryChild;

}
```

```
public interface ICategoryService extends IService<Category> {

    /**
     * 封装成三级菜单形式
     * @return
     */
    List<Category> listWithTree();
}
```

真正的业务是在 serviceImpl 中

```
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

//    @Autowired 
//    CategoryMapper categoryMapper;

    @Override
    public List<Category> listWithTree() {
        //1、查询出所有的分类
        List<Category> allCategory = baseMapper.selectList(null);
        //2、组装成父子的树形结构
        //2.1、找到所有的一级分类
        List<Category> categoriesLevel1 = allCategory.stream()
              // filter 方法就如名称一样，就是用来过滤的 
              // 满足条件的会留下，到最后会返回一个流式对象
                .filter(category ->
                        category.getParentCid() == 0
                )
              // map 的理解百话点说就是可以对传入的对象做出修改 
              // 这里的意思就是找出当前一级分类中的二级分类，然后再set进去
                // 最后再返回一个流式对象
                .map((menu) -> {
                    menu.setCategoryChild(getChildrens(menu, allCategory));
                    return menu;
                })
              // 见名知意，这就是排序 ，其实也算是重定义Java比较器
               .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
              // 就是将流转换为带泛型的List对象
                .collect(Collectors.toList());
        return categoriesLevel1;
    }


    private List<Category> getChildrens(Category root, List<Category> all) {
        // 这里的逻辑和上面一段是一样的，这里是构造三级、四级等等多层也是一样构造的
        List<Category> collect = all.stream()
                .filter(category -> {
                    return category.getParentCid().equals(root.getCatId());
                }).map((menu) -> {
                    menu.setCategoryChild(getChildrens(menu, all));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return collect;
    }
}
```

还有一些公共代码和配置文件什么的，在仓库中都是有的，这里不再重复贴出来啦。

### 3.4、测试结果

我就是使用 Postman 测试的，没有专门画前端，前端太弱鸡啦，求大佬们放过...

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ebae8dbb2a2c47169efefe514e0ec101~tplv-k3u1fbpfcp-zoom-1.image)

源码：[github](https://github.com/ningzaichun/tree_menu_demo) 、 [gitee](https://gitee.com/crushlxb/tree_menu_demo)

## 三、自言自语

真正到了工作的时候，你会发现你写代码和平常还是会有很大差别的，最主要就是体现在业务场景这个方面。

写 Demo 的时候，主要就想着能写出个 Demo 就不错了，但实际上真到了该上业务的时候，综合起来还是有很多值得思考的问题。

在学习的时候针对一些必要的技术还是可以往深里挖掘挖掘的，并且自己也要学会根据现学的东西想到应用场景，思考如何整合项目，那些地方还有不足的，项目的扩展性如何等等，这些可以不用实现，但是在学习的时候能够多一些思考，这对于学习一个新技术是很有必要的。
