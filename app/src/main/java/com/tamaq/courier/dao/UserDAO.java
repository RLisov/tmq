package com.tamaq.courier.dao;

import com.tamaq.courier.events.OpenOrderTabEvent;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.utils.HelperCommon;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.functions.Action0;

public class UserDAO {

    public static final String TMP_USER_ID = "TMP_USER_ID";

    private List<Action0> activeStatusChangeListeners = new ArrayList<>();
    private List<Action0> userConfirmedListeners = new ArrayList<>();

    public static UserDAO getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isActiveStatus() {
        Realm realm = Realm.getDefaultInstance();
        CommonOrdersSettingRealm commonOrdersSettingRealm = getCommonOrderSettings(realm);
        if (commonOrdersSettingRealm == null) {
            realm.close();
            return false;
        }
        commonOrdersSettingRealm = realm.copyFromRealm(getCommonOrderSettings(realm));
        realm.close();
        return !commonOrdersSettingRealm.getWorkType().equals(
                CommonOrdersSettingRealm.WorkType.NOT_WORKING.toString());
    }

    public CommonOrdersSettingRealm getCommonOrderSettings(Realm realm) {
        return realm.where(CommonOrdersSettingRealm.class).findFirst();
    }

    public void setUserStatusConfirmed(Realm realm) {
        UserRealm userRealm = getUser(realm);
        if (!realm.isInTransaction()) realm.beginTransaction();
        userRealm.setConfirmed(true);
        realm.commitTransaction();
        callUserConfirmedListeners();
    }

    public UserRealm getUser(Realm realm) {
        return realm.where(UserRealm.class).findFirst();
    }

    private void callUserConfirmedListeners() {
        for (Action0 action0 : userConfirmedListeners)
            action0.call();
    }

