package com.tamaq.courier.shared.google_map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MarkerAnimationController {

    private HashMap<Marker, MarkerAnimationHelper> animationMap = new HashMap<>();

    public void addAnimateMarkerByPath(String totalPath, Marker marker) {
        List<MarkerAnimationInfo> markerAnimationInfoList = prepareMarkerAnimationInfoList(totalPath);
        MarkerAnimationHelper markerAnimationHelper = animationMap.get(marker);
        if (markerAnimationHelper == null) {
            markerAnimationHelper = new MarkerAnimationHelper(marker);
            animationMap.put(marker, markerAnimationHelper);
        }
        markerAnimationHelper.addPoints(markerAnimationInfoList);
        markerAnimationHelper.startIfNotStarted();
    }

    private List<MarkerAnimationInfo> prepareMarkerAnimationInfoList(String totalPath) {
        List<MarkerAnimationInfo> animationInfoList = new ArrayList<>();
        List<LatLng> latLngList = PolyUtil.decode(totalPath);
        if (latLngList.size() < 2)
            return new ArrayList<>();

        double distanceTotal = SphericalUtil.computeLength(latLngList);
        long totalTime = 30 * 1000;
        double speed = distanceTotal / (float) totalTime;
        for (int i = 0; i < latLngList.size() - 1; i++) {
            LatLng firstLatLng = latLngList.get(i);
            LatLng secondLatLng = latLngList.get(i + 1);
            long distance = (long) SphericalUtil.computeDistanceBetween(firstLatLng, secondLatLng);
            int toAngle = bearingBetweenLocations(firstLatLng, secondLatLng);
            long duration = (long) (distance / speed);
            MarkerAnimationInfo markerAnimationInfo = new MarkerAnimationInfo(duration, toAngle, secondLatLng);
            animationInfoList.add(markerAnimationInfo);
        }
        return animationInfoList;
    }

    private int bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return (int) brng;
    }

    public void addAnimateMarkerByPoint(LatLng curLatLng, Marker marker, long time) {
        List<MarkerAnimationInfo> markerAnimationInfoList = prepareMarkerAnimationInfoList(
                marker.getPosition(), curLatLng, time);
        MarkerAnimationHelper markerAnimationHelper = animationMap.get(marker);
        if (markerAnimationHelper == null) {
            markerAnimationHelper = new MarkerAnimationHelper(marker);
            animationMap.put(marker, markerAnimationHelper);
        }
        markerAnimationHelper.addPoints(markerAnimationInfoList);
        markerAnimationHelper.startIfNotStarted();
    }

    private List<MarkerAnimationInfo> prepareMarkerAnimationInfoList(LatLng oldLatLng,
                                                                     LatLng latLng,
                                                                     long time) {
        List<MarkerAnimationInfo> animationInfoList = new ArrayList<>();
        List<LatLng> latLngListPoint = new ArrayList<>();
        latLngListPoint.add(latLng);
        latLngListPoint.add(oldLatLng);

        double distanceTotal = SphericalUtil.computeLength(latLngListPoint);
        double speed = distanceTotal / (float) time;
        for (int i = 0; i < latLngListPoint.size() - 1; i++) {
            LatLng firstLatLng = latLngListPoint.get(i);
            LatLng secondLatLng = latLngListPoint.get(i + 1);
            long distance = (long) SphericalUtil.computeDistanceBetween(firstLatLng, secondLatLng);
            int toAngle = bearingBetweenLocations(firstLatLng, secondLatLng);
            long duration = (long) (distance / speed);
            MarkerAnimationInfo markerAnimationInfo = new MarkerAnimationInfo(duration, toAngle, secondLatLng);
            animationInfoList.add(markerAnimationInfo);
        }
        return animationInfoList;
    }
}
