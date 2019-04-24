package com.tamaq.courier.presenters.tabs.payments.all_payments;

import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface PaymentsContract {

    interface View extends BaseView {

        void onUserChecked(boolean active);

        void displayBalance(String balance);

        void paymentsLoaded();

        void displayEmptyView();
    }

    interface Presenter extends BasePresenter<View> {

        void checkUserState();

        void loadPayments();

        void getBalance();

    }

}
