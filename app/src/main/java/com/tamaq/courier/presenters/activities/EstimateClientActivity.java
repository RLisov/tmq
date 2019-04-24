package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.orders.estimate_client.EstimateClientFragment;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderFragment;

public class EstimateClientActivity extends BaseActivity {

    public static Intent newInstance(Context context, String orderId) {
        Intent intent = new Intent(context, EstimateClientActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewOrderFragment.ARG_ORDER_ID, orderId);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        replaceFragment(EstimateClientFragment.newInstance(
                getIntent().getStringExtra(EstimateClientFragment.ARG_ORDER_ID)));
    }

    @Override
    public void onBackPressed() {
        // Do not allow the user to leave the screen themselves
    }
}
