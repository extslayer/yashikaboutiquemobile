package android.support.customtabs;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* loaded from: classes.dex */
public final class CustomTabColorSchemeParams {
    @ColorInt
    @Nullable
    public final Integer navigationBarColor;
    @ColorInt
    @Nullable
    public final Integer secondaryToolbarColor;
    @ColorInt
    @Nullable
    public final Integer toolbarColor;

    CustomTabColorSchemeParams(@ColorInt @Nullable Integer num, @ColorInt @Nullable Integer num2, @ColorInt @Nullable Integer num3) {
        this.toolbarColor = num;
        this.secondaryToolbarColor = num2;
        this.navigationBarColor = num3;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @NonNull
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        Integer num = this.toolbarColor;
        if (num != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, num.intValue());
        }
        Integer num2 = this.secondaryToolbarColor;
        if (num2 != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR, num2.intValue());
        }
        Integer num3 = this.navigationBarColor;
        if (num3 != null) {
            bundle.putInt(CustomTabsIntent.EXTRA_NAVIGATION_BAR_COLOR, num3.intValue());
        }
        return bundle;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @NonNull
    public static CustomTabColorSchemeParams fromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle(0);
        }
        return new CustomTabColorSchemeParams((Integer) bundle.get(CustomTabsIntent.EXTRA_TOOLBAR_COLOR), (Integer) bundle.get(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR), (Integer) bundle.get(CustomTabsIntent.EXTRA_NAVIGATION_BAR_COLOR));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @NonNull
    public CustomTabColorSchemeParams withDefaults(@NonNull CustomTabColorSchemeParams customTabColorSchemeParams) {
        Integer num = this.toolbarColor;
        if (num == null) {
            num = customTabColorSchemeParams.toolbarColor;
        }
        Integer num2 = this.secondaryToolbarColor;
        if (num2 == null) {
            num2 = customTabColorSchemeParams.secondaryToolbarColor;
        }
        Integer num3 = this.navigationBarColor;
        if (num3 == null) {
            num3 = customTabColorSchemeParams.navigationBarColor;
        }
        return new CustomTabColorSchemeParams(num, num2, num3);
    }

    /* loaded from: classes.dex */
    public static final class Builder {
        @ColorInt
        @Nullable
        private Integer mNavigationBarColor;
        @ColorInt
        @Nullable
        private Integer mSecondaryToolbarColor;
        @ColorInt
        @Nullable
        private Integer mToolbarColor;

        @NonNull
        public Builder setToolbarColor(@ColorInt int i) {
            this.mToolbarColor = Integer.valueOf(i);
            return this;
        }

        @NonNull
        public Builder setSecondaryToolbarColor(@ColorInt int i) {
            this.mSecondaryToolbarColor = Integer.valueOf(i);
            return this;
        }

        @NonNull
        public Builder setNavigationBarColor(@ColorInt int i) {
            this.mNavigationBarColor = Integer.valueOf(i);
            return this;
        }

        @NonNull
        public CustomTabColorSchemeParams build() {
            return new CustomTabColorSchemeParams(this.mToolbarColor, this.mSecondaryToolbarColor, this.mNavigationBarColor);
        }
    }
}
