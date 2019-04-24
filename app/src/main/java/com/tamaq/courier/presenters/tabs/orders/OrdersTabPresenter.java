package com.tamaq.courier.presenters.tabs.orders;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;


public class OrdersTabPresenter extends BasePresenterAbs<OrdersTabContract.View>
        implements OrdersTabContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public OrdersTabPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(OrdersTabContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void checkNeedShowCurrentOrder() {
        if (UserDAO.getInstance().getUser(getRealm()) != null && !PrefsHelper.isUserBlocked(getView().getContext())) {
            String currentOrderId = UserDAO.getInstance().getUser(getRealm()).getCurrentOrderId();
            if (currentOrderId != null) getView().onNeedShowCurrentOrder(currentOrderId);
        }
    }
}
