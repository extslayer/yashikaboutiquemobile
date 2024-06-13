package android.support.customtabs.trusted;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* loaded from: classes.dex */
public class LauncherActivityMetadata {
    private static final int DEFAULT_COLOR_ID = 17170443;
    private static final String METADATA_DEFAULT_URL = "android.support.customtabs.trusted.DEFAULT_URL";
    private static final String METADATA_FILE_PROVIDER_AUTHORITY = "android.support.customtabs.trusted.FILE_PROVIDER_AUTHORITY";
    private static final String METADATA_SPLASH_IMAGE_DRAWABLE_ID = "android.support.customtabs.trusted.SPLASH_IMAGE_DRAWABLE";
    private static final String METADATA_SPLASH_SCREEN_BACKGROUND_COLOR = "android.support.customtabs.trusted.SPLASH_SCREEN_BACKGROUND_COLOR";
    private static final String METADATA_SPLASH_SCREEN_FADE_OUT_DURATION = "android.support.customtabs.trusted.SPLASH_SCREEN_FADE_OUT_DURATION";
    private static final String METADATA_STATUS_BAR_COLOR_ID = "android.support.customtabs.trusted.STATUS_BAR_COLOR";
    @Nullable
    public final String defaultUrl;
    @Nullable
    public final String fileProviderAuthority;
    public final int splashImageDrawableId;
    public final int splashScreenBackgroundColorId;
    public final int splashScreenFadeOutDurationMillis;
    public final int statusBarColorId;

    private LauncherActivityMetadata(@NonNull Bundle bundle) {
        this.defaultUrl = bundle.getString(METADATA_DEFAULT_URL);
        this.statusBarColorId = bundle.getInt(METADATA_STATUS_BAR_COLOR_ID, DEFAULT_COLOR_ID);
        this.splashImageDrawableId = bundle.getInt(METADATA_SPLASH_IMAGE_DRAWABLE_ID, 0);
        this.splashScreenBackgroundColorId = bundle.getInt(METADATA_SPLASH_SCREEN_BACKGROUND_COLOR, DEFAULT_COLOR_ID);
        this.fileProviderAuthority = bundle.getString(METADATA_FILE_PROVIDER_AUTHORITY);
        this.splashScreenFadeOutDurationMillis = bundle.getInt(METADATA_SPLASH_SCREEN_FADE_OUT_DURATION, 0);
    }

    public static LauncherActivityMetadata parse(Activity activity) {
        Bundle bundle;
        try {
            bundle = activity.getPackageManager().getActivityInfo(new ComponentName(activity, activity.getClass()), 128).metaData;
        } catch (PackageManager.NameNotFoundException unused) {
            bundle = null;
        }
        if (bundle == null) {
            bundle = new Bundle();
        }
        return new LauncherActivityMetadata(bundle);
    }
}
