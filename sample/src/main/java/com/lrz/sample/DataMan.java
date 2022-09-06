package com.lrz.sample;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

/**
 * 本地存储类，将类映射成sp中的表
 * 该类和其子类，不可混淆
 */
@Table(name = "data_man", lazy = true)
public interface DataMan {
    @Get(name = "man_name")
    String getName();


    @Set(name = "man_name")
    void setName(String name);
}
