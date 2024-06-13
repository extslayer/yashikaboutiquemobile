package android.support.customtabs.trusted;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;

/* loaded from: classes.dex */
public class Utils {
    public static void setStatusBarColor(Activity activity, int i) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        activity.getWindow().setStatusBarColor(i);
    }

    public static void setDarkStatusBarIcons(Activity activity) {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        addSystemUiVisibilityFlag(activity, 8192);
    }

    public static void setWhiteNavigationBar(Activity activity) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        activity.getWindow().setNavigationBarColor(-1);
        addSystemUiVisibilityFlag(activity, 16);
    }

    private static void addSystemUiVisibilityFlag(Activity activity, int i) {
        View rootView = activity.getWindow().getDecorView().getRootView();
        rootView.setSystemUiVisibility(i | rootView.getSystemUiVisibility());
    }

    public static boolean shouldUseDarkStatusBarIcons(int i) {
        return Math.abs(1.05f / ((((luminanceOfColorComponent((float) Color.red(i)) * 0.2126f) + (luminanceOfColorComponent((float) Color.green(i)) * 0.7152f)) + (luminanceOfColorComponent((float) Color.blue(i)) * 0.0722f)) + 0.05f)) < 3.0f;
    }

    private static float luminanceOfColorComponent(float f) {
        float f2 = f / 255.0f;
        return f2 < 0.03928f ? f2 / 12.92f : (float) Math.pow((f2 + 0.055f) / 1.055f, 2.4000000953674316d);
    }

    @Nullable
    public static Bitmap convertDrawableToBitmap(Context context, int i) {
        Drawable drawable = ContextCompat.getDrawable(context, i);
        if (drawable == null) {
            return null;
        }
        Drawable wrap = DrawableCompat.wrap(drawable);
        Bitmap createBitmap = Bitmap.createBitmap(wrap.getIntrinsicWidth(), wrap.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        wrap.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        wrap.draw(canvas);
        return createBitmap;
    }
}
