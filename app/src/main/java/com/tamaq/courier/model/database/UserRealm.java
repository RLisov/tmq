package com.tamaq.courier.model.database;


import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

import static com.tamaq.courier.utils.HelperCommon.checkNotNull;
import static com.tamaq.courier.utils.HelperCommon.checkNotZero;

public class UserRealm extends RealmObject {

    public static final String EXECUTOR_ROLE = "Executor";

    private static UserRealm mUser;

    @PrimaryKey
    private String mId;
    private String mAvatarUrl;
    private String mName;
    private String mLastName;
    private String mMobile;
    private String mRnn;
    private int mAge;
    private LocationRealm mCountryRealm;
    private LocationRealm mWorkCityRealm;
    private TransportTypeRealm mTransportType;
    private String mCurrentOrderId;
    private int mPositiveValuations;
    private int mNegativeValuations;
    private String mPhoneCallcenter;
    private double mBalance;
    @SerializedName("bonus_balance")
    private int mBonusBalance;

    private int mAvatarVersion;


    @Ignore
    private HashMap<IdentificationPhoto, Bitmap> mIdentificationPhotos = new HashMap<>();
    @Ignore
    private String mPassportPhoto1Path;
    @Ignore
    private String mPassportPhoto2Path;
    @Ignore
    private LastLocation mLastLocation;
    @Ignore
    private Bitmap mAvatar;
    @Ignore
    private String mAvatarPhotoPath;


    /**
     * Is the user verified? After registration, it is necessary for the administration to verify
     * the user and confirm it. Initially, this field is false. After confirmation, it will be true
     */
    private boolean mIsConfirmed;

    public UserRealm() {
    }

    public static UserRealm getInstance() {
        if (mUser == null) mUser = new UserRealm();
        return mUser;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }

    public void addIdentificationPhotoByKey(IdentificationPhoto key, Bitmap bitmap) {
        mIdentificationPhotos.put(key, bitmap);
    }

    public void removeIdentificationPhotoByKey(IdentificationPhoto key) {
        if (mIdentificationPhotos != null && mIdentificationPhotos.containsKey(key)) {
            mIdentificationPhotos.remove(key);
        }
    }

