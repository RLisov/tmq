package com.tamaq.courier.model.api.common;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class UserPojo {

    public String id;
    public String created;
    @SerializedName("positive_valuations")
    public int positiveValuations;
    @SerializedName("negative_valuations")
    public int negativeValuations;
    public String phone;
    @SerializedName("phone_callcenter")
    public String phoneCallcenter;
    public String name;
    public List<?> favorite;
    public String role;
    public Integer age;
    public LocationPojo country;
    public LocationPojo town;
    public TransportTypeRealm transport;
    public String workingMode;
    public Integer minReward;
    public String automaticWorkingMode;
    public String code;
    public Integer minWorkTime;
    public Integer maxServicePrice;
    public Integer minClientRating;
    public Integer workingRadius;
    public boolean blocked;
    public List<?> askroles;
    public Double balance;
    @SerializedName("bonus_balance")
    public Integer bonusBalance;
    public OrderPojo executing_order;
    private boolean is_delete;

    public static UserPojo createForProfileUpdate(UserRealm userRealm) {
        UserPojo userPojo = createForRegistration(userRealm);
        userPojo.phone = userRealm.getFullPhoneNumber();
        return userPojo;
    }

    public static UserPojo createForRegistration(UserRealm userRealm) {
        Realm realm = Realm.getDefaultInstance();
        UserPojo user = new UserPojo();
        user.name = String.format("%s %s", userRealm.getName(), userRealm.getLastName());
        user.age = userRealm.getAge();
        user.code = userRealm.getRnn();
        user.country = convertLocationRealmToPojo(RealmHelper.getCopyIfManaged(userRealm.getCountry(), realm));
        user.town = convertLocationRealmToPojo(RealmHelper.getCopyIfManaged(userRealm.getWorkCity(), realm));
        if (userRealm.getTransportType() != null) {
            user.transport = RealmHelper.getCopyIfManaged(userRealm.getTransportType(), realm);
        }
        user = fillCommonOrderSettings(user,
                UserDAO.getInstance().getCommonOrderSettingsUnmanaged(realm), realm);
        user = fillAutoRateSettings(user,
                UserDAO.getInstance().getAutoRateSettingsUnmanaged(realm), realm);
        realm.close();
        return user;
    }

    private static UserPojo fillCommonOrderSettings(UserPojo user,
                                                    CommonOrdersSettingRealm commonOrdersSettingRealm,
                                                    Realm realm) {
        user.workingMode = commonOrdersSettingRealm.getWorkType();
        user.minReward = commonOrdersSettingRealm.getMinReward();
        if (commonOrdersSettingRealm.getTransportTypeRealm() != null)
            user.transport = RealmHelper.getCopyIfManaged(
                    commonOrdersSettingRealm.getTransportTypeRealm(), realm);
        return user;
    }

    private static UserPojo fillAutoRateSettings(UserPojo user,
                                                 AutoRateSettingRealm autoRateSettingRealm,
                                                 Realm realm) {
        user.automaticWorkingMode = autoRateSettingRealm.getAutomaticWorkingMode();
        user.minWorkTime = autoRateSettingRealm.getMaximumWorkTime();
        user.maxServicePrice = autoRateSettingRealm.getMaxServicePrice();
        user.minClientRating = autoRateSettingRealm.getMinimumClientRating();
        user.workingRadius = autoRateSettingRealm.getWorkingRadius();
        if (autoRateSettingRealm.getWorkRegion() != null && user.town == null)
            user.town = convertLocationRealmToPojo(RealmHelper.getCopyIfManaged(autoRateSettingRealm.getWorkRegion(), realm));
        return user;
    }

    private static LocationPojo convertLocationRealmToPojo(LocationRealm locationRealm) {
        LocationPojo result = convertBasicLocationRealmToPojo(locationRealm);
//        result.setChildes(convertLocationListRealmToPojo(locationRealm.getChildes()));
        return result;
    }

    private static List<LocationPojo> convertLocationListRealmToPojo(List<LocationRealm> locationsRealm) {
        List<LocationPojo> result = new ArrayList<>();
        for (LocationRealm locationRealm : locationsRealm) {
            result.add(convertBasicLocationRealmToPojo(locationRealm));
        }
        return result;
    }

    private static LocationPojo convertBasicLocationRealmToPojo(LocationRealm locationRealm) {
        LocationPojo result = new LocationPojo();
        result.setKey(locationRealm.getKey());
        result.setType(locationRealm.getType());
        result.setValueEn(locationRealm.getValueEn());
        result.setValueKz(locationRealm.getValueKz());
        result.setValueRu(locationRealm.getValueRu());
        return result;
    }

    public static UserPojo createForUpdateAutoRateParams(AutoRateSettingRealm autoRateSettingRealm,
                                                         Realm realm) {
        UserPojo user = new UserPojo();
        return fillAutoRateSettings(user, autoRateSettingRealm, realm);
    }

    public static UserPojo createForUpdateCommonOrderSettings(CommonOrdersSettingRealm commonOrdersSettingRealm,
                                                              Realm realm) {
        UserPojo user = new UserPojo();
        return fillCommonOrderSettings(user, commonOrdersSettingRealm, realm);
    }

    public String getFirstName() {
        return getPartNameString(0);
    }

    public String getLastName() {
        return getPartNameString(1);
    }

    private String getPartNameString(int part) {
        try {
            return name.trim().replaceAll(" {2,}", " ").split(" ")[part];
        } catch (Exception e) {
            return name;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getPositiveValuations() {
        return positiveValuations;
    }

    public void setPositiveValuations(int positiveValuations) {
        this.positiveValuations = positiveValuations;
    }

    public int getNegativeValuations() {
        return negativeValuations;
    }

    public void setNegativeValuations(int negativeValuations) {
        this.negativeValuations = negativeValuations;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoneCallcenter() {
        return phoneCallcenter;
    }

    public void setPhoneCallcenter(String phoneCallcenter) {
        this.phoneCallcenter = phoneCallcenter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<?> getFavorite() {
        return favorite;
    }

    public void setFavorite(List<?> favorite) {
        this.favorite = favorite;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocationPojo getCountry() {
        return country;
    }

    public void setCountry(LocationPojo country) {
        this.country = country;
    }

    public LocationPojo getTown() {
        return town;
    }

    public void setTown(LocationPojo town) {
        this.town = town;
    }

    public TransportTypeRealm getTransport() {
        return transport;
    }

    public void setTransport(TransportTypeRealm transport) {
        this.transport = transport;
    }

    public String getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(String workingMode) {
        this.workingMode = workingMode;
    }

    public Integer getMinReward() {
        return minReward;
    }

    public void setMinReward(Integer minReward) {
        this.minReward = minReward;
    }

    public String getAutomaticWorkingMode() {
        return automaticWorkingMode;
    }

    public void setAutomaticWorkingMode(String automaticWorkingMode) {
        this.automaticWorkingMode = automaticWorkingMode;
    }

    public Integer getMaxWorkTime() {
        return minWorkTime;
    }

    public void setMaxWorkTime(Integer maxWorkTime) {
        this.minWorkTime = maxWorkTime;
    }

    public Integer getMaxServicePrice() {
        return maxServicePrice;
    }

    public void setMaxServicePrice(Integer maxServicePrice) {
        this.maxServicePrice = maxServicePrice;
    }

    public Integer getMinClientRating() {
        return minClientRating;
    }

    public void setMinClientRating(Integer minClientRating) {
        this.minClientRating = minClientRating;
    }

    public Integer getWorkingRadius() {
        return workingRadius;
    }

    public void setWorkingRadius(Integer workingRadius) {
        this.workingRadius = workingRadius;
    }

    public List<?> getAskroles() {
        return askroles;
    }

    public void setAskroles(List<?> askroles) {
        this.askroles = askroles;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Integer getBonusBalance() {
        return bonusBalance;
    }

    public void setBonusBalance(Integer bonusBalance) {
        this.bonusBalance = bonusBalance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isDeleted() {
        return is_delete;
    }

    public int getRatingPercent() {
        int positive = getPositiveValuations();
        int negative = getNegativeValuations();
        int total = positive + negative;
        return (positive * 100) / total;
    }

    public OrderPojo getExecutingOrder() {
        return executing_order;
    }

    public boolean isExecutingOrder() {
        return executing_order != null && executing_order.getId() != null;
    }

}
