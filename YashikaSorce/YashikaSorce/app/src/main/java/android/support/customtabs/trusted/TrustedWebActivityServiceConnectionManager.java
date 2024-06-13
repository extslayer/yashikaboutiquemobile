package android.support.customtabs.trusted;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.TransactionTooLargeException;
import android.support.annotation.Nullable;
import android.support.customtabs.trusted.ITrustedWebActivityService;
import android.util.Log;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public class TrustedWebActivityServiceConnectionManager {
    private static final String PREFS_FILE = "TrustedWebActivityVerifiedPackages";
    private static final String TAG = "TWAConnectionManager";
    private static AtomicReference<SharedPreferences> sSharedPreferences = new AtomicReference<>();
    private Map<Uri, Connection> mConnections = new HashMap();
    private final Context mContext;

    /* loaded from: classes.dex */
    public interface ExecutionCallback {
        void onConnected(@Nullable TrustedWebActivityServiceWrapper trustedWebActivityServiceWrapper) throws RemoteException;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface WrappedCallback {
        void onConnected(@Nullable TrustedWebActivityServiceWrapper trustedWebActivityServiceWrapper);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class Connection implements ServiceConnection {
        private List<WrappedCallback> mCallbacks = new LinkedList();
        private final Uri mScope;
        private TrustedWebActivityServiceWrapper mService;

        public Connection(Uri uri) {
            this.mScope = uri;
        }

        public TrustedWebActivityServiceWrapper getService() {
            return this.mService;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            this.mService = new TrustedWebActivityServiceWrapper(ITrustedWebActivityService.Stub.asInterface(iBinder), componentName);
            for (WrappedCallback wrappedCallback : this.mCallbacks) {
                wrappedCallback.onConnected(this.mService);
            }
            this.mCallbacks.clear();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            this.mService = null;
            TrustedWebActivityServiceConnectionManager.this.mConnections.remove(this.mScope);
        }

        public void addCallback(WrappedCallback wrappedCallback) {
            TrustedWebActivityServiceWrapper trustedWebActivityServiceWrapper = this.mService;
            if (trustedWebActivityServiceWrapper == null) {
                this.mCallbacks.add(wrappedCallback);
            } else {
                wrappedCallback.onConnected(trustedWebActivityServiceWrapper);
            }
        }
    }

    public static Set<String> getVerifiedPackages(Context context, String str) {
        StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        try {
            HashSet hashSet = null;
            if (sSharedPreferences.get() == null) {
                sSharedPreferences.compareAndSet(null, context.getSharedPreferences(PREFS_FILE, 0));
            }
            if (str != null) {
                hashSet = new HashSet(sSharedPreferences.get().getStringSet(str, Collections.emptySet()));
            }
            return hashSet;
        } finally {
            StrictMode.setThreadPolicy(allowThreadDiskReads);
        }
    }

    public TrustedWebActivityServiceConnectionManager(Context context) {
        this.mContext = context.getApplicationContext();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() { // from class: android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager.1
            @Override // java.lang.Runnable
            public void run() {
                TrustedWebActivityServiceConnectionManager.getVerifiedPackages(TrustedWebActivityServiceConnectionManager.this.mContext, null);
            }
        });
    }

    private static WrappedCallback wrapCallback(final ExecutionCallback executionCallback) {
        return new WrappedCallback() { // from class: android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager.2
            @Override // android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager.WrappedCallback
            public void onConnected(@Nullable final TrustedWebActivityServiceWrapper trustedWebActivityServiceWrapper) {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() { // from class: android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager.2.1
                    @Override // java.lang.Runnable
                    public void run() {
                        try {
                            ExecutionCallback.this.onConnected(trustedWebActivityServiceWrapper);
                        } catch (TransactionTooLargeException e) {
                            Log.w(TrustedWebActivityServiceConnectionManager.TAG, "TransactionTooLargeException from TrustedWebActivityService, possibly due to large size of small icon.", e);
                        } catch (RemoteException e2) {
                            e = e2;
                            Log.w(TrustedWebActivityServiceConnectionManager.TAG, "Exception while trying to use TrustedWebActivityService.", e);
                        } catch (RuntimeException e3) {
                            e = e3;
                            Log.w(TrustedWebActivityServiceConnectionManager.TAG, "Exception while trying to use TrustedWebActivityService.", e);
                        }
                    }
                });
            }
        };
    }

    /* JADX WARN: Type inference failed for: r10v5, types: [android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager$3] */
    @SuppressLint({"StaticFieldLeak"})
    public boolean execute(final Uri uri, String str, ExecutionCallback executionCallback) {
        final WrappedCallback wrapCallback = wrapCallback(executionCallback);
        Connection connection = this.mConnections.get(uri);
        if (connection != null) {
            connection.addCallback(wrapCallback);
            return true;
        }
        final Intent createServiceIntent = createServiceIntent(this.mContext, uri, str, true);
        if (createServiceIntent == null) {
            return false;
        }
        final Connection connection2 = new Connection(uri);
        connection2.addCallback(wrapCallback);
        new AsyncTask<Void, Void, Connection>() { // from class: android.support.customtabs.trusted.TrustedWebActivityServiceConnectionManager.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Connection doInBackground(Void... voidArr) {
                try {
                    if (!TrustedWebActivityServiceConnectionManager.this.mContext.bindService(createServiceIntent, connection2, 1)) {
                        TrustedWebActivityServiceConnectionManager.this.mContext.unbindService(connection2);
                        return null;
                    }
                    return connection2;
                } catch (SecurityException e) {
                    Log.w(TrustedWebActivityServiceConnectionManager.TAG, "SecurityException while binding.", e);
                    return null;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Connection connection3) {
                if (connection3 != null) {
                    TrustedWebActivityServiceConnectionManager.this.mConnections.put(uri, connection3);
                } else {
                    wrapCallback.onConnected(null);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        return true;
    }

    public boolean serviceExistsForScope(Uri uri, String str) {
        return (this.mConnections.get(uri) == null && createServiceIntent(this.mContext, uri, str, false) == null) ? false : true;
    }

    void unbindAllConnections() {
        for (Connection connection : this.mConnections.values()) {
            this.mContext.unbindService(connection);
        }
        this.mConnections.clear();
    }

    @Nullable
    private Intent createServiceIntent(Context context, Uri uri, String str, boolean z) {
        String str2;
        Set<String> verifiedPackages = getVerifiedPackages(context, str);
        if (verifiedPackages == null || verifiedPackages.size() == 0) {
            return null;
        }
        Intent intent = new Intent();
        intent.setData(uri);
        intent.setAction("android.intent.action.VIEW");
        Iterator<ResolveInfo> it = context.getPackageManager().queryIntentActivities(intent, 65536).iterator();
        while (true) {
            if (!it.hasNext()) {
                str2 = null;
                break;
            }
            str2 = it.next().activityInfo.packageName;
            if (verifiedPackages.contains(str2)) {
                break;
            }
        }
        if (str2 == null) {
            if (z) {
                Log.w(TAG, "No TWA candidates for " + str + " have been registered.");
            }
            return null;
        }
        Intent intent2 = new Intent();
        intent2.setPackage(str2);
        intent2.setAction(TrustedWebActivityService.INTENT_ACTION);
        ResolveInfo resolveService = context.getPackageManager().resolveService(intent2, 131072);
        if (resolveService == null) {
            if (z) {
                Log.w(TAG, "Could not find TWAService for " + str2);
            }
            return null;
        }
        if (z) {
            Log.i(TAG, "Found " + resolveService.serviceInfo.name + " to handle request for " + str);
        }
        Intent intent3 = new Intent();
        intent3.setComponent(new ComponentName(str2, resolveService.serviceInfo.name));
        return intent3;
    }

    public static void registerClient(Context context, String str, String str2) {
        Set<String> verifiedPackages = getVerifiedPackages(context, str);
        verifiedPackages.add(str2);
        SharedPreferences.Editor edit = sSharedPreferences.get().edit();
        edit.putStringSet(str, verifiedPackages);
        edit.apply();
    }
}
