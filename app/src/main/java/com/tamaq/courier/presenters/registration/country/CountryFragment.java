package com.tamaq.courier.presenters.registration.country;


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
import com.tamaq.courier.controllers.adapters.CountryRecyclerAdapter;
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


public class CountryFragment extends BaseFragment implements CountryContract.View {

    public static final String ARG_SELECTED_COUNTRY_ID = "arg_selected_country_id";
    public static final String ARG_IS_IN_ACTIVITY = "arg_is_in_activity";

    @Inject
    CountryContract.Presenter presenter;

    @BindView(R.id.recycler)
    RecyclerView recycler;

    @BindView(R.id.emptyView)
    View emptyView;

    private CountryRecyclerAdapter mAdapter;
    private Action1<LocationRealm> countrySelectedAction;

    public CountryFragment() {
    }

    public static CountryFragment newInstance(String selectedCountryKey) {
        return newInstance(selectedCountryKey, false);
    }

    public static CountryFragment newInstance(String selectedCountryKey, boolean isInActivity) {
        CountryFragment fragment = new CountryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SELECTED_COUNTRY_ID, selectedCountryKey);
        bundle.putBoolean(ARG_IS_IN_ACTIVITY, isInActivity);
        fragment.setArguments(bundle);
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
        getSupportActionBar().setTitle(R.string.country);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_dark);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(false);

        presenter.loadCountries(getArguments().getString(ARG_SELECTED_COUNTRY_ID));
        return rootView;
    }

    @Override
    public void displayCountries(List<LocationRealm> locationRealmList) {
        mAdapter = new CountryRecyclerAdapter(locationRealmList, getContext());
        mAdapter.setListener(new CountryRecyclerAdapter.Listener() {
            @Override
            public void onCountryClick(LocationRealm locationRealm, int position) {
                if (countrySelectedAction != null)
                    countrySelectedAction.call(locationRealm);
                mAdapter.setSelected(position);
                if (!getArguments().getBoolean(ARG_IS_IN_ACTIVITY))
                    getFragmentManager().popBackStackImmediate();
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint(getString(R.string.country_search_hint));

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
                if (!getArguments().getBoolean(ARG_IS_IN_ACTIVITY))
                    getFragmentManager().popBackStackImmediate();
                else
                    getActivity().finish();
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

    public void setCountrySelectedAction(Action1<LocationRealm> countrySelectedAction) {
        this.countrySelectedAction = countrySelectedAction;
    }
}
