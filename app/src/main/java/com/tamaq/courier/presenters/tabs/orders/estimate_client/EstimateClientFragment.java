package com.tamaq.courier.presenters.tabs.orders.estimate_client;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EstimateClientFragment extends BaseFragment implements EstimateClientContract.View {

    public static final String ARG_ORDER_ID = "arg_order_id";

    @Inject
    EstimateClientContract.Presenter presenter;

    @BindView(R.id.positiveImageView)
    ImageView positiveImageView;
    @BindView(R.id.positiveTextView)
    TextView positiveTextView;
    @BindView(R.id.negativeImageView)
    ImageView negativeImageView;
    @BindView(R.id.negativeTextView)
    TextView negativeTextView;
    @BindView(R.id.infoView)
    View infoView;
    @BindView(R.id.tipsSwitch)
    SwitchCompat tipsSwitch;
    @BindView(R.id.reasonLayout)
    ViewGroup reasonLayout;
    @BindView(R.id.commentTextInput)
    TextInputEditText commentTextInput;
    @BindView(R.id.commentLayout)
    TextInputLayout commentInputLayout;
    @BindView(R.id.buttonReady)
    Button buttonReady;

    private String orderId;

    public static EstimateClientFragment newInstance(String orderId) {
        Bundle args = new Bundle();
        EstimateClientFragment fragment = new EstimateClientFragment();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parseArguments();
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_estimate_client, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        setUpToolbar();
        setChangeToolbarColor(false);
        setUpViews();
        PrefsHelper.setNeedEstimateLastOrder(true, getContext());
        return rootView;
    }

    private void parseArguments() {
        orderId = getArguments().getString(ARG_ORDER_ID);
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.estimate_client);
        }
    }

    private void setUpViews() {
        positiveImageView.setActivated(true);
        negativeImageView.setActivated(true);
        positiveImageView.setOnClickListener(v -> toggleViews(true));
        negativeImageView.setOnClickListener(v -> toggleViews(false));

        infoView.setOnClickListener(v -> replaceFragment(
                CompletedOrderFragment.newInstance(orderId)));
        RxTextView.afterTextChangeEvents(commentTextInput).subscribe(
                textChangeEvent -> {
                    //noinspection ConstantConditions
                    if (textChangeEvent.editable() != null && textChangeEvent.editable().length() > 0)
                        commentInputLayout.setError("");
                });
        buttonReady.setOnClickListener(v -> {
            if (!validateFields()) return;
            OrderRealm.EstimateType estimateType = positiveImageView.isSelected()
                    ? OrderRealm.EstimateType.ESTIMATE
                    : OrderRealm.EstimateType.COMPLAINT;
            presenter.estimate(orderId, estimateType, commentTextInput.getText().toString(),
                    !tipsSwitch.isChecked());
        });
    }

    private void toggleViews(boolean positiveSelection) {
        positiveImageView.setActivated(positiveSelection);
        positiveImageView.setSelected(positiveSelection);
        positiveTextView.setSelected(positiveSelection);
        negativeImageView.setSelected(!positiveSelection);
        negativeImageView.setActivated(!positiveSelection);
        negativeTextView.setSelected(!positiveSelection);
        reasonLayout.setVisibility(positiveSelection ? View.GONE : View.VISIBLE);
        tipsSwitch.setVisibility(positiveSelection ? View.GONE : View.VISIBLE);
    }

    private boolean validateFields() {
        if (!positiveImageView.isSelected() && !negativeImageView.isSelected()) {
            snackBarLong(rootView, R.string.you_must_estimate_client);
            return false;
        }
        if (positiveImageView.isSelected())
            return true;
        if (commentTextInput.getText().length() == 0) {
            commentInputLayout.setError(getString(R.string.please_select_reason));
            return false;
        }
        if (commentTextInput.getText().length() > 300) {
            snackBarLong(rootView, R.string.max_length_of_comment_300);
            return false;
        }
        return true;
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
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onEstimateSuccess() {
        PrefsHelper.setNeedEstimateLastOrder(false, getContext());
        presenter.changeWorkStatusToLastActive();
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
