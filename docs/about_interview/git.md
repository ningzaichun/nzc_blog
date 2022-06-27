# Git 相关操作

### 一、github 初始化一个仓库

- 首先在 github 创建一个远程仓库

- 本地建立与远程仓库的连接

- push 上去

  ```bash
  git init
  git add README.md
  git commit -m "first commit"
  git branch -M main
  git remote add origin 远程仓库地址
  git push -u origin main
  ```



### 二、同伴拉取一个远程仓库 

克隆仓库命令

```bash
git clone git仓库地址
```

### 三、同伴对文件进行修改然后推送到远程仓库



```bash
git add 修改的文件
git commit -m "同伴修改并推送"
git push -u origin main
```



### 四、自己和同伴修改同一个文件，并且









### git add 操作时，有时会误添加一些不想提交的文件，如何解决？

1、误add单个文件

git reset HEAD 将file退回到unstage区

2、误add多个文件，只撤销部分文件

git reset HEAD 将file退回到unstage区

git rm 与 git reset的区别
git rm：用于从工作区和索引中删除文件
git reset：用于将当前HEAD复位到指定状态。一般用于撤消之前的一些操作(如：git add,git commit等)。

git rm file_path 删除暂存区和分支上的文件，同时工作区也不需要
git rm --cached file_path 删除暂存区或分支上的文件, 但工作区需要使用, 只是不希望被版本控制（适用于已经被git add,但是又想撤销的情况）
git reset HEAD 回退暂存区里的文件

来源：https://blog.csdn.net/crjmail/article/details/100011063



### git commit后，如何撤销commit

修改了本地的代码，然后使用：

```
git add file
git commit -m '修改原因'
12
```

执行[commit](https://so.csdn.net/so/search?q=commit&spm=1001.2101.3001.7020)后，还没执行push时，想要撤销这次的commit，该怎么办？

解决方案：
使用命令：

```
git reset --soft HEAD^
1
```

这样就成功撤销了commit，如果想要连着add也撤销的话，–soft改为–hard（删除工作空间的改动代码）。

命令详解：

HEAD^ 表示上一个版本，即上一次的commit，也可以写成HEAD~1
如果进行两次的commit，想要都撤回，可以使用HEAD~2

–soft
不删除工作空间的改动代码 ，撤销commit，不撤销git add file

–hard
删除工作空间的改动代码，撤销commit且撤销add

另外一点，如果commit注释写错了，先要改一下注释，有其他方法也能实现，如：

```
git commit --amend
这时候会进入vim编辑器，修改完成你要的注释后保存即可。
```

来源：

https://www.jianshu.com/p/a9f327da3562





### git stash和git stash pop



https://blog.csdn.net/qq_36898043/article/details/79431168





### git 创建新分支

来源：https://blog.csdn.net/qq_37899792/article/details/121328761







### git 删除本地分支

查看项目中分支

```bash
git branch -a
```



删除本地分支



```bash
git branch -d 分支名
```





### git 分支操作



https://www.cnblogs.com/aaron-agu/p/10454788.html





### git merge的三种操作merge, squash merge, 和rebase merge

来源https://www.jianshu.com/p/ff1877c5864e







### Git 分支设计规范

来源：https://zhuanlan.zhihu.com/p/108385922

#### 代码管理规范、Commit message 和 Change log 编写指南



来源：http://www.ruanyifeng.com/blog/2016/01/commit_message_change_log.html



来源：https://blog.csdn.net/adaivskean/article/details/124878194#:~:text=git-flow%20%E6%B5%81%E7%A8%8B%E4%B8%AD%E5%8C%85%E5%90%AB%205,%E7%B1%BB%E5%88%86%E6%94%AF%EF%BC%8C%E5%88%86%E5%88%AB%E6%98%AF%20master%E3%80%81develop%E3%80%81%E6%96%B0%E5%8A%9F%E8%83%BD%E5%88%86%E6%94%AF%EF%BC%88feature%EF%BC%89%E3%80%81%E5%8F%91%E5%B8%83%E5%88%86%E6%94%AF%EF%BC%88release%EF%BC%89%E5%92%8C%20hotfix%E3%80%82



### 代码提交规范



代码提交过程命令示例如下，也可使用开发工具提交，只要符合规范即可。

```bash
git pull origin develop:develop // 必须
git checkout -b feature/ibreeze-001 // 必须 ,其中ibreeze-001改为缺陷号。
开发代码
git status //建议，通过IDE可视化操作时注意查看自己修改的内容
git add -A // 必须 参数可选(.英文句号，提交新增和修改;-A 提交所有变化;-u 提交修改和删除，不包括新建)，可以指定文件名
git status //建议，通过IDE可视化操作时注意查看自己修改的内容
git commit -m ‘【功能模块】变更描述’ //必须
git pull origin develop // 必须
如无冲突则继续下一步，有冲突则解决冲突
git push -u origin feature/ibreeze-001 //必须


```

自测完成后在Gitlab发起合并请求，源分支为feature/ibreeze-001，目标分支为develop,正确合并完成后删除来源分支，已合并的分支需要再次修改则建立新分支。
注意事项：

git config core.autocrlf input
设置自动换行转义，Windows设置为true，linux、Mac设置为input。参考Git换行问题
git config core.safecrlf true
强制校验换行符号与仓库一致
git checkout feature/ibreeze-001
当目标分支修改提交后可不加选项切换到其他分支，-b 选项为创建并切换分支
git branch -a 可查看所有本地分支，-d 删除指定分支
用代码库文件覆盖本地文件
git fetch --all
git reset --hard origin/develop
