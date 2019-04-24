package com.tamaq.courier.presenters.code_verification;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.events.BlockBottomBarEvent;
import com.tamaq.courier.model.api.response.ApiError;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.main.MainActivity;
import com.tamaq.courier.presenters.registration.RegistrationFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.PrefsHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;


public class CodeVerificationFragment extends BaseFragment implements CodeVerificationContract.View {

    private static final String ARG_PHONE_NUMBER = "arg_phone_number";
    private static final String ARG_FROM_WHICH_SCREEN = "arg_from_which_screen";
    private static final int CODE_VALID_ITEMS_COUNT = 4;

    @Inject
    CodeVerificationContract.Presenter codeVerificationPresenter;
    @BindView(R.id.codeEditText)
    EditText codeEditText;
    @BindView(R.id.registrationButton)
    Button registrationButton;
    @BindView(R.id.codeSendToYouNumberTextView)
    TextView codeSendToYouNumberTextView;
    @BindView(R.id.confirmButton)
    Button confirmButton;
    @BindView(R.id.sendSmsAgainButton)
    Button sendSmsAgainButton;
    private FromWhichScreen mFromWhichScreen;
    private Listener mListener;

    public static CodeVerificationFragment newInstance(String phoneNumber) {
        return newInstance(phoneNumber, FromWhichScreen.LOGIN);
    }

    public static CodeVerificationFragment newInstance(String phoneNumber, FromWhichScreen from) {
        Bundle args = new Bundle();
        args.putString(ARG_PHONE_NUMBER, phoneNumber);
        args.putSerializable(ARG_FROM_WHICH_SCREEN, from);

        CodeVerificationFragment fragment = new CodeVerificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        codeVerificationPresenter.attachPresenter(this);
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_code_verification, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        mFromWhichScreen = (FromWhichScreen) getArguments().getSerializable(ARG_FROM_WHICH_SCREEN);
        setUpToolbar();
        setUpViews();
        return rootView;
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.enter_code);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_dark);
        }
    }

    private void setUpViews() {
        codeSendToYouNumberTextView.setText(getString(R.string.code_send_to_you_number,
                getArguments().getString(ARG_PHONE_NUMBER)));
        RxTextView.textChanges(codeEditText).subscribe(
                text -> confirmButton.setEnabled(text.length() >= CODE_VALID_ITEMS_COUNT));

        final String phoneNumber = getArguments().getString(ARG_PHONE_NUMBER);

        confirmButton.setOnClickListener(v -> {
            String password = codeEditText.getText().toString();
            if (mFromWhichScreen == FromWhichScreen.PROFILE) {
                codeVerificationPresenter.checkSmsCodeForChange(password, phoneNumber);
            } else codeVerificationPresenter.checkSmsCode(password, phoneNumber);
        });

        disableResendButton();

        if (mFromWhichScreen == FromWhichScreen.PROFILE) {
            sendSmsAgainButton.setOnClickListener(v -> {
                codeVerificationPresenter.requestSmsCodeForChange(phoneNumber);
                disableResendButton();
            });
        } else {
            sendSmsAgainButton.setOnClickListener(v ->
                    codeVerificationPresenter.requestSmsCode(phoneNumber));
        }

        registrationButton.setOnClickListener(v ->
                replaceFragment(RegistrationFragment.newInstance(true)));

        if (mFromWhichScreen == FromWhichScreen.PROFILE) {
            registrationButton.setVisibility(View.GONE);
            setChangeToolbarColor(false);
        }
    }

    private void disableResendButton() {
        disableSmsButton();
        Observable.timer(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> enableSmsButton());
    }

    @Override
    public void onCheckSmsCodeForChangeSuccess(String password) {
        if (mFromWhichScreen == FromWhichScreen.PROFILE) {
            if (checkNotNull(mListener)) mListener.isVerificationSuccess(true, password);
            EventBus.getDefault().post(new BlockBottomBarEvent(false));
            getFragmentManager().popBackStackImmediate();
        }
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
        if (item.getItemId() == android.R.id.home) {
            EventBus.getDefault().post(new BlockBottomBarEvent(false));
            getFragmentManager().popBackStackImmediate();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        codeVerificationPresenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onCheckSmsCodeSuccess() {
        PrefsHelper.setUserAuthorized(true, getContext());
        switch (mFromWhichScreen) {
            case REGISTRATION:
                codeVerificationPresenter.updateUserRegistrationInfoToServer();
                break;

            case LOGIN:
                codeVerificationPresenter.loadUserInfo();
                break;
        }
    }

    @Override
    public void onUserInfoLoaded() {
        startActivity(MainActivity.newInstance(getContext()));
    }

    @Override
    public void showRegistrationSuccess() {
        AlertDialog alertDialog = DialogHelper.buildDialog(getActivity(), R.string.thanks_for_registration_title,
                R.string.thanks_for_registration_description, R.string.ok,
                0, (dialog, which) -> startActivity(MainActivity.newInstance(getContext())), null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void showError(ApiError apiError) {
        if (apiError.getCauseSafe().matches("user_provider_id|password")) {
            onCheckSmsCodeWrongCode();
            hideCommonLoader();
        } else //noinspection StatementWithEmptyBody
            if (apiError.getCauseSafe().equals("role")) {
                // do nothing
            } else {
                super.showError(apiError);
            }
    }

    @Override
    public void onCheckSmsCodeWrongCode() {
        DialogHelper.showDialog(getActivity(), 0, R.string.wrong_sms_code, R.string.ok, 0,
                (dialog, which) -> {
                }, null);
    }

    @Override
    public void showTooManySmsAttempts() {
        TechnicalSupportDialog technicalSupportDialog = TechnicalSupportDialog.newInstance();
        technicalSupportDialog.show(getFragmentManager(),
                technicalSupportDialog.getClass().getSimpleName());
    }

    @Override
    public void disableSmsButton() {
        sendSmsAgainButton.setEnabled(false);
    }

    @Override
    public void enableSmsButton() {
        sendSmsAgainButton.setEnabled(true);
    }

    @Override
    public void onRequestSmsCodeSuccess() {
        snackBar(rootView, R.string.sms_sent_again);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public enum FromWhichScreen {
        REGISTRATION, PROFILE, LOGIN
    }

    public interface Listener {
        void isVerificationSuccess(boolean verificationStatus, String password);
    }

    public static class TechnicalSupportDialog extends AppCompatDialogFragment {

        private Action0 onCreatedAction;

        public static TechnicalSupportDialog newInstance() {
            Bundle args = new Bundle();
            TechnicalSupportDialog fragment = new TechnicalSupportDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(AppCompatDialogFragment.STYLE_NO_TITLE, R.style.DialogFragmentTheme);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dialog_technical_support, container, false);
            TextView emailTextView = (TextView) rootView.findViewById(R.id.emailText);
            emailTextView.setOnClickListener(v11 -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: " + emailTextView.getText().toString()));
                startActivity(Intent.createChooser(emailIntent, ""));
                dismiss();
            });
            rootView.findViewById(R.id.positiveButton).setOnClickListener(v -> dismiss());
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (onCreatedAction != null)
                onCreatedAction.call();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                int width = ViewGroup.LayoutParams.WRAP_CONTENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
            }
        }

        public void setOnCreatedAction(Action0 onCreatedAction) {
            this.onCreatedAction = onCreatedAction;
        }
    }
}
