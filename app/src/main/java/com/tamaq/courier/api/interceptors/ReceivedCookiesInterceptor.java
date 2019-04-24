package com.tamaq.courier.api.interceptors;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tamaq.courier.utils.PrefsHelper;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {

    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }

            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putStringSet(PrefsHelper.PREFS_COOKIES, cookies)
                    .apply();
        }

        return originalResponse;
    }
}