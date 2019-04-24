package com.tamaq.courier.presenters.main;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.tamaq.courier.BuildConfig;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.service.LocationTrackService;
import com.tamaq.courier.controllers.service.MyFirebaseInstanceIDService;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.events.BlockBottomBarEvent;
import com.tamaq.courier.events.OpenOrderTabEvent;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderRespondedInfo;
import com.tamaq.courier.presenters.activities.EstimateClientActivity;
import com.tamaq.courier.presenters.activities.NewOrderActivity;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.chat.ChatTabFragment;
import com.tamaq.courier.presenters.tabs.notifications.NotificationsFragment;
import com.tamaq.courier.presenters.tabs.orders.OrdersTabFragment;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersFragment;
import com.tamaq.courier.presenters.tabs.payments.all_payments.PaymentsFragment;
import com.tamaq.courier.presenters.tabs.profile.ProfileFragment;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.PrefsHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action0;

public class MainActivity extends BaseActivity implements MainContract.View {

    public static final String TAB_NUMBER = "tab_number";
    @Inject
    MainContract.Presenter mPresenter;
    private AHBottomNavigation mBottomNavigation;
    private AHNotification mNotification = new AHNotification.Builder().setText(" ").build();
    private AHNotification mEmptyNotification = new AHNotification.Builder().setText("").build();
    private Action0 onCurrentOrderCanceledByClientListener = () -> {
        mPresenter.changeWorkStatusToLastActive();
        Dialog alert = DialogHelper.buildDialog(this, R.string.order_canceled,
                R.string.contact_dispatcher_if_necessary,
                R.string.ok,
                0,
                (dialog, which) -> {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    replaceFragment(OrdersFragment.newInstance());
                }, null);
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    };

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent newInstance(Context context, int tabNumber) {
        Bundle extras = new Bundle();
        extras.putInt(TAB_NUMBER, tabNumber);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtras(extras);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.attachPresenter(this);

        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        initTabs();

        findViewById(R.id.bottomNavigationShadow).setVisibility(
                Build.VERSION.SDK_INT < 21 ? View.VISIBLE : View.GONE);

        MyFirebaseInstanceIDService.sendPushTokenAndSubscribe(this);

        OrderDAO.getInstance().addOrderCanceledFromClientListener(onCurrentOrderCanceledByClientListener);

        PrefsHelper.saveChatScreenIsActive(this, false);
        mPresenter.getServerTime();
        mPresenter.loadCitiesAndDistricts();
        mPresenter.checkIsUserConfirmed();
        mPresenter.checkNeedTrackUserLocation();
    }

    private void initTabs() {
        mBottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        mBottomNavigation.setBehaviorTranslationEnabled(true);
        mBottomNavigation.setTranslucentNavigationEnabled(true);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.title_orders, R.drawable.orders, R.color.windows_blue);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.title_chat, R.drawable.chat, R.color.windows_blue);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.title_payment, R.drawable.ic_balance, R.color.windows_blue);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.title_notification, R.drawable.ic_bnb_bell, R.color.windows_blue);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.title_profile, R.drawable.personal_cabinet, R.color.windows_blue);

        mBottomNavigation.addItem(item1);
        mBottomNavigation.addItem(item2);
        mBottomNavigation.addItem(item3);
        mBottomNavigation.addItem(item4);
        mBottomNavigation.addItem(item5);
