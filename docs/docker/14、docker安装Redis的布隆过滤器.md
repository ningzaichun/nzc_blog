# Redis 布隆过滤器使用



因为平常使用 Docker 比较多，所以照常还是使用Docker来准备环境啦。

## 一、Docker 安装 Redis 布隆过滤器

> Redis 本身并不支持布隆过滤器，而是采用插件的方式去安装的，以达到一种热拔插的效果。

因为我对于 liunx 来说就是一个小白，所以还是用 docker 香。

我就开始搜索如何为 docker 上的 Redis 安装布隆过滤器， 实现方式我看了看有好几种~，但是大都数都需要去下载 redis 布隆过滤器的那个模块，我觉得这样也太麻烦了，肯定有前人偷过懒了，果然有更直接的方式~ 

> 学习历史重要原因之一，就是要学会感恩，因为我们都是站在巨人的肩膀上。

Docker 上有个`redislabs/rebloom`的镜像，它是将 `redis` 和`布隆过滤器`打包在一起的一个镜像，直接使用这个镜像启动的redis容器，直接就可以使用布隆过滤器~👩‍💻

### 1.1、安装

搜索镜像

```bash
docker search redislabs/rebloom
```

![image-20220825204436899](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825204436899.png)



拉取镜像

```bash
docker pull redislabs/rebloom
```

![image-20220825204647920](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825204647920.png)



启动镜像

```bash
docker run -d -p 6379:6379 --name myrebloom redislabs/rebloom
```

![image-20220825204805226](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825204805226.png)

**注意**：

我只是为了有个写demo的环境，所以redis未落盘，也未设置密码，用的也是默认端口，**真实要用，不能这么玩，当然也没这样玩的**。



### 1.2、测试

进入容器中，我们来用`redis-cli`玩一玩~

```bash
docker exec -it [容器名|容器id] bash #/bin/bash 都可
redis-cli
```

![image-20220825205131988](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825205131988.png)

![image-20220825205325227](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825205325227.png)

确定没啥问题，咱们来玩 Redis 的布隆过滤器



## 二、RedisBloom 命令讲解

### 2.1、命令大纲

