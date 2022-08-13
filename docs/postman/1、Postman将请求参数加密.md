Postman 加密接口测试 | 使用Rsa、Aes对参数加密
> 说句废话，以前都是笑别人接手看到什么xxxx代码，轮到自己看到了就是真的笑不出啦....

## 前言

做接口加密的测试也是上次遇到的，在这之前，都是在浏览器登录后，从请求头中复制 token 过来测试....

**说真的，一瞬间我都有点诧异**，这样也太麻烦了吧~，因而也就产生了这篇文章。

还有一些问题：

1.  postman 有内置加密Api，但不支持RSA加解密码。 （引入其他的js文件至环境变量，利用eval 函数进行解析，还可以利用request获取，将其保存至全局变量中）
1.  postman 中 request对象属性皆为只读，如何把提交时的明文变为密文? (前置脚本)

***

我先说说本次文章最终要实现的效果，以给大家一个阅读的参考。

目标：在测试登录接口时，针对登录接口需要用到的 `username、password`进行加密（加密方式分别为 `rsa、aes` ），再将加密后的数据传输给后端。

方法都是相似的，知道如何加密，其他的接口和字段都是差不多的实现方式。

## 一、Postman

对于Postman，对于这个工具，我认为是大都数小伙伴都要会的一个工具~，只是学习的程度的不同罢了

大致就是分为：

1、刚学的我们，就是用来测试一些基本接口

2、用了一段时间的我们，知道了有环境变量、集合操作等

3、了解到 postman 中可以结合Js文件对请求做一些参数，断言等等

4、集合接口测试、编写测试用例、利用内置变量随机生成数据测试接口等

5、.......

***

今天用到的就是 Postman 中的前置脚本`Pre-request Script`

## 二、Pre-request Script 编写前置脚本

### 2.1、脚本执行顺序

说之前，先说说postman中**脚本的执行顺序**，这里贴一张官方的图 [postman 官方文档](https://learning.postman.com/docs/writing-scripts/intro-to-scripts/)

在 Postman 中，单个请求的脚本执行顺序如下所示：

-   与请求关联的预请求脚本将在发送请求之前执行
-   与请求关联的测试脚本将在请求发送后执行

![单个请求的工作流程](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aeac636fffef47d3b7b1305e670b67a5~tplv-k3u1fbpfcp-zoom-1.image)

在发起`request`请求前，会先执行**前置脚本**，收到接口返回结果后，再执行 `test script`

### 2.2、准备测试接口信息

准备一个后端请求接口，能接收请求参数即可，我采取的是将加密的信息打印

测试接口信息

`http://localhost:8080/login`

```
 {
     "username":"{{rsa:username}}",
     "password":"{{esc:password}}"
 }
```

`补充`：用双层大括号包裹的参数，是**引用postman的环境变量**，做到动态可变

参数名前的：`rsa，aes`是为了测试多种加密方式给加的判断依据。

![image-20220811215659641](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2292454ae8d44eaf892ccc6af416dc61~tplv-k3u1fbpfcp-zoom-1.image)

（图片说明：增加一个接口，填入基本信息）

### 2.3、Postman设置环境变量

这两处我们都需要用到~


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ccde9140320242938233f6e9ed0cb6a8~tplv-k3u1fbpfcp-watermark.image?)

接口用到的数据，一般是存放在某一个环境变量中，

如果很多处用到，一般可以考虑放到全局变量中了~

我们将`rsa:username、aes:password`放到一个环境变量中，这个环境变量的名称的就叫`login`

点击上图中的`add`即可

![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/31fc349a0beb4651ad1b32aa987b0c79~tplv-k3u1fbpfcp-watermark.image?)


（图片说明：记得 ctrl + s 保存噢，手误：`esc`应为`aes`）

另外我们用全局变量来保存一下 rsa的公钥，我这里的公私钥都是拿工具直接生成的 [工具链接](https://www.bejson.com/enc/rsa/)


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/bf5a48ad850d43a4a4e67a4857c3b181~tplv-k3u1fbpfcp-watermark.image?)

将公钥保存在postman的全局变量中


![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/786b0574110f4bbea6a19b9294bd632a~tplv-k3u1fbpfcp-watermark.image?)

另外全局变量之后我们用要来保存用来加密的 js 文件，不过这一步是利用前置脚本做的。

#### 下载forge

```
 git clone https://github.com/digitalbazaar/forge.git
 cd forge文件夹下
 npm install
```


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7e49647b2ad44c419305ba45577ced8f~tplv-k3u1fbpfcp-watermark.image?)

这样就算安装完了，否则会一直报没有找到 `forge` 对象

### 2.4、编写前置脚本

我们今天编写**前置脚本的作用**，就是给接口的参数进行加密，

所以最简单的方式，

1.  拿到js文件，
1.  运行
1.  将参数进行加密

