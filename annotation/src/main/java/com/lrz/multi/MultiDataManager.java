package com.lrz.multi;


import android.app.Application;

import com.lrz.multi.Interface.IMultiData;
import com.lrz.multi.annotation.OnMultiDataListener;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class MultiDataManager {
    public static final MultiDataManager MANAGER = new MultiDataManager();
    private Application application;
    OnMultiDataListener customerListener;
    final OnMultiDataListener defaultListener = new OnMultiDataListener() {
        @Override
        public <T> void onSave(String table, String key, T value) {
            //支持基本类型，list，set，map
            if (MultiDataUtil.isPrimitive(value.getClass()) || value instanceof List || value instanceof Map || value instanceof Set) {
                if (customerListener != null) {
                    customerListener.onSave(table, key, value);
                } else {
                    MultiDataUtil.putAsy(table, key, value);
                }
            } else if (value instanceof IMultiData) {
                ((IMultiData) value).saveMulti();
            } else {
                if (customerListener != null) {
                    customerListener.onSave(table, key, value);
                } else {
                    MultiDataUtil.putAsy(table, key, value);
                }
            }
        }

        @Override
        public <T> T onLoad(String table, String key, T value) {
            if (MultiDataUtil.isPrimitive(value.getClass()) || value instanceof List || value instanceof Map || value instanceof Set) {
                if (customerListener != null) {
                    return customerListener.onLoad(table, key, value);
                } else {
                    return MultiDataUtil.get(table, key, value);
                }
            } else if (value instanceof IMultiData) {
                //不是sp支持的基本类型，则走读表
                //根据imp类找到 原始类
                Map<Class, Class> map = MultiData.MULTI_DATA.getClassHash();
                if (map == null) return value;
                Class realClass = null;
                for (Map.Entry<Class, Class> entry : map.entrySet()) {
                    if (entry.getValue() == value.getClass()) {
                        realClass = entry.getKey();
                        break;
                    }
                }
                return realClass == null ? value : (T) MultiData.MULTI_DATA.get(realClass);
            } else {
                if (customerListener != null) {
                    return customerListener.onLoad(table, key, value);
                } else {
                    return MultiDataUtil.get(table, key, value);
                }
            }
        }
    };

    public void setOnMultiDataListener(OnMultiDataListener listener) {
        customerListener = listener;
    }

    public OnMultiDataListener getInnerDataListener() {
        return defaultListener;
    }

    /**
     * 提前初始化
     * 将重要的值提前从磁盘中初始化出来
     * 如果注解中delay= true 将不会提前初始化
     * 如果存在表中表的情况，则 父表 delay = false，字表delay=true 将不生效
     *
     * @see com.lrz.multi.annotation.Table 中 delay = false
     */
    public void initPre(Application application) {
        if (application == null) return;
        this.application = application;
        MultiData.MULTI_DATA.initPre();
    }

    public Application getApplication() {
        return application;
    }
}
