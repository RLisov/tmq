package com.tamaq.courier.presenters.tabs.payments.payments_for_period;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.SortedPaymentsRecyclerAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information.PaymentInformationFragment;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list.PaymentPeriodListFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.ColoredLabelXAxisRenderer;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PaymentsForPeriodFragment extends BaseFragment implements PaymentsForPeriodContract.View {

    public static final String PAYMENT_PERIOD_KEY = "payment_key";

    @Inject
    PaymentsForPeriodContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    @BindView(R.id.chart)
    BarChart chart;

    @BindView(R.id.loader)
    View loader;

    @BindView(R.id.secondLoader)
    View secondLoader;

    private SortedPaymentsRecyclerAdapter mAdapter;
    private PaymentPeriod mPaymentPeriod;
    private String mLastDateForPayment;

    public static PaymentsForPeriodFragment newInstance(PaymentPeriod paymentPeriod) {
        Bundle args = new Bundle();
        args.putSerializable(PAYMENT_PERIOD_KEY, paymentPeriod);

        PaymentsForPeriodFragment fragment = new PaymentsForPeriodFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_inner_tab_payments, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        setChangeToolbarColor(false);
        mPaymentPeriod = (PaymentPeriod) getArguments().get(PAYMENT_PERIOD_KEY);

        loader.setVisibility(View.VISIBLE);
        secondLoader.setVisibility(View.VISIBLE);

        initRecycler();
        initChart();

        presenter.getPaymentsData(mPaymentPeriod);

        return rootView;
    }

    private void initRecycler() {
        mAdapter = new SortedPaymentsRecyclerAdapter(getContext(), mPaymentPeriod, presenter);
        mAdapter.setListener(new SortedPaymentsRecyclerAdapter.Listener() {
            @Override
            public void onPeriodItemClicked(String periodString, int timeGap, PaymentPeriod paymentPeriod) {
                replaceFragment(PaymentPeriodListFragment.newInstance(periodString, timeGap, paymentPeriod),
                        getParentFragment().getFragmentManager());
            }

            @Override
            public void onAllTimeItemClicked(String paymentId) {
                replaceFragment(PaymentInformationFragment.newInstance(paymentId),
                        getParentFragment().getFragmentManager());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);
    }

    private void initChart() {
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.setBorderColor(getColor(R.color.pinkish_grey_two));
        chart.setHorizontalScrollBarEnabled(true);
        chart.setVerticalScrollBarEnabled(false);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.setScaleEnabled(false);
        chart.getLegend().setEnabled(false);
//        chart.setXAxisRenderer(new ColoredLabelXAxisRenderer(chart.getViewPortHandler(), chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT)));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGridColor(getColor(R.color.pinkish_grey_two));
        leftAxis.setAxisLineColor(getColor(R.color.pinkish_grey_two));
        leftAxis.setTextColor(getColor(R.color.black_87));
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setLabelCount(0);
        rightAxis.setDrawTopYLabelEntry(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setAxisMinimum(0f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(10);
        xAxis.setTextColor(getColor(R.color.black_87));
        xAxis.setAxisLineColor(getColor(R.color.pinkish_grey_two));
        xAxis.setLabelCount(6, false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }

    @SuppressLint("CheckResult")
    @Override
    public void displaySortedPaymentList(List<PaymentSortedRealm> sortedPaymentList) {
        Observable.fromIterable(sortedPaymentList)
                .toList()
                .map(sortedList -> {
                    Collections.reverse(sortedList);
                    return sortedList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(barData -> {
                    mAdapter.setObjects(sortedPaymentList);
                    secondLoader.setVisibility(View.GONE);
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void displayPaymentChart(List<PaymentSortedRealm> list) {
        PaymentSortedRealm lastPayment = list.get(list.size() - 1);
        mLastDateForPayment = getFormattedStringForPayment(lastPayment);

        Observable.fromIterable(list)
                .toList()
                .map(paymentSorted -> {
                    List<BarEntry> barEntryList = new ArrayList<>();

                    for (int i = 0; i < paymentSorted.size(); i++) {
                        float y = Double.valueOf(paymentSorted.get(i).getValue()).floatValue();
                        if (y < 0) y = 0;
                        barEntryList.add(new BarEntry(i, y));
                    }

                    BarDataSet barDataSet = new BarDataSet(barEntryList, "");
                    barDataSet.setColor(getColor(R.color.lightblue_two));

                    BarData barData = new BarData(barDataSet);
                    barData.setValueTextColor(getColor(R.color.black_87));
                    barData.setHighlightEnabled(false);

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setValueFormatter((value, axis) -> {
                        int index = (int) value;
                        PaymentSortedRealm payment = list.get(index);

                        return getFormattedStringForPayment(payment);
                    });

                    return barData;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(barData -> {
                    chart.setData(barData);
                    chart.setVisibleXRange(6f, list.size() - 1 > 6 ? 6 : list.size() - 1);
                    chart.moveViewToX(list.size() - 1);
                    chart.setXAxisRenderer(new ColoredLabelXAxisRenderer(chart.getViewPortHandler(), chart.getXAxis(), chart.getTransformer(YAxis.AxisDependency.LEFT), mLastDateForPayment));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (loader != null) loader.setVisibility(View.GONE);
                    }, 1000);
                });
    }

    private String getFormattedStringForPayment(PaymentSortedRealm lastPayment) {
        switch (mPaymentPeriod) {
            case WEEK:
                return String.valueOf(lastPayment.getTimeGap());
            case MONTH:
                return lastPayment.getMonthShortString();
            case YEAR:
                return lastPayment.getDateString();
            default:
                return lastPayment.getDateString().length() > 8 // 00.00.00 = 8 chars
                        ? DateHelper.getDateForPayment(lastPayment.getDateString())
                        : lastPayment.getDateString(); //TODO change solution. Save dates in right format
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
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

}
