package com.tamaq.courier.model.database;


import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class CityRealm extends RealmObject {

    public static final String KEY_ROW = "mKey";
    public static final String NAME_RU_ROW = "mValueRu";
    public static final String NAME_EN_ROW = "mValueEn";
    public static final String NAME_KZ_ROW = "mValueKz";

    private String mType;
    @PrimaryKey
    private String mKey;
    @SerializedName("value_ru")
    private String mValueRu;
    @SerializedName("value_en")
    private String mValueEn;
    @SerializedName("value_kz")
    private String mValueKz;
    @Ignore
    private boolean mChosen;

    public CityRealm() {
    }

    public boolean isChosen() {
        return mChosen;
    }

    public void setChosen(boolean chosed) {
        mChosen = chosed;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getValueRu() {
        return mValueRu;
    }

    public void setValueRu(String valueRu) {
        mValueRu = valueRu;
    }

    public String getValueEn() {
        return mValueEn;
    }

    public void setValueEn(String valueEn) {
        mValueEn = valueEn;
    }

    public String getValueKz() {
        return mValueKz;
    }

    public void setValueKz(String valueKz) {
        mValueKz = valueKz;
    }
}
