package com.tamaq.courier.presenters.tabs.orders.order_cancel;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;


public interface OrderCancelContract {

    interface View extends BaseView {

        void onCancelOrderSuccess();

    }

    interface Presenter extends BasePresenter<View> {

        void cancelOrder(String orderId, List<OrderRealm.CancelReason> checkedReasonsList,
                         String reason);

    }

}
