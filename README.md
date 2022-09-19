# MultiData

MultiData 是一个管理全局配置的工具框架，让业务层不用再去关注全局配置的加载和读写，甚至是空判断，隔离了配置的缓存和读写。并可以维护多张表

##### 1.以接口定义配置

##### 2.通过注解来实现全局配置自动化管理

##### 3.支持缓存，配置默认的缓存策略是SharedPreferences

##### 4.天然支持分表，每一个接口即是一张表，对应磁盘中的一个文件

##### 5.支持接口嵌套（表始终保持分离）

##### 6.支持的数据类型，基本类型，string，map（hash），set（hash），list等，也支持自定义的javabean（在磁盘中通过json 保存）

##### 7.支持自定义缓存策略，如：将缓存策略改为mmkv，或者是sql 等；

##### 8.混淆配置

```
-keep interface com.lrz.multi.Interface.** { *; }
-keep class * extends  com.lrz.multi.Interface.IMultiData
```
