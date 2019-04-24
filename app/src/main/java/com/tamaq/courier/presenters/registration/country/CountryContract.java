package com.tamaq.courier.presenters.registration.country;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface CountryContract {

    interface View extends BaseView {

        void displayCountries(List<LocationRealm> locationRealmList);

    }

    interface Presenter extends BasePresenter<View> {

        void loadCountries(String selectedCountryId);


    }

}
