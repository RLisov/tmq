package com.tamaq.courier.presenters.tabs.orders.empty_state;

import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;


public interface OrdersContract {

    interface View extends BaseView {

        void displayTransportTypes(List<TransportTypeRealm> objects);

        void onCommonOrdersSettingsLoaded();

        void onUserChecked(boolean active);

        void onExecutingOrderStatus(boolean isExecuting);
    }

    interface Presenter extends BasePresenter<View> {

        void checkUserState();

        void loadTransportTypes();

        boolean isAutoRateDefault();

        CommonOrdersSettingRealm loadCommonOrderSettings();

        void updateWorkType(String workType);

        void updateMinPayment(int minPayment);

        void updateTransportType(TransportTypeRealm transportType);

        UserRealm getUser();

        void loadCommonOrdersSettingsFromServer();

        void checkIsExecutingOrder(boolean onServer);

        void changeWorkStatusToLastActive();
    }

}
