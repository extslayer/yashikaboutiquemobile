package android.support.customtabs.trusted.splashscreens;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;
import android.support.customtabs.TrustedWebUtils;
import android.support.customtabs.trusted.TrustedWebActivityBuilder;
import android.support.customtabs.trusted.Utils;
import android.support.customtabs.trusted.splashscreens.SplashImageTransferTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

/* loaded from: classes.dex */
public class PwaWrapperSplashScreenStrategy implements SplashScreenStrategy {
    private static final String TAG = "SplashScreenStrategy";
    private final Activity mActivity;
    @ColorInt
    private final int mBackgroundColor;
    @DrawableRes
    private final int mDrawableId;
    private boolean mEnterAnimationComplete;
    private final int mFadeOutDurationMillis;
    private final String mFileProviderAuthority;
    @Nullable
    private Runnable mOnEnterAnimationCompleteRunnable;
    @Nullable
    private String mProviderPackage;
    private boolean mProviderSupportsSplashScreens;
    private final ImageView.ScaleType mScaleType;
    @Nullable
    private Bitmap mSplashImage;
    @Nullable
    private SplashImageTransferTask mSplashImageTransferTask;
    @Nullable
    private final Matrix mTransformationMatrix;

    public PwaWrapperSplashScreenStrategy(Activity activity, @DrawableRes int i, @ColorInt int i2, ImageView.ScaleType scaleType, @Nullable Matrix matrix, int i3, String str) {
        this.mEnterAnimationComplete = Build.VERSION.SDK_INT < 21;
        this.mDrawableId = i;
        this.mBackgroundColor = i2;
        this.mScaleType = scaleType;
        this.mTransformationMatrix = matrix;
        this.mActivity = activity;
        this.mFileProviderAuthority = str;
        this.mFadeOutDurationMillis = i3;
    }

    @Override // android.support.customtabs.trusted.splashscreens.SplashScreenStrategy
    public void onTwaLaunchInitiated(String str, @Nullable Integer num) {
        this.mProviderPackage = str;
        this.mProviderSupportsSplashScreens = TrustedWebUtils.splashScreensAreSupported(this.mActivity, str, TrustedWebUtils.SplashScreenVersion.V1);
        if (!this.mProviderSupportsSplashScreens) {
            Log.w(TAG, "Provider " + str + " doesn't support splash screens");
            return;
        }
        showSplashScreen();
        if (this.mSplashImage != null) {
            customizeStatusAndNavBarDuringSplashScreen(num);
        }
    }

    private void showSplashScreen() {
        this.mSplashImage = Utils.convertDrawableToBitmap(this.mActivity, this.mDrawableId);
        if (this.mSplashImage == null) {
            Log.w(TAG, "Failed to retrieve splash image from provided drawable id");
            return;
        }
        ImageView imageView = new ImageView(this.mActivity);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        imageView.setImageBitmap(this.mSplashImage);
        imageView.setBackgroundColor(this.mBackgroundColor);
        imageView.setScaleType(this.mScaleType);
        if (this.mScaleType == ImageView.ScaleType.MATRIX) {
            imageView.setImageMatrix(this.mTransformationMatrix);
        }
        this.mActivity.setContentView(imageView);
    }

    private void customizeStatusAndNavBarDuringSplashScreen(@Nullable Integer num) {
        Utils.setWhiteNavigationBar(this.mActivity);
        if (num == null) {
            return;
        }
        Utils.setStatusBarColor(this.mActivity, num.intValue());
        if (Utils.shouldUseDarkStatusBarIcons(num.intValue())) {
            Utils.setDarkStatusBarIcons(this.mActivity);
        }
    }

