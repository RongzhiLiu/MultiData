package com.lrz.multi;

import android.text.TextUtils;

import com.lrz.multi.Interface.IMultiCollection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @Author: liurongzhi
 * @CreateTime: 2022/9/15
 * @Description:
 */
public class MultiArrayList<T> extends ArrayList<T> implements IMultiCollection {
    private final String table;
    private final String key;

    public MultiArrayList(String table, String key) {
        this.table = table;
        this.key = key;
    }

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);
        onChanged(this.table, this.key);
        return result;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        onChanged(this.table, this.key);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);
        if (result) {
            onChanged(this.table, this.key);
        }
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = super.addAll(index, c);
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
    public T remove(int index) {
        T t = super.remove(index);
        onChanged(this.table, this.key);
        return t;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        onChanged(this.table, this.key);
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
            MultiDataManager.MANAGER.getInnerDataListener().onSave(table, key, MultiArrayList.this);
        }
    };
}
