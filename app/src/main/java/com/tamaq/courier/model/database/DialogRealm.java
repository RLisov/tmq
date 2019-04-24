package com.tamaq.courier.model.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DialogRealm extends RealmObject {

    public static final String CHAT_ID_ROW = "mChatId";
    public static final String HIDE_FROM_LIST_ROW = "mHideFromList";

    @SerializedName("userId")
    @Expose
    private String mUserId;

    @PrimaryKey
    private String mChatId;

    private String mCreatedDate;
    @SerializedName("userName")
    @Expose
    private String mUserName;
    @SerializedName("avatarUrl")
    @Expose
    private String mAvatarUrl;
    @SerializedName("orderStatus")
    @Expose
    private boolean mOrderStatus;
    @SerializedName("userAddress")
    @Expose
    private String mUserAddress;
    @SerializedName("messages")
    @Expose
    private RealmList<MessageRealm> mMessages = null;

    private boolean mHideFromList;

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String chatId) {
        mChatId = chatId;
    }

    public String getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        mCreatedDate = createdDate;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String mAvatarUrl) {
        this.mAvatarUrl = mAvatarUrl;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String id) {
        mUserId = id;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String name) {
        mUserName = name;
    }

    public String getUserAddress() {
        return mUserAddress;
    }

    public void setUserAddress(String address) {
        mUserAddress = address;
    }

    public boolean isOrderCompleted() {
        return mOrderStatus;
    }

    public void setOrderStatus(boolean orderStatus) {
        mOrderStatus = orderStatus;
    }

    public int getLastMessageIndex() {
        return getMessages().size() - 1;
    }

    public List<MessageRealm> getMessages() {
        return mMessages;
    }

    public void setMessages(RealmList<MessageRealm> messages) {
        this.mMessages = messages;
    }

    public boolean isHideFromList() {
        return mHideFromList;
    }

    public void setHideFromList(boolean hideFromList) {
        mHideFromList = hideFromList;
    }

    public void addMessages(List<MessageRealm> unreadedMessages) {
        getMessages().addAll(unreadedMessages);
    }

    @Override
    public int hashCode() {
        int result = mUserId.hashCode();
        result = 31 * result + mChatId.hashCode();
        result = 31 * result + mCreatedDate.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogRealm that = (DialogRealm) o;

        return mUserId.equals(that.mUserId)
                && mChatId.equals(that.mChatId)
                && mCreatedDate.equals(that.mCreatedDate);

    }


}
