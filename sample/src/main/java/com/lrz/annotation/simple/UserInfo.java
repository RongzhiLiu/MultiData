package com.lrz.annotation.simple;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Author:  liurongzhi
 * CreateTime:  2022/12/1
 * Description:
 */
@Table(name = "user_info")
public class UserInfo {
    protected String name;

    @Get(name = "name")
    public String getName() {
        return name;
    }
    @Set(name = "name")
    public void setName(String name) {
        this.name = name;
    }


}
