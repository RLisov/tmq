package com.tamaq.courier.presenters.base;


public interface BasePresenter<View extends BaseView> {

    void detachPresenter();

    void attachPresenter(View v);

    String getUserCurrency();

}
