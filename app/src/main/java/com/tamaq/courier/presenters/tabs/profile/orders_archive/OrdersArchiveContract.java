package com.tamaq.courier.presenters.tabs.profile.orders_archive;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.Date;
import java.util.List;

public interface OrdersArchiveContract {

    interface View extends BaseView {

        void displayOrders(List<OrderRealm> list);

        void displayNoOrders();

        void enableSearchIcon();

    }

    interface Presenter extends BasePresenter<View> {

        void loadOrders();

        Date getMinDate();

        void setMinDate(Date minDate);

        Date getMaxDate();

        void setMaxDate(Date maxDate);

        void clearDatePeriodSearch();

        Date getMinDateOriginal();

        Date getMaxDateOriginal();
    }
}
