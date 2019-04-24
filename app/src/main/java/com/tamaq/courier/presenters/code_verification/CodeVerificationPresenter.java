package com.tamaq.courier.presenters.code_verification;

import android.os.Handler;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PhotoHelper;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CodeVerificationPresenter extends BasePresenterAbs<CodeVerificationContract.View>
        implements CodeVerificationContract.Presenter {

    private static final int MAX_RETRY_ATTEMPTS = 4;

    private CodeVerificationFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    /**
     * Сколько раз пользователь повторно запросил отправку смс
     */
    private int requestSmsCount = 0;

    public CodeVerificationPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.mApp = app;
        this.mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(CodeVerificationContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (CodeVerificationFragment) view;
    }

    @Override
    public void checkSmsCode(String code, String mobileNumber) {
        getView().disableSmsButton();
        getView().showCommonLoader();
        launchSmsDelayTimer();
        mServerCommunicator.login(mobileNumber, code)
                .subscribe(() -> getView().onCheckSmsCodeSuccess(), onError);
    }

    @Override
    public void checkSmsCodeForChange(String code, String mobileNumber) {
        getView().disableSmsButton();
        getView().showCommonLoader();
        launchSmsDelayTimer();
        mServerCommunicator.approve(mobileNumber, code)
                .subscribe(() -> getView().onCheckSmsCodeForChangeSuccess(code), onError);
    }

    @Override
    public void requestSmsCode(String phoneNumber) {
        requestSmsCount++;
        if (requestSmsCount >= MAX_RETRY_ATTEMPTS) {
            getView().showTooManySmsAttempts();
            return;
        }
        getView().disableSmsButton();
        getView().showCommonLoader();
        launchSmsDelayTimer();

        mServerCommunicator.registrate(phoneNumber)
                .subscribe(() -> {
                    getView().hideCommonLoader();
                    getView().onRequestSmsCodeSuccess();
                }, throwable -> {
                    getView().hideCommonLoader();
                    onError.accept(throwable);
                });
    }

    @Override
    public void requestSmsCodeForChange(String phoneNumber) {
        getView().disableSmsButton();
        getView().showCommonLoader();
        launchSmsDelayTimer();
        mServerCommunicator.askapprove(phoneNumber)
                .subscribe(() -> {
                    getView().onRequestSmsCodeSuccess();
                    getView().hideCommonLoader();
                }, onError);
    }

    private void launchSmsDelayTimer() {
        new Handler().postDelayed(() -> {
            if (getView() != null) getView().enableSmsButton();
        }, 30 * 1000);
    }

    @Override
    public void updateUserRegistrationInfoToServer() {
        UserDAO.getInstance().createAutoRateSettingsIfNotExist();
        UserDAO.getInstance().createCommonOrderSettingsIfNotExist();
        UserRealm user = UserRealm.getInstance();
        getView().showCommonLoader();
        mServerCommunicator.askroleExecutor().subscribe((object) -> {
        }, onError);
        Completable.defer(() -> getUserUpdateCompletable(user))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> getView().hideCommonLoader())
                .subscribe(() -> {
                    getView().hideCommonLoader();
                    UserDAO.getInstance().saveUserInstanceToRealmInNotExist();
                    PhotoHelper.clearCurrentPhotoPath();
                    getView().showRegistrationSuccess();
                }, onError);
    }

    private Completable getUserUpdateCompletable(UserRealm user) {
        return mServerCommunicator.getUserInfo()
                .doOnSuccess(userPojo -> UserRealm.getInstance().setId(userPojo.id))
                .flatMapCompletable(s -> {
                    UserPojo userPojo = UserPojo.createForRegistration(user);
                    return mServerCommunicator.updateUserInfo(userPojo);
                })
                .andThen(Completable.concatArray(
                        mServerCommunicator.updateUserPhoto(
                                ServerCommunicator.PhotoTag.avatar,
                                new File(user.getAvatarPhotoPath())).doOnError(onError),
                        mServerCommunicator.updateUserPhoto(
                                ServerCommunicator.PhotoTag.passport1,
                                new File(user.getPassportPhoto1Path())).doOnError(onError),
                        mServerCommunicator.updateUserPhoto(
                                ServerCommunicator.PhotoTag.passport2,
                                new File(user.getPassportPhoto2Path()))).doOnError(onError)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    @Override
    public void loadUserInfo() {
        getView().showCommonLoader();
        mServerCommunicator
                .getUserInfo()
                .doFinally(() -> getView().hideCommonLoader())
                .subscribe(userPojo -> {
                    UserDAO.getInstance().saveUserDataFromPojo(userPojo, getRealm());
                    getView().onUserInfoLoaded();
                }, onError);
    }
}
