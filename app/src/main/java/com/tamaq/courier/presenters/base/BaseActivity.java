package com.tamaq.courier.presenters.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.events.LogoutEvent;
import com.tamaq.courier.model.api.response.ApiError;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.activities.SplashScreenActivity;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DialogHelper;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.utils.PrefsHelper;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.Realm;

public abstract class BaseActivity extends RxAppCompatActivity implements BaseView {

    private boolean needHandleDispatchTouchEvent = true;
    private BroadcastReceiver mNetworkStateBroadcastReceiver;
    private AlertDialog mNetworkDisabledAlertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onInjectDependencies(TamaqApp.get(getContext()).getAppComponent());
    }


    public void onInjectDependencies(AppComponent apiComponent) {
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

        mNetworkStateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!HelperCommon.isNetworkConnected(getApplicationContext())) {
                    showLostNetworkDialog();
                } else hideLostNetworkDialog();
            }
        };

        registerReceiver(mNetworkStateBroadcastReceiver, filter);
    }

    private void showLostNetworkDialog() {
        if (mNetworkDisabledAlertDialog == null) {
            mNetworkDisabledAlertDialog = DialogHelper.buildDialog(this,
                    getString(R.string.lost_network_connection),
                    getString(R.string.check_network_connection),
                    R.string.enable_data_transfer,
                    R.string.cancel,
                    (dialog, which) -> startActivity(new Intent(Settings.ACTION_SETTINGS)),
                    (dialog, which) -> hideLostNetworkDialog());
        }
        if (mNetworkDisabledAlertDialog != null) {
            mNetworkDisabledAlertDialog.setCanceledOnTouchOutside(false);
            mNetworkDisabledAlertDialog.setCancelable(false);
            mNetworkDisabledAlertDialog.show();
        }
    }

    private void hideLostNetworkDialog() {
        if (mNetworkDisabledAlertDialog != null) {
            mNetworkDisabledAlertDialog.hide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkStateBroadcastReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LogoutEvent event) {
        try {
            if (PrefsHelper.isUserAuthorized(getContext())) {
                UserRealm.getInstance().reset();
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(it -> it.deleteAll());
                realm.close();
                PrefsHelper.setUserAuthorized(false, getContext());
                PrefsHelper.setUserConfirmed(false, getContext());
                startActivity(SplashScreenActivity.newInstance(getContext(), false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (needHandleDispatchTouchEvent) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                // Hiding keyboard, if SearchView was focused
                if (v instanceof SearchView.SearchAutoComplete) {
                    hideKeyboardIfTouchOutside(
                            (SearchView) v.getParent().getParent().getParent().getParent(),
                            event);
                } else if (v instanceof EditText) {
                    View newTouchedView = findViewAt((ViewGroup) v.getRootView(), (int) event.getRawX(), (int) event.getRawY());
                    if (newTouchedView instanceof EditText) // if new touched view is edit text, don`t hide keyboard
                        return super.dispatchTouchEvent(event);
                    hideKeyboardIfTouchOutside(v, event);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboardIfTouchOutside(View v, MotionEvent event) {
        Rect outRect = new Rect();
        // we need in rect of SearchView, because SearchAutoComplete is a child of SearchView
        v.getGlobalVisibleRect(outRect);
        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
            v.clearFocus();
            HelperCommon.hideKeyboard(this);
        }
    }

    private View findViewAt(ViewGroup viewGroup, int x, int y) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                View foundView = findViewAt((ViewGroup) child, x, y);
                if (foundView != null && foundView.isShown()) {
                    return foundView;
                }
            } else {
                int[] location = new int[2];
                child.getLocationOnScreen(location);
                Rect rect = new Rect(location[0], location[1], location[0] + child.getWidth(), location[1] + child.getHeight());
                if (rect.contains(x, y)) {
                    return child;
                }
            }
        }
        return null;
    }

    protected void displayAppBarShadowIfNeed() {
        View shadow = findViewById(R.id.appBarShadow);
        if (shadow == null) return;
        shadow.setVisibility(Build.VERSION.SDK_INT < 21 ? View.VISIBLE : View.GONE);
    }

    public void setNeedHandleDispatchTouchEvent(boolean needHandleDispatchTouchEvent) {
        this.needHandleDispatchTouchEvent = needHandleDispatchTouchEvent;
    }

    protected void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(fragment.getClass().getSimpleName())
                .replace(R.id.containerFrame, fragment, fragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }

    protected void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (addToBackStack) fragmentTransaction = fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction
                .replace(R.id.containerFrame, fragment, fragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    protected void toastS(@StringRes int stringId) {
        Toast.makeText(this, getResources().getString(stringId), Toast.LENGTH_SHORT).show();
    }

    protected void toastS(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected void toastL(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleInternetDisabled() {

    }

    @Override
    public void showCommonLoader() {

    }

    @Override
    public void hideCommonLoader() {

    }

    @Override
    public void showError(String error) {
        toastL(error);
    }

    @Override
    public void showError(ApiError apiError) {

    }
}