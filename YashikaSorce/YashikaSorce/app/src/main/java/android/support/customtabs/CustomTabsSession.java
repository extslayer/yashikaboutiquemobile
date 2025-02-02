package android.support.customtabs;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.customtabs.CustomTabsSessionToken;
import android.widget.RemoteViews;
import java.util.List;

/* loaded from: classes.dex */
public final class CustomTabsSession {
    private static final String TAG = "CustomTabsSession";
    private final ICustomTabsCallback mCallback;
    private final ComponentName mComponentName;
    private final PendingIntent mId;
    private final Object mLock = new Object();
    private final ICustomTabsService mService;

    @VisibleForTesting
    @NonNull
    public static CustomTabsSession createMockSessionForTesting(@NonNull ComponentName componentName) {
        return new CustomTabsSession(null, new CustomTabsSessionToken.MockCallback(), componentName, null);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CustomTabsSession(ICustomTabsService iCustomTabsService, ICustomTabsCallback iCustomTabsCallback, ComponentName componentName, @Nullable PendingIntent pendingIntent) {
        this.mService = iCustomTabsService;
        this.mCallback = iCustomTabsCallback;
        this.mComponentName = componentName;
        this.mId = pendingIntent;
    }

    public boolean mayLaunchUrl(Uri uri, Bundle bundle, List<Bundle> list) {
        addIdToBundle(bundle);
        try {
            return this.mService.mayLaunchUrl(this.mCallback, uri, bundle, list);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean setActionButton(@NonNull Bitmap bitmap, @NonNull String str) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CustomTabsIntent.KEY_ICON, bitmap);
        bundle.putString(CustomTabsIntent.KEY_DESCRIPTION, str);
        Bundle bundle2 = new Bundle();
        bundle2.putBundle(CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE, bundle);
        addIdToBundle(bundle);
        try {
            return this.mService.updateVisuals(this.mCallback, bundle2);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean setSecondaryToolbarViews(@Nullable RemoteViews remoteViews, @Nullable int[] iArr, @Nullable PendingIntent pendingIntent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CustomTabsIntent.EXTRA_REMOTEVIEWS, remoteViews);
        bundle.putIntArray(CustomTabsIntent.EXTRA_REMOTEVIEWS_VIEW_IDS, iArr);
        bundle.putParcelable(CustomTabsIntent.EXTRA_REMOTEVIEWS_PENDINGINTENT, pendingIntent);
        addIdToBundle(bundle);
        try {
            return this.mService.updateVisuals(this.mCallback, bundle);
        } catch (RemoteException unused) {
            return false;
        }
    }

    @Deprecated
    public boolean setToolbarItem(int i, @NonNull Bitmap bitmap, @NonNull String str) {
        Bundle bundle = new Bundle();
        bundle.putInt(CustomTabsIntent.KEY_ID, i);
        bundle.putParcelable(CustomTabsIntent.KEY_ICON, bitmap);
        bundle.putString(CustomTabsIntent.KEY_DESCRIPTION, str);
        Bundle bundle2 = new Bundle();
        bundle2.putBundle(CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE, bundle);
        addIdToBundle(bundle2);
        try {
            return this.mService.updateVisuals(this.mCallback, bundle2);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean requestPostMessageChannel(Uri uri) {
        Bundle bundle = new Bundle();
        addIdToBundle(bundle);
        try {
            return this.mService.requestPostMessageChannelWithExtras(this.mCallback, uri, bundle);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public int postMessage(String str, Bundle bundle) {
        int postMessage;
        addIdToBundle(bundle);
        synchronized (this.mLock) {
            try {
                try {
                    postMessage = this.mService.postMessage(this.mCallback, str, bundle);
                } catch (RemoteException unused) {
                    return -2;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return postMessage;
    }

    public boolean validateRelationship(int i, @NonNull Uri uri, @Nullable Bundle bundle) {
        if (i >= 1 && i <= 2) {
            if (bundle == null) {
                bundle = new Bundle();
            }
            addIdToBundle(bundle);
            try {
                return this.mService.validateRelationship(this.mCallback, i, uri, bundle);
            } catch (RemoteException unused) {
            }
        }
        return false;
    }

    public boolean receiveFile(@NonNull Uri uri, int i, @Nullable Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        addIdToBundle(bundle);
        try {
            return this.mService.receiveFile(this.mCallback, uri, i, bundle);
        } catch (RemoteException unused) {
            return false;
        }
    }

    private void addIdToBundle(Bundle bundle) {
        PendingIntent pendingIntent = this.mId;
        if (pendingIntent != null) {
            bundle.putParcelable(CustomTabsIntent.EXTRA_SESSION_ID, pendingIntent);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public IBinder getBinder() {
        return this.mCallback.asBinder();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public PendingIntent getId() {
        return this.mId;
    }

    /* loaded from: classes.dex */
    public static class PendingSession {
        private final CustomTabsCallback mCallback;
        private final PendingIntent mId;

        /* JADX INFO: Access modifiers changed from: package-private */
        public PendingSession(CustomTabsCallback customTabsCallback, PendingIntent pendingIntent) {
            this.mCallback = customTabsCallback;
            this.mId = pendingIntent;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public PendingIntent getId() {
            return this.mId;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public CustomTabsCallback getCallback() {
            return this.mCallback;
        }
    }
}