先看看总共有哪些命令，待会咱们一条一条来尝试，均来自[redis官网](https://docs.redis.com/latest/modules/redisbloom/commands/)。

| Command                                                | Description                                                  |
| :----------------------------------------------------- | :----------------------------------------------------------- |
| [BF.ADD](https://redis.io/commands/bf.add)             | 添加一个元素到布隆过滤器                                     |
| [BF.EXISTS](https://redis.io/commands/bf.exists)       | 判断元素是否在布隆过滤器                                     |
| [BF.INFO](https://redis.io/commands/bf.info)           | 返回有关布隆过滤器的信息                                     |
| [BF.INSERT](https://redis.io/commands/bf.insert)       | 将多个元素添加到过滤器。如果键不存在，它会创建一个新的过滤器。 |
| [BF.MADD](https://redis.io/commands/bf.madd)           | 添加多个元素到布隆过滤器                                     |
| [BF.MEXISTS](https://redis.io/commands/bf.mexists)     | 判断多个元素是否在布隆过滤器                                 |
| [BF.RESERVE](https://redis.io/commands/bf.reserve)     | 创建一个布隆过滤器。设置误判率和容量                         |
| [BF.SCANDUMP](https://redis.io/commands/bf.scandump)   | 开始增量保存 Bloom 过滤器。                                  |
| [BF.LOADCHUNK](https://redis.io/commands/bf.loadchunk) | [恢复之前使用BF.SCANDUMP](https://redis.io/commands/bf.scandump)保存的布隆过滤器。 |

### 2.2、BF.ADD 和 BF.MADD

语法格式：

```bash
BF.ADD key value 

BF.MADD key value1 value2 ...
```

当key不存在的时候，会创建一个空的布隆过滤器，并会给定一个默认的误判率和含有上限容量的的子过滤器。并且通过这种方式创建出来的布隆过滤器，是能够自动缩放的。

每一次扩容，新的子过滤器是用前一个子过滤器的大小来进行扩张，默认扩张倍数为2.

`BF.ADD`类似于集合的`sadd`命令，不过`bf.add`一次只能添加一个元素到集合中。一次性添加多个则使用`bf.madd`.

![image-20220825210743010](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825210743010.png)

![image-20220825214258264](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825214258264.png)



### 2.3、BF.EXISTS 和 BF.MEXISTS

上面也已经用到了，它就是用来判断元素是否在布隆过滤器中

语法：

```bash
bf.exists key value
bf.mexists key value1 value2 ...
```

存在返回1，不存在返回0

![image-20220825214403593](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825214403593.png)



![image-20220825214344315](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825214344315.png)

### 2.4、BF.INFO

返回有关布隆过滤器的信息,

语法格式：

```bash
bf.info key
```

![](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825212009963.png)

```bash
Capacity #  子过滤器的上限
(integer) 100
Size #布隆过滤器的容量大小
(integer) 296 
Number of filters  #当前过滤器数量 这里应该是记录子过滤器数量吧
(integer) 1
Number of items inserted # 插入的值的数量
(integer) 1
Expansion rate #默认的扩张倍数
(integer) 2
```

一个数据看不出来，多插入两条，看一下对比，大家就明白了

![image-20220825213447254](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825213447254.png)



### 2.5、BF.RESERVE

语法格式：

```bash
bf.reserve key error_rate capacity [EXPANSION expansion] [NONSCALING]
```

使用 `bf.reserve` 命令创建一个自定义的布隆过滤器。`bf.reserve`命令有三个参数，分别是：

- `key`：键
- `error_rate`：期望错误率，期望错误率越低，需要的空间就越大。默认 0.1
- `capacity`：初始容量，当实际元素的数量超过这个初始化容量时，误判率上升。 默认 100

然后还有两个可选参数： `EXPANSION` 和 `NONSCALING`

1、`EXPANSION`：简单说它就是扩张倍数，省略则默认为2。

如果要存储在过滤器中的元素数量未知，我们建议您使用`expansion`2 或更多来减少子过滤器的数量。否则，我们建议您使用 `expansion`1 来减少内存消耗。默认扩展值为 2。

2、`NONSCALING`：如果写了这个参数，在达到初始容量，为防止过滤器创建额外的子过滤器。过滤器在达到容量时会返回错误`capacity`，当让非缩放过滤器比缩放过滤器需要的内存略少。

示例：

```bash
bf.reserve nzc:redisboom1 0.01 100

bf.reserve nzc:redisboom2 0.01 100  EXPANSION 1 

bf.reserve nzc:redisboom3 0.01 100  NONSCALING
```

我把相关的信息都打印出来了，大家从所输出的信息中也能够看出它们的一些区别。

![image-20220825215227753](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825215227753.png)

![image-20220825215255672](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825215255672.png)

![image-20220825215316937](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825215316937.png)

小结：

1. 如果是预估不到数据的范围量，而且没有办法计算的增长量，那么我觉得暂定为默认的即可。
2. 如果是能够估算数据的大致范围，数据增长速度有迹可循，那么可以适当的推算一下`EXPANSION`这个参数该设置的大小。
3. 如果数据范围没有那么那么大，并且可以接受较大程度的误判率，那么`EXPANSION`设置为1会更好，能够节省内存消耗。
4. `NONSCALING`如果使用此参数，则是将数组大小固定了，需要考虑清楚是否适合。
5. 布隆过滤器的 `error_rate` 越小，需要的存储空间就越大，对于不需要过于精确的场景，`error_rate`设置稍大一点也可以。布隆过滤器的`capacity`设置的过大，会浪费存储空间，设置的过小，就会影响准确率，所以在使用之前一定要尽可能地精确估计好元素数量，还需要加上一定的冗余空间以避免实际元素可能会意外高出设置值很多。总之，`error_rate`和 `capacity`都需要设置一个合适的数值。

### 2.6、BF.INSERT

语法格式

```bash
BF.INSERT key [CAPACITY capacity] [ERROR error]
  [EXPANSION expansion] [NOCREATE] [NONSCALING] ITEMS item [item...]
```

由中括号包裹的都是可选参数。大都数参数上面都说到了

`NOCREATE`指示如果过滤器不存在，则不应创建它。如果过滤器尚不存在，则返回错误而不是自动创建它。这可以用于过滤器创建和过滤器添加之间需要严格分离的地方。

示例：

```bash
BF.INSERT nzc:redisboom4  items key1 key2 key3
```

![image-20220825223206826](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220825223206826.png)

补充：

```bash
BF.SCANDUMP # 开始布隆过滤器的增量保存。这对于无法适应法线DUMP和RESTORE模型的大型布隆过滤器很有用。
BF.LOADCHUNK  #恢复以前使用保存的过滤器SCANDUMP。
```

这两点就不咋说啦~

## 后记

> 一篇简简单单的文章~

就剩最后几天了，更文要结束啦~

下一次，应该会变成佛系的参与者了吧

> 下一次准备好好的重构自己的知识体系了，我已经开始慢慢变得为了更文而更文了，已经有点偏离属于自己的轨道了。
>
> 哈哈

写于 2022 年 8 月 25 日晚，作者：宁在春









