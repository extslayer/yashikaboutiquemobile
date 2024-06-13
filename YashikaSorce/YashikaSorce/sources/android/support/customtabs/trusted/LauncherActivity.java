package android.support.customtabs.trusted;

import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.TrustedWebUtils;
import android.support.customtabs.trusted.splashscreens.PwaWrapperSplashScreenStrategy;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class LauncherActivity extends AppCompatActivity {
    private static final String BROWSER_WAS_LAUNCHED_KEY = "android.support.customtabs.trusted.BROWSER_WAS_LAUNCHED_KEY";
    private static final String TAG = "TWALauncherActivity";
    private static boolean sChromeVersionChecked;
    private boolean mBrowserWasLaunched;
    private LauncherActivityMetadata mMetadata;
    @Nullable
    private PwaWrapperSplashScreenStrategy mSplashScreenStrategy;
    @Nullable
    private TwaLauncher mTwaLauncher;

    @Nullable
    protected Matrix getSplashImageTransformationMatrix() {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (bundle != null && bundle.getBoolean(BROWSER_WAS_LAUNCHED_KEY)) {
            finish();
            return;
        }
        this.mMetadata = LauncherActivityMetadata.parse(this);
        if (splashScreenNeeded()) {
            this.mSplashScreenStrategy = new PwaWrapperSplashScreenStrategy(this, this.mMetadata.splashImageDrawableId, getColorCompat(this.mMetadata.splashScreenBackgroundColorId), getSplashImageScaleType(), getSplashImageTransformationMatrix(), this.mMetadata.splashScreenFadeOutDurationMillis, this.mMetadata.fileProviderAuthority);
        }
        TrustedWebActivityBuilder statusBarColor = new TrustedWebActivityBuilder(this, getLaunchingUrl()).setStatusBarColor(getColorCompat(this.mMetadata.statusBarColorId));
        this.mTwaLauncher = new TwaLauncher(this);
        this.mTwaLauncher.launch(statusBarColor, this.mSplashScreenStrategy, new Runnable() { // from class: android.support.customtabs.trusted.-$$Lambda$LauncherActivity$y3M4Hm9MsrRadTqF7Qzkjdy5Mzk
            @Override // java.lang.Runnable
            public final void run() {
                LauncherActivity.this.lambda$onCreate$0$LauncherActivity();
            }
        });
        if (sChromeVersionChecked) {
            return;
        }
        TrustedWebUtils.promptForChromeUpdateIfNeeded(this, this.mTwaLauncher.getProviderPackage());
        sChromeVersionChecked = true;
    }

    public /* synthetic */ void lambda$onCreate$0$LauncherActivity() {
        this.mBrowserWasLaunched = true;
    }

    private boolean splashScreenNeeded() {
        if (this.mMetadata.splashImageDrawableId == 0) {
            return false;
        }
        return isTaskRoot();
    }

    @NonNull
    protected ImageView.ScaleType getSplashImageScaleType() {
        return ImageView.ScaleType.CENTER;
    }

    private int getColorCompat(int i) {
        return ContextCompat.getColor(this, i);
    }

    @Override // android.app.Activity
    protected void onRestart() {
        super.onRestart();
        if (this.mBrowserWasLaunched) {
            finish();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        TwaLauncher twaLauncher = this.mTwaLauncher;
        if (twaLauncher != null) {
            twaLauncher.destroy();
        }
        PwaWrapperSplashScreenStrategy pwaWrapperSplashScreenStrategy = this.mSplashScreenStrategy;
        if (pwaWrapperSplashScreenStrategy != null) {
            pwaWrapperSplashScreenStrategy.destroy();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(BROWSER_WAS_LAUNCHED_KEY, this.mBrowserWasLaunched);
    }

    @Override // android.app.Activity
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        PwaWrapperSplashScreenStrategy pwaWrapperSplashScreenStrategy = this.mSplashScreenStrategy;
        if (pwaWrapperSplashScreenStrategy != null) {
            pwaWrapperSplashScreenStrategy.onActivityEnterAnimationComplete();
        }
    }

    protected Uri getLaunchingUrl() {
        Uri data = getIntent().getData();
        if (data != null) {
            Log.d(TAG, "Using URL from Intent (" + data + ").");
            return data;
        } else if (this.mMetadata.defaultUrl != null) {
            Log.d(TAG, "Using URL from Manifest (" + this.mMetadata.defaultUrl + ").");
            return Uri.parse(this.mMetadata.defaultUrl);
        } else {
            return Uri.parse("https://www.example.com/");
        }
    }
}