    public boolean isValidate() {
        return checkNotNull(getName())
                && checkNotNull(getLastName())
                && checkNotNull(getAvatar())
                && checkNotZero(getAge())
                && checkNotNull(getRnn())
                && checkNotNull(getMobile())
                && checkNotNull(getCountry())
                && checkNotNull(getWorkCity())
                && checkNotNull(getTransportType())
                && getIdentificationPhotos().size() == 2;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public Bitmap getAvatar() {
        return mAvatar;
    }

    public void setAvatar(Bitmap avatar) {
        mAvatar = avatar;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public String getRnn() {
        return mRnn;
    }

    public void setRnn(String rnn) {
        mRnn = rnn;
    }

    public String getMobile() {
        return mMobile;
    }

    public void setMobile(String mobile) {
        mMobile = mobile;
    }

    public LocationRealm getCountry() {
        return mCountryRealm;
    }

    public void setCountry(LocationRealm locationRealm) {
        mCountryRealm = locationRealm;
    }

    public LocationRealm getWorkCity() {
        return mWorkCityRealm;
    }

    public void setWorkCity(LocationRealm workCityRealm) {
        mWorkCityRealm = workCityRealm;
    }

    public TransportTypeRealm getTransportType() {
        return mTransportType;
    }

    public void setTransportType(TransportTypeRealm transportType) {
        mTransportType = transportType;
    }

    public HashMap<IdentificationPhoto, Bitmap> getIdentificationPhotos() {
        return mIdentificationPhotos;
    }

    public void setIdentificationPhotos(HashMap<IdentificationPhoto, Bitmap> identificationPhotos) {
        mIdentificationPhotos = identificationPhotos;
    }

    public LastLocation getLastLocation() {
        return mLastLocation;
    }

    public void setLastLocation(LastLocation lastLocation) {
        mLastLocation = lastLocation;
    }

    public String getCurrentOrderId() {
        return mCurrentOrderId;
    }

    public void setCurrentOrderId(String currentOrderId) {
        mCurrentOrderId = currentOrderId;
    }

    public boolean isConfirmed() {
        return mIsConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        mIsConfirmed = confirmed;
    }

    public String getFullPhoneNumber() {
        return getCountry().getCountryCodeString() + String.valueOf(getMobile());
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getAvatarPhotoPath() {
        return mAvatarPhotoPath;
    }

    public void setAvatarPhotoPath(String avatarFilePath) {
        mAvatarPhotoPath = avatarFilePath;
    }

    public String getPassportPhoto1Path() {
        return mPassportPhoto1Path;
    }

    public void setPassportPhoto1Path(String passportPhoto1Path) {
        mPassportPhoto1Path = passportPhoto1Path;
    }

    public String getPassportPhoto2Path() {
        return mPassportPhoto2Path;
    }

    public void setPassportPhoto2Path(String passportPhoto2Path) {
        mPassportPhoto2Path = passportPhoto2Path;
    }

    public String getPhoneCallcenter() {
        return mPhoneCallcenter;
    }

    public void setPhoneCallcenter(String phoneCallcenter) {
        mPhoneCallcenter = phoneCallcenter;
    }

    public double getBalance() {
        return mBalance;
    }

    public void setBalance(double balance) {
        mBalance = balance;
    }

    public int getBonusBalance() {
        return mBonusBalance;
    }

    public void setBonusBalance(int bonusBalance) {
        mBonusBalance = bonusBalance;
    }

    public String getFirstAndLastName() {
        if (TextUtils.isEmpty(getName()) && TextUtils.isEmpty(getLastName())) return null;
        if (!TextUtils.isEmpty(getName()) && TextUtils.isEmpty(getLastName())) return getName();
        if (TextUtils.isEmpty(getName()) && !TextUtils.isEmpty(getLastName())) return getLastName();
        return String.format("%s %s", getName(), getLastName());
    }

    public int getPositiveValuationsPercents() {
        int positiveValuations = getPositiveValuations();
        int negativeValuations = getNegativeValuations();
        int totalValuations = positiveValuations + negativeValuations;
        return (int) (100 * ((float) positiveValuations / totalValuations));
    }

    public int getPositiveValuations() {
        return mPositiveValuations;
    }

    public void setPositiveValuations(int positiveValuations) {
        mPositiveValuations = positiveValuations;
    }

    public int getNegativeValuations() {
        return mNegativeValuations;
    }

    public void setNegativeValuations(int negativeValuations) {
        mNegativeValuations = negativeValuations;
    }

    public int getTotalValuations() {
        return getPositiveValuations() + getNegativeValuations();
    }

    public int getAvatarVersion() {
        return mAvatarVersion;
    }

    public void setAvatarVersion(int avatarVersion) {
        mAvatarVersion = avatarVersion;
    }

    public void reset() {
        mId = null;
        mAvatarUrl = null;
        mName = null;
        mLastName = null;
        mMobile = null;
        mRnn = null;
        mAge = 0;
        mCountryRealm = null;
        mWorkCityRealm = null;
        mTransportType = null;
        mCurrentOrderId = null;
        mPositiveValuations = 0;
        mNegativeValuations = 0;
        mPhoneCallcenter = null;
        mBalance = 0;
        mBonusBalance = 0;
        mAvatarVersion = 0;
        mIdentificationPhotos = new HashMap<>();
        mPassportPhoto1Path = null;
        mPassportPhoto2Path = null;
        mLastLocation = null;
        mAvatar = null;
        mAvatarPhotoPath = null;
    }

    public enum IdentificationPhoto {
        FIRST, SECOND
    }
}
