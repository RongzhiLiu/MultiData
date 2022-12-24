package com.lrz.annotation.simple;

import android.app.Application;

import com.lrz.multi.MultiData;
import com.lrz.multi.MultiDataManager;
import com.lrz.multi.MultiDataUtil;
import com.lrz.multi.annotation.OnMultiDataListener;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDataManager.MANAGER.setOnMultiDataListener(new OnMultiDataListener() {
            @Override
            public void onClear(String table) {
                MultiDataUtil.clear(table);
            }

            @Override
            public <T> void onSave(String table, String key, T value) {
                MultiDataUtil.putAsy(table, key, value);
                System.out.println("--------save:" + value.getClass());
            }

            @Override
            public <T> T onLoad(String table, String key, T value) {
                return MultiDataUtil.get(table, key, value);
            }
        });
        MultiDataManager.setMemorySize(1024 * 1024);
        MultiDataManager.initPre(this);
    }
}
