package com.tamaq.courier.presenters.tabs.payments.payments_for_period;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.PaymentDAO;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.shared.TamaqApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentParser.prepareChartDataForAllTime;
import static com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentParser.prepareDataForCalendarPeriod;
import static com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentParser.prepareListDataForAllTime;
import static com.tamaq.courier.utils.DateHelper.parseDateFromString;

public class PaymentsForPeriodPresenter extends BasePresenterAbs<PaymentsForPeriodContract.View>
        implements PaymentsForPeriodContract.Presenter {

    private PaymentsForPeriodFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    private PaymentPeriod mPaymentPeriod;
    private int mCalendarPeriod;

    public PaymentsForPeriodPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(PaymentsForPeriodContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (PaymentsForPeriodFragment) view;
    }

    @Override
    public void getPaymentsData(PaymentPeriod paymentPeriod) {
        mPaymentPeriod = paymentPeriod;
        mCalendarPeriod = Calendar.YEAR;
        switch (mPaymentPeriod) {
            case MONTH:
                mCalendarPeriod = Calendar.MONTH;
                break;
            case WEEK:
                mCalendarPeriod = Calendar.WEEK_OF_YEAR;
                break;
        }

        if (mPaymentPeriod == PaymentPeriod.ALL_TIME) {
            List<PaymentSortedRealm> sortedPaymentsChart =
                    PaymentDAO.getInstance().getSortedPayments(paymentPeriod, true, getRealm());

            List<PaymentSortedRealm> sortedPaymentsList =
                    PaymentDAO.getInstance().getSortedPayments(paymentPeriod, false, getRealm());

            if (!sortedPaymentsChart.isEmpty()) getView().displayPaymentChart(sortedPaymentsChart);
            if (!sortedPaymentsList.isEmpty()) reverseList(sortedPaymentsList);

            List<PaymentRealm> payments = new ArrayList<>(PaymentDAO.getInstance().getPayments(getRealm()));
            sortListByDate(payments);

        } else {
            List<PaymentSortedRealm> sortedPayments =
                    PaymentDAO.getInstance().getSortedPayments(paymentPeriod, getRealm());

            if (!sortedPayments.isEmpty()) {
                getView().displayPaymentChart(sortedPayments);
                reverseList(sortedPayments);
            }

            List<PaymentRealm> payments = new ArrayList<>(PaymentDAO.getInstance().getPayments(getRealm()));
            sortListByDate(payments);
        }
    }

    private void reverseList(List<PaymentSortedRealm> sortedPayments) {
        Observable.fromIterable(sortedPayments)
                .toList()
                .map(sortedPaymentsList -> {
                    Collections.reverse(sortedPaymentsList);
                    return sortedPaymentsList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sortedPaymentsList -> getView().displaySortedPaymentList(sortedPaymentsList));
    }

    private void sortListByDate(List<PaymentRealm> paymentsList) {
        Observable.fromIterable(paymentsList)
                .toSortedList((o1, o2) -> {
                    Date firstDate = parseDateFromString(o1.getFullDate());
                    Date secondDate = parseDateFromString(o2.getFullDate());
                    return firstDate.compareTo(secondDate);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::choosePeriodFromList);
    }

    private void choosePeriodFromList(List<PaymentRealm> paymentsList) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        Calendar paymentCalendar = Calendar.getInstance();
        paymentCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        int currentYear = calendar.get(Calendar.YEAR);

        switch (mPaymentPeriod) {
            case YEAR:
            case ALL_TIME:
                preparePaymentChartData(paymentsList);
                preparePaymentPaymentList(paymentsList);
                break;
            default:
                Observable.fromIterable(paymentsList)
                        .filter(currentPayment -> {
                            Date paymentDate = parseDateFromString(currentPayment.getFullDate());
                            paymentCalendar.setTime(paymentDate);
                            int paymentYear = paymentCalendar.get(Calendar.YEAR);
                            return currentYear == paymentYear;
                        })
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(paymentRealmList -> {
                            preparePaymentChartData(paymentRealmList);
                            preparePaymentPaymentList(paymentRealmList);
                        });
                break;
        }
    }

    private void preparePaymentChartData(List<PaymentRealm> paymentsList) {
        final int finalCalendarPeriod = mCalendarPeriod;
        final PaymentPeriod finalPaymentPeriod = mPaymentPeriod;
        Observable.fromIterable(paymentsList)
                .toList()
                .map(paymentRealmList -> finalPaymentPeriod == PaymentPeriod.ALL_TIME
                        ? prepareChartDataForAllTime(paymentRealmList, finalPaymentPeriod)
                        : prepareDataForCalendarPeriod(paymentRealmList, finalCalendarPeriod, finalPaymentPeriod))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sortedPayments -> {
                    PaymentDAO.getInstance().addSortedPayments(sortedPayments, getRealm());
                    List<PaymentSortedRealm> paymentsRealm = mPaymentPeriod == PaymentPeriod.ALL_TIME
                            ? PaymentDAO.getInstance().getSortedPayments(finalPaymentPeriod, true, getRealm())
                            : PaymentDAO.getInstance().getSortedPayments(finalPaymentPeriod, getRealm());
                    getView().displayPaymentChart(paymentsRealm);
                });
    }

    private void preparePaymentPaymentList(List<PaymentRealm> paymentsList) {
        final int finalCalendarPeriod = mCalendarPeriod;
        final PaymentPeriod finalPaymentPeriod = mPaymentPeriod;
        Observable.fromIterable(paymentsList)
                .toList()
                .map(paymentRealmList -> finalPaymentPeriod == PaymentPeriod.ALL_TIME
                        ? prepareListDataForAllTime(paymentRealmList, finalPaymentPeriod, getView().getContext())
                        : prepareDataForCalendarPeriod(paymentRealmList, finalCalendarPeriod, finalPaymentPeriod))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sortedPayments -> {
                    PaymentDAO.getInstance().addSortedPayments(sortedPayments, getRealm());
                    List<PaymentSortedRealm> paymentsRealm = mPaymentPeriod == PaymentPeriod.ALL_TIME
                            ? PaymentDAO.getInstance().getSortedPayments(finalPaymentPeriod, false, getRealm())
                            : PaymentDAO.getInstance().getSortedPayments(finalPaymentPeriod, getRealm());
                    reverseList(paymentsRealm);
                });
    }

}
