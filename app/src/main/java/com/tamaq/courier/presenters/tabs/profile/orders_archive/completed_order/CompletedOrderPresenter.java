package com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;


public class CompletedOrderPresenter extends BasePresenterAbs<CompletedOrderContract.View>
        implements CompletedOrderContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public CompletedOrderPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(CompletedOrderContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadOrder(String orderId) {
        OrderRealm orderById = OrderDAO.getInstance().getOrderById(orderId, getRealm());
        prepareOrderToDisplay(orderById);

        getView().showCommonLoader();
        mServerCommunicator
                .getOrderById(orderId)
                .flatMap(orderPojo -> {
                    OrderDAO.getInstance().addOrUpdateOrderFromPojo(orderPojo, getRealm(), getView().getContext());
                    OrderDAO.getInstance().updateOrderWithClientInfo(orderId, orderPojo.getCustomer(), getRealm());
                    return mServerCommunicator.getServiceById(orderPojo.getService().id);
                })
                .subscribe(servicePojo -> {
                    OrderDAO.getInstance().updateOrderWithServiceInfo(orderId, servicePojo, getRealm());
                    OrderRealm order = OrderDAO.getInstance().getOrderById(orderId, getRealm());
                    if (getView() != null) {
                        prepareOrderToDisplay(order);
                        getView().hideCommonLoader();
                    }
                }, onError);

    }

    private void prepareOrderToDisplay(OrderRealm order) {
        if (order != null) {
            String userCallcenterPhone = UserDAO.getInstance().getUser(getRealm()).getCountry().getCallcenterPhone();
            String callcenterPhone = order.getDispatcherPhone() != null
                    ? order.getDispatcherPhone().replace(" ", "")
                    : userCallcenterPhone;

            getView().displayOrder(order, callcenterPhone);
        }
    }
}
