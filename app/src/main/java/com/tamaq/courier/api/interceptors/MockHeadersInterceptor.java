package com.tamaq.courier.api.interceptors;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MockHeadersInterceptor implements Interceptor {

    private List<String> excludedJsonHeaderUrls = Arrays.asList(
            "https://tamaq.kz/askrole/Executor",
            "https://tamaq.kz/logout",
            "https://tamaq.kz/orders/offer"
    );

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();
        if (!checkIsUrlInExcludedArray(originalRequest.url().toString()))
            builder.header("Content-Type", "application/json");
        Request requestWithUserAgent = builder.build();
        return chain.proceed(requestWithUserAgent);
    }

    private boolean checkIsUrlInExcludedArray(String url) {
        for (String s : excludedJsonHeaderUrls) {
            if (url.startsWith(s)) return true;
        }
        return false;
    }
}
