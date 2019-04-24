package com.tamaq.courier.presenters.tabs.profile;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.dialogs.ExitDialog;
import com.tamaq.courier.controllers.service.LocationTrackService;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.activities.SplashScreenActivity;
import com.tamaq.courier.presenters.activities.WebViewInfoActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.OrdersArchiveFragment;
import com.tamaq.courier.presenters.tabs.profile.settings.ProfileSettingsFragment;
import com.tamaq.courier.utils.PrefsHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends BaseFragment implements ProfileContract.View {

    @Inject
    ProfileContract.Presenter presenter;

    @BindView(R.id.userAvatarImage)
    CircleImageView userAvatarImage;
    @BindView(R.id.userNameTextView)
    TextView userNameTextView;
    @BindView(R.id.ratingTextView)
    TextView ratingTextView;

    @BindView(R.id.profileSettingsContainerView)
    View profileSettingsContainerView;
    //    @BindView(R.mId.paymentsMethodsContainerView)
//    View paymentsMethodsContainerView;
    @BindView(R.id.ordersArchiveContainerView)
    View ordersArchiveContainerView;
    @BindView(R.id.contactDispatcherContainerView)
    View contactDispatcherContainerView;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initializeNavigationBar();
        getSupportActionBar().setTitle(R.string.my_cabinet);
        setChangeToolbarColor(false);

        RxView.clicks(profileSettingsContainerView).subscribe(o -> replaceFragment(ProfileSettingsFragment.newInstance()));
//        RxView.clicks(paymentsMethodsContainerView).subscribe(o -> replaceFragment());
        RxView.clicks(ordersArchiveContainerView).subscribe(o -> replaceFragment(OrdersArchiveFragment.newInstance()));
//        RxView.clicks(contactDispatcherContainerView).subscribe(o -> replaceFragment());

        presenter.loadUserInformation();

        if (PrefsHelper.isUserBlocked(getContext())) {
            profileSettingsContainerView.setEnabled(false);
            profileSettingsContainerView.setAlpha(0.5f);
            ordersArchiveContainerView.setEnabled(false);
            ordersArchiveContainerView.setAlpha(0.5f);
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.information:
                startActivity(WebViewInfoActivity.newInstance(
                        getContext(),
                        getString(R.string.html_lorem_ipsum),
                        WebViewInfoActivity.Type.INFO));
                break;

            case R.id.exit:
                ExitDialog exitDialog = new ExitDialog();
                exitDialog.setListener(() -> presenter.exitFromAccount());
                exitDialog.show(getFragmentManager(), exitDialog.getClass().getSimpleName());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadUserInformation();
    }

    @Override
    public void displayUserInformation(UserRealm userRealm) {
        Glide.with(getContext())
                .load(userRealm.getAvatarUrl())
                .asBitmap()
                .signature(new StringSignature(String.valueOf(userRealm.getAvatarVersion())))
                .placeholder(R.drawable.user_pik_80)
                .error(R.drawable.user_pik_80)
//                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(userAvatarImage);
        userNameTextView.setText(userRealm.getFirstAndLastName() == null ? getString(R.string.name_not_defined) : userRealm.getFirstAndLastName());
        if (userRealm.getTotalValuations() == 0) {
            ratingTextView.setText(R.string.no_rating);
        } else {
            ratingTextView.setText(getString(
                    R.string.positive_valuations, userRealm.getPositiveValuationsPercents()));
        }

        if (userRealm.getCountry() != null) {
            onCallCenterNumberLoaded(userRealm.getCountry().getCallcenterPhone());
        }
    }

    @Override
    public void onCallCenterNumberLoaded(String number) {
        if (!TextUtils.isEmpty(number)) {
            contactDispatcherContainerView.setAlpha(1f);
            contactDispatcherContainerView.setClickable(true);
            contactDispatcherContainerView.setOnClickListener(v -> {
                RxPermissions rxPermissions = new RxPermissions(getActivity());
                if (rxPermissions.isGranted(Manifest.permission.CALL_PHONE)) {
                    callDispatcher(number);
                } else {
                    rxPermissions.request(Manifest.permission.CALL_PHONE)
                            .subscribe(isGranted -> {
                                if (isGranted) callDispatcher(number);
                            });
                }
            });
        }
    }

    private void callDispatcher(String number) {
        if (!TextUtils.isEmpty(number)) startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null)));
    }

    @Override
    public void onExitCompleted() {
        getActivity().stopService(LocationTrackService.getIntent(getContext()));
        startActivity(SplashScreenActivity.newInstance(getContext(), true));
    }
}
