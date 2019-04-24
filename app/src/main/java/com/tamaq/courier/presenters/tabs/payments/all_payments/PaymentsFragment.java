package com.tamaq.courier.presenters.tabs.payments.all_payments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.TabsAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentsForPeriodFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;
import com.tamaq.courier.widgets.NonSwipeableViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentsFragment extends BaseFragment implements PaymentsContract.View {

    @Inject
    PaymentsContract.Presenter presenter;

    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    NonSwipeableViewPager mViewPager;
    @BindView(R.id.loader)
    View loaderView;
    @BindView(R.id.emptyStateLayout)
    View emptyView;
    @BindView(R.id.accessDeniedLayout)
    View accessDeniedLayout;

    public PaymentsFragment() {
    }

    public static PaymentsFragment newInstance() {
        return new PaymentsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_payments, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        initializeNavigationBar();
        setChangeToolbarColor(false);

        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            loaderView.setVisibility(View.VISIBLE);
            presenter.getBalance();
            presenter.loadPayments();
        }

        presenter.checkUserState();

        return rootView;
    }

    @Override
    public void onUserChecked(boolean active) {
        PrefsHelper.setUserBlocked(!active, getContext());
        if (!active) onUserBlocked();
    }

    private void onUserBlocked() {
        setUpToolbar(getString(R.string.access_denied));
        accessDeniedLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayBalance(String balance) {
        getSupportActionBar().setTitle(String.format("%s %s", getString(R.string.balance), balance));
    }

    @Override
    public void paymentsLoaded() {
        initTabs();
        tabLayout.setVisibility(View.VISIBLE);
        loaderView.setVisibility(View.GONE);
    }

    private void initTabs() {
        TabsAdapter tabsAdapter = new TabsAdapter(getChildFragmentManager());

        PaymentsForPeriodFragment allTimeFragment = PaymentsForPeriodFragment.newInstance(PaymentPeriod.ALL_TIME);
        PaymentsForPeriodFragment weekFragment = PaymentsForPeriodFragment.newInstance(PaymentPeriod.WEEK);
        PaymentsForPeriodFragment monthFragment = PaymentsForPeriodFragment.newInstance(PaymentPeriod.MONTH);
        PaymentsForPeriodFragment yearFragment = PaymentsForPeriodFragment.newInstance(PaymentPeriod.YEAR);

        tabsAdapter.addFragment(allTimeFragment, getString(R.string.all_time));
        tabsAdapter.addFragment(weekFragment, getString(R.string.week));
        tabsAdapter.addFragment(monthFragment, getString(R.string.month));
        tabsAdapter.addFragment(yearFragment, getString(R.string.year));

        mViewPager.setAdapter(tabsAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setSwipePagingEnabled(false);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void displayEmptyView() {
        loaderView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.payments_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.recharge)
//            toast("Recharge");
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


    private void setUpToolbar(String title) {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
