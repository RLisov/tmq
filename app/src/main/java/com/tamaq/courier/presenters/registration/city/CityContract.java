package com.tamaq.courier.presenters.registration.city;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface CityContract {

    interface View extends BaseView {

        void displayCities(List<LocationRealm> list);

        void citySelected(int positionInList);

    }

    interface Presenter extends BasePresenter<View> {

        void loadCities(String countryKey, String selectedCityKey);

        void selectCity(LocationRealm cityRealm, int position);

    }

}
