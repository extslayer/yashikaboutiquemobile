package android.support.customtabs.trusted.splashscreens;

import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.trusted.TrustedWebActivityBuilder;

/* loaded from: classes.dex */
public interface SplashScreenStrategy {
    void configureTwaBuilder(TrustedWebActivityBuilder trustedWebActivityBuilder, CustomTabsSession customTabsSession, Runnable runnable);

    void onTwaLaunchInitiated(String str, @Nullable Integer num);
}
