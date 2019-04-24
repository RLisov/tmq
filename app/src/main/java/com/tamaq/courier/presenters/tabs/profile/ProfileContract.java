package com.tamaq.courier.presenters.tabs.profile;

import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface ProfileContract {

    interface View extends BaseView {

        void displayUserInformation(UserRealm userRealm);

        void onExitCompleted();

        void onCallCenterNumberLoaded(String number);
    }

    interface Presenter extends BasePresenter<View> {

        void loadUserInformation();

        void exitFromAccount();

    }

}