    public void saveUserInstanceToRealmInNotExist() {
        Realm realm = Realm.getDefaultInstance();
        UserRealm userRealm = getUser(realm);
        if (userRealm == null) {
            if (!realm.isInTransaction()) realm.beginTransaction();
            userRealm = new UserRealm();

            userRealm.setMobile(UserRealm.getInstance().getMobile());
            userRealm.setAvatarUrl(UserRealm.getInstance().getAvatarPhotoPath());
            userRealm.setName(UserRealm.getInstance().getName());
            userRealm.setLastName(UserRealm.getInstance().getLastName());
            userRealm.setTransportType(UserRealm.getInstance().getTransportType());
            userRealm.setCountry(UserRealm.getInstance().getCountry());
            userRealm.setWorkCity(UserRealm.getInstance().getWorkCity());
            userRealm.setId(UserRealm.getInstance().getId());

            realm.copyToRealmOrUpdate(userRealm);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void saveUserDataFromPojo(UserPojo userPojo, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        saveUserToRealm(userPojo, realm);
        saveAutoRateSettingsToRealm(userPojo, realm);
        saveCommonOrdersSettingsToRealm(userPojo, realm);
        realm.commitTransaction();
    }

    private void saveUserToRealm(UserPojo userPojo, Realm realm) {
        UserRealm userRealm = getUser(realm);
        if (userRealm == null) {
            userRealm = new UserRealm();
            userRealm.setId(userPojo.id);
            userRealm = realm.copyToRealmOrUpdate(userRealm);
        }
        int lastAvatarVersion = userRealm.getAvatarVersion();
        userRealm = fillFromPojo(userRealm, userPojo, realm);
        userRealm.setAvatarVersion(lastAvatarVersion);
        realm.copyToRealmOrUpdate(userRealm);
    }

    private void saveAutoRateSettingsToRealm(UserPojo userPojo, Realm realm) {
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        if (autoRateSettingRealm == null) {
            autoRateSettingRealm = new AutoRateSettingRealm();
            autoRateSettingRealm = realm.copyToRealm(autoRateSettingRealm);
        }
        fillFromPojo(autoRateSettingRealm, userPojo, realm);
    }

    private void saveCommonOrdersSettingsToRealm(UserPojo userPojo, Realm realm) {
        CommonOrdersSettingRealm settings = getCommonOrderSettings(realm);
        if (settings == null) {
            settings = new CommonOrdersSettingRealm();
            settings = realm.copyToRealm(settings);
        }
        fillFromPojo(settings, userPojo, realm);
    }

    private UserRealm fillFromPojo(UserRealm userRealm, UserPojo userPojo, Realm realm) {
        if (userRealm.getId() == null)
            userRealm.setId(userPojo.id);
        userRealm.setAge(HelperCommon.getIntSafe(userPojo.age));
        userRealm.setConfirmed(userPojo.role.equals(UserRealm.EXECUTOR_ROLE));
        userRealm.setCountry(LocationDAO.getInstance().getLocationByKey(
                userPojo.country.getKey(), realm));
        userRealm.setWorkCity(LocationDAO.getInstance().getLocationByKey(
                userPojo.town.getKey(), realm));
        userRealm.setName(userPojo.getFirstName());
        userRealm.setLastName(userPojo.getLastName());
        userRealm.setAvatarUrl(formatAvatarUrl(userPojo.id));
        userRealm.setPositiveValuations(userPojo.positiveValuations);
        userRealm.setNegativeValuations(userPojo.negativeValuations);
        userRealm.setPhoneCallcenter(userPojo.phoneCallcenter);
        userRealm.setBalance(userPojo.balance);
        userRealm.setBonusBalance(userPojo.bonusBalance);
        userRealm.setMobile(userPojo.phone.replace(
                userRealm.getCountry().getCountryCodeString(), ""));
        return userRealm;
    }

    public AutoRateSettingRealm getAutoRateSettings(Realm realm) {
        return realm.where(AutoRateSettingRealm.class).findFirst();
    }

    private void fillFromPojo(AutoRateSettingRealm settings, UserPojo userPojo,
                              Realm realm) {
        settings.setWorkRegion(LocationDAO.getInstance().getLocationByKey(
                userPojo.town.getKey(), realm));
        settings.setAutomaticWorkingMode(userPojo.automaticWorkingMode);
        settings.setMaxServicePrice(HelperCommon.getIntSafe(userPojo.maxServicePrice));
        settings.setMinimumClientRating(HelperCommon.getIntSafe(userPojo.minClientRating));
        settings.setWorkingRadius(HelperCommon.getIntSafe(userPojo.workingRadius));
        settings.setMaximumWorkTime(HelperCommon.getIntSafe(userPojo.minWorkTime));
    }

    private void fillFromPojo(CommonOrdersSettingRealm settings,
                              UserPojo userPojo, Realm realm) {
        settings.setMinReward(HelperCommon.getIntSafe(userPojo.minReward));
        settings.setWorkType(userPojo.workingMode);
        settings.setTransportTypeRealm(TransportTypeDAO.getInstance().getTransportByKey(
                userPojo.transport.getKey(), realm));
    }

    private String formatAvatarUrl(String userId) {
        return String.format("https://tamaq.kz/imgs/%s_avatar_140.png", userId);
    }

    public void createAutoRateSettingsIfNotExist() {
        Realm realm = Realm.getDefaultInstance();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        if (autoRateSettingRealm == null) {
            if (!realm.isInTransaction()) realm.beginTransaction();
            autoRateSettingRealm = new AutoRateSettingRealm();
            autoRateSettingRealm.setAutomaticWorkingMode(AutoRateSettingRealm.DEFAULT_AUTOMATIC_WORKING_MODE);
            autoRateSettingRealm.setMaximumWorkTime(AutoRateSettingRealm.DEFAULT_MAXIMUM_WORK_TIME);
            autoRateSettingRealm.setMaxServicePrice(AutoRateSettingRealm.DEFAULT_MAX_SERVICE_PRICE);
            autoRateSettingRealm.setMinimumClientRating(AutoRateSettingRealm.DEFAULT_MIN_CLIENT_RATING);
            autoRateSettingRealm.setWorkingRadius(AutoRateSettingRealm.DEFAULT_WORKING_RADIUS);
            autoRateSettingRealm.setWorkRegion(null);
            realm.copyToRealm(autoRateSettingRealm);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void createCommonOrderSettingsIfNotExist() {
        Realm realm = Realm.getDefaultInstance();
        CommonOrdersSettingRealm commonOrderSettingRealm = getCommonOrderSettings(realm);
        if (commonOrderSettingRealm == null) {
            if (!realm.isInTransaction()) realm.beginTransaction();
            commonOrderSettingRealm = new CommonOrdersSettingRealm();
            commonOrderSettingRealm.setMinReward(CommonOrdersSettingRealm.DEFAULT_MIN_PAYMENT);
            UserRealm userRealm = UserDAO.getInstance().getUser(realm);
            if (userRealm != null)
                commonOrderSettingRealm.setTransportTypeRealm(
                        TransportTypeDAO.getInstance().getTransportByKey(userRealm.getTransportType().getKey(), realm));
            commonOrderSettingRealm.setWorkType(CommonOrdersSettingRealm.DEFAULT_WORK_TYPE);
            realm.copyToRealm(commonOrderSettingRealm);
            realm.commitTransaction();
        }
        realm.close();
    }

    public void updateCommonOrderSettingsWorkType(String type, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        CommonOrdersSettingRealm commonOrderSettings = getCommonOrderSettings(realm);
        String oldType = commonOrderSettings.getWorkType();
        commonOrderSettings.setWorkType(type);
        realm.commitTransaction();
        if (!oldType.equals(type)) callActiveStatusChangeListeners();
    }

    private void callActiveStatusChangeListeners() {
        for (Action0 action0 : activeStatusChangeListeners) {
            action0.call();
        }
    }

    public void updateCommonOrderSettingsMinPayment(int minPayment, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        CommonOrdersSettingRealm commonOrderSettings = getCommonOrderSettings(realm);
        commonOrderSettings.setMinReward(minPayment);
        realm.commitTransaction();
    }

    public void updateCommonOrderSettingsTransport(String transportTypeKey, Realm realm) {
        updateCommonOrderSettingsTransport(transportTypeKey, realm, true);
    }

    public void updateCommonOrderSettingsTransport(String transportTypeKey, Realm realm,
                                                   boolean manageTransaction) {
        if (manageTransaction && !realm.isInTransaction()) realm.beginTransaction();
        CommonOrdersSettingRealm commonOrderSettings = getCommonOrderSettings(realm);
        commonOrderSettings.setTransportTypeRealm(
                TransportTypeDAO.getInstance().getTransportByKey(transportTypeKey, realm));
        if (manageTransaction) realm.commitTransaction();
    }

    public AutoRateSettingRealm getAutoRateSettingsUnmanaged(Realm realm) {
        return realm.copyFromRealm(realm.where(AutoRateSettingRealm.class).findFirst());
    }

    public void setCurrentOrderId(String id, Realm realm) {
        setCurrentOrderId(id, realm, true);
    }

    public void setCurrentOrderId(String id, Realm realm, boolean manageTransaction) {
        if (manageTransaction && !realm.isInTransaction()) realm.beginTransaction();

        UserRealm userRealm = getUser(realm);
        userRealm.setCurrentOrderId(id);

        if (manageTransaction)
            realm.commitTransaction();

        EventBus.getDefault().post(new OpenOrderTabEvent());
    }

    public void updateBalance(double balance, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();

        UserRealm userRealm = getUser(realm);
        userRealm.setBalance(balance);

        if (realm.isInTransaction()) realm.commitTransaction();
    }

    public CommonOrdersSettingRealm getCommonOrderSettingsUnmanaged(Realm realm) {
        return realm.copyToRealm(realm.where(CommonOrdersSettingRealm.class).findFirst());
    }

    public void updateAutoRateGeographyType(String type, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setAutomaticWorkingMode(type);
        realm.commitTransaction();
    }

    public void updateAutoRateRegion(String regionKey, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setWorkRegion(LocationDAO.getInstance().getLocationByKey(regionKey, realm));
        realm.commitTransaction();
    }

    public void updateAutoRateRegion(String regionKey, Realm realm, boolean manageTransaction) {
        if (manageTransaction && !realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setWorkRegion(LocationDAO.getInstance().getLocationByKey(regionKey, realm));
        if (manageTransaction) realm.commitTransaction();
    }

    public void updateAutoRateRadius(int radius, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setWorkingRadius(radius);
        realm.commitTransaction();
    }

    public void updateAutoRateMinTime(int time, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setMaximumWorkTime(time);
        realm.commitTransaction();
    }

    public void updateAutoRateMaxPayment(int maxPayment, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setMaxServicePrice(maxPayment);
        realm.commitTransaction();
    }

    public void updateAutoRateMinRating(int minRating, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        AutoRateSettingRealm autoRateSettingRealm = getAutoRateSettings(realm);
        autoRateSettingRealm.setMinimumClientRating(minRating);
        realm.commitTransaction();
    }

    public void addTransportTypes(List<TransportTypeRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public void setAvatarUrlAsServerUrl(Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        UserRealm userRealm = UserRealm.getInstance();
        userRealm.setAvatarUrl(formatAvatarUrl(userRealm.getId()));
        realm.commitTransaction();
    }

    public List<TransportTypeRealm> getTransportTypes(Realm realm) {
        return realm.where(TransportTypeRealm.class).findAll();
    }

    public void updatePhoto(String avatarPath, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        UserRealm user = getUser(realm);
        user.setAvatarUrl(avatarPath);
        user.setAvatarVersion(user.getAvatarVersion() + 1);
        realm.commitTransaction();
    }

    public void updateTransportType(String transportType, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        updateCommonOrderSettingsTransport(TransportTypeDAO.getInstance().getTransportByName(transportType, realm).getKey(), realm, false);
        if (!realm.isInTransaction()) realm.beginTransaction();
    }

    public void updateCountry(LocationRealm country, Realm realm) {
        realm.executeTransaction(realm1 -> getUser(realm1).setCountry(country));
    }

    public void updateCity(LocationRealm city, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        UserRealm user = getUser(realm);
        if (!city.isManaged()) city = LocationDAO.getInstance().getLocationByKey(city.getKey(), realm);
        user.setWorkCity(city);
        realm.commitTransaction();
    }

    public void updateMobile(String mobileNumber, Realm realm) {
        realm.executeTransaction(realm1 -> getUser(realm1).setMobile(mobileNumber));
    }

    public void addActiveStatusChangeListener(Action0 action0) {
        activeStatusChangeListeners.add(action0);
    }

    public void removeActiveStatusChangeListener(Action0 action0) {
        activeStatusChangeListeners.remove(action0);
    }

    public void addUserConfirmedListener(Action0 action0) {
        userConfirmedListeners.add(action0);
    }

    public void removeUserConfirmedListener(Action0 action0) {
        userConfirmedListeners.remove(action0);
    }

    public void removeUserPhoto(Realm realm) {
        realm.executeTransaction(realm1 -> realm1.where(UserRealm.class).findFirst().setAvatarUrl(""));
    }

    public void updateCallCenterPhone(Realm realm, UserRealm user, String phoneCallcenter) {
        realm.executeTransaction(realm1 -> user.getCountry().setCallcenterPhone(phoneCallcenter));
    }

    private static class InstanceHolder {
        private static final UserDAO instance = new UserDAO();
    }
}
