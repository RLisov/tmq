package com.tamaq.courier.model.database;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NotificationRealm extends RealmObject {

    public static final String NOTIFICATION_ID_ROW = "mId";

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private String mId;

    @SerializedName("date")
    @Expose
    private String mDate;

    @SerializedName("title")
    @Expose
    private String mTitle;

    @SerializedName("mDescription")
    @Expose
    private String mDescription;

    @SerializedName("orderId")
    @Expose
    private String mOrderId;

    @SerializedName("userId")
    @Expose
    private String mUserId;

    @SerializedName("type")
    @Expose
    private String mType;

    private boolean mIsReaded;
    private boolean mIsReadLocally;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public boolean isReaded() {
        return mIsReaded;
    }

    public void setReaded(boolean readed) {
        mIsReaded = readed;
    }

    public boolean isReadLocally() {
        return mIsReadLocally;
    }

    public void setReadLocally(boolean readLocally) {
        mIsReadLocally = readLocally;
    }

    public String getTitleUI(Context context) {
        String title = getTitle();
        if (title != null) return title;
        return NotificationType.getNotificationTitleUI(NotificationType.getNotificationType(getType()), context);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }
}
