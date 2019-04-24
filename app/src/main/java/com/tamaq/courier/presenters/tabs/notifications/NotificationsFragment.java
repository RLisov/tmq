package com.tamaq.courier.presenters.tabs.notifications;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.NotificationsRecyclerAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.notifications.notification_details.NotificationDetailsFragment;
import com.tamaq.courier.utils.PrefsHelper;
import com.tamaq.courier.utils.SearchMenuHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsFragment extends BaseFragment implements NotificationsContract.View {

    @Inject
    NotificationsContract.Presenter presenter;

    @BindView(R.id.loader)
    View loader;
    @BindView(R.id.emptyStateLayout)
    View emptyStateLayout;
    @BindView(R.id.emptyView)
    View emptySearchView;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.accessDeniedLayout)
    View accessDeniedLayout;

    private NotificationsRecyclerAdapter mAdapter;

    private boolean mIsNeedShowSearchIcon;
    private boolean mIsNeedEnableSearchIcon;

    public NotificationsFragment() {
    }

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
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
        rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        initializeNavigationBar();
        setChangeToolbarColor(false);


        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            getSupportActionBar().setTitle(getString(R.string.title_notification));
            initRecycler();

            loader.setVisibility(View.VISIBLE);
            presenter.loadNotifications();
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

    private void setUpToolbar(String title) {
        initializeNavigationBar();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
    }

    private void initRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new NotificationsRecyclerAdapter(getContext());
        mAdapter.setListener(new NotificationsRecyclerAdapter.Listener() {
            @Override
            public void onItemClick(String notificationId) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.dark_sky_blue)));
                replaceFragment(NotificationDetailsFragment.newInstance(notificationId));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu_white_icon, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchMenuHelper searchMenuHelper = new SearchMenuHelper(getActivity(), menuItem,
                getString(R.string.search_with_ellipsis));

        searchMenuHelper.showIcon(mIsNeedShowSearchIcon);
        searchMenuHelper.enableIcon(mIsNeedEnableSearchIcon);

        searchMenuHelper.setOnActionExpandListener(isExpand ->
                toolbar.setBackgroundColor(getColor(isExpand ? R.color.white : R.color.dark_sky_blue)));

        searchMenuHelper.setTextChangeListener(s -> {
            if (mAdapter != null) mAdapter.onSearchRequested(s);
        });
    }

    @Override
    public void displayNotifications(List<NotificationRealm> list) {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.setObjects(list);
        loader.setVisibility(View.GONE);
        displaySearchIcon(true);
    }

    @Override
    public void displayNoNotification() {
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

    private void displaySearchIcon(boolean needDisplay) {
        mIsNeedShowSearchIcon = needDisplay;
        if (getActivity() != null) getActivity().invalidateOptionsMenu();
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
