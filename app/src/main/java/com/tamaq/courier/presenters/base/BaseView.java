package com.tamaq.courier.presenters.base;


import android.content.Context;

import com.tamaq.courier.model.api.response.ApiError;

public interface BaseView {

//    void setPresenter(T presenter);

    Context getContext();

    void handleInternetDisabled();

    void showCommonLoader();

    void hideCommonLoader();

    void showError(String error);

    void showError(ApiError apiError);

}
