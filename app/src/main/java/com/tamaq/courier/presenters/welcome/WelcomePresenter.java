package com.tamaq.courier.presenters.welcome;


import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

public class WelcomePresenter extends BasePresenterAbs<WelcomeContract.View> implements WelcomeContract.Presenter {

    private WelcomeFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public WelcomePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(WelcomeContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (WelcomeFragment) view;
    }

}
