package com.tamaq.courier.presenters.tabs.orders.estimate_client;

import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;


public class EstimateClientPresenter extends BasePresenterAbs<EstimateClientContract.View>
        implements EstimateClientContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private String mLastWorkType;

    public EstimateClientPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(EstimateClientContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }


    @Override
    public OrderRealm getOrderById(String orderId) {
        return OrderDAO.getInstance().getOrderById(orderId, getRealm());
    }

    @Override
    public void estimate(String orderId, OrderRealm.EstimateType estimateType, String comment,
                         boolean livedTips) {
        getView().showCommonLoader();
        mServerCommunicator
                .rateClient(orderId, comment, estimateType.isPositive(), livedTips)
                .subscribe(estimatePojo -> {
                    OrderDAO.getInstance().updateOrderEstimate(
                            orderId, estimateType, comment, livedTips, getRealm());
                    getView().onEstimateSuccess();
                    getView().hideCommonLoader();
                }, onError);

    }

    @Override
    public void changeWorkStatusToLastActive() {
        String lastWorkType = PrefsHelper.getLastWorkType(getView().getContext());
        if (!TextUtils.isEmpty(lastWorkType)) {
            UserDAO.getInstance().updateCommonOrderSettingsWorkType(lastWorkType, getRealm());

            mServerCommunicator
                    .updateUserInfo(UserPojo.createForUpdateCommonOrderSettings(
                            UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()), getRealm()))
                    .subscribe(() -> {
                    }, onError);
        }
    }
}
