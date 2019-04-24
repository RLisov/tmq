package com.tamaq.courier.presenters.region_select;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;

public class RegionSelectActivity extends BaseActivity {

    public static final String ARG_SELECTED_REGION_KEY = "arg_selected_region_id";

    public static final String RESULT_SELECTED_REGION_KEY = "result_selected_region_id";

    public static Intent newInstance(Context context, String selectedRegionId) {
        Intent intent = new Intent(context, RegionSelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_SELECTED_REGION_KEY, selectedRegionId);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_select);
        replaceFragment(RegionSelectFragment.newInstance(
                getIntent().getStringExtra(ARG_SELECTED_REGION_KEY)));
    }
}
