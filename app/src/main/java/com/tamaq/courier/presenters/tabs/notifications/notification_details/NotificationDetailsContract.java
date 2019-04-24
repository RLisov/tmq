package com.tamaq.courier.presenters.tabs.notifications.notification_details;

import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;

public interface NotificationDetailsContract {

    interface View extends BaseView {

        void displayNotification(NotificationRealm notification, NotificationType notificationType);

        void onOrderCompleted(String orderId, boolean orderCompleted, boolean needShowOrderButton, boolean needShowChatButton);

    }

    interface Presenter extends BasePresenter<View> {

        void loadNotification(String id);

    }

}
