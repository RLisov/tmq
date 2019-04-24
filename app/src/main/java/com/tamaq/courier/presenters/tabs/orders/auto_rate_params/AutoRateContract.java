package com.tamaq.courier.presenters.tabs.orders.auto_rate_params;

import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.ui.CheckableSingleTextItemWithValue;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;


public interface AutoRateContract {

    interface View extends BaseView {

        void onAutoRateSettingsUpdated();

        void onRegionsAvailable();
    }

    interface Presenter extends BasePresenter<View> {

        List<CheckableSingleTextItemWithValue> loadDeliveryTimeValues();

        List<CheckableSingleTextItemWithValue> loadKitchenPaymentValues();

        List<CheckableSingleTextItemWithValue> loadRatingValues();

        List<CheckableSingleTextItemWithValue> loadRadiuses();

        LocationRealm getRegionByKey(String regionKey);

        AutoRateSettingRealm getAutoRateSetting();

        void updateSelectedGeographyType(String type);

        void updateSelectedRegionKey(String key);

        void resetSettingsToTown(LocationRealm currentLocation);

        void updateSelectedRadius(int radius);

        void updateSelectedMinTime(int time);

        void updateMaxPayment(int payment);

        void updateMaxClientRating(int rating);

        void sendOrdersSettingsToServer();

        void checkIfSeparateRegionAvailable(String selectedRegionKey);
    }

}
