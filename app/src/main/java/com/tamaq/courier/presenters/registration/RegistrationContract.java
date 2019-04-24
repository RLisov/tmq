package com.tamaq.courier.presenters.registration;

import android.graphics.Bitmap;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public class RegistrationContract {

    interface View extends BaseView {

        void initUser(UserRealm user);

        void initTransportSpinner(List<String> transportTypes, String selectedTransportType);

        void displayCanRegister(boolean canRegister);

        void onSmsCodeRequested(String phoneNumber);
    }

    public interface Presenter extends BasePresenter<View> {

        void checkUser();

        void setUserAvatar(Bitmap avatarBitmap, String path);

        void setTransportType(String transportType);

        void loadTransportTypes();

        void checkCanRegister();

//        void registerUser();

        void setUserName(String userName);

        void setUserLastName(String userLastName);

        void setUserAge(String age);

        void setUserRnn(String rnn);

        void setUserMobile(String mobile);

        void selectCountry(LocationRealm locationRealm);

        void requestSmsCode();
    }

}
