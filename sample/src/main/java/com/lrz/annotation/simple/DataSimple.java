package com.lrz.annotation.simple;

import com.lrz.annotation.simple.data.TextConfig;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

/**
 * 基本类型，二级表示例
 */
@Table(name = "data_simple")
public interface DataSimple {
    @Get(name = "name")
    String getStr();

    @Set(name = "name")
    void setStr(String str);

    @Get(name = "page", defaultInt = 2)
    int getPage();

    @Set(name = "page")
    void setPage(int p);

    //二级表，接口类型
    @Get(name = "data")
    Data2Simple getData2();

    @Set(name = "data")
    void setData2(Data2Simple data2);

    // 二级表，实体类型
    @Set(name = "text_config")
    void setTextConfig(TextConfig config);

    @Get(name = "text_config")
    TextConfig getTextConfig();
}
