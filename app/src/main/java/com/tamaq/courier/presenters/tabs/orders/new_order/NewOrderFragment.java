package com.tamaq.courier.presenters.tabs.orders.new_order;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.tamaq.courier.R;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.activities.WebViewInfoActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.widgets.TwoLinedTextView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Action0;


public class NewOrderFragment extends BaseFragment implements NewOrderContract.View {

    public static final String ARG_ORDER_ID = "arg_order_id";
    public static final String ARG_ALREADY_ACCEPTED = "ARG_ALREADY_ACCEPTED";
    public static final String ARG_TOTAL_TIMER_TIME = "ARG_TOTAL_TIMER_TIME";
    public static final String ARG_LEAST_TIMER_TIME = "ARG_LEAST_TIMER_TIME";
    private static final float MAP_HEIGHT_WIDTH_SCALE_FACTOR = 0.64f;
    @Inject
    NewOrderContract.Presenter presenter;

    @BindView(R.id.timerTextView)
    TextView timerTextView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.cancelButton)
    Button cancelButton;
    @BindView(R.id.acceptButton)
    Button acceptButton;
    @BindView(R.id.acceptedOrderLayout)
    View acceptedOrderLayout;
    @BindView(R.id.bottomLayout)
    View bottomLayout;
    @BindView(R.id.scrollView)
    View scrollView;
    @BindView(R.id.bottomShadow)
    View bottomShadow;
    @BindView(R.id.mapContainer)
    ViewGroup mapContainer;
    @BindView(R.id.mapProgressBarLayout)
    View mapProgressBarLayout;
    @BindView(R.id.routeEmptyView)
    View routeEmptyView;
    @BindView(R.id.arriveTitleTextView)
    TextView arriveTitleTextView;
    @BindView(R.id.restaurantNameTextView)
    TextView restaurantNameTextView;
    @BindView(R.id.restaurantAddressTextView)
    TextView restaurantAddressTextView;
    @BindView(R.id.paymentForDeliveryValue)
    TextView paymentForDeliveryValue;
    @BindView(R.id.restaurantAddressView)
    TwoLinedTextView restaurantAddressView;
    @BindView(R.id.clientAddressView)
    TwoLinedTextView clientAddressView;

    private SupportMapFragment mMapFragment;
    private Disposable mTimerSubscription;
    private GoogleMap mGoogleMap;

    private Marker mUserMarker;
    private String mOrderId;

    private GpsReceiver mGpsReceiver = new GpsReceiver();
    private OrderRealm mOrderRealm;

    private int mTimerTotalTime = -1;
    private int mLastEmittedTimerValue = -1;

    public static NewOrderFragment newInstance(String orderId) {
        return newInstance(orderId, false);
    }

    public static NewOrderFragment newInstance(String orderId, boolean alreadyAccepted) {
        Bundle args = new Bundle();
        NewOrderFragment fragment = new NewOrderFragment();
        args.putString(ARG_ORDER_ID, orderId);
        args.putBoolean(ARG_ALREADY_ACCEPTED, alreadyAccepted);
        fragment.setArguments(args);
        return fragment;
    }

    public static NewOrderFragment newInstance(String orderId, int totalTimerTime,
                                               int leastTimerTime) {
        Bundle args = new Bundle();
        NewOrderFragment fragment = new NewOrderFragment();
        args.putString(ARG_ORDER_ID, orderId);
        args.putBoolean(ARG_ALREADY_ACCEPTED, false);
        args.putInt(ARG_TOTAL_TIMER_TIME, totalTimerTime);
        args.putInt(ARG_LEAST_TIMER_TIME, leastTimerTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parseArguments();
        registerGpsReceiver();
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_new_order, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        presenter.changeWorkStatus(false);
        mOrderRealm = presenter.getOrderById(mOrderId);
        presenter.saveOrderRespondedId(mOrderId);
        setUpToolbar();
        setUpViews();
        setUpMap();
        updateGpsState();

        if (getArguments().getBoolean(ARG_ALREADY_ACCEPTED)) {
            presenter.startNotificationsCheck(mOrderId);
        }
        return rootView;
    }

    private void parseArguments() {
        mOrderId = getArguments().getString(ARG_ORDER_ID);
    }

    private void registerGpsReceiver() {
        getActivity().registerReceiver(mGpsReceiver,
                new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.new_order);
    }

    // Always use active theme for toolbar, exception from default logic
    @Override
    protected boolean updateToolbarState() {
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_sky_blue));
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), R.color.white));
        return UserDAO.getInstance().isActiveStatus();
    }

    // Always use active button for toolbar, exception from default logic
    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        menu.findItem(R.id.actionInfo).setIcon(R.drawable.ic_info_app_bar);
    }

    private void setUpViews() {
        cancelButton.setOnClickListener(v ->
                DialogHelper.showDialog(getActivity(),
                        R.string.refuse_the_order_dialog_title,
                        R.string.refuse_the_order_dialog_description,
                        R.string.refuse_action,
                        R.string.not_refuse_action,
                        (dialog, which) -> {
                            // // TODO: 07.03.2017 handle order cancel
                            presenter.changeWorkStatus(true);
                            getActivity().finish();
                        }, null));
        acceptButton.setOnClickListener(v -> DialogHelper.showDialog(getActivity(),
                R.string.accept_order_dialog_title,
                R.string.accept_order_dialog_description,
                R.string.accept_action,
                R.string.cancel_action,
                (dialog, which) -> {
                    switchViewsToAcceptedState();
                    presenter.acceptOrder(mOrderId);
                    // // TODO: 07.03.2017 handle order accept
                }, null));

        boolean alreadyAccepted = getArguments().getBoolean(ARG_ALREADY_ACCEPTED);
        if (!alreadyAccepted) {
            if (getArguments().getInt(ARG_LEAST_TIMER_TIME, -1) != -1) {
                int totalTime = getArguments().getInt(ARG_TOTAL_TIMER_TIME);
                int leastTime = getArguments().getInt(ARG_LEAST_TIMER_TIME);
                mTimerTotalTime = totalTime;
                startOrderTimer(totalTime, leastTime);
            } else {
                startOrderTimer(35);
                mTimerTotalTime = 35;
            }
        } else switchViewsToAcceptedState();

        updateGpsLayoutState();
        displayOrder();
    }

    private void setUpMap() {
        if (mMapFragment == null) {
            setUpMapContainerHeight(() -> {
                GoogleMapOptions options = new GoogleMapOptions();
                mMapFragment = SupportMapFragment.newInstance(options);
                FragmentTransaction fragmentTransaction =
                        getChildFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.mapContainer, mMapFragment);
                fragmentTransaction.commit();
                mMapFragment.getMapAsync(map -> {
                    mGoogleMap = map;
                    onMapReadyActions();
                });
            });
        } else onMapReadyActions();
    }

    private void updateGpsState() {
        routeEmptyView.setVisibility(HelperCommon.isLocationEnabledByMax(getContext())
                ? View.GONE : View.VISIBLE);
    }

    @Override
    public void switchViewsToAcceptedState() {
        acceptedOrderLayout.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        bottomLayout.setVisibility(View.GONE);
        ((ViewGroup.MarginLayoutParams) scrollView.getLayoutParams()).bottomMargin = 0;
        scrollView.requestLayout();
        bottomShadow.setVisibility(View.GONE);
        cancelTimerSubscription();
    }

    //start timer function
    void startOrderTimer(int seconds, int leastTime) {
        cancelTimerSubscription();
        final int multiply = 1000;
        progressBar.setMax(seconds * multiply);

        mTimerSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(aLong -> aLong + seconds - leastTime)
                .subscribe(aLong -> {
                    long value = seconds - aLong;
                    mLastEmittedTimerValue = (int) value;
                    String text = DateHelper.getStringMinutesAndSeconds(value * 1000);
                    timerTextView.setText(text);

                    if (aLong >= seconds) {
                        mTimerSubscription.dispose();
                        presenter.checkNeedShowTimeIsOutDialog(mOrderId);
                        new Handler().postDelayed(() -> timerTextView.setText(R.string.time_is_over), 500);
                        return;
                    }
                    // It is necessary that the animation ends exactly at 00:00
                    // Otherwise it will end a second later
                    int progress = (int) (value * 1000 - 1000);
                    setProgressAnimate(progressBar, progress);
                });
    }

    void startOrderTimer(int seconds) {
        startOrderTimer(seconds, seconds);
    }

    private void updateGpsLayoutState() {
        if (HelperCommon.isLocationEnabledByMax(getContext())) {
            // TODO: 20.03.17 normal state, wait for project-manager
        } else {
            // empty state
        }
    }

    @SuppressLint("DefaultLocale")
    private void displayOrder() {
        try {
            if (mOrderRealm == null) mOrderRealm = presenter.getOrderById(mOrderId);
            DecimalFormat format = new DecimalFormat("#.##");
            arriveTitleTextView.setText(mOrderRealm.getFormattedArriveTime(getContext()));
            restaurantNameTextView.setText(mOrderRealm.getRestaurantName());
            restaurantAddressTextView.setText(mOrderRealm.getRestAddressUI());

            paymentForDeliveryValue.setText(String.format("%s%s", format.format(mOrderRealm.getProfitUI()), presenter.getUserCurrency()));

            restaurantAddressView.setTitle(mOrderRealm.getRestAddressUI());
            restaurantAddressView.setSubTitle(
                    getString(R.string.restaurant_address, mOrderRealm.getRestaurantName()));
            clientAddressView.setTitle(mOrderRealm.getClientAddressUI());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setUpMapContainerHeight(Action0 onCompleteAction) {
        mapContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mapContainer.getLayoutParams().height =
                                (int) (mapContainer.getWidth() * MAP_HEIGHT_WIDTH_SCALE_FACTOR);
                        mapContainer.requestLayout();
                        onCompleteAction.call();
                    }
                });
    }

    private void onMapReadyActions() {
        double latClient = mOrderRealm.getClientAddressLat();
        double lngClient = mOrderRealm.getClientAddressLng();
        double latRestaurant = mOrderRealm.getRestaurantAddressLat();
        double lngRestaurant = mOrderRealm.getRestaurantAddressLng();
        presenter.loadPolylines(latClient, lngClient, latRestaurant, lngRestaurant);
        mGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_restaurant_map))
                .position(new LatLng(latRestaurant, lngRestaurant)));
        mGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_client_map))
                .position(new LatLng(latClient, lngClient)));
    }

    //cancel timer
    void cancelTimerSubscription() {
        if (mTimerSubscription != null) {
            mTimerSubscription.dispose();
            mTimerSubscription = null;
        }
    }

    private void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }

    @Override
    public void showTimeIsOutDialog() {
        AlertDialog alertDialog = DialogHelper.buildDialog(getActivity(),
                R.string.time_is_over_dialog_title,
                R.string.time_is_over_dialog_description,
                R.string.ok, 0, (dialog, which) -> {
                    presenter.changeWorkStatus(true);
                    getActivity().finish();
                }, null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.orders_menu, menu);
        onNeedUpdateToolbarState(UserDAO.getInstance().isActiveStatus());
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

        } else if (item.getItemId() == R.id.actionInfo) {
            OrderRealm orderRealm = presenter.getOrderById(mOrderId);
            if (orderRealm != null)
                startActivity(WebViewInfoActivity.newInstance(
                        getContext(), orderRealm.getInfoHTML(), WebViewInfoActivity.Type.INFO_ABOUT_ORDER));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        cancelTimerSubscription();
        unRegisterGpsReceiver();
        super.onDestroyView();
    }

    private void unRegisterGpsReceiver() {
        getActivity().unregisterReceiver(mGpsReceiver);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void applyPolyline(String polyline, int color) {
        Observable.just(polyline)
                .map(encodedPath -> {
                    List<LatLng> fullDecodedLine = PolyUtil.decode(encodedPath);
                    Log.d("Merov", "new order: " + fullDecodedLine.size());
                    return new PolylineOptions().addAll(fullDecodedLine);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(polylineOptions -> {
                    mGoogleMap.addPolyline(polylineOptions.color(ContextCompat.getColor(getContext(), color)));
                }, Throwable::printStackTrace);
    }

    @Override
    public void applyLatLngBounds(LatLngBounds latLngBounds) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 80));
    }

    @Override
    public void setUserMarker(double lat, double lng) {
        if (mGoogleMap == null) return;
        if (mUserMarker == null) {
            mUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_tamaq))
                    .position(new LatLng(lat, lng)));
        } else mUserMarker.setPosition(new LatLng(lat, lng));
    }

    @Override
    public void onOrderAcceptedByClient() {
        AlertDialog alertDialog = DialogHelper.buildDialog(getActivity(),
                R.string.congratulations,
                R.string.client_chosen_you,
                R.string.ok, 0, (dialog, which) -> {
                    presenter.changeWorkStatus(true);
                    getActivity().finish();
                }, null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void showMapLoader() {
        mapProgressBarLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideMapLoader() {
        mapProgressBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onOrderNotAcceptedByClient() {
        AlertDialog alertDialog = DialogHelper.buildDialog(getAppCompatActivity(),
                0,
                R.string.customer_selected_another_candidate,
                R.string.ok,
                0,
                (dialog, which) -> {
                    presenter.changeWorkStatus(true);
                    getActivity().finish();
                },
                null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onClientNotSelectedAnyExecutor() {
        AlertDialog alertDialog = DialogHelper.buildDialog(getAppCompatActivity(),
                R.string.customer_not_choose_any_candidate,
                0,
                R.string.ok,
                0,
                (dialog, which) -> {
                    presenter.changeWorkStatus(true);
                    getActivity().finish();
                },
                null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.saveOrderRespondedTimerInfo(mTimerTotalTime, mLastEmittedTimerValue);
    }

    private class GpsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateGpsLayoutState();
        }
    }
}