package com.tamaq.courier.presenters.base;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.tamaq.courier.R;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.model.api.response.ApiError;
import com.tamaq.courier.presenters.activities.SplashScreenActivity;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.HelperCommon;
import com.trello.rxlifecycle2.components.BuildConfig;
import com.trello.rxlifecycle2.components.support.RxFragment;

import butterknife.Unbinder;
import rx.functions.Action0;

public abstract class BaseFragment extends RxFragment implements BaseView {

    private static final String LOG_TAG = "BaseFragment";
    public Toolbar toolbar;
    public View rootView;
    protected Unbinder unbinder;
    protected View loader;
    protected Menu menu;
    private LoaderShowHelper loaderShowHelper;
    /**
     * Use this variable to automatically change the color of the toolbar depending
     * on whether the user has a confirmed mStatus or not
     * <p>
     * if true, then toolbar color will be changed in onStart
     */
    private boolean changeToolbarColor = true;
    private Action0 activeStatusChangeListener = () -> {
        if (toolbar != null) {
            boolean isActiveStatus = updateToolbarState();
            onNeedUpdateToolbarState(isActiveStatus);
        }
    };

    public BaseFragment() {
    }

    private LoaderShowHelper getLoaderShowHelper() {
        if (loaderShowHelper == null) loaderShowHelper = new LoaderShowHelper();
        return loaderShowHelper;
    }

    @Override
    public void handleInternetDisabled() {
        showError(getString(R.string.lost_network_connection));
    }

    @Override
    public void showError(String error) {
        hideCommonLoader();
        snackBarLong(rootView, error);
    }

    @Override
    public void hideCommonLoader() {
        if (loader != null) loader.setVisibility(View.GONE);
    }

    public void snackBarLong(View v, String message) {
        if (!TextUtils.isEmpty(message)) Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
    }

    protected void initializeNavigationBar() {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (toolbar != null) ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Nullable
    protected Toolbar getToolbar() {
        return toolbar;
    }

    public boolean checkNotNull(Object object) {
        return object != null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        onInjectDependencies(TamaqApp.get(getContext()).getAppComponent());
    }

    public void onInjectDependencies(AppComponent appComponent) {
    }

    @Override
    public Context getContext() {
        Context context = getAppCompatActivity();
        if (context == null) context = super.getContext();
        if (context == null) context = getActivity();
        return context;
    }

    protected AppCompatActivity getAppCompatActivity() {
        return ((AppCompatActivity) getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (changeToolbarColor && toolbar != null) updateToolbarState();
    }

    /**
     * Method is needed to update the mStatus of the toolbar depending on the user's mStatus (active / not active)
     *
     * @return true if user's mStatus is active, false otherwise
     */
    protected boolean updateToolbarState() {
        boolean isActiveStatus = UserDAO.getInstance().isActiveStatus();
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(),
                isActiveStatus ? R.color.dark_sky_blue : R.color.white));
        toolbar.setTitleTextColor(ContextCompat.getColor(getContext(),
                isActiveStatus ? R.color.white : R.color.black_87));
        return isActiveStatus;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
    }

    public void onNeedUpdateToolbarState(boolean activeStatus) {
    }

    protected ActionBar getSupportActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    public void toast(@StringRes int stringId) {
        Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT).show();
    }

    public void snackBar(View v, String message) {
        Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBar(View v, @StringRes int stringId) {
        Snackbar.make(v, stringId, Snackbar.LENGTH_SHORT).show();
    }

    public void snackBarLong(View v, @StringRes int stringId) {
        Snackbar.make(v, stringId, Snackbar.LENGTH_LONG).show();
    }

    public int getColor(@ColorRes int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    protected void replaceFragment(Fragment fragment, boolean addToBackStack) {
        HelperCommon.hideKeyboard(getActivity());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (addToBackStack)
            ft = ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.replace(R.id.containerFrame, fragment, fragment.getClass().getSimpleName())
                .commitAllowingStateLoss();
    }

    protected void replaceFragment(Fragment fragment) {
        HelperCommon.hideKeyboard(getActivity());
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(fragment.getClass().getSimpleName())
                .replace(R.id.containerFrame, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

//    @Override
//    public void handleInternetDisabled() {
//        showInternetDisabledSnack();
//    }

    protected void replaceFragment(Fragment fragment, FragmentManager fragmentManager) {
        try {
            HelperCommon.hideKeyboard(getActivity());
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .replace(R.id.containerFrame, fragment, fragment.getClass().getSimpleName())
                    .commit();
        } catch (IllegalStateException e) {
            if (BuildConfig.DEBUG) Log.e(LOG_TAG, "", e);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (rootView != null)
            loader = rootView.findViewById(R.id.loader);
        UserDAO.getInstance().addActiveStatusChangeListener(activeStatusChangeListener);
    }

    //    public void showInternetDisabledDialog() {
//        DialogHelper.showDialog(getActivity(),
//                R.string.error_unknown_host_title,
//                R.string.error_unknown_host_message,
//                R.string.ok_button_caption,
//                0,
//                null,
//                null);
//    }
//
//    public void showInternetDisabledSnack() {
//        snackBarLong(rootView, R.string.error_unknown_host_message);
//    }
//
//    private void displayAppBarShadowIfNeed() {
//        if (rootView == null)
//            return;
//        View shadow = rootView.findViewById(R.mId.appBarShadow);
//        if (shadow == null)
//            return;
//        shadow.setVisibility(Build.VERSION.SDK_INT < 21 ? View.VISIBLE : View.GONE);
//    }
//
    @Override
    public void onDestroyView() {
        UserDAO.getInstance().removeActiveStatusChangeListener(activeStatusChangeListener);
        super.onDestroyView();
    }

    @Override
    public void showCommonLoader() {
        if (loader != null) loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        loader = null;
        super.onDestroy();
    }

    /**
     * see {@link #changeToolbarColor}
     */
    public void setChangeToolbarColor(boolean changeToolbarColor) {
        this.changeToolbarColor = changeToolbarColor;
    }

    @Override
    public void showError(ApiError apiError) {
        hideCommonLoader();
        if (apiError.getHttpCode() == 401) {
            startActivity(SplashScreenActivity.newInstance(getContext(), SplashScreenActivity.ScreenToOpen.LOGIN));
            return;
        }
        snackBarLong(rootView, apiError.getErrorMessage());
    }

    public void onBackPressed() {

    }

    private class LoaderShowHelper {
        int loaderShowDelay = 500;

        Runnable loaderShowRunnable = getLoaderShowRunnable();
        Handler loaderShowHandler = new Handler();

        void showLoader() {
            loaderShowHandler.removeCallbacks(loaderShowRunnable);
            loaderShowRunnable = getLoaderShowRunnable();
            loaderShowHandler.postDelayed(loaderShowRunnable, loaderShowDelay);
        }

        Runnable getLoaderShowRunnable() {
            return () -> {
                if (loader != null) loader.setVisibility(View.VISIBLE);
            };
        }

        void hideLoader() {
            loaderShowHandler.removeCallbacks(loaderShowRunnable);
            if (loader != null) loader.setVisibility(View.GONE);
        }

        public void setLoaderShowDelay(int loaderShowDelay) {
            this.loaderShowDelay = loaderShowDelay;
        }
    }
}