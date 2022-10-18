package com.lrz.multi.Interface;

/**
 * @Author: liurongzhi
 * @CreateTime: 2022/9/15
 * @Description:
 */
public interface IMultiCollection<D> {
    void onChanged(String table,String key);
    void putAllData(D d);
}
