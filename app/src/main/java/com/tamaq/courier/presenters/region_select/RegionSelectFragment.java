package com.tamaq.courier.presenters.region_select;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.CheckableSingleTextItemAdapter;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.HelperCommon;
import com.tamaq.courier.widgets.recycler_decorations.SpaceItemDecorationFirst;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RegionSelectFragment extends BaseFragment implements RegionSelectContract.View {

    @Inject
    RegionSelectContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    CheckableSingleTextItemAdapter<LocationRealm> adapter;

    private String selectedRegionKey;

    public static RegionSelectFragment newInstance(String selectedRegionId) {
        Bundle args = new Bundle();
        args.putString(RegionSelectActivity.ARG_SELECTED_REGION_KEY, selectedRegionId);
        RegionSelectFragment fragment = new RegionSelectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_region_select, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        setUpToolbar();
        setUpViews();
        parseArguments();
        presenter.loadRegions(selectedRegionKey);
        return rootView;
    }

    private void setUpToolbar() {
        initializeNavigationBar();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.region_select_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpViews() {
        adapter = new CheckableSingleTextItemAdapter<>(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpaceItemDecorationFirst(HelperCommon.dpToPx(8)));
        adapter.setClickListener(regionRealm -> {
            Intent intent = new Intent();
            intent.putExtra(RegionSelectActivity.RESULT_SELECTED_REGION_KEY, regionRealm.getIdUI());
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        });
    }

    private void parseArguments() {
        selectedRegionKey = getArguments().getString(RegionSelectActivity.ARG_SELECTED_REGION_KEY);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra(RegionSelectActivity.RESULT_SELECTED_REGION_KEY, selectedRegionKey);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        onNeedUpdateToolbarState(UserDAO.getInstance().isActiveStatus());
    }

    @Override
    public void onNeedUpdateToolbarState(boolean activeStatus) {
        super.onNeedUpdateToolbarState(activeStatus);
        getSupportActionBar().setHomeAsUpIndicator(activeStatus
                ? R.drawable.ic_arrow_back_white
                : R.drawable.ic_arrow_back_dark);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public void onRegionsLoaded(List<LocationRealm> list) {
        adapter.setObjects(list);
        if (selectedRegionKey != null && !selectedRegionKey.isEmpty()) {
            adapter.selectItemById(selectedRegionKey, false);
        }
    }
}
