package com.tamaq.courier.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tamaq.courier.events.OpenOrderTabEvent;
import com.tamaq.courier.model.api.common.ObjectWithIdAndStatus;
import com.tamaq.courier.model.api.response.NotificationPojo;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;
import com.tamaq.courier.utils.DateHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;

public class NotificationsDAO {

    public static final String EMPTY_NOTIFICATION_TYPE = "EMPTY_NOTIFICATION_TYPE";

    private List<Listener> mListener;

    public static NotificationsDAO getInstance() {
        return NotificationsDAO.InstanceHolder.instance;
    }

    public NotificationRealm getEmptyNotification() {
        class EmptyNotification extends NotificationRealm {
            @Override
            public String getType() {
                return EMPTY_NOTIFICATION_TYPE;
            }
        }
        return new EmptyNotification();
    }

    public List<NotificationRealm> addNotificationsFromPojo(List<NotificationPojo> list, Realm realm, @Nullable Context context) {
        return addNotificationsFromPojo(list, realm, context, false);
    }

    public List<NotificationRealm> addNotificationsFromPojo(List<NotificationPojo> list, Realm realm, @Nullable Context context, boolean needReadLocally) {
        checkCurrentOrderFirstTime(list, realm, context);

        int oldNotificationCount = getNotifications(realm).size();
        int newNotificationCount = list.size();

        if (!realm.isInTransaction()) realm.beginTransaction();
        List<NotificationRealm> notificationsRealmList = new ArrayList<>();
        for (NotificationPojo notificationPojo : list) {
            NotificationRealm notificationRealm = getNotificationById(notificationPojo.getId(), realm);
            if (notificationRealm == null) {
                if (!realm.isInTransaction()) realm.beginTransaction();
                notificationRealm = realm.createObject(NotificationRealm.class, notificationPojo.getId());
            }
            if (!realm.isInTransaction()) realm.beginTransaction();
            notificationsRealmList.add(fillNotificationRealmFromPojo(notificationRealm, notificationPojo, needReadLocally));
        }
        notificationsRealmList = realm.copyToRealmOrUpdate(notificationsRealmList);
        realm.commitTransaction();
        if (newNotificationCount > oldNotificationCount) {
            updateBadge(true);
            checkIfNewMessage(list, newNotificationCount - oldNotificationCount);
            checkIfOrderCanceled(list, newNotificationCount - oldNotificationCount, realm);
        }
        return notificationsRealmList;
    }

    private void checkIfOrderCanceled(List<NotificationPojo> list, int newNotificationsCount, Realm realm) {
        for (int i = 0; i < newNotificationsCount; i++) {
            ObjectWithIdAndStatus order = list.get(i).getOrder();
            if (order == null) return;
            boolean orderCanceledByCustomer = OrderStatus.fromString(order.getStatus()) == OrderStatus.CANCEL_CUSTOMER;
            String currentOrderId = UserDAO.getInstance().getUser(realm).getCurrentOrderId();
            if (TextUtils.isEmpty(currentOrderId)) return;
            boolean orderIdsEqual = currentOrderId.equals(order.getId());
            if (orderCanceledByCustomer && orderIdsEqual) OrderDAO.getInstance().cancelOrderFromClient();
        }
    }

