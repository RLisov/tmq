package com.tamaq.courier.model.api.common;

import com.google.gson.annotations.SerializedName;


public class LatLngPojo {

    /**
     * lng : 44808000
     * lat : 6.552200999999999E7
     */

    @SerializedName("lng")
    public int lng;
    @SerializedName("lat")
    public double lat;

    public int getLng() {
        return lng;
    }

    public void setLng(int lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

}
