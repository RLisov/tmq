package com.tamaq.courier.presenters.registration;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.cocosw.bottomsheet.BottomSheet;
import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.code_verification.CodeVerificationFragment;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.registration.city.CityFragment;
import com.tamaq.courier.presenters.registration.country.CountryFragment;
import com.tamaq.courier.presenters.registration.identifictaion.IdentificationFragment;
import com.tamaq.courier.presenters.registration.terms_of_use.TermsOfUseFragment;
import com.tamaq.courier.utils.PhotoHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class RegistrationFragment extends BaseFragment implements
        RegistrationContract.View, View.OnFocusChangeListener {

    private static final String SHOW_BACK_ARROW_KEY = "show_back_arrow_key";

    @BindView(R.id.photo)
    CircleImageView userPicture;


    @BindView(R.id.nameInputLayout)
    TextInputLayout nameInputLayout;
    @BindView(R.id.name)
    EditText name;

    @BindView(R.id.lastNameInputLayout)
    TextInputLayout lastNameInputLayout;
    @BindView(R.id.lastName)
    EditText lastName;

    @BindView(R.id.countryCode)
    EditText countryCode;

    @BindView(R.id.mobileInputLayout)
    TextInputLayout mobileInputLayout;
    @BindView(R.id.mobile)
    EditText mobile;

    @BindView(R.id.rnnInputLayout)
    TextInputLayout rnnInputLayout;
    @BindView(R.id.rnn)
    EditText rnn;

    @BindView(R.id.ageInputLayout)
    TextInputLayout ageInputLayout;
    @BindView(R.id.age)
    EditText age;

    @BindView(R.id.cityInputLayout)
    TextInputLayout cityInputLayout;
    @BindView(R.id.workingCity)
    EditText workCity;

    @BindView(R.id.transportSpinner)
    Spinner transportSpinner;

    @BindView(R.id.identificationInputLayout)
    TextInputLayout identificationInputLayout;
    @BindView(R.id.identification)
    EditText identification;

    @BindView(R.id.termsOfUse)
    View termsOfUse;
    @BindView(R.id.checkbox)
    CheckBox checkBox;
    @BindView(R.id.apply)
    Button applyButton;
    @BindView(R.id.registrationEnter)
    View registrationEnterView;

    @Inject
    RegistrationContract.Presenter presenter;
    private int mMenu = R.menu.camer_menu_new;

    //    private String mCountryName = "";
    private String mCountrySelectedKey = "";
    private String mSelectedWorkCityKey = "";

    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    public static RegistrationFragment newInstance(boolean needShowBackArrow) {
        Bundle args = new Bundle();
        args.putBoolean(SHOW_BACK_ARROW_KEY, needShowBackArrow);

        RegistrationFragment fragment = new RegistrationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) return rootView;

        rootView = inflater.inflate(R.layout.fragment_registration, container, false);
        presenter.attachPresenter(this);
        ButterKnife.bind(this, rootView);
        setChangeToolbarColor(false);

        initViews();
        initUserPicture();
        initEditText();
        presenter.loadTransportTypes();

        return rootView;
    }

    private void initViews() {
        initializeNavigationBar();
        if (checkNotNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(R.string.registration);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            if (checkNotNull(getArguments())) {
                boolean showBackArrow = getArguments().getBoolean(SHOW_BACK_ARROW_KEY, false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(showBackArrow);
            }
        }

        RxView.clicks(countryCode).subscribe(aVoid -> {
            CountryFragment countryFragment = CountryFragment.newInstance(mCountrySelectedKey);
            countryFragment.setCountrySelectedAction(country -> {
                presenter.selectCountry(country);
                presenter.checkUser();
                mSelectedWorkCityKey = "";
            });
            replaceFragment(countryFragment);
        });
        RxView.clicks(workCity).subscribe(aVoid -> replaceFragment(CityFragment.newInstance(mCountrySelectedKey, mSelectedWorkCityKey)));
        RxView.clicks(identification).subscribe(aVoid -> replaceFragment(IdentificationFragment.newInstance()));
        RxView.clicks(termsOfUse).subscribe(aVoid -> replaceFragment(TermsOfUseFragment.newInstance()));
        RxView.clicks(checkBox).subscribe(aVoid -> {
            checkBox.setAlpha(checkBox.isChecked() ? 1 : 0.5f);
            presenter.checkCanRegister();
        });
        RxView.clicks(applyButton).subscribe(aVoid -> presenter.requestSmsCode());
        RxView.clicks(registrationEnterView).subscribe(aVoid -> replaceFragment(LoginFragment.newInstance(true)));
    }

    private void initUserPicture() {
        userPicture.setOnClickListener(v -> new BottomSheet.Builder(getActivity())
                .sheet(mMenu)
                .listener((dialog, which) -> {
                    switch (which) {
                        case R.id.takePhoto:
                            takePhoto();
                            break;
                        case R.id.choosePhoto:
                            chooseGalleryPhoto();
                            break;
                        case R.id.delete:
                            userPicture.setImageDrawable(ContextCompat.getDrawable(getContext(),
                                    R.drawable.user_pik_80));
                            presenter.setUserAvatar(null, null);
                            presenter.checkCanRegister();
                            mMenu = R.menu.camer_menu_new;
                            break;
                    }
                }).show());
    }

    private void initEditText() {
        name.setOnFocusChangeListener(this);
        lastName.setOnFocusChangeListener(this);
        mobile.setOnFocusChangeListener(this);
        rnn.setOnFocusChangeListener(this);
        age.setOnFocusChangeListener(this);
    }

    private void takePhoto() {
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(Manifest.permission.CAMERA)
                && rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            startActivityForResult(PhotoHelper.prepareTakePhotoIntent(getContext()), PhotoHelper.CAMERA_KEY);
        } else {
            rxPermissions.request(android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            startActivityForResult(PhotoHelper.prepareTakePhotoIntent(getContext()), PhotoHelper.CAMERA_KEY);
                        }
                    });
        }
    }

    private void chooseGalleryPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startActivityForResult(intent, PhotoHelper.GALLERY_KEY);
        } else {
            rxPermissions.request(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(isGranted -> {
                        if (isGranted) {
                            startActivityForResult(intent, PhotoHelper.GALLERY_KEY);
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.checkUser();
    }

    @Override
    public void initUser(UserRealm user) {
        String countedPhotos = "";

        if (checkNotNull(user.getIdentificationPhotos())) {
            if (user.getIdentificationPhotos().size() == 2) {
                countedPhotos = getString(R.string.chosenTwoIdentificationPhoto);
                checkOnFullness(false, identificationInputLayout, "");
            } else if (user.getIdentificationPhotos().size() == 1) {
                countedPhotos = getString(R.string.chosenOneIdentificationPhoto);
                String errorString = getString(R.string.error_first_ident_photo);

                //noinspection LoopStatementThatDoesntLoop
                for (UserRealm.IdentificationPhoto identificationPhoto : user.getIdentificationPhotos().keySet()) {
                    if (identificationPhoto == UserRealm.IdentificationPhoto.FIRST)
                        errorString = getString(R.string.error_second_ident_photo);
                    break;
                }

                checkOnFullness(true, identificationInputLayout, errorString);
            }
        }
        identification.setText(countedPhotos);

        if (checkNotNull(user.getName())) name.setText(user.getName());
        if (checkNotNull(user.getLastName())) lastName.setText(user.getLastName());
        if (checkNotNull(user.getMobile())) mobile.setText(String.valueOf(user.getMobile()));
        if (checkNotNull(user.getRnn())) rnn.setText(String.valueOf(user.getRnn()));
        if (user.getAge() != 0) age.setText(String.valueOf(user.getAge()));
        if (checkNotNull(user.getAvatar())) userPicture.setImageBitmap(user.getAvatar());
        if (checkNotNull(user.getWorkCity())) {
            mSelectedWorkCityKey = user.getWorkCity().getKey();
            workCity.setText(user.getWorkCity().getValueRu());
        } else workCity.setText("");
        if (checkNotNull(user.getCountry())) {
            mCountrySelectedKey = user.getCountry().getKey();
//            mCountryName = user.getCountry().getValueRu();
            countryCode.setText(user.getCountry().getCountryCodeString());
        }
        presenter.checkCanRegister();
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

    @Override
    public void initTransportSpinner(List<String> transportTypes, String selectedTransportType) {
        ArrayAdapter<String> transportSpinnerAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_element,
                transportTypes);
        transportSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        transportSpinner.setAdapter(transportSpinnerAdapter);
        transportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                presenter.setTransportType(transportSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int spinnerPosition = transportSpinnerAdapter.getPosition(selectedTransportType);
        transportSpinner.setSelection(spinnerPosition);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {

            EditText currentEditText = (EditText) v;
            String text = currentEditText.getText().toString();
            String errorText = null;
            TextInputLayout currentLayout = null;
            boolean hasError = false;

            if (!text.isEmpty()) {
                switch (v.getId()) {
                    case R.id.name:
                        hasError = text.length() < 2;
                        errorText = getString(R.string.error_set_name);
                        currentLayout = nameInputLayout;
                        presenter.setUserName(text);
                        break;

                    case R.id.lastName:
                        hasError = text.length() < 2;
                        errorText = getString(R.string.error_set_last_name);
                        currentLayout = lastNameInputLayout;
                        presenter.setUserLastName(text);
                        break;

                    case R.id.mobile:
                        hasError = text.length() < 8;
                        errorText = getString(R.string.error_incorrect_mobile);
                        currentLayout = mobileInputLayout;
                        presenter.setUserMobile(text);
                        break;

                    case R.id.rnn:
                        hasError = text.length() < 12;
                        errorText = getString(R.string.error_incorrect_rnn);
                        currentLayout = rnnInputLayout;
                        presenter.setUserRnn(text);
                        break;

                    case R.id.age:
                        hasError = Integer.parseInt(text) < 21 || Integer.parseInt(text) > 90;
                        errorText = getString(R.string.error_incorrect_age);
                        currentLayout = ageInputLayout;
                        presenter.setUserAge(text);
                        break;
                }
            } else {
                hasError = true;
                switch (v.getId()) {
                    case R.id.name:
                        errorText = getString(R.string.error_set_name);
                        currentLayout = nameInputLayout;
                        break;

                    case R.id.lastName:
                        errorText = getString(R.string.error_set_last_name);
                        currentLayout = lastNameInputLayout;
                        presenter.setUserLastName(text);
                        break;

                    case R.id.mobile:
                        errorText = getString(R.string.error_empty_mobile);
                        currentLayout = mobileInputLayout;
                        break;

                    case R.id.rnn:
                        errorText = getString(R.string.error_empty_rnn);
                        currentLayout = rnnInputLayout;
                        break;

                    case R.id.age:
                        errorText = getString(R.string.error_empty_age);
                        currentLayout = ageInputLayout;
                        break;
                }
            }

            checkOnFullness(hasError, currentLayout, errorText);

            presenter.checkCanRegister();
        }
    }

    private void checkOnFullness(boolean hasError, TextInputLayout layout, String errorText) {
        if (layout == null) return;
        layout.setErrorEnabled(hasError);
        layout.setError(hasError ? errorText : null);
    }

    @Override
    public void displayCanRegister(boolean canRegister) {
        applyButton.setEnabled(canRegister && checkBox.isChecked());
    }

    @Override
    public void onSmsCodeRequested(String phoneNumber) {
        replaceFragment(CodeVerificationFragment.newInstance(
                phoneNumber, CodeVerificationFragment.FromWhichScreen.REGISTRATION));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PhotoHelper.CAMERA_KEY || requestCode == PhotoHelper.GALLERY_KEY) {

                String picturePath = requestCode == PhotoHelper.CAMERA_KEY
                        ? PhotoHelper.getCurrentPhotoPath()
                        : PhotoHelper.getGalleryPicturePath(getContext(), data);

                PhotoHelper.getSaveBitmapThumbToFileSingle(new File(picturePath), getContext())
                        .subscribe(pathAndBitmap -> {
                            userPicture.setImageBitmap(pathAndBitmap.second);
                            presenter.setUserAvatar(pathAndBitmap.second, pathAndBitmap.first);
                            mMenu = R.menu.camer_menu_exist;
                            presenter.checkCanRegister();
                        }, Throwable::printStackTrace);
            }
        }
    }
}
