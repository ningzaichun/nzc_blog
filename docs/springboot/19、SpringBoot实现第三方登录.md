## 手动实现第三方登录

> 昨晚写了一篇[关于 Oauth 2 的文章](https://juejin.cn/post/7136555594797809677)，今天就打算用它来实现一个第三方登录的案例。今日是实操啦~
>
> 但其实在此之前，我还写过 [SpringBoot 整合 JustAuth 实现第三方登录 | gitee登录](https://juejin.cn/post/7133617235792232462)，但那次是借助 JustAuth 这个第三方包来实现的。
>
> 那我们如果自己实现该是如何勒？
>
> **一起来看看吧，其实没有你想象的那么难**~

建议：

1、先阅读阅读[关于 Oauth 2 的文章](https://juejin.cn/post/7136555594797809677)，更好的理解流程。

2、如果想一步到位，可以看看[SpringBoot 整合 JustAuth 实现第三方登录 | gitee登录](https://juejin.cn/post/7133617235792232462) 这篇

大都数采取授权码模式的应用，在我们的应用中实现第三方登录时，大都一样的。很多共同的特性。

## 一、时序图

![thirdlyLogin](C:\Users\ASUS\Desktop\nzc_blog\img\thirdlyLogin.png)

代码的实现，也是按照这些步骤所写的~

## 二、实现效果

![QQ录屏20220828144516_](C:\Users\ASUS\Desktop\nzc_blog\img\QQ录屏20220828144516_.gif)



补充说明：我是之前登录过了，所以没弹出那个认证授权页面，大家测试的时候，无痕测试就可以看出来啦。

授权页面如下：

![image-20220828145410194](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220828145410194.png)



## 三、Gitee第三方登录案例

项目的话，就创建一个普通的Springboot 项目即可，都是些常规依赖~

### 3.1、准备工作

在编写代码之前，记得先去Gitee创建一个第三方应用

点击个人头像-->设置-->侧边栏找到数据管理-->第三方应用-->创建一个第三方应用

![image-20220828145819548](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220828145819548.png)

上面的`Client ID`和 `Client Secret`待会也是要用到的。

主要填写的就是应用回调地址，这是在认证成功后，会调用的地址。

**项目的整体结构**

![image-20220828150211856](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220828150211856.png)

**相关依赖**

```xml
<parent>
    <artifactId>spring-boot-dependencies</artifactId>
    <groupId>org.springframework.boot</groupId>
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
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.7.22</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.79</version>
    </dependency>
</dependencies>
```

**配置文件**

```yml
erver:
  port: 8089
spring:
  application:
    name: springboot-thirdly-login
gitee:
  client-id: 52ac7765xxxxx10b6c539b2c9d402
  client-secret: 1198d5xxd8b0627cxxcd09cc818
  redirect-uri: http://127.0.0.1:8089/oauth/gitee/callback
```



```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年08月28日 11:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "gitee")
public class GiteeProperties {

    private String clientId;

    private String clientSecret;

    private String redirectUri;
}
```

```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年08月28日 11:27
 */
public class ThirdlyLoginTypeConstant {
    public final static String  GITEE="gitee";
    public final static String  GITEE_URL="https://gitee.com/oauth/authorize?client_id={client_id}&redirect_uri={redirect_uri}&response_type=code";
    public final static String  GITEE_OAUTH_TOKEN_URL="https://gitee.com/oauth/token";
}

```



### 3.2、service

```java
/**
 * @description:
 * @author: Ning zaichun
 * @date: 2022年08月28日 12:42
 */
public interface OauthService {

    /**
     * 根据传入的所选择的第三方登录类型，返回认证的url
     * @param loginType 第三方登录类型
     * @return
     */
    String choiceLoginType(String loginType);

    /**
     *
     * 根据用户的授权码和登录类型，获取第三方应用的 access_token
     * @param loginType 第三方登录类型
     * @param code 用户授权码
     * @return
     */
    String getOauthToken(String loginType, String code);


    /**
     * 获取授权的用户信息
     * @param loginType 第三方登录类型
     * @param accessToken 认证通过的令牌
     * @return
     */
    String getUserInfo(String loginType,String accessToken);
}
```





```java
/**
 * @description:
 * @author: Ning Zaichun
 * @date: 2022年08月28日 12:42
 */
@Service
public class OauthServiceImpl implements OauthService {

    @Autowired
    GiteeProperties giteeProperties;

    @Override
    public String choiceLoginType(String loginType) {
        String url = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            url = ThirdlyLoginTypeConstant.GITEE_URL
                    .replace("{client_id}", giteeProperties.getClientId())
                    .replace("{redirect_uri}", giteeProperties.getRedirectUri());
        }
        return url;
    }

    @Override
    public String getOauthToken(String loginType, String code) {
        Map<String, Object> map = new HashMap<>();
        String result = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            String url = ThirdlyLoginTypeConstant.GITEE_OAUTH_TOKEN_URL;
            map.put("grant_type", "authorization_code");
            map.put("code", code);
            map.put("client_id", giteeProperties.getClientId());
            map.put("client_secret", giteeProperties.getClientSecret());
            map.put("redirect_uri", giteeProperties.getRedirectUri());
            //发送get请求并接收响应数据
            result = HttpUtil.createPost(url).form(map).execute().body();
        }
        return result;
    }

    @Override
    public String getUserInfo(String loginType, String accessToken) {
        String userInfo = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            String userInfoUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
            userInfo = HttpUtil.createGet(userInfoUrl).execute().body();
        }
        return userInfo;
    }
}
```

### 3.3、controller



```java
/**
 * @description:
 * @author: Ning zaichun
 * @date: 2022年08月28日 11:12
 */
@Slf4j
@RequestMapping("/oauth")
@RestController
public class OauthController {


    @Autowired
    OauthService oauthService;

    /**
     * 1、用户点击使用 Gitee 作为第三方登录，后台重定向至gitee认证页面
     *
     * @param loginType
     * @param response
     * @throws IOException
     */
    @GetMapping("/login/{loginType}")
    public void thirdlyLogin(@PathVariable("loginType") String loginType, HttpServletResponse response) throws IOException {
        String url = oauthService.choiceLoginType(loginType);
        log.info(url);
        response.sendRedirect(url);
    }


    /**
     * 回调地址 这里就对应着我们在gitee创建第三方应用时填写的回调地址 这里会传回一个 code ，这个code 就是用户认证通过的授权码
     
     * http://127.0.0.1:8089/oauth/gitee/callback?code=xxxxebe2a67ba13xxxx925615aa89041
     * 其中 code 为用户授权码，由gitee授权服务器调用回调地址携带而来
     *
     * @param loginType
     * @param code
     * @param state 它并非是必填项，gitee调用回调地址时，其实没有携带这个参数，但是在oauth中有讲到，当时就写上去了。
     * @return
     */
    @ResponseBody
    @GetMapping("/{loginType}/callback")
    public String redirectUri(@PathVariable("loginType") String loginType, String code, String state) {
        log.info("code==>{}", code);
        //1、拿到这个code之后，我们要再向 gitee 服务起请求，获取 access_token 请注意这是一个post 请求
        // https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}
        String result = oauthService.getOauthToken(loginType, code);
        ThirdlyResult thirdlyResult = (ThirdlyResult) JSON.parseObject(result, ThirdlyResult.class);
        /**
         * {
         * "access_token":"f7d851b2cdxx1fd491b",
         * "token_type":"bearer",
         * "expires_in":86400,
         * "refresh_token":"9f3098c7a8be09cd15xxcc38fb3dxxxb8e40f0800ced8",
         * "scope":"user_info",
         * "created_at":1661659283
         * }
         */

        /**
         * 我这里只是测试获取用户信息的请求 
         * 如果需要实现登录的话，自己加上业务逻辑即可
         */
        // 2、拿到 access_token 以后发送一个get 请求，获取用户信息
        String userInfo = oauthService.getUserInfo(loginType, thirdlyResult.getAccessToken());
        return userInfo;
    }
}
```

在请求获取 access_token的接口会返回的数据格式，方便转换，就写了一下~

```java
/**
 * @description:
 * @author: Ning zaichun
 * @date: 2022年08月28日 12:18
 */
@Data
public class ThirdlyResult {

    /**
     * {
     * "access_token":"f7xxxb71fd491b",
     * "token_type":"bearer",
     * "expires_in":86400,
     * "refresh_token":"9f3098c7a8be09cdxxxxx53a2f69dccxxxx8e40f0800ced8",
     * "scope":"user_info",
     * "created_at":1661659283
     * }
     */
    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String refreshToken;

    private String scope;

    private String createAt;
}
```



### 3.4、小结

其实实现第三方登录其实也没有想象的那么难，把思路一步一步理清楚就可以了。

我也相信大家在看完文章后已经能够手动的实现第三方登录啦吧~

## 后记

> 尝试做一些自己喜欢的事情吧~
>
> 今天结束啦，周末也要结束啦~

写于 2022 年 8 月 28日午后，作者：宁在春