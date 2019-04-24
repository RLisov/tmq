package com.tamaq.courier.model.api.response;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.api.common.ObjectWithId;

import java.util.List;

public class ChatMessageResponse {

    @SerializedName("id")
    private String id;
    @SerializedName("created")
    private String created;
    @SerializedName("user")
    private ObjectWithId user;
    @SerializedName("from_tamaq")
    private boolean fromTamaq;
    @SerializedName("msg")
    private String msg;
    //    @SerializedName("photos")
//    private PhotosBean mPhotos;
    @SerializedName("photos")
    private List<PhotosInfo> mPhotos;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public ObjectWithId getUser() {
        return user;
    }

    public void setUser(ObjectWithId user) {
        this.user = user;
    }

    public boolean isFromTamaq() {
        return fromTamaq;
    }

    public void setFromTamaq(boolean fromTamaq) {
        this.fromTamaq = fromTamaq;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getChatPhotoUrl() {
        return mPhotos != null && !mPhotos.isEmpty()
                ? "https://tamaq.kz" + mPhotos.get(0).getPath()
                : null;
    }

    public int getChatPhotoWidth() {
        return mPhotos != null && !mPhotos.isEmpty()
                ? mPhotos.get(0).getWidth()
                : 0;
    }

    public int getChatPhotoHeight() {
        return mPhotos != null && !mPhotos.isEmpty()
                ? mPhotos.get(0).getHeight()
                : 0;
    }

//    public PhotosBean getPhotos() {
//        return mPhotos;
//    }
//
//    public void setPhotos(PhotosBean photos) {
//        mPhotos = photos;
//    }

    public List<PhotosInfo> getPhotos() {
        return mPhotos;
    }

    public void setPhotos(List<PhotosInfo> photos) {
        mPhotos = photos;
    }

//    public static class PhotosBean {
//        @SerializedName("main")
//        private List<String> mMain;
//
//        public List<String> getMain() {
//            return mMain;
//        }
//
//        public void setMain(List<String> main) {
//            mMain = main;
//        }
//    }

    public static class PhotosInfo {
        @SerializedName("tag")
        private String mTag;
        @SerializedName("path")
        private String mPath;
        @SerializedName("width")
        private int mWidth;
        @SerializedName("height")
        private int mHeight;

        public String getTag() {
            return mTag;
        }

        public void setTag(String tag) {
            mTag = tag;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            mPath = path;
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
    }
}
