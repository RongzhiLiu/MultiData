package com.lrz.annotation.simple;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: liurongzhi
 * @CreateTime: 2022/9/15
 * @Description: 集合参数示例
 */
@Table(name = "CollectionData")
public interface CollectionData {
    @Get(name = "array_list")
    ArrayList<String> getArray();

    @Set(name = "array_list")
    void setArray(ArrayList<String> array);

    @Get(name = "hash_map")
    HashMap<String,String> getHashMap();

    @Set(name = "hash_map")
    void setHashMap(HashMap<String,String> map);

    @Get(name = "map")
    Map<String,String> getMap();

    @Set(name = "map")
    void setMap(Map<String,String> map);
}
