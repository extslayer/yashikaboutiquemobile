package android.support.customtabs.trusted;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.TrustedWebUtils;
import android.support.customtabs.trusted.TwaProviderPicker;
import android.support.customtabs.trusted.splashscreens.SplashScreenStrategy;
import android.util.Log;

/* loaded from: classes.dex */
public class TwaLauncher {
    private static final int DEFAULT_SESSION_ID = 96375;
    private static final String TAG = "TwaLauncher";
    private final Context mContext;
    private boolean mDestroyed;
    private final int mLaunchMode;
    @Nullable
    private Runnable mOnSessionCreatedRunnable;
    @Nullable
    private final String mProviderPackage;
    @Nullable
    private TwaCustomTabsServiceConnection mServiceConnection;
    @Nullable
    private CustomTabsSession mSession;
    private final int mSessionId;

    public TwaLauncher(Context context) {
        this(context, null);
    }

    public TwaLauncher(Context context, @Nullable String str) {
        this(context, str, DEFAULT_SESSION_ID);
    }

    public TwaLauncher(Context context, @Nullable String str, int i) {
        this.mContext = context;
        this.mSessionId = i;
        if (str == null) {
            TwaProviderPicker.Action pickProvider = TwaProviderPicker.pickProvider(context.getPackageManager());
            this.mProviderPackage = pickProvider.provider;
            this.mLaunchMode = pickProvider.launchMode;
            return;
        }
        this.mProviderPackage = str;
        this.mLaunchMode = 0;
    }

    public void launch(Uri uri) {
        launch(new TrustedWebActivityBuilder(this.mContext, uri), null, null);
    }

    public void launch(TrustedWebActivityBuilder trustedWebActivityBuilder, @Nullable SplashScreenStrategy splashScreenStrategy, @Nullable Runnable runnable) {
        if (this.mDestroyed) {
            throw new IllegalStateException("TwaLauncher already destroyed");
        }
        if (this.mLaunchMode == 0) {
            launchTwa(trustedWebActivityBuilder, splashScreenStrategy, runnable);
        } else {
            launchCct(trustedWebActivityBuilder, runnable);
        }
    }

    private void launchCct(TrustedWebActivityBuilder trustedWebActivityBuilder, @Nullable Runnable runnable) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        Integer statusBarColor = trustedWebActivityBuilder.getStatusBarColor();
        if (statusBarColor != null) {
            builder.setToolbarColor(statusBarColor.intValue());
        }
        CustomTabsIntent build = builder.build();
        if (this.mProviderPackage != null) {
            build.intent.setPackage(this.mProviderPackage);
        }
        build.launchUrl(this.mContext, trustedWebActivityBuilder.getUrl());
        if (runnable != null) {
            runnable.run();
        }
    }

    private void launchTwa(final TrustedWebActivityBuilder trustedWebActivityBuilder, @Nullable final SplashScreenStrategy splashScreenStrategy, @Nullable final Runnable runnable) {
        Integer statusBarColor = trustedWebActivityBuilder.getStatusBarColor();
        if (splashScreenStrategy != null) {
            splashScreenStrategy.onTwaLaunchInitiated(this.mProviderPackage, statusBarColor);
        }
        Runnable runnable2 = new Runnable() { // from class: android.support.customtabs.trusted.-$$Lambda$TwaLauncher$0c_zkUHgDllmcAlOM3gechJInp0
            @Override // java.lang.Runnable
            public final void run() {
                TwaLauncher.this.lambda$launchTwa$0$TwaLauncher(trustedWebActivityBuilder, splashScreenStrategy, runnable);
            }
        };
        if (this.mSession != null) {
            runnable2.run();
            return;
        }
        this.mOnSessionCreatedRunnable = runnable2;
        if (this.mServiceConnection == null) {
            this.mServiceConnection = new TwaCustomTabsServiceConnection();
        }
        CustomTabsClient.bindCustomTabsService(this.mContext, this.mProviderPackage, this.mServiceConnection);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: launchWhenSessionEstablished */
    public void lambda$launchTwa$0$TwaLauncher(final TrustedWebActivityBuilder trustedWebActivityBuilder, @Nullable SplashScreenStrategy splashScreenStrategy, @Nullable final Runnable runnable) {
        CustomTabsSession customTabsSession = this.mSession;
        if (customTabsSession == null) {
            throw new IllegalStateException("mSession is null in launchWhenSessionEstablished");
        }
        if (splashScreenStrategy != null) {
            splashScreenStrategy.configureTwaBuilder(trustedWebActivityBuilder, customTabsSession, new Runnable() { // from class: android.support.customtabs.trusted.-$$Lambda$TwaLauncher$p8IaMFtGyVmHx_RSJfDTd6DUy1Q
                @Override // java.lang.Runnable
                public final void run() {
                    TwaLauncher.this.lambda$launchWhenSessionEstablished$1$TwaLauncher(trustedWebActivityBuilder, runnable);
                }
            });
        } else {
            lambda$launchWhenSessionEstablished$1$TwaLauncher(trustedWebActivityBuilder, runnable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: launchWhenSplashScreenReady */
    public void lambda$launchWhenSessionEstablished$1$TwaLauncher(TrustedWebActivityBuilder trustedWebActivityBuilder, @Nullable Runnable runnable) {
        Log.d(TAG, "Launching Trusted Web Activity.");
        trustedWebActivityBuilder.launchActivity(this.mSession);
        TrustedWebActivityService.setVerifiedProvider(this.mContext, this.mProviderPackage);
        if (runnable != null) {
            runnable.run();
        }
    }

    public void destroy() {
        TwaCustomTabsServiceConnection twaCustomTabsServiceConnection = this.mServiceConnection;
        if (twaCustomTabsServiceConnection != null) {
            this.mContext.unbindService(twaCustomTabsServiceConnection);
        }
        this.mDestroyed = true;
    }

    @Nullable
    public String getProviderPackage() {
        return this.mProviderPackage;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class TwaCustomTabsServiceConnection extends CustomTabsServiceConnection {
        private TwaCustomTabsServiceConnection() {
        }

        @Override // android.support.customtabs.CustomTabsServiceConnection
        public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
            if (TrustedWebUtils.warmupIsRequired(TwaLauncher.this.mContext, TwaLauncher.this.mProviderPackage)) {
                customTabsClient.warmup(0L);
            }
            TwaLauncher twaLauncher = TwaLauncher.this;
            twaLauncher.mSession = customTabsClient.newSession(null, twaLauncher.mSessionId);
            if (TwaLauncher.this.mOnSessionCreatedRunnable != null) {
                TwaLauncher.this.mOnSessionCreatedRunnable.run();
                TwaLauncher.this.mOnSessionCreatedRunnable = null;
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            TwaLauncher.this.mSession = null;
        }
    }
}
