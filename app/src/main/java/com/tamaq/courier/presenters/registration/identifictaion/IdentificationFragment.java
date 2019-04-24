package com.tamaq.courier.presenters.registration.identifictaion;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.utils.PhotoHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class IdentificationFragment extends BaseFragment
        implements IdentificationContract.View, View.OnClickListener {

    private static final int FIRST_PHOTO = 2;
    private static final int SECOND_PHOTO = 3;

    @BindView(R.id.addFirstIdentPhoto)
    TextView addFirstIdentPhoto;
    @BindView(R.id.addSecondIdentPhoto)
    TextView addSecondIdentPhoto;
    @BindView(R.id.firstIdentPhoto)
    ImageView firstPhotoImageView;
    @BindView(R.id.secondIdentPhoto)
    ImageView secondPhotoImageView;
    @BindView(R.id.hintTextFirst)
    TextView firstHint;
    @BindView(R.id.hintTextSecond)
    TextView secondHint;
    @Inject
    IdentificationContract.Presenter presenter;
    private int mLastChosenPhoto;
    private boolean mFirstPhotoUploaded;
    private boolean mSecondPhotoUploaded;

    public static IdentificationFragment newInstance() {
        return new IdentificationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        presenter.attachPresenter(this);
        rootView = inflater.inflate(R.layout.fragment_identification, container, false);
        ButterKnife.bind(this, rootView);
        initializeNavigationBar();
        getSupportActionBar().setTitle(R.string.identification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_dark);
        initPhoto();

        presenter.checkIsHasOldPicture();
        return rootView;
    }

    private void initPhoto() {
        addFirstIdentPhoto.setOnClickListener(this);
        addSecondIdentPhoto.setOnClickListener(this);
        firstPhotoImageView.setOnClickListener(this);
        secondPhotoImageView.setOnClickListener(this);
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onOldFirstPicturesChecked(Bitmap photo) {
        firstPhotoImageView.setImageBitmap(photo);
        firstHint.setVisibility(View.INVISIBLE);
        addFirstIdentPhoto.setVisibility(View.INVISIBLE);
        mFirstPhotoUploaded = true;
    }

    @Override
    public void onOldSecondPicturesChecked(Bitmap photo) {
        secondPhotoImageView.setImageBitmap(photo);
        secondHint.setVisibility(View.INVISIBLE);
        addSecondIdentPhoto.setVisibility(View.INVISIBLE);
        mSecondPhotoUploaded = true;
    }

    @Override
    public void onClick(View v) {
        int menu;
        if (v.getId() == R.id.addFirstIdentPhoto || v.getId() == R.id.firstIdentPhoto) {
            mLastChosenPhoto = FIRST_PHOTO;
            if (mFirstPhotoUploaded) menu = R.menu.camer_menu_exist;
            else menu = R.menu.camer_menu_new;
        } else {
            mLastChosenPhoto = SECOND_PHOTO;
            if (mSecondPhotoUploaded) menu = R.menu.camer_menu_exist;
            else menu = R.menu.camer_menu_new;
        }

        new BottomSheet.Builder(getActivity())
                .sheet(menu)
                .listener((dialog, which) -> {
                    switch (which) {
                        case R.id.takePhoto:
                            takePhoto();
                            break;

                        case R.id.choosePhoto:
                            chooseGalleryPhoto();
                            break;

                        case R.id.delete:
                            ImageView photo;
                            TextView takePhoto;
                            TextView hint;
                            if (mLastChosenPhoto == FIRST_PHOTO) {
                                photo = firstPhotoImageView;
                                presenter.removePicture(IdentificationPicture.FIRST);
                                mFirstPhotoUploaded = false;
                                takePhoto = addFirstIdentPhoto;
                                hint = firstHint;
                            } else {
                                photo = secondPhotoImageView;
                                presenter.removePicture(IdentificationPicture.SECOND);
                                mSecondPhotoUploaded = false;
                                takePhoto = addSecondIdentPhoto;
                                hint = secondHint;
                            }

                            photo.setImageBitmap(null);
                            photo.setVisibility(View.INVISIBLE);

                            takePhoto.setVisibility(View.VISIBLE);
                            hint.setVisibility(View.VISIBLE);
                            break;
                    }
                }).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String picturePath = requestCode == PhotoHelper.CAMERA_KEY
                    ? PhotoHelper.getCurrentPhotoPath()
                    : PhotoHelper.getGalleryPicturePath(getContext(), data);

            PhotoHelper.getSaveBitmapThumbToFileSingle(new File(picturePath), getContext())
                    .subscribe(pathAndBitmap -> {
                        Bitmap identPhoto = null;
                        ImageView photo;
                        TextView hint;
                        TextView takePhoto;
                        identPhoto = pathAndBitmap.second;
                        String picturePathLocal = pathAndBitmap.first;

                        if (mLastChosenPhoto == FIRST_PHOTO) {
                            photo = firstPhotoImageView;
                            presenter.addPicture(IdentificationPicture.FIRST, identPhoto, picturePathLocal);
                            mFirstPhotoUploaded = true;
                            takePhoto = addFirstIdentPhoto;
                            hint = firstHint;
                        } else {
                            photo = secondPhotoImageView;
                            presenter.addPicture(IdentificationPicture.SECOND, identPhoto, picturePathLocal);
                            mSecondPhotoUploaded = true;
                            takePhoto = addSecondIdentPhoto;
                            hint = secondHint;
                        }

                        photo.setImageBitmap(identPhoto);
                        photo.setVisibility(View.VISIBLE);

                        takePhoto.setVisibility(View.INVISIBLE);
                        hint.setVisibility(View.INVISIBLE);
                    }, Throwable::printStackTrace);

        }
    }

}