```
 // ------ 导入RSA ------
 if(!pm.globals.has("forgeJS")){
   pm.sendRequest("https://raw.githubusercontent.com/loveiset/RSAForPostman/master/forge.js", (err, res) => {
     if (!err) {
         // 保存至全局变量中，forgeJs 为 key，res.text() 为value值
         pm.globals.set("forgeJS", res.text())
     }
 })}
 
 // 这个函数前端的小伙伴应该比较了解
 // 它的作用是把对应的字符串解析成js代码并运行(将json的字符串解析成为JSON对象)；
 eval(postman.getGlobalVariable("forgeJS"));
 
 // ------------ AES 加密 ------------
 function aesEncrypt(content){
     const key = CryptoJS.enc.Utf8.parse("Y5MUIOM7BUWI7BQR");
     const iv = CryptoJS.enc.Utf8.parse('S41AXIPFRFVJL73Z');
     const encrypted = CryptoJS.AES.encrypt(content, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7});
     return encrypted.toString();
 }
 
 // ------------ RSA 加密 ------------
 function rsaEncrypt(content){
     const pubKey = postman.getGlobalVariable("RSA_Public_Key");
     if(pubKey){
         const publicKey = forge.pki.publicKeyFromPem(pubKey);
         const encryptedText = forge.util.encode64(
             publicKey.encrypt(content, 'RSAES-PKCS1-V1_5', {
               md: forge.md.sha1.create(),
               mgf: forge.mgf.mgf1.create(forge.md.sha1.create())
         }));
         return encryptedText;
     }
 }
 pm.environment.set("rsa:username", aesEncrypt("nzc_wyh"));
  pm.environment.set("aes:password", rsaEncrypt("123456"));
```

我后端的接口返回数据就是将加密的数据直接放回~

这种方式接口的测试结果~

![image-20220811233914894](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/110eef4e1949408bbf929d2a5616b176~tplv-k3u1fbpfcp-zoom-1.image)

**运行完查看环境变量和全局全量的变化**

![image-20220811234115035](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0a0b36f4662e4d75bf3067884b1bb944~tplv-k3u1fbpfcp-zoom-1.image)

> 思考：但是你能感觉到这个前置脚本存在的问题吗？

**如果同一个请求中有多个参数要进行加密，那我们岂不是要写多次set，这显然是不合理的，下面就做一个改善**，

当然如果不会的话，**我们可以一起请教前端小伙伴**，以让代码更加完善。

### 2.5 、优化前置脚本

```
 // ------ 通用方法 ------
 // 提取{{}}中内容
 function getBracketStr(text) {
     let result = ''
     let regex = /{{(.+?)}}/g;
     let options = text.match(regex);
     if (options && options.length > 0) {
         let option = options[0];
         if (option) {
             result = option.substring(2, option.length - 2)
         }
     }
     return result
 }
 
 
 // ------ 导入RSA ------
 if(!pm.globals.has("forgeJS")){
   pm.sendRequest("https://raw.githubusercontent.com/loveiset/RSAForPostman/master/forge.js", (err, res) => {
     if (!err) {
         // 保存至全局变量中，forgeJs 为 key，res.text() 为value值
         pm.globals.set("forgeJS", res.text())
     }
 })}
 
 // 这个函数前端的小伙伴应该比较了解
 // 它的作用是把对应的字符串解析成js代码并运行(将json的字符串解析成为JSON对象)；
 eval(postman.getGlobalVariable("forgeJS"));
 
 // ------------ AES 加密 ------------
 function aesEncrypt(content){
     const key = CryptoJS.enc.Utf8.parse("Y5MUIOM7BUWI7BQR");
     const iv = CryptoJS.enc.Utf8.parse('S41AXIPFRFVJL73Z');
     const encrypted = CryptoJS.AES.encrypt(content, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7});
     return encrypted.toString();
 }
 
 
 // ------------ RSA 加密 ------------
 function rsaEncrypt(content){
     const pubKey = postman.getGlobalVariable("RSA_Public_Key");
     if(pubKey){
         const publicKey = forge.pki.publicKeyFromPem(pubKey);
         const encryptedText = forge.util.encode64(
             publicKey.encrypt(content, 'RSAES-PKCS1-V1_5', {
               md: forge.md.sha1.create(),
               mgf: forge.mgf.mgf1.create(forge.md.sha1.create())
         }));
         return encryptedText;
     }
 }
 // 获取当前请求中的加密变量 这里判断为字符串的原因是，
 // 我们引用环境变量时，一定是"{{}}" 这种格式的
 let requestData; 
 if((typeof request.data) === 'string'){
     requestData = JSON.parse(request.data)
 } else {
     requestData = request.data;
 }
 
 // Object.assign 拷贝对象 将request.headers 中的所有数据，拷贝到 requestData中
 requestData = Object.assign(requestData, request.headers);
 
 // 遍历
 Object.keys(requestData).map(key => {
      // 内容
     let value = requestData[key] + '';
     // 是否为变量
     if (value.indexOf('{{') !== -1) { 
         let content = getBracketStr(value);
         // 判断用是否加密，加密的话又是用哪种方式加密
         if (content.indexOf('aes:') !== -1) {
             let c = content.split('aes:')[1];
             let encryptedContent = pm.environment.get(c); // 加密内容
             encryptedContent = encryptedContent ? encryptedContent : c;
             pm.environment.set(content, aesEncrypt(encryptedContent));
         } else if (content.indexOf('rsa:') !== -1) {
             let c = content.split('rsa:')[1];
             let encryptedContent = pm.environment.get(c); // 加密内容
             encryptedContent = encryptedContent ? encryptedContent : c;
             pm.environment.set(content, rsaEncrypt(encryptedContent));
         }
     }
 });
 
```

**优点**：

1.  如果同一个请求中有多个参数加密，不用手动set，而是通过循环全部set进去
1.  可以使用多种加密方式，只要继续扩展即可
1.  扩展性更高


测试结果：
![image.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/aeda645e8da540228cd4162021251caa~tplv-k3u1fbpfcp-watermark.image?)


## 后记

> 小小一篇文章，希望给大家一点点帮助~

写于 2022 年 8 月 11 日晚 23 时，作者：宁在春