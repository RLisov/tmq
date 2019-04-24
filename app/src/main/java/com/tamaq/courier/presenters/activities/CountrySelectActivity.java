package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.registration.country.CountryFragment;

public class CountrySelectActivity extends BaseActivity {

    public static final String RESULT_SELECTED_COUNTRY_ID = "result_selected_country_id";

    public static Intent newInstance(Context context, String selectedCountryKey) {
        Intent intent = new Intent(context, CountrySelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(CountryFragment.ARG_SELECTED_COUNTRY_ID, selectedCountryKey);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        CountryFragment countryFragment = CountryFragment.newInstance(
                getIntent().getStringExtra(CountryFragment.ARG_SELECTED_COUNTRY_ID),
                true);
        countryFragment.setCountrySelectedAction(country -> {
            Intent intent = new Intent();
            intent.putExtra(RESULT_SELECTED_COUNTRY_ID, country.getKey());
            setResult(RESULT_OK, intent);
            finish();
        });
        replaceFragment(countryFragment);
    }
}
