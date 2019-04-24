package com.tamaq.courier.model.api.common;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.database.LocationRealm;

public class AddressPojo {

    @SerializedName("id")
    public String id;
    @SerializedName("created")
    public String created;
    @SerializedName("town")
    public LocationRealm town;
    @SerializedName("street")
    public String street;
    @SerializedName("house")
    public String house;
    @SerializedName("floor")
    public String floor;
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("longitude")
    public double longitude;

    public String getFormattedAddress(Context context) {
        String resultAddress = "";

        if (!TextUtils.isEmpty(street)) resultAddress += street;
        if (!TextUtils.isEmpty(house)) {
            resultAddress += ", " + house;
            if (!TextUtils.isEmpty(floor)) resultAddress += ", этаж " + floor;
        }

        return resultAddress;
    }

    public String getFormattedAddressForArchive(Context context) {
        String resultAddress = "";
        if (!TextUtils.isEmpty(street)) resultAddress += street;
        if (!TextUtils.isEmpty(house)) resultAddress += ", " + house;
        return resultAddress;
    }

}
