package android.support.customtabs.browseractions;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.GuardedBy;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.content.FileProvider;
import android.support.v4.util.AtomicFile;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class BrowserServiceFileProvider extends FileProvider {
    private static final String AUTHORITY_SUFFIX = ".image_provider";
    private static final String CLIP_DATA_LABEL = "image_provider_uris";
    private static final String CONTENT_SCHEME = "content";
    private static final String FILE_EXTENSION = ".png";
    private static final String FILE_SUB_DIR = "image_provider";
    private static final String FILE_SUB_DIR_NAME = "image_provider_images/";
    private static final String LAST_CLEANUP_TIME_KEY = "last_cleanup_time";
    private static final String TAG = "BrowserServiceFileProvider";
    @GuardedBy("sLatchMapLock")
    private static Set<Uri> sFilesInSerialization = new HashSet();
    @GuardedBy("sLatchMapLock")
    private static Map<Uri, CountDownLatch> sUriLatchMap = new HashMap();
    private static Object sLatchMapLock = new Object();
    private static Object sFileCleanupLock = new Object();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class FileCleanupTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<Context> mContextRef;
        private static final long IMAGE_RETENTION_DURATION = TimeUnit.DAYS.toMillis(7);
        private static final long CLEANUP_REQUIRED_TIME_SPAN = TimeUnit.DAYS.toMillis(7);
        private static final long DELETION_FAILED_REATTEMPT_DURATION = TimeUnit.DAYS.toMillis(1);

        public FileCleanupTask(WeakReference<Context> weakReference) {
            this.mContextRef = weakReference;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(Void... voidArr) {
            long currentTimeMillis;
            Context context = this.mContextRef.get();
            if (context == null) {
                return null;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + BrowserServiceFileProvider.AUTHORITY_SUFFIX, 0);
            if (shouldCleanUp(sharedPreferences)) {
                synchronized (BrowserServiceFileProvider.sFileCleanupLock) {
                    File file = new File(context.getFilesDir(), BrowserServiceFileProvider.FILE_SUB_DIR);
                    if (file.exists()) {
                        File[] listFiles = file.listFiles();
                        long currentTimeMillis2 = System.currentTimeMillis() - IMAGE_RETENTION_DURATION;
                        boolean z = true;
                        for (File file2 : listFiles) {
                            if (isImageFile(file2) && file2.lastModified() < currentTimeMillis2 && !file2.delete()) {
                                Log.e(BrowserServiceFileProvider.TAG, "Fail to delete image: " + file2.getAbsoluteFile());
                                z = false;
                            }
                        }
                        if (z) {
                            currentTimeMillis = System.currentTimeMillis();
                        } else {
                            currentTimeMillis = (System.currentTimeMillis() - CLEANUP_REQUIRED_TIME_SPAN) + DELETION_FAILED_REATTEMPT_DURATION;
                        }
                        SharedPreferences.Editor edit = sharedPreferences.edit();
                        edit.putLong(BrowserServiceFileProvider.LAST_CLEANUP_TIME_KEY, currentTimeMillis);
                        edit.apply();
                        return null;
                    }
                    return null;
                }
            }
            return null;
        }

        private boolean isImageFile(File file) {
            return file.getName().endsWith("..png");
        }

        private boolean shouldCleanUp(SharedPreferences sharedPreferences) {
            return System.currentTimeMillis() > sharedPreferences.getLong(BrowserServiceFileProvider.LAST_CLEANUP_TIME_KEY, System.currentTimeMillis()) + CLEANUP_REQUIRED_TIME_SPAN;
        }
    }

    /* loaded from: classes.dex */
    private static class FileSaveTask extends AsyncTask<String, Void, Void> {
        private final Bitmap mBitmap;
        private final WeakReference<Context> mContextRef;
        private final Uri mFileUri;
        private final String mFilename;

        public FileSaveTask(Context context, String str, Bitmap bitmap, Uri uri) {
            this.mContextRef = new WeakReference<>(context);
            this.mFilename = str;
            this.mBitmap = bitmap;
            this.mFileUri = uri;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Void doInBackground(String... strArr) {
            saveFileIfNeededBlocking();
            return null;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Void r3) {
            synchronized (BrowserServiceFileProvider.sLatchMapLock) {
                if (BrowserServiceFileProvider.sUriLatchMap.containsKey(this.mFileUri)) {
                    ((CountDownLatch) BrowserServiceFileProvider.sUriLatchMap.get(this.mFileUri)).countDown();
                }
                BrowserServiceFileProvider.sFilesInSerialization.remove(this.mFileUri);
                BrowserServiceFileProvider.sUriLatchMap.remove(this.mFileUri);
            }
            new FileCleanupTask(this.mContextRef).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Void[0]);
        }

        private void saveFileBlocking(File file) {
            FileOutputStream fileOutputStream;
            if (Build.VERSION.SDK_INT >= 22) {
                AtomicFile atomicFile = new AtomicFile(file);
                try {
                    fileOutputStream = atomicFile.startWrite();
                    try {
                        this.mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        fileOutputStream.close();
                        atomicFile.finishWrite(fileOutputStream);
                    } catch (IOException e) {
                        e = e;
                        Log.e(BrowserServiceFileProvider.TAG, "Fail to save file", e);
                        atomicFile.failWrite(fileOutputStream);
                    }
                } catch (IOException e2) {
                    e = e2;
                    fileOutputStream = null;
                }
            } else {
                try {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                    this.mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream2);
                    fileOutputStream2.close();
                } catch (IOException e3) {
                    Log.e(BrowserServiceFileProvider.TAG, "Fail to save file", e3);
                }
            }
        }

        private void saveFileIfNeededBlocking() {
            if (this.mContextRef.get() == null) {
                return;
            }
            File file = new File(this.mContextRef.get().getFilesDir(), BrowserServiceFileProvider.FILE_SUB_DIR);
            synchronized (BrowserServiceFileProvider.sFileCleanupLock) {
                if (file.exists() || file.mkdir()) {
                    File file2 = new File(file, this.mFilename + BrowserServiceFileProvider.FILE_EXTENSION);
                    if (!file2.exists()) {
                        saveFileBlocking(file2);
                    }
                    file2.setLastModified(System.currentTimeMillis());
                }
            }
        }
    }

    @UiThread
    public static Uri generateUri(Context context, Bitmap bitmap, String str, int i) {
        boolean add;
        String str2 = str + "_" + Integer.toString(i);
        Uri generateUri = generateUri(context, str2);
        synchronized (sLatchMapLock) {
            add = sFilesInSerialization.add(generateUri);
        }
        if (add) {
            new FileSaveTask(context, str2, bitmap, generateUri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
        }
        return generateUri;
    }

    private static Uri generateUri(Context context, String str) {
        return new Uri.Builder().scheme(CONTENT_SCHEME).authority(context.getPackageName() + AUTHORITY_SUFFIX).path(FILE_SUB_DIR_NAME + str + FILE_EXTENSION).build();
    }

    public static void grantReadPermission(Intent intent, List<Uri> list, Context context) {
        if (list == null || list.size() == 0) {
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        intent.addFlags(1);
        ClipData newUri = ClipData.newUri(contentResolver, CLIP_DATA_LABEL, list.get(0));
        for (int i = 1; i < list.size(); i++) {
            newUri.addItem(new ClipData.Item(list.get(i)));
        }
        intent.setClipData(newUri);
    }

    @Override // android.support.v4.content.FileProvider, android.content.ContentProvider
    @WorkerThread
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        if (!blockUntilFileReady(uri)) {
            throw new FileNotFoundException("File open is interrupted");
        }
        return super.openFile(uri, str);
    }

    @Override // android.support.v4.content.FileProvider, android.content.ContentProvider
    @WorkerThread
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (blockUntilFileReady(uri)) {
            return super.query(uri, strArr, str, strArr2, str2);
        }
        return null;
    }

    private boolean blockUntilFileReady(Uri uri) {
        CountDownLatch countDownLatch;
        synchronized (sLatchMapLock) {
            if (sFilesInSerialization.contains(uri)) {
                if (sUriLatchMap.containsKey(uri)) {
                    countDownLatch = sUriLatchMap.get(uri);
                } else {
                    countDownLatch = new CountDownLatch(1);
                    sUriLatchMap.put(uri, countDownLatch);
                }
                try {
                    countDownLatch.await();
                    return true;
                } catch (InterruptedException unused) {
                    Log.e(TAG, "Interrupt waiting for file: " + uri.toString());
                    return false;
                }
            }
            return true;
        }
    }
}
