package com.tamaq.courier.dao;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tamaq.courier.model.api.common.OrderPojo;
import com.tamaq.courier.model.api.common.ServicePojo;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.api.response.EstimatePojo;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.model.database.StatisticItemRealm;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.functions.Action0;

public class OrderDAO {

    private List<Action0> onOrderCanceledFromClientListeners = new ArrayList<>();

    public static OrderDAO getInstance() {
        return InstanceHolder.instance;
    }

    public void addOrders(List<OrderRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public void addOrder(OrderRealm orderRealm, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(orderRealm);
        realm.commitTransaction();
    }

    public OrderRealm addOrUpdateOrderFromPojo(OrderPojo orderPojo, Realm realm, Context context) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRealm orderRealm = getOrderById(orderPojo.getId(), realm);
        if (orderRealm == null) {
            orderRealm = realm.createObject(OrderRealm.class, orderPojo.getId());
        }
        fillFromPojo(orderRealm, orderPojo, context, realm);
        realm.commitTransaction();
        return orderRealm;
    }

    public OrderRealm getOrderById(String orderId, Realm realm) {
        return realm.where(OrderRealm.class)
                .equalTo(OrderRealm.ID_ROW, orderId)
                .findFirst();
    }

    private OrderRealm fillFromPojo(OrderRealm orderRealm, OrderPojo orderPojo,
                                    Context context, Realm realm) {
        try {
            if (orderRealm.getOrderId() == null) orderRealm.setOrderId(orderPojo.getId());
            orderRealm.setCreatedDate(DateHelper.correctDateWithDifference(orderPojo.getCreated()));
            orderRealm.setOrderStatus(orderPojo.getStatus());
            orderRealm.setRequestedTime(
                    DateHelper.convertStringDateFromServerToLocalTimeZone(orderPojo.getToTime()));
//        orderRealm.setTimeArriveToClient(orderPojo.mToTime);
            orderRealm.setRestaurantName("");
            orderRealm.setRestaurantPhone("");
            orderRealm.setClientRating(getClientRatingFromOrderPojo(orderPojo));
            orderRealm.setNeedChangeFrom(orderPojo.getNeedChangeFrom());
            orderRealm.setNumber(orderPojo.getNumber());

            if (orderPojo.getServiceAddress() != null) {
                orderRealm.setRestaurantAddress(orderPojo.getServiceAddress().getFormattedAddress(context));
                orderRealm.setRestaurantAddressLat(orderPojo.getServiceAddress().latitude);
                orderRealm.setRestaurantAddressLng(orderPojo.getServiceAddress().longitude);
            }

            if (orderPojo.getAddress() != null) {
                orderRealm.setClientAddress(orderPojo.getAddress().getFormattedAddress(context));
                orderRealm.setDeliveryAddressLat(orderPojo.getAddress().latitude);
                orderRealm.setDeliveryAddressLng(orderPojo.getAddress().longitude);

                if (orderPojo.getCustomer() != null && orderPojo.getCustomer().getLastLocation() != null) {
                    orderRealm.setClientAddressLat(orderPojo.getCustomer().getLastLocation().latitude);
                    orderRealm.setClientAddressLng(orderPojo.getCustomer().getLastLocation().longitude);
                }
            }
            if (orderPojo.getAddress() != null && orderPojo.getServiceAddress() != null) {
                orderRealm.setAddressForArchive(String.format(
                        "%s - %s",
                        orderPojo.getAddress().getFormattedAddressForArchive(context),
                        orderPojo.getServiceAddress().getFormattedAddressForArchive(context)));
            }

            orderRealm.setProfitForDelivery(orderPojo.getEearn());
            orderRealm.setClientNeedToPay(orderPojo.getSum() + orderPojo.getEsum() - orderPojo.getPayBonus());
            orderRealm.setNeedChangeFrom(orderPojo.getNeedChangeFrom());

            if (orderRealm.getStatisticList() == null) {
                orderRealm.setStatisticList(new RealmList<>());
            }
            for (OrderPojo.StatisticsPojo statisticsPojo : orderPojo.statistics) {
                StatisticItemRealm statisticItemRealm = getStatisticsItemById(statisticsPojo.id, realm);
                if (statisticItemRealm == null) {
                    statisticItemRealm = realm.createObject(StatisticItemRealm.class, statisticsPojo.id);
                    orderRealm.getStatisticList().add(fillFromStatisticsPojo(statisticItemRealm, statisticsPojo));
                } else {
                    fillFromStatisticsPojo(statisticItemRealm, statisticsPojo);
                }

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return orderRealm;
    }

    @Nullable
    private Integer getClientRatingFromOrderPojo(OrderPojo orderPojo) {
        if (orderPojo.getRates() == null) return null;
        for (EstimatePojo estimatePojo : orderPojo.getRates()) {
            if (estimatePojo.typerate.key.equals("rate_client")) {
                return estimatePojo.valuation
                        ? OrderRealm.EstimateType.ESTIMATE.ordinal()
                        : OrderRealm.EstimateType.COMPLAINT.ordinal();
            }
        }
        return null;
    }

    public StatisticItemRealm getStatisticsItemById(String id, Realm realm) {
        return realm.where(StatisticItemRealm.class)
                .equalTo(StatisticItemRealm.ID_ROW, id)
                .findFirst();
    }

    private StatisticItemRealm fillFromStatisticsPojo(StatisticItemRealm statisticItemRealm,
                                                      OrderPojo.StatisticsPojo statisticsPojo) {
        statisticItemRealm.setCreated(
                DateHelper.convertStringDateFromServerToLocalTimeZone(statisticsPojo.created));
        statisticItemRealm.setMoveFrom(statisticsPojo.moveFrom);
        statisticItemRealm.setMoveTo(statisticsPojo.moveTo);
        return statisticItemRealm;
    }

    public void updateOrderWithServiceInfo(String orderId, ServicePojo servicePojo, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRealm orderRealm = getOrderById(orderId, realm);
        fillFromServiceInfo(orderRealm, servicePojo);
        realm.commitTransaction();
    }

    private void fillFromServiceInfo(OrderRealm orderRealm, ServicePojo servicePojo) {
        orderRealm.setRestaurantName(servicePojo.name);
        orderRealm.setRestaurantPhone(servicePojo.phone);
        orderRealm.setProvideTime(servicePojo.providetime);
    }

    public OrderRealm updateOrderWithClientInfo(String orderId, UserPojo userPojo,
                                                Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRealm orderRealm = getOrderById(orderId, realm);
        Log.d("Merov", "orderID: " + orderId);
        fillFromClientInfo(orderRealm, userPojo);
        realm.commitTransaction();
        return orderRealm;
    }

    private void fillFromClientInfo(OrderRealm orderRealm, UserPojo userPojo) {
        if (userPojo != null) {
            orderRealm.setClientName(userPojo.name);
            orderRealm.setClientPhone(userPojo.phone);
            orderRealm.setDispatcherPhone(userPojo.phoneCallcenter);
        }
    }

    public List<OrderRealm> setOrdersFromPojo(List<OrderPojo> orderPojoList, Realm realm, Context context) {

        if (!realm.isInTransaction()) realm.beginTransaction();

        RealmResults<OrderRealm> archiveOrdersRealm = OrderDAO.getInstance().getRealmResultAllOrders(realm);
        archiveOrdersRealm.deleteAllFromRealm();

        for (OrderPojo orderPojo : orderPojoList) {
            if (orderPojo.getStatistics() == null || orderPojo.getStatistics().isEmpty()) continue;

            for (OrderPojo.StatisticsPojo statisticsPojo : orderPojo.getStatistics()) {

                if (statisticsPojo.getMoveTo() != null && (statisticsPojo.getMoveTo().equals(OrderStatus.PUBLISHED.getValue())
                        || statisticsPojo.getMoveTo().equals(OrderStatus.EXECUTOR_SELECTED.getValue()))) {

                    try {
                        OrderRealm orderRealm = realm.createObject(OrderRealm.class, orderPojo.getId());
                        orderRealm = fillFromPojo(orderRealm, orderPojo, context, realm);
                        archiveOrdersRealm.add(orderRealm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
        realm.commitTransaction();
        return archiveOrdersRealm;
    }

    public List<OrderRealm> getAllOrders(Realm realm) {
        return realm.where(OrderRealm.class).findAll();
    }

    public RealmResults<OrderRealm> getRealmResultAllOrders(Realm realm) {
        return realm.where(OrderRealm.class).findAll();
    }

    public void updateOrderStatus(String orderId, OrderStatus orderStatus, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRealm orderRealm = getOrderById(orderId, realm);
        orderRealm.setOrderStatus(orderStatus.getValue());
        if (!OrderStatus.isActive(orderStatus) &&
                UserDAO.getInstance().getUser(realm).getCurrentOrderId().equals(orderId)) {
            UserDAO.getInstance().setCurrentOrderId(null, realm, false);
        }
        realm.commitTransaction();
    }

    public void updateOrderEstimate(String orderId, OrderRealm.EstimateType estimateType,
                                    String comment,
                                    boolean livedTips,
                                    Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        OrderRealm orderRealm = getOrderById(orderId, realm);
        orderRealm.setReviewType(estimateType.ordinal());
        orderRealm.setReviewComment(comment);
        orderRealm.setLivedTips(livedTips);
        orderRealm.setClientRating(estimateType.ordinal());
        realm.commitTransaction();
    }

    public void cancelOrderFromClient() {
        Realm realm = Realm.getDefaultInstance();
        if (!realm.isInTransaction()) realm.beginTransaction();
        UserDAO.getInstance().setCurrentOrderId(null, realm, false);
        realm.commitTransaction();
        callOrderCanceledFromClientListeners();
    }

    private void callOrderCanceledFromClientListeners() {
        if (onOrderCanceledFromClientListeners != null && !onOrderCanceledFromClientListeners.isEmpty()) {
            for (Action0 action0 : onOrderCanceledFromClientListeners) {
                action0.call();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public OrderRealm getLastCompletedOrder(Realm realm) {
        return Observable.fromIterable(realm.where(OrderRealm.class)
                .equalTo(OrderRealm.STATISTICS_ROW + "." + StatisticItemRealm.MOVE_TO_ROW,
                        OrderStatus.COMPLETE.getValue())
                .findAll())
                .filter(orderRealm -> orderRealm.getCompletedStatusTime() != null)
                .sorted((first, second) ->
                        first != null && second != null && first.getCompletedStatusTime() != null
                                ? second.getCompletedStatusTime().compareTo(first.getCompletedStatusTime())
                                : 0)
                .firstElement()
                .blockingGet();
    }

    public void addOrderCanceledFromClientListener(Action0 action0) {
        onOrderCanceledFromClientListeners.add(action0);
    }

    public void removeOrderCanceledFromClientListener(Action0 action0) {
        onOrderCanceledFromClientListeners.remove(action0);
    }

    private static class InstanceHolder {
        private static final OrderDAO instance = new OrderDAO();
    }
}
