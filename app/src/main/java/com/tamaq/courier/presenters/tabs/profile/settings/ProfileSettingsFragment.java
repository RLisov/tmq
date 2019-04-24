package com.tamaq.courier.presenters.tabs.profile.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.cocosw.bottomsheet.BottomSheet;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.events.BlockBottomBarEvent;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.code_verification.CodeVerificationFragment;
import com.tamaq.courier.presenters.registration.city.CityFragment;
import com.tamaq.courier.presenters.registration.country.CountryFragment;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.PhotoHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileSettingsFragment extends BaseFragment implements ProfileSettingsContract.View {

    @Inject
    ProfileSettingsContract.Presenter presenter;

    @BindView(R.id.userAvatarImage)
    CircleImageView userAvatarImage;

    @BindView(R.id.countryCode)
    EditText countryCodeEditText;
    @BindView(R.id.mobile)
    EditText mobileEditText;

    @BindView(R.id.workingCity)
    EditText workCityEditText;

    @BindView(R.id.transportSpinner)
    Spinner transportSpinner;

    @BindView(R.id.saveChangesButton)
    Button saveChangesButton;

    private int mMenu = R.menu.camer_menu_new;

    private LocationRealm mCurrentCountry;
    private String mOriginalCountryKey = "";
    private String mSelectedCountryKey = "";

    private LocationRealm mCurrentCity;
    private String mOriginalCityName;
    private String mSelectedWorkCityKey = "";

    private String mOriginalPicturePath;
    private String mCurrentPicturePath;

    private String mMobileNumber;
    private boolean isFieldsInitialized;
    private String mOriginalTransportType;
    private int mCurrentAvatarVersion;

    public ProfileSettingsFragment() {
    }

    public static ProfileSettingsFragment newInstance() {
        return new ProfileSettingsFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (checkNotNull(mCurrentCountry)) {
            countryCodeEditText.setText(mCurrentCountry.getCountryCodeString());
        }

        workCityEditText.setText(mCurrentCity != null
                ? mCurrentCity.getValueRu()
                : "");

        if (checkNotNull(mCurrentCity)) {
            enableSaveButton(checkNotNull(mCurrentCity)
                    && !mOriginalCityName.equals(mCurrentCity.getValueRu()));
        }
    }

    private void enableSaveButton(boolean condition) {
        saveChangesButton.setEnabled((
                (condition || !mOriginalCountryKey.equals(mSelectedCountryKey))
                        && checkNotNull(mCurrentCity))
                || !mOriginalPicturePath.equals(mCurrentPicturePath));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;

        rootView = inflater.inflate(R.layout.fragment_profile_settings, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initToolbar();
        initViews();
        initUserPicture();

        presenter.loadUserInformation();
        return rootView;
    }

    private void initToolbar() {
        initializeNavigationBar();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.profile_settings));
        setChangeToolbarColor(false);
    }

    private void initViews() {
        if (isFieldsInitialized) enableSaveButton(false);

        RxView.clicks(countryCodeEditText).subscribe(aVoid -> {
            CountryFragment countryFragment = CountryFragment.newInstance(mSelectedCountryKey);
            countryFragment.setCountrySelectedAction(country -> {
                mCurrentCountry = country;
                mSelectedCountryKey = country.getKey();
                mCurrentCity = null;
                mSelectedWorkCityKey = "";
            });
            replaceFragment(countryFragment);
        });

        RxView.clicks(workCityEditText).subscribe(aVoid -> {
            CityFragment cityFragment;
            if (!mSelectedWorkCityKey.isEmpty()) {
                cityFragment = CityFragment.newInstance(mCurrentCountry.getKey(), mSelectedWorkCityKey);
            } else cityFragment = CityFragment.newInstance(mCurrentCountry.getKey());

            cityFragment.setCitySelectedAction(city -> {
                mCurrentCity = city;
                mSelectedWorkCityKey = city.getKey();
            });
            replaceFragment(cityFragment);
        });
    }

    private void initUserPicture() {
        userAvatarImage.setOnClickListener(v -> new BottomSheet.Builder(getActivity())
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
                            userAvatarImage.setImageDrawable(ContextCompat.getDrawable(getContext(),
                                    R.drawable.user_pik_80));
                            mCurrentPicturePath = "";
                            mMenu = R.menu.camer_menu_new;
                            enableSaveButton(true);
                            break;
                    }
                }).show());
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
    public void displayUserInfo(UserRealm user) {
        mOriginalPicturePath = user.getAvatarUrl();
        mCurrentPicturePath = user.getAvatarUrl();
        mCurrentAvatarVersion = user.getAvatarVersion();
        setUserPicture();

        mCurrentCountry = user.getCountry();
        mSelectedCountryKey = mCurrentCountry.getKey();
        mOriginalCountryKey = mCurrentCountry.getKey();

        mCurrentCity = user.getWorkCity();
        mOriginalCityName = user.getWorkCity().getTitleUI();
        mSelectedWorkCityKey = mCurrentCity != null
                ? mCurrentCity.getKey()
                : "";

        mMobileNumber = user.getMobile().replace(user.getCountry().getCountryCodeString(), "");

        countryCodeEditText.setText(user.getCountry().getCountryCodeString());
        workCityEditText.setText(user.getWorkCity().getTitleUI());
        mobileEditText.setText(mMobileNumber);

        RxView.clicks(saveChangesButton).subscribe(o -> {
            EventBus.getDefault().post(new BlockBottomBarEvent(true));
            saveChanges();
        });
        RxTextView.textChanges(mobileEditText).subscribe(charSequence ->
                enableSaveButton(charSequence.length() > 6
                        && !mMobileNumber.equals(charSequence.toString())));
        isFieldsInitialized = true;
    }

    private void setUserPicture() {
        if (mCurrentPicturePath != null && !mCurrentPicturePath.isEmpty()) {
            Glide.with(getContext())
                    .load(mCurrentPicturePath)
                    .asBitmap()
                    .signature(new StringSignature(String.valueOf(mCurrentAvatarVersion)))
                    .placeholder(R.drawable.user_pik_80)
                    .error(R.drawable.user_pik_80)
//                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(userAvatarImage);
            mMenu = R.menu.camer_menu_exist;
        }
    }

    @Override
    public void onSmsCodeRequested(String phoneNumber) {
        String currentMobileNumber = mobileEditText.getText().toString();
        CodeVerificationFragment codeVerificationFragment = CodeVerificationFragment.newInstance(
                countryCodeEditText.getText().toString() + currentMobileNumber,
                CodeVerificationFragment.FromWhichScreen.PROFILE);

        codeVerificationFragment.setListener((verificationStatus, password) -> {
            if (verificationStatus) {
                presenter.changeLoginOnServer(mCurrentCountry, currentMobileNumber, password);
                presenter.updateCountry(mCurrentCountry);
                presenter.updateMobile(currentMobileNumber);
                updateCommonSettings();
            }
        });

        AlertDialog alertDialog = DialogHelper.buildDialog(getActivity(),
                R.string.empty_string,
                R.string.verification_code,
                R.string.ok,
                0, (dialog, which) -> replaceFragment(codeVerificationFragment), null);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void saveChanges() {
        String currentMobileNumber = mobileEditText.getText().toString();
        if (!currentMobileNumber.equals(mMobileNumber)
                || !mOriginalCountryKey.equals(mSelectedCountryKey)) {
            presenter.requestSmsCode(countryCodeEditText.getText().toString() + currentMobileNumber);
        } else updateCommonSettings();
    }

    private void updateCommonSettings() {
        String currentTransportType = transportSpinner.getSelectedItem().toString();
        if (!mOriginalTransportType.equals(currentTransportType))
            presenter.updateTransportType(transportSpinner.getSelectedItem().toString());

        if (!mCurrentPicturePath.equals(mOriginalPicturePath)) {
            if (mCurrentPicturePath != null && !mCurrentPicturePath.isEmpty())
                presenter.updatePhoto(mCurrentPicturePath);
            else presenter.removeUserPhotoFromServer();
        }

        if (!mSelectedWorkCityKey.isEmpty() && !mOriginalCityName.equals(mCurrentCity.getTitleUI())) {
            presenter.updateCity(mCurrentCity);
        }
    }

    @Override
    public void initTransportSpinner(final List<String> transportTypes, final String selectedTransportType) {
        mOriginalTransportType = selectedTransportType;

        ArrayAdapter<String> transportSpinnerAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_element,
                transportTypes);
        transportSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportSpinner.setAdapter(transportSpinnerAdapter);

        int spinnerPosition = transportSpinnerAdapter.getPosition(selectedTransportType);
        transportSpinner.setSelection(spinnerPosition);

        transportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean mNotFirstTime;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                enableSaveButton(mNotFirstTime
                        && !transportTypes.get(position).equals(selectedTransportType));
                mNotFirstTime = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onChangesSaved() {
        snackBar(rootView, R.string.settings_saved);
        new Handler().postDelayed(() -> {
            EventBus.getDefault().post(new BlockBottomBarEvent(false));
            PhotoHelper.clearCurrentPhotoPath();
            if (getContext() != null) getFragmentManager().popBackStackImmediate();
        }, 1500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == PhotoHelper.CAMERA_KEY || requestCode == PhotoHelper.GALLERY_KEY)) {
            mCurrentPicturePath = requestCode == PhotoHelper.CAMERA_KEY
                    ? PhotoHelper.getCurrentPhotoPath()
                    : PhotoHelper.getGalleryPicturePath(getContext(), data);

            PhotoHelper.getSaveBitmapThumbToFileSingle(
                    new File(mCurrentPicturePath), getContext())
                    .subscribe(pathAndBitmapPair -> {
                        mCurrentPicturePath = pathAndBitmapPair.first;
                        setUserPicture();
                        enableSaveButton(true);
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }
}
