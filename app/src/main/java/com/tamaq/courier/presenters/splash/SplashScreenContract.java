package com.tamaq.courier.presenters.splash;

import android.location.Address;

import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface SplashScreenContract {

    interface View extends BaseView {

        void dataLoaded(boolean isFirstTime, boolean userExist);

        void goToMainActivity();

    }

    interface Presenter extends BasePresenter<View> {

        void getLocation(Address address);

        void loadChatsAndNotifications();

        void logout();

        void loadData();

    }

}
