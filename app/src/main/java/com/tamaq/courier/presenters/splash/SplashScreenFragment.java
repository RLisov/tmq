package com.tamaq.courier.presenters.splash;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.main.MainActivity;
import com.tamaq.courier.presenters.tutorial.TutorialFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;


public class SplashScreenFragment extends BaseFragment implements SplashScreenContract.View {

    private static final String ARG_NEED_LOGOUT = "ARG_NEED_LOGOUT";
    @Inject
    SplashScreenContract.Presenter presenter;

    public static SplashScreenFragment newInstance(boolean needLogout) {
        SplashScreenFragment fragment = new SplashScreenFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NEED_LOGOUT, needLogout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;

        presenter.attachPresenter(this);
        rootView = inflater.inflate(R.layout.fragment_splash_screen, container, false);
        checkCanGetLocation();

        Bundle arguments = getArguments();
        if (!arguments.isEmpty()) {
            if (arguments.containsKey(ARG_NEED_LOGOUT)) {
                if (arguments.getBoolean(ARG_NEED_LOGOUT)) {
                    presenter.logout();
                }
            }
        }

        presenter.loadData();

        return rootView;
    }

    private void checkCanGetLocation() {
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
                && rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            getLocation();
        } else {
            rxPermissions.request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(isGranted -> {
                        if (isGranted)
                            getLocation();
                    });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            try {
                Geocoder gcd = new Geocoder(getContext(), new Locale("ru"));
                List<Address> fromLocation = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (!fromLocation.isEmpty()) presenter.getLocation(fromLocation.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(TamaqApp.get(getContext()).getAppComponent())
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void dataLoaded(boolean isFirstTime, boolean userExist) {
        if (isFirstTime) replaceFragment(TutorialFragment.newInstance(), false);
        else if (userExist) presenter.loadChatsAndNotifications();
        else replaceFragment(LoginFragment.newInstance(), false);
    }

    @Override
    public void goToMainActivity() {
        startActivity(MainActivity.newInstance(getContext()));
    }
}
