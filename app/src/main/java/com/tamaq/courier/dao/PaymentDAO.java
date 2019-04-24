package com.tamaq.courier.dao;

import com.tamaq.courier.model.api.response.PaymentPojo;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.utils.DateHelper;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class PaymentDAO {

    public static PaymentDAO getInstance() {
        return InstanceHolder.instance;
    }

    public void addPayments(List<PaymentRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public List<PaymentRealm> getPayments(Realm realm) {
        RealmResults<PaymentRealm> all = realm.where(PaymentRealm.class).findAll();
        return realm.copyFromRealm(all);
    }

    public PaymentRealm getPaymentById(String id, Realm realm) {
        return realm.where(PaymentRealm.class).equalTo(PaymentRealm.ID_ROW, id).findFirst();
    }

    public PaymentRealm fillPaymentFromPojo(PaymentRealm paymentRealm, PaymentPojo paymentPojo) {
        paymentRealm.setId(paymentPojo.id);
        paymentRealm.setDate(DateHelper.correctDateWithDifference(paymentPojo.created));
        paymentRealm.setTime(DateHelper.correctDateWithDifference(paymentPojo.created));
        paymentRealm.setType(paymentPojo.paytype);
        paymentRealm.setMoney(paymentPojo.amount);
        paymentRealm.setDetails(paymentPojo.details);
        if (paymentPojo.order != null) paymentRealm.setOrderId(paymentPojo.order.id);
        if (paymentPojo.orderNumber != 0) paymentRealm.setOrderNumber(paymentPojo.orderNumber);
        return paymentRealm;
    }

    public void addSortedPayments(List<PaymentSortedRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public List<PaymentSortedRealm> getSortedPayments(PaymentPeriod paymentPeriod, Realm realm) {
        RealmResults<PaymentSortedRealm> all = realm
                .where(PaymentSortedRealm.class)
                .equalTo(PaymentSortedRealm.PERIOD_ROW, paymentPeriod.getPeriodString())
                .findAll();
        return realm.copyFromRealm(all);
    }

    public List<PaymentSortedRealm> getSortedPayments(PaymentPeriod paymentPeriod, boolean isForChart, Realm realm) {
        RealmResults<PaymentSortedRealm> all = realm
                .where(PaymentSortedRealm.class)
                .equalTo(PaymentSortedRealm.PERIOD_ROW, paymentPeriod.getPeriodString())
                .equalTo(PaymentSortedRealm.CHART_DATA_ROW, isForChart)
                .findAll();
        return realm.copyFromRealm(all);
    }

    public List<PaymentSortedRealm> getSortedPaymentsWithTimeGap(int timeGap, PaymentPeriod paymentPeriod, Realm realm) {
        RealmResults<PaymentSortedRealm> all = realm
                .where(PaymentSortedRealm.class)
                .equalTo(PaymentSortedRealm.PERIOD_ROW, paymentPeriod.getPeriodString())
                .equalTo(PaymentSortedRealm.TIME_GAP_ROW, timeGap)
                .findAll();
        return realm.copyFromRealm(all);
    }

    private static class InstanceHolder {
        private static final PaymentDAO instance = new PaymentDAO();
    }
}
