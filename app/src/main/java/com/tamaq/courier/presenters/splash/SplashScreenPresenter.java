package com.tamaq.courier.presenters.splash;

import android.content.SharedPreferences;
import android.location.Address;
import android.preference.PreferenceManager;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.LastLocation;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.NetworkChangeReceiver;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashScreenPresenter extends BasePresenterAbs<SplashScreenContract.View>
        implements SplashScreenContract.Presenter, NetworkChangeReceiver.NetworkStateChangedListener {

    private SplashScreenFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private boolean mIsError;

    public final Consumer<Throwable> localErrorHandler = this::onErrorAction;

    private void onErrorAction(Throwable throwable) {
        mIsError = true;
        onErrorDefaultAction(throwable);
    }

    public SplashScreenPresenter(TamaqApp app, ServerCommunicator serverCommunicator, NetworkChangeReceiver receiver) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
        receiver.setNetworkStateChangedListener(this);
    }

    @Override
    public void attachPresenter(SplashScreenContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (SplashScreenFragment) view;
    }

    public void checkIsFirstTime() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putBoolean("isFirstTime", false);
            ed.apply();

//            mServerCommunicator.getServerTime().subscribe(DateHelper::setDiffWithServer, onError);
        }

        mServerCommunicator.getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((serverTime) -> {
                    DateHelper.setDiffWithServer(serverTime);
                    getView().dataLoaded(isFirstTime, UserDAO.getInstance().getUser(getRealm()) != null);
                }, throwable -> {
                    getView().dataLoaded(isFirstTime, UserDAO.getInstance().getUser(getRealm()) != null);
                    onError.accept(throwable);
                });
    }

    @Override
    public void getLocation(Address address) {
        if (UserRealm.getInstance().getLastLocation() == null) {
            LastLocation lastLocation = new LastLocation();
            lastLocation.setCountryName(address.getCountryName());
            lastLocation.setCityName(address.getLocality());
            lastLocation.setRegion(address.getAdminArea());
            lastLocation.setLatitude(address.getLatitude());
            lastLocation.setLongitude(address.getLongitude());
            UserRealm.getInstance().setLastLocation(lastLocation);
        }
    }

    public Single<List<LocationRealm>> loadCountries() {
        List<LocationRealm> countries = LocationDAO.getInstance().getCountries(getRealm());
        if (!countries.isEmpty()) {
            return Single.just(countries);
        } else {
             return mServerCommunicator.getCountries()
                    .doOnSuccess(countryList -> LocationDAO.getInstance().addLocations(countryList, getRealm()));
        }
    }

    @Override
    public void loadChatsAndNotifications() {
        mServerCommunicator.getUserInfo() // to check is user authorized or not
                .subscribe(userPojo -> {
                    PrefsHelper.setUserBlocked(userPojo.isBlocked(), getView().getContext());
                    getView().goToMainActivity();
                }, localErrorHandler);
    }

    private Single<List<TransportTypeRealm>> loadTransportTypes() {
        return mServerCommunicator.getTransportTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(transportTypeRealms -> UserDAO.getInstance().addTransportTypes(transportTypeRealms, getRealm()));
    }

    @Override
    public void logout() {
        mServerCommunicator.logout().subscribe(() -> {
        }, localErrorHandler);
    }

    @Override
    public void loadData() {
        loadCountries().zipWith(loadTransportTypes(), (locationRealms, transportTypeRealms) -> true)
                .subscribe(result -> checkIsFirstTime(), localErrorHandler);
    }

    @Override
    public void onNetworkChanged(boolean isNetworkEnabled) {
        if(mIsError && isNetworkEnabled) {
            loadData();
        }
    }
}
