package com.tamaq.courier.model.database;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.PhotoHelper;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MessageRealm extends RealmObject {

    public static final String KEY_ID = "mId";
    public static final String KEY_CHAT_ID = "mChatId";

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private String mId;
    @SerializedName("type")
    @Expose
    private Integer mType;
    @SerializedName("message")
    @Expose
    private String mMessage;
    @SerializedName("isRead")
    @Expose
    private Boolean mIsRead;
    @SerializedName("time")
    @Expose
    private String mTime;
    @SerializedName("imageUrl")
    @Expose
    private String mImageUrl;

    private int mWidth;
    private int mHeight;

    private String mChatId;

    private boolean mIsSent;

    public static MessageRealm createNewMessage(String chatId, String text, String id, boolean isRead, int type) {
        MessageRealm message = new MessageRealm();
        message.setIsRead(isRead);
        message.setMessage(text);
        message.setType(type);
        message.setTime(DateHelper.getCurrentDateString());
        message.setImageUrl(null);
        message.setSent(false);
        message.setId(id);
        message.setChatId(chatId);
        return message;
    }

    public void setIsRead(Boolean read) {
        mIsRead = read;
    }

    public static MessageRealm createNewPictureMessage(String chatId, String imagePath, String id, boolean isRead, int type) {
        MessageRealm message = new MessageRealm();
        message.setIsRead(isRead);
        message.setMessage("");
        message.setType(type);
        message.setTime(DateHelper.getCurrentDateString());
        message.setImageUrl(imagePath);
        message.setSent(false);
        message.setId(id);
        message.setChatId(chatId);

        Bitmap currentBitmap = PhotoHelper.getCurrentBitmapByLocalPath(imagePath);
        message.setWidth(currentBitmap.getWidth());
        message.setHeight(currentBitmap.getHeight());

        return message;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public MessageType getEnumType() {
        return MessageType.values()[getType()];
    }

    public Integer getType() {
        return mType;
    }

    public void setType(Integer type) {
        mType = type;
    }

    public void setTypeByEnum(MessageType type) {
        setType(type.ordinal());
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public Boolean isRead() {
        return mIsRead;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public boolean isSent() {
        return mIsSent;
    }

    public void setSent(boolean sent) {
        mIsSent = sent;
    }

    public String getChatId() {
        return mChatId;
    }

    public void setChatId(String chatId) {
        mChatId = chatId;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public enum MessageType {
        ME, CUSTOMER, TAMAQ
    }
}
