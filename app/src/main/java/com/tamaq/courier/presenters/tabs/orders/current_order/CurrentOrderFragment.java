package com.tamaq.courier.presenters.tabs.orders.current_order;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.TimeWithEventAdapter;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.model.ui.TimeWithEventItem;
import com.tamaq.courier.presenters.activities.EstimateClientActivity;
import com.tamaq.courier.presenters.activities.MapRouteActivity;
import com.tamaq.courier.presenters.activities.WebViewInfoActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersFragment;
import com.tamaq.courier.presenters.tabs.orders.map_route.MapRouteFragment;
import com.tamaq.courier.presenters.tabs.orders.order_cancel.OrderCancelActivity;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.widgets.TwoLinedTextView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.DecimalFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action0;


public class CurrentOrderFragment extends BaseFragment implements CurrentOrderContract.View {

    public static final int REQUEST_CODE_CANCEL_ORDER = 10;
    public static final int REQUEST_CODE_VIEW_MAP = 11;
    public static final int REQUEST_CODE_ESTIMATE = 12;

    public static final String ARG_ORDER_ID = "arg_order_id";
    public static final String ARG_SHOW_BACK_KEY = "arg_show_back_key";

    @Inject
    CurrentOrderContract.Presenter presenter;

    @BindView(R.id.bottomLayout)
    View bottomLayout;
    @BindView(R.id.scrollView)
    View scrollView;
    @BindView(R.id.bottomShadow)
    View bottomShadow;
    @BindView(R.id.timeEventsRecycler)
    RecyclerView timeEventsRecycler;
    @BindView(R.id.paymentForDeliveryValue)
    TextView paymentForDeliveryValue;
    @BindView(R.id.paymentClientValue)
    TextView paymentClientValue;
    @BindView(R.id.paymentChangeValue)
    TextView paymentChangeValue;
    @BindView(R.id.restaurantAddressView)
    TwoLinedTextView restaurantAddressView;
    @BindView(R.id.clientAddressView)
    TwoLinedTextView clientAddressView;
    @BindView(R.id.dispatcherPhoneView)
    TwoLinedTextView dispatcherPhoneView;
    @BindView(R.id.restaurantPhoneView)
    TwoLinedTextView restaurantPhoneView;
    @BindView(R.id.clientPhoneView)
    TwoLinedTextView clientPhoneView;
    @BindView(R.id.restaurantNameTextView)
    TextView restaurantNameTextView;
    @BindView(R.id.restaurantAddressTextView)
    TextView restaurantAddressTextView;
    @BindView(R.id.arriveTitleTextView)
    TextView arriveTitleTextView;
    @BindView(R.id.dispatcherPhoneLayout)
    View dispatcherPhoneLayout;
    @BindView(R.id.restaurantPhoneLayout)
    View restaurantPhoneLayout;
    @BindView(R.id.clientPhoneLayout)
    View clientPhoneLayout;
    @BindView(R.id.cancelOrderButton)
    Button cancelOrderButton;
    @BindView(R.id.changeStatusButton)
    Button changeStatusButton;
    @BindView(R.id.goToMapView)
    View goToMapView;
    @BindView(R.id.gpsDisabledLayout)
    ViewGroup gpsDisabledLayout;
    @BindView(R.id.internetDisabledLayout)
    ViewGroup internetDisabledLayout;
    @BindView(R.id.enableDataTransferInternet)
    TextView enableDataTransferInternet;
    @BindView(R.id.callDispatcherButton)
    TextView callDispatcherButton;
    @BindView(R.id.enableDataTransferGeo)
    Button enableDataTransferGeo;
    @BindView(R.id.restaurantAddressLayout)
    View restaurantAddressLayout;
    @BindView(R.id.clientAddressLayout)
    View clientAddressLayout;
    @BindView(R.id.emptyTimeTextView)
    View emptyTimeTextView;

