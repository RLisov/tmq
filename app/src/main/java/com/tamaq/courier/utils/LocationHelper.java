package com.tamaq.courier.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import rx.functions.Action1;


public class LocationHelper {

    public static void getLastKnownLocationObservable(Activity activity,
                                                      Action1<Observable<Address>> completeAction) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        if (rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                && rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            completeAction.call(getLocationObservable(activity));
        } else {
            rxPermissions.request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(isGranted -> {
                        if (isGranted)
                            completeAction.call(getLocationObservable(activity));
                    });
        }
    }

    @SuppressWarnings("MissingPermission")
    private static Observable<Address> getLocationObservable(Activity activity) {
        return Observable.create(observableEmitter -> {
            LocationManager locationManager = (LocationManager)
                    activity.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                try {
                    Geocoder gcd = new Geocoder(activity, new Locale("ru"));
                    List<Address> fromLocation =
                            gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    observableEmitter.onNext(fromLocation.get(0));
                    observableEmitter.onComplete();
                } catch (IOException e) {
                    observableEmitter.onError(e);
                }
            }
            observableEmitter.onComplete();
        });
    }

}
