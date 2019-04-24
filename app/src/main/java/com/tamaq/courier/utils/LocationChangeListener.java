package com.tamaq.courier.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Action2;


@SuppressWarnings("EmptyFinallyBlock")
public class LocationChangeListener {

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public List<Action1<Location>> additionalLocationChangeListeners = new ArrayList<>();
    public Location previousBestLocation = null;
    private Action1<Location> resultCompletionBlock = null;
    private Action2<Location, Long> locationWithDurationListener;
    private long lastUpdateTime;

    public LocationChangeListener(Context appContext, Action1<Location> resultCompletionBlock) {
        this(appContext, resultCompletionBlock, 4000, null);
    }

    @SuppressWarnings("MissingPermission")
    public LocationChangeListener(Context appContext, Action1<Location> resultCompletionBlock,
                                  long minUpdateTime,
                                  Action2<Location, Long> locationWithDurationListener) {
        try {
            this.resultCompletionBlock = resultCompletionBlock;
            this.locationWithDurationListener = locationWithDurationListener;
            locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
            listener = new MyLocationListener();
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, minUpdateTime, 0, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minUpdateTime, 0, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastUpdateTime = System.currentTimeMillis();
        } finally {
        }
    }

    public LocationChangeListener(Context appContext, Action1<Location> resultCompletionBlock,
                                  long minUpdateTime) {
        this(appContext, resultCompletionBlock, minUpdateTime, null);
    }

    public LocationChangeListener(Context appContext,
                                  Action2<Location, Long> locationWithDurationListener,
                                  long minUpdateTime) {
        this(appContext, null, minUpdateTime, locationWithDurationListener);
    }

    public void stop() {
        try {
            locationManager.removeUpdates(listener);
        } finally {
        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public void addAdditionalLocationChangeListener(Action1<Location> completionListener) {
        additionalLocationChangeListeners.add(completionListener);
    }

    public void removeAdditionalLocationChangeListener(Action1<Location> completionListener) {
        additionalLocationChangeListeners.remove(completionListener);
    }


    public class MyLocationListener implements android.location.LocationListener {

        public void onLocationChanged(final Location loc) {
            if (isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                if (resultCompletionBlock != null) {
                    resultCompletionBlock.call(loc);
                }
                if (locationWithDurationListener != null)
                    locationWithDurationListener.call(loc, System.currentTimeMillis() - lastUpdateTime);
                for (Action1<Location> listener : additionalLocationChangeListeners)
                    listener.call(loc);
                Log.d("Location changed", String.format("lat: %f; lng: %f",
                        loc.getLatitude(),
                        loc.getLongitude()));

                lastUpdateTime = System.currentTimeMillis();
            }
        }

        public void onProviderDisabled(String provider) {
        }


        public void onProviderEnabled(String provider) {
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

}