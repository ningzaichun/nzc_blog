# Java 将 Excel 转换为 PDF 文件

今天这是篇粗糙的文章，文字也较少，整理了个Java将Excel 转换为 PDF 文件的工具类。（还非常粗糙~）

用的是 `aspose` 和`pdfbox`实现的。

`aspose`是没办法在公开的`Maven`仓库下载的。得去它官网下载，或者是去互联网上搜一搜~  

## 获取 Aspose

官网地址：[aspose](https://releases.aspose.com/pdf/java/)

我是在网上冲浪拿到的~ ，有需求的私我就好~ 

其中里面的 `license.xml` 文件，是参考下面文章获得：

[Java操作excel转pdf工具类](https://blog.csdn.net/m0_58596092/article/details/127472171) 👈这篇文章中的代码是失败的，我帮你验证了...

```java
<License>
  <Data>
    <Products>
      <Product>Aspose.Total for Java</Product>
      <Product>Aspose.Words for Java</Product>
    </Products>
    <EditionType>Enterprise</EditionType>
    <SubscriptionExpiry>20991231</SubscriptionExpiry>
    <LicenseExpiry>20991231</LicenseExpiry>
    <SerialNumber>8bfe198c-7f0c-4ef8-8ff0-acc3237bf0d7</SerialNumber>
  </Data>
  <Signature>sNLLKGMUdF0r8O1kKilWAGdgfs2BvJb/2Xp8p5iuDVfZXmhppo+d0Ran1P9TKdjV4ABwAgKXxJ3jcQTqE/2IRfqwnPf8itN8aFZlV3TJPYeD3yWE7IT55Gz6EijUpC7aKeoohTb4w2fpox58wWoF3SNp6sK6jDfiAUGEHYJ9pjU=</Signature>
</License>
```

如果不加这个`license`，在PDF文件的顶部会出现水印~



## 工具类的实现

导入相关依赖：

```xml
<dependencies>
    <dependency>
        <groupId>com.lowagie</groupId>
        <artifactId>itext</artifactId>
        <version>2.1.7</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.pdfbox/pdfbox -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.24</version>
    </dependency>
    <dependency>
        <groupId>com.aspose</groupId>
        <artifactId>aspose-cells</artifactId>
        <version>8.6.2</version>
    </dependency>
</dependencies>
```

`注意`：此处的`aspose-cells`是由我手动放入我本地的Maven仓库后，再手动导入至项目中的。

![image-20221028234833629](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028234833629.png)

不知道怎么手动导入Jar的小伙伴，互联网冲浪一下即可~ 

编写代码~

```java
package com.utils;

import com.aspose.cells.License;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import org.apache.pdfbox.pdmodel.PDDocument;


import java.io.*;
import java.util.List;

public class ExcelToPdf {

    private static License asposeLic;

    public static void main(String[] args) throws IOException {
        //convertPDFImage("d:\\demo.xls", null);
        // 直接在相同目录输出pdf文件
        ExcelToPdf.convertPDF("E:\\456.xlsx");
    }

    public static String convertPDF(String excelFileName) {
        return convertPDF(false, excelFileName);
    }

    /**
     * 是否每页大小自适应(超页不分页)
     *
     * @param onePagePerSheet excel每sheet页生成一页pad
     * @param excelFileName excel 文件
     * @return pdf
     */
    public static String convertPDF(boolean onePagePerSheet, String excelFileName) {
        String pdfFileName = "";
        PDDocument pdfDocument = null;
        try {
            Workbook workbook = new Workbook(excelFileName);
            getLicense();
            PdfSaveOptions saveOptions = new PdfSaveOptions();
            saveOptions.setCompliance(PdfCompliance.PDF_A_1_B);
            saveOptions.setOnePagePerSheet(onePagePerSheet);
            pdfFileName = excelFileName.substring(0, excelFileName.lastIndexOf(".")) + ".pdf";
            workbook.save(pdfFileName, saveOptions);
            pdfDocument = PDDocument.load(new File(pdfFileName));
            // Output file name
            pdfDocument.save(pdfFileName);
            editPDF(pdfFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }finally  {
            if (pdfDocument != null) {
                try {
                    pdfDocument.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return pdfFileName;
    }

    /**
     * 获取license
     *
     * @return
     */
    public static boolean getLicense() {
        boolean result = false;
        InputStream license = null;
        try {
            license = ExcelToPdf.class.getClassLoader().getResourceAsStream("\\license.xml");
            asposeLic = new License();
            asposeLic.setLicense(license);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (license != null) {
                    license.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean delete(String strFileName) {
        File fileDelete = new File(strFileName);

        if (!fileDelete.exists() || !fileDelete.isFile()) {
            System.out.println("错误: " + strFileName + "不存在!");
            return false;
        }

        return fileDelete.delete();
    }

}
```



测试结果：

测试控制台是没有输出的，只要正常结束就表示成功了。

![image-20221028235546376](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028235546376.png)

![image-20221028235558635](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028235558635.png)

![image-20221028235614601](C:\Users\ASUS\Desktop\nzc_blog\img\image-20221028235614601.png)

后续的扩展还没有想好~，还可以使excel转成图片，也可以输出到浏览器。

今天只是个开端~

## 后记

今天就写到了这里啦~ 感觉自己还好菜啊~  一起努力哦~

**希望你是满载而归的**~ 

> 写于 2022 年 10 月 28 日晚，作者：[宁在春](https://juejin.cn/user/2859142558267559/posts)

