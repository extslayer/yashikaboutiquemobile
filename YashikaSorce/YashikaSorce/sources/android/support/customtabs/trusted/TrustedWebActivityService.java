package android.support.customtabs.trusted;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.service.notification.StatusBarNotification;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.customtabs.trusted.ITrustedWebActivityService;
import android.support.customtabs.trusted.TrustedWebActivityServiceWrapper;
import android.support.v4.app.NotificationManagerCompat;
import java.util.Arrays;
import java.util.Locale;

/* loaded from: classes.dex */
public class TrustedWebActivityService extends Service {
    public static final String INTENT_ACTION = "android.support.customtabs.trusted.TRUSTED_WEB_ACTIVITY_SERVICE";
    static final String KEY_SMALL_ICON_BITMAP = "android.support.customtabs.trusted.SMALL_ICON_BITMAP";
    public static final int NO_ID = -1;
    private static final String PREFS_FILE = "TrustedWebActivityVerifiedProvider";
    private static final String PREFS_VERIFIED_PROVIDER = "Provider";
    public static final String SMALL_ICON_META_DATA_NAME = "android.support.customtabs.trusted.SMALL_ICON";
    private NotificationManager mNotificationManager;
    public int mVerifiedUid = -1;
    private final ITrustedWebActivityService.Stub mBinder = new ITrustedWebActivityService.Stub() { // from class: android.support.customtabs.trusted.TrustedWebActivityService.1
        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public Bundle areNotificationsEnabled(Bundle bundle) {
            checkCaller();
            return new TrustedWebActivityServiceWrapper.ResultArgs(TrustedWebActivityService.this.areNotificationsEnabled(TrustedWebActivityServiceWrapper.NotificationsEnabledArgs.fromBundle(bundle).channelName)).toBundle();
        }

        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public Bundle notifyNotificationWithChannel(Bundle bundle) {
            checkCaller();
            TrustedWebActivityServiceWrapper.NotifyNotificationArgs fromBundle = TrustedWebActivityServiceWrapper.NotifyNotificationArgs.fromBundle(bundle);
            return new TrustedWebActivityServiceWrapper.ResultArgs(TrustedWebActivityService.this.notifyNotificationWithChannel(fromBundle.platformTag, fromBundle.platformId, fromBundle.notification, fromBundle.channelName)).toBundle();
        }

        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public void cancelNotification(Bundle bundle) {
            checkCaller();
            TrustedWebActivityServiceWrapper.CancelNotificationArgs fromBundle = TrustedWebActivityServiceWrapper.CancelNotificationArgs.fromBundle(bundle);
            TrustedWebActivityService.this.cancelNotification(fromBundle.platformTag, fromBundle.platformId);
        }

        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public Bundle getActiveNotifications() {
            checkCaller();
            return new TrustedWebActivityServiceWrapper.ActiveNotificationsArgs(TrustedWebActivityService.this.getActiveNotifications()).toBundle();
        }

        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public int getSmallIconId() {
            checkCaller();
            return TrustedWebActivityService.this.getSmallIconId();
        }

        @Override // android.support.customtabs.trusted.ITrustedWebActivityService
        public Bundle getSmallIconBitmap() {
            checkCaller();
            return TrustedWebActivityService.this.getSmallIconBitmap();
        }

        private void checkCaller() {
            if (TrustedWebActivityService.this.mVerifiedUid == -1) {
                String[] packagesForUid = TrustedWebActivityService.this.getPackageManager().getPackagesForUid(getCallingUid());
                StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
                try {
                    if (Arrays.asList(packagesForUid).contains(TrustedWebActivityService.getPreferences(TrustedWebActivityService.this).getString(TrustedWebActivityService.PREFS_VERIFIED_PROVIDER, null))) {
                        TrustedWebActivityService.this.mVerifiedUid = getCallingUid();
                        return;
                    }
                } finally {
                    StrictMode.setThreadPolicy(allowThreadDiskReads);
                }
            }
            if (TrustedWebActivityService.this.mVerifiedUid != getCallingUid()) {
                throw new SecurityException("Caller is not verified as Trusted Web Activity provider.");
            }
        }
    };

