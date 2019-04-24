package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list;

import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;

import java.util.List;

public interface PaymentPeriodListContract {

    interface View extends BaseView {

        void displayPaymentsList(List<PaymentSortedRealm> list);

    }

    interface Presenter extends BasePresenter<View> {

        void loadPaymentsList(int paymentGap, PaymentPeriod paymentPeriod);

    }

}
