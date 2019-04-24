package com.tamaq.courier.model.api.request_bodies;


import com.google.gson.annotations.SerializedName;

public class SendLocationBody {

    @SerializedName("time")
    public String time;
    @SerializedName("latitude")
    public String latitude;
    @SerializedName("longitude")
    public String longitude;
    @SerializedName("os")
    public String os;

    public SendLocationBody(String time, String latitude, String longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.os = "Android";
    }
}
