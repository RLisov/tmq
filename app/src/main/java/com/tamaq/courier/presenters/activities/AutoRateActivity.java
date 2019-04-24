package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.orders.auto_rate_params.AutoRateFragment;

public class AutoRateActivity extends BaseActivity {


    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, AutoRateActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        replaceFragment(AutoRateFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(
                AutoRateFragment.class.getSimpleName());
        if (fragment != null) {
            AutoRateFragment autoRateFragment = (AutoRateFragment) fragment;
            autoRateFragment.onBackPressed();
            return;
        }
        super.onBackPressed();
    }

}