    @Override // android.support.customtabs.trusted.splashscreens.SplashScreenStrategy
    public void configureTwaBuilder(final TrustedWebActivityBuilder trustedWebActivityBuilder, CustomTabsSession customTabsSession, final Runnable runnable) {
        if (!this.mProviderSupportsSplashScreens || this.mSplashImage == null) {
            runnable.run();
        } else if (TextUtils.isEmpty(this.mFileProviderAuthority)) {
            Log.w(TAG, "FileProvider authority not specified, can't transfer splash image.");
            runnable.run();
        } else {
            this.mSplashImageTransferTask = new SplashImageTransferTask(this.mActivity, this.mSplashImage, this.mFileProviderAuthority, customTabsSession, this.mProviderPackage);
            this.mSplashImageTransferTask.execute(new SplashImageTransferTask.Callback() { // from class: android.support.customtabs.trusted.splashscreens.-$$Lambda$PwaWrapperSplashScreenStrategy$jDY80P1hZNWBo2Kcj8dcQK1HJcE
                @Override // android.support.customtabs.trusted.splashscreens.SplashImageTransferTask.Callback
                public final void onFinished(boolean z) {
                    PwaWrapperSplashScreenStrategy.this.lambda$configureTwaBuilder$0$PwaWrapperSplashScreenStrategy(trustedWebActivityBuilder, runnable, z);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: onSplashImageTransferred */
    public void lambda$configureTwaBuilder$0$PwaWrapperSplashScreenStrategy(TrustedWebActivityBuilder trustedWebActivityBuilder, boolean z, final Runnable runnable) {
        if (!z) {
            Log.w(TAG, "Failed to transfer splash image.");
            runnable.run();
            return;
        }
        trustedWebActivityBuilder.setSplashScreenParams(makeSplashScreenParamsBundle());
        runWhenEnterAnimationComplete(new Runnable() { // from class: android.support.customtabs.trusted.splashscreens.-$$Lambda$PwaWrapperSplashScreenStrategy$l6IFUqYkMLBaC8kplZ_11ELNWME
            @Override // java.lang.Runnable
            public final void run() {
                PwaWrapperSplashScreenStrategy.this.lambda$onSplashImageTransferred$1$PwaWrapperSplashScreenStrategy(runnable);
            }
        });
    }

    public /* synthetic */ void lambda$onSplashImageTransferred$1$PwaWrapperSplashScreenStrategy(Runnable runnable) {
        runnable.run();
        this.mActivity.overridePendingTransition(0, 0);
    }

    private void runWhenEnterAnimationComplete(Runnable runnable) {
        if (this.mEnterAnimationComplete) {
            runnable.run();
        } else {
            this.mOnEnterAnimationCompleteRunnable = runnable;
        }
    }

    @NonNull
    private Bundle makeSplashScreenParamsBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(TrustedWebUtils.SplashScreenParamKey.VERSION, TrustedWebUtils.SplashScreenVersion.V1);
        bundle.putInt(TrustedWebUtils.SplashScreenParamKey.FADE_OUT_DURATION_MS, this.mFadeOutDurationMillis);
        bundle.putInt(TrustedWebUtils.SplashScreenParamKey.BACKGROUND_COLOR, this.mBackgroundColor);
        bundle.putInt(TrustedWebUtils.SplashScreenParamKey.SCALE_TYPE, this.mScaleType.ordinal());
        Matrix matrix = this.mTransformationMatrix;
        if (matrix != null) {
            float[] fArr = new float[9];
            matrix.getValues(fArr);
            bundle.putFloatArray(TrustedWebUtils.SplashScreenParamKey.IMAGE_TRANSFORMATION_MATRIX, fArr);
        }
        return bundle;
    }

    public void onActivityEnterAnimationComplete() {
        this.mEnterAnimationComplete = true;
        Runnable runnable = this.mOnEnterAnimationCompleteRunnable;
        if (runnable != null) {
            runnable.run();
            this.mOnEnterAnimationCompleteRunnable = null;
        }
    }

    public void destroy() {
        SplashImageTransferTask splashImageTransferTask = this.mSplashImageTransferTask;
        if (splashImageTransferTask != null) {
            splashImageTransferTask.cancel();
        }
    }
}
