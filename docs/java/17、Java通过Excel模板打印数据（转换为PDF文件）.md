# Java 读取 Excel 模板转换为PDF文件（实用）

> 昨天写了篇较为粗糙的文章，👉[Java 将 Excel 转换为 PDF 文件](https://juejin.cn/post/7159593527414882340) 完成了 Excel 文件转为 PDF 文件的操作
>
> 但实际上那还只是解决了我的后半部分的问题~

我实际遇到的问题：**在页面的表格中，选中一行数据，需要执行打印预览**(即PDF文件预览)

从一开始我就把一行一行的编辑表格的方式~~给pass掉了~~，太麻烦了，也没有办法进行复用。

## 前言

我在互联网上冲浪的时候，看到有的小伙伴说可以使用`Excel`模板，来实现这个功能。

这个方式我是喜欢的~  （ps：因为不要用代码去画表格，是真的舒服~）

大致流程如下：

- 编写一个 Excel  的模板，就相当于先在excel文件中，把表格信息都弄好，以及相关的占位符。
- 使用 Java 读取这个 Excel 文件，并将数据塞进 Excel 表格中
- 最后就是将这个 Excel 文件转为PDF文件 （昨天已经实现了）

先说说我使用的相关jar包

```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.24</version>
    </dependency>
    <dependency>
        <groupId>net.sf.jxls</groupId>
        <artifactId>jxls-core</artifactId>
        <version>1.0.6</version>
    </dependency>
      <!--这个jar需要看我上一篇文章 -->
    <dependency>
        <groupId>com.aspose</groupId>
        <artifactId>aspose-cells</artifactId>
        <version>8.6.2</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.poi/poi -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi</artifactId>
        <version>5.2.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.3</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml-schemas -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml-schemas</artifactId>
        <version>4.1.2</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.24</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.6.1</version>
    </dependency>
</dependencies>
```

这些jar包，虽然我都去找了最新的`jar`，但不得不说，我现在的解决方式应该不能算是一个好的方式，因为引入的`net.sf.jxls`相关的`jar`包已经在 2014 年停止维护了😂。

其他的方式我也还在找，其实不是没有好的方式吧，只能说是免费的不太行。

有好的方式滴我一下，感谢各位大佬们~

## 编码

### excel 模板文件

先看看excel 表格的模板：

![image-20221029233444916](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029233444916.png)

里面的`${data.titleBean.date}`都是占位符，稍后在程序中，会进行填充。

昨天已经实现了 Excel 表格转换为PDF文件的功能~

文章链接：

今天的任务就是读取这个Excel表格模板，将数组组装好，然后塞进去，再转成PDF文件，就完事啦~  冲！！！

### 修改 net.sf.jxls 包下的代码

在开始之前，我们需要改写修改一下`net.sf.jxls`包下的几个类，否则会一直报错。

比如：` net.sf.jxls.transformer`包下的`CellTransformer`

![image-20221029225911330](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029225911330.png)

这个`Cell.CELL_TYPE_BLANK`在之后的`org.apache.poi`包中已经被移除了。

所以如果要使用的话，就需要对这些地方做一些覆盖，

- 可以直接修改源码，再重新打包，
- 也可以像我这样，直接在项目中建立一个同名的包、类，然后进行修改，实现覆盖的效果。

![image-20221029230342334](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029230342334.png)

代码修改的地方都很简单，但是代码本身的量很大，贴出来太麻烦啦。

说一下覆盖的方法：

- 首先建立同名的包和类（就是上面的几个类要动，其他的暂时还没发觉）
- 直接从源码中将代码拷贝进来
- 针对爆红的地方修改就好了（修改的对应规则在后文）

比如源代码中的几个问题：

![image-20221029231036290](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029231036290.png)

![image-20221029231118097](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029231118097.png)

源码中`Cell.CELL_TYPE_BLANK、CELL_TYPE_STRING `等等对应的值：

| CELL_TYPE_NUMERIC | 数值     | 0    |
| ----------------- | -------- | ---- |
| CELL_TYPE_STRING  | 字符串型 | 1    |
| CELL_TYPE_FORMULA | 公式型   | 2    |
| CELL_TYPE_BLANK   | 空值     | 3    |
| CELL_TYPE_BOOLEAN | 布尔型   | 4    |
| CELL_TYPE_ERROR   | 错误     | 5    |

我们将它与之对应修改为：

```java
public enum CellType {
    @Internal(
        since = "POI 3.15 beta 3"
    )
    _NONE(-1),
    NUMERIC(0),
    STRING(1),
    FORMULA(2),
    BLANK(3),
    BOOLEAN(4),
    ERROR(5);
}
```

比如`Cell.CELL_TYPE_NUMERIC ` 就改成`CellType.NUMERIC`

其他同上~ 

### 组装数据 & 测试

测试代码

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年10月29日 10:32
 */
public class TestExcelToPdf {

    public static void main(String[] args) throws IOException, InvalidFormatException {
        List<Map<String, Object>> objects = new ArrayList<>();
        // sheetTitle 几个表的意思 我这里模拟的是照着这个模板打印三个 为了演示这个参数
        int sheetTitle = 1;
        // 将下面的循环中 i<=3 改为 i<=1 ，则打印一遍
        for (int i = 1; i <=1; i++) {
            TitleBean titleBean = new TitleBean("宁在春", new Date(), "掘金", "春春", new Date());
            List< Bill> list = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                list.add(new Bill(""+i,"ningzaichun"+i,"给我点个赞吧~"));
            }
            Map<String, Object> printMap = new LinkedHashMap<>();
            printMap.put("listBean", list);
            printMap.put("titleBean", titleBean);
            printMap.put("sheetTitle", sheetTitle++);
            objects.add(printMap);
        }
        //临时excel路径
        String templatePath = "E:\\789.xls";
        InputStream is = new FileInputStream(templatePath);
        List<String> listSheetNames = new ArrayList<>();
        for (Map<String, Object> object : objects) {
            listSheetNames.add(object.get("sheetTitle").toString());
        }
        String targetPath = "D:\\ceshi\\123.xls";
        XLSTransformer transformer1 = new XLSTransformer();
        Map<String, String> beans = new LinkedHashMap<>();
        Workbook workbook = transformer1.transformMultipleSheetsList(is, objects, listSheetNames, "data", beans, 0);
        workbook.write(new FileOutputStream(targetPath));
        ExcelToPdf.convertPDF(true, targetPath);
    }
}
```

`补充`：如果需要更高的复用性，可以直接使用Map包裹，这样可以直接让这个方法变得通用。（ps：此处是为了更好且更简单的阅读体验而修改）

`ExcelToPdf.convertPDF(true, targetPath);`是在这昨天的这篇文章中👉[Java 将 Excel 转换为 PDF 文件](https://juejin.cn/post/7159593527414882340)

相关的两个实体类：（excel 模板表格中的所有的字段名都是与实体类中一一对应的）

```java
@Data
@AllArgsConstructor
public class Bill {
    private String  rowNo;
    private String  productName;
    private String  remark;
}

@Data
@AllArgsConstructor
public class TitleBean {
private String  customerName;
    private Date billDate;
    private String  saleArea;
    private String  createUserName;
    private Date createDate;
}
```

测试结果：

![image-20221029234352309](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029234352309.png)

我没有做什么输出，只要是正常结束即表示成功。

那个`log4j2`什么的报错，降之前的引入日志包的版本即可，我看着不影响就没有去降了。

![image-20221029234531321](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221029234531321.png)

  

从结果来看，是可以正常将数据填入，并且实现转换，但一些问题还是存在的~    	

## 还存在的问题

目前还存在的问题：

- 很多样式还没有进行测试，不知道是否都支持
- 一些样式的调整，表格位于PDF文件的位置需要调整（如边框距，上下边框）
- 字体的支持等等

反正目前先写到这吧~

## 后记

今天就写到了这里啦~ 感觉自己还好菜啊~  一起努力哦~

**希望你是满载而归的**~

**周六就这样的度过了，还没有好好感受片刻，就已经消失**。 

> 写于 2022 年 10 月 29 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)

