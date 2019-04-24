package com.tamaq.courier.presenters.registration.city;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

import java.util.List;

import io.reactivex.Observable;

public class CityPresenter extends BasePresenterAbs<CityContract.View>
        implements CityContract.Presenter {

    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private CityFragment mRxFragment;
    private UserRealm mUser = UserRealm.getInstance();

    public CityPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(CityContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (CityFragment) view;
    }

    @Override
    public void loadCities(String countryKey, String selectedCityKey) {
//        List<CityRealm> cityRealmListList = LocationDAO.getInstance().getCitiesUnmanaged(getRealm());
//        if (!cityRealmListList.isEmpty()) {
//            prepareCityList(cityRealmListList, selectedCityKey);
//        } else {
        mServerCommunicator.getCitiesByCountry(countryKey)
                .map(LocationRealm::getChildes)
                .flatMapObservable(Observable::fromIterable)
                .sorted((o1, o2) -> o1.getTitleUI().compareTo(o2.getTitleUI()))
                .toList()
                .subscribe(cityList -> prepareCityList(cityList, selectedCityKey), onError);
//        }
    }

    private void prepareCityList(List<LocationRealm> cityList, String selectedCityKey) {
        if (!selectedCityKey.isEmpty()) {
            for (LocationRealm cityRealm : cityList) {
                if (cityRealm.getKey().equals(selectedCityKey)) {
                    cityRealm.setChosen(true);
                    break;
                }
            }
        }
        getView().displayCities(cityList);
    }

    public void selectCity(LocationRealm cityRealm, int position) {
        mUser.setWorkCity(cityRealm);
        getView().citySelected(position);
    }
}
