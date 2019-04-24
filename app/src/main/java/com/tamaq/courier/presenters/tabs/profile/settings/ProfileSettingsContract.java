package com.tamaq.courier.presenters.tabs.profile.settings;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface ProfileSettingsContract {

    interface View extends BaseView {

        void displayUserInfo(UserRealm user);

        void initTransportSpinner(List<String> transportTypes, String selectedTransportType);

        void onChangesSaved();

        void onSmsCodeRequested(String phoneNumber);

    }

    interface Presenter extends BasePresenter<View> {

        void loadUserInformation();

        void updatePhoto(String filePath);

        void updateTransportType(String transportType);

        void updateCountry(LocationRealm country);

        void updateCity(LocationRealm city);

        void updateMobile(String mobileNumber);

        void removeUserPhotoFromServer();

        void requestSmsCode(String phoneNumber);

        void changeLoginOnServer(LocationRealm currentCountry, String currentMobileNumber, String password);
    }

}
