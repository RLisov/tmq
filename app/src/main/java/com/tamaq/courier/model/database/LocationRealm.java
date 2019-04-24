package com.tamaq.courier.model.database;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.ui.CheckableUIItem;
import com.tamaq.courier.model.ui.SingleTextUIItem;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class LocationRealm extends RealmObject implements CheckableUIItem, SingleTextUIItem {

    public static final String KEY_ROW = "mKey";
    public static final String TYPE_ROW = "mType";
    public static final String NAME_RU_ROW = "mValueRu";
    public static final String NAME_EN_ROW = "mValueEn";
    public static final String NAME_KZ_ROW = "mValueKz";
    public static final String DETAILS_ROW = "mDetails";
    @SerializedName("type")
    private String mType;
    @SerializedName("value_ru")
    private String mValueRu;
    @SerializedName("value_en")
    private String mValueEn;
    @SerializedName("value_kz")
    private String mValueKz;
    @SerializedName("details")
    private DetailsRealm mDetails;
    @SerializedName("childes")
    private RealmList<LocationRealm> mChildes;
    @SerializedName("key")
    @PrimaryKey
    private String mKey;
    @Ignore
    private boolean mChosen;

    @Override
    public boolean isCheckedUI() {
        return mChosen;
    }

    @Override
    public void setCheckedUI(boolean checked) {
        this.mChosen = checked;
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

    @Override
    public String getIdUI() {
        return getKey();
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
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

    public String getCountryCodeString() {
        return getDetails().getPhoneCode();
    }

    public DetailsRealm getDetails() {
        return mDetails;
    }

    public void setDetails(DetailsRealm details) {
        mDetails = details;
    }

    public String getCurrency() {
        return getDetails().getCurrency();
    }

    public String getCallcenterPhone() {
        return getDetails().getCallcenterPhone();
    }

    public void setCallcenterPhone(String number) {
        getDetails().setCallcenterPhone(number);
    }

    public RealmList<LocationRealm> getChildes() {
        return mChildes;
    }

    public void setChildes(RealmList<LocationRealm> childes) {
        mChildes = childes;
    }

    public enum Type {
        COUNTRIES("countries"), CITIES("cities"), DISTRICTS("districts");

        String mValue;

        Type(String value) {
            mValue = value;
        }

        @Override
        public String toString() {
            return mValue;
        }
    }
}
