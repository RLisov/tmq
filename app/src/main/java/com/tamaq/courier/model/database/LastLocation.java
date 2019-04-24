package com.tamaq.courier.model.database;

public class LastLocation {

    private String mCountryName;
    private String mCityName;
    private String mRegion;
    private double mLatitude;
    private double mLongitude;

    public String getCountryName() {
        return mCountryName;
    }

    public void setCountryName(String mCountryName) {
        this.mCountryName = mCountryName;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String mCityName) {
        this.mCityName = mCityName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public String getRegion() {
        return mRegion;
    }

    public void setRegion(String region) {
        mRegion = region;
    }
}
