package com.lrz.multi;

import android.text.TextUtils;

import com.lrz.multi.Interface.IMultiCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @Author: liurongzhi
 * @CreateTime: 2022/9/15
 * @Description:
 */
public class MultiLinkedList<T> extends LinkedList<T> implements IMultiCollection<Collection<T>> {
    private String table;
    private String key;

    public MultiLinkedList(String table, String key) {
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
    public T removeFirst() {
        T t = super.removeFirst();
        onChanged(this.table, this.key);
        return t;
    }

    @Override
    public T removeLast() {
        T t = super.removeLast();
        onChanged(this.table, this.key);
        return t;
    }

    @Override
    public void addLast(T t) {
        super.addLast(t);
        onChanged(this.table, this.key);
    }

    @Override
    public void addFirst(T t) {
        super.addFirst(t);
        onChanged(this.table, this.key);
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

    @Override
    public void putAllData(Collection<T> es) {
        if (es != null) {
            super.addAll(es);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MultiDataManager.MANAGER.getInnerDataListener().onSave(table, key, MultiLinkedList.this);
        }
    };

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public String getKey() {
        return key;
    }
}
