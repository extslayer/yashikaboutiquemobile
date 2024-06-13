package android.support.customtabs.trusted;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.TrustedWebUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/* loaded from: classes.dex */
public class ManageDataLauncherActivity extends AppCompatActivity {
    private static final String METADATA_MANAGE_SPACE_URL = "android.support.customtabs.trusted.MANAGE_SPACE_URL";
    private static final String TAG = "ManageDataLauncher";
    private final CustomTabsServiceConnection mServiceConnection = new CustomTabsServiceConnection() { // from class: android.support.customtabs.trusted.ManageDataLauncherActivity.1
        private CustomTabsCallback mCustomTabsCallback = new CustomTabsCallback() { // from class: android.support.customtabs.trusted.ManageDataLauncherActivity.1.1
            @Override // android.support.customtabs.CustomTabsCallback
            public void onRelationshipValidationResult(int i, Uri uri, boolean z, Bundle bundle) {
                if (!z) {
                    ManageDataLauncherActivity manageDataLauncherActivity = ManageDataLauncherActivity.this;
                    manageDataLauncherActivity.onError(new RuntimeException("Failed to validate origin " + uri));
                    ManageDataLauncherActivity.this.finish();
                    return;
                }
                try {
                    TrustedWebUtils.launchBrowserSiteSettings(ManageDataLauncherActivity.this, AnonymousClass1.this.mSession, uri);
                } catch (RuntimeException e) {
                    ManageDataLauncherActivity.this.onError(e);
                    ManageDataLauncherActivity.this.finish();
                }
            }
        };
        private CustomTabsSession mSession;

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }

        @Override // android.support.customtabs.CustomTabsServiceConnection
        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
            Uri urlForManagingSpace = ManageDataLauncherActivity.this.getUrlForManagingSpace();
            if (urlForManagingSpace == null) {
                ManageDataLauncherActivity.this.finish();
                return;
            }
            this.mSession = customTabsClient.newSession(this.mCustomTabsCallback);
            if (this.mSession == null) {
                ManageDataLauncherActivity.this.onError(new RuntimeException("Failed to create CustomTabsSession"));
                ManageDataLauncherActivity.this.finish();
                return;
            }
            customTabsClient.warmup(0L);
            this.mSession.validateRelationship(2, urlForManagingSpace, null);
        }
    };

    @Nullable
    protected Uri getUrlForManagingSpace() {
        try {
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 128);
            if (activityInfo.metaData == null || !activityInfo.metaData.containsKey(METADATA_MANAGE_SPACE_URL)) {
                return null;
            }
            Uri parse = Uri.parse(activityInfo.metaData.getString(METADATA_MANAGE_SPACE_URL));
            Log.d(TAG, "Using clean-up URL from Manifest (" + parse + ").");
            return parse;
        } catch (PackageManager.NameNotFoundException e) {
            onError(new RuntimeException(e));
            return null;
        }
    }

    @Nullable
    protected View createLoadingView() {
        return new ProgressBar(this);
    }

    protected void onError(RuntimeException runtimeException) {
        throw runtimeException;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        String packageName = CustomTabsClient.getPackageName(this, TrustedWebUtils.SUPPORTED_CHROME_PACKAGES, false);
        if (packageName == null) {
            onError(new RuntimeException("No valid build of Chrome found"));
            finish();
            return;
        }
        View createLoadingView = createLoadingView();
        if (createLoadingView != null) {
            setContentView(createLoadingView);
        }
        CustomTabsClient.bindCustomTabsService(this, packageName, this.mServiceConnection);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        unbindService(this.mServiceConnection);
        finish();
    }
}
