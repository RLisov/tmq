package com.tamaq.courier.presenters.tabs.payments.all_payments;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.PaymentDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PaymentsPresenter extends BasePresenterAbs<PaymentsContract.View>
        implements PaymentsContract.Presenter {

    private PaymentsFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public PaymentsPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(PaymentsContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (PaymentsFragment) view;
    }

    @Override
    public void checkUserState() {
        mServerCommunicator.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPojo -> getView().onUserChecked(!userPojo.isBlocked()), onError);
    }

    @Override
    public void loadPayments() {
        List<PaymentRealm> payments = new ArrayList<>(PaymentDAO.getInstance().getPayments(getRealm()));
        if (!payments.isEmpty()) getView().paymentsLoaded();

        mServerCommunicator.getPayments(0, 1000000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paymentList -> {
                    if (paymentList.isEmpty()) getView().displayEmptyView();
                    else {
                        PaymentDAO.getInstance().addPayments(paymentList, getRealm());
                        getView().paymentsLoaded();
                    }
                }, throwable -> {
                    if (getView() == null) return;
                    onError.accept(throwable);
                });
    }

    @Override
    public void getBalance() {
        double lastBalance = getAndSetBalance();

        mServerCommunicator.getUserInfo()
                .subscribe(userPojo -> {
                    UserDAO.getInstance().updateBalance(userPojo.getBalance(), getRealm());
                    if (userPojo.getBalance() != lastBalance) getAndSetBalance();
                }, onError);
    }

    private double getAndSetBalance() {
        double balance = 0;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        UserRealm user = UserDAO.getInstance().getUser(getRealm());
        if (user != null) balance = user.getBalance();
        getView().displayBalance(decimalFormat.format(balance) + getUserCurrency());
        return balance;
    }
}
