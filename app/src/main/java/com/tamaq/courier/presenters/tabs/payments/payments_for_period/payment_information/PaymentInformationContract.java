package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information;

import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface PaymentInformationContract {

    interface View extends BaseView {

        void displayInformation(PaymentRealm paymentRealm, OrderRealm orderRealm, UserRealm userRealm);

        void displayInformation(PaymentRealm paymentRealm, UserRealm user);
    }

    interface Presenter extends BasePresenter<View> {

        void loadInformation(String paymentId);

    }

}
