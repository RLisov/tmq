package com.tamaq.courier.presenters.tabs.orders.auto_rate_params;

import com.tamaq.courier.R;
import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.ui.CheckableSingleTextItemWithValue;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class AutoRatePresenter extends BasePresenterAbs<AutoRateContract.View>
        implements AutoRateContract.Presenter {

    private AutoRateFragment mRxFragment;
    private TamaqApp app;
    private ServerCommunicator serverCommunicator;

    public AutoRatePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.app = app;
        this.serverCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(AutoRateContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (AutoRateFragment) view;
    }

    @Override
    public List<CheckableSingleTextItemWithValue> loadDeliveryTimeValues() {
        List<CheckableSingleTextItemWithValue> values = new ArrayList<>();

        for (int minutes = 20; minutes <= 120; minutes += 10) {
            values.add(new CheckableSingleTextItemWithValue(
                    app.getString(R.string.minutes_count, minutes),
                    minutes,
                    String.valueOf(minutes)));
        }
        return values;
    }

    @Override
    public List<CheckableSingleTextItemWithValue> loadKitchenPaymentValues() {
        List<CheckableSingleTextItemWithValue> values = new ArrayList<>();

        int price = 100;
        while (price <= 15000) {
            values.add(new CheckableSingleTextItemWithValue(String.format("%d %s", price, getUserCurrency()),
                    price, String.valueOf(price)));
            if (price < 1000)
                price += 100;
            else if (price < 2000)
                price += 250;
            else price += 1000;
        }
        return values;
    }

    @Override
    public List<CheckableSingleTextItemWithValue> loadRatingValues() {
        List<CheckableSingleTextItemWithValue> values = new ArrayList<>();

        for (int rating = 10; rating <= 100; rating += 10) {
            values.add(new CheckableSingleTextItemWithValue(String.format("%d %s", rating, "%"),
                    rating, String.valueOf(rating)));
        }
        return values;
    }

    @Override
    public List<CheckableSingleTextItemWithValue> loadRadiuses() {
        List<CheckableSingleTextItemWithValue> values = new ArrayList<>();

        for (int kilometers = 2; kilometers <= 20; kilometers++) {
            values.add(new CheckableSingleTextItemWithValue(app.getString(R.string.kilometers_value, kilometers),
                    kilometers, String.valueOf(kilometers)));
        }
        return values;
    }

    @Override
    public LocationRealm getRegionByKey(String regionKey) {
        return LocationDAO.getInstance().getLocationByKey(regionKey, getRealm());
    }

    @Override
    public AutoRateSettingRealm getAutoRateSetting() {
        return UserDAO.getInstance().getAutoRateSettings(getRealm());
    }

    @Override
    public void updateSelectedGeographyType(String type) {
        UserDAO.getInstance().updateAutoRateGeographyType(type, getRealm());
    }

    @Override
    public void updateSelectedRegionKey(String key) {
        UserDAO.getInstance().updateAutoRateRegion(key, getRealm());
    }

    @Override
    public void resetSettingsToTown(LocationRealm currentLocation) {
        if (currentLocation.getType().equals("districts")) {
            String userCountryKey = UserDAO.getInstance().getUser(getRealm()).getCountry().getKey();
            LocationRealm userCountryRealm = LocationDAO.getInstance().getLocationByKey(userCountryKey, getRealm());

            RealmList<LocationRealm> cities = userCountryRealm.getChildes();

            forCities:
            for (LocationRealm city : cities) {
                RealmList<LocationRealm> regions = city.getChildes();

                if (regions != null && !regions.isEmpty()) {
                    for (LocationRealm region : regions) {
                        if (region.getKey().equals(currentLocation.getKey())) {
                            UserDAO.getInstance().updateCity(city, getRealm());
                            UserDAO.getInstance().updateAutoRateRegion(city.getKey(), getRealm());
                            break forCities;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateSelectedRadius(int radius) {
        UserDAO.getInstance().updateAutoRateRadius(radius, getRealm());
    }

    @Override
    public void updateSelectedMinTime(int time) {
        UserDAO.getInstance().updateAutoRateMinTime(time, getRealm());
    }

    @Override
    public void updateMaxPayment(int payment) {
        UserDAO.getInstance().updateAutoRateMaxPayment(payment, getRealm());
    }

    @Override
    public void updateMaxClientRating(int minRating) {
        UserDAO.getInstance().updateAutoRateMinRating(minRating, getRealm());
    }

    @Override
    public void sendOrdersSettingsToServer() {
        getView().showCommonLoader();
        serverCommunicator
                .updateUserInfo(UserPojo.createForUpdateAutoRateParams(
                        UserDAO.getInstance().getAutoRateSettingsUnmanaged(getRealm()),
                        getRealm()))
                .doFinally(getView()::hideCommonLoader)
                .subscribe(() -> getView().onAutoRateSettingsUpdated(), onError);
    }

    @Override
    public void checkIfSeparateRegionAvailable(String selectedRegionKey) {
        getView().showCommonLoader();
        if (selectedRegionKey.isEmpty()) return;

        LocationRealm regionRealm = LocationDAO.getInstance().getLocationByKey(selectedRegionKey, getRealm());
        if (regionRealm == null) return;

        if (regionRealm.getType().equals(LocationRealm.Type.CITIES.toString())) {
            serverCommunicator
                    .getRegionsByCity(regionRealm.getKey())
                    .map(LocationRealm::getChildes)
                    .doFinally(() -> getView().hideCommonLoader())
                    .subscribe(locationRealms -> {
                        if (locationRealms.size() != 0) {
                            LocationDAO.getInstance().addLocations(locationRealms, getRealm());
                            getView().onRegionsAvailable();
                        }
                    }, onError);
            return;
        }

        if (regionRealm.getType().equals(LocationRealm.Type.DISTRICTS.toString())) {
            serverCommunicator
                    .getCountries()
                    .toObservable()
                    .flatMapIterable(countries -> countries)
                    .filter(country ->
                            country.getChildes() != null && country.getChildes().size() > 0)
                    .map(LocationRealm::getChildes)
                    .flatMapIterable(cities -> cities)
                    .filter(city ->
                            city.getChildes() != null && city.getChildes().size() > 0)
                    .filter(city -> {
                        for (LocationRealm region : city.getChildes()) {
                            if (region.getKey().equals(regionRealm.getKey())) return true;
                        }
                        return false;
                    })
                    .map(LocationRealm::getChildes)
                    .doFinally(() -> getView().hideCommonLoader())
                    .subscribe(locationRealms -> {
                        if (locationRealms.size() != 0) {
                            LocationDAO.getInstance().addLocations(locationRealms, getRealm());
                            getView().onRegionsAvailable();
                        }
                    }, onError);
        }
    }
}
