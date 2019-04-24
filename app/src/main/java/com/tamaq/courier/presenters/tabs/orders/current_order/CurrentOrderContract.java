package com.tamaq.courier.presenters.tabs.orders.current_order;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface CurrentOrderContract {

    interface View extends BaseView {

        void displayOrder(OrderRealm orderRealm);

        void onOrderStatusChanged();

        void hideOrder();
    }

    interface Presenter extends BasePresenter<View> {

        void loadOrder(String orderId);

        void changeOrderStatus(String orderId, OrderStatus orderStatus);
    }

}
