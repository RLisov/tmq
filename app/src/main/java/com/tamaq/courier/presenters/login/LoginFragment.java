package com.tamaq.courier.presenters.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.activities.CountrySelectActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.code_verification.CodeVerificationFragment;
import com.tamaq.courier.presenters.registration.RegistrationFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginFragment extends BaseFragment implements LoginContract.View {

    private static final String COME_FROM_REGISTRATION = "come_from_registration";

    private static final int REQUEST_CODE_SELECT_COUNTRY = 10;

    @Inject
    LoginContract.Presenter presenter;

    @BindView(R.id.phoneNumberEditText)
    EditText phoneNumberEditText;
    @BindView(R.id.getCodeAndEnterButton)
    Button getCodeAndEnterButton;
    @BindView(R.id.registrationButton)
    Button registrationButton;
    @BindView(R.id.countryCodeEditText)
    EditText countryCodeEditText;
    @BindView(R.id.timerTextView)
    TextView timerTextView;

    private LocationRealm mSelectedLocationRealm;
    private CountDownTimer mCountDownTimer;

    public static LoginFragment newInstance() {
        return newInstance(false);
    }

    public static LoginFragment newInstance(boolean comeFromRegistration) {
        Bundle args = new Bundle();
        args.putBoolean(COME_FROM_REGISTRATION, comeFromRegistration);

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        presenter.attachPresenter(this);
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.loadUserCountry();
        setUpToolbar();
        setUpViews();
        return rootView;
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.login);
            getSupportActionBar().setDisplayHomeAsUpEnabled(getArguments().getBoolean(COME_FROM_REGISTRATION));
        }
    }

    private void setUpViews() {
        RxTextView.textChanges(phoneNumberEditText)
                .subscribe(charSequence -> getCodeAndEnterButton.setEnabled(validatePhone(charSequence.toString()) && !PrefsHelper.isTimerActive(getContext())));

        registrationButton.setOnClickListener(v -> {
            if (getArguments().getBoolean(COME_FROM_REGISTRATION)) getFragmentManager().popBackStackImmediate();
            else replaceFragment(RegistrationFragment.newInstance(true));
        });

        getCodeAndEnterButton.setOnClickListener(v -> {
            if (!PrefsHelper.isTimerActive(getContext())) {
                PrefsHelper.setTimerActive(true, getContext());
                PrefsHelper.setLastMilliseconds(Calendar.getInstance().getTimeInMillis(), getContext());
                PrefsHelper.setLastTimerValue(30, getContext());
            }
            presenter.requestSmsCode(getPhoneNumberFromFields());
        });
        countryCodeEditText.setOnClickListener(v -> startActivityForResult(
                CountrySelectActivity.newInstance(getContext(), mSelectedLocationRealm.getKey()),
                REQUEST_CODE_SELECT_COUNTRY));
    }

    @Override
    public void onStart() {
        super.onStart();
        getCodeAndEnterButton.setEnabled(validatePhone(phoneNumberEditText.getText().toString()));
        checkNeedInitTimer();
    }

    private void checkNeedInitTimer() {
        if (PrefsHelper.isTimerActive(getContext())) {
            long lastMilliseconds = PrefsHelper.getLastMilliseconds(getContext()) / 1000;
            long currentTimeInMillis = Calendar.getInstance().getTimeInMillis() / 1000;
            int difference = (int) (currentTimeInMillis - lastMilliseconds);
            if (difference > 30) {
                PrefsHelper.setTimerActive(false, getContext());
                PrefsHelper.setLastTimerValue(0, getContext());
            } else {
                PrefsHelper.setLastTimerValue(30 - difference, getContext());
            }
        }

        if (PrefsHelper.isTimerActive(getContext())) {
            getCodeAndEnterButton.setEnabled(false);
            timerTextView.setVisibility(View.VISIBLE);

            long startTime = PrefsHelper.getLastTimerValue(getContext());
            mCountDownTimer = new CountDownTimer(startTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        int second = (int) millisUntilFinished / 1000;
                        String timerDigits = "00:" + (second < 10
                                ? "0" + second
                                : second);

                        if (timerTextView != null) timerTextView.setText(timerDigits);

                        if (second == 1) second = 0;
                        int finalSecond = second;
                        PrefsHelper.setLastTimerValue(finalSecond, getContext());
                    });
                }

                @Override
                public void onFinish() {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (PrefsHelper.getLastTimerValue(getContext()) == 0) {
                            timerTextView.setVisibility(View.GONE);
                            getCodeAndEnterButton.setEnabled(validatePhone(phoneNumberEditText.getText().toString()));
                            PrefsHelper.setTimerActive(false, getContext());
                        }
                    });
                }
            }.start();
        }
    }

    private boolean validatePhone(String phoneAfterCountryCode) {
        return phoneAfterCountryCode != null && phoneAfterCountryCode.length() > 6;
    }

    private String getPhoneNumberFromFields() {
        return countryCodeEditText.getText().toString() + phoneNumberEditText.getText().toString();
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(TamaqApp.get(getContext()).getAppComponent())
                .commonModule(new CommonModule())
                .build().inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onUserCountryLoaded(@NonNull LocationRealm locationRealm) {
        mSelectedLocationRealm = locationRealm;
        countryCodeEditText.setText(locationRealm.getCountryCodeString());
    }

    @Override
    public void onSmsCodeRequested(String phoneNumber) {
        replaceFragment(CodeVerificationFragment.newInstance(phoneNumber));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_COUNTRY) {
            String selectedCountryId = data.getStringExtra(CountrySelectActivity.RESULT_SELECTED_COUNTRY_ID);
            mSelectedLocationRealm = presenter.loadCountryById(selectedCountryId);
            countryCodeEditText.setText(mSelectedLocationRealm.getCountryCodeString());
        }
    }
}
