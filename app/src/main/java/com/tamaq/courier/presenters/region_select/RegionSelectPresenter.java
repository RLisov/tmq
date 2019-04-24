package com.tamaq.courier.presenters.region_select;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;

public class RegionSelectPresenter extends BasePresenterAbs<RegionSelectContract.View>
        implements RegionSelectContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp app;
    private ServerCommunicator serverCommunicator;

    public RegionSelectPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.app = app;
        this.serverCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(RegionSelectContract.View v) {
        super.attachPresenter(v);
        mRxFragment = (RxFragment) v;
    }

    @Override
    public void loadRegions(String selectedRegionKey) {
        getView().showCommonLoader();
        if (selectedRegionKey.isEmpty()) {
            return;
        }
        LocationRealm regionRealm = LocationDAO.getInstance().getLocationByKey(
                selectedRegionKey, getRealm());
        if (regionRealm == null) {
            return;
        }
        if (regionRealm.getType().equals(LocationRealm.Type.CITIES.toString())) {
            serverCommunicator
                    .getRegionsByCity(regionRealm.getKey())
                    .map(LocationRealm::getChildes)
                    .doFinally(() -> getView().hideCommonLoader())
                    .subscribe(locationRealms -> {
                        LocationDAO.getInstance().addLocations(locationRealms, getRealm());
                        getView().onRegionsLoaded(locationRealms);
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
                            if (region.getKey().equals(regionRealm.getKey()))
                                return true;
                        }
                        return false;
                    })
                    .map(LocationRealm::getChildes)
                    .doFinally(() -> getView().hideCommonLoader())
                    .subscribe(locationRealms -> {
                        LocationDAO.getInstance().addLocations(locationRealms, getRealm());
                        getView().onRegionsLoaded(locationRealms);
                    }, onError);
        }
    }
}
