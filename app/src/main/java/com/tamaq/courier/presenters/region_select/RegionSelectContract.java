package com.tamaq.courier.presenters.region_select;

import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;


public interface RegionSelectContract {

    interface View extends BaseView {

        void onRegionsLoaded(List<LocationRealm> list);

    }

    interface Presenter extends BasePresenter<View> {

        void loadRegions(String selectedRegionKey);

    }

}
