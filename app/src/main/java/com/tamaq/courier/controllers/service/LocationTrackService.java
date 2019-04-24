package com.tamaq.courier.controllers.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.LocationChangeListener;
import com.tamaq.courier.utils.PrefsHelper;

import javax.inject.Inject;

public class LocationTrackService extends Service {

    @Inject
    ServerCommunicator serverCommunicator;

    private LocationChangeListener locationChangeListener;

    public LocationTrackService() {
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, LocationTrackService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        injectDependencies();
        locationChangeListener = new LocationChangeListener(getApplicationContext(),
                location -> {
                    if (!PrefsHelper.isUserBlocked(getApplicationContext()) && PrefsHelper.isUserAuthorized(getApplicationContext())) {
                        try {
                            sendLocation(location);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 30_000);
    }

    private void injectDependencies() {
        TamaqApp.get(getApplicationContext()).getAppComponent().inject(this);
    }

    private void sendLocation(Location location) {
        serverCommunicator.sendUserLocation(location.getLatitude(), location.getLongitude())
                .subscribe(object -> {
                }, Throwable::printStackTrace);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationChangeListener != null) {
            locationChangeListener.stop();
        }
    }
}

