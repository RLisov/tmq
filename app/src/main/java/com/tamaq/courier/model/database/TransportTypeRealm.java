package com.tamaq.courier.model.database;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.ui.CheckableUIItem;
import com.tamaq.courier.model.ui.SingleTextUIItem;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class TransportTypeRealm extends RealmObject implements CheckableUIItem, SingleTextUIItem {

    public static final String KEY_ROW = "mKey";

    public static final String NAME_RU_ROW = "mValueRu";
    public static final String NAME_EN_ROW = "mValueEn";
    public static final String NAME_KZ_ROW = "mValueKz";

    @SerializedName("type")
    private String mType;
    @SerializedName("key")
    @PrimaryKey
    private String mKey;
    @SerializedName("value_ru")
    private String mValueRu;
    @SerializedName("value_en")
    private String mValueEn;
    @SerializedName("value_kz")
    private String mValueKz;
    @Ignore
    private boolean mChecked;

    public TransportTypeRealm() {
    }

    //
    public TransportTypeRealm(String id, String name) {
        mKey = id;
        mValueRu = name;
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

    @Override
    public String getTitleUI() {
        return getValueRu();
    }

    public String getValueRu() {
        return mValueRu;
    }

    public void setValueRu(String valueRu) {
        mValueRu = valueRu;
    }

    public String getIdUI() {
        return getKey();
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    @Override
    public boolean isCheckedUI() {
        return mChecked;
    }

    @Override
    public void setCheckedUI(boolean checked) {
        mChecked = checked;
    }
}
