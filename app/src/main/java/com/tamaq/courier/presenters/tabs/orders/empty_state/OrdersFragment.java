package com.tamaq.courier.presenters.tabs.orders.empty_state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.SpinnerTwoLinedAdapter;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.presenters.activities.AutoRateActivity;
import com.tamaq.courier.presenters.activities.WebViewInfoActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.registration.city.CityFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.utils.PrefsHelper;
import com.tamaq.courier.widgets.TwoLinedTextEdit;
import com.tamaq.courier.widgets.TwoLinedTextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action0;


public class OrdersFragment extends BaseFragment implements OrdersContract.View {

    @Inject
    OrdersContract.Presenter presenter;

    @BindView(R.id.notReceiveView)
    View notReceiveView;
    @BindView(R.id.receiveAllView)
    View receiveAllView;
    @BindView(R.id.autoRateView)
    View autoRateView;
    @BindView(R.id.notReceiveRadioButton)
    RadioButton notReceiveRadioButton;
    @BindView(R.id.receiveAllRadioButton)
    RadioButton receiveAllRadioButton;
    @BindView(R.id.autoRateRadioButton)
    RadioButton autoRateRadioButton;
    @BindView(R.id.minimumPaymentView)
    TwoLinedTextEdit minimumPaymentTextEdit;
    @BindView(R.id.transportSpinner)
    Spinner transportSpinner;
    @BindView(R.id.autoRateParamsView)
    TwoLinedTextView autoRateParamsView;
    @BindView(R.id.disabledGPSLayout)
    View disabledGPSLayout;
    @BindView(R.id.accessDeniedLayout)
    View accessDeniedLayout;
    @BindView(R.id.cityNotSupportedLayout)
    View cityNotSupportedLayout;
    @BindView(R.id.enableGpsButton)
    Button enableGpsButton;
    @BindView(R.id.cityChangeButton)
    Button cityChangeButton;
    SpinnerTwoLinedAdapter<TransportTypeRealm> transportSpinnerAdapter;
    private RadioButton mWaitingForChecked;
    private GpsReceiver gpsReceiver = new GpsReceiver();

    private Action0 userConfirmedAction = () -> DialogHelper.showDialog(getActivity(),
            R.string.congratulations_without_dot,
            R.string.you_successfully_passed_check,
            R.string.ok,
            0, null, null);


    public static OrdersFragment newInstance() {
        Bundle args = new Bundle();
        OrdersFragment fragment = new OrdersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registerGpsReceiver();
        UserDAO.getInstance().addUserConfirmedListener(userConfirmedAction);
        presenter.attachPresenter(this);
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_orders, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            setUpToolbar();
            setUpViews();

            presenter.checkIsExecutingOrder(false);
            presenter.loadTransportTypes();
            presenter.loadCommonOrderSettings();
            presenter.loadCommonOrdersSettingsFromServer();
        }

        presenter.checkUserState();

