package com.tamaq.courier.model.database;


import com.tamaq.courier.model.ui.CheckableUIItem;
import com.tamaq.courier.model.ui.SingleTextUIItem;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class RegionRealm extends RealmObject implements CheckableUIItem, SingleTextUIItem {

    public static final long NOT_SELECTED_ID = -1;

    public static final String ID_ROW = "mId";
    @PrimaryKey
    private String mId;
    private String mName;
    @Ignore
    private boolean mChecked;

    public RegionRealm() {
    }

    public RegionRealm(String id, String name) {
        mId = id;
        mName = name;
    }

    public RegionRealm(int id, String name) {
        mId = String.valueOf(id);
        mName = name;
    }

    @Override
    public String getTitleUI() {
        return getName();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getIdUI() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
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
