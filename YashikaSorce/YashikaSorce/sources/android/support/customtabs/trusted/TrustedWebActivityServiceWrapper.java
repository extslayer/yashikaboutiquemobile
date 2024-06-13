package android.support.customtabs.trusted;

import android.app.Notification;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;

/* loaded from: classes.dex */
public class TrustedWebActivityServiceWrapper {
    private static final String KEY_ACTIVE_NOTIFICATIONS = "android.support.customtabs.trusted.ACTIVE_NOTIFICATIONS";
    private static final String KEY_CHANNEL_NAME = "android.support.customtabs.trusted.CHANNEL_NAME";
    private static final String KEY_NOTIFICATION = "android.support.customtabs.trusted.NOTIFICATION";
    private static final String KEY_NOTIFICATION_SUCCESS = "android.support.customtabs.trusted.NOTIFICATION_SUCCESS";
    private static final String KEY_PLATFORM_ID = "android.support.customtabs.trusted.PLATFORM_ID";
    private static final String KEY_PLATFORM_TAG = "android.support.customtabs.trusted.PLATFORM_TAG";
    private final ComponentName mComponentName;
    private final ITrustedWebActivityService mService;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TrustedWebActivityServiceWrapper(ITrustedWebActivityService iTrustedWebActivityService, ComponentName componentName) {
        this.mService = iTrustedWebActivityService;
        this.mComponentName = componentName;
    }

    public boolean areNotificationsEnabled(String str) throws RemoteException {
        return ResultArgs.fromBundle(this.mService.areNotificationsEnabled(new NotificationsEnabledArgs(str).toBundle())).success;
    }

    public boolean notify(String str, int i, Notification notification, String str2) throws RemoteException {
        return ResultArgs.fromBundle(this.mService.notifyNotificationWithChannel(new NotifyNotificationArgs(str, i, notification, str2).toBundle())).success;
    }

    public void cancel(String str, int i) throws RemoteException {
        this.mService.cancelNotification(new CancelNotificationArgs(str, i).toBundle());
    }

    public StatusBarNotification[] getActiveNotifications() throws RemoteException {
        return ActiveNotificationsArgs.fromBundle(this.mService.getActiveNotifications()).notifications;
    }

    public int getSmallIconId() throws RemoteException {
        return this.mService.getSmallIconId();
    }

    @Nullable
    public Bitmap getSmallIconBitmap() throws RemoteException {
        return (Bitmap) this.mService.getSmallIconBitmap().getParcelable("android.support.customtabs.trusted.SMALL_ICON_BITMAP");
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    /* loaded from: classes.dex */
    static class NotifyNotificationArgs {
        public final String channelName;
        public final Notification notification;
        public final int platformId;
        public final String platformTag;

        public NotifyNotificationArgs(String str, int i, Notification notification, String str2) {
            this.platformTag = str;
            this.platformId = i;
            this.notification = notification;
            this.channelName = str2;
        }

        public static NotifyNotificationArgs fromBundle(Bundle bundle) {
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG);
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID);
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_NOTIFICATION);
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME);
            return new NotifyNotificationArgs(bundle.getString(TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG), bundle.getInt(TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID), (Notification) bundle.getParcelable(TrustedWebActivityServiceWrapper.KEY_NOTIFICATION), bundle.getString(TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME));
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putString(TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG, this.platformTag);
            bundle.putInt(TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID, this.platformId);
            bundle.putParcelable(TrustedWebActivityServiceWrapper.KEY_NOTIFICATION, this.notification);
            bundle.putString(TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME, this.channelName);
            return bundle;
        }
    }

    /* loaded from: classes.dex */
    static class CancelNotificationArgs {
        public final int platformId;
        public final String platformTag;

        public CancelNotificationArgs(String str, int i) {
            this.platformTag = str;
            this.platformId = i;
        }

        public static CancelNotificationArgs fromBundle(Bundle bundle) {
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG);
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID);
            return new CancelNotificationArgs(bundle.getString(TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG), bundle.getInt(TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID));
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putString(TrustedWebActivityServiceWrapper.KEY_PLATFORM_TAG, this.platformTag);
            bundle.putInt(TrustedWebActivityServiceWrapper.KEY_PLATFORM_ID, this.platformId);
            return bundle;
        }
    }

    /* loaded from: classes.dex */
    static class ResultArgs {
        public final boolean success;

        public ResultArgs(boolean z) {
            this.success = z;
        }

        public static ResultArgs fromBundle(Bundle bundle) {
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_NOTIFICATION_SUCCESS);
            return new ResultArgs(bundle.getBoolean(TrustedWebActivityServiceWrapper.KEY_NOTIFICATION_SUCCESS));
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TrustedWebActivityServiceWrapper.KEY_NOTIFICATION_SUCCESS, this.success);
            return bundle;
        }
    }

    /* loaded from: classes.dex */
    static class ActiveNotificationsArgs {
        public final StatusBarNotification[] notifications;

        public ActiveNotificationsArgs(StatusBarNotification[] statusBarNotificationArr) {
            this.notifications = statusBarNotificationArr;
        }

        public static ActiveNotificationsArgs fromBundle(Bundle bundle) {
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_ACTIVE_NOTIFICATIONS);
            return new ActiveNotificationsArgs((StatusBarNotification[]) bundle.getParcelableArray(TrustedWebActivityServiceWrapper.KEY_ACTIVE_NOTIFICATIONS));
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putParcelableArray(TrustedWebActivityServiceWrapper.KEY_ACTIVE_NOTIFICATIONS, this.notifications);
            return bundle;
        }
    }

    /* loaded from: classes.dex */
    static class NotificationsEnabledArgs {
        public final String channelName;

        public NotificationsEnabledArgs(String str) {
            this.channelName = str;
        }

        public static NotificationsEnabledArgs fromBundle(Bundle bundle) {
            TrustedWebActivityServiceWrapper.ensureBundleContains(bundle, TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME);
            return new NotificationsEnabledArgs(bundle.getString(TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME));
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putString(TrustedWebActivityServiceWrapper.KEY_CHANNEL_NAME, this.channelName);
            return bundle;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void ensureBundleContains(Bundle bundle, String str) {
        if (bundle.containsKey(str)) {
            return;
        }
        throw new IllegalArgumentException("Bundle must contain " + str);
    }
}
