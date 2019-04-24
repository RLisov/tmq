package com.tamaq.courier.presenters.registration.terms_of_use;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

public class TermsOfUsePresenter extends BasePresenterAbs<TermsOfUseContract.View>
        implements TermsOfUseContract.Presenter {


    private TermsOfUseFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public TermsOfUsePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(TermsOfUseContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (TermsOfUseFragment) view;
    }

}
