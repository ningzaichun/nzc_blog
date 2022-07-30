# Git 在工作中的最佳实践

> 就是最近开始工作啦吗，然后 Git 是团队开发中必须要会的一个工具，在最近的使用中，发现自己还是有很多细节问题没能把控好，也正巧赶着活动写下了这篇文章。

本文的大致内容为：基本操作、分支开发、误操挽救、提交规范

默认大家 Git 的环境是没有问题的哈。

## Git 基本操作

既然是工作中的最佳实践，那么我也会用案例的方式来力争将每一个步骤说明白，争取不浪费大家的时间。

平时都是团队开发，所以本地我会分别用 `git01` 和 `git02` 两个文件夹模拟团队开发

![image-20220713204933080](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220713204933080.png)

远程仓库使用 `gitee`，在你新建好仓库好一刻，不要勾选什么文件，直接生成空的文件，你会看到 gitee 仓库给除的提示命令。

![image-20220713205709382](C:\Users\ASUS\Desktop\nzc_blog\img\image-20220713205709382.png)



```bash
git config --global user.name "宁在春"
git config --global user.email "xxxx@qq.com"

mkdir git_study
cd git_study
git init 
touch README.md
git add README.md
git commit -m "first commit"
git remote add origin https://gitee.com/crushlxb/git_study.git
git push -u origin "master"

# 已有仓库
cd existing_git_repo
git remote add origin https://gitee.com/crushlxb/git_study.git
git push -u origin "master"
```

咱们先把本地环境搭起来吧。我首先在 git01 文件下创建了一个 maven 项目。









## Git 分支开发











## Git 误操挽救





## Git 提交规范