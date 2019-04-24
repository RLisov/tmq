package com.tamaq.courier.presenters.registration.city;


import android.app.SearchManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tamaq.courier.R;
import com.tamaq.courier.controllers.adapters.CityRecyclerAdapter;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.shared.TamaqApp;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

import static android.content.Context.SEARCH_SERVICE;


public class CityFragment extends BaseFragment implements CityContract.View {

    public static final String ARG_SELECTED_CITY_KEY = "arg_selected_city_id";
    public static final String ARG_COUNTRY_KEY = "arg_country_id";

    @Inject
    CityContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recycler;

    @BindView(R.id.emptyView)
    View emptyView;

    @BindView(R.id.loader)
    View loader;

    private CityRecyclerAdapter mAdapter;
    private Action1<LocationRealm> mCitySelectionAction;

    public CityFragment() {
    }

    public static CityFragment newInstance(String countryKey) {
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_CITY_KEY, "");
        args.putString(ARG_COUNTRY_KEY, countryKey);

        CityFragment fragment = new CityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CityFragment newInstance(String countryKey, String selectedCityKey) {
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_CITY_KEY, selectedCityKey);
        args.putString(ARG_COUNTRY_KEY, countryKey);

        CityFragment fragment = new CityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_recycler, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        setChangeToolbarColor(false);

        initializeNavigationBar();
        getSupportActionBar().setTitle(R.string.working_city);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_dark);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(false);

        String selectedCityKey = getArguments().getString(ARG_SELECTED_CITY_KEY);
        String countryKey = getArguments().getString(ARG_COUNTRY_KEY, "");
        loader.setVisibility(View.VISIBLE);
        presenter.loadCities(countryKey, selectedCityKey);
        return rootView;
    }

    @Override
    public void displayCities(List<LocationRealm> list) {
        mAdapter = new CityRecyclerAdapter(list, getContext());
        mAdapter.setListener(new CityRecyclerAdapter.Listener() {
            @Override
            public void onCityClick(LocationRealm cityRealm, int position) {
                if (mCitySelectionAction != null) mCitySelectionAction.call(cityRealm);
                presenter.selectCity(cityRealm, position);
            }

            @Override
            public void onSearchEmpty() {
                emptyView.setVisibility(View.VISIBLE);
                recycler.setVisibility(View.GONE);
            }

            @Override
            public void onSearchNotEmpty() {
                if (checkNotNull(emptyView) && checkNotNull(recycler)) {
                    emptyView.setVisibility(View.GONE);
                    recycler.setVisibility(View.VISIBLE);
                }
            }

        });
        recycler.setAdapter(mAdapter);
        loader.setVisibility(View.GONE);
    }

    @Override
    public void citySelected(int positionInList) {
        mAdapter.setSelected(positionInList);
        getFragmentManager().popBackStackImmediate();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint(getString(R.string.search_city));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.onSearchRequested(newText);
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStackImmediate();
                break;
        }
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

    public void setCitySelectedAction(Action1<LocationRealm> citySelectionAction) {
        mCitySelectionAction = citySelectionAction;
    }

}
