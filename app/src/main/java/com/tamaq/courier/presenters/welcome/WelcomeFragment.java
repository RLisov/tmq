package com.tamaq.courier.presenters.welcome;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.registration.RegistrationFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WelcomeFragment extends BaseFragment implements WelcomeContract.View {

    @BindView(R.id.enterButton)
    View enterButton;
    @BindView(R.id.registrationButton)
    View registrationButton;
    @Inject
    WelcomeContract.Presenter presenter;

    public WelcomeFragment() {
    }

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        if (rootView != null) return rootView;
        presenter.attachPresenter(this);

        rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        RxView.clicks(enterButton).subscribe(aVoid -> replaceFragment(LoginFragment.newInstance()));
        RxView.clicks(registrationButton).subscribe(aVoid ->
                replaceFragment(RegistrationFragment.newInstance()));

        return rootView;
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }
}
