package com.tamaq.courier.presenters.login;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginPresenter extends BasePresenterAbs<LoginContract.View>
        implements LoginContract.Presenter {

    private static final UserRealm USER = UserRealm.getInstance();
    private LoginFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public LoginPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(LoginContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (LoginFragment) view;
    }

    @Override
    public void loadUserCountry() {
        if (USER.getCountry() == null) {
            loadUserCountryPart();
        } else {
            getView().onUserCountryLoaded(USER.getCountry());
        }
    }

    private void loadUserCountryPart() {
        List<LocationRealm> locationRealmList = new ArrayList<>(LocationDAO.getInstance().getCountries(getRealm()));
        if (!locationRealmList.isEmpty()) {
            setUserCountry(locationRealmList);
        } else {
            mServerCommunicator.getCountries()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setUserCountry, onError);
        }
    }

    private void setUserCountry(List<LocationRealm> list) {
        if (USER.getLastLocation() != null) {
            LocationRealm countryByName = LocationDAO.getInstance()
                    .getCountryByName(USER.getLastLocation().getCountryName(), getRealm());
            if (countryByName != null)
                USER.setCountry(countryByName.isManaged()
                        ? getRealm().copyFromRealm(countryByName)
                        : countryByName);
            else
                USER.setCountry(list.get(0).isManaged()
                        ? getRealm().copyFromRealm(list.get(0))
                        : list.get(0));
        } else {
            USER.setCountry(list.get(0).isManaged()
                    ? getRealm().copyFromRealm(list.get(0))
                    : list.get(0));
        }
        getView().onUserCountryLoaded(USER.getCountry());
    }

    @Override
    public LocationRealm loadCountryById(String countryId) {
        return LocationDAO.getInstance().getLocationByKey(countryId, getRealm());
    }

    @Override
    public void requestSmsCode(String phoneNumber) {
        getView().showCommonLoader();

        String fullPhoneNumber = phoneNumber;
        if (fullPhoneNumber.equals("+7975671978")
                || fullPhoneNumber.equals("+380663333334")
                || fullPhoneNumber.equals("+73335552288")
//                || fullPhoneNumber.equals("+380968422058")
                || fullPhoneNumber.equals("+3801112266")) { // // TODO: 26.05.17 hardcode
            getView().onSmsCodeRequested(fullPhoneNumber);
            getView().hideCommonLoader();
            return;
        }

        mServerCommunicator.registrate(phoneNumber)
                .subscribe(() -> {
                    getView().onSmsCodeRequested(phoneNumber);
                    getView().hideCommonLoader();
                }, onError);
    }
}
