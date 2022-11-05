# Oauth 2 授权码模式

> 如果要手动来实现第三方登录，该怎么做勒，今天来仔细聊聊这个流程。 依然还是以 gitee 第三方登录为案例，非实战。

## 前言

OAuth 就是一个关于授权（authorization）的开放网络标准，在全世界得到广泛应用，目前的版本是2.0版。

> 网络协议（标准）存在的目的是为了互联网更好的互通。

## 一、OAuth

OAuth在"客户端"与"服务提供商"之间，设置了一个授权层（authorization layer）。"客户端"不能直接登录"服务提供商"，只能登录授权层，以此将用户与客户端区分开来。"客户端"登录授权层所用的令牌（token），与用户的密码不同。用户可以在登录的时候，指定授权层令牌的权限范围和有效期。

"客户端"登录授权层以后，"服务提供商"根据令牌的权限范围和有效期，向"客户端"开放用户储存的资料。

---

假如我们要访问用户的gitee的资源，那么会大致经历下面的几个过程。

大致如下图为例：

![image-20220827211338075](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220827211338075.png)

如果我们需要访问用户在的gitee信息，我们就必须让用户授权，拿到用户的授权令牌，然后带着令牌访问 gitee ，gitee才会让我们相应信息。

可以明显看出，在这个过程中，第三方应用是完全没有办法接触到用户的密码的，更加不用说存储密码这种事情啦。

上图中的模式是属于 Oauth 2授权模式中的授权码模式，也是Oauth2中采用比较多的方式之一。



## 二、授权模式

客户端要拿到用户的授权，才能获得令牌，Oauth 2.0 协议中定义了四种模式。

- 授权码模式（authorization code）
- 简化模式（implicit）
- 密码模式（resource owner password credentials）
- 客户端模式（client credentials）

其中授权码模式应用的最为广泛，大都数应用都是基于这种方式实现。



## 三、授权码模式

Gitee的实现流程图：

![image-20220827204947859](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220827204947859.png)

我们分别来看看都要做些什么吧：

A、应用通过 浏览器 或 Webview 将用户引导到码云三方认证页面上（ **GET请求** ）

`https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code`

这里就相当于在页面上放一个gitee的图标，点击通过后台controller跳转到 gitee 的认证页面上去。

`client_id` 是我们第三方应用的id，`redirect_uri`是我们认证成功的回调地址。



B、用户对应用进行授权

注意: 如果之前已经授权过的需要跳过授权页面，需要在上面第一步的 URL 加上 scope 参数，且 scope 的值需要和用户上次授权的勾选的一致。如用户在上次授权了user_info、projects以及pull_requests。

则步骤A 中 GET 请求应为：`https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code&scope=user_info%20projects%20pull_requests`

这里在页面上表示的话，就是让用户确认是否授权~



C、码云认证服务器通过回调地址 {redirect_uri}  将用户授权码传递给 应用服务器 或者 直接在 Webview 中跳转到携带 用户授权码的回调地址上，Webview 直接获取code即可（{redirect_uri}?code=abc&state=xyz)

- code：表示授权码，必选项。该码的有效期应该很短，通常设为10分钟，客户端只能使用该码一次，否则会被授权服务器拒绝。该码与客户端ID和重定向URI，是一一对应关系。
- state：如果客户端的请求中包含这个参数，认证服务器的回应也必须一模一样包含这个参数。

这表示在用户授权以后，gitee会返回code和state给我们~

待我们拿到认证通过的code 之后，才能继续下面的事情



D、应用服务器 或 Webview 使用 access_token API 向 码云认证服务器发送post请求传入 用户授权码 以及 回调地址（ **POST请求** ）**注：请求过程建议将 client_secret 放在 Body 中传值，以保证数据安全。**`https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}`

- grant_type：表示使用的授权模式，此处的值固定为"authorization_code"。
- code：表示上一步获得的授权码，
- redirect_uri：表示重定向URI，必选项，且必须与A步骤中的该参数值保持一致。
- client_id：表示客户端ID，
- client_secret：客户端密钥，

这次的请求是为了获取到 `access_token` 和`refresh_token `  及相应的用户公开信息。

在下一次访问的时候，携带`access_token` ，按照gitee 公开的 api 以及用户授权的权利，就能够获取到用户授权的信息啦。



诸如此类的返回数据：

```json
{
    "access_token":"2YotnFZFEjr1zCsicMWpAA",
    "token_type":"example",
    "expires_in":3600,
    "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
    "example_parameter":"example_value"
}
```

gitee的话，还会携带一些其他的信息~

E、码云认证服务器返回 access_token，应用通过 access_token 访问 Open API 使用用户数据。

F、当 access_token 过期后（有效期为一天），你可以通过以下 refresh_token 方式重新获取 access_token（ **POST请求** ）`https://gitee.com/oauth/token?grant_type=refresh_token&refresh_token={refresh_token}`

大致的流程就是如此啦







## 后记

> 又是一个宅在屋中的周六，这让人多少有些不舒心了。

写于 2022 年 8 月 27 日晚，作者：宁在春



