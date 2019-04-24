package com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.TimeWithEventAdapter;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.ui.TimeWithEventItem;
import com.tamaq.courier.presenters.activities.ConcreteChatActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.HelperCommon;

import java.text.DecimalFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CompletedOrderFragment extends BaseFragment implements CompletedOrderContract.View {

    public static final String ORDER_ID_KEY = "order_id_key";

    @Inject
    CompletedOrderContract.Presenter presenter;

    @BindView(R.id.ratingContainerLayout)
    View ratingContainerLayout;
    @BindView(R.id.positiveImageView)
    ImageView positiveImageView;
    @BindView(R.id.positiveTextView)
    TextView positiveTextView;

    @BindView(R.id.negativeImageView)
    ImageView negativeImageView;
    @BindView(R.id.negativeTextView)
    TextView negativeTextView;

    @BindView(R.id.profitTextView)
    TextView profitTextView;
    @BindView(R.id.clientsOweTextView)
    TextView clientsOweTextView;
    @BindView(R.id.needToReturnContainer)
    View needToReturnContainer;
    @BindView(R.id.needChangeFromTextView)
    TextView needChangeFromTextView;

    @BindView(R.id.restaurantAddressTextView)
    TextView restaurantAddressTextView;
    @BindView(R.id.restaurantNameTextView)
    TextView restaurantNameTextView;
    @BindView(R.id.clientAddressTextView)
    TextView clientAddressTextView;

    @BindView(R.id.dispatcherPhoneContainer)
    View dispatcherPhoneContainer;
    @BindView(R.id.dispatcherPhoneTextView)
    TextView dispatcherPhoneTextView;

    @BindView(R.id.goToChatButton)
    Button goToChatButton;

    @BindView(R.id.timeEventsRecycler)
    RecyclerView timeEventsRecycler;
    @BindView(R.id.emptyTimeTextView)
    View emptyTimeTextView;

    private TimeWithEventAdapter<TimeWithEventItem> timeWithEventAdapter;

    public CompletedOrderFragment() {
    }

    public static CompletedOrderFragment newInstance(String orderId) {
        Bundle args = new Bundle();
        args.putString(ORDER_ID_KEY, orderId);

        CompletedOrderFragment fragment = new CompletedOrderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_completed_order, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initToolbar();
        presenter.loadOrder(getArguments().getString(ORDER_ID_KEY));
        goToChatButton.setVisibility(View.GONE);

        return rootView;
    }

    private void initToolbar() {
        initializeNavigationBar();
//        getSupportActionBar().setTitle(R.string.order_completed_without_dot);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        setChangeToolbarColor(false);
    }

    @Override
    public void displayOrder(OrderRealm order, String callcenterPhone) {
        getSupportActionBar().setTitle(order.getStatusForDisplay(getContext())/*.equals(getString(R.string.order_completed)) ? order.getStatusForDisplay(getContext()) : getString(R.string.order_done)*/);
        getSupportActionBar().setSubtitle(String.valueOf(order.getNumber()));
        setRating(order.getClientRating());
        setMoney(order);
        setChronology(order);
        setAddresses(order);
        if (!TextUtils.isEmpty(callcenterPhone)) dispatcherPhoneTextView.setText(callcenterPhone);

        DialogRealm dialogByChatId = DialogDAO.getInstance().getDialogByChatId(order.getOrderId());
        if (checkNotNull(dialogByChatId)
                && checkIfChatNotOlder24Hours(dialogByChatId)
                && !dialogByChatId.isHideFromList()) {
            goToChatButton.setVisibility(View.VISIBLE);
            RxView.clicks(goToChatButton).subscribe(o ->
                    startActivity(ConcreteChatActivity.newInstance(getContext(), order.getOrderId(), order.getClientName(), true)));
        }

        RxView.clicks(dispatcherPhoneContainer).subscribe(o ->
                HelperCommon.grantedCallPhonePermission(getAppCompatActivity(),
                        () -> startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", order.getDispatcherPhone(), null))))
        );

    }

    private void setRating(Integer rating) {
        if (checkNotNull(rating)) {
            ratingContainerLayout.setVisibility(View.VISIBLE);
            if (rating == OrderRealm.EstimateType.ESTIMATE.ordinal()) {
                positiveImageView.setActivated(true);
                positiveImageView.setSelected(true);
                positiveTextView.setSelected(true);
            } else {
                negativeImageView.setActivated(true);
                negativeImageView.setSelected(true);
                negativeTextView.setSelected(true);
            }
        }
    }

    private void setMoney(OrderRealm order) {
        DecimalFormat format = new DecimalFormat("#.##");

        clientsOweTextView.setText(String.format(
                "%s%s", format.format(order != null ? order.getClientNeedToPay() : 0), presenter.getUserCurrency()));
        profitTextView.setText(String.format(
                "%s%s", format.format(order != null ? order.getProfitUI() : 0), presenter.getUserCurrency()));
        if (order != null && order.getNeedChangeFrom() != 0) {
            needToReturnContainer.setVisibility(View.VISIBLE);
            needChangeFromTextView.setText(String.format(
                    "%s%s", format.format(order.getNeedChangeFrom()), presenter.getUserCurrency()));
        }
    }

    private void setChronology(OrderRealm order) {
        timeWithEventAdapter = new TimeWithEventAdapter<>(getContext());
        timeEventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        timeEventsRecycler.setAdapter(timeWithEventAdapter);
        timeEventsRecycler.setNestedScrollingEnabled(false);
        timeWithEventAdapter.setObjects(order.prepareEventItems(getContext()));
        updateTimeRecyclerVisibility();
    }

    private void setAddresses(OrderRealm order) {
        restaurantAddressTextView.setText(order.getRestaurantAddress());
        clientAddressTextView.setText(order.getClientAddress());
        restaurantNameTextView.setText(String.format(getString(R.string.restaurant_address_name), order.getRestaurantName()));
    }

    private boolean checkIfChatNotOlder24Hours(DialogRealm dialogByChatId) {
        if (dialogByChatId.isOrderCompleted()) {
            Date orderDate = DateHelper.parseDateFromString(dialogByChatId.getCreatedDate());
            Date currentDate = DateHelper.getCurrentDate();
            int differenceBetweenInHours = DateHelper.differenceBetweenInHours(orderDate, currentDate);
            return differenceBetweenInHours < 24;
        } else return true;
    }

    private void updateTimeRecyclerVisibility() {
        timeEventsRecycler.setVisibility(timeWithEventAdapter.getItemCount() == 0
                ? View.GONE : View.VISIBLE);
        emptyTimeTextView.setVisibility(timeWithEventAdapter.getItemCount() == 0
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
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
}
