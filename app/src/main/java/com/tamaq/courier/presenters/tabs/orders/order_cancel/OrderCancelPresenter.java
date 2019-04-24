package com.tamaq.courier.presenters.tabs.orders.order_cancel;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.List;

public class OrderCancelPresenter extends BasePresenterAbs<OrderCancelContract.View>
        implements OrderCancelContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp app;
    private ServerCommunicator serverCommunicator;

    public OrderCancelPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.app = app;
        this.serverCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(OrderCancelContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void cancelOrder(String orderId, List<OrderRealm.CancelReason> checkedReasonsList,
                            String reason) {
        getView().showCommonLoader();
        serverCommunicator
                .moveOrderStatus(orderId, OrderStatus.CANCEL_EXECUTOR, reason, checkedReasonsList)
                .subscribe(orderPojo -> {
                    getView().onCancelOrderSuccess();
                    getView().hideCommonLoader();
                    UserDAO.getInstance().setCurrentOrderId(null, getRealm());
                }, onError);
    }
}
