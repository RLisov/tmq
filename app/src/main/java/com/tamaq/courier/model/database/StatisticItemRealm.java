package com.tamaq.courier.model.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class StatisticItemRealm extends RealmObject {

    public static final String ID_ROW = "mId";
    public static final String MOVE_TO_ROW = "mMoveTo";

    @PrimaryKey
    private String mId;
    private String mCreated;
    private String mMoveFrom;
    private String mMoveTo;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getMoveFrom() {
        return mMoveFrom;
    }

    public void setMoveFrom(String moveFrom) {
        mMoveFrom = moveFrom;
    }

    public String getMoveTo() {
        return mMoveTo;
    }

    public void setMoveTo(String moveTo) {
        mMoveTo = moveTo;
    }
}
