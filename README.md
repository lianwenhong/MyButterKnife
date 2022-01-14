# MyButterKnife

ButterKnife通过注解的方式实现视图映射和事件的绑定，其实原理是通过自定义编译期注解以及注解解析器在代码编译期动态的注入自定义类ViewBinder，在ViewBinder内部帮用户实现本该由用户实现的findViewById、setOnclickListener等代码以达到使业务代码看起来更简洁方便的效果。

关于ButterKnife组件化过程中的R2实现原因及原理我查阅了几篇文章：
ButterKnife 是如何通过 Gradle Plugin 来生成 R2 类的原理分析（https://www.jianshu.com/p/33bf0c0e6453）
https://www.sohu.com/a/333371250_611601

ButterKnife使用R2解决组件化中id冲突问题的思路是：生成一个R2.java文件并将 R.java 文件拷贝一份到 R2.java 中，然后将R2中每一个属性加上final变成常量，这样最终就变成 @BindView(R2.id.textview) 了。


