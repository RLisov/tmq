package com.tamaq.courier.shared.google_map;

import com.google.android.gms.maps.model.LatLng;


public class MarkerAnimationInfo {

    public long duration;
    public int toAngle;
    public LatLng toPosition;

    public MarkerAnimationInfo(long duration, int toAngle, LatLng toPosition) {
        this.duration = duration;
        this.toAngle = toAngle;
        this.toPosition = toPosition;
    }
}
