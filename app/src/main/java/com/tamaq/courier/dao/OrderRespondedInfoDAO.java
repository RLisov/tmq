package com.tamaq.courier.dao;


import com.tamaq.courier.model.database.OrderRespondedInfo;

import java.util.Date;

import io.realm.Realm;

public class OrderRespondedInfoDAO {

    public static OrderRespondedInfoDAO getInstance() {
        return InstanceHolder.getInstance();
    }

    public void saveRespondedOrderId(String orderId, Realm realm) {
        OrderRespondedInfo orderRespondedInfo = getNotNullOrderRespondedInfo(realm);
        if (!realm.isInTransaction()) realm.beginTransaction();
        orderRespondedInfo.setOrderId(orderId);
        realm.commitTransaction();
    }

    private OrderRespondedInfo getNotNullOrderRespondedInfo(Realm realm) {
        OrderRespondedInfo orderRespondedInfo = getOrderRespondedInfo(realm);
        if (orderRespondedInfo == null) {
            if (!realm.isInTransaction()) realm.beginTransaction();
            orderRespondedInfo = realm.createObject(OrderRespondedInfo.class);
            realm.commitTransaction();
        }
        return orderRespondedInfo;
    }

    public OrderRespondedInfo getOrderRespondedInfo(Realm realm) {
        return realm.where(OrderRespondedInfo.class).findFirst();
    }

    public void saveRespondedOrderTimeInfo(int timerTotalTime, int lastEmittedTimerValue,
                                           Realm realm) {
        OrderRespondedInfo orderRespondedInfo = getNotNullOrderRespondedInfo(realm);
        if (!realm.isInTransaction()) realm.beginTransaction();
        orderRespondedInfo.setTotalTime(timerTotalTime);
        orderRespondedInfo.setLeastTime(lastEmittedTimerValue);
        orderRespondedInfo.setDateOfUpdateTime(new Date());
        realm.commitTransaction();
    }

    public void setOrderAccepterByUser(boolean acceptedByUser, Realm realm) {
        OrderRespondedInfo orderRespondedInfo = getNotNullOrderRespondedInfo(realm);
        if (!realm.isInTransaction()) realm.beginTransaction();
        orderRespondedInfo.setAcceptedByUser(acceptedByUser);
        realm.commitTransaction();
    }

    public void clear(Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRespondedInfo orderRespondedInfo = getOrderRespondedInfo(realm);
        if (orderRespondedInfo != null) orderRespondedInfo.deleteFromRealm();
        realm.commitTransaction();
    }

    private static class InstanceHolder {
        private static OrderRespondedInfoDAO instance;

        private static OrderRespondedInfoDAO getInstance() {
            if (instance == null) instance = new OrderRespondedInfoDAO();
            return instance;
        }
    }
}
