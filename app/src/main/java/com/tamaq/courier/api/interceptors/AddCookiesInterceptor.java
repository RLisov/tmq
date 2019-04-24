package com.tamaq.courier.api.interceptors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tamaq.courier.utils.PrefsHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AddCookiesInterceptor implements Interceptor {

    private Context mContext;

    public AddCookiesInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        HashSet<String> preferences = (HashSet) PreferenceManager.getDefaultSharedPreferences(mContext)
                .getStringSet(PrefsHelper.PREFS_COOKIES, new HashSet<>());
        HashMap<String, String> cookiesMap = new HashMap<>();
        for (String cookie : preferences) {
            String key = cookie.substring(0, cookie.indexOf("="));
            String value = cookie.substring(cookie.indexOf("=") + 1, cookie.indexOf(";"));
            cookiesMap.put(key, value);
//            builder.addHeader("Cookie", cookie);
        }

        Iterator<String> iterator = cookiesMap.keySet().iterator();
        for (int i = 0; i < cookiesMap.size(); i++) {
            if (!iterator.hasNext())
                break;
            String key = iterator.next();
            String value = cookiesMap.get(key);
            builder.addHeader("Cookie", key + "=" + value + ";");
            Log.v("OkHttp", "Cookie:" + key + "=" + value + ";"); // This is done so I know which headers are being added;
        }

        return chain.proceed(builder.build());
    }
}