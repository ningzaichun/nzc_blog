# 使用 doscify 将文章写成文档一般丝滑

> 以前老是看到 `github pages` 制作的那种笔记博客，总是想做而没有时间做，这次终于可以实现愿望啦。
>
> 一方面可以将自己的数据保存到云端，另一方面，`github pages` 也可以被 google 搜索引擎收录进去。（国内的百度不收录，也可以试下 gitee）

我以前觉得老麻烦啦，这次刚好看到这个 docsify , 就想要试一试。为了降低大家的学习成本，我会写的偏向于简单和实践，因为很多东西都是在使用的时候自己测出来。



### 📃Docsify简单介绍

[docsify：**一个神奇的文档站点生成器。**](https://docsify.js.org/#/?id=docsify-4122) 

#### 🧐它是什么

docsify 即时生成您的文档网站。与 GitBook 不同的是，它不会生成静态 html 文件。相反，**它会巧妙地加载和解析您的 Markdown 文件并将它们显示为网站**。要开始使用它，您需要做的就是创建一个`index.html`并将[其部署在 GitHub Pages 上](https://docsify.js.org/#/deploy)。

![image-20220522142251923](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205221428227.png)

页面特别小清新，反正是很讨我喜欢哈哈。



### 🧱准备操作

我默认大家都装了 node.js 环境哈.

#### 开始入坑(安装)

打开命令行,安装 `docsify-cli`,方便本地初始化和预览.

```bash
npm install docsify-cli -g
```

可能会出现的情况，1）下载失败，2）下载巨慢。npm 的常见问题哈，我自己是开了vpn的😂。

解决方法，换成国内的淘宝镜像就好，把 npm 换成 cnpm 即可。

执行命令：

```bash
npm install -g cnpm --registry=https://registry.npm.taobao.org #  将 npm 换成 cnpm 

cnpm i docsify-cli -g ##用 cnpm 下载
```



#### 初始化项目

我们想在文件夹中初始化一个项目的话，直接在命令行输入下面的命令即可。

```bash
docsify init [name]
```

我这里的  nzc_blog 是我自己的项目命名。

`注：` **建议使用名称  <u>docs</u>** 方便 `github pages`的部署。

![image-20220522144914488](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222036818.png)

初始化成功后，可以看到目录下创建的几个文件：

![image-20220522145051455](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222036254.png)

解释如下：

- `index.html` 入口文件
- `README.md` 会做为主页内容渲染
- `.nojekyll` 用于阻止 GitHub Pages 忽略掉下划线开头的文件



#### 启动项目

在上一级目录，我们执行

```bash
docsify serve [项目名称]
```

![image-20220522145606695](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222036794.png)

我们在浏览器中输入 `http://localhost:3000`

![image-20220522145706546](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222036117.png)

展示的首页内容就是 `README.md` 中的内容。这里我就不再重复述说了，自己试就好了。



### 🥂上手使用

我只着重几个最常用的，也是我今天已经使用的，剩下的我也在慢慢探索。

1. 封面  
2. 侧边栏目录设置
3. 导航栏设置
4. 常用的插件

这个 `index.html`文末有完整带解释版cv可用的那种，大家前面阅读一下即可，理解是什么意思即可。

#### 1）封面设置

我们要先在index.html 中开启渲染封面功能，之后再在文档的根目录中创建 `_coverpage.md` 文档，渲染的内容就是 `_coverpage.md` 中的内容。

设置 `index.html`

```html
<body>
  <div id="app"></div>
  <script>
    window.$docsify = {
      name: 'Docsify-Guide',// 项目名称
      repo: '',// 仓库地址，设置了这个，点击右上角的Github章鱼猫头像会跳转到此地址
      coverpage: true // 封面支持，默认加载的是项目根目录下的_coverpage.md文件
    }
  </script>
  <!-- 引入 Docsify 相关的 js 文件--> 
  <script src="//cdn.jsdelivr.net/npm/docsify/lib/docsify.min.js"></script> 
  <!-- 图片放大缩小支持 这个一定要引入 不然没有滑动条，很奇怪的事情 -->
  <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/zoom-image.min.js"></script>
    
  <!-- Docsify v4 -->
  <script src="//cdn.jsdelivr.net/npm/docsify@4"></script>
</body>
```

设置 `name  和 repo `的话，可以看到如下样式：

![image-20220522152714747](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222037087.png)

编写 `_coverpage.md` 文件

我这里以官方文档为例，内容如下：

```markdown
<!-- _coverpage.md --> 

![logo](https://docsify.js.org/_media/icon.svg) 

**# docsify <small>3.5</small>** 

> 一个神奇的文档网站生成器。 

- 简单、轻便 (压缩后 ~21kB) 
- 无需生成 html 文件 
- 众多主题 

[GitHub](https://github.com/docsifyjs/docsify/) [Get Started](
```

展示效果

![](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222037048.png)

这里还可以自定义背景，多张封面等，没去深入了解了。大家感兴趣可以去看看。



#### 2）侧边栏配置

为了获得侧边栏，您需要创建自己的`_sidebar.md`，你也可以自定义加载的文件名。默认情况下侧边栏会通过 `Markdown `文件自动生成，效果如当前的文档的侧边栏。

其实开启这个侧边栏，也就是像刚刚一样，在index.html 中多开启个配置。

```html
<script>
  window.$docsify = {
    loadSidebar: true // 就只要复制这一行到之前的那个 index.html 中就好了。
  }
</script>
```

创建 `_sidebar.md`，创建位置和封面文件的位置一样的。

```markdown
<!-- _sidebar.md -->

* Docsify相关使用
  * [Docsify使用指南](/mix/Docsify使用指南.md) <!--注意这里是相对路径 如果有多层的话也是一样的规则-->
```

这个文件的位置

![image-20220522153409128](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222037519.png)

随便写点内容进去， 不然访问的时候一直是报404的。

![image-20220522153958577](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222037165.png)

#### 3）导航栏设置

一样的多加一个设置`loadNavbar: true`,然后再编写一个`_navbar.md`文件。

```html
<script>
  window.$docsify = {
    loadNavbar: true
  }
</script>
```

```markdown
<!-- _navbar.md -->

* 链接到我
  - [CSDN | 宁在春](https://blog.csdn.net/weixin_45821811?type=blog)
  - [掘金 | 宁在春](https://juejin.cn/user/2859142558267559)


* 友情链接
  * [Docsify](https://docsify.js.org/#/)
  * [掘金](https://juejin.cn/)
```

![image-20220522160622183](https://raw.githubusercontent.com/ningzaichun/nzc_img_store/main/img/202205222037848.png)



#### 4）常用的插件

##### 1、全文搜索

配置比较多，具体设置我贴在后面的完整 `index.html` 文件中了。

##### 2、代码高亮

docsify 内置的代码高亮工具是  [Prism](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2FPrismJS%2Fprism) ，支持语言列表如下：

- Markup - `markup`, `html`, `xml`, `svg`, `mathml`, `ssml`, `atom`, `rss`
- CSS - `css`
- C-like - `clike`
- JavaScript - `javascript`, `js`

如果和我一样，写 `Java` 代码的话，需要在`index.html`引入

```html
<script src="https://cdn.jsdelivr.net/npm/prismjs@1.22.0/components/prism-java.min.js"></script>
```

##### 3、emoji 表情处理

引入 `js文件即可`

```html
<script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/emoji.min.js"></script>
```

##### 4、图片缩放

```html
<script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/zoom-image.min.js"></script>
```

##### 5、点击复制

```html
<script src="//cdn.jsdelivr.net/npm/docsify-copy-code/dist/docsify-copy-code.min.js"></script>
```

##### 6、侧边栏目录折叠

官方插件中没有实现这个功能，我在翻 issue 的时候，看到也有人提到这个问题，一些人就是直接引入了 `jquery`，自己实现这个功能。我觉得还是太麻烦，最后找到一个非官方插件实现了这个功能。

**挺赞的，你如果使用的方便的话， 可以和我一样给作者点个赞吧，感谢每一个帮助到我们的人**。

[插件链接](https://github.com/iPeng6/docsify-sidebar-collapse) 

使用方式，大致还是一样的，引入`js`文件，开启配置，不过这里还多了一步引入`css`样式文件。

```html
<!-- 侧边栏目录折叠 -->
<link rel="stylesheet" href="//cdn.jsdelivr.net/npm/docsify-sidebar-collapse/dist/sidebar.min.css" />  

<script>
    window.$docsify = {
        // 项目名称
        name: 'Docsify-Guide',
        // 仓库地址，点击右上角的Github章鱼猫头像会跳转到此地址
        repo: 'https://github.com/YSGStudyHards',
        // 侧边栏支持，默认加载的是项目根目录下的_sidebar.md文件
        loadSidebar: true,
        // 导航栏支持，默认加载的是项目根目录下的_navbar.md文件
        loadNavbar: true,
        // 封面支持，默认加载的是项目根目录下的_coverpage.md文件
        coverpage: true,
        // 最大支持渲染的标题层级
        maxLevel: 5,
        // 自定义侧边栏后默认不会再生成目录，设置生成目录的最大层级（建议配置为2-4）
        subMaxLevel: 4,
        // 小屏设备下合并导航栏到侧边栏
        mergeNavbar: true,
        coverpage: true,
        sidebarDisplayLevel: 1, //设置侧边栏目录展示级别
    }
</script>

<!-- 侧边栏目录折叠 plugins -->
<script src="//cdn.jsdelivr.net/npm/docsify-sidebar-collapse/dist/docsify-sidebar-collapse.min.js"></script>
```







完整的 `index.html`

```html
<!DOCTYPE html>
<html lang="en">

    <head>
        <meta charset="UTF-8">
        <title>Docsify-Guide</title>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
        <meta name="description" content="Description">
        <meta name="viewport"
              content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
        <!-- 设置浏览器图标 -->
        <link rel="icon" href="/favicon.ico" type="image/x-icon" />
        <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
        <!-- 默认主题 -->
        <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/docsify/lib/themes/vue.css">

        <!-- 侧边栏目录折叠 -->
        <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/docsify-sidebar-collapse/dist/sidebar.min.css" />
    </head>

    <body>
        <!-- 定义加载时候的动作 -->
        <div id="app">加载中...</div>
        <!-- index.html -->
        <script>
            window.$docsify = {
                // 项目名称
                name: 'Docsify-Guide',
                // 仓库地址，点击右上角的Github章鱼猫头像会跳转到此地址
                repo: 'https://github.com/YSGStudyHards',
                // 侧边栏支持，默认加载的是项目根目录下的_sidebar.md文件
                loadSidebar: true,
                // 导航栏支持，默认加载的是项目根目录下的_navbar.md文件
                loadNavbar: true,
                // 封面支持，默认加载的是项目根目录下的_coverpage.md文件
                coverpage: true,
                // 最大支持渲染的标题层级
                maxLevel: 5,
                // 自定义侧边栏后默认不会再生成目录，设置生成目录的最大层级（建议配置为2-4）
                subMaxLevel: 4,
                // 小屏设备下合并导航栏到侧边栏
                mergeNavbar: true,
                coverpage: true,
                sidebarDisplayLevel: 1, // set sidebar display level
            }
        </script>
        <script>
            // 搜索配置(url：https://docsify.js.org/#/zh-cn/plugins?id=%e5%85%a8%e6%96%87%e6%90%9c%e7%b4%a2-search)
            window.$docsify = {
                // 基础配置参数 完整的参数请参考 官网
                search: {
                    maxAge: 86400000, // 过期时间，单位毫秒，默认一天
                    paths: auto,// 注意：仅适用于 paths: 'auto' 模式
                    placeholder: 'Type to search',
                    // 支持本地化
                    placeholder: {
                        '/zh-cn/': '搜索',
                        '/': 'Type to search'
                    },
                    noData: 'No Results!',
                    // 支持本地化
                    noData: {
                        '/zh-cn/': '找不到结果',
                        '/': 'No Results'
                    },

                    // 搜索标题的最大层级, 1 - 6
                    depth: 2,
                    // 您可以提供一个正则表达式来匹配前缀。在这种情况下，
                    // 匹配到的字符串将被用来识别索引
                    hideOtherSidebarContent: false,
                    namespace:'此处放项目名称',
                    pathNamespaces: /^(\/(zh-cn|ru-ru))?(\/(v1|v2))?/
                }
            }
        </script>
        <!-- docsify的js依赖 -->
        <script src="//cdn.jsdelivr.net/npm/docsify/lib/docsify.min.js"></script>
        <!-- emoji表情支持 -->
        <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/emoji.min.js"></script>
        <!-- 图片放大缩小支持 -->
        <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/zoom-image.min.js"></script>
        <!-- 搜索功能支持 -->
        <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/search.min.js"></script>
        <!--在所有的代码块上添加一个简单的Click to copy按钮来允许用户从你的文档中轻易地复制代码-->
        <script src="//cdn.jsdelivr.net/npm/docsify-copy-code/dist/docsify-copy-code.min.js"></script>

        <!-- 侧边栏目录折叠 plugins -->
        <script src="//cdn.jsdelivr.net/npm/docsify-sidebar-collapse/dist/docsify-sidebar-collapse.min.js"></script>

    </body>
</html>
```

此外还有很多插件可以使用，[官网列表](https://docsify.js.org/#/plugins)，仍然还觉得不完美的话，大家可以自己`DIY`。

到这一步，基本上是可以简单的使用了。但实际上我还有很多有趣的东西没有介绍给大家，如封面可以自定义背景、设置多个封面，多种主题设置等等。还有很多有趣的东西，是需要大家自己去探索的。

### 👨‍💻自言自语

这篇文章就写到这里啦，明天等我部署到`github pages` 上，再写写如何部署吧。



重新开始写文了，生活又开始重新充实起来啦，只不过焦虑也越来越严重啦。

我记得我自己常说希望一直把写文当作生活中的乐趣，现在的我仍然是如此。

只不过这次我不再想做个简单的创作者了，我想去试着去写出自己的风格，成为一名真正意义上的内容创作者，没有人知道未来会不会是如此，又或许今天的我是这么说，三天后的我又完全是另外的态度，这才是人之常态吧。

> 祝你 他日凌云，万事胜意。

