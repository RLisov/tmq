package com.tamaq.courier.presenters.tabs.payments.payments_for_period;

import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;

import java.util.List;

public interface PaymentsForPeriodContract {

    interface View extends BaseView {

        void displaySortedPaymentList(List<PaymentSortedRealm> sortedPaymentList);

        void displayPaymentChart(List<PaymentSortedRealm> list);

    }

    interface Presenter extends BasePresenter<View> {

        void getPaymentsData(PaymentPeriod paymentPeriod);

    }

}
