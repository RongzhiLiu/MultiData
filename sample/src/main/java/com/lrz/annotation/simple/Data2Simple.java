package com.lrz.annotation.simple;

import com.lrz.annotation.simple.data.LargeData;
import com.lrz.annotation.simple.data.TextConfig;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

/**
 * 二级表，其在 DataSimple中被定义
 */
@Table(name = "data_simple2", lazy = true)
public interface Data2Simple {
    @Get(name = "large_data")
    LargeData getLargeData();

    @Set(name = "large_data")
    void setLargeData(LargeData largeData);

    @Get(name = "config1")
    TextConfig getConfig1();

    @Set(name = "config1")
    void setConfig1(TextConfig config);

}
