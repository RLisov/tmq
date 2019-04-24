package com.tamaq.courier.presenters.tabs.orders.map_route;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.activities.MapRouteActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MapRouteFragment extends BaseFragment implements MapRouteContract.View {

    public static final int DEFAULT_MAP_ZOOM = 15;
    public static final String ARG_ORDER_ID = "arg_order_id";
    public static final String ARG_TARGET_MARKER_TYPE = "arg_target_marker_type";
    private static final int TAG_CLIENT = 10;
    private static final int TAG_RESTAURANT = 20;
    @Inject
    MapRouteContract.Presenter presenter;
    @BindView(R.id.fabMyLocation)
    View fabMyLocation;
    @BindView(R.id.changeStatusButton)
    Button changeStatusButton;
    @BindView(R.id.bottomLayout)
    View bottomLayout;
    SupportMapFragment mapFragment;
    TargetMarkerType targetMarkerType;
    private GoogleMap mGoogleMap;
    private Marker mUserMarker;
    private String mOrderId;
    private OrderRealm mOrderRealm;

    public static MapRouteFragment newInstance(String orderId) {
        return newInstance(orderId, null);
    }

    public static MapRouteFragment newInstance(String orderId, TargetMarkerType targetMarkerType) {
        Bundle args = new Bundle();
        MapRouteFragment fragment = new MapRouteFragment();
        args.putString(ARG_ORDER_ID, orderId);
        args.putSerializable(ARG_TARGET_MARKER_TYPE, targetMarkerType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parseArguments();
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_map_route, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        mOrderRealm = presenter.getOrderById(mOrderId);
        setUpToolbar();
        setUpViews();
        setUpMap();
        setChangeToolbarColor(false);
        return rootView;
    }

    private void parseArguments() {
        mOrderId = getArguments().getString(ARG_ORDER_ID);
        if (getArguments().getSerializable(ARG_TARGET_MARKER_TYPE) != null) {
            targetMarkerType = (TargetMarkerType)
                    getArguments().getSerializable(ARG_TARGET_MARKER_TYPE);
        }
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
            getSupportActionBar().setTitle(String.format(new Locale("ru"), "%s %d",
                    getString(R.string.order_route), mOrderRealm.getNumber()));
        }
    }

    private void setUpViews() {
        fabMyLocation.setOnClickListener(v -> {
            if (mUserMarker != null) zoomToPosition(mUserMarker.getPosition());
            else {
                Location location = presenter.getLastUserLocation();
                if (location != null) zoomToPosition(location);
            }
        });
        changeStatusButton.setText(OrderRealm.getStatusAsStringForNext(
                getContext(), mOrderRealm.getOrderStatus()));
        changeStatusButton.setOnClickListener(v -> {
            OrderStatus orderStatus = OrderRealm.getNextStatus(mOrderRealm.getOrderStatus());
            if (orderStatus != null)
                presenter.changeOrderStatus(mOrderRealm.getOrderId(), orderStatus);
        });
    }

    private void setUpMap() {
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mapContainer, mapFragment);
            fragmentTransaction.commit();
            mapFragment.getMapAsync(map -> {
                mGoogleMap = map;
                mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
                onMapReadyActions();
            });
        } else onMapReadyActions();
    }

    private void zoomToPosition(Location location) {
        if (location != null) {
            float zoom = mGoogleMap.getCameraPosition().zoom;
            if (zoom < DEFAULT_MAP_ZOOM)
                zoom = DEFAULT_MAP_ZOOM;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    zoom));
        }
    }

    private void zoomToPosition(LatLng location) {
        if (location != null) {
            float zoom = mGoogleMap.getCameraPosition().zoom;
            if (zoom < DEFAULT_MAP_ZOOM) zoom = DEFAULT_MAP_ZOOM;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        }
    }

    private void onMapReadyActions() {
        presenter.checkHasPreviousCoordinates();

        fabMyLocation.setEnabled(true);
        fabMyLocation.callOnClick();
        double latClient = mOrderRealm.getDeliveryAddressLat();
        double lngClient = mOrderRealm.getDeliveryAddressLng();
        double latRestaurant = mOrderRealm.getRestaurantAddressLat();
        double lngRestaurant = mOrderRealm.getRestaurantAddressLng();
        presenter.loadPolylines(latClient, lngClient, latRestaurant, lngRestaurant);
        Marker restaurantMarker = mGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_restaurant_map))
                .position(new LatLng(latRestaurant, lngRestaurant)));
        restaurantMarker.setTag(TAG_RESTAURANT);
        Marker clientMarker = mGoogleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_client_map))
                .position(new LatLng(latClient, lngClient)));
        clientMarker.setTag(TAG_CLIENT);
        mGoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        if (TargetMarkerType.CLIENT.equals(targetMarkerType)) {
            moveCameraToMarkerAndOpen(clientMarker);
        } else if (TargetMarkerType.RESTAURANT.equals(targetMarkerType)) {
            moveCameraToMarkerAndOpen(restaurantMarker);
        }
    }

    private void moveCameraToMarkerAndOpen(Marker marker) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), DEFAULT_MAP_ZOOM));
        marker.showInfoWindow();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getActivity().finish();
        return super.onOptionsItemSelected(item);
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
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void applyPolyline(String polyline, int color) {

        Observable.just(polyline)
                .map(encodedPath -> {
                    List<LatLng> fullDecodedLine = PolyUtil.decode(encodedPath);
                    Log.d("Merov", "map route: " + fullDecodedLine.size());
                    return new PolylineOptions().addAll(fullDecodedLine);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(polylineOptions -> {
                    mGoogleMap.addPolyline(polylineOptions.color(ContextCompat.getColor(getContext(), color)));
                }, Throwable::printStackTrace);

//        Observable.just(polyline)
//                .map(encodedPath -> {
//                    List<LatLng> fullDecodedLine = PolyUtil.decode(encodedPath);
//
//                    List<LatLng> shortDecodedLine = new ArrayList<>();
//                    shortDecodedLine.add(fullDecodedLine.get(0));
//                    shortDecodedLine.add(fullDecodedLine.get(fullDecodedLine.size() - 1));
//
//                    PolylineOptions polylineOptions = new PolylineOptions().addAll(shortDecodedLine);
//
//                    return new Pair<>(polylineOptions, fullDecodedLine);
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(pair -> {
//
//                    PolylineOptions polylineOptions = pair.first;
//                    List<LatLng> latLngList = pair.second;
//
//                    Polyline finalPolyline = mGoogleMap.addPolyline(polylineOptions.color(ContextCompat.getColor(getContext(), color)));
//                    finalPolyline.setPoints(latLngList);
//
//                }, Throwable::printStackTrace);

//        mGoogleMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(polyline))
//                .color(ContextCompat.getColor(getContext(), color)));
    }

    @Override
    public void onPolylinesLoaded(LatLngBounds latLngBounds) {
        if (targetMarkerType == null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 80));
            presenter.saveMapLastCoordinates(
                    latLngBounds.getCenter().latitude,
                    latLngBounds.getCenter().longitude,
                    mGoogleMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void displayUserMarker(Location location) {
        if (mGoogleMap == null) return;
        if (mUserMarker == null) {
            mUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_tamaq_view_from_above))
                    .position(new LatLng(location.getLatitude(), location.getLongitude())));
        } else {
            mUserMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public Marker provideUserMarker() {
        return mUserMarker;
    }

    @Override
    public void onOrderStatusChanged() {
        changeStatusButton.setText(OrderRealm.getStatusAsStringForNext(getContext(), mOrderRealm.getOrderStatus()));
        if (mOrderRealm.getOrderStatus().equals(OrderStatus.COMPLETE.getValue())) {
            bottomLayout.setVisibility(View.GONE);
            getActivity().setResult(MapRouteActivity.RESULT_ORDER_FINISHED);
            getActivity().finish();
        }
    }

    @Override
    public void displayPreviousCoordinates(double lat, double lng, float zoom) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    @Override
    public void displayDistanceIsTooLong() {
        ((MapRouteActivity) getAppCompatActivity()).showError(getString(R.string.distance_is_too_long));
    }

    public enum TargetMarkerType {RESTAURANT, CLIENT}

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            if (marker.getTag() == null)
                return null;
            if (marker.getTag().equals(TAG_CLIENT) || marker.getTag().equals(TAG_RESTAURANT)) {
                View rootView = LayoutInflater.from(getContext()).inflate(
                        R.layout.google_map_popup_layout, null);
                TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
                TextView addressTextView = (TextView) rootView.findViewById(R.id.subtitleTextView);
                if (marker.getTag().equals(TAG_CLIENT)) {
                    titleTextView.setText(mOrderRealm.getClientName());
                    addressTextView.setText(mOrderRealm.getClientAddressUI());
                } else if (marker.getTag().equals(TAG_RESTAURANT)) {
                    titleTextView.setText(mOrderRealm.getRestaurantName());
                    addressTextView.setText(mOrderRealm.getRestaurantAddress());
                }
                return rootView;
            }
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }
}
