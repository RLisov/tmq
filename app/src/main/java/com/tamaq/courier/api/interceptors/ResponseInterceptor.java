package com.tamaq.courier.api.interceptors;

import android.util.Log;

import com.tamaq.courier.events.LogoutEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ResponseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        try {
            boolean isUserUnauthorized = response.code() == 401;
            if (isUserUnauthorized) EventBus.getDefault().post(new LogoutEvent(true));
        } catch (Exception e) {
            Log.e("Tamaq", "ResponseInterceptor", e);
        }
        return response;
    }
}
