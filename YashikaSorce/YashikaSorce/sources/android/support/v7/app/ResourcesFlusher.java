package android.support.v7.app;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.LongSparseArray;
import java.lang.reflect.Field;
import java.util.Map;

/* loaded from: classes.dex */
class ResourcesFlusher {
    private static final String TAG = "ResourcesFlusher";
    private static Field sDrawableCacheField;
    private static boolean sDrawableCacheFieldFetched;
    private static Field sResourcesImplField;
    private static boolean sResourcesImplFieldFetched;
    private static Class sThemedResourceCacheClazz;
    private static boolean sThemedResourceCacheClazzFetched;
    private static Field sThemedResourceCache_mUnthemedEntriesField;
    private static boolean sThemedResourceCache_mUnthemedEntriesFieldFetched;

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void flush(@NonNull Resources resources) {
        if (Build.VERSION.SDK_INT >= 28) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            flushNougats(resources);
        } else if (Build.VERSION.SDK_INT >= 23) {
            flushMarshmallows(resources);
        } else if (Build.VERSION.SDK_INT >= 21) {
            flushLollipops(resources);
        }
    }

    @RequiresApi(21)
    private static void flushLollipops(@NonNull Resources resources) {
        Map map;
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }
        Field field = sDrawableCacheField;
        if (field != null) {
            try {
                map = (Map) field.get(resources);
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Could not retrieve value from Resources#mDrawableCache", e2);
                map = null;
            }
            if (map != null) {
                map.clear();
            }
        }
    }

    @RequiresApi(23)
    private static void flushMarshmallows(@NonNull Resources resources) {
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }
        Object obj = null;
        Field field = sDrawableCacheField;
        if (field != null) {
            try {
                obj = field.get(resources);
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "Could not retrieve value from Resources#mDrawableCache", e2);
            }
        }
        if (obj == null) {
            return;
        }
        flushThemedResourcesCache(obj);
    }

    @RequiresApi(24)
    private static void flushNougats(@NonNull Resources resources) {
        Object obj;
        if (!sResourcesImplFieldFetched) {
            try {
                sResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                sResourcesImplField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mResourcesImpl field", e);
            }
            sResourcesImplFieldFetched = true;
        }
        Field field = sResourcesImplField;
        if (field == null) {
            return;
        }
        Object obj2 = null;
        try {
            obj = field.get(resources);
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "Could not retrieve value from Resources#mResourcesImpl", e2);
            obj = null;
        }
        if (obj == null) {
            return;
        }
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = obj.getClass().getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e3) {
                Log.e(TAG, "Could not retrieve ResourcesImpl#mDrawableCache field", e3);
            }
            sDrawableCacheFieldFetched = true;
        }
        Field field2 = sDrawableCacheField;
        if (field2 != null) {
            try {
                obj2 = field2.get(obj);
            } catch (IllegalAccessException e4) {
                Log.e(TAG, "Could not retrieve value from ResourcesImpl#mDrawableCache", e4);
            }
        }
        if (obj2 != null) {
            flushThemedResourcesCache(obj2);
        }
    }

    @RequiresApi(16)
    private static void flushThemedResourcesCache(@NonNull Object obj) {
        LongSparseArray longSparseArray;
        if (!sThemedResourceCacheClazzFetched) {
            try {
                sThemedResourceCacheClazz = Class.forName("android.content.res.ThemedResourceCache");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Could not find ThemedResourceCache class", e);
            }
            sThemedResourceCacheClazzFetched = true;
        }
        Class cls = sThemedResourceCacheClazz;
        if (cls == null) {
            return;
        }
        if (!sThemedResourceCache_mUnthemedEntriesFieldFetched) {
            try {
                sThemedResourceCache_mUnthemedEntriesField = cls.getDeclaredField("mUnthemedEntries");
                sThemedResourceCache_mUnthemedEntriesField.setAccessible(true);
            } catch (NoSuchFieldException e2) {
                Log.e(TAG, "Could not retrieve ThemedResourceCache#mUnthemedEntries field", e2);
            }
            sThemedResourceCache_mUnthemedEntriesFieldFetched = true;
        }
        Field field = sThemedResourceCache_mUnthemedEntriesField;
        if (field == null) {
            return;
        }
        try {
            longSparseArray = (LongSparseArray) field.get(obj);
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "Could not retrieve value from ThemedResourceCache#mUnthemedEntries", e3);
            longSparseArray = null;
        }
        if (longSparseArray != null) {
            longSparseArray.clear();
        }
    }

    private ResourcesFlusher() {
    }
}
