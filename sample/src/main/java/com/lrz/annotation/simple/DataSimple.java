package com.lrz.annotation.simple;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 基本类型，二级表示例
 */
@Table(name = "data_simple", lazy = true)
public interface DataSimple {
    @Get(name = "map")
    Map<String, UserInfo> getMap();

    @Set(name = "map")
    void setMap(Map<String, UserInfo> map);

    @Get(name = "mapint")
    Map<String, Integer> getMapI();

    @Set(name = "mapint")
    void setMapI(Map<String, Integer> map);

    @Get(name = "hash")
    HashMap<String, UserInfo> getHashMap();

    @Set(name = "hash")
    void setHashMap(HashMap<String, UserInfo> map);

    @Get(name = "set")
    HashSet<UserInfo> getSet();

    @Set(name = "set")
    void setSet(HashSet<UserInfo> set);

    @Get(name = "set1")
    java.util.Set<UserInfo> getSet1();

    @Set(name = "set1")
    void setSet1(java.util.Set<UserInfo> set);

    @Get(name = "list")
    List<UserInfo> getList();

    @Set(name = "list")
    void setList(List<UserInfo> map);


    @Get(name = "str")
    String getStr();

    @Set(name = "str")
    void setStr(String str);
}
