package com.tamaq.courier.presenters.base;


import android.util.Log;

import com.tamaq.courier.BuildConfig;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.response.ApiError;
import com.tamaq.courier.model.api.response.ApiErrorResponse;
import com.tamaq.courier.model.database.UserRealm;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.concurrent.CancellationException;

import io.reactivex.functions.Consumer;
import io.realm.Realm;
import retrofit2.HttpException;

public abstract class BasePresenterAbs<View extends BaseView> implements BasePresenter<View> {

    private WeakReference<View> mView;
    public final Consumer<Throwable> onError = this::onErrorDefaultAction;
    private Realm mRealm;
    private String userCurrency;

    @Override
    public void attachPresenter(View v) {
        mView = new WeakReference<>(v);
    }

    public void detachPresenter() {
        if (mView != null) {
            mView.clear();
            mView = null;
        }
        if (mRealm != null) {
            mRealm.close();
            mRealm = null;
        }
    }

    public final void onErrorDefaultAction(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            try {
                String errorBody = httpException.response().errorBody().string();
                ApiErrorResponse apiErrorResponse = ApiErrorResponse.parse(errorBody);
                ApiError apiError = apiErrorResponse.firstOrNull();
                if (apiError == null) {
                    if (getView() != null) getView().showError(httpException.message());
                } else {
                    apiError.setHttpCode(httpException.code());
                    if (getView() != null) getView().showError(apiError);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ApiError apiError = new ApiError();
                apiError.setExplanation(e.getMessage());
                if (getView() != null) getView().showError(apiError);
            }
        } else if (throwable instanceof UnknownHostException) {
            if (getView() != null) getView().handleInternetDisabled();
        } else if (throwable instanceof CancellationException) {
            if (BuildConfig.DEBUG)
                Log.i("RxLifecycle", "See: https://github.com/trello/RxLifecycle/tree/2.x#unsubscription", throwable);
        } else {
            throwable.printStackTrace();

            if (getView() != null) getView().showError(throwable.getMessage());
        }
    }

    protected View getView() {
        return mView != null && mView.get() != null ? mView.get() : null;
    }

    @Override
    public String getUserCurrency() {
        if (userCurrency == null) {
            try {
                UserRealm user = UserDAO.getInstance().getUser(getRealm());
                if (user != null) userCurrency = user.getCountry().getCurrency();
            } catch (Exception e) {
                Log.e("Tamaq", "getUserCurrency: ", e);
                return "";
            }
        }
        return " " + userCurrency;
    }

    public Realm getRealm() {
        if (mRealm == null) mRealm = Realm.getDefaultInstance();
        return mRealm;
    }
}
