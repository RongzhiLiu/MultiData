# MultiData

MultiData 是一个缓存管理中间件，将业务逻辑和缓存解耦，方便且快速的以对象/接口的形式去管理缓存,你定义的每一个带有@Table的接口/类 在内存中都是一个单例，在磁盘中都是一个单独的文件

#### 1.添加依赖 和混淆配置
```java
// 在project下的build.gradle中添加如下maven地址
maven { url 'https://jitpack.io' }
```        
```java
	//依赖
api 'com.github.RongzhiLiu.MultiData:annotation:v1.0.7'
annotationProcessor 'com.github.RongzhiLiu.MultiData:processor:v1.0.7'
  //混淆
-keep interface com.lrz.multi.Interface.** { *; }
-keep class com.lrz.multi.Interface.** { *; }        
-keep class * extends  com.lrz.multi.Interface.IMultiData
```



#### 2.定义缓存对象

##### 		a.需要缓存的对象，可以是一个接口（我们更加建议使用接口的形式），例：

```java
/**
 * Table 给接口定义缓存的表名
 * Get/Set 定义表中的字段名
 * 表中的字段类型支持：
 * 	基本类型，string，
 *	Map,List,Set,HashMap,ArrayList,HashSet,TreeSet,LinkedList
 *	表接口/对象，javaBean
 */
@Table(name = "data_simple")
public interface DataSimple {
    @Get(name = "name")
    String getStr();

    @Set(name = "name")
    void setStr(String str);
  
  	@Get(name = "map")
    Map<String,String> getMap();

    @Set(name = "map")
    void setMap(Map<String,String> map);
}

```

#####				b.也可以定义一张class 类型（注意：class中的变量和方法，不可以是private,且Get/Set中的字段名必须和变量名保持一致）

```java
/**
 * class 类型的表
 * 注意，class类型的表，必须Get/Set中的字段名必须和变量名保持一致
 */
@Table(name = "text_config")
public class TextConfig {
    @SerializedName("book_id")
    protected String bookId;

    @Get(name = "bookId")
    public String getBookId() {
        return bookId;
    }

    @Set(name = "bookId")
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}


```

#####				c.支持定义二级表对象，实际的读写和缓存操作依旧是分离的，在磁盘上也指向不同的文件

```java
@Table(name = "data_simple")
public interface DataSimple {
    @Get(name = "name")
    String getStr();

    @Set(name = "name")
    void setStr(String str);

    //二级表，接口类型
    @Get(name = "data")
    Data2Simple getData2();

    @Set(name = "data")
    void setData2(Data2Simple data2);
}
```

#### 2.注解的含义

##### 		a.@Table

```java
/**
Table注解有两个属性
	name:表名，必填，必须全局唯一，请结合实际业务定义，定义了两个相同的表名，则在磁盘上将会指向同一个文件，且，相同字段名会有相互覆盖甚至解析异常的风险
	lazy:在MultiDataManager.initPre(context);初始化时，会先将磁盘上的表加载出来，如果lazy=true，则不会被提前加载
			默认值 false
*/
@Table(name = "CollectionData",lazy = true)
public interface DataSimple {}
```

##### 		b.@Get/Set

```java
// 一个属性对应一个get 方法和一个set方法，此方法成对出现
@Get(name = "name")  	@Set(name = "name")
// 其中，我们可以通过@Get(default)来指定对应属性的初始默认值（只有基本类型和string 才可以指定默认值）
@Get(name = "name",defaultString = "1")
```



#### 3.初始化和 加载/保存 实例对象

##### 		a.在使用前，请先初始化

```java
//MultiData 采用的磁盘缓存方案是SharedPreferences，保存时通过单线程操作，且通过合理的分表，可有效避免sp导致的anr
//你也可以通过下面方法来自定义磁盘缓存
MultiDataManager.MANAGER.setOnMultiDataListener(new OnMultiDataListener() {
            @Override
            public <T> void onSave(String table, String key, T value) {
                //将键值对保存到 table文件中
            }

            @Override
            public <T> T onLoad(String table, String key, T value) {
              //从文件table中加载key对应的值，T value是该类型的默认值，如果磁盘上没有，则直接返回value
                return value;
            }

            @Override
            public void onClear(String table) {
							// 清除文件上的缓存
            }
        });

//调用 initPre初始化（此方法非强制调用，可根据实际情况确定是否需要使用）
MultiDataManager.initPre(context);
//可设置最大内存，默认1mb,内存中只保留最热的数据
MultiDataManager.setMemorySize(1024*1024);
```

##### 		b.每一个被定义的@Table 的interface/Class 在内存中都是一个单例对象，可通过以下方式获取

```java
//获取
DataSimple simple = MultiData.DATA.get(DataSimple.class);
//使用 get方法
simple.getStr();
//使用set方法，调用set()给该实例设置新的属性时，都会直接反应到磁盘上，下一次打开app，将可以读取到最新的属性值
simple.setStr("my name");
//使用get获取我们定义的map时，在操作map做put 和remove操作时，也同样会反应到磁盘上，而不用针对磁盘的读写做额外的操作
//集合类属性的多次修改，在同一个时间片下，只会操作一次磁盘，不用担心性能问题
simple.getMap().put(string,string);

```

##### 		c.保存/清除某个表的数据

```java
// 将TextConfig实例保存到MultiData中，之前的数据将会被新的实例覆盖，通常，只有Class 才会使用到此方法，interface则不用
MultiData.DATA.clear(TextConfig.class,config);
// 清除DataSimple的所有数据和缓存
MultiData.DATA.clear(DataSimple.class);
```

#### 4.高级用法

##### 		1.一个class/interface 在内存中之对应一个实例，如果保存两个相同类型的实例呢？

​		解：一个子类去继承class/interface。并给其设置Table表名，则可以生成两个相同类型的实例对象，例：

```java
@Table(name = "data_simple3")
public interface Data3Simple extends DataSimple{

}
@Table(name = "data_simple2")
public interface Data2Simple extends DataSimple{

}
//获取
DataSimple simple = MultiData.DATA.get(DataSimple2.class);
DataSimple simple = MultiData.DATA.get(DataSimple3.class);
```

##### 	2.对于class/interface 中，可以有普通的javabean（没有设置Table 的普通类）吗？

##### 	应尽量避免定义包含很多属性且没有设置Table 的类型，这种普通类型的bean 在保存到磁盘时，是以json 的形式保存的，虽然在框架自带的sp中已经实现的异步磁盘操作，但是频繁的修改下，也会影响性能，且，这一类对象，在get出来后，还需要手动set才能保存到磁盘中，如下

```java
//包含很多子属性的类型
public class LargeData {
    String name;
    String desc;
    String url;
    ArrayList<String> data;
    ...
}
@Table(name = "data_simple2", lazy = true)
public interface Data2Simple {
    @Get(name = "large_data")
    LargeData getLargeData();
  
    @Set(name = "large_data")
    void setLargeData(LargeData largeData);
}

//那么我们在通过get获取到LargeData。并修改其内部属性后，都要再次调用set 通知其刷新
LargeData largeData = MultiData.DATA.get(DataSimple2.class).getLargeData();
largeData.setName("name");
//调用set 重新保存
MultiData.DATA.get(DataSimple2.class).setLargeData(largeData);
```



##### 若有问题，请提交issues
