package android.support.customtabs.browseractions;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.IOException;

/* loaded from: classes.dex */
public abstract class BrowserServiceImageReadTask extends AsyncTask<Uri, Void, Bitmap> {
    private static final String TAG = "BrowserServiceImageReadTask";
    private final ContentResolver mResolver;

    protected abstract void onBitmapFileReady(Bitmap bitmap);

    public BrowserServiceImageReadTask(ContentResolver contentResolver) {
        this.mResolver = contentResolver;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Bitmap doInBackground(Uri... uriArr) {
        try {
            ParcelFileDescriptor openFileDescriptor = this.mResolver.openFileDescriptor(uriArr[0], "r");
            if (openFileDescriptor == null) {
                return null;
            }
            Bitmap decodeFileDescriptor = BitmapFactory.decodeFileDescriptor(openFileDescriptor.getFileDescriptor());
            openFileDescriptor.close();
            return decodeFileDescriptor;
        } catch (IOException e) {
            Log.e(TAG, "Failed to read bitmap", e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public final void onPostExecute(Bitmap bitmap) {
        onBitmapFileReady(bitmap);
    }
}