    private void checkCurrentOrderFirstTime(List<NotificationPojo> list, Realm realm, @Nullable Context context) {
        try {
            if (context != null) {
                UserRealm user = UserDAO.getInstance().getUser(realm);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean isFirstTime = sharedPreferences.getBoolean("isFirstNotificationRequest", true);

                if (TextUtils.isEmpty(user.getCurrentOrderId()) && isFirstTime) {
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    ed.putBoolean("isFirstNotificationRequest", false);
                    ed.apply();

                    int foundNotificationIndex = -1;
                    String foundNotificationOrderId = "";
                    boolean foundOrderCompleted = false;

                    for (int i = 0; i < list.size(); i++) {
                        NotificationPojo notificationPojo = list.get(i);
                        if (notificationPojo.getType().equals(NotificationType.YOU_SELECTED_EXECUTOR.getTypeId())) {
                            foundNotificationIndex = i;
                            foundNotificationOrderId = notificationPojo.getOrder().getId();
                            break;
                        }
                    }

                    if (foundNotificationIndex != -1 && !TextUtils.isEmpty(foundNotificationOrderId)) {
                        for (int i = 0; i < foundNotificationIndex; i++) {
                            NotificationPojo notificationPojo = list.get(i);
                            if (notificationPojo.getOrder() != null
                                    && notificationPojo.getOrder().getId() != null
                                    && notificationPojo.getOrder().getId().equals(foundNotificationOrderId)) {
                                foundOrderCompleted = true;
                                break;
                            }
                        }

                        if (!foundOrderCompleted) {
                            UserDAO.getInstance().setCurrentOrderId(foundNotificationOrderId, realm);
                            EventBus.getDefault().post(new OpenOrderTabEvent());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("Merov", "checkCurrentOrderFirstTime: ", e);
        }
    }

    public List<NotificationRealm> getNotifications(Realm realm) {
        RealmResults<NotificationRealm> all = realm.where(NotificationRealm.class).findAll();
        return realm.copyFromRealm(all);
    }

    public NotificationRealm getNotificationById(String id, Realm realm) {
        return realm.where(NotificationRealm.class)
                .equalTo(NotificationRealm.NOTIFICATION_ID_ROW, id)
                .findFirst();
    }

    private NotificationRealm fillNotificationRealmFromPojo(NotificationRealm notificationRealm,
                                                            NotificationPojo notificationPojo, boolean needReadLocally) {
        if (notificationRealm.getId() == null) notificationRealm.setId(notificationPojo.getId());
        notificationRealm.setDate(DateHelper.correctDateWithDifference(notificationPojo.getCreated()));
        notificationRealm.setType(notificationPojo.getType());
//        notificationRealm.setTitle(notificationPojo.getText()); // todo is this right ? "title" not coming from API, only "text"
        notificationRealm.setDescription(notificationPojo.getText());
        notificationRealm.setReaded(notificationPojo.isRead());
        if (needReadLocally) notificationRealm.setReadLocally(true);

        if (notificationPojo.getOrder() != null && notificationPojo.getOrder().getId() != null) {
            notificationRealm.setOrderId(notificationPojo.getOrder().getId());
        }
        notificationRealm.setUserId(notificationPojo.getUser().getId());
        return notificationRealm;
    }

    private void checkIfNewMessage(List<NotificationPojo> list, int newNotificationsCount) {
        for (int i = 0; i < newNotificationsCount; i++) {
            NotificationType notificationType = NotificationType.getNotificationType(list.get(i).getType());
            switch (notificationType) {
                case CHAT:
                case NEW_MESSAGE:
                    DialogDAO.getInstance().updateBadge();
                    DialogDAO.getInstance().callListenerNewMessageReceived();
                    break;
            }
        }
    }

    public void addNotifications(List<NotificationRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public void newNotificationAdded() {
        updateBadge(true);
    }

    public Observable<NotificationRealm> findNewOrderAutorateNotification(List<NotificationRealm> notificationRealms) {
        return Observable.fromIterable(notificationRealms)
                .filter(item -> item.getType().equals(NotificationType.YOU_SELECTED_EXECUTOR.getTypeId())
                        && !item.isReaded()
                        && !item.isReadLocally());
    }

    public Observable<NotificationRealm> findNewOrderNotification(List<NotificationRealm> notificationRealms) {
        return Observable.fromIterable(notificationRealms)
                .filter(item -> item.getType().equals(NotificationType.CAN_MAKE_IT.getTypeId())
                        && !item.isReaded()
                        && !item.isReadLocally());
    }

    public Observable<NotificationRealm> findAnswerOnOrderOffer(List<NotificationRealm> notificationRealms,
                                                                String orderId) {
        return Observable.fromIterable(notificationRealms)
                .filter(item ->
                        (item.getType().equals(NotificationType.YOU_NOT_SELECTED_EXECUTOR.getTypeId())
                                || item.getType().equals(NotificationType.YOU_SELECTED_EXECUTOR.getTypeId()))
                                && item.getOrderId().equals(orderId));
    }

    public boolean checkUnreadedNotifications() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRealm> all = realm.where(NotificationRealm.class).findAll();
        for (NotificationRealm notification : all) {
            if (!notification.isReadLocally()) return true;
        }
        return false;
    }

    public void updateBadge(boolean needShowBubble) {
        if (mListener != null && !mListener.isEmpty()) {
            for (Listener listener : mListener) {
                listener.updateBadge(needShowBubble);
            }
        }
    }

    public void setListener(Listener listener) {
        if (mListener == null) mListener = new ArrayList<>();
        mListener.add(listener);
    }

    public void removeListener(Listener listener) {
        if (mListener != null && !mListener.isEmpty() && mListener.contains(listener)) {
            mListener.remove(listener);
        }
    }

    public void saveNotificationWasReadLocally(NotificationRealm notificationRealm, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        if (notificationRealm != null) notificationRealm.setReadLocally(true);
        realm.commitTransaction();
    }

    public interface Listener {
        void updateBadge(boolean needShowBubble);
    }

    private static class InstanceHolder {
        private static final NotificationsDAO instance = new NotificationsDAO();
    }
}
