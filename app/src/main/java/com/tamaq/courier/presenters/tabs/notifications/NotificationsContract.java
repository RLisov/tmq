package com.tamaq.courier.presenters.tabs.notifications;

import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface NotificationsContract {

    interface View extends BaseView {

        void onUserChecked(boolean active);

        void displayNotifications(List<NotificationRealm> list);

        void displayNoNotification();

        void enableSearchIcon();

    }

    interface Presenter extends BasePresenter<View> {

        void checkUserState();

        void loadNotifications();

    }
}
