package com.tamaq.courier.model.database;


import io.realm.RealmObject;

/**
 * Class for working with general settings of orders of user
 */
public class CommonOrdersSettingRealm extends RealmObject {

    public static final String DEFAULT_WORK_TYPE = WorkType.NOT_WORKING.toString();
    public static final int DEFAULT_MIN_PAYMENT = 50;

    private TransportTypeRealm mTransportTypeRealm;
    private int mMinReward;
    private String mWorkType;

    public TransportTypeRealm getTransportTypeRealm() {
        return mTransportTypeRealm;
    }

    public void setTransportTypeRealm(TransportTypeRealm transportTypeRealm) {
        mTransportTypeRealm = transportTypeRealm;
    }

    public int getMinReward() {
        return mMinReward;
    }

    public void setMinReward(int minReward) {
        mMinReward = minReward;
    }

    public String getWorkType() {
        return mWorkType;
    }

    public void setWorkType(String workType) {
        mWorkType = workType;
    }

    public enum WorkType {
        NOT_WORKING("not_working"), REVIEW("review"), AUTOMATIC("automatic");

        private String mValue;

        WorkType(String value) {
            mValue = value;
        }

        public static WorkType getByValue(String value) {
            switch (value) {
                case "not_working":
                    return NOT_WORKING;
                case "review":
                    return REVIEW;
                case "automatic":
                    return AUTOMATIC;
                default:
                    return NOT_WORKING;
            }
        }

        @Override
        public String toString() {
            return mValue;
        }
    }
}