//        mBottomNavigation.setBehaviorTranslationEnabled(false);

        mBottomNavigation.setDefaultBackgroundColor(Color.parseColor("#f7f7f7"));
        mBottomNavigation.setAccentColor(Color.parseColor("#4aaee2"));
        mBottomNavigation.setNotificationBackground(ContextCompat.getDrawable(
                getApplicationContext(), R.drawable.oval_notification));

        mBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            if (wasSelected) return false;

            mBottomNavigation.restoreBottomNavigation(true);

            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() > 0) manager.popBackStack();

            switch (position) {
                case 0:
                    replaceFragment(OrdersTabFragment.newInstance(), false);
                    break;
                case 1:
                    replaceFragment(ChatTabFragment.newInstance(), false);
                    break;
                case 2:
                    replaceFragment(PaymentsFragment.newInstance(), false);
                    break;
                case 3:
                    replaceFragment(NotificationsFragment.newInstance(), false);
                    break;
                case 4:
                    replaceFragment(ProfileFragment.newInstance(), false);
                    break;
            }

            return true;
        });
        mBottomNavigation.setCurrentItem(4, false);

        if (getIntent().getExtras() != null) mBottomNavigation.setCurrentItem(1, true);
        else mBottomNavigation.setCurrentItem(0, true);

        addNotificationsListeners();

        mBottomNavigation.post(() -> {// // TODO: 09.06.17 check is it works
            try {
                Field f = mBottomNavigation.getClass().getDeclaredField("views"); //NoSuchFieldException
                f.setAccessible(true);
                ArrayList<View> views = (ArrayList<View>) f.get(mBottomNavigation); //IllegalAccessException
                for (View v : views) {
                    ViewGroup viewGroup = (ViewGroup) v;
                    ImageView imageView = (ImageView) viewGroup.getChildAt(0);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) Log.e("MainActivity", "", e.fillInStackTrace());
            }
        });
    }

    private void addNotificationsListeners() {
        OrderDAO.getInstance().addOrderCanceledFromClientListener(() ->
                setBottomNotification(mNotification, 3));

        DialogDAO.getInstance().setNewMessageRecievedActions(() -> mPresenter.updateDialogs());
        DialogDAO.getInstance().setUpdateBadgeActions(() ->
                setBottomNotification(
                        DialogDAO.getInstance().isUnreadedMessages() ? mNotification : mEmptyNotification, 1));

        if (DialogDAO.getInstance().isUnreadedMessages()) {
            setBottomNotification(mNotification, 1);
        }

        if (NotificationsDAO.getInstance().checkUnreadedNotifications()) {
            setBottomNotification(mNotification, 3);
        }

        NotificationsDAO.getInstance().setListener(needShowBubble -> {
            setBottomNotification(needShowBubble ? mNotification : mEmptyNotification, 3);
        });
    }

    @Override
    public void onInjectDependencies(AppComponent apiComponent) {
        DaggerCommonComponent.builder()
                .appComponent(apiComponent)
                .commonModule(new CommonModule())
                .build().inject(this);
    }

    private void setBottomNotification(AHNotification notification, int itemPosition) {
        new Handler(Looper.getMainLooper()).post(() -> mBottomNavigation.setNotification(notification, itemPosition));
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.checkNeedStartChecker();
        if (mBottomNavigation.getCurrentItem() == 0) replaceFragment(OrdersTabFragment.newInstance(), false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OpenOrderTabEvent event) {
        openCurrentOrderScreen();
    }

    @Override
    public void openCurrentOrderScreen() {
        if (mBottomNavigation.getCurrentItem() == 0) replaceFragment(OrdersTabFragment.newInstance(), false);
        else mBottomNavigation.setCurrentItem(0, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BlockBottomBarEvent event) {
        if (event.isNeedBlock()) {
            for (int i = 0; i < mBottomNavigation.getItemsCount() - 1; i++) {
                mBottomNavigation.disableItemAtPosition(i);
            }
        } else {
            for (int i = 0; i < mBottomNavigation.getItemsCount() - 1; i++) {
                mBottomNavigation.enableItemAtPosition(i);
            }
        }
    }

    @Override
    public void serverTimeUpdated() {
        mPresenter.checkNeedShowEstimateScreen();
        mPresenter.checkNeedShowNewOrderScreen();
    }

    @Override
    public void onNeedTrackUserLocation() {
        startService(LocationTrackService.getIntent(getContext()));
    }

    @Override
    public void onNewOrderNotification(OrderRealm orderRealm, NotificationRealm notificationRealm) {
        startActivity(NewOrderActivity.newInstance(getContext(), orderRealm.getOrderId()));
        mPresenter.saveNotificationWasReadLocally(notificationRealm);
    }

    @Override
    public void onNewAutoRateNotification(NotificationRealm notificationRealm, boolean needShowDialog) {
        if (needShowDialog) {
            AlertDialog alertDialog = DialogHelper.buildDialog(this,
                    R.string.congratulations,
                    R.string.your_auto_rate_win,
                    R.string.ok,
                    0,
                    (dialog, which) -> {
                        if (mBottomNavigation.getCurrentItem() != 0) mBottomNavigation.setCurrentItem(0);
                        else {
//                        OrdersTabFragment ordersTabFragment = (OrdersTabFragment) getSupportFragmentManager()
//                                .findFragmentByTag(OrdersTabFragment.class.getSimpleName());
//                        ordersTabFragment.showCurrentOrderIfNeed();
                            replaceFragment(OrdersTabFragment.newInstance(), false);
                        }
                    }, null);
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            mPresenter.saveNotificationWasReadLocally(notificationRealm);
        } else {
            if (mBottomNavigation.getCurrentItem() != 0) mBottomNavigation.setCurrentItem(0);
            else replaceFragment(OrdersTabFragment.newInstance(), false);
        }
    }

    @Override
    public void onNeedShowEstimateScreen(String orderId) {
        startActivity(EstimateClientActivity.newInstance(getContext(), orderId));
    }

    @Override
    public void onNeedShowNewOrderScreen(OrderRespondedInfo orderRespondedInfo) {
        if (orderRespondedInfo.isAcceptedByUser()) {
            startActivity(NewOrderActivity.newInstance(getContext(),
                    orderRespondedInfo.getOrderId(),
                    orderRespondedInfo.isAcceptedByUser()));
        } else if (orderRespondedInfo.isActual()) {
            startActivity(NewOrderActivity.newInstance(getContext(),
                    orderRespondedInfo.getOrderId(),
                    orderRespondedInfo.getTotalTime(),
                    orderRespondedInfo.getLeastSecondsToShow()));
        }
    }

    @Override
    public void onDialogsUpdated() {
        DialogDAO.getInstance().callListenerMessageUpdated();
        if (DialogDAO.getInstance().isUnreadedMessages()) {
            mBottomNavigation.setNotification(mNotification, 1);
        } else mBottomNavigation.setNotification(mEmptyNotification, 1);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (onBackPressed(fm)) return;
        super.onBackPressed();
    }

    /**
     * Почти работает, но не до конца
     * На некоторых фрагментах (обычно на корневых) нужно два раза нажать кнопку назад,
     * чтобы выйти из приложения
     */
    private boolean onBackPressed(FragmentManager fm) {
        if (fm != null) {
            if (fm.getBackStackEntryCount() > 1) {
                fm.popBackStackImmediate();
                return true;
            }

            @SuppressLint("RestrictedApi") List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) continue;

                    if (frag.isVisible() && onBackPressed(frag.getChildFragmentManager())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachPresenter();
        OrderDAO.getInstance().removeOrderCanceledFromClientListener(onCurrentOrderCanceledByClientListener);
        stopService(LocationTrackService.getIntent(getContext()));
        super.onDestroy();
    }
}
