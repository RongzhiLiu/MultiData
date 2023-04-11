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
public class UserInfo implements Comparable<UserInfo>{
    public String name;
    public int i;
    public boolean b;

    @Override
    public int compareTo(UserInfo o) {
        return 0;
    }
}
