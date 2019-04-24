package com.tamaq.courier.presenters.tabs.orders.estimate_client;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface EstimateClientContract {

    interface View extends BaseView {

        void onEstimateSuccess();

    }

    interface Presenter extends BasePresenter<View> {

        OrderRealm getOrderById(String orderId);

        void estimate(String orderId, OrderRealm.EstimateType estimateType, String comment, boolean livedTips);

        void changeWorkStatusToLastActive();

    }

}
