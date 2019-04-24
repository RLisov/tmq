package com.tamaq.courier.presenters.tabs.orders.empty_state;

import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.TransportTypeDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class OrdersPresenter extends BasePresenterAbs<OrdersContract.View>
        implements OrdersContract.Presenter {

    private OrdersFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private UserDAO userDAO = UserDAO.getInstance();

    public OrdersPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(OrdersContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (OrdersFragment) view;

//        if (!getUser().isConfirmed()) { //// TODO: 17.03.2017 Only for testing. Remove later
//            new Handler().postDelayed(() ->
//                    UserDAO.getInstance().setUserStatusConfirmed(getRealm()), 60 * 1000);
//        }
    }

    @Override
    public void checkUserState() {
        mServerCommunicator.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPojo -> getView().onUserChecked(!userPojo.isBlocked()), onError);
    }

    @Override
    public boolean isAutoRateDefault() {
        return userDAO.getAutoRateSettings(getRealm()).isDefault();
    }

    @Override
    public CommonOrdersSettingRealm loadCommonOrderSettings() {
        return userDAO.getCommonOrderSettings(getRealm());
    }

    @Override
    public void updateWorkType(String workType) {
        userDAO.updateCommonOrderSettingsWorkType(workType, getRealm());
        sendOrdersSettingsToServer();
    }

    private void sendOrdersSettingsToServer() {
        mServerCommunicator
                .updateUserInfo(UserPojo.createForUpdateCommonOrderSettings(
                        UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()),
                        getRealm()))
                .subscribe(() -> {
                }, onError);
    }

    @Override
    public void updateMinPayment(int minPayment) {
        userDAO.updateCommonOrderSettingsMinPayment(minPayment, getRealm());
        sendOrdersSettingsToServer();
    }

    @Override
    public void updateTransportType(TransportTypeRealm transportTypeRealm) {
        userDAO.updateCommonOrderSettingsTransport(transportTypeRealm.getKey(), getRealm());
        sendOrdersSettingsToServer();
    }

    @Override
    public UserRealm getUser() {
        return UserDAO.getInstance().getUser(getRealm());
    }

    @Override
    public void loadCommonOrdersSettingsFromServer() {
        mServerCommunicator
                .getUserInfo()
                .subscribe(
                        userPojo -> {
                            UserDAO.getInstance().saveUserDataFromPojo(userPojo, getRealm());
                            if (getView() != null) getView().onCommonOrdersSettingsLoaded();
                            loadTransportTypes();
                        }, onError);
    }

    @Override
    public void loadTransportTypes() {
        List<TransportTypeRealm> items = TransportTypeDAO.getInstance().getAll(getRealm());
        if (getView() != null) getView().displayTransportTypes(items);
    }

    @Override
    public void checkIsExecutingOrder(boolean onServer) {
        if (onServer) {
            mServerCommunicator.getUserInfo()
                    .subscribe(userPojo -> {
                        if (userPojo.isExecutingOrder()) {
                            UserDAO.getInstance().setCurrentOrderId(userPojo.getExecutingOrder().getId(), getRealm());
                        }
                        getView().onExecutingOrderStatus(userPojo.isExecutingOrder());
                    }, onError);
        } else {
            String currentOrderId = UserDAO.getInstance().getUser(getRealm()).getCurrentOrderId();
            getView().onExecutingOrderStatus(!TextUtils.isEmpty(currentOrderId));
        }
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
