package com.lrz.multi;

import android.text.TextUtils;

import com.lrz.multi.Interface.IMultiCollection;

import java.util.Collection;
import java.util.TreeSet;

/**
 * @Author: liurongzhi
 * @CreateTime: 2022/9/15
 * @Description:
 */
public class MultiTreeSet<E> extends TreeSet<E> implements IMultiCollection {
    private final String table;
    private final String key;

    public MultiTreeSet(String table, String key) {
        this.table = table;
        this.key = key;
    }

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = super.addAll(c);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = super.removeAll(c);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
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
            MultiDataManager.MANAGER.getInnerDataListener().onSave(table, key, MultiTreeSet.this);
        }
    };
}
