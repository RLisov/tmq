package com.tamaq.courier.presenters.login;

import android.support.annotation.NonNull;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface LoginContract {

    interface View extends BaseView {

        void onUserCountryLoaded(@NonNull LocationRealm locationRealm);

        void onSmsCodeRequested(String phoneNumber);
    }

    interface Presenter extends BasePresenter<View> {

        void loadUserCountry();

        LocationRealm loadCountryById(String countryId);

        void requestSmsCode(String phoneNumber);

    }

}
