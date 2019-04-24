package com.tamaq.courier.di.modules;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.tamaq.courier.api.ApiService;
import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.api.interceptors.AddCookiesInterceptor;
import com.tamaq.courier.api.interceptors.MockHeadersInterceptor;
import com.tamaq.courier.api.interceptors.ReceivedCookiesInterceptor;
import com.tamaq.courier.api.interceptors.ResponseInterceptor;
import com.tamaq.courier.shared.TamaqApp;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiModule {

    public static final String API_URL = "https://tamaq.kz/";

    @Provides
    @Singleton
    public ServerCommunicator provideServerCommunicator(@Named("apiservice") ApiService apiService) {
        return new ServerCommunicator(apiService);
    }

    @Provides
    @Singleton
    @Named("apiservice")
    public ApiService provideApiService(@Named("api") Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @Singleton
    @Named("api")
    public Retrofit provideRetrofit(Retrofit.Builder builder) {
        return builder.baseUrl(API_URL).build();
    }

    @Provides
    @Singleton
    public Retrofit.Builder provideRetrofitBuilder(TamaqApp tamaqApp) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
//                .hostnameVerifier((hostname, session) -> true) // for certificate expired
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new ResponseInterceptor())
                .addInterceptor(new MockHeadersInterceptor())
                .addInterceptor(new AddCookiesInterceptor(tamaqApp))
                .addInterceptor(new ReceivedCookiesInterceptor(tamaqApp))
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        return new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

    }

}
