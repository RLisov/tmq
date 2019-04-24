package com.tamaq.courier.model.api.common;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.database.LocationRealm;

import java.util.List;

public class ServicePojo {

    @SerializedName("id")
    public String id;
    @SerializedName("created")
    public String created;
    @SerializedName("name")
    public String name;
    @SerializedName("positive_valuations")
    public int positiveValuations;
    @SerializedName("negative_valuations")
    public int negativeValuations;
    @SerializedName("pricelevel")
    public int pricelevel;
    @SerializedName("openfrom")
    public int openfrom;
    @SerializedName("opento")
    public int opento;
    @SerializedName("minbill")
    public int minbill;
    @SerializedName("mDescription")
    public String description;
    @SerializedName("country")
    public LocationRealm country;
    @SerializedName("distance")
    public int distance;
    @SerializedName("providetime")
    public int providetime;
    @SerializedName("categories")
    public List<CategoryPojo> categories;
    @SerializedName("addresses")
    public List<AddressPojo> addresses;
    @SerializedName("owners")
    public List<ObjectWithId> owners;
    public String phone;
}