    @Override // android.app.Service
    @CallSuper
    public void onCreate() {
        super.onCreate();
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
    }

    protected boolean areNotificationsEnabled(String str) {
        ensureOnCreateCalled();
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT < 26) {
                return true;
            }
            NotificationChannel notificationChannel = this.mNotificationManager.getNotificationChannel(channelNameToId(str));
            return notificationChannel == null || notificationChannel.getImportance() != 0;
        }
        return false;
    }

    protected boolean notifyNotificationWithChannel(String str, int i, Notification notification, String str2) {
        ensureOnCreateCalled();
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= 26) {
                String channelNameToId = channelNameToId(str2);
                this.mNotificationManager.createNotificationChannel(new NotificationChannel(channelNameToId, str2, 3));
                if (this.mNotificationManager.getNotificationChannel(channelNameToId).getImportance() == 0) {
                    return false;
                }
                Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(this, notification);
                recoverBuilder.setChannelId(channelNameToId);
                notification = recoverBuilder.build();
            }
            this.mNotificationManager.notify(str, i, notification);
            return true;
        }
        return false;
    }

    protected void cancelNotification(String str, int i) {
        ensureOnCreateCalled();
        this.mNotificationManager.cancel(str, i);
    }

    @TargetApi(23)
    protected StatusBarNotification[] getActiveNotifications() {
        ensureOnCreateCalled();
        if (Build.VERSION.SDK_INT >= 23) {
            return this.mNotificationManager.getActiveNotifications();
        }
        throw new IllegalStateException("getActiveNotifications cannot be called pre-M.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Bundle getSmallIconBitmap() {
        int smallIconId = getSmallIconId();
        Bundle bundle = new Bundle();
        if (smallIconId == -1) {
            return bundle;
        }
        bundle.putParcelable(KEY_SMALL_ICON_BITMAP, BitmapFactory.decodeResource(getResources(), smallIconId));
        return bundle;
    }

    protected int getSmallIconId() {
        try {
            ServiceInfo serviceInfo = getPackageManager().getServiceInfo(new ComponentName(this, getClass()), 128);
            if (serviceInfo.metaData == null) {
                return -1;
            }
            return serviceInfo.metaData.getInt(SMALL_ICON_META_DATA_NAME, -1);
        } catch (PackageManager.NameNotFoundException unused) {
            return -1;
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    @Override // android.app.Service
    public final boolean onUnbind(Intent intent) {
        this.mVerifiedUid = -1;
        return super.onUnbind(intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, 0);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [android.support.customtabs.trusted.TrustedWebActivityService$2] */
    public static final void setVerifiedProvider(final Context context, @Nullable final String str) {
        str = (str == null || str.isEmpty()) ? null : null;
        new AsyncTask<Void, Void, Void>() { // from class: android.support.customtabs.trusted.TrustedWebActivityService.2
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                SharedPreferences.Editor edit = TrustedWebActivityService.getPreferences(context).edit();
                edit.putString(TrustedWebActivityService.PREFS_VERIFIED_PROVIDER, str);
                edit.apply();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public static final void setVerifiedProviderSynchronouslyForTesting(Context context, @Nullable String str) {
        str = (str == null || str.isEmpty()) ? null : null;
        StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        try {
            SharedPreferences.Editor edit = getPreferences(context).edit();
            edit.putString(PREFS_VERIFIED_PROVIDER, str);
            edit.apply();
        } finally {
            StrictMode.setThreadPolicy(allowThreadDiskReads);
        }
    }

    private static String channelNameToId(String str) {
        return str.toLowerCase(Locale.ROOT).replace(' ', '_') + "_channel_id";
    }

    private void ensureOnCreateCalled() {
        if (this.mNotificationManager == null) {
            throw new IllegalStateException("TrustedWebActivityService has not been properly initialized. Did onCreate() call super.onCreate()?");
        }
    }
}
