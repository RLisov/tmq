package com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.HelperCommon;

import java.text.DecimalFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaymentInformationFragment extends BaseFragment implements PaymentInformationContract.View {

    private static final String PAYMENT_ID_KEY = "payment_id_key";

    @Inject
    PaymentInformationContract.Presenter presenter;

    @BindView(R.id.typeAndPaymentNumberTextView)
    TextView typeAndPaymentNumberTextView;

    @BindView(R.id.paymentMoneyTextView)
    TextView paymentMoneyTextView;

    @BindView(R.id.paymentDescriptionTextView)
    TextView paymentDescriptionTextView;

    @BindView(R.id.managerPhoneNumber)
    TextView managerPhoneNumber;

    @BindView(R.id.goToOrderButton)
    View goToOrderButton;

    public PaymentInformationFragment() {
    }

    public static PaymentInformationFragment newInstance(String paymentId) {
        Bundle args = new Bundle();
        PaymentInformationFragment fragment = new PaymentInformationFragment();
        args.putString(PAYMENT_ID_KEY, paymentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_payment_information, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        loader = rootView.findViewById(R.id.loader);

        initializeNavigationBar();
        setChangeToolbarColor(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);

        presenter.loadInformation(getArguments().getString(PAYMENT_ID_KEY));

        return rootView;
    }

    @Override
    public void displayInformation(PaymentRealm payment, OrderRealm orderRealm, UserRealm userRealm) {
        typeAndPaymentNumberTextView.setText(String.format("%s. Заказ %s",
                payment.getTypeUI(getContext()), orderRealm.getNumber()));

        String dispatcherPhone = orderRealm.getDispatcherPhoneUI();
        if (dispatcherPhone == null) dispatcherPhone = userRealm.getCountry().getCallcenterPhone();

        setScreenData(payment, userRealm, dispatcherPhone);
    }

    @Override
    public void displayInformation(PaymentRealm payment, UserRealm userRealm) {
        typeAndPaymentNumberTextView.setText(R.string.handfill);

        String dispatcherPhone = userRealm.getCountry().getCallcenterPhone();

        setScreenData(payment, userRealm, dispatcherPhone);
    }

    private void setScreenData(PaymentRealm payment, UserRealm userRealm, String dispatcherPhone) {
        Date dateFromString = DateHelper.parseDateFromString(payment.getFullDate());
        String stringFromDate = DateHelper.getStringFromDate(dateFromString, DateHelper.PAYMENT_DATE_FORMAT);

        getSupportActionBar().setTitle(stringFromDate);

        DecimalFormat format = new DecimalFormat("#.##");
        String resultMoneyString = String.format("%s %s", format.format(payment.getMoney()), presenter.getUserCurrency());
        if (payment.getType().equals(getString(R.string.write_off)))
            resultMoneyString = "-" + resultMoneyString;
        paymentMoneyTextView.setText(resultMoneyString);

        paymentDescriptionTextView.setText(payment.getDetails());

        managerPhoneNumber.setText(dispatcherPhone);

        managerPhoneNumber.setOnClickListener(v ->
                HelperCommon.grantedCallPhonePermission(getActivity(),
                        () -> startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts(
                                "tel", userRealm.getPhoneCallcenter(), null)))));

        switch (payment.getTypeEnum()) {
            case HANDFILL:
            case MONEYBACK:
                goToOrderButton.setVisibility(View.GONE);
                break;
            default:
                RxView.clicks(goToOrderButton).subscribe(o ->
                        replaceFragment(CompletedOrderFragment.newInstance(payment.getOrderId())));

        }
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
