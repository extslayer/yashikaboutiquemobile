package android.support.customtabs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.BundleCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/* loaded from: classes.dex */
public class TrustedWebUtils {
    public static final String ACTION_MANAGE_TRUSTED_WEB_ACTIVITY_DATA = "android.support.customtabs.action.ACTION_MANAGE_TRUSTED_WEB_ACTIVITY_DATA";
    public static final String EXTRA_ADDITIONAL_TRUSTED_ORIGINS = "android.support.customtabs.extra.ADDITIONAL_TRUSTED_ORIGINS";
    public static final String EXTRA_LAUNCH_AS_TRUSTED_WEB_ACTIVITY = "android.support.customtabs.extra.LAUNCH_AS_TRUSTED_WEB_ACTIVITY";
    public static final String EXTRA_SPLASH_SCREEN_PARAMS = "androidx.browser.trusted.EXTRA_SPLASH_SCREEN_PARAMS";
    private static final int NO_PREWARM_CHROME_VERSION_CODE = 368300000;
    private static final String NO_PROVIDER_RESOURCE_ID = "string/no_provider_toast";
    private static final int SUPPORTING_CHROME_VERSION_CODE = 362600000;
    private static final String UPDATE_CHROME_MESSAGE_RESOURCE_ID = "string/update_chrome_toast";
    private static final String CHROME_LOCAL_BUILD_PACKAGE = "com.google.android.apps.chrome";
    private static final String CHROMIUM_LOCAL_BUILD_PACKAGE = "org.chromium.chrome";
    private static final String CHROME_CANARY_PACKAGE = "com.chrome.canary";
    private static final String CHROME_DEV_PACKAGE = "com.chrome.dev";
    private static final String CHROME_BETA_PACKAGE = "com.chrome.beta";
    private static final String CHROME_STABLE_PACKAGE = "com.android.chrome";
    public static final List<String> SUPPORTED_CHROME_PACKAGES = Arrays.asList(CHROME_LOCAL_BUILD_PACKAGE, CHROMIUM_LOCAL_BUILD_PACKAGE, CHROME_CANARY_PACKAGE, CHROME_DEV_PACKAGE, CHROME_BETA_PACKAGE, CHROME_STABLE_PACKAGE);
    private static final List<String> VERSION_CHECK_CHROME_PACKAGES = Arrays.asList(CHROME_BETA_PACKAGE, CHROME_STABLE_PACKAGE);

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface SplashScreenParamKey {
        public static final String BACKGROUND_COLOR = "androidx.browser.trusted.trusted.KEY_SPLASH_SCREEN_BACKGROUND_COLOR";
        public static final String FADE_OUT_DURATION_MS = "androidx.browser.trusted.KEY_SPLASH_SCREEN_FADE_OUT_DURATION";
        public static final String IMAGE_TRANSFORMATION_MATRIX = "androidx.browser.trusted.KEY_SPLASH_SCREEN_TRANSFORMATION_MATRIX";
        public static final String SCALE_TYPE = "androidx.browser.trusted.KEY_SPLASH_SCREEN_SCALE_TYPE";
        public static final String VERSION = "androidx.browser.trusted.KEY_SPLASH_SCREEN_VERSION";
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface SplashScreenVersion {
        public static final String V1 = "androidx.browser.trusted.category.TrustedWebActivitySplashScreensV1";
    }

    private TrustedWebUtils() {
    }

    public static void launchBrowserSiteSettings(Context context, CustomTabsSession customTabsSession, Uri uri) {
        Intent intent = new Intent(ACTION_MANAGE_TRUSTED_WEB_ACTIVITY_DATA);
        intent.setPackage(customTabsSession.getComponentName().getPackageName());
        intent.setData(uri);
        Bundle bundle = new Bundle();
        BundleCompat.putBinder(bundle, CustomTabsIntent.EXTRA_SESSION, customTabsSession.getBinder());
        intent.putExtras(bundle);
        PendingIntent id = customTabsSession.getId();
        if (id != null) {
            intent.putExtra(CustomTabsIntent.EXTRA_SESSION_ID, id);
        }
        context.startActivity(intent);
    }

    public static void promptForChromeUpdateIfNeeded(Context context, String str) {
        if (VERSION_CHECK_CHROME_PACKAGES.contains(str) && chromeNeedsUpdate(context.getPackageManager(), str)) {
            showToastIfResourceExists(context, UPDATE_CHROME_MESSAGE_RESOURCE_ID);
        }
    }

    public static void showNoPackageToast(Context context) {
        showToastIfResourceExists(context, NO_PROVIDER_RESOURCE_ID);
    }

    public static boolean warmupIsRequired(Context context, String str) {
        return !CHROME_LOCAL_BUILD_PACKAGE.equals(str) && !CHROMIUM_LOCAL_BUILD_PACKAGE.equals(str) && SUPPORTED_CHROME_PACKAGES.contains(str) && getVersionCode(context.getPackageManager(), str) < NO_PREWARM_CHROME_VERSION_CODE;
    }

    public static boolean splashScreensAreSupported(Context context, String str, String str2) {
        ResolveInfo resolveService = context.getPackageManager().resolveService(new Intent().setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION).setPackage(str), 64);
        if (resolveService == null || resolveService.filter == null) {
            return false;
        }
        return resolveService.filter.hasCategory(str2);
    }

    public static boolean transferSplashImage(Context context, File file, String str, String str2, CustomTabsSession customTabsSession) {
        Uri uriForFile = FileProvider.getUriForFile(context, str, file);
        context.grantUriPermission(str2, uriForFile, 1);
        return customTabsSession.receiveFile(uriForFile, 1, null);
    }

    private static int getVersionCode(PackageManager packageManager, String str) {
        try {
            return packageManager.getPackageInfo(str, 0).versionCode;
        } catch (PackageManager.NameNotFoundException unused) {
            return 0;
        }
    }

    private static void showToastIfResourceExists(Context context, String str) {
        int identifier = context.getResources().getIdentifier(str, null, context.getPackageName());
        if (identifier == 0) {
            return;
        }
        Toast.makeText(context, identifier, 1).show();
    }

    public static boolean chromeNeedsUpdate(PackageManager packageManager, String str) {
        int versionCode = getVersionCode(packageManager, str);
        return versionCode != 0 && versionCode < SUPPORTING_CHROME_VERSION_CODE;
    }
}
