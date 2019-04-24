package com.tamaq.courier.presenters.tabs.notifications.notification_details;

import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class NotificationDetailsPresenter extends BasePresenterAbs<NotificationDetailsContract.View>
        implements NotificationDetailsContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public NotificationDetailsPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(NotificationDetailsContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadNotification(String id) {
        NotificationRealm notificationById = NotificationsDAO.getInstance().getNotificationById(id, getRealm());
        NotificationsDAO.getInstance().saveNotificationWasReadLocally(notificationById, getRealm());

        NotificationType notificationType = NotificationType.getNotificationType(notificationById.getType());
        getView().displayNotification(notificationById, notificationType);

        loadOrderInfo(notificationById.getOrderId(), notificationType);
    }

    private void loadOrderInfo(String orderId, NotificationType notificationType) {
        if (TextUtils.isEmpty(orderId)) return;
        OrderRealm orderById = OrderDAO.getInstance().getOrderById(orderId, getRealm());
        if (orderById != null) checkIfOrderCompleted(orderById, notificationType);
        else {
            mServerCommunicator.getOrderById(orderId)
                    .map(orderPojo -> OrderDAO.getInstance()
                            .addOrUpdateOrderFromPojo(orderPojo, getRealm(), getView().getContext()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(order -> checkIfOrderCompleted(order, notificationType), onError);
        }
    }

    private void checkIfOrderCompleted(OrderRealm order, NotificationType notificationType) {
        boolean orderCompleted = OrderStatus.isOrderCompleted(OrderStatus.fromString(order.getOrderStatus()));

//        if (!orderCompleted && TextUtils.isEmpty(UserDAO.getInstance().getUser(getRealm()).getCurrentOrderId()))  { // not sure we need it. Waiting for answer
//            if (!getRealm().isInTransaction()) getRealm().beginTransaction();
//            UserDAO.getInstance().getUser(getRealm()).setCurrentOrderId(order.getOrderId());
//            getRealm().commitTransaction();
//        }

        boolean needShowOrderButton = notificationType != NotificationType.SIMPLE
                && notificationType != NotificationType.YOU_NOT_SELECTED_EXECUTOR;

        DialogRealm dialogByChatId = DialogDAO.getInstance().getDialogByChatId(order.getOrderId());
        boolean needShowChatButton = dialogByChatId != null
                && checkIfChatNotOlder24Hours(dialogByChatId)
                && (notificationType == NotificationType.NEW_MESSAGE || notificationType == NotificationType.CHAT)
                && !dialogByChatId.isHideFromList();

        getView().onOrderCompleted(order.getOrderId(), orderCompleted, needShowOrderButton, needShowChatButton);
    }

    private boolean checkIfChatNotOlder24Hours(DialogRealm dialogByChatId) {
        if (dialogByChatId.isOrderCompleted()) {
            Date orderDate = DateHelper.parseDateFromString(dialogByChatId.getCreatedDate());
            Date currentDate = DateHelper.getCurrentDate();
            int differenceBetweenInHours = DateHelper.differenceBetweenInHours(orderDate, currentDate);
            return differenceBetweenInHours < 24;
        } else return true;
    }

}
