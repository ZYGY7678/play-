package com.example.contacts.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;
import android.widget.ImageView;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;

import java.io.InputStream;

public final class PhotoCache {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(2 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getRowBytes() * value.getHeight(); }
    };
    private PhotoCache() {}

    public static Bitmap get(String key) { return key == null ? null : CACHE.get(key); }

    public static void loadInto(final Context c, final String uriText, final ImageView view, final String placeholder) {
        if (uriText == null || uriText.length() == 0) return;
        Bitmap hit = CACHE.get(uriText);
        if (hit != null) { view.setImageBitmap(hit); return; }
        view.setTag(uriText);
        new AsyncTask<Void, Void, Bitmap>() {
            protected Bitmap doInBackground(Void... v) {
                InputStream in = null;
                try {
                    in = c.getContentResolver().openInputStream(Uri.parse(uriText));
                    if (in == null) return null;
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inSampleSize = 2;
                    Bitmap b = BitmapFactory.decodeStream(in, null, o);
                    if (b != null) CACHE.put(uriText, b);
                    return b;
                } catch (Exception ignored) { return null; }
                finally { if (in != null) try { in.close(); } catch (Exception ignored) {} }
            }
            protected void onPostExecute(Bitmap b) {
                if (b != null && uriText.equals(view.getTag())) view.setImageBitmap(b);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static GradientDrawable circle(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }
}