package com.tamaq.courier.model.database;


import io.realm.RealmObject;

/**
 * It is a class for collecting data of auto rate settings
 */
public class AutoRateSettingRealm extends RealmObject {

    public static final String DEFAULT_AUTOMATIC_WORKING_MODE = AutomaticWorkingMode.FULL_TOWN.getValue();
    public static final int DEFAULT_MAXIMUM_WORK_TIME = 30;
    public static final int DEFAULT_MAX_SERVICE_PRICE = 1500;
    public static final int DEFAULT_MIN_CLIENT_RATING = 10; // percents
    public static final int DEFAULT_WORKING_RADIUS = 10; // kilometers

    private String mAutomaticWorkingMode;
    private int mMaximumWorkTime; // in minutes
    private int mMaxServicePrice;     // Should I add the Currency ID?
    private int mMinimumClientRating;
    private int mWorkingRadius;
    private LocationRealm mWorkRegion;

    /**
     * true if the settings do not match the default settings
     */
    public boolean isDefault() {
        String automaticWorkingMode = getAutomaticWorkingMode();
        return automaticWorkingMode != null
                && automaticWorkingMode.equals(AutomaticWorkingMode.FULL_TOWN.getValue())
                && getMaximumWorkTime() == DEFAULT_MAXIMUM_WORK_TIME
                && getMaxServicePrice() == DEFAULT_MAX_SERVICE_PRICE
                && getMinimumClientRating() == DEFAULT_MIN_CLIENT_RATING
                && getWorkingRadius() == DEFAULT_WORKING_RADIUS;
    }

    public String getAutomaticWorkingMode() {
        return mAutomaticWorkingMode;
    }

    public void setAutomaticWorkingMode(String automaticWorkingMode) {
        mAutomaticWorkingMode = automaticWorkingMode;
    }

    public int getMaximumWorkTime() {
        return mMaximumWorkTime;
    }

    public void setMaximumWorkTime(int maximumWorkTime) {
        mMaximumWorkTime = maximumWorkTime;
    }

    public int getMaxServicePrice() {
        return mMaxServicePrice;
    }

    public void setMaxServicePrice(int maxServicePrice) {
        mMaxServicePrice = maxServicePrice;
    }

    public int getMinimumClientRating() {
        return mMinimumClientRating;
    }

    public void setMinimumClientRating(int minimumClientRating) {
        mMinimumClientRating = minimumClientRating;
    }

    public int getWorkingRadius() {
        return mWorkingRadius;
    }

    public void setWorkingRadius(int workingRadius) {
        mWorkingRadius = workingRadius;
    }

    public LocationRealm getWorkRegion() {
        return mWorkRegion;
    }

    public void setWorkRegion(LocationRealm workRegion) {
        mWorkRegion = workRegion;
    }

    public enum AutomaticWorkingMode {
        FULL_TOWN("full_town"), AROUND_ME("around_my"), SEPARATE_DISTRICT("district");

        private String mValue;

        AutomaticWorkingMode(String value) {
            mValue = value;
        }

        public static AutomaticWorkingMode getTypeByValue(String value) {
            switch (value) {
                case "full_town":
                    return FULL_TOWN;
                case "around_my":
                    return AROUND_ME;
                case "district":
                    return SEPARATE_DISTRICT;
                default:
                    return FULL_TOWN;
            }
        }

        public String getValue() {
            return mValue;
        }
    }
}