    private TimeWithEventAdapter<TimeWithEventItem> mTimeWithEventAdapter;
    private String mOrderId;
    private OrderRealm mCurrentOrder;

    private boolean mNeedShowBackArrow;

    private InternetConnectionReceiver internetConnectionReceiver = new InternetConnectionReceiver(() -> {
                internetDisabledLayout.setVisibility(View.GONE);
                updateViewsEnabledState();
    }, () -> {
                internetDisabledLayout.setVisibility(View.VISIBLE);
                updateViewsEnabledState();
            });
    private GpsReceiver gpsReceiver = new GpsReceiver();

    public static CurrentOrderFragment newInstance(String orderId) {
        return newInstance(orderId, false);
    }

    public static CurrentOrderFragment newInstance(String orderId, boolean showBackArrow) {
        Bundle args = new Bundle();
        CurrentOrderFragment fragment = new CurrentOrderFragment();
        args.putString(ARG_ORDER_ID, orderId);
        args.putBoolean(ARG_SHOW_BACK_KEY, showBackArrow);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parseArguments();
        registerInternetReceiver();
        registerGpsReceiver();
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_current_order, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        setUpToolbar();
        setUpViews();

        showCommonLoader();
        presenter.loadOrder(mOrderId);
        return rootView;
    }

    private void parseArguments() {
        mOrderId = getArguments().getString(ARG_ORDER_ID);
    }

    private void registerInternetReceiver() {
        getActivity().registerReceiver(internetConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void registerGpsReceiver() {
        getActivity().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        updateToolbarTitle("");
        mNeedShowBackArrow = getArguments().getBoolean(ARG_SHOW_BACK_KEY);
        getSupportActionBar().setDisplayHomeAsUpEnabled(mNeedShowBackArrow);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
    }

    private void setUpViews() {
        loader = rootView.findViewById(R.id.loader);

        mTimeWithEventAdapter = new TimeWithEventAdapter<>(getContext());
        timeEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        timeEventsRecycler.setAdapter(mTimeWithEventAdapter);
        timeEventsRecycler.setNestedScrollingEnabled(false);

        if (mCurrentOrder != null && mCurrentOrder.getOrderStatus().equals(OrderStatus.COMPLETE.getValue())) {
            changeStatusButton.setVisibility(View.GONE);
        } else {
            changeStatusButton.setOnClickListener(v -> {
                if (mCurrentOrder != null) {
                    OrderStatus orderStatus = OrderRealm.getNextStatus(mCurrentOrder.getOrderStatus());
                    if (orderStatus != null) presenter.changeOrderStatus(mCurrentOrder.getOrderId(), orderStatus);
                }
            });
        }

        goToMapView.setOnClickListener(v ->
                startActivityForResult(MapRouteActivity.newInstance(getContext(), mOrderId), REQUEST_CODE_VIEW_MAP));

        enableDataTransferInternet.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        callDispatcherButton.setOnClickListener(v -> callNumber(mCurrentOrder.getClientPhone()));
        enableDataTransferGeo.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        updateGpsLayoutState();
        updateInternetLayoutState();
        updateViewsEnabledState();

        restaurantAddressLayout.setOnClickListener(v ->
                startActivityForResult(MapRouteActivity.newInstance(getContext(), mOrderId,
                        MapRouteFragment.TargetMarkerType.RESTAURANT),
                        REQUEST_CODE_VIEW_MAP));
        clientAddressLayout.setOnClickListener(v ->
                startActivityForResult(MapRouteActivity.newInstance(getContext(), mOrderId,
                        MapRouteFragment.TargetMarkerType.CLIENT),
                        REQUEST_CODE_VIEW_MAP));
    }

    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.current_order_title, title));
    }

    private void callNumber(String phone) {
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(android.Manifest.permission.CALL_PHONE)) startCallActivity(phone);
        else {
            rxPermissions.request(android.Manifest.permission.CALL_PHONE)
                    .subscribe(isGranted -> {
                        if (isGranted) startCallActivity(phone);
                    });
        }
    }

    private void updateGpsLayoutState() {
        gpsDisabledLayout.setVisibility(HelperCommon.isLocationEnabledByMax(getContext())
                ? View.GONE : View.VISIBLE);
    }

    private void updateInternetLayoutState() {
        internetDisabledLayout.setVisibility(HelperCommon.isNetworkConnected(getContext())
                ? View.GONE : View.VISIBLE);
    }

    private void updateViewsEnabledState() {
        boolean internetEnabled = HelperCommon.isNetworkConnected(getContext());
        boolean gpsOnMax = HelperCommon.isLocationEnabledByMax(getContext());
        boolean makeViewsEnabled = internetEnabled && gpsOnMax;
        cancelOrderButton.setEnabled(makeViewsEnabled);
        changeStatusButton.setEnabled(makeViewsEnabled);
    }

    private void startCallActivity(String phone) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.orders_menu, menu);
        onNeedUpdateToolbarState(UserDAO.getInstance().isActiveStatus());
    }

    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        super.onNeedUpdateToolbarState(activeStatus);
        menu.findItem(R.id.actionInfo).setIcon(activeStatus
                ? R.drawable.ic_info_app_bar : R.drawable.ic_info_dark);
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(TamaqApp.get(getContext()).getAppComponent())
                .commonModule(new CommonModule())
                .build().inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStackImmediate();
        } else if (item.getItemId() == R.id.actionInfo) {
            if (mCurrentOrder != null)
                startActivity(WebViewInfoActivity.newInstance(
                        getContext(), mCurrentOrder.getInfoHTML(), WebViewInfoActivity.Type.INFO_ABOUT_ORDER));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        unRegisterInternetReceiver();
        unRegisterGpsReceiver();
        super.onDestroyView();
    }

    private void unRegisterInternetReceiver() {
        getActivity().unregisterReceiver(internetConnectionReceiver);
    }

    private void unRegisterGpsReceiver() {
        getActivity().unregisterReceiver(gpsReceiver);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void displayOrder(OrderRealm orderRealm) {
        mCurrentOrder = orderRealm;
        arriveTitleTextView.setText(orderRealm.getFormattedArriveTime(getContext()));

        restaurantNameTextView.setText(orderRealm.getRestaurantName());
        restaurantAddressTextView.setText(orderRealm.getRestaurantAddress());

        DecimalFormat format = new DecimalFormat("#.##");

        paymentForDeliveryValue.setText(format.format(orderRealm.getProfitUI()) + presenter.getUserCurrency());
        paymentClientValue.setText(format.format(orderRealm.getPaymentUI()) + presenter.getUserCurrency());
        paymentChangeValue.setText(format.format(orderRealm.getPaymentChangeUI()) + presenter.getUserCurrency());

        restaurantAddressView.setTitle(orderRealm.getRestAddressUI());
        restaurantAddressView.setSubTitle(
                getString(R.string.restaurant_address, orderRealm.getRestaurantName()));
        clientAddressView.setTitle(orderRealm.getClientAddressUI());

        dispatcherPhoneView.setTitle(orderRealm.getDispatcherPhone());
        restaurantPhoneView.setTitle(orderRealm.getRestaurantPhone());
        clientPhoneView.setTitle(orderRealm.getClientPhone());
        clientPhoneView.setSubTitle(
                getString(R.string.client_name, orderRealm.getClientName()));

        mTimeWithEventAdapter.setObjects(orderRealm.prepareEventItems(getContext()));
        updateTimeRecyclerVisibility();

        dispatcherPhoneLayout.setOnClickListener(v -> callNumber(orderRealm.getDispatcherPhone()));
        restaurantPhoneLayout.setOnClickListener(v -> callNumber(orderRealm.getRestaurantPhone()));
        clientPhoneLayout.setOnClickListener(v -> callNumber(orderRealm.getClientPhone()));

        if (orderRealm.getOrderStatus().equals(OrderStatus.CANCEL_CUSTOMER.getValue())
                || orderRealm.getOrderStatus().equals(OrderStatus.CANCEL_EXECUTOR.getValue()))
            cancelOrderButton.setVisibility(View.GONE);
        cancelOrderButton.setOnClickListener(v ->
                startActivityForResult(OrderCancelActivity.newInstance(
                        getContext(), mOrderId), REQUEST_CODE_CANCEL_ORDER));

        changeStatusButton.setText(OrderRealm.getStatusAsStringForNext(
                getContext(), mCurrentOrder.getOrderStatus()));
        hideStatusButtonIfTextEmpty();

        updateToolbarTitle(String.valueOf(orderRealm.getNumber()));
    }

    private void updateTimeRecyclerVisibility() {
        timeEventsRecycler.setVisibility(mTimeWithEventAdapter.getItemCount() == 0
                ? View.GONE : View.VISIBLE);
        emptyTimeTextView.setVisibility(mTimeWithEventAdapter.getItemCount() == 0
                ? View.VISIBLE : View.GONE);
    }

    private void hideStatusButtonIfTextEmpty() {
        if (changeStatusButton.getText().length() == 0)
            changeStatusButton.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CANCEL_ORDER || requestCode == REQUEST_CODE_ESTIMATE) {
                if (mNeedShowBackArrow) getFragmentManager().popBackStackImmediate();
                else goToOrdersFragment();
                return;
            }
        }
        if (requestCode == REQUEST_CODE_VIEW_MAP) {
            if (resultCode == MapRouteActivity.RESULT_ORDER_FINISHED) goToEstimateActivity();
            else onOrderStatusChanged();
        }
    }

    private void goToOrdersFragment() {
        replaceFragment(OrdersFragment.newInstance(), false);
    }

    private void goToEstimateActivity() {
        startActivityForResult(EstimateClientActivity.newInstance(getContext(), mOrderId),
                REQUEST_CODE_ESTIMATE);
    }

    @Override
    public void hideOrder() {
        hideCommonLoader();
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onOrderStatusChanged() {
        mTimeWithEventAdapter.setObjects(mCurrentOrder.prepareEventItems(getContext()));
        updateTimeRecyclerVisibility();
        changeStatusButton.setText(OrderRealm.getStatusAsStringForNext(
                getContext(), mCurrentOrder.getOrderStatus()));
        hideStatusButtonIfTextEmpty();
        if (mCurrentOrder.getOrderStatus().equals(OrderStatus.COMPLETE.getValue())) {
            changeStatusButton.setVisibility(View.GONE);
            goToEstimateActivity();
        }
    }

    private static class InternetConnectionReceiver extends BroadcastReceiver {

        private Action0 connectAction;
        private Action0 disconnectAction;
        private ConnectListener connectListener;
        private boolean firstConnect = true;

        public InternetConnectionReceiver(ConnectListener connectListener) {
            this.connectListener = connectListener;
        }

        public InternetConnectionReceiver(Action0 connectAction, Action0 disconnectAction) {
            this.connectAction = connectAction;
            this.disconnectAction = disconnectAction;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // workaround to not call multiplyTimes http://stackoverflow.com/questions/8412714/broadcastreceiver-receives-multiple-identical-messages-for-one-event?answertab=active#tab-top
            boolean isConnected = HelperCommon.isNetworkConnected(context);
            if (isConnected) {
                if (firstConnect) {
                    firstConnect = false;
                    if (connectListener != null)
                        connectListener.onRealConnect();
                    if (connectAction != null)
                        connectAction.call();
                }
            } else {
                firstConnect = true;
                if (connectListener != null)
                    connectListener.onDisconnect();
                if (disconnectAction != null)
                    disconnectAction.call();
            }
        }

        interface ConnectListener {
            void onRealConnect();

            void onDisconnect();
        }
    }

    private class GpsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateGpsLayoutState();
            updateViewsEnabledState();
        }
    }

}
