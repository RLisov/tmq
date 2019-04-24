package com.tamaq.courier.presenters.tabs.profile.settings;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.events.BlockBottomBarEvent;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.trello.rxlifecycle2.components.support.RxFragment;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;


public class ProfileSettingsPresenter extends BasePresenterAbs<ProfileSettingsContract.View>
        implements ProfileSettingsContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;


    public ProfileSettingsPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(ProfileSettingsContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public void loadUserInformation() {
        UserRealm userRealm = UserDAO.getInstance().getUser(getRealm());
        if (userRealm != null) getView().displayUserInfo(userRealm);
        loadTransportTypes();

        mServerCommunicator.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPojo -> {
                    UserDAO.getInstance().saveUserDataFromPojo(userPojo, getRealm());
                    UserRealm user = UserDAO.getInstance().getUser(getRealm());

                    if (user.getWorkCity().getType().equals("districts")) {
                        String userCountryKey = UserDAO.getInstance().getUser(getRealm()).getCountry().getKey();
                        LocationRealm userCountryRealm = LocationDAO.getInstance().getLocationByKey(userCountryKey, getRealm());

                        RealmList<LocationRealm> cities = userCountryRealm.getChildes();

                        forCities:
                        for (LocationRealm city : cities) {
                            RealmList<LocationRealm> regions = city.getChildes();

                            if (regions != null && !regions.isEmpty()) {
                                for (LocationRealm region : regions) {
                                    if (region.getKey().equals(user.getWorkCity().getKey())) {
                                        UserDAO.getInstance().updateCity(city, getRealm());
                                        break forCities;
                                    }
                                }
                            }
                        }
                    }

                    getView().displayUserInfo(user);
                }, onError);
    }

    private void loadTransportTypes() {
        List<TransportTypeRealm> transportTypeRealmList = UserDAO.getInstance().getTransportTypes(getRealm());
        if (!transportTypeRealmList.isEmpty())
            prepareTransportTypesForDisplay(transportTypeRealmList);

        mServerCommunicator.getTransportTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::prepareTransportTypesForDisplay, onError);
    }

    private void prepareTransportTypesForDisplay(List<TransportTypeRealm> list) {
        List<String> transportsNames = new ArrayList<>();
        String userTransportType = UserDAO.getInstance().getCommonOrderSettings(
                getRealm()).getTransportTypeRealm().getTitleUI();
        String selectedTransportType = list.get(0).getTitleUI();

        for (TransportTypeRealm transportRealm : list) {
            transportsNames.add(transportRealm.getTitleUI());
            if (userTransportType.equals(transportRealm.getTitleUI())) {
                selectedTransportType = userTransportType;
            }
        }
        if (getView() != null)
            getView().initTransportSpinner(transportsNames, selectedTransportType);
    }

    @Override
    public void updatePhoto(String filePath) {
        UserDAO.getInstance().updatePhoto(filePath, getRealm());
        sendUserPhotoToServer(filePath);
    }

    private void sendUserPhotoToServer(String mCurrentPicturePath) {
        mServerCommunicator.updateUserPhoto(
                ServerCommunicator.PhotoTag.avatar,
                new File(mCurrentPicturePath))
                .subscribe(() -> {
                    UserDAO.getInstance().setAvatarUrlAsServerUrl(getRealm());
                    getView().onChangesSaved();
                }, onError);
    }

    @Override
    public void updateTransportType(String transportType) {
        UserDAO.getInstance().updateTransportType(transportType, getRealm());
        sendUserInfoToServer();
    }

    private void sendUserInfoToServer() {
        UserRealm user = UserDAO.getInstance().getUser(getRealm());
        mServerCommunicator
                .updateUserInfo(UserPojo.createForProfileUpdate(user))
                .subscribe(() -> getView().onChangesSaved(), onError);
    }

    @Override
    public void updateCountry(LocationRealm country) {
        UserDAO.getInstance().updateCountry(country, getRealm());
        sendUserInfoToServer();
    }

    @Override
    public void updateCity(LocationRealm city) {
        UserDAO.getInstance().updateCity(city, getRealm());
        sendUserInfoToServer();
    }

    @Override
    public void updateMobile(String mobileNumber) {
        UserDAO.getInstance().updateMobile(mobileNumber, getRealm());
        sendUserInfoToServer();
    }

    @Override
    public void removeUserPhotoFromServer() {
        UserDAO.getInstance().updatePhoto("", getRealm());
        UserDAO.getInstance().removeUserPhoto(getRealm());
        mServerCommunicator
                .removeUserPhoto()
                .subscribe(() -> getView().onChangesSaved(), onError);
    }

    @Override
    public void requestSmsCode(String phoneNumber) {
        getView().showCommonLoader();
        mServerCommunicator.askapprove(phoneNumber)
                .subscribe(() -> {
                    getView().onSmsCodeRequested(phoneNumber);
                    getView().hideCommonLoader();
                }, throwable -> {
                    EventBus.getDefault().post(new BlockBottomBarEvent(false));
                    onError.accept(throwable);
                });
    }

    @Override
    public void changeLoginOnServer(LocationRealm currentCountry, String currentMobileNumber, String password) {
        String fullNumber = currentCountry.getCountryCodeString() + currentMobileNumber;

        mServerCommunicator.changelogin(fullNumber, password)
                .subscribe(() -> getView().onChangesSaved(), onError);
    }
}
