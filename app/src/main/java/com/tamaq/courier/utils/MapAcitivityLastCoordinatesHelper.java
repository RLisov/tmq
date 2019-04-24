package com.tamaq.courier.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * To solve this issue https://bitbucket.org/WoxappGIT/client-tamaq-android-curier/issues/58/------------------------------------------
 */

public class MapAcitivityLastCoordinatesHelper {

    public static final String MAP_ACTIVITY_LAST_LAT = "map_activity_last_lat";
    public static final String MAP_ACTIVITY_LAST_LNG = "map_activity_last_lng";
    public static final String MAP_ACTIVITY_LAST_ZOOM = "map_activity_last_zoom";

    /**
     * Call when order finished or canceled
     */
    public static void clearMapActivityLastCoordinates(Context context) {
        saveMapActivityLastCoordinates(context, -1, -1, -1);
    }

    public static void saveMapActivityLastCoordinates(Context context, double lat, double lng, float zoom) {
        SharedPreferences sharedPreferences = PrefsHelper.getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(MAP_ACTIVITY_LAST_LAT, String.valueOf(lat));
        ed.putString(MAP_ACTIVITY_LAST_LNG, String.valueOf(lng));
        ed.putFloat(MAP_ACTIVITY_LAST_ZOOM, zoom);
        ed.apply();
    }

    public static MapActivityLastCoordinates getLastMapActivityCoordinates(Context context) {
        SharedPreferences prefs = PrefsHelper.getCommonSharedPref(context);
        MapActivityLastCoordinates coordinates = new MapActivityLastCoordinates(
                Double.valueOf(prefs.getString(MAP_ACTIVITY_LAST_LAT, "-1")),
                Double.valueOf(prefs.getString(MAP_ACTIVITY_LAST_LNG, "-1")),
                prefs.getFloat(MAP_ACTIVITY_LAST_ZOOM, -1)
        );
        return coordinates;
    }

    public static class MapActivityLastCoordinates {

        public double lat;
        public double lng;
        public float zoom;
        public MapActivityLastCoordinates(double lat, double lng, float zoom) {
            this.lat = lat;
            this.lng = lng;
            this.zoom = zoom;
        }

        public boolean isEmpty() {
            return lat == -1 || lng == -1 || zoom == -1;
        }
    }
}
