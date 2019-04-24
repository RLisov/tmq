package com.tamaq.courier.presenters.registration;

import android.graphics.Bitmap;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.dao.TransportTypeDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class RegistrationPresenter extends BasePresenterAbs<RegistrationContract.View>
        implements RegistrationContract.Presenter {

    private static final UserRealm USER = UserRealm.getInstance();

    private RegistrationFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;

    public RegistrationPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(RegistrationContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RegistrationFragment) view;
    }

    @Override
    public void checkUser() {
        if (USER.getTransportType() == null) loadTransportTypes();
        if (USER.getCountry() == null) loadUserCountry();
        else if (USER.getWorkCity() == null) loadCities(USER.getCountry().getKey());
        else getView().initUser(USER);
    }

    @Override
    public void setUserAvatar(Bitmap avatarBitmap, String path) {
        USER.setAvatar(avatarBitmap);
        USER.setAvatarPhotoPath(path);
    }

    @Override
    public void setTransportType(String transportType) {
        USER.setTransportType(TransportTypeDAO.getInstance().getTransportByName(transportType, getRealm()));
    }

    private void loadUserCountry() {
        List<LocationRealm> locationRealmList = new ArrayList<>(LocationDAO.getInstance().getCountries(getRealm()));
        if (!locationRealmList.isEmpty()) {
            setUserCountry(locationRealmList);
        } else {
            mServerCommunicator.getCountries()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setUserCountry, onError);
        }
    }

    private void setUserCountry(List<LocationRealm> list) {
        if (USER.getLastLocation() != null) {
            LocationRealm countryByName = LocationDAO.getInstance()
                    .getCountryByName(USER.getLastLocation().getCountryName(), getRealm());
            if (countryByName != null) {
                USER.setCountry(RealmHelper.getCopyIfManaged(countryByName, getRealm()));
            } else USER.setCountry(RealmHelper.getCopyIfManaged(list.get(0), getRealm()));
        } else USER.setCountry(RealmHelper.getCopyIfManaged(list.get(0), getRealm()));

        if (USER.getWorkCity() == null) loadCities(USER.getCountry().getKey());
        getView().initUser(USER);
    }

    @Override
    public void loadTransportTypes() {
        List<TransportTypeRealm> transportTypeRealmList = UserDAO.getInstance().getTransportTypes(getRealm());
        if (!transportTypeRealmList.isEmpty()) {
            prepareTransportTypesForDisplay(transportTypeRealmList);
        }

        mServerCommunicator.getTransportTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::prepareTransportTypesForDisplay, onError);
    }

    private void prepareTransportTypesForDisplay(List<TransportTypeRealm> list) {
        USER.setTransportType(list.get(0));

        List<String> transportsNames = new ArrayList<>();

        for (TransportTypeRealm transportRealm : list) {
            transportsNames.add(transportRealm.getTitleUI());
        }
        getView().initUser(USER);
        getView().initTransportSpinner(transportsNames, list.get(0).getTitleUI());
    }

    @Override
    public void checkCanRegister() {
        getView().displayCanRegister(USER.isValidate());
    }

    @Override
    public void setUserName(String userName) {
        USER.setName(userName);
    }

    @Override
    public void setUserLastName(String userLastName) {
        USER.setLastName(userLastName);
    }

    @Override
    public void setUserAge(String age) {
        USER.setAge(Integer.parseInt(age));
    }

    @Override
    public void setUserRnn(String rnn) {
        USER.setRnn(rnn);
    }

    @Override
    public void setUserMobile(String mobile) {
        USER.setMobile(mobile);
    }

    @Override
    public void selectCountry(LocationRealm countryRealm) {
        USER.setCountry(countryRealm);
        USER.setWorkCity(null);
        LocationDAO.getInstance().deleteAllCities(getRealm());
        loadCities(countryRealm.getKey());
        getView().initUser(USER);
    }

    private void loadCities(String countryName) {
        List<LocationRealm> cityRealmList = LocationDAO.getInstance().getCitiesUnmanaged(getRealm());
        if (!cityRealmList.isEmpty()) {
            setUserCity();
        } else {
            mServerCommunicator.getCitiesByCountry(countryName)
                    .subscribe(countryWithCities -> {
                        LocationDAO.getInstance().addCountryWithCities(countryWithCities, getRealm());
                        setUserCity();
                    }, onError);
        }
    }

    private void setUserCity() {
        if (USER.getLastLocation() != null) {
            String cityName = USER.getLastLocation().getCityName();
            if (cityName != null) {
                List<LocationRealm> cityList = LocationDAO.getInstance().getCitiesUnmanaged(getRealm());
                for (LocationRealm city : cityList) {
                    if (city.getValueRu().equals(cityName)
                            || cityName.toLowerCase().contains(city.getValueRu().toLowerCase())) {
                        city.setChosen(true);
                        LocationRealm cityRealm = RealmHelper.getCopyIfManaged(city, getRealm());
                        cityRealm.setChosen(true);
                        USER.setWorkCity(cityRealm);
                        break;
                    }
                }
            }
        }
        getView().initUser(USER);
    }

    @Override
    public void requestSmsCode() {
        getView().showCommonLoader();
        String fullPhoneNumber = USER.getFullPhoneNumber();
        if (fullPhoneNumber.equals("+7975671978")) { // // TODO: 26.05.17 hardcode
            getView().onSmsCodeRequested(fullPhoneNumber);
            getView().hideCommonLoader();
            return;
        }
        mServerCommunicator.registrate(fullPhoneNumber)
                .subscribe(() -> {
                    getView().onSmsCodeRequested(fullPhoneNumber);
                    getView().hideCommonLoader();
                }, throwable -> {
                    getView().hideCommonLoader();
                    onError.accept(throwable);
                });
    }
}
