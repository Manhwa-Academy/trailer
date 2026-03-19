package com.mari.magic.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.VolleyError;

public class VolleySingleton {

    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private static final int TIMEOUT_MS = 10000; // 10s timeout
    private static final int MAX_RETRIES = 2; // retry 2 lần

    private VolleySingleton(Context context) {
        ctx = context.getApplicationContext();
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        // Set retry policy mặc định cho tất cả request
        req.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Wrap request để log lỗi chi tiết
        req.setShouldCache(false); // optional, tuỳ request muốn cache hay không

        getRequestQueue().add(req);
    }

    // Optional: helper để log lỗi VolleyError đẹp hơn
    public static void logVolleyError(VolleyError error) {
        if (error.networkResponse != null) {
            Log.e("VOLLEY_ERROR", "HTTP Status: " + error.networkResponse.statusCode);
            Log.e("VOLLEY_ERROR", "Response data: " +
                    new String(error.networkResponse.data != null ? error.networkResponse.data : new byte[]{}));
        } else {
            Log.e("VOLLEY_ERROR", "Network error: " + error.toString());
        }
    }
}