package com.tamaq.courier.model.api.common;

import com.google.gson.annotations.SerializedName;

public class LastLocationPojo {

    /**
     * mId : 60c459d3-d2a2-43a6-a5bb-c285902b88b1
     * mCreated : 2017-05-30 07:54:45
     * user : {"mId":"c66e84a1-5c49-461f-ac2e-cb672d6a61f5"}
     * time : 2017-05-30 19:00:23
     * latitude : 44.808001
     * longitude : 65.5220105
     */

    @SerializedName("id")
    public String id;
    @SerializedName("created")
    public String created;
    @SerializedName("user")
    public ObjectWithId user;
    @SerializedName("time")
    public String time;
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("longitude")
    public double longitude;

}