        return rootView;
    }

    @Override
    public void onUserChecked(boolean active) {
        PrefsHelper.setUserBlocked(!active, getContext());
        if (!active) onUserBlocked();
    }

    @Override
    public void onExecutingOrderStatus(boolean isExecuting) {
        float alpha = isExecuting ? 0.25f : 1f;

        notReceiveView.setAlpha(alpha);
        receiveAllView.setAlpha(alpha);
        autoRateView.setAlpha(alpha);

        notReceiveView.setClickable(!isExecuting);
        receiveAllView.setClickable(!isExecuting);
        autoRateView.setClickable(!isExecuting);

        if (isExecuting) notReceiveRadioButton.setChecked(true);
        else if (mWaitingForChecked != null) {
            mWaitingForChecked.setChecked(true);
            mWaitingForChecked = null;
        } /*else presenter.changeWorkStatusToLastActive();*/
    }

    private void onUserBlocked() {
        setUpToolbar(getString(R.string.access_denied));
        accessDeniedLayout.setVisibility(View.VISIBLE);
    }

    private void registerGpsReceiver() {
        getActivity().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.title_orders);
    }

    private void setUpToolbar(String title) {
        initializeNavigationBar();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    private void setUpViews() {
        CommonOrdersSettingRealm commonOrdersSettingRealm = presenter.loadCommonOrderSettings();

        View.OnClickListener radioGroupsClickListener = v -> {
            ViewGroup viewGroup = (ViewGroup) v;
            if (v.getId() == R.id.receiveAllView || v.getId() == R.id.autoRateView) {
                if (!presenter.getUser().isConfirmed()) {
                    showUserNotConfirmedDialog();
                    return;
                }
            }
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View innerView = viewGroup.getChildAt(i);
                if (innerView instanceof RadioButton) {
                    presenter.checkIsExecutingOrder(true);
                    mWaitingForChecked = (RadioButton) innerView;
//                    ((RadioButton) innerView).setChecked(true);
                }
            }
        };

        notReceiveView.setOnClickListener(radioGroupsClickListener);
        receiveAllView.setOnClickListener(radioGroupsClickListener);
        autoRateView.setOnClickListener(radioGroupsClickListener);

        CompoundButton.OnCheckedChangeListener radioGroupsCheckedChangeListener =
                (buttonView, isChecked) -> {
                    if (!isChecked) return;
                    unCheckIfNotEquals(buttonView, notReceiveRadioButton);
                    unCheckIfNotEquals(buttonView, receiveAllRadioButton);
                    unCheckIfNotEquals(buttonView, autoRateRadioButton);
//                    if (!buttonView.isPressed()) return;
                    switch (buttonView.getId()) {
                        case R.id.notReceiveRadioButton:
                            presenter.updateWorkType(CommonOrdersSettingRealm.WorkType.NOT_WORKING.toString());
                            break;
                        case R.id.receiveAllRadioButton:
                            presenter.updateWorkType(CommonOrdersSettingRealm.WorkType.REVIEW.toString());
                            break;
                        case R.id.autoRateRadioButton:
                            presenter.updateWorkType(CommonOrdersSettingRealm.WorkType.AUTOMATIC.toString());
                            break;
                    }
                };
        switch (CommonOrdersSettingRealm.WorkType.getByValue(commonOrdersSettingRealm.getWorkType())) {
            case NOT_WORKING:
                notReceiveRadioButton.setChecked(true);
                break;
            case REVIEW:
                receiveAllRadioButton.setChecked(true);
                break;
            case AUTOMATIC:
                autoRateRadioButton.setChecked(true);
                break;
        }

        notReceiveRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);
        receiveAllRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);
        autoRateRadioButton.setOnCheckedChangeListener(radioGroupsCheckedChangeListener);

        minimumPaymentTextEdit.setOnClickListener(v -> minimumPaymentTextEdit.requestEditTextFocus());
        minimumPaymentTextEdit.getEditTextSubtitle().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateMinPayment();
        });
        minimumPaymentTextEdit.setOnEditTextInputDoneAction(this::updateMinPayment);
        minimumPaymentTextEdit.setUpEditTextMaxLength(10);
        minimumPaymentTextEdit.setSubTitle(String.valueOf(commonOrdersSettingRealm.getMinReward()));

        transportSpinnerAdapter = new SpinnerTwoLinedAdapter<>(getContext());
        transportSpinnerAdapter.setTitle(getString(R.string.transport));
        new Handler().postDelayed(() -> transportSpinnerAdapter.setSelectedAction(transportRealm ->
                presenter.updateTransportType(transportRealm)), 500);

        transportSpinnerAdapter.setSpinner(transportSpinner);
        transportSpinner.setAdapter(transportSpinnerAdapter);

        autoRateParamsView.setOnClickListener(v -> startActivity(AutoRateActivity.newInstance(getContext())));
        updateAutoRateParamsView();


        // TODO: Only for testing city disabled
        toolbar.setOnLongClickListener(v -> {
            showCityDisabled();
            return false;
        });

        enableGpsButton.setOnClickListener(v -> startActivity(
                new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        cityChangeButton.setOnClickListener(v -> replaceFragment(CityFragment.newInstance(
                presenter.getUser().getCountry().getValueRu()),
                getParentFragment().getFragmentManager()));
    }

    private void updateMinPayment() {
        minimumPaymentTextEdit.clearEditTextFocus(getActivity());
        try {
            int value = Integer.parseInt(minimumPaymentTextEdit.getEditTextSubtitle().getText().toString());
            presenter.updateMinPayment(value);
        } catch (Exception ignored) {
        }
    }

    private void showUserNotConfirmedDialog() {
        DialogHelper.showDialog(getActivity(),
                0,
                R.string.you_can_receive_order_after_confirmation,
                R.string.ok,
                0, null, null);
    }

    private void unCheckIfNotEquals(CompoundButton first, RadioButton second) {
        if (!first.equals(second)) second.setChecked(false);
    }

    private void updateAutoRateParamsView() {
        autoRateParamsView.setSubTitle(getString(presenter.isAutoRateDefault()
                ? R.string.by_default
                : R.string.configured));
    }

    private void showCityDisabled() {
        disabledGPSLayout.setVisibility(View.GONE);
        cityNotSupportedLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(R.string.not_supported_city_title);
        { // todo remove this two lines later, when checkCityEnabled() will work
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), R.color.black_87));
        }

        if (menu != null) menu.findItem(R.id.actionInfo).setVisible(false);
    }

    @Override
    public void onDestroyView() {
        unRegisterGpsReceiver();
        UserDAO.getInstance().removeUserConfirmedListener(userConfirmedAction);
        super.onDestroyView();
    }

    private void unRegisterGpsReceiver() {
        getActivity().unregisterReceiver(gpsReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            updateAutoRateParamsView();
            checkCityAndGpsEnabled();
        }
    }

    private boolean checkCityAndGpsEnabled() {
        if (!HelperCommon.isLocationEnabledByMax(getContext())) {
            showGpsDisabled();
            return false;
        }
        if (!checkCityEnabled()) {
            showCityDisabled();
            return false;
        }
        showCityAndGpsEnabled();
        return true;
    }

    private void showGpsDisabled() {
        disabledGPSLayout.setVisibility(View.VISIBLE);
        cityNotSupportedLayout.setVisibility(View.GONE);
        getSupportActionBar().setTitle(R.string.disabled_geolocation_title);
        if (menu != null) menu.findItem(R.id.actionInfo).setVisible(false);
    }

    /**
     * Todo check city enabled or not
     */
    private boolean checkCityEnabled() {
        return true;
    }

    private void showCityAndGpsEnabled() {
        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            disabledGPSLayout.setVisibility(View.GONE);
            cityNotSupportedLayout.setVisibility(View.GONE);
            getSupportActionBar().setTitle(R.string.title_orders);
            if (menu != null) menu.findItem(R.id.actionInfo).setVisible(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.orders_menu, menu);
        onNeedUpdateToolbarState(UserDAO.getInstance().isActiveStatus());
    }

    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        if (menu == null) return;
        menu.findItem(R.id.actionInfo).setIcon(activeStatus
                ? R.drawable.ic_info_app_bar : R.drawable.ic_info_dark);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionInfo:
                startActivity(WebViewInfoActivity.newInstance(
                        getContext(), getString(R.string.html_demo), WebViewInfoActivity.Type.INFO));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    protected boolean updateToolbarState() {
        if (!checkCityAndGpsEnabled()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), R.color.black_87));
            return false;
        }
        return super.updateToolbarState();
    }

    @Override
    public void displayTransportTypes(List<TransportTypeRealm> objects) {
        transportSpinnerAdapter.setObjects(objects);
        CommonOrdersSettingRealm commonOrdersSettingRealm = presenter.loadCommonOrderSettings();
        TransportTypeRealm transportTypeRealm = commonOrdersSettingRealm.getTransportTypeRealm();
        if (transportTypeRealm != null)
            transportSpinnerAdapter.selectItemById(
                    commonOrdersSettingRealm.getTransportTypeRealm().getKey());
    }

    @Override
    public void onCommonOrdersSettingsLoaded() {
        setUpViews();
    }

    private class GpsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateToolbarState();
        }
    }
}
