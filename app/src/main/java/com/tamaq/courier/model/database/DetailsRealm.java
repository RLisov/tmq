package com.tamaq.courier.model.database;


import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class DetailsRealm extends RealmObject {

    @SerializedName("phone_code")
    private String mPhoneCode;
    @SerializedName("currency")
    private String mCurrency;
    @SerializedName("callcenter_phone")
    private String mCallcenterPhone;

    public String getPhoneCode() {
        return mPhoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        mPhoneCode = phoneCode;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public String getCallcenterPhone() {
        return mCallcenterPhone;
    }

    public void setCallcenterPhone(String callcenterPhone) {
        mCallcenterPhone = callcenterPhone;
    }
}
