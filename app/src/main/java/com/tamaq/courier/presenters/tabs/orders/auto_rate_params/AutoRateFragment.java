package com.tamaq.courier.presenters.tabs.orders.auto_rate_params;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.SpinnerTwoLinedAdapter;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.ui.CheckableSingleTextItemWithValue;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.region_select.RegionSelectActivity;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DialogHelper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AutoRateFragment extends BaseFragment implements AutoRateContract.View {

    private static final int REQUEST_CODE_SELECT_REGION = 10;

    @Inject
    AutoRateContract.Presenter presenter;

    @BindView(R.id.allCityView)
    View allCityView;
    @BindView(R.id.aroundMeView)
    View aroundMeView;
    @BindView(R.id.singleRegionView)
    View singleRegionView;
    @BindView(R.id.allCityRadioButton)
    RadioButton allCityRadioButton;
    @BindView(R.id.aroundMeRadioButton)
    RadioButton aroundMeRadioButton;
    @BindView(R.id.singleRegionRadioButton)
    RadioButton singleRegionRadioButton;
    @BindView(R.id.timeSpinner)
    Spinner timeSpinner;
    @BindView(R.id.paymentSpinner)
    Spinner paymentSpinner;
    @BindView(R.id.ratingSpinner)
    Spinner ratingSpinner;
    @BindView(R.id.radiusSpinner)
    Spinner radiusSpinner;
    @BindView(R.id.regionSelectView)
    View regionSelectView;
    @BindView(R.id.regionTextView)
    TextView regionTextView;
    @BindView(R.id.radiusSpinnerDivider)
    View radiusSpinnerDivider;
    @BindView(R.id.separateDistrictTextView)
    TextView separateDistrictTextView;

    SpinnerTwoLinedAdapter<CheckableSingleTextItemWithValue> timeSpinnerAdapter;
    SpinnerTwoLinedAdapter<CheckableSingleTextItemWithValue> paymentSpinnerAdapter;
    SpinnerTwoLinedAdapter<CheckableSingleTextItemWithValue> ratingSpinnerAdapter;
    SpinnerTwoLinedAdapter<CheckableSingleTextItemWithValue> radiusSpinnerAdapter;

    private LocationRealm mSelectedRegion;

    public static AutoRateFragment newInstance() {
        Bundle args = new Bundle();
        AutoRateFragment fragment = new AutoRateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_auto_rate_params, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        setUpToolbar();
        setUpViews();

        return rootView;
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_auto_rate_params);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpViews() {
        AutoRateSettingRealm autoRateSettingRealm = presenter.getAutoRateSetting();

        View.OnClickListener radioGroupsClickListener = v -> {
            ViewGroup viewGroup = (ViewGroup) v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View innerView = viewGroup.getChildAt(i);
                if (innerView instanceof RadioButton) {
                    ((RadioButton) innerView).setChecked(true);
                }
            }
        };
        allCityView.setOnClickListener(radioGroupsClickListener);
        aroundMeView.setOnClickListener(radioGroupsClickListener);
        singleRegionView.setOnClickListener(radioGroupsClickListener);

        singleRegionView.setEnabled(false);
        singleRegionRadioButton.setEnabled(false);
        singleRegionView.setClickable(false);
        singleRegionView.setAlpha(0.5f);
        separateDistrictTextView.setText(getText(R.string.cant_choose_separate_district));

        CompoundButton.OnCheckedChangeListener radioGroupsCheckedChangeListener =
                (buttonView, isChecked) -> {
                    if (!isChecked) return;
                    unCheckIfNotEquals(buttonView, allCityRadioButton);
                    unCheckIfNotEquals(buttonView, aroundMeRadioButton);
                    unCheckIfNotEquals(buttonView, singleRegionRadioButton);

                    switch (buttonView.getId()) {
                        case R.id.allCityRadioButton:
                            radiusSpinnerDivider.setVisibility(View.GONE);
                            radiusSpinner.setVisibility(View.GONE);
                            regionSelectView.setVisibility(View.GONE);
                            presenter.resetSettingsToTown(mSelectedRegion);
                            presenter.updateSelectedGeographyType(
                                    AutoRateSettingRealm.AutomaticWorkingMode.FULL_TOWN.getValue());
                            break;

                        case R.id.aroundMeRadioButton:
                            radiusSpinnerDivider.setVisibility(View.VISIBLE);
                            radiusSpinner.setVisibility(View.VISIBLE);
                            regionSelectView.setVisibility(View.GONE);
                            presenter.updateSelectedGeographyType(
                                    AutoRateSettingRealm.AutomaticWorkingMode.AROUND_ME.getValue());
                            break;

                        case R.id.singleRegionRadioButton:
                            radiusSpinnerDivider.setVisibility(View.GONE);
                            radiusSpinner.setVisibility(View.GONE);
                            regionSelectView.setVisibility(View.VISIBLE);
                            presenter.updateSelectedGeographyType(
                                    AutoRateSettingRealm.AutomaticWorkingMode.SEPARATE_DISTRICT.getValue());

                            if (!checkNotNull(mSelectedRegion)) {
                                AlertDialog alertDialog = DialogHelper.buildDialog(getActivity(),
                                        R.string.single_region,
                                        R.string.choose_region_for_orders,
                                        R.string.choose_region,
                                        R.string.cancel,
                                        (dialog, which) -> startRegionActivity(),
                                        (dialog, which) -> allCityRadioButton.setChecked(true));
                                alertDialog.setCanceledOnTouchOutside(false);
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                            break;
                    }
                };
        switch (AutoRateSettingRealm.AutomaticWorkingMode.getTypeByValue(
                autoRateSettingRealm.getAutomaticWorkingMode())) {
            case FULL_TOWN:
                allCityRadioButton.setChecked(true);
                radiusSpinnerDivider.setVisibility(View.GONE);
                radiusSpinner.setVisibility(View.GONE);
                regionSelectView.setVisibility(View.GONE);
                break;

            case AROUND_ME:
                aroundMeRadioButton.setChecked(true);
                radiusSpinnerDivider.setVisibility(View.VISIBLE);
                radiusSpinner.setVisibility(View.VISIBLE);
                regionSelectView.setVisibility(View.GONE);
                break;

            case SEPARATE_DISTRICT:
                singleRegionRadioButton.setChecked(true);
                radiusSpinnerDivider.setVisibility(View.GONE);
                radiusSpinner.setVisibility(View.GONE);
                regionSelectView.setVisibility(View.VISIBLE);
                break;
        }

        allCityRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);
        aroundMeRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);
        singleRegionRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);

        timeSpinnerAdapter = new SpinnerTwoLinedAdapter<>(getContext());
        timeSpinnerAdapter.setTitle(getString(R.string.maximum_delivery_time));
        timeSpinnerAdapter.setSelectedAction(item ->
                presenter.updateSelectedMinTime(Integer.valueOf(item.getValue())));
        timeSpinner.setAdapter(timeSpinnerAdapter);
        timeSpinnerAdapter.setSpinner(timeSpinner);
        timeSpinnerAdapter.setObjects(presenter.loadDeliveryTimeValues());
        timeSpinnerAdapter.selectItemById(autoRateSettingRealm.getMaximumWorkTime());

        paymentSpinnerAdapter = new SpinnerTwoLinedAdapter<>(getContext());
        paymentSpinnerAdapter.setTitle(getString(R.string.maximum_kitchen_payment));
        paymentSpinnerAdapter.setSelectedAction(item ->
                presenter.updateMaxPayment(Integer.valueOf(item.getValue())));
        paymentSpinner.setAdapter(paymentSpinnerAdapter);
        paymentSpinnerAdapter.setSpinner(paymentSpinner);
        paymentSpinnerAdapter.setObjects(presenter.loadKitchenPaymentValues());
        paymentSpinnerAdapter.selectItemById(autoRateSettingRealm.getMaxServicePrice());

        ratingSpinnerAdapter = new SpinnerTwoLinedAdapter<>(getContext());
        ratingSpinnerAdapter.setTitle(getString(R.string.minimum_client_rating));
        ratingSpinnerAdapter.setSelectedAction(item ->
                presenter.updateMaxClientRating(Integer.valueOf(item.getValue())));
        ratingSpinner.setAdapter(ratingSpinnerAdapter);
        ratingSpinnerAdapter.setSpinner(ratingSpinner);
        ratingSpinnerAdapter.setObjects(presenter.loadRatingValues());
        ratingSpinnerAdapter.selectItemById(autoRateSettingRealm.getMinimumClientRating());

        radiusSpinnerAdapter = new SpinnerTwoLinedAdapter<>(getContext());
        radiusSpinnerAdapter.setTitle(getString(R.string.working_radius));
        radiusSpinnerAdapter.setSelectedAction(item ->
                presenter.updateSelectedRadius(Integer.valueOf(item.getValue())));
        radiusSpinner.setAdapter(radiusSpinnerAdapter);
        radiusSpinnerAdapter.setSpinner(radiusSpinner);
        radiusSpinnerAdapter.setObjects(presenter.loadRadiuses());
        radiusSpinnerAdapter.selectItemById(autoRateSettingRealm.getWorkingRadius());

        if (autoRateSettingRealm.getWorkRegion() != null) {
            mSelectedRegion = presenter.getRegionByKey(autoRateSettingRealm.getWorkRegion().getKey());
        }
        presenter.checkIfSeparateRegionAvailable(mSelectedRegion.getKey());
        updateSelectedRegionView();
        regionSelectView.setOnClickListener(v -> startRegionActivity());
    }

    private void unCheckIfNotEquals(CompoundButton first, RadioButton second) {
        if (!first.equals(second)) second.setChecked(false);
    }

    private void startRegionActivity() {
        startActivityForResult(RegionSelectActivity.newInstance(getContext(),
                mSelectedRegion != null ? mSelectedRegion.getKey() : ""),
                REQUEST_CODE_SELECT_REGION);
    }

    private void updateSelectedRegionView() {
        if (mSelectedRegion != null) regionTextView.setText(mSelectedRegion.getValueRu());
        else regionTextView.setText(getString(R.string.not_selected));
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
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        presenter.sendOrdersSettingsToServer();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onNeedUpdateToolbarState(UserDAO.getInstance().isActiveStatus());
    }

    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        getSupportActionBar().setHomeAsUpIndicator(activeStatus
                ? R.drawable.ic_arrow_back_white
                : R.drawable.ic_arrow_back_dark);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_REGION) {
                String regionKey = data.getStringExtra(RegionSelectActivity.RESULT_SELECTED_REGION_KEY);
                mSelectedRegion = presenter.getRegionByKey(regionKey);
                presenter.updateSelectedRegionKey(regionKey);
                updateSelectedRegionView();
            }
        } else allCityRadioButton.setChecked(true);
    }

    @Override
    public void onAutoRateSettingsUpdated() {
        getActivity().finish();
    }

    @Override
    public void onRegionsAvailable() {
        separateDistrictTextView.setText(getString(R.string.single_region_description));
        singleRegionRadioButton.setEnabled(true);
        singleRegionView.setEnabled(true);
        singleRegionView.setClickable(true);
        singleRegionView.setAlpha(1);
    }
}
