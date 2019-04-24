package com.tamaq.courier.presenters.registration.country;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CountryPresenter extends BasePresenterAbs<CountryContract.View>
        implements CountryContract.Presenter {

    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private CountryFragment mRxFragment;

    public CountryPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(CountryContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (CountryFragment) view;
    }

    @Override
    public void loadCountries(String selectedCountryKey) {
        List<LocationRealm> locationRealmList = new ArrayList<>(LocationDAO.getInstance().getCountries(getRealm()));
        if (!locationRealmList.isEmpty()) {
            prepareCountriesForDisplaying(locationRealmList, selectedCountryKey);
        } else {
            mServerCommunicator.getCountries()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(countryList -> {
                        LocationDAO.getInstance().addLocations(countryList, getRealm());
                        prepareCountriesForDisplaying(countryList, selectedCountryKey);
                    }, onError);
        }
    }

    private void prepareCountriesForDisplaying(List<LocationRealm> countryList, String selectedCountryKey) {
        for (LocationRealm locationRealm : countryList) {
            if (locationRealm.getKey().equals(selectedCountryKey))
                locationRealm.setChosen(true);
        }
        getView().displayCountries(countryList);
    }
}
