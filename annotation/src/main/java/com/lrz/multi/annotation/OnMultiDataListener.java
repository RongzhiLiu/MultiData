package com.lrz.multi.annotation;

public interface OnMultiDataListener {
    <T> void onSave(String table, String key, T value);

    <T> T onLoad(String table, String key, T value);
}
