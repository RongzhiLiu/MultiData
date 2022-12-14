package com.lrz.multi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lrz.multi.Interface.IMultiCollection;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Author And Date: liurongzhi on 2020/12/22.
 * Description: com.yilan.sdk.common.util
 */
public class MultiDataUtil {

    /**
     * 异步调用sp。可避免anr的发生，在极短时间内（毫秒级）不需要保证数据一致性，建议调用此方法，否则使用apply。
     * 例：如果想异步保存，且想保证其他页面刷新用到最新数据，可使用 putAsy(table,key, value,result)
     * 2022-8-31：将异步操作放到单线程中，如果是多个线程操作commit 也会导致anr
     * todo 将这种后台操作文件和数据库的线程统一使用一个线程，且使用线程池调度，增加线程的利用率
     *
     * @param table 表名
     * @param key   键
     * @param value 值
     * @see MultiDataUtil#putAsy(String, String, Object
     */
    public static void putAsy(final String table, final String key, final Object value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || value == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table))
            return;
        getHandlerThread().execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSp(context, table);
                if (value instanceof Integer) {
                    sharedPreferences.edit().putInt(key, (Integer) value).commit();
                } else if (value instanceof String) {
                    sharedPreferences.edit().putString(key, (String) value).commit();
                } else if (value instanceof Float) {
                    sharedPreferences.edit().putFloat(key, (Float) value).commit();
                } else if (value instanceof Long) {
                    sharedPreferences.edit().putLong(key, (Long) value).commit();
                } else if (value instanceof Boolean) {
                    sharedPreferences.edit().putBoolean(key, (Boolean) value).commit();
                } else {
                    sharedPreferences.edit().putString(key, GSON.toJson(value)).commit();
                }
            }
        });
    }

    /**
     * 内存同步，但磁盘异步（apply），此方法可以保证极短时间内数据一致性，但在大量数据频繁存取的情况下，可能会导致anr，请结合业务实际情况使用
     *
     * @param table 表名
     * @param key   键
     * @param value 值
     */
    public static <T> void put(String table, String key, T value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || value == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table))
            return;
        SharedPreferences sharedPreferences = getSp(context, table);
        if (value instanceof Integer) {
            sharedPreferences.edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof String) {
            sharedPreferences.edit().putString(key, (String) value).apply();
        } else if (value instanceof Float) {
            sharedPreferences.edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Long) {
            sharedPreferences.edit().putLong(key, (Long) value).apply();
        } else if (value instanceof Boolean) {
            sharedPreferences.edit().putBoolean(key, (Boolean) value).apply();
        } else {
            sharedPreferences.edit().putString(key, GSON.toJson(value)).apply();
        }
    }

    /**
     * 获取对应key的值
     *
     * @param table 表名
     * @param key   键
     * @param value 默认值
     * @param <V>   值的类型
     * @return <V>
     */
    public static <V> V get(String table, String key, V value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table)) return value;
        SharedPreferences sharedPreferences = getSp(context, table);
        V v = value;
        if (value instanceof Integer) {
            v = (V) (Integer) sharedPreferences.getInt(key, (Integer) value);
        } else if (value instanceof String) {
            v = (V) sharedPreferences.getString(key, (String) value);
        } else if (value instanceof Float) {
            v = (V) (Float) sharedPreferences.getFloat(key, (Float) value);
        } else if (value instanceof Long) {
            v = (V) (Long) sharedPreferences.getLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            v = (V) (Boolean) sharedPreferences.getBoolean(key, (Boolean) value);
        } else if (value instanceof Map) {
            v = (V) getHash(table, key, (Map<Object, Object>) value);
        } else if (value instanceof List) {
            v = (V) getList(table, key, (List<Object>) value);
        } else if (value instanceof Set) {
            v = (V) getSet(table, key, (Set<Object>) value);
        } else {
            String json = getSp(context, table).getString(key, "");
            v = (V) GSON.fromJson(json, value.getClass());
        }
        if (v != null) {
            return v;
        } else {
            return value;
        }
    }

    public static <K, V> Map<K, V> getHash(String table, String key, Map<K, V> value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table)) return value;
        String json = getSp(context, table).getString(key, "");
        Map<K, V> map = GSON.fromJson(json, new TypeToken<Map<K, V>>() {
        }.getType());
        if (value instanceof IMultiCollection && map != null) {
            ((IMultiCollection) value).putAllData(map);
            map = value;
        }
        return map == null ? value : map;
    }

    public static <K> List<K> getList(String table, String key, List<K> value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table)) return value;
        String json = getSp(context, table).getString(key, "");
        List<K> map = GSON.fromJson(json, new TypeToken<List<K>>() {
        }.getType());
        if (value instanceof IMultiCollection && map != null) {
            ((IMultiCollection) value).putAllData(map);
            map = value;
        }
        return map == null ? value : map;
    }

    public static <K> Set<K> getSet(String table, String key, Set<K> value) {
        final Context context = MultiDataManager.MANAGER.getContext();
        if (context == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(table)) return value;
        String json = getSp(context, table).getString(key, "");
        Set<K> map = GSON.fromJson(json, new TypeToken<Set<K>>() {
        }.getType());
        if (value instanceof IMultiCollection && map != null) {
            ((IMultiCollection) value).putAllData(map);
            map = value;
        }
        return map == null ? value : map;
    }

    public static void clear(String table) {
        getHandlerThread().execute(() -> {
            final Context context = MultiDataManager.MANAGER.getContext();
            if (context == null || TextUtils.isEmpty(table)) return;
            getSp(context, table).edit().clear().commit();
        });
    }

    private static SharedPreferences getSp(Context context, String table) {
        return context.getSharedPreferences(table, Context.MODE_PRIVATE);
    }

    static volatile ExecutorService handlerThread;

    static ExecutorService getHandlerThread() {
        if (handlerThread == null) {
            synchronized (MultiDataUtil.class) {
                if (handlerThread == null) {
                    handlerThread = Executors.newSingleThreadExecutor(new ThreadFactory() {
                        @Override
                        public Thread newThread(final Runnable runnable) {
                            Runnable wrapperRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                    runnable.run();
                                }
                            };
                            return new Thread(wrapperRunnable);
                        }
                    });
                }
            }
        }
        return handlerThread;
    }


    /**
     * 判断class 是不是基本类型或string
     *
     * @param c class对象
     * @return true or false
     */
    public static boolean isPrimitive(Class c) {
        return c.isPrimitive() || c == String.class;
    }

    public static final Gson GSON = new Gson();


    /**
     * 获取class 的实现类
     *
     * @param klass
     * @return
     */
    public static <C> Class<C> getTableImp(Class<C> klass) {
        final String fullPackage = klass.getPackage().getName();
        String name = klass.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty()
                ? name
                : name.substring(fullPackage.length() + 1);
        final String implName = postPackageName.replace(".", "") + "Imp";
        try {

            final String fullClassName = fullPackage.isEmpty()
                    ? implName
                    : fullPackage + "." + implName;
            final Class aClass;
            try {
                aClass = Class.forName(
                        fullClassName, true, klass.getClassLoader());
                return aClass;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private final static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void postTask(Runnable runnable) {
        Message m = Message.obtain(mainHandler, runnable);
        //hasJob()只会检测what=0的任务
        m.what = runnable.hashCode();
        mainHandler.sendMessage(m);
    }

    public static boolean hasTask(Runnable runnable) {
        return mainHandler.hasMessages(runnable.hashCode());
    }

    /**
     * 获取object 占用的内存大小
     *
     * @param object
     * @return
     */
    public static int getObjectSize(Object object) {
        int size = 8 + 4;
        Class c = object.getClass();
        while (c != Object.class) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                size += getFieldSize(field, object);
            }
            c = c.getSuperclass();
        }
        return size;
    }

    private static int getFieldSize(Field field, Object obj) {
        if (field == null) return 0;
        field.setAccessible(true);
        Class<?> aClass = field.getType();
        Object fieldValue = null;
        try {
            fieldValue = field.get(obj);
            if (fieldValue == null) return 0;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (aClass == Integer.class || aClass == int.class
                || aClass == float.class || aClass == Float.class) {
            return 4;
        } else if (aClass == Long.class || aClass == long.class
                || aClass == double.class || aClass == Double.class) {
            return 8;
        } else if (aClass == boolean.class || aClass == Boolean.class
                || aClass == byte.class || aClass == Byte.class) {
            return 1;
        } else if (aClass == short.class || aClass == Short.class
                || aClass == char.class || aClass == Character.class) {
            return 2;
        } else if (aClass == String.class) {
            String value = (String) fieldValue;
            if (value != null) {
                return 40 + 2 * value.length();
            }
            return 40;
        } else if (aClass.isAssignableFrom(Map.class) || aClass.isAssignableFrom(List.class) || aClass.isAssignableFrom(Set.class)) {
            //如果是集合，则以缓存中的字段长度来计算
            IMultiCollection collection = (IMultiCollection) fieldValue;
            String memoryStr = MultiDataUtil.GSON.toJson(collection);
            return 40 + 2 * memoryStr.length();
        }
        return 0;
    }
}
