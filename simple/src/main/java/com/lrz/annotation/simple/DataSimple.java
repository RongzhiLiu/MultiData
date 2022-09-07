package com.lrz.annotation.simple;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

@Table(name = "data_simple",version = 0)
public interface DataSimple {
    @Get(name = "name",defaultString = "name")
    String getStr();

    @Set(name = "name")
    void setStr(String str);
}
