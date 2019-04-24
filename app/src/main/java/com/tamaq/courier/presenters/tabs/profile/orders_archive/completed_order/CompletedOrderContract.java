package com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface CompletedOrderContract {

    interface View extends BaseView {

        void displayOrder(OrderRealm order, String callcenterPhone);

    }

    interface Presenter extends BasePresenter<View> {

        void loadOrder(String orderId);

    }
}
