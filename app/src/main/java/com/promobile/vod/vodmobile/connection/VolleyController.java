package com.promobile.vod.vodmobile.connection;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


public class VolleyController extends Application {

    public static final String TAG = VolleyController.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static VolleyController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized VolleyController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue, new VodImageCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private static class VodImageCache implements ImageLoader.ImageCache {
        private static LruCache<String, Bitmap> bitmapLruCache;

        public VodImageCache() {
            bitmapLruCache = new LruCache<>(10);
        }

        @Override
        public Bitmap getBitmap(String url) {
            Bitmap bitmap = bitmapLruCache.get(url);

            return bitmap;
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            bitmapLruCache.put(url, bitmap);
        }
    }
}