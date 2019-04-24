package com.tamaq.courier.presenters.tutorial;


import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

public class TutorialPresenter extends BasePresenterAbs<TutorialContract.View>
        implements TutorialContract.Presenter {

    private TutorialFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public TutorialPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(TutorialContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (TutorialFragment) view;
    }

}
