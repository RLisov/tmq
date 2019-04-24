package com.tamaq.courier.presenters.tabs.chat;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;
import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.ChatRecyclerAdapter;
import com.tamaq.courier.controllers.service.MyFirebaseMessagingService;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.presenters.activities.ConcreteChatActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatTabFragment extends BaseFragment implements ChatTabContract.View {

    @Inject
    ChatTabContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.emptyStateLayout)
    View emptyStateLayout;
    @BindView(R.id.loader)
    View loader;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.accessDeniedLayout)
    View accessDeniedLayout;

    private ChatRecyclerAdapter mAdapter;
    private boolean isFragmentActive = true;

    public ChatTabFragment() {
    }

    public static ChatTabFragment newInstance() {
        return new ChatTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_chat_tab, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initializeNavigationBar();
        setChangeToolbarColor(false);

        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else getSupportActionBar().setTitle(R.string.chat_with_client);
        presenter.checkUserState();

        MyFirebaseMessagingService.resetChatFields();

        return rootView;
    }

    @Override
    public void onUserChecked(boolean active) {
        PrefsHelper.setUserBlocked(!active, getContext());
        if (active) {
            getSupportActionBar().setTitle(R.string.chat_with_client);
            initRecycler();
            presenter.loadDialogs();
            DialogDAO.getInstance().setMessageUpdatedActions(() -> {
                if (isFragmentActive) presenter.updateDialogs();
            });
        } else onUserBlocked();
    }

    private void onUserBlocked() {
        setUpToolbar(getString(R.string.access_denied));
        accessDeniedLayout.setVisibility(View.VISIBLE);
    }

    private void setUpToolbar(String title) {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void initRecycler() {
        if (mAdapter == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setNestedScrollingEnabled(false);

            mAdapter = new ChatRecyclerAdapter(getContext());

            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                presenter.manualUpdateMessages();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getContext() != null) swipeRefreshLayout.setRefreshing(false);
                }, 10000);
            });

            final SwipeToDismissTouchListener<RecyclerViewAdapter> touchListener =
                    new SwipeToDismissTouchListener<>(new RecyclerViewAdapter(recyclerView),
                            new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>() {


                                @Override
                                public boolean canDismiss(int position) {
                                    return true;
                                }

                                @Override
                                public void onPendingDismiss(RecyclerViewAdapter recyclerView, int position) {
                                }

                                @Override
                                public void onDismiss(RecyclerViewAdapter view, int position) {
                                    DialogRealm dialog = mAdapter.getDialog(position);
                                    mAdapter.removeItemByPosition(position);
                                    presenter.deleteDialog(dialog.getChatId());
                                    if (mAdapter.getItemCount() == 0) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
                                        });
                                    }
                                }
                            });

            touchListener.setDismissDelay(15000);

            recyclerView.setOnTouchListener(touchListener);
            recyclerView.addOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
            recyclerView.addOnItemTouchListener(new SwipeableItemClickListener(getContext(),
                    (view, position) -> {
                        switch (view.getId()) {
                            case R.id.deleteTextView:
                                touchListener.processPendingDismisses();
                                break;

                            case R.id.undoTextView:
                                touchListener.undoPendingDismiss();
                                break;

                            default:
                                DialogRealm dialog = mAdapter.getDialog(position);
                                startActivity(ConcreteChatActivity.newInstance(
                                        getContext(),
                                        dialog.getChatId(), dialog.getUserName(), dialog.isOrderCompleted()));
                                break;
                        }
                    }));
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onStart() {
        if (PrefsHelper.isUserBlocked(getContext())) onUserBlocked();
        else {
            presenter.updateBadge();
            presenter.loadDialogs();
        }
        isFragmentActive = true;
        PrefsHelper.saveChatScreenIsActive(getContext(), true);
        super.onStart();
    }

    @Override
    public void onPause() {
        isFragmentActive = false;
        PrefsHelper.saveChatScreenIsActive(getContext(), false);
        super.onPause();
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

    @Override
    public void displayDialogs(List<DialogRealm> dialogRealmList) {
        emptyStateLayout.setVisibility(View.GONE);
        if (mAdapter != null) mAdapter.setObjects(dialogRealmList);
        else initRecycler();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void displayNoDialogs() {
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayLoader() {
        loader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        loader.setVisibility(View.GONE);
    }
}
