package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.PaymentDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

public class PaymentInformationPresenter extends BasePresenterAbs<PaymentInformationContract.View>
        implements PaymentInformationContract.Presenter {

    private PaymentInformationFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public PaymentInformationPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(PaymentInformationContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (PaymentInformationFragment) view;
    }

    @Override
    public void loadInformation(String paymentId) {
        PaymentRealm paymentRealm = PaymentDAO.getInstance().getPaymentById(paymentId, getRealm());

        if (paymentRealm.getOrderId() == null) {
            getView().displayInformation(paymentRealm, UserDAO.getInstance().getUser(getRealm()));
            return;
        }

        OrderRealm orderRealm = OrderDAO.getInstance().getOrderById(paymentRealm.getOrderId(), getRealm());
        if (orderRealm != null) {
            getView().displayInformation(paymentRealm, orderRealm, UserDAO.getInstance().getUser(getRealm()));
        } else {
            getView().showCommonLoader();
            mServerCommunicator
                    .getOrderById(paymentRealm.getOrderId())
                    .compose(mRxFragment.bindToLifecycle())
                    .flatMap(orderPojo -> {
                        OrderDAO.getInstance().addOrUpdateOrderFromPojo(orderPojo, getRealm(), getView().getContext());
                        OrderDAO.getInstance().updateOrderWithClientInfo(paymentRealm.getOrderId(), orderPojo.getCustomer(), getRealm());
                        return mServerCommunicator.getServiceById(orderPojo.getService().id);
                    })
                    .subscribe(servicePojo -> {
                        OrderDAO.getInstance().updateOrderWithServiceInfo(paymentRealm.getOrderId(), servicePojo, getRealm());
                        OrderRealm order = OrderDAO.getInstance().getOrderById(paymentRealm.getOrderId(), getRealm());
                        getView().displayInformation(
                                PaymentDAO.getInstance().getPaymentById(paymentId, getRealm()),
                                order,
                                UserDAO.getInstance().getUser(getRealm()));
                        getView().hideCommonLoader();
                    }, onError);
        }
    }
}
