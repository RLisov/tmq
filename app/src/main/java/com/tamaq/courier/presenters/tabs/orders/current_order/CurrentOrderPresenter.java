package com.tamaq.courier.presenters.tabs.orders.current_order;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;

public class CurrentOrderPresenter extends BasePresenterAbs<CurrentOrderContract.View>
        implements CurrentOrderContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp app;
    private ServerCommunicator mServerCommunicator;

    public CurrentOrderPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.app = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(CurrentOrderContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadOrder(String orderId) {
        OrderRealm orderRealm = OrderDAO.getInstance().getOrderById(orderId, getRealm());
        if (orderRealm != null) getView().displayOrder(orderRealm);

//        mServerCommunicator.getOrderById(orderId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(orderPojo -> {
//                    OrderStatus orderStatus = OrderStatus.fromString(orderPojo.getStatus());
//                    if (OrderStatus.isOrderCompleted(orderStatus)) {
//                        UserDAO.getInstance().setCurrentOrderId(null, getRealm());
//                        getView().hideOrder();
//                    } else getView().onNeedShowCurrentOrder(orderPojo.getId());
//                }, onError);

        mServerCommunicator
                .getOrderById(orderId)
                .filter(orderPojo -> {
                    OrderStatus orderStatus = OrderStatus.fromString(orderPojo.getStatus());
                    if (OrderStatus.isOrderCompleted(orderStatus)) {
                        UserDAO.getInstance().setCurrentOrderId(null, getRealm());
                        getView().hideOrder();
                        return false;
                    } else return true;
                })
                .toSingle()
                .flatMap(orderPojo -> {
                    OrderDAO.getInstance().addOrUpdateOrderFromPojo(orderPojo, getRealm(), getView().getContext());
                    OrderDAO.getInstance().updateOrderWithClientInfo(orderId, orderPojo.getCustomer(), getRealm());
                    return mServerCommunicator.getServiceById(orderPojo.getService().id);
                })
                .subscribe(servicePojo -> {
                    OrderDAO.getInstance().updateOrderWithServiceInfo(orderId, servicePojo, getRealm());
                    OrderRealm orderById = OrderDAO.getInstance().getOrderById(orderId, getRealm());
                    if (getView() != null) {
                        getView().displayOrder(orderById);
                        getView().hideCommonLoader();
                    }
                }, onError);
    }

    @Override
    public void changeOrderStatus(String orderId, OrderStatus orderStatus) {
        getView().showCommonLoader();
        mServerCommunicator
                .moveOrderStatus(orderId, orderStatus)
                .subscribe(orderPojo -> {
                    OrderDAO.getInstance().addOrUpdateOrderFromPojo(
                            orderPojo, getRealm(), getView().getContext());
                    OrderDAO.getInstance().updateOrderStatus(orderId, orderStatus, getRealm());
                    getView().hideCommonLoader();
                    getView().onOrderStatusChanged();
                }, onError);
    }

}
