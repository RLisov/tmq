package com.tamaq.courier.presenters.code_verification;

import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface CodeVerificationContract {

    interface View extends BaseView {

        void onCheckSmsCodeSuccess();

        void onCheckSmsCodeWrongCode();

        void enableSmsButton();

        void onRequestSmsCodeSuccess();

        void showTooManySmsAttempts();

        void disableSmsButton();

        void showRegistrationSuccess();

        void onUserInfoLoaded();

        void onCheckSmsCodeForChangeSuccess(String password);
    }

    interface Presenter extends BasePresenter<View> {

        void checkSmsCode(String code, String mobileNumber);

        void checkSmsCodeForChange(String code, String mobileNumber);

        void requestSmsCode(String phoneNumber);

        void requestSmsCodeForChange(String phoneNumber);

        void updateUserRegistrationInfoToServer();

        void loadUserInfo();
    }

}
