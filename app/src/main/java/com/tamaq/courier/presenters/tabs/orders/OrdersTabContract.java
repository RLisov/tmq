package com.tamaq.courier.presenters.tabs.orders;

import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface OrdersTabContract {

    interface View extends BaseView {

        void onNeedShowCurrentOrder(String orderId);

    }

    interface Presenter extends BasePresenter<View> {

        void checkNeedShowCurrentOrder();

    }

}
