package com.lrz.multi;

import android.util.Log;

import com.lrz.multi.Interface.IMultiData;
import com.lrz.multi.annotation.Table;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 不可混淆
 */
public class MultiData {
    public static final String DEFAULT_TABLE = "DEFAULT_SP";
    private final ConcurrentHashMap<Class, Object> data = new ConcurrentHashMap<>();
    public static final MultiData MULTI_DATA = new MultiData();
    private volatile ConcurrentHashMap<Class, Class> classHashMap;

    public <T> T get(Class<T> tClass) {
        T t = (T) data.get(tClass);
        if (t == null) {
            try {
                Table table = ((Table) tClass.getAnnotation(Table.class));
                if (table != null) {
                    Class imp = classHashMap.get(tClass);
                    if (imp == null) {
                        imp = MultiDataUtil.getTableImp(tClass);
                    }
                    IMultiData data = (IMultiData) imp.newInstance();
                    data.loadMulti();
                    t = (T) data;
                    this.data.put(tClass, data);
                } else {
                    t = tClass.newInstance();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    void initPre() {
        try {
            Map<Class, Class> map = getClassHash();
            for (Map.Entry<Class, Class> entry : map.entrySet()) {
                Class imp = entry.getValue();
                Table table = ((Table) entry.getKey().getAnnotation(Table.class));
                if (table == null || table.lazy()) continue;
                if (imp == null) {
                    imp = MultiDataUtil.getTableImp(entry.getKey());
                }
                IMultiData data = (IMultiData) imp.newInstance();
                data.loadMulti();
                this.data.put(entry.getKey(), data);
                Log.e("MultiData", "预初始化成功：" + MultiDataUtil.GSON.toJson(data));
            }
        } catch (Exception e) {
            Log.e("MultiData", "预初始化失败,请检查混淆配置");
            e.printStackTrace();
        }
    }

    Map<Class, Class> getClassHash() {
        if (classHashMap == null) {
            synchronized (this) {
                if (classHashMap == null) {
                    Class c = null;
                    try {
                        c = Class.forName("com.lrz.multi.Interface.MultiConstants");
                        classHashMap = new ConcurrentHashMap<>();
                        classHashMap.putAll((Map<? extends Class, ? extends Class>) c.getDeclaredField("CLASSES").get(c));
                    } catch (Exception e) {
                        Log.e("MultiData", "没有发现注册的接口类，初始化失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        return classHashMap;
    }
}
