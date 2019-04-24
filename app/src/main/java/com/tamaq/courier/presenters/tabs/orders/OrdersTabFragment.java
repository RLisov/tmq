package com.tamaq.courier.presenters.tabs.orders;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.orders.current_order.CurrentOrderFragment;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersFragment;
import com.tamaq.courier.utils.PrefsHelper;

import javax.inject.Inject;


public class OrdersTabFragment extends BaseFragment implements OrdersTabContract.View {

    @Inject
    OrdersTabContract.Presenter presenter;

    public static OrdersTabFragment newInstance() {
        return new OrdersTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_common_tab, container, false);
        presenter.attachPresenter(this);
        Fragment fragment = OrdersFragment.newInstance();
        replaceFragment(fragment, getChildFragmentManager());
        return rootView;
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        showCurrentOrderIfNeed();
        //TODO wait for test, if need then add one more event, to show current order.
    }

    public void showCurrentOrderIfNeed() {
        if (!PrefsHelper.isUserBlocked(getContext())) presenter.checkNeedShowCurrentOrder();
    }

    @Override
    public void onNeedShowCurrentOrder(String orderId) {
        if (getChildFragmentManager().findFragmentByTag(CurrentOrderFragment.class.getSimpleName()) == null) {
            Fragment fragment = CurrentOrderFragment.newInstance(orderId);
            fragment.setRetainInstance(true);
            replaceFragment(fragment, getChildFragmentManager());
        }
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }
}
