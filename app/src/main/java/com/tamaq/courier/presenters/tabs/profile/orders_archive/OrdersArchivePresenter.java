package com.tamaq.courier.presenters.tabs.profile.orders_archive;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.tamaq.courier.utils.DateHelper.parseDateFromString;


public class OrdersArchivePresenter extends BasePresenterAbs<OrdersArchiveContract.View>
        implements OrdersArchiveContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    private Date mMinDateOriginal;
    private Date mMaxDateOriginal;
    private Date mMinDate;
    private Date mMaxDate;
    private List<OrderRealm> mOriginalList;

    public OrdersArchivePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(OrdersArchiveContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadOrders() {
        String userId = UserDAO.getInstance().getUser(getRealm()).getId();
        mServerCommunicator.getOrdersCurrentUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(archiveOrderList -> {
                    if (getView() == null) return;

                    if (archiveOrderList.isEmpty()) getView().displayNoOrders();
                    else {
                        getView().enableSearchIcon();
                        OrderDAO.getInstance().setOrdersFromPojo(archiveOrderList, getRealm(), getView().getContext());
                        prepareList(new ArrayList<>(OrderDAO.getInstance().getAllOrders(getRealm())));
                    }
                }, throwable -> {
                    getView().enableSearchIcon();
                    onError.accept(throwable);
                });
    }

    private void prepareList(List<OrderRealm> list) {
        mOriginalList = list;

        List<OrderRealm> sortedList = sortByDateDescending(list);
        mMinDate = parseDateFromString(sortedList.get(sortedList.size() - 1).getCreatedDate());
        mMaxDate = parseDateFromString(sortedList.get(0).getCreatedDate());

        mMinDateOriginal = mMinDate;
        mMaxDateOriginal = mMaxDate;

        getView().displayOrders(list);
    }

    private List<OrderRealm> sortByDateDescending(List<OrderRealm> list) {
        Collections.sort(list, (o1, o2) -> {

            Date firstDate = parseDateFromString(o1.getCreatedDate());
            Date secondDate = parseDateFromString(o2.getCreatedDate());

            return secondDate.compareTo(firstDate);
        });
        return list;
    }

    @Override
    public void clearDatePeriodSearch() {
        mMaxDate = mMaxDateOriginal;
        mMinDate = mMinDateOriginal;
        getView().displayOrders(mOriginalList);
    }

    @Override
    public Date getMinDate() {
        return mMinDate;
    }

    @Override
    public void setMinDate(Date minDate) {
        mMinDate = minDate;
        sortByDatePeriod();
    }

    private void sortByDatePeriod() {
        List<OrderRealm> sortedList = new ArrayList<>();

        for (OrderRealm orderRealm : mOriginalList) {
            Date currentOrderDate = parseDateFromString(orderRealm.getCreatedDate());
            if (currentOrderDate.getTime() < mMinDate.getTime()
                    || currentOrderDate.getTime() > mMaxDate.getTime())
                continue;

            sortedList.add(orderRealm);
        }

        getView().displayOrders(sortByDateDescending(sortedList));
    }

    @Override
    public Date getMaxDate() {
        return mMaxDate;
    }

    @Override
    public void setMaxDate(Date maxDate) {
        mMaxDate = maxDate;
        sortByDatePeriod();
    }

    @Override
    public Date getMinDateOriginal() {
        return mMinDateOriginal;
    }

    @Override
    public Date getMaxDateOriginal() {
        return mMaxDateOriginal;
    }
}
