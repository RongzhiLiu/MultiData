# MultiData

MultiData is a cache management middleware that decouples business logic and cache, and manages cache in the form of objects/interfaces conveniently and quickly. Each interface/class you define with @Table is a single entity in memory. For example, it is a single file on disk

#### 1. Add dependencies and obfuscate configuration
```java
// add maven in project file build.gradle
maven { url 'https://jitpack.io' }
```        
````java
//depends
api 'com.github.RongzhiLiu.MultiData:annotation:v1.0.9'
annotationProcessor 'com.github.RongzhiLiu.MultiData:processor:v1.0.9'
  //obfuscate
-keep interface com.lrz.multi.Interface.** { *; }
-keep class com.lrz.multi.Interface.** { *; }
-keep class * extends com.lrz.multi.Interface.IMultiData
````



#### 2. Define the cache object

##### 		a. The object to be cached can be an interface (we recommend using the interface form), for example:

````java
/**
 * Table defines the cache table name for the interface
 * Get/Set defines the field name in the table
 * Field types in tables support:
 * basic type, string,
 * Map,List,Set,HashMap,ArrayList,HashSet,TreeSet,LinkedList
 * table interface/object, javaBean
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

````

##### 		b. You can also define a class type (note: variables and methods in class cannot be private, and the field name in Get/Set must be consistent with the variable name)

````java
/**
 * a table of type class
 * Note that for a table of class type, the field name in Get/Set must be consistent with the variable name
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


````

##### 		c. Supports the definition of secondary table objects, the actual read and write and cache operations are still separated, and also point to different files on the disk

````java
@Table(name = "data_simple")
public interface DataSimple {
    @Get(name = "name")
    String getStr();

    @Set(name = "name")
    void setStr(String str);

    //Secondary table, interface type
    @Get(name = "data")
    Data2Simple getData2();

    @Set(name = "data")
    void setData2(Data2Simple data2);
}
````

#### 2. The meaning of annotations

##### 		a.@Table

````java
/**
Table annotation has two properties
name: table name, required, must be globally unique. If two identical table names are defined in combination with the actual business definition, they will point to the same file on the disk, and the same field names will overwrite each other or even resolve exceptions risks of
lazy: When MultiDataManager.initPre(context); is initialized, the table on the disk will be loaded first. If lazy=true, it will not be loaded in advance
Default value false
*/
@Table(name = "CollectionData", lazy = true)
public interface DataSimple {}
````

##### 		b.@Get/Set

````java
// A property corresponds to a get method and a set method, this method appears in pairs
@Get(name = "name") @Set(name = "name")
// Among them, we can specify the initial default value of the corresponding attribute through @Get(default) (only basic types and strings can specify the default value)
@Get(name = "name",defaultString = "1")
````



#### 3. Initialize and load/save instance objects

##### 		a. Please initialize before use

````java
//The disk cache scheme adopted by MultiData is SharedPreferences, which is operated through a single thread when saving, and can effectively avoid anr caused by sp through reasonable table division.
//You can also customize the disk cache by the following methods
MultiDataManager.MANAGER.setOnMultiDataListener(new OnMultiDataListener() {
            @Override
            public <T> void onSave(String table, String key, T value) {
                //Save the key-value pair to the table file
            }

            @Override
            public <T> T onLoad(String table, String key, T value) {
              //Load the value corresponding to the key from the file table, T value is the default value of this type, if there is no value on the disk, return the value directly
                return value;
            }

            @Override
            public void onClear(String table) {
// clear the cache on the file
            }
        });

//Call initPre to initialize (this method is not mandatory, you can determine whether it needs to be used according to the actual situation)
MultiDataManager.initPre(context);
````

##### 		b. Each defined interface/Class of @Table is a singleton object in memory, which can be obtained in the following ways

````java
//Obtain
DataSimple simple = MultiData.DATA.get(DataSimple.class);
// use the get method
simple.getStr();
//Using the set method, when calling set() to set a new attribute for the instance, it will be directly reflected on the disk. The next time you open the app, you will be able to read the latest attribute value
simple.setStr("my name");
//When using get to get the map we defined, when operating the map to do put and remove operations, it will also be reflected on the disk, instead of doing additional operations for the read and write of the disk
//Multiple modifications of the properties of the collection class, in the same time slice, will only operate the disk once, so don't worry about performance issues
simple.getMap().put(string,string);

````

##### 		c. Save/clear data of a table

````java
// Save the TextConfig instance to MultiData, the previous data will be overwritten by the new instance, usually, this method is only used for Class, not for interface
MultiData.DATA.clear(TextConfig.class,config);
// Clear all data and cache of DataSimple
MultiData.DATA.clear(DataSimple.class);
````

#### 4. Advanced usage

##### 		1. A class/interface corresponds to an instance in memory. What if two instances of the same type are stored?

 Solution: A subclass inherits class/interface. And set the Table name to it, you can generate two instance objects of the same type, for example:

````java
@Table(name = "data_simple3")
public interface Data3Simple extends DataSimple{

}
@Table(name = "data_simple2")
public interface Data2Simple extends DataSimple{

}
//Obtain
DataSimple simple = MultiData.DATA.get(DataSimple2.class);
DataSimple simple = MultiData.DATA.get(DataSimple3.class);
````

##### 		2. For class/interface, can there be ordinary javabeans (ordinary classes without Table)?

##### Try to avoid defining a type that contains many attributes and does not set Table. This common type of bean is saved in the form of json when it is saved to disk, although it has been implemented in the sp that comes with the framework. Asynchronous disk operation, but frequent modification will also affect performance, and this type of object needs to be manually set to save to disk after get, as follows

````java
// type with many sub-properties
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

//Then we are getting LargeData through get. And after modifying its internal properties, call set again to notify it to refresh
LargeData largeData = MultiData.DATA.get(DataSimple2.class).getLargeData();
largeData.setName("name");
//call set to re-save
MultiData.DATA.get(DataSimple2.class).setLargeData(largeData);
````



##### If you have any questions, please submit issues
