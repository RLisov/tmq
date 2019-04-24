package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.PaymentDAO;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.shared.TamaqApp;

import java.util.Calendar;
import java.util.List;

import static com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentParser.prepareDataForCalendarPeriod;
import static com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentParser.prepareListDataForAllTime;

public class PaymentPeriodListPresenter extends BasePresenterAbs<PaymentPeriodListContract.View>
        implements PaymentPeriodListContract.Presenter {

    private PaymentPeriodListFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public PaymentPeriodListPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(PaymentPeriodListContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (PaymentPeriodListFragment) view;
    }


    @Override
    public void loadPaymentsList(int timeGap, PaymentPeriod paymentPeriod) {
        List<PaymentSortedRealm> sortedPaymentsWithTimeGap =
                PaymentDAO.getInstance().getSortedPaymentsWithTimeGap(timeGap, paymentPeriod, getRealm());

        List<PaymentSortedRealm> paymentSortedRealms;
        switch (paymentPeriod) {
            case YEAR:
                paymentPeriod = PaymentPeriod.MONTH;
                paymentSortedRealms = prepareDataForCalendarPeriod(sortedPaymentsWithTimeGap.get(0).getPaymentList(),
                        Calendar.MONTH,
                        paymentPeriod);
                break;

            case MONTH:
                paymentPeriod = PaymentPeriod.WEEK;
                paymentSortedRealms = prepareDataForCalendarPeriod(
                        sortedPaymentsWithTimeGap.get(0).getPaymentList(),
                        Calendar.WEEK_OF_YEAR,
                        paymentPeriod);
                break;

            default:
                paymentSortedRealms = prepareListDataForAllTime(
                        sortedPaymentsWithTimeGap.get(0).getPaymentList(),
                        paymentPeriod, getView().getContext());
                break;
        }
        getView().displayPaymentsList(paymentSortedRealms);
    }

}
