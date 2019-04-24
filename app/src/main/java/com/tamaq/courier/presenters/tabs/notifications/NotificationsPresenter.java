package com.tamaq.courier.presenters.tabs.notifications;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class NotificationsPresenter extends BasePresenterAbs<NotificationsContract.View>
        implements NotificationsContract.Presenter, NotificationsDAO.Listener {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public NotificationsPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(NotificationsContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void checkUserState() {
        mServerCommunicator.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPojo -> getView().onUserChecked(!userPojo.isBlocked()), onError);
    }

    @Override
    public void loadNotifications() {
        NotificationsDAO.getInstance().updateBadge(false);

        List<NotificationRealm> notifications = new ArrayList<>(
                NotificationsDAO.getInstance().getNotifications(getRealm()));
        if (!notifications.isEmpty()) {
            Observable.fromIterable(notifications)
                    .toList()
                    .map(this::sortByDate)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(notificationList -> getView().displayNotifications(notificationList),
                            onError);
        }

        mServerCommunicator
                .getNotifications()
                .compose(mRxFragment.bindToLifecycle())
                .map(notificationList -> {
                    for (int i = 0; i < notifications.size(); i++) {
                        notificationList.get(i).setRead(notifications.get(i).isReadLocally());
                    }
                    return notificationList;
                })
                .flatMap(Observable::fromIterable)
                .sorted((o1, o2) -> {
                    Date firstDate = DateHelper.parseDateFromString(DateHelper.correctDateWithDifference(o1.getCreated()));
                    Date secondDate = DateHelper.parseDateFromString(DateHelper.correctDateWithDifference(o2.getCreated()));
                    return secondDate.compareTo(firstDate);
                })
                .toList()
                .map(notificationPojoList ->
                        NotificationsDAO.getInstance().addNotificationsFromPojo(
                                notificationPojoList, getRealm(), getView() != null ? getView().getContext() : null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess(notificationRealms -> sendNotificationsReadToServer())
                .subscribe(notificationList -> {
                    if (getView() == null) return;
                    if (notificationList.isEmpty()) {
                        getView().displayNoNotification();
                        return;
                    }

                    getView().enableSearchIcon();

                    if (!notifications.isEmpty() && notifications.size() < notificationList.size()) {
                        NotificationsDAO.getInstance().newNotificationAdded();
                    }
                    NotificationsDAO.getInstance().addNotifications(notificationList, getRealm());
                    getView().displayNotifications(notificationList);
                }, throwable -> {
                    getView().enableSearchIcon();
                    onError.accept(throwable);
                });
    }

    public void sendNotificationsReadToServer() {
        mServerCommunicator.readNotifications().subscribe();
    }

    private List<NotificationRealm> sortByDate(List<NotificationRealm> list) {
        Collections.sort(list, (o1, o2) -> {

            Date firstDate = DateHelper.parseDateFromString(o1.getDate());
            Date secondDate = DateHelper.parseDateFromString(o2.getDate());

            return secondDate.compareTo(firstDate);
        });
        return list;
    }

    @Override
    public void updateBadge(boolean needShowBubble) {
        if (needShowBubble) NotificationsPresenter.this.loadNotifications();
    }

    @Override
    public void detachPresenter() {
        NotificationsDAO.getInstance().removeListener(this);
        super.detachPresenter();
    }
}
