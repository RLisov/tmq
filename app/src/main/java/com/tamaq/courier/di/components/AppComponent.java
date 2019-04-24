package com.tamaq.courier.di.components;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.controllers.service.LocationTrackService;
import com.tamaq.courier.di.modules.ApiModule;
import com.tamaq.courier.di.modules.AppModule;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.NetworkChangeReceiver;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {

    void inject(TamaqApp app);

    NetworkChangeReceiver getReceiver();

    void inject(LocationTrackService locationTrackService);

    TamaqApp getApp();

    ServerCommunicator getServerCommunicator();

}
