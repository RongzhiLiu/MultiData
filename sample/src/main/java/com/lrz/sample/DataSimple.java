package com.lrz.sample;

import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

import java.util.HashMap;
import java.util.List;

@Table(name = "data_local")
public interface DataSimple {
    @Get(name = "user_id")
    String getUserId();

    @Set(name = "user_id")
    void setUserId(String userId);

    @Set(name = "user_num")
    void setUserNum(List<String> list);

    @Get(name = "user_num")
    List<String> getUserNum();

    @Set(name = "man")
    void setMan(DataMan man);

    @Get(name = "man")
    DataMan getMan();

    @Set(name = "data_local")
    void setDataLocal(DataLocal dataLocal);

    @Get(name = "data_local")
    DataLocal getDataLocal();

    @Get(name = "hash")
    HashMap<String, DataLocal> getHash();

    @Set(name = "hash")
    void setHash(HashMap<String, DataLocal> hashMap);

    @Get(name = "num", defaultInt = 9)
    Integer getNum();

    @Set(name = "num")
    void setNum(int num);
}
