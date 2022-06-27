# 关于使用Security框架实现权限管理系统



### 1、登录业务分析

1）获取验证码

- 后台生成 uuid + 验证码
- 以 uuid 为 key ，验证码的值为 value 存进redis ，设定为 5 分钟
- 返回的结果类包含 uuid 及 base64 编码格式图片（就是String字符串）

2）登录流程分析

- 用户输入 username、password、验证码及随验证码生成的uuid、是否记住密码
- 请求到达第一个过滤器，将请求中的请求信息，转换为 JavaBean
- 验证验证码
  - 取出 uuid 和 验证码，使用 uuid 作为 key 从 redis 中取出之前生成的 验证码
  - 对验证码进行判断，
    - 当为从 redis 中获取的验证码 value 值为 null 时，那么这个验证码是过期的啦，所以我们直接抛出异常。
    - 当为从 redis 中获取的验证码 value 值不为 null 时，将此值与请求携带的验证码进行比较，判断是否正确
    - 不管正确与否，我们都将此验证码从redis中删除，不再使用第二次。
- Security 验证用户名和密码，根据正确与否分别交由不同的handler处理。
- 登录成功：
  1. 清除登录错误次数
  2. 将用户信息存进 redis 中， redis 中使用 hash 类型存储
     - 因为 hash 类型可以存储多对 key、value 值
     - redis 中的 key 是生成的 uuid
  3. 判断该用户是否已经登录
     - 1）已经登录的话，我们要把已经登录的账户给顶下去。2）设置该账户己在其他地方登陆。
     - 2）未登录的话，存到已登录的 hashmap
  4. 用 redis 的 key 值即之前的 uuid 当作生成 token 中的 body，给定有效时间
  5. 选择要返回给前端的数据
  6. 异步方式记录登录日志



3）发送请求

- 取出请求头，解析出 token 中的 body，得到用户存入 redis 中的 key
- 判断此用户之前有没有别处登录的记录，有的话，删除这个曾在别处登录的用户
- 通过这个 key 从redis 中取出用户信息













![image-20220526005741179](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526005741179.png)







![image-20220526005805104](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526005805104.png)







![image-20220526005834562](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526005834562.png)







![image-20220526005946431](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526005946431.png)







![image-20220526010016138](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526010016138.png)





![image-20220526010056951](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526010056951.png)





![image-20220526010125105](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526010125105.png)

![image-20220526010151057](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526010151057.png)

![image-20220526010202282](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220526010202282.png)