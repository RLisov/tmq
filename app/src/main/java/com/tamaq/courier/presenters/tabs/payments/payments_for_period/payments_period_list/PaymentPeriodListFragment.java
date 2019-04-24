package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.SortedPaymentsRecyclerAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information.PaymentInformationFragment;
import com.tamaq.courier.shared.TamaqApp;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentPeriodListFragment extends BaseFragment implements PaymentPeriodListContract.View {

    public static final String PAYMENT_PERIOD_KEY = "payment_key";
    public static final String PERIOD_STRING_KEY = "period_string_key";
    public static final String TIME_GAP_KEY = "time_gap_key";

    @Inject
    PaymentPeriodListContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private SortedPaymentsRecyclerAdapter mAdapter;
    private PaymentPeriod mPaymentPeriod;

    public PaymentPeriodListFragment() {
    }

    public static PaymentPeriodListFragment newInstance(String periodString, int timeGap, PaymentPeriod paymentPeriod) {
        Bundle args = new Bundle();
        args.putString(PERIOD_STRING_KEY, periodString);
        args.putInt(TIME_GAP_KEY, timeGap);
        args.putSerializable(PAYMENT_PERIOD_KEY, paymentPeriod);

        PaymentPeriodListFragment fragment = new PaymentPeriodListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        mPaymentPeriod = (PaymentPeriod) getArguments().get(PAYMENT_PERIOD_KEY);

        initToolbar();
        initRecycler();

        presenter.loadPaymentsList(getArguments().getInt(TIME_GAP_KEY), mPaymentPeriod);
        return rootView;
    }

    private void initToolbar() {
        initializeNavigationBar();
        setChangeToolbarColor(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.dark_sky_blue)));

        String title = getArguments().getString(PERIOD_STRING_KEY);
//        if (mPaymentPeriod == PaymentPeriod.WEEK && title != null) {
//            title = title.substring(0, title.lastIndexOf(" "));
//        }

        if (title != null) {
            Spannable spannableTitle = new SpannableString(title);
            spannableTitle.setSpan(new ForegroundColorSpan(getColor(R.color.white)),
                    0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            getSupportActionBar().setTitle(spannableTitle);
        }
    }

    private void initRecycler() {
        PaymentPeriod periodForAdapter = PaymentPeriod.MONTH;
        switch (mPaymentPeriod) {
            case MONTH:
                periodForAdapter = PaymentPeriod.WEEK;
                break;
            case WEEK:
                periodForAdapter = PaymentPeriod.ALL_TIME;
                break;
        }
        mAdapter = new SortedPaymentsRecyclerAdapter(getContext(), periodForAdapter, presenter);
        mAdapter.setListener(new SortedPaymentsRecyclerAdapter.Listener() {

            @Override
            public void onPeriodItemClicked(String periodString, int timeGap, PaymentPeriod paymentPeriod) {
                replaceFragment(PaymentPeriodListFragment.newInstance(periodString, timeGap, paymentPeriod),
                        getFragmentManager());

            }

            @Override
            public void onAllTimeItemClicked(String paymentId) {
                replaceFragment(PaymentInformationFragment.newInstance(paymentId), getFragmentManager());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void displayPaymentsList(List<PaymentSortedRealm> list) {
        Collections.reverse(list);
        mAdapter.setObjects(list);
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
                .appComponent(TamaqApp.get(getContext()).getAppComponent())
                .commonModule(new CommonModule())
                .build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }


}
