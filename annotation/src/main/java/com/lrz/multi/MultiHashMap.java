package com.lrz.multi;

import android.text.TextUtils;

import com.lrz.multi.Interface.IMultiCollection;

import java.util.HashMap;
import java.util.Map;

public class MultiHashMap<V> extends HashMap<String, V> implements IMultiCollection {
    private final String table;
    private final String key;

    public MultiHashMap(String table, String key) {
        this.table = table;
        this.key = key;
    }

    @Override
    public V put(String key, V value) {
        V v = super.put(key, value);
        onChanged(this.table, this.key);
        return v;
    }

    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        if (v != null) {
            onChanged(this.table, this.key);
        }
        return v;
    }

    @Override
    public boolean remove(Object key, Object value) {
        boolean result = super.remove(key, value);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        super.putAll(m);
        if (!m.isEmpty()) {
            onChanged(this.table, this.key);
        }
    }

    @Override
    public void clear() {
        super.clear();
        onChanged(this.table, this.key);
    }

    @Override
    public void onChanged(String table, String key) {
        if (TextUtils.isEmpty(table) || TextUtils.isEmpty(key)) return;
        if (!MultiDataUtil.hasTask(runnable)) {
            MultiDataUtil.postTask(runnable);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MultiDataManager.MANAGER.getInnerDataListener().onSave(table, key, MultiHashMap.this);
        }
    };
}