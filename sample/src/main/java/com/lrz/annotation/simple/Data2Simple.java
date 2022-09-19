package com.lrz.annotation.simple;

import com.lrz.annotation.simple.data.TextConfig;
import com.lrz.annotation.simple.data.TextConfig2;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

/**
 * 二级表，其在 DataSimple中被定义
 */
@Table(name = "data_simple2",lazy = true)
public interface Data2Simple {
    @Get(name = "config1")
    TextConfig getConfig1();

    @Set(name = "config1")
    void setConfig1(TextConfig config);

    /**
     * 表是唯一的，表在全局中要保证唯一性
     * 如果必须有这种需求，则可以创建子类继承，并设置新的表名
     */
    @Get(name = "config2")
    TextConfig2 getConfig2();

    @Set(name = "config2")
    void setConfig2(TextConfig2 config);
}
