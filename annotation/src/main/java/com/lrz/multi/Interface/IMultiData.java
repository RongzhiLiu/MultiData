package com.lrz.multi.Interface;

public interface IMultiData {
    //当imp类作为子类时，需要将其重新序列化到磁盘中
    void saveMulti();

    void loadMulti();

    String tableName();

    boolean isLazy();
}
