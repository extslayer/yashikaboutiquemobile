package android.support.customtabs.trusted;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.TrustedWebUtils;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class TrustedWebActivityBuilder {
    @Nullable
    private List<String> mAdditionalTrustedOrigins;
    private final Context mContext;
    @Nullable
    private Bundle mSplashScreenParams;
    @Nullable
    private Integer mStatusBarColor;
    private final Uri mUri;

    public TrustedWebActivityBuilder(Context context, Uri uri) {
        this.mContext = context;
        this.mUri = uri;
    }

    public TrustedWebActivityBuilder setStatusBarColor(int i) {
        this.mStatusBarColor = Integer.valueOf(i);
        return this;
    }

    public TrustedWebActivityBuilder setAdditionalTrustedOrigins(List<String> list) {
        this.mAdditionalTrustedOrigins = list;
        return this;
    }

    public TrustedWebActivityBuilder setSplashScreenParams(Bundle bundle) {
        this.mSplashScreenParams = bundle;
        return this;
    }

    public void launchActivity(CustomTabsSession customTabsSession) {
        if (customTabsSession == null) {
            throw new NullPointerException("CustomTabsSession is required for launching a TWA");
        }
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabsSession);
        Integer num = this.mStatusBarColor;
        if (num != null) {
            builder.setToolbarColor(num.intValue());
        }
        Intent intent = builder.build().intent;
        intent.setData(this.mUri);
        intent.putExtra(TrustedWebUtils.EXTRA_LAUNCH_AS_TRUSTED_WEB_ACTIVITY, true);
        List<String> list = this.mAdditionalTrustedOrigins;
        if (list != null) {
            intent.putExtra(TrustedWebUtils.EXTRA_ADDITIONAL_TRUSTED_ORIGINS, new ArrayList(list));
        }
        Bundle bundle = this.mSplashScreenParams;
        if (bundle != null) {
            intent.putExtra(TrustedWebUtils.EXTRA_SPLASH_SCREEN_PARAMS, bundle);
        }
        ContextCompat.startActivity(this.mContext, intent, null);
    }

    public Uri getUrl() {
        return this.mUri;
    }

    @Nullable
    public Integer getStatusBarColor() {
        return this.mStatusBarColor;
    }
}
