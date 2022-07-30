原 node 14



升级后为 node 16.16



原本的vue项目无法正常启动



npm install 报错 :





![image-20220720230544647](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220720230544647.png)

把 package-lock.json处删除，npm install 时会重新生成的。

```shell
npm cache clear --force 

npm --registry https://registry.npm.taobao.org install cluster

npm install --force

npm run build
```

