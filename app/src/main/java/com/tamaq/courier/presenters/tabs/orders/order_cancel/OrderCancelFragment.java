package com.tamaq.courier.presenters.tabs.orders.order_cancel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.widgets.CheckBoxAdvanced;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderCancelFragment extends BaseFragment implements OrderCancelContract.View {

    public static final String ARG_ORDER_ID = "arg_order_id";

    @Inject
    OrderCancelContract.Presenter presenter;

    @BindView(R.id.notHaveTimeCheckBox)
    CheckBoxAdvanced notHaveTimeCheck;
    @BindView(R.id.transportOutOfOrderCheckBox)
    CheckBoxAdvanced transportOutOfOrderCheck;
    @BindView(R.id.anotherReasonCheckBox)
    CheckBoxAdvanced anotherReasonCheck;
    @BindView(R.id.cancelReasonLayout)
    TextInputLayout cancelReasonLayout;
    @BindView(R.id.cancelReasonTextInput)
    TextInputEditText cancelReasonTextInput;
    @BindView(R.id.buttonReady)
    Button buttonReady;

    private String orderId;

    public static OrderCancelFragment newInstance(String orderId) {
        Bundle args = new Bundle();
        OrderCancelFragment fragment = new OrderCancelFragment();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parseArguments();
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_order_cancel, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        setChangeToolbarColor(false);
        setUpToolbar();
        setUpViews();
        return rootView;
    }

    private void parseArguments() {
        orderId = getArguments().getString(ARG_ORDER_ID);
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.order_cancel);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.dark_sky_blue)));
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        }
    }

    @SuppressLint("CheckResult")
    private void setUpViews() {
        RxTextView.textChanges(cancelReasonTextInput).subscribe(charSequence -> {
            if (charSequence.length() > 0)
                cancelReasonLayout.setError("");
        });
        buttonReady.setOnClickListener(v -> {
            if (!validateFields())
                return;
            List<OrderRealm.CancelReason> checkedReasons = getCheckedReasonsList();
            presenter.cancelOrder(orderId, checkedReasons,
                    cancelReasonTextInput.getText().toString());
        });
    }

    private boolean validateFields() {
        if (!notHaveTimeCheck.isChecked()
                && !transportOutOfOrderCheck.isChecked()
                && !anotherReasonCheck.isChecked()) {
            snackBarLong(rootView, R.string.select_at_least_one_reason);
            return false;
        }
        if (cancelReasonTextInput.getText().length() == 0) {
            cancelReasonLayout.setError(getString(R.string.please_select_cancel_reason));
            return false;
        }
        if (cancelReasonTextInput.getText().length() > 300) {
            snackBarLong(rootView, R.string.max_length_of_comment_300);
            return false;
        }
        return true;
    }

    private List<OrderRealm.CancelReason> getCheckedReasonsList() {
        List<OrderRealm.CancelReason> list = new ArrayList<>();
        if (notHaveTimeCheck.isChecked()) list.add(OrderRealm.CancelReason.NOT_TIME);
        if (transportOutOfOrderCheck.isChecked()) list.add(OrderRealm.CancelReason.TRANSPORT_BREAK);
        if (anotherReasonCheck.isChecked()) list.add(OrderRealm.CancelReason.OTHER_REASON);
        return list;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onNeedUpdateToolbarState(true);
    }

    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        super.onNeedUpdateToolbarState(activeStatus);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        buttonReady.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
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
        if (item.getItemId() == android.R.id.home) getActivity().finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCancelOrderSuccess() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }
}
