package com.tamaq.courier.presenters.tabs.profile.orders_archive;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.OrdersArchiveRecyclerAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.orders.current_order.CurrentOrderFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderFragment;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.SearchMenuHelper;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tamaq.courier.utils.DateHelper.CHART_DATE_FORMAT;
import static com.tamaq.courier.utils.DateHelper.getStringFromDate;

public class OrdersArchiveFragment extends BaseFragment implements OrdersArchiveContract.View {

    @Inject
    OrdersArchiveContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.datesContainer)
    View datesContainer;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.periodFromDateTextView)
    TextView periodFromDateTextView;
    @BindView(R.id.periodToDateTextView)
    TextView periodToDateTextView;
    @BindView(R.id.cancelDate)
    View cancelDateButton;

    @BindView(R.id.emptyStateLayout)
    View emptyStateLayout;

    @BindView(R.id.emptySearchView)
    View emptySearchView;

    @BindView(R.id.loader)
    View loader;

    private OrdersArchiveRecyclerAdapter mAdapter;
    private boolean mIsNeedShowSearchIcon;
    private boolean mIsNeedEnableSearchIcon;

    public OrdersArchiveFragment() {
    }

    public static OrdersArchiveFragment newInstance() {
        return new OrdersArchiveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_orders_archive, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initToolbar();
        initRecycler();

        loader.setVisibility(View.VISIBLE);
        presenter.loadOrders();

        return rootView;
    }

    private void initToolbar() {
        initializeNavigationBar();
        getSupportActionBar().setTitle(R.string.orders_archive);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        setChangeToolbarColor(false);
    }

    private void initRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new OrdersArchiveRecyclerAdapter(getContext());
        mAdapter.setListener(new OrdersArchiveRecyclerAdapter.Listener() {
            @Override
            public void onItemClick(OrderRealm orderRealm) {
                replaceFragment(orderRealm.isArchive()
                        ? CompletedOrderFragment.newInstance(orderRealm.getOrderId())
                        : CurrentOrderFragment.newInstance(orderRealm.getOrderId(), true));
            }

            @Override
            public void onSearchEmpty() {
                emptySearchView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onSearchNotEmpty() {
                emptySearchView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void displayOrders(List<OrderRealm> list) {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.setObjects(list);
        loader.setVisibility(View.GONE);
        initDatePickers();
        displaySearchIcon(true);
    }

    @Override
    public void displayNoOrders() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
        displaySearchIcon(false);
    }

    @Override
    public void enableSearchIcon() {
        mIsNeedEnableSearchIcon = true;
        if (getActivity() != null) getActivity().invalidateOptionsMenu();
    }

    private void initDatePickers() {
        datesContainer.setVisibility(View.VISIBLE);
        periodFromDateTextView.setText(getStringFromDate(presenter.getMinDate(), CHART_DATE_FORMAT));
        periodToDateTextView.setText(getStringFromDate(presenter.getMaxDate(), CHART_DATE_FORMAT));

        RxView.clicks(cancelDateButton).subscribe(o -> {
            cancelDateButton.setVisibility(View.GONE);
            icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_calendar_all_period));
            presenter.clearDatePeriodSearch();
        });

        RxView.clicks(periodFromDateTextView).subscribe(o -> showDatePicker(PeriodType.MINIMUM));
        RxView.clicks(periodToDateTextView).subscribe(o -> showDatePicker(PeriodType.MAXIMUM));
    }

    private void showDatePicker(final PeriodType periodType) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodType == PeriodType.MAXIMUM ? presenter.getMaxDate() : presenter.getMinDate());
        int currentSelectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentSelectedMonth = calendar.get(Calendar.MONTH);
        int currentSelectedYear = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                R.style.DatePickerDialogTheme,
                (view, year, month, dayOfMonth) -> {
                    Calendar resultCalendar = Calendar.getInstance();
                    resultCalendar.set(Calendar.YEAR, year);
                    resultCalendar.set(Calendar.MONTH, month);
                    resultCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    resultCalendar.setTime(resultCalendar.getTime());

                    if (periodType == PeriodType.MAXIMUM) {
                        DateHelper.setCalendarDayMaximum(resultCalendar);
                        presenter.setMaxDate(resultCalendar.getTime());
                        periodToDateTextView.setText(getStringFromDate(presenter.getMaxDate(), CHART_DATE_FORMAT));
                    } else {
                        DateHelper.setCalendarDayMinimum(resultCalendar);
                        presenter.setMinDate(resultCalendar.getTime());
                        periodFromDateTextView.setText(getStringFromDate(presenter.getMinDate(), CHART_DATE_FORMAT));
                    }

                    cancelDateButton.setVisibility(View.VISIBLE);
                    icon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_calendar_selected_period));

                }, currentSelectedYear, currentSelectedMonth, currentSelectedDay);

        calendar.setTime(periodType == PeriodType.MINIMUM ? presenter.getMinDateOriginal() : presenter.getMinDate());
        DateHelper.setCalendarDayMinimum(calendar);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        calendar.setTime(periodType == PeriodType.MAXIMUM ? presenter.getMaxDateOriginal() : presenter.getMaxDate());
        DateHelper.setCalendarDayMaximum(calendar);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu_white_icon, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchMenuHelper searchMenuHelper = new SearchMenuHelper(getActivity(), menuItem,
                getString(R.string.order_search_with_ellipsis));

        searchMenuHelper.showIcon(mIsNeedShowSearchIcon);
        searchMenuHelper.enableIcon(mIsNeedEnableSearchIcon);

        searchMenuHelper.setOnActionExpandListener(isExpand ->
                toolbar.setBackgroundColor(getColor(isExpand ? R.color.white : R.color.dark_sky_blue)));

        searchMenuHelper.setTextChangeListener(searchText -> {
            if (mAdapter != null) mAdapter.onSearchRequested(searchText);
        });
    }

    private void displaySearchIcon(boolean needDisplay) {
        mIsNeedShowSearchIcon = needDisplay;
        if (getActivity() != null) getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) getFragmentManager().popBackStackImmediate();
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

    private enum PeriodType {
        MINIMUM, MAXIMUM
    }
}
