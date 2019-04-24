package com.tamaq.courier.di.modules;

import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.NetworkChangeReceiver;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private TamaqApp mApp;

    public AppModule(TamaqApp app) {
        mApp = app;
    }

    @Provides
    @Singleton
    public TamaqApp provideApp() {
        return mApp;
    }

    @Provides
    @Singleton
    public NetworkChangeReceiver provideNetworkReceiver(TamaqApp app) {
        NetworkChangeReceiver receiver = new NetworkChangeReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        app.registerReceiver(receiver, intentFilter);
        return receiver;
    }

}
