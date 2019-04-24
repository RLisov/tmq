package com.tamaq.courier.model.api.common;


import com.google.gson.annotations.SerializedName;

public class CategoryPojo {

    @SerializedName("type")
    public String type;
    @SerializedName("mKey")
    public String key;
    @SerializedName("value_ru")
    public String valueRu;
    @SerializedName("value_en")
    public String valueEn;
    @SerializedName("value_kz")
    public String valueKz;

}
