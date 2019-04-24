package com.tamaq.courier.presenters.registration.identifictaion;

import android.graphics.Bitmap;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PhotoHelper;

import java.util.HashMap;

public class IdentificationPresenter extends BasePresenterAbs<IdentificationContract.View>
        implements IdentificationContract.Presenter {

    private final static UserRealm USER = UserRealm.getInstance();

    private IdentificationFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public IdentificationPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(IdentificationContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (IdentificationFragment) view;
    }

    @Override
    public void checkIsHasOldPicture() {
        HashMap<UserRealm.IdentificationPhoto, Bitmap> photos = USER.getIdentificationPhotos();

        if (photos != null) {
            if (photos.containsKey(UserRealm.IdentificationPhoto.FIRST)) {
                getView().onOldFirstPicturesChecked(photos.get(UserRealm.IdentificationPhoto.FIRST));
            }

            if (photos.containsKey(UserRealm.IdentificationPhoto.SECOND)) {
                getView().onOldSecondPicturesChecked(photos.get(UserRealm.IdentificationPhoto.SECOND));
            }

        }
    }

    @Override
    public void addPicture(IdentificationPicture picture, Bitmap photo, String filePath) {
        if (picture.equals(IdentificationPicture.FIRST)) {
            USER.addIdentificationPhotoByKey(UserRealm.IdentificationPhoto.FIRST, photo);
            USER.setPassportPhoto1Path(filePath);
        } else {
            USER.addIdentificationPhotoByKey(UserRealm.IdentificationPhoto.SECOND, photo);
            USER.setPassportPhoto2Path(filePath);
        }
        PhotoHelper.clearCurrentPhotoPath();
    }

    @Override
    public void removePicture(IdentificationPicture picture) {
        if (picture.equals(IdentificationPicture.FIRST)) {
            USER.removeIdentificationPhotoByKey(UserRealm.IdentificationPhoto.FIRST);
            USER.setPassportPhoto1Path(null);
        } else {
            USER.removeIdentificationPhotoByKey(UserRealm.IdentificationPhoto.SECOND);
            USER.setPassportPhoto2Path(null);
        }
    }
}
