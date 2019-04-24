package com.tamaq.courier.presenters.registration.identifictaion;

import android.graphics.Bitmap;

import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public class IdentificationContract {

    interface View extends BaseView {

        void onOldFirstPicturesChecked(Bitmap photo);

        void onOldSecondPicturesChecked(Bitmap photo);

    }

    public interface Presenter extends BasePresenter<View> {

        void checkIsHasOldPicture();

        void addPicture(IdentificationPicture picture, Bitmap photo, String filePath);

        void removePicture(IdentificationPicture picture);

    }

}
