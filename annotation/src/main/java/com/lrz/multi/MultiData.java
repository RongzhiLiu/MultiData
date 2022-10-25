package com.lrz.multi;

import android.util.Log;

import com.lrz.multi.Interface.IMultiClassData;
import com.lrz.multi.Interface.IMultiData;
import com.lrz.multi.annotation.Table;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 不可混淆
 */
public class MultiData {
    private final ConcurrentHashMap<Class, Object> data = new ConcurrentHashMap<>();
    public static final MultiData DATA = new MultiData();
    private volatile ConcurrentHashMap<Class, Class> classHashMap;

    public <T> T get(Class<T> tClass) {
        T t = (T) data.get(tClass);
        if (t == null) {
            try {
                Table table = tClass.getAnnotation(Table.class);
                if (table != null) {
                    Class imp = getClassHash().get(tClass);
                    if (imp == null) {
                        imp = MultiDataUtil.getTableImp(tClass);
                    }

                    Constructor constructor = imp.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    IMultiData data = (IMultiData) constructor.newInstance();

                    data.loadMulti(true);
                    t = (T) data;
                    this.data.put(tClass, data);
                } else {
                    t = tClass.newInstance();
                }
            } catch (Exception e) {
                Log.e("MultiData", tClass.getName() + " 加载失败", e);
                e.printStackTrace();
            }
        }
        return t;
    }

    public <T> void save(Class<T> tClass, T t) {
        if (t == null) return;
        Table table = ((Table) tClass.getAnnotation(Table.class));
        if (table == null) return;
        try {
            Class imp = getClassHash().get(tClass);
            if (imp == null) {
                imp = MultiDataUtil.getTableImp(tClass);
            }
            Constructor constructor = imp.getDeclaredConstructor();
            constructor.setAccessible(true);
            IMultiData dataImp = (IMultiData) constructor.newInstance();
            if (dataImp instanceof IMultiClassData) {
                //把t保存到dataImp中
                data.put(tClass, dataImp);
                ((IMultiClassData<T>) dataImp).saveByObj(t);
            }
        } catch (Exception e) {
            Log.e("MultiData", tClass.getName() + " 保存失败", e);
        }
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
                data.loadMulti(false);
                this.data.put(entry.getKey(), data);
                Log.d("MultiData", "预初始化成功：" + data.tableName() + "  " + MultiDataUtil.GSON.toJson(data));
            }
        } catch (Exception e) {
            Log.e("MultiData", "预初始化失败,请检查混淆配置", e);
        }
    }

    public Map<Class, Class> getClassHash() {
        if (classHashMap == null) {
            synchronized (this) {
                if (classHashMap == null) {
                    classHashMap = new ConcurrentHashMap<>();
                }
                try {
                    Class c = null;
                    c = Class.forName("com.lrz.multi.Interface.MultiConstants");
                    classHashMap.putAll((Map<? extends Class, ? extends Class>) c.getDeclaredField("CLASSES").get(c));
                } catch (Exception e) {
                    Log.e("MultiData", "没有发现注册的接口类，初始化失败", e);
                    e.printStackTrace();
                }
            }
        }
        return classHashMap;
    }

    /**
     * 清除表缓存
     *
     * @param tClass
     */
    public <T> void clear(Class<T> tClass) {
        T t = (T) data.remove(tClass);
        if (t instanceof IMultiData) {
            MultiDataManager.MANAGER.getInnerDataListener().onClear(((IMultiData) t).tableName());
            return;
        }
        //删除本地缓存
        Table table = tClass.getAnnotation(Table.class);
        if (table == null) {
            Log.e("MultiData", "你删除的不是一张表，删除失败！");
            return;
        }
        if (!"DEFAULT_TABLE".equals(table.name())) {
            MultiDataManager.MANAGER.getInnerDataListener().onClear(table.name());
        } else {
            try {
                Class imp = getClassHash().get(tClass);
                if (imp == null) {
                    imp = MultiDataUtil.getTableImp(tClass);
                }

                Constructor constructor = imp.getDeclaredConstructor();
                constructor.setAccessible(true);
                IMultiData data = (IMultiData) constructor.newInstance();
                data.saveMulti();
            } catch (Exception e) {
                Log.e("MultiData", "clear error！", e);
            }
        }
    }
}
