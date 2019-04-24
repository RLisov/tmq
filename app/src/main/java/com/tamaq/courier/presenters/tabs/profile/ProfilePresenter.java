package com.tamaq.courier.presenters.tabs.profile;

import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;


public class ProfilePresenter extends BasePresenterAbs<ProfileContract.View>
        implements ProfileContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public ProfilePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(ProfileContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadUserInformation() {
        UserRealm user = UserDAO.getInstance().getUser(getRealm());
        if (user != null) {
            getView().displayUserInformation(user);
            loadCallCenterNumber(user);
        }
    }

    private void loadCallCenterNumber(UserRealm user) {
        if (user != null && user.getCountry() != null &&
                TextUtils.isEmpty(user.getCountry().getCallcenterPhone())) {
            mServerCommunicator.getUserInfo().subscribe(userPojo -> {
                if (!TextUtils.isEmpty(userPojo.getPhoneCallcenter())) {
                    UserDAO.getInstance().updateCallCenterPhone(getRealm(), user, userPojo.getPhoneCallcenter());
                    getView().onCallCenterNumberLoaded(user.getCountry().getCallcenterPhone());
                }
            }, onError);
        }
    }

    @Override
    public void exitFromAccount() {
        try {
            getView().showCommonLoader();
            UserRealm.getInstance().reset();
            if (!getRealm().isInTransaction()) getRealm().beginTransaction();
            getRealm().deleteAll();
            if (getRealm().isInTransaction()) getRealm().commitTransaction();
            PrefsHelper.setUserAuthorized(false, getView().getContext());
            PrefsHelper.setUserConfirmed(false, getView().getContext());
            getView().onExitCